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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityIndex;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.ARelationKind;

/**
 * A database storing all the relations between different program elements.
 */
class BerkeleyProgramDatabase extends AbstractProgramDatabase {

	private static BerkeleyProgramDatabase instance = null;

	public static BerkeleyProgramDatabase getInstance() {
		if (instance == null)
			instance = new BerkeleyProgramDatabase();
		return instance;
	}

	// Maps IElements (unique because of the Flyweight pattern
	// to bundles containing modifiers and relations

	private EntityStore elementStore;
	private PrimaryIndex<String, AElement> elementById;
	private PrimaryIndex<Integer, ARelation> relationsByNr;
	private SecondaryIndex<String, Integer, ARelation> relationsByElement;

	/**
	 * Creates an empty program database.
	 */
	private BerkeleyProgramDatabase() {
		Environment myDbEnvironment = null;

		try {
			EnvironmentConfig envConfig = new EnvironmentConfig();
			envConfig.setTransactional(false);
			envConfig.setAllowCreate(true);
			StoreConfig storeConfig = new StoreConfig();
			storeConfig.setAllowCreate(true);
			myDbEnvironment = new Environment(new File("d:/tmp/dbEnv"),
					envConfig);
			elementStore = new EntityStore(myDbEnvironment, "Elements",
					storeConfig);
			elementStore.truncateClass(AElement.class);
			elementStore.truncateClass(ARelation.class);

			elementById = elementStore.getPrimaryIndex(String.class,
					AElement.class);
			relationsByNr = elementStore.getPrimaryIndex(Integer.class,
					ARelation.class);
			relationsByElement = elementStore.getSecondaryIndex(relationsByNr,
					String.class, "elementId_from");

			// elementInformationById =
			// elementStore.getPrimaryIndex(String.class,
			// ARelation.class);

		} catch (DatabaseException dbe) {
			dbe.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ovgu.cide.mining.database.IProgramDatabase#getAllElements()
	 */
	@Override
	public Iterable<AElement> getAllElements() {
		return elementById.entities();
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
		return elementById.get(id);
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
		return elementById.contains(pElement.getId());
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
		elementById.put(pElement);
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

		// elementStore.
		relationsByNr.put(new ARelation(pElement1.getId(), pRelation, pElement2
				.getId()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ovgu.cide.mining.database.IProgramDatabase#getRange(de.ovgu.cide.mining
	 * .database.model.AIElement, de.ovgu.cide.mining.database.model.ARelation)
	 */
	@Override
	public Set<AElement> getRange(AElement pElement, ARelationKind... pKinds)
			throws ElementNotFoundException {
		assert (pElement != null);
		assert (pKinds != null);
		if (!contains(pElement))
			throw new ElementNotFoundException(pElement.getId());

		Set<AElement> lReturn = new HashSet<AElement>();
		EntityIndex<Integer, ARelation> relations = relationsByElement
				.subIndex(pElement.getId());
		for (ARelation relation : relations.entities()) {
			if (in(relation.kind, pKinds)) {
				AElement element = getElement(relation.elementId_to);
				if (element != null)
					lReturn.add(element);
			}
		}
		return lReturn;
	}

	private boolean in(ARelationKind kind, ARelationKind[] kinds) {
		for (ARelationKind akind : kinds)
			if (akind == kind)
				return true;
		return false;
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

		return relationsByElement.get(pElement.getId()) != null;
	}

	@Override
	public void estimateFootprint() {
		// TODO Auto-generated method stub

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
	// Map<ARelationKind, Set<String>> lRelations =
	// getElementInfo(pFrom).aRelations;
	// for (Iterator<ARelationKind> i = lRelations.keySet().iterator(); i
	// .hasNext();) {
	// ARelationKind lNext = (ARelationKind) i.next();
	// Set<String> lElements = lRelations.get(lNext);
	// for (Iterator<String> j = lElements.iterator(); j.hasNext();) {
	// addRelationAndTranspose(pTo, lNext, getElement(j.next()));
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
	// throw new UnsupportedOperationException();
	// }

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
	// throw new UnsupportedOperationException();
	// }
	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see de.ovgu.cide.mining.database.IProgramDatabase#dump()
	// */
	// @Override
	// public void dump() {
	// throw new UnsupportedOperationException();
	// //
	// // for (Iterator i = aElements.keySet().iterator(); i.hasNext();) {
	// // AElement lElement1 = (AElement) i.next();
	// // System.out.println(lElement1);
	// //
	// // Object obj = aElements.get(lElement1);
	// //
	// // if (obj == null)
	// // continue;
	// //
	// // Bundle bundle = (Bundle) obj;
	// //
	// // Map lRelations;
	// //
	// // if ((lRelations = bundle.getRelationMap()) == null)
	// // continue;
	// //
	// // for (Iterator j = lRelations.keySet().iterator(); j.hasNext();) {
	// // ARelationKind lRelation = (ARelationKind) j.next();
	// // System.out.println("    " + lRelation);
	// // for (Iterator k = ((Set) lRelations.get(lRelation)).iterator(); k
	// // .hasNext();) {
	// // System.out.println("        " + k.next());
	// // }
	// // }
	// // }
	// }

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// * de.ovgu.cide.mining.database.IProgramDatabase#getModifiers(de.ovgu.cide
	// * .mining.database.model.AIElement)
	// */
	// @Override
	// public int getModifiers(AElement pElement) {
	// int lReturn = 0;
	// if (aElements.containsKey(pElement)) {
	// Bundle lBundle = (Bundle) aElements.get(pElement);
	// lReturn = lBundle.aModifier;
	// }
	// return lReturn;
	// }
}
