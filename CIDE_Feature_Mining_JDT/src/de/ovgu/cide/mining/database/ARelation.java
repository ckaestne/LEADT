package de.ovgu.cide.mining.database;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.ARelationKind;

/**
 * used only for the BerkeleyProgramDatabase
 * 
 * @author kaestner
 * 
 */
@Entity
public class ARelation {
	@SuppressWarnings("unused")
	@PrimaryKey
	private int nr;// no meaning, just provided for the database

	@SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = AElement.class)
	public String elementId_from;

	public ARelationKind kind;
	public String elementId_to;

	public ARelation(String from, ARelationKind kind, String to) {
		elementId_from = from;
		this.kind = kind;
		elementId_to = to;
		nr = ++counter;
	}

	/**
	 * default constructor for BerkeleyDB only
	 */
	ARelation() {
	}

	static int counter = 0;
}
