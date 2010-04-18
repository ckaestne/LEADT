package edu.wm.flat3.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;


import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.util.ProblemManager;

public class CheckConsistencyAction
	extends Action
{
	private CheckConsistencyVisitor visitor;
	private IStatusLineManager statusLineManager;
	
	public CheckConsistencyAction(IConcernModelProvider concernModelProvider,
	                              IStatusLineManager statusLineManager)
	{
		this.visitor = new CheckConsistencyVisitor(concernModelProvider);
		this.statusLineManager = statusLineManager; 
	
		setText(FLATTT
				.getResourceString("actions.CheckConsistencyAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/configure.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.CheckConsistencyAction.ToolTip"));
	}

	@Override
	public void run()
	{
		Job job = new Job("Checking link consistency")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					visitor.resetCounts();
					
					IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());

					IJavaProject[] projects = javaModel.getJavaProjects();

					monitor.beginTask("Checking", projects.length);
					
					for(IJavaProject project : projects)
					{
						monitor.subTask(project.getElementName());
						
						IStatus status = checkProject(project, 
								new SubProgressMonitor(monitor, 1));
						if (!status.isOK())
							return status;
						
						monitor.worked(1);
					}

					monitor.done();

					Display.getDefault().asyncExec(new Runnable()
						{
							//@Override
							public void run()
							{
								statusLineManager.setMessage(
										visitor.getNotLinkedCount() + " not linked, " +
										visitor.getInconsistentCount() + " inconsistent");
							}
						}
					);
					
					return Status.OK_STATUS;
				}
				catch (JavaModelException e)
				{
					ProblemManager.reportException(e);
					return Status.CANCEL_STATUS;
				}
			}
		};
		
		job.schedule();
	}

	private IStatus checkProject(IJavaProject project, IProgressMonitor monitor)
	{
		IPackageFragment[] packageFragments;
		
		try
		{
			packageFragments = project.getPackageFragments();
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e);
			return Status.CANCEL_STATUS;
		}

		for(IPackageFragment packageFragment : packageFragments)
		{
			IStatus status = checkPackageFragment(packageFragment, monitor); 
			if (!status.isOK())
				return status;
		}
		
		return Status.OK_STATUS;
	}
	
	private IStatus checkPackageFragment(IPackageFragment packageFragment,
	                                  IProgressMonitor monitor)
	{
		if (packageFragment.isDefaultPackage())
			return Status.OK_STATUS;
	
		ICompilationUnit[] cus = null;
		
		try
		{
			cus = packageFragment.getCompilationUnits();
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e,
					"Failed to obtain compilation units for package "
					+ packageFragment.getElementName(), true);
			return Status.CANCEL_STATUS;
		}
		
		monitor.beginTask("Checking", cus.length);
		
		for(ICompilationUnit cu : cus)
		{
			IStatus status = checkCompilationUnit(cu, 
					new SubProgressMonitor(monitor, 1));
			if (!status.isOK())
				return status;
			
			monitor.worked(1);
		}
		
		monitor.done();
		
		return Status.OK_STATUS;
	}
	
	private IStatus checkCompilationUnit(ICompilationUnit icu, 
	                                     IProgressMonitor monitor)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(icu);
		parser.setResolveBindings(true);

		ASTNode root = parser.createAST(null);

		visitor.setProgressMonitor(monitor);

		visitor.init();
		CompilationUnit cu = (CompilationUnit) root;
		cu.accept(visitor);

		monitor.done();
		
		return visitor.getStatus();
	}
}

class CheckConsistencyVisitor extends ASTVisitor
{
	private IConcernModelProvider concernModelProvider;

	private List<Set<Concern>> concernStack; 
	private Set<Concern> concernStackTop;
	private int methodDepth;
	
	private IProgressMonitor monitor = null;

	private int notLinked = 0;
	private int inconsistent = 0;
	
	public CheckConsistencyVisitor(IConcernModelProvider concernModelProvider)
	{
		this.concernModelProvider = concernModelProvider;
		init();
	}

	public void setProgressMonitor(IProgressMonitor monitor)
	{
		this.monitor = monitor;
	}
	
	public IStatus getStatus()
	{
		if (monitor == null || monitor.isCanceled())
			return Status.CANCEL_STATUS;
		else 
			return Status.OK_STATUS;
	}
	
	public int getNotLinkedCount()
	{
		return notLinked;
	}
	
	public int getInconsistentCount()
	{
		return inconsistent;
	}

	/**
	 * Call this before the first call to visit() (e.g., before calling accept())
	 */
	public void init()
	{
		concernStack = new ArrayList<Set<Concern>>();
		concernStackTop = null;
		methodDepth = 0;
	}
	
