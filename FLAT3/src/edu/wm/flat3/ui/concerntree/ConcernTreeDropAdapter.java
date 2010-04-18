package edu.wm.flat3.ui.concerntree;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.jface.util.LocalSelectionTransfer;

import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.util.ConcernJob;

/**
 * Allows the dropping of elements into this view.
 */
class ConcernTreeDropAdapter extends ViewerDropAdapter
{
	private long timeDragOverStartMS = 0;
	private Concern lastConcernDropTarget = null;

	private static final long MINIMUM_HOVER_EXPANSION_MS = 500;

	private IConcernModelProvider concernModelProvider;

	/**
	 * Creates a new adapter.
	 */
	public ConcernTreeDropAdapter(	IConcernModelProvider concernModelProvider, 
									TreeViewer treeViewer)
	{
		super(treeViewer);
		
		// Turn off default insertion of fake items into the tree
		this.setFeedbackEnabled(false);
		this.setSelectionFeedbackEnabled(true);
		this.concernModelProvider = concernModelProvider;
	}

	/**
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 * @param pEvent
	 *            the information associated with the drag enter event
	 */
	@Override
	public void dragEnter(DropTargetEvent pEvent)
	{
		super.dragEnter(pEvent);
		if (isDragSourceOutsideConcernMapperTree())
		{
			pEvent.detail = DND.DROP_COPY;
		}
	}

	/**
	 * Overrides dragOver() to increase timeout for auto expanding tree items.
	 * The default timeout was too short which made it very difficult when you
	 * need to pass over many different tree items during dnd.
	 */
	@Override
	public void dragOver(DropTargetEvent event)
	{
		super.dragOver(event);

		// By default, disable auto expansion of tree items
		event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;

		// Are we over a valid drop target?
		Object dropTarget = unwrapSelectedOrTargetObject(getCurrentTarget(), true);
		if (dropTarget == null || !(dropTarget instanceof Concern))
			return;

		// Only auto expand if we hover over the same concern for more than
		// 1/2 second

		Concern concernDropTarget = (Concern) dropTarget;
		if (concernDropTarget != null &&
			lastConcernDropTarget == concernDropTarget)
		{
			long timeDragOverMS = event.time & 0xFFFFFFFFL;

			long hoverTimeMS = timeDragOverMS - timeDragOverStartMS;

			if (hoverTimeMS > MINIMUM_HOVER_EXPANSION_MS)
			{
				event.feedback |= DND.FEEDBACK_EXPAND;
			}
		}
		else
		{
			timeDragOverStartMS = event.time & 0xFFFFFFFFL;
			lastConcernDropTarget = concernDropTarget;
		}
	}
	
