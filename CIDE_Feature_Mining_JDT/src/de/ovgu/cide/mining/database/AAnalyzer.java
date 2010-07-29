/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.9 $
 */

package de.ovgu.cide.mining.database;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.ARelationKind;

/**
 * Query engine for a program database
 */
public class AAnalyzer {
	private AbstractProgramDatabase aDB;

	/**
	 * @param pDB
	 *            The program database
	 * @pre pDB != null
	 */
	public AAnalyzer(AbstractProgramDatabase pDB) {
		assert (pDB != null);
		aDB = pDB;
	}

	/**
	 * Returns all the elements matching a defined relations
	 * 
	 * @param pElement
	 * @param pRelation
	 * @return
	 */
	// public Set<AIElement> getRange( AIElement pElement, Relation pRelation )
	// {
	// Set<AIElement> lReturn = new HashSet<AIElement>();
	// if( (pRelation == Relation.DECLARES) || (pRelation ==
	// Relation.T_DECLARES) ||
	// (pRelation == Relation.ACCESSES ) || (pRelation == Relation.T_ACCESSES )
	// ||
	// (pRelation == Relation.EXTENDS_CLASS ) || (pRelation ==
	// Relation.T_EXTENDS_CLASS ) ||
	// (pRelation == Relation.EXTENDS_INTERFACES) || (pRelation ==
	// Relation.T_EXTENDS_INTERFACES ) ||
	// (pRelation == Relation.OVERRIDES) || (pRelation == Relation.T_OVERRIDES )
	// ||
	// (pRelation == Relation.CREATES) || (pRelation == Relation.T_CREATES) ||
	// (pRelation == Relation.CHECKS) || (pRelation == Relation.T_CHECKS) ||
	// (pRelation == Relation.OF_TYPE) || (pRelation == Relation.T_OF_TYPE) ||
	// (pRelation == Relation.HAS_RETURN_TYPE) || (pRelation ==
	// Relation.T_HAS_RETURN_TYPE) ||
	//
	// (pRelation == Relation.IMPLEMENTS_INTERFACE) || (pRelation ==
	// Relation.T_IMPLEMENTS_INTERFACE ))
	// {
	// lReturn.addAll( aDB.getRange( pElement, pRelation ));
	// }
	// else if( pRelation == Relation.TRANS_EXTENDS )
	// {
	// lReturn = getTransitivelyExtends( pElement );
	// }
	// else if( pRelation == Relation.T_TRANS_EXTENDS )
	// {
	// lReturn = getTTransitivelyExtends( pElement );
	// }
	// else if( pRelation == Relation.TRANS_IMPLEMENTS )
	// {
	// lReturn = getTransitivelyImplements( pElement );
	// }
	// else if( pRelation == Relation.T_TRANS_IMPLEMENTS )
	// {
	// lReturn = getTTransitivelyImplements( pElement );
	// }
	// else if( pRelation == Relation.CALLS )
	// {
	// lReturn = getCalls( pElement );
	// }
	// else if( pRelation == Relation.T_CALLS )
	// {
	// lReturn = getTCalls( pElement );
	// }
	// else
	// {
	// throw new RelationNotSupportedException( pRelation.getName() );
	// }
	// return lReturn;
	// }

