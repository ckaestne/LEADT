package de.ovgu.cide.mining.database.recommendationengine.typechecking;

import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.model.AElement;

public abstract class AbstractTypingCheck implements IElementTypingCheck {
	protected final AElement sourceElement;
	protected final AElement targetElement;

	IFeatureModel model;

	public AbstractTypingCheck(AElement sourceElement, AElement targetElement,
			IFeatureModel model) {
		this.sourceElement = sourceElement;
		this.targetElement = targetElement;

		this.model = model;
	}

	public AElement getSourceElement() {
		return sourceElement;
	}

	public AElement getTargetElement() {
		return targetElement;
	}

	public IFeatureModel getFeatureModel() {
		return model;
	}

	public Severity getSeverity() {
		return Severity.ERROR;
	}

}