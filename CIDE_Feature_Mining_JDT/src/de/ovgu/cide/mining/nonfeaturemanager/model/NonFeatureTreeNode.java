package de.ovgu.cide.mining.nonfeaturemanager.model;

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
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.recommendationengine.AElementViewCountManager;
import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode;
import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode.NODE_KIND;

public class NonFeatureTreeNode implements IAdaptable  {
	
	public static enum NODE_KIND {ROOT, FEATURE, COMPILATION_UNIT, ELEMENT};

	private NODE_KIND kind;
	private Object data;
	private ArrayList<NonFeatureTreeNode> children;
	private NonFeatureTreeNode parent;
	private int startRange, endRange;
	private int viewCount;
	private int elementsCount;

	
	private int addCount;
	

	public NonFeatureTreeNode(NODE_KIND kind, Object data, int addCount) {
			
		this.kind = kind;
		this.data = data;
		children = new ArrayList<NonFeatureTreeNode>();
		viewCount = 0;		
		elementsCount = 0;

		this.addCount = addCount;
		
		switch (kind) {
		case ELEMENT:
			AIElement el = (AIElement)data;
			//UnifiedASTNode uniNode = el.getUnifiedASTNode();
			startRange = el.getStartPosition();
			endRange = startRange + el.getLength();
			break;
		default:
			startRange = endRange = 0;
		
		}
		
		
	}
	
	
	public void setParent(NonFeatureTreeNode parent) {
		this.parent = parent;
	}
	
	
	public NonFeatureTreeNode getNodeParent() {
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

	public void addChild(NonFeatureTreeNode child) {
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild(NonFeatureTreeNode child) {
		children.remove(child);
		child.setParent(null);
	}
	
	public NonFeatureTreeNode[] getChildren() {
		return (NonFeatureTreeNode [])children.toArray(new NonFeatureTreeNode[children.size()]);
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
		case ELEMENT:
			return ((AIElement)data).getId();
		}
		
		return "";
	}
	

	
	public String getDisplayName() {
		switch (kind) {
		case FEATURE:
			return ((IFeature)data).getName();
		case COMPILATION_UNIT:
			return ((CUDummy)data).getName();
		case ELEMENT:
			return ((AIElement)data).getShortName();

		}
		
		return "";
	}
	
	public String getType() {
		switch (kind) {
		case FEATURE:
			return "";
		case COMPILATION_UNIT:
			return "";
		case ELEMENT:
			String type = ((AIElement)data).getCategory().toString();
			
			for (AICategories cat : ((AIElement)data).getSubCategories()) {
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
	
	public void setElementsCount(int elCount) {
		this.elementsCount = elCount;
	}
	
	public int getElementsCount() {
		switch (kind) {
		case FEATURE:
			return elementsCount;
		case COMPILATION_UNIT:
			return elementsCount;
		case ELEMENT:
			return 1;
		}
		return 0;
	}
	
	public String getElementsCountString() {
		return String.valueOf(getElementsCount());
	}
	
	public int getViewCount() {
		switch (kind) {
		case FEATURE:
			return viewCount;
		case COMPILATION_UNIT:
			return viewCount;
		case ELEMENT:
			return ApplicationController.getInstance().getViewCountForElement(((AIElement)data));
		}
		return 0;
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
	

	public static IFeature getColor(NonFeatureTreeNode node) {
		
		while (!node.getKind().equals(NODE_KIND.FEATURE)) {
			node = node.getNodeParent();
		}
		
		return (IFeature)node.getDataObject();
	}
}
