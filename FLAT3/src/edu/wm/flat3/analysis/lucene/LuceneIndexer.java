package edu.wm.flat3.analysis.lucene;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.CodeModelRule;
import edu.wm.flat3.FLATTT;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.ConcernRepository;
//import org.severe.jripples.eig.JRipplesEIG;
//import org.severe.jripples.eig.JRipplesEIGNode;
//import org.severe.jripples.logging.JRipplesLog;


import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;

public class LuceneIndexer {

	private static HashMap <IMember, String> pathMatches = new HashMap<IMember, String>();
	private static HashMap <String, IMember> nodesMap = new HashMap<String, IMember>();
	private static File indexDir;

	private static String[] getStopWords() {
		String[] JAVA_STOP_WORDS = { "public", "private", "protected",
				"interface", "abstract", "implements", "extends", "null",
				"new", "switch", "case", "default", "synchronized", "do", "if",
				"else", "break", "continue", "this", "assert", "for",
				"instanceof", "transient", "final", "static", "void", "catch",
				"try", "throws", "throw", "class", "finally", "return",
				"const", "native", "super", "while", "import", "package",
				"true", "false" };

		HashSet<String> st = new HashSet<String>(Arrays.asList(StopAnalyzer.ENGLISH_STOP_WORDS));
		st.addAll(Arrays.asList(JAVA_STOP_WORDS));

		return st.toArray(new String[st.size()]);
	}

	public static void index(final ConcernRepository concernRep) {
		try {
			Job job = new Job("FLAT3 Lucene Indexing") {
		     protected IStatus run(IProgressMonitor monitor) {
		    		nodesMap.clear();

					try {
						IndexWriter writer = new IndexWriter(indexDir,new SnowballAnalyzer("English", getStopWords()),true);

						// TODO, vital: Modify this to work with the FLATTT database instead (did we do this yet?)
						List<Component> nodes = concernRep.getAllComponents();
						monitor.beginTask("Building index", nodes.size());
						
						for (Component comp : nodes) { //(int i = 0; i < nodes.length; i++) {
						    if (monitor.isCanceled()) return Status.CANCEL_STATUS;
							if (( comp.getJavaElement() instanceof IField) ||
							    ( comp.getJavaElement() instanceof IMethod)) {
								indexNode(writer, (IMember) comp.getJavaElement());
								nodesMap.put(comp.getJavaElement().getHandleIdentifier(), (IMember) comp.getJavaElement());
								monitor.worked(1);
							}
						}
						
						monitor.done();
						writer.close();
						
					} catch (Exception e) {
						//JRipplesLog.logError(e); // TODO: Log via some other method?
					}
		        //   if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		           
		           return Status.OK_STATUS;
		        }
		     };
		  job.setPriority(Job.LONG);
		  job.setRule(new CodeModelRule());
		  job.setUser(true);
		  job.schedule();
		
//		ProgressMonitorDialog progress = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//		try {
//			progress.run(false, false, new IRunnableWithProgress() {
//				public void run(IProgressMonitor monitor)
//				throws InvocationTargetException, InterruptedException {
//
//					nodesMap.clear();
//
//					try {
//						IndexWriter writer = new IndexWriter(indexDir,new SnowballAnalyzer("English", getStopWords()),true);
//
//						// TODO, vital: Modify this to work with the FLATTT database instead (did we do this yet?)
//						List<Component> nodes = concernRep.getAllComponents();
//						monitor.beginTask("Building index", nodes.size());
//						
//						for (Component comp : nodes) { //(int i = 0; i < nodes.length; i++) {
//							if (( comp.getJavaElement() instanceof IField) ||
//							    ( comp.getJavaElement() instanceof IMethod)) {
//								indexNode(writer, (IMember) comp.getJavaElement());
//								nodesMap.put(comp.getJavaElement().getHandleIdentifier(), (IMember) comp.getJavaElement());
//								monitor.worked(1);
//							}
//						}
//						
//						monitor.done();
//						writer.close();
//						
//					} catch (Exception e) {
//						//JRipplesLog.logError(e); // TODO: Log via some other method?
//					}
//				}
//			}
//			);
		} catch (Exception e) {
		}
		;
	}

