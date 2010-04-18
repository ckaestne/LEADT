package edu.wm.flat3.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;

public class DisplayableValues
{
	List<DisplayableValue> values = new ArrayList<DisplayableValue>();

	public void add(float val)
	{
		values.add(new DisplayableValue(val));
	}

	public void addValue(int val)
	{
		values.add(new DisplayableValue(val));
	}

	public void addValue(String val)
	{
		values.add(new DisplayableValue(val));
	}
	
	public void add(Collection<Concern> val)
	{
		values.add(new DisplayableValue(val));
	}

	public void addValue2(Collection<Component> val)
	{
		// Fake boolean needed because Java doesn't do generics
		// properly
		values.add(new DisplayableValue(val, false));
	}
	
	public float getFloatValue(int index)
	{
		return values.get(index).getFloatValue();
	}
	
	public String getDisplayValue(int index)
	{
		return values.get(index).toString();
	}
	
	public DisplayableValue getValue(int index)
	{
		return values.get(index);
	}
	
	public static int compareTo(DisplayableValues lhs, DisplayableValues rhs, int index)
	{
		return lhs.getValue(index).compareTo(rhs.getValue(index));
	}

	/**
	 * Returns a list of comma-separated values
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		boolean first = true;
		
		for(DisplayableValue value : values)
		{
			if (!first)
				buf.append(',');
			
			buf.append(value.toCSVString());
			
			first = false;
		}
		
		return buf.toString();
	}
}
