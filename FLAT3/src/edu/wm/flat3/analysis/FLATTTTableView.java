package edu.wm.flat3.analysis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.plaf.ToolBarUI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.util.LocalSelectionTransfer;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.CollapseAllAction;
import edu.wm.flat3.actions.MultiConcernAction;
import edu.wm.flat3.actions.NewConcernAction;
import edu.wm.flat3.actions.OpenSearchViewAction;
import edu.wm.flat3.actions.RevealInEditorAction;
import edu.wm.flat3.analysis.lucene.actions.LinkElementsAction;
import edu.wm.flat3.analysis.lucene.actions.UnlinkElementsAction;
import edu.wm.flat3.analysis.mutt.actions.ClearCombinationalSearchAction;
import edu.wm.flat3.analysis.mutt.actions.CombinationalSearch;
import edu.wm.flat3.analysis.mutt.actions.ExportTraceAction;
import edu.wm.flat3.analysis.mutt.actions.ImportTraceAction;
import edu.wm.flat3.analysis.mutt.actions.StartTraceAction;
import edu.wm.flat3.analysis.visualization.VisualizeAction;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.ui.concerntree.ConcernTreeItem;
//import edu.wm.flattt.ui.concerntree.ConcernTreeDropAdapter;


public class FLATTTTableView extends ViewPart  implements IShowInTarget,IShowInSource  {
	private TableViewer viewer;

	//private JRipplesEIG EIG;

	
	private FLATTTTableViewSorter sorter;

	private TableColumn iconColumn;
	private TableColumn nameColumn;
	private TableColumn probabilityColumn;
	private TableColumn markColumn;
	private TableColumn fullNameColumn;
	private TableColumn featureColumn;

	public Action importAction = null;
	public Action exportAction = null;
	public Action combinationalAction = null;
	public Action clearCombinationalAction = null;
	public Action visualizeAction = null;
	
	//protected Actions actions;
	
	
	
	private Menu contextualMenu;

	private void createTableSorter() {
		
		Comparators comp=new Comparators();
		
		sorter = new FLATTTTableViewSorter(
				viewer, 
				// TODO: mroe comparators
				new TableColumn[] {iconColumn, nameColumn, probabilityColumn, markColumn, fullNameColumn, featureColumn },
				new Comparator[] {comp.getNameComparator(), comp.getNameComparator(), comp.getProbabilityComparator(),comp.getMarkComparator(), comp.getFullyQualifiedNameComparator(), comp.getNameComparator()},
				viewer.getTable()
				);
		
		viewer.setSorter(sorter);
	}
	/**
	 * This is a callback that allows us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
	//	EIG = JRipplesEIG.getEIG();
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION );//| SWT.VIRTUAL);
		
		viewer.setContentProvider(new TableViewContentProvider(this));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setUseHashlookup(true);
		
		getSite().setSelectionProvider(viewer);
		
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		createColumns(table);
		
		table.setSortColumn(nameColumn);
		table.setSortDirection(SWT.DOWN);
		this.createTableSorter();	
		
		// Add elements to the action bars
		IActionBars lBars = getViewSite().getActionBars();
		fillLocalToolBar(lBars.getToolBarManager());
		//fillToolBarMenu(lBars.getMenuManager());
		//actions=new Actions(this, viewer);
		//actions.hookUpActions();
		//actions.hookContextMenuForNonHierarchicalView();
		hookUpHelp();
		
		FLATTT.tableView = this;
		//viewer.setInput(EIG);
		
		// Specify our custom right-mouse menu
		hookContextMenu();
		
		// Handle double clicks too
		hookDoubleClick();
		
		// Handle drag-n-drop
		hookDragAndDrop();
		
		//this.linkAction = new MultiConcernAction(FLATTT.treeView, true);
		//this.unlinkAction = new MultiConcernAction(FLATTT.treeView, false);
	}
	
	
	
	
	private void hookUpHelp() {
			//PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),JRipplesViewsConstants.HELP_JRIPPLES_TABLE_VIEW);
	}
	

	private void createColumns(Table table) {
		iconColumn = new TableColumn(table, SWT.LEFT);
		iconColumn.setWidth(30);
		
		nameColumn = new TableColumn(table, SWT.LEFT);
		nameColumn.setText(FLATTTViewsConstants.SHORT_NAME_COLUMN_TITLE);
		nameColumn.setWidth(100);
		

		markColumn = new TableColumn(table, SWT.LEFT);
		markColumn.setText(FLATTTViewsConstants.CLASS_COLUMN_TITLE);
		markColumn.setWidth(100);
		

		probabilityColumn = new TableColumn(table, SWT.LEFT);
		probabilityColumn.setWidth(75);
		probabilityColumn.setText(FLATTTViewsConstants.PROBABILITY_COLUMN_TITLE);
		/*if (ModuleProxy.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_ANALYSIS)!=null) {
			probabilityTitle=((JRipplesAnalysisModuleInterface)ModuleProxy.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_ANALYSIS)).getUnitsTitle();
		} 
		if (probabilityTitle==null) probabilityColumn.setText(FLATTTViewsConstants.PROBABILITY_COLUMN_TITLE);
			else probabilityColumn.setText(probabilityTitle);
		ModuleProxy.addJRipplesModuleSwitchingListener(this);*/

