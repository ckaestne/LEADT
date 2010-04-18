package edu.wm.flat3.ui.concerntree;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * The sorter class for elements in the tree viewer This class only needs to
 * spit the elements into categories. The default behavior of the class takes
 * care of the sorting
 */
class ConcernTreeSorter extends ViewerSorter
{
	JavaElementComparator javaElementComparator = new JavaElementComparator();
	
	/**
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer viewer, Object e1, Object e2)
	 */
	@Override
    public int compare(Viewer viewer, Object o1, Object o2) 
    {
    	ConcernTreeItem node1 = (ConcernTreeItem) o1;
        IJavaElement je1 = node1.getJavaElement();

        ConcernTreeItem node2 = (ConcernTreeItem) o2;
        IJavaElement je2 = node2.getJavaElement();
        
        if (je1 == null && je2 == null)
        {
       		return node1.getConcern().getId() - node2.getConcern().getId();
        }
        else if (je1 == null)
        {
        	return -1; // Sub-concerns go before java elements
        }
        else if (je2 == null)
        {
        	return +1; // Sub-concerns go before java elements
        }
        else
        {
        	return javaElementComparator.compare(viewer, je1, je2);
        }
    }	
}
