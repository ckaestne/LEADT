package edu.wm.flat3.analysis.visualization;

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


public class VisualizeAction extends Action
{
	private ConcernDomain concernDomain;
	private IWorkbenchPartSite site;

	public VisualizeAction(	
								IWorkbenchPartSite site)
	{
		//this.concernDomain = concernDomain;
		this.site = site;
		this.setEnabled(false);
		
		setText(FLATTT
				.getResourceString("actions.VisualizeAction.Label"));
		setImageDescriptor(FLATTT.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/map.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.VisualizeAction.ToolTip"));
	}

	@Override
	public void run()
	{
		FLATTT.singleton().openView(FLATTT.ID_VISUALIZATION_VIEW, site);
		// Update visualization
		FLATTT.visualizationView.imageCanvas.drawVisualization(FLATTT.searchResults);
	}
}