		fullNameColumn = new TableColumn(table, SWT.LEFT);
		fullNameColumn.setText(FLATTTViewsConstants.FULL_NAME_COLUMN_TITLE);
		fullNameColumn.setWidth(300);

		featureColumn = new TableColumn(table, SWT.LEFT);
		featureColumn.setText(FLATTTViewsConstants.FEATURE_COLUMN_TITLE);
		featureColumn.setWidth(100);
		
	}
	
	/**
	 * Adds the action to the toolbar.
	 * 
	 * @param pManager
	 *            The toolbar manager.
	 */
	private void fillLocalToolBar(IToolBarManager pManager)
	{
		// TODO: New search button on this toolbar too?
		combinationalAction = new CombinationalSearch();
		clearCombinationalAction = new ClearCombinationalSearchAction();
		pManager.add(combinationalAction);
		pManager.add(clearCombinationalAction);
		
		pManager.add(new Separator());
		exportAction = new ExportTraceAction(this);
		importAction = new ImportTraceAction(this);
		pManager.add(exportAction);
		pManager.add(importAction);
		
		pManager.add(new Separator());
		visualizeAction = new VisualizeAction(getSite());
		pManager.add(visualizeAction);
	}

	public void updateToolbarButtons() {
		if (combinationalAction != null) { // if buttons have been created already
			combinationalAction.setEnabled((FLATTT.searchResults != null) && (FLATTT.searchResults.size() > 0));
			visualizeAction.setEnabled((FLATTT.searchResults != null) && (FLATTT.searchResults.size() > 0));
			
			clearCombinationalAction.setEnabled(FLATTT.searchResultsAreCombinational);
			exportAction.setEnabled(FLATTT.searchResultsAreTrace);		
		}
	}
	

	

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (viewer.getControl().isDisposed()) return;
		viewer.getControl().setFocus();
	}

	public void dispose() {
		super.dispose();
		//actions.dispose();
		//ModuleProxy.removeJRipplesModuleSwitchingListener(this);
		//ModuleProxy.deactivateModule(JRipplesDefaultModulesConstants.MODULE_VIEW_TABLE_TITLE, JRipplesModuleInterface.CONTROLLER_TYPE_SELF);
	}
	
	public List<FLATTTMember> getSelectedItems()
	{
		List<FLATTTMember> selectedConcernTreeItems = new ArrayList<FLATTTMember>();

		ISelection selection = viewer.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return null;

		IStructuredSelection selectedConcernItems = (IStructuredSelection) selection;

		Iterator selectionIter = selectedConcernItems.iterator();
		while (selectionIter.hasNext())
		{
			selectedConcernTreeItems.add((FLATTTMember) selectionIter.next());
		}

		return selectedConcernTreeItems;
	}
	
	private void hookDoubleClick()
	{
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent pEvent)
			{
				List<FLATTTMember> selectedConcernItems = getSelectedItems();

				if (selectedConcernItems.size() != 1)
					return; // Even possible?

				IJavaElement javaElement = selectedConcernItems.get(0).getNodeIMember();

				if (javaElement != null)
					(new RevealInEditorAction(javaElement)).run();
			}
		});
	}
	
	private void hookDragAndDrop()//IConcernModelProvider concernModelProvider)
	{
		// Initializes the tree viewer to support drop actions

		int lOps = DND.DROP_MOVE;
		Transfer[] lTransfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		// Permit any ConcernTreeItem to be a drag source
		viewer.addDragSupport(lOps, lTransfers, new DragSourceListener() {
				   public void dragStart(DragSourceEvent event) {
					      // Only start the drag if there is actually text in the
					      // label - this text will be what is dropped on the target.
					    //  if (dragLabel.getText().length() == 0) {
					     //     event.doit = false;
					     // }
					     LocalSelectionTransfer.getTransfer().setSelection(
									viewer.getSelection());
					   }
					   public void dragSetData(DragSourceEvent event) {
					     // Provide the data of the requested type.
					     if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
					          event.data = viewer.getSelection();
					     }
					   //  LocalSelectionTransfer.getTransfer().setSelection(
						//			viewer.getSelection());
					   }
					   public void dragFinished(DragSourceEvent event) {
					     // If a move operation has been performed, remove the data
					     // from the source
					   //  if (event.detail == DND.DROP_MOVE)
					   //      dragLabel.setText("");
					    // }
					   }});

		// Listen for our own selection changes

	/*	viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				// LocalSelectionTransfer is a kind of 'data channel' used
				// by all the views to pass selected objects from the view
				// to the drop target.

				// Update the data channel with the currently selected objects
				Object sel = getSelectedItems();
				LocalSelectionTransfer.getTransfer().setSelection(
						viewer.getSelection());
				Object test = viewer.getSelection();
				Object test2 = null;
				// after one drag operation, this data becomes stale...
				// TODO: stop from becoming stale or refresh it when it does
				
				//List<FLATTTMember> selectedItems = getSelectedItems();
				//statusLineManager.setMessage(selectedItems.size() +
				//		" concern tree item" +
				//		(selectedItems.size() == 1 ? "" : "s") + " selected");

			}
		});*/
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

		Menu lMenu = lMenuManager.createContextMenu(viewer.getControl());
		contextualMenu = lMenu;
		viewer.getControl().setMenu(lMenu);
		getSite().registerContextMenu(lMenuManager, viewer);
	}
	
	/**
	 * Fills the context menu based on the type of selection.
	 * 
	 * @param pManager
	 */
	private void fillContextMenu(IMenuManager pManager)
	{
		/*linkAction.clearConcerns();

		for (ConcernTreeItem item : FLATTT.treeView.getSelectedItems())
		{
			assert item.getJavaElement() == null;
			linkAction.addConcernItem(item);
		}

		// Make sure the user has selected some Java elements in
		// Package Explorer, Editor, etc.
		linkAction.retainOnlyActionableElements();

		pManager.add(linkAction);

		unlinkAction.clearConcerns();

		for (ConcernTreeItem item :  FLATTT.treeView.getSelectedItems())
		{
			assert item.getJavaElement() == null;
			unlinkAction.addConcernItem(item);
		}

		// Make sure the user has selected some Java elements in
		// Package Explorer, Editor, etc.
		unlinkAction.retainOnlyActionableElements();

		pManager.add(unlinkAction);*/
		LinkElementsAction link = new LinkElementsAction();
		//link.getMenu(contextualMenu);
		link.setMenuCreator(link);
		pManager.add(link);
		
		
		UnlinkElementsAction unlink = new UnlinkElementsAction();
		//link.getMenu(contextualMenu);
		unlink.setMenuCreator(unlink);
		pManager.add(unlink);
	//	pManager.
	}


	
	@Override
	public boolean show(ShowInContext context) {

		if (viewer == null || context == null)
	         return false;
	      ISelection sel = context.getSelection();
	      if (sel instanceof IStructuredSelection) {
	         IStructuredSelection ss = (IStructuredSelection)sel;
	         Object first = ss.getFirstElement();
	         if (first instanceof IMember) {
	     //   	 if (JRipplesEIG.getNode((IMember)first)==null) return false;
	        	 try { 
	            //  viewer.setSelection(new StructuredSelection(JRipplesEIG.getNode((IMember)first)), true);
	        	 } catch (Exception e) {
	        		 return false;
	        	 }
	            return true;
	         }
	      }
	      return false;
	}
	@Override
	public ShowInContext getShowInContext() {
		//TODO - figure out
		if ((2+2)==4)return null;
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection)
				.getFirstElement();
		if (obj==null) return null;
		//if (((JRipplesEIGNode) obj).getNodeIMember() != null)
			try {
		//		IMember member=((JRipplesEIGNode) obj)
		//		.getNodeIMember();
				//return new ShowInContext(null, new StructuredSelection(member));
				return new ShowInContext(null, viewer.getSelection());
				//ICompilationUnit cu = member.getCompilationUnit();
		        //IEditorPart javaEditor = JavaUI.openInEditor(cu);
		        //JavaUI.revealInEditor(javaEditor, (IJavaElement)member);
				
			} catch (Exception e) {
				return null;
			}
		// return null;
		 

	}
	
	public TableViewer getViewer() {
		return viewer;
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this))
			return this;
		if (adapter.isInstance(IShowInSource.class)) {
			
			ISelection selection = viewer.getSelection();
			Object obj = ((IStructuredSelection) selection)
					.getFirstElement();
			if (obj==null) return null;
			/*if (((JRipplesEIGNode) obj).getNodeIMember() != null)
				try {
					IMember member=((JRipplesEIGNode) obj)
					.getNodeIMember();
					return new ShowInContext(null, new StructuredSelection(member));
					//ICompilationUnit cu = member.getCompilationUnit();
			        //IEditorPart javaEditor = JavaUI.openInEditor(cu);
			        //JavaUI.revealInEditor(javaEditor, (IJavaElement)member);
					
				} catch (Exception e) {
					return null;
				}*/
		}
				
		return Platform.getAdapterManager().getAdapter(this, adapter);
	
	}

	
}
