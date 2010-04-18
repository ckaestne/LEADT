/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.decorators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernListener;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.ui.ConcernViewPreferencePage;
import edu.wm.flat3.util.ProblemManager;

/**
 * Decorates elements in Package Explorer, Outline, Type Hierarchy, Search Results, etc.
 */
public class LinkedElementDecorator
	extends LabelProvider
	implements ILightweightLabelDecorator, IConcernListener,
	IPropertyChangeListener
{
	FLATTT concernMapper;
	ConcernModel concernModel;

	Font boldFont = null;

	/**
	 * Creates the new label decorator.
	 */
	public LinkedElementDecorator()
	{
		concernMapper = FLATTT.singleton();
		concernMapper.getPreferenceStore().addPropertyChangeListener(this);

		// We want to be notified when the active concern model changes
		ConcernModelFactory.singleton().addListener(this);

		concernModel = ConcernModelFactory.singleton().getModel();
		concernModel.addListener(this);
	}

	/**
	 * Decorates elements belonging to the concern model in the JDT views.
	 * 
	 * @param pElement
	 *            The element being decorated
	 * @param pDecoration
	 *            The decoration to add to the element's label
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object pElement, IDecoration pDecoration)
	{
		boolean isSuffixEnabled = concernMapper.getPreferenceStore().getBoolean(
				ConcernViewPreferencePage.P_SUFFIX_ENABLED);

		boolean isHighlightingEnabled = concernMapper.getPreferenceStore().getBoolean(
				ConcernViewPreferencePage.P_BOLD_ENABLED);

		if (!isSuffixEnabled && !isHighlightingEnabled)
			return;

		if (!(pElement instanceof IJavaElement))
			return;

		IJavaElement javaElement = Component.validateAndConvertJavaElement((IJavaElement) pElement);

		if (javaElement == null)
			return; // Element is not linkable

		EdgeKind linkType = ConcernModelFactory.singleton().getLinkType();

		// Get the names of the concerns from the concern model
		Collection<Concern> linkedConcerns = concernModel.getLinkedConcerns(
				javaElement, linkType);

		// add the decorations
		if (isSuffixEnabled && linkedConcerns != null)
		{
			StringBuffer buf = new StringBuffer(" ~ ");

			boolean first = true;

			for (Concern concern : linkedConcerns)
			{
				if (!first)
					buf.append(", ");

				buf.append(concern.getShortDisplayName());

				first = false;
			}

			pDecoration.addSuffix(buf.toString());
		}

		if (!isHighlightingEnabled)
			return;

		if ((linkedConcerns == null || linkedConcerns.size() == 0)/*
																	 * &&
																	 * !concernModel.areDescendantComponentsLinked(javaElement.getHandleIdentifier(),
																	 * linkType)
																	 */)
		{
			return;
		}

		if (boldFont == null)
			boldFont = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getBold(
					"Text Font");

		pDecoration.setFont(boldFont);
	}

	/**
	 * Gets the ConcernMapper decorator.
	 * 
	 * @return The decorator.
	 */
	public static LinkedElementDecorator getDecorator()
	{
		// Can we use PlatformUI.getWorkbench() instead?
		IDecoratorManager lDecoratorManager = FLATTT.singleton().getWorkbench().getDecoratorManager();

		if (lDecoratorManager.getEnabled(FLATTT.ID_DECORATOR))
		{
			return (LinkedElementDecorator) lDecoratorManager.getBaseLabelProvider(FLATTT.ID_DECORATOR);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Refreshes decorations when a change in the Concern Model is reported.
	 * 
	 * @param type
	 * @see edu.wm.flat3.model.IConcernListener#modelChanged(int)
	 * @param type
	 *            The type of change to the model. See the constants in ConcernModel
	 */
	@Override
	public void modelChanged(ConcernEvent events)
	{
		if (events.isChangedActiveConcernModel())
		{
			this.concernModel.removeListener(this);
			this.concernModel = ConcernModelFactory.singleton().getModel();

			// We want to be notified when any concerns or links are
			// changed in the active concern model
			this.concernModel.addListener(this);
		}

		if (events.isUpdateConcernLabel() ||
				events.isChangedLinkType() ||
				events.isChangedActiveConcernModel())
		{
			refresh(null); // Refresh all Java elements
		}
		else
		{
			// Refresh only the elements affected by the (un)link

			List<Object> changedElements = null;

			for (ConcernEvent event : events)
			{
				if (!event.isLinked() && !event.isUnlinked())
					continue;

				if (changedElements == null)
					changedElements = new ArrayList<Object>();

				IJavaElement javaElementLinkedOrUnlinked = event.getJavaElement();

				if (javaElementLinkedOrUnlinked instanceof IMethod)
				{
					updateChangedElementsRecursive((IMember) javaElementLinkedOrUnlinked, 
							changedElements);
				}
				else
				{
					changedElements.add(javaElementLinkedOrUnlinked);
				}
			}

			if (changedElements != null)
				refresh(changedElements.toArray());
		}
	}
	
	void updateChangedElementsRecursive(IMember member, List<Object> changedElements)
	{
		changedElements.add(member);
		
		try
		{
			for (IJavaElement elementChild : member.getChildren())
			{
				if (!(elementChild instanceof IMember))
					continue;
				
				IMember memberChild = (IMember) elementChild;
				updateChangedElementsRecursive(memberChild, changedElements);
			}
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e);
		}
	}

	void refresh(Object[] elements)
	{
		Display.getDefault().asyncExec(new RefreshLabelsRunner(elements));
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 * @param pEvent
	 *            the property change event object describing which property changed and how
	 */
	public void propertyChange(PropertyChangeEvent pEvent)
	{
		boldFont = null; // User may have changed the font
		refresh(null);
	}

	private final class RefreshLabelsRunner
		implements Runnable
	{
		LinkedElementDecorator labelProvider;
		Object[] elements;

		public RefreshLabelsRunner(Object[] elements)
		{
			this.labelProvider = getDecorator();
			this.elements = elements;
		}

		public void run()
		{
			if (labelProvider != null)
			{
				fireLabelProviderChanged(new LabelProviderChangedEvent(
						labelProvider, elements));
			}
		}
	}
}
