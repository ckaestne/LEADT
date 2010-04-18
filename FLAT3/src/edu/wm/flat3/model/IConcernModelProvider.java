package edu.wm.flat3.model;

import edu.wm.flat3.repository.EdgeKind;

public interface IConcernModelProvider
{
	ConcernModel getModel();
	EdgeKind getLinkType();
}
