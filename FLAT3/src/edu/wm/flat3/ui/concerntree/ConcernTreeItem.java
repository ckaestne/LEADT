/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.ui.concerntree;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.util.Comparer;

/**
 * Represents a sub-concern or Java element item in the Concern
 * Tree.  Note: The top-level items are all Concern objects.
 */
public class ConcernTreeItem implements IAdaptable
{
	private IConcernModelProvider provider;
	
	private ConcernTreeItem parent;

	private Concern concern = null;
	private boolean hasChildConcerns = false;

	// The Java element wrapped by this object.
	private IJavaElement javaElement = null;

	// The relationship between the concern and the element
	private EdgeKind linkType = null;

	private static JavaElementLabelProvider aProvider = new JavaElementLabelProvider(
			JavaElementLabelProvider.SHOW_SMALL_ICONS |
			JavaElementLabelProvider.SHOW_PARAMETERS);

	// We leak these
	private static Image parentConcern_SelfLinked_ChildLinked = null;
	private static Image parentConcern_SelfLinked_NotChildLinked = null;
	private static Image parentConcern_NotSelfLinked_ChildLinked = null;
	private static Image parentConcern_NotSelfLinked_NotChildLinked = null;
	private static Image leafConcern_Linked = null;
	private static Image leafConcern_NotLinked = null;
	
	/**
	 * Create a tree item that represents a concern.
	 * 
	 * @param concern
	 *            The concern that the tree item will represent.
	 */
	protected ConcernTreeItem(IConcernModelProvider provider,
	                          ConcernTreeItem parent,
	                          Concern concern)
	{
		this.parent = parent;
		this.provider = provider;
		this.concern = concern;
		this.hasChildConcerns = concern.hasChildren();
	}
	
	/**
	 * Create a tree item that represents a Java element.
	 * <P>
	 * Java element tree items always have a parent tree item for the
	 * concern the element is linked to.  The same Java element may
	 * be linked to multiple concerns and each of these links
	 * will become a child item of the concern.
	 * 
	 * @param parent
	 *            The concern the element is linked to.
	 * @param javaElement
	 *            The Java element that the tree item will represent.
	 * @param linkType
	 * 			  The relationship between the concern and the element.
	 */
	protected ConcernTreeItem(IConcernModelProvider provider,
	                          ConcernTreeItem parent,
	                          IJavaElement javaElement)
	{
		this.parent = parent;
		this.provider = provider;
		
		assert parent != null;
		this.concern = parent.getConcern();
		
		this.javaElement = javaElement;
	}

	public ConcernTreeItem getParent()
	{
		return parent;
	}

	public ConcernTreeItem getParentConcernItem()
	{
		assert javaElement != null;
	
		ConcernTreeItem parentConcernItem = parent;
		
		while (parentConcernItem != null && parentConcernItem.javaElement != null)
		{
			parentConcernItem = parentConcernItem.getParent();
		}

		assert parentConcernItem != null;
		assert parentConcernItem.concern.equals(concern);
		
		return parentConcernItem;
	}
	
	/**
	 * @return The Java element wrapped by this node.
	 */
	public IJavaElement getJavaElement()
	{
		return javaElement;
	}
	
	/**
	 * @return The relationship between the concern and the component
	 */
	public EdgeKind getLinkType()
	{
		return provider.getLinkType();
	}

	/**
	 * @return The name of the concern the element wrapped by this object is in.
	 */
	public Concern getConcern()
	{
		return concern;
	}

	public boolean hasLink(IJavaElement element)
	{
		if (javaElement == null)
		{
			return concern.isLinked(element, getLinkType());
		}
		else
		{
			// For element items, we want to avoid retrieving and
			// storing the links redundantly, since they are
			// already stored in our concern item ancestor.
			// 
			// Walk up the parent until we find the concern item
			// and connect up our links.
			
			return getParentConcernItem().concern.isLinked(element, 
					getLinkType());
		}
	}
	
