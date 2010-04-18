/*******************************************************************************
* Copyright (c) 2004 Chengdong Li : cdli@ccs.uky.edu
* All rights reserved. This program and the accompanying materials 
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
*******************************************************************************/
package edu.wm.flat3.analysis.visualization;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ScrollBar;


import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.repository.SourceRange;
import edu.wm.flat3.util.LineSet;

/**
 * A scrollable image canvas that extends org.eclipse.swt.graphics.Canvas.
 * <p/>
 * It requires Eclipse (version >= 2.1) on Win32/win32; Linux/gtk; MacOSX/carbon.
 * <p/>
 * This implementation using the pure SWT, no UI AWT package is used. For 
 * convenience, I put everything into one class. However, the best way to
 * implement this is to use inheritance to create multiple hierarchies.
 * 
 * @author Chengdong Li: cli4@uky.edu
 */
public class SWTImageCanvas extends Canvas {
	/* zooming rates in x and y direction are equal.*/
	final float ZOOMIN_RATE = 1.1f; /* zoomin rate */
	final float ZOOMOUT_RATE = 0.9f; /* zoomout rate */
	private Image sourceImage; /* original image */
	private Image screenImage; /* screen image */
	private AffineTransform transform = new AffineTransform();

	private String currentDir=""; /* remembering file open directory */
	
	ArrayList<FLATTTMember> nodes = null;
	ArrayList<FLATTTSourceFile> files = null; 
	Image visualization = null;
	
	public void sortNodes() {
		boolean found;
		
		files = new ArrayList<FLATTTSourceFile>();
		
		for (FLATTTMember node : nodes) {
			found = false;
			for (FLATTTSourceFile file : files) {
				if (node.getNodeIMember().getCompilationUnit().equals(file.fileUnit)) {
					file.add(node);
					found = true;
				}
			}
			
			// compilation unit isn't already in files
			if (!found) {
				FLATTTSourceFile newFile = new FLATTTSourceFile(node.getNodeIMember().getCompilationUnit());
				newFile.add(node);
				files.add(newFile);
			}
		}
	}
	
	public void drawVisualization(ArrayList<FLATTTMember> nodes) {
		this.nodes = nodes;
		sortNodes();
		
		int HEIGHT = 175;
		int WIDTH = 100;
		int SPACING = 10;
		// Create an image, draw the files on it, draw the source ranges on the files
		visualization = new Image(getDisplay(), (files.size()*(WIDTH+SPACING))+10, HEIGHT+25);
		
		GC newGC = new GC(visualization);
	//	newGC.setClipping(clientRect);
		int x = 0;
		for (FLATTTSourceFile file : files) {
			newGC.setAlpha(255);
			newGC.setBackground(newGC.getDevice().getSystemColor(SWT.COLOR_WHITE)); 
			newGC.drawRectangle(SPACING + (x*(WIDTH+SPACING)), 10, WIDTH, HEIGHT);
			newGC.drawText(file.fileUnit.getElementName(),SPACING + (x*(WIDTH+SPACING))+1, 10+1);
			newGC.drawLine(SPACING + (x*(WIDTH+SPACING))+1, 10+1+15, SPACING + (x*(WIDTH+SPACING))+WIDTH,10+1+15);
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setSource(file.fileUnit);

			// Make sure we set resolve bindings to true
			parser.setResolveBindings(true);
			
			// Create AST
			ASTNode root = parser.createAST(null);
			CompilationUnit cu = (CompilationUnit) root;
			LineSet acc = new LineSet(cu);
			float lines = cu.getLineNumber(cu.getLength()-1);
			
			for (FLATTTMember node : file.components) {
				SourceRange rng = SourceRange.create(cu, node.getNodeIMember(), node.getShortName(),acc);
				float start = ((rng.getBeginLine()/lines)*(HEIGHT-25));
				float height = ((rng.getEndLine()/lines)*(HEIGHT-25)) - start;
	//			float start = ((0/lines)*(HEIGHT-25));
		//		float height = ((lines/lines)*(HEIGHT-17)) - start;

				if (node.getProbability() != null)
					newGC.setAlpha((int)(255 * Float.valueOf(node.getProbability())));
				else // If probability isn't set, just use no transparency
					newGC.setAlpha(255);
				newGC.setBackground(newGC.getDevice().getSystemColor(SWT.COLOR_DARK_RED)); 	
				newGC.fillRectangle(SPACING + (x*(WIDTH+SPACING))+1, (int)Math.floor((double)start)+27,WIDTH-1, (int)height);
			}
			x++;
		}
	}

	public SWTImageCanvas(final Composite parent) {
		this(parent, SWT.NULL);
	}

