/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.ui.concerntree;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableFontProvider;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;


import edu.wm.flat3.actions.MultiElementAction;
import edu.wm.flat3.metrics.ScatteringMetricsView;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.ui.ConcernView;
import edu.wm.flat3.util.Comparer;
import edu.wm.flat3.util.ProblemManager;

// See LightWeightDecorator to see how to allow the user
// to turn on and off decoration

/**
 * Provides the image and text labels for elements in a concern model. Also
 * keeps track of the selected java element in Package Explorer and highlights
 * any concerns that are linked to it.
 * <P>
 * Note: We don't have to listen for changes to the concern tree (e.g.,
 * links) since this causes ConcernTreeViewer to refresh and redraw
 * all the labels.
 */
public class ConcernTreeLabelProvider 
	extends 
		BaseLabelProvider 		// Allows us to force icons, labels, and fonts to be updated
	implements
		ILabelProvider, 		// Provides icons and text labels
		ITableFontProvider, 	// Provides highlighting
		//ITableColorProvider,
		ISelectionListener		// Listens for selection changes in Package Explorer
{
	private Set<IJavaElement> currentlySelectedElements = null;
	private Set<Concern> currentlySelectedConcerns = null;
	
	private Font boldFont = null;
	
	private ConcernTreeViewer concernTreeViewer;
	
	private IStatusLineManager statusLineManager;
	
	private SelectedConcernsAsynchronousUpdater selectedConcernsUpdater =
		new SelectedConcernsAsynchronousUpdater();

	boolean slowButAccurateHighlighting = true;
	
	/**
	 * Enables highlighting of concerns in concern tree based
	 * on the Java element currently selected in Project Explorer,
	 * Editor, etc.
	 */
	private boolean highlightPackageExplorerSelection = false;

	public ConcernTreeLabelProvider(	ConcernTreeViewer concernTreeViewer,
										boolean highlightPackageExplorerSelection,
										IStatusLineManager statusLineManager)
	{
		this.concernTreeViewer = concernTreeViewer;
		this.highlightPackageExplorerSelection = highlightPackageExplorerSelection;
		this.statusLineManager = statusLineManager;
	}
	
	// -----------------------------------------------------
	// BaseLabelProvider overrides
	// -----------------------------------------------------
	// These methods are only called once to initialize the
	// labels for each item in the ConcernMapper tree. You
	// must call fireLabelEvent() to force an update to the
	// icons, labels, or font.

	/**
	 * Provides the text for an object in a concern model.
	 * 
	 * @param o
	 *            The object to provide the text for.
	 * @return The text label
	 */
	public String getText(Object o)
	{
		if (!(o instanceof ConcernTreeItem))
		{
			assert false;
			return null;
		}
		
		ConcernTreeItem cti = (ConcernTreeItem)o; 
		return cti.getText();
	}

	/**
	 * Provides the image for an object in a concern model.
	 * 
	 * @param pObject
	 *            The object to provide the image for.
	 * @return The image
	 */
	public Image getImage(Object o)
	{
		if (!(o instanceof ConcernTreeItem))
		{
			assert false;
			return null;
		}

		ConcernTreeItem cti = (ConcernTreeItem)o;
		return cti.getImage();
	}

	// -----------------------------------------------------
	// ITableFontProvider implementation
	// -----------------------------------------------------

	/**
	 * This method is called by the TreeViewer to get the font for each item in
	 * the tree.
	 */
	@Override
	public Font getFont(Object element, int columnIndex)
	{
		boolean debug = true;
		
		if (!highlightPackageExplorerSelection)
			return null;
		
		// We only highlight concerns
		if (!(element instanceof ConcernTreeItem))
		{
			assert false;
			return null;
		}

		// Lazy initialize the fonts - Must do it here since otherwise
		// the object chain might not be valid

		if (boldFont == null)
		{
			FontRegistry fontRegistry = PlatformUI.getWorkbench()
					.getThemeManager().getCurrentTheme().getFontRegistry();

			boldFont = fontRegistry.getBold("Text Font");
		}
		
		ConcernTreeItem concernTreeItem = (ConcernTreeItem) element;

		Concern concernInTree = concernTreeItem.getConcern();
		assert concernInTree != null;
		
		IJavaElement javaElementInTree = concernTreeItem.getJavaElement(); 
		
		if (javaElementInTree == null)
		{
			// ConcernTreeItem represents a concern.  Highlight it if
			// its one of the selected concerns.
			if (currentlySelectedConcerns != null &&
				currentlySelectedConcerns.contains(concernInTree))
			{
				if (debug)
					System.out.println("Update font: BOLD " + concernTreeItem);
				return boldFont;
			}
		}
		else if (currentlySelectedElements != null)
		{
			// ConcernTreeItem represents a a type and/or link
			
			for(IJavaElement selectedJavaElement : currentlySelectedElements)
			{
				if (selectedJavaElement.equals(javaElementInTree))
				{
					// Item can be in the tree for two reasons: 1) it is an
					// linked type, field, or method, or 2) it is an
					// unlinked type that has a linked field or method

					if (javaElementInTree.getElementType() != IJavaElement.TYPE)
					{
						// Item is a linked field or method 
						if (debug)
							System.out.println("Update font: BOLD " + concernTreeItem);
						return boldFont;
					}
					// Item is a type, see if its linked
					else if (concernTreeItem.hasLink(selectedJavaElement))
					{
						if (debug)
							System.out.println("Update font: BOLD " + concernTreeItem);
						return boldFont;
					}
				}

				// Item is a type and selectedJavaElement one of the type's
				// fields or methods.  Highlight the type if the field or
				// method is selected.
				else if (javaElementInTree.getElementType() == IJavaElement.TYPE &&
						selectedJavaElement.getParent().equals(javaElementInTree) &&
						concernTreeItem.hasLink(selectedJavaElement))
				{
					if (debug)
						System.out.println("Update font: BOLD " + concernTreeItem);
					return boldFont;
				}
			}
		}

		// Just use the normal font (most cases)
		if (debug)
			System.out.println("Update font: NORMAL " + concernTreeItem);
		return null;
	}

	// -----------------------------------------------------
	// ITableColorProvider implementation
	// -----------------------------------------------------
	
	//@Override
	//public Color getBackground(Object element, int columnIndex)
	//{
	//	return null;
	//}
	//
	//@Override
	//public Color getForeground(Object element, int columnIndex)
	//{
	//	return Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	//}
	
	// -----------------------------------------------------
	// ISelectionListener implementation
	// -----------------------------------------------------

	/**
	 * Keeps track of selection changes in Package Explorer and
	 * highlights the concerns linked to the selected Java elements.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel)
	{
		assert highlightPackageExplorerSelection;

		if (part instanceof ConcernView || part instanceof ScatteringMetricsView)
			return;

		Set<IJavaElement> selectedElements = new HashSet<IJavaElement>();
		
		boolean ignoreSelection = MultiElementAction.getSelectedJavaElements(
				part, sel, selectedElements);

		// When the user clicks in the editor it causes an empty selection,
		// and we don't want this to clear our currently selected elements.
		// We also don't want to clear when the user selects a portion of
		// an item (e.g., IJavaElement == null)

		if (ignoreSelection)
			return;
		
		if (Comparer.safeEquals(currentlySelectedElements, selectedElements))
		{
			// Handle simple case when nothing is selected or when
			// the user clicks on the same elements again
			return;
		}
		
		// Create events to update the labels of the previously
		// selected elements and the newly selected elements

		ConcernEvent refreshLabelsEvent = new ConcernEvent();
		addUpdateLabelEvents(refreshLabelsEvent, currentlySelectedElements,
				selectedElements);
		
		// User selected something new -- figure out what concerns
		// to highlight
		
		currentlySelectedElements = selectedElements;

		// Note: This doesn't actually work since another selection
		// listener overrides our status line message (oh, the humanity!)
		statusLineManager.setMessage(currentlySelectedElements.size() + 
				" Java element" +
				(currentlySelectedElements.size() == 1 ? "" : "s") +
				" selected");
		
		// True means refresh the labels
		selectedConcernsUpdater.asyncUpdate(currentlySelectedElements, 
				refreshLabelsEvent, true);
	}

	private static void addUpdateLabelEvents(	ConcernEvent refreshLabelsEvent,
	                                            Collection<?> oldSelection,
	                                            Collection<?> newSelection)
	{
		if (refreshLabelsEvent == null)
			return;
		
		addIfLeftNotInRight(refreshLabelsEvent, oldSelection, newSelection);
		addIfLeftNotInRight(refreshLabelsEvent, newSelection, oldSelection);
	}
	
	private static void addIfLeftNotInRight(	ConcernEvent refreshLabelsEvent,
	                                            Collection<?> setToAdd,
	                                            Collection<?> setToCheck)
	{
		if (setToAdd == null)
			return;

		for(Object o : setToAdd)
		{
			if (setToCheck == null || !setToCheck.contains(o))
				refreshLabelsEvent.addUpdateLabelEvent(o);
			
			if (o instanceof IJavaElement)
			{
				IJavaElement element = (IJavaElement) o;
				if (element.getElementType() == IJavaElement.TYPE)
					continue;
				
				IJavaElement typeParent = element.getParent();
				if (setToCheck == null || !setToCheck.contains(typeParent))
					refreshLabelsEvent.addUpdateLabelEvent(typeParent);
			}
		}
	}

	public void updateSelectedConcerns()
	{
		// The label provider's cache of concerns to highlight may
		// be invalid since the links have changed so update
		// the cache.  This may cause this run() method to be called
		// again eventually to change the highlighting of some items.
		
		selectedConcernsUpdater.syncUpdate(currentlySelectedElements, null, false);
	}
	
	// HELPER CLASSES
	
	private final class SelectedConcernsAsynchronousUpdater
		extends Job
	{
		private IJavaElement[] selectedJavaElements = null;
		
		private EdgeKind linkType = null;
		private ConcernModel concernModel = null;
		
		private ConcernEvent refreshLabelsEvent = null; 

		private boolean refreshLabels = false;
		
		public SelectedConcernsAsynchronousUpdater()
		{
			super("Highlighting...");
		}
		
		/**
		 * Same as {@link #syncUpdate} except asynchronous. 
		 */
		public void asyncUpdate(Set<IJavaElement> selectedJavaElements,
		                        ConcernEvent refreshLabelsEvent,
		                        boolean refreshLabels)
		{
			// We can't modify these member variables while run() is
			// still using them
			if (!this.cancel())
				waitUntilFinished();
				
			// Cache the selected elements since selectionChanged()
			// may alter them when we called re-entrantly
			if (selectedJavaElements != null)
				this.selectedJavaElements = 
					selectedJavaElements.toArray( new IJavaElement[]{} );

			// Cache these at the start
			linkType = concernTreeViewer.getLinkType();
			concernModel = concernTreeViewer.getModel();

			if (refreshLabelsEvent == null && refreshLabels)
				this.refreshLabelsEvent = new ConcernEvent();
			else
				this.refreshLabelsEvent = refreshLabelsEvent;
			
			this.refreshLabels = refreshLabels;

			// Spin up a new job to be run
			schedule();
		}

		/**
		 * Updates the currently selected concerns based on the currently
		 * selected Java elements.
		 * 
		 * @param selectedElements 
		 * 		Currently selected Java elements
		 * @param refreshLabelsEvent
		 * 		Should we refresh the labels after updating the selected
		 * 		concerns?
		 */
		public void syncUpdate(Set<IJavaElement> selectedElements,
		                       ConcernEvent refreshLabelsEvent,
		                       boolean refreshLabels)
		{
			asyncUpdate(selectedElements, refreshLabelsEvent, refreshLabels);
			waitUntilFinished();
		}

		//-------------------------------------------------
		// Job implementation
		//-------------------------------------------------
		
		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
			boolean debug = false;
			
			Set<Concern> selectedConcerns = null;

			for(int i = 0; 
				selectedJavaElements != null && 
				i < selectedJavaElements.length && 
				!monitor.isCanceled(); 
				++i)
			{
				if (debug)
					System.out.println("Selected element: " + 
							selectedJavaElements[i].getElementName() + " " +
							linkType.name());
				
				List<Concern> concernsLinkedToComponent =
					concernModel.getLinkedConcerns(
							selectedJavaElements[i], linkType);

				if (concernsLinkedToComponent != null)
				{
					// Walk array in reverse so it's easy to add elements without
					// invalidating the iteration
					for(int j = concernsLinkedToComponent.size()-1; 
						j >= 0 && !monitor.isCanceled(); 
						--j)
					{
						// Add ancestors to the array (at the end so we
						// don't upset the active iterator)
						
						Concern parentConcern = 
							concernsLinkedToComponent.get(j).getParent();
						while (parentConcern != null && !parentConcern.isRoot())
						{
							concernsLinkedToComponent.add(parentConcern);
							parentConcern = parentConcern.getParent();
						}
					}
					
					if (debug)
					{
						for(Concern concern : concernsLinkedToComponent)
						{
							System.out.println("  Intersection: " + 
									concern.getDisplayName() +
									" " + linkType.name());
						}
					}
	
					// Only select the linked concerns common to all the
					// selected elements
					if (selectedConcerns == null)
						selectedConcerns = new HashSet<Concern>(concernsLinkedToComponent);
					else
						selectedConcerns.retainAll(concernsLinkedToComponent);
				}
	
				// If there are no selected concerns at this point
				// then we can stop looking since the intersection
				// above won't change
				if (selectedConcerns == null)
					break;
			}

			if (debug)
			{
				if (selectedConcerns != null)
				{
					for(Concern concern : selectedConcerns)
					{
						System.out.println("  Linked to: " + 
								concern.getDisplayName() +
								" " + linkType.name());
					}
				}
				else
				{
					System.out.println("  Linked to: nothing " + linkType.name());
				}
			}

			if (monitor.isCanceled() || 
				Comparer.safeEquals(currentlySelectedConcerns, selectedConcerns))
			{
				// We were told to abort OR
				// Nothing is highlighted (still) OR
				// Currently selected concerns didn't change
				if (debug)
					System.out.println("  No change " + linkType.name());
			}
			else
			{
				// Create events to update the labels of the previously
				// highlighted concerns and the newly highlighted concerns
				addUpdateLabelEvents(refreshLabelsEvent, 
						currentlySelectedConcerns, selectedConcerns);
			
				// Update the selected concerns
				currentlySelectedConcerns = selectedConcerns;
			}
				
			if (refreshLabels && refreshLabelsEvent.isUpdateLabel())
			{
				if (debug)
				{
					System.out.println("Refresh Events:");
					for(ConcernEvent event : refreshLabelsEvent)
					{
						System.out.println("  " + event);
					}
				}
				
				// ConcernTreeViewer does the actual refresh
				concernTreeViewer.refresh(refreshLabelsEvent);
			}
			
			return Status.OK_STATUS;
		}

		//-------------------------------------------------
		// PRIVATE HELPER METHODS
		//-------------------------------------------------
		
		private void waitUntilFinished()
		{
			try
			{
				// Wait until we've finished or we're interrupted
				join(); 
			}
			catch (InterruptedException e)
			{
				ProblemManager.reportException(e, 
						"Failed to determine selected concerns", true);
			}
		}
	}
}
