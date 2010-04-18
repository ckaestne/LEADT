/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeDeclarationMatch;

/**
 * Converts Java elements to unique ID strings and vice-versa. This object keeps
 * a cache of recently converted types, so it pays to keep a Converter object
 * around for a while.
 */
public class Converter
{
	private static final String CONSTRUCTOR = "<init>";
	// A cache of recently converted types.
	private Map<String, IType> aCache = new HashMap<String, IType>(); 
	private IJavaSearchScope aScope; // The scope for the project elements.

	/**
	 * A requestor obtaining the elements matching a type.
	 */
	class TypeSearchRequestor extends SearchRequestor
	{
		private Set<IType> aType = new HashSet<IType>();

		/**
		 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
		 * @throws CoreException
		 *             a core exception
		 * @param pMatch
		 *            the found match
		 */
		@Override
		public void acceptSearchMatch(SearchMatch pMatch) throws CoreException
		{
			if (pMatch instanceof TypeDeclarationMatch)
			{
				aType.add((IType) pMatch.getElement());
			}
		}

		/**
		 * @return The type found, if a single type is found. Throws an
		 *         exception if more than one types are found.
		 * @throws ConversionException
		 *             If more than one type are found.
		 */
		public IType getType() throws ConversionException
		{
			if (aType.size() != 1)
			{
				throw new ConversionException("Could not match type");
			}
			Object lType = aType.iterator().next();
			if (!(lType instanceof IType))
			{
				throw new ConversionException("Could not match type");
			}
			return (IType) lType;
		}
	}

	/**
	 * Creates a converter which searches for elements in the entire workspace.
	 */
	public Converter()
	{
		aScope = SearchEngine.createWorkspaceScope();
	}

	/**
	 * Sets the project where the Converter will look for Java elements.
	 * 
	 * @param pProject
	 *            The project to specify.
	 */
	public void setJavaProject(IJavaProject pProject)
	{
		IJavaElement[] lProject = new IJavaElement[1];
		lProject[0] = pProject;
		aScope = SearchEngine.createJavaSearchScope(lProject, true);
	}

