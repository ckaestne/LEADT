package edu.wm.flat3.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.metrics.IRefreshableView;

/**
 * Class that refreshes the metrics view.
 * 
 * @author vgarg
 * 
 */
public class RefreshMetricsAction extends Action
{
	private IRefreshableView refreshableView;

	public RefreshMetricsAction(IRefreshableView refreshableView)
	{
		this.refreshableView = refreshableView;
		setText(FLATTT
				.getResourceString("actions.RefreshMetricAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/action_refresh.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.RefreshMetricAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		if (refreshableView != null)
			refreshableView.updateMetricsAsync();
	}
}
