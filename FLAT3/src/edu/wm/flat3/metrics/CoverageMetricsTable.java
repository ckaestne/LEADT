package edu.wm.flat3.metrics;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import org.eclipse.swt.SWT;

import edu.wm.flat3.repository.Component;

public class CoverageMetricsTable
	extends MetricsTable
{
	public CoverageMetricsTable()
	{
		super(new ColumnInfo[]
		    { 
				new ColumnInfo("Metric", SWT.LEFT, 50),
				new ColumnInfo("All", SWT.CENTER, 50),
				new ColumnInfo("Linked", SWT.CENTER, 100),
				new ColumnInfo("%", SWT.CENTER, 50),
				new ColumnInfo("Not Linked", SWT.LEFT, 200)
		    }
		);
	}

	/**
	 * Note: Synchronized to prevent ConcurrentModificationException
	 * when methods are called re-entrantly 
	 */
	public synchronized void add(String name, int total, int mapped, 
	                Collection<Component> notMapped)
	{
		float percent = ((float) mapped / total) * 100.0f;

		DisplayableValues metrics = new DisplayableValues();
		
		metrics.addValue(total);
		metrics.addValue(mapped);
		metrics.addValue((int) percent);
		
		if (notMapped == null)
			metrics.addValue("");
		else
			metrics.addValue2(notMapped);
		
		DisplayableValues prev = allMetrics.put(name, metrics);
		assert prev == null;
	}

	// ----------------------------------------------------
	// MetricsTable overrides
	// ----------------------------------------------------
	
	/**
	 * Note: Synchronized to prevent ConcurrentModificationException
	 * when methods are called re-entrantly 
	 */
	@Override
	protected synchronized void outputRows(PrintStream out)
	{
		for(Map.Entry<Object, DisplayableValues> entry : allMetrics.entrySet())
		{
			out.println(entry.getKey() + "," + entry.getValue());
		}
	}
}
