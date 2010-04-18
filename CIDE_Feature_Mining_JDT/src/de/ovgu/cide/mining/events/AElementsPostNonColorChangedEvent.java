package de.ovgu.cide.mining.events;

import java.util.EventObject;
import java.util.Map;
import java.util.Set;

import cide.gast.IASTNode;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.model.AIElement;

public class AElementsPostNonColorChangedEvent extends EventObject {


	private static final long serialVersionUID = 1L;

	private Map<AIElement, IFeature> addedElements;
	private Map<AIElement, IFeature> removedElements;

	public AElementsPostNonColorChangedEvent(Object source, Map<AIElement, IFeature> addedElements, Map<AIElement, IFeature> removedElements) {
		super(source);
		this.addedElements = addedElements;
		this.removedElements = removedElements;
	}

	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Map<AIElement, IFeature> getAddedElements() {
		return addedElements;
	}

	public Map<AIElement, IFeature> getRemovedElements() {
		return removedElements;
	}


}
