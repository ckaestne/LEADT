package de.ovgu.cide.mining.relationmanager;

import java.util.Comparator;
import java.util.LinkedList;

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
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementPreviewEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode.NODE_KIND;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class RelationManagerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "de.ovgu.cide.mining.relationmanager";

	public static enum MESSAGE_TYPE {
		WARNING, ERROR, INFO, ELEMENT, NONE
	}

	Image imgError;
	Image imgWarning;
	Image imgInfo;
	Image imgElment;

	private TreeViewer viewer;
	private Tree tree;
	private Label infoLabel;
	private Label infoIconLabel;

	public static final int HISTROY_SIZE = 10;
	private Button[] historyButton;
	private LinkedList<AElement> historyList;

	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private Action selectionChangedAction;

	private RelationContentProvider contentProvider;

	private TreeColumn[] columns;
	private RelationSorter sorter;

	/**
	 * The constructor.
	 */
	public RelationManagerView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

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
		drillDownAdapter = new DrillDownAdapter(viewer);

		tree = viewer.getTree();

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		columns = new TreeColumn[3];

		columns[0] = new TreeColumn(tree, SWT.LEFT);
		columns[0].setText("Name");
		columns[0].setWidth(250);

		columns[1] = new TreeColumn(tree, SWT.CENTER);
		columns[1].setText("Range");
		columns[1].setWidth(80);

		columns[2] = new TreeColumn(tree, SWT.CENTER);
		columns[2].setText("Views");
		columns[2].setWidth(50);

		viewer.setContentProvider(contentProvider = new RelationContentProvider(
				this));
		viewer.setLabelProvider(new RelationLabelProvider());
		createSorter();
		viewer.setInput(getViewSite());

		Composite historyArea = new Composite(workArea, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = HISTROY_SIZE + 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;

		historyArea.setLayout(layout);

		data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.TOP;
		data.grabExcessHorizontalSpace = true;
		historyArea.setLayoutData(data);

		historyList = new LinkedList<AElement>();

		Button clearButton = new Button(historyArea, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				clearHistory();
			}

		});

		historyButton = new Button[HISTROY_SIZE];

		for (int i = 0; i < historyButton.length; i++) {

			historyButton[i] = new Button(historyArea, SWT.CENTER);
			data = new GridData();
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.CENTER;
			data.grabExcessHorizontalSpace = true;
			historyButton[i].setLayoutData(data);
			historyButton[i].setData(i);

			final Button curButton = historyButton[i];
			curButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {

					showHistoryItem((Integer) curButton.getData());
				}

			});

		}

		makeActions();

		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionChangedAction();
		contributeToActionBars();

	}

	public void addToHistory(AElement element) {
		if (element == null)
			return;

		if (historyList.size() >= HISTROY_SIZE) {
			historyList.removeFirst();
		}
		historyList.add(element);

		showHistory();

	}

	private void showHistoryItem(int i) {
		int size = historyList.size();
		if (i < size) {
			contentProvider.displayItemChanged(historyList.get(i), false);

			for (int j = i + 1; j < size; j++) {
				historyList.removeLast();
				historyButton[j].setImage(null);
				historyButton[j].setToolTipText("");

			}

		}

	}

	private void clearHistory() {
		historyList.clear();
		for (int i = 0; i < historyButton.length; i++) {
			historyButton[i].setToolTipText("");
			historyButton[i].setImage(null);

		}
	}

	private void showHistory() {
		int i = 0;

		for (AElement tmpElement : historyList) {
			historyButton[i].setToolTipText(tmpElement.getFullName());
			historyButton[i].setImage(imgElment);
			i++;
		}

	}

	public Tree getTree() {
		return tree;
	}

	public TreeViewer getTreeViewer() {
		return viewer;
	}

	public void setInfoMessage(final String msg, final MESSAGE_TYPE type) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
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

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RelationManagerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		// EXAMPLE 1
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		// EXAMPLE 2
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		doubleClickAction = new Action() {
			public void run() {

				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				if (!(obj instanceof RelationTreeNode))
					return;

				if (((RelationTreeNode) obj).getKind() != NODE_KIND.ELEMENT)
					return;

				AElement jayElement = (AElement) ((RelationTreeNode) obj)
						.getDataObject();

				contentProvider.displayItemChanged(jayElement, true);

			}
		};

		selectionChangedAction = new Action() {
			public void run() {

				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				if (!(obj instanceof RelationTreeNode))
					return;

				if (((RelationTreeNode) obj).getKind() != NODE_KIND.ELEMENT)
					return;

				ApplicationController jayFX = ApplicationController
						.getInstance();

				try {
					AElement jayElement = (AElement) ((RelationTreeNode) obj)
							.getDataObject();

					setInfoMessage(jayElement.getId(), MESSAGE_TYPE.ELEMENT);

					IEditorPart javaEditor;

					javaEditor = JavaUI.openInEditor(jayFX
							.getICompilationUnit(jayElement
									.getCompelationUnitHash()));

					if (javaEditor instanceof ITextEditor) {

						jayFX.fireEvent(new AElementPreviewEvent(
								RelationManagerView.this));
						// UnifiedASTNode node = jayElement.getUnifiedASTNode();
						((ITextEditor) javaEditor).selectAndReveal(
								jayElement.getStartPosition(),
								jayElement.getLength());
						// ((ITextEditor)javaEditor).etHighlightRange(node.getStartPosition(),
						// node.getLength(),true);

					}

				}

				catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// showMessage("Selection changed on "+obj.toString());

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

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Relation Manager View", message);
	}

	private void createSorter() {

		Comparator<RelationTreeNode>[] comparators = new Comparator[3];

		comparators[0] = new Comparator<RelationTreeNode>() {
			public int compare(RelationTreeNode o1, RelationTreeNode o2) {
				return o1.getDisplayName().compareTo(o2.getDisplayName());
			}
		};

		// range
		comparators[1] = new Comparator<RelationTreeNode>() {
			public int compare(RelationTreeNode o1, RelationTreeNode o2) {
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

		// views
		comparators[2] = new Comparator<RelationTreeNode>() {
			public int compare(RelationTreeNode o1, RelationTreeNode o2) {
				if (o1.getViewCount() > o2.getViewCount())
					return 1;

				if (o1.getViewCount() < o2.getViewCount())
					return -1;

				return 0;
			}
		};

		sorter = new RelationSorter(viewer, columns, comparators);
		viewer.setSorter(sorter);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}