package edu.wm.flat3.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.metrics.ScatteringMetricsView;
import edu.wm.flat3.ui.ConcernView;
import edu.wm.flat3.ui.concerntree.ConcernTreeItem;
import edu.wm.flat3.ui.concerntree.ConcernTreeViewer;
import edu.wm.flat3.util.ConcernJob;

/**
 * Handles Link and Unlink initiated by right-clicking one or more
 * concerns in the concern tree. 
 *
 * @author eaddy
 */
public class MultiConcernAction 
	extends 
		Action
	implements 
		ISelectionListener // Listen for selection changes in other views/editors
{
	ConcernTreeViewer concernTreeViewer;
	
	ConcernJob job;

	Set<ConcernTreeItem> selectedConcernItems = new HashSet<ConcernTreeItem>();
	List<IJavaElement> selectedJavaElements = new ArrayList<IJavaElement>();
	List<IJavaElement> javaElementsToUse = null;

	StringBuffer label;

	boolean link;
	
	/**
	 * Creates the action.
	 */
	public MultiConcernAction(ConcernTreeViewer concernTreeViewer, boolean link)
	{
		this.concernTreeViewer = concernTreeViewer;
		this.link = link;

		if (link)
		{
			label = new StringBuffer(
					FLATTT.getResourceString("actions.MultiConcernAction.Link.Label"));

			setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/link.png"));
			
			setToolTipText(
					FLATTT.getResourceString("actions.MultiConcernAction.Link.ToolTip"));
		}
		else
		{
			label = new StringBuffer(
					FLATTT.getResourceString("actions.MultiConcernAction.Unlink.Label"));

			setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/link_break.png"));
			
			setToolTipText(
					FLATTT.getResourceString("actions.MultiConcernAction.Unlink.ToolTip"));
		}

		job = new ConcernJob(label + "ing...", concernTreeViewer);
		
		updateLabel();
	}

	public void clearConcerns()
	{
		selectedConcernItems.clear();
	}
	
	public void addConcernItem(ConcernTreeItem concernToLinkOrUnlink)
	{
		selectedConcernItems.add(concernToLinkOrUnlink);
	}

	public boolean hasWork()
	{
		return !selectedConcernItems.isEmpty() && hasSelectedJavaElements();
	}
	
	public boolean hasSelectedJavaElements()
	{
		if (javaElementsToUse != null)
			return !javaElementsToUse.isEmpty();
		else
			return !selectedJavaElements.isEmpty();
	}

	public boolean retainOnlyActionableElements()
	{
		javaElementsToUse = new ArrayList<IJavaElement>(selectedJavaElements);
		
		elementPassedTheTest: for(int i = javaElementsToUse.size()-1; i >= 0; --i)
		{
			IJavaElement javaElement = javaElementsToUse.get(i);
			
			for(ConcernTreeItem cti : selectedConcernItems)
			{
				boolean hasLink = cti.hasLink(javaElement);
				
				if ((link && !hasLink) || (!link && hasLink))
				{
					continue elementPassedTheTest;
				}
			}
			
			javaElementsToUse.remove(i);
		}
		
		updateLabel();
		
		setEnabled(hasWork());
		
		return hasSelectedJavaElements();
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		if (!hasWork())
			return;
		
		if (javaElementsToUse == null)
			javaElementsToUse = selectedJavaElements;
		
		for(ConcernTreeItem selectedConcernItem : selectedConcernItems)
		{
			for(IJavaElement selectedElement : javaElementsToUse)
			{
				if (link)
				{
					job.addLinkTask(selectedElement, 
							selectedConcernItem.getConcern(), 
							selectedConcernItem.getLinkType());
				}
				else
				{
					job.addUnlinkTask(selectedElement, 
							selectedConcernItem.getConcern(), 
							selectedConcernItem.getLinkType());
				}
			}
		}

		if (job.hasWork())
			job.schedule();
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel)
	{
		if (part instanceof ConcernView || part instanceof ScatteringMetricsView)
			return;

		List<IJavaElement> selectedElements = new ArrayList<IJavaElement>();
		boolean ignoreSelection = 
			MultiElementAction.getSelectedJavaElements(part, sel, selectedElements);

		if (ignoreSelection)
			return;
	
		selectedJavaElements = selectedElements;
	}
	
	public void updateLabel()
	{
		StringBuffer buf = new StringBuffer(label);

		List<IJavaElement> elementsToUse;
		if (javaElementsToUse != null)
			elementsToUse = javaElementsToUse;
		else
			elementsToUse = selectedJavaElements;
		
		int count = 0;
		for(IJavaElement javaElement : elementsToUse)
		{
			if (buf.length() > 15)
			{
				// So menu item label doesn't become too long
				buf.append(", ... (+");
				buf.append(elementsToUse.size() - count);
				buf.append(" more)");
				break;
			}
			
			if (count > 0)
				buf.append(',');
			
			buf.append(" \'");
			if (javaElement.getElementType() == IJavaElement.INITIALIZER)
			{
				buf.append("{...}");
			}
			else
			{
				buf.append(javaElement.getElementName());
			}

			buf.append('\'');
			
			++count;
		}

		setText(buf.toString());
	}
}
