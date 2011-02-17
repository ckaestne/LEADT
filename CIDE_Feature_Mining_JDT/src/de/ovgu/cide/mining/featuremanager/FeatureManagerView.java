package de.ovgu.cide.mining.featuremanager;

import java.util.Comparator;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementPreviewEvent;
import de.ovgu.cide.mining.events.ARecommenderElementSelectedEvent;
import de.ovgu.cide.mining.featuremanager.model.ASTDummy;
import de.ovgu.cide.mining.featuremanager.model.CUDummy;
import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode;
import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode.NODE_KIND;

public class FeatureManagerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "de.ovgu.cide.mining.nonfeaturemanager";

	public static enum MESSAGE_TYPE {
		WARNING, ERROR, INFO, ELEMENT, NONE
	}

	Image imgError;
	Image imgWarning;
	Image imgInfo;
	Image imgElment;

	private TreeViewer viewer;
	private Tree tree;
	private TreeColumn[] columns;

	private Label infoLabel;
	private Label infoIconLabel;

	private Action doubleClickAction;
	private Action selectionChangedAction;
	// private Action printInfoAction;

	private FeatureContentProvider contentProvider;
	private FeatureSorter sorter;

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public FeatureManagerView() {

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		imgWarning = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_WARN_TSK);
		imgError = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_ERROR_TSK);
		imgInfo = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_INFO_TSK);
		imgElment = PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FILE);

		Composite workArea = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		workArea.setLayout(layout);

		Composite infoArea = new Composite(workArea, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		infoArea.setLayout(layout);

		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.TOP;
		data.grabExcessHorizontalSpace = true;
		infoArea.setLayoutData(data);

		infoIconLabel = new Label(infoArea, SWT.LEFT);
		data = new GridData();
		data.horizontalAlignment = SWT.LEFT;
		data.verticalAlignment = SWT.CENTER;
		infoIconLabel.setLayoutData(data);

		infoLabel = new Label(infoArea, SWT.LEFT);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.TOP;
		data.grabExcessHorizontalSpace = true;
		infoLabel.setLayoutData(data);

		Composite line = new Composite(workArea, SWT.NONE);
		line.setBackground(parent.getShell().getDisplay().getSystemColor(
				SWT.COLOR_GRAY));
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.TOP;
		data.grabExcessHorizontalSpace = true;
		data.heightHint = 1;
		line.setLayoutData(data);

		Composite viewerArea = new Composite(workArea, SWT.NONE);
		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;

		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		viewerArea.setLayoutData(data);
		viewerArea.setLayout(new FillLayout());

		viewer = new TreeViewer(viewerArea, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);

		tree = viewer.getTree();

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		columns = new TreeColumn[6];

		columns[0] = new TreeColumn(tree, SWT.LEFT);
		columns[0].setText("Name");
		columns[0].setWidth(300);

		columns[1] = new TreeColumn(tree, SWT.CENTER);
		columns[1].setText("Range");
		columns[1].setWidth(80);

		columns[2] = new TreeColumn(tree, SWT.CENTER);
		columns[2].setWidth(150);
		columns[2].setText("Type");

		columns[3] = new TreeColumn(tree, SWT.CENTER);
		columns[3].setText("Add");
		columns[3].setWidth(50);

		columns[4] = new TreeColumn(tree, SWT.CENTER);
		columns[4].setText("Views");
		columns[4].setWidth(50);

		columns[5] = new TreeColumn(tree, SWT.CENTER);
		columns[5].setText("Rec's");
		columns[5].setWidth(60);

		viewer.setContentProvider(contentProvider = new FeatureContentProvider(
				this));
		viewer.setLabelProvider(new FeatureLabelProvider());
		createSorter();
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
		// "TestTableTree.viewer");

		makeActions();

		hookDoubleClickAction();
		hookSelectionChangedAction();
		contributeToActionBars();

	}

	private void createSorter() {

		Comparator<FeatureTreeNode>[] comparators = new Comparator[6];

		comparators[0] = new Comparator<FeatureTreeNode>() {
			public int compare(FeatureTreeNode o1, FeatureTreeNode o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		};

		// range
		comparators[1] = new Comparator<FeatureTreeNode>() {
			public int compare(FeatureTreeNode o1, FeatureTreeNode o2) {
				if (o1.getStartRange() < o2.getStartRange())
					return -1;

				if (o1.getStartRange() > o2.getStartRange())
					return 1;

				if (o1.getEndRange() < o2.getEndRange())
					return -1;

				if (o1.getEndRange() > o2.getEndRange())
					return 1;

				return 0;
			}
		};

		// type
		comparators[2] = new Comparator<FeatureTreeNode>() {
			public int compare(FeatureTreeNode o1, FeatureTreeNode o2) {
				return o1.getType().compareTo(o2.getType());
			}
		};

		// add count
		comparators[3] = new Comparator<FeatureTreeNode>() {
			public int compare(FeatureTreeNode o1, FeatureTreeNode o2) {
				if (o1.getAddCount() > o2.getAddCount())
					return 1;

				if (o1.getAddCount() < o2.getAddCount())
					return -1;

				return 0;
			}
		};

		// views
		comparators[4] = new Comparator<FeatureTreeNode>() {
			public int compare(FeatureTreeNode o1, FeatureTreeNode o2) {
				if (o1.getViewCount() > o2.getViewCount())
					return 1;

				if (o1.getViewCount() < o2.getViewCount())
					return -1;

				return 0;
			}
		};

		// recommendations
		comparators[5] = new Comparator<FeatureTreeNode>() {
			public int compare(FeatureTreeNode o1, FeatureTreeNode o2) {
				if (o1.getRecommendationCount() > o2.getRecommendationCount())
					return 1;

				if (o1.getRecommendationCount() < o2.getRecommendationCount())
					return -1;

				return 0;
			}
		};

		sorter = new FeatureSorter(viewer, columns, comparators);
		viewer.setSorter(sorter);

	}

	public void setInfoMessage(String msg, MESSAGE_TYPE type) {
		if (type == MESSAGE_TYPE.NONE)
			infoIconLabel.setImage(null);
		else if (type == MESSAGE_TYPE.ERROR)
			infoIconLabel.setImage(imgError);
		else if (type == MESSAGE_TYPE.WARNING)
			infoIconLabel.setImage(imgWarning);
		else if (type == MESSAGE_TYPE.INFO)
			infoIconLabel.setImage(imgInfo);
		else if (type == MESSAGE_TYPE.ELEMENT)
			infoIconLabel.setImage(imgElment);

		infoLabel.setToolTipText(msg);
		infoLabel.setText(msg);
	}

	public Tree getTree() {
		return tree;
	}

	public TreeViewer getTreeViewer() {
		return viewer;
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		// manager.add(printInfoAction);
	}

	private void makeActions() {

		// <-- STATISTICS
		// printInfoAction = new Action() {
		// public void run() {
		// //print all elements which are assigned to features
		// long printNbr = System.currentTimeMillis();
		//				
		// ApplicationController jayFX = ApplicationController.getInstance();
		//				
		// //
		// Statistics.writeElementsCategories(jayFX.getAllElements(),printNbr,
		// "SYSTEM");
		// // Statistics.writeElements(jayFX.getAllElements(), printNbr,
		// "SYSTEM", false);
		//				
		// for (IFeature feature : jayFX.getProjectFeatures()) {
		// Statistics.writeElementsCategories(jayFX.getElementsOfColor(feature),printNbr,
		// "0_"+feature.getName());
		// Statistics.writeElements(jayFX.getElementsOfColor(feature), printNbr,
		// "0_"+feature.getName(), true);
		// }
		//				
		//					
		// for (IFeature feature : jayFX.getProjectFeatures()) {
		// Statistics.writeElementsCategories(jayFX.getElementsOfNonColor(feature),printNbr,
		// "1_" +feature.getName());
		// Statistics.writeElements(jayFX.getElementsOfNonColor(feature),
		// printNbr, "1_" +feature.getName(), true);
		// }
		//
		// }
		// };
		//		
		// printInfoAction.setToolTipText("Print Feature Information");
		// printInfoAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
		// getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
		// STATISTICS-->

		doubleClickAction = new Action() {
			public void run() {

			}
		};

		selectionChangedAction = new Action() {
			public void run() {

				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				if (!(obj instanceof FeatureTreeNode))
					return;

				FeatureTreeNode node = (FeatureTreeNode) obj;

				int cuHash, start, len;

				ApplicationController jayFX = ApplicationController
						.getInstance();

				if (node.getKind() == NODE_KIND.ELEMENT) {
					AElement jayElement = (AElement) node.getDataObject();
					cuHash = jayElement.getCompelationUnitHash();
					start = jayElement.getStartPosition();
					len = jayElement.getLength();
					jayFX.fireEvent(new ARecommenderElementSelectedEvent(this,
							FeatureTreeNode.getColor(node), jayElement));
				} else if (node.getKind() == NODE_KIND.ASTDUMMY) {
					ASTDummy dummy = (ASTDummy) node.getDataObject();
					cuHash = dummy.getHashCode();
					start = dummy.getStart();
					len = dummy.getLength();
					jayFX.fireEvent(new ARecommenderElementSelectedEvent(this,
							FeatureTreeNode.getColor(node), start, start + len,
							cuHash));

				} else if (node.getKind() == NODE_KIND.COMPILATION_UNIT) {
					CUDummy dummy = (CUDummy) node.getDataObject();
					cuHash = dummy.getHashCode();
					start = -1;
					len = -1;
					jayFX.fireEvent(new ARecommenderElementSelectedEvent(this,
							FeatureTreeNode.getColor(node), cuHash));

				} else {
					jayFX.fireEvent(new ARecommenderElementSelectedEvent(this,
							FeatureTreeNode.getColor(node)));
					return;
				}

				try {

					// setInfoMessage(jayElement.getId(), MESSAGE_TYPE.ELEMENT);
					jayFX.fireEvent(new AElementPreviewEvent(
							FeatureManagerView.this));

					IEditorPart javaEditor;
					javaEditor = JavaUI.openInEditor(jayFX
							.getICompilationUnit(cuHash));

					if ((start >= 0) && (javaEditor instanceof ITextEditor)) {
						((ITextEditor) javaEditor).selectAndReveal(start, len);

					}

				}

				catch (PartInitException e) {
					e.printStackTrace();
				} catch (JavaModelException e) {
					e.printStackTrace();
				}

			}
		};

	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});

	}

	private void hookSelectionChangedAction() {

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub
				selectionChangedAction.run();
			}
		});

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}