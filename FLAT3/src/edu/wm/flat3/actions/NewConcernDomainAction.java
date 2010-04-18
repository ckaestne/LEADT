package edu.wm.flat3.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.ConcernDomain;

public class NewConcernDomainAction extends Action
{
	private IConcernModelProvider concernModelProvider;
	private IWorkbenchPartSite site;
	
	public NewConcernDomainAction(	IConcernModelProvider concernModelProvider,
									IWorkbenchPartSite site)
	{
		this.concernModelProvider = concernModelProvider;
		this.site = site;
		
		setText(FLATTT
				.getResourceString("actions.NewConcernDomainAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/concern_domain.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.NewConcernDomainAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		InputDialog lDialog = new InputDialog(
				site.getShell(),
				FLATTT.getResourceString("actions.NewConcernDomainAction.DialogTitle"),
				FLATTT.getResourceString("actions.NewConcernDomainAction.DialogLabel"),
				"", 
				new IInputValidator()
				{
					public String isValid(String pName)
					{
						return ConcernDomain.isNameValid(pName);
					}
				});

		if (lDialog.open() != Window.OK)
			return;

		ConcernDomain concernDomainJustAdded = 
			concernModelProvider.getModel().createConcernDomain(lDialog.getValue(), "", "", "", null);

		assert concernDomainJustAdded.getName() == lDialog.getValue();
		
		OpenConcernDomainAction.openConcernDomainHelper(site, 
				concernDomainJustAdded.getName());
	}
}
