package de.ovgu.cide.mining.autoeval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.junit.Assert;
import org.junit.Test;

import cide.gast.ASTVisitor;
import cide.gast.IASTNode;
import cide.gast.ISourceFile;
import cide.gparser.ParseException;
import de.ovgu.cide.ASTColorChangedEvent;
import de.ovgu.cide.CIDECorePlugin;
import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;

public class RunAutoEval {

	@Test
	public void run1() throws Exception {
		String projectName = "Test";
		String featureName = "Locking";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		project.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
		Set<IFeature> features = FeatureModelManager.getInstance()
				.getFeatureModel(project).getFeatures();
		IFeature color = null;
		for (IFeature f : features)
			if (f.getName().equals(featureName))
				color = f;
		Assert.assertNotNull(color);
		Set<String> targetNodes = AutoEval.readElements(project
				.getFile("target_" + featureName + ".log"));
		Assert.assertTrue("no target elements found", targetNodes.size() > 0);

		BufferedWriter csv = new BufferedWriter(new FileWriter("log_"
				+ projectName + "_" + featureName + ".log"));
		csv.write("nr;isCorrect;priority;completeness;failuresInARow\n");

		// load seeds
		WorkspaceJob op = new LoadSeedsJob(new IProject[] { project });
		op.runInWorkspace(new NullProgressMonitor());

		// init recommender
		ApplicationController lDB = ApplicationController.getInstance();
		lDB.initialize(project, new NullProgressMonitor());

		Set<AElement> targetElements = getTargetElements(lDB, color,
				targetNodes);
		System.out.println("complete: "
				+ getCompleteRate(lDB, color, targetElements));

		// find top recommendation
		int errorCounter = 0;
		int nr = 0;
		boolean match;
		MyRecommendation topRecommendation;
		while ((topRecommendation = getTopRecommendation(color, lDB)) != null
				&& getCompleteRate(lDB, color, targetElements) < 1) {
			match = targetNodes.contains(topRecommendation.element.getId());
			nr++;

			System.out.println("next recommendation =================");
			System.out.println(topRecommendation);
			System.out.println("match: " + match);

			if (match) {
				errorCounter = 0;
				ColoredSourceFile source = getSource(lDB, topRecommendation);
				IASTNode node = findASTNode(source, topRecommendation);

				source.getColorManager().addColor(node, color);
				CIDECorePlugin.getDefault().notifyListeners(
						new ASTColorChangedEvent(this, node, source));
			} else {
				errorCounter++;
				Map<AElement, IFeature> elementsToIgnore = new HashMap<AElement, IFeature>();
				elementsToIgnore.put(topRecommendation.element, color);
				lDB.fireEvent(new AElementsNonColorChangedEvent(this,
						elementsToIgnore, new HashMap<AElement, IFeature>()));
			}
			System.out.println("complete: "
					+ getCompleteRate(lDB, color, targetElements));

			csv.write(nr + ";" + match + ";"
					+ topRecommendation.context.getSupportValue() + ";"
					+ getCompleteRate(lDB, color, targetElements) + ";"
					+ errorCounter+"\n");
		}
		;

		csv.close();
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