package de.ovgu.cide.mining.events;

import java.util.EventObject;

public class AGenerateRecommendationsEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public AGenerateRecommendationsEvent(Object source) {
		super(source);

	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
