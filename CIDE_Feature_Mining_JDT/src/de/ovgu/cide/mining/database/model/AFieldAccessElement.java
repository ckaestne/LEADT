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
//
//public class AFieldAccessElement extends AElement
//{
//
//	protected AFieldAccessElement( UnifiedASTNode node, int compUnitHash)
//	{
//		super( node, compUnitHash);
//	}
//	
//	/** 
//	 * Returns the category of this element type, i.e., a method.
//	 * @return ICategories.METHOD
//	 */
//	public AICategories getCategory()
//	{
//		return AICategories.FIELD_ACCESS;
//	}
//	
//	/**
//	 * Equality for method elements is based on the equality
//	 * of their corresponding ids.
//	 * @param pObject the object to compare
//	 * to.
//	 * @return true if this object has the same 
//	 * id as pObject.
//	 * @see java.lang.Object#equals(Object)
//	 */
//	public boolean equals( Object pObject )
//	{
//		if( !(pObject instanceof AFieldAccessElement))
//			return false;
//		else
//			return getId().equals(((AFieldAccessElement)pObject).getId() );
//	}
//	
//	/** 
//	 * The hashcode is determined based on the id of the method.
//	 * @return The hashcode of the id String for this method.
//	 * @see java.lang.Object#hashCode()
//	 */
//	public int hashCode()
//	{
//		return getId().hashCode();
//	}
//	
//}

