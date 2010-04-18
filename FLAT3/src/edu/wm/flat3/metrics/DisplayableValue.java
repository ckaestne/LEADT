package edu.wm.flat3.metrics;

import java.util.Collection;

import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;

public class DisplayableValue implements Comparable<DisplayableValue>
{
	Object value;
	
	public DisplayableValue(int val)
	{
		this.value = val;
	}

	public DisplayableValue(float val)
	{
		this.value = val;
	}

	public DisplayableValue(String val)
	{
		this.value = val;
	}
	
	public DisplayableValue(Collection<Concern> concerns)
	{
		this.value = concerns;
	}

	// Fake boolean needed because Java doesn't do generics
	// properly
	public DisplayableValue(Collection<Component> components, boolean fake)
	{
		this.value = components;
	}
	
	public float getFloatValue()
	{
		if (value instanceof Integer)
		{
			return (float) ((Integer) value).intValue();
		}
		else if (value instanceof Float)
		{
			return ((Float) value).floatValue();
		}
		else
		{
			assert false;
			return 0.0f;
		}
	}
	
	/**
	 * Returns a value suitable for display
	 */
	@Override
	public String toString()
	{
		if (value instanceof String)
		{
			return (String) value;
		}
		else if (value instanceof Integer)
		{
			return value.toString();
		}
		else if (value instanceof Float)
		{
			float val = ((Float) value).floatValue();
			if (Float.isNaN(val))
				return String.valueOf(val);
			else
				return String.format("%5.3f", Math.abs(val));
		}
		else if (value instanceof Collection)
		{
			Collection collection = (Collection) value;
			
			StringBuffer buf = new StringBuffer();
			boolean first = true;
			for(Object item : collection)
			{
				if (!first)
					buf.append(", ");
				
				if (item instanceof Concern)
				{
					buf.append(((Concern) item).getShortDisplayName());
				}
				else
				{
					buf.append(((Component) item).getName());
				}
				
				first = false;
			}
			
			return buf.toString();
		}
		else
		{
			assert false;
			return null;
		}
	}

	/**
	 * Returns a value suitable for a comma-separated value (CSV) file
	 * (i.e., string values with commas and spaces are quoted)
	 */
	public String toCSVString()
	{
		if (value instanceof Collection)
			return "\"" + toString() + "\"";
		else
			return toString();
	}

	@Override
	public int compareTo(DisplayableValue o)
	{
		if (value instanceof String)
		{
			return ((String) value).compareTo((String) o.value);
		}
		else if (value instanceof Integer)
		{
			return ((Integer) value).compareTo((Integer) o.value);
		}
		else if (value instanceof Float)
		{
			return ((Float) value).compareTo((Float) o.value);
		}
		else if (value instanceof Collection)
		{
			return toString().compareTo(o.toString());
		}
		else
		{
			assert false;
			return 0;
		}
	}
}
