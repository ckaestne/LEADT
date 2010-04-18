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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.InvalidConcernNameException;
import edu.wm.flat3.util.ARFFFile;
import edu.wm.flat3.util.ProblemManager;

/**
 * An action to add a new concern to the model.
 */
public class NewConcernAction extends Action
{
	private Shell shell;
	private IConcernModelProvider concernModelProvider;
	private Concern parent;

	private Concern concernJustAdded = null;
	
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public NewConcernAction(Shell shell, 
							IConcernModelProvider concernModelProvider,
							Concern parent)
	{
		this.shell = shell;
		this.concernModelProvider = concernModelProvider;
		this.parent = parent;

		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/lightbulb_add.png"));
		
		if (parent.isRoot())
		{
			setText(FLATTT
					.getResourceString("actions.NewConcernAction.Top.Label"));
			setToolTipText(FLATTT
					.getResourceString("actions.NewConcernAction.Top.ToolTip"));
		}
		else
		{
			setText(FLATTT
					.getResourceString("actions.NewConcernAction.Child.Label"));
			setToolTipText(FLATTT
					.getResourceString("actions.NewConcernAction.Child.ToolTip"));
		}
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		InputDialog dialog = new InputDialog(
				shell,
				FLATTT.getResourceString("actions.NewConcernAction.DialogTitle"),
				FLATTT.getResourceString("actions.NewConcernAction.DialogLabel"),
				"", 
				new IInputValidator()
				{
					public String isValid(String concernName)
					{
						return parent.isChildNameValid(concernName);
					}
				});

		if (dialog.open() != Window.OK)
			return;

		ConcernModel concernModel = concernModelProvider.getModel();
		
		String newConcernName = ARFFFile.escape(dialog.getValue());
		
		try
		{
			concernJustAdded = concernModel.createConcernPath(newConcernName, "");
		}
		catch (InvalidConcernNameException e)
		{
			ProblemManager.reportException(e);
			assert false; // Shouldn't happen since the name already passed validation
		}
		
		if (concernJustAdded != null && !parent.isRoot())
		{
			// Move concern from root to the specific parent
			parent.addChild(concernJustAdded);
		}
	}
	
	public Concern getConcernJustAdded()
	{
		return concernJustAdded;
	}
}
