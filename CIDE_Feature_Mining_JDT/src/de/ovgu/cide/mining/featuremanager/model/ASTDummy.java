package de.ovgu.cide.mining.featuremanager.model;

import cide.gast.IASTNode;

public class ASTDummy {

	private int start, length, hashCode;
	private String name, id;

	public ASTDummy(IASTNode node, int hashCode) {
		start = node.getStartPosition();
		length = node.getLength();
		name = node.getDisplayName();
		id = node.getId();
		this.hashCode = hashCode;
	}

	public int getEnd() {
		return start + length;
	}

	public int getStart() {
		return start;
	}

	public int getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public int getHashCode() {
		return hashCode;
	}

}
