package de.ovgu.cide.mining.recommendationmanager;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostColorChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostNonColorChangedEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.events.ARecommenderElementSelectedEvent;
import de.ovgu.cide.mining.events.ARecommenderElementSelectedEvent.EVENT_TYPE;
import de.ovgu.cide.mining.recommendationmanager.RecommendationManagerView.MESSAGE_TYPE;
import de.ovgu.cide.mining.recommendationmanager.model.RecommendationTreeNode;
import de.ovgu.cide.mining.recommendationmanager.model.RecommendationTreeNode.NODE_KIND;
import de.ovgu.cide.util.Statistics;


class RecommendationContentProvider implements IStructuredContentProvider, ITreeContentProvider,  Observer  {
	
	private final RecommendationManagerView recommendationManager;
	private final ApplicationController AC;
	private boolean isInit;

	
	private ARecommenderElementSelectedEvent curEvent;
	private RecommendationTreeNode invisibleRoot;
	
	public RecommendationContentProvider(RecommendationManagerView featureManager) {
		this.recommendationManager = featureManager;		
		this.AC = ApplicationController.getInstance();
		invisibleRoot = new RecommendationTreeNode(RecommendationTreeNode.NODE_KIND.ROOT,null,null,null);
		
		isInit = false;
		checkIsIntialized();	
		
		AC.addObserver(this);
		
	}

	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	private boolean checkIsIntialized() {
		
		if (isInit)
			return true;
		
		IProject project = AC.getInitializedProject();
		
		if (project == null) {
			recommendationManager.setInfoMessage("Data base has not been created for Feature Mining", MESSAGE_TYPE.ERROR);
			return false;
		}
		
		recommendationManager.setInfoMessage("Data base been created for " + project.getName(), MESSAGE_TYPE.INFO);
		isInit = true;
		
		return true;
	}
	
	

	public void showRecommendations(ARecommenderElementSelectedEvent event) {
		
		if (event == null) {
			recommendationManager.setInfoMessage("There are no recommendations for the selected element.", MESSAGE_TYPE.WARNING);
			recommendationManager.getTreeViewer().refresh();
			return;
		}
		
			
		//BUILD TREE TO DISPLAY
		invisibleRoot = new RecommendationTreeNode(RecommendationTreeNode.NODE_KIND.ROOT,null,null,null);
		Map<AElement, ARecommendationContextCollection> recommendations;
		
		if (event.getType().equals(EVENT_TYPE.ELEMENT)) {
			AElement sourceElement = event.getElement();
			recommendations = AC.getRecommendations(event.getColor(), sourceElement);
		}
		else {
			recommendations = AC.getRecommendations(event.getColor(), event.getStart(), event.getEnd(), event.getCuHash());
		}
		
		for (AElement tmpElement : recommendations.keySet()) {
			ARecommendationContextCollection collection = recommendations.get(tmpElement);
			RecommendationTreeNode tmpCollectionNode = new RecommendationTreeNode(NODE_KIND.CONTEXTCOLLECTION, tmpElement, event.getColor(), collection);
			invisibleRoot.addChild(tmpCollectionNode);
			for (ARecommendationContext context : collection.getContexts()) {
				tmpCollectionNode.addChild(new RecommendationTreeNode(NODE_KIND.CONTEXT, context.getSupporter(), null, context));
			}
		}
			
		if (invisibleRoot.getChildrenCount() > 0) {
			recommendationManager.setInfoMessage("Recommendations for selected element", MESSAGE_TYPE.INFO);
		}
		else {
			recommendationManager.setInfoMessage("There are no recommendations for the selected element.", MESSAGE_TYPE.WARNING);
		}
	
		recommendationManager.getTreeViewer().refresh();
		
		//STATISTICS!
//		printForStatistics();
		
		
	}
	
	
	