	public Set<AElement> getRange(AElement pElement, ARelationKind pRelation) {
		Set<AElement> lReturn = new HashSet<AElement>();
		if ((pRelation == ARelationKind.DECLARES_FIELD)
				|| (pRelation == ARelationKind.T_DECLARES_FIELD)
				|| (pRelation == ARelationKind.DECLARES_IMPORT)
				|| (pRelation == ARelationKind.T_DECLARES_IMPORT)
				|| (pRelation == ARelationKind.DECLARES_LOCAL_VARIABLE)
				|| (pRelation == ARelationKind.T_DECLARES_LOCAL_VARIABLE)
				|| (pRelation == ARelationKind.DECLARES_METHOD)
				|| (pRelation == ARelationKind.T_DECLARES_METHOD)
				|| (pRelation == ARelationKind.DECLARES_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.T_DECLARES_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.DECLARES_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.T_DECLARES_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.DECLARES_FIELD_TRANSITIVE)
				|| (pRelation == ARelationKind.T_DECLARES_FIELD_TRANSITIVE)
				|| (pRelation == ARelationKind.DECLARES_LOCAL_VARIABLE_TRANSITIVE)
				|| (pRelation == ARelationKind.T_DECLARES_LOCAL_VARIABLE_TRANSITIVE)
				|| (pRelation == ARelationKind.IMPLEMENTS_TYPE)
				|| (pRelation == ARelationKind.T_IMPLEMENTS_TYPE)
				|| (pRelation == ARelationKind.EXTENDS_TYPE)
				|| (pRelation == ARelationKind.T_EXTENDS_TYPE)
				|| (pRelation == ARelationKind.IMPLEMENTS_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.T_IMPLEMENTS_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.EXTENDS_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.T_EXTENDS_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.OVERRIDES_METHOD)
				|| (pRelation == ARelationKind.T_OVERRIDES_METHOD)
				|| (pRelation == ARelationKind.OVERRIDES_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.T_OVERRIDES_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.IMPLEMENTS_METHOD)
				|| (pRelation == ARelationKind.T_IMPLEMENTS_METHOD)
				|| (pRelation == ARelationKind.IMPLEMENTS_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.T_IMPLEMENTS_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.DECLARES_PARAMETER)
				|| (pRelation == ARelationKind.T_DECLARES_PARAMETER)
				|| (pRelation == ARelationKind.ACCESS_TYPE)
				|| (pRelation == ARelationKind.T_ACCESS_TYPE)
				|| (pRelation == ARelationKind.ACCESS_FIELD)
				|| (pRelation == ARelationKind.T_ACCESS_FIELD)
				|| (pRelation == ARelationKind.ACCESS_LOCAL_VARIABLE)
				|| (pRelation == ARelationKind.T_ACCESS_LOCAL_VARIABLE)
				|| (pRelation == ARelationKind.ACCESS_METHOD)
				|| (pRelation == ARelationKind.T_ACCESS_METHOD)
				|| (pRelation == ARelationKind.ACCESS_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.T_ACCESS_TYPE_TRANSITIVE)
				|| (pRelation == ARelationKind.ACCESS_FIELD_TRANSITIVE)
				|| (pRelation == ARelationKind.T_ACCESS_FIELD_TRANSITIVE)
				|| (pRelation == ARelationKind.ACCESS_LOCAL_VARIABLE_TRANSITIVE)
				|| (pRelation == ARelationKind.T_ACCESS_LOCAL_VARIABLE_TRANSITIVE)
				|| (pRelation == ARelationKind.ACCESS_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.T_ACCESS_METHOD_TRANSITIVE)
				|| (pRelation == ARelationKind.BELONGS_TO)
				|| (pRelation == ARelationKind.T_BELONGS_TO)
				|| (pRelation == ARelationKind.REQUIRES)
				|| (pRelation == ARelationKind.T_REQUIRES)
				|| (pRelation == ARelationKind.DECLARES_FIELD_ACCESS)
				|| (pRelation == ARelationKind.T_DECLARES_FIELD_ACCESS)
				|| (pRelation == ARelationKind.DECLARES_LOCAL_VARIABLE_ACCESS)
				|| (pRelation == ARelationKind.T_DECLARES_LOCAL_VARIABLE_ACCESS)
				|| (pRelation == ARelationKind.DECLARES_TYPE_ACCESS)
				|| (pRelation == ARelationKind.T_DECLARES_TYPE_ACCESS)
				|| (pRelation == ARelationKind.DECLARES_METHOD_ACCESS)
				|| (pRelation == ARelationKind.T_DECLARES_METHOD_ACCESS)
				|| (pRelation == ARelationKind.DECLARES_TYPE)
				|| (pRelation == ARelationKind.T_DECLARES_TYPE)) {
			lReturn.addAll(aDB.getRange(pElement, pRelation));
		} else if (pRelation == ARelationKind.DECLARES) {
			lReturn = getDeclares(pElement);
		} else if (pRelation == ARelationKind.T_DECLARES) {
			lReturn = getT_Declares(pElement);
		} else if (pRelation == ARelationKind.ACCESSES) {
			lReturn = getAccess(pElement);
		} else if (pRelation == ARelationKind.T_ACCESS) {
			lReturn = getT_Access(pElement);
		} else if (pRelation == ARelationKind.REFERENCES) {
			lReturn = getReferences(pElement);
		} else if (pRelation == ARelationKind.T_REFERENCES) {
			lReturn = getT_References(pElement);
		}

		else {
			throw new RelationNotSupportedException(pRelation.getName());
		}
		return lReturn;
	}

