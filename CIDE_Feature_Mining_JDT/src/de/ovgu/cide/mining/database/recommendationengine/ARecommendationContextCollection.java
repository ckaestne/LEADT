package de.ovgu.cide.mining.database.recommendationengine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ARecommendationContextCollection {

	private Set<ARecommendationContext> contexts;

	public ARecommendationContextCollection() {
		contexts = new HashSet<ARecommendationContext>();
	}

	public void addContext(ARecommendationContext context) {
		contexts.add(context);
	}

	public void addContexts(Set<ARecommendationContext> contexts) {
		this.contexts.addAll(contexts);
	}

	public Set<ARecommendationContext> getContexts() {
		return contexts;
	}

	// public boolean hasSupport() {
	// return !reasons.isEmpty();
	// }

	public double getSupportValue() {
		double supportValue = 0;
		Map<String, Double> max4Type = new HashMap<String, Double>();
		double curMaxValue = 0;

		for (ARecommendationContext context : contexts) {
			// //FUZZY STANDARD
			// supportValue = Math.max(supportValue, context.getSupportValue());

			// ROB08-ANSATZ - Über alle
			// supportValue = supportValue + context.getSupportValue() -
			// (supportValue * context.getSupportValue());

			// ROB08- ANSATZ - zwischen unterschiedlichen Recommendern
			Double maxValue = max4Type.get(context.getRecommenderType());
			if (maxValue == null) {
				curMaxValue = context.getSupportValue();
			} else {
				curMaxValue = Math.max(maxValue, context.getSupportValue());
			}

			max4Type.put(context.getRecommenderType(), curMaxValue);

		}

		// ROB08- ANSATZ - zwischen unterschiedlichen Recommendern
		for (Entry<String, Double> entry : max4Type.entrySet()) {
			supportValue = supportValue + entry.getValue()
					- (supportValue * entry.getValue());
		}

		return supportValue;
	}
	
	//special version for a specific recommender
	public double getSupportValue(String recommenderKind) {
		for (ARecommendationContext context : contexts) {
			if (context.getRecommenderType().equals(recommenderKind))
				return context.getSupportValue();
		}
		return 0;
	}

	public String getSupportReasons() {

		Map<String, Integer> reasonMap = new HashMap<String, Integer>();

		for (ARecommendationContext context : contexts) {
			Integer value = reasonMap.get(context.getRecommenderType() + ":"
					+ context.getReason());
			if (value == null)
				value = 0;

			reasonMap.put(context.getRecommenderType() + ":"
					+ context.getReason(), ++value);

		}

		String reasons = "";
		for (Entry<String, Integer> entry : reasonMap.entrySet()) {
			reasons += entry.getKey() + "(" + entry.getValue() + "), ";
		}

		return reasons.substring(0, reasons.length() - 2);
	}



}
