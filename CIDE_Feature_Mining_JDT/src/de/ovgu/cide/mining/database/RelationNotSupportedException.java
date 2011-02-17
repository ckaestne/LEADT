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

public class RelationNotSupportedException extends RuntimeException {
	public RelationNotSupportedException(String pMessage) {
		super(pMessage);
	}

	public RelationNotSupportedException(String pMessage, Throwable pException) {
		super(pMessage, pException);
	}

	public RelationNotSupportedException(Throwable pException) {
		super(pException);
	}
}
