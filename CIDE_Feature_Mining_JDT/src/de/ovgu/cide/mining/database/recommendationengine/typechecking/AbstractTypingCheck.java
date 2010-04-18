package de.ovgu.cide.mining.database.recommendationengine.typechecking;

import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.model.AIElement;

public abstract class AbstractTypingCheck implements IElementTypingCheck {
	protected final AIElement sourceElement;
	protected final AIElement targetElement;
	
	

	IFeatureModel model;

	public AbstractTypingCheck(AIElement sourceElement, AIElement targetElement, IFeatureModel model) {
		this.sourceElement = sourceElement;
		this.targetElement = targetElement;
		
		
		this.model = model;
	}

	public AIElement getSourceElement() {
		return sourceElement;
	}
	public AIElement getTargetElement() {
		return targetElement;
	}
	
	public IFeatureModel getFeatureModel() {
		return model;
	}

	public Severity getSeverity() {
		return Severity.ERROR;
	}

	
}