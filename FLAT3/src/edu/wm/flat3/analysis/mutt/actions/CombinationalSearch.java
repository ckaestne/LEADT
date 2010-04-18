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

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.lucene.FLATTTLuceneAnalysis;

/**
 * An action to add a new concern to the model.
 */
public class CombinationalSearch extends Action
{
//	private Shell shell;
	//private IConcernModelProvider concernModelProvider;
//	private Concern parent;
	private class SearchDialog extends InputDialog{

		public SearchDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parentShell,"org.severe.help.jripplesLucene");
		}

		protected Control createDialogArea(Composite parent) {
			Composite ctrl= (Composite)super.createDialogArea(parent);
			Composite pane = new Composite(ctrl, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			pane.setLayout(layout);
			return ctrl;
		}
	}
	
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public CombinationalSearch()
	{
		this.setEnabled(false);
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/magnifier+.png"));

		setText(FLATTT
					.getResourceString("actions.CombinationalSearch.Label"));
		setToolTipText(FLATTT
					.getResourceString("actions.CombinationalSearch.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		ArrayList<FLATTTMember> oldnodes;
		oldnodes = FLATTT.searchResults;
		boolean oldResultsAreTrace = FLATTT.searchResultsAreTrace;
		
		SearchDialog dialog = new SearchDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow()
				.getShell(), "FLATTT Lucene Analysis",
				"Enter refining query:", FLATTTLuceneAnalysis.getSearchString(), null); 
		dialog.open();										
		if (dialog.getReturnCode()==Window.OK) {
			FLATTTLuceneAnalysis.setSearchString(dialog.getValue());
		}	
		ArrayList<FLATTTMember> newnodes;
		newnodes = FLATTT.searchResults;
		ArrayList<FLATTTMember> combinednodes = new ArrayList<FLATTTMember>();
		
		for (FLATTTMember node : oldnodes) {
			for (FLATTTMember node2 : newnodes) {
				if (node.getNodeIMember().equals(node2.getNodeIMember())) {
					combinednodes.add(node);
				}
			}
		}	
	
		if (!FLATTT.searchResultsAreCombinational) {
			FLATTT.searchResultsAreCombinational = true;
			FLATTT.originalSearchResults = oldnodes;
		}
		FLATTT.searchResultsAreTrace = oldResultsAreTrace;
		FLATTT.searchResults = combinednodes;
		TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
		contentP.refreshTable();
		
	}
	
}
