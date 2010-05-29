/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.6 $
 */

package de.ovgu.cide.mining.database.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Encapsulate various services related to relations.
 */
public enum ARelation
{	

//ALEX RELATIONS
	
	DECLARES_TYPE 						( Type.ID_DECLARES_TYPE, true),
	DECLARES_IMPORT 					( Type.ID_DECLARES_IMPORT, true),
	DECLARES_FIELD	 					( Type.ID_DECLARES_FIELD, true),
	DECLARES_METHOD						( Type.ID_DECLARES_METHOD, true),
	DECLARES_LOCAL_VARIABLE				( Type.ID_DECLARES_LOCAL_VARIABLE, true),
	DECLARES_FIELD_ACCESS				( Type.ID_DECLARES_FIELD_ACCESS, true),
	DECLARES_METHOD_ACCESS				( Type.ID_DECLARES_METHOD_ACCESS, true),
	DECLARES_TYPE_ACCESS				( Type.ID_DECLARES_TYPE_ACCESS, true),
	DECLARES_LOCAL_VARIABLE_ACCESS		( Type.ID_LOCAL_VARIABLE_ACCESS	, true),
	DECLARES_PARAMETER					( Type.ID_DECLARES_PARAMTER, true),
	
	
	DECLARES							( Type.ID_DECLARES, true),
	ACCESSES							( Type.ID_ACCESS, true),
	REFERENCES							( Type.ID_REFERENCES, true),
		
	ACCESS_TYPE							( Type.ID_ACCESS_TYPE, true),
	ACCESS_FIELD						( Type.ID_ACCESS_FIELD, true),
	ACCESS_LOCAL_VARIABLE				( Type.ID_ACCESS_LOCAL_VARIABLE, true),
	ACCESS_METHOD						( Type.ID_ACCESS_METHOD, true),
	
	OVERRIDES_METHOD					( Type.ID_OVERRIDES_METHOD, true),
	OVERRIDES_METHOD_TRANSITIVE			( Type.ID_OVERRIDES_METHOD_TRANSITIVE, true),
	IMPLEMENTS_METHOD					( Type.ID_IMPLEMENTS_METHOD, true),
	IMPLEMENTS_METHOD_TRANSITIVE		( Type.ID_IMPLEMENTS_METHOD_TRANSITIVE, true),
	
	EXTENDS_TYPE						( Type.ID_EXTENDS_TYPE, true),
	IMPLEMENTS_TYPE						( Type.ID_IMPLEMENTS_TYPE, true),
	EXTENDS_TYPE_TRANSITIVE				( Type.ID_EXTENDS_TYPE_TRANSITIVE, true),
	IMPLEMENTS_TYPE_TRANSITIVE			( Type.ID_IMPLEMENTS_TYPE_TRANSITIVE, true),
	BELONGS_TO							( Type.ID_BELONGS_TO, true),
	REQUIRES							( Type.ID_REQUIRES, true),
	
	ACCESS_TYPE_TRANSITIVE				( Type.ID_ACCESS_TYPE_TRANSITIVE, true),
	ACCESS_FIELD_TRANSITIVE				( Type.ID_ACCESS_FIELD_TRANSITIVE, true),
	ACCESS_LOCAL_VARIABLE_TRANSITIVE		( Type.ID_ACCESS_LOCAL_VARIABLE_TRANSITIVE, true),
	ACCESS_METHOD_TRANSITIVE			( Type.ID_ACCESS_METHOD_TRANSITIVE, true),
	
	DECLARES_TYPE_TRANSITIVE			( Type.ID_DECLARES_TYPE_TRANSITIVE, true),
	DECLARES_FIELD_TRANSITIVE			( Type.ID_DECLARES_FIELD_TRANSITIVE, true),
	DECLARES_METHOD_TRANSITIVE			( Type.ID_DECLARES_METHOD_TRANSITIVE, true),
	DECLARES_LOCAL_VARIABLE_TRANSITIVE 	( Type.ID_DECLARES_LOCAL_VARIABLE_TRANSITIVE, true),
	
	T_DECLARES_LOCAL_VARIABLE			( Type.ID_DECLARES_LOCAL_VARIABLE, false),
	T_DECLARES_METHOD					( Type.ID_DECLARES_METHOD, false),
	T_DECLARES_FIELD	 				( Type.ID_DECLARES_FIELD, false),
	T_DECLARES_IMPORT 					( Type.ID_DECLARES_IMPORT, false),
	T_DECLARES_TYPE 					( Type.ID_DECLARES_TYPE, false),
	T_DECLARES_FIELD_ACCESS				( Type.ID_DECLARES_FIELD_ACCESS, false),
	T_DECLARES_METHOD_ACCESS			( Type.ID_DECLARES_METHOD_ACCESS, false),
	T_DECLARES_TYPE_ACCESS				( Type.ID_DECLARES_TYPE_ACCESS, false),
	T_DECLARES_LOCAL_VARIABLE_ACCESS	( Type.ID_LOCAL_VARIABLE_ACCESS	, false),
	T_DECLARES_PARAMETER				( Type.ID_DECLARES_PARAMTER, false),
	