	//<-- STATISTICS
//	final Set<String> featureExpElements = Statistics.loadFeatureElements(true);
//	final Set<String> featureOrgElements = Statistics.loadFeatureElements(false);
//	
//	
//	private void printForStatistics() {
//		StringBuilder line = new StringBuilder();
//		StringBuilder row = new StringBuilder();
//		
//		
//		int items = Math.min(recommendationManager.getTreeViewer().getTree().getItemCount(), 50);
//		
//		for (int i = 0; i < items ; i++) {
//		
//			RecommendationTreeNode node = (RecommendationTreeNode) recommendationManager.getTreeViewer().getTree().getItem(i).getData();
//			boolean isFeatureExpElement = featureExpElements.contains(node.getElement().getId());
//			boolean isFeatureOrignalElement = featureOrgElements.contains(node.getElement().getId());
//			
//			
//			if (i==0) {
//				recommendationManager.setInfoMessage(node.getDisplayName()+"   +++ "+isFeatureExpElement+ " +++"+"   +++ "+isFeatureOrignalElement+ " +++" , MESSAGE_TYPE.INFO);
//				
//				line.append(AC.getElementsOfColor(node.getColor()).size());
//				line.append("\t");
//				
//				line.append(AC.getElementsOfNonColor(node.getColor()).size());
//				line.append("\t");				
//			
//				if (isFeatureOrignalElement) {
//					line.append("true");
//					line.append("\t");
//					line.append(node.getSupportValueAsString());
//					line.append("\t");
//				}
//				else {
//					line.append("\t\t");
//						
//				}
//				
//				
//				if (!isFeatureOrignalElement && isFeatureExpElement){
//					line.append("true");
//					line.append("\t");
//					line.append(node.getSupportValueAsString());
//					line.append("\t");
//				}
//				else {
//					line.append("\t\t");
//						
//				}
//					
//				if (!isFeatureExpElement){
//					line.append("false");
//					line.append("\t");
//					line.append(node.getSupportValueAsString());
//					line.append("\t");
//				}
//				else {
//					line.append("\t\t");		
//				}
//				
//				row.append(node.getSupportValueAsString());
//				row.append("\t");
//				
//				row.append(isFeatureExpElement);
//				row.append(System.getProperty("line.separator"));
//				
//			}
//			else {
//			
//				line.append(isFeatureExpElement);
//				line.append("\t");
//			
//				line.append(node.getSupportValueAsString());
//				line.append("\t");
//				
//			}
//			
//			
//			
//		}
//		
//		//print line!
//		Statistics.writeRecommendations(line.toString(), row.toString());	
//		
//	}	
	//STATISTICS-->
	
	
	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {
			
			if (arg instanceof AInitEvent) {
				recommendationManager.setInfoMessage("Data base been created for " + ((AInitEvent)arg).getProject().getName(), MESSAGE_TYPE.INFO);
				isInit = true;
							
			}
			else if (arg instanceof AElementViewCountChangedEvent) {
				recommendationManager.getTreeViewer().refresh();
			}
			else if (arg instanceof ARecommenderElementSelectedEvent) {
				
				curEvent = (ARecommenderElementSelectedEvent)arg;
				showRecommendations(curEvent);
				
			} else if (arg instanceof AElementsPostColorChangedEvent) {
				showRecommendations(curEvent);
			}
			else if (arg instanceof AElementsPostNonColorChangedEvent) {
				showRecommendations(curEvent);
			}
	
			
		}

	}


	public Object[] getElements(Object parent) {
		
		
		if (parent.equals(recommendationManager.getViewSite())) {	
			
			return getChildren(invisibleRoot);
		}
		
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		
		if (child instanceof RecommendationTreeNode) {
			return ((RecommendationTreeNode) child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		
		if (parent instanceof RecommendationTreeNode) {
			return ((RecommendationTreeNode) parent).getChildren();
		}
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		
		if (parent instanceof RecommendationTreeNode)
			return ((RecommendationTreeNode) parent).hasChildren();
		return false;
	}
	
	
}