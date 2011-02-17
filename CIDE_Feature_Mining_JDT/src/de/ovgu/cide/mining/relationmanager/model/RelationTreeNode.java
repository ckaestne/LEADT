package de.ovgu.cide.mining.relationmanager.model;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;

import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;

public class RelationTreeNode implements IAdaptable {

	public static enum NODE_KIND {
		ROOT, FOLDER, ELEMENT
	};

	private NODE_KIND kind;
	private Object data;
	private ArrayList<RelationTreeNode> children;
	private RelationTreeNode parent;
	private int viewCount;
	private int startRange, endRange;

	public RelationTreeNode(NODE_KIND kind, Object data) {
		this.kind = kind;
		this.data = data;
		children = new ArrayList<RelationTreeNode>();
		viewCount = 0;

		if (kind == NODE_KIND.ELEMENT) {
			AElement el = (AElement) data;
			// UnifiedASTNode uniNode = el.getUnifiedASTNode();
			startRange = el.getStartPosition();
			endRange = startRange + el.getLength();
		} else {
			startRange = endRange = 0;

		}

	}

	public void setParent(RelationTreeNode parent) {
		this.parent = parent;
	}

	public RelationTreeNode getParent() {
		return parent;
	}

	// inherited...
	public Object getAdapter(Class key) {
		return null;
	}

	public NODE_KIND getKind() {
		return kind;
	}

	public Object getDataObject() {
		return data;
	}

	public void addChild(RelationTreeNode child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(RelationTreeNode child) {
		children.remove(child);
		child.setParent(null);
	}

	public RelationTreeNode[] getChildren() {
		return (RelationTreeNode[]) children
				.toArray(new RelationTreeNode[children.size()]);
	}

	public int getChildrenCount() {
		return children.size();
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public String getDisplayName() {
		switch (kind) {
		case FOLDER:
			return (String) data;
		case ELEMENT:
			return ((AElement) data).getShortName();

		}

		return "";
	}

	public String getRange() {
		switch (kind) {
		case FOLDER:
			return "";
		case ELEMENT:
			return getRangeString();
		}
		return "";
	}

	public String getViewCountString() {
		switch (kind) {
		case FOLDER:
			return String.valueOf(viewCount);
		case ELEMENT:
			return String.valueOf(ApplicationController.getInstance()
					.getViewCountForElement(((AElement) data)));
		}
		return "";
	}

	public int getViewCount() {
		switch (kind) {
		case FOLDER:
			return viewCount;
		case ELEMENT:
			return ApplicationController.getInstance().getViewCountForElement(
					((AElement) data));
		}
		return 0;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	private String getRangeString() {
		return startRange + "-" + endRange;
	}

	public int getStartRange() {
		return startRange;
	}

	public int getEndRange() {
		return endRange;
	}

}
