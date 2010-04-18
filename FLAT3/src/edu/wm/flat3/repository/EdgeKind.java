package edu.wm.flat3.repository;

/**
 * Enumaration for edge kinds.
 * 
 * @author vibhav.garg
 * 
 */
public enum EdgeKind
{
	CONTAINS, 
	RELATED_TO,
	EXECUTED_BY,
	DEPENDS_ON_REMOVAL,
	FIXED_FOR;

	public static EdgeKind valueOfIgnoreCase(String name)
	{
		for(EdgeKind edgeKind : EdgeKind.values())
		{
			if (edgeKind.name().equalsIgnoreCase(name))
				return edgeKind;
		}
		
		throw new AssertionError("Unknown enumeration value " + name);
	}

	@Override
	public String toString()
	{
		char[] buf = name().toCharArray();
		
		char last = '_'; // Force first character to be uppercase
		
		for(int i = 0; i < buf.length; ++i)
		{
			char c = buf[i];

			if (c == '_')
			{
				buf[i] = '-';
			}
			else if (last == '_')
			{
				buf[i] = Character.toUpperCase(c);
			}
			else
			{
				buf[i] = Character.toLowerCase(c);
			}

			last = c;
		}
		
		return new String(buf);
	}
}
