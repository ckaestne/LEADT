package edu.wm.flat3.util;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;

import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.repository.InvalidConcernNameException;
import edu.wm.flat3.ui.concerntree.ConcernTreeItem;

public class ConcernTask implements Comparable<ConcernTask>
{
	Concern newConcern = null;
	
	Concern oldConcern = null;

	IJavaElement element = null;

	EdgeKind oldLinkType = null;
	EdgeKind newLinkType = null;
	
	boolean processChildrenToo = false;

	boolean move = false;
	
	private ConcernTask()
	{ }
	
	/**
	 * Removes the concern or link.  When removing a concern,
	 * subconcerns will also be removed.  When removing a link, just
	 * that link will be removed. 
	 * @param concernToRemove
	 * 	Must be non-null.
	 * @param elementToRemove
	 * 	The element to unlink, or null if removing a concern.
	 * @param linkTypeToRemove
	 * 	The link type to remove, or null if removing a concern. 
	 */
	public static ConcernTask createRemovalOrUnlinkTask(	Concern concernToRemove, 
	                                                      	IJavaElement elementToRemove,
	                                                      	EdgeKind linkTypeToRemove)
	{
		ConcernTask task = new ConcernTask();
		task.oldConcern = concernToRemove;
		task.element = elementToRemove;
		task.oldLinkType = linkTypeToRemove;
		task.processChildrenToo = false;
		task.move = true;
		
		assert task.oldConcern != null || task.element != null;
		
		return task;
	}

	/**
	 * Removes the item and its children items.  When removing a
	 * concern item, removes all child concerns.  When removing a
	 * link, also removes sublinks (links of the element's children). 
	 * <P>
	 * Called when DEL is pressed on concern or link items in the concern
	 * tree, or when user rights clicks concern and link items and selects
	 * Remove.
	 */
	public static ConcernTask createRemovalTask(ConcernTreeItem cti)
	{
		ConcernTask task = createRemovalOrUnlinkTask(cti.getConcern(), cti.getJavaElement(),
				cti.getLinkType());
		task.processChildrenToo = true;
		return task;
	}

	/**
	 * Creates a simple link task.
	 */
	public static ConcernTask createLinkTask(	IJavaElement elementToLink, 
	                                            Concern concernToLink,
	                                            EdgeKind linkType)
	{
		ConcernTask task = new ConcernTask();
		task.newConcern = concernToLink;
		task.element = elementToLink;
		task.newLinkType = linkType;
		return task;
	}

	/**
	 * Called when dragging an item from the concern tree to another item in
	 * the (possibly different) concern tree. 
	 */
	public static ConcernTask createMoveOrCopyTask(	ConcernTreeItem itemSource,
	                                                Concern newConcern,
	                                                EdgeKind newLinkType,
	                                                boolean move)
	{
		ConcernTask task = new ConcernTask();
		
		task.oldConcern = itemSource.getConcern();
		task.oldLinkType = itemSource.getLinkType();
		task.element = itemSource.getJavaElement();
		task.newConcern = newConcern;
		task.newLinkType = newLinkType;
		task.processChildrenToo = true;
		task.move = move;

		assert task.oldConcern != null;
		
		// Can only copy/move to a concern item
		assert task.newConcern != null;
		
		return task;
	}
	
