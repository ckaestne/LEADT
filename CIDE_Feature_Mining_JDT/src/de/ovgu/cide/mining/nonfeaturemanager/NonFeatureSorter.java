package de.ovgu.cide.mining.nonfeaturemanager;

import java.util.Comparator;


import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TreeColumn;

import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode;

public class NonFeatureSorter extends ViewerSorter{
	
	private class SortInfo {
		int columnIndex;
		Comparator<NonFeatureTreeNode> comparator;
		boolean descending;
	}
	
	private TreeViewer viewer;
	private SortInfo[] infos;
	
	public NonFeatureSorter(TreeViewer viewer, TreeColumn[] columns, Comparator<NonFeatureTreeNode>[] comparators) {
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
			int result = infos[i].comparator.compare((NonFeatureTreeNode)entry1,(NonFeatureTreeNode)entry2);
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

