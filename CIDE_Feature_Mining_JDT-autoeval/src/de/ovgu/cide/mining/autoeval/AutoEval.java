package de.ovgu.cide.mining.autoeval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class AutoEval {

	public static void writeElements(String targetFilename,
			Set<String> elements) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(targetFilename, true), "ISO-8859-1"));

			for (String element : elements) {
				String content;
				content = element;

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

	public static Set<String> readElements(IFile file) {
		Set<String> values = new HashSet<String>();
		if (file.exists())
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(file.getContents()));
				while (reader.ready()) {
					String value = reader.readLine();
					values.add(value);
				}
				reader.close();
			} catch (IOException e) {
				System.err.println("I/O Error: " + e.getMessage());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		return values;
	}

}