	public void resetCounts()
	{
		notLinked = 0;
		inconsistent = 0;
	}
	
	//-----------------------------------------------------
	// ASTVisitor overrides
	//-----------------------------------------------------
	
	@Override
	public boolean visit(TypeDeclaration typeDecl)
	{
		if (!getStatus().isOK())
			return false;

		pushConcernStack();
		
		IJavaElement typeElement = typeDecl.resolveBinding().getJavaElement();
		
		Component typeComponent = concernModelProvider.getModel().getComponent(
				typeElement.getHandleIdentifier());

		monitor.beginTask(typeDecl.getName().toString(), typeDecl.getMethods().length);
		
		// Don't traverse types that are not in the code model imported by
		// ConcernTagger
		return typeComponent != null;
	}

	@Override
	public void endVisit(TypeDeclaration typeDecl)
	{
		if (!getStatus().isOK())
			return;

		Set<Concern> containedConcerns = popConcernStack();

		checkLinks(typeDecl.resolveBinding().getJavaElement(), containedConcerns);
		
		monitor.done();
	}
	
	@Override
	public boolean visit(MethodDeclaration methodDecl)
	{
		if (!getStatus().isOK())
			return false;
		
		ASTNode parentNode = methodDecl.getParent();
		if (parentNode.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION)
			return false;
		
		if (parentNode.getNodeType() == ASTNode.TYPE_DECLARATION)
		{
			TypeDeclaration typeDecl = (TypeDeclaration) parentNode;
			
			// Entering method body
			monitor.subTask(typeDecl.getName() + "." + methodDecl.getName() + "()");
		}
		
		++methodDepth;

		pushConcernStack();
		
		return true;
	}

	@Override
	public void endVisit(MethodDeclaration methodDecl)
	{
		if (!getStatus().isOK())
			return;

		ASTNode parentNode = methodDecl.getParent();
		if (parentNode.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION)
			return;
		
		Set<Concern> containedConcerns = popConcernStack();
		
		// Exiting method body
		assert methodDepth > 0;
		checkLinks(methodDecl.resolveBinding().getJavaElement(), containedConcerns);
		--methodDepth;
		
		monitor.worked(1);
	}
	
	// Visit field accesses (joe.a, this.a)
	
	@Override
	public boolean visit(FieldAccess node)
	{
		if (!getLinkedConcernsUnion(concernStackTop, 
						node.resolveFieldBinding().getJavaElement()))
			return false;
		
		return getStatus().isOK();
	}

	// Visit field accesses (super.a)
	
	@Override
	public boolean visit(SuperFieldAccess node)
	{
		if (!getLinkedConcernsUnion(concernStackTop, 
				node.resolveFieldBinding().getJavaElement()))
			return false;
		
		return getStatus().isOK();
	}

	// Visit method invocations (foo(), this.foo())
	
	@Override
	public boolean visit(MethodInvocation node)
	{
		if (!getLinkedConcernsUnion(concernStackTop, 
				node.resolveMethodBinding().getJavaElement()))
			return false;
		
		return getStatus().isOK();
	}

	// Visit method invocations (super())
	
	@Override
	public boolean visit(SuperConstructorInvocation node)
	{
		if (!getLinkedConcernsUnion(concernStackTop, 
				node.resolveConstructorBinding().getJavaElement()))
			return false;
		
		return getStatus().isOK();
	}

	// Visit method invocations (super.foo())
	
	@Override
	public boolean visit(SuperMethodInvocation node)
	{
		if (!getLinkedConcernsUnion(concernStackTop, 
				node.resolveMethodBinding().getJavaElement()))
			return false;
		
		return getStatus().isOK();
	}
	
	// Visit type references (Joe.Hey.foo(), instanceof Joe.Hey, (Joe.Hey) joe)
	
	@Override
	public boolean visit(QualifiedType node)
	{
		if (!getLinkedConcernsUnion(concernStackTop, node.resolveBinding().getJavaElement()))
			return false;
		
		return getStatus().isOK();
	}

	// Visit type references (Hey.foo(), instanceof Hey, (Hey) joe)
	
	@Override
	public boolean visit(SimpleType node)
	{
		if (!getLinkedConcernsUnion(concernStackTop, node.resolveBinding().getJavaElement()))
			return false;
		
		return getStatus().isOK();
	}

	//-----------------------------------------------------
	// HELPER METHODS
	//-----------------------------------------------------

