package de.ovgu.cide.mining.database.recommendationengine;

import de.ovgu.cide.mining.database.model.AElement;


public class ARecommendationContext {
	
	private double supportValue;
	private String reason;
	private AElement supporter;
	private String recommenderType;
	
	public ARecommendationContext(AElement supporter, String reason, String recommenderType, double value) {
		this.supportValue = value;
		this.supporter = supporter;
		this.reason = reason;
		this.recommenderType = recommenderType;
	}
	
	public ARecommendationContext(ARecommendationContext context1, ARecommendationContext context2, String recommenderType) {
	
		//FUZZY STANDARD
		supportValue = Math.max(context1.getSupportValue(), context2.getSupportValue());
		
		//ROB08-ANSATZ
		//supportValue = context1.getSupportValue() + context2.getSupportValue() - (context1.getSupportValue() *+
		//		context2.getSupportValue());
		
		supporter = context1.getSupporter();
		reason = context1.getReason() + ", " + context2.getReason();
		this.recommenderType = recommenderType;
	
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

	public AElement getSupporter() {
		return supporter;
	}

	public String getRecommenderType() {
		return recommenderType;
	}
	
	
//	public void setSupportValue(double supportValue) {
//		this.supportValue = supportValue;
//	}



}
