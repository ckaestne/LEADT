package edu.wm.flat3.util;

import java.util.Set;

public class Comparer
{
	public static boolean safeEquals(Object o1, Object o2)
	{
		if (o1 == o2)
			return true;
		else if (o1 == null || o2 == null)
			return false;
		else
			return o1.equals(o2);
	}

	public static boolean safeEquals(Set c1, Set c2)
	{
		if (c1 == c2)
			return true;
		else if (c1 == null)
			return c2.isEmpty();
		else if (c2 == null)
			return c1.isEmpty();
		else
			return c1.equals(c2);
	}
	
	public static int safeCompare(Comparable o1, Comparable o2)
	{
		if (o1 == o2)
			return 0;
		else if (o1 == null)
			return -1;
		else if (o2 == null)
			return +1;
		else
			return o1.compareTo(o2);
	}
}