	T_DECLARES							( Type.ID_DECLARES, false),
	T_ACCESS							( Type.ID_ACCESS, false),
	T_REFERENCES						( Type.ID_REFERENCES, false),
	
	T_ACCESS_TYPE						( Type.ID_ACCESS_TYPE, false),
	T_ACCESS_FIELD						( Type.ID_ACCESS_FIELD, false),
	T_ACCESS_LOCAL_VARIABLE				( Type.ID_ACCESS_LOCAL_VARIABLE, false),
	T_ACCESS_METHOD						( Type.ID_ACCESS_METHOD, false),
	
	
	T_OVERRIDES_METHOD					( Type.ID_OVERRIDES_METHOD, false),
	T_OVERRIDES_METHOD_TRANSITIVE		( Type.ID_OVERRIDES_METHOD_TRANSITIVE, false),
	T_IMPLEMENTS_METHOD					( Type.ID_IMPLEMENTS_METHOD, false),
	T_IMPLEMENTS_METHOD_TRANSITIVE		( Type.ID_IMPLEMENTS_METHOD_TRANSITIVE, false),
	
	T_EXTENDS_TYPE						( Type.ID_EXTENDS_TYPE, false),
	T_IMPLEMENTS_TYPE					( Type.ID_IMPLEMENTS_TYPE, false),
	T_EXTENDS_TYPE_TRANSITIVE			( Type.ID_EXTENDS_TYPE_TRANSITIVE, false),
	T_IMPLEMENTS_TYPE_TRANSITIVE		( Type.ID_IMPLEMENTS_TYPE_TRANSITIVE, false),
	T_BELONGS_TO						( Type.ID_BELONGS_TO, false),
	T_REQUIRES							( Type.ID_REQUIRES, false),
	
	T_ACCESS_TYPE_TRANSITIVE				( Type.ID_ACCESS_TYPE_TRANSITIVE, false),
	T_ACCESS_FIELD_TRANSITIVE				( Type.ID_ACCESS_FIELD_TRANSITIVE, false),
	T_ACCESS_LOCAL_VARIABLE_TRANSITIVE		( Type.ID_ACCESS_LOCAL_VARIABLE_TRANSITIVE, false),
	T_ACCESS_METHOD_TRANSITIVE				( Type.ID_ACCESS_METHOD_TRANSITIVE, false),
	
	
	T_DECLARES_TYPE_TRANSITIVE				( Type.ID_DECLARES_TYPE_TRANSITIVE, false),
	T_DECLARES_FIELD_TRANSITIVE 			( Type.ID_DECLARES_FIELD_TRANSITIVE, false),
	T_DECLARES_METHOD_TRANSITIVE			( Type.ID_DECLARES_METHOD_TRANSITIVE, false),
	T_DECLARES_LOCAL_VARIABLE_TRANSITIVE	( Type.ID_DECLARES_LOCAL_VARIABLE_TRANSITIVE, false);
	
//ALEX RELATIONS
	
//	EXPLICITLY_CALLS		( Type.ID_EXPLICITLY_CALLS, true ),
//	CHECKS 					( Type.ID_CHECKS, true ),
//	CREATES					( Type.ID_CREATES, true ),
//	DECLARES 				( Type.ID_DECLARES, true ),
//	EXTENDS_CLASS			( Type.ID_EXTENDS_CLASS, true ),
//	EXTENDS_INTERFACES		( Type.ID_EXTENDS_INTERFACES, true ),
//	HAS_PARAMETER_TYPES 	( Type.ID_HAS_PARAMETER_TYPES, true ),
//	HAS_RETURN_TYPE			( Type.ID_HAS_RETURN_TYPE, true ),
//	IMPLEMENTS_INTERFACE	( Type.ID_IMPLEMENTS_INTERFACE, true ),
//	OF_TYPE					( Type.ID_OF_TYPE, true ),
//	TRANS_EXTENDS			( Type.ID_TRANS_EXTENDS, true ),
//	TRANS_IMPLEMENTS		( Type.ID_TRANS_IMPLEMENTS, true ),
//
//	ACCESSES 				( Type.ID_ACCESSES, true ),
//	CALLS					( Type.ID_CALLS, true ),
//	IMPLEMENTS_METHOD		( Type.ID_IMPLEMENTS_METHOD, true ),
//	INHERITS				( Type.ID_INHERITS, true ),	
//	OVERRIDES				( Type.ID_OVERRIDES, true ),
//	USES					( Type.ID_USES, true ),
//
//	IDENTITY				( Type.ID_IDENTITY, true ),
//	STATIC_CALLS			( Type.ID_STATIC_CALLS, true ),
//	REFERENCES				( Type.ID_REFERENCES, true ),
//
//	T_EXPLICITLY_CALLS		( Type.ID_EXPLICITLY_CALLS, false ),
//	T_CHECKS 				( Type.ID_CHECKS, false ),
//	T_CREATES				( Type.ID_CREATES, false ),
//	T_DECLARES 				( Type.ID_DECLARES, false ),
//	T_EXTENDS_CLASS			( Type.ID_EXTENDS_CLASS, false ),
//	T_EXTENDS_INTERFACES	( Type.ID_EXTENDS_INTERFACES, false ),
//	T_HAS_PARAMETER_TYPES 	( Type.ID_HAS_PARAMETER_TYPES, false ),
//	T_HAS_RETURN_TYPE		( Type.ID_HAS_RETURN_TYPE, false ),
//	T_IMPLEMENTS_INTERFACE	( Type.ID_IMPLEMENTS_INTERFACE, false ),
//	T_OF_TYPE				( Type.ID_OF_TYPE, false ),
//	T_TRANS_EXTENDS			( Type.ID_TRANS_EXTENDS, false ),
//	T_TRANS_IMPLEMENTS		( Type.ID_TRANS_IMPLEMENTS, false ),
//
//	T_ACCESSES 				( Type.ID_ACCESSES, false ),
//	T_CALLS					( Type.ID_CALLS, false ),
//	T_IMPLEMENTS_METHOD		( Type.ID_IMPLEMENTS_METHOD, false ),
//	T_INHERITS				( Type.ID_INHERITS, false ),	
//	T_OVERRIDES				( Type.ID_OVERRIDES, false ),
//	T_USES					( Type.ID_USES, false ),
//
//	T_IDENTITY				( Type.ID_IDENTITY, false ),
//	T_STATIC_CALLS			( Type.ID_STATIC_CALLS, false ),
//	T_REFERENCES			( Type.ID_REFERENCES, false );


