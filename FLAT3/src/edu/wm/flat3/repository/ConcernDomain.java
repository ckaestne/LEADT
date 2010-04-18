package edu.wm.flat3.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.OpenConcernDomainAction;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernListener;
import edu.wm.flat3.util.Comparer;
import edu.wm.flat3.util.ProblemManager;

public class ConcernDomain implements Comparable<ConcernDomain>
{
	private ConcernRepository hsqldb;

	private Integer id;
	private String name;
	//private String description;
	//private String shortName;
	private String kind;

	private IConcernListener changeListener;

	private Concern root;

	public ConcernDomain(ConcernRepository hsqldb,
			IConcernListener changeListener, 
			ResultSet resultSet)
	{
		this.hsqldb = hsqldb;
		this.changeListener = changeListener;

		try
		{
			this.id 			= resultSet.getInt(		1);
			this.name 			= resultSet.getString(	2);
			//this.shortName 	= resultSet.getString(	3);
			//this.description 	= resultSet.getString(	4);
			this.kind 			= resultSet.getString(	5);
		}
		catch (SQLException e)
		{
			ProblemManager.reportException(e, true);
		}

		root = hsqldb.getConcern(id, changeListener);
	}

	public boolean isDefault()
	{
		return name.equals(ConcernRepository.DEFAULT_CONCERN_DOMAIN_NAME);
	}
	
	public boolean rename(String newName)
	{
		if (name.equals(newName))
			return false;

		hsqldb.renameConcernDomain(name, newName);

		// Safe to update locally cached name
		name = newName;
		
		if (changeListener != null)
		{
			changeListener.modelChanged(ConcernEvent.createDomainNameChangedEvent());
		}
		
		return true;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSingularName()
	{
		// Doesn't have to 100% accurate since its only used as a suggestion
		
		if (name.endsWith("oes"))
		{
			// echoes -> echo
			return name.substring(0, name.length()-2);
		}
		else if (name.endsWith("ves"))
		{
			// calves -> calf
			return name.substring(0, name.length()-3) + "f";
		}
		else if (name.endsWith("ies"))
		{
			// poppies -> poppy
			return name.substring(0, name.length()-3) + "y";
		}
		else if (	name.endsWith("os") ||
					name.endsWith("ys") ||
					name.endsWith("es") ||
					name.endsWith("s"))
		{
			// autos -> auto
			// bays -> bay
			// horses -> horse
			// requirements -> requirement
			return name.substring(0, name.length()-1);
		}
		else
		{
			// Already singular or a plural we didn't handle (e.g., firemen)
			return name;
		}
	}

	public String getKind()
	{
		return kind;
	}

	public Concern getRoot()
	{
		return root;
	}

	public Concern getConcernByPath(String concernPath)
	{
		return root.findByPath(concernPath);
	}
	
	public static String isNameValid(String name)
	{
		if (name == null)
		{
			return FLATTT.getResourceString("NullName");
		}
		else if (name.isEmpty())
		{
			return FLATTT.getResourceString("EmptyName");
		}
		else if (name.indexOf(OpenConcernDomainAction.ID_DOMAIN_SEP) >= 0 ||
				name.indexOf(OpenConcernDomainAction.ID_COUNT_SEP) >= 0 ||
				name.indexOf(':' /*ViewFactory.ID_SEP*/ ) >= 0)
		{
			return FLATTT.getResourceString("ConcernDomains.IllegalCharacter");
		}
		else if (ConcernModelFactory.singleton().getModel().hasConcernDomain(name))
		{
			return FLATTT.getResourceString("NameInUse");
		}
		else if (name.equals(ConcernRepository.DEFAULT_CONCERN_DOMAIN_NAME))
		{
			return FLATTT.getResourceString("NotAllowed");
		}
		else
		{
			return null; // Name is valid
		}
	}

	@Override
	public int compareTo(ConcernDomain that)
	{
		int res = Comparer.safeCompare(this.id, that.id);
		if (res != 0)
			return res;

		res = Comparer.safeCompare(this.name, that.name);
		if (res != 0)
			return res;

		return Comparer.safeCompare(this.kind, that.kind);
	}
	
	@Override
	public boolean equals(Object that)
	{
		if (that == null || !(that instanceof ConcernDomain))
			return false;
		
		return compareTo((ConcernDomain) that) == 0;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