	/**
	 * Constructor for ScrollableCanvas.
	 * @param parent the parent of this control.
	 * @param style the style of this control.
	 */
	public SWTImageCanvas(final Composite parent, int style) {
		super( parent, style|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL
				            | SWT.NO_BACKGROUND);
		addControlListener(new ControlAdapter() { /* resize listener. */
			public void controlResized(ControlEvent event) {
				syncScrollBars();
			}
		});
		addPaintListener(new PaintListener() { /* paint listener. */
			public void paintControl(final PaintEvent event) {
				paint(event.gc);
			}
		});
		initScrollBars();
	}

	/**
	 * Dispose the garbage here
	 */
	public void dispose() {
		if (visualization != null && !visualization.isDisposed()) {
			visualization.dispose();
		}
		if (screenImage != null && !screenImage.isDisposed()) {
			screenImage.dispose();
		}
	}

	/* Paint function */
	private void paint(GC gc) {
		Rectangle clientRect = getClientArea(); /* Canvas' painting area */
		if (visualization != null) {
			
			// create a sourceImage that's our visualization at some point
			
			Rectangle imageRect =
				SWT2Dutil.inverseTransformRect(transform, clientRect);
			int gap = 2; /* find a better start point to render */
			imageRect.x -= gap; imageRect.y -= gap;
			imageRect.width += 2 * gap; imageRect.height += 2 * gap;

			Rectangle imageBound = visualization.getBounds();
			imageRect = imageRect.intersection(imageBound);
			Rectangle destRect = SWT2Dutil.transformRect(transform, imageRect);

			if (screenImage != null)
				screenImage.dispose();
			screenImage =
				new Image(getDisplay(), clientRect.width, clientRect.height);
			GC newGC = new GC(screenImage);
			newGC.setClipping(clientRect);
			newGC.drawImage(
				visualization,
				imageRect.x,
				imageRect.y,
				imageRect.width,
				imageRect.height,
				destRect.x,
				destRect.y,
				destRect.width,
				destRect.height);
			newGC.dispose();

			gc.drawImage(screenImage, 0, 0);
			
			syncScrollBars();
		} else {
			gc.setClipping(clientRect);
			gc.fillRectangle(clientRect);
			initScrollBars();
		}
	}

