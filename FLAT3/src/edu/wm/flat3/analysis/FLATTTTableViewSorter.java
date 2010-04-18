/**
 * 
 */
package edu.wm.flat3.analysis;

import java.util.Comparator;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

class FLATTTTableViewSorter extends ViewerSorter {
	
	
	private boolean autoSort=false;
	private Object treeOrTable;
	private class SortInfo {
		int columnIndex;

		Comparator comparator;

		boolean descending;
	}

	private StructuredViewer viewer;

	private SortInfo[] infos;

	public FLATTTTableViewSorter(StructuredViewer viewer,
			Item[] columns, Comparator[] comparators,Object treeOrTable) {
		
		this.viewer = viewer;
		
		this.treeOrTable=treeOrTable;
		
		infos = new SortInfo[columns.length];
		
		for (int i = 0; i < columns.length; i++) {
			infos[i] = new SortInfo();
			infos[i].columnIndex = i;
			infos[i].comparator = comparators[i];
			infos[i].descending = false;
			createSelectionListener(columns[i], infos[i]);
		}
		
		// TODO: set default sort column properly
		infos[2].descending = false;
		this.sortUsing(infos[2]);
		columns[2].notifyListeners(SWT.Selection, new Event());
	}

	public void setAutoSort(boolean autoSortOn) {
		this.autoSort=autoSortOn;
	}
	
	public boolean getAutoSort() {
		return this.autoSort;
	}
	
	public int compare(Viewer viewer, Object node1, Object node2) {
		//if (actions.autoSort==false) return 0;
		for (int i = 0; i < infos.length; i++) {
			int result = infos[i].comparator.compare(node1, node2);
			if (result != 0) {
				if (infos[i].descending)
					return -result;
				return result;
			}
		}
		return 0;
	}

	private void createSelectionListener(final Item column, final SortInfo info) {
		column.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				
				sortUsing(info);

				// THIS IS PROBABLY A HACK; how it it supposed to work? sort never gets called?
				((TableViewContentProvider)viewer.getContentProvider()).refreshTable();
				
				if (treeOrTable instanceof Tree) {
					Item columnn = ((Tree) treeOrTable).getSortColumn();
					int dir = ((Tree) treeOrTable).getSortDirection();
					
					if (column instanceof TreeColumn) {
						if (column != columnn)
							((Tree) treeOrTable)
									.setSortColumn((TreeColumn) column);
						else {if (dir == SWT.DOWN)
								dir = SWT.UP;
							else
								dir = SWT.DOWN;
							((Tree) treeOrTable).setSortDirection(dir);
						}
						
					}
				} else if (treeOrTable instanceof Table) {
					Item columnn = ((Table) treeOrTable).getSortColumn();
					int dir = ((Table) treeOrTable).getSortDirection();
					if (column instanceof TableColumn) {
						if (column != columnn)
							((Table) treeOrTable)
									.setSortColumn((TableColumn) column);
						else {
							if (dir == SWT.DOWN)
								dir = SWT.UP;
							else
								dir = SWT.DOWN;
							((Table) treeOrTable).setSortDirection(dir);
						}
					}
				}

			}

		});
		viewer.refresh();
//		 ((TableColumn) column).addSelectionListener(
//		 new SelectionAdapter() {
//		 public void widgetSelected(SelectionEvent e) {
//				sortUsing(info);
//			}
//		});
	}

	public void sortUsing(SortInfo info) {
		
		if (info == infos[0])
			info.descending = !info.descending;
		else {
			for (int i = 0; i < infos.length; i++) {
				if (info == infos[i]) {
					System.arraycopy(infos, 0, infos, 1, i);
					infos[0] = info;
					info.descending = false;
					break;
				}
			}
		}
		
		viewer.setSorter(null);
		viewer.setSorter(this);
		
	}
	
	public void sort(final Viewer viewer, Object[] elements) {
		super.sort(viewer, elements);
	}
	 
	
	public boolean isSorterProperty(Object element, String property) {
	
		if (!this.getAutoSort()) return false;
		if (property==null) return false;
		Item columnn=null;
		
		if (treeOrTable instanceof Tree) {
			columnn = ((Tree) treeOrTable).getSortColumn();
		} else if (treeOrTable instanceof Table) {
			columnn = ((Table) treeOrTable).getSortColumn();
		}
		
		if (columnn==null)	return false;
		if (columnn.getText()==null) return false;
		if (columnn.getText().compareTo(property)==0) return true;
		
		return false;
    }
	
}