package edu.wm.flat3.repository;

/**
 * Enumeration for component kind.
 * 
 * @author vibhav.garg
 * 
 */
public enum ComponentKind
{
	PROJECT, 
	PACKAGE, 
	CLASS, // This should be TYPE since we handle enums, classes, etc.
	METHOD,
	FIELD, 
	STATEMENT,
	FILE, 
	UNINITIALIZED;

	public static ComponentKind valueOfIgnoreCase(String name)
	{
		for(ComponentKind kind : ComponentKind.values())
		{
			if (kind.name().equalsIgnoreCase(name))
			{
				return kind;
			}
		}
		
		throw new AssertionError("Unknown enumeration value " + name);
	}
	
	@Override
	public String toString()
	{
		char[] titleCase = name().toLowerCase().toCharArray();
		titleCase[0] = Character.toUpperCase(titleCase[0]);
		return new String(titleCase);
	}
}
