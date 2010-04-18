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

import java.util.HashSet;
import java.util.Set;

import de.ovgu.cide.language.jdt.UnifiedASTNode;



/**
 * Abtract class for the various program elements in the
 * model.  
 */ 
public abstract class AAbstractElement implements AIElement
{
	//protected UnifiedASTNode node;
	private String id;
	private String displayName;
	private int start, length;
	private int cuHash;
	private Set<AICategories> subCategories;
	
	/**
	 * Builds an abstract element. 
	 * @param pId The id uniquely identifying the element.
	 * This id consists in the fully-qualified name of a class element,
	 * the field name appended to the fully-qualified named of the 
	 * declaring class for fields, and the name and signature appended
	 * to the fully-qualified name of the declaring class for methods.
	 */
	protected AAbstractElement( UnifiedASTNode node,  int compUnitHash)
	{
		this.id = node.getId();
		this.start = node.getStartPosition();
		this.length = node.getLength();
		this.displayName = node.getDisplayName();
		
		//this.node = node;
		this.cuHash = compUnitHash;
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
	 * This method must be redeclared here for compatibility
	 * with the IElement interface.  Returns the category of the element 
	 * within the general model.
	 * @return An int representing the category of the element.
	 * @see AIElement.ubc.cs.javadb.model.IElement#getCategory()
	 */
	public abstract AICategories getCategory();
	
	/**
	 * This method must be redeclared here for compatibility
	 * with the IElement interface.  Returns the unique (fully qualified)
	 * name of the element.
	 * @return A String representing the fully qualified name of the
	 * element.
	 * @see AIElement.ubc.cs.javadb.model.IElement#getId()
	 */
	public String getId()
	{
		return id;
	}
	

//	public UnifiedASTNode getUnifiedASTNode() {
//		return node;
//	}

	
//	/** 
//	 * @return The name of the package in which this class is defined.
//	 */
//	public String getPackageName()	{
//		return packageName;
//	
//	}
	
	/** 
	 * Returns a String representation of the element.
	 * @return The element's ID.
	 */
	public String toString()
	{
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
		
	
	public int getCompelationUnitHash(){
		return cuHash;
	}
	
	public int getStartPosition() {
		return start;
	}

	public int getLength() {
		return length;
	}
	
	
	
}

