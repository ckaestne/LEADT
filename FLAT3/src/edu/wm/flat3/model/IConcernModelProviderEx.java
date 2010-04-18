package edu.wm.flat3.model;

import edu.wm.flat3.repository.EdgeKind;

public interface IConcernModelProviderEx extends IConcernModelProvider
{
	void setConcernDomain(String concernDomain);
	void setLinkType(EdgeKind linkType);
}