	/**
	 * Determines if two ConcernTreeItems are equal. Two element nodes are equal if
	 * both of their wrapped objects are equal and if their concerns are equal.
	 * 
	 * @param pObject
	 *            The element to compare to.
	 * @return true if pObject is equal to this object.
	 */
	@Override
	public boolean equals(Object rhs)
	{
		if (rhs instanceof ConcernTreeItem)
		{
			return equals((ConcernTreeItem) rhs);
		}
		else
		{
			return false;
		}
	}

	public boolean equals(ConcernTreeItem rhs)
	{
		return Comparer.safeEquals(this.javaElement, rhs.javaElement) &&
				Comparer.safeEquals(this.getConcern(), rhs.getConcern());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @return the hash code
	 */
	@Override
	public int hashCode()
	{
		String code = concern.toString();
		if (javaElement != null)
			code += javaElement.getElementName();

		return code.hashCode();
	}

	@Override
	public String toString()
	{
		if (javaElement != null)
		{
			aProvider.turnOn(JavaElementLabelProvider.SHOW_TYPE);
			String result = aProvider.getText(javaElement) + " -> " + 
				concern.getDisplayName() +
				" (" + getLinkType() + ")";
			aProvider.turnOff(JavaElementLabelProvider.SHOW_TYPE);
			return result;
		}
		else
			return concern.getDisplayName() + " (" + getLinkType() + ")";
	}
	
	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 * @param pAdapter
	 *            the adapter class to look up
	 * @return a object castable to the given class, or <code>null</code> if
	 *         this object does not have an adapter for the given class
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class pAdapter)
	{
		if (pAdapter == IJavaElement.class)
		{
			return javaElement;
		}
		else if (pAdapter == Concern.class)
		{
			return concern;
		}
		else
		{
			return null;
		}
	}

	public String getText()
	{
		if (javaElement != null)
			return aProvider.getText(javaElement);
		else
			return concern.getDisplayName();
	}

	public Image getImage()
	{
		if (parentConcern_SelfLinked_ChildLinked == null)
			parentConcern_SelfLinked_ChildLinked = AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/lightbulbs_both_on.ico").createImage();
		
		if (parentConcern_SelfLinked_NotChildLinked == null)
			parentConcern_SelfLinked_NotChildLinked = AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/lightbulbs_parent_on.ico").createImage();

		if (parentConcern_NotSelfLinked_ChildLinked == null)
			parentConcern_NotSelfLinked_ChildLinked = AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/lightbulbs_child_on.ico").createImage();

		if (parentConcern_NotSelfLinked_NotChildLinked == null)
			parentConcern_NotSelfLinked_NotChildLinked = AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/lightbulbs_both_off.ico").createImage();
		
		if (leafConcern_Linked == null)
			leafConcern_Linked = AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/lightbulb.png").createImage(); 

		if (leafConcern_NotLinked == null)
			leafConcern_NotLinked = AbstractUIPlugin.imageDescriptorFromPlugin(
					FLATTT.ID_PLUGIN, "icons/lightbulb_off.png").createImage(); 
		
		if (javaElement != null)
		{
			return aProvider.getImage(javaElement);
		}
		
		boolean isLinked = concern.isLinked(getLinkType());
		
		if (hasChildConcerns)
		{
			boolean isChildLinked = concern.isDescendantLinked(getLinkType());
			
			if (isLinked && isChildLinked)
				return parentConcern_SelfLinked_ChildLinked;
			else if (isLinked)
				return parentConcern_SelfLinked_NotChildLinked;
			else if (isChildLinked)
				return parentConcern_NotSelfLinked_ChildLinked;
			else
				return parentConcern_NotSelfLinked_NotChildLinked;
		}
		else
		{
			return isLinked ? 
					leafConcern_Linked : 
					leafConcern_NotLinked;
		}
	}
}
