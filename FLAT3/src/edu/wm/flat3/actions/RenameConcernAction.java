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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.ui.concerntree.ConcernTreeViewer;
import edu.wm.flat3.util.ARFFFile;

/**
 * An action to rename a concern to the model.
 */
public class RenameConcernAction extends Action
{
	private ConcernTreeViewer viewer;
	private Concern concern; // The concern to rename

	/**
	 * Creates the action.
	 * 
	 * @param concern
	 *            The view from where the action is triggered
	 * @param viewer
	 *            The viewer controlling this action.
	 */
	public RenameConcernAction(ConcernTreeViewer viewer, Concern concern)
	{
		this.viewer = viewer;
		this.concern = concern;
		
		setText(FLATTT.getResourceString("actions.RenameConcernAction.Label"));
		setToolTipText(FLATTT.getResourceString("actions.RenameConcernAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		InputDialog lDialog = new InputDialog(
				viewer.getTree().getShell(),
				FLATTT.getResourceString("actions.RenameConcernAction.DialogTitle"),
				FLATTT.getResourceString("actions.RenameConcernAction.DialogLabel"),
				concern.getDisplayName(), 
				new IInputValidator()
					{
						public String isValid(String concernName)
						{
							if (concernName.equals(concern.getDisplayName()))
								return FLATTT.getResourceString("SameName");
							else
								return concern.getParent().isChildNameValid(concernName);
						}
					}
				);

		if (lDialog.open() == Window.OK)
		{
			String escapedName = ARFFFile.escape(lDialog.getValue()); 
			concern.rename(escapedName);
		}
	}
}
