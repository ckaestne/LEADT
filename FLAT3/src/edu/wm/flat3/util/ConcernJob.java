package edu.wm.flat3.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;


import edu.wm.flat3.CodeModelRule;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.ui.concerntree.ConcernTreeItem;

public class ConcernJob extends Job
{
	List<ConcernTask> todo = new ArrayList<ConcernTask>();
	
	ConcernModel concernModel;
	EdgeKind concernComponentRelation;
	IConcernModelProvider concernModelProvider;

	public ConcernJob(String description, IConcernModelProvider concernModelProvider)
	{
		super(description);
	
		this.concernModelProvider = concernModelProvider;
		

		this.setRule(new CodeModelRule());
	}

	public void clearWork()
	{
		todo.clear();
	}
	
	public int getWorkItemCount()
	{
		return todo == null ? 0 : todo.size();
	}
	
	public boolean hasWork()
	{
		return getWorkItemCount() > 0;
	}

	/**
	 * Called when user right clicks an element from outside of the concern tree
	 * and selects Unlink, or right clicks a concern item and selects Unlink. 
	 */
	public void addUnlinkTask(		IJavaElement elementToRemove, 
	                                Concern concernToRemove,
	                                EdgeKind linkTypeToRemove)
	{
		todo.add(ConcernTask.createRemovalOrUnlinkTask(concernToRemove, elementToRemove, 
				linkTypeToRemove));
	}

	/**
	 * Called when DEL is pressed on concern or link items in the concern
	 * tree, or when user rights clicks concern and link items and selects
	 * Remove. Removes all children concerns and links.
	 */
	public void addRemovalTask(ConcernTreeItem cti)
	{
		todo.add(ConcernTask.createRemovalTask(cti));
	}

	/**
	 * Called when user right-clicks a Java element and selects Link, or
	 * when they right-click one or more concern items and selects Link,
	 * or when a Java element from outside the concern tree is dropped
	 * onto onto a concern item in the tree.
	 */
	public void addLinkTask(	IJavaElement elementToLink, 
	                            Concern concernToLink,
	                            EdgeKind linkType)
	{
		todo.add(ConcernTask.createLinkTask(elementToLink, concernToLink, linkType));
	}

	/**
	 * Called when dragging an item from the concern tree to another item in
	 * the (possibly different) concern tree. 
	 */
	public void addMoveOrCopyTask(ConcernTreeItem sourceItem, 
	                              Concern destinationConcern,
	                              EdgeKind destinationLinkType,
	                              boolean move)
	{
		todo.add(ConcernTask.createMoveOrCopyTask(sourceItem, 
				destinationConcern, destinationLinkType, move));
	}
	
	@Override
	public IStatus run(IProgressMonitor monitor)
	{
		if (!hasWork())
			return Status.OK_STATUS; // Nothing to do
		
		// Copy these just in case they change in the interim
		this.concernModel = concernModelProvider.getModel();
		
		boolean showProgress = todo.size() > 5;
		
		if (showProgress)
		{
			monitor.beginTask("Processing " + todo.size() + " items", todo.size());
		}

		try
		{
			concernModel.disableNotifications();

			// Make sure the concerns are sorted in id order so that copy
			// creates the concerns in the right order.  Must reverse the
			// list since we iterate from last to first.
			Collections.sort(todo);
			Collections.reverse(todo);
			
			for(int i = todo.size() - 1; i >=0; --i)
			{
				ConcernTask task = todo.remove(i);

				// Did the user ask us to quit?
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				IStatus status = task.run(showProgress ? monitor : null);
				if (!status.isOK())
					return status;
	
				if (showProgress)
					monitor.worked(1);	
			}

			assert !hasWork();

			return Status.OK_STATUS;
		}
		finally
		{
			clearWork();
			
			if (showProgress)
				monitor.done();

			concernModel.enableNotifications();
		}
	}
}
