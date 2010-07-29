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
public class AFlyweightElementFactory {
	private Hashtable<String, AElement> javaElements = null;

	// private static AFlyweightElementFactory factory = null;

	public AFlyweightElementFactory() {
		javaElements = new Hashtable<String, AElement>();

	}

	// public static AFlyweightElementFactory getInstance() {
	// if (factory == null)
	// factory = new AFlyweightElementFactory();
	// return factory;
	// }

	public AElement getElement(IBinding binding) {
		return (AElement) javaElements.get(binding.getKey());
	}

	public AElement getElement(String key) {
		return (AElement) javaElements.get(key);
	}

	public AElement getElement(ASTNode node) {
		UnifiedASTNode uniNode = (UnifiedASTNode) bridge(node);

		return (AElement) javaElements.get(uniNode.getId());
	}

	/**
	 * Returns a flyweight object representing a program element.
	 * 
	 * @param pCategory
	 *            The category of element. Must be a value declared in
	 *            ICategories.
	 * @param pId
	 *            The id for the element. For example, a field Id for
	 *            ICategories.FIELD.
	 * @see <a
	 *      href="http://java.sun.com/docs/books/jls/third_edition/html/binaryComp.html#13.1">
	 *      Java Specification, Third Section, 13.1 Section for the binary name
	 *      convention</a>
	 * @return A flyweight IElement.
	 * @exception AInternalProblemException
	 *                if an invalid category is passed as parameter.
	 */
	// public AIElement createNonBindingElement( AICategories pCategory, int
	// compUnitHash, ASTNode node) {
	// return createBindingElement(pCategory, null, compUnitHash, node);
	// }

	public AElement createElement(AICategories pCategory, IBinding binding,
			int compUnitHash, ASTNode node) {
		AElement lReturn = null;
		String bindingKey = null;
		String nodeId = null;

		if (binding != null) {
			bindingKey = binding.getKey();
			lReturn = (AElement) javaElements.get(bindingKey);
			if (lReturn != null) {
				if (!lReturn.getCategory().equals(pCategory)) {
					lReturn.addSubcategory(pCategory);
				}
				return lReturn;
			}

		}

		UnifiedASTNode uniNode = (UnifiedASTNode) bridge(node);
		nodeId = uniNode.getId();
		lReturn = (AElement) javaElements.get(nodeId);
		if (lReturn != null) {
			if (!lReturn.getCategory().equals(pCategory)) {
				lReturn.addSubcategory(pCategory);
			}
			return lReturn;
		}

		lReturn = new AElement(uniNode, compUnitHash, pCategory);

		if (bindingKey != null)
			javaElements.put(bindingKey, lReturn);
		else
			javaElements.put(nodeId, lReturn);

		return lReturn;
	}

	// private String getPackageKey(IBinding binding) {
	//
	// ITypeBinding typeBinding = null;
	//
	// if (binding instanceof ITypeBinding) {
	// typeBinding = (ITypeBinding)binding;
	// }
	//
	// if (binding instanceof IMethodBinding) {
	// typeBinding = ((IMethodBinding) binding).getDeclaringClass();
	// }
	//
	// if (binding instanceof IVariableBinding) {
	// typeBinding = ((IVariableBinding) binding).getDeclaringClass();
	// }
	//
	// if (typeBinding == null)
	// return "";
	//
	// IPackageBinding packBinding = typeBinding.getPackage();
	//
	// if (packBinding == null)
	// return "";
	//
	// return packBinding.getKey();
	//
	// }

	private IASTNode bridge(ASTNode node) {
		return ASTBridge.bridge(node);
	}
}
