package edu.wm.flat3.actions;

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
import edu.wm.flat3.util.ConcernLinksARFFFile;

/**
 * @author eaddy
 * 
 */
public class ImportLinksAction extends Action
{
	private IConcernModelProvider concernModelProvider;
	private IStatusLineManager statusLineManager;
	
	public ImportLinksAction(IConcernModelProvider concernModelProvider,
	                             IStatusLineManager statusLineManager)
	{
		this.concernModelProvider = concernModelProvider;
		this.statusLineManager = statusLineManager;
		
		setText(FLATTT
				.getResourceString("actions.ImportLinksAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/table_link.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.ImportLinksAction.ToolTip"));
	}

	@Override
	public void run()
	{
		final FileDialog fileOpenDialog = new FileDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		fileOpenDialog.setText(
				FLATTT.getResourceString("actions.ImportLinksAction.DialogTitle"));
		fileOpenDialog.setFilterNames(new String[] { 
				FLATTT.getResourceString("actions.ImportLinksAction.DialogFilterName") });
		fileOpenDialog.setFilterExtensions(new String[] {
				FLATTT.getResourceString("actions.ImportLinksAction.DialogFilterExt") });
		fileOpenDialog.open();

		final String[] fileNames = fileOpenDialog.getFileNames();
		if (fileNames == null || fileNames.length == 0)
			return;

		Job job = new Job("Importing links...")
			{
				@Override
				protected IStatus run(IProgressMonitor progressMonitor)
				{
					return readConcernLinksFiles(fileOpenDialog.getFilterPath(), fileNames, 
							progressMonitor, statusLineManager);
				}
		
			};
				
		job.setUser(true);
		job.schedule();
	}

	private IStatus readConcernLinksFiles(final String dir, final String[] fileNames, 
			IProgressMonitor progressMonitor, IStatusLineManager statusLineManager)
	{
		for (String fileName : fileNames)
		{
			String path = dir + java.io.File.separator + fileName;

			ConcernLinksARFFFile asf = 
				new ConcernLinksARFFFile(	path, 
												concernModelProvider,
												progressMonitor,
												statusLineManager);
			asf.read();
		}

		return Status.OK_STATUS;
	}
}
