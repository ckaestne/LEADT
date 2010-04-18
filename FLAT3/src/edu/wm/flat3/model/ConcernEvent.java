package edu.wm.flat3.model;

import java.util.Iterator;

import org.eclipse.jdt.core.IJavaElement;

import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.ui.concerntree.ConcernTreeItem;

public class ConcernEvent implements Iterable<ConcernEvent>
{
	private enum Reason
	{
		LINKED,
		UNLINKED,
		UPDATE_LABEL,
		REMOVED_ELEMENT,
		CHANGED_CONCERN_CHILDREN,
		CHANGED_ALL_CONCERNS,
		CHANGED_DOMAIN_NAME,
		CHANGED_ACTIVE_CONCERN_MODEL,
		CHANGED_LINK_TYPE,
	};
	
	Reason reason = null;
	Concern concern = null;
	IJavaElement element = null;
	EdgeKind linkType = null;

	ConcernEvent next = null;

	public ConcernEvent()
	{ }
	
	private ConcernEvent createNextEvent(Reason reason)
	{
		if (this.reason == null)
		{
			this.reason = reason; 
			return this;
		}
		else
		{
			if (next == null)
				next = new ConcernEvent();

			return next.createNextEvent(reason);
		}
	}
	
	public static ConcernEvent createLinkEvent(	Concern concern, 
	                                            IJavaElement element,
	                                            EdgeKind linkType)
	{
		ConcernEvent event = new ConcernEvent();
		event.addLinkEvent(concern, element, linkType);
		return event;
	}
	
	public static ConcernEvent createUnlinkEvent(	Concern concern, 
	                                                IJavaElement element,
	                                                EdgeKind linkType)
	{
		ConcernEvent event = new ConcernEvent();
		event.addUnlinkEvent(concern, element, linkType);
		return event;
	}
	
	public static ConcernEvent createRemovalEvent(IJavaElement element)
	{
		ConcernEvent event = new ConcernEvent();
		event.addRemovalEvent(element);
		return event;
	}

	public static ConcernEvent createUpdateLabelEvent(Concern concern)
	{
		ConcernEvent event = new ConcernEvent();
		event.addUpdateLabelEvent(concern);
		return event;
	}

	public static ConcernEvent createUpdateLabelEvent(IJavaElement element)
	{
		ConcernEvent event = new ConcernEvent();
		event.addUpdateLabelEvent(element);
		return event;
	}
	
	public static ConcernEvent createChildrenChangedEvent(Concern concern)
	{
		ConcernEvent event = new ConcernEvent();
		event.addChildrenChangedEvent(concern);
		return event;
	}

	public static ConcernEvent createAllConcernsChanged()
	{
		ConcernEvent event = new ConcernEvent();
		event.addAllConcernsChangedEvent();
		return event;
	}

	public static ConcernEvent createDomainNameChangedEvent()
	{
		ConcernEvent event = new ConcernEvent();
		event.addDomainNameChangedEvent();
		return event;
	}

	public static ConcernEvent createActiveConcernModelChangedEvent()
	{
		ConcernEvent event = new ConcernEvent();
		event.createNextEvent();
		return event;
	}

	public static ConcernEvent createLinkTypeChangedEvent()
	{
		ConcernEvent event = new ConcernEvent();
		event.addLinkTypeChangedEvent();
		return event;
	}
	
	public void addEvent(ConcernEvent eventToAdd)
	{
		ConcernEvent event = createNextEvent(eventToAdd.reason);
		event.concern = eventToAdd.concern;
		event.element = eventToAdd.element;
		event.linkType = eventToAdd.linkType;
		event.next = eventToAdd.next;
	}

	public void addLinkEvent(	Concern concern, 
	                            IJavaElement element,
	                            EdgeKind linkType)
	{
		ConcernEvent event = createNextEvent(Reason.LINKED);
		event.concern = concern;
		event.element = element;
		event.linkType = linkType;
	}

	public void addUnlinkEvent(	Concern concern, 
	                            IJavaElement element,
	                            EdgeKind linkType)
	{
		ConcernEvent event = createNextEvent(Reason.UNLINKED);
		event.concern = concern;
		event.element = element;
		event.linkType = linkType;
	}
	
	public void addRemovalEvent(IJavaElement element)
	{
		ConcernEvent event = createNextEvent(Reason.REMOVED_ELEMENT);
		event.element = element;
	}

	public void addUpdateLabelEvent(Object o)
	{
		if (o instanceof Concern)
		{
			addUpdateLabelEvent((Concern) o);
		}
		else if (o instanceof IJavaElement)
		{
			addUpdateLabelEvent((IJavaElement) o);
		}
		else
		{
			assert false;
		}
	}
	
	public void addUpdateLabelEvent(Concern concern)
	{
		ConcernEvent event = createNextEvent(Reason.UPDATE_LABEL);
		event.concern = concern;
	}

	public void addUpdateLabelEvent(IJavaElement element)
	{
		ConcernEvent event = createNextEvent(Reason.UPDATE_LABEL);
		event.element = element;
	}
	
