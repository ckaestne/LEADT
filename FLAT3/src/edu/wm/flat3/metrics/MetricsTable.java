package edu.wm.flat3.metrics;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.util.ConcernWithSectionComparator;

public abstract class MetricsTable
	extends
		ViewerSorter
	implements 
		ITableLabelProvider, 
		IContentProvider, 
		IStructuredContentProvider
{
	private Table table;
	private TableViewer viewer;
	private ColumnInfo[] columns;

	private int sortColumn = 0;
	
	protected Map<Object, DisplayableValues> allMetrics = 
		new HashMap<Object, DisplayableValues>();
	
	public MetricsTable(ColumnInfo[] columns)
	{
		this.columns = columns;
	}
	
	public void initialize(Composite parent)
	{
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
			| SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		table = new Table(parent, style);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] columnNames = new String[columns.length];
		for(int columnIndex = 0; 
			columnIndex < columns.length; 
			++columnIndex)
		{
			final int index = columnIndex;
			
			columnNames[columnIndex] = columns[columnIndex].title;
			
			TableColumn column = new TableColumn(table, 
					columns[columnIndex].style);
			column.setText(columns[columnIndex].title);
			column.setWidth(columns[columnIndex].width);
			column.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					if (index == sortColumn)
					{
						// Clicked twice on the same column so
						// reverse the sort direction
						columns[index].reverseSortDirection();
					}
					
					sortColumn = index;
					table.setSortColumn(table.getColumn(sortColumn));
					table.setSortDirection(columns[index].direction);
					viewer.refresh();
				}
			}
			);
		}
		
		viewer = new TableViewer(table);
		viewer.setColumnProperties(columnNames);
		viewer.setContentProvider(this);
		viewer.setLabelProvider(this);
		viewer.setSorter(this);
	}

	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	public void refresh()
	{
		Display display = safeGetDisplay();
		if (display == null)
			return;
		
		// Once we are finished, we need to refresh the display.
		// However, this must be done on the UI thread.
		display.asyncExec(new Runnable() 
			{
				public void run() 
				{
					viewer.setInput(this);
					viewer.refresh();
				}
			}
		);
	}
	
	/**
	 * For outputting to the console or saving to a file
	 */
	public void output(PrintStream out)
	{
		StringBuffer buf = new StringBuffer();

		for(ColumnInfo columnInfo : columns)
		{
			if (buf.length() > 0)
				buf.append(',');
			
			buf.append(columnInfo.title);
		}
		
		out.println(buf.toString());
		outputRows(out);
	}
	
	/**
	 * Note: Synchronized to prevent ConcurrentModificationException
	 * when methods are called re-entrantly 
	 */
	public synchronized void clear()
	{
		allMetrics.clear();
	}

	// ----------------------------------------------------
	// Abstract methods
	// ----------------------------------------------------
	
	abstract protected void outputRows(PrintStream out);

	// ----------------------------------------------------
	// Null IBaseLabelProvider implementation
	// ----------------------------------------------------
	
	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void addListener(ILabelProviderListener listener) 
	{ /*Do Nothing*/ }

	@Override
	public void removeListener(ILabelProviderListener listener)	
	{ /*Do Nothing*/ }
	
	@Override
	public void dispose() 
	{ /*Do Nothing*/ }

	// ----------------------------------------------------
	// ITableLabelProvider implementation
	// ----------------------------------------------------
	
	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	/**
	 * Note: Synchronized to prevent ConcurrentModificationException
	 * when methods are called re-entrantly 
	 */
	@Override
	public synchronized String getColumnText(Object element, int columnIndex)
	{
		if (columnIndex == 0)
		{
			if (element instanceof Concern)
			{
				Concern concern = (Concern) element;
				return concern.getDisplayName();
			}
			else
			{
				return element.toString(); // This is just the key
			}
		}

		DisplayableValues metrics = allMetrics.get(element);
		
		// Can't be null since concern comes from map's keySet()
		assert metrics != null;
		
		return metrics.getDisplayValue(columnIndex-1);
	}

	// ----------------------------------------------------
	// IStructuredContentProvider implementation
	// ----------------------------------------------------

	/**
	 * Note: Synchronized to prevent ConcurrentModificationException
	 * when methods are called re-entrantly 
	 */
	@Override
	public synchronized Object[] getElements(Object inputElement)
	{
		return allMetrics.keySet().toArray();
	}
	
	// ----------------------------------------------------
	// IContentProvider implementation
	// ----------------------------------------------------
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
	{ }

	// ----------------------------------------------------
	// ViewerComparator overrides
	// ----------------------------------------------------
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		int cmp;
		
		if (sortColumn == 0)
		{
			if (e1 instanceof String)
			{
				cmp = e1.toString().compareTo(e2.toString());
			}
			else
			{
				cmp = ConcernWithSectionComparator.compareTo((Concern) e1, (Concern) e2);
			}
		}
		else
		{
			DisplayableValues metricsLhs = allMetrics.get(e1);
			
			// Can't be null since concern comes from map's keySet()
			assert metricsLhs != null;
			
			DisplayableValues metricsRhs = allMetrics.get(e2);
			
			// Can't be null since concern comes from map's keySet()
			assert metricsRhs != null;
	
			cmp = DisplayableValues.compareTo(metricsLhs, metricsRhs, 
					sortColumn-1);
		}

		if (table.getSortDirection() == SWT.UP)
			return cmp;
		else
			return -cmp;
	}
	
	// PRIVATE HELPER METHODS
	
	private Display safeGetDisplay()
	{
		if (viewer == null || 
			viewer.getControl() == null ||
			viewer.getControl().isDisposed() ||
			viewer.getControl().getDisplay() == null ||
			viewer.getControl().getDisplay().isDisposed())
		{
			return null;
		}
		else
		{
			return viewer.getControl().getDisplay();
		}
	}
}
