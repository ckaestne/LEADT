package de.ovgu.cide.mining.autoeval;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import cide.gast.ASTVisitor;
import cide.gast.IASTNode;
import cide.gast.ISourceFile;
import cide.gast.Property;
import cide.gast.PropertyZeroOrMore;
import cide.gparser.ParseException;
import de.ovgu.cide.ASTColorChangedEvent;
import de.ovgu.cide.CIDECorePlugin;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.ApplicationControllerException;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.recommendationengine.AElementRecommendationManager;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;

public class RunAutoEval {

	static final String PRIORITYSETTING_NAME = (AElementRecommendationManager.USE_TYPESYSTEM ? "TS"
			: "")
			+ (AElementRecommendationManager.USE_TOPOLOGYANALYSIS ? "TA" : "")
			+ (AElementRecommendationManager.USE_SUBSTRINGCOMP ? "SS" : "")
			+ (AElementRecommendationManager.USE_FOCUS_TS_09 ? ".9" : "");

	static final boolean ISGREEDY = true;
	static final int MAX_FAILURE = 50;

	// @Test
	// @Ignore
	// public void run1() throws Exception {
	// String projectName = "MobileMedia_Eval";
	// String featureName = "Play_Music";
	// IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
	// projectName);
	// project.refreshLocal(IResource.DEPTH_INFINITE,
	// new NullProgressMonitor());
	// Set<IFeature> features = FeatureModelManager.getInstance()
	// .getFeatureModel(project).getFeatures();
	//
	// IFeature color = getFeature(featureName, features);
	// measureOneFeatureDefault(project, color);
	// }

	String[] features = new String[] { "Copy_Media", "Count_and_Sort",
			"Favourites", "SMS_Transfer", "Play_Music" };
	String[] featuresAll = new String[] { "Copy_Media", "Count_and_Sort",
			"Favourites", "SMS_Transfer", "Play_Music", "View_Photo",
			"SMS_or_Copy" };

	Map<String, String[]> seeds = new HashMap<String, String[]>();
	{
		seeds.put("Copy_Media", new String[] { "seed_Copy_Media_1_1.log",
				"seed_Copy_Media_2_0961.log", "seed_Copy_Media_3_0694.log",
				"seed_Copy_Media_4_0579.log", "seed_Copy_Media_5_0507.log",
				"seed_Copy_Media_domain.log" });
		seeds.put("Count_and_Sort", new String[] {
				"seed_Count_and_Sort_1_1.log",
				"seed_Count_and_Sort_2_0707.log",
				"seed_Count_and_Sort_3_066.log",
				"seed_Count_and_Sort_4_0577.log",
				"seed_Count_and_Sort_5_0412.log",
				"seed_Count_and_Sort_domain.log" });
		seeds.put("Favourites", new String[] { "seed_Favourites_1_1.log",
				"seed_Favourites_2_0875.log", "seed_Favourites_3_075.log",
				"seed_Favourites_4_0438.log", "seed_Favourites_5_025.log",
				"seed_Favourites_domain.log" });
		seeds.put("Play_Music", new String[] { "seed_Play_Music_1_1.log",
				"seed_Play_Music_2_08.log", "seed_Play_Music_3_08.log",
				"seed_Play_Music_4_0707.log", "seed_Play_Music_5_0707.log",
				"seed_Play_Music_domain.log" });
		seeds
				.put("SMS_Transfer", new String[] {
						"seed_SMS_Transfer_2_0373.log",
						"seed_SMS_Transfer_3_0323.log",
						"seed_SMS_Transfer_4_0311.log",
						"seed_SMS_Transfer_5_0308.log",
						"seed_SMS_Transfer_6_308.log",
						"seed_SMS_Transfer_domain.log" });

	}

	String[] featuresPrevayler = new String[] { "Snapshot", "Censor" /*"GZip", "Monitor",
			"Replication",*/  };

