package edu.wm.flat3.actions;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.util.ConcernLinksARFFFile;

/**
 * @author eaddy
 * 
 */
public class ExportLinksAction extends Action
{
	private IConcernModelProvider concernModelProvider;
	private IStatusLineManager statusLineManager;
	private String suggestedPrefix = "";
	
	public ExportLinksAction(IConcernModelProvider concernModelProvider,
	                             IStatusLineManager statusLineManager)
	{
		this.concernModelProvider = concernModelProvider;
		this.statusLineManager = statusLineManager;
		
		setText(FLATTT
				.getResourceString("actions.ExportLinksAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/action_save.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.ExportLinksAction.ToolTip"));
	}

	public void setSuggestedPrefix(String suggestedPrefix)
	{
		this.suggestedPrefix = suggestedPrefix;
	}
	
	@Override
	public void run()
	{
		String fileExt = FLATTT.getResourceString(
				"actions.ExportLinksAction.FileExt");

		String path = "";
		
		boolean done = false;
		
		while (!done)
		{
			final FileDialog fileSaveDialog = new FileDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(),
					SWT.SAVE);
			fileSaveDialog.setText(
					FLATTT.getResourceString("actions.ExportLinksAction.DialogTitle"));
			fileSaveDialog.setFilterNames(new String[] { 
					FLATTT.getResourceString("actions.ExportLinksAction.DialogFilterName"),
					"All Files (*.*)"});
			fileSaveDialog.setFilterExtensions(new String[] { 
					"*" + fileExt,
					"*.*" });
			
			String suggested = suggestedPrefix;
			if (suggested.isEmpty())
				suggested = "links";
			
			suggested += fileExt; 
	
			fileSaveDialog.setFileName(suggested);
			
			path = fileSaveDialog.open();
			if (path == null || path.isEmpty())
				return;
	
			if (path.indexOf('.') == -1)
				path += fileExt;
			
			if (new File(path).exists())
			{
				done = MessageDialog.openQuestion(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Confirm File Overwrite",
						"The file already exists. Overwrite?");
			}
			else
			{
				done = true;
			}
		}
			
		final String pathForJob = path; 
		
		Job job = new Job("Exporting links...")
			{
				@Override
				protected IStatus run(IProgressMonitor progressMonitor)
				{
					return saveLinkFile(pathForJob, progressMonitor, 
							statusLineManager);
				}
		
			};
				
		job.setUser(true);
		job.schedule();
	}

	private IStatus saveLinkFile(final String path, 
	                             IProgressMonitor progressMonitor, 
	                             IStatusLineManager statusLineManager)
	{
		ConcernLinksARFFFile asf = new ConcernLinksARFFFile(	path, 
											concernModelProvider,
											progressMonitor,
											statusLineManager);
		asf.save();

		return Status.OK_STATUS;
	}
}
