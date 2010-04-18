package edu.wm.flat3.util;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class LineSet
{
	private CompilationUnit cu;
	
	private Set<Integer> lines = new TreeSet<Integer>();

	private String debugInfo = null;

	private boolean reportDoubleCounting = false;
	
	public LineSet(CompilationUnit cu)
	{
		this.cu = cu;
	}
	
	public void setDebugInfo(String debugInfo)
	{
		this.debugInfo = debugInfo;
	}
	
	public void setReportDoubleCounting(boolean value)
	{
		reportDoubleCounting = value;
	}
	
	public int size()
	{
		return lines.size();
	}
	
	public int add(int line)
	{
		assert line > 0;
		
		if (!lines.add(line))
		{
			// We expect this since the algorithm we implemented using
			// the visitors doesn't always work
			if (reportDoubleCounting)
			{
				System.out.println("Double counting warning! " + line + 
						" -> " + toString());
			}
			
			return 0;
		}
		else
		{
			return 1;
		}
	}
	
	public int add(int start, int end)
	{
		assert start <= end;
		
		int count = 0;
		
		for(int i = start; i <= end; ++i)
		{
			count += add(i);
		}
		
		return count;
	}
	
	public int add(ASTNode node)
	{
		int startPos = node.getStartPosition();
		int startLine = cu.getLineNumber(startPos);
		int endLine = getEndLineNumberHelper(node);

		return add(startLine, endLine);
	}
	
	public int getEndLineNumberHelper(ASTNode node)
	{
		return cu.getLineNumber(node.getStartPosition() + node.getLength() - 1);
	}
	
	@Override
	public String toString()
	{
		int last = -1;
		int rangeStart = -1;
		
		StringBuffer buf = new StringBuffer();
		for(int i : lines)
		{
			assert i > last;
			
			if (i == last + 1)
			{
				assert rangeStart != -1 && rangeStart < i;
				last = i;
				continue;
			}

			if (rangeStart != -1 && rangeStart != last)
			{
				buf.append("-");
				buf.append(last);
			}
			
			if (buf.length() != 0)
				buf.append(", ");

			buf.append(i);
			
			rangeStart = i;
			last = i;
		}
		
		if (rangeStart != last)
		{
			buf.append("-");
			buf.append(last);
		}
	
		if (debugInfo != null)
			return debugInfo + ": " + buf.toString();
		else
			return buf.toString();
	}
	
}
