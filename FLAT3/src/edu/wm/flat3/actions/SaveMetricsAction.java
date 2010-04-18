package edu.wm.flat3.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.metrics.MetricsTable;
import edu.wm.flat3.util.ProblemManager;

public class SaveMetricsAction
	extends Action
{
	private MetricsTable metricsTable;
	private String suggestedPrefix = "";
	
	public SaveMetricsAction(MetricsTable metricsTable)
	{
		this.metricsTable = metricsTable;

		setText(FLATTT
				.getResourceString("actions.SaveMetricsAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/action_save.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.SaveMetricsAction.ToolTip"));
	}
	
	public void setSuggestedPrefix(String suggestedPrefix)
	{
		this.suggestedPrefix = suggestedPrefix;
	}

	@Override
	public void run()
	{
		String fileExt = FLATTT.getResourceString("actions.SaveMetricsAction.FileExt");

		String path = "";
		
		boolean done = false;
		
		while (!done)
		{
			final FileDialog fileSaveDialog = new FileDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(), 
					SWT.SAVE);
			fileSaveDialog.setText(FLATTT
					.getResourceString("actions.SaveMetricsAction.DialogTitle"));
			fileSaveDialog.setFilterNames(new String[] { FLATTT
					.getResourceString("actions.SaveMetricsAction.DialogFilterName"),
					"All Files (*.*)"});
			fileSaveDialog.setFilterExtensions(new String[] { 
					"*" + fileExt,
					"*.*"});
	
			String suggested = suggestedPrefix;
			if (!suggested.isEmpty())
				suggested += ".";
			
			suggested += "metrics" + fileExt; 
	
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
		
		FileOutputStream stream = null;
		
		try
		{
			stream = new FileOutputStream(path);
			PrintStream out = new PrintStream(stream);
			metricsTable.output(out);
		}
		catch (IOException e)
		{
			ProblemManager.reportException(e);
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					ProblemManager.reportException(e);
				}
			}
		}
	}
}
