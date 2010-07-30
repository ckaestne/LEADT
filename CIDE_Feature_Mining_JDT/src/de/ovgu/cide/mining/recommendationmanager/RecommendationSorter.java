package de.ovgu.cide.mining.recommendationmanager;

import java.util.Comparator;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableColumn;

import de.ovgu.cide.mining.recommendationmanager.RecommendationManagerView.Recommendation;

public class RecommendationSorter extends ViewerSorter {

	private class SortInfo {
		int columnIndex;
		Comparator<Recommendation> comparator;
		boolean descending;
	}

	private TableViewer viewer;
	private SortInfo[] infos;

	public RecommendationSorter(TableViewer viewer, TableColumn[] columns,
			Comparator<Recommendation>[] comparators) {
		this.viewer = viewer;
		infos = new SortInfo[columns.length];

		for (int i = 0; i < columns.length; i++) {
			infos[i] = new SortInfo();
			infos[i].columnIndex = i;
			infos[i].comparator = comparators[i];
			infos[i].descending = false;
			createSelectionListener(columns[i], infos[i]);
		}

		// setDefault
		sortUsing(infos[1]);
		sortUsing(infos[2]);
		sortUsing(infos[0]);

	}

	public int compare(Viewer viewer, Object entry1, Object entry2) {
		for (int i = 0; i < infos.length; i++) {
			int result = infos[i].comparator.compare(
					(Recommendation) entry1,
					(Recommendation) entry2);
			if (result != 0) {
				if (infos[i].descending)
					return -result;
				return result;
			}
		}
		return 0;
	}

	private void createSelectionListener(final TableColumn column,
			final SortInfo info) {
		column.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				sortUsing(info);
				System.out.println("SORTING: " + info.columnIndex);

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	protected void sortUsing(SortInfo info) {
		if (info == infos[0]) {
			info.descending = !info.descending;
		} else {
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
