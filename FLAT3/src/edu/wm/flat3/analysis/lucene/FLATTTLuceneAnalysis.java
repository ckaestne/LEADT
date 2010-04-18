package edu.wm.flat3.analysis.lucene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.ui.IActionFilter;
/*import org.severe.jripples.eig.JRipplesEIG;
import org.severe.jripples.eig.JRipplesEIGEvent;
import org.severe.jripples.eig.JRipplesEIGListener;
import org.severe.jripples.eig.JRipplesEIGNode;
import org.severe.jripples.logging.JRipplesLog;
import org.severe.jripples.modules.interfaces.JRipplesAnalysisModuleInterface;
import org.severe.jripples.modules.interfaces.JRipplesModuleInterface;
import org.severe.jripples.modules.manager.ModuleProxy;*/

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
/**
 * @author max
 * 
 */
public class FLATTTLuceneAnalysis implements IActionFilter {

	private static HashMap<IMember, String> nodeMatches = new HashMap<IMember, String>();


	private static boolean isActiveModule;
	public static String searchString;
	private static FLATTTLuceneAnalysis analysismodule;
	private static boolean dirty;
	private static boolean isInitialized = false;
	
	private static ArrayList<FLATTTMember> lastSearch;

	public FLATTTLuceneAnalysis() {
		checkIfInitialized();
		analysismodule = this;
	}

	private static void checkIfInitialized() {
		if (!isInitialized) {
			isInitialized = true;
			searchString = "";
			dirty = false;
		}
	}

	private static void assignProbabilities(HashMap<IMember, String> nodeMatches) {
		//JRipplesEIG.doLock(analysismodule);
		ArrayList<FLATTTMember> nodes = new ArrayList<FLATTTMember>();//JRipplesEIG.getAllNodes();
		Collection c = nodeMatches.keySet();
		
		Iterator itr = c.iterator();
		 
		while(itr.hasNext()) {

		//	System.out.println(itr.next());
			//System.out.println(nodeMatches.get(itr.next()));
			FLATTTMember n = new FLATTTMember((IMember)itr.next());
			n.setProbability(nodeMatches.get(n.getNodeIMember()));
			nodes.add(n);
		}

			/*if (nodeMatches.containsKey(nodes[i]))
				nodes[i].setProbability(nodeMatches.get(nodes[i]));
			else
				if (nodes[i].getProbability()!=null)
					if (nodes[i].getProbability().length()>0)
						nodes[i].setProbability("");
		}*/
		
		//JRipplesEIG.doUnLock(analysismodule);
		
		
		// Put nodes somewhere ContentProvider can get to it
		
		FLATTTLuceneAnalysis.lastSearch = nodes;
		FLATTT.searchResults = nodes;
		FLATTT.searchResultsAreTrace = false;
		
	}

// ----------------------------------------------------------------

	public static void DoProjectAnalysis() {
		if (getSearchString()==null) return;
		if (getSearchString().trim().length()==0) return;
		if (dirty) {

			nodeMatches.clear();

			//todo: why do we set the index here? oh well
			LuceneIndexer.setIndexDir(FLATTT.singleton().getStateLocation().append("luceneindex").toFile()); // was IMember.getJavaProject().getName()

			luceneDoSearch();
			assignProbabilities(nodeMatches);
			
			dirty = false;
		}

	}



	public void AnalyzeProject() {
		checkIfInitialized();
		dirty = true;
		DoProjectAnalysis();

	}

	public void ReAnalyzeProjectAtNodes(Set changed_nodes) {
		checkIfInitialized();
		dirty = true;
		DoProjectAnalysis();
		//TODO can change to re-analyze individual nodes

	}

	public void loadUp(int controllerType) {
		checkIfInitialized();
	//	if (JRipplesEIG.getEIG() != null)
	//		JRipplesEIG.addJRipplesEIGListener(this);
		setActiveModule(true);
		dirty = true;
		AnalyzeProject();

	}

	public void shutDown(int controllerType) {
	//	if (JRipplesEIG.getEIG() != null)
	//		JRipplesEIG.removeJRipplesEIGListener(this);
		setActiveModule(false);
	}

//	public void JRipplesEIGChanged(JRipplesEIGEvent evt) {
		//do nothing
	//}


	public static String getSearchString() {
		checkIfInitialized();
		return searchString;
	}

	public static void setSearchString(String newSearchString) {
		checkIfInitialized();
		if (newSearchString == null)
			return;
		if (FLATTTLuceneAnalysis.searchString != null) {
			if (FLATTTLuceneAnalysis.searchString.compareTo(newSearchString) != 0) {
				FLATTTLuceneAnalysis.searchString = newSearchString;
				dirty = true;
			} else {
				FLATTT.searchResults = FLATTTLuceneAnalysis.lastSearch;
				FLATTT.searchResultsAreTrace = false;
			}
		} else {
			FLATTTLuceneAnalysis.searchString = newSearchString;
			dirty = true;

		}
		if (dirty) {
			/*if (!FLATTTLuceneAnalysis.isActiveModule()) {
				ModuleProxy.setActiveModule("Lucene Analysis",
						JRipplesModuleInterface.CONTROLLER_TYPE_SELF);

			};*/
			FLATTTLuceneAnalysis.DoProjectAnalysis();
		}
		
	//	FLATTT.searchResultsAreTrace = false;
	}


	public static boolean isActiveModule() {
		checkIfInitialized();
		return isActiveModule;
	}

	public static void setActiveModule(boolean newIsActiveModule) {
		checkIfInitialized();
		FLATTTLuceneAnalysis.isActiveModule = newIsActiveModule;
	}

	public boolean testAttribute(Object target, String name, String value) {
		if (name == null)
			return false;
		if (name.compareTo("state") == 0)
			return isActiveModule;
		return false;
	}

	public String getUnitsTitle() {
		return "Rank";
	}

	// --------------------------------- Lucene -----------------------------

	public static void luceneDoSearch() {
		HashMap <IMember, String>  matches = null;
		try {
			matches = LuceneIndexer.search(getSearchString());
		} catch (Exception e) {
			//JRipplesLog.logError(e);
		}
		if (matches == null)
			return;

		nodeMatches.clear();
		try {
			for (Iterator<IMember> iter=matches.keySet().iterator(); iter.hasNext(); ) {
				IMember node=iter.next();
				if (node!=null)
					nodeMatches.put(node, matches.get(node));
			}
		} catch (Exception e) {
			//JRipplesLog.logError(e);
		}
	}

}
