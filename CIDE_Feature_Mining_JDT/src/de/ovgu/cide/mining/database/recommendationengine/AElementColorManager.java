package de.ovgu.cide.mining.database.recommendationengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;

import cide.gast.IASTNode;
import cide.gparser.ParseException;
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
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementsNonColorChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostColorChangedEvent;
import de.ovgu.cide.mining.events.AElementsPostColorChangedEvent.ColorUpdate;
import de.ovgu.cide.mining.events.AElementsPostNonColorChangedEvent;
import de.ovgu.cide.mining.events.AGenerateRecommendationsEvent;
import de.ovgu.cide.mining.logging.EvalLogging;

public class AElementColorManager implements IColorChangeListener, Observer {

	private Map<IFeature, Map<AElement, Integer>> color2Elements;
	private final Map<AElement, Set<IFeature>> element2Colors;
	private final Map<AElement, Set<IFeature>> element2NonColors;
	private Map<IFeature, Set<AElement>> nonColor2Elements;

	private Map<IFeature, Set<IFeature>> feature2relatedFeatures;
	private Map<IFeature, Set<IFeature>> feature2alternativeFeatures;

	private final static Set<IFeature> NOCOLORS = Collections.EMPTY_SET;

	private ApplicationController AC;

	public AElementColorManager(ApplicationController AC) {
		this.AC = AC;
		color2Elements = new HashMap<IFeature, Map<AElement, Integer>>();
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
				model.getSelectedAndUnselectedFeatures(new HashSet<IFeature>(),
						alwaysTrueElements, alwaysFalseElements);

				// System.out.println("ALWAYS TRUE:");
				// for (IFeature iFeature : alwaysTrueElements) {
				// System.out.println("> " + iFeature);
				// }
				// System.out.println("ALWAYS FALSE:");

				// for (IFeature iFeature : alwaysFalselements) {
				// System.out.println("> " + iFeature);
				// }

				// TEST
				// System.out.println("ALL FEATURE:");
				for (IFeature curFeature : model.getFeatures()) {

					if (alwaysFalseElements.contains(curFeature))
						continue;

					if (alwaysTrueElements.contains(curFeature))
						continue;

					System.out.println("   ->" + curFeature.getName());

					Set<IFeature> s = new HashSet<IFeature>();
					s.add(curFeature);

					Set<IFeature> selectedFeatures = new HashSet<IFeature>();
					Set<IFeature> unselectedFeatures = new HashSet<IFeature>();

					model.getSelectedAndUnselectedFeatures(s, selectedFeatures,
							unselectedFeatures);

					// remove the current selected
					selectedFeatures.remove(curFeature);

					// remove features which are always true
					selectedFeatures.removeAll(alwaysTrueElements);

					// store relations
					for (IFeature selectedFeature : selectedFeatures) {

						// check if transpose relation is already stored
						Set<IFeature> tRelatedFeatures = feature2relatedFeatures
								.get(curFeature);
						if (tRelatedFeatures != null) {
							if (tRelatedFeatures.contains(selectedFeature)) {
								// if transpose relation is stored, remove it!
								tRelatedFeatures.remove(selectedFeature);

								// remove set if there are no more relations
								if (tRelatedFeatures.isEmpty()) {
									feature2relatedFeatures.remove(curFeature);
								}
								continue;
							}
						}
						// check end

						Set<IFeature> relatedFeatures = feature2relatedFeatures
								.get(selectedFeature);
						if (relatedFeatures == null) {
							relatedFeatures = new HashSet<IFeature>();
							feature2relatedFeatures.put(selectedFeature,
									relatedFeatures);
						}

						relatedFeatures.add(curFeature);
						System.out.println("    --> SELECTED:"
								+ selectedFeature.getName());
					}

					// remove features which are always false
					unselectedFeatures.remove(alwaysFalseElements);

					// add alternative relations
					for (IFeature unselectedFeature : unselectedFeatures) {

						// //check if transpose relation is already stored
						// Set<IFeature> tAlternativeFeatures =
						// feature2relatedFeatures.get(unselectedFeature);
						// if (tAlternativeFeatures != null) {
						// if (tAlternativeFeatures.contains(curFeature)) {
						// //if transpose relation is stored, remove it!
						// tAlternativeFeatures.remove(curFeature);
						//
						// //remove set if there are no more relations
						// if (tAlternativeFeatures.isEmpty()) {
						// feature2relatedFeatures.remove(unselectedFeature);
						// }
						// continue;
						// }
						// }
						// //check end

						Set<IFeature> alternativeFeatures = feature2relatedFeatures
								.get(curFeature);

						if (alternativeFeatures == null) {
							alternativeFeatures = new HashSet<IFeature>();
							feature2alternativeFeatures.put(curFeature,
									alternativeFeatures);
						}

						alternativeFeatures.add(unselectedFeature);
						System.out.println("    --> UNSELECTED:"
								+ unselectedFeature.getName());
					}

				}
				// TEST

				// statistic!!
				// loadFeatureElements("0_SMS_Transfer", "SMS_Transfer", true);
				// loadFeatureElements("0_SMS_or_Copy", "SMS_or_Copy",true);
				// loadFeatureElements("0_View_Photo", "View_Photo", true);

			} catch (FeatureModelNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		CIDECorePlugin.getDefault().addColorChangeListener(this);
		AC.addObserver(this);
	}

