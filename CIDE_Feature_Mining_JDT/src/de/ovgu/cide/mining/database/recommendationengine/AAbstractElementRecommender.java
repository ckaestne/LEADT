package de.ovgu.cide.mining.database.recommendationengine;

import java.util.HashMap;
import java.util.Map;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AIElement;

public abstract class AAbstractElementRecommender {
	
	protected ApplicationController AC;

	public AAbstractElementRecommender() {
		AC = ApplicationController.getInstance();
	}
	
	protected boolean isValidRecommendation(AIElement element, IFeature color) {
		if (AC.getElementColors(element).contains(color))
			return false;
		
		if (AC.getElementNonColors(element).contains(color))
			return false;
	
		if (ApplicationController.CHECK_COLOR_RELATIONS) {
			for (IFeature relNonColor : AC.getRelatedNonColors(color)) {
				if (AC.getElementNonColors(element).contains(relNonColor))
					return false;	
			} 
			
			for (IFeature relColor : AC.getRelatedColors(color)) {
				if (AC.getElementColors(element).contains(relColor))
					return false;	
			} 
		}
		
		return true;
	}
	
	protected boolean isInColor(AIElement element, IFeature color) {
		if (AC.getElementColors(element).contains(color))
			return true;
		
	
		if (ApplicationController.CHECK_COLOR_RELATIONS) {
			
			for (IFeature relColor : AC.getRelatedColors(color)) {
				if (AC.getElementColors(element).contains(relColor))
					return true;	
			} 
		}
		
		return false;
	}
	
	protected boolean isInNonColor(AIElement element, IFeature color) {
		
		if (AC.getElementNonColors(element).contains(color))
			return true;
	
		if (ApplicationController.CHECK_COLOR_RELATIONS) {
			for (IFeature relNonColor : AC.getRelatedNonColors(color)) {
				if (AC.getElementNonColors(element).contains(relNonColor))
					return true;	
			} 
		}
		
		return false;
	}
	
	protected Map<AIElement, ARecommendationContext>  filterValidRecommendations(IFeature color, Map<AIElement, ARecommendationContext> recommendations) {
		Map<AIElement, ARecommendationContext>  actualRecom = new HashMap<AIElement, ARecommendationContext>();
		
			if (recommendations == null)
				return actualRecom;	
			
			for (AIElement curElement : recommendations.keySet()) {
				if (isValidRecommendation(curElement, color))
					actualRecom.put(curElement,recommendations.get(curElement));
			}
			
			return actualRecom;
		
	}
	
//	protected Map<AIElement, ARecommendationContext>  filterForColor(IFeature color, Map<AIElement, ARecommendationContext> recommendations) {
//	Map<AIElement, ARecommendationContext>  actualRecom = new HashMap<AIElement, ARecommendationContext>();
//	
//		if (recommendations == null)
//			return actualRecom;	
//		
//		for (AIElement curElement : recommendations.keySet()) {
//			if (!jayFX.getElementColors(curElement).contains(color))
//				actualRecom.put(curElement,recommendations.get(curElement));
//		}
//		
//		return actualRecom;
//	
//	}
//	
//	protected Map<AIElement, ARecommendationContext>  filterForNonColor(IFeature color, Map<AIElement, ARecommendationContext> recommendations) {
//		Map<AIElement, ARecommendationContext>  actualRecom = new HashMap<AIElement, ARecommendationContext>();
//		
//		if (recommendations == null)
//			return actualRecom;	
//		
//		for (AIElement curElement : recommendations.keySet()) {
//			if (!jayFX.getElementNonColors(curElement).contains(color))
//				actualRecom.put(curElement,recommendations.get(curElement));
//		}
//		
//		return actualRecom;
//	}	
	
	public abstract String getRecommendationType();
	
	public abstract Map<AIElement, ARecommendationContext> getRecommendations(AIElement element, IFeature color);
}