	private static void indexNode(IndexWriter writer,IMember node) {
		Document doc = new Document();
		String source;
		try {
			node.getOpenable().open(new NullProgressMonitor());	
			source=node.getSource();
			if (source!=null) {
				doc.add(new Field("contents",source,Field.Store.NO, Field.Index.TOKENIZED));
				doc.add(new Field("nodeHandlerID", node.getHandleIdentifier(), Field.Store.YES, Field.Index.UN_TOKENIZED));
				writer.addDocument(doc);}
		} catch (Exception e) {
			//JRipplesLog.logError(e);
		}
	}


	public static void setIndexDir(File indexDir) {
		if (LuceneIndexer.indexDir!=null) 
			if (LuceneIndexer.indexDir.compareTo(indexDir)!=0)
				if (nodesMap!=null)
					nodesMap.clear();
		LuceneIndexer.indexDir = indexDir;
	}

	public static void checkIfIndexed()  throws Exception {
		if ((nodesMap==null) || (nodesMap.size()==0)) {
			if (indexDir.exists()) FLATTT.rebuildNodesMap();  // TODO: why does it know it doesn't need to index if the dir exists??
			else FLATTT.index();
		}
		
		if (FLATTT.nextSearch != null) {// now that indexing's started, do any pending search
			FLATTT.nextSearch.schedule();
			FLATTT.nextSearch = null;
		}
	}

	public static HashMap<IMember, String> search(String queryString) throws Exception {
		pathMatches.clear();
		checkIfIndexed();

		Directory fsDir = FSDirectory.getDirectory(indexDir, false);

		if (fsDir.list()==null) FLATTT.index();
		if (fsDir.list().length==0) FLATTT.index();

		IndexSearcher indexSearcher=null;
		try {
			indexSearcher = new IndexSearcher(fsDir);

			// StopAnalyzer.ENGLISH_STOP_WORDS
			QueryParser parser = new QueryParser("contents",
					new SnowballAnalyzer("English", getStopWords()));

			Query query = parser.parse(queryString);
			Hits hits = indexSearcher.search(query);

			for (int i = 0; i < hits.length(); i++) {
				Document doc = hits.doc(i);
				pathMatches.put(nodesMap.get(doc.get("nodeHandlerID")), Float.valueOf(hits.score(i)).toString());
			}
		} finally {
			if (indexSearcher != null) {
				indexSearcher.close();
			}
		}

		return pathMatches;
	}

	public static void rebuildNodesMap(final ConcernRepository concernRep) throws IOException {
		if (nodesMap==null) nodesMap = new HashMap<String, IMember>();

		ProgressMonitorDialog progress = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			progress.run(false, false, new IRunnableWithProgress (){

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					
					List<Component> nodes = concernRep.getAllComponents();
					HashMap<String, IMember> nodesHandelMap=new HashMap<String, IMember>();

					IndexReader reader;
					try {
						monitor.beginTask("Rebuilding node index map", 10);
						IProgressMonitor subMonitor=new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );
						subMonitor.beginTask("Building EIG nodes map", nodes.size());
						
						for (Component comp : nodes) { //(int i = 0; i < nodes.length; i++) {
							if ( comp.getJavaElement() instanceof IMember) { // == IMember.TYPE ) {
								nodesHandelMap.put(comp.getJavaElement().getHandleIdentifier(), (IMember) comp.getJavaElement());
								subMonitor.worked(1);
							}
						}
						
						subMonitor.done();
						subMonitor=new SubProgressMonitor(monitor, 9, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );

						reader = IndexReader.open(indexDir);
						subMonitor.beginTask("Building EIG nodes map", reader.numDocs());

						for (int i=0;i<reader.numDocs();i++) {
							String handler=reader.document(i).get("nodeHandlerID");
							if (handler==null) continue;
							if (nodesHandelMap.containsKey(handler))
								nodesMap.put(handler, nodesHandelMap.get(handler));
							subMonitor.worked(1);
						}
						
						subMonitor.done();
						monitor.done();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} 


			);	
		}
		catch (Exception e) {

		};

	}

}
