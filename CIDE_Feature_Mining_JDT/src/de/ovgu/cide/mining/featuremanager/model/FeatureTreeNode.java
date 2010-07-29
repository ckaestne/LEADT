package de.ovgu.cide.mining.featuremanager.model;

import java.util.ArrayList;

import javax.swing.text.Element;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import de.ovgu.cide.CIDECorePlugin;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.language.jdt.UnifiedASTNode;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.recommendationengine.AElementViewCountManager;

public class FeatureTreeNode implements IAdaptable  {
	
	public static enum NODE_KIND {ROOT, FEATURE, COMPILATION_UNIT, ASTDUMMY, ELEMENT};

	private NODE_KIND kind;
	private Object data;
	private ArrayList<FeatureTreeNode> children;
	private FeatureTreeNode parent;
	private int startRange, endRange;
	private int viewCount;
	private int recommendCount;

	
	private int addCount;
	

	public FeatureTreeNode(NODE_KIND kind, Object data, int addCount) {
			
		this.kind = kind;
		this.data = data;
		children = new ArrayList<FeatureTreeNode>();
		viewCount = 0;		
		recommendCount = 0;

		this.addCount = addCount;
		
		switch (kind) {
			case ASTDUMMY:
			ASTDummy dummy = (ASTDummy)data;
			startRange = dummy.getStart();
			endRange = startRange + dummy.getLength();
			break;
		case ELEMENT:
			AElement el = (AElement)data;
			//UnifiedASTNode uniNode = el.getUnifiedASTNode();
			startRange = el.getStartPosition();
			endRange = startRange + el.getLength();
			break;
		default:
			startRange = endRange = 0;
		
		}
		
		
	}
	
	
	public void setParent(FeatureTreeNode parent) {
		this.parent = parent;
	}
	
	
	public FeatureTreeNode getNodeParent() {
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

	public void addChild(FeatureTreeNode child) {
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild(FeatureTreeNode child) {
		children.remove(child);
		child.setParent(null);
	}
	
	public FeatureTreeNode[] getChildren() {
		return (FeatureTreeNode [])children.toArray(new FeatureTreeNode[children.size()]);
	}
	
	public int getChildrenCount() {
		return children.size();
	}
	
	public boolean hasChildren() {
		return children.size()>0;
	}
	
	public int getAddCount() {
		return addCount;
	}

	public String getAddCountString() {
		return String.valueOf(getAddCount());
	}

	
	public String getID() {
		switch (kind) {
		case FEATURE:
			return ((IFeature)data).getName();
		case COMPILATION_UNIT:
			return String.valueOf(((CUDummy)data).getHashCode());
		case ASTDUMMY:
			return ((ASTDummy)data).getId();
		case ELEMENT:
			return ((AElement)data).getId();
		}
		
		return "";
	}
	

	
	public String getDisplayName() {
		switch (kind) {
		case FEATURE:
			return ((IFeature)data).getName();
		case COMPILATION_UNIT:
			return ((CUDummy)data).getName();
		case ASTDUMMY:
			return ((ASTDummy)data).getName();
		case ELEMENT:
			return ((AElement)data).getShortName();

		}
		
		return "";
	}
	
	public String getType() {
		switch (kind) {
		case FEATURE:
			return "";
		case COMPILATION_UNIT:
			return "";
		case ASTDUMMY:
			return "";
		case ELEMENT:
			String type = ((AElement)data).getCategory().toString();
			
			for (AICategories cat : ((AElement)data).getSubCategories()) {
				type += ", " + cat.toString();
			}
			
			return type;

		}
		
		return "";
	}
	
	public String getRange() {
		switch (kind) {
		case FEATURE:
			return "";
		case COMPILATION_UNIT:
			return "";
		case ASTDUMMY:
			return getRangeString();
		case ELEMENT:
			return getRangeString();
		}
		return "";
	}
	
	public String getViewCountString() {
		return String.valueOf(getViewCount());
	}
	
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	
	public void setRecommendationCount(int rCount) {
		this.recommendCount = rCount;
	}
	
	
	public int getViewCount() {
		switch (kind) {
		case FEATURE:
			return viewCount;
		case COMPILATION_UNIT:
			return viewCount;
		case ASTDUMMY:
			return viewCount;
		case ELEMENT:
			return ApplicationController.getInstance().getViewCountForElement(((AElement)data));
		}
		return 0;
	}
	
	
	public int getRecommendationCount() {
		switch (kind) {
		case FEATURE:
			return ApplicationController.getInstance().getRecommendationsCount(getColor(this), -1, -1, -1);
		case COMPILATION_UNIT:
			return ApplicationController.getInstance().getRecommendationsCount(getColor(this), -1, -1, ((CUDummy)data).getHashCode());
		case ASTDUMMY:
			return ApplicationController.getInstance().getRecommendationsCount(getColor(this), startRange, endRange, ((ASTDummy)data).getHashCode());
		case ELEMENT:
			return ApplicationController.getInstance().getRecommendationsCount(getColor(this), ((AElement)data));
		}
		return 0;
	}
	
	public String getRecommendationCountString() {
		return String.valueOf(getRecommendationCount());
	}
	
	private String getRangeString(){
		return  startRange + "-" + endRange;
	}
	
	public int getStartRange() {
		return startRange;
	}
	
	public int getEndRange() {
		return endRange;
	}
	
	public static IFeature getColor(FeatureTreeNode node) {
		
		while (!node.getKind().equals(NODE_KIND.FEATURE)) {
			node = node.getNodeParent();
		}
		
		return (IFeature)node.getDataObject();
	}

	
}
