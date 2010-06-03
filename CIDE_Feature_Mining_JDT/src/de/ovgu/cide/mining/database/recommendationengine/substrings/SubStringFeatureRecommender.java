package de.ovgu.cide.mining.database.recommendationengine.substrings;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.recommendationengine.AAbstractElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.AAbstractFeatureRecommender;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;

public class SubStringFeatureRecommender extends AAbstractFeatureRecommender {

	private static final double TRESHOLD = 0.05;
	//private static final double ATTENUATOR = 0.05;
	
	private static final AICategories[] registerElement = new AICategories[] {
			AICategories.TYPE, AICategories.METHOD, AICategories.FIELD, AICategories.LOCAL_VARIABLE};
	
	private static final AICategories[] primaryElement = new AICategories[] {
		AICategories.TYPE, AICategories.METHOD, AICategories.FIELD,
		AICategories.LOCAL_VARIABLE };

	

	Map<AIElement, Map<AIElement, ARecommendationContext>> cache;

	public SubStringFeatureRecommender() {
		super();
		
		cache = new HashMap<AIElement, Map<AIElement,ARecommendationContext>>();

	}

	@Override
	public String getRecommendationType() {
		return "TPF";
	}
	
	private boolean isPrimaryElement(AIElement element) {

		for (int i = 0; i < primaryElement.length; i++) {
			if (primaryElement[i] == element.getCategory())
				return true;
		}

		return false;
	}
	
	private boolean isRegisterElement(AIElement element) {

		for (int i = 0; i < registerElement.length; i++) {
			if (registerElement[i] == element.getCategory())
				return true;
		}

		return false;
	}

//	private int matchCount(String source, String substring){
//		if ( source == null || source.isEmpty() || substring == null || substring.isEmpty() )
//			return 0;
//		
//		int count = 0;
//		for ( int pos = 0; (pos = source.indexOf( substring, pos )) != -1; count++ ) 
//			pos += substring.length();    
//		
//		return count; 
//	}

	private List<String> getSubStrings(String source){
		List<String> substrings = new ArrayList<String>();
		
		String curSubString = "";
		char curChar;
		boolean curUpper, lastUpper = false;
		int changer = 1;
		
		
		for (int i = 0; i < source.length(); i++) {
			
			curChar = source.charAt(i);
			curUpper = Character.isUpperCase(curChar);
			
			if (lastUpper != curUpper && i > changer) {
				substrings.add(curSubString.toUpperCase());
				changer = i + 1;
				curSubString = "";
			}
			
			if (Character.isLetter(curChar))
				curSubString += curChar;
			
			lastUpper = curUpper;
			
			
		}
		
		//add last element
		substrings.add(curSubString.toUpperCase());
		
		return substrings;
		
	}
	
