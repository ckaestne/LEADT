/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.3 $
 */

package edu.wm.flat3.analysis.mutt.actions;

import java.io.File;

import javax.swing.plaf.basic.BasicSliderUI.ActionScroller;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.mutt.MUTTTrace;

/**
 * An action to add a new concern to the model.
 */
public class RightClickTraceAction extends Action implements IObjectActionDelegate
{	
	Object selection;
	
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public RightClickTraceAction()
	{
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/chart_line.png"));

		setText(FLATTT
					.getResourceString("actions.StartTraceAction.Label"));
		setToolTipText(FLATTT
					.getResourceString("actions.StartTraceAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run(IAction action)
	{
		// Get the project directory, class name of the class that was right clicked
		System.out.println(selection.getClass().getPackage());
		if (selection instanceof CompilationUnit) {
				CompilationUnit unit = (CompilationUnit) selection;
				
				//CompilationUnit root = (CompilationUnit)unit;
				//char[] path = unit.getPa;
				// Somehow get the path of the unit's package...
				//String directory = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(path).toOSString();
				//String directory = unit.getJavaProject().getPath();//"/home/f85/tcsava/projects/2008/solver";
				
				String fileDelim = "";
				if (System.getProperty("os.name").startsWith("Windows")) {
					fileDelim = "\\";
				}
				 else{
					fileDelim = "/";
				 }
				
				// We need the project bin folder
				IPath directory = unit.getJavaProject().getResource().getLocation();
				if (directory.append("bin").toFile().exists()) directory.append("bin");
				
				String className = "";
				char[][] packageName = unit.getPackageName();
				for (char[] pck : packageName) {
						className += String.valueOf(pck)+".";
				}
				className += unit.getElementName().substring(0, unit.getElementName().indexOf(".java"));
				String args[] = {};//{"harder.txt"}; // TODO: let user set these args
				MUTTTrace.trace(directory.toOSString(), className, args);

		}
	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		if (arg1 instanceof StructuredSelection)
			selection = ((StructuredSelection)arg1).getFirstElement();		
	}
	
}