	private static EnumSet<ARelation> aRelationSet;
	private static EnumSet<ARelation> tRelationSet; 			// Transpose
	private static EnumMap<Type, ARelation> aRelationMap;
	private static EnumMap<Type, ARelation> tRelationMap;	// Transpose
	private static final String TRANSPOSE_CODE = "*";
	private final boolean aDirect;
	private final Type aId;

	static {
		aRelationSet = EnumSet.range(ARelation.DECLARES_TYPE , ARelation.DECLARES_LOCAL_VARIABLE_TRANSITIVE);
		tRelationSet = EnumSet.complementOf(aRelationSet);
		//cRelationSet = EnumSet.range(Relation.T_EXPLICITLY_CALLS, Relation.T_REFERENCES);

		aRelationMap = new EnumMap<Type, ARelation>(Type.class);
		tRelationMap = new EnumMap<Type, ARelation>(Type.class);

		for (ARelation lRelation : aRelationSet)
		{
			aRelationMap.put(lRelation.getType(), lRelation);
		}
		for (ARelation lTransRelation : tRelationSet)
		{
			tRelationMap.put(lTransRelation.getType(), lTransRelation);
		}
	}

	/** 
	 * Construct a relation by specifying the relation Id and whether
	 * it is a direct or transposed relation.
	 * @param pId The id code for the relation.
	 * @param pDirect true for a direct relation, false for a transpose
	 * relation.
	 * @exception AUnsupportedRelationException if the id does not correspond to a 
	 * recognized relation.
	 */
	private ARelation(Type pId, boolean pDirect )
	{
		aId = pId;
		aDirect = pDirect;
	}

	private Type getType()
	{
		return aId;
	}

	/**
	 * Returns a flyweight relation.
	 * @param pEncoding The full encoding of the relation, prefixed in the
	 * case of a  transposed relation.
	 * @return The unique relation object corresponding to pEncoding.
	 * @exception AUnsupportedRelationException if the encoding does not
	 * resolve to a known exception
	 */
	public static ARelation getRelation( String pEncoding ) throws AUnsupportedRelationException
	{
		String lCode = pEncoding;
		// if 
		if( lCode.startsWith( TRANSPOSE_CODE )) // 
		{
			lCode = pEncoding.substring( TRANSPOSE_CODE.length(), pEncoding.length() );
			for (Type relationType : Type.values())
			{	
				if( lCode.equals(relationType.getCode()))
				{			
					return tRelationMap.get(relationType); 	// return the transpose relation
				}
			}
		}
		else 
		{
			for (Type lRelationType : Type.values())
			{	
				if( lCode.equals(lRelationType.getCode()))
				{			
					return aRelationMap.get(lRelationType);
				}
			}
		}

		throw new AUnsupportedRelationException( "Code: " + lCode );
	}



	/**
	 * @return The name of the relation.
	 */
	public String getName()
	{
		if( isDirect() )
			return aId.getDirectName();
		else
			return aId.getTransposeName();
	}

	/** 
	 * @return The complete encoding for this relation, that is, the
	 * simple code, prefixed with the transpose code in the case
	 * of a transpose relation.
	 */
	public String getFullCode()
	{
		if( isDirect() )
		{
			return aId.getCode();
		}
		else
		{
			return TRANSPOSE_CODE + aId.getCode();
		}
	}

	/**
	 * @return The full code for this relation.
	 */
	public String toString()
	{
		return getFullCode();
	}

	/** 
	 * @return True if this is a direct relation.
	 */
	public boolean isDirect()
	{
		return aDirect;
	}

	/**
	 * @return A Description of this relation, in English.
	 */
	public String getDescription()
	{
		return aId.getDescription();
	}

	/**
	 * @return Whether the relation is a primitive relation.
	 */
	public boolean isPrimitive()
	{
		return aId.ordinal() <= 9;
	}

