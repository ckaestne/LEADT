package de.ovgu.cide.mining.database.recommendationengine.substrings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.recommendationengine.AAbstractElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;

public class SubStringElementRecommender extends AAbstractElementRecommender {

	private static final double TRESHOLD = 0.05;
	private static final double ATTENUATOR = 0.1;
	
	private static final AICategories[] primaryElement = new AICategories[] {
			AICategories.TYPE, AICategories.METHOD, AICategories.FIELD,
			AICategories.LOCAL_VARIABLE };

	Map<AElement, Map<AElement, ARecommendationContext>> cache;

	public SubStringElementRecommender() {
		super();
		
		cache = new HashMap<AElement, Map<AElement,ARecommendationContext>>();

	}

	@Override
	public String getRecommendationType() {
		return "TPE";
	}
	
	private boolean isPrimaryElement(AElement element) {

		for (int i = 0; i < primaryElement.length; i++) {
			if (primaryElement[i] == element.getCategory())
				return true;
		}

		return false;
	}

	private int matchCount(String source, String substring){
		if ( source == null || source.isEmpty() || substring == null || substring.isEmpty() )
			return 0;
		
		int count = 0;
		for ( int pos = 0; (pos = source.indexOf( substring, pos )) != -1; count++ ) 
			pos += substring.length();    
		
		return count; 
	}

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
	

	
	public Map<AElement, ARecommendationContext> getRecommendations(
			AElement element, IFeature color) {

		Map<AElement, ARecommendationContext> recommendations;

		if (cache.keySet().contains(element)) {

			recommendations = cache.get(element);

			if (recommendations != null)
				return filterValidRecommendations(color, recommendations);

			return new HashMap<AElement, ARecommendationContext>();

		}

		// mark element as handled
		cache.put(element, null);
		recommendations = new HashMap<AElement, ARecommendationContext>();

		if (!isPrimaryElement(element))
			return recommendations;
		
		
		String source = removeNamingConvention(element.getShortName());
		List<String> subStrings = getSubStrings(source);
		
		for (AElement curElement : AC.getAllElements()) {

			//do not check with itself
			if (curElement.equals(element))
				continue;
			
			//check only primary elements
			if (!isPrimaryElement(curElement))
				continue;
			

			String targetString = removeNamingConvention(curElement.getShortName()).toUpperCase();
		
			int matchChars = 0;
			for (String subString : subStrings) {
				matchChars += matchCount(targetString, subString) * subString.length();
			}
			
			double support = (double)matchChars / (double)targetString.length() * ATTENUATOR * (double)matchChars ;
			
			if (support > 1)
				support = 1;
			
			if (support >= TRESHOLD) {
				ARecommendationContext context = new ARecommendationContext(element, "Text Match",getRecommendationType(),  support );
				recommendations.put(curElement, context);
			}

		}

		// CHECK IF RECOMMENDATION IS ALREADY IN COLOR!

		// cache results for element
		if (recommendations.size() > 0) {
			cache.put(element, recommendations);
		}

		return filterValidRecommendations(color, recommendations);
	}

}