	public IStatus run(IProgressMonitor monitor)
	{
		if (oldConcern != null && newConcern != null && element == null)
		{
			if (monitor != null)
				monitor.subTask(oldConcern.getDisplayName());

			// User dragged a concern and dropped it onto another concern
			if (move)
				newConcern.addChild(oldConcern);
			else
				copySubtree(oldConcern, oldLinkType, newConcern, newLinkType);
		}
		else if (oldConcern != null && newConcern == null && element == null)
		{
			if (monitor != null)
				monitor.subTask(oldConcern.getDisplayName());

			// User pressed DEL on a concern
			oldConcern.remove();
		}
		else if (element != null)
		{
			// User is moving a link from one concern to another
			// (in which case newConcern != null), OR
			// User pressed DEL on a link item (in which case newConcern
			// == null and oldConcern is the link to be deleted), OR
			// User right-clicked on a java element and selected Link
			// (in which case newConcern != null)
			
			if (monitor != null)
				monitor.subTask(element.getElementName());

			// A java element in the concern tree may be 'virtual', i.e.,
			// only its children are linked to the concern, not the element
			// itself.  For example, if a member is linked to a concern, it
			// will be shown beneath its class item, which is not linked.
			// In this case, we want to only unlink/link the member
			// elements actually linked to the concern.
			
			assert newConcern == null || newLinkType != null;

			if (processChildrenToo)
			{
				assert oldConcern != null;
				assert oldLinkType != null;
				
				Collection<Component> linkedComponents =
					Component.getLinksRecursive(oldConcern, element, oldLinkType);
				
				for(Component linkedComponent : linkedComponents)
				{
					if (monitor != null && monitor.isCanceled())
						return Status.CANCEL_STATUS;
					
					// We are moving a link from one concern to another
					// in the ConcernMapper tree. Unlink the element from the
					// original concern.
					if (move)
						oldConcern.unlink(linkedComponent, oldLinkType);
					
					if (newConcern != null)
					{
						newConcern.link(linkedComponent, newLinkType);
					}
				}
			}
			else
			{
				// We are moving a link from one concern to another
				// in the ConcernMapper tree. Unlink the element from the
				// original concern.
				if (move)
				{
					assert oldConcern != null;
					assert oldLinkType != null;
					oldConcern.unlink(element, oldLinkType);
				}
				
				if (newConcern != null)
				{
					newConcern.link(element, newLinkType);
				}
			}
		}
		else
		{
			assert false; // Can't happen
		}
		
		return Status.OK_STATUS;
	}
	
	/**
	 * This ends up calling createChild(), which in turn calls
	 * addChild(child, false), so several "children changed"
	 * events will be created.
	 */ 
	public static void copySubtree(Concern sourceConcern, EdgeKind sourceLinkType, 
	                               Concern destinationConcern, EdgeKind destinationLinkType)
	{
		Concern destinationChild;
		try
		{
			destinationChild = destinationConcern.createChild(	sourceConcern.getName(), 
																sourceConcern.getShortName());
			if (destinationChild == null)
				return;
		}
		catch (InvalidConcernNameException e)
		{
			ProblemManager.reportException(e);
			assert false; // UI shouldn't have allowed an invalid job to be queued
			return;
		}
		
		for(Component linkedComponent : sourceConcern.getLinks(sourceLinkType))
		{
			destinationChild.link(linkedComponent, destinationLinkType);
		}
		
		for(Concern sourceConcernChild : sourceConcern.getChildren())
		{
			// Ignore return value since we've already had at least one success
			copySubtree(sourceConcernChild, sourceLinkType, destinationChild, destinationLinkType);
		}
	}

	@Override
	public int compareTo(ConcernTask rhs)
	{
		int cmp = safeCompareConcerns(this.newConcern, rhs.newConcern);
		if (cmp != 0)
			return cmp;

		cmp = safeCompareConcerns(this.oldConcern, rhs.oldConcern);
		if (cmp != 0)
			return cmp;
		
		return 0;
	}
	
	public static int safeCompareConcerns(Concern lhs, Concern rhs)
	{
		if (lhs != null && rhs != null)
			return lhs.compareTo(rhs);
		else if (lhs != null)
			return -1; // Concerns come first
		else if (rhs != null)
			return +1; // Concerns come first
		else
			return 0;
	}
	
	@Override
	public String toString()
	{
		if (oldConcern != null && newConcern != null && element == null)
		{
			// User dragged a concern and dropped it onto another concern
			if (move)
				return "Move " + oldConcern + " to " + newConcern;
			else
				return "Copy " + oldConcern + " to " + newConcern;
		}
		else if (oldConcern != null && newConcern == null && element == null)
		{
			// User pressed DEL on a concern
			return "Remove " + oldConcern;
		}
		else if (element != null)
		{
			// User is moving a link from one concern to another
			// (in which case newConcern != null), OR
			// User pressed DEL on a link item (in which case newConcern
			// == null and oldConcern is the link to be deleted), OR
			// User right-clicked on a java element and selected Link
			// (in which case newConcern != null)

			assert newConcern == null || newLinkType != null;

			if (newConcern != null)
			{
				if (move)
				{
					return "Move " + element + " from " + oldConcern + " to " + newConcern;
				}
				else
				{
					return "Copy " + element + " from " + oldConcern + " to " + newConcern;
				}
			}
			else
			{
				return "Remove " + element + "->" + oldConcern;
			}
		}
		else
		{
			assert false;
			return "UNKNOWN!";
		}
	}
}