	private void checkLinks(IJavaElement javaElement, 
	                             Set<Concern> commonChildConcerns)
	{
		Set<Concern> linkedConcerns = new HashSet<Concern>();
		Collection<Concern> temp = concernModelProvider.getModel().getLinkedConcerns(
						javaElement, 
						concernModelProvider.getLinkType());
		if (temp != null)
			linkedConcerns.addAll(temp);
		
		//Set<Concern> executedConcerns = new HashSet<Concern>();
		//getLinkedConcerns(executedConcerns, javaElement, EdgeKind.EXECUTEDBY);
		
		if (linkedConcerns == null)
			linkedConcerns = Collections.emptySet();

		//if (executedConcerns == null || executedConcerns.size() > 10)
		//	executedConcerns = Collections.emptySet();
		
		String name;
		if (javaElement.getElementType() == IJavaElement.FIELD ||
			javaElement.getElementType() == IJavaElement.METHOD)
		{
			IType itype = ((IMember) javaElement).getDeclaringType();
			name = itype.getElementName() + "." + javaElement.getElementName();
			
			if (javaElement.getElementType() == IJavaElement.METHOD)
				name += "()";
		}
		else if (javaElement.getElementType() == IJavaElement.TYPE)
		{
			IType itype = (IType) javaElement;
			name = itype.getElementName();
		}
		else
		{
			name = javaElement.getElementName();
		}
		
		StringBuffer details = new StringBuffer();

		if (linkedConcerns.isEmpty())
		{
			details.append("NOT LINKED");
			++notLinked;
		}
		else //if (commonChildConcerns.containsAll(linkedConcerns))
		{
			// For now, we only check that the element is linked
			return;
		}
		
		String msg = subtractToString(commonChildConcerns, linkedConcerns, 
			"Concerns REFERENCED IN COMMON but not LINKED");
		
		if (!msg.isEmpty())
		{
			details.append(msg);
			++inconsistent;
		}

		if (details.length() == 0)
			return;
		
		ProblemManager.reportInfo("Inconsistent: " + name, details.toString());
	}

	private boolean getLinkedConcernsUnion(		Set<Concern> concerns, 
	                                         	IJavaElement javaElement,
	                                         	EdgeKind edgeKind)
	{
		// When traversing a type's children, we may come across unsupported
		// elements.  We ignore these when it comes to determining the union
		// of the valid childrens' links.
		if (methodDepth == 0)
			return false; // Leave the union unchanged 
		
		javaElement = Component.validateAndConvertJavaElement(javaElement);
		if (javaElement == null)
			return false; // Leave the union unchanged 
		
		Collection<Concern> linkedConcerns = concernModelProvider.getModel().getLinkedConcerns(
				javaElement, edgeKind);
		
		if (linkedConcerns == null)
		{
			// The element has no concerns so the union must be empty from here on
			concerns.clear();
		}
		else if (concerns.isEmpty())
		{
			// This is the first child so initialize the union
			concerns.addAll(linkedConcerns);
		}
		else
		{
			// This is not the first child so only retain the concerns
			// in common (do the union)
			concerns.retainAll(linkedConcerns);
		}

		return true;
	}
	
	private boolean getLinkedConcernsUnion(Set<Concern> concerns, 
	                                         IJavaElement javaElement)
	{
		return getLinkedConcernsUnion(concerns, javaElement, 
				concernModelProvider.getLinkType());
	}
	
	private static List<Concern> sort(Set<Concern> set)
	{
		List<Concern> list = new ArrayList<Concern>(set);
		Collections.sort(list, new Comparator<Concern>()
			{
				@Override
				public int compare(Concern lhs, Concern rhs)
				{
					return lhs.getId() - rhs.getId();
				}
			}
		);
		
		return list;
	}	

	private static String subtractToString(Set<Concern> lhs, 
	                                       Set<Concern> rhs, 
	                                       String description)
	{
		if (lhs.isEmpty())
			return "";
		
		Set<Concern> setLhsOnly = new HashSet<Concern>(lhs);
		
		if (rhs != null)
		{
			setLhsOnly.removeAll(rhs);
		
			if (setLhsOnly.isEmpty())
				return "";
		}
		
		StringBuffer strLhsOnly = new StringBuffer('\n' + description + ':');
		
		for(Concern concernLhsOnly : sort(setLhsOnly))
		{
			strLhsOnly.append("\n\t" + concernLhsOnly.getDisplayName());
		}
		
		return strLhsOnly.toString();
	}

	public void pushConcernStack()
	{
		concernStackTop = new HashSet<Concern>(); 
		
		concernStack.add(concernStackTop);
	}
	
	public Set<Concern> popConcernStack()
	{
		// Pop the stack
		Set<Concern> tmp = concernStack.remove(concernStack.size() - 1);
		assert tmp == concernStackTop;
		
		// Get the new top
		tmp = concernStackTop;
		
		if (concernStack.size() > 0)	
			concernStackTop = concernStack.get(concernStack.size() - 1);
		else
			concernStackTop = null;
		
		return tmp;
	}
}
