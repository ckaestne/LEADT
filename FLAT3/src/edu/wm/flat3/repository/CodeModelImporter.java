package edu.wm.flat3.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.CodeModelRule;
import edu.wm.flat3.FLATTT;
import edu.wm.flat3.util.LineSet;
import edu.wm.flat3.util.ProblemManager;

/**
 * Generates the code model and persists it to the database. It takes
 * a handle to the project using the <code>IJavaProject</code> class. It then
 * persists all the packages in the class. For each package, it persists all the
 * files. For each file it persists all the top level classes. For each class,
 * it persists the class, its inner classes, all the fields and all the methods.
 * 
 * The model generation process depends on the AST Parser to get statement and
 * line number information. This requires that the parser resolve all the
 * bindings. In order for this to occur, Eclipse requires that there be no
 * compilation errors in the class.
 * 
 * Currently, the line number information for a class/method/field is calculated
 * using the starting position of the class/method/field name because the char
 * position of the modifiers is not available
 * 
 * @author Vibhav Garg
 * 
 */
public class CodeModelImporter
{
	// handle to the database handler class
	private ConcernRepository repository;
	private boolean persistAnswer = false;
	private IJavaProject project = null;
	private Job job = null;

	private List<Component> allComponents;
	
	public CodeModelImporter(ConcernRepository repository, IJavaProject project)
	{
		this.repository = repository;
		this.project = project;
	}

	/**
	 * Persist the project to the database. This is the entry method into this
	 * class
	 * 
	 * @param project
	 */
	public void run()
	{
		if (job != null && job.getState() != Job.NONE)
			return;
		
		try
		{
			// make sure the user actually wants to persist the project.
			persistAnswer = MessageDialog.openQuestion(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				FLATTT.getResourceString("actions.PersistToDBAction.DialogTitle"),
				"This action will generate the code model for project "+project.getElementName()+". Do you want to proceed?");
				//FLATTT.getResourceString("actions.PersistToDBAction.DialogMessage"));

			if (persistAnswer)
			{
				job = new Job("Importing " + project.getElementName() + " project")
					{
						@Override
						protected IStatus run(IProgressMonitor monitor)
						{
							IStatus stat = importProject(project, monitor);
							return stat;
						}
					};
				job.setRule(new CodeModelRule());
				//job.setUser(true); // TODO: make it user if standalone, nonuser if along with lucene
				job.schedule();
			}
		}
		catch (Exception e)
		{
			ProblemManager.reportException(e, true);
		}
	}
	
	public boolean isCanceled()
	{
		return !persistAnswer;
	}

