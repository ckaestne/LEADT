/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.wm.flat3.analysis.visualization;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import edu.wm.flat3.FLATTT;


/**
 * This ImageView class shows how to use SWTImageCanvas to 
 * manipulate images. 
 * <p>
 * To facilitate the usage, you should setFocus to the canvas
 * at the beginning, and call the dispose at the end.
 * <p>
 * @author Chengdong Li: cli4@uky.edu
 * @see edu.wm.flat3.analysis.visualization.SWTImageCanvas
 */

public class VisualizationView extends ViewPart {
	public SWTImageCanvas imageCanvas;
	
	/**
	 * The constructor.
	 */
	public VisualizationView() {
		FLATTT.visualizationView = this;
	}
	
	/**
	 * Create the GUI.
	 * @param frame The Composite handle of parent
	 */
	public void createPartControl(Composite frame) {
		imageCanvas=new SWTImageCanvas(frame);
	}

	/**
	 * Called when we must grab focus.
	 * @see org.eclipse.ui.part.ViewPart#setFocus
	 */
	public void setFocus() {
		imageCanvas.setFocus();
	}

	/**
	 * Called when the View is to be disposed
	 */
	public void dispose() {
		imageCanvas.dispose();
		super.dispose();
	}

}