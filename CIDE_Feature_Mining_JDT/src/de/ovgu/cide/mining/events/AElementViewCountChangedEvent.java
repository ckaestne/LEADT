package de.ovgu.cide.mining.events;

import java.util.EventObject;

import de.ovgu.cide.mining.database.model.AIElement;
  
public class AElementViewCountChangedEvent extends EventObject {
	final AIElement element;
	final Object previewSource;
	
	public AIElement getElement() {
		return element;
	}

	public AElementViewCountChangedEvent(Object source, AIElement element, Object previewSource ) {
		super(source);
		this.element = element;
		this.previewSource = previewSource;
	}
	
	public Object getPreviewSource() {
		return previewSource;
	}
	
	public boolean isPreviewMode() {
		return previewSource != null;
	}
	
}