	public Set<AElement> getT_Access(AElement pElement) {
		Set<AElement> declaresElements = new HashSet<AElement>();
		declaresElements.addAll(aDB.getRange(pElement,
				ARelationKind.BELONGS_TO, ARelationKind.IMPLEMENTS_METHOD,
				ARelationKind.OVERRIDES_METHOD));

		return declaresElements;
	}

	public Set<AElement> getAccess(AElement pElement) {
		Set<AElement> declaresElements = new HashSet<AElement>();
		declaresElements.addAll(aDB.getRange(pElement,
				ARelationKind.T_BELONGS_TO, ARelationKind.T_IMPLEMENTS_METHOD,
				ARelationKind.T_OVERRIDES_METHOD));

		return declaresElements;
	}

	public Set<AElement> getDeclares(AElement pElement) {
		Set<AElement> declaresElements = new HashSet<AElement>();
		declaresElements.addAll(aDB.getRange(pElement,
				ARelationKind.DECLARES_FIELD, ARelationKind.DECLARES_IMPORT,
				ARelationKind.DECLARES_LOCAL_VARIABLE,
				ARelationKind.DECLARES_METHOD, ARelationKind.DECLARES_TYPE,
				ARelationKind.DECLARES_FIELD_ACCESS,
				ARelationKind.DECLARES_LOCAL_VARIABLE_ACCESS,
				ARelationKind.DECLARES_TYPE_ACCESS,
				ARelationKind.DECLARES_METHOD_ACCESS));
		// declaresElements.addAll( aDB.getRange( pElement,
		// ARelation.DECLARES_PARAMETER));

		return declaresElements;
	}

	public Set<AElement> getT_Declares(AElement pElement) {
		Set<AElement> declaresElements = new HashSet<AElement>();
		declaresElements.addAll(aDB.getRange(pElement,
				ARelationKind.T_DECLARES_FIELD,
				ARelationKind.T_DECLARES_IMPORT,
				ARelationKind.T_DECLARES_LOCAL_VARIABLE,
				ARelationKind.T_DECLARES_METHOD, ARelationKind.T_DECLARES_TYPE,
				ARelationKind.T_DECLARES_FIELD_ACCESS,
				ARelationKind.T_DECLARES_LOCAL_VARIABLE_ACCESS,
				ARelationKind.T_DECLARES_TYPE_ACCESS,
				ARelationKind.T_DECLARES_METHOD_ACCESS));
		// declaresElements.addAll( aDB.getRange( pElement,
		// ARelation.T_DECLARES_PARAMETER));

		return declaresElements;
	}

	public Set<AElement> getReferences(AElement pElement) {
		Set<AElement> accessElements = new HashSet<AElement>();
		accessElements.addAll(aDB.getRange(pElement,
				ARelationKind.ACCESS_FIELD,
				ARelationKind.ACCESS_LOCAL_VARIABLE,
				ARelationKind.ACCESS_METHOD, ARelationKind.ACCESS_TYPE));

		Set<AElement> referenceElements = new HashSet<AElement>();

		for (AElement accessElement : accessElements) {
			referenceElements.addAll(aDB.getRange(accessElement,
					ARelationKind.BELONGS_TO));
		}

		return referenceElements;
	}