	/**
	 * Determines whether it is possible to drop a certain type of object in
	 * the tree viewer. It is only possible to drop field and method objects
	 * into concerns, and concern files within the general space (null target).
	 * 
	 * @param pTarget
	 *            the object that the mouse is currently hovering over, or
	 *            <code>null</code> if the mouse is hovering over empty space
	 * @param pOperation
	 *            the current drag operation (copy, move, etc.)
	 * @param pTransferType
	 *            the current transfer type
	 * @return <code>true</code> if the drop is valid, and <code>false</code>
	 *         otherwise
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean validateDrop(Object pTarget, int pOperation,
			TransferData pTransferType)
	{
		Concern concernDropTarget;
		
		// Can only drop onto concern items
		if (pTarget != null)
		{
			if (!(pTarget instanceof ConcernTreeItem))
				return false;
			
			ConcernTreeItem ctiDropTarget = (ConcernTreeItem) pTarget;
			if (ctiDropTarget.getJavaElement() != null)
				return false; // Can only drop onto concern items
			
			concernDropTarget = ctiDropTarget.getConcern();
		}
		else
		{
			concernDropTarget = concernModelProvider.getModel().getRoot();
		}

		Object dropSources = LocalSelectionTransfer.getTransfer().getSelection();
		if (!(dropSources instanceof IStructuredSelection))
			return false;

		boolean atLeastOneIsValid = false;

		// Cache the value here so it stays the same throughout the while loop
		EdgeKind concernComponentRelation = 
			concernModelProvider.getLinkType();

		Iterator lI = ((IStructuredSelection) dropSources).iterator();
		while (lI.hasNext())
		{
			Object dropSource = lI.next();

			// Converts the selection into either a valid concern or
			// a valid IJavaElement (must use instanceof to figure out
			// which)
			Object unwrappedDropSource = unwrapSelectedOrTargetObject(dropSource, false);
			if (unwrappedDropSource == null)
				return false;

			if (unwrappedDropSource instanceof IJavaElement)
			{
				// Can't drop links onto the root!
				if (concernDropTarget.isRoot())
					return false;
				
				IJavaElement javaElementDropSource = (IJavaElement) unwrappedDropSource;

				if (!concernDropTarget.isLinked(javaElementDropSource, 
						concernComponentRelation))
				{
					atLeastOneIsValid = true;
				}
			}
			else
			{
				Concern concernDropSource = (Concern) unwrappedDropSource;

				// Verify that we aren't trying to drop a concern item onto itself
				if (concernDropTarget.getDisplayName().equals(concernDropSource.getDisplayName()))
					return false;
				
				// Verify that we didn't try to drag a parent and drop it onto a child
				Concern current = concernDropTarget;
				while (current != null)
				{
					// Compare names instead of ids since the concern may come
					// from a different concern domain
					if (current.getName().equals(concernDropSource.getName()))
						return false;
					
					current = current.getParent();
				}
				
				// Verify that we didn't try to drag a child and drop it onto its parent
				// (i.e., new location would be the same as the old one)
				
				if (concernDropTarget.isChildNameValid(concernDropSource.getDisplayName()) != null)
					return false; // One bad apple invalidates the bunch

				atLeastOneIsValid = true;
			}
		}

		return atLeastOneIsValid;
	}

	/**
	 * When a user has dragged a elements into the tree viewer, take the steps
	 * necessary to include these elements into the model.
	 * 
	 * @param pData
	 *            the drop data
	 * @return <code>true</code> if the drop was successful, and
	 *         <code>false</code> otherwise
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean performDrop(Object pData)
	{
		Concern concernDropTarget;
		EdgeKind linkType;
		
		Object dropTarget = getCurrentTarget();
		if (dropTarget != null)
		{			
			// Can only drop onto a concern item
			ConcernTreeItem ctiDropTarget = (ConcernTreeItem) dropTarget;
			assert ctiDropTarget.getJavaElement() == null;
			
			concernDropTarget = ctiDropTarget.getConcern();
			linkType = ctiDropTarget.getLinkType();
		}
		else
		{
			concernDropTarget = concernModelProvider.getModel().getRoot();
			linkType = concernModelProvider.getLinkType();
		}
		
		Object dropSources = LocalSelectionTransfer.getTransfer().getSelection();
		if (!(dropSources instanceof IStructuredSelection))
			return false;

		ConcernJob job = new ConcernJob("Linking...", concernModelProvider);
		
		boolean move = getCurrentOperation() != DND.DROP_COPY;
		
		Iterator lI = ((IStructuredSelection) dropSources).iterator();
		
		while (lI.hasNext())
		{
			Object dropSource = lI.next();

			if (dropSource instanceof ConcernTreeItem)
			{
				// Within tree DnD - Copy or move concern or link items to 
				// another concern (possibly in a different tree)
				
				ConcernTreeItem ctiDropSource = (ConcernTreeItem) dropSource;

				job.addMoveOrCopyTask(ctiDropSource, concernDropTarget, linkType, move);
			}
			else
			{
				// Dragged Java element from outside the concern tree to a
				// concern item in the tree

				// validateDrop() should have prevented this
				assert !concernDropTarget.isRoot();
				
				// Try to extract a IJavaElement
				Object unwrappedDropSource = unwrapSelectedOrTargetObject(dropSource, false);
				if (unwrappedDropSource == null)
					continue; // validateDrop() should have prevented this
	
				assert unwrappedDropSource instanceof IJavaElement;
				assert !move; // We set this to true in dragEnter()
				
				IJavaElement elementDropSource = (IJavaElement) unwrappedDropSource;
				
				job.addLinkTask(elementDropSource, concernDropTarget, linkType);
			}
		}
		
		job.schedule();

		return true;
	}

	// HELPER METHODS
	
	/**
	 * We consider to be dragging from within the model if any of the local
	 * selection is an element node.
	 */
	@SuppressWarnings("unchecked")
	private boolean isDragSourceOutsideConcernMapperTree()
	{
		Object lSelection = LocalSelectionTransfer.getTransfer().getSelection();
		if (!(lSelection instanceof IStructuredSelection))
			return false;

		Iterator lI = ((IStructuredSelection) lSelection).iterator();
		while (lI.hasNext())
		{
			Object lNext = lI.next();
			if (lNext instanceof ConcernTreeItem)
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Convert a opaque drop source and drop target object to either a concern
	 * or a valid java element.
	 * 
	 * @param o
	 *            A drop target or drop source object passed to validateDrop()
	 *            or obtained during performDrop()
	 * @return Concern, valid java element, or null
	 */
	private Object unwrapSelectedOrTargetObject(Object o, boolean isTarget)
	{
		IJavaElement javaElement = null;
		Concern concern = null;

		if (o == null)
		{
			return null;
		}
		else if (o instanceof IJavaElement)
		{
			// o is an IJavaElement being dragged from Project
			// Explorer
			javaElement = (IJavaElement) o;
		}
		else if (o instanceof ConcernTreeItem)
		{
			// o is a node in the ConcernMapper tree, either
			// being dropped on (most likely) or dragged
			// (happens when we are dropping one node onto
			// another in the tree)
			javaElement = ((ConcernTreeItem) o).getJavaElement();
			concern = ((ConcernTreeItem) o).getConcern();
		}
		else if (o instanceof IAdaptable)
		{
			// When does this get called?
			assert false;

			IAdaptable adaptable = (IAdaptable) o;

			javaElement = (IJavaElement) adaptable
					.getAdapter(IJavaElement.class);
			concern = (Concern) adaptable.getAdapter(Concern.class);
		}

		if (isTarget && concern != null)
			return concern; 
		
		// Further validation for java elements. Note: Java elements
		// are always drop sources, never drop targets. Only Concerns
		// can be drop targets.

		if (javaElement != null)
		{
			return Component.validateAndConvertJavaElement(javaElement);
		}
		else if (concern != null)
		{
			return concern;
		}
		else
		{
			return null;
		}
	}
}
