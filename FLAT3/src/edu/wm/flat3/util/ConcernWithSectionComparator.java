package edu.wm.flat3.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.wm.flat3.repository.Concern;

public class ConcernWithSectionComparator implements Comparator<Concern>
{
	private static final int END_OF_SECTION_OFFSET_INDEX = 0;
	
	public static boolean doesNameStartWithDigit(String concernName)
	{
		return concernName.length() > 0 && Character.isDigit(concernName.charAt(0));
	}
	
	@Override
	public int compare(Concern lhs, Concern rhs)
	{
		return compareTo(lhs, rhs);
	}

	static public int compareTo(Concern lhs, Concern rhs)
	{
		if (lhs == rhs)
			return 0;
		else if (lhs == null)
			return -1;
		else if (rhs == null)
			return +1;

		return sectionCompareTo(lhs.getName(), rhs.getName());
	}

	static public int sectionCompareTo(String lhs, String rhs)
	{
		if (lhs == rhs)
			return 0;
		else if (lhs == null)
			return -1;
		else if (rhs == null)
			return +1;

		List<Integer> partsLhs = parseSection(lhs);
		List<Integer> partsRhs = parseSection(rhs);

		int numPartsLhs = partsLhs.size();
		int numPartsRhs = partsRhs.size();

		assert numPartsLhs != 0 && numPartsRhs != 0;
		
		if (numPartsLhs == 1 && numPartsRhs == 1)
		{
			// Neither concern name starts with a section 
			// number, just do a normal compare
			return lhs.compareTo(rhs);
		}
		else if (numPartsLhs == 1)
		{
			// Concern names with section numbers come before
			// those without
			return +1;
		}
		else if (numPartsRhs == 1)
		{
			// Concern names with section numbers come before
			// those without
			return -1;
		}

		// Both concern names start with a section number
		
		// Start at 1 to skip the end-of-section array item
		for(int partIndex = END_OF_SECTION_OFFSET_INDEX + 1; 
			partIndex < Math.max(numPartsLhs, numPartsRhs); 
			++partIndex)
		{
			// Substitute 0 when the section number is not
			// explicit so that 8 < 8.1 becomes 8.0 < 8.1
			
			int partLhs = partIndex < numPartsLhs ?
						partsLhs.get(partIndex) : 0;

			int partRhs = partIndex < numPartsRhs ?
						partsRhs.get(partIndex) : 0;

			int cmp = partLhs - partRhs;
			if (cmp != 0)
				return cmp;
		}

		// Section numbers are equal so verify that the text
		// after the section is the same (it always is)
		
		int sectionEndLhs = partsLhs.get(END_OF_SECTION_OFFSET_INDEX);
		int sectionEndRhs = partsRhs.get(END_OF_SECTION_OFFSET_INDEX);
		
		if (sectionEndLhs == -1 && sectionEndRhs == -1)
			return 0;	// lhs and rhs are both section numbers only
		else if (sectionEndLhs == -1)
			return -1; 	// lhs is a section number only
		else if (sectionEndRhs == -1)
			return +1;	// rhs is a section number only
		else
		{
			// lhs and rhs both have text after the section
			return lhs.substring(sectionEndLhs).compareTo(
					rhs.substring(sectionEndRhs));
		}
	}

	static private List<Integer> parseSection(String s)
	{
		List<Integer> parts = new ArrayList<Integer>(1);
		parts.add(-1); // Initialize value for END_OF_SECTION_OFFSET_INDEX 
		
		int len = s.length();
		
		for(int index = 0; index < len; ++index)
		{
			int numLen = 0;

			// Consume digits
			while (index + numLen < len &&
					Character.isDigit(s.charAt(index + numLen)))
			{
				++numLen;
			}

			if (numLen > 0)
			{
				parts.add(
						Integer.parseInt(
								s.substring(index, index + numLen)));
			}
			else if (s.charAt(index) != '.')
			{
				// Stop consuming the section when we reach a
				// non-section character (neither a digit nor
				// a dot section separator)
				parts.set(END_OF_SECTION_OFFSET_INDEX, index);
				break;
			}
		}

		return parts;
	}
}

