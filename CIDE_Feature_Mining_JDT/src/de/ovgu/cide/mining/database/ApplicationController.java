/* JayFX - A Fact Extractor Plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~swevo/jayfx)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.7 $
 */

package de.ovgu.cide.mining.database;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import cide.gast.ASTNode;
import cide.gast.IASTNode;

import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.model.AFlyweightElementFactory;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.model.ARelation;
import de.ovgu.cide.mining.database.recommendationengine.AElementColorManager;
import de.ovgu.cide.mining.database.recommendationengine.AElementRecommendationManager;
import de.ovgu.cide.mining.database.recommendationengine.AElementViewCountManager;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;
import de.ovgu.cide.mining.database.recommendationengine.typechecking.TypeCheckElementRecommender;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostColorChangedEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode;
import de.ovgu.cide.typing.internal.manager.EvaluationStrategyManager;
import de.ovgu.cide.typing.model.IEvaluationStrategy;
import de.ovgu.cide.typing.model.ITypingCheck;

/**
 * Facade for the JavaDB component. This component takes in a Java projects and
 * produces a database of the program relations between all the source element
 * in the input project and dependent projects.
 */
public class ApplicationController extends Observable{

	IProject initializedProject = null;
	public static final boolean CHECK_COLOR_RELATIONS = true;

	// The database object should be used for building the database
	private ProgramDatabase aDB;
	private AElementColorManager elementColorManager;
	private AElementViewCountManager viewCountManager;
	//private TypeCheckManager typingManager;
	private AElementRecommendationManager elementRecommendationManager;
	
//	private IEvaluationStrategy evaluationStrategy;
	
	private AFlyweightElementFactory elementFactory ;
	private Set<IFeature> projectFeatures;
	

	private Map<Integer, ICompilationUnit> compUnitMap;

	// The analyzer is a wrapper providing additional query functionalities
	// to the database.
	private AAnalyzer aAnalyzer;


	private static ApplicationController AC = null;

	private ApplicationController() {
		AC = null;
		initializedProject = null;
	}
	
	public int getViewCountForElement(AIElement element) {
		return viewCountManager.getViewCountForElement(element);
	}
	
	public Set<IFeature> getElementNonColors(AIElement element) {
		return elementColorManager.getElementNonColors(element);
	}
	
	public Set<IFeature> getElementColors(AIElement element) {
		return elementColorManager.getElementColors(element);
	}
	
	public Set<AIElement> getElementsOfColor(IFeature color) {
		return elementColorManager.getElementsOfColor(color);
	}
	public Set<AIElement> getElementsOfNonColor(IFeature color) {
		return elementColorManager.getElementsOfNonColor(color);
	}
	
	public Set<IFeature> getRelatedColors(IFeature color) {
		return elementColorManager.getRelatedColors(color);
	}
	
	public Set<IFeature> getRelatedNonColors(IFeature color) {
		return elementColorManager.getRelatedNonColors(color);
	}
	
	
	public Set<IFeature> getAvailableColors() {
		return elementColorManager.getAvailableColors();
	}
	
	public Map<IFeature,  ARecommendationContextCollection> getAllRecommendations(AIElement element) {
		return elementRecommendationManager.getAllRecommendations(element);
	}
	
	
	public Map<AIElement, ARecommendationContextCollection> getRecommendations(IFeature color, AIElement element) {
		return elementRecommendationManager.getRecommendations(color, element);
	}
	
	public int getRecommendationsCount(IFeature color, AIElement element) {
		return elementRecommendationManager.getRecommendationsCount(color, element);
	}
	
	public int getRecommendationsCount(IFeature color, int start, int end, int cuhash) {
		return elementRecommendationManager.getRecommendationsCount(color, start, end, cuhash);
	}
	
	public Map<AIElement, ARecommendationContextCollection> getRecommendations(IFeature color, int start, int end, int cuhash) {
		return elementRecommendationManager.getRecommendations(color, start, end, cuhash);
	}

	public static ApplicationController getInstance() {
		if (AC == null)
			AC = new ApplicationController();
		return AC;
	}

	/**
	 * Returns an IElement describing the argument Java element. Not designed to
	 * be able to find initializer blocks or arrays.
	 * 
	 * @param pElement
	 *            Never null.
	 * @return Never null
	 * @throws ConversionException
	 *             if the element cannot be converted.
	 */
	public AIElement convertToElement(ASTNode astNode)
			throws ConversionException {

		return aDB.getElement(astNode.getId());

	}

	public Set<AIElement> getRange(AIElement pElement, ARelation pRelation) {

		return aAnalyzer.getRange(pElement, pRelation);
	}

	/**
	 * Returns all the elements in the database in their lighweight form.
	 * 
	 * @return A Set of IElement objects representing all the elements in the
	 *         program database.
	 */
	public Set<AIElement> getAllElements() {

		return aDB.getAllElements();
	}

	/**
	 * Returns whether pElements has any associated relations.
	 * 
	 * @param pElement
	 *            The element to check. Must not be null and exist in the
	 *            database.
	 * @return True if pElement has any associated relations.
	 * @throws ElementNotFoundException
	 *             If either pFrom or pTo is not indexed in the database.
	 */
	public boolean hasRelations(AIElement pElement)
			throws ElementNotFoundException {

		return aDB.hasRelations(pElement);
	}

	public ICompilationUnit getICompilationUnit(int hash) {
		return compUnitMap.get(hash);
	}
	
//	public AElementColorManager getElementColorManager() {
//		return elementColorManager;
//	}

