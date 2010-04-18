package edu.wm.flat3.metrics;

import java.io.PrintStream;
import java.util.Arrays;

import org.eclipse.swt.SWT;

import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.util.ConcernWithSectionComparator;

public class ScatteringMetricsTable
	extends MetricsTable
{
	public ScatteringMetricsTable()
	{
		super(new ColumnInfo[] 
		    { 
				new ColumnInfo("Concern", SWT.LEFT, 200),
				new ColumnInfo("DOSC", SWT.CENTER, 100),
				new ColumnInfo("DOSM", SWT.CENTER, 100),
				new ColumnInfo("CDC", SWT.CENTER, 50),
				new ColumnInfo("CDO", SWT.CENTER, 50),
				new ColumnInfo("SLOCC", SWT.CENTER, 50),
				new ColumnInfo("Count", SWT.CENTER, 50),
				new ColumnInfo("Tangled With", SWT.LEFT, 400) 
			});
	}
	
	/**
	 * Note: Synchronized to prevent ConcurrentModificationException
	 * when methods are called re-entrantly 
	 */
	public synchronized void add(Concern concern, DisplayableValues metricsForConcern)
	{
		assert metricsForConcern != null;

		DisplayableValues prev = allMetrics.put(concern,
				metricsForConcern);
		assert prev == null;
	}

	/**
	 * Note: Synchronized to prevent ConcurrentModificationException
	 * when methods are called re-entrantly 
	 */
	@Override
	protected synchronized void outputRows(PrintStream out)
	{
		int size = allMetrics.keySet().size();
		if (size <= 0)
			return;
		
		Concern[] concerns = new Concern[size];
		for(Object object : allMetrics.keySet())
		{
			concerns[--size] = (Concern) object;
		}
		
		Arrays.sort(concerns, new ConcernWithSectionComparator());
		
		for (Concern concern : concerns)
		{
			String displayName = concern.getDisplayName();
			
			// CSV files must escape double quotes by using
			// double double quotes
			displayName = displayName.replace("\"", "\"\"");
			
			out.print("\"" + displayName + "\",");
			out.println(allMetrics.get(concern).toString());
		}
	}
}
