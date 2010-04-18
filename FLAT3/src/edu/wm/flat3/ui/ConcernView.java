/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.ui;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.CheckConsistencyAction;
import edu.wm.flat3.actions.CollapseAllAction;
import edu.wm.flat3.actions.CreateRandomLinksAction;
import edu.wm.flat3.actions.ExportConcernsAction;
import edu.wm.flat3.actions.ExportLinksAction;
import edu.wm.flat3.actions.ImportConcernsAction;
import edu.wm.flat3.actions.ImportLinksAction;
import edu.wm.flat3.actions.NewConcernAction;
import edu.wm.flat3.actions.NewConcernDomainAction;
import edu.wm.flat3.actions.OpenConcernDomainAction;
import edu.wm.flat3.actions.OpenSearchViewAction;
import edu.wm.flat3.actions.RemoveAllConcernsAction;
import edu.wm.flat3.actions.RemoveAllLinksAction;
import edu.wm.flat3.actions.RenameConcernDomainAction;
import edu.wm.flat3.actions.ResetDatabaseAction;
import edu.wm.flat3.actions.SetLinkTypeAction;
import edu.wm.flat3.actions.ShowMetricsAction;
import edu.wm.flat3.analysis.lucene.actions.IndexProjectAction;
import edu.wm.flat3.analysis.lucene.actions.SetQueryAction;
import edu.wm.flat3.analysis.mutt.actions.StartTraceAction;
import edu.wm.flat3.analysis.visualization.VisualizeAction;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernListener;
import edu.wm.flat3.model.IConcernModelProviderEx;
import edu.wm.flat3.repository.ConcernDomain;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.ui.concerntree.ConcernTreeViewer;

/**
 * Implements a view of the Concern model associated with the ConcernMapper
 * plug-in.
 */
