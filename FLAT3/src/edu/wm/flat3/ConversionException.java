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

/**
 * A problem with the conversion of Java elements into unique string IDs.
 */
public class ConversionException extends Exception
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new ConversionException.
	 * 
	 * @param pMessage
	 *            A message for the exception.
	 */
	public ConversionException(String pMessage)
	{
		super(pMessage);
	}

	/**
	 * Creates a new ConversionException.
	 * 
	 * @param pMessage
	 *            A message for the exception.
	 * @param pException
	 *            A nested exception.
	 */
	public ConversionException(String pMessage, Throwable pException)
	{
		super(pMessage, pException);
	}

	/**
	 * Creates a new ConversionException.
	 * 
	 * @param pException
	 *            A nested exception.
	 */
	public ConversionException(Throwable pException)
	{
		super(pException);
	}
}
