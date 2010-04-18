package de.ovgu.cide.mining.events;

import java.util.EventObject;

import de.ovgu.cide.mining.database.model.AIElement;
  
public class AElementPreviewEvent extends EventObject {
	
//	public AIElement getS() {
//		return element;
//	}

	public AElementPreviewEvent(Object source) {
		super(source);
	}
	
}
