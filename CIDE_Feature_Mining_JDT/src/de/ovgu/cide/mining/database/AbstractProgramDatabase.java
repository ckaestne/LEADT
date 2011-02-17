package de.ovgu.cide.mining.database;

import java.util.Set;

import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.ARelationKind;

public abstract class AbstractProgramDatabase {

	/**
	 * Returns all the elements indexed in the database.
	 * 
	 * @return A Set of IElement objects
	 */
	public abstract Iterable<AElement> getAllElements();

	public abstract AElement getElement(String id);

	/**
	 * Returns whether an element is indexed in the database.
	 * 
	 * @param pElement
	 *            An element to check for. Should not be null.
	 * @return Whether the database has information about pElement.
	 */
	public abstract boolean contains(AElement pElement);

	/**
	 * Adds an element in the database. The element is initialized with an empty
	 * relation set. If the element is already in the database, nothing happens.
	 * 
	 * @param pElement
	 *            The element to add. Should never be null.
	 */
	public abstract void addElement(AElement pElement);

	/**
	 * Adds a relation pRelation between pElement1 and pElement2. If pElement1
	 * or pElement2 does not exist in the database, an exception is raised, so
	 * these should always be added first.
	 * 
	 * @param pElement1
	 *            The first element in the relation, never null.
	 * @param pRelation
	 *            The relation, never null.
	 * @param pElement2
	 *            The second element in the relation, never null.
	 * @throws ElementNotFoundException
	 *             If pElement1 or pElement2 is not found in the database.
	 */
	public abstract void addRelation(AElement pElement1,
			ARelationKind pRelation, AElement pElement2)
			throws ElementNotFoundException;

	/**
	 * Returns the set of elements related to the domain element through the
	 * specified relation.
	 * 
	 * @param pElement
	 *            The domain element. Cannot be null.
	 * @param pRelation
	 *            The target relation. Cannot be null.
	 * @return A Set of IElement representing the desired range. Never null.
	 * @throws ElementNotFoundException
	 *             If pElement is not indexed in the database
	 */
	public abstract Set<AElement> getRange(AElement pElement,
			ARelationKind... pRelations) throws ElementNotFoundException;

	/**
	 * Convenience method to add a relatio and its transpose at the same time.
	 * 
	 * @param pElement1
	 *            The domain of the relation. Should not be null.
	 * @param pRelation
	 *            The Relation relating the domain to the range. Should not be
	 *            null.
	 * @param pElement2
	 *            The range of the relation. Should not be null.
	 * @throws ElementNotFoundException
	 *             if either of pElement1 or pElement2 are not indexed in the
	 *             database.
	 */
	public abstract void addRelationAndTranspose(AElement pElement1,
			ARelationKind pRelation, AElement pElement2)
			throws ElementNotFoundException;

	/**
	 * Returns whether pElements has any associated relations.
	 * 
	 * @param pElement
	 *            The element to check. Must not be null and exist in the
	 *            database.
	 * @return True if pElement has any associated relations.
	 * @throws ElementNotFoundException
	 *             If either pFrom or pTo is not indexed in the database.
	 */
	public abstract boolean hasRelations(AElement pElement)
			throws ElementNotFoundException;

	public abstract void estimateFootprint();

	// /**
	// * Copies all the relations associated with pFrom to pTo, including its
	// * transposes
	// *
	// * @param pFrom
	// * The source element. Must not be null and must exist in the
	// * database.
	// * @param pTo
	// * The target element. Must not be null and must exist in the
	// * database.
	// * @throws ElementNotFoundException
	// * If either pFrom or pTo is not indexed in the database.
	// **/
	// public abstract void copyRelations(AElement pFrom, AElement pTo)
	// throws ElementNotFoundException;
	//
	// /**
	// * Remove an element and all its direct and transpose relations.
	// *
	// * @param pElement
	// * The element to remove. Must not be null and must exist in the
	// * database.
	// * @throws ElementNotFoundException
	// * If pElement is not indexed in the database.
	// */
	// public abstract void removeElement(AElement pElement)
	// throws ElementNotFoundException;
	//
	// /**
	// * Dumps an image of the database to System.out. For testing purposes. Can
	// * be removed from stable releases.
	// */
	// public abstract void dump();

	// /**
	// * Returns the modifier flag for the element
	// *
	// * @return An integer representing the modifier. 0 if the element cannot
	// be
	// * found.
	// */
	// public abstract int getModifiers(AElement pElement);

}