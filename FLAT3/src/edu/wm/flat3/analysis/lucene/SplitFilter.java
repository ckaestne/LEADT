package edu.wm.flat3.analysis.lucene;

import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/*
 	Filter for splitting identifiers in source code
 */

public class SplitFilter extends TokenFilter {
	
	  	private LinkedList<Token> listOfTokens=new LinkedList<Token>();
	  	
		public SplitFilter(TokenStream incomingStreamOfTokens) {
		    super(incomingStreamOfTokens);
		  }

		  public final Token next() throws IOException {
			if (listOfTokens.size()>0) return listOfTokens.removeFirst();
			
			Token token = input.next();

		    if ((token == null) && (listOfTokens.size()==0))
		      return null;
		    
		    String text=token.termText();
		    		    
		    //Capture thing like hieght_color, HeighColor, heightColor
		    Pattern splitPattern=Pattern.compile("(\\b|_|[A-Z])[a-z]+"); //Upper case indicates a word in an identifier
		    Matcher myMatcher=splitPattern.matcher(text);
		    
		    while (myMatcher.find()) {
		    	Token newToken=new Token(myMatcher.group(),token.startOffset()+myMatcher.start(),token.startOffset()+myMatcher.end()-1 );
		    	listOfTokens.add(newToken);
		    }
		    
		    

			splitPattern=Pattern.compile("([A-Z]+)(([A-Z][a-z])|_|\\b)"); //Hunts down all-upper-case acronyms
		    myMatcher=splitPattern.matcher(text);
		    
		    while (myMatcher.find()) {
		    	if (myMatcher.groupCount()>2) { 
		    			myMatcher.group(1);
		    			Token newToken=new Token(myMatcher.group(1),token.startOffset()+text.indexOf(myMatcher.group(1)),token.startOffset()+text.indexOf(myMatcher.group(1))+myMatcher.group(1).length()-1 );
		    			listOfTokens.add(newToken);
		    	}
		    }
		                  
		    if (listOfTokens.size()<2) listOfTokens.clear(); 
		    	else listOfTokens.add(token); 				// Add the original identifier as well

		    return token;
		  }
	

}
