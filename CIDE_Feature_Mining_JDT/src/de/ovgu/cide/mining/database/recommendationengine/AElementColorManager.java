package de.ovgu.cide.mining.database.recommendationengine;

import java.awt.List;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;

import cide.gast.IASTNode;

import de.ovgu.cide.ASTColorChangedEvent;
import de.ovgu.cide.CIDECorePlugin;
import de.ovgu.cide.ColorListChangedEvent;
import de.ovgu.cide.FileColorChangedEvent;
import de.ovgu.cide.IColorChangeListener;
import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.language.jdt.JDTParserWrapper;
import de.ovgu.cide.language.jdt.UnifiedASTNode;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostNonColorChangedEvent;
import de.ovgu.cide.mining.events.AGenerateRecommendationsEvent;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostColorChangedEvent;
import de.ovgu.cide.mining.events.AInitEvent;
import de.ovgu.cide.mining.events.ARecommenderElementSelectedEvent;
import de.ovgu.cide.mining.featuremanager.FeatureManagerView.MESSAGE_TYPE;
import de.ovgu.cide.mining.featuremanager.model.ASTDummy;
import de.ovgu.cide.mining.featuremanager.model.CUDummy;
import de.ovgu.cide.mining.featuremanager.model.FeatureTreeNode;
import de.ovgu.cide.util.Statistics;

public class AElementColorManager implements IColorChangeListener, Observer{
	
		
	private Map<IFeature, Map<AElement, Integer>> color2Elements;
	private final Map<AElement, Set<IFeature>> element2Colors;
	private final Map<AElement, Set<IFeature>> element2NonColors;
	private Map<IFeature, Set<AElement>> nonColor2Elements;

	private Map<IFeature, Set<IFeature>> feature2relatedFeatures;
	private Map<IFeature, Set<IFeature>> feature2alternativeFeatures;
 	
	private final static Set<IFeature> NOCOLORS = Collections.EMPTY_SET;
	
	private ApplicationController AC;
	