	/* Initalize the scrollbar and register listeners. */
	private void initScrollBars() {
		ScrollBar horizontal = getHorizontalBar();
		horizontal.setEnabled(false);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
			}
		});
		ScrollBar vertical = getVerticalBar();
		vertical.setEnabled(false);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
			}
		});
	}

	/* Scroll horizontally */
	private void scrollHorizontally(ScrollBar scrollBar) {
		if (visualization == null)
			return;

		AffineTransform af = transform;
		double tx = af.getTranslateX();
		double select = -scrollBar.getSelection();
		af.preConcatenate(AffineTransform.getTranslateInstance(select - tx, 0));
		transform = af;
		syncScrollBars();
	}

	/* Scroll vertically */
	private void scrollVertically(ScrollBar scrollBar) {
		if (visualization == null)
			return;

		AffineTransform af = transform;
		double ty = af.getTranslateY();
		double select = -scrollBar.getSelection();
		af.preConcatenate(AffineTransform.getTranslateInstance(0, select - ty));
		transform = af;
		syncScrollBars();
	}

	/**
	 * Source image getter.
	 * @return sourceImage.
	 */
	public Image getSourceImage() {
		return visualization;
	}

	/**
	 * Synchronize the scrollbar with the image. If the transform is out
	 * of range, it will correct it. This function considers only following
	 * factors :<b> transform, image size, client area</b>.
	 */
	public void syncScrollBars() {
		if (visualization == null) {
			redraw();
			return;
		}

		AffineTransform af = transform;
		double sx = af.getScaleX(), sy = af.getScaleY();
		double tx = af.getTranslateX(), ty = af.getTranslateY();
		if (tx > 0) tx = 0;
		if (ty > 0) ty = 0;

		ScrollBar horizontal = getHorizontalBar();
		horizontal.setIncrement((int) (getClientArea().width / 100));
		horizontal.setPageIncrement(getClientArea().width);
		Rectangle imageBound = visualization.getBounds();
		int cw = getClientArea().width, ch = getClientArea().height;
		if (imageBound.width * sx > cw) { /* image is wider than client area */
			horizontal.setMaximum((int) (imageBound.width * sx));
			horizontal.setEnabled(true);
			if (((int) - tx) > horizontal.getMaximum() - cw)
				tx = -horizontal.getMaximum() + cw;
		} else { /* image is narrower than client area */
			horizontal.setEnabled(false);
			tx = (cw - imageBound.width * sx) / 2; //center if too small.
		}
		horizontal.setSelection((int) (-tx));
		horizontal.setThumb((int) (getClientArea().width));

		ScrollBar vertical = getVerticalBar();
		vertical.setIncrement((int) (getClientArea().height / 100));
		vertical.setPageIncrement((int) (getClientArea().height));
		if (imageBound.height * sy > ch) { /* image is higher than client area */
			vertical.setMaximum((int) (imageBound.height * sy));
			vertical.setEnabled(true);
			if (((int) - ty) > vertical.getMaximum() - ch)
				ty = -vertical.getMaximum() + ch;
		} else { /* image is less higher than client area */
			vertical.setEnabled(false);
			ty = (ch - imageBound.height * sy) / 2; //center if too small.
		}
		vertical.setSelection((int) (-ty));
		vertical.setThumb((int) (getClientArea().height));

		/* update transform. */
		af = AffineTransform.getScaleInstance(sx, sy);
		af.preConcatenate(AffineTransform.getTranslateInstance(tx, ty));
		transform = af;

		redraw();
	}

	/**
	 * Reload image from a file
	 * @param filename image file
	 * @return swt image created from image file
	 */
	public Image loadImage(String filename) {
		/*if (sourceImage != null && !sourceImage.isDisposed()) {
			sourceImage.dispose();
			sourceImage = null;
		}
		sourceImage = new Image(getDisplay(), filename);
		showOriginal();
		return sourceImage;*/
		return visualization;
	}

	/**
	 * Call back funtion of button "open". Will open a file dialog, and choose
	 * the image file. It supports image formats supported by Eclipse.
	 */
	public void onFileOpen() {
		FileDialog fileChooser = new FileDialog(getShell(), SWT.OPEN);
		fileChooser.setText("Open image file");
		fileChooser.setFilterPath(currentDir);
		fileChooser.setFilterExtensions(
			new String[] { "*.gif; *.jpg; *.png; *.ico; *.bmp" });
		fileChooser.setFilterNames(
			new String[] { "SWT image" + " (gif, jpeg, png, ico, bmp)" });
		String filename = fileChooser.open();
		if (filename != null){
			loadImage(filename);
			currentDir = fileChooser.getFilterPath();
		}
	}

	/**
	 * Get the image data. (for future use only)
	 * @return image data of canvas
	 */
	public ImageData getImageData() {
		return visualization.getImageData();
	}

	/**
	 * Reset the image data and update the image
	 * @param data image data to be set
	 */
	public void setImageData(ImageData data) {
		if (sourceImage != null)
			sourceImage.dispose();
		if (data != null)
			sourceImage = new Image(getDisplay(), data);
		syncScrollBars();
	}

	/**
	 * Fit the image onto the canvas
	 */
	public void fitCanvas() {
		if (visualization == null)
			return;
		Rectangle imageBound = visualization.getBounds();
		Rectangle destRect = getClientArea();
		double sx = (double) destRect.width / (double) imageBound.width;
		double sy = (double) destRect.height / (double) imageBound.height;
		double s = Math.min(sx, sy);
		double dx = 0.5 * destRect.width;
		double dy = 0.5 * destRect.height;
		centerZoom(dx, dy, s, new AffineTransform());
	}

	/**
	 * Show the image with the original size
	 */
	public void showOriginal() {
		if (visualization == null)
			return;
		transform = new AffineTransform();
		syncScrollBars();
	}

	/**
	 * Perform a zooming operation centered on the given point
	 * (dx, dy) and using the given scale factor. 
	 * The given AffineTransform instance is preconcatenated.
	 * @param dx center x
	 * @param dy center y
	 * @param scale zoom rate
	 * @param af original affinetransform
	 */
	public void centerZoom(
		double dx,
		double dy,
		double scale,
		AffineTransform af) {
		af.preConcatenate(AffineTransform.getTranslateInstance(-dx, -dy));
		af.preConcatenate(AffineTransform.getScaleInstance(scale, scale));
		af.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
		transform = af;
		syncScrollBars();
	}

	/**
	 * Zoom in around the center of client Area.
	 */
	public void zoomIn() {
		if (visualization == null)
			return;
		Rectangle rect = getClientArea();
		int w = rect.width, h = rect.height;
		double dx = ((double) w) / 2;
		double dy = ((double) h) / 2;
		centerZoom(dx, dy, ZOOMIN_RATE, transform);
	}

	/**
	 * Zoom out around the center of client Area.
	 */
	public void zoomOut() {
		if (visualization == null)
			return;
		Rectangle rect = getClientArea();
		int w = rect.width, h = rect.height;
		double dx = ((double) w) / 2;
		double dy = ((double) h) / 2;
		centerZoom(dx, dy, ZOOMOUT_RATE, transform);
	}
}
