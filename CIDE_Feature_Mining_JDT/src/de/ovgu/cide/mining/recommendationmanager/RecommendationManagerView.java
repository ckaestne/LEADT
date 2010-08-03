package de.ovgu.cide.mining.recommendationmanager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;
import de.ovgu.cide.mining.events.AElementPreviewEvent;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostColorChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostNonColorChangedEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.events.ARecommenderElementSelectedEvent;
import de.ovgu.cide.mining.events.ARecommenderElementSelectedEvent.EVENT_TYPE;
import de.ovgu.cide.mining.logging.EvalLogging;

public class RecommendationManagerView extends ViewPart implements Observer {

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

	private TableViewer viewer;
	private Table tree;
	private TableColumn[] columns;

	private Label infoLabel;
	private Label infoIconLabel;

	private Action deleteElementAction;
	private Action doubleClickAction;
	private Action selectionChangedAction;
	private Action printViewAction;

	private RecommendationContentProvider contentProvider;
	private RecommendationSorter sorter;
	ApplicationController AC;

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

		tree = new Table(viewerArea, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.VIRTUAL);
		viewer = new TableViewer(tree);
		viewer.setUseHashlookup(true);

		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		columns = new TableColumn[9];

		columns[0] = new TableColumn(tree, SWT.LEFT);
		columns[0].setText("Name");
		columns[0].setWidth(250);

		columns[1] = new TableColumn(tree, SWT.LEFT);
		columns[1].setText("Type-Prio.");
		columns[1].setWidth(50);

		columns[2] = new TableColumn(tree, SWT.CENTER);
		columns[2].setText("Value");
		columns[2].setWidth(50);

		columns[3] = new TableColumn(tree, SWT.CENTER);
		columns[3].setText("Reasons");
		columns[3].setWidth(200);

		columns[4] = new TableColumn(tree, SWT.CENTER);
		columns[4].setText("Supports");
		columns[4].setWidth(50);

		columns[5] = new TableColumn(tree, SWT.CENTER);
		columns[5].setText("> Value for");
		columns[5].setWidth(100);

		columns[6] = new TableColumn(tree, SWT.CENTER);
		columns[6].setText("Range");
		columns[6].setWidth(80);
		columns[7] = new TableColumn(tree, SWT.CENTER);
		columns[7].setText("Length");
		columns[7].setWidth(80);

		columns[8] = new TableColumn(tree, SWT.CENTER);
		columns[8].setText("Views");
		columns[8].setWidth(50);

		// columns[2] = new TableColumn(table, SWT.CENTER);
		// columns[2].setWidth(155);
		// columns[2].setText("Type");

