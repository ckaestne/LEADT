package edu.wm.flat3.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.ConcernRepository;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.util.ConcernJob;

public class LinkElementsAction extends MultiElementAction
{
	protected LinkMenuItemListener clickListener = new LinkMenuItemListener();

	/**
	 * Populate the dynamic menu
	 */ 
	@Override
	protected void fillMenu(Menu parent, List<Concern> concerns)
	{
		assert parent != null;
		assert !parent.isDisposed();

		if (!selectedJavaElements.isEmpty())
		{
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
		}
		else
		{
			parent.setEnabled(false);
		}
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
			
			// PATCH: This could probably be sent upstream to ConcernMapper too
			// what if elements are from 2 different projects?
			// Now, verify we have a code model, otherwise we can't add the elements to the new concern:
			if (!targetConcern.verifyCodeModelExists(selectedJavaElements.get(0)))
				return;
			
			ConcernJob job = new ConcernJob("Linking", concernModelProvider);
			
			EdgeKind concernComponentRelation = 
				concernModelProvider.getLinkType();
			
			for(IJavaElement javaElement : selectedJavaElements)
			{
				job.addLinkTask(javaElement, targetConcern, concernComponentRelation);
			}

			job.schedule();
		}
	}
}
