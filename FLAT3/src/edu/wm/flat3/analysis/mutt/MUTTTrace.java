package edu.wm.flat3.analysis.mutt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IMember;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.OpenSearchViewAction;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.repository.Component;

public class MUTTTrace {

	public static void trace(String directory, String className, String arguments[]) {
		// no better way to do this? gotta get the path to the bin directory or to our jar
		// so we can launch our MUTT tracer
		// TODO: double check this when we package plugin for distribution and/or in a jar
		// could IPath parse this??
		String bundlePaths = FLATTT.singleton().getBundle().getLocation();//=.split("@");
		//bundlePaths = bundlePaths.split("reference:file:")[1]; // will this always work?
	
	
		// Classpath delimiter varies by platform, along with other things
		String classpathDelimiter = "";
		String fileDelim = "";
		if (System.getProperty("os.name").startsWith("Windows")) {
			classpathDelimiter = ";";
			bundlePaths = bundlePaths.split("reference:file:/")[1];
			bundlePaths = bundlePaths.replace("/", "\\");
			fileDelim = "\\";
		}
		 else{
			classpathDelimiter = ":";		
			fileDelim = "/";
			//bundlePaths = bundlePaths.split("reference:file:")[1];
		 }
		 // other possibilities?
		
		// toolsAutomatic seems to be the canonical way to locate tools.jar
	//	String tools = "C:\\Sun\\SDK\\jdk\\lib\\tools.jar";
		String toolsAutomatic = new Path(System.getProperty("java.home")).append("lib").append("tools.jar").toOSString();//System.getProperty("java.home")+fileDelim+"lib"+fileDelim+"tools.jar"; // TODO, vital: why doesn't this work sometimes?!
		String toolsPlugin = FLATTT.singleton().getStateLocation().append("tools.jar").toOSString();
		
		// so, check possible locatins for tools, including soemwhere special for the plugin
		if (!(new File(toolsAutomatic).exists() || new File(toolsPlugin).exists())) {
			// if none found, display dialog: issue. fixes: install jdk, switch to the jdk, or copy tools.jar to one of these locations
			MessageBox dialog = new MessageBox(Display.getCurrent().getActiveShell());
			dialog.setMessage("Could not locate tools.jar, which is needed to trace programs and which comes with the JDK (not the JRE). Locations tried:\n"+toolsAutomatic+"\n"+toolsPlugin+"\n\nPlease switch to a JDK runtime or move a copy of your tools.jar to "+toolsPlugin);
			dialog.open();
			return;
		}
		
		// huh; with the /bin works if we're running eclipse in eclipse to debug, without it works if we're running
		// from a production .jar?
		String muttClassDirectory = toolsAutomatic+classpathDelimiter+toolsPlugin+classpathDelimiter+bundlePaths+fileDelim+"bin"+classpathDelimiter+bundlePaths; 
		//System.out.println(muttClassDirectory); // debug println
		String traceArgs[] = {"java", "-Dfile.encoding=UTF-8",
				"-Xbootclasspath/a:" + directory,
				"-classpath", muttClassDirectory,
				// MUTT's class
				"com.sun.tools.example.trace.Trace",
				// dot directory
				"-dotdir", FLATTT.singleton().getTraceLocation().addTrailingSeparator().toOSString(),
				// taking out -output since it doesn't work anyway, it always outputs to a diff file for the .dot
				// needs to be here though? doubt it, should take it out
				"-output", FLATTT.singleton().getTraceLocation().append("output").toOSString(),
				className};
		
		// Append arguments
		String args[] = new String[traceArgs.length+arguments.length];
		for (int i = 0; i < traceArgs.length; i++) args[i] = traceArgs[i];
		for (int i = traceArgs.length; i < (traceArgs.length+arguments.length); i++) args[i] = arguments[i-traceArgs.length];

		// Make sure we have a directory to store the traces in
		if (!FLATTT.singleton().getTraceLocation().toFile().exists())
			FLATTT.singleton().getTraceLocation().toFile().mkdirs();
		
		// Delete all .dot files
		for ( File dot : FLATTT.singleton().getTraceLocation().toFile().listFiles())
			dot.delete();
		
		String command = "";
		for (int i = 0; i < args.length; i++)
			command += " " +args[i];
		System.out.println(command);
		
		// Call MUTT
		try {
			Process process = Runtime.getRuntime().exec(args, null, new File(directory));
			process.waitFor();
			
			// Output MUTT's output
			//BufferedReader inputStreamReader  = new BufferedReader(new InputStreamReader(process.getInputStream()));  
			BufferedReader errStreamReader  = new BufferedReader(new InputStreamReader(process.getErrorStream()));  
			  
			//StringBuffer output = new StringBuffer();  
			StringBuffer error = new StringBuffer();  
			//for(String line;(line=inputStreamReader.readLine())!=null;)  
			//{  
			//       output.append(line);  
			//}  
			for(String line;(line=errStreamReader.readLine())!=null;)  
			{  
			       error.append(line);  
			}  
			
			//System.out.println("output = " + output);  
			System.out.println("error = " + error);  
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		loadTraceData();
	}
	
	private static void loadTraceData() {
		// Parse all .dot files, right?
		File dir = FLATTT.singleton().getTraceLocation().toFile();//new File(directory);
		ArrayList<FLATTTMember> nodes = new ArrayList<FLATTTMember>();
		
		for (String file : dir.list()) {
			if (file.endsWith(".dot") && file.startsWith("dcg.")) {
				
				TraceGraph g = new TraceGraph();
				if (!g.parseDOT(dir + "/" + file)) continue; // if file's invalid, skip it
		
				Hashtable<String,CallNode> results = g.callNodes;
				Collection c = g.callNodes.keySet();
				
				Iterator itr = c.iterator();
				 
				while(itr.hasNext()) {
					String key = (String) itr.next();
				
					String cname = key.split("\\.",2)[0];
					String name = key.split("\\.",2)[1];
					//System.out.println(cname+ " : " + name);
		
					IMember imember = null;
		
					Component comp =  FLATTT.repository.getComponentWithName(key);
					if (comp != null) imember = (IMember) comp.getJavaElement();
					
					if (imember != null) {
						FLATTTMember n = new FLATTTMember(imember);
						nodes.add(n);
					}
				}
			}
		}
	
		// Put nodes somewhere ContentProvider can get to it
		FLATTT.searchResults = nodes;
		FLATTT.originalSearchResults = nodes;
		FLATTT.searchResultsAreTrace = true;
		FLATTT.searchResultsAreCombinational = false;
	
		OpenSearchViewAction a = new OpenSearchViewAction(FLATTT.tableView.getSite());
		a.run();
			
		TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
		contentP.refreshTable();
	}
	
	/**
	 * Saves the last trace done to a zip file for later reloading.
	 * @param filename
	 * @throws IOException
	 */
	public static void exportToFile(String filename) throws IOException {
		FileOutputStream zipFile = new FileOutputStream(filename);
		ZipOutputStream zip = new ZipOutputStream(zipFile);

		File dir = FLATTT.singleton().getTraceLocation().toFile();
		
		for (String file : dir.list()) {
			ZipEntry entry = new ZipEntry(file);
			FileInputStream fileStream = new FileInputStream(dir +"/" + file);
			
			zip.putNextEntry(entry);
	        for (int c = fileStream.read(); c != -1; c = fileStream.read()) {
	        	zip.write(c);
	        }
			fileStream.close();
		}
		
		
		zip.close();
		zipFile.close();
	}
	
	/**
	 * Loads a trace from a zip file into our data directory and loads it into the view
	 * @param filename
	 * @throws IOException
	 */
	public static void importFromFile(String filename) throws IOException {
		// overwrite existing map? GUI should warn about that "this will overwrite your current map for project x, are you sure?"
		
		// extract files to current map directory, load them up.
		ZipFile zip = new ZipFile(filename);
		 for (Enumeration e = zip.entries(); e.hasMoreElements();)
         {
			ZipEntry entry = (ZipEntry) e.nextElement();
			InputStream entryIn = zip.getInputStream(entry);
		
			FileOutputStream entryOut = new FileOutputStream(FLATTT.singleton().getTraceLocation().toOSString() + "/" + entry.getName());		
			for (int c = entryIn.read(); c != -1; c = entryIn.read()) {
			       entryOut.write(c);
			}
		}
		
		loadTraceData();
	}
	
}
	
