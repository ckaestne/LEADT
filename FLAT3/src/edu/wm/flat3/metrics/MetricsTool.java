package edu.wm.flat3.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.ComponentKind;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.ConcernDomain;
import edu.wm.flat3.repository.ConcernRepository;
import edu.wm.flat3.repository.EdgeKind;

public class MetricsTool
{
    final static double epsilon = 0.00001;
	
	List<Component> allTypes;
	List<Component> allMethods;
	List<Component> allFields;
	
	IConcernModelProvider concernModelProvider;

	public MetricsTool(IConcernModelProvider concernModelProvider)
	{
		this.concernModelProvider = concernModelProvider;

		allTypes = ConcernModelFactory.singleton().getModel().getComponents(
				ComponentKind.CLASS);
		allMethods = ConcernModelFactory.singleton().getModel().getComponents(
				ComponentKind.METHOD);
		allFields = ConcernModelFactory.singleton().getModel().getComponents(
				ComponentKind.FIELD);
	}

	/**
	 * Calculates metrics for all concerns in the concern domain
	 */
	public ScatteringMetricsTable getMetricsForAllConcerns()
	{
		ScatteringMetricsTable metricsTable = new ScatteringMetricsTable();
		
		getMetricsForConcernAndChildren(concernModelProvider.getModel().getRoot(), 
				metricsTable);
		
		return metricsTable;
	}

	/**
	 * Calculates metrics for a concern and its children (recursive)
	 */
	public void getMetricsForConcernAndChildren(Concern concern, 
	                                            ScatteringMetricsTable metricsTable)
	{
		DisplayableValues metricsForConcern = getMetricsForConcern(concern, null); 
		metricsTable.add(concern, metricsForConcern);
		
		for (Concern child : concern.getChildren())
		{
			getMetricsForConcernAndChildren(child, metricsTable);
		}
	}

	/**
	 * Calculates metrics for a concern
	 */
	public DisplayableValues getMetricsForConcern(final Concern concern, 
	                                              final IProgressMonitor progressMonitor)
	{
		if (progressMonitor != null && progressMonitor.isCanceled())
			return null;
		
		DisplayableValues metricForConcern = new DisplayableValues();

		Map<Component, Integer> componentContributionsForConcern =
			getContributionsForAllComponents(concern, 
					concernModelProvider.getLinkType());
		
		// Calculate DOSC, CDC, and SLOCs
		
		DOSResult doscResult = calculateDegreeOfScattering(
				componentContributionsForConcern,
				ComponentKind.CLASS,
				allTypes.size(),
				progressMonitor);
		
		metricForConcern.add(doscResult.dos);

		if (progressMonitor != null && progressMonitor.isCanceled())
			return null;

		// Calculate DOSM and CDO
		
		DOSResult dosmResult = calculateDegreeOfScattering(
				componentContributionsForConcern,
				ComponentKind.METHOD, 
				allMethods.size(), 
				progressMonitor);
		
		metricForConcern.add(dosmResult.dos);

		// Add CDC
		
		int cdc = doscResult.linkedComponents;
		metricForConcern.addValue(cdc);
		
		// Add CDO
		
		int cdo = dosmResult.linkedComponents;
		metricForConcern.addValue(cdo);

		// Add total SLOCs (calculated at the class level)

		metricForConcern.addValue(doscResult.totalLinkedSlocs);

		return metricForConcern;
	}

