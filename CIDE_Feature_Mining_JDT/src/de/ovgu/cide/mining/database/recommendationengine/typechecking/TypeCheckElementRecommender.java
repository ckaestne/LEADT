package de.ovgu.cide.mining.database.recommendationengine.typechecking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.model.ARelation;
import de.ovgu.cide.mining.database.recommendationengine.AAbstractElementRecommender;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContext;
import de.ovgu.cide.typing.internal.manager.EvaluationStrategyManager;
import de.ovgu.cide.typing.model.IEvaluationStrategy;


public class TypeCheckElementRecommender extends AAbstractElementRecommender {
	
	
	private IEvaluationStrategy strategy;
	private IFeatureModel model;
	
	
	//private Map<AIElement, IElementTypingCheck> element2check;
	
	//private IProgressMonitor lMonitor;
	//private final HashMap<ITypingCheck, Long> markerIds;
	
	public TypeCheckElementRecommender() {
		super();
	
		
		try {
			model = FeatureModelManager.getInstance()
			.getFeatureModel(AC.getInitializedProject());
			
			strategy = EvaluationStrategyManager.getInstance()
			.getEvaluationStrategy(AC.getInitializedProject());

			
		} catch (FeatureModelNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//element2check = new HashMap<AIElement, IElementTypingCheck>();
		
		//lMonitor = monitor;
		//markerIds = new HashMap<ITypingCheck, Long>();
		
		// called from within a job!
		

	}
	
	
	public Map<AIElement, ARecommendationContext> getRecommendations(AIElement element, IFeature color) {
		

		Map<AIElement, ARecommendationContext> recommendations = new HashMap<AIElement, ARecommendationContext>();
		
		//CHECK REFERENCE CHECK		
		Set<AIElement> accessElements = AC.getRange(element, ARelation.T_BELONGS_TO);
		for (AIElement accessElement : accessElements) {
				
			ReferenceCheck check = new ReferenceCheck(element, accessElement, model);
			if (!check.evaluate(strategy) && isValidRecommendation(accessElement, color)) {
				//access element should be recommended
				ARecommendationContext context = new ARecommendationContext(element, "Check Accesses",getRecommendationType(),1);
				recommendations.put(accessElement, context);
			}
			
		}
				
		//CHECK PARAM ACCESS - VIEWPOINT PARAM DECLARATION		 
		Set<AIElement> paramTargetElements = AC.getRange(element, ARelation.REQUIRES);
		for (AIElement paramTargetElement : paramTargetElements) {
			
			Object[] bodyTargetElements = AC.getRange(paramTargetElement, ARelation.T_DECLARES_PARAMETER).toArray();
			AIElement bodyTargetElement = null;
			if (bodyTargetElements.length > 0)
				bodyTargetElement = (AIElement)bodyTargetElements[0];
			
			Set<AIElement> bodySourceElements = AC.getRange(bodyTargetElement, ARelation.BELONGS_TO);
			AIElement bodySourceElement = null;
			for (AIElement tmpElement : bodySourceElements) {
				if (tmpElement.getCategory() == AICategories.METHOD) {
					bodySourceElement= tmpElement;
					break;
				}
			}
	
			
			InvocationCheck check = new InvocationCheck(element, paramTargetElement, bodySourceElement, bodyTargetElement, model);
			
			if (!check.evaluate(strategy)) {
				//access element should be recommended
				int solutionsCount = 0;
				boolean[] solutions = new boolean[2];
			
//				AFTER EVALUATION	
//				if (bodyTargetElement != null && isValidRecommendation(bodyTargetElement, color)) {
//					solutionsCount++;
//					solutions[0] = true;
//				}
				
				if (paramTargetElement != null && isValidRecommendation(paramTargetElement, color)) {
					solutionsCount++;
					solutions[1] = true;
				}
				
				if (solutionsCount > 0) {
				
					ARecommendationContext context = new ARecommendationContext(element, "Check Param Access", getRecommendationType(), (double)1/(double)solutionsCount);
					
					if (solutions[0])
						recommendations.put(bodyTargetElement, context);
					
					if (solutions[1])
						recommendations.put(paramTargetElement, context);
				}
			}	
			
		}
		
		
		//CHECK PARAM ACCESS	 
		if (element.getCategory().equals(AICategories.PARAMETER_ACCESS)) {
			AIElement paramTargetElement = element;
			
			Object[] bodyTargetElements = AC.getRange(paramTargetElement, ARelation.T_DECLARES_PARAMETER).toArray();
			AIElement bodyTargetElement = null;
			if (bodyTargetElements.length > 0)
				bodyTargetElement = (AIElement)bodyTargetElements[0];
			
			Object[] paramSourceElements = AC.getRange(paramTargetElement, ARelation.T_REQUIRES).toArray();
			AIElement paramSourceElement = null;
			if (paramSourceElements.length > 0)
				paramSourceElement = (AIElement)paramSourceElements[0];
			
			Set<AIElement> bodySourceElements = AC.getRange(bodyTargetElement, ARelation.BELONGS_TO);
			AIElement bodySourceElement = null;
			for (AIElement tmpElement : bodySourceElements) {
				if (tmpElement.getCategory() == AICategories.METHOD) {
					bodySourceElement= tmpElement;
					break;
				}
			}
			
			InvocationCheck check = new InvocationCheck(paramSourceElement, paramTargetElement, bodySourceElement, bodyTargetElement, model);
			
			if (!check.evaluate(strategy)) {
				//create recommendations
				
				int solutionsCount = 0;
				boolean[] solutions = new boolean[3];
				
				if (bodyTargetElement != null && isValidRecommendation(bodyTargetElement, color)) {
					solutionsCount++;
					solutions[0] = true;
				}
				
				if (paramSourceElement != null && isValidRecommendation(paramSourceElement, color)) {
					solutionsCount++;
					solutions[1] = true;
				}

//				AFTER EVALUATION
//				if (bodySourceElement != null && isValidRecommendation(bodySourceElement, color)) {
//					solutionsCount++;
//					solutions[2] = true;
//				}
				
				if (solutionsCount > 0) {
				
					if (solutions[0]) {
						ARecommendationContext context = new ARecommendationContext(element, "Check Param Access", getRecommendationType(), (double)1/(double)solutionsCount);
						recommendations.put(bodyTargetElement, context);
					}
					
					ARecommendationContext context = new ARecommendationContext(element, "Check Decl.", getRecommendationType() ,(double)1 /(double)solutionsCount);
					
					if (solutions[1])
						recommendations.put(paramSourceElement, context);
					
					if (solutions[2])
						recommendations.put(bodySourceElement, context);
				}
			}	
			
		
		}
	
		
		return recommendations;
	}


	@Override
	public String getRecommendationType() {
		return "TC";
	}
	
//	public void addChecks(AIElement element) {
//		
//		Set<ITypingCheck> checks;
//		
//		// called from within a job!
//		evaluateChecks(checks);
//		
//		knownChecks.put(element, checks);
//		
//	}
//	
//	public void removeChecks(AIElement element) {
//		
//		Set<ITypingCheck> obsoleteChecks = knownChecks.get(element);
//		
//		if (obsoleteChecks == null)
//			return;
//		
//		// called from within a job!
//		knownChecks.remove(element); 
//		
//		for (ITypingCheck check : obsoleteChecks)
//			markWelltyped(check);
//		
//	}
	
	

//	/**
//	 * called from within a job
//	 * 
//	 * @param monitor
//	 */
//	public void evaluateChecks(Collection<ITypingCheck> checks) {
//		
//		if (lMonitor != null)
//			lMonitor.beginTask("Evaluating type checks...", checks.size());
//	
//		// cannot check anything without a strategy
//		if (strategy == null)
//			return;
//
//		int i = 0;
//		for (ITypingCheck check : checks) {
//			i++;
//			
//			if (lMonitor != null)
//				lMonitor.worked(1);
//
//			boolean isWelltyped = check.evaluate(strategy);
//
//			if (!isWelltyped)
//				markIlltyped(check);
//			else
//				markWelltyped(check);
//
//		}
//
//		lMonitor.done();
//	}

//	private void markWelltyped(ITypingCheck check) {
//		// remove marker in case one exists (can happen during reevaluation)
//		assert check.getFile() != null;
//		if (markerIds.containsKey(check)) {
//			long markerId = markerIds.remove(check);
//			IMarker marker = check.getFile().getResource().getMarker(markerId);
//			if (marker.exists())
//				try {
//					marker.delete();
//				} catch (CoreException e) {
//					e.printStackTrace();
//				}
//		}
//	}
//
//	private void markIlltyped(ITypingCheck check) {
//
//		try {
//			if (markerIds.containsKey(check)) {
//				long markerId = markerIds.get(check);
//				IMarker marker = check.getFile().getResource().getMarker(
//						markerId);
//				if (marker.exists()) {
//					new TypingMarkerFactory().updateErrorMarker(marker, check);
//					return;
//				}
//			}
//			IMarker marker = new TypingMarkerFactory().createErrorMarker(check);
//			markerIds.put(check, marker.getId());
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	
//	protected void clearEvaluationStrategyCache(IProject project) {
//		IEvaluationStrategy strategy;
//		try {
//			IFeatureModel featureModel = FeatureModelManager.getInstance()
//					.getFeatureModel(project);
//			strategy = EvaluationStrategyManager.getInstance()
//					.getEvaluationStrategy(project);
//			strategy.clearCache(featureModel);
//		} catch (FeatureModelNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			return;
//		}
//	}




//	/**
//	 * do not call from within a job
//	 */
//	protected void reevaluateProjectChecks(final IProject project) {
//		final HashSet<ITypingCheck> checks = new HashSet<ITypingCheck>(
//				knownChecks);
//		WorkspaceJob op = new WorkspaceJob("Reevaluate Typing") {
//
//			@Override
//			public IStatus runInWorkspace(IProgressMonitor monitor)
//					throws CoreException {
//				
////				List<ITypingProvider> typingProviders = TypingExtensionManager
////						.getInstance().getTypingProviders(project);
////				for (ITypingProvider typingProvider : typingProviders) {
////					typingProvider.prepareReevaluationAll(monitor);
////				}
//
//				// TODO currently pretty inefficient. should store
//				// association
//				// of checks to projects or files more directly
//				LinkedList<ITypingCheck> toCheck = new LinkedList<ITypingCheck>();
//				for (ITypingCheck check : checks) {
//					if (check.getFile().getResource().getProject() == project)
//						toCheck.add(check);
//				}
//				evaluateChecks(toCheck, project, monitor);
//
//				return Status.OK_STATUS;
//			}
//		};
//		op.setUser(true);
//		op.schedule();
//	}




//	/**
//	 * do not call outside this plugin. this method is only used to access
//	 * cached checks to marker resolutions
//	 */
//	public Set<ITypingCheck> getKnownChecks() {
//		return knownChecks;
//	}

}
