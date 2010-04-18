package edu.wm.flat3.analysis.lucene.actions;

import java.util.List;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
//import org.severe.jripples.defaultmodules.presentation.JRipplesViewsConstants;
import edu.wm.flat3.CodeModelRule;
import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.OpenSearchViewAction;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.lucene.FLATTTLuceneAnalysis;
import edu.wm.flat3.analysis.lucene.LuceneIndexer;
import edu.wm.flat3.repository.Component;

/**
 * @author max
 *
 */
public class SetQueryAction extends Action {
	private IWorkbenchPartSite site;
	
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

	public SetQueryAction(IWorkbenchPartSite workbenchPartSite) {
		site = workbenchPartSite;
		setText(FLATTT
				.getResourceString("actions.SetQueryAction.Top.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/magnifier.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.SetQueryAction.Top.ToolTip"));
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */

	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		setText(FLATTT
				.getResourceString("actions.SetQueryAction.Top.Label"));
		setToolTipText(FLATTT
				.getResourceString("actions.SetQueryAction.Top.ToolTip"));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run() {//(IAction action) {
		SearchDialog dialog = new SearchDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow()
				.getShell(), "FLATTT Lucene Analysis",
				"Enter query:", FLATTTLuceneAnalysis.getSearchString(), null); 
		dialog.open();									
		if (dialog.getReturnCode()==Window.OK) {
			
			LuceneIndexer.setIndexDir(FLATTT.singleton().getStateLocation().append("luceneindex").toFile()); //TODO: should this be the projectname? or do we want one index for the whole workspace?
			
			// Do the search when done indexing
			UIJob job = new SearchJob(dialog.getValue());
			FLATTT.nextSearch = job;
			//job.schedule();
			
			// Index if needed
			try {
				LuceneIndexer.checkIfIndexed();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
	}
; 

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	public class SearchJob extends UIJob {
		String query;
		public SearchJob(String query) {
			super("FLAT3 Lucene Search");
			this.query = query;
			this.setPriority(Job.SHORT);
			this.setRule(new CodeModelRule());
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
	   			FLATTT.searchResultsAreCombinational = false;
					FLATTTLuceneAnalysis.setSearchString(query);
					
					OpenSearchViewAction a = new OpenSearchViewAction(site);
					a.run();
					
					// Tell the table to refresh
					TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
					contentP.refreshTable();
					
		           return Status.OK_STATUS;
		        }
		   }
}