	/**
	 * Aggregate links for a concern.
	 * <P>
	 * A concern's links include all links beneath it in the Concern Tree
	 * view, including links beneath its child concerns.  If a type is
	 * linked the assumption is that we treat its children (methods,
	 * fields, and inner types) as also being linked.
	 *
	 * @param concern
	 * @param linkType
	 * @return
	 */
	private static Map<Component, Integer> getContributionsForAllComponents(
			Concern concern, 
			EdgeKind concernComponentRelation)
	{
		// Get links for the concern and its child concerns
		
		Set<Component> allLinkedComponents = new HashSet<Component>();
		concern.getSelfAndDesecendantLinks(concernComponentRelation, 
				allLinkedComponents);

		// We need to consider components that are directly linked as
		// well as component's whose ancestors or descendants are linked.
		
		// If the component or one of its ancestors is directly linked,
		// its contribution is its source line count.  Otherwise, its
		// contribution is the sum of the contributions of its children.
		
		Map<Component, Integer> componentContributions = 
			new HashMap<Component, Integer>(allLinkedComponents.size()*2);  
		
		for(Component linkedComponent : allLinkedComponents)
		{
			int linkedComponentsSlocs = 
				linkedComponent.getSourceRange().getNumSourceLines();

			// The component is directly linked so record its contribution
			Integer linkedComponentsOldContribution = 
				componentContributions.put(linkedComponent, linkedComponentsSlocs);
			assert linkedComponentsOldContribution == null ||
				linkedComponentsOldContribution <= linkedComponentsSlocs;
			
			// A child contributes all the way up the code model tree:
			// enclosing type, enclosing file, enclosing package, and
			// finally enclosing project.
			//
			// Since we only calculate metrics at or below the type
			// level, setting the contribution for parents above the
			// type level is not really necessary
			for(Component ancestor : linkedComponent.getAncestors())
			{
				if (linkedComponent.isKind(ComponentKind.CLASS) &&
					ancestor.isKind(ComponentKind.CLASS))
				{
					// Inner types never contribute to their outer
					// class's contribution; however, they do contribute
					// to the file, package, etc.
					continue;
				}
				
				// When the declaring type is not linked its contribution
				// is the sum of its methods' and fields' contributions
				
				Integer parentsOldContribution = componentContributions.get(ancestor);
				
				int parentsNewContribution = linkedComponentsSlocs;
				if (parentsOldContribution != null)
				{
					parentsNewContribution += parentsOldContribution;
				}
				
				// Children can only contribute to their parent's
				// contribution if the parent hasn't already contributed
				// its max amount
				
				int parentsSlocs = ancestor.getSourceRange().getNumSourceLines();
				if (parentsNewContribution <= parentsSlocs)
				{
					// Update the parent's contribution
					componentContributions.put(ancestor, parentsNewContribution);
				}
			}

			// Descendants of an linked component are also treated
			// as linked (including inner types)
			
			for(Component descendant : linkedComponent.getDescendants())
			{
				int descendantsSlocs = descendant.getSourceRange().getNumSourceLines();

				Integer descendantsOldContribution = 
					componentContributions.put(descendant, descendantsSlocs);
				
				// If the descendant's current contribution > 0 but
				// < descendantSlocs, we've already visited one of
				// its linked descendants.  If the contribution
				// == descendantSlocs, then the descendant is already
				// linked and we've already visited it, in which
				// case we didn't change the contribution.
				
				assert descendantsOldContribution == null ||
					descendantsOldContribution <= descendantsSlocs;
			}
		}
		
		return componentContributions;
	}

	/**
	 * Calculate Degree of Scattering (DOS)
	 */
	public DOSResult calculateDegreeOfScattering(Map<Component, Integer> componentContributions,
	                                    ComponentKind componentKind,
	                                    final int totalComponentsOfKind, // |T|
	                                    IProgressMonitor progressMonitor)
	{
		DOSResult result = new DOSResult();
		
		result.totalLinkedSlocs = getContributedSourceLines(
				componentContributions,
				componentKind,
				progressMonitor);
		
		if (result.totalLinkedSlocs == 0)
		{
			// Concern has no SLOCs linked to it - IGNORE 
			return result;
		}

        // Since the sum of the concentration over all components is 1,
        // the average concentration is just 1 / # components.
		final float meanConcentration = 1.0f / totalComponentsOfKind; 	// 1 / |T|
		final float meanConcentrationSquared = meanConcentration * meanConcentration;
		
		float devianceSquaredSum = 0;
		float totalConcentration = 0;
		
		int slocsLinkedToConcernCheck = 0;

		for (Map.Entry<Component, Integer> componentContribution : 
				componentContributions.entrySet())
		{
			if (progressMonitor != null && progressMonitor.isCanceled())
				return result;

			int linkedSlocs =
				getContributedSourceLines(componentContribution, componentKind);

			// Skip components that are not the right kind
			if (linkedSlocs == 0)
				continue;
			
			++result.linkedComponents;
			
	        // Concentration is the number of source lines shared by the concern
	        // and the component, divided by the total number of the concern's
	        // source lines.  The more a concern is concentrated in a component,
	        // the more of the concern's source lines will be in the component.
			float actualConcentration = 
				linkedSlocs / (float) result.totalLinkedSlocs;
			
			if (actualConcentration > 0 && actualConcentration < 1)
				actualConcentration += 0;
			
			float deviance = actualConcentration - meanConcentration;
			devianceSquaredSum += deviance * deviance;

			// For later sanity checks
			totalConcentration += actualConcentration;
			slocsLinkedToConcernCheck += linkedSlocs;
		}

		assert isNear(totalConcentration, 1.0f);
		assert slocsLinkedToConcernCheck == result.totalLinkedSlocs;

		// Accumulate the deviances of the components that were not linked
		devianceSquaredSum += meanConcentrationSquared *
			(totalComponentsOfKind - result.linkedComponents); 
		
		result.dos = 1.0f - (totalComponentsOfKind * devianceSquaredSum) / 
			(totalComponentsOfKind - 1.0f);
		
		return result;
	}

