package de.ovgu.cide.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;

public class Statistics {
	
	static final String exportPath = "D:/STUDIUM/Semester 9/Diplomarbeit/Evaluation/Data/";
	
	
	public static void writeElementsCategories(Set<AIElement> elements, long printNbr, String filename) {
		
		if (elements.size() == 0)
			return;
		
		BufferedWriter out = null;
		String content;
		ApplicationController jayFX = ApplicationController.getInstance();
		
		try {
			File f = new File(exportPath +filename+"/");
			f.mkdir();
			
			
				out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f.getAbsolutePath()+"/Categories_"+printNbr+".txt", true), "ISO-8859-1"));
			
			//count elements
			HashMap<AICategories, Integer> elementCounter = new HashMap<AICategories, Integer>();
			
			for (AIElement element : elements) {
				Integer counter = elementCounter.get(element.getCategory());
				
				if (counter == null)
					elementCounter.put(element.getCategory(), 1);
				else
					elementCounter.put(element.getCategory(), ++counter);
			}
			
			
			for (AICategories cat : elementCounter.keySet()) {
				content = cat+ "\t" + elementCounter.get(cat);
				content += System.getProperty("line.separator");
				out.write(content, 0, content.length());
				
			}
			
			
				out.close();
				
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	public static void writeElements(Set<AIElement> elements, long printNbr, String filename, boolean isFeature) {
		if (elements.size() == 0)
			return;
		
		BufferedWriter out = null;
		String content;
		ApplicationController jayFX = ApplicationController.getInstance();
		
		try {
			File f = new File(exportPath+filename+"/");
			f.mkdir();
			
			
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f.getAbsolutePath()+"/Elements_"+printNbr+".txt", true), "ISO-8859-1"));
			
			for (AIElement element : elements) {
				content = element.getId();
				
				if (!isFeature)
					for (IFeature feature : jayFX.getElementColors(element)) {
						content += "µ"+feature.getName();
					}
				
				
				content += System.getProperty("line.separator");
						
				out.write(content, 0, content.length());
				
				
			}
			
				out.close();
				
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		

	}

}