	public void addChildrenChangedEvent(Concern concern)
	{
		ConcernEvent event = createNextEvent(Reason.CHANGED_CONCERN_CHILDREN);
		event.concern = concern;
	}

	public void addAllConcernsChangedEvent()
	{
		createNextEvent(Reason.CHANGED_ALL_CONCERNS);
	}

	public void addDomainNameChangedEvent()
	{
		createNextEvent(Reason.CHANGED_DOMAIN_NAME);
	}

	public void createNextEvent()
	{
		createNextEvent(Reason.CHANGED_ACTIVE_CONCERN_MODEL);
	}

	public void addLinkTypeChangedEvent()
	{
		createNextEvent(Reason.CHANGED_LINK_TYPE);
	}
	
	public Concern getConcern()
	{
		return concern;
	}
	
	public boolean chainIncludesRootConcern()
	{
		for(ConcernEvent current : this)
		{
			if (current.getConcern() != null && current.getConcern().isRoot())
				return true;
		}
		
		return false;
	}

	public IJavaElement getJavaElement()
	{
		return element;
	}
	
	public EdgeKind getLinkType()
	{
		return linkType;
	}
	
	public boolean isLinked()
	{
		return reason == Reason.LINKED;
	}

	public boolean isUnlinked()
	{
		return reason == Reason.UNLINKED;
	}

	public boolean isUpdateLabel()
	{
		return reason == Reason.UPDATE_LABEL;
	}

	public boolean isUpdateConcernLabel()
	{
		return isUpdateLabel() && concern != null;
	}

	public boolean isUpdateElementLabel()
	{
		return isUpdateLabel() && element != null;
	}
	
	public boolean isChangedConcernChildren()
	{
		return reason == Reason.CHANGED_CONCERN_CHILDREN;
	}

	public boolean isChangedAllConcerns()
	{
		return reason == Reason.CHANGED_ALL_CONCERNS;
	}

	public boolean isChangedDomainName()
	{
		return reason == Reason.CHANGED_DOMAIN_NAME;
	}

	public boolean isChangedActiveConcernModel()
	{
		return reason == Reason.CHANGED_ACTIVE_CONCERN_MODEL;
	}

	public boolean isChangedLinkType()
	{
		return reason == Reason.CHANGED_LINK_TYPE;
	}

	/** 
	 * This is only used by ConcernTreeViewer.RefreshRunner() when it calls
	 * StructuredViewer.findItems().  It causes findItems() to return the
	 * widgets for each concern tree item that are potentially affected by
	 * the concern event. 
	 * @return
	 * 	True if this concern tree item may be affected by the concern event.
	 *  If the event is a link, return true if the item is a related link
	 *  (the links are equal or the event link is a child of the item link).
	 *  If the event is a java element, return true if the item is a related
	 *  link (the event's element equals or is a child of the item link).
	 *  If the event is a concern, return true if the item is the same concern.  
	 */
	public boolean matches(ConcernTreeItem cti)
	{
		Concern eventConcern = getConcern();
		IJavaElement eventJavaElement = getJavaElement();
		EdgeKind eventLinkType = getLinkType();
		
		IJavaElement itemJavaElement = cti.getJavaElement();
		Concern itemConcern = cti.getConcern();
		EdgeKind itemLinkType = cti.getLinkType();
		
		// If the item is an element, then the event must be related to the element
		if (itemJavaElement != null)
		{
			if (eventJavaElement == null)
				return false;

			// We are dealing with a link item and an element event.
			// If the event's element is a descendant of the link item's
			// element, then we say it matches.  When the event's element
			// is a member, this will cause two link items to be matched,
			// for the type and the member.  This is okay since ConcernTreeViewer
			// will disregard refreshing the member since it's type will
			// be refreshed.
			IJavaElement current = eventJavaElement;
			while (current != null && !itemJavaElement.equals(current))
			{
				current = current.getParent();
			}
			
			if (!itemJavaElement.equals(current))
			{
				return false;
			}	
			// If the event is a link, verify that our link matches 
			else if (eventConcern != null)
			{
				return itemConcern.equals(eventConcern) &&
					itemLinkType.equals(eventLinkType);
			}
			else
			{
				return true;
			}
		}
		// The item is a concern item
		else if (eventConcern != null)
		{
			return itemConcern.equals(eventConcern);
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public Iterator<ConcernEvent> iterator()
	{
		return new ConcernModelEventIterator(this);
	}

	@Override
	public String toString()
	{
		if (element != null && concern != null)
			return element.getElementName() + " -> " + concern.getDisplayName();
		else if (element != null)
			return element.getElementName();
		else
			return concern.getDisplayName();
	}
}

class ConcernModelEventIterator implements Iterator<ConcernEvent>
{
	ConcernEvent cursor;
	
	public ConcernModelEventIterator(ConcernEvent start)
	{
		cursor = start;
	}
	
	@Override
	public boolean hasNext()
	{
		return cursor != null;
	}

	@Override
	public ConcernEvent next()
	{
		ConcernEvent result = cursor;
		cursor = cursor.next;
		return result;
	}

	@Override
	public void remove()
	{
		assert false; // Not supported
	}
}
