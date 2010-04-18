/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.6 $
 */

package de.ovgu.cide.mining.database.model;

/**
 * The various categories of elements supported by the
 * Concern Graph model for the Java language.
 */
public enum AICategories
{
	TYPE,
	FIELD,
	METHOD,
	LOCAL_VARIABLE,
	IMPORT,
	COMPILATION_UNIT,
	TYPE_ACCESS,
	FIELD_ACCESS,
	LOCAL_VARIABLE_ACCESS,
	PARAMETER_ACCESS,
	METHOD_ACCESS,
	OUT_OF_CONTEXT,
	FEATURE
}

