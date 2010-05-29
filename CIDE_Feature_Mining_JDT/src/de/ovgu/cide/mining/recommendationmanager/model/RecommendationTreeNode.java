package de.ovgu.cide.mining.recommendationmanager.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.text.Element;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.accessibility.ACC;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.language.jdt.UnifiedASTNode;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.recommendationengine.AElementViewCountManager;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;

public class RecommendationTreeNode implements IAdaptable  {
	
	public static enum NODE_KIND {ROOT, CONTEXTCOLLECTION, CONTEXT};

	private NODE_KIND kind;
	private Object data;
	private ArrayList<RecommendationTreeNode> children;
	private RecommendationTreeNode parent;
	private AIElement element;
	private ApplicationController AC;
	private IFeature color;
	
	public RecommendationTreeNode(NODE_KIND kind, AIElement element, IFeature color, Object data) {
		this.kind = kind;
		this.data = data;
		this.element = element;
		
	
		
		AC = ApplicationController.getInstance();
		this.color = color;
		children = new ArrayList<RecommendationTreeNode>();
	}
	
	public IFeature getColor() {
		return color;
	}
	
		
	public void setParent(RecommendationTreeNode parent) {
		this.parent = parent;
	}
	
	
	public RecommendationTreeNode getParent() {
		return parent;
	}
	
	//inherited...
	public Object getAdapter(Class key) {
		return null;
	}
	
	public NODE_KIND getKind() {
		return kind;
	}
	
	public Object getDataObject() {
		return data;
	}

	public void addChild(RecommendationTreeNode child) {
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild(RecommendationTreeNode child) {
		children.remove(child);
		child.setParent(null);
	}
	
	public RecommendationTreeNode[] getChildren() {
		return (RecommendationTreeNode [])children.toArray(new RecommendationTreeNode[children.size()]);
	}
	
	public int getChildrenCount() {
		return children.size();
	}
	
	public boolean hasChildren() {
		return children.size()>0;
	}
	
	public AIElement getElement() {
		return element;
	}
	
	public String getDisplayName() {
		switch (kind) {
		case CONTEXTCOLLECTION:
			return element.getShortName();
		case CONTEXT:
			return element.getShortName();

		}
		
		return null;
	}
	
	public int getTypePriority() {
		
		switch (element.getCategory()) {
		
		case COMPILATION_UNIT:
			return 0;
		case TYPE:
			return 1;
		case METHOD:
			return 2;
		case FIELD:
			return 3;
		case LOCAL_VARIABLE:
			return 4;
		case IMPORT:
			return 5;	
		case METHOD_ACCESS:
			return 6;					
		}
		
		return 7;
	}
	
	public String getTypePriorityString() {
		return String.valueOf(getTypePriority());
	}
	
	
	public double getSupportValue() {
		switch (kind) {
		case CONTEXTCOLLECTION:
			return ((ARecommendationContextCollection)data).getSupportValue();
		case CONTEXT:
			return ((ARecommendationContext)data).getSupportValue();

		}
		return 0;
	}
	
	public int getSupportersCount() {
		switch (kind) {
		case CONTEXTCOLLECTION:
			return getChildrenCount();
		case CONTEXT:
			return 1;

		}
		return 0;
	}
	
	public String getSupportersCountString() {
		return String.valueOf(getSupportersCount());
	}
	
	
	private double roundSupportValue(double supportValue) {
		
		return (double)((int)(supportValue*100))/(double)100;
	}
	
	public String getSupportValueAsString() {

		return String.valueOf(roundSupportValue(getSupportValue()));
	}
	
	public String getReasons() {
		switch (kind) {
		case CONTEXTCOLLECTION:
			return ((ARecommendationContextCollection)data).getSupportReasons();
		case CONTEXT:
			return ((ARecommendationContext)data).getRecommenderType()+":"+((ARecommendationContext)data).getReason();
		}
		
		return "";
	}
	
	
	public String getMaxSupportFeature() {
		switch (kind) {
		case CONTEXTCOLLECTION:
			
			double supportValue = getSupportValue();
			Set<String> betterRecommendations = new TreeSet<String>();
			
			Map<IFeature,  ARecommendationContextCollection> recommendationMap = AC.getAllRecommendations(element);
			for (IFeature color : recommendationMap.keySet()) {
				
				if (color.equals(this.color))
					continue;
				
				ARecommendationContextCollection collection = recommendationMap.get(color);
				double tmpSupportValue = collection.getSupportValue();
				
				if (tmpSupportValue > supportValue) {
					betterRecommendations.add(color.getName()+" [" +roundSupportValue(tmpSupportValue)+"]");
				}
			} 
			
			String result = "";
			for (String tmp : betterRecommendations) {
				result += tmp +"; ";
			}
			
			if (result.length() > 0) {
				result = result.substring(0, result.length()-2);
			}
			
			return result;
			
		}

		return "";
	}
	
	public String getRange(){
		return  element.getStartPosition() + "-" + element.getStartPosition() + element.getLength();
	}
	
	public int getStartRange() {
		return element.getStartPosition();
	}
	
	public int getEndRange() {
		return element.getStartPosition() + element.getLength();
	}
	
	
	
	public String getViewCountAsString() {
		return String.valueOf(ApplicationController.getInstance().getViewCountForElement(element));
	}

	public int getViewCount() {
		return ApplicationController.getInstance().getViewCountForElement(element);
	}

	
	
}