public class ConcernView 
	extends 
		ViewPart 
	implements
		IConcernListener, IConcernModelProviderEx, IPropertyChangeListener
{
	private ConcernTreeViewer viewer;

	private EdgeKind concernComponentRelation;
	private ConcernModel concernModel;

	private ExportConcernsAction exportConcernsAction = null;
	private ExportLinksAction exportLinksAction = null;
	
	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 * 
	 * @param pParent
	 *            The parent widget.
	 */
	@Override
	public void createPartControl(Composite pParent)
	{
		String concernDomain = OpenConcernDomainAction.extractConcernDomainFromSecondaryId(
				getViewSite().getSecondaryId());

		concernModel = ConcernModelFactory.singleton().getConcernModel(concernDomain);
		concernComponentRelation = concernModel.getDefaultLinkType();
		
		exportConcernsAction = new ExportConcernsAction(this, 
				getViewSite().getActionBars().getStatusLineManager());
		
		exportLinksAction = new ExportLinksAction(this, 
				getViewSite().getActionBars().getStatusLineManager());
		
		updateTitleAndToolTip();

		GridLayout lLayout = new GridLayout();
		lLayout.numColumns = 1;
		lLayout.horizontalSpacing = 0; // remove if not needed
		lLayout.verticalSpacing = 0; // remove if not needed
		lLayout.marginHeight = 0; // remove if not needed
		lLayout.marginWidth = 0; // remove if not needed
		pParent.setLayout(lLayout);
		pParent.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = new ConcernTreeViewer(pParent, this, this, getViewSite());
		viewer.init(pParent);

		// Add elements to the action bars
		IActionBars lBars = getViewSite().getActionBars();
		fillLocalToolBar(lBars.getToolBarManager());
		fillToolBarMenu(lBars.getMenuManager());

		// Add this view as a listener for model and property change events
		concernModel.addListener(this);
		FLATTT.singleton().getPreferenceStore().addPropertyChangeListener(this);

		// Artificial call to refresh the view
		modelChanged(ConcernEvent.createAllConcernsChanged());
	}

	public void updateTitleAndToolTip()
	{
		if (concernModel == null)
			return;
		
		ConcernDomain concernDomain = concernModel.getConcernDomain();
		if (concernDomain == null)
			return;
		
		if (concernDomain.isDefault())
		{
			this.setPartName("Features");
			this.setTitleToolTip("Features (Relation: " + concernComponentRelation + ")");
			
			exportConcernsAction.setSuggestedPrefix("concerns");
			
			exportLinksAction.setSuggestedPrefix(
					"component_" + 
					concernComponentRelation.toString().toLowerCase() + 
					"_concern");
		}
		else
		{
			String domainName = concernDomain.getName();
			
			this.setPartName(domainName);
			this.setTitleToolTip(domainName + 
				" Concerns (Relation: " + concernComponentRelation + ")");

			exportConcernsAction.setSuggestedPrefix(domainName);
			
			exportLinksAction.setSuggestedPrefix(
					"component_" + 
					concernComponentRelation.toString().toLowerCase() + 
					"_" + concernDomain.getSingularName().toLowerCase());
		}
	}
	
	@Override
	public void setConcernDomain(String concernDomain)
	{
		OpenConcernDomainAction.openConcernDomainHelper(getViewSite(), concernDomain);
	}
	
	public void setLinkType(EdgeKind edgeKind)
	{
		if (concernComponentRelation == edgeKind)
			return;
		
		concernComponentRelation = edgeKind;

		updateTitleAndToolTip();
		
		ConcernEvent event = ConcernEvent.createLinkTypeChangedEvent();
		
		modelChanged(event);
		ConcernModelFactory.singleton().modelChanged(event);
	}

	public EdgeKind getLinkType()
	{
		return concernComponentRelation;
	}
	
	public ConcernModel getModel()
	{
		return concernModel;
	}
	
	/**
	 * Collapses the element tree.
	 */
	public void collapseAll()
	{
		viewer.collapseAll();
	}

	/**
	 * @see edu.wm.flat3.model.IConcernListener#modelChanged(int)
	 * @param pType
	 *            The type of change to the model. See the constants in
	 *            ConcernModel
	 */
	public void modelChanged(ConcernEvent event)
	{
		if (event.isChangedDomainName())
		{
			// Renaming our concern domain is a big deal since is stored in
			// the view's secondary id, which is used by Eclipse to restore
			// views on opening.  We have to 'restart' the view by creating
			// a new one and closing the old one.
			
			OpenConcernDomainAction.openConcernDomainHelper(getSite(), 
					concernModel.getConcernDomain().getName());
			
			closeMe();
			return;
		}		

		boolean updateActionState = false;

		Display lDisplay = viewer.getControl().getDisplay();
		
		if (viewer.getControl().isDisposed() || lDisplay.isDisposed())
		{
			updateActionState = true;
		}

		viewer.refresh(event);
		
		// Updates the action buttons to reflect the state of the plugin
		if (updateActionState)
			getViewSite().getActionBars().updateActionBars();
	}

	// Is there a better way?
	public void closeMe()
	{
		Display lDisplay = viewer.getControl().getDisplay();
		
		lDisplay.asyncExec(new Runnable()
			{
				public void run()
				{
					getSite().getPage().hideView(ConcernView.this);
				}
			}
		);
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 * @param pEvent
	 *            the property change event object describing which property
	 *            changed and how
	 */
	public void propertyChange(PropertyChangeEvent pEvent)
	{
		// No properties currently affect the way concerns are viewed
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		viewer.setFocus();
		ConcernModelFactory.singleton().setActiveConcernModelProvider(this);
	}

	/**
	 * Adds the action to the toolbar.
	 * 
	 * @param pManager
	 *            The toolbar manager.
	 */
	private void fillLocalToolBar(IToolBarManager pManager)
	{

	//	pManager.add(new VisualizeAction(getSite()));
		pManager.add(new StartTraceAction());
		pManager.add(new SetQueryAction(getSite()));

		pManager.add(new Separator());
		pManager.add(new NewConcernAction(viewer.getTree().getShell(), 
				viewer, viewer.getModel().getRoot() ));
		pManager.add(new CollapseAllAction(this));
	}

	/**
	 * Adds the actions to the menu.
	 * 
	 * @param pManager
	 *            the menu manager.
	 */
	private void fillToolBarMenu(IMenuManager pManager)
	{
		IStatusLineManager statusLineManager = 
			getViewSite().getActionBars().getStatusLineManager();
		
		// Don't disable the same concern domain since we might want to open
		// a new view
		pManager.add( new OpenConcernDomainAction(this, false) );
		
		pManager.add( new NewConcernDomainAction(this, getSite()) );
		pManager.add( new RenameConcernDomainAction(this, getViewSite() ));

		pManager.add( new Separator() );
		
		pManager.add( new ImportConcernsAction(this, statusLineManager) );
		pManager.add( exportConcernsAction );
		pManager.add( new RemoveAllConcernsAction(this, statusLineManager) );

		pManager.add( new Separator() );
		
		pManager.add( new ImportLinksAction(this, statusLineManager) );
		pManager.add( exportLinksAction );
		pManager.add( new SetLinkTypeAction(this) );
		pManager.add( new CheckConsistencyAction(this, statusLineManager) );
		pManager.add( new RemoveAllLinksAction(this, statusLineManager) );
		pManager.add( new CreateRandomLinksAction(this) );

		pManager.add( new Separator() );
		
		pManager.add( new ShowMetricsAction(concernModel.getConcernDomain(), getSite()) );
		
		pManager.add( new Separator() );
		
		pManager.add( new ResetDatabaseAction(this) );
		
		pManager.add( new Separator() );
		pManager.add( new IndexProjectAction() );
		pManager.add( new SetQueryAction(getSite()) );
	}

	/**
	 * Called when the view is closed. Deregister the view as a listener to the
	 * model.
	 */
	@Override
	public void dispose()
	{
		if (concernModel != null)
			concernModel.removeListener(this);
		
		FLATTT.singleton().getPreferenceStore()
				.removePropertyChangeListener(this);

		if (viewer != null)
			viewer.dispose();
		
		ConcernModelFactory.singleton().clearActiveConcernModelProvider(concernModel);
		super.dispose();
	}
}
