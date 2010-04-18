package edu.wm.flat3.ui.concerntree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.actions.JavaSearchActionGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.MoveConcernsToTopAction;
import edu.wm.flat3.actions.MultiConcernAction;
import edu.wm.flat3.actions.NewConcernAction;
import edu.wm.flat3.actions.RemoveMultipleItemsAction;
import edu.wm.flat3.actions.RenameConcernAction;
import edu.wm.flat3.actions.RevealInEditorAction;
import edu.wm.flat3.analysis.visualization.RightClickVisualizeAction;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;

public class ConcernTreeViewer
	extends TreeViewer
	implements IConcernModelProvider
{
	private IConcernModelProvider concernModelProvider;

	private IViewPart viewPart;

	private IStatusLineManager statusLineManager;

	private ConcernTreeLabelProvider labelProvider;

	private JavaSearchActionGroup javaSearchActions;

	private MultiConcernAction linkAction;
	private MultiConcernAction unlinkAction;
	private RightClickVisualizeAction visualizeAction;

	static private final Object NO_UPDATE = new Object();
	static private final Object RECREATE = new Object();
	static private final Object REFRESH = new Object();
	
	static private boolean debug = true;
	
	/**
	 * Enables highlighting of concerns in concern tree based on the Java element currently selected in Project Explorer, Editor, etc.
	 */
	private static final boolean highlightPackageExplorerSelection = true;

	public ConcernTreeViewer(Composite parent,
			IConcernModelProvider concernModelProvider, IViewPart viewPart,
			IViewSite viewSite)
	{
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		this.concernModelProvider = concernModelProvider;
		this.viewPart = viewPart;

		this.statusLineManager = viewSite.getActionBars().getStatusLineManager();

		// Specify our layout

		GridData lGridData = new GridData();
		lGridData.verticalAlignment = GridData.FILL;
		lGridData.horizontalAlignment = GridData.FILL;
		lGridData.grabExcessHorizontalSpace = true;
		lGridData.grabExcessVerticalSpace = true;

		getControl().setLayoutData(lGridData);

		// Specify a custom content provider that provides a
		// combined tree showing concerns and their links

		ConcernTreeContentProvider contentProvider = new ConcernTreeContentProvider(
				concernModelProvider);
		setContentProvider(contentProvider);

		// Specify custom sort order for concerns and links

		setSorter(new ConcernTreeSorter());

		// Specify a custom label provider that can highlight
		// tree items based on Java elements selected in other
		// views (Package Explorer, Editor, Search Results, etc.)

		this.labelProvider = new ConcernTreeLabelProvider(this,
				highlightPackageExplorerSelection, statusLineManager);
		setLabelProvider(labelProvider);

		// Listen for selection changes in Package Explorer, Editor, etc.
		if (highlightPackageExplorerSelection)
			viewPart.getSite().getPage().addSelectionListener(labelProvider);

		// Initialize right-mouse menu item that allows selected items from
		// other views/editors to be linked to multiple selected concerns

		this.linkAction = new MultiConcernAction(this, true);
		this.unlinkAction = new MultiConcernAction(this, false);
		this.visualizeAction = new RightClickVisualizeAction(viewPart.getSite());

		// Listen for selection changes in Package Explorer, Editor, etc.
		viewPart.getSite().getPage().addSelectionListener(linkAction);
		viewPart.getSite().getPage().addSelectionListener(unlinkAction);

		// Broadcast selection changes to other editors, views, etc.
		viewPart.getSite().setSelectionProvider(this);

		// Handle drag-n-drop
		hookDragAndDrop(concernModelProvider);

		// Listen for the DELETE key
		hookDELETEKey(viewSite);

		// Listen for double click events
		hookDoubleClick();

		// Specify our custom right-mouse menu
		hookContextMenu();
		
		FLATTT.treeView = this;
	}

	public void init(Composite pParent)
	{
		Composite lBottomFrame = new Composite(pParent, 0);
		lBottomFrame.setLayout(new GridLayout(2, false));
		lBottomFrame.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));
	}

	// ----------------------------------------------------
	// IConcernModelProvider implementation
	// ----------------------------------------------------

	@Override
	public ConcernModel getModel()
	{
		return concernModelProvider.getModel();
	}

	@Override
	public EdgeKind getLinkType()
	{
		return concernModelProvider.getLinkType();
	}

	// ----------------------------------------------------
	// PUBLIC METHODS
	// ----------------------------------------------------

	@SuppressWarnings("unchecked")
	public List<ConcernTreeItem> getSelectedItems()
	{
		List<ConcernTreeItem> selectedConcernTreeItems = new ArrayList<ConcernTreeItem>();

		ISelection selection = getSelection();
		if (!(selection instanceof IStructuredSelection))
			return null;

		IStructuredSelection selectedConcernItems = (IStructuredSelection) selection;

		Iterator selectionIter = selectedConcernItems.iterator();
		while (selectionIter.hasNext())
		{
			selectedConcernTreeItems.add((ConcernTreeItem) selectionIter.next());
		}

		return selectedConcernTreeItems;
	}

	public void refresh(ConcernEvent event)
	{
		Display lDisplay = getControl().getDisplay();

		// Setting the input must be done asynchronously.
		// see:
		// http://docs.jboss.org/jbosside/cookbook/build/en/html/Example6.html#d0e996
		lDisplay.asyncExec(new RefreshRunner(event));
	}

	public void setFocus()
	{
		getTree().setFocus();
	}

	public void dispose()
	{
		if (highlightPackageExplorerSelection)
			viewPart.getSite().getPage().removeSelectionListener(labelProvider);

		if (javaSearchActions != null)
		{
			javaSearchActions.dispose();
			javaSearchActions = null;
		}

		if (labelProvider != null)
		{
			labelProvider.dispose();
			labelProvider = null;
		}
	}

	// ----------------------------------------------------
	// PRIVATE HELPER METHODS
	// ----------------------------------------------------

	private void hookDragAndDrop(IConcernModelProvider concernModelProvider)
	{
		// Initializes the tree viewer to support drop actions

		int lOps = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] lTransfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		addDropSupport(lOps, lTransfers, new ConcernTreeDropAdapter(
				concernModelProvider, this));

		// Permit any ConcernTreeItem to be a drag source
		addDragSupport(lOps, lTransfers, new DragSourceAdapter());

		// Listen for our own selection changes

		this.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				// LocalSelectionTransfer is a kind of 'data channel' used
				// by all the views to pass selected objects from the view
				// to the drop target.

				// Update the data channel with the currently selected objects
				LocalSelectionTransfer.getTransfer().setSelection(
						getSelection());

				List<ConcernTreeItem> selectedItems = getSelectedItems();
				statusLineManager.setMessage(selectedItems.size() +
						" concern tree item" +
						(selectedItems.size() == 1 ? "" : "s") + " selected");

			}
		});
	}

	private void hookDELETEKey(IViewSite viewSite)
	{
		IActionBars actionBars = viewSite.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
				new Action()
				{
					@Override
					public void run()
					{
						RemoveMultipleItemsAction removeAction = new RemoveMultipleItemsAction(
								ConcernTreeViewer.this);
						removeAction.addItemsToRemove(getSelectedItems());
						removeAction.run();
					}
				});
	}

	private void hookDoubleClick()
	{
		addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent pEvent)
			{
				List<ConcernTreeItem> selectedConcernItems = getSelectedItems();

				if (selectedConcernItems.size() != 1)
					return; // Even possible?

				IJavaElement javaElement = selectedConcernItems.get(0).getJavaElement();

				if (javaElement != null)
					(new RevealInEditorAction(javaElement)).run();
			}
		});
	}

	/**
	 * Registers the context menu on the view.
	 */
	private void hookContextMenu()
	{
		MenuManager lMenuManager = new MenuManager("#PopupMenu");
		lMenuManager.setRemoveAllWhenShown(true);
		lMenuManager.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager pManager)
			{
				fillContextMenu(pManager);
			}
		});

		Menu lMenu = lMenuManager.createContextMenu(getControl());
		getControl().setMenu(lMenu);
		viewPart.getSite().registerContextMenu(lMenuManager, this);
	}

	/**
	 * Fills the context menu based on the type of selection.
	 * 
	 * @param pManager
	 */
	private void fillContextMenu(IMenuManager pManager)
	{
		List<ConcernTreeItem> selectedConcernItems = getSelectedItems();

		boolean allSelectedItemsAreConcerns = !selectedConcernItems.isEmpty();

		for (ConcernTreeItem item : selectedConcernItems)
		{
			if (item.getJavaElement() != null)
			{
				allSelectedItemsAreConcerns = false;
				break;
			}
		}

		addMenuItem_Visualize(pManager, selectedConcernItems,
				allSelectedItemsAreConcerns);
		addMenuItem_LinkOrUnlink(pManager, selectedConcernItems,
				allSelectedItemsAreConcerns);
		addMenuItem_NewConcern(pManager, selectedConcernItems,
				allSelectedItemsAreConcerns);
		addMenuItem_MoveToTop(pManager, selectedConcernItems,
				allSelectedItemsAreConcerns);
		addMenuItem_Rename(pManager, selectedConcernItems,
				allSelectedItemsAreConcerns);
		addMenuItem_JavaElements(pManager, selectedConcernItems,
				allSelectedItemsAreConcerns);
		addMenuItem_Remove(pManager, selectedConcernItems);

		// What is this for?
		pManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void addMenuItem_Visualize(IMenuManager pManager, List<ConcernTreeItem> selectedConcernItems, boolean allSelectedItemsAreConcerns)
	{
		// Can only link to concern items
		if (!allSelectedItemsAreConcerns || selectedConcernItems.size() == 0)
			return;

		/*linkAction.clearConcerns();

		for (ConcernTreeItem item : selectedConcernItems)
		{
			assert item.getJavaElement() == null;
			linkAction.addConcernItem(item);
		}

		// Make sure the user has selected some Java elements in
		// Package Explorer, Editor, etc.
		linkAction.retainOnlyActionableElements();

		pManager.add(linkAction);

		unlinkAction.clearConcerns();

		for (ConcernTreeItem item : selectedConcernItems)
		{
			assert item.getJavaElement() == null;
			unlinkAction.addConcernItem(item);
		}

		// Make sure the user has selected some Java elements in
		// Package Explorer, Editor, etc.
		unlinkAction.retainOnlyActionableElements();*/

		visualizeAction.setSelection(selectedConcernItems);
		pManager.add(visualizeAction);
	}

	private void addMenuItem_LinkOrUnlink(IMenuManager pManager, List<ConcernTreeItem> selectedConcernItems, boolean allSelectedItemsAreConcerns)
	{
		// Can only link to concern items
		if (!allSelectedItemsAreConcerns || selectedConcernItems.size() == 0)
			return;

		linkAction.clearConcerns();

		for (ConcernTreeItem item : selectedConcernItems)
		{
			assert item.getJavaElement() == null;
			linkAction.addConcernItem(item);
		}

		// Make sure the user has selected some Java elements in
		// Package Explorer, Editor, etc.
		linkAction.retainOnlyActionableElements();

		pManager.add(linkAction);

		unlinkAction.clearConcerns();

		for (ConcernTreeItem item : selectedConcernItems)
		{
			assert item.getJavaElement() == null;
			unlinkAction.addConcernItem(item);
		}

		// Make sure the user has selected some Java elements in
		// Package Explorer, Editor, etc.
		unlinkAction.retainOnlyActionableElements();

		pManager.add(unlinkAction);
	}

	private void addMenuItem_NewConcern(IMenuManager pManager, List<ConcernTreeItem> selectedConcernItems, boolean allSelectedItemsAreConcerns)
	{
		if (selectedConcernItems.size() == 0)
		{
			// User right-clicked on empty space in the tree
			pManager.add(new NewConcernAction(this.getTree().getShell(), this,
					getModel().getRoot()));
		}
		else if (allSelectedItemsAreConcerns &&
				selectedConcernItems.size() == 1)
		{
			// User right-clicked on a concern
			Concern selectedConcern = selectedConcernItems.get(0).getConcern();
			pManager.add(new NewConcernAction(this.getTree().getShell(), this,
					selectedConcern));
		}
	}

	private void addMenuItem_MoveToTop(IMenuManager pManager, List<ConcernTreeItem> selectedConcernItems, boolean allSelectedItemsAreConcerns)
	{
		if (!allSelectedItemsAreConcerns || selectedConcernItems.isEmpty())
			return; // No items to move to the top

		MoveConcernsToTopAction action = new MoveConcernsToTopAction(this);

		for (ConcernTreeItem cti : selectedConcernItems)
		{
			Concern concern = cti.getConcern();

			if (cti.getJavaElement() == null && concern.getParent() != null &&
					!concern.getParent().isRoot())
			{
				action.addConcern(concern);
			}
		}

		action.setEnabled(action.hasWork());

		pManager.add(action);
	}

	private void addMenuItem_Remove(IMenuManager pManager, List<ConcernTreeItem> selectedConcernItems)
	{
		if (selectedConcernItems.size() == 0)
			return; // No items to remove

		RemoveMultipleItemsAction removeAction = new RemoveMultipleItemsAction(
				concernModelProvider);
		removeAction.addItemsToRemove(selectedConcernItems);
		pManager.add(removeAction);
	}

	private void addMenuItem_Rename(IMenuManager pManager, List<ConcernTreeItem> selectedConcernItems, boolean allSelectedItemsAreConcerns)
	{
		// Can only rename a single selected concern
		if (!allSelectedItemsAreConcerns || selectedConcernItems.size() != 1)
			return;

		ConcernTreeItem cti = selectedConcernItems.get(0);

		pManager.add(new RenameConcernAction(this, cti.getConcern()));
	}

	private void addMenuItem_JavaElements(IMenuManager pManager, List<ConcernTreeItem> selectedConcernItems, boolean allSelectedItemsAreConcerns)
	{
		if (javaSearchActions != null)
		{
			javaSearchActions.dispose();
			javaSearchActions = null;
		}

		if (allSelectedItemsAreConcerns || selectedConcernItems.size() != 1)
			return;

		ConcernTreeItem cti = selectedConcernItems.get(0);

		IJavaElement javaElement = cti.getJavaElement();

		// We provide the context menu for searching

		javaSearchActions = new JavaSearchActionGroup(viewPart);

		javaSearchActions.setContext(new ActionContext(new StructuredSelection(
				javaElement)));
		GroupMarker lSearchGroup = new GroupMarker("group.search");
		pManager.add(lSearchGroup);
		javaSearchActions.fillContextMenu(pManager);
		javaSearchActions.setContext(null);
		pManager.remove(lSearchGroup);

		// They selected a single type or member
		pManager.add(new RevealInEditorAction(javaElement));
	}

	private final class RefreshRunner
		implements Runnable
	{
		ConcernEvent events;

		public RefreshRunner(ConcernEvent events)
		{
			this.events = events;
		}

		public void run()
		{
			if (getControl() == null || getControl().isDisposed())
				return;

			// make sure the tree still exists
			if (getInput() == null)
			{
				setInput(concernModelProvider);
			}
			else if (events.isChangedLinkType())
			{
				// Force the label provider to update its cache of selected elements
				// and the concerns they are linked to (otherwise, when we refresh,
				// the highlighting will be wrong)
				labelProvider.updateSelectedConcerns();

				// Force a redraw of the tree and labels
				ConcernTreeViewer.super.refresh();
			}
			else if (events.isChangedAllConcerns() ||
					events.chainIncludesRootConcern())
			{
				if (debug)
					System.out.println("Refreshing entire concern tree... (" +
							concernModelProvider.getLinkType() + ")");

				// Force the label provider to update its cache of selected elements
				// and the concerns they are linked to (otherwise, when we refresh,
				// the highlighting will be wrong). Removed: causes an exception.
				// labelProvider.refresh();

				// The currently selected item may have been removed
				// so clear it
				// ConcernTreeViewer.super.setSelection((ISelection) null);

				// Force a redraw of the tree and labels
				ConcernTreeViewer.super.refresh();
			}
			else
			{
				if (debug)
					System.out.println("Refreshing concern tree... (" +
							concernModelProvider.getLinkType() + ")");

				// Force the label provider to update its cache of selected elements
				// and the concerns they are linked to (otherwise, when we refresh
				// the highlighting will be wrong)
				labelProvider.updateSelectedConcerns();

				// The currently selected item may have been removed
				// so clear it
				// ConcernTreeViewer.super.setSelection((ISelection) null);

				clearWidgetsAffectedByEvents(getControl());
				markWidgetsAffectedByEvents(getControl(), events);
				updateWidgetsAffectedByEvents(getControl());
			}
		}

		private void clearWidgetsAffectedByEvents(Widget widget)
		{
			widget.setData("update", NO_UPDATE);
			for(Widget child : getChildren(widget))
			{
				clearWidgetsAffectedByEvents(child);
			}
		}
		
		private void markWidgetsAffectedByEvents(Widget widget, ConcernEvent events)
		{
			if (widget.getData() instanceof ConcernTreeItem)
			{
				ConcernTreeItem cti = (ConcernTreeItem) widget.getData();
				for(ConcernEvent event : events)
				{
					if (!event.matches(cti))
						continue;

					if (event.isLinked() || 
						event.isUnlinked() ||
						event.isChangedConcernChildren())
					{
						// Links have changed so we need to recreate the
						// concern item (i.e, refetch the links)
						
						widget.setData("update", RECREATE);

						// Every item from here to the top level needs its
						// label refreshed
						
						Item current = getParentItem((Item) widget);
						while (current != null)
						{
							current.setData("update", REFRESH);
							current = getParentItem(current);
						}
						
						return; // Don't need to mark children since they'll be recreated
					}
					else if (	event.isUpdateConcernLabel() ||
								event.isUpdateElementLabel())
					{
						// Every item from here to the top level needs its
						// label refreshed
						
						Item current = (Item) widget;
						while (current != null)
						{
							current.setData("update", REFRESH);
							current = getParentItem(current);
						}
					}
				}
			}
			
			for(Widget child : getChildren(widget))
			{
				markWidgetsAffectedByEvents(child, events);
			}
		}

		private void updateWidgetsAffectedByEvents(Widget widget)
		{
			Object updateInfo = widget.getData("update");
			if (updateInfo == RECREATE)
			{
				if (debug)
					System.out.println("Recreating: " +	widget.getData());

				internalRefresh(widget, widget.getData(), true, true);
				return; // Skip children since they will automatically be recreated
			}
			else if (updateInfo == REFRESH)
			{
				if (debug)
					System.out.println("Refreshing: " +	widget.getData());

				doUpdateItem((Item) widget, widget.getData());
			}

			for(Widget child : getChildren(widget))
			{
				updateWidgetsAffectedByEvents(child);
			}
		}
	}
}
