package edu.wm.flat3.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.metrics.ScatteringMetricsView;
import edu.wm.flat3.repository.ConcernDomain;
import edu.wm.flat3.util.ProblemManager;

public class ShowMetricsAction extends Action
{
	private ConcernDomain concernDomain;
	private IWorkbenchPartSite site;

	public ShowMetricsAction(	ConcernDomain concernDomain,
								IWorkbenchPartSite site)
	{
		this.concernDomain = concernDomain;
		this.site = site;
		
		setText(FLATTT
				.getResourceString("actions.ShowMetricsViewAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/chart_bar.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.ShowMetricsViewAction.ToolTip"));
	}

	@Override
	public void run()
	{
		try
		{
			
			String secondaryId = OpenConcernDomainAction.createSecondaryId(
					FLATTT.ID_SCATTERING_METRICS_VIEW, 
					concernDomain.getName());
			
			IViewReference firstView = site.getPage().findViewReference(FLATTT.ID_SCATTERING_METRICS_VIEW);
			if (firstView == null)
			{
				// There are no Metrics views currently.  We have to create
				// the first one.
				
				IViewPart viewPart = site.getPage().showView(FLATTT.ID_SCATTERING_METRICS_VIEW);
				
				assert viewPart != null;
				assert viewPart instanceof ScatteringMetricsView;
				
				ScatteringMetricsView metricsView = (ScatteringMetricsView) viewPart;
				metricsView.setConcernDomain(concernDomain.getName());
				
				site.getPage().activate(metricsView);
			}
			else
			{
				// There is at least one metrics view.  The remaining metrics
				// views must use a secondary id
				
				IViewReference domainSpecificView =
					site.getPage().findViewReference(FLATTT.ID_SCATTERING_METRICS_VIEW, 
							secondaryId);
				
				if (domainSpecificView != null)
				{
					// There is already a metrics view open for this concern
					// domain so activate it
					site.getPage().activate(domainSpecificView.getPart(false));
				}
				else
				{
					IViewPart viewPart = site.getPage().showView(
							FLATTT.ID_SCATTERING_METRICS_VIEW, 
							secondaryId, 
							IWorkbenchPage.VIEW_ACTIVATE | IWorkbenchPage.VIEW_CREATE);
					
					assert viewPart != null;
					assert viewPart instanceof ScatteringMetricsView;
					
					ScatteringMetricsView metricsView = (ScatteringMetricsView) viewPart;
					metricsView.setConcernDomain(concernDomain.getName());
					site.getPage().activate(metricsView);
				}
			}
		}
		catch (PartInitException e)
		{
			ProblemManager.reportException(e);
		}
	}
}
