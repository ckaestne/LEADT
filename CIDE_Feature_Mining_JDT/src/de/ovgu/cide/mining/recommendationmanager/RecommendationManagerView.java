package de.ovgu.cide.mining.recommendationmanager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
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

import cide.gast.IASTNode;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.language.jdt.UnifiedASTNode;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementPreviewEvent;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;
import de.ovgu.cide.mining.featuremanager.FeatureManagerView;
import de.ovgu.cide.mining.recommendationmanager.model.RecommendationTreeNode;
import de.ovgu.cide.util.Statistics;

public class RecommendationManagerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "de.ovgu.cide.mining.recommendationmanager";

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

	private Action deleteElementAction;
	private Action doubleClickAction;
	private Action selectionChangedAction;
	private Action printViewAction;

	private RecommendationContentProvider contentProvider;
	private RecommendationSorter sorter;
	private ApplicationController AC;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		AC = ApplicationController.getInstance();

		imgWarning = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		imgError = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		imgInfo = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		imgElment = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJ_FILE);

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
		line.setBackground(parent.getShell().getDisplay()
				.getSystemColor(SWT.COLOR_GRAY));
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

		columns = new TreeColumn[9];

		columns[0] = new TreeColumn(tree, SWT.LEFT);
		columns[0].setText("Name");
		columns[0].setWidth(250);

		columns[1] = new TreeColumn(tree, SWT.LEFT);
		columns[1].setText("Type-Prio.");
		columns[1].setWidth(50);

		columns[2] = new TreeColumn(tree, SWT.CENTER);
		columns[2].setText("Value");
		columns[2].setWidth(50);

		columns[3] = new TreeColumn(tree, SWT.CENTER);
		columns[3].setText("Reasons");
		columns[3].setWidth(200);

		columns[4] = new TreeColumn(tree, SWT.CENTER);
		columns[4].setText("Supports");
		columns[4].setWidth(50);

		columns[5] = new TreeColumn(tree, SWT.CENTER);
		columns[5].setText("> Value for");
		columns[5].setWidth(100);

		columns[6] = new TreeColumn(tree, SWT.CENTER);
		columns[6].setText("Range");
		columns[6].setWidth(80);
		columns[7] = new TreeColumn(tree, SWT.CENTER);
		columns[7].setText("Length");
		columns[7].setWidth(80);

		columns[8] = new TreeColumn(tree, SWT.CENTER);
		columns[8].setText("Views");
		columns[8].setWidth(50);

		// columns[2] = new TableColumn(table, SWT.CENTER);
		// columns[2].setWidth(155);
		// columns[2].setText("Type");

		viewer.setContentProvider(contentProvider = new RecommendationContentProvider(
				this));
		viewer.setLabelProvider(new RecommendationLabelProvider());
		createSorter();
		viewer.setInput(getViewSite());

		makeActions();

		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionChangedAction();
		contributeToActionBars();

	}

	private void createSorter() {

		Comparator<RecommendationTreeNode>[] comparators = new Comparator[9];

		comparators[0] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		};

		comparators[1] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				if (o1.getTypePriority() < o2.getTypePriority())
					return 1;

				if (o1.getTypePriority() > o2.getTypePriority())
					return -1;

				return 0;
			}
		};

		// recommendation value
		comparators[2] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				if (o1.getSupportValue() > o2.getSupportValue())
					return 1;

				if (o1.getSupportValue() < o2.getSupportValue())
					return -1;

				return 0;
			}
		};

		// recommendation reason
		comparators[3] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				return o1.getReasons().compareTo(o2.getReasons());
			}
		};

		// supports
		comparators[4] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				if (o1.getSupportersCount() > o2.getSupportersCount())
					return 1;

				if (o1.getSupportersCount() < o2.getSupportersCount())
					return -1;

				return 0;
			}
		};

		comparators[5] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				return o1.getMaxSupportFeature().compareTo(
						o2.getMaxSupportFeature());
			}
		};

		// range
		comparators[6] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
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
		// range
		comparators[7] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				return o1.getLength()-o2.getLength();
			}
		};

		// views
		comparators[8] = new Comparator<RecommendationTreeNode>() {
			public int compare(RecommendationTreeNode o1,
					RecommendationTreeNode o2) {
				if (o1.getViewCount() > o2.getViewCount())
					return 1;

				if (o1.getViewCount() < o2.getViewCount())
					return -1;

				return 0;
			}
		};

		sorter = new RecommendationSorter(viewer, columns, comparators);
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

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RecommendationManagerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(deleteElementAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(deleteElementAction);
		// manager.add(printViewAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Non Feature Manager", message);
	}

	// private Set<AIElement> getElementsInElementRange(AIElement sourceElement)
	// {
	//
	// //FIND RELATED ELEMENTS
	//
	// int start = sourceElement.getStartPosition();
	// int end = start + sourceElement.getLength();
	// int CUHash = sourceElement.getCompelationUnitHash();
	//
	// Set<AIElement> elements = new HashSet<AIElement>();
	//
	// for(AIElement element : AC.getAllElements()) {
	// if (element.getCompelationUnitHash() != CUHash)
	// continue;
	//
	// if (element.getStartPosition() < start)
	// continue;
	//
	// if ((element.getStartPosition() + element.getLength()) > end)
	// continue;
	//
	// elements.add(element);
	//
	// }
	//
	// return elements;
	//
	// }

	private void makeActions() {

		deleteElementAction = new Action() {
			public void run() {

				ISelection selection = viewer.getSelection();
				TreePath[] paths = ((ITreeSelection) selection).getPaths();

				Map<AElement, IFeature> elementsToAdd = new HashMap<AElement, IFeature>();

				for (TreePath treePath : paths) {
					if (treePath.getSegmentCount() > 1)
						continue;

					Object obj = treePath.getFirstSegment();

					if (!(obj instanceof RecommendationTreeNode))
						continue;

					if (((RecommendationTreeNode) obj).getColor() == null)
						continue;

					AElement sourceElement = ((RecommendationTreeNode) obj)
							.getElement();
					IFeature feature = ((RecommendationTreeNode) obj)
							.getColor();
					elementsToAdd.put(sourceElement, feature);

					// add also all elements which are included in this element
					// for (AIElement subElement :
					// getElementsInElementRange(sourceElement)) {
					// elementsToAdd.put(subElement,feature);
					// }

				}

				if (elementsToAdd.size() > 0)
					AC.fireEvent(new AElementsNonColorChangedEvent(this,
							elementsToAdd, new HashMap<AElement, IFeature>()));

			}
		};

		deleteElementAction.setText("Hide Recommendation");
		deleteElementAction
				.setToolTipText("Mark as - recommendation does not belong to feature.");
		deleteElementAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		deleteElementAction.setDisabledImageDescriptor(PlatformUI
				.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		deleteElementAction.setEnabled(false);

		selectionChangedAction = new Action() {
			public void run() {

				deleteElementAction.setEnabled(false);

				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				if (!(obj instanceof RecommendationTreeNode))
					return;

				RecommendationTreeNode node = (RecommendationTreeNode) obj;

				if (node.getElement().getCategory() == AICategories.FEATURE)
					return;

				deleteElementAction.setEnabled(true);

				int cuHash, start, len;

				cuHash = node.getElement().getCompelationUnitHash();
				start = node.getElement().getStartPosition();
				len = node.getElement().getLength();

				try {

					AC.fireEvent(new AElementPreviewEvent(
							RecommendationManagerView.this));

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