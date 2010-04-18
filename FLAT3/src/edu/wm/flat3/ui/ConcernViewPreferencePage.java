/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.wm.flat3.FLATTT;

/**
 * Implements the preference page for ConcernMapper.
 */
public class ConcernViewPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage
{
	public static final String P_FILTER_ENABLED = "FilterEnabledPreference";
	public static final String P_BOLD_ENABLED = "BoldEnabledPreference";
	public static final String P_DECORATION_LIMIT = "DecorationLimitPreference";
	public static final String P_SUFFIX_ENABLED = "SuffixEnabledPreference";

	/**
	 * Creates a new preference page for ConcernMapper.
	 */
	public ConcernViewPreferencePage()
	{
		super(FieldEditorPreferencePage.GRID);
		IPreferenceStore lStore = FLATTT.singleton()
				.getPreferenceStore();
		setPreferenceStore(lStore);
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors()
	{
		// ConcernMapperFilter
		addField(new LabelFieldEditor("Filter:", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				P_FILTER_ENABLED,
				FLATTT
						.getResourceString("ui.ConcernMapperPreferencePage.FilterEnabled"),
				getFieldEditorParent()));

		// Decorations
		addField(new LabelFieldEditor("Decorations:", getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				ConcernViewPreferencePage.P_SUFFIX_ENABLED,
				FLATTT
						.getResourceString("ui.ConcernMapperPreferencePage.SuffixEnabled"),
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				ConcernViewPreferencePage.P_BOLD_ENABLED,
				FLATTT
						.getResourceString("ui.ConcernMapperPreferencePage.BoldEnabled"),
				getFieldEditorParent()));

		String[][] lRadioGroupValues = {
				{
						FLATTT
								.getResourceString("ui.ConcernMapperPreferencePage.Parent1"),
						"1" },
				{
						FLATTT
								.getResourceString("ui.ConcernMapperPreferencePage.Parent2"),
						"2" },
				{
						FLATTT
								.getResourceString("ui.ConcernMapperPreferencePage.Parent3"),
						"3" },
				{
						FLATTT
								.getResourceString("ui.ConcernMapperPreferencePage.Parent4"),
						"4" },
				{
						FLATTT
								.getResourceString("ui.ConcernMapperPreferencePage.Parent5"),
						"5" },
				{
						FLATTT
								.getResourceString("ui.ConcernMapperPreferencePage.Parent6"),
						"7" } };
		addField(new RadioGroupFieldEditor(
				ConcernViewPreferencePage.P_DECORATION_LIMIT,
				FLATTT
						.getResourceString("ui.ConcernMapperPreferencePage.ParentDecoration"),
				1, lRadioGroupValues, getFieldEditorParent()));
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 * @param pWorkbench
	 *            the workbench
	 */
	public void init(IWorkbench pWorkbench)
	{}

	/**
	 * A field editor for displaying labels not associated with other widgets.
	 */
	class LabelFieldEditor extends FieldEditor
	{
		private Label aLabel;

		/**
		 * All labels can use the same preference name since they don't store
		 * any preference.
		 * 
		 * @param pValue
		 *            The value for the label.
		 * @param pParent
		 *            The parent widget.
		 */
		public LabelFieldEditor(String pValue, Composite pParent)
		{
			super("label", pValue, pParent);
		}

		/**
		 * Adjusts the field editor to be displayed correctly for the given
		 * number of columns.
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
		 * @param pNumColumns
		 *            the number of columns
		 */
		@Override
		protected void adjustForNumColumns(int pNumColumns)
		{
			((GridData) aLabel.getLayoutData()).horizontalSpan = pNumColumns;
		}

		/**
		 * Fills the field editor's controls into the given parent.
		 * 
		 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite,
		 *      int)
		 * @param pParent
		 *            the composite used as a parent for the basic controls; the
		 *            parent's layout must be a <code>GridLayout</code>
		 * @param pNumColumns
		 *            the number of columns
		 * 
		 */
		@Override
		protected void doFillIntoGrid(Composite pParent, int pNumColumns)
		{
			aLabel = getLabelControl(pParent);

			GridData lGridData = new GridData();
			lGridData.horizontalSpan = pNumColumns;
			lGridData.horizontalAlignment = GridData.FILL;
			lGridData.grabExcessHorizontalSpace = false;
			lGridData.verticalAlignment = GridData.CENTER;
			lGridData.grabExcessVerticalSpace = false;

			aLabel.setLayoutData(lGridData);
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
		 * @return the number of controls
		 */
		@Override
		public int getNumberOfControls()
		{
			return 1;
		}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
		 */
		@Override
		protected void doLoad()
		{}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
		 */
		@Override
		protected void doLoadDefault()
		{}

		/**
		 * @see org.eclipse.jface.preference.FieldEditor#doStore()
		 */
		@Override
		protected void doStore()
		{}
	}
}
