/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.analysis.mutt.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.mutt.MUTTTrace;

/**
 * An action to add a new concern to the model.
 */
public class ExportTraceAction extends Action
{
	IViewPart view; 
	
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public ExportTraceAction(IViewPart view)
	{
		this.view = view;
		this.setEnabled(false);
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/chart_line_save.png"));

		setText(FLATTT
					.getResourceString("actions.ExportTraceAction.Label"));
		setToolTipText(FLATTT
					.getResourceString("actions.ExportTraceAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		
		if (FLATTT.searchResultsAreTrace) {
			try {
				// TODO: file dialog
				 FileDialog fileDialog = new FileDialog(view.getViewSite().getShell(), SWT.SAVE);
				 fileDialog.setFilterNames(new String[] {"Zip Files (*.zip)"});
				 fileDialog.setFilterExtensions(new String[] {"*.zip"});
				 fileDialog.setText("Save trace data...");
				// fileDialog.setFilterPath(folder.getRawLocation().toString());
				  
				 String file = fileDialog.open();
				   
				MUTTTrace.exportToFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	
	}
	
}
