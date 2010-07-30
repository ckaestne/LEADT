package de.ovgu.cide.mining.nonfeaturemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostNonColorChangedEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.nonfeaturemanager.NonFeatureManagerView.MESSAGE_TYPE;
import de.ovgu.cide.mining.nonfeaturemanager.model.CUDummy;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode.NODE_KIND;

class NonFeatureContentProvider implements IStructuredContentProvider,
		ITreeContentProvider, Observer {

	private final NonFeatureManagerView nonFeatureManager;
	private final ApplicationController AC;
	// private AElementColorManager elementColorManager;
	private boolean isInit;
	private int addCount;
	private List<TreePath> selections;

	// private final HashMap<String, ASTDummy> keys2ASTDummys;

	public NonFeatureContentProvider(NonFeatureManagerView nonFeatureManager) {
		this.nonFeatureManager = nonFeatureManager;
		// keys2ASTDummys = new HashMap<String, ASTDummy>();

		this.AC = ApplicationController.getInstance();
		addCount = 0;

		invisibleRoot = new NonFeatureTreeNode(
				NonFeatureTreeNode.NODE_KIND.ROOT, "", 0);
		isInit = false;
		checkIsIntialized();

		AC.addObserver(this);

		// CIDECorePlugin.getDefault().addColorChangeListener(this);
	}

	private NonFeatureTreeNode invisibleRoot;

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	private boolean checkIsIntialized() {

		if (isInit)
			return true;

		IProject project = AC.getInitializedProject();

		if (project == null) {
			nonFeatureManager.setInfoMessage(
					"Database has not been created for Feature Mining",
					MESSAGE_TYPE.ERROR);
			return false;
		}

		nonFeatureManager.setInfoMessage("Database created for "
				+ project.getName(), MESSAGE_TYPE.INFO);
		isInit = true;

		return true;
	}

	private int[] calculateCounts(NonFeatureTreeNode curNode) {

		if (curNode == null)
			return new int[] { 0, 0 };

		if (curNode.getKind() == NODE_KIND.ELEMENT) {
			return new int[] { curNode.getViewCount(),
					curNode.getElementsCount() };
		}

		int[] counter = new int[] { 0, 0 };

		for (NonFeatureTreeNode child : curNode.getChildren()) {
			int[] tmpCounter = calculateCounts(child);
			counter[0] += tmpCounter[0];
			counter[1] += tmpCounter[1];
		}

		curNode.setViewCount(counter[0]);
		curNode.setElementsCount(counter[1]);

		return counter;
	}

	public Object[] getElements(Object parent) {

		if (parent.equals(nonFeatureManager.getViewSite())) {
			calculateCounts(invisibleRoot);
			return getChildren(invisibleRoot);
		}

		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof NonFeatureTreeNode) {
			return ((NonFeatureTreeNode) child).getNodeParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof NonFeatureTreeNode) {
			return ((NonFeatureTreeNode) parent).getChildren();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof NonFeatureTreeNode)
			return ((NonFeatureTreeNode) parent).hasChildren();
		return false;
	}

	private NonFeatureTreeNode getColorNode(IFeature color) {

		for (NonFeatureTreeNode node : invisibleRoot.getChildren()) {
			if (node.getID().equals(color.getName())) {
				return node;
			}
		}

		NonFeatureTreeNode tmpFeatureNode = new NonFeatureTreeNode(
				NonFeatureTreeNode.NODE_KIND.FEATURE, color, addCount);
		invisibleRoot.addChild(tmpFeatureNode);

		return tmpFeatureNode;

	}

	private NonFeatureTreeNode getCompilationUnitNode(
			NonFeatureTreeNode featureNode, int cuHash) {

		for (NonFeatureTreeNode node : featureNode.getChildren()) {
			if (node.getID().equals(String.valueOf(cuHash))) {
				return node;
			}
		}

		ICompilationUnit iCU = AC.getICompilationUnit(cuHash);
		String cuName = iCU.getResource().getProjectRelativePath().toString();
		CUDummy dummy = new CUDummy(cuName, cuHash);
		NonFeatureTreeNode tmpCUNode = new NonFeatureTreeNode(
				NonFeatureTreeNode.NODE_KIND.COMPILATION_UNIT, dummy, addCount);
		featureNode.addChild(tmpCUNode);

		return tmpCUNode;

	}

	private NonFeatureTreeNode getElementNode(NonFeatureTreeNode featureNode,
			AElement element) {

		for (NonFeatureTreeNode node : featureNode.getChildren()) {
			if (node.getID().equals(element.getId())) {
				return node;
			}
		}

		return null;

	}

	private void addElement(IFeature color, AElement element) {

		NonFeatureTreeNode tmpFeatureNode = getColorNode(color);
		NonFeatureTreeNode tmpCUNode = getCompilationUnitNode(tmpFeatureNode,
				element.getCompelationUnitHash());

		NonFeatureTreeNode tmpElementNode = new NonFeatureTreeNode(
				NonFeatureTreeNode.NODE_KIND.ELEMENT, element, addCount);
		tmpCUNode.addChild(tmpElementNode);

		// SELECT THE NEW AST NODE
		// build the path
		selections.add(new TreePath(new Object[] { tmpFeatureNode, tmpCUNode,
				tmpElementNode }));

	}

	private void removeElement(IFeature color, AElement element) {

		NonFeatureTreeNode tmpFeatureNode = getColorNode(color);
		NonFeatureTreeNode tmpCUNode = getCompilationUnitNode(tmpFeatureNode,
				element.getCompelationUnitHash());
		NonFeatureTreeNode tmpElementNode = getElementNode(tmpCUNode, element);

		if (tmpElementNode != null)
			tmpCUNode.removeChild(tmpElementNode);

		// remove parent nodes if they do not have children
		if (tmpCUNode.getChildrenCount() == 0) {
			tmpFeatureNode.removeChild(tmpCUNode);

			// if (tmpFeatureNode.getChildrenCount() == 0) {
			// invisibleRoot.removeChild(tmpFeatureNode);
			// }
		}

	}

	public void elementNonColorsChanged(AElementsPostNonColorChangedEvent event) {

		selections = new ArrayList<TreePath>();

		if (event.getAddedElements().size() > 0)
			addCount++;

		for (AElement element : event.getAddedElements().keySet()) {
			IFeature color = event.getAddedElements().get(element);
			addElement(color, element);
		}

		for (AElement element : event.getRemovedElements().keySet()) {
			IFeature color = event.getRemovedElements().get(element);
			removeElement(color, element);
		}

		nonFeatureManager.getTreeViewer().refresh();

		// reveal elements
		for (TreePath path : selections) {
			nonFeatureManager.getTreeViewer().reveal(path);
		}

	}

	public void selectElements(AElementViewCountChangedEvent event) {

		AElement elementToSelect = event.getElement();
		selections = new ArrayList<TreePath>();

		for (NonFeatureTreeNode feature : invisibleRoot.getChildren()) {
			for (NonFeatureTreeNode cu : feature.getChildren()) {
				for (NonFeatureTreeNode element : cu.getChildren()) {
					if (((AElement) element.getDataObject())
							.equals(elementToSelect)) {
						selections.add(new TreePath(new Object[] { feature, cu,
								element }));
					}
				}

			}
		}

		setSelections();

	}

	private void setSelections() {
		nonFeatureManager.getTreeViewer().refresh();

		if (selections.size() > 0) {
			TreePath[] selArray = new TreePath[selections.size()];
			for (int i = 0; i < selArray.length; i++) {
				selArray[i] = selections.get(i);

			}
			nonFeatureManager.getTreeViewer().setSelection(
					new TreeSelection(selArray), true);
		}

	}

	public void update(final Observable o, final Object arg) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {

				if (o.equals(AC)) {

					if (arg instanceof AInitEvent) {
						nonFeatureManager.setInfoMessage(
								"Database created for "
										+ ((AInitEvent) arg).getProject()
												.getName(), MESSAGE_TYPE.INFO);
						isInit = true;

						// CREATE FEATURES

						for (IFeature color : AC.getProjectFeatures()) {
							NonFeatureTreeNode tmpFeatureNode = new NonFeatureTreeNode(
									NonFeatureTreeNode.NODE_KIND.FEATURE,
									color, addCount);
							invisibleRoot.addChild(tmpFeatureNode);
						}

						nonFeatureManager.getTreeViewer().refresh();

					} else if (arg instanceof AElementViewCountChangedEvent) {

						if (!((AElementViewCountChangedEvent) arg)
								.isPreviewMode()) {
							selectElements((AElementViewCountChangedEvent) arg);
						}

						nonFeatureManager.getTreeViewer().refresh();

					} else if (arg instanceof AElementsPostNonColorChangedEvent) {
						elementNonColorsChanged((AElementsPostNonColorChangedEvent) arg);

					}

				}
			}
		});
	}

}