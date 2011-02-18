package de.ovgu.cide.mining.database.recommendationengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Map.Entry;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.recommendationengine.graphrelation.GraphRelationElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.substrings.SubStringFeatureRecommender;
import de.ovgu.cide.mining.database.recommendationengine.typechecking.TypeCheckElementRecommender;
import de.ovgu.cide.mining.events.AGenerateRecommendationsEvent;

//TODO: CACHE! RESULTS!
public class AElementRecommendationManager implements Observer {
	private ApplicationController AC;
	private Map<IFeature, Map<AElement, ARecommendationContextCollection>> element2Recommendation;

	private Set<AAbstractElementRecommender> elementRecommenders;
	private Set<AAbstractFeatureRecommender> featureRecommenders;

	public AElementRecommendationManager(ApplicationController AC,
			AElementColorManager elementColorManager) {
		this.AC = AC;
		element2Recommendation = new HashMap<IFeature, Map<AElement, ARecommendationContextCollection>>();

		elementRecommenders = new HashSet<AAbstractElementRecommender>();
		elementRecommenders.add(new TypeCheckElementRecommender());
		elementRecommenders.add(new GraphRelationElementRecommender());
		// recommenders.add(new SubStringElementRecommender());

		featureRecommenders = new HashSet<AAbstractFeatureRecommender>();
		featureRecommenders.add(new SubStringFeatureRecommender());

		AC.addObserver(this);
	}

	public Map<IFeature, ARecommendationContextCollection> getAllRecommendations(
			AElement element) {
		Map<IFeature, ARecommendationContextCollection> result = new HashMap<IFeature, ARecommendationContextCollection>();

		for (IFeature color : element2Recommendation.keySet()) {
			Map<AElement, ARecommendationContextCollection> recommendations = element2Recommendation
					.get(color);

			if (recommendations == null || recommendations.size() == 0)
				continue;

			if (!recommendations.containsKey(element))
				continue;

			result.put(color, recommendations.get(element));

		}
		return result;
	}

	public Map<AElement, ARecommendationContextCollection> getRecommendations(
			IFeature color, AElement element) {
		Map<AElement, ARecommendationContextCollection> colorRecommendations = element2Recommendation
				.get(color);

		if (colorRecommendations == null)
			return new HashMap<AElement, ARecommendationContextCollection>();
		colorRecommendations = new HashMap<AElement, ARecommendationContextCollection>(
				colorRecommendations);

		Map<AElement, ARecommendationContextCollection> resultRecommendations = new HashMap<AElement, ARecommendationContextCollection>();

		for (Entry<AElement, ARecommendationContextCollection> entry : colorRecommendations
				.entrySet()) {
			AElement recElement = entry.getKey();
			ARecommendationContextCollection collection = entry.getValue();
			for (ARecommendationContext context : collection.getContexts()) {
				if (!element.equals(context.getSupporter()))
					continue;

				resultRecommendations.put(recElement, collection);
				break;
			}
		}

		return resultRecommendations;
	}

	public int getRecommendationsCount(IFeature color, AElement element) {
		return getRecommendations(color, element).size();
	}

	public Map<AElement, ARecommendationContextCollection> getRecommendations(
			IFeature color) {
		return getRecommendations(color, -1, -1, -1);
	}

	public Map<AElement, ARecommendationContextCollection> getRecommendations(
			IFeature color, int start, int end, int cuhash) {

		Map<AElement, ARecommendationContextCollection> recommendations = new HashMap<AElement, ARecommendationContextCollection>();

		if (ApplicationController.CHECK_COLOR_RELATIONS) {
			Map<AElement, ARecommendationContextCollection> colorRecommendations = element2Recommendation
					.get(color);
			if (colorRecommendations != null && colorRecommendations.size() > 0) {
				recommendations = colorRecommendations;
			}
		}

		Set<AElement> elements = AC.getElementsOfColor(color);

		for (AElement tmpElement : new ArrayList<AElement>(elements)) {

			if (cuhash != -1 && tmpElement.getCompelationUnitHash() != cuhash)
				continue;

			if (start > -1 && tmpElement.getStartPosition() < start)
				continue;

			if (end > -1
					&& (tmpElement.getStartPosition() + tmpElement.getLength()) > end)
				continue;

			Map<AElement, ARecommendationContextCollection> tmpRecommendations = getRecommendations(
					color, tmpElement);

			if (tmpRecommendations == null)
				continue;

			mergeRecommendations(tmpRecommendations, recommendations);

		}

		return recommendations;
	}

	public int getRecommendationsCount(IFeature color, int start, int end,
			int cuhash) {
		return getRecommendations(color, start, end, cuhash).size();
	}

	private void generateRecommendations() {

		element2Recommendation = new HashMap<IFeature, Map<AElement, ARecommendationContextCollection>>();

		for (IFeature color : AC.getProjectFeatures()) {

			// RESET RECOMMENDATIONS
			Map<AElement, ARecommendationContextCollection> recommendations = new HashMap<AElement, ARecommendationContextCollection>();
			element2Recommendation.put(color, recommendations);

			// RECOMMENDATION BASED ON LOCAL ELEMENT DATA
			Set<AElement> elements = AC.getElementsOfColor(color);

			if (ApplicationController.CHECK_COLOR_RELATIONS) {

				Set<AElement> tmpElements = new HashSet<AElement>();
				tmpElements.addAll(elements);

				// ADD ELEMENTS OF RELATED COLORS
				for (IFeature relatedColor : AC.getRelatedColors(color)) {
					tmpElements.addAll(AC.getElementsOfColor(relatedColor));
				}
				elements = tmpElements;

			}

			// generate recommendations for all elements
			for (AElement element : elements) {

				// recommend elements according to recommendation type
				for (AAbstractElementRecommender recommender : elementRecommenders) {

					Map<AElement, ARecommendationContext> tmpRecommendations = recommender
							.getRecommendations(element, color);
					addRecommendations(tmpRecommendations, recommendations);

				}

			}

			// RECOMMENDATION BASED ON GLOBAL FEATURE DATA
			// recommend elements according to recommendation type
			for (AAbstractFeatureRecommender recommender : featureRecommenders) {
				Map<AElement, ARecommendationContext> tmpRecommendations = recommender
						.getRecommendations(color);
				addRecommendations(tmpRecommendations, recommendations);
			}

		}

	}

	private void mergeRecommendations(
			Map<AElement, ARecommendationContextCollection> newRecommendations,
			Map<AElement, ARecommendationContextCollection> oldRecommendations) {
		for (AElement tmpRecElement : newRecommendations.keySet()) {

			ARecommendationContextCollection oldCollection = oldRecommendations
					.get(tmpRecElement);

			if (oldCollection == null) {
				oldRecommendations.put(tmpRecElement, newRecommendations
						.get(tmpRecElement));
			}

		}
	}

	private void addRecommendations(
			Map<AElement, ARecommendationContext> newRecommendations,
			Map<AElement, ARecommendationContextCollection> oldRecommendations) {

		for (AElement tmpRecElement : newRecommendations.keySet()) {

			ARecommendationContextCollection collection = oldRecommendations
					.get(tmpRecElement);

			if (collection == null) {
				collection = new ARecommendationContextCollection();
				oldRecommendations.put(tmpRecElement, collection);
			}

			// add the new context
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
	
	public void __script_updateRecommendations() {
		generateRecommendations();
	}
}
