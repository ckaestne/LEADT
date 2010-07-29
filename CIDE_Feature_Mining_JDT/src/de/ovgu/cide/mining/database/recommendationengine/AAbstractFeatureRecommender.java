package de.ovgu.cide.mining.database.recommendationengine;

import java.util.HashMap;
import java.util.Map;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;

public abstract class AAbstractFeatureRecommender {
	
	protected ApplicationController AC;

	public AAbstractFeatureRecommender() {
		AC = ApplicationController.getInstance();
	}
	
	protected boolean isValidRecommendation(AElement element, IFeature color) {
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
	
	
//	protected Map<AIElement, ARecommendationContext>  filterValidRecommendations(IFeature color, Map<AIElement, ARecommendationContext> recommendations) {
//		Map<AIElement, ARecommendationContext>  actualRecom = new HashMap<AIElement, ARecommendationContext>();
//		
//			if (recommendations == null)
//				return actualRecom;	
//			
//			for (AIElement curElement : recommendations.keySet()) {
//				if (isValidRecommendation(curElement, color))
//					actualRecom.put(curElement,recommendations.get(curElement));
//			}
//			
//			return actualRecom;
//		
//	}
	
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
	public abstract Map<AElement, ARecommendationContext> getRecommendations(IFeature color);
}
