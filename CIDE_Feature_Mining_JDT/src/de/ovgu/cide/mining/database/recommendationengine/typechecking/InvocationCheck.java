package de.ovgu.cide.mining.database.recommendationengine.typechecking;

import org.eclipse.jdt.core.dom.IMethodBinding;

import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.typing.jdt.BindingProjectColorCache;
import de.ovgu.cide.typing.model.IEvaluationStrategy;

public class InvocationCheck extends AbstractTypingCheck {
	private AIElement bodySource;
	private AIElement bodyTarget;
	
	
	public InvocationCheck(AIElement paramSource, AIElement paramTarget, AIElement bodySource,  AIElement bodyTarget,  IFeatureModel model) {
		super(paramSource, paramTarget, model);
		this.bodySource = bodySource;
		this.bodyTarget = bodyTarget;
	
	}
	

	
	public boolean evaluate(IEvaluationStrategy strategy) {
		ApplicationController jayFX = ApplicationController.getInstance();
		
		if (!strategy.implies(getFeatureModel(), jayFX.getElementColors(bodyTarget), jayFX.getElementColors(bodySource))) {
			return false;
		}
		
		if (strategy.equal(getFeatureModel(), jayFX.getElementColors(targetElement), jayFX.getElementColors(sourceElement)))
			return true;
		
		if (!strategy.implies(getFeatureModel(), jayFX.getElementColors(bodySource), jayFX.getElementColors(bodyTarget))) {
			
			if (strategy.equal(getFeatureModel(), jayFX.getElementColors(targetElement), jayFX.getElementColors(bodyTarget)))
				return true;
			
		}
	
		return false;
			
	}

	public String getErrorMessage() {
		return "Requirement not present";
	}

	public String getProblemType() {
		return "de.ovgu.cide.typing.jdt.requirement";
	}

}
