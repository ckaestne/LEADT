package de.ovgu.cide.mining.database.recommendationengine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ARecommendationContextCollection {
	//enum Reason {TYPING_ERROR, SUBSTRING_MATCH, OTHER};
	
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
	

	
//	public boolean hasSupport() {
//		return !reasons.isEmpty();
//	}
	
	public double getSupportValue() {
		double supportValue = 0;
		
		for (ARecommendationContext context : contexts) {
//			//FUZZY STANDARD
			supportValue = Math.max(supportValue, context.getSupportValue());
		
			//ROB08-ANSATZ
//			supportValue = supportValue + context.getSupportValue() - (supportValue * context.getSupportValue());
			
		}
		
		return supportValue;
	}

	public String getSupportReasons() {
		
		Map<String, Integer> reasonMap = new HashMap<String, Integer>();
		
		for (ARecommendationContext context : contexts) {
			Integer value = reasonMap.get(context.getReason());
			if (value == null)
				value = 0;
			
			reasonMap.put(context.getReason(),  ++value);
		
		}

		
		String reasons = "";
		for (String reason : reasonMap.keySet()) {
			reasons += reason +"(" + reasonMap.get(reason)  +"), ";
		}

		return reasons.substring(0, reasons.length() - 2) ;
	}



	


}
