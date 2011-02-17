package de.ovgu.cide.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.AICategories;

public class Statistics {

	static final String exportPath = "D:/STUDIUM/Semester 9/Diplomarbeit/Evaluation/Data/";
	static final String importPath = "D:/STUDIUM/Semester 9/Diplomarbeit/Evaluation/Elements/";
	static final String curFeature = "0_View_Photo";

	// static final String curDepFeature = "0_SMS_Transfer";
	// public static final String depFeatureName = "SMS_Transfer";
	//	

	public static void writeElementsCategories(Set<AElement> elements,
			long printNbr, String filename) {

		if (elements.size() == 0)
			return;

		BufferedWriter out = null;
		String content;
		// ApplicationController jayFX = ApplicationController.getInstance();

		try {
			File f = new File(exportPath + filename + "/");
			f.mkdir();

			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f.getAbsolutePath() + "/Categories_"
							+ printNbr + ".txt", true), "ISO-8859-1"));

			// count elements
			HashMap<AICategories, Integer> elementCounter = new HashMap<AICategories, Integer>();

			for (AElement element : elements) {
				Integer counter = elementCounter.get(element.getCategory());

				if (counter == null)
					elementCounter.put(element.getCategory(), 1);
				else
					elementCounter.put(element.getCategory(), ++counter);
			}

			for (AICategories cat : elementCounter.keySet()) {
				content = cat + "\t" + elementCounter.get(cat);
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

	public static void writeElements(Set<AElement> elements, long printNbr,
			String filename, boolean isFeature) {
		if (elements.size() == 0)
			return;

		BufferedWriter out = null;
		String content;
		ApplicationController jayFX = ApplicationController.getInstance();

		try {
			File f = new File(exportPath + filename + "/");
			f.mkdir();

			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f.getAbsolutePath() + "/Elements_"
							+ printNbr + ".txt", true), "ISO-8859-1"));

			for (AElement element : elements) {
				content = element.getId();

				if (!isFeature) {
					for (IFeature feature : jayFX.getElementColors(element)) {
						content += "µ" + feature.getName();
					}
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

	public static String[] readStringArray(String filename) {
		String[] result = null;

		Vector values = new Vector();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			while (reader.ready()) {
				String value = reader.readLine();
				values.addElement(value);
			}
			reader.close();
			result = new String[values.size()];
			for (int i = 0; i < values.size(); i++)
				result[i] = ((String) values.elementAt(i)).toString();

		} catch (IOException e) {
			System.err.println("I/O Error: " + e.getMessage());
		}

		return result;
	}

	public static Set<String> loadFeatureElements(boolean expanded) {
		String[] elements = null;

		if (expanded)
			elements = readStringArray(importPath + curFeature
					+ "/Elements_expanded.txt");
		else
			elements = readStringArray(importPath + curFeature
					+ "/Elements_original.txt");

		Set<String> elementKeys = new HashSet<String>();

		for (int i = 0; i < elements.length; i++) {
			elementKeys.add(elements[i]);
		}
		System.out.println("---------->" + elementKeys.size());

		return elementKeys;
	}

	public static Set<String> loadDependentFeatureElements(String featurePath,
			boolean expanded) {
		String[] elements = null;

		if (expanded)
			elements = readStringArray(importPath + featurePath
					+ "/Elements_expanded.txt");
		else
			elements = readStringArray(importPath + featurePath
					+ "/Elements_original.txt");

		Set<String> elementKeys = new HashSet<String>();

		for (int i = 0; i < elements.length; i++) {
			elementKeys.add(elements[i]);
		}
		System.out.println("---------->" + elementKeys.size());

		return elementKeys;
	}

	public static void writeRecommendations(String line, String row) {

		BufferedWriter out = null;
		String content;

		try {
			File f = new File(exportPath + curFeature + "/");
			f.mkdir();

			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f.getAbsolutePath()
							+ "/Recommendations-line.txt", true), "ISO-8859-1"));

			content = line;
			content += System.getProperty("line.separator");
			out.write(content, 0, content.length());
			out.close();

			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f.getAbsolutePath()
							+ "/Recommendations-row.txt", true), "ISO-8859-1"));
			content = row;
			content += "-1 \t break" + System.getProperty("line.separator");
			out.write(content, 0, content.length());
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
