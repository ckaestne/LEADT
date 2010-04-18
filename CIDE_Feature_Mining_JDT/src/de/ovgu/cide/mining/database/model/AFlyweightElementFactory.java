/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.8 $
 */

package de.ovgu.cide.mining.database.model;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;


import cide.gast.IASTNode;
import de.ovgu.cide.language.jdt.ASTBridge;
import de.ovgu.cide.language.jdt.UnifiedASTNode;

/**
 * CHANGED BY A. DREILING
 * 
 */
public class AFlyweightElementFactory
{
	private  Hashtable<String, AIElement> javaElements = null;
//	private static AFlyweightElementFactory factory = null;
	
	public AFlyweightElementFactory() {
		javaElements = new Hashtable<String, AIElement>();
			
	}
	
//	public static AFlyweightElementFactory getInstance() {
//    	if (factory == null)
//    		factory = new AFlyweightElementFactory();
//    	return factory;
//    }
	
	public  AIElement getElement(IBinding binding) {
		return (AIElement) javaElements.get(binding.getKey());
	}
	
	public  AIElement getElement(String key) {
		return (AIElement) javaElements.get(key);
	}
	
	public  AIElement getElement(ASTNode node) {
		UnifiedASTNode uniNode = (UnifiedASTNode)bridge(node);
		
		return (AIElement) javaElements.get(uniNode.getId());
	}

	/** 
	 * Returns a flyweight object representing a program element.
	 * @param pCategory The category of element.  Must be a value 
	 * declared in ICategories.
	 * @param pId The id for the element.  For example, a field Id for 
	 * ICategories.FIELD.
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/binaryComp.html#13.1">
	 * Java Specification, Third Section, 13.1 Section for the binary name convention</a> 
	 * @return A flyweight IElement.
	 * @exception AInternalProblemException if an invalid category is passed as parameter.
	 */
//	public AIElement createNonBindingElement( AICategories pCategory, int compUnitHash, ASTNode node) {
//		return createBindingElement(pCategory, null, compUnitHash, node);
//	}
	
	public AIElement createElement( AICategories pCategory, IBinding binding, int compUnitHash, ASTNode node)
	{
		AIElement lReturn = null;
		String bindingKey = null;
		String nodeId = null;
		
		if (binding != null) {
			bindingKey = binding.getKey();
			lReturn = (AIElement) javaElements.get(bindingKey);
			if (lReturn != null) {
				if (!lReturn.getCategory().equals(pCategory)) {
					lReturn.addSubcategory(pCategory);
				}
				return lReturn;
			}
				
		}
		
		UnifiedASTNode uniNode = (UnifiedASTNode)bridge(node);
		nodeId = uniNode.getId();
		lReturn = (AIElement) javaElements.get(nodeId);
		if (lReturn != null) {
			if (!lReturn.getCategory().equals(pCategory)) {
				lReturn.addSubcategory(pCategory);
			}
			return lReturn;
		}
			
		if( pCategory == AICategories.TYPE )
		{
			lReturn = new ATypeElement( uniNode, compUnitHash );
		}
		else if( pCategory == AICategories.FIELD )
		{
			lReturn = new AFieldElement( uniNode, compUnitHash);
		}
		else if( pCategory == AICategories.METHOD )
		{
			lReturn = new AMethodElement( uniNode, compUnitHash);
		}
		else if( pCategory == AICategories.LOCAL_VARIABLE )
		{
			lReturn = new ALocalVariableElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.COMPILATION_UNIT )
		{
			lReturn = new ACompilationUnitElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.IMPORT)
		{
			lReturn = new AImportElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.TYPE_ACCESS)
		{
			lReturn = new ATypeAccessElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.FIELD_ACCESS)
		{
			lReturn = new AFieldAccessElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.LOCAL_VARIABLE_ACCESS)
		{
			lReturn = new ALocalVariableAccessElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.PARAMETER_ACCESS)
		{
			lReturn = new AParameterAccessElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.METHOD_ACCESS)
		{
			lReturn = new AMethodAccessElement( uniNode,  compUnitHash);
		}
		else if( pCategory == AICategories.OUT_OF_CONTEXT)
		{
			lReturn = new AOutOfContextElement( uniNode,  compUnitHash);
		}
		else
		{
			throw new AInternalProblemException( "Invalid element category: " + pCategory );
		}	
		
		if (bindingKey != null)
			javaElements.put( bindingKey, lReturn );
		else
			javaElements.put(nodeId, lReturn);
	
		return lReturn;
	}
	
//	private String getPackageKey(IBinding binding) {
//		
//		ITypeBinding typeBinding = null;
//		
//		if (binding instanceof ITypeBinding) {
//			typeBinding = (ITypeBinding)binding;
//		}
//			
//		if (binding instanceof IMethodBinding) {
//			typeBinding = ((IMethodBinding) binding).getDeclaringClass();
//		}
//		
//		if (binding instanceof IVariableBinding) {
//			typeBinding = ((IVariableBinding) binding).getDeclaringClass();
//		}
//		
//		if (typeBinding == null)
//			return "";
//		
//		IPackageBinding packBinding = typeBinding.getPackage();
//		
//		if (packBinding == null)
//			return "";
//		
//		return packBinding.getKey();
//		
//	}
	
	private IASTNode bridge(ASTNode node) {
		return ASTBridge.bridge(node);
	}
}