	public void getCoverageMetrics(CoverageMetricsTable coverageMetrics,
	                                IProgressMonitor progressMonitor)
	{
		int totalTypes = 0;
		int totalMethods = 0;
		int totalFields = 0;
		int totalSourceLines = 0;

		int mappedTypes = 0;
		int mappedMethods = 0;
		int mappedFields = 0;
		int mappedSourceLines = 0;

		Set<Component> unmappedTypes = new HashSet<Component>();
		Set<Component> unmappedMethods = new HashSet<Component>();
		Set<Component> unmappedFields = new HashSet<Component>();
		
		EdgeKind linkType = concernModelProvider.getLinkType();
		
		List<Component> allComponents = concernModelProvider.getModel().getComponents();
		
		if (progressMonitor != null)
			progressMonitor.beginTask("Calculating Coverage", 
					allComponents.size());
		
		for(Component component : allComponents)
		{
			if (progressMonitor != null)
			{
				if (progressMonitor.isCanceled())
					return;
				
				progressMonitor.subTask(component.getName());
				progressMonitor.worked(1);
			}
			
			int sourceLines = component.getSourceRange().getNumSourceLines();
			
			if (component.isKind(ComponentKind.CLASS))
			{
				totalSourceLines += sourceLines;
				++totalTypes;
				
				if (concernModelProvider.getModel().hasLinks(component, linkType))
				{
					++mappedTypes;
					mappedSourceLines += sourceLines;
				}
				else
				{
					boolean neverSeen = unmappedTypes.add(component);
					assert neverSeen;
				}
			}
			else if (component.isKind(ComponentKind.METHOD) ||
					component.isKind(ComponentKind.FIELD))
			{
				if (component.isKind(ComponentKind.METHOD))
					++totalMethods;
				else
					++totalFields;

				if (concernModelProvider.getModel().hasLinks(component, linkType))
				{
					if (component.isKind(ComponentKind.METHOD))
						++mappedMethods;
					else
						++mappedFields;

					// Count the mapped SLOCS.  However, prevent
					// double counting by first checking if any
					// parent will be counted.
					
					boolean ancestorLinked = false;
					
					for(Component parent : component.getAncestors())
					{
						if (concernModelProvider.getModel().hasLinks(parent, linkType))
						{
							// The class is already linked so it
							// will contribute the mapped SLOCS
							ancestorLinked = true;
							assert parent.isKind(ComponentKind.CLASS);
							break;
						}
						else if (parent.isKind(ComponentKind.CLASS))
						{
							// Don't look above classes since outer
							// classes and above do not contain the
							// SLOCS of inner classes
							break;
						}
					}

					if (!ancestorLinked)
						mappedSourceLines += sourceLines;
				}
				else
				{
					if (component.isKind(ComponentKind.METHOD))
					{
						boolean neverSeen = unmappedMethods.add(component);
						assert neverSeen;
					}
					else
					{
						boolean neverSeen = unmappedFields.add(component);
						assert neverSeen;
					}
				}
			}
		}
		
		Set<Component> unmappedMembers = new HashSet<Component>();
		unmappedMembers.addAll(unmappedMethods);
		unmappedMembers.addAll(unmappedFields);
		
		int totalMembers = totalMethods + totalFields;
		int mappedMembers = mappedMethods + mappedFields;
		
		coverageMetrics.add("Types",   
				totalTypes,       mappedTypes,       unmappedTypes);
		coverageMetrics.add("Methods", 
				totalMethods,     mappedMethods,     unmappedMethods);
		coverageMetrics.add("Fields",  
				totalFields,      mappedFields,      unmappedFields);
		coverageMetrics.add("Members",  
				totalMembers,     mappedMembers,     unmappedMembers);
		coverageMetrics.add("Source Lines",
				totalSourceLines, mappedSourceLines, new HashSet<Component>());
	
		if (progressMonitor != null)
			progressMonitor.done();
	}