	/** 
	 * @return All relations.
	 */
	public static ARelation[] getAllRelations()
	{

		return ARelation.values();
	}

	/**
	 * Returns all the relations for which a domain category is 
	 * valid.
	 */
	public static Set<ARelation> getAllRelations( AICategories pCategory, boolean general,  boolean pDirect )
	{
		Set<ARelation> lReturn = new HashSet<ARelation>();

		if( pDirect )
		{
			for (ARelation r : aRelationSet)
			{
				if (general) {
					if (r.hasGeneralizedCategory( pCategory ))
						lReturn.add(r);
					
					
				} else{
					if (r.hasDomainCategory( pCategory ))
						lReturn.add(r);
				}
			}
		}
		else // get transpose relations
		{
			for (ARelation t : tRelationSet)
			{
				if (general) {
					if (t.hasGeneralizedCategory( pCategory ))
						lReturn.add(t);
				}
				else {
					if (t.hasDomainCategory( pCategory ))
						lReturn.add(t);
				}
			}
		}

		return lReturn;
	}

	public static ARelation[] getAllRelations( boolean pDirect )
	{
		ARelation[] rArray = new ARelation[aRelationSet.size()];
		if( pDirect )
			return aRelationSet.toArray(rArray);
		else
			return tRelationSet.toArray(rArray);

	}

	public boolean hasGeneralizedCategory(AICategories pCategory) {
		boolean lReturn = false;
		if( pCategory == AICategories.COMPILATION_UNIT )
		{
			if( this == DECLARES)
				lReturn = true;
		}
		else if( pCategory == AICategories.TYPE )
		{
			if( this == DECLARES ||
					this == T_DECLARES ||
					this == ACCESSES ||
					this == REFERENCES ||
					this == T_REFERENCES)
				lReturn = true;
		}
		else if( pCategory == AICategories.METHOD )
		{
			if( this == DECLARES ||
					this == ACCESSES ||
					this == T_DECLARES ||
					this == REFERENCES ||
					this == T_REFERENCES)
					lReturn = true;
		}
		else if( pCategory == AICategories.FIELD )
		{
			if( this == T_DECLARES ||
					this == ACCESSES ||
					this == REFERENCES ||
					this == T_REFERENCES)
				lReturn = true;
		}
		else if( pCategory == AICategories.LOCAL_VARIABLE )
		{
			if( this == T_DECLARES ||
					this == ACCESSES ||
					this == REFERENCES ||
					this == T_REFERENCES)
				lReturn = true;
		}
		else if( pCategory == AICategories.IMPORT )
		{
			if( this == T_DECLARES ||
					this == ACCESSES ||
					this == REFERENCES ||
					this == T_REFERENCES)
				lReturn = true;
		}
		else if( pCategory == AICategories.FIELD_ACCESS )
		{
			if( this == T_DECLARES ||
					this == T_ACCESS)
				lReturn = true;
		}
		else if( pCategory == AICategories.TYPE_ACCESS )
		{
			//TODO evtl. REFERNCES / T_REF.
			if( this == T_DECLARES ||
					this == T_ACCESS ||
					this == ACCESSES)
				lReturn = true;
		}
		else if( pCategory == AICategories.METHOD_ACCESS )
		{
			if( this == T_DECLARES ||
					this == DECLARES ||
					 this == T_ACCESS)
				lReturn = true;
		}
		else if( pCategory == AICategories.LOCAL_VARIABLE_ACCESS )
		{
			if( this == T_DECLARES ||
				this == T_ACCESS)
				lReturn = true;
			
		}else if( pCategory == AICategories.PARAMETER_ACCESS )
		{
			if (this == T_DECLARES)
				lReturn = true;
		}
		
		
		return lReturn;
	}
	
