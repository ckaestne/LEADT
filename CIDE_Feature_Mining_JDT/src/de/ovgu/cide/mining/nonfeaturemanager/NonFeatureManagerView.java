package de.ovgu.cide.mining.nonfeaturemanager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementPreviewEvent;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;
import de.ovgu.cide.mining.nonfeaturemanager.model.CUDummy;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode.NODE_KIND;

public class NonFeatureManagerView extends ViewPart {

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
	private Action deleteElementAction;

	private NonFeatureContentProvider contentProvider;
	private NonFeatureSorter sorter;

	private ApplicationController AC;

	/**
	 * The constructor.
	 */
	public NonFeatureManagerView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		AC = ApplicationController.getInstance();

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
		columns[5].setText("Ele's");
		columns[5].setWidth(60);

		viewer
				.setContentProvider(contentProvider = new NonFeatureContentProvider(
						this));
		viewer.setLabelProvider(new NonFeatureLabelProvider());
		createSorter();
		viewer.setInput(getViewSite());

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionChangedAction();
		contributeToActionBars();

	}

	private void createSorter() {

		Comparator<NonFeatureTreeNode>[] comparators = new Comparator[6];

		comparators[0] = new Comparator<NonFeatureTreeNode>() {
			public int compare(NonFeatureTreeNode o1, NonFeatureTreeNode o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		};

		// range
		comparators[1] = new Comparator<NonFeatureTreeNode>() {
			public int compare(NonFeatureTreeNode o1, NonFeatureTreeNode o2) {
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
		comparators[2] = new Comparator<NonFeatureTreeNode>() {
			public int compare(NonFeatureTreeNode o1, NonFeatureTreeNode o2) {
				return o1.getType().compareTo(o2.getType());
			}
		};

		// add count
		comparators[3] = new Comparator<NonFeatureTreeNode>() {
			public int compare(NonFeatureTreeNode o1, NonFeatureTreeNode o2) {
				if (o1.getAddCount() > o2.getAddCount())
					return 1;

				if (o1.getAddCount() < o2.getAddCount())
					return -1;

				return 0;
			}
		};

		// views
		comparators[4] = new Comparator<NonFeatureTreeNode>() {
			public int compare(NonFeatureTreeNode o1, NonFeatureTreeNode o2) {
				if (o1.getViewCount() > o2.getViewCount())
					return 1;

				if (o1.getViewCount() < o2.getViewCount())
					return -1;

				return 0;
			}
		};

		// recommendations
		comparators[5] = new Comparator<NonFeatureTreeNode>() {
			public int compare(NonFeatureTreeNode o1, NonFeatureTreeNode o2) {
				if (o1.getElementsCount() > o2.getElementsCount())
					return 1;

				if (o1.getElementsCount() < o2.getElementsCount())
					return -1;

				return 0;
			}
		};

		sorter = new NonFeatureSorter(viewer, columns, comparators);
		viewer.setSorter(sorter);

	}

	public void setInfoMessage(final String msg, final MESSAGE_TYPE type) {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
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
		});
	}

	public Tree getTree() {
		return tree;
	}

	public TreeViewer getTreeViewer() {
		return viewer;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				NonFeatureManagerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(deleteElementAction);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(deleteElementAction);
	}

	private void makeActions() {

		// EXAMPLE 1
		deleteElementAction = new Action() {
			public void run() {

				ISelection selection = viewer.getSelection();
				TreePath[] paths = ((ITreeSelection) selection).getPaths();

				Map<AElement, IFeature> elementsToRemove = new HashMap<AElement, IFeature>();

				for (TreePath treePath : paths) {
					if (treePath.getSegmentCount() < 3)
						continue;

					Object obj = treePath.getLastSegment();

					if (!(obj instanceof NonFeatureTreeNode))
						continue;

					NonFeatureTreeNode node = (NonFeatureTreeNode) obj;

					if (node.getKind() != NODE_KIND.ELEMENT)
						continue;

					elementsToRemove.put((AElement) node.getDataObject(),
							NonFeatureTreeNode.getColor(node));
				}

				if (elementsToRemove.size() > 0)
					AC
							.fireEvent(new AElementsNonColorChangedEvent(this,
									new HashMap<AElement, IFeature>(),
									elementsToRemove));

			}
		};
		deleteElementAction.setText("Remove");
		deleteElementAction
				.setToolTipText("Mark as - possible recommendation for features.");
		deleteElementAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));
		deleteElementAction.setDisabledImageDescriptor(PlatformUI
				.getWorkbench().getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE_DISABLED));
		deleteElementAction.setEnabled(false);

		selectionChangedAction = new Action() {
			public void run() {

				deleteElementAction.setEnabled(false);

				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				if (!(obj instanceof NonFeatureTreeNode))
					return;

				NonFeatureTreeNode node = (NonFeatureTreeNode) obj;

				int cuHash, start, len;

				if (node.getKind() == NODE_KIND.ELEMENT) {

					deleteElementAction.setEnabled(true);

					AElement jayElement = (AElement) node.getDataObject();
					cuHash = jayElement.getCompelationUnitHash();
					start = jayElement.getStartPosition();
					len = jayElement.getLength();
				}

				else if (node.getKind() == NODE_KIND.COMPILATION_UNIT) {
					CUDummy dummy = (CUDummy) node.getDataObject();
					cuHash = dummy.getHashCode();
					start = -1;
					len = -1;

				} else {
					return;
				}

				try {

					AC.fireEvent(new AElementPreviewEvent(
							NonFeatureManagerView.this));

					IEditorPart javaEditor;
					javaEditor = JavaUI.openInEditor(AC
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