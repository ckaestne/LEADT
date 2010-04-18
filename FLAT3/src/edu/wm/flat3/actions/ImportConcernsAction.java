package edu.wm.flat3.actions;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.swt.widgets.FileDialog;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.util.ConcernARFFFile;

/**
 * @author eaddy
 * 
 */
public class ImportConcernsAction extends Action
{
	private IConcernModelProvider concernModelProvider;
	private IStatusLineManager statusLineManager;
	
	public ImportConcernsAction(IConcernModelProvider concernModelProvider,
	                          IStatusLineManager statusLineManager)
	{
		this.concernModelProvider = concernModelProvider;
		this.statusLineManager = statusLineManager;
		
		setText(FLATTT
				.getResourceString("actions.ImportConcernsAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/concerns.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.ImportConcernsAction.ToolTip"));
	}

	@Override
	public void run()
	{
		final FileDialog fileOpenDialog = new FileDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());
		fileOpenDialog.setText(FLATTT
				.getResourceString("actions.ImportConcernsAction.DialogTitle"));
		fileOpenDialog.setFilterNames(new String[] { FLATTT
				.getResourceString("actions.ImportConcernsAction.DialogFilterName") });
		fileOpenDialog.setFilterExtensions(new String[] { FLATTT
				.getResourceString("actions.ImportConcernsAction.DialogFilterExt") });
		fileOpenDialog.open();

		final String[] fileNames = fileOpenDialog.getFileNames();
		if (fileNames == null || fileNames.length == 0)
			return;

		Job job = new Job("Importing concerns...")
		{
			@Override
			protected IStatus run(IProgressMonitor progressMonitor)
			{
				readConcernFiles(fileOpenDialog.getFilterPath(), fileNames, 
						progressMonitor, statusLineManager);

				return Status.OK_STATUS;
			}
	
		};
			
		job.setUser(true);
		job.schedule();
	}

	private void readConcernFiles(final String dir, final String[] fileNames,
			IProgressMonitor progressMonitor, IStatusLineManager statusLineManager)
	{
		for (String fileName : fileNames)
		{
			String path = dir + File.separator + fileName;

			ConcernARFFFile concernArffFile = new ConcernARFFFile(path, 
					concernModelProvider, progressMonitor, statusLineManager);

			concernArffFile.read();
		}
	}
}
