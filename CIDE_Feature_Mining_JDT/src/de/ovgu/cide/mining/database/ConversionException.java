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
 * Represents problems converting a element from the model to the Eclipse
 * workspace, and vice versa.
 */
public class ConversionException extends Exception {
	/**
	 * Constructor for ConversionException.
	 */
	public ConversionException() {
		super();
	}

	/**
	 * Constructor for ConversionException.
	 * 
	 * @param arg0
	 */
	public ConversionException(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor for ConversionException.
	 * 
	 * @param arg0
	 * @param arg1
	 */
	public ConversionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * Constructor for ConversionException.
	 * 
	 * @param arg0
	 */
	public ConversionException(Throwable arg0) {
		super(arg0);
	}
}