 	private IStatus importProject(	IJavaProject project, 
									IProgressMonitor monitor)
	{
		allComponents = repository.getAllComponents();
		
		Component projectComponent = getOrCreateProjectAndComponentDomain(project);
		if (projectComponent == null)
			return new Status(Status.ERROR, FLATTT.ID_PLUGIN,
					"Failed to import project: " + project.getElementName());
		
		try
		{
			IPackageFragment[] packageFragments = project.getPackageFragments();
			IStatus result = importPackages(projectComponent, packageFragments, monitor);

//			for(Component orphanedComponent : allComponents)
//			{
//				//System.out.println("removing:" +orphanedComponent);
//				ProblemManager.reportError("Orphaned Component", 
//						"Orphaned component: " + orphanedComponent.getName(), 
//						orphanedComponent.getHandle());
//				orphanedComponent.remove(); // FIXME: is this bad??? should we really remove them? but if we do, it takes as long as adding them would!
//			}
			
			return result;
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e, true);
			return new Status(Status.ERROR, FLATTT.ID_PLUGIN,
					"Failed to obtain package fragments for project: " +
						project.getElementName(), e);
		}
	}
	
	/**
	 * Persist the packages to the database.
	 * 
	 * @param projectComponent
	 * @param packageFragments
	 */
	private IStatus importPackages(Component projectComponent,
			IPackageFragment[] packageFragments,
			IProgressMonitor monitor)
	{
		int taskSize = 0;
		
		for (IPackageFragment packageFragment : packageFragments)
		{
			taskSize += countTypes(packageFragment);
		}
		
		monitor.beginTask("Importing " + projectComponent.getName() + " project", 
				taskSize);

		for (IPackageFragment packageFragment : packageFragments)
		{
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			
			// Shouldn't we import the default package?  Otherwise,
			// ConcernTagger won't work with projects that don't
			// specify a package.
		/*	if (packageFragment.isDefaultPackage())
			{
				monitor.worked(countTypes(packageFragment));
				continue;
			}*/

			String packageName = packageFragment.getElementName();
			
			ICompilationUnit[] compilationUnits = null;
			
			try
			{
				compilationUnits = packageFragment.getCompilationUnits();
			}
			catch (JavaModelException e)
			{
				ProblemManager.reportException(e,
						"Failed to obtain compilation units for package "
						+ packageName, true);
				continue;
			}

			// Ignore empty packages
			if (compilationUnits.length == 0)
				continue;
			
			System.out.println("Importing package " + packageName);

			// Check if component already exists
			// Packages aren't considered to have a source range
			Component packageComponent = getOrCreateComponent(packageFragment);
			if (packageComponent == null)
			{
				// Already reported failure
				monitor.worked(countTypes(packageFragment));
				continue;
			}

			if (!projectComponent.isConnected(packageComponent))
			{
				projectComponent.addChild(packageComponent);
			}

			// Persist all files in the package
			importFiles(compilationUnits, packageComponent, monitor);
		}
		
		monitor.done();
		
		return Status.OK_STATUS;
	}

	/**
	 * Persists the files to the database.
	 * 
	 * @param compilationUnits
	 * @param packageComponent
	 * @throws JavaModelException
	 * @throws SQLException
	 */
	private void importFiles(	ICompilationUnit[] compilationUnits,
								Component packageComponent,
								IProgressMonitor monitor)
	{
		for (ICompilationUnit compilationUnit : compilationUnits)
		{
			if (monitor.isCanceled())
				return;
			
			Component fileComponent = importFile(compilationUnit,  monitor);
			if (fileComponent == null)
			{
				ProblemManager.reportInfo(
						"Failed to Import Compilation Unit " + compilationUnit,
						null);
				monitor.worked(countTypes(compilationUnit));
				continue;
			}
			
			if (!packageComponent.isConnected(fileComponent))
			{
				packageComponent.addChild(fileComponent);
			}
		}
	}

	/**
	 * Create the component domain. This will be the project in this case.
	 * 
	 * @param project
	 * @return
	 * @throws SQLException
	 */
	private Component getOrCreateProjectAndComponentDomain(IJavaElement project)
	{
		Component component = getComponent(project);
		if (component != null)
		{
			allComponents.remove(component);
			return component;
		}

		ComponentDomain domain = new ComponentDomain(project.getElementName(), 
				"Java");
		
		// Projects aren't considered to have a source range (but
		// they should!-ME)
		SourceRange emptySourceRange = new SourceRange(0,0,0,0,0);
		
		component = Component.createComponent(repository, 
				project,
				emptySourceRange,
				project.getElementName(),
				domain);
		
		allComponents.remove(component);
		
		return component;
	}

	/**
	 * Parse file and persist all top-level classes.
	 * 
	 * @param icu
	 * @return
	 * @throws JavaModelException
	 * @throws SQLException
	 */
	public Component importFile(ICompilationUnit icu, IProgressMonitor monitor)
	{
		// Projects aren't considered to have a source range (but
		// they should!-ME)
		Component fileComponent = getOrCreateComponent(icu);
		if (fileComponent == null)
			return null;
		
		// Parse file

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(icu);

		// Make sure we set resolve bindings to true
		parser.setResolveBindings(true);
		
		// Create AST
		ASTNode root = parser.createAST(null);

		CompilationUnit cu = (CompilationUnit) root;

		LineSet sourceLineCounter = new LineSet(cu);
		
		// Get all top level classes

		for (Object typeObj : cu.types())
		{
			if (monitor.isCanceled())
				return null;
			
			AbstractTypeDeclaration type = (AbstractTypeDeclaration) typeObj;
			
			ITypeBinding binding = type.resolveBinding();
			if (binding == null)
			{
				ProblemManager.reportInfo( 
						"Binding for " + type.getName() + " is null", null);
				monitor.worked(1);
				continue;
			}
			
			IType javaType = (IType) binding.getJavaElement();
			assert javaType != null;

			monitor.subTask(javaType.getElementName());
			
			// make sure we only do classes and enums and which have source
			// code.
			
			boolean isAnnotation = false;
			
			try
			{
				isAnnotation = javaType.isAnnotation();
			}
			catch (JavaModelException e)
			{
				ProblemManager.reportException(e, true);
				// Fall through. Assume the type is not an annotation.
			}
			
			if (!isAnnotation && !javaType.isBinary())
			{
				Component typeComponent = importType(cu, javaType, type, 
						sourceLineCounter);
				if (typeComponent == null)
				{
					ProblemManager.reportError("Code Model Import Failure", 
							"Failed to import class: " + javaType.getFullyQualifiedName(),
							null, true);
					monitor.worked(1);
					continue;
				}

				if (!fileComponent.isConnected(typeComponent))
				{
					fileComponent.addChild(typeComponent);
				}
			}

			monitor.worked(1);
		}

		return fileComponent;
	}

	/**
	 * Persist a class to the database.
	 * 
	 * @param compilationUnit
	 * @param typeElement
	 * @param astType
	 * @return
	 * @throws SQLException
	 * @throws JavaModelException
	 */
	private Component importType(	CompilationUnit compilationUnit,
	                                IType typeElement, 
	                                AbstractTypeDeclaration astType,
	                                LineSet sourceLineCounter)
	{
		System.out.println("Importing type: " + typeElement.getElementName());
		
		List<Component> childComponents = new ArrayList<Component>();

		int childrenSlocs = 0;

		// Import static constructors

		List<Component> initializerComponents = 
			importInitializers(compilationUnit, typeElement, sourceLineCounter);
		if (initializerComponents == null)
			return null; // Already reported error
		
		for(Component initializerComponent : initializerComponents)
		{
			childComponents.add(initializerComponent);
			childrenSlocs += initializerComponent.getSourceRange().getNumSourceLines();
		}
		
		// Import members (fields, methods, and inner classes)
		
		for (Object bodyDeclaration : astType.bodyDeclarations())
		{
			if (bodyDeclaration instanceof MethodDeclaration)
			{
				Component method = importMethodComponent(compilationUnit,
						(MethodDeclaration) bodyDeclaration, sourceLineCounter);
				if (method == null)
					return null; // Already reported error
				
				childComponents.add(method);
				childrenSlocs += method.getSourceRange().getNumSourceLines();
			}
			else if (bodyDeclaration instanceof FieldDeclaration)
			{
				List<Component> fields = importFieldComponents(compilationUnit,
						(FieldDeclaration) bodyDeclaration, sourceLineCounter);
				if (fields == null)
					return null; // Already reported error
				
				for(Component field : fields)
				{
					childComponents.add(field);
					childrenSlocs += field.getSourceRange().getNumSourceLines();
				}
			}
			else if (bodyDeclaration instanceof AbstractTypeDeclaration)
			{
				AbstractTypeDeclaration innerClassDecl = 
					(AbstractTypeDeclaration) bodyDeclaration;
				IType innerClassElem = 
					(IType) innerClassDecl.resolveBinding().getJavaElement();
				
				if (!innerClassElem.isBinary())
				{
					// recursively persist inner classes and inner enums
					Component innerTypeComponent = importType(compilationUnit, 
							innerClassElem, innerClassDecl, sourceLineCounter);
					if (innerTypeComponent == null)
						return null; // Already reported error

					// We don't include inner classes in the line count for
					// the outer class.  However, keep in mind that inner
					// classes inherit the links of their outer class.
					
					childComponents.add(innerTypeComponent);
				}
			}
			// Skip initializers since we already added them
			else if (!(bodyDeclaration instanceof Initializer))
			{
				ProblemManager.reportError("Unknown Construct",
						"Unexpected construct found in type " + 
						typeElement.getFullyQualifiedName() + ": " +
						bodyDeclaration.toString(),
						bodyDeclaration.getClass().toString(),
						true);
			}
		}

		SourceRange sourceRange = SourceRange.create(compilationUnit, 
				astType, childrenSlocs, typeElement.getElementName(), 
				sourceLineCounter);
		
		Component typeComponent = getOrCreateComponent(typeElement, sourceRange);
		if (typeComponent == null)
			return null; // Already reported error
		
		for (Component child : childComponents)
		{
			//System.out.println("Importing: " + child.getName());
			
			if (!typeComponent.isConnected(child))
				typeComponent.addChild(child);
		}

		return typeComponent;
	}

	/**
	 * Adds the static initializer components if found in class
	 * 
	 * @param compilationUnit
	 * @param sourceType
	 */
	private List<Component> importInitializers(	CompilationUnit compilationUnit, 
												IType typeElement,
												LineSet sourceLineCounter)
	{
		IInitializer[] initializers = null;
		
		try
		{
			initializers = typeElement.getInitializers();
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e,
					"Failed to retrieve initializers for type: " + 
					typeElement.getFullyQualifiedName(), true);
			return null;
		}

		List<Component> initializerComponents = new ArrayList<Component>();
		
		int i = 0;

		for (IInitializer initializerElement : initializers)
		{
			// Give it a fake name, since there is no name for a static init
			String initializerName = typeElement.getElementName() + 
				".<static constructor>[" + i++ + "]";
			
			SourceRange sourceRange = SourceRange.create(compilationUnit, 
					initializerElement, initializerName, sourceLineCounter);
			
			Component initializerComponent = getOrCreateComponent(initializerElement,
					sourceRange, initializerName);
			if (initializerComponent == null)
				return null; // Already reported error
			
			initializerComponents.add(initializerComponent);
		}
		
		return initializerComponents;
	}

	/**
	 * Handle single and multi-field declarations:
	 * 
	 * int i = 0;		Single field (i is associated with 1 source line)
	 * 
	 * 		or
	 * 
	 * int
	 * 		i = 0;		Single field (i is associated with 2 source lines)
	 * 
	 * 		or
	 * 
	 * int 				Multiple fields:
	 * 		i = 0,		  i is only associated with 1 source line (this one)
	 * 		j = 1;		  j is only associated with 1 source line (this one)
	 * 
	 * int i, j;		Multiple fields:  Both fields will get their numLines
	 * 					set to 1.  The type will not double count though. 
	 * 
	 */
	private List<Component> importFieldComponents(CompilationUnit compilationUnit,
			FieldDeclaration fieldDeclaration,
			LineSet sourceLineCounter)
	{
		List<Component> fields = new ArrayList<Component>();

		int totalFields = fieldDeclaration.fragments().size();
		
		for (Object frag : fieldDeclaration.fragments())
		{
			VariableDeclarationFragment vdFragment = 
				(VariableDeclarationFragment) frag;
			
			IVariableBinding binding = vdFragment.resolveBinding();
			if (binding == null)
			{
				ProblemManager.reportError("Failed to Import Field", 
						"Failed to import field",  
						null, true);
				return null;
			}

			IJavaElement fieldElement = binding.getJavaElement();

			String fieldName = fieldElement.getParent().getElementName() + "." +
				fieldElement.getElementName();

			// Judgement call: Only single field decls can claim
			// the source line associated with the field decl type when
			// the type is on a different line than the field name.  There
			// is no clear "owner" of the type's source line when there
			// are multiple fields, therefore each field only gets a single
			// source line (the one that the field name is on)
			
			SourceRange sourceRange = SourceRange.create(compilationUnit, 
					totalFields == 1 ? fieldDeclaration.getType() : null, 
					vdFragment, 
					fieldName,
					sourceLineCounter);
			
			Component fieldComponent = getOrCreateComponent(fieldElement, 
					sourceRange, fieldName); 
			if (fieldComponent == null)
				return null; // Already reported error
			
			fields.add(fieldComponent);
		}

		return fields;
	}

	/**
	 * Persist the method to the database.
	 * 
	 * @param compilationUnit
	 * @param methodDeclaration
	 * @return
	 * @throws SQLException
	 * @throws JavaModelException
	 */
	private Component importMethodComponent(CompilationUnit compilationUnit,
			MethodDeclaration methodDeclaration,
			LineSet sourceLineCounter)
	{
		// getStatements(methodDeclaration);
		IMethodBinding binding = methodDeclaration.resolveBinding();
		IMethod methodElement = null; //FIXME: Why is this ever null??
		if (binding != null) methodElement = (IMethod) binding.getJavaElement();
		if (methodElement == null)
		{
			ProblemManager.reportError("Failed to Import Component", 
					"Failed to import method " + methodDeclaration.toString(),
					//	methodElement.getParent().getElementName() + 
						//"." + methodElement.getElementName(),  
					"Handle: " //+ methodElement.getHandleIdentifier()
					, true);
			return null;
		}

		String methodName = methodElement.getParent().getElementName() + "." +
			methodElement.getElementName();
	
		SourceRange sourceRange = SourceRange.create(compilationUnit, 
				methodDeclaration, methodName, sourceLineCounter);
		
		return getOrCreateComponent(methodElement, sourceRange, methodName);
	}

	private int countTypes(IPackageFragment packageFragment)
	{
		int count = 0;
		
		try
		{
			for(ICompilationUnit compilationUnit : packageFragment.getCompilationUnits())
			{
				count += countTypes(compilationUnit);
			}
		}
		catch (JavaModelException e)
		{
			// Ignore exception, we just want a rough count
		}
		
		return count;
	}		
	
	private int countTypes(ICompilationUnit compilationUnit)
	{
		try
		{
			return compilationUnit.getAllTypes().length;
		}
		catch (JavaModelException e)
		{
			// Ignore exception, we just want a rough count
			return 0;
		}
	}

	private Component getOrCreateComponent(final IJavaElement element)
	{
		return getOrCreateComponent(element, null, null);
	}
	
	private Component getOrCreateComponent(final IJavaElement element, 
	                                       final SourceRange sourceRange)
	{
		return getOrCreateComponent(element, sourceRange, null);
	}
	
	private Component getOrCreateComponent(final IJavaElement element, 
	                                       SourceRange sourceRange, 
	                                       String name)
	{
		if (name == null)
			name = element.getElementName();
		
		if (sourceRange == null)
			sourceRange = new SourceRange(0, 0, 0, 0, 0);
		
		Component component = getComponent(element);
		if (component == null)
		{
			component = Component.createComponent(repository, 
				element, sourceRange, name, null);

			if (component == null)
			{
				// It is possible that two java element will have the 
				// same handle!  Here's an example:
				//
				// class XML {
				//		public XML() { }					<- CTOR
				//		public XML XML() { return this; }	<- METHOD
				// }
				// Notice that the element types are different.
				
				ProblemManager.reportError("Failed to Import Component", 
						"Failed to import method " + name,  
						"Handle: " + element.getHandleIdentifier(), true);
			}
		}
		else
		{
			// Older version of CT used incorrect/unfortunate values
			// for some fields.  Update them.

			if (!component.getName().equals(name))
			{
				component.updateName(name);
			}
			
			if (!component.getSourceRange().equals(sourceRange))
			{
				component.updateSourceRange(sourceRange);
			}
		}

		// Keep track of every component we've seen
		allComponents.remove(component);
		
		return component;
	}

	public Component getComponent(IJavaElement element)
	{
		return repository.getComponent(element.getHandleIdentifier());
	}
}