	public boolean hasDomainCategory( AICategories pCategory )
	{
		boolean lReturn = false;
		if( pCategory == AICategories.COMPILATION_UNIT )
		{
			if( this == DECLARES_TYPE ||
					this == DECLARES_IMPORT ||
					this == DECLARES_TYPE_TRANSITIVE ||
					this == DECLARES_FIELD_TRANSITIVE ||
					this == DECLARES_METHOD_TRANSITIVE ||
					this == DECLARES_LOCAL_VARIABLE_TRANSITIVE ||
					this == ACCESS_TYPE ||
					this == ACCESS_FIELD ||
					this == ACCESS_LOCAL_VARIABLE ||
					this == ACCESS_METHOD)
				lReturn = true;
		}
		else if( pCategory == AICategories.TYPE )
		{
			if( this == DECLARES_TYPE ||
					this == T_DECLARES_TYPE ||
					this == DECLARES_TYPE_TRANSITIVE||
					this == T_DECLARES_TYPE_TRANSITIVE||
					this == DECLARES_FIELD ||
					this == DECLARES_FIELD_TRANSITIVE ||
					this == DECLARES_METHOD ||
					this == DECLARES_METHOD_TRANSITIVE ||
					this == DECLARES_LOCAL_VARIABLE_TRANSITIVE ||
					this == T_BELONGS_TO ||
					this == ACCESS_TYPE ||
					this == EXTENDS_TYPE ||
					this == IMPLEMENTS_TYPE ||
					this == T_EXTENDS_TYPE ||
					this == T_IMPLEMENTS_TYPE ||
					this == EXTENDS_TYPE_TRANSITIVE ||
					this == IMPLEMENTS_TYPE_TRANSITIVE ||
					this == T_EXTENDS_TYPE_TRANSITIVE ||
					this == T_IMPLEMENTS_TYPE_TRANSITIVE ||
					this == ACCESS_FIELD ||
					this == ACCESS_LOCAL_VARIABLE ||
					this == ACCESS_METHOD ||
					this == ACCESS_TYPE_TRANSITIVE ||
					this == ACCESS_FIELD_TRANSITIVE ||
					this == ACCESS_LOCAL_VARIABLE_TRANSITIVE ||
					this == ACCESS_METHOD_TRANSITIVE)
				lReturn = true;
		}
		else if( pCategory == AICategories.METHOD )
		{
			if( this == DECLARES_LOCAL_VARIABLE ||
					this == DECLARES_FIELD_ACCESS ||
					this == DECLARES_LOCAL_VARIABLE_ACCESS ||
					this == DECLARES_METHOD_ACCESS ||
					this == DECLARES_TYPE_ACCESS ||
					this == T_DECLARES_METHOD ||
					this == T_DECLARES_METHOD_TRANSITIVE ||
					this == T_BELONGS_TO ||
					this == OVERRIDES_METHOD || 
					this == OVERRIDES_METHOD_TRANSITIVE ||
					this == T_OVERRIDES_METHOD || 
					this == T_OVERRIDES_METHOD_TRANSITIVE ||
					this == IMPLEMENTS_METHOD || 
					this == IMPLEMENTS_METHOD_TRANSITIVE ||
					this == T_IMPLEMENTS_METHOD || 
					this == T_IMPLEMENTS_METHOD_TRANSITIVE ||
					this == ACCESS_TYPE ||
					this == ACCESS_TYPE_TRANSITIVE ||
					this == ACCESS_FIELD ||
					this == ACCESS_LOCAL_VARIABLE ||
					this == ACCESS_METHOD ||
					this == ACCESS_FIELD_TRANSITIVE ||
					this == ACCESS_LOCAL_VARIABLE_TRANSITIVE ||
					this == ACCESS_METHOD_TRANSITIVE)
					lReturn = true;
		}
		else if( pCategory == AICategories.FIELD )
		{
			if( this == T_DECLARES_FIELD ||
					this == T_DECLARES_FIELD_TRANSITIVE ||
					this == T_BELONGS_TO ||
					this == ACCESS_TYPE ||
					this == ACCESS_TYPE_TRANSITIVE ||
					this == ACCESS_FIELD ||
					this == ACCESS_LOCAL_VARIABLE ||
					this == ACCESS_METHOD ||
					this == ACCESS_FIELD_TRANSITIVE ||
					this == ACCESS_LOCAL_VARIABLE_TRANSITIVE ||
					this == ACCESS_METHOD_TRANSITIVE)
				lReturn = true;
		}
		else if( pCategory == AICategories.LOCAL_VARIABLE )
		{
			if( this == T_DECLARES_LOCAL_VARIABLE || 
					this == T_DECLARES_LOCAL_VARIABLE_TRANSITIVE||
					this == T_BELONGS_TO ||
					this == REQUIRES ||
					this == ACCESS_TYPE ||
					this == ACCESS_TYPE_TRANSITIVE ||
					this == ACCESS_FIELD ||
					this == ACCESS_LOCAL_VARIABLE ||
					this == ACCESS_METHOD ||
					this == ACCESS_FIELD_TRANSITIVE ||
					this == ACCESS_LOCAL_VARIABLE_TRANSITIVE ||
					this == ACCESS_METHOD_TRANSITIVE)
				lReturn = true;
		}
		else if( pCategory == AICategories.IMPORT )
		{
			if( this == T_DECLARES_IMPORT ||
					this == T_BELONGS_TO ||
					this == ACCESS_TYPE)
				lReturn = true;
		}
		else if( pCategory == AICategories.TYPE_ACCESS )
		{
			if( this == BELONGS_TO ||
					this == T_DECLARES_TYPE_ACCESS ||
					this == T_BELONGS_TO ||
					this == DECLARES_PARAMETER ||
					this == T_ACCESS_TYPE ||
					this == T_ACCESS_TYPE_TRANSITIVE)
				lReturn = true;
		}
		else if( pCategory == AICategories.FIELD_ACCESS )
		{
			if( this == BELONGS_TO ||
					this == T_DECLARES_FIELD_ACCESS ||
					this == T_ACCESS_FIELD ||
					this == T_ACCESS_FIELD_TRANSITIVE)
				lReturn = true;
		}
		else if( pCategory == AICategories.LOCAL_VARIABLE_ACCESS )
		{
			if( this == BELONGS_TO ||
					this == T_DECLARES_LOCAL_VARIABLE_ACCESS ||
					this == T_ACCESS_LOCAL_VARIABLE ||
					this == T_ACCESS_LOCAL_VARIABLE_TRANSITIVE)
				lReturn = true;
		}
		else if( pCategory == AICategories.PARAMETER_ACCESS )
		{
			if( this == T_REQUIRES ||
					this == T_DECLARES_PARAMETER)
				lReturn = true;
		}
		else if(pCategory == AICategories.METHOD_ACCESS )
		{
			if( this == BELONGS_TO ||
					this == T_DECLARES_METHOD_ACCESS ||
					this == DECLARES_PARAMETER ||
					this == T_ACCESS_METHOD ||
					this == T_ACCESS_METHOD_TRANSITIVE)
				lReturn = true;
		}
		else if( pCategory == AICategories.OUT_OF_CONTEXT )
		{
			if( this == DECLARES_PARAMETER)
				lReturn = true;
		}
		return lReturn;
	}

