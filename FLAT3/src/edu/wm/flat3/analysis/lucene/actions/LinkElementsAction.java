package edu.wm.flat3.analysis.lucene.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.MultiElementAction;
import edu.wm.flat3.actions.NewConcernAction;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernListener;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.util.ConcernJob;
//import edu.wm.flattt.actions.LinkElementsAction.UpdateDropDownMenusRunner;

public class LinkElementsAction extends Action
		implements 
		//IActionDelegate,			// For selection change notification
		//IEditorActionDelegate,		// To capture java element selection in the editor 
		IMenuCreator,				// Allows us to create the drop down menu
		IConcernListener			// Refresh menu when concerns and links change
{
	protected LinkMenuItemListener clickListener = new LinkMenuItemListener();
	
	private Menu menu;					// Drop down menu (we must dispose ourselves)
	protected IEditorPart aJavaEditor;	// For handling selection in the Editor
	protected List<IJavaElement> selectedJavaElements = // Currently selected items 
		new ArrayList<IJavaElement>(); 
	protected IConcernModelProvider concernModelProvider; // Concern model we are dealing with
	

	/**
	 * Populate the dynamic menu
	 */ 
	protected void fillMenu(Menu parent, List<Concern> concerns)
	{
		assert parent != null;
		assert !parent.isDisposed();

	//	if (!selectedJavaElements.isEmpty())
		//{
			parent.setEnabled(true);
			
			fillMenuRecursive(parent, concerns, selectedJavaElements,
					concernModelProvider.getLinkType());

			String linkAllLabel = getNewConcernMenuItemText();
			
			for(MenuItem menuItem : parent.getItems())
			{
				String text = menuItem.getText();
				
				if (text.isEmpty())
				{
					menuItem.dispose();
				}
				else if (text.equals(linkAllLabel))
				{
					menuItem.dispose();
					break;
				}
			}

			// If there were concerns in the model, add a separator before
			// the New Concern item
			
			boolean hasItemsToLink = parent.getItemCount() > 0;
			
			if (hasItemsToLink)
			{
				new MenuItem(parent, SWT.SEPARATOR);
			}
			
			// Add the "New concern..." item
			MenuItem lNewConcernItem = new MenuItem(parent, SWT.PUSH);
			lNewConcernItem.addSelectionListener(clickListener);
			lNewConcernItem.setText(linkAllLabel);
	/*	}
		else
		{
			parent.setEnabled(false);
		}*/
	}

	/**
	 * Builds the cascading 'Link' menu
	 */
	private void fillMenuRecursive(	Menu parent, 
									List<Concern> concerns,
									List<IJavaElement> selectedJavaElements,
									EdgeKind concernComponentRelation)
	{
		Set<Character> mnemonicsUsed = new HashSet<Character>(); 
		
		for (Concern concern : concerns)
		{
			// See if any of the selected elements are already
			// linked to the concern
			//boolean isLinked = isLinked(concern, selectedJavaElements, 
			//		linkType);
			boolean isLinked = false; // Inaccurate but much faster
			
			List<Concern> children = concern.getChildren();

			MenuItem lMenuItem = null;
			Menu childMenu = null;

			// For the 'Link' menu, create menu items for all concerns,
			// regardless of whether they are linked.
			
			// See if we already created the item
			for(MenuItem menuItem : parent.getItems())
			{
				Object data = menuItem.getData();
				if (data != null && 
					data.equals(concern))
				{
					lMenuItem = menuItem;
					childMenu = lMenuItem.getMenu();
					break;
				}
			}

			// Lazily create the concern menu item
			if (lMenuItem == null)
			{
				if (!children.isEmpty())
				{
					lMenuItem = new MenuItem(parent, SWT.CASCADE);
				}
				else
				{
					lMenuItem = new MenuItem(parent, SWT.PUSH);

					// Can't click on cascading menu
					lMenuItem.addSelectionListener(clickListener);
				}

				lMenuItem.setData(concern);
				lMenuItem.setText(getConcernNameWithMnemonic(concern, mnemonicsUsed));
			}

			lMenuItem.setEnabled(!children.isEmpty() || !isLinked);
			
			if (!children.isEmpty())
			{
				// The 'Link' menu is hierarchical, so we create
				// a cascading menu for the children
				
				assert lMenuItem != null;
				
				if (childMenu == null)
				{
					childMenu = new Menu(lMenuItem);
				}
				
				fillMenuRecursive(childMenu, children, selectedJavaElements, 
						concernComponentRelation);

				lMenuItem.setMenu(childMenu);
			}
			
			children = null; // Helps GC
		}
		
		mnemonicsUsed = null; // Helps GC
	}

	private class LinkMenuItemListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent pEvent)
		{
			MenuItem menuItemClicked = (MenuItem) pEvent.widget;
			assert menuItemClicked != null; 
			
			Concern targetConcern = null;
			
			Object data = menuItemClicked.getData();
			if (data == null)
			{
				Shell shell;
				if (aJavaEditor != null)
					shell = aJavaEditor.getSite().getShell();
				else
					shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				NewConcernAction nca = new NewConcernAction(shell, 
						concernModelProvider, concernModelProvider.getModel().getRoot());
				nca.run();
				
				targetConcern = nca.getConcernJustAdded();
			}
			else
			{
				targetConcern = (Concern) data;
				assert targetConcern != null;
			}

			ConcernJob job = new ConcernJob("Linking", concernModelProvider);
			
			EdgeKind concernComponentRelation = 
				concernModelProvider.getLinkType();
			
			// Get selected elements in FLATTTTableView and add them...

			getSelectedJavaElements(aJavaEditor, FLATTT.tableView.getViewer().getSelection(), selectedJavaElements);
//			getStructuredSelection(FLATTT.tableView.getViewer().getSelection(), aJavaEditor);
			for(IJavaElement javaElement : selectedJavaElements)
			{
				job.addLinkTask(javaElement, targetConcern, concernComponentRelation);
			}

			job.schedule();
		}
	}
	
	public LinkElementsAction()
	{
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/link.png"));
		
		setText(
				FLATTT.getResourceString("actions.LinkElementsAction.ToolTip"));

		
		// We want to be notified when the active concern model changes
		ConcernModelFactory.singleton().addListener(this);

		concernModelProvider = ConcernModelFactory.singleton();

		// We want to be notified when any concerns or links are
		// changed in the active concern model
		concernModelProvider.getModel().addListener(this);
		
	}

	//-----------------------------------------------------
	// IActionDelegate implementation
	//-----------------------------------------------------
    
	// Never called because the action becomes a menu.
	public void run(IAction pAction)
	{ 
		System.out.println();
	}

	// Keeps track of selection changes in Package Explorer, Editor,
	// etc.
	public void selectionChanged(IAction pAction, ISelection pSelection)
	{
		pAction.setMenuCreator(this);
		
		getSelectedJavaElements(aJavaEditor, pSelection, selectedJavaElements);

		// Force the menus to be redrawn since we enable/disable
		// concern menu items based on the selected element
		refresh(null);
	}

	//-----------------------------------------------------
	// IEditorActionDelegate implementation
	//-----------------------------------------------------
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor)
	{
		// Keep track of the editor so we can capture selection changes
		aJavaEditor = targetEditor;
	}

	//-----------------------------------------------------
	// IMenuCreator implementation
	//-----------------------------------------------------
	
	public void dispose()
	{
		if (menu != null && !menu.isDisposed())
		{
			menu.dispose();
			menu = null;
		}
	}

	public Menu getMenu(Control parent)
	{
		return null;
	}
	
	public Menu getMenu(Menu parent)
	{
		dispose();

		menu = new Menu(parent);
		fillMenu(menu, concernModelProvider.getModel().getRoot().getChildren());
		return menu;
	}
	
	public Menu getMenu(MenuManager mng)
	{
		dispose();
		menu = mng.getMenu();
		fillMenu(menu, concernModelProvider.getModel().getRoot().getChildren());
		return menu;
	}

	//-----------------------------------------------------
	// ConcernModelChangeListener implementation
	//-----------------------------------------------------

	/*
	 * Refresh menu when concerns and links change
	 */
	@Override
	public void modelChanged(ConcernEvent events)
	{
		if (events.isChangedDomainName())
			return;
		
		if (events.isChangedActiveConcernModel())
		{
			concernModelProvider.getModel().removeListener(this);
			concernModelProvider = ConcernModelFactory.singleton();

			// We want to be notified when any concerns or links are
			// changed in the active concern model
			concernModelProvider.getModel().addListener(this);
		}

		boolean hasLinkOrUnlink = false;
		
		for(ConcernEvent event : events)
		{
			if (event.isLinked() || event.isUnlinked())
			{
				hasLinkOrUnlink = true;
				break;
			}
		}

		if (hasLinkOrUnlink)
			Display.getDefault().asyncExec(new UpdateDropDownMenusRunner(events));
	}
	
	//-----------------------------------------------------
	// HELPER METHODS
	//-----------------------------------------------------

	private void refresh(ConcernEvent events)
	{
		if (menu != null)
		{
			fillMenu(menu, concernModelProvider.getModel().getRoot().getChildren());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static boolean getSelectedJavaElements(	IWorkbenchPart workbenchPart,
	                                              	ISelection selection,
	                                              	Collection<IJavaElement> selectedJavaElements)
	{
		selectedJavaElements.clear();
		
		if (selection == null)
			return false;
		
		IStructuredSelection structuredSelection = getStructuredSelection(
				selection, workbenchPart);
		if (structuredSelection == null)
			return false;

		boolean isEditor = workbenchPart instanceof JavaEditor;
		boolean isSearch = false;
		if (workbenchPart != null && 
			workbenchPart.getClass() != null &&
			workbenchPart.getClass().getName() != null)
		{
			isSearch = workbenchPart.getClass().getName().endsWith("SearchView");
		}
		
		Iterator lI = structuredSelection.iterator();
		while (lI.hasNext())
		{
			Object selectionObj = lI.next();
			if (!(selectionObj instanceof IJavaElement) && !(selectionObj instanceof FLATTTMember))
			{
				selectedJavaElements.clear();
				return true; // User selected something that isn't a java element
			}
			
			IJavaElement element;
			
			if (selectionObj instanceof FLATTTMember)
				element = ((FLATTTMember)selectionObj).getNodeIMember();
			else
				element = Component.validateAndConvertJavaElement((IJavaElement) selectionObj);
			
			if (element == null)
			{
				selectedJavaElements.clear();
				return false; // We only support methods, fields, and types
			}
			
			selectedJavaElements.add(element);
		}
		
		if ((isEditor || isSearch) && selectedJavaElements.isEmpty())
			return true;	// Ignore this selection
		else
			return false;	// Don't ignore the selection (may be empty)
	}
	
	private static IStructuredSelection getStructuredSelection(	ISelection selection,
	                                                            IWorkbenchPart workbenchPart)
	{
		if (selection == null)
			return null;
		
		IStructuredSelection structuredSelection = null;

		if (selection instanceof IStructuredSelection)
		{
			structuredSelection = (IStructuredSelection) selection;
		}
		else if (workbenchPart != null)
		{
			try
			{
				structuredSelection = SelectionConverter.getStructuredSelection(workbenchPart);
			}
			catch (JavaModelException e)
			{
				return null;
			}
		}

		return structuredSelection;
	}


	protected static boolean isLinked(Concern concern, 
	                                    List<IJavaElement> javaElements,
	                                    EdgeKind concernComponentRelation)
	{
		// See if any of the selected elements are already
		// linked to the concern
		for(IJavaElement javaElement : javaElements)
		{
			if (concern.isLinked(javaElement, concernComponentRelation))
			{
				return true;
			}
		}
		
		return false;
	}
	
	protected static String getConcernNameWithMnemonic(	Concern concern, 
														Set<Character> mnemonicsAlreadyUsed)
	{
		String concernName = concern.getDisplayName();
		assert concernName != null && !concernName.isEmpty();

		if (concernName.indexOf('&') >= 0)
			return concernName;
		
		char[] buf = concernName.toCharArray(); 

		char[] newBuf = new char[buf.length + 1]; // Add one for the ampersand

		for(int i = 0, newIndex = 0; i < buf.length; ++i, ++newIndex)
		{
			char c = buf[i];
			
			if ((newIndex == i) &&
				Character.isLetter(c) &&
				mnemonicsAlreadyUsed.add(c)) // Returns true if mnemonic doesn't exist
			{
				newBuf[newIndex++] = '&';
			}
			
			newBuf[newIndex] = c;
		}
		
		return new String(newBuf);
	}
	
	protected static String getNewConcernMenuItemText()
	{
		return FLATTT.getResourceString("actions.EditorLinkAction.NewConcern");
	}
	
	private final class UpdateDropDownMenusRunner implements Runnable
	{
		ConcernEvent events;
		
		public UpdateDropDownMenusRunner(ConcernEvent events)
		{
			this.events = events;
		}
		
		@Override
		public void run()
		{
			refresh(events);
		}
	}

	public IMenuManager getMenuManager() {
		MenuManager menMgr = new MenuManager("Link");
		Menu men = menMgr.createContextMenu(FLATTT.tableView.getViewer().getControl());
		getMenu(menMgr);

		return menMgr;
	}
}