	/**
	 * Returns the number of source lines contributed by all components of
	 * the given kind.
	 */
	private int getContributedSourceLines(Map<Component, Integer> componentContributions,
	                                      ComponentKind componentKind,
	                                      IProgressMonitor progressMonitor)
	{
		int totalSlocsLinkedToConcern = 0;

		for(Map.Entry<Component, Integer> componentContribution : 
			componentContributions.entrySet())
		{
			if (progressMonitor != null && progressMonitor.isCanceled())
				return -1;

			totalSlocsLinkedToConcern += 
				getContributedSourceLines(componentContribution, componentKind);
		}
		
		return totalSlocsLinkedToConcern;
	}

	/**
	 * @return
	 * 	0 if the component isn't of the specified kind,
	 * 	otherwise returns the component's contribution 
	 */
	private int getContributedSourceLines(Map.Entry<Component, Integer> componentContribution,
	                                      ComponentKind componentKind)
	{
		Component linkedComponent = componentContribution.getKey();
		
		if (!linkedComponent.isKind(componentKind))
			return 0;
		
		int contribution = componentContribution.getValue();

		assert contribution > 0;

		// An component's contribution should never be more than its
		// line count
		assert contribution <= linkedComponent.getSourceRange().getNumSourceLines();

		// Only classes can have contributions less than their line
		// count since the class may have children linked but not
		// be linked itself
		
		assert contribution == linkedComponent.getSourceRange().getNumSourceLines() ||
			linkedComponent.isKind(ComponentKind.CLASS);
		
		return contribution;
	}
	
    static boolean isNear(float lhs, float rhs)
    {
        return Math.abs(lhs - rhs) < epsilon;
    }

    /**
     * Command-line interface.
     */
	public static void main(String[] args)
			throws IOException
	{
		String pathToWorkspaceOrDatabase = null;
		String concernDomainName = null;

		if (args != null)
		{
			if (args.length >= 1)
			{
				pathToWorkspaceOrDatabase = args[0];
			}

			if (args.length >= 2)
			{
				concernDomainName = args[1];
			}
		}
		
		if (pathToWorkspaceOrDatabase == null)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.print("Enter Eclipse workspace or database directory (may contain spaces): ");
			pathToWorkspaceOrDatabase = reader.readLine();
			reader.close();
		}

		ConcernRepository hsqldb = ConcernRepository.openDatabase(pathToWorkspaceOrDatabase, false);
		if (hsqldb == null)
		{
			System.err.println("Failed to open database: "
					+ pathToWorkspaceOrDatabase);
			return;
		}
		
		List<ConcernDomain> concernDomains = hsqldb.getConcernDomains(null);

		if (concernDomainName == null)
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.print("Enter concern domain name (one of: " + concernDomains.toString() + "): ");
			concernDomainName = reader.readLine();
			reader.close();
		}
		
		if (concernDomainName != null && !concernDomains.contains(concernDomainName))
		{
			System.err.println("Unknown concern domain: '" + concernDomainName + "'. " +
					"Expected one of: " + concernDomains.toString());
			return;
		}

		// TODO: Must pick concern-component relation
		
		// This call is needed to initialize the default IConcernModelProvider
		ConcernModelFactory.singleton().getConcernModel(
					hsqldb, concernDomainName);

		MetricsTool metricsTool = new MetricsTool(ConcernModelFactory.singleton());

		MetricsTable metricsTable = metricsTool.getMetricsForAllConcerns();

		metricsTable.output(System.out);

		System.out.println();
		//metricsTool.printLinkStatistics();
		
		hsqldb.shutdown();
	}
	
	class DOSResult
	{
		float dos = Float.NaN;
		int totalLinkedSlocs;
		int linkedComponents;
	}
}
