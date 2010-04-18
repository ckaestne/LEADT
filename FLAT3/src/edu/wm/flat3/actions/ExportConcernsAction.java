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
import edu.wm.flat3.util.ConcernARFFFile;

/**
 * @author eaddy
 * 
 */
public class ExportConcernsAction extends Action
{
	private IConcernModelProvider concernModelProvider;
	private IStatusLineManager statusLineManager;
	private String suggestedPrefix = "";
	
	private boolean outputARFF = false;
	
	public ExportConcernsAction(IConcernModelProvider concernModelProvider,
	                             IStatusLineManager statusLineManager)
	{
		this.concernModelProvider = concernModelProvider;
		this.statusLineManager = statusLineManager;
		
		setText(FLATTT
				.getResourceString("actions.ExportConcernsAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/action_save.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.ExportConcernsAction.ToolTip"));
	}

	public void setSuggestedPrefix(String suggestedPrefix)
	{
		this.suggestedPrefix = suggestedPrefix;
	}
	
	@Override
	public void run()
	{
		String arffFileExt = FLATTT.getResourceString(
				"actions.ExportConcernsAction.FileExt1");
		String txtFileExt = FLATTT.getResourceString(
			"actions.ExportConcernsAction.FileExt2");

		String path = "";
		
		boolean done = false;
		
		while (!done)
		{
			final FileDialog fileSaveDialog = new FileDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(),
					SWT.SAVE);
			fileSaveDialog.setText(
					FLATTT.getResourceString("actions.ExportConcernsAction.DialogTitle"));
			fileSaveDialog.setFilterNames(new String[] { 
					FLATTT.getResourceString("actions.ExportConcernsAction.DialogFilterName1"),
					FLATTT.getResourceString("actions.ExportConcernsAction.DialogFilterName2") });
			fileSaveDialog.setFilterExtensions(new String[] { 
					"*" + arffFileExt,
					"*" + txtFileExt });
			
			String suggested = suggestedPrefix;
			if (suggested.isEmpty())
				suggested = "concerns";
			
			suggested += arffFileExt; 
	
			fileSaveDialog.setFileName(suggested);
			
			path = fileSaveDialog.open();
			if (path == null || path.isEmpty())
				return;
	
			if (path.lastIndexOf('.') == -1)
			{
				path += arffFileExt;
			}
			
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
			
		int lastDot = path.lastIndexOf('.');
		if (lastDot > -1)
		{
			String fileExt = path.substring(lastDot);
			outputARFF = fileExt.compareToIgnoreCase(arffFileExt) == 0;
		}
		else
		{
			outputARFF = true;
		}
		
		final String pathForJob = path; 
		
		Job job = new Job("Exporting concerns...")
			{
				@Override
				protected IStatus run(IProgressMonitor progressMonitor)
				{
					return saveConcernsToFile(pathForJob, progressMonitor,  statusLineManager);
				}
		
			};
				
		job.setUser(true);
		job.schedule();
	}

	private IStatus saveConcernsToFile(final String path, 
			IProgressMonitor progressMonitor, 
			IStatusLineManager statusLineManager)
	{
		ConcernARFFFile asf = new ConcernARFFFile(	path, 
													concernModelProvider,
													progressMonitor,
													statusLineManager);
		if (outputARFF)
			asf.save();
		else
			asf.saveWithIndention();

		return Status.OK_STATUS;
	}
}
