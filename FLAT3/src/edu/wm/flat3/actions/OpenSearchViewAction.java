package edu.wm.flat3.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTTableView;
import edu.wm.flat3.repository.ConcernDomain;
import edu.wm.flat3.util.ProblemManager;


public class OpenSearchViewAction extends Action
{
	private ConcernDomain concernDomain;
	private IWorkbenchPartSite site;

	public OpenSearchViewAction(	
								IWorkbenchPartSite site)
	{
		//this.concernDomain = concernDomain;
		this.site = site;
		
		setText(FLATTT
				.getResourceString("actions.OpenSearchViewAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/text_search.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.OpenSearchViewAction.ToolTip"));
	}

	@Override
	public void run()
	{
		FLATTT.singleton().openView(FLATTT.ID_SEARCH_VIEW, site);
	}
}
