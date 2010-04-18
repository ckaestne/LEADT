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

import java.util.Set;

import de.ovgu.cide.language.jdt.UnifiedASTNode;

/** 
 * A program element in the Java model.
 */
public interface AIElement
{
	/**
	 * @return The category for this element.
	 */
	public AICategories getCategory();
	
	/**
	 * @return The ID for this element.
	 */
	public String getId();
	
	/**
	 * @return The declaring class of the element.
	 * Null if the element is a top-level class (i.e., not
	 * an inner class.
	 */
	//public AClassElement getDeclaringClass();
	
	/** 
	 * @return The id of this element without the package.
	 */
	public String getShortName();
	
	
	/** 
	 * @return The id of this element without the package.
	 */
	public String getFullName();
	
	/**
	 * @return String Name of the package this element belongs to.
	 */
	//public String getPackageName();
	
//	public UnifiedASTNode getUnifiedASTNode();
	
	public int getCompelationUnitHash();
	
	public Set<AICategories> getSubCategories();
	
	public void addSubcategory(AICategories category);
	public int getStartPosition();
	public int getLength();
	
	
}

