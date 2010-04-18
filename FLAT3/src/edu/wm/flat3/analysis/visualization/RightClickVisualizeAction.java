/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.analysis.visualization;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.plaf.basic.BasicSliderUI.ActionScroller;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.mutt.MUTTTrace;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.ui.concerntree.ConcernTreeItem;
import edu.wm.flat3.util.ProblemManager;

/**
 * An action to add a new concern to the model.
 */
public class RightClickVisualizeAction extends Action
{	
	 List<ConcernTreeItem> selection;
	 IWorkbenchPartSite site;
	
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public RightClickVisualizeAction(IWorkbenchPartSite site)
	{
		this.site = site;
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/map.png"));

		setText(FLATTT
					.getResourceString("actions.VisualizeRightClickAction.Label"));
		setToolTipText(FLATTT
					.getResourceString("actions.VisualizeRightClickAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		
		try
		{
			// create the list to visualize
			ArrayList<FLATTTMember> nodes = new ArrayList<FLATTTMember>();
			
			for ( ConcernTreeItem item : selection ) {
				if (item.getJavaElement() == null) {
					Concern conc = item.getConcern();
				
					Collection<Component>  colec = conc.getLinks(EdgeKind.CONTAINS);
					colec.addAll(conc.getLinks(EdgeKind.RELATED_TO));
					colec.addAll(conc.getLinks(EdgeKind.DEPENDS_ON_REMOVAL));
					colec.addAll(conc.getLinks(EdgeKind.EXECUTED_BY));
					colec.addAll(conc.getLinks(EdgeKind.FIXED_FOR));
					for (Component comp : colec) {
						if (comp.getJavaElement() instanceof IMember) 
							nodes.add(new FLATTTMember((IMember)comp.getJavaElement()));
					}					       
				}
			}
			
			// Then load the view and draw the visualization.			
			IViewReference firstView = site.getPage().findViewReference(FLATTT.ID_VISUALIZATION_VIEW);
			if (firstView == null)
			{
				// There are no Metrics views currently.  We have to create
				// the first one.
				
				IViewPart viewPart = site.getPage().showView(FLATTT.ID_VISUALIZATION_VIEW);
				
				assert viewPart != null;
				assert viewPart instanceof VisualizationView;
				
				VisualizationView metricsView = (VisualizationView) viewPart;
				//metricsView.setConcernDomain(concernDomain.getName());
				
				site.getPage().activate(metricsView);
			}
			else
			{
				// There is at least one metrics view.  The remaining metrics
				// views must use a secondary id
				// TODO: Tidy this up. Fixed visualization button not working bug though.

				IViewReference domainSpecificView =
					site.getPage().findViewReference(FLATTT.ID_VISUALIZATION_VIEW);
				
				if (domainSpecificView != null)
				{
					// There is already a metrics view open for this concern
					// domain so activate it
					site.getPage().activate(site.getPage().showView(FLATTT.ID_VISUALIZATION_VIEW));
				}
			/*	else
				{
					IViewPart viewPart = site.getPage().showView(
							FLATTT.ID_VISUALIZATION_VIEW, 
							null, 
							IWorkbenchPage.VIEW_ACTIVATE | IWorkbenchPage.VIEW_CREATE);
					
					assert viewPart != null;
					assert viewPart instanceof VisualizationView;
					
					VisualizationView metricsView = (VisualizationView) viewPart;
					//metricsView.setConcernDomain(concernDomain.getName());
					site.getPage().activate(metricsView);
				}*/
			}
			
			// Update visualization
			FLATTT.visualizationView.imageCanvas.drawVisualization(nodes);
		}
		catch (PartInitException e)
		{
			ProblemManager.reportException(e);
		}
	}

	public void setSelection(List<ConcernTreeItem> selectedConcernItems) {
		 selection = selectedConcernItems;
	}
	
}
