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
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.ARelationKind;
import de.ovgu.cide.mining.database.recommendationengine.AAbstractElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode.NODE_KIND;
import de.ovgu.cide.typing.internal.manager.EvaluationStrategyManager;
import de.ovgu.cide.typing.model.IEvaluationStrategy;


public class CopyOfGraphRelationElementRecommender extends AAbstractElementRecommender {
	
	
	public CopyOfGraphRelationElementRecommender() {
		super();

	}
	
	public Map<AElement, ARecommendationContext> getRecommendations(AElement element, IFeature color) {
		

		Map<AElement, ARecommendationContext> recommendations = new HashMap<AElement, ARecommendationContext>();
		
		Set<ARelationKind> validTransponseRelations = ARelationKind.getAllRelations(element.getCategory(), true, false);
		for (AICategories cat : element.getSubCategories()) {
			validTransponseRelations.addAll(ARelationKind.getAllRelations(cat, true, false));
		}
		
		//check all relations
		for (ARelationKind tmpTransRelation : validTransponseRelations) {
			try {
				
				//get the forward elements
				Set<AElement> forwardElements = AC.getRange(element, tmpTransRelation);
				Set<AElement> validRecommendationElements = new HashSet<AElement>();
				
				
				//check how much of them already in color
				//int validRecommendationCount = 0;
				for (AElement forwardElement : forwardElements) {
					
					if (isValidRecommendation(forwardElement, color))
						validRecommendationElements.add(forwardElement);
				}
				
				//if they are all already in color or marked as not color elements, skip to next relation
				if (validRecommendationElements.size() == 0)
					continue;
				
				int invalidForwardRecommendations = forwardElements.size() - validRecommendationElements.size();
								
				for (AElement validForwardElement : validRecommendationElements) {
						
					//get backward elements for transpose 
					Set<AElement> backwardElements = AC.getRange(validForwardElement, tmpTransRelation.getInverseRelation());

					//calc how much of backward is already in color
					int invalidBackwardRecommendations = 0;
					for (AElement backwardElement : backwardElements) {
						if (!isValidRecommendation(backwardElement, color))
							invalidBackwardRecommendations++;
					}
					
					//calc the degree
					double degree = ((double)(1 + invalidForwardRecommendations) / (double)forwardElements.size()) * ((double)invalidBackwardRecommendations / (double)backwardElements.size());
					
					
					//add / merge recommendation with alreay available ones
					ARecommendationContext newContext = new ARecommendationContext(element, tmpTransRelation.getName(), getRecommendationType(), degree);
					ARecommendationContext oldContext = recommendations.get(validForwardElement);
				
					if (oldContext != null) {
						newContext = new ARecommendationContext(newContext, oldContext, getRecommendationType());
					}
					recommendations.put(validForwardElement, newContext);
	
				}

				
			} catch (Exception e) {
				
			}
		}
		
		
		return recommendations;
	}
	@Override
	public String getRecommendationType() {
		return "GR";
	}
	

}
