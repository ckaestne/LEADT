package de.ovgu.cide.mining.database.recommendationengine.graphrelation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IFontDecorator;

import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.model.ARelation;
import de.ovgu.cide.mining.database.recommendationengine.AAbstractElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode.NODE_KIND;
import de.ovgu.cide.typing.internal.manager.EvaluationStrategyManager;
import de.ovgu.cide.typing.model.IEvaluationStrategy;


public class GraphRelationElementRecommender extends AAbstractElementRecommender {
	
	
	public GraphRelationElementRecommender() {
		super();

	}
	
	public Map<AIElement, ARecommendationContext> getRecommendations(AIElement element, IFeature color) {
		

		Map<AIElement, ARecommendationContext> recommendations = new HashMap<AIElement, ARecommendationContext>();
		
		Set<ARelation> validTransponseRelations = ARelation.getAllRelations(element.getCategory(), true, false);
		for (AICategories cat : element.getSubCategories()) {
			validTransponseRelations.addAll(ARelation.getAllRelations(cat, true, false));
		}
		
		//check all relations
		for (ARelation tmpTransRelation : validTransponseRelations) {
			try {
				
				//get the forward elements
				Set<AIElement> forwardElements = AC.getRange(element, tmpTransRelation);
				Set<AIElement> validRecommendationElements = new HashSet<AIElement>();
				
				
				//check how much of them already in color
				//int validRecommendationCount = 0;
				for (AIElement forwardElement : forwardElements) {
					
					if (isValidRecommendation(forwardElement, color))
						validRecommendationElements.add(forwardElement);
				}
				
				//if they are all already in color or marked as not color elements, skip to next relation
				if (validRecommendationElements.size() == 0)
					continue;
				
				int invalidForwardRecommendations = forwardElements.size() - validRecommendationElements.size();
								
				for (AIElement validForwardElement : validRecommendationElements) {
						
					//get backward elements for transpose 
					Set<AIElement> backwardElements = AC.getRange(validForwardElement, tmpTransRelation.getInverseRelation());

					//calc how much of backward is already in color
					int invalidBackwardRecommendations = 0;
					for (AIElement backwardElement : backwardElements) {
						if (!isValidRecommendation(backwardElement, color))
							invalidBackwardRecommendations++;
					}
					
					//calc the degree
					double degree = ((double)(1 + invalidForwardRecommendations) / (double)forwardElements.size()) * ((double)invalidBackwardRecommendations / (double)backwardElements.size());
					
					
					//add / merge recommendation with alreay available ones
					ARecommendationContext newContext = new ARecommendationContext(element, tmpTransRelation.getName(),degree);
					ARecommendationContext oldContext = recommendations.get(validForwardElement);
				
					if (oldContext != null) {
						newContext = new ARecommendationContext(newContext, oldContext);
					}
					recommendations.put(validForwardElement, newContext);
	
				}

				
			} catch (Exception e) {
				
			}
		}
		
		
		return recommendations;
	}
	

}
