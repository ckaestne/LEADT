/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.5 $
 */

package de.ovgu.cide.mining.database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.ARelationKind;

/**
 * A database storing all the relations between different program elements.
 */
class ProgramDatabase extends AbstractProgramDatabase {

	/**
	 * Data bundle associated with an element. Contains modifier flags and a map
	 * linking relations to their ranges. an IElement instance.
	 */
	

	class Bundle implements Serializable {
		private static final long serialVersionUID = 1L;

		private final Map<ARelationKind, Set<AElement>> aRelations = new HashMap<ARelationKind, Set<AElement>>();

		// int aModifier;

		/**
		 * @return The Map of relations to range. never null.
		 */
		public Map<ARelationKind, Set<AElement>> getRelationMap() {
			return aRelations;
		}
	}

	// Maps IElements (unique because of the Flyweight pattern
	// to bundles containing modifiers and relations
	private WeakHashMap<AElement, Bundle> aElements;
	private Map<String, AElement> elementIndexMap;

	/**
	 * Creates an empty program database.
	 */
	public ProgramDatabase() {
		aElements = new WeakHashMap<AElement, Bundle>();
		elementIndexMap = new HashMap<String, AElement>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ovgu.cide.mining.database.IProgramDatabase#getAllElements()
	 */
	@Override
	public Set<AElement> getAllElements() {
		return aElements.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#getElement(java.lang.String
	 * )
	 */
	@Override
	public AElement getElement(String id) {
		return elementIndexMap.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#contains(de.ovgu.cide.mining
	 * .database.model.AIElement)
	 */
	@Override
	public boolean contains(AElement pElement) {
		assert (pElement != null);
		return aElements.containsKey(pElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#addElement(de.ovgu.cide
	 * .mining.database.model.AIElement)
	 */
	@Override
	public void addElement(AElement pElement) {
		assert (pElement != null);
		if (!aElements.containsKey(pElement)) {
			aElements.put(pElement, new Bundle());
			elementIndexMap.put(pElement.getId(), pElement);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#addRelation(de.ovgu.cide
	 * .mining.database.model.AIElement,
	 * de.ovgu.cide.mining.database.model.ARelation,
	 * de.ovgu.cide.mining.database.model.AIElement)
	 */
	@Override
	public void addRelation(AElement pElement1, ARelationKind pRelation,
			AElement pElement2) throws ElementNotFoundException {
		assert (pElement1 != null);
		assert (pElement2 != null);
		assert (pRelation != null);

		if (!contains(pElement1))
			throw new ElementNotFoundException(pElement1.getId());
		if (!contains(pElement2))
			throw new ElementNotFoundException(pElement2.getId());

		Map<ARelationKind, Set<AElement>> lRelations = (aElements
				.get(pElement1)).getRelationMap();
		assert (lRelations != null);

		Set<AElement> lElements = lRelations.get(pRelation);
		if (lElements == null) {
			lElements = new HashSet<AElement>();
			lRelations.put(pRelation, lElements);
		}
		lElements.add(pElement2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#getRange(de.ovgu.cide.mining
	 * .database.model.AIElement, de.ovgu.cide.mining.database.model.ARelation)
	 */
	@Override
	public Set<AElement> getRange(AElement pElement,
			ARelationKind... pRelations) throws ElementNotFoundException {
		assert (pElement != null);
		assert (pRelations != null);
		if (!contains(pElement))
			throw new ElementNotFoundException(pElement.getId());

		Set<AElement> lReturn = new HashSet<AElement>();
		Map<ARelationKind, Set<AElement>> lRelations = (aElements.get(pElement))
				.getRelationMap();

		for (ARelationKind lRelation : pRelations)
			if (lRelations.containsKey(lRelation)) {
				lReturn.addAll(lRelations.get(lRelation));
			}
		return lReturn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#addRelationAndTranspose
	 * (de.ovgu.cide.mining.database.model.AIElement,
	 * de.ovgu.cide.mining.database.model.ARelation,
	 * de.ovgu.cide.mining.database.model.AIElement)
	 */
	@Override
	public void addRelationAndTranspose(AElement pElement1,
			ARelationKind pRelation, AElement pElement2)
			throws ElementNotFoundException {
		assert (pElement1 != null);
		assert (pElement2 != null);
		assert (pRelation != null);

		// if( !contains( pElement1 ))
		// throw new ElementNotFoundException( pElement1.getId() );
		// if( !contains( pElement2 ))
		// throw new ElementNotFoundException( pElement2.getId() );

		if (contains(pElement1) && contains(pElement2)) {
			addRelation(pElement1, pRelation, pElement2);
			addRelation(pElement2, pRelation.getInverseRelation(), pElement1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#hasRelations(de.ovgu.cide
	 * .mining.database.model.AIElement)
	 */
	@Override
	public boolean hasRelations(AElement pElement)
			throws ElementNotFoundException {
		assert (pElement != null);
		if (!contains(pElement))
			throw new ElementNotFoundException(pElement.getId());

		Map lRelations = ((Bundle) aElements.get(pElement)).getRelationMap();
		return !lRelations.isEmpty();
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// *
	// de.ovgu.cide.mining.database.IProgramDatabase#copyRelations(de.ovgu.cide
	// * .mining.database.model.AIElement,
	// * de.ovgu.cide.mining.database.model.AIElement)
	// */
	// @Override
	// public void copyRelations(AElement pFrom, AElement pTo)
	// throws ElementNotFoundException {
	// assert (pFrom != null);
	// assert (pTo != null);
	//
	// if (!contains(pFrom))
	// throw new ElementNotFoundException(pFrom.getId());
	// if (!contains(pTo))
	// throw new ElementNotFoundException(pTo.getId());
	//
	// Map lRelations = ((Bundle) aElements.get(pFrom)).getRelationMap();
	// for (Iterator i = lRelations.keySet().iterator(); i.hasNext();) {
	// ARelationKind lNext = (ARelationKind) i.next();
	// Set lElements = (Set) lRelations.get(lNext);
	// for (Iterator j = lElements.iterator(); j.hasNext();) {
	// addRelationAndTranspose(pTo, lNext, (AElement) j.next());
	// }
	// }
	// }

	// /**
	// * Removes a relation from an element. Does not automatically remove the
	// * transpose relation.
	// *
	// * @param pElement1
	// * The element to remove the relation from. Must not be null and
	// * must exist in the program database.
	// * @param pRelation
	// * The relation linking pElement1 with pElement2. Must not be
	// * null.
	// * @param pElement2
	// * The range element of the relation. Must not be null and must
	// * exist in the program database.
	// * @throws ElementNotFoundException
	// * If either pFrom or pTo is not indexed in the database.
	// */
	// private void removeRelation(AElement pElement1, ARelationKind pRelation,
	// AElement pElement2) throws ElementNotFoundException {
	// assert (pElement1 != null);
	// assert (pElement2 != null);
	// assert (pRelation != null);
	//
	// if (!contains(pElement1))
	// throw new ElementNotFoundException(pElement1.getId());
	// if (!contains(pElement2))
	// throw new ElementNotFoundException(pElement2.getId());
	//
	// Map lRelations = ((Bundle) aElements.get(pElement1)).getRelationMap();
	// if (!lRelations.containsKey(pRelation))
	// return;
	//
	// Set lElements = (Set) lRelations.get(pRelation);
	// lElements.remove(pElement2);
	// elementIndexMap.remove(pElement2.getId());
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// *
	// de.ovgu.cide.mining.database.IProgramDatabase#removeElement(de.ovgu.cide
	// * .mining.database.model.AIElement)
	// */
	// @Override
	// public void removeElement(AElement pElement)
	// throws ElementNotFoundException {
	// assert (pElement != null);
	// if (!contains(pElement))
	// throw new ElementNotFoundException(pElement.getId());
	//
	// Map lRelations = ((Bundle) aElements.get(pElement)).getRelationMap();
	// for (Iterator i = lRelations.keySet().iterator(); i.hasNext();) {
	// ARelationKind lNext = (ARelationKind) i.next();
	// Set lElements = (Set) lRelations.get(lNext);
	// for (Iterator j = lElements.iterator(); j.hasNext();) {
	// removeRelation((AElement) j.next(), lNext.getInverseRelation(),
	// pElement);
	// }
	// }
	//
	// // Remove the element
	// aElements.remove(pElement);
	// elementIndexMap.remove(pElement.getId());
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see de.ovgu.cide.mining.database.IProgramDatabase#dump()
	// */
	// @Override
	// public void dump() {
	// for (Iterator i = aElements.keySet().iterator(); i.hasNext();) {
	// AElement lElement1 = (AElement) i.next();
	// System.out.println(lElement1);
	//
	// Object obj = aElements.get(lElement1);
	//
	// if (obj == null)
	// continue;
	//
	// Bundle bundle = (Bundle) obj;
	//
	// Map lRelations;
	//
	// if ((lRelations = bundle.getRelationMap()) == null)
	// continue;
	//
	// for (Iterator j = lRelations.keySet().iterator(); j.hasNext();) {
	// ARelationKind lRelation = (ARelationKind) j.next();
	// System.out.println("    " + lRelation);
	// for (Iterator k = ((Set) lRelations.get(lRelation)).iterator(); k
	// .hasNext();) {
	// System.out.println("        " + k.next());
	// }
	// }
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#getModifiers(de.ovgu.cide
	 * .mining.database.model.AIElement)
	 */
	// @Override
	// public int getModifiers(AElement pElement) {
	// int lReturn = 0;
	// if (aElements.containsKey(pElement)) {
	// Bundle lBundle = (Bundle) aElements.get(pElement);
	// lReturn = lBundle.aModifier;
	// }
	// return lReturn;
	// }

	public void estimateFootprint() {

		System.out.println("elementIndexMap.size(): " + elementIndexMap.size());
		System.out.println("aElements.size(): " + aElements.size());
		int s = 0;
		for (AElement a : elementIndexMap.values()) {
			s += a.getId().length();
		}
		System.out.println("combined size of all IDs: " + s);
		s = 0;
		for (AElement a : elementIndexMap.values()) {
			s += a.getDisplayName().length();
		}
		System.out.println("combined size of all displaynames: " + s);

//		FileOutputStream fos = null;
//		ObjectOutputStream out = null;
//		try {
////			fos = new FileOutputStream("d:/tmp/aElements.dat");
////			out = new ObjectOutputStream(fos);
////			out.writeObject(aElements);
////			out.close();
////			fos = new FileOutputStream("d:/tmp/elementIndexMap.dat");
////			out = new ObjectOutputStream(fos);
////			out.writeObject(elementIndexMap);
////			out.close();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
	}

}
