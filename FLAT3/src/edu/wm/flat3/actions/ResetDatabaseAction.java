package edu.wm.flat3.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProvider;

/**
 * Action that resets the database.
 * 
 * @author vgarg
 * 
 */
public class ResetDatabaseAction extends Action
{
	private IConcernModelProvider concernModelProvider;
	
	public ResetDatabaseAction(IConcernModelProvider concernModelProvider)
	{
		this.concernModelProvider = concernModelProvider;
		
		setText(FLATTT
				.getResourceString("actions.ResetDatabaseAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/exclamation_point.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.ResetDatabaseAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		boolean resetOK = MessageDialog.openQuestion(
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
			FLATTT.getResourceString("actions.ResetDatabaseAction.DialogTitle"),
			FLATTT.getResourceString("actions.ResetDatabaseAction.DialogMessage"));

		if (resetOK)
		{
			concernModelProvider.getModel().resetDatabase();
			
		}
	}

}
