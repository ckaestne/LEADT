package de.ovgu.cide.mining.database.recommendationengine;

import de.ovgu.cide.mining.database.model.AIElement;


public class ARecommendationContext {
	
	private double supportValue;
	private String reason;
	private AIElement supporter;
	
	public ARecommendationContext(AIElement supporter, String reason, double value) {
		this.supportValue = value;
		this.supporter = supporter;
		this.reason = reason;
	}
	
	public ARecommendationContext(ARecommendationContext context1, ARecommendationContext context2) {
	
		//FUZZY STANDARD
//		supportValue = Math.max(context1.getSupportValue(), context2.getSupportValue());
		
		//ROB08-ANSATZ
		supportValue = context1.getSupportValue() + context2.getSupportValue() - (context1.getSupportValue() *+
				context2.getSupportValue());
		
		supporter = context1.getSupporter();
		reason = context1.getReason() + ", " + context2.getReason();
	
	}
	
//	public void setReason(String reason) {
//		this.reason = reason;
//	}
	

	public String getReason() {
		return reason;
	}
		
	public double getSupportValue() {
		return supportValue;
	}

	public AIElement getSupporter() {
		return supporter;
	}

	
	
//	public void setSupportValue(double supportValue) {
//		this.supportValue = supportValue;
//	}



}
