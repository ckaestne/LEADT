/**
 * 
 */
package edu.wm.flat3.analysis;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernListener;
import edu.wm.flat3.model.IConcernModelProvider;

import java.util.ArrayList;



public class TableViewContentProvider implements IStructuredContentProvider, IConcernListener  {

	/**
	 * 
	 */
	private FLATTTTableView view;
	private TableViewer viewer;
	//private JRipplesEIG EIG;
	private Object imageChangeListener;
	private Object[] list;
	
	
	protected IConcernModelProvider concernModelProvider; // Concern model we are dealing with
	
	
	/**
	 * @param table
	 */
	TableViewContentProvider(FLATTTTableView view) {
		// We want to be notified when the active concern model changes
		ConcernModelFactory.singleton().addListener(this);

		concernModelProvider = ConcernModelFactory.singleton();

		// We want to be notified when any concerns or links are
		// changed in the active concern model
		concernModelProvider.getModel().addListener(this);
		
		
		this.view = view;
		this.viewer = view.getViewer();
		/*this.imageChangeListener= new NodeImageRegistry.NodesImageChangedListener() {

			public void handleNodesImageChangeEvent(final NodesImageChangedEvent evt) {
				try {
					if ((viewer == null))
						return;
					if ((viewer.getTable() == null))
						return;
					if (Thread.currentThread().getName().compareTo("main") == 0) {
						viewer.update(evt.getNodes().toArray(), new String[] {FLATTTViewsConstants.SHORT_NAME_COLUMN_TITLE});
					}
					else {
						PlatformUI.getWorkbench().getDisplay().syncExec(
								new Runnable() {
									public void run() {
										TableViewContentProvider.this.view.setFocus();
										viewer.update(evt.getNodes().toArray(), new String[] {FLATTTViewsConstants.SHORT_NAME_COLUMN_TITLE});
					
									}
								});
					}
				} catch (Exception e) {
					JRipplesLog.logError(e);
				}
				
			
				
				
			}
			
		};*/
		//NodeImageRegistry.addNodesImageChangedListener((NodeImageRegistry.NodesImageChangedListener)this.imageChangeListener);
	}

	

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) v;
		
	/*	if (EIG != null)
			JRipplesEIG.removeJRipplesEIGListener(this);
		EIG = (JRipplesEIG) newInput;
		if (EIG != null)
			{JRipplesEIG.addJRipplesEIGListener(this);
			 
			}
		*/
		
	}

	public void dispose() {
		//if (resourceSelectionTracker!=null) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages()[0].removeSelectionListener(resourceSelectionTracker);
	//	JRipplesEIG.removeJRipplesEIGListener(this);
	//	NodeImageRegistry.removeNodesImageChangedListener((NodeImageRegistry.NodesImageChangedListener)this.imageChangeListener);
	}

	public Object[] getElements(Object parent) {
		if (FLATTT.searchResults != null) {
			Object[] array = FLATTT.searchResults.toArray();
			return array;//FLATTT.searchResults.toArray();
		} else {
			return null;
		}
	}

	public void refreshTable() {
	
		if ((list != null) && (list.length != 0)) {
			// scroll to the top, otherwise it's really slow for some reason
			viewer.reveal(list[0]);
//			for ( int i = 0; i < list.length; i++) {
//				
//				Object member = list[i];
//				viewer.remove(list[i]);
//			}	
			viewer.remove(list);  // revert to other way?
			 // viewer.remove hangs forever on win7 and eclipse 3.5??
		}
		Object[] elements = getElements(null);


		if ((elements != null) && (elements.length != 0)) {
			viewer.getSorter().sort(viewer, elements);
			viewer.add(elements);
			
			view.updateToolbarButtons();
		//viewer.update((Object[])getElements(null), new String[] {FLATTTViewsConstants.SHORT_NAME_COLUMN_TITLE});
		}
		
		list = elements;
		// update botton enabled statuses
		//FLATTT.tableView
		
	}

	public void modelChangedUpdate(ConcernEvent events) {
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
			if (list != null)
				viewer.update(list, new String[] {FLATTTViewsConstants.FEATURE_COLUMN_TITLE});
	}

	@Override
	public void modelChanged(final ConcernEvent events)
	{
		Display.getDefault().syncExec (new Runnable () {
			public void run () {
				modelChangedUpdate(events);
			}
		});

	}
	
}