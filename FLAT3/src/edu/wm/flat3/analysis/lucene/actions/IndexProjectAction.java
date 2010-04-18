package edu.wm.flat3.analysis.lucene.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.plugin.AbstractUIPlugin;
//import org.severe.jripples.eig.JRipplesEIG;
//import edu.wm.flattt.analysis.lucene.Activator;
import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.lucene.LuceneIndexer;


public class IndexProjectAction extends Action {// IWorkbenchWindowActionDelegate{

	public IndexProjectAction() {
		setText(FLATTT
				.getResourceString("actions.IndexProjectAction.Top.Label"));
		setToolTipText(FLATTT
				.getResourceString("actions.IndexProjectAction.Top.ToolTip"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/magnifier_zoom_in.png"));
	}
	
	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run() {//(IAction action) {
		FLATTT.index();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