	Map<String, String[]> seedsPrevayler = new HashMap<String, String[]>();
	{
		seedsPrevayler.put("Censor", new String[] { "seed_Censor_1_1.log",
				"seed_Censor_package.log" });
		seedsPrevayler.put("GZip", new String[] { "seed_GZip_1_x.log",
				"seed_GZip_package.log" });
		seedsPrevayler.put("Monitor", new String[] { "seed_Monitor_1_1.log",
				"seed_Monitor_package.log" });
		seedsPrevayler.put("Replication", new String[] {
				"seed_Replication_1_1.log", "seed_Replication_package.log" });
		seedsPrevayler.put("Snapshot", new String[] { "seed_Snapshot_1_1.log",
				"seed_Censor_package.log" });
	}

	private Set<SeedInfo> getSeeds(int nr, String[] features,
			Map<String, String[]> seeds) throws CoreException,
			FeatureModelNotFoundException {
		Set<SeedInfo> seedInfos = new HashSet<SeedInfo>();
		for (String f : features) {
			seedInfos.add(new SeedInfo(EvalHelper.getFeatureByName(f), seeds
					.get(f)[nr]));
		}
		return seedInfos;
	}

	@Test
	public void runDefault0() throws Exception {
		Set<SeedInfo> seedInfos = getSeeds(0, featuresPrevayler, seedsPrevayler);

		IProject project = EvalHelper.getProject();
		ApplicationController lDB = setupWorkspace(project, seedInfos);

		for (String f : featuresPrevayler) {
			IFeature color = EvalHelper.getFeatureByName(f);
			String targetAnnotationFile = getTargetFilename(color);
			measureFeature(lDB, project, color, targetAnnotationFile,
					seedInfos, ISGREEDY);
		}

	}

	@Test
	public void runDefault1() throws Exception {
		Set<SeedInfo> seedInfos = getSeeds(1, featuresPrevayler, seedsPrevayler);

		IProject project = EvalHelper.getProject();
		ApplicationController lDB = setupWorkspace(project, seedInfos);

		for (String f : featuresPrevayler) {
			IFeature color = EvalHelper.getFeatureByName(f);
			String targetAnnotationFile = getTargetFilename(color);
			measureFeature(lDB, project, color, targetAnnotationFile,
					seedInfos, ISGREEDY);
		}

	}

	@Test
	@Ignore
	public void loadTargetStatistics() throws Exception {
		// Set<SeedInfo> seedInfo = new HashSet<SeedInfo>();
		// for (String f : featuresAll) {
		// IFeature color = EvalHelper.getFeatureByName(f);
		// seedInfo.add(new SeedInfo(color, getTargetFilename(color)));
		// }
		IProject project = EvalHelper.getProject();
		ApplicationController lDB = setupWorkspace(project,
				new HashSet<SeedInfo>());

		for (String f : featuresPrevayler) {
			IFeature color = EvalHelper.getFeatureByName(f);
			String targetAnnotationFile = getTargetFilename(color);
			Set<String> targetNodes = AutoEval.readElements(project
					.getFile(targetAnnotationFile));
			Set<AElement> targetElements = getTargetElements(lDB, color,
					targetNodes);
			calcTargetStatistics(color, targetElements);
		}
	}

	// @Test
	// public void runAllFirst() throws Exception {
	// for (String f : features) {
	// measureMobileMedia(f, seeds.get(f)[0], ISGREEDY);
	// }
	// }
	// @Test
	// public void loadTargetAnnotations() throws Exception {
	// Set<SeedInfo> seedInfo = new HashSet<SeedInfo>();
	// for (String f : featuresAll) {
	// IFeature color = EvalHelper.getFeatureByName(f);
	// seedInfo.add(new SeedInfo(color, getTargetFilename(color)));
	// }
	// measureMobileMedia("SMS_Transfer", seedInfo, ISGREEDY);
	// }

	//
	// @Test
	// public void runPhoto() throws Exception {
	// Set<SeedInfo> seedInfo = new HashSet<SeedInfo>();
	// for (String f : features) {
	// IFeature color = EvalHelper.getFeatureByName(f);
	// seedInfo.add(new SeedInfo(color, getTargetFilename(color)));
	// }
	// measureMobileMedia("View_Photo", seedInfo, ISGREEDY);
	// }

