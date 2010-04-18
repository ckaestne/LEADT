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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.TableViewContentProvider;

/**
 * An action to add a new concern to the model.
 */
public class ClearCombinationalSearchAction extends Action
{
	
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public ClearCombinationalSearchAction()
	{
		this.setEnabled(false);
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/clear.gif"));

		setText(FLATTT
					.getResourceString("actions.ClearCombinationalSearch.Label"));
		setToolTipText(FLATTT
					.getResourceString("actions.ClearCombinationalSearch.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		if (FLATTT.searchResultsAreCombinational && FLATTT.originalSearchResults != null) {
			FLATTT.searchResults = FLATTT.originalSearchResults;
			FLATTT.originalSearchResults = null;
			FLATTT.searchResultsAreCombinational = false;
			TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
			contentP.refreshTable();
		}
	}
	
}
