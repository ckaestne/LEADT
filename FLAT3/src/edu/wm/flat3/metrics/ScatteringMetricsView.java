package edu.wm.flat3.metrics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import edu.wm.flat3.actions.OpenConcernDomainAction;
import edu.wm.flat3.actions.SetLinkTypeAction;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernModelProviderEx;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.ConcernRepository;
import edu.wm.flat3.repository.EdgeKind;

public class ScatteringMetricsView 
	extends MetricsView
{
	private ConcernModel concernModelRhs = null;
	private EdgeKind linkTypeRhs = null;

	private LeftRightMenuAction leftAction = 
		new LeftRightMenuAction("Left", this );
	
	private LeftRightMenuAction rightAction =
		new LeftRightMenuAction("Right", new RhsListener() );
	
	protected MetricsTool metricsTool = new MetricsTool(this);
	
	private static final boolean includeAll = false;
	
	public ScatteringMetricsView()
	{
		super(new ScatteringMetricsTable());
	}
	
	@Override
	public void createPartControl(Composite parent)
	{
		super.createPartControl(parent);
		concernModelRhs = concernModel;
		linkTypeRhs = linkType;
		handleDomainAndLinkTypeChanges();
	}

	@Override
	protected void handleDomainAndLinkTypeChanges()
	{
		String lhs = "";
		if (concernModel != null && !concernModel.getConcernDomain().isDefault())
		{
			lhs = concernModel.getConcernDomain().getName() +
				" (Link type: " + linkType + ")";
		}
		
		String rhs = "";
		if (concernModelRhs != null && !concernModelRhs.getConcernDomain().isDefault())
		{
			rhs = concernModelRhs.getConcernDomain().getName() +
				" (Link type: " + linkTypeRhs + ")";
		}
		
		String toolTip;
		
		saveAction.setSuggestedPrefix("");
		
		if (!lhs.isEmpty())
		{
			if (!rhs.isEmpty())
			{
				toolTip = "Concern metrics for " + 
						lhs + " and " + rhs;

				saveAction.setSuggestedPrefix(
						lhs.replace("Link type: ", "") + "-" + 
						rhs.replace("Link type: ", ""));
			}
			else
			{
				toolTip = "Concern metrics for " + lhs;
			}
		}
		else
		{
			toolTip = "Concern metrics";
		}
		
		setTitleToolTip(toolTip);
		
		leftAction.updateMenu();
		rightAction.updateMenu();
	}
	
	public void setConcernDomainRhs(String concernDomain)
	{
		if (concernModelRhs != null && 
			concernModelRhs.getConcernDomain() != null &&
			concernModelRhs.getConcernDomain().equals(concernDomain))
		{
			return;
		}
		
		// True means create the database if it doesn't exist
		ConcernRepository repository = ConcernRepository.openDatabase();
	
		concernModelRhs = ConcernModelFactory.singleton().getConcernModel(
				repository, concernDomain);

		// Make sure we have a valid relation
		if (linkTypeRhs == null)
		{
			linkTypeRhs = ConcernModelFactory.singleton().getLinkType();
			if (!concernModelRhs.getRoot().isLinked(linkTypeRhs))
			{
				linkTypeRhs = concernModelRhs.getDefaultLinkType();
			}
		}
		
		concernModelRhs.addListener(this);

		handleDomainAndLinkTypeChanges();
	}
	
	@Override
	public IStatus doMetrics(IProgressMonitor progressMonitor)
	{
		if (progressMonitor != null)
			progressMonitor.beginTask("Concern", concernModel.getNumConcerns());
		
		return processRecursive(concernModel.getRoot(),
								(ScatteringMetricsTable) metricsTable,
								progressMonitor);
	}

	private IStatus processRecursive(Concern concern,
	                                 ScatteringMetricsTable intersectionMetricsTable,
	                                 IProgressMonitor progressMonitor)
	{
		if (progressMonitor != null && progressMonitor.isCanceled())
			return Status.CANCEL_STATUS;
		
		if (!concern.isRoot() && shouldIncludeConcern(concern))
		{
			if (progressMonitor != null)
				progressMonitor.subTask(concern.getDisplayName());

			// Calculate concern metrics for this concern
			DisplayableValues metrics = metricsTool.getMetricsForConcern(concern, 
					progressMonitor);

			if (progressMonitor != null && progressMonitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			if (isValidMetrics(metrics))
			{
				Set<Concern> tangledConcernsRhs = new TreeSet<Concern>();
	
				// Concerns A is considered tangled with concern B iff any
				// component linked (directly or indirectly, as determined
				// by the aggregation rules) to A is also linked (directly
				// or indirectly) to B.						
				
				Set<Component> linksToA = new HashSet<Component>(); 
				concern.getSelfAndDesecendantLinks(linkType, linksToA);
	
				// Use a set so we don't check the same component twice
				Set<Component> linksToCheck = new HashSet<Component>();
				
				for(Component linkToA : linksToA)
				{
					// Check all direct links to A
					linksToCheck.add(linkToA);
					
					// All descendants of components directly linked to A
					// are considered indirectly linked to A
					linksToCheck.addAll(linkToA.getDescendants());
	
					// Check if any ancestor of the component is directly
					// linked to B since this would mean the component
					// is indirectly linked to B, and thus tangled with A
					linksToCheck.addAll(linkToA.getAncestors());
				}
				
				for(Component linkToCheck : linksToCheck)
				{
					Collection<Concern> tangledConcernsForThisComponent =
						concernModelRhs.getLinkedConcerns(linkToCheck, 
								linkTypeRhs);
					
					if (tangledConcernsForThisComponent != null)
						tangledConcernsRhs.addAll(tangledConcernsForThisComponent);
				}
	
				metrics.addValue(tangledConcernsRhs.size());
				metrics.add(tangledConcernsRhs);
	
				intersectionMetricsTable.add(concern, metrics);
	
				// Once we are finished, we need to refresh the display
				metricsTable.refresh();
			}

			if (progressMonitor != null)
				progressMonitor.worked(1);
		}
		
		for(Concern child : concern.getChildren())
		{
			IStatus status = processRecursive(child, intersectionMetricsTable, progressMonitor);
			if (!status.isOK())
				return status;
		}
		
		return Status.OK_STATUS;
	}

	private static boolean shouldIncludeConcern(Concern concern)
	{
		// Only include leaf concerns
		return includeAll || concern.getChildren().isEmpty();
	}

	private static boolean isValidMetrics(DisplayableValues metrics)
	{
		float DSOC = metrics.getFloatValue(0);
		float DSOM = metrics.getFloatValue(1);
		
		return includeAll || (!Float.isNaN(DSOC) && !Float.isNaN(DSOM));
	}
	
	/**
	 * Adds the actions to the menu.
	 * 
	 * @param pManager
	 *            the menu manager.
	 */
	@Override
	protected void fillToolBarMenu(IMenuManager pManager)
	{
		pManager.add( leftAction );
		pManager.add( rightAction );
	}
	
	class LeftRightMenuAction
		extends Action
		implements IMenuCreator
	{
		private Menu menu = null;
		private IConcernModelProviderEx concernModelListener;
		
		public LeftRightMenuAction(String prefix,
		                           IConcernModelProviderEx concernModelListener)
		{
			this.setText(prefix + "-Hand Side");
			
			this.concernModelListener = concernModelListener;
			
			setMenuCreator(this);
		}
		
		@Override
		public void dispose()
		{ 
			if (menu != null && !menu.isDisposed())
			{
				menu.dispose();
				menu = null;
			}
		}

		@Override
		public Menu getMenu(Control parent)
		{
			return null;
		}

		@Override
		public Menu getMenu(Menu parent)
		{
			dispose();
			
			menu = new Menu(parent);
			updateMenu();
			return menu;
		}
		
		public void updateMenu()
		{
			// Ignore update requests that occur before the menu
			// is initialized
			if (menu == null || menu.isDisposed())
				return;
			
			for(MenuItem child : menu.getItems())
			{
				assert !child.isDisposed();
				child.dispose();
			}
			
			// V
			//	Left-Hand Side >
			//				Domain >
			//					ECMA Spec
			//					Bugs
			//				Relation >
			//					CONTAINS
			//					RELATED_TO
			//	Right-Hand Side >
			//				Domain >
			//					ECMA Spec
			//					Bugs
			//				Relation >
			//					CONTAINS
			//					RELATED_TO

			//				Domain >
			//					ECMA Spec
			//					Bugs


			// true means disable the currently selected domain
			OpenConcernDomainAction domainMenuAction = 
				new OpenConcernDomainAction(concernModelListener, true);
			
			MenuItem domainMenuItem =  new MenuItem(menu, SWT.CASCADE);
			domainMenuItem.setText("Domain");

			// Create Domain's > and child items
			Menu domainChildMenu = new Menu(domainMenuItem);
			domainMenuItem.setMenu(domainMenuAction.getMenu(domainChildMenu));

			//				Link Type >
			//					CONTAINS
			//					RELATED_TO

			// Create Relations' >
			MenuItem relationMenuItem = new MenuItem(menu, SWT.CASCADE);
			relationMenuItem.setText("Link Type");
			
			SetLinkTypeAction relationMenuAction = 
				new SetLinkTypeAction(concernModelListener);
			
			// Create Relations' > and child items
			Menu relationChildMenu = new Menu(relationMenuItem);
			relationMenuItem.setMenu(relationMenuAction.getMenu(relationChildMenu));
		}
	}
	
	class RhsListener implements IConcernModelProviderEx
	{
		@Override
		public EdgeKind getLinkType()
		{
			return linkTypeRhs;
		}

		@Override
		public void setLinkType(EdgeKind edgeKind)
		{
			linkTypeRhs = edgeKind;
			handleDomainAndLinkTypeChanges();
		}

		@Override
		public void setConcernDomain(String concernDomain)
		{
			 // Set RHS
			ScatteringMetricsView.this.setConcernDomainRhs(concernDomain);
		}

		@Override
		public ConcernModel getModel()
		{
			return concernModelRhs;
		}
	}
}