	public Set<AElement> getT_References(AElement pElement) {
		Set<AElement> accessElements = new HashSet<AElement>();
		accessElements.addAll(aDB
				.getRange(pElement, ARelationKind.T_BELONGS_TO));

		Set<AElement> referenceElements = new HashSet<AElement>();

		for (AElement accessElement : accessElements) {
			referenceElements
					.addAll(aDB.getRange(accessElement,
							ARelationKind.T_ACCESS_FIELD,
							ARelationKind.T_ACCESS_LOCAL_VARIABLE,
							ARelationKind.T_ACCESS_METHOD,
							ARelationKind.T_ACCESS_TYPE));
		}

		return referenceElements;
	}

	// WIEDER VERWENDEN!!!

	/**
	 * Does not do class-hierarchy analysis
	 * 
	 * @param pElement
	 * @return
	 */
	// private Set<AIElement> getCalls( AIElement pElement )
	// {
	// assert(pElement instanceof AMethodElement);
	//
	// Set<AIElement> lReturn = new HashSet<AIElement>();
	// // static stuff
	// lReturn.addAll( aDB.getRange( pElement, Relation.STATIC_CALLS ));
	//
	// // dynamic stuff
	// Set<AIElement> lVirtualCalls = aDB.getRange( pElement, Relation.CALLS );
	//
	// lReturn.addAll( lVirtualCalls );
	// for (AIElement lVirtualCallMember : lVirtualCalls)
	// {
	// // Note: a static method cannot be overriden
	// lReturn.addAll( aDB.getRange( lVirtualCallMember, Relation.T_OVERRIDES
	// ));
	// }
	//
	//
	// return lReturn;
	// }

	/**
	 * Does not do class-hierarchy analysis
	 * 
	 * @param pElement
	 *            IMethodElement
	 * @return Set of IMethodElement that calls the method
	 */
	// private Set<AIElement> getTCalls( AIElement pElement )
	// {
	// assert(pElement instanceof AMethodElement);
	//
	// Set<AIElement> lReturn = new HashSet<AIElement>();
	// // static stuff
	// lReturn.addAll( aDB.getRange( pElement, Relation.T_STATIC_CALLS ));
	//
	// // dynamic stuff
	// lReturn.addAll( aDB.getRange( pElement, Relation.T_CALLS ) );
	// Set<AIElement> lOverrides = aDB.getRange( pElement, Relation.OVERRIDES );
	// for (AIElement lOverridsElement : lOverrides)
	// {
	// // Note: a static method cannot be overriden
	// lReturn.addAll( aDB.getRange(lOverridsElement, Relation.T_CALLS ));
	// }
	// return lReturn;
	// }

	/**
	 * Returns all the classes extending class pElement directly or indirectly.
	 * 
	 * @param pElement
	 *            The domain class
	 * @return A set of IElement containing classes
	 */
	// private Set<AIElement> getTransitivelyExtends( AIElement pElement )
	// {
	// assert(pElement instanceof ATypeElement);
	//
	// Set<AIElement> lRange = aDB.getRange(pElement, Relation.EXTENDS_CLASS );
	// Set<AIElement> lReturn = new HashSet<AIElement>();
	//
	// while( lRange.size() > 0 )
	// {
	// assert( lRange.size() == 1 );
	//
	// AIElement lSuperClass = (AIElement)lRange.iterator().next();
	// lReturn.add( lSuperClass );
	// lRange = aDB.getRange( lSuperClass, Relation.EXTENDS_CLASS );
	// }
	// return lReturn;
	// }

