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

/** Represents a field element in the Java program model.
 */
public class ALocalVariableElement extends AAbstractElement
{
	/** 
	 * Creates a local variable element.  This constructor should not
	 * be used directly.  FieldElements should be obtained
	 * through the FlyweightElementFactory.getElement method.
	 * @param pId the Id representing the field, i.e., the fully
	 * qualified name of the declaring class followed by the name of the
	 * field, in dot notation.
	 */
	int paramIndex;
	protected ALocalVariableElement(  UnifiedASTNode node, int compUnitHash )
	{
		super(node, compUnitHash);
		paramIndex = -1;
	}
	
	public boolean isParameter() {
		return paramIndex >= 0;
	}
	
	public int getParamIndex() {
		return paramIndex; 
	}
	
	public void setParamIndex(int paramIndex) {
		this.paramIndex = paramIndex; 
	}
	
	
	/** 
	 * Returns the category of this element, i.e., a field.
	 * @return ICategories.FIELD.
	 */
	public AICategories getCategory()
	{
		return AICategories.LOCAL_VARIABLE;
	}
	
	/** 
	 * Determines equality.
	 * @param pObject the object to compare to this object.
	 * @return true if pObject is a Field element with the  same
	 * id as this element.
	 */
	public boolean equals( Object pObject )
	{
		if( !(pObject instanceof ALocalVariableElement))
			return false;
		else
			return getId().equals(((ALocalVariableElement)pObject).getId() );
	}
	
	/** 
	 * @return a hash code for this object.
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}
	
		
}

