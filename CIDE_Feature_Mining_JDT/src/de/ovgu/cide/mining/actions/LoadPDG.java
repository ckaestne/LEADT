/**
 * 
 */
package de.ovgu.cide.mining.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.ApplicationControllerException;

/**
 * @author Alex
 * 
 */
public class LoadPDG implements IObjectActionDelegate {

	/**
	 * 
	 */
	public LoadPDG() {
		// TODO Auto-generated constructor stub
	}

	private IStructuredSelection aSelection;

	// public void run(IAction action) {
	//
	// try {
	// // get instance and init the database
	// ApplicationController lDB = ApplicationController.getInstance();
	// IProgressMonitor lMonitor = PlatformUI.getWorkbench()
	// .getActiveWorkbenchWindow().getActivePage()
	// .getViewReferences()[0].getView(true).getViewSite()
	// .getActionBars().getStatusLineManager()
	// .getProgressMonitor();
	// NullProgressMonitor monitor = new NullProgressMonitor();
	// lDB.initialize(getSelectedProject(), monitor);
	//
	// } catch (ApplicationControllerException lException) {
	// lException.printStackTrace();
	// }
	//
	// }

	public void run(IAction action) {

		WorkspaceJob op = new WorkspaceJob("LoadPDG") {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				try {
					// get instance and init the database
					ApplicationController lDB = ApplicationController
							.getInstance();
					lDB.initialize(getSelectedProject(), monitor);

				} catch (ApplicationControllerException lException) {
					lException.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		op.setUser(true);
		op.schedule();

	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
			aSelection = (IStructuredSelection) selection;

	}

	private IProject getSelectedProject() {
		IProject lReturn = null;
		Iterator i = aSelection.iterator();
		if (i.hasNext()) {
			Object lNext = i.next();
			if (lNext instanceof IResource) {
				lReturn = ((IResource) lNext).getProject();
			} else if (lNext instanceof IJavaElement) {
				IJavaProject lProject = ((IJavaElement) lNext).getJavaProject();
				lReturn = lProject.getProject();
			}
		}
		return lReturn;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

}