	/**
	 * Initializes the program database with information about relations between
	 * all the source elements in pProject and all of its dependent projects.
	 * 
	 * @param pProject
	 *            The project to analyze. Should never be null.
	 * @param pProgress
	 *            A progress monitor. Can be null.
	 * @param pCHA
	 *            Whether to calculate overriding relationships between methods
	 *            and to use these in the calculation of CALLS and CALLS_BY
	 *            relations.
	 * @throws ApplicationControllerException
	 *             If the method cannot complete correctly
	 */
	public void initialize(IProject pProject, IProgressMonitor pProgress)
			throws ApplicationControllerException {
		assert (pProject != null);

		// The database object should be used for building the database
		aDB = new ProgramDatabase();
		elementFactory = new AFlyweightElementFactory();

		// The analyzer is a wrapper providing additional query functionalities
		// to the database.
		aAnalyzer = new AAnalyzer(aDB);

		compUnitMap = new HashMap<Integer, ICompilationUnit>();

		initializedProject = pProject;

		// Collect all target classes
		List<ICompilationUnit> lTargets = new ArrayList<ICompilationUnit>();
		for (IJavaProject lNext : getJavaProjects(pProject)) {
			lTargets.addAll(getCompilationUnits(lNext));
		}

		if (pProgress != null)
			pProgress.beginTask("Building program database", lTargets.size());

		ADeclareRelationBuilder loader = new ADeclareRelationBuilder(aDB, elementFactory);

		for (ICompilationUnit lCU : lTargets) {

			int lCUHash = lCU.hashCode();
			compUnitMap.put(lCUHash, lCU);

			loader.createElementsAndDeclareRelations(lCU, lCUHash);

			if (pProgress != null)
				pProgress.worked(1);
		}

		AAccessRelationBuilder relationBuilder = new AAccessRelationBuilder(aDB, elementFactory);
		for (ICompilationUnit lCU : lTargets) {
			int lCUHash = lCU.hashCode();
			relationBuilder.buildRelations(lCU, lCUHash);

			if (pProgress != null)
				pProgress.worked(1);
		}	
				
//		try {
//			evaluationStrategy = EvaluationStrategyManager.getInstance()
//					.getEvaluationStrategy(pProject);
//			
//	
//			
//		} catch (FeatureModelNotFoundException e1) {
//			e1.printStackTrace();
//		}
//	
		
		try {
			IFeatureModel model = FeatureModelManager.getInstance()
				.getFeatureModel(initializedProject);
			
			projectFeatures = model.getFeatures();
		
				
		} catch (FeatureModelNotFoundException e) {
				// TODO Auto-generated catch block
				projectFeatures = new HashSet<IFeature>();
		}
		
		elementColorManager = new AElementColorManager(this);
		elementRecommendationManager = new AElementRecommendationManager(this, elementColorManager);
		viewCountManager = new AElementViewCountManager(this);
	
		
		
		
		fireEvent(new AInitEvent(this, initializedProject));
		
		pProgress.done();
		
		

	}
	
//	public IEvaluationStrategy getEvaluationStrategy() {
//		return evaluationStrategy;
//	}
	
	public IProject getInitializedProject() {
		return initializedProject;
	}
	
	public Set<IFeature> getProjectFeatures() {
		return projectFeatures;
	}
	

	
	public void fireEvent(EventObject event) {
		setChanged();
		notifyObservers(event);
	}

	
	/**
	 * Returns all projects to analyze in IJavaProject form, including the
	 * dependent projects.
	 * 
	 * @param pProject
	 *            The project to analyze (with its dependencies. Should not be
	 *            null.
	 * @return A list of all the dependent projects (including pProject). Never
	 *         null.
	 * @throws ApplicationControllerException
	 *             If the method cannot complete correctly.
	 */
	private static List<IJavaProject> getJavaProjects(IProject pProject)
			throws ApplicationControllerException {
		assert (pProject != null);

		List<IJavaProject> lReturn = new ArrayList<IJavaProject>();
		try {
			lReturn.add(JavaCore.create(pProject));
			IProject[] lReferencedProjects = pProject.getReferencedProjects();
			for (int i = 0; i < lReferencedProjects.length; i++) {
				lReturn.add(JavaCore.create(lReferencedProjects[i]));
			}
		} catch (CoreException pException) {
			throw new ApplicationControllerException("Could not extract project information",
					pException);
		}
		return lReturn;
	}

	/**
	 * Returns all the compilation units in this projects
	 * 
	 * @param pProject
	 *            The project to analyze. Should never be null.
	 * @return The compilation units to generate. Never null.
	 * @throws ApplicationControllerException
	 *             If the method cannot complete correctly
	 */
	private static List<ICompilationUnit> getCompilationUnits(
			IJavaProject pProject) throws ApplicationControllerException {
		assert (pProject != null);

		List<ICompilationUnit> lReturn = new ArrayList<ICompilationUnit>();

		try {
			IPackageFragment[] lFragments = pProject.getPackageFragments();
			for (int i = 0; i < lFragments.length; i++) {
				ICompilationUnit[] lCUs = lFragments[i].getCompilationUnits();
				for (int j = 0; j < lCUs.length; j++) {
					lReturn.add(lCUs[j]);
				}
			}
		} catch (JavaModelException pException) {
			throw new ApplicationControllerException(
					"Could not extract compilation units from project",
					pException);
		}
		return lReturn;
	}

}
