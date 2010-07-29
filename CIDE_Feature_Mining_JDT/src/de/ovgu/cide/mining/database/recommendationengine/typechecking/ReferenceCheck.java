package de.ovgu.cide.mining.database.recommendationengine.typechecking;

import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.typing.model.IEvaluationStrategy;

public class ReferenceCheck extends AbstractTypingCheck {

	public ReferenceCheck(AElement sourceElement, AElement targetElement, IFeatureModel model) {
		super(sourceElement, targetElement, model);
	}

	public boolean evaluate(IEvaluationStrategy strategy) {
		ApplicationController jayFX = ApplicationController.getInstance();
		
//		System.out.println("::: CHECK :::" );
//		System.out.println(" ==> MODEL:" + getFeatureModel() );
//		System.out.println(" ==> SOURCE:" + jayFX.getElementColors(getSourceElement()) );
//		System.out.println(" ==> TARGET:" + jayFX.getElementColors(targetElement));
//		System.out.println(" ===> VAR1: " + strategy.implies(getFeatureModel(), jayFX.getElementColors(getSourceElement()), jayFX.getElementColors(targetElement)));
//		System.out.println(" ===> VAR2 - X: " + strategy.implies(getFeatureModel(),  jayFX.getElementColors(targetElement), jayFX.getElementColors(getSourceElement())));
		
		
		return strategy.implies(getFeatureModel(), jayFX.getElementColors(targetElement), jayFX.getElementColors(sourceElement));
	}

	public String getErrorMessage() {
		return "Access not present";
	}

	public String getProblemType() {
		return "de.ovgu.cide.typing.jdt.reference";
	}

}
