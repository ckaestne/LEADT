/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.4 $
 */

package de.ovgu.cide.mining.database.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import de.ovgu.cide.language.jdt.UnifiedASTNode;

/**
 * class for the various program elements in the model.
 */
@Entity
public class AElement implements Serializable {
	private static final long serialVersionUID = 1L;

	// protected UnifiedASTNode node;
	@PrimaryKey
	private String id;
	private String displayName;
	private int start, length;
	private int cuHash;
	private Set<AICategories> subCategories;
	private AICategories category;
	private int paramIndex = -1;

	/**
	 * Builds an abstract element.
	 * 
	 * @param pId
	 *            The id uniquely identifying the element. This id consists in
	 *            the fully-qualified name of a class element, the field name
	 *            appended to the fully-qualified named of the declaring class
	 *            for fields, and the name and signature appended to the
	 *            fully-qualified name of the declaring class for methods.
	 */
	public AElement(String id, int start, int length, String displayName,
			int compUnitHash, AICategories category) {
		this.id = id;
		this.start = start;
		this.length = length;
		this.displayName = displayName;

		// this.node = node;
		this.cuHash = compUnitHash;
		this.category = category;
	}

	/**
	 * default constructor for BerkeleyDB only
	 */
	AElement() {
	}

	protected AElement(UnifiedASTNode node, int compUnitHash,
			AICategories category) {
		this(node.getId(), node.getStartPosition(), node.getLength(),
				getDisplay(node.getDisplayName(), category), compUnitHash,
				category);
	}

	private static String getDisplay(String name, AICategories cat) {
		return name + " - " + cat;
	}

	public void addSubcategory(AICategories category) {
		if (subCategories == null)
			subCategories = new HashSet<AICategories>();

		subCategories.add(category);
	}

	public Set<AICategories> getSubCategories() {
		if (subCategories == null)
			subCategories = new HashSet<AICategories>();

		return subCategories;
	}

	/**
	 * This method must be redeclared here for compatibility with the IElement
	 * interface. Returns the category of the element within the general model.
	 * 
	 * @return An int representing the category of the element.
	 * @see AElement.ubc.cs.javadb.model.IElement#getCategory()
	 */
	public AICategories getCategory() {
		return category;
	}

	/**
	 * This method must be redeclared here for compatibility with the IElement
	 * interface. Returns the unique (fully qualified) name of the element.
	 * 
	 * @return A String representing the fully qualified name of the element.
	 * @see AElement.ubc.cs.javadb.model.IElement#getId()
	 */
	public String getId() {
		return id;
	}

	// public UnifiedASTNode getUnifiedASTNode() {
	// return node;
	// }

	// /**
	// * @return The name of the package in which this class is defined.
	// */
	// public String getPackageName() {
	// return packageName;
	//
	// }

	/**
	 * Returns a String representation of the element.
	 * 
	 * @return The element's ID.
	 */
	public String toString() {
		return getId();
	}

	/**
	 * @return The id of this element without the package.
	 */
	public String getShortName() {
		return displayName;
	}

	public String getFullName() {
		return displayName + " (" + getCategory() + ")";
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getCompelationUnitHash() {
		return cuHash;
	}

	public int getStartPosition() {
		return start;
	}

	public int getLength() {
		return length;
	}

	/**
	 * Determines equality.
	 * 
	 * @param pObject
	 *            the object to compare to this object.
	 * @return true if pObject is a Field element with the same id as this
	 *         element.
	 */
	public boolean equals(Object pObject) {
		if (pObject instanceof AElement) {
			AElement that = (AElement) pObject;
			return this.category == that.category
					&& this.getId().equals(that.getId());
		} else
			return false;
	}

	/**
	 * @return a hash code for this object.
	 */
	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * only for ALocalVariableElement
	 */
	public boolean isParameter() {
		return paramIndex >= 0;
	}

	/**
	 * only for ALocalVariableElement
	 */
	public int getParamIndex() {
		return paramIndex;
	}

	/**
	 * only for ALocalVariableElement
	 */
	public void setParamIndex(int paramIndex) {
		this.paramIndex = paramIndex;
	}
}