	/**
	 * Returns all the classes extended by class pElement directly or
	 * indirectly.
	 * 
	 * @param pElement
	 *            The domain class
	 * @return A set of IElement containing classes
	 */
	// private Set<AIElement> getTTransitivelyExtends( AIElement pElement )
	// {
	// assert(pElement instanceof ATypeElement);
	//
	// Set<AIElement> lToProcess = aDB.getRange( pElement,
	// Relation.T_EXTENDS_CLASS );
	// Set<AIElement> lReturn = new HashSet<AIElement>();
	//
	// while( lToProcess.size() > 0 )
	// {
	// AIElement lNext = (AIElement)lToProcess.iterator().next();
	// lReturn.add( lNext );
	// lToProcess.remove( lNext );
	// lToProcess.addAll( aDB.getRange( lNext, Relation.T_EXTENDS_CLASS ));
	// }
	// lToProcess = aDB.getRange( pElement, Relation.T_EXTENDS_CLASS );
	//
	// return lReturn;
	// }

	/**
	 * Returns all the interfaces that pElement implements, directly or not.
	 * 
	 * @param pElement
	 *            The domain class
	 * @return A set of IElement containing classes
	 */
	// private Set<AIElement> getTransitivelyImplements( AIElement pElement )
	// {
	// assert(pElement instanceof ATypeElement);
	//
	// Set<AIElement> lReturn = new HashSet<AIElement>();
	//
	// // First get all directly implemented interfaces
	// Set<AIElement> lInterfaces = new HashSet<AIElement>();
	// lInterfaces.addAll( aDB.getRange( pElement, Relation.IMPLEMENTS_INTERFACE
	// ));
	//
	// //Then find the interfaces that are extended by one or more of the
	// interfaces we found
	// while( lInterfaces.size() > 0 )
	// {
	// AIElement lNext = (AIElement)lInterfaces.iterator().next();
	// lReturn.add( lNext );
	// lInterfaces.remove( lNext );
	// lInterfaces.addAll( aDB.getRange( lNext, Relation.EXTENDS_INTERFACES ));
	// }
	//
	// //Now find the class this class extends
	// Set<AIElement> lSuperclass = aDB.getRange( pElement,
	// Relation.EXTENDS_CLASS );
	//
	// // Obtain all it interfaces
	// for (AIElement lSuperclassElement : lSuperclass)
	// {
	// lReturn.addAll( getTransitivelyImplements( lSuperclassElement ));
	// }
	// // for( Iterator i = lSuperclass.iterator(); i.hasNext(); )
	// // {
	// // lReturn.addAll( getTransitivelyImplements( (IElement)i.next() ));
	// // }
	//
	// return lReturn;
	// }

	/**
	 * Returns all the classes that implement, directly or not, pElement.
	 * 
	 * @param pElement
	 *            The domain interface class
	 * @return A set of IElement containing classes
	 */
	// private Set<AIElement> getTTransitivelyImplements( AIElement pElement )
	// {
	// assert(pElement instanceof ATypeElement);
	//
	// Set<AIElement> lReturn = new HashSet<AIElement>();
	//
	// // First get all transitively extending interfaces
	// Set<AIElement> lInterfaces = new HashSet<AIElement>();
	// Set<AIElement> lToProcess = new HashSet<AIElement>();
	// lToProcess.add( pElement );
	// lInterfaces.add( pElement );
	//
	// // Retrieve all the interfaces that extend pElement
	// while( lToProcess.size() > 0 )
	// {
	// AIElement lNext = (AIElement)lToProcess.iterator().next();
	// lToProcess.remove( lNext );
	// lInterfaces.addAll( aDB.getRange( lNext, Relation.T_EXTENDS_INTERFACES
	// ));
	// lToProcess.addAll( aDB.getRange( lNext, Relation.T_EXTENDS_INTERFACES ));
	// }
	//
	// //Then for each interface find all implementing classes and their
	// subclasses
	// lToProcess = new HashSet<AIElement>();
	// for( Iterator i = lInterfaces.iterator(); i.hasNext(); )
	// {
	// AIElement lNext = (AIElement)i.next();
	// lToProcess.addAll( aDB.getRange( lNext, Relation.T_IMPLEMENTS_INTERFACE
	// ));
	// lReturn.addAll( aDB.getRange( lNext, Relation.T_IMPLEMENTS_INTERFACE ));
	// }
	//
	// for( Iterator i = lToProcess.iterator(); i.hasNext(); )
	// {
	// AIElement lNext = (AIElement)i.next();
	// lReturn.addAll( getTTransitivelyExtends( lNext ));
	//
	// }
	// return lReturn;
	// }