	public AElementColorManager(ApplicationController AC)  {
		this.AC = AC;
		color2Elements = new HashMap<IFeature, Map<AElement, Integer>> ();
		element2Colors = new HashMap<AElement, Set<IFeature>>();
		
		element2NonColors = new HashMap<AElement, Set<IFeature>>();
		nonColor2Elements = new HashMap<IFeature, Set<AElement>>();
		
		feature2relatedFeatures = new HashMap<IFeature, Set<IFeature>>();
		feature2alternativeFeatures = new HashMap<IFeature, Set<IFeature>>();
		
		
		if (ApplicationController.CHECK_COLOR_RELATIONS) {
			try {
				IFeatureModel model = FeatureModelManager.getInstance()
				.getFeatureModel(AC.getInitializedProject());
				
				Set<IFeature> alwaysTrueElements = new HashSet<IFeature>();
				Set<IFeature> alwaysFalseElements = new HashSet<IFeature>();
				model.getSelectedAndUnselectedFeatures(new HashSet<IFeature>(), alwaysTrueElements, alwaysFalseElements);
	
//				System.out.println("ALWAYS TRUE:");
//				for (IFeature iFeature : alwaysTrueElements) {
//					System.out.println("> " + iFeature);
//				}
//				System.out.println("ALWAYS FALSE:");
				
//				for (IFeature iFeature : alwaysFalselements) {
//					System.out.println("> " + iFeature);
//				}
			
				//TEST
//				System.out.println("ALL FEATURE:");
				for (IFeature curFeature : model.getFeatures()) {
					
					if (alwaysFalseElements.contains(curFeature))
						continue;
					
					if (alwaysTrueElements.contains(curFeature))
						continue;
					
					System.out.println( "   ->" + curFeature.getName());
					
					
					Set<IFeature> s = new HashSet<IFeature>();
					s.add(curFeature);
					
					Set<IFeature> selectedFeatures = new HashSet<IFeature>();
					Set<IFeature> unselectedFeatures = new HashSet<IFeature>();
					
					model.getSelectedAndUnselectedFeatures(s, selectedFeatures, unselectedFeatures);
					
					//remove the current selected
					selectedFeatures.remove(curFeature);
					
					//remove features which are always true
					selectedFeatures.removeAll(alwaysTrueElements);
					
					
					
					//store relations
					for (IFeature selectedFeature : selectedFeatures) {
						
						//check if transpose relation is already stored
						Set<IFeature> tRelatedFeatures = feature2relatedFeatures.get(curFeature);
						if (tRelatedFeatures != null) {						
							if (tRelatedFeatures.contains(selectedFeature)) {
								//if transpose relation is stored, remove it!
								tRelatedFeatures.remove(selectedFeature);
								
								//remove set if there are no more relations
								if (tRelatedFeatures.isEmpty()) {
									feature2relatedFeatures.remove(curFeature);
								}
								continue;
							}
						}
						//check end
						
						
						Set<IFeature> relatedFeatures = feature2relatedFeatures.get(selectedFeature);
						if (relatedFeatures == null) {						
							relatedFeatures = new HashSet<IFeature>();
							feature2relatedFeatures.put(selectedFeature, relatedFeatures);
						}
						
						relatedFeatures.add(curFeature);			
						System.out.println("    --> SELECTED:" + selectedFeature.getName());
					}
					
					//remove features which are always false
					unselectedFeatures.remove(alwaysFalseElements);
					
					//add alternative relations				
					for (IFeature unselectedFeature : unselectedFeatures) {
						
						
//						//check if transpose relation is already stored
//						Set<IFeature> tAlternativeFeatures = feature2relatedFeatures.get(unselectedFeature);
//						if (tAlternativeFeatures != null) {						
//							if (tAlternativeFeatures.contains(curFeature)) {
//								//if transpose relation is stored, remove it!
//								tAlternativeFeatures.remove(curFeature);
//								
//								//remove set if there are no more relations
//								if (tAlternativeFeatures.isEmpty()) {
//									feature2relatedFeatures.remove(unselectedFeature);
//								}
//								continue;
//							}
//						}
//						//check end
						
						
						Set<IFeature> alternativeFeatures = feature2relatedFeatures.get(curFeature);
						
						if (alternativeFeatures == null) {
							alternativeFeatures = new HashSet<IFeature>();
							feature2alternativeFeatures.put(curFeature, alternativeFeatures);
						}
						
						alternativeFeatures.add(unselectedFeature);
						System.out.println("    --> UNSELECTED:" + unselectedFeature.getName());
					}
					
				}
				//TEST
				
				//statistic!!
	//			loadFeatureElements("0_SMS_Transfer", "SMS_Transfer", true);
	//			loadFeatureElements("0_SMS_or_Copy", "SMS_or_Copy",true);
	//			loadFeatureElements("0_View_Photo", "View_Photo", true);
							
				
			} catch (FeatureModelNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		CIDECorePlugin.getDefault().addColorChangeListener(this);
		AC.addObserver(this);
	}
	
	
	//STATISTICS!!!!!
//	public void loadFeatureElements(String path, String featureName, boolean exp) {
//		Set<String> depKeys = Statistics.loadDependentFeatureElements(path, exp);
//		
//		IFeature color = null;
//		
//		for (IFeature f : AC.getProjectFeatures()) {
//			if (f.getName().equals(featureName))
//				color = f;
//		} 
//		
//		int i = 0;	
//		for(AIElement element: AC.getAllElements()) {
//			if (depKeys.contains(element.getId())) {
//				addElementToColor(color, element);
//				i++;
//			}
//				
//		}
//							
//		System.out.println("DEP ELEMENTS ADDED: " + i);
//		AC.fireEvent(new AGenerateRecommendationsEvent(this));
//		
//		
//	}
	//STATISTICS!!!!

	public Set<IFeature> getRelatedColors(IFeature color) {
		Set<IFeature> result = feature2relatedFeatures.get(color);
		
		if (result == null)
			return new HashSet<IFeature>();
		
		return result;
	}
	
	public Set<IFeature> getRelatedNonColors(IFeature color) {
		Set<IFeature> result = feature2alternativeFeatures.get(color);
		
		if (result == null)
			return new HashSet<IFeature>();
		
		return result;
	}
	
	
	public Set<IFeature> getElementColors(AElement element) {
		Set<IFeature> colors = element2Colors.get(element);
		
		if (colors == null)
			return NOCOLORS;
		
		return colors;
		
	}
	
	public Set<IFeature> getElementNonColors(AElement element) {
		Set<IFeature> colors = element2NonColors.get(element);
		
		if (colors == null)
			return NOCOLORS;
		
		return colors;
	
	}
	
	public Set<AElement> getElementsOfColor(IFeature color) {

		Map<AElement, Integer> map = color2Elements.get(color);
		if (map!=null)
			return map.keySet();
		
		return new HashSet<AElement>();
	}
	
	public Set<AElement> getElementsOfNonColor(IFeature color) {

		Set<AElement> set = nonColor2Elements.get(color);
		
		if (set!=null)
			return set;
		
		return new HashSet<AElement>();
	}
	
	public Set<IFeature> getAvailableColors() {
		return color2Elements.keySet();
	}
	
	private boolean addElementToColor(IFeature color, AElement element) {
		Map<AElement, Integer> elementRef = color2Elements.get(color);
		
		if (elementRef == null) {
			elementRef = new HashMap<AElement, Integer>();
			color2Elements.put(color, elementRef);
		}
		
		Integer refCount = elementRef.get(element);
		
		if (refCount == null) {
			elementRef.put(element, 1);
			
			Set<IFeature> colors = element2Colors.get(element);
			
			if (colors == null) {
				colors = new HashSet<IFeature>();
				element2Colors.put(element, colors);
			}
			
			colors.add(color);
			return true;
			
		} 
		
		refCount++;
		elementRef.put(element, refCount);
		return false;
		

	}
	

	
	private boolean removeElementFromColor(IFeature color, AElement element) {
		Map<AElement, Integer> elementRef = color2Elements.get(color);
		
		if (elementRef == null) 
			return false;
		
		Integer refCount = elementRef.get(element);
		
		if (refCount == null) 
			return false;
		 
		if (refCount > 1) {
			refCount--;
			elementRef.put(element, refCount);
			return false;
		}
		
		elementRef.remove(element);

		Set<IFeature> colors = element2Colors.get(element);
		colors.remove(color);
		if (colors.size() == 0) {
			element2Colors.remove(element);
		}
		
		return true;
		
		
	}

	private Set<AElement> getElementsInASTNode(IASTNode sourceNode, int CUHash) {
		
		//FIND RELATED ELEMENTS
		UnifiedASTNode node;
		int start = sourceNode.getStartPosition();
		int end = start + sourceNode.getLength();
		Set<AElement> elements = new HashSet<AElement>();
		
		for(AElement element : AC.getAllElements()) {
			if (element.getCompelationUnitHash() != CUHash)
				continue;
						
			if (element.getStartPosition() < start)
				continue;
			
			if ((element.getStartPosition() + element.getLength()) > end)
				continue;
				
			elements.add(element);
		
		}
		
		return elements;
		
	}
	
	private Map<String, Set<IFeature>> keys2colors = new HashMap<String, Set<IFeature>>();
	
	public void astColorChanged(ASTColorChangedEvent event) {
		
		ColoredSourceFile file = event.getColoredSourceFile();
		ICompilationUnit iCU = JDTParserWrapper.getICompilationUnit(file.getResource());
	
		int cuHashCode = iCU.hashCode();
		String cuName = file.getResource().getProjectRelativePath().toString();
		
		Map<IASTNode, Set<IFeature>>  node2AddColors = new HashMap<IASTNode, Set<IFeature>>();
		Map<IASTNode, Set<IFeature>>  node2RemoveColors = new HashMap<IASTNode, Set<IFeature>>();
		Map<IASTNode, Set<AElement>>  node2elements = new HashMap<IASTNode, Set<AElement>>();
		
		Collection<IASTNode> nodes = event.getAffectedNodes();
		
		for (IASTNode node : nodes) {
			
			String key = node.getId();
			
			Set<IFeature> oldColors = keys2colors.get(key);
			Set<IFeature> newColors = file.getColorManager().getOwnColors(node);
			node2elements.put(node, getElementsInASTNode(node, cuHashCode));
			
			if (oldColors == null) {
				keys2colors.put(key, newColors);
				//CALL ADD OPERATION
				node2AddColors.put(node, newColors);
				//addASTDummyToColors(node, newColors);
				continue;
			}
			
			if (newColors == null || newColors.size() == 0) {
				//CALL REMOVE OPERATION
				keys2colors.remove(key);
				node2RemoveColors.put(node, oldColors);
				//removeASTDummyFromColors(node, oldColors);
				continue;
			}
			
			//at least one color has been added or removed
			Set<IFeature> addColors = new HashSet<IFeature>();
			for (IFeature newColor : newColors) {
				if (!oldColors.contains(newColor))
					addColors.add(newColor);
			}
			node2AddColors.put(node, addColors);
			//addASTDummyToColors(node, addColors);
			
			Set<IFeature> removeColors = new HashSet<IFeature>();
			for (IFeature oldColor : oldColors) {
				if (!newColors.contains(oldColor))
					removeColors.add(oldColor);
			}
			
			node2RemoveColors.put(node, removeColors);
			//removeASTDummyFromColors(node, removeColors);
					
			//set the new colors
			keys2colors.put(key, newColors);
				
		}
		
		//Map<AIElement, IFeature> addedElements = new HashMap<AIElement, IFeature>();
		
		for (IASTNode node : node2AddColors.keySet()) {
			Set<IFeature> colors = node2AddColors.get(node);
			Set<AElement> elements = node2elements.get(node);
			
			for (IFeature color : colors) {
				for (AElement element : elements) {
					addElementToColor(color, element);
						//addedElements.put(element, color);
				}
			}
			
		}
		
		//Map<AIElement, IFeature> removedElements = new HashMap<AIElement, IFeature>();
		for (IASTNode node : node2RemoveColors.keySet()) {
			Set<IFeature> colors = node2RemoveColors.get(node);
			Set<AElement> elements = node2elements.get(node);
			
			for (IFeature color : colors) {
				for (AElement element : elements) {
					removeElementFromColor(color, element);
						//removedElements.put(element, color);
				}
			}
			
		}
				
		
		AC.fireEvent(new AGenerateRecommendationsEvent(this));
		AC.fireEvent(new AElementsPostColorChangedEvent(this, cuName, cuHashCode, node2AddColors, node2RemoveColors, node2elements));
		
		
		
	}

	public void colorListChanged(ColorListChangedEvent event) {
	}

	public void fileColorChanged(FileColorChangedEvent event) {
	}


	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {
			
			if (arg instanceof AElementsNonColorChangedEvent) {
				AElementsNonColorChangedEvent event = (AElementsNonColorChangedEvent) arg;
				
				//ADD PART
				Map<AElement, IFeature> addedElements = new HashMap<AElement, IFeature>();
				for (AElement elementToAdd : event.getAddedElements().keySet()) {
					
					IFeature color = event.getAddedElements().get(elementToAdd);
					
					Set<IFeature> colors = element2NonColors.get(elementToAdd);
					
					if (colors == null) {
						colors = new HashSet<IFeature>();
						element2NonColors.put(elementToAdd, colors);
					}
					
					colors.add(color);
					
					
					Set<AElement> elements = nonColor2Elements.get(color);
					
					if (elements == null) {
						elements = new HashSet<AElement>();
						nonColor2Elements.put(color, elements);
					}
					
					if (elements.add(elementToAdd)) {
						addedElements.put(elementToAdd, color);
					}
					
				}	
				
				//REMOVE PART
				Map<AElement, IFeature> removedElements = new HashMap<AElement, IFeature>();
				for (AElement elementToRemove : event.getRemovedElements().keySet()) {
					
					IFeature color = event.getRemovedElements().get(elementToRemove);
					Set<IFeature> colors = element2NonColors.get(elementToRemove);
					
					if (colors != null) {
						colors.remove(color);
						
						if (colors.size() == 0)
							element2NonColors.remove(elementToRemove);
					}
					
					Set<AElement> elements = nonColor2Elements.get(color);
					
					if (elements != null) {
						if (elements.remove(elementToRemove)) {
							removedElements.put(elementToRemove, color);
						}
						
						if (elements.size() == 0 )
							nonColor2Elements.remove(color);
					}
					
				}	
							
				AC.fireEvent(new AGenerateRecommendationsEvent(this));
				AC.fireEvent(new AElementsPostNonColorChangedEvent(this, addedElements, removedElements));
			
			}
			
	
			
		}
		
	}


}