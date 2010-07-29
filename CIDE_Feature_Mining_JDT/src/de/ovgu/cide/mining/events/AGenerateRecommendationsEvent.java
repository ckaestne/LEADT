package de.ovgu.cide.mining.events;

import java.util.EventObject;
import java.util.Map;
import java.util.Set;

import cide.gast.IASTNode;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.model.AElement;

public class AGenerateRecommendationsEvent extends EventObject {


	private static final long serialVersionUID = 1L;


	public AGenerateRecommendationsEvent(Object source) {
		super(source);
	
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}



}
