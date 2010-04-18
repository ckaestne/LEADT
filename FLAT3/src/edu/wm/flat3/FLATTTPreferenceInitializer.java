/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import edu.wm.flat3.ui.ConcernViewPreferencePage;

/**
 * Initializes the default preferences for the plug-in.
 */
public class FLATTTPreferenceInitializer extends
		AbstractPreferenceInitializer
{
	/**
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		final IEclipsePreferences lNode = new DefaultScope()
				.getNode(FLATTT.ID_PLUGIN);

		lNode
				.put(
						ConcernViewPreferencePage.P_BOLD_ENABLED,
						FLATTT
								.getResourceString("ConcernMapperPreferenceInitializer.DefaultBoldEnabled"));
		lNode
				.put(
						ConcernViewPreferencePage.P_SUFFIX_ENABLED,
						FLATTT
								.getResourceString("ConcernMapperPreferenceInitializer.DefaultSuffixEnabled"));
		lNode
				.put(
						ConcernViewPreferencePage.P_FILTER_ENABLED,
						FLATTT
								.getResourceString("ConcernMapperPreferenceInitializer.DefaultFilterEnabled"));
		lNode
				.put(
						ConcernViewPreferencePage.P_DECORATION_LIMIT,
						FLATTT
								.getResourceString("ConcernMapperPreferenceInitializer.DefaultDecorationLimit"));
	}
}
