/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.1 $
 */

package edu.wm.flat3.model;

/**
 * Interface describing objects interested in reacting to changes to the concern
 * model.
 */
public interface IConcernListener
{
	/**
	 * A signal that the concern model has changed.
	 * 
	 * @param pType
	 *            The type of change to the model. See the constants in
	 *            ConcernModel
	 */
	void modelChanged(ConcernEvent event);
}