	private static class Line implements Comparable<Line> {
		final int file, line;

		public Line(int file, int line) {
			this.file = file;
			this.line = line;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Line)
				return ((Line) obj).file == file && ((Line) obj).line == line;
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return file + line * 177;
		}

		@Override
		public int compareTo(Line o) {
			if (this.file < o.file)
				return -1;
			if (this.file > o.file)
				return 1;
			if (this.line < o.line)
				return -1;
			if (this.line > o.line)
				return 1;
			return 0;
		}
	}

	private void calcTargetStatistics(IFeature color,
			Set<AElement> targetElements) {
		System.out.println("Feature " + color.getName());

		Set<Line> lines = new HashSet<Line>();
		Set<Integer> files = new HashSet<Integer>();

		for (AElement element : targetElements) {

			int hash = element.getCompelationUnitHash();
			files.add(hash);
			for (int line = element.getStartLine(); line <= element
					.getEndLine(); line++)
				lines.add(new Line(hash, line));
		}

		System.out.println("LOC " + lines.size());
		System.out.println("affected files " + files.size());

		List<Line> fragments = new ArrayList<Line>(lines);
		Collections.sort(fragments);

		// delete consecutive lines in one file (=1 fragment)
		for (int i = fragments.size() - 1; i > 0; i--) {
			if (fragments.get(i).file == fragments.get(i - 1).file)
				if (Math.abs(fragments.get(i).line - fragments.get(i - 1).line) == 1)
					fragments.remove(i);
		}
		System.out.println("fragments " + fragments.size());

	}

	private void measureMobileMedia(String featureName, String seedFileName,
			boolean isGreedy) throws Exception {
		measureMobileMedia(featureName, new String[] { seedFileName }, isGreedy);
	}

	private void measureMobileMedia(String featureName, String[] seedFileNames,
			boolean isGreedy) throws Exception {
		IFeature color = EvalHelper.getFeatureByName(featureName);
		Set<SeedInfo> seeds = new HashSet<SeedInfo>();
		for (String seedFileName : seedFileNames)
			seeds.add(new SeedInfo(color, seedFileName));
		measureMobileMedia(featureName, seeds, isGreedy);
	}

	private void measureMobileMedia(String featureName, Set<SeedInfo> seeds,
			boolean isGreedy) throws Exception {
		IProject project = EvalHelper.getProject();
		IFeature color = EvalHelper.getFeatureByName(featureName);

		String targetAnnotationFile = getTargetFilename(color);
		ApplicationController lDB = setupWorkspace(project, seeds);
		measureFeature(lDB, project, color, targetAnnotationFile, seeds,
				isGreedy);
	}

	private String getTargetFilename(IFeature color) {
		String targetAnnotationFile = "target_" + color.getName() + ".log";
		return targetAnnotationFile;
	}

	private void measureFeature(ApplicationController lDB, IProject project,
			IFeature color, String targetAnnotationFile, Set<SeedInfo> seeds,
			boolean isGreedy) throws Exception {
		Connection database = EvalHelper.getDBConnection();

		Set<String> targetNodes = AutoEval.readElements(project
				.getFile(targetAnnotationFile));
		Assert.assertTrue("no target elements found", targetNodes.size() > 0);

		Set<AElement> targetElements = getTargetElements(lDB, color,
				targetNodes);
		int targetLOC = getTargetLOC(targetElements).size();
		int runId = createRun(database, project, color, seeds, isGreedy,
				PRIORITYSETTING_NAME, targetLOC);
		writeSeeds(database, runId, seeds);
		System.out.println("Feature LOC Total: " + targetLOC);
		System.out.println("complete: "
				+ getCompleteRate(lDB, color, targetElements));
		createRow(database, runId, 0, false,
				new ARecommendationContextCollection(), getCompleteRate(lDB,
						color, targetElements), getCompleteLOC(lDB, color,
						targetElements), targetLOC, 0, "seeds");

		// find top recommendation
		int errorCounter = 0;
		int nr = 0;
		boolean match;
		MyRecommendation topRecommendation;
		double completeRate = 0;
		int completeLOC = 0;
		while ((topRecommendation = getTopRecommendation(color, lDB)) != null
				&& getCompleteRate(lDB, color, targetElements) < 1
				&& errorCounter < MAX_FAILURE) {
			match = targetNodes.contains(topRecommendation.element.getId());
			nr++;

			System.out.println("next recommendation " + color.getName() + " ("
					+ nr + "; " + errorCounter + ") =================");

			if (match) {
				errorCounter = 0;
				ColoredSourceFile source = getSource(lDB, topRecommendation);
				IASTNode foundNode = findASTNode(source, topRecommendation);
				// greedy
				Set<IASTNode> nodes = isGreedy ? extendAnnotation(foundNode,
						targetNodes) : Collections.singleton(foundNode);

				for (IASTNode node : nodes) {
					source.getColorManager().addColor(node, color);
					CIDECorePlugin.getDefault().notifyListeners(
							new ASTColorChangedEvent(this, node, source));
				}
				completeRate = getCompleteRate(lDB, color, targetElements);
				completeLOC = getCompleteLOC(lDB, color, targetElements);
			} else {
				errorCounter++;
				Map<AElement, IFeature> elementsToIgnore = new HashMap<AElement, IFeature>();
				elementsToIgnore.put(topRecommendation.element, color);
				lDB.fireEvent(new AElementsNonColorChangedEvent(this,
						elementsToIgnore, new HashMap<AElement, IFeature>()));
			}

			createRow(database, runId, nr, match, topRecommendation.context,
					completeRate, completeLOC, targetLOC, errorCounter,
					topRecommendation.element.getId());
			String csvLine = (nr + ";" + match + ";"
					+ topRecommendation.context.getSupportValue() + ";"
					+ completeRate + ";" + completeLOC + ";"
					+ (((double) completeLOC) / targetLOC) + ";" + errorCounter + "\n")
					.replace('.', ',');
			System.out.println(csvLine);
			// csv.write(csvLine);
			// csv.flush();
		}

		writeMissingElements(targetElements, color, lDB, database, runId);

	}

	private ApplicationController setupWorkspace(IProject project,
			Set<SeedInfo> seeds) throws CoreException,
			ApplicationControllerException {
		// load seeds
		new LoadSeedsJob(project, seeds)
				.runInWorkspace(new NullProgressMonitor());

		// init recommender
		ApplicationController lDB = ApplicationController.getInstance();
		lDB.initialize(project, new NullProgressMonitor());
		return lDB;
	}

	private void writeMissingElements(Set<AElement> targetElements,
			IFeature color, ApplicationController lDB, Connection database,
			int runId) throws SQLException {
		for (AElement element : targetElements)
			if (!lDB.getElementColors(element).contains(color)) {
				Statement statement2 = database.createStatement();
				statement2.executeUpdate("INSERT INTO notfound (run, astid)"
						+ " VALUES (" + runId + ",'" + element.getId() + "')");
			}

	}

	private void createRow(Connection database, int runId, int nr,
			boolean match, ARecommendationContextCollection context,
			double completeRate, int completeLOC, int targetLOC,
			int errorCounter, String astid) throws SQLException {
		Statement statement2 = database.createStatement();
		statement2
				.executeUpdate("INSERT INTO datapoint (run, nr, priority, "
						+ "priorityts, priorityta, priorityss, "
						+ "completenessel, completenessloc, foundloc, failuresinrow, iscorrect, astid)"
						+ " VALUES ("
						+ runId
						+ ", "
						+ nr
						+ ", "
						+ context.getSupportValue()
						+ ", "
						+ context.getSupportValue("TC")
						+ ", "
						+ context.getSupportValue("GR")
						+ ", "
						+ context.getSupportValue("TPF")
						+ ", "
						+ completeRate
						+ ", "
						+ (((double) completeLOC) / targetLOC)
						+ ", "
						+ completeLOC
						+ ", "
						+ errorCounter
						+ ", "
						+ (match ? "TRUE" : "FALSE") + ",'" + astid + "')");
	}

	private void writeSeeds(Connection database, int runId, Set<SeedInfo> seeds)
			throws SQLException {
		for (SeedInfo s : seeds) {
			Statement statement2 = database.createStatement();
			statement2.executeUpdate("INSERT INTO seeds (run, seed)"
					+ " VALUES (" + runId + ", '" + s.filename + "')");
		}
	}

	private int createRun(Connection database, IProject project,
			IFeature color, Set<SeedInfo> seeds, boolean isGreedy,
			String prioritysettingName, int totalLOC) throws Exception {

		Statement statement = database.createStatement();
		ResultSet result = statement
				.executeQuery("select max(id) from evalrun");
		result.next();
		int runId = result.getInt(1) + 1;

		String sql = "INSERT INTO evalrun (id, project, feature, config, ttlloc, isgreedy, date)"
				+ " VALUES ("
				+ runId
				+ ", '"
				+ project.getName()
				+ "', '"
				+ color.getName()
				+ "', '"
				+ prioritysettingName
				+ "', "
				+ totalLOC + ", " + (isGreedy ? "TRUE" : "FALSE") + ", now())";
		System.out.println(sql);
		Statement statement2 = database.createStatement();
		statement2.executeUpdate(sql);

		return runId;
	}

	private int getCompleteLOC(ApplicationController lDB, IFeature color,
			Set<AElement> targetElements) {
		Set<String> result = new HashSet<String>();

		for (AElement element : targetElements)
			if (lDB.getElementColors(element).contains(color)) {

				int hash = element.getCompelationUnitHash();
				for (int line = element.getStartLine(); line <= element
						.getEndLine(); line++)
					result.add(hash + "-" + line);
			}

		return result.size();
	}

	private Set<String> getTargetLOC(Set<AElement> targetElements) {
		Set<String> result = new HashSet<String>();

		for (AElement element : targetElements) {

			int hash = element.getCompelationUnitHash();
			for (int line = element.getStartLine(); line <= element
					.getEndLine(); line++)
				result.add(hash + "-" + line);
		}

		return result;
	}

	private String getSeedStr(Set<SeedInfo> seeds) {
		ArrayList<SeedInfo> seedList = new ArrayList<SeedInfo>(seeds);
		Collections.sort(seedList);
		String result = "";
		for (SeedInfo s : seedList) {
			result = result + "-" + s;
		}
		return result;
	}

	private Set<IASTNode> extendAnnotation(IASTNode node,
			Set<String> targetNodes) {
		Set<IASTNode> result = new HashSet<IASTNode>();
		result.add(node);
		result.addAll(findSiblings(node, targetNodes));
		while (node.getParent() != null
				&& targetNodes.contains(node.getParent().getId())) {
			node = node.getParent();
			result.add(node);
			result.addAll(findSiblings(node, targetNodes));
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private Collection<IASTNode> findSiblings(IASTNode node,
			Set<String> targetNodes) {
		if (node.getParent() == null)
			return Collections.EMPTY_SET;

		Set<IASTNode> result = new HashSet<IASTNode>();
		Property prop = node.getLocationInParent();
		if (prop instanceof PropertyZeroOrMore<?>) {
			ArrayList<IASTNode> siblings = ((PropertyZeroOrMore<IASTNode>) prop)
					.getValue();
			int idx = siblings.indexOf(node);
			assert idx > 0;
			int searchlower = idx - 1;
			while ((searchlower >= 0)
					&& targetNodes.contains(siblings.get(searchlower).getId())) {
				result.add(siblings.get(searchlower));
				searchlower--;
			}
			int searchupper = idx + 1;
			while ((searchupper < siblings.size())
					&& targetNodes.contains(siblings.get(searchupper).getId())) {
				result.add(siblings.get(searchupper));
				searchupper++;
			}
		}

		return result;
	}

	private Set<AElement> getTargetElements(ApplicationController lDB,
			IFeature color, Set<String> targetNodes) {
		HashSet<AElement> result = new HashSet<AElement>();

		for (AElement element : lDB.getAllElements())
			if (targetNodes.contains(element.getId()))
				result.add(element);

		return result;
	}

	private double getCompleteRate(ApplicationController lDB, IFeature color,
			Set<AElement> targetElements) {

		double success = 0;
		for (AElement element : targetElements)
			if (lDB.getElementColors(element).contains(color))
				success += 1;

		return success / (double) targetElements.size();
	}

	private MyRecommendation getTopRecommendation(IFeature color,
			ApplicationController lDB) {
		lDB.__script_updateRecommendations();

		ArrayList<MyRecommendation> currentRecommendations = new ArrayList<MyRecommendation>();
		for (Entry<AElement, ARecommendationContextCollection> e : lDB
				.getRecommendations(color).entrySet())
			currentRecommendations.add(new MyRecommendation(e.getKey(), e
					.getValue()));
		Collections.sort(currentRecommendations);
		if (currentRecommendations.isEmpty())
			return null;
		else
			return currentRecommendations.get(0);
	}

	private IASTNode findASTNode(ColoredSourceFile source,
			MyRecommendation recommendation)
			throws FeatureModelNotFoundException, CoreException, ParseException {
		ISourceFile ast = source.getAST();
		ASTIDFinder nodefinder = new ASTIDFinder(recommendation.element.getId());
		ast.accept(nodefinder);
		// IASTNode node = NodeFinder.perform(ast, start, len);
		IASTNode node = nodefinder.result;
		Assert.assertEquals(node.getId(), recommendation.element.getId());
		while (!node.isOptional())
			node = node.getParent();

		return node;
	}

	private static class ASTIDFinder extends ASTVisitor {
		IASTNode result = null;
		final String targetId;

		public ASTIDFinder(String id) {
			targetId = id;
		}

		@Override
		public boolean visit(IASTNode node) {
			if (node.getId().equals(targetId))
				result = node;
			return super.visit(node);
		}

	}

	private ColoredSourceFile getSource(ApplicationController lDB,
			MyRecommendation recommendation)
			throws FeatureModelNotFoundException {
		int cuHash = recommendation.element.getCompelationUnitHash();
		ICompilationUnit cu = lDB.getICompilationUnit(cuHash);
		ColoredSourceFile source = ColoredSourceFile
				.getColoredSourceFile((IFile) cu.getResource());
		return source;
	}

}

class MyRecommendation implements Comparable<MyRecommendation> {
	final AElement element;
	final ARecommendationContextCollection context;

	MyRecommendation(AElement element, ARecommendationContextCollection context) {
		this.element = element;
		this.context = context;
	}

	@Override
	public int compareTo(MyRecommendation o) {
		double v1 = this.context.getSupportValue();
		double v2 = o.context.getSupportValue();
		if (v1 > v2)
			return -1;
		if (v2 > v1)
			return 1;
		// take the bigger element
		if (this.element.getLength() > o.element.getLength())
			return -1;
		if (this.element.getLength() < o.element.getLength())
			return 1;
		// finally use astid
		return this.element.getId().compareTo(o.element.getId());
	}

	@Override
	public String toString() {
		return element.getId() + " - " + context.getSupportValue();
	}
}

class SeedInfo implements Comparable<SeedInfo> {
	SeedInfo(IFeature feature) {
		this.feature = feature;
		filename = "seed_" + feature.getName() + ".log";
	}

	SeedInfo(IFeature feature, String filename) {
		this.feature = feature;
		this.filename = filename;
	}

	final String filename;
	final IFeature feature;

	@Override
	public String toString() {
		return filename.substring(0, filename.length() - 4);
	}

	@Override
	public int compareTo(SeedInfo that) {
		return filename.compareTo(that.filename);
	}
}