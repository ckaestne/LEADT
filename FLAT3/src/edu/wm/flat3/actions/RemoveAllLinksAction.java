/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProvider;

/**
 * Clears the concern model.
 */
public class RemoveAllLinksAction extends Action
{
	private IConcernModelProvider concernModelProvider;
	private IStatusLineManager statusLineManager;
	
	/**
	 * The constructor. Sets the text label and tooltip
	 */
	public RemoveAllLinksAction(IConcernModelProvider concernModelProvider,
	                            IStatusLineManager statusLineManager)
	{
		this.concernModelProvider = concernModelProvider;
		this.statusLineManager = statusLineManager;
		
		setText(FLATTT
				.getResourceString("actions.RemoveAllLinksAction.Label"));
		//setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
		//		ConcernMapper.ID_PLUGIN, "icons/delete.png"));
		setImageDescriptor( PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor( ISharedImages.IMG_TOOL_DELETE)); 
		setToolTipText(FLATTT
				.getResourceString("actions.RemoveAllLinksAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		if (shouldProceed())
		{
			int numUnlinked = concernModelProvider.getModel().removeLinks(
					concernModelProvider.getLinkType());
			
			if (statusLineManager != null)
				statusLineManager.setMessage(numUnlinked + " links removed");
		}
	}

	private boolean shouldProceed()
	{
		return MessageDialog.openQuestion(
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
			FLATTT.getResourceString("actions.RemoveAllLinksAction.QuestionDialogTitle"),
			FLATTT.getResourceString("actions.RemoveAllLinksAction.WarningOverwrite"));
	}
}
