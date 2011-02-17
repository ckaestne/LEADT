/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.3 $
 */

package de.ovgu.cide.mining.database;

/**
 * Element never been added to the database.
 */
public class ElementNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	public ElementNotFoundException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public ElementNotFoundException(String arg0) {
		super(arg0);

	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ElementNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);

	}

	/**
	 * @param arg0
	 */
	public ElementNotFoundException(Throwable arg0) {
		super(arg0);

	}

}
