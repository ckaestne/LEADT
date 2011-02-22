package de.ovgu.cide.mining.autoeval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
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
import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.recommendationengine.AElementRecommendationManager;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;

public class RunAutoEval {

	static final String PRIORITYSETTING_NAME = (AElementRecommendationManager.USE_TYPESYSTEM ? "TS"
			: "")
			+ (AElementRecommendationManager.USE_TOPOLOGYANALYSIS ? "TA" : "")
			+ (AElementRecommendationManager.USE_SUBSTRINGCOMP ? "SS" : "");

	static final boolean ISGREEDY = true;
	static final int MAX_FAILURE = 50;

	@Test
	@Ignore
	public void run1() throws Exception {
		String projectName = "MobileMedia_Eval";
		String featureName = "Play_Music";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		project.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
		Set<IFeature> features = FeatureModelManager.getInstance()
				.getFeatureModel(project).getFeatures();

		IFeature color = getFeature(featureName, features);
		measureOneFeatureDefault(project, color);
	}

	@Test
	public void runPlayMusic1_Greedy() throws Exception {
		measureMobileMedia("Play_Music", "seed_Play_Music_1_1.log", ISGREEDY);
	}

	@Test
	public void runPlayMusic2_Greedy() throws Exception {
		measureMobileMedia("Play_Music", "seed_Play_Music_2_08.log", ISGREEDY);
	}

	@Test
	public void runPlayMusic3_Greedy() throws Exception {
		measureMobileMedia("Play_Music", "seed_Play_Music_3_08.log", ISGREEDY);
	}

	@Test
	public void runPlayMusic4_Greedy() throws Exception {
		measureMobileMedia("Play_Music", "seed_Play_Music_4_0707.log", ISGREEDY);
	}

	@Test
	public void runPlayMusic5_Greedy() throws Exception {
		measureMobileMedia("Play_Music", "seed_Play_Music_5_0707.log", ISGREEDY);
	}

	@Test
	public void runPlayMusic15_Greedy() throws Exception {
		measureMobileMedia("Play_Music", new String[] {
				"seed_Play_Music_1_1.log", "seed_Play_Music_2_08.log",
				"seed_Play_Music_3_08.log", "seed_Play_Music_4_0707.log",
				"seed_Play_Music_5_0707.log" }, ISGREEDY);
	}

	private void measureMobileMedia(String featureName, String seedFileName,
			boolean isGreedy) throws Exception {
		measureMobileMedia(featureName, new String[] { seedFileName }, isGreedy);
	}

	private void measureMobileMedia(String featureName, String[] seedFileNames,
			boolean isGreedy) throws Exception {
		String projectName = "MobileMedia_Eval";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		if (!project.isOpen())
			project.open(new NullProgressMonitor());
		project.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
		Set<IFeature> features = FeatureModelManager.getInstance()
				.getFeatureModel(project).getFeatures();
		IFeature color = getFeature(featureName, features);

		String targetAnnotationFile = "target_" + color.getName() + ".log";
		Set<SeedInfo> seeds = new HashSet<SeedInfo>();
		for (String seedFileName : seedFileNames)
			seeds.add(new SeedInfo(color, seedFileName));
		measureFeature(project, color, targetAnnotationFile, seeds, isGreedy);
	}

	private void measureOneFeatureDefault(IProject project, IFeature color)
			throws Exception {
		String targetAnnotationFile = "target_" + color.getName() + ".log";
		Set<SeedInfo> seeds = new HashSet<SeedInfo>();
		seeds.add(new SeedInfo(color));

		measureFeature(project, color, targetAnnotationFile, seeds, false);
	}

	private void measureFeature(IProject project, IFeature color,
			String targetAnnotationFile, Set<SeedInfo> seeds, boolean isGreedy)
			throws Exception {
		Connection database = connectToDatabase();

		Set<String> targetNodes = AutoEval.readElements(project
				.getFile(targetAnnotationFile));
		Assert.assertTrue("no target elements found", targetNodes.size() > 0);

		// BufferedWriter csv = new BufferedWriter(new FileWriter(project
		// .getName()
		// + ","
		// + color.getName()
		// + ","
		// + seedsStr
		// + ","
		// + (isGreedy ? "greedy" : "conserv")
		// + ","
		// + PRIORITYSETTING_NAME + ".csv"));
		// csv
		// .write("nr;isCorrect;priority;completenessElem;LOC;completenessLOC;failuresInARow\n");

		// load seeds
		new LoadSeedsJob(project, seeds)
				.runInWorkspace(new NullProgressMonitor());

		// init recommender
		ApplicationController lDB = ApplicationController.getInstance();
		lDB.initialize(project, new NullProgressMonitor());

		Set<AElement> targetElements = getTargetElements(lDB, color,
				targetNodes);
		int targetLOC = getTargetLOC(targetElements).size();
		int runId = createRun(database, project, color, seeds, isGreedy,
				PRIORITYSETTING_NAME, targetLOC);
		writeSeeds(database, runId, seeds);
		System.out.println("Feature LOC Total: " + targetLOC);
		System.out.println("complete: "
				+ getCompleteRate(lDB, color, targetElements));
		createRow(database, runId, 0, false, 0, getCompleteRate(lDB, color,
				targetElements), getCompleteLOC(lDB, color, targetElements),
				targetLOC, 0);

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
			double supportValue = topRecommendation.context.getSupportValue();

			System.out.println("next recommendation (" + nr + "; "
					+ errorCounter + ") =================");

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

			createRow(database, runId, nr, match, supportValue, completeRate,
					completeLOC, targetLOC, errorCounter);
			String csvLine = (nr + ";" + match + ";" + supportValue + ";"
					+ completeRate + ";" + completeLOC + ";"
					+ (((double) completeLOC) / targetLOC) + ";" + errorCounter + "\n")
					.replace('.', ',');
			System.out.println(csvLine);
			// csv.write(csvLine);
			// csv.flush();
		}

		// csv.close();
	}

	private void createRow(Connection database, int runId, int nr,
			boolean match, double supportValue, double completeRate,
			int completeLOC, int targetLOC, int errorCounter)
			throws SQLException {
		Statement statement2 = database.createStatement();
		statement2
				.executeUpdate("INSERT INTO datapoint (run, nr, priority, "
						+ "completenessel, completenessloc, foundloc, failuresinrow, iscorrect)"
						+ " VALUES (" + runId + ", " + nr + ", " + supportValue
						+ ", " + completeRate + ", "
						+ (((double) completeLOC) / targetLOC) + ", "
						+ completeLOC + ", " + errorCounter + ", "
						+ (match ? "TRUE" : "FALSE") + ")");
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

	private Connection connectToDatabase() throws SQLException,
			ClassNotFoundException {
		Class.forName("org.postgresql.Driver");

		String url = "jdbc:postgresql://localhost/autoeval";
		Properties props = new Properties();
		props.setProperty("user", "dude");
		props.setProperty("password", "supersecret");
		// props.setProperty("ssl", "false");
		return DriverManager.getConnection(url, props);

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

	private IFeature getFeature(String featureName, Set<IFeature> features) {
		IFeature color = null;
		for (IFeature f : features)
			if (f.getName().equals(featureName))
				color = f;
		Assert.assertNotNull(color);
		return color;
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
		return 0;
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