	/** 
	 * Returns whether a relation as a specified domain category.
	 * A domain category indicates the categories of elements which 
	 * can be in a valid domain for a relation.
	 * @param pCategory The category to test for.
	 * @return True if this relation can have elements of 
	 * category pCategory in its domain.
	 */
//	public boolean hasDomainCategory( AICategories pCategory )
//	{
//		boolean lReturn = false;
//		if( pCategory == AICategories.CLASS )
//		{
//			if( this == DECLARES ||
//					this == T_DECLARES ||
//					this == EXTENDS_CLASS ||
//					this == EXTENDS_INTERFACES ||
//					this == IMPLEMENTS_INTERFACE ||
//					this == INHERITS ||
//					this == REFERENCES ||
//					this == T_CHECKS ||
//					this == T_CREATES ||
//					this == T_EXTENDS_CLASS ||
//					this == T_EXTENDS_INTERFACES ||
//					this == T_HAS_PARAMETER_TYPES ||
//					this == T_HAS_RETURN_TYPE ||
//					this == T_IMPLEMENTS_INTERFACE ||
//					this == T_OF_TYPE ||
//					this == T_USES ||
//					this == IDENTITY ||
//					this == T_IDENTITY ||
//					this == TRANS_EXTENDS ||
//					this == TRANS_IMPLEMENTS ||
//					this == T_TRANS_EXTENDS ||
//					this == T_TRANS_IMPLEMENTS ||
//					this == T_REFERENCES )
//				lReturn = true;
//		}
//		else if( pCategory == AICategories.METHOD )
//		{
//			if( this == ACCESSES ||
//					this == CALLS ||
//					this == EXPLICITLY_CALLS ||
//					this == CHECKS ||
//					this == CREATES ||
//					this == HAS_PARAMETER_TYPES ||
//					this == HAS_RETURN_TYPE ||
//					this == IMPLEMENTS_METHOD ||
//					this == OVERRIDES ||
//					this == USES ||
//					this == REFERENCES ||
//					this == T_EXPLICITLY_CALLS ||
//					this == T_DECLARES ||
//					this == T_CALLS ||
//					this == T_IMPLEMENTS_METHOD ||
//					this == T_INHERITS ||
//					this == T_OVERRIDES ||
//					this == T_USES ||
//					this == IDENTITY ||
//					this == T_IDENTITY ||
//					this == STATIC_CALLS ||
//					this == T_STATIC_CALLS ||
//					this == T_REFERENCES )
//				lReturn = true;
//		}
//		else if( pCategory == AICategories.FIELD )
//		{
//			if( this == OF_TYPE ||
//					this == T_DECLARES ||
//					this == T_ACCESSES ||
//					this == T_INHERITS ||
//					this == T_USES ||
//					this == IDENTITY ||
//					this == T_IDENTITY ||
//					this == T_REFERENCES )
//				lReturn = true;
//		}
//		return lReturn;
//	}

//	/** 
//	 * @return Whether the relation is a union of two or more
//	 * relations, or not.
//	 */
//	public boolean isUnion()
//	{
//		boolean lReturn = false;
//		if( aId == Type.ID_ACCESSES ||
//				aId == Type.ID_CALLS ||
//				aId == Type.ID_USES ||
//				aId == Type.ID_REFERENCES )
//			lReturn = true;
//		return lReturn;
//	}

	/**
	 * @return The direct relation corresponding to this relation, whether
	 * or not the relation is transpose.
	 */
	public ARelation getDirectRelation()
	{
		if( isDirect() )
		{
			return this;
		}
		else
		{
			return aRelationMap.get(aId);
		}
	}

	/**
	 * @return If this is a direct relation, returns
	 * the corresponding transpose.  If this is a transpose
	 * relation, returns the corresponding direct relation.
	 */
	public ARelation getInverseRelation()
	{
		if( isDirect() )
			return tRelationMap.get(aId);
		else
			return getDirectRelation();
	}

//	/**
//	 * @return Whether this relation implies pRelation.
//	 * For example, CALLS implies USES.
//	 */
//	public boolean implies( Relation pRelation )
//	{
//		if( (this.getDirectRelation() == Relation.CALLS) || 
//				(this.getDirectRelation() == Relation.CREATES) ||
//				(this.getDirectRelation() == Relation.ACCESSES ))
//		{
//			if( pRelation.getDirectRelation() == Relation.USES )
//			{
//				return true;
//			}
//		}
//		if( (this.getDirectRelation() == Relation.IMPLEMENTS_INTERFACE ) &&
//				(pRelation.getDirectRelation() == Relation.TRANS_IMPLEMENTS ))
//		{
//			return true;
//		}
//		if( (this.getDirectRelation() == Relation.EXTENDS_CLASS ) &&
//				(pRelation.getDirectRelation() == Relation.TRANS_EXTENDS ))
//		{
//			return true;
//		}
//		if( (this.getDirectRelation() == Relation.T_ACCESSES) ||
//				(this.getDirectRelation() == Relation.T_CALLS) ||
//				(this.getDirectRelation() == Relation.T_CHECKS) ||
//				(this.getDirectRelation() == Relation.T_CREATES) ||
//				(this.getDirectRelation() == Relation.T_EXTENDS_CLASS) ||
//				(this.getDirectRelation() == Relation.T_EXTENDS_INTERFACES) ||
//				(this.getDirectRelation() == Relation.T_HAS_PARAMETER_TYPES) ||
//				(this.getDirectRelation() == Relation.T_HAS_RETURN_TYPE) ||
//				(this.getDirectRelation() == Relation.T_IMPLEMENTS_INTERFACE) ||
//				(this.getDirectRelation() == Relation.T_OF_TYPE) ||
//				(this.getDirectRelation() == Relation.T_STATIC_CALLS ) )
//		{
//			if( pRelation.getDirectRelation() == Relation.REFERENCES )
//			{
//				return true;
//			}
//		}			
//		return false;
//	}


	/**
	 * Type enum encapsulates the following information of a relation:
	 * i. code name
	 * ii. direct name
	 * iii. transpose name of the relation
	 * iv. detailed description
	 * 
	 * @author	iyuen
	 * @see 	de.ovgu.cide.mining.ARelation.model.Relation
	 */
	public enum Type 
	{
		
		//ALEX RELATION
		ID_DECLARES_TYPE 
		("declares type", "declaring type/s", "type declared by", "compilation unit or type declares a type"), 
		ID_DECLARES_IMPORT 
		("declares import", "declaring import/s", "import declared by", "compilation unit declares an import"), 
		ID_DECLARES_FIELD 
		("declares field", "declaring field/s", "field declared by", "type or compilation unit declares a field"), 
		ID_DECLARES_METHOD
		("declares method", "declaring method/s", "method declared by", "type or compilation unit declares a method"), 
		ID_DECLARES_LOCAL_VARIABLE
		("declares local variable", "declaring local variable/s", "local variable declared by", "method, type or compilation unit declares a local variable"), 
	
		ID_DECLARES_FIELD_ACCESS
		("declares field access", "declaring field access/es", "field access declared by", "method declares a field access"), 
		ID_DECLARES_METHOD_ACCESS
		("declares method access", "declaring method access/es", "method access declared by", "method declares a method access"), 
		ID_DECLARES_TYPE_ACCESS
		("declares type access", "declaring type access/es", "type access declared by", "method declares a type access"), 
		ID_LOCAL_VARIABLE_ACCESS
		("declares local variable access", "declaring local variable access/es", "local variable access declared by", "method declares a local variable access"), 
		
		ID_DECLARES
		("declares element", "declaring element", "declared by", "element declare another element"), 
		ID_ACCESS
		("access element", "accesses element", "accessed by", "accessed element belongs to a specific element"),
		ID_REFERENCES
		("references element", "references element", "referenced by", "references element belongs to a specific element"),
		
		ID_ACCESS_TYPE
		("accesses type", "accessing type", "type accessed by", "type accessed by another element"),
		ID_ACCESS_FIELD
		("accesses field", "accessing field", "field accessed by", "field accessed by another element"),
		ID_ACCESS_LOCAL_VARIABLE
		("accesses local variable", "accessing local variable", "local variable accessed by", "local variable accessed by another element"),
		ID_ACCESS_METHOD
		("accesses method", "accessing method", "method accessed by", "method accessed by another element"),
		ID_DECLARES_PARAMTER
		("declares parameter", "declares parameter", "parameter declared by", "parameter declared by method"),
		
		
		ID_ACCESS_TYPE_TRANSITIVE
		("transitively accesses type", "transitively accessing type", "type transitively accessed by", "type transitively accessed by another element"),
		ID_ACCESS_FIELD_TRANSITIVE
		("transitively accesses field", "transitively accessing field", "field transitively accessed by", "field transitively accessed by another element"),
		ID_ACCESS_LOCAL_VARIABLE_TRANSITIVE
		("transitively accesses local variable", "transitively accessing local variable", "local variable transitively accessed by", "local variable transitively accessed by another element"),
		ID_ACCESS_METHOD_TRANSITIVE
		("transitively accesses method", "transitively accessing method", "method transitively accessed by", "method transitively accessed by another element"),
		
		ID_BELONGS_TO
		("belongs to", "belongs to", "referenced in (*belongs to)", "accessed element belongs to a specific element"),
		ID_REQUIRES
		("requires", "requires", "*requires", "element requires another element"),
	
		
		
		
		ID_EXTENDS_TYPE
		("extends type", "extending type", "type extended by", "type extending another type"),
		ID_IMPLEMENTS_TYPE
		("implements type", "implementing type", "type implemented by", "type implementing another type"),
		
