package de.ovgu.cide.mining.featuremanager;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;

import cide.gast.IASTNode;
import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostColorChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostNonColorChangedEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.featuremanager.FeatureManagerView.MESSAGE_TYPE;
import de.ovgu.cide.mining.featuremanager.model.ASTDummy;
import de.ovgu.cide.mining.featuremanager.model.CUDummy;
import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode;
import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode.NODE_KIND;


class FeatureContentProvider implements IStructuredContentProvider,
		ITreeContentProvider, Observer  {
	
	private final FeatureManagerView featureManager;
	private final ApplicationController AC;
	//private  AElementColorManager elementColorManager;
	private boolean isInit;
	private int addCount;
	private List<TreePath> selections;
	
	//private final HashMap<String, ASTDummy> keys2ASTDummys;
	
	
	
	public FeatureContentProvider(FeatureManagerView featureManager) {
		this.featureManager = featureManager;
	//	keys2ASTDummys = new HashMap<String, ASTDummy>();
		
		this.AC = ApplicationController.getInstance();
		addCount = 0;
		
		invisibleRoot = new FeatureTreeNode(FeatureTreeNode.NODE_KIND.ROOT,"",0);
		
				
		isInit = false;
		checkIsIntialized();	
		
		AC.addObserver(this);
		
		
		//CIDECorePlugin.getDefault().addColorChangeListener(this);
	}

	
	private FeatureTreeNode invisibleRoot;
	
	

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	private boolean checkIsIntialized() {
		
		if (isInit)
			return true;
		
		IProject project = AC.getInitializedProject();
		
		if (project == null) {
			featureManager.setInfoMessage("Data base has not been created for Feature Mining", MESSAGE_TYPE.ERROR);
			return false;
		}
		
		featureManager.setInfoMessage("Data base been created for " + project.getName(), MESSAGE_TYPE.INFO);
		isInit = true;
		
		return true;
	}
	
	private int calculateViews(FeatureTreeNode curNode) {
		
		if (curNode == null)
			return 0;
		
		if (curNode.getKind() == NODE_KIND.ELEMENT) {
			return curNode.getViewCount();
		}
		
		int counter = 0;
		
		for (FeatureTreeNode child : curNode.getChildren()) {
			counter += calculateViews(child);
		}
		
		curNode.setViewCount(counter);
		
		return counter;
	}
	
	public Object[] getElements(Object parent) {
		
			
		if (parent.equals(featureManager.getViewSite())) {	
			
			//calculate views
			calculateViews(invisibleRoot);
			
			return getChildren(invisibleRoot);
		}
		
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		//System.out.println( " ==>=>=>=>= GET PARENT:" + child.toString());
		
		if (child instanceof FeatureTreeNode) {
			return ((FeatureTreeNode) child).getNodeParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
//		System.out.println( " ==>=>=>=>= GET CHILDREN:" + parent.toString());
		
		if (parent instanceof FeatureTreeNode) {
			return ((FeatureTreeNode) parent).getChildren();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
//		System.out.println( " ==>=>=>=>= HAS CHILDREN:" + parent.toString());
		
		if (parent instanceof FeatureTreeNode)
			return ((FeatureTreeNode) parent).hasChildren();
		return false;
	}

	
	
	private FeatureTreeNode getColorNode(IFeature color) {
		
		for (FeatureTreeNode node : invisibleRoot.getChildren()) {
			if (node.getID().equals(color.getName())) {
				return node;
			}
		} 

		FeatureTreeNode tmpFeatureNode = new FeatureTreeNode(FeatureTreeNode.NODE_KIND.FEATURE, color, addCount);
		invisibleRoot.addChild(tmpFeatureNode);
		
		return tmpFeatureNode;
		
	}
	
	private FeatureTreeNode getCompilationUnitNode(FeatureTreeNode featureNode, CUDummy dummy) {
		
		for (FeatureTreeNode node : featureNode.getChildren()) {
			if (node.getID().equals(String.valueOf(dummy.getHashCode()))) {
				return node;
			}
		} 

		FeatureTreeNode tmpCUNode = new FeatureTreeNode(FeatureTreeNode.NODE_KIND.COMPILATION_UNIT, dummy, addCount);
		featureNode.addChild(tmpCUNode);
		
		return tmpCUNode;
		
	}
	
	private FeatureTreeNode getASTNode(FeatureTreeNode cuNode, ASTDummy dummy) {
		
		for (FeatureTreeNode node : cuNode.getChildren()) {
			if (node.getID().equals(dummy.getId())) {
				return node;
			}
		} 

		FeatureTreeNode tmpASTNode = new FeatureTreeNode(FeatureTreeNode.NODE_KIND.ASTDUMMY, dummy, addCount);
		cuNode.addChild(tmpASTNode);
		
		return tmpASTNode;
		
	}
	
	
	
	private void addElements(Set<IFeature> addColors, IASTNode node, CUDummy cuDummy, Set<AIElement> elements) {
		
		
		for (IFeature color : addColors) {
			
			FeatureTreeNode tmpFeatureNode = getColorNode(color);
			FeatureTreeNode tmpCUNode = getCompilationUnitNode(tmpFeatureNode, cuDummy);
			
			ASTDummy dummy = new ASTDummy(node, cuDummy.getHashCode());
			
			FeatureTreeNode tmpDummyNode = new FeatureTreeNode(FeatureTreeNode.NODE_KIND.ASTDUMMY, dummy, addCount);
			tmpCUNode.addChild(tmpDummyNode);
				
			for(AIElement element : elements) {		
				
				FeatureTreeNode tmpElementNode = new FeatureTreeNode(FeatureTreeNode.NODE_KIND.ELEMENT, element, addCount);
				tmpDummyNode.addChild(tmpElementNode);
							
			}	
			
			//SELECT THE NEW AST NODE
			//build the path
			
			selections.add( new TreePath(new Object[] {tmpFeatureNode, tmpCUNode, tmpDummyNode}));
			
			
		}
		
	}
	
	private void removeElements(Set<IFeature> removeColors, IASTNode node, CUDummy cuDummy) {
		
		for (IFeature color : removeColors) {
			
			FeatureTreeNode tmpFeatureNode = getColorNode(color);
			FeatureTreeNode tmpCUNode = getCompilationUnitNode(tmpFeatureNode, cuDummy);
			
			ASTDummy astDummy = new ASTDummy(node, cuDummy.getHashCode());
			
			FeatureTreeNode tmpDummyNode = getASTNode(tmpCUNode, astDummy);
			
			tmpCUNode.removeChild(tmpDummyNode);
		
			//remove parent nodes if they do not have children
			if (tmpCUNode.getChildrenCount() == 0) {
				tmpFeatureNode.removeChild(tmpCUNode);
				
//				if (tmpFeatureNode.getChildrenCount() == 0) {
//					invisibleRoot.removeChild(tmpFeatureNode);
//				}
				
			}
						
		}
		
	}
	
	

	
	
	public void elementColorsChanged(AElementsPostColorChangedEvent event) {	
			
		selections = new ArrayList<TreePath>();
		
		CUDummy baseCUDummy = new CUDummy(event.getCuName(), event.getCuHashCode());
		
		if (event.getNode2AddColors().size() > 0)
			addCount++;
		
		for (IASTNode node : event.getNode2AddColors().keySet()) {
			Set<IFeature> colors = event.getNode2AddColors().get(node);
			Set<AIElement> elements = event.getNode2elements().get(node);
			addElements(colors, node, baseCUDummy, elements);
		}
		
		for (IASTNode node : event.getNode2RemoveColors().keySet()) {
			Set<IFeature> colors = event.getNode2RemoveColors().get(node);
			removeElements(colors, node, baseCUDummy);
		}
	
		
		
		featureManager.getTreeViewer().refresh();
		for (TreePath path: selections) {
			featureManager.getTreeViewer().reveal(path);
		}
		
		
		
		//setSelections();
		
	}
	
	
	
	public void selectElements(AElementViewCountChangedEvent event) {	
		
		AIElement elementToSelect = event.getElement();
		selections = new ArrayList<TreePath>();
		
		for (FeatureTreeNode feature : invisibleRoot.getChildren()) {
			for (FeatureTreeNode cu : feature.getChildren()) {
				for (FeatureTreeNode ast : cu.getChildren()) {
					for (FeatureTreeNode element : ast.getChildren()) {
						if (((AIElement)element.getDataObject()).equals(elementToSelect)) {
							selections.add(new TreePath(new Object[] {feature, cu, ast, element}));
						}
					}
				}
			}
		}
		
		setSelections();
		
	}
	
	private void setSelections() {
		featureManager.getTreeViewer().refresh();

		if (selections.size() > 0) {
			TreePath[] selArray = new TreePath[selections.size()];
			for (int i = 0; i < selArray.length; i++) {
				selArray[i] = selections.get(i);
				
			}
			featureManager.getTreeViewer().setSelection(new TreeSelection(selArray),true);
		}
	
	}


	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {
			
			if (arg instanceof AInitEvent) {
				featureManager.setInfoMessage("Data base been created for " + ((AInitEvent)arg).getProject().getName(), MESSAGE_TYPE.INFO);
				isInit = true;
				
				//CREATE FEATURES
				for (IFeature color : AC.getProjectFeatures()) {
					FeatureTreeNode tmpFeatureNode = new FeatureTreeNode(FeatureTreeNode.NODE_KIND.FEATURE, color, addCount);
					invisibleRoot.addChild(tmpFeatureNode);
				}
				
				featureManager.getTreeViewer().refresh();
				
				
				
			}
			else if (arg instanceof AElementViewCountChangedEvent) {
				if (!((AElementViewCountChangedEvent)arg).isPreviewMode()) {
					selectElements((AElementViewCountChangedEvent)arg);
				}
		
				featureManager.getTreeViewer().refresh();
				
			} 
			else if (arg instanceof AElementsPostColorChangedEvent) {
				elementColorsChanged((AElementsPostColorChangedEvent)arg);
				
			} 
			else if (arg instanceof AElementsPostNonColorChangedEvent) {
				featureManager.getTreeViewer().refresh();
				
			} 
			
		}

	}
	
	
}