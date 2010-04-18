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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.lucene.FLATTTLuceneAnalysis;
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
public class ImportTraceAction extends Action
{
	IViewPart view; 
	/**
	 * Creates the action.
	 * 
	 * @param pViewer
	 *            The view from where the action is triggered
	 */
	public ImportTraceAction(IViewPart view)
	{
		this.view = view;
		//this.setEnabled(false);
		
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/chart_line_load.png"));

		setText(FLATTT
					.getResourceString("actions.ImportTraceAction.Label"));
		setToolTipText(FLATTT
					.getResourceString("actions.ImportTraceAction.ToolTip"));
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		try {
			// TODO: file dialog, warning
			 FileDialog fileDialog = new FileDialog(view.getViewSite().getShell(), SWT.OPEN);
			 fileDialog.setFilterNames(new String[] {"Zip Files (*.zip)"});
			 fileDialog.setFilterExtensions(new String[] {"*.zip"});
			 fileDialog.setText("Select trace data zip...");
			// fileDialog.setFilterPath(folder.getRawLocation().toString());
			  
			 String file = fileDialog.open();
			 MUTTTrace.importFromFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
