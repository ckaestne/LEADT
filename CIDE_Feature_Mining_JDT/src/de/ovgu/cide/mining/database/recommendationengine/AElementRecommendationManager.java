package de.ovgu.cide.mining.database.recommendationengine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.recommendationengine.graphrelation.GraphRelationElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.substrings.SubStringElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.substrings.SubStringFeatureRecommender;
import de.ovgu.cide.mining.database.recommendationengine.typechecking.TypeCheckElementRecommender;
import de.ovgu.cide.mining.events.AGenerateRecommendationsEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode;

//TODO: CACHE! RESULTS!
public class AElementRecommendationManager implements Observer {
	private ApplicationController AC;
	private Map<IFeature, Map<AIElement, ARecommendationContextCollection>>element2Recommendation; 
	
	private Set<AAbstractElementRecommender> elementRecommenders;
	private Set<AAbstractFeatureRecommender> featureRecommenders;
	

	
	public AElementRecommendationManager(ApplicationController AC, AElementColorManager elementColorManager) {
		this.AC = AC;
		element2Recommendation = new HashMap<IFeature, Map<AIElement,ARecommendationContextCollection>>();	
		
		elementRecommenders = new HashSet<AAbstractElementRecommender>();
		elementRecommenders.add(new TypeCheckElementRecommender());
		elementRecommenders.add(new GraphRelationElementRecommender());
		//recommenders.add(new SubStringElementRecommender());	
		
		featureRecommenders = new HashSet<AAbstractFeatureRecommender>();
		featureRecommenders.add(new SubStringFeatureRecommender());

		
		AC.addObserver(this);
	}
	
	public Map<IFeature,  ARecommendationContextCollection> getAllRecommendations(AIElement element) {
		Map<IFeature,  ARecommendationContextCollection>  result = new HashMap<IFeature, ARecommendationContextCollection>();	
	
		for (IFeature color : element2Recommendation.keySet()) {
			Map<AIElement, ARecommendationContextCollection> recommendations = element2Recommendation.get(color);
			
			if (recommendations == null || recommendations.size() == 0)
				continue;
		
			if (!recommendations.containsKey(element))
				continue;
			
			result.put(color, recommendations.get(element));
			
		}
		return result;
	}
	
	public Map<AIElement, ARecommendationContextCollection> getRecommendations(IFeature color, AIElement element) { 
		Map<AIElement,ARecommendationContextCollection> colorRecommendations = element2Recommendation.get(color);
		
		if (colorRecommendations == null)
			return new HashMap<AIElement, ARecommendationContextCollection>();
		
		Map<AIElement, ARecommendationContextCollection> resultRecommendations = new HashMap<AIElement, ARecommendationContextCollection>();
		
		for (AIElement recElement : colorRecommendations.keySet()) {
			ARecommendationContextCollection collection = colorRecommendations.get(recElement);
			for (ARecommendationContext context : collection.getContexts()) {
				if (!element.equals(context.getSupporter()))
					continue;
				
				resultRecommendations.put(recElement, collection);
				break;
			}  
		}
		
		return resultRecommendations;
	}
	
	public int getRecommendationsCount(IFeature color, AIElement element) {
		return getRecommendations(color, element).size();
	}
	

	
	public Map<AIElement, ARecommendationContextCollection> getRecommendations(IFeature color, int start, int end, int cuhash) {
		
		Map<AIElement, ARecommendationContextCollection>  recommendations = new HashMap<AIElement, ARecommendationContextCollection>();
		
		
		if (ApplicationController.CHECK_COLOR_RELATIONS) {
			Map<AIElement,ARecommendationContextCollection> colorRecommendations = element2Recommendation.get(color);
			if (colorRecommendations != null && colorRecommendations.size() > 0) {
				recommendations = colorRecommendations;
			}
		}
		
		Set<AIElement> elements = AC.getElementsOfColor(color);
		
		for (AIElement tmpElement : elements) {
			
			if (cuhash != -1 && tmpElement.getCompelationUnitHash() != cuhash)
				continue;
			
			if (start > -1 && tmpElement.getStartPosition() < start)
				continue;
			
			if (end > -1 && (tmpElement.getStartPosition() + tmpElement.getLength()) > end)
				continue;
				
			Map<AIElement, ARecommendationContextCollection> tmpRecommendations =  getRecommendations(color, tmpElement);
			
			if (tmpRecommendations == null)
				continue;
			
			mergeRecommendations(tmpRecommendations, recommendations);	
			
		}
			
			
		return recommendations;
	}
		
