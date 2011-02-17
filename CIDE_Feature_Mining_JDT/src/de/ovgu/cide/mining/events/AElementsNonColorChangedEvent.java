package de.ovgu.cide.mining.events;

import java.util.EventObject;
import java.util.Map;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.model.AElement;

public class AElementsNonColorChangedEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private Map<AElement, IFeature> addedElements;
	private Map<AElement, IFeature> removedElements;

	public AElementsNonColorChangedEvent(Object source,
			Map<AElement, IFeature> addedElements,
			Map<AElement, IFeature> removedElements) {
		super(source);
		this.addedElements = addedElements;
		this.removedElements = removedElements;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Map<AElement, IFeature> getAddedElements() {
		return addedElements;
	}

	public Map<AElement, IFeature> getRemovedElements() {
		return removedElements;
	}

}
