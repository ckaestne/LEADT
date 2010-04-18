package edu.wm.flat3.metrics;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.action.IMenuManager;

import edu.wm.flat3.actions.OpenConcernDomainAction;
import edu.wm.flat3.actions.SetLinkTypeAction;

public abstract class CoverageMetricsView 
	extends MetricsView
{
	protected MetricsTool metricsTool = new MetricsTool(this);

	private String name;
	private String titlePostfix;
	
	private OpenConcernDomainAction openConcernDomainAction;
	private SetLinkTypeAction setLinkTypeAction;
	
	public CoverageMetricsView(MetricsTable metricsTable,
	                           String name,
	                           String titlePostfix)
	{
		super(new CoverageMetricsTable());
		
		this.name = "Coverage";
		this.titlePostfix = "Stats";
		
		// True means disable currently selected domain
		openConcernDomainAction = new OpenConcernDomainAction(this, true);
		setLinkTypeAction = new SetLinkTypeAction(this);
	}

	@Override
	protected void fillToolBarMenu(IMenuManager pManager)
	{
		pManager.add( openConcernDomainAction );
		pManager.add( setLinkTypeAction );
	}

	@Override
	protected void handleDomainAndLinkTypeChanges()
	{
		String domainAndLinkType;
		
		if (!concernModel.getConcernDomain().isDefault())
		{
			domainAndLinkType = concernModel.getConcernDomain().getName();
			setPartName(domainAndLinkType + " " + titlePostfix);
		}
		else
		{
			domainAndLinkType = "Default";
			setPartName(name + " " + titlePostfix);
		}
		
		domainAndLinkType += " (Link type: " + linkType + ")"; 

		this.setTitleToolTip(name + " " + titlePostfix.toLowerCase() + 
				" for " + domainAndLinkType);
		saveAction.setSuggestedPrefix(
			domainAndLinkType.replace("Link type: ", ""));
		
		openConcernDomainAction.updateMenu();
		setLinkTypeAction.updateMenu();
	
		// Clear the table so user must press Refresh button
		metricsTable.clear();
		metricsTable.refresh();
	}

	@Override
	public IStatus doMetrics(IProgressMonitor progressMonitor)
	{
		// Calculate concern metrics for this concern
		metricsTool.getCoverageMetrics((CoverageMetricsTable) metricsTable, 
				progressMonitor);
				
		// Once we are finished, we need to refresh the display
		metricsTable.refresh();

		return Status.OK_STATUS;
	}
}
