/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.5 $
 */

package de.ovgu.cide.mining.database.model;

import de.ovgu.cide.language.jdt.UnifiedASTNode;




/**
 * Represents a class program element.
 */
public class ATypeElement extends AAbstractElement
{
	/** Initialize a class element with its fully qualified name 
	 * Class elements should only be created by a FlyweightElementFactory.
	 * @param pId The fully qualified name of the class.
	 */
	protected ATypeElement(UnifiedASTNode node, int compUnitHash)
	{
		super(node, compUnitHash);
	}
	
	/** Returns the category of this element, which always a class.
	 * @return the keyword "class".
	 */
	public AICategories getCategory()
	{
		return AICategories.TYPE;
	}
	
	/**
	 * @param pObject The object to compare the class to.
	 * @return Whether pObject has the same ID as this element.
	 */
	public boolean equals( Object pObject )
	{
		if( !(pObject instanceof ATypeElement))
			return false;
		else
			return getId().equals(((ATypeElement)pObject).getId() );
	}
	
	/** 
	 * @return A hash code for this element.
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}
		
	
}

