package edu.wm.flat3.metrics;

import org.eclipse.swt.SWT;

public class ColumnInfo
{
	public String title;
	public int style;
	public int width;
	public int direction = SWT.DOWN;
	
	public ColumnInfo(String title, int style, int width)
	{
		this.title = title;
		this.style = style;
		this.width = width;
	}
	
	public void reverseSortDirection()
	{
		switch (direction)
		{
			case SWT.NONE:
			case SWT.UP:
				direction = SWT.DOWN;
				break;
			case SWT.DOWN:
				direction = SWT.UP;
				break;
			default:
				assert false;
		}
	}
}
