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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.mutt.CallNode;
import edu.wm.flat3.analysis.mutt.MUTTTrace;
import edu.wm.flat3.analysis.mutt.TraceGraph;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.InvalidConcernNameException;
import edu.wm.flat3.util.ARFFFile;
import edu.wm.flat3.util.ProblemManager;

import dapeng.*;
import com.sun.tools.example.trace.*;

/**
 * An action to add a new concern to the model.
 */
public class StartTraceAction extends Action
{	
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public StartTraceAction()
	{
		// TODO: make it work like the run button:
		//   Click, if trace has been run before, trace last java app traced
		//   menu, list of last x apps traced, option to trace other... which takes you to a class selection dialog
		//			(+all the other options one can have for running... basically we need the whole run dialog??)
		//   right click a class, option: trace with FLATTT...
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
	public void run()
	{
		//Workspace.defaultWorkspaceDescription();
		//** default to last app traced
		MessageBox dialog = new MessageBox(Display.getCurrent().getActiveShell());
		dialog.setMessage("Please right click on a java file with a main method and select \"Trace with MUTT\" to begin tracing.");
		dialog.open();
		
//		String directory = "/home/f85/tcsava/projects/2008/solver";
//		String className = "edu.Solver"; 
//		String args[] = {"harder.txt"};
//		MUTTTrace.trace(directory, className, args);
	}
	
}
