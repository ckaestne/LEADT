package edu.wm.flat3.metrics;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import edu.wm.flat3.actions.OpenConcernDomainAction;
import edu.wm.flat3.actions.RefreshMetricsAction;
import edu.wm.flat3.actions.SaveMetricsAction;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernListener;
import edu.wm.flat3.model.IConcernModelProviderEx;
import edu.wm.flat3.repository.ConcernRepository;
import edu.wm.flat3.repository.EdgeKind;

/**
 * Base class for all metrics-based views
 * 
 * @author Marc Eaddy
 * 
 */
public abstract class MetricsView 
	extends 
		ViewPart
	implements 
		IConcernListener, IConcernModelProviderEx, IRefreshableView
{
	protected ConcernModel concernModel;
	protected EdgeKind linkType;

	private MetricsJob job = null;
	
	protected MetricsTable metricsTable;

	// Put this after concernMetricsTable above is instantiated!
	protected SaveMetricsAction saveAction;
	
	public MetricsView(MetricsTable metricsTable)
	{
		this.metricsTable = metricsTable;
		saveAction = new SaveMetricsAction(metricsTable);
	}

	abstract protected void handleDomainAndLinkTypeChanges();
	abstract public IStatus doMetrics(IProgressMonitor progressMonitor);
	
	/**
	 * Adds the actions to the menu.
	 * 
	 * @param pManager
	 *            the menu manager.
	 */
	abstract protected void fillToolBarMenu(IMenuManager pManager);
	
	@Override
	public void createPartControl(Composite parent)
	{
		// Create a composite to hold the children
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_BOTH);
		parent.setLayoutData(gridData);

		metricsTable.initialize(parent);
		
		// Add elements to the action bars
		IActionBars lBars = getViewSite().getActionBars();
		fillLocalToolBar(lBars.getToolBarManager());
		fillToolBarMenu(lBars.getMenuManager());
		
		String concernDomain = OpenConcernDomainAction.extractConcernDomainFromSecondaryId(
				getViewSite().getSecondaryId());

		setConcernDomain(concernDomain);
	}
	
	@Override
	final public void setConcernDomain(String concernDomain)
	{
		if (concernModel != null && 
			concernModel.getConcernDomain() != null &&
			concernModel.getConcernDomain().equals(concernDomain))
		{
			return;
		}
		
		// True means create the database if it doesn't exist
		ConcernRepository repository = ConcernRepository.openDatabase();
		
		concernModel = ConcernModelFactory.singleton().getConcernModel(
				repository, concernDomain);

		// This is gheto since it ignores the relation established by the
		// launching concern view, it just uses the currently active one
		linkType = ConcernModelFactory.singleton().getLinkType();
		if (!concernModel.getRoot().isLinked(linkType))
		{
			linkType = concernModel.getDefaultLinkType();
		}

		concernModel.addListener(this);

		handleDomainAndLinkTypeChanges();
	}
	
	@Override
	final public void setLinkType(EdgeKind linkType)
	{
		if (this.linkType == linkType)
			return;
		
		this.linkType = linkType;

		handleDomainAndLinkTypeChanges();
		
		updateMetricsAsync();
	}

	@Override
	final public void setFocus()
	{
		metricsTable.setFocus();
	}

	@Override
	final public void updateMetricsAsync()
	{
		if (job != null)
			job.cancel();

		// Clear the metrics and refresh the display
		metricsTable.clear();
		metricsTable.refresh();
		
		// Calculate the metrics in a concurrent worker job
		job = new MetricsJob(
				"Calculating metrics for '" +
				concernModel.getConcernDomain().toString() + "'");
		job.schedule();
	}

	@Override
	final public void dispose()
	{
		if (job != null)
			job.cancel();
		super.dispose();
	}

	@Override
	public void modelChanged(ConcernEvent event)
	{
		if (event.isChangedDomainName())
		{
			// Renaming our concern domain is a big deal since is stored in
			// the view's secondary id, which is used by Eclipse to restore
			// views on opening.  We have to 'restart' the view by creating
			// a new one and closing the old one.
			
			closeMe();
		}		
	}

	// Is there a better way?
	private void closeMe()
	{
		Display display = Display.getDefault();
		if (display == null)
			return;
		
		display.asyncExec(new Runnable()
			{
				public void run()
				{
					getSite().getPage().hideView(MetricsView.this);
				}
			}
		);
	}

	@Override
	public EdgeKind getLinkType()
	{
		return linkType;
	}

	@Override
	public ConcernModel getModel()
	{
		return concernModel;
	}

	private void fillLocalToolBar(IToolBarManager pManager)
	{
		pManager.add( new RefreshMetricsAction(this) );
		pManager.add(saveAction);
	}
	
	//-----------------------------------------------------
	// HELPER CLASSES
	//-----------------------------------------------------
	
	/**
	 * Calculates the concern metrics in a separate job thread.
	 */
	private final class MetricsJob 
		extends Job
	{
		IProgressMonitor myProgressMonitor;
		
		private MetricsJob(String name)
		{
			super(name);
		}

		@Override
		public IStatus run(IProgressMonitor progressMonitor)
		{
			if (concernModel == null)
				return null; // Causes null exception to be thrown
			
			myProgressMonitor = progressMonitor;
			
			// Clear the metrics since we are recalculating them
			metricsTable.clear();
			
			IStatus status = doMetrics(myProgressMonitor);
			
			if (myProgressMonitor != null)
				myProgressMonitor.done();
			
			return status;
		}
	}
}
