/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.2 $
 */

package edu.wm.flat3.model;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.ComponentKind;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.ConcernDomain;
import edu.wm.flat3.repository.ConcernRepository;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.repository.InvalidConcernNameException;
import edu.wm.flat3.util.ProblemManager;

/**
 * A Concern Model represents a collection of concerns. This class also
 * implements the Observable role of the Observer design pattern and a Facade to
 * use the concern model. It uses the database as the underlying model.
 */
public class ConcernModel 
	extends 
		ConcernListenerManager 
	implements 
		IConcernListener
{
	private ConcernDomain concernDomain = null;

	// reference to the database class
	private ConcernRepository repository;

	/**
	 * Creates a new, empty concern model.
	 */
	public ConcernModel(ConcernRepository hsqldb, String concernDomainName)
	{
		this.repository = hsqldb;
		initConcernDomain(concernDomainName);
	}

	public void initConcernDomain(String concernDomainName)
	{
		if (concernDomainName == null)
		{
			// Get the default domain
			concernDomain = repository.getConcernDomain(ConcernRepository.DEFAULT_CONCERN_DOMAIN_NAME, 
					this);
			
			if (concernDomain == null)
			{
				List<ConcernDomain> concernDomains = repository.getConcernDomains(this);
				if (concernDomains != null && concernDomains.size() > 0)
					concernDomain = concernDomains.get(0);
			}
				
			if (concernDomain == null)
			{
				// This is the first time the database has been opened so create
				// the default concern domain.
				
				disableNotifications();
				concernDomain = createConcernDomain(
						ConcernRepository.DEFAULT_CONCERN_DOMAIN_NAME,
						"",
						"",
						ConcernRepository.DEFAULT_CONCERN_DOMAIN_KIND,
						this);
				enableNotifications();
			}
		}
		else
		{
			// Get the requested domain
			concernDomain = repository.getConcernDomain(concernDomainName, this);
			if (concernDomain == null)
			{
				ProblemManager.reportError("Concern Domain Not Found", 
						"Concern domain '" + concernDomainName + "' was not found.", 
						null);
			}
		}
	}
	
	// ACCESSOR METHODS

	public Concern getRoot()
	{
		if (concernDomain == null)
			return null;
		else
			return concernDomain.getRoot();
	}

	public List<ConcernDomain> getConcernDomains(IConcernListener changeListener)
	{
		return repository.getConcernDomains(changeListener);
	}
	
	public ConcernDomain getConcernDomain()
	{
		return concernDomain;
	}

	/**
	 * Returns an array containing all the concerns in the concern model.
	 * 
	 * @return The names of the concerns in the concern model.
	 */
	public int getNumConcerns()
	{
		return concernDomain.getRoot().getDescendantCount();
	}
	// CONCERN METHODS

	public Concern getConcernByPath(String concernPath)
	{
		if (concernDomain == null)
			return null;
		else
			return concernDomain.getConcernByPath(concernPath);
	}
	
	public boolean hasConcernDomain(String concernDomainName)
	{
		for(ConcernDomain concernDomain : getConcernDomains(null))
		{
			if (concernDomain.getName().equals(concernDomainName))
				return true;
		}
		
		return false;
	}
	
	public Concern createConcernPath(String concernPath, String concernShortName) 
		throws InvalidConcernNameException
	{
		return getRoot().createConcernPath("/" + concernPath, concernShortName);
	}
	
	public ConcernDomain createConcernDomain(String name, String shortName,
			String description, String kind, IConcernListener changeListener)
	{
		return repository.createConcernDomain(name, shortName, description, kind, changeListener);
	}
	
	public int removeAllConcerns()
	{
		disableNotifications();

		int numRemoved = 0;
		
		// Remove all concerns except the ROOT element
		for(Concern topLevelChild : concernDomain.getRoot().getChildren())
		{
			numRemoved += topLevelChild.remove();
		}

		// Convert the individual CONCERN_CHILD_CHANGED events into a single
		// ALL_CONCERNS_CHANGED event
		
		clearQueuedEvents();
		enableNotifications();
		
		if (numRemoved > 0)
		{
			modelChanged(ConcernEvent.createAllConcernsChanged());
		}
		
		return numRemoved;
	}
	
	public int removeLinks(EdgeKind linkType)
	{
		disableNotifications();
		
		int numUnlinked = repository.unlink(linkType); 

		clearQueuedEvents();
		enableNotifications();
		
		if (numUnlinked > 0)
		{
			modelChanged(ConcernEvent.createAllConcernsChanged());
		}
		
		return numUnlinked;
	}

	/**
	 * Removes all the concerns from the database.
	 */
	public void resetDatabase()
	{
		removeAllConcerns(); // in order to properly update the gui; could this cause any problems??
		repository.resetDatabase();
		concernDomain = null;
		initConcernDomain(null);
		modelChanged(ConcernEvent.createAllConcernsChanged());
	}

	// COMPONENT METHODS

	public List<Component> getComponents(ComponentKind kind)
	{
		return repository.getComponents(kind);
	}

	public List<Component> getComponents()
	{
		return repository.getAllComponents();
	}
	
	public Component getComponent(String javaElementHandle)
	{
		return repository.getComponent(javaElementHandle);
	}

	public boolean hasLinks(Component component, EdgeKind linkType)
	{
		return component.isLinked(concernDomain, linkType);
	}
	
	public List<Concern> getLinkedConcerns(	IJavaElement javaElement, 
											EdgeKind linkType)
	{
		Component component = repository.getComponent(javaElement.getHandleIdentifier());
		if (component == null)
			return null;
		else
			return getLinkedConcerns(component, linkType);
	}

	public List<Concern> getLinkedConcerns(	Component component, 
											EdgeKind linkType)
	{
		return component.getLinkedConcerns(concernDomain, linkType, this);
	}
	
	public EdgeKind getDefaultLinkType()
	{
		int max = 25;
		// Set the default relation to be the first one that has links
		for(Concern topLevelConcern : concernDomain.getRoot().getChildren())
		{
			if (--max <= 0)
				break;
			
			for(EdgeKind concernComponentRelation : EdgeKind.values())
			{
				if (topLevelConcern.isLinked(concernComponentRelation))
				{
					return concernComponentRelation;
				}
			}
		}
		
		return EdgeKind.RELATED_TO;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof ConcernModel))
			return false;
		
		ConcernModel that = (ConcernModel) obj;
	
		return this.getConcernDomain().equals(that.getConcernDomain());
	}
	
	@Override
	public String toString()
	{
		if (concernDomain == null)
			return "<null>";
		else
		{
			return concernDomain.toString();
		}
	}
}