		contentProvider = new RecommendationContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new RecommendationLabelProvider(this));
		createSorter();
		tree.setItemCount(recommendations.size());
		viewer.setInput(new Recommendation[0]);

		makeActions();

		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionChangedAction();
		contributeToActionBars();

		isInit = false;
		checkIsIntialized();

		AC.addObserver(this);

	}

	private void createSorter() {

		Comparator<Recommendation>[] comparators = new Comparator[9];

		comparators[0] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				return o1.element.getDisplayName().compareTo(
						o2.element.getDisplayName());
			}
		};

		comparators[1] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				return RecommendationLabelProvider.getTypePriority(o1)
						- RecommendationLabelProvider.getTypePriority(o2);
			}
		};

		// recommendation value
		comparators[2] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				if (o1.context.getSupportValue() > o2.context.getSupportValue())
					return 1;

				if (o1.context.getSupportValue() < o2.context.getSupportValue())
					return -1;

				return 0;
			}
		};

		// recommendation reason
		comparators[3] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				return o1.context.getSupportReasons().compareTo(
						o2.context.getSupportReasons());
			}
		};

		// supports
		comparators[4] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				return (o1.context.getContexts().size() - o2.context
						.getContexts().size());
			}
		};

		comparators[5] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				return 0;
			}
		};

		// range
		comparators[6] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				if (o1.element.getStartPosition() < o2.element
						.getStartPosition())
					return -1;

				if (o1.element.getStartPosition() > o2.element
						.getStartPosition())
					return 1;

				if (o1.element.getLength() < o2.element.getLength())
					return -1;

				if (o1.element.getLength() > o2.element.getLength())
					return 1;

				return 0;
			}
		};
		// range
		comparators[7] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				return o1.element.getLength() - o2.element.getLength();
			}
		};

		// views
		comparators[8] = new Comparator<Recommendation>() {
			public int compare(Recommendation o1, Recommendation o2) {
				return RecommendationLabelProvider.getViewCount(o1)
						- RecommendationLabelProvider.getViewCount(o2);
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

	public Table getTree() {
		return tree;
	}

	public TableViewer getTreeViewer() {
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
				Object selectedRecommendation = ((IStructuredSelection) selection)
						.getFirstElement();

				Map<AElement, IFeature> elementsToAdd = new HashMap<AElement, IFeature>();

				if (!(selectedRecommendation instanceof Recommendation))
					return;

				if (currentColor == null)
					return;

				AElement sourceElement = ((Recommendation) selectedRecommendation).element;
				IFeature feature = currentColor;
				elementsToAdd.put(sourceElement, feature);

				// add also all elements which are included in this element
				// for (AIElement subElement :
				// getElementsInElementRange(sourceElement)) {
				// elementsToAdd.put(subElement,feature);
				// }

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

				if (!(obj instanceof Recommendation))
					return;

				Recommendation node = (Recommendation) obj;

				if (node.element.getCategory() == AICategories.FEATURE)
					return;

				deleteElementAction.setEnabled(true);

				int cuHash, start, len;

				cuHash = node.element.getCompelationUnitHash();
				ICompilationUnit cu = AC.getICompilationUnit(cuHash);
				start = node.element.getStartPosition();
				len = node.element.getLength();

				EvalLogging.getInstance().selectRecommendation(cu, start, len,
						node.context.getSupportValue());

				try {

					AC.fireEvent(new AElementPreviewEvent(
							RecommendationManagerView.this));

					IEditorPart javaEditor;
					javaEditor = JavaUI.openInEditor(cu);

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
				// doubleClickAction.run();
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

	private ARecommenderElementSelectedEvent curEvent;

	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {

			if (arg instanceof AInitEvent) {
				setInfoMessage("Database created for "
						+ ((AInitEvent) arg).getProject().getName(),
						MESSAGE_TYPE.INFO);
				isInit = true;

			} else if (arg instanceof AElementViewCountChangedEvent) {
				getTreeViewer().refresh();
			} else if (arg instanceof ARecommenderElementSelectedEvent) {
				curEvent = (ARecommenderElementSelectedEvent) arg;
				updateRecommendations(curEvent);
			} else if (arg instanceof AElementsPostColorChangedEvent) {
				updateRecommendations(curEvent);
			} else if (arg instanceof AElementsPostNonColorChangedEvent) {
				updateRecommendations(curEvent);
			}

		}

	}

	private void updateRecommendations(
			final ARecommenderElementSelectedEvent event) {
		Job update = new Job("Update recommendations") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				calculateRecommendations(event);
				EvalLogging.getInstance()
						.updateRecommendations(recommendations);

				Display.getDefault().syncExec(new Runnable() {

					public void run() {
						if (!recommendations.isEmpty()) {
							setInfoMessage(
									"Recommendations for selected element",
									MESSAGE_TYPE.INFO);
						} else {
							setInfoMessage(
									"There are no recommendations for the selected element.",
									MESSAGE_TYPE.WARNING);
						}

						getTreeViewer().getTable().setItemCount(
								recommendations.size());
						getTreeViewer().setInput(
								recommendations.toArray(new Recommendation[0]));
					}
				});

				return Status.OK_STATUS;
			}

		};
		update.setPriority(Job.SHORT);
		update.schedule();
	}

	private boolean checkIsIntialized() {

		if (isInit)
			return true;

		IProject project = AC.getInitializedProject();

		if (project == null) {
			setInfoMessage("Database has not been created for Feature Mining",
					MESSAGE_TYPE.ERROR);
			return false;
		}

		setInfoMessage("Database created for " + project.getName(),
				MESSAGE_TYPE.INFO);
		isInit = true;

		return true;
	}

	static class Recommendation implements IAdaptable {
		public Recommendation(AElement e, ARecommendationContextCollection c) {
			element = e;
			context = c;
		}

		AElement element;
		ARecommendationContextCollection context;

		public Object getAdapter(Class adapter) {
			return null;
		}
	}

	List<Recommendation> recommendations = new ArrayList<Recommendation>();
	IFeature currentColor;

	public void calculateRecommendations(ARecommenderElementSelectedEvent event) {
		if (event == null) {
			recommendations = new ArrayList<Recommendation>();

		} else {

			// BUILD TREE TO DISPLAY
			Map<AElement, ARecommendationContextCollection> providedRecommendations;

			if (event.getType().equals(EVENT_TYPE.ELEMENT)) {
				AElement sourceElement = event.getElement();
				providedRecommendations = AC.getRecommendations(
						event.getColor(), sourceElement);
			} else {
				providedRecommendations = AC.getRecommendations(
						event.getColor(), event.getStart(), event.getEnd(),
						event.getCuHash());
			}
			this.currentColor = event.getColor();
			recommendations = new ArrayList<Recommendation>();

			for (Entry<AElement, ARecommendationContextCollection> entry : providedRecommendations
					.entrySet()) {
				recommendations.add(new Recommendation(entry.getKey(), entry
						.getValue()));
			}

		}

	}

	private boolean isInit;
}