///* JayFX - A Fact Extractor Plug-in for Eclipse
// * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * $Revision: 1.5 $
// */
//
//package de.ovgu.cide.mining.database.model;
//
//import de.ovgu.cide.language.jdt.UnifiedASTNode;
//
///** Represents a field element in the Java program model.
// */
//public class AFieldElement extends AElement
//{
//	/** 
//	 * Creates a field element.  This constructor should not
//	 * be used directly.  FieldElements should be obtained
//	 * through the FlyweightElementFactory.getElement method.
//	 * @param pId the Id representing the field, i.e., the fully
//	 * qualified name of the declaring class followed by the name of the
//	 * field, in dot notation.
//	 */
//	protected AFieldElement(  UnifiedASTNode node, int compUnitHash )
//	{
//		super(node, compUnitHash);
//	}
//	
//	/** 
//	 * Returns the category of this element, i.e., a field.
//	 * @return ICategories.FIELD.
//	 */
//	public AICategories getCategory()
//	{
//		return AICategories.FIELD;
//	}
//	
//	/** 
//	 * Determines equality.
//	 * @param pObject the object to compare to this object.
//	 * @return true if pObject is a Field element with the  same
//	 * id as this element.
//	 */
//	public boolean equals( Object pObject )
//	{
//		if( !(pObject instanceof AFieldElement))
//			return false;
//		else
//			return getId().equals(((AFieldElement)pObject).getId() );
//	}
//	
//	/** 
//	 * @return a hash code for this object.
//	 */
//	public int hashCode()
//	{
//		return getId().hashCode();
//	}
//	
//		
//}

