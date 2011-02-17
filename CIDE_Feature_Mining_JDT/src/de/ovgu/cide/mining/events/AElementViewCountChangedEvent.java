package de.ovgu.cide.mining.events;

import java.util.EventObject;

import de.ovgu.cide.mining.database.model.AElement;

public class AElementViewCountChangedEvent extends EventObject {
	final AElement element;
	final Object previewSource;

	public AElement getElement() {
		return element;
	}

	public AElementViewCountChangedEvent(Object source, AElement element,
			Object previewSource) {
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