		ID_EXTENDS_TYPE_TRANSITIVE
		("transitively extends type", "transitively extending type", "type transitively extended by", "type transitively extending another type"),
		ID_IMPLEMENTS_TYPE_TRANSITIVE
		("transitively implements type", "transitively implementing type", "type transitively implemented by", "type transitively implementing another type"),
		
		ID_OVERRIDES_METHOD
		("overrides method", "overriding method", "method overridden by", "method overriding another method"),
		ID_OVERRIDES_METHOD_TRANSITIVE
		("transitively overrides method", "transitively overriding method", "method transitively overridden by", "method transitively overriding another method"),
		ID_IMPLEMENTS_METHOD
		("implements method", "implementing method", "method implemented by", "method implementing another method"),
		ID_IMPLEMENTS_METHOD_TRANSITIVE
		("transitively implements method", "transitively implementing method", "method transitively implemented by", "method transitively implementing another method"),
			
		ID_DECLARES_TYPE_TRANSITIVE 
		("transitively declares type", "transitively declaring type/s", "type transitively declared by", "compilation unit or type transitively declares a type"), 
		ID_DECLARES_FIELD_TRANSITIVE
		("transitively declares field", "transitively declaring field/s", "field transitively declared by", "type or compilation unit transitively declares a field"), 
		ID_DECLARES_METHOD_TRANSITIVE
		("transitively declares method", "transitively declaring method/s", "method transitively declared by", "type or compilation unit transitively declares a type"), 
		ID_DECLARES_LOCAL_VARIABLE_TRANSITIVE
		("transitively declares local variable", "transitively declaring local variable/s", "local variable transitively declared by", "method, type or compilation unit transitively declares a local variable"); 

		
		/*
		 * Primitive: 0-9
		 */
//		ID_EXPLICITLY_CALLS 
//		("explicitly-calls", "e-calling", "e-called by",	"a method calls a method explicitly"),
//		ID_CHECKS 
//		("checks", "checking", "checked by", "a method checks the run-time type of an object for a specific type"),
//		ID_CREATES 
//		("creates", "creating", "created by", "a method creates an object of a type"),
//		ID_DECLARES 
//		("declares", "declaring", "declared by", "a type declares a member"), 
//		ID_EXTENDS_CLASS 
//		("extends-class", "extending", "extended by", "a class directly extends a class"),
//		ID_EXTENDS_INTERFACES 
//		("extends-interface", "i-extending", "i-extended by", "an interface directly extends an interface"),
//		ID_HAS_PARAMETER_TYPES 
//		("has-parameter-types", "having p-types", "p-type of", "a method has parameters of types"),
//		ID_HAS_RETURN_TYPE 
//		("has-return-type", "having return-type", "return-type of", "a method has return type"),
//		ID_IMPLEMENTS_INTERFACE 
//		("implements-interface", "implementing", "implemented by", "a class directly implements an interface"),
//		ID_OF_TYPE 
//		("of-type", "being of type", "type of", "a field is of type"),
//
//		ID_TRANS_EXTENDS 
//		("transitively-extends", "transitively extending", "transitively extended by", "a class transitively extends a class"),
//		ID_TRANS_IMPLEMENTS 
//		("transitively-implements", "transitively implementing", "transitively implemented by", "a class transitively implements an interface"),
//
//		ID_ACCESSES 
//		("accesses", "accessing", "accessed by", "a method accesses a field, either to read or write"),
//		ID_CALLS 
//		("calls", "calling", "called by", "a method calls a method"),
//		ID_IMPLEMENTS_METHOD 
//		("implements-method", "m-implementing", "m-implemented by", "a method implements an interface method"),
//		ID_INHERITS 
//		("inherits", "inheriting", "inherited by", "a class inherits fields and methods"),	
//		ID_OVERRIDES 
//		("overrides", "overriding", "overridden by", "a method overrides a method"),
//		ID_USES 
//		("uses", "using", "used by", "a method uses a program element"),
//
//		ID_IDENTITY 
//		("identity", "is", "is", "an element is itself"),
//		ID_STATIC_CALLS 
//		("statically-calls", "statically calls", "statically called by", "a method statically calls another method"),
//		ID_REFERENCES 
//		("references", "references", "referenced by", "a class/method references a class/method/field"),
//
//		ID_ERROR 
//		("error", "error", "error", "error");
		
		
		

		private final String aCode;
		private final String aDirectName;
		private final String aTransposeName;
		private final String aDescription;

		Type(String pCode, String pDirectName, String pTransposeName, String pDescription)
		{
			aCode = pCode;
			aDirectName = pDirectName;
			aTransposeName = pTransposeName;
			aDescription = pDescription;
		}

		public String getCode() 
		{
			return aCode;
		}

		public String getDirectName()
		{
			return aDirectName;
		}

		public String getTransposeName()
		{
			return aTransposeName;
		}

		public String getDescription()
		{
			return aDescription;
		}
	}

}


