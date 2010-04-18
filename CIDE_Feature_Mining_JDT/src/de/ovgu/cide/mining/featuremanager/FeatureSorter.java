package de.ovgu.cide.mining.featuremanager;

import java.util.Comparator;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TreeColumn;

import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode;

public class FeatureSorter extends ViewerSorter{
	
	private class SortInfo {
		int columnIndex;
		Comparator<FeatureTreeNode> comparator;
		boolean descending;
	}
	
	private TreeViewer viewer;
	private SortInfo[] infos;
	
	public FeatureSorter(TreeViewer viewer, TreeColumn[] columns, Comparator<FeatureTreeNode>[] comparators) {
		this.viewer = viewer;
		infos = new SortInfo[columns.length];
		
		for (int i = 0; i < columns.length; i++) {
			infos[i] = new SortInfo();
			infos[i].columnIndex = i;
			infos[i].comparator = comparators[i];
			infos[i].descending = false;
			createSelectionListener(columns[i], infos[i]);
			
			
		}
	}
	
	public int compare(Viewer viewer, Object entry1, Object entry2) {
		for (int i = 0; i < infos.length; i++) {
			int result = infos[i].comparator.compare((FeatureTreeNode)entry1,(FeatureTreeNode)entry2);
			if (result != 0) {
				if (infos[i].descending)
					return -result;
				return result;
			}
		}
		return 0;
	}
	
	private void createSelectionListener(final TreeColumn column, final SortInfo info) {
		column.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				sortUsing(info);
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	protected void sortUsing(SortInfo info) {
		if (info == infos[0]) {
			info.descending = !info.descending;
		}
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
		viewer.refresh();
	}

}