	private String removeNamingConvention(String source) {
		String[] filter = source.split(":");
		
		//filter naming convention for elements
		if (filter.length == 2)
			source = filter[1].substring(1);
		
		return source;
	}
	

	
	public Map<AIElement, ARecommendationContext> getRecommendations(final IFeature color) {
		//PREPARATION PART!
		
		AIElement dummyElement = new AIElement() {
			
			public Set<AICategories> getSubCategories() {
				
				return new HashSet<AICategories>();
			}
			
			public int getStartPosition() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public String getShortName() {
				// TODO Auto-generated method stub
				return color.getName();
			}
			
			public int getLength() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public String getId() {
				// TODO Auto-generated method stub
				return color.getName();
			}
			
			public String getFullName() {
				// TODO Auto-generated method stub
				return color.getName();
			}
			
			public int getCompelationUnitHash() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public AICategories getCategory() {
				// TODO Auto-generated method stub
				return AICategories.FEATURE;
			}
			
			public void addSubcategory(AICategories category) {				
			}
		};
		
		Set<AIElement> featureElements = AC.getElementsOfColor(color);
		Map<String, Double> featureSubStringRegister = new HashMap<String, Double>();
		double featureRegisterSize = 0.0;
		
		Set<AIElement> nonFeatureElements = AC.getElementsOfNonColor(color);
		Map<String, Double> nonFeatureSubStringRegister = new HashMap<String, Double>();
		double nonFeatureRegisterSize = 0.0;
		
		if (ApplicationController.CHECK_COLOR_RELATIONS) {
			
			//ADD ELEMENTS OF RELATED COLORS
			Set<AIElement> tmpElements = new HashSet<AIElement>();
			tmpElements.addAll(featureElements);
			
			for (IFeature relatedColor : AC.getRelatedColors(color)) {
				tmpElements.addAll(AC.getElementsOfColor(relatedColor));
			} 
			featureElements = tmpElements;
			
			//ADD ELEMENTS OF NON RELATED COLORS
			Set<AIElement> tmpNonFeatureElements = new HashSet<AIElement>();
			tmpNonFeatureElements.addAll(nonFeatureElements);
			
			for (IFeature nonRelatedColor : AC.getRelatedNonColors(color)) {
				tmpNonFeatureElements.addAll(AC.getElementsOfColor(nonRelatedColor));
			} 
			nonFeatureElements = tmpNonFeatureElements;

		}
		
		if (featureElements.size() == 0)
			return new HashMap<AIElement, ARecommendationContext>();

		//create SubString Register for Feature Elements
		for (AIElement curElement : featureElements) {
			if (!isRegisterElement(curElement))
				continue;
			
			String source = removeNamingConvention(curElement.getShortName());
			List<String> subStrings = getSubStrings(source);
			
			for (String subString : subStrings) {
				Double value = featureSubStringRegister.get(subString);
				
				if (value == null)
					value = 0.0;
	
				featureSubStringRegister.put(subString, ++value);
				featureRegisterSize++;
			}
			
		}
		
		if (featureRegisterSize == 0)
			featureRegisterSize = 1.0;
		
		//create SubString Register for Non Feature Elements
		for (AIElement curElement : nonFeatureElements) {
			if (!isRegisterElement(curElement))
				continue;
			
			String source = removeNamingConvention(curElement.getShortName());
			List<String> subStrings = getSubStrings(source);
			
			for (String subString : subStrings) {
				Double value = nonFeatureSubStringRegister.get(subString);
				
				if (value == null)
					value = 0.0;
	
				nonFeatureSubStringRegister.put(subString, ++value);
				nonFeatureRegisterSize++;
			}
			
			
		}
		
		if (nonFeatureRegisterSize == 0)
			nonFeatureRegisterSize = 1.0;
		
		//normalize registers
		double tmpFeatureRegisterSize = featureRegisterSize;
		double tmpNonFeatureRegisterSize = nonFeatureRegisterSize;
		
		for (String matchSubString : nonFeatureSubStringRegister.keySet()) {
			//check if non feature string is also feature string 
			if (!featureSubStringRegister.containsKey(matchSubString))
				continue;
			
			double featureValue = featureSubStringRegister.get(matchSubString)/tmpFeatureRegisterSize;
			double nonFeatureValue = nonFeatureSubStringRegister.get(matchSubString)/tmpNonFeatureRegisterSize;
			double normValue;
			
			if (featureValue > nonFeatureValue) {
				normValue = nonFeatureValue * tmpFeatureRegisterSize;
				featureRegisterSize -= normValue;
				featureSubStringRegister.put(matchSubString,featureSubStringRegister.get(matchSubString)-normValue);
				
				nonFeatureRegisterSize -= nonFeatureSubStringRegister.remove(matchSubString);
				
			}
			else if (featureValue < nonFeatureValue) {
				normValue = featureValue * tmpNonFeatureRegisterSize;
				nonFeatureRegisterSize -= normValue;
				nonFeatureSubStringRegister.put(matchSubString,nonFeatureSubStringRegister.get(matchSubString) - normValue);
				
				featureRegisterSize -= featureSubStringRegister.remove(matchSubString);
				
			}
			else {
				nonFeatureRegisterSize -= nonFeatureSubStringRegister.get(matchSubString);
				nonFeatureSubStringRegister.put(matchSubString,0.0);
				featureRegisterSize -= featureSubStringRegister.get(matchSubString);
				featureSubStringRegister.put(matchSubString,0.0);
			}
			
		}	
		
		
		//RECOMMENDATION PART!
		Map<AIElement, ARecommendationContext> recommendations = new HashMap<AIElement, ARecommendationContext>();
		
		for (AIElement curElement : AC.getAllElements()) {
			
			//check only primary elements
			if (!isPrimaryElement(curElement))
				continue;
			
			//check only if element can be recommended
			if (!isValidRecommendation(curElement, color))
				continue;
			

			String target = removeNamingConvention(curElement.getShortName());
			List<String> subStrings = getSubStrings(target);
			double subStringCount = subStrings.size();
			double support = 0;
			int unknownStrings = 0;
			for (String subString : subStrings) {
				double lenFactor;
				if (subString.length() >= 6) {
					lenFactor = (double)1.0;
				}
				else{
					lenFactor = Math.max((double)1.0, Math.log10(subString.length())/Math.log10(6));
				}
				
				Double featureSubStringCount = featureSubStringRegister.get(subString);
				if (featureSubStringCount == null)
					featureSubStringCount = 0.0;
				double featureIndex = (double)featureSubStringCount / (double)featureRegisterSize;
				
				Double nonFeatureSubStringCount = nonFeatureSubStringRegister.get(subString);
				if (nonFeatureSubStringCount == null)
					nonFeatureSubStringCount = 0.0;
				double nonFeatureIndex = (double)nonFeatureSubStringCount / (double)nonFeatureRegisterSize;
				
				if ((featureIndex-nonFeatureIndex) == 0)
					unknownStrings++;
				else				
					support += (lenFactor *(featureIndex-nonFeatureIndex));
			}
			
			//support = support * (subStringCount-unknownStrings) / subStringCount; 
			
			
			if (support > 1)
				support = (double)1;
			
			if (support >= TRESHOLD) {
				ARecommendationContext context = new ARecommendationContext(dummyElement, "Text Match", getRecommendationType(), support);
				recommendations.put(curElement, context);
			}

		}


		return recommendations;
		
	}

}