	/**
	 * Returns whether pElement is an interface type that exists in the DB.
	 */
	// public boolean isInterface( IElement pElement )
	// {
	// boolean lReturn = false;
	// if( pElement.getCategory() == ICategories.CLASS )
	// {
	// if( aDB.getModifiers( pElement ) >= 16384 )
	// {
	// lReturn = true;
	// }
	// }
	// return lReturn;
	// }

	/**
	 * Returns whether pElement is an non-implemented method, either in an
	 * interface or as an abstract method in an abstract class. Description of
	 * JavaDB
	 */
	// public boolean isAbstractMethod( IElement pElement )
	// {
	// boolean lReturn = false;
	// if( pElement.getCategory() == ICategories.METHOD )
	// {
	// if( aDB.getModifiers( pElement ) >= 16384 )
	// {
	// lReturn = true;
	// }
	// }
	// return lReturn;
	// }

	/**
	 * returns pelement included in the set if it is a class. pelement can be an
	 * interface, in which case all the implementing types will be included.
	 * 
	 * @param pElement
	 * @return
	 */
	// private Set getNonAbstractSubtypes( IElement pElement )
	// {
	// Set lReturn = new HashSet();
	// if( isInterface( pElement ))
	// {
	// Set lImplementors = getTTransitivelyImplements( pElement );
	// for( Iterator i = lImplementors.iterator(); i.hasNext(); )
	// {
	// IElement lNext = (IElement)i.next();
	// if( !Modifier.isAbstract( aDB.getModifiers( lNext )))
	// {
	// lReturn.add( lNext );
	// }
	// }
	// }
	// else
	// {
	// if( !Modifier.isAbstract( aDB.getModifiers( pElement )))
	// {
	// lReturn.add( pElement );
	// }
	// Set lSubclasses = getTTransitivelyExtends( pElement );
	// for( Iterator i = lSubclasses.iterator(); i.hasNext(); )
	// {
	// IElement lNext = (IElement)i.next();
	// if( !Modifier.isAbstract( aDB.getModifiers( lNext )))
	// {
	// lReturn.add( lNext );
	// }
	// }
	// }
	// return lReturn;
	// }

	/**
	 * Returns the method implementation that is executed if pMethod is called
	 * on an object of dynamic type pTarget.
	 * 
	 * @param pMethod
	 *            the static type of the method called
	 * @param pTarget
	 *            the dynamic type of the object
	 * @return The lowest non-abstract method in the class hierarchy. null if
	 *         none are found. (should not happen)
	 */
	// private IElement getMethodImplementation( MethodElement pMethod, IElement
	// pTarget )
	// {
	// IElement lTarget = pTarget;
	// IElement lReturn = matchMethod( pMethod, lTarget );
	// while( lReturn == null )
	// {
	// Set lSuperclass = aDB.getRange( lTarget, Relation.EXTENDS_CLASS );
	// if( lSuperclass.size() != 1 )
	// {
	// break;
	// }
	// lTarget = (IElement)lSuperclass.iterator().next();
	// lReturn = matchMethod( pMethod, lTarget );
	// }
	// return lReturn;
	// }