	/**
	 * Returns the type in the Java model associated with this string.
	 * 
	 * @param pType
	 *            The ID string representing a type.
	 * @return The type object represented by pType
	 * @throws ConversionException
	 *             If the type cannot be converted.
	 */
	public IType toType(String pType) throws ConversionException
	{
		IType lReturn = aCache.get(pType);
		if (lReturn != null)
		{
			return lReturn;
		}

		SearchPattern lPattern = SearchPattern.createPattern(pType.replace('$',
				'.'), IJavaSearchConstants.TYPE,
				IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
		TypeSearchRequestor lRequestor = new TypeSearchRequestor();

		try
		{
			SearchParticipant[] lParticipants = new SearchParticipant[1];
			lParticipants[0] = SearchEngine.getDefaultSearchParticipant();
			new SearchEngine().search(lPattern, lParticipants, aScope,
					lRequestor, null);
			lReturn = lRequestor.getType();
		}
		catch (CoreException lException)
		{
			throw new ConversionException(lException);
		}
		aCache.put(pType, lReturn);
		return lReturn;
	}

	/**
	 * Returns the field in the Java model associated with the parameter string.
	 * 
	 * @param pField
	 *            A string ID representing the field to fetch.
	 * @return A Field object corresponding to pField.
	 * @throws ConversionException
	 *             If the conversion cannot be made properly.
	 */
	public IField toField(String pField) throws ConversionException
	{
		int lIndex = pField.lastIndexOf('.');
		IType lType = toType(pField.substring(0, lIndex));
		IField lReturn = lType.getField(pField.substring(lIndex + 1));
		return lReturn;
	}

	/**
	 * Returns the method in the Java model associated with the parameter
	 * string.
	 * 
	 * @param pMethod
	 *            A string ID representing the method to fetch.
	 * @return A Method object corresponding to pMethod
	 * @throws ConversionException
	 *             If the conversion cannot be made properly.
	 */
	public IMethod toMethod(String pMethod) throws ConversionException
	{
		int lSignatureIndex = pMethod.indexOf('(');
		String lPreSignature = pMethod.substring(0, lSignatureIndex);
		int lIndex = lPreSignature.lastIndexOf('.');
		String lName = lPreSignature.substring(lIndex + 1);
		IType lDecl = toType(lPreSignature.substring(0, lIndex));
		IMethod lReturn = null;

		try
		{
			IMethod[] lMethods = lDecl.getMethods();
			Set<IMethod> lCandidates = new HashSet<IMethod>();
			for (IMethod element : lMethods)
			{
				if (element.isConstructor() && lName.equals(CONSTRUCTOR))
				{
					lCandidates.add(element);
				}
				else
				{
					if (element.getElementName().equals(lName))
					{
						lCandidates.add(element);
					}
				}
			}
			if (lCandidates.size() == 1)
			{
				lReturn = lCandidates.iterator().next();
			}
			else
			{
				for (IMethod lNext : lCandidates)
				{
					if (toIDString(lNext).equals(pMethod))
					{
						lReturn = lNext;
						break;
					}
				}
			}
		}
		catch (JavaModelException lException)
		{
			throw new ConversionException(lException);
		}
		if (lReturn == null)
		{
			throw new ConversionException(pMethod);
		}
		return lReturn;
	}

	/**
	 * Converts pType into a fully qualified name using $S$ as a separator for
	 * inner classes.
	 * 
	 * @param pType
	 *            The type to convert.
	 * @return A String representing the type.
	 */
	public static String toIDString(IType pType)
	{
		return pType.getFullyQualifiedName('$');
	}

	/**
	 * Converts pField into a unique descriptor.
	 * 
	 * @param pField
	 *            The field to convert
	 * @return A String representing the field.
	 */
	public static String toIDString(IField pField)
	{
		String lReturn = toIDString(pField.getDeclaringType());
		lReturn += "." + pField.getElementName();
		return lReturn;
	}

	/**
	 * Converts pMethod into a unique descriptor.
	 * 
	 * @param pMethod
	 *            The method to convert.
	 * @return A String representing the method.
	 * @throws ConversionException
	 *             If we cannot convert the method properly.
	 */
	public static String toIDString(IMethod pMethod) throws ConversionException
	{
		String lReturn = toIDString(pMethod.getDeclaringType()) + ".";

		if (pMethod.getElementName().equals(
				pMethod.getDeclaringType().getElementName()))
		{
			lReturn += CONSTRUCTOR + "(";
		}
		else
		{
			lReturn += pMethod.getElementName() + "(";
		}

		String[] lParams = pMethod.getParameterTypes();

		if (lParams.length > 0)
		{
			for (int lI = 0; lI < lParams.length - 1; lI++)
			{
				lReturn += expandSignatureType(pMethod, lParams[lI]) + ",";
			}
			lReturn += expandSignatureType(pMethod, lParams[lParams.length - 1]);
		}

		lReturn += ")";

		return lReturn;
	}

	private static String expandSignatureType(IMethod pMethod, String pType)
			throws ConversionException
	{
		String lReturn = "";
		String lRawType = Signature.getElementType(pType);
		if (Signature.getTypeSignatureKind(lRawType) == Signature.BASE_TYPE_SIGNATURE)
		{
			lReturn = pType;
		}
		else if (Signature.getTypeSignatureKind(lRawType) == Signature.CLASS_TYPE_SIGNATURE)
		{
			if (lRawType.charAt(0) == Signature.C_UNRESOLVED)
			{
				try
				{
					String[][] lTypes = pMethod.getDeclaringType().resolveType(
							lRawType.substring(1, lRawType.length() - 1));
					if (lTypes.length != 1)
					{
						throw new ConversionException("Cannot resolve type "
								+ pType);
					}
					else
					{
						lReturn = lTypes[0][0] + "." + lTypes[0][1];
					}
				}
				catch (JavaModelException lException)
				{
					throw new ConversionException(lException);
				}
			}
			else if (lRawType.charAt(0) == Signature.C_RESOLVED)
			{
				lReturn = lRawType.substring(1, lRawType.length() - 1);
			}
			else
			{
				throw new ConversionException("Unknown type format " + pType);
			}
			for (int lI = 0; lI < Signature.getArrayCount(pType); lI++)
			{
				lReturn = "[" + lReturn;
			}
		}
		else
		{
			throw new ConversionException("Cannot convert type parameter "
					+ pType);
		}
		return lReturn;
	}

}