	// STATISTICS!!!!!
	// public void loadFeatureElements(String path, String featureName, boolean
	// exp) {
	// Set<String> depKeys = Statistics.loadDependentFeatureElements(path, exp);
	//
	// IFeature color = null;
	//
	// for (IFeature f : AC.getProjectFeatures()) {
	// if (f.getName().equals(featureName))
	// color = f;
	// }
	//
	// int i = 0;
	// for(AIElement element: AC.getAllElements()) {
	// if (depKeys.contains(element.getId())) {
	// addElementToColor(color, element);
	// i++;
	// }
	//
	// }
	//
	// System.out.println("DEP ELEMENTS ADDED: " + i);
	// AC.fireEvent(new AGenerateRecommendationsEvent(this));
	//
	//
	// }
	// STATISTICS!!!!

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
		if (map != null)
			return map.keySet();

		return new HashSet<AElement>();
	}

	public Set<AElement> getElementsOfNonColor(IFeature color) {

		Set<AElement> set = nonColor2Elements.get(color);

		if (set != null)
			return set;

		return new HashSet<AElement>();
	}

	public Set<IFeature> getAvailableColors() {
		return color2Elements.keySet();
	}

	public boolean addElementToColor(IFeature color, AElement element) {
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

	private Set<AElement> getElementsInASTNode(int start, int end, int CUHash) {

		// FIND RELATED ELEMENTS
		Set<AElement> result = new HashSet<AElement>();

		for (AElement element : AC.getAllElements()) {
			if (element.getCompelationUnitHash() != CUHash)
				continue;

			if (element.getStartPosition() < start)
				continue;

			if ((element.getStartPosition() + element.getLength()) > end)
				continue;

			result.add(element);

		}

		return result;

	}

	private Map<String, Set<IFeature>> keys2colors = new HashMap<String, Set<IFeature>>();

	public void astColorChanged(ASTColorChangedEvent event) {
		EvalLogging.getInstance().astColorChanged(event);
		updateElementColors(event.getColoredSourceFile(),
				event.getAffectedNodes());
	}

	/**
	 * changes the color of an element and all nested elements
	 * 
	 * @param event
	 */
	private void updateElementColors(ColoredSourceFile file,
			Collection<IASTNode> nodes) {
		ICompilationUnit iCU = JDTParserWrapper.getICompilationUnit(file
				.getResource());

		int cuHashCode = iCU.hashCode();
		String cuName = file.getResource().getProjectRelativePath().toString();

		// Map<IASTNode, Set<IFeature>> node2AddColors = new HashMap<IASTNode,
		// Set<IFeature>>();
		// Map<IASTNode, Set<IFeature>> node2RemoveColors = new
		// HashMap<IASTNode, Set<IFeature>>();
		// Map<IASTNode, Set<AElement>> node2elements = new HashMap<IASTNode,
		// Set<AElement>>();
		Set<ColorUpdate> addedColorUpdates = new HashSet<AElementsPostColorChangedEvent.ColorUpdate>();
		Set<ColorUpdate> removedColorUpdates = new HashSet<AElementsPostColorChangedEvent.ColorUpdate>();

		for (IASTNode node : nodes) {

			String key = node.getId();

			Set<IFeature> oldColors = keys2colors.get(key);
			Set<IFeature> newColors = file.getColorManager().getOwnColors(node);

			Set<AElement> elements = getElementsInASTNode(
					node.getStartPosition(),
					node.getStartPosition() + node.getLength(), cuHashCode);
			Set<IFeature> addColors = new HashSet<IFeature>();
			Set<IFeature> removeColors = new HashSet<IFeature>();

			if (oldColors == null) {
				keys2colors.put(key, newColors);
				addColors.addAll(newColors);
				// CALL ADD OPERATION
			} else if (newColors == null || newColors.size() == 0) {
				// CALL REMOVE OPERATION
				keys2colors.remove(key);
				removeColors.addAll(oldColors);
			} else {

				// at least one color has been added or removed
				for (IFeature newColor : newColors) {
					if (!oldColors.contains(newColor))
						addColors.add(newColor);
				}
				for (IFeature oldColor : oldColors) {
					if (!newColors.contains(oldColor))
						removeColors.add(oldColor);
				}
				// set the new colors
				keys2colors.put(key, newColors);
			}
			if (!addColors.isEmpty())
				addedColorUpdates
						.add(new ColorUpdate(addColors, elements, node));
			if (!removeColors.isEmpty())
				removedColorUpdates.add(new ColorUpdate(addColors, elements,
						node));
		}

		if (removedColorUpdates.isEmpty() && addedColorUpdates.isEmpty())
			return;

		for (ColorUpdate addUpdate : addedColorUpdates) {
			for (IFeature color : addUpdate.colors) {
				for (AElement element : addUpdate.elements) {
					addElementToColor(color, element);
					// addedElements.put(element, color);
				}
			}
		}

		for (ColorUpdate removeUpdate : removedColorUpdates) {
			for (IFeature color : removeUpdate.colors) {
				for (AElement element : removeUpdate.elements) {
					removeElementFromColor(color, element);
					// removedElements.put(element, color);
				}
			}

		}

		AC.fireEvent(new AGenerateRecommendationsEvent(this));
		AC.fireEvent(new AElementsPostColorChangedEvent(this, cuName,
				cuHashCode, addedColorUpdates, removedColorUpdates));
	}

	public void colorListChanged(ColorListChangedEvent event) {
		// would need to update features and their relationships. probably need
		// to recreate entire model
	}

	public void fileColorChanged(FileColorChangedEvent event) {
		EvalLogging.getInstance().fileColorChanged(event);

		Collection<IContainer> folders = event.getAffectedFolders();
		// get all files
		List<ColoredSourceFile> files = new ArrayList<ColoredSourceFile>();
		for (IContainer folder : folders) {
			files.addAll(findSourceFiles(folder));

		}

		// update all files
		for (ColoredSourceFile file : files)
			try {
				updateElementColors(file,
						Collections.singleton((IASTNode) file.getAST()));
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
	}

	private Collection<ColoredSourceFile> findSourceFiles(IContainer folder) {
		List<ColoredSourceFile> result = new ArrayList<ColoredSourceFile>();

		try {
			for (IResource resource : folder.members()) {
				if (resource instanceof IFile) {
					try {
						ColoredSourceFile crf = ColoredSourceFile
								.getColoredSourceFile((IFile) resource);
						if (crf != null && crf.isColored())
							result.add(crf);
					} catch (FeatureModelNotFoundException e) {
					}
				}
				if (resource instanceof IContainer)
					result.addAll(findSourceFiles((IContainer) resource));
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return result;
	}

	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {

			if (arg instanceof AElementsNonColorChangedEvent) {
				AElementsNonColorChangedEvent event = (AElementsNonColorChangedEvent) arg;

				// ADD PART
				Map<AElement, IFeature> addedElements = new HashMap<AElement, IFeature>();
				for (Entry<AElement, IFeature> entry : event.getAddedElements()
						.entrySet()) {
					AElement elementToAdd = entry.getKey();
					IFeature color = entry.getValue();

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

				// REMOVE PART
				Map<AElement, IFeature> removedElements = new HashMap<AElement, IFeature>();
				for (Entry<AElement, IFeature> entry : event
						.getRemovedElements().entrySet()) {

					AElement elementToRemove = entry.getKey();

					IFeature color = entry.getValue();
					Set<IFeature> colors = element2NonColors
							.get(elementToRemove);

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

						if (elements.size() == 0)
							nonColor2Elements.remove(color);
					}

				}

				AC.fireEvent(new AGenerateRecommendationsEvent(this));
				AC.fireEvent(new AElementsPostNonColorChangedEvent(this,
						addedElements, removedElements));

			}

		}

	}

}