	/**
	 * Returns a non-static, non-constructor method that matches pMethod but
	 * that is declared in pClass. null if none are found. Small concession to
	 * correctness here for sake of efficiency: methods are matched only if they
	 * parameter types match exactly. pAbstract whether to look for abstract or
	 * non-abstract methods
	 */
	// private IElement matchMethod( MethodElement pMethod, IElement pClass,
	// boolean pAbstract )
	// {
	// IElement lReturn = null;
	// String lThisName = pMethod.getName();
	//
	// Set lElements = aDB.getRange( pClass, Relation.DECLARES );
	// for( Iterator i = lElements.iterator(); i.hasNext(); )
	// {
	// IElement lNext = (IElement)i.next();
	// if( lNext.getCategory() == ICategories.METHOD )
	// {
	// if( !((MethodElement)lNext).getName().startsWith("<init>") &&
	// !((MethodElement)lNext).getName().startsWith("<clinit>"))
	// {
	// if( !Modifier.isStatic( aDB.getModifiers( lNext )))
	// {
	// if( lThisName.equals( ((MethodElement)lNext).getName() ))
	// {
	// pMethod.getParameters().equals( ((MethodElement)lNext).getParameters() );
	// if( isAbstractMethod( lNext ) == pAbstract )
	// {
	// lReturn = lNext;
	// break;
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// return lReturn;
	// }

	/**
	 * Returns the method that this method directly overrides.
	 * 
	 * @return A non-null set.
	 */
	// private Set getOverrides( IElement pElement )
	// {
	// Set lReturn = new HashSet();
	//
	// if( !(pElement instanceof MethodElement ))
	// return lReturn;
	//
	// if( isAbstractMethod( pElement ))
	// return lReturn;
	//
	// // look in superclasses
	// Set lSuperclass = aDB.getRange( pElement.getDeclaringClass(),
	// Relation.EXTENDS_CLASS );
	// while( lSuperclass.size() != 0 )
	// {
	// IElement lNext = (IElement)lSuperclass.iterator().next();
	// IElement lMethod = matchMethod( (MethodElement)pElement, lNext, false );
	// if( lMethod != null )
	// {
	// lReturn.add( lMethod );
	// break;
	// }
	// lSuperclass = aDB.getRange( lNext, Relation.EXTENDS_CLASS );
	// }
	//
	// return lReturn;
	// }

	/**
	 * Returns the methods that this method is directly overriden by.
	 * 
	 * @return A non-null set.
	 */
	// private Set getTOverrides( IElement pElement )
	// {
	// Set lReturn = new HashSet();
	//
	// if( !(pElement instanceof MethodElement ))
	// return lReturn;
	//
	// if( isAbstractMethod( pElement ))
	// return lReturn;
	//
	// // look in subclasses
	// Set lSubclasses = new HashSet();
	// lSubclasses.addAll( aDB.getRange( pElement.getDeclaringClass(),
	// Relation.T_EXTENDS_CLASS ));
	// while( lSubclasses.size() != 0 )
	// {
	// IElement lNext = (IElement)lSubclasses.iterator().next();
	// lSubclasses.remove( lNext );
	// IElement lMethod = matchMethod( (MethodElement)pElement, lNext, false );
	// if( lMethod != null )
	// {
	// lReturn.add( lMethod );
	// }
	// else
	// {
	// lSubclasses.addAll( aDB.getRange( lNext, Relation.T_EXTENDS_CLASS ));
	// }
	// }
	//
	// return lReturn;
	//
	// }

	/**
	 * Returns the first abstract method found that matches this method. The
	 * methods are - are abstract, non-static - match pElement - and that are
	 * not implemented by any method that pElement overrides. Typically there
	 * should only be one method returned.
	 * 
	 * @param pElement
	 * @return
	 */
	// private Set getImplementsMethod( IElement pElement )
	// {
	// Set lReturn = new HashSet();
	//
	// if( !(pElement instanceof MethodElement ))
	// return lReturn;
	//
	// // First, search the superclasses for abstract methods
	// Set lSuperclass = aDB.getRange( pElement.getDeclaringClass(),
	// Relation.EXTENDS_CLASS );
	// while( lSuperclass.size() != 0 )
	// {
	// IElement lNext = (IElement)lSuperclass.iterator().next();
	// IElement lMethod = matchMethod( (MethodElement)pElement, lNext, true );
	// if( lMethod != null )
	// {
	// lReturn.add( lMethod );
	// break;
	// }
	// lSuperclass = aDB.getRange( lNext, Relation.EXTENDS_CLASS );
	// }
	//
	//
	// return lReturn;
	// }
}