	public int getRecommendationsCount(IFeature color, int start, int end, int cuhash) {
		return getRecommendations(color, start, end, cuhash).size();
	}
	
	private void generateRecommendations() {
		
		element2Recommendation = new HashMap<IFeature, Map<AIElement,ARecommendationContextCollection>>();	
		
		for (IFeature color : AC.getProjectFeatures()) {
			
			//RESET RECOMMENDATIONS
			Map<AIElement, ARecommendationContextCollection>  recommendations = new HashMap<AIElement, ARecommendationContextCollection>();
			element2Recommendation.put(color, recommendations);
		
			
			//RECOMMENDATION BASED ON LOCAL ELEMENT DATA
			Set<AIElement> elements = AC.getElementsOfColor(color);
			
			if (ApplicationController.CHECK_COLOR_RELATIONS) {
				
				Set<AIElement> tmpElements = new HashSet<AIElement>();
				tmpElements.addAll(elements);
				
			
				//ADD ELEMENTS OF RELATED COLORS
				for (IFeature relatedColor : AC.getRelatedColors(color)) {
					tmpElements.addAll(AC.getElementsOfColor(relatedColor));
				} 
				elements = tmpElements;
				
			}
			
				
			//generate recommendations for all elements	
			for (AIElement element : elements) {
				
				//recommend elements according to recommendation type
				for (AAbstractElementRecommender recommender : elementRecommenders) {
					
					Map<AIElement, ARecommendationContext> tmpRecommendations = recommender.getRecommendations(element, color);
					addRecommendations(tmpRecommendations, recommendations);	
				
				}				
				
			} 
			
			
			//RECOMMENDATION BASED ON GLOBAL FEATURE DATA
			//recommend elements according to recommendation type
			for (AAbstractFeatureRecommender recommender : featureRecommenders) {
				Map<AIElement, ARecommendationContext> tmpRecommendations = recommender.getRecommendations(color);
				addRecommendations(tmpRecommendations, recommendations);	
			}	
	
			
		}
		
			
	}
	
	private void mergeRecommendations(Map<AIElement, ARecommendationContextCollection> newRecommendations, Map<AIElement, ARecommendationContextCollection> oldRecommendations) {
		for (AIElement tmpRecElement : newRecommendations.keySet()) {
			
			ARecommendationContextCollection oldCollection = oldRecommendations.get(tmpRecElement);
			
			if (oldCollection == null) {
				oldRecommendations.put(tmpRecElement, newRecommendations.get(tmpRecElement));
			}

		}
	}

	private void addRecommendations(Map<AIElement, ARecommendationContext> newRecommendations, Map<AIElement, ARecommendationContextCollection> oldRecommendations) {
		
		for (AIElement tmpRecElement : newRecommendations.keySet()) {
			
			
			ARecommendationContextCollection collection = oldRecommendations.get(tmpRecElement);
			
			if (collection == null) {
				collection = new ARecommendationContextCollection();
				oldRecommendations.put(tmpRecElement, collection);
			}
			
			//add the new context
			collection.addContext(newRecommendations.get(tmpRecElement));
			
		}
	}
	
	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {
			
			if (arg instanceof AGenerateRecommendationsEvent) {
				generateRecommendations();			
			} 
	
			
		}
		
	}
}
