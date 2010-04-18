package de.ovgu.cide.mining.relationmanager;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import cide.gast.ASTNode;

import de.ovgu.cide.language.jdt.UnifiedASTNode;
import de.ovgu.cide.mining.database.ConversionException;
import de.ovgu.cide.mining.database.ElementNotFoundException;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.model.ARelation;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.relationmanager.RelationManagerView.MESSAGE_TYPE;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode.NODE_KIND;
import de.ovgu.cide.util.EditorUtilityJava;
import de.ovgu.cide.utils.EditorUtility;



/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content 
 * (like Task List, for example).
 */

class RelationContentProvider implements IStructuredContentProvider,
		ITreeContentProvider,  ISelectionListener, Observer {
	
	private final RelationManagerView relationManager;
	private final ApplicationController AC;

	private AIElement topElement;
	
	private boolean isInit;
	
	public RelationContentProvider(RelationManagerView viewPart) {
		// TODO Auto-generated constructor stub
		this.relationManager = viewPart;
		this.AC = ApplicationController.getInstance();
		isInit = false;
		checkIsIntialized();
		
		ISelectionService service = viewPart.getSite().getWorkbenchWindow().getSelectionService();		
		service.addPostSelectionListener(this);
		AC.addObserver(this);
	
	}

	

	
	private RelationTreeNode invisibleRoot;

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	private boolean checkIsIntialized() {
		
		if (isInit)
			return true;
		
		IProject project = AC.getInitializedProject();
		
		if (project == null) {
			relationManager.setInfoMessage("Data base has not been created for Feature Mining", MESSAGE_TYPE.ERROR);
			return false;
		}
		
		relationManager.setInfoMessage("Data base been created for " + project.getName(), MESSAGE_TYPE.INFO);
		isInit = true;
		
		return true;
	}
	
	private int calculateViews(RelationTreeNode curNode) {
		
		if (curNode == null)
			return 0;
		
		if (curNode.getKind() == NODE_KIND.ELEMENT) {
			return curNode.getViewCount();
		}
		
		int curCounter = 0;
		
		for (RelationTreeNode child : curNode.getChildren()) {
			curCounter += calculateViews(child);
		}
		
		curNode.setViewCount(curCounter);
		
		return curCounter;
	}
	
	public Object[] getElements(Object parent) {
		
		//System.out.println( " ==>=>=>=>= GET ELEEMENTS:" + parent.toString());
			
		if (parent.equals(relationManager.getViewSite())) {		
			calculateViews(invisibleRoot);
			return getChildren(invisibleRoot);
		}
		
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		
		if (child instanceof RelationTreeNode) {
			return ((RelationTreeNode) child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		
		if (parent instanceof RelationTreeNode) {
			return ((RelationTreeNode) parent).getChildren();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		
		if (parent instanceof RelationTreeNode)
			return ((RelationTreeNode) parent).hasChildren();
		return false;
	}

	public AIElement getTopElement() {
		return topElement;
	}

	
	public void displayItemChanged (AIElement jayElement, boolean logChange) {
		displayItemChanged(jayElement, false, logChange);
	}		
	
	private void displayItemChanged (AIElement jayElement, boolean internal, boolean logChange) {
			
		//TODO PRÜFEN OB DAS ALTE ELEMENT AUCH DAS NEUE IST!
		
		AIElement oldTopElement = topElement;
		
		if (!internal)
			relationManager.getTree().deselectAll();
		
		//BUILD TREE TO DISPLAY
		invisibleRoot = null;
			

		try {
			
			//check if relations are available
			if (jayElement == null) {
				relationManager.setInfoMessage("There are no relations for the selected element in the database.", MESSAGE_TYPE.WARNING);
				topElement = null;
				relationManager.getTreeViewer().refresh();
				return;
			}		
			
			relationManager.setInfoMessage("", MESSAGE_TYPE.NONE);
	
			topElement = jayElement;
			
			invisibleRoot = new RelationTreeNode(NODE_KIND.ROOT,"");
			invisibleRoot.addChild(new RelationTreeNode(NODE_KIND.ELEMENT, jayElement));
		
			Set<ARelation> validDirectRelations = ARelation.getAllRelations(jayElement.getCategory(),false, true);
			for (AICategories cat : jayElement.getSubCategories()) {
				validDirectRelations.addAll(ARelation.getAllRelations(cat,false, true));
			}
			
			Set<ARelation> validTransponseRelations = ARelation.getAllRelations(jayElement.getCategory(),false, false);
			for (AICategories cat : jayElement.getSubCategories()) {
				validTransponseRelations.addAll(ARelation.getAllRelations(cat,false, false));
			}
			
			//create structure nodes
			RelationTreeNode directRelNode = new RelationTreeNode(NODE_KIND.FOLDER,"X: Direct Relations");
			RelationTreeNode transRelNode = new RelationTreeNode(NODE_KIND.FOLDER,"X: Transpose Relations");
			
			RelationTreeNode tmpStructureNode = null;
			
			//DIRECT!
			boolean hasDirectRelations = false;
			for (ARelation tmpDirectRelation :  validDirectRelations) {
			
				try {
					tmpStructureNode = new RelationTreeNode(NODE_KIND.FOLDER, tmpDirectRelation.getName());
					Set<AIElement> elements = AC.getRange(jayElement, tmpDirectRelation);
					
					for (AIElement iElement : elements) {
						tmpStructureNode.addChild(new RelationTreeNode(NODE_KIND.ELEMENT, iElement));
					}
					
					if (elements.size() > 0) {
						directRelNode.addChild(tmpStructureNode);	
						hasDirectRelations = true;
					}
					
				} catch (Exception e) {
					//directFail.addChild(tmpStructureNode);
					//System.out.println("OTHER ERROR FOR " + element.getElementName() + " (DIRECT-RELATION:" +validDirectRelations[i]  + ") in the database.");
					
				}
			}
			
			if (hasDirectRelations)
				invisibleRoot.addChild(directRelNode);	
			
			
			
			//transpose
			boolean hasTransRelations = false;
			for (ARelation tmpTranRelation: validTransponseRelations) {
				
				
				try {
					tmpStructureNode = new RelationTreeNode(NODE_KIND.FOLDER, tmpTranRelation.getName());
					Set<AIElement> elements = AC.getRange(jayElement, tmpTranRelation);
					
					
					for (AIElement iElement : elements) {
						tmpStructureNode.addChild(new RelationTreeNode(NODE_KIND.ELEMENT,iElement));
					}
					if (elements.size() > 0) {
						transRelNode.addChild(tmpStructureNode);
						hasTransRelations = true;
					}
					
				} catch (Exception e) {
					//transFail.addChild(tmpStructureNode);
					//System.out.println("OTHER ERROR FOR " + element.getElementName() + " (TRANSPONSE-RELATION:" +validDirectRelations[i]  + ") in the database.");
					
				}
		
			}
			
			if (hasTransRelations)
				invisibleRoot.addChild(transRelNode);	
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			relationManager.setInfoMessage("There is no entry for the selected element in the database.", MESSAGE_TYPE.WARNING);
			topElement = null;
		} 
	
		if (logChange && oldTopElement != null && topElement != null && !topElement.equals(oldTopElement)) {
			relationManager.addToHistory(oldTopElement);
		}
	
		relationManager.getTreeViewer().refresh();
		relationManager.getTreeViewer().expandToLevel(2);

	}
	
	/* 
	 * IMPLEMENTED METHODS
	 */
	
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		if (!checkIsIntialized())
			return;
		

//		if (selection instanceof ITextSelection) {
//			
//			ITextSelection textSelection = (ITextSelection) selection;
//			
//			if (previewModus) {
//				previewModus = false;
//				oldSelection = textSelection;
//				return;
//			}
//			else {
//				if (oldSelection != null && oldSelection.equals(textSelection)) {
//					oldSelection = textSelection;
//					return;
//				}
//			}
//			
//			oldSelection = textSelection;
//			
//			
//			//show element for editor selection
//			
//			int offset = textSelection.getOffset();
//			int length = textSelection.getLength();
//		
//			if (length == 0)
//				return;
//			
//			IEditorPart editor = EditorUtility.getActiveEditor();
//			if (editor == null)
//				return;
//			
//			ICompilationUnit CU = EditorUtilityJava.getCompilationUnitFromInput(editor);
//			if (CU == null)
//			return;
//			
//			int CUHash = CU.hashCode();
//			UnifiedASTNode node;
//			
//			for(AIElement element : jayFX.getAllElements()) {
//				if (element.getCompelationUnitHash() != CUHash)
//					continue;
//				
//				node = element.getUnifiedASTNode();
//				if (node.getStartPosition() != offset)
//					continue;
//				
//				if (node.getLength() != length)
//					continue;
//				
//				displayItemChanged(element, true);
//				
//				
//				return;
//			}	
//			
//			displayItemChanged(null, true, false);
//			
//		}
//			
		
		
		if (!(selection instanceof TreeSelection))
			return;
			
		TreeSelection tSelection = (TreeSelection) selection;
		if (!(tSelection.getFirstElement() instanceof ASTNode))
			return;
		
		
		ASTNode astNode = (ASTNode) tSelection.getFirstElement();
		AIElement jayElement = null;
		
		
		try {
			jayElement = AC.convertToElement(astNode);
			
		} catch (ConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		displayItemChanged(jayElement,true, true);
		
	}
	
	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {
			if (arg instanceof AInitEvent) {
				relationManager.setInfoMessage("Data base been created for " + ((AInitEvent)arg).getProject().getName(), MESSAGE_TYPE.INFO);
				isInit = true;
			}
			else if (arg instanceof AElementViewCountChangedEvent) {
				
				if (relationManager.equals(((AElementViewCountChangedEvent)arg).getPreviewSource())) {
					relationManager.getTreeViewer().refresh();
					
				}
				else {					
					displayItemChanged(((AElementViewCountChangedEvent)arg).getElement(), true);
				}	
	
				
		
			}
	
	 		
			
		}
		
	}
}