package edu.wm.flat3.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.IConcernListener;
import edu.wm.flat3.util.ARFFFile;
import edu.wm.flat3.util.ProblemManager;

public class Concern implements Comparable<Concern>
{
	private ConcernRepository repository;

	private int id;
	
	/**
	 * The name may contain escaped characters like: \\ \,
	 */
	private String name;
	
	private String unescapedName;
	
	private String shortName;
	private String description;
	private String color;

	private IConcernListener changeListener;

	static CodeModelImporter codeImporter = null; 
	
	public Concern(ConcernRepository repository, 
	               IConcernListener changeListener,
	               ResultSet resultSet)
	{
		this.repository = repository;
		this.changeListener = changeListener;

		try
		{
			id 			=    resultSet.getInt(1);
			setName(	  resultSet.getString(2));
			shortName 	= resultSet.getString(3);
			description = resultSet.getString(4);
			color 		= resultSet.getString(5);
		}
		catch (SQLException e)
		{
			ProblemManager.reportException(e, true);
		}
	}
	
	// ----------------------------------------------------
	// ACCESSORS
	// ----------------------------------------------------

	public int getId()
	{
		return id;
	}

	/**
	 * @return The name of the concern.  May include escaped
	 * characters (e.g., Jack\,Jill)
	 */
	public String getName()
	{
		return name;
	}
	
	public String getQualifiedName()
	{
		String path = name;
		
		Concern parent = getParent();
		while (parent != null && !parent.isRoot())
		{
			path = parent.getName() + "/" + path;
			parent = parent.getParent();
		}

		return path;
	}

	/**
	 * Changes the name of a concern.
	 * 
	 * @param pNewName
	 *            The new name for the concern.
	 */
	public boolean rename(String newName)
	{
		assert !isRoot(); // Can't rename the root element!
		
		if (name.equals(newName))
			return false;

		repository.renameConcern(id, newName);

		// Safe to update locally cached name
		setName(newName);
		
		if (changeListener != null)
		{
			changeListener.modelChanged(ConcernEvent.createUpdateLabelEvent(this));
		}

		return true;
	}
	
	private boolean isNameEqual(String nameToCompare)
	{
		if (name == nameToCompare)
			return true;
		else if (name == null || nameToCompare == null)
			return false;
		
		if (isRoot())
		{
			// The root concern doesn't match it's name (which is
			// machine generated), it matches the empty string.
			return nameToCompare.isEmpty();
		}
		else
		{
			return name.equals(nameToCompare); 
		}
	}
	
	/**
	 * Checks the validity of the concern name.
	 * @param proposedChildName
	 * 	Display name of the child we are curious about.
	 * @return
	 * 	null if the name is valid,
	 * 	otherwise the reason why it is invalid
	 */
	public String isChildNameValid(String proposedChildName)
	{
		if (proposedChildName == null)
		{
			return FLATTT.getResourceString("NullName");
		}
		else if (proposedChildName.isEmpty())
		{
			return FLATTT.getResourceString("EmptyName");
		}
		else if (proposedChildName.indexOf(ConcernRepository.DEFAULT_ROOT_CONCERN_NAME) >= 0)
		{
			return FLATTT.getResourceString("NotAllowed");
		}
		
		// Make sure we don't already have a child by the same name
		for(Concern child : getChildren())
		{
			if (child.getDisplayName().equals(proposedChildName))
			{
				return FLATTT.getResourceString("AlreadyExists");
			}
		}
		
		return null; // Name is valid
	}
	
	/**
	 * @return The name of the concern with escapes removed 
	 * (e.g., Jack\,Jill -> Jack, Jill).
	 */
	public String getDisplayName()
	{
		return unescapedName;
	}
	
	private void setName(String name)
	{
		this.name = name;
		this.unescapedName = ARFFFile.unescape(name);
	}

	public String getShortName()
	{
		return shortName;
	}
	
	public String getSafeShortName()
	{
		if (shortName != null && !shortName.isEmpty())
			return shortName;
		else
			return getName();
	}

	public String getShortDisplayName()
	{
		if (shortName != null && !shortName.isEmpty())
			return shortName;
		else
			return getDisplayName();
	}
	
	public String getDescription()
	{
		return description;
	}

	public String getColor()
	{
		return color;
	}

	public boolean isRoot()
	{
		return name.endsWith(ConcernRepository.DEFAULT_ROOT_CONCERN_NAME);
	}

	public IConcernListener getChangeListener()
	{
		return changeListener;
	}

	// ----------------------------------------------------
	// Tree methods
	// ----------------------------------------------------
	
	public boolean isInConcernDomain(ConcernDomain concernDomain)
	{
		Concern concernDomainRoot = concernDomain.getRoot();
		
		Concern parent = this;
		while (parent != null)
		{
			if (parent.equals(concernDomainRoot))
			{
				return true;
			}
			
			parent = parent.getParent();
		}
		
		return false;
	}

	public Concern getParent()
	{
		return repository.getParentConcern(this);
	}

	public List<Concern> getChildren()
	{
		return repository.getChildConcerns(this);
	}

	public Collection<Concern> getSelfAndDescendants()
	{
		return getSelfAndDescendants(new ArrayList<Concern>());
	}
	
	public Collection<Concern> getSelfAndDescendants(Collection<Concern> descendants)
	{
		descendants.add(this);
		
		for(Concern child : getChildren())
		{
			child.getSelfAndDescendants(descendants);
		}
		
		return descendants;
	}
	
	public int getDescendantCount()
	{
		List<Concern> children = getChildren();
		if (children.isEmpty())
			return 1; // Ourselves
		else
		{
			int count = 0;
			
			for(Concern child : children)
			{
				count += child.getDescendantCount();
			}
			
			return count;
		}
	}
	
	public boolean hasChildren()
	{
		return repository.hasChildConcerns(this);
	}
	
	/**
	 * Create a child under the current concern.
	 * @param concernPath
	 * 	Path of the new child.  If the path is a simple name (e.g., Joe),
	 *	an immediate child will be created.  Otherwise, a subtree of
	 *  concerns will be created under this concern to satisfy the path
	 *  (e.g., Joe/Jim will create Joe if it doesn't exist, then Jim). 
	 *  <P> 
	 *  Path element names are separated by forward slashes (/).  You
	 *  can escape the slash (\/) to avoid it being treated as a separator
	 *  and it will become part of the created concern's name.
	 * @param concernShortName
	 * 	Short name for the concern.
	 * @param listener
	 * 	Non-null if you are interested in being notified of changes to the
	 * 	concern.
	 * @return
	 * 	The newly created concern.
	 * @throws InvalidConcernNameException 
	 */
	public Concern createConcernPath(String concernPath, String concernShortName) 
		throws InvalidConcernNameException
	{
		return findOrCreateByPath(concernPath, concernShortName, true);
	}
	
	/**
	 * Create an immediate child of the current concern.
	 * @param concernName
	 * 	Name of the new child. May contain escaped characters (e.g., \/,
	 * 	\,).
	 * @param concernShortName
	 * 	Short name for the concern.
	 * @param listener
	 * 	Non-null if you are interested in being notified of changes to the
	 * 	concern.
	 * @return
	 * 	The newly created concern.
	 * @throws InvalidConcernNameException 
	 */
	public Concern createChild(String concernName, String concernShortName) 
		throws InvalidConcernNameException
	{
		String reason = isChildNameValid(concernName);
		if (reason != null)
		{
			throw new InvalidConcernNameException(concernName, reason);
		}
		
		// Now that we're ready to create the actual concern, 
		// put the slash back without the backslash escape
		Concern newChild = repository.createConcern(changeListener, 
										concernName, 
										concernShortName, 
										"", 
										"");
		if (newChild != null)
			addChild(newChild);
		
		return newChild;
	}
	
	/**
	 * Changes the parent of the specific concern to be the current
	 * concern.
	 * @param child
	 * 	Concern to reparent.
	 * @return
	 */
	public void addChild(Concern child)
	{
		// Get the parent *before* reparenting
		Concern oldParent = child.getParent();
		
		if (!repository.addChildConcern(this, child))
			return;

		// The child may be from a different concern model so update the
		// change listener to be for the current concern model
		child.changeListener = this.changeListener;

		if (changeListener != null)
		{
			ConcernEvent events = new ConcernEvent();
			
			// Create two events: one for the old parent and one for the new
			// (in that order)
			if (oldParent != null)
			{
				events.addChildrenChangedEvent(oldParent);
			}
			
			events.addChildrenChangedEvent(this);
			changeListener.modelChanged(events);
		}
	}
	
	/**
	 * Searches for a concern using a concern path.
	 * @param path
	 * 	Path to the concern relative to the current concern,
	 * 	e.g., "7 Lexical Conventions/7.2 Whitespace"
	 * @return
	 * 		The concern or null
	 */
	public Concern findByPath(final String path)
	{
		try
		{
			return findOrCreateByPath(path, null, false);
		}
		catch (InvalidConcernNameException e)
		{
			return null;
		}
	}

	private Concern findOrCreateByPath(final String path,
	                                   final String shortName,
	                                   final boolean create) 
		throws InvalidConcernNameException
	{
		if (path == null || path.isEmpty())
			return null;

		String[] parts = splitPath(path);
		assert parts.length > 0;

		// Convert escaped slashes to normal slashes when doing
		// the name compare
		if (!isNameEqual(parts[0]))
		{
			// Our name isn't equal to the current part of the path
			return null;
		}
		else if (parts.length == 1)
		{
			// Success: We've matched the entire path
			return this;
		}

		assert parts.length > 1;
		
		// Start at one so we skip the part of the path belonging
		// to this concern (the parent)

		String childPath = parts[1];
		for(int i = 2; i < parts.length; ++i)
		{
			childPath += "/" + parts[i];
		}
		
		List<Concern> children = getChildren();
		if (children != null)
		{
			for (Concern child : children)
			{
				Concern found = child.findOrCreateByPath(childPath, 
						shortName, create);
				if (found != null)
					return found;
			}
		}

		if (create)
		{
			// Create the child on demand
			Concern newChild = createChild(parts[1], shortName);
			if (newChild != null)
				return newChild.findOrCreateByPath(childPath, shortName, create);
		}

		return null;
	}

	/**
	 * Splits a path into subpaths.
	 * 
	 * @param path
	 * 		Concern path.  May contain escaped path delimiters.
	 * @return
	 */
	private static String[] splitPath(String path)
	{
		path = path.replace("\\/", "$FORWARD_SLASH_ESCAPE$");
		
		String[] parts = path.split("/");
		for(int i = 0; i < parts.length; ++i)
		{
			parts[i] = parts[i].replace("$FORWARD_SLASH_ESCAPE$", "\\/");
		}
		
		return parts;
	}
	
	/**
	 * Removes this concern from the repository.
	 */
	public int remove()
	{
		Concern parent = getParent();
		
		// The also removes children
		int numRemoved = repository.removeConcernAndChildren(this);
		if (numRemoved > 0 && changeListener != null)
		{
			changeListener.modelChanged(ConcernEvent.createChildrenChangedEvent(parent));
		}

		// We should really put ourselves into an invalid state at
		// this point so that no one accidentally use us
		
		return numRemoved;
	}
	
	// ----------------------------------------------------
	// LINKS
	// ----------------------------------------------------

	public boolean link(IJavaElement javaElement, EdgeKind linkType)
	{
		assert javaElement instanceof IMember;

		if (!(javaElement instanceof IMember))
			return false;

		IMember member = (IMember) javaElement;
		
		String elementHandle = member.getHandleIdentifier();

		Component component = repository.getComponent(elementHandle);
		if (component == null)
		{
			ProblemManager.reportError("Component not found", 
					"Component '" + member.getDeclaringType().getElementName() +
					"." + member.getElementName() +
					"' not found", "Handle: " + elementHandle);
			return false;
		}
		
		return link(component, linkType);
	}
	
	public boolean link(Component component, EdgeKind linkType)
	{
		boolean success = repository.link(this, component, linkType);

		if (changeListener != null && success)
			changeListener.modelChanged(ConcernEvent.createLinkEvent(this, 
					component.getJavaElement(), linkType));

		return success;
	}

	public boolean link(Component[] components, EdgeKind linkType)
	{
		boolean success = repository.link(this, components, linkType);

		// We assume the caller will send the event for efficiency
		//if (changeListener != null && success)
		//{
		//	changeListener.modelChanged(ConcernEvent.createLinkEvent(this, 
		//			component.getJavaElement(), linkType));
		//}

		return success;
	}
	
	/**
	 * Removes an element from its concern.
	 * 
	 * @param pElement
	 *            The element to remove. Must exist.
	 */
	public boolean unlink(Component component, EdgeKind linkType)
	{
		return unlink(component.getJavaElement(), linkType);
	}
	
	/**
	 * Removes an element from its concern.
	 * 
	 * @param pElement
	 *            The element to remove. Must exist.
	 */
	public boolean unlink(IJavaElement pElement, EdgeKind linkType)
	{
		assert pElement instanceof IMember;

		if (!(pElement instanceof IMember))
			return false;

		String elementHandle = ((IMember) pElement).getHandleIdentifier();
		if (elementHandle == null)
			return false;

		boolean result = repository.unlink(this, elementHandle, linkType);

		if (changeListener != null && result)
		{
			changeListener.modelChanged(
					ConcernEvent.createUnlinkEvent(this, pElement, linkType));
		}
		
		return result;
	}

	/**
	 * Removes an element from its concern.
	 * 
	 * @param pElement
	 *            The element to remove. Must exist.
	 */
	public boolean unlinkRecursive(IJavaElement pElement, EdgeKind linkType)
	{
		assert pElement instanceof IMember;

		boolean result = false;
		
		if (unlink(pElement, linkType))
			result = true;
		
		IMember member = (IMember) pElement;

		try
		{
			for(IJavaElement child : member.getChildren())
			{
				if (unlinkRecursive(child, linkType))
					result = true;
			}
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e, 
					"Failed to unlink concern '" + toString() + 
					"' from element '" + pElement.getHandleIdentifier() + "'.", true);
		}
		
		return result;
	}

	public Collection<Component> getLinks(final EdgeKind linkType)
	{
		return repository.getLinks(this, linkType);
	}

	public void getSelfAndDesecendantLinks(	final EdgeKind linkType,
	                                        final Collection<Component> components)
	{
		components.addAll(getLinks(linkType));
		for(Concern child : getChildren())
		{
			child.getSelfAndDesecendantLinks(linkType, components);
		}		
	}
	
	public boolean isLinked(final Component component, final EdgeKind linkType)
	{
		return repository.isLinked(this, component, linkType);
	}
	
	public boolean isLinked(final IJavaElement javaElement, final EdgeKind linkType)
	{
		if (javaElement == null || linkType == null)
			return false;

		if (!verifyCodeModelExists(javaElement))
			return false;
		
		// get handle to the element
		Component component = repository.getComponent(javaElement.getHandleIdentifier());
		if (component == null) 
		{
			// This will happen if PersistToDB hasn't finished loading the
			// Java Model
			return false;
		}

		// if there is an edge b/w the concern and the component
		// return true
		return isLinked(component, linkType);
	}

	public boolean isLinked(final EdgeKind linkType)
	{
		return repository.isLinked(this, linkType);
	}

	public boolean isSelfOrDescendantLinked(final EdgeKind linkType)
	{
		if (isLinked(linkType))
			return true;
		else
		{
			List<Concern> children = getChildren();
			if (children == null)
				return false;
			
			for(Concern child : children)
			{
				if (child.isSelfOrDescendantLinked(linkType))
					return true;
			}

			return false;
		}
	}

	public boolean isDescendantLinked(final EdgeKind linkType)
	{
		List<Concern> children = getChildren();
		if (children == null)
			return false;
		
		for(Concern child : children)
		{
			if (child.isSelfOrDescendantLinked(linkType))
				return true;
		}

		return false;
	}
	
	/*
	 * Is the component linked to this concern or any descendant
	 * of this concern.
	 */
	public boolean isSelfOrDescendantLinked(final Component component, final EdgeKind linkType)
	{
		if (isLinked(component, linkType))
			return true;
		else
		{
			List<Concern> children = getChildren();
			if (children == null)
				return false;
			
			for(Concern child : children)
			{
				if (child.isSelfOrDescendantLinked(component, linkType))
					return true;
			}

			return false;
		}
	}
	
	public boolean verifyCodeModelExists(final IJavaElement javaElement)
	{
		// check if the project this element is in has already
		// been written to the database. if not persist the project.
		IJavaProject project = javaElement.getJavaProject();
Component test =repository.getComponent(javaElement.getHandleIdentifier());
		// Lazy load the project code model
		if (project != null && 
			repository.getComponent(javaElement.getHandleIdentifier()) == null)// test for the element, not the whole project; could only have part of project 
																//			project.getHandleIdentifier()) == null)
		{
			// Lazy instantiation
			if (codeImporter == null)
			{
				codeImporter = new CodeModelImporter(repository, project);
			}
			
			codeImporter.run();

			// User requested to not persist the model
			if (codeImporter.isCanceled())
				return false;
		}
		
		return true;
	}

	// ----------------------------------------------------
	// Comparable interface implementation
	// ----------------------------------------------------

	@Override
	public int compareTo(Concern rhs)
	{
		return id - rhs.id;
	}

	// ----------------------------------------------------
	// Object overrides
	// ----------------------------------------------------

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		// Why not just id?
		return (name + id).hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Concern)
		{
			return equals((Concern) obj);
		}
		else
		{
			return false;
		}
	}

	public boolean equals(Concern rhs)
	{
		return this.id == rhs.id;
	}
}
