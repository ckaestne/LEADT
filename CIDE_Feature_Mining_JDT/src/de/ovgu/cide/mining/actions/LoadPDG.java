/**
 * 
 */
package de.ovgu.cide.mining.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.ovgu.cide.mining.database.ConversionException;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.ApplicationControllerException;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.model.ARelation;

/**
 * @author Alex
 *
 */
public class LoadPDG implements  IObjectActionDelegate {

	/**
	 * 
	 */
	public LoadPDG()  {
		// TODO Auto-generated constructor stub
	}



    private IStructuredSelection aSelection;


	public void run(IAction action) {

	
		try
		{
			//get instance and init the database
    		ApplicationController lDB = ApplicationController.getInstance();
    		IProgressMonitor lMonitor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences()[0].getView( true ).getViewSite().getActionBars().getStatusLineManager().getProgressMonitor();
    		lDB.initialize( getSelectedProject(), lMonitor );
  
    		
		}
    	catch( ApplicationControllerException lException )
		{
    		lException.printStackTrace();
		}

		
	}
	


	 public void selectionChanged(IAction action, ISelection selection) {
	        if( selection instanceof IStructuredSelection )
	            aSelection = (IStructuredSelection)selection;

	    }
	    
	   private IProject getSelectedProject()
		{
			IProject lReturn = null;
			Iterator i = aSelection.iterator();
			if( i.hasNext() )
			{
				Object lNext = i.next();
				if( lNext instanceof IResource )
				{
					lReturn = ((IResource)lNext).getProject();
				}
				else if( lNext instanceof IJavaElement )
				{
					IJavaProject lProject = ((IJavaElement)lNext).getJavaProject();
					lReturn = lProject.getProject();
				}
			}
			return lReturn;
		}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		
	}



}
