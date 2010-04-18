/**
 * 
 */
package edu.wm.flat3.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.IStatusLineManager;

import edu.wm.flat3.model.ConcernModel;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.ComponentKind;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;
import edu.wm.flat3.repository.InvalidConcernNameException;

/**
 * Parses AARF files that contain concern link data.
 * <P>
 * We expect three &#064;ATTRIBUTE declarations, followed by a &#064;DATA
 * declaration, followed by the instance data.
 * <P>
 * Here's a minimal example:
 * <P>
 * &#064;ATTRIBUTE entity-name string<BR>
 * &#064;ATTRIBUTE entity-type {method,field}<BR>
 * &#064;ATTRIBUTE concern-list string<BR>
 * &#064;DATA<BR>
 * Logging,Java.util.Logging.log(),method<BR>
 * <P>
 * Other than these requirements, we try to be as permissive as possible and
 * ignore stuff we don't understand.
 * 
 * @author eaddy
 */
public class ConcernLinksARFFFile extends ARFFFile
{
	private int entityNameCol = -1;
	private static final String ENTITY_NAME_ATTR_NAME = "entity-name";

	private int entityTypeCol = -1;
	private static final String ENTITY_TYPE_ATTR_NAME = "entity-type";

	private int concernListCol = -1;
	private static final String CONCERN_LIST_ATTR_NAME = "concern-list";

	private String[] entityTypes = null;

	private Map<String, IType> typeCache = new HashMap<String, IType>();

	private IJavaModel javaModel = null;
	
	public ConcernLinksARFFFile(	final String path, 
										final IConcernModelProvider provider,
										final IProgressMonitor progressMonitor,
										final IStatusLineManager statusLineManager)
	{
		super(path, provider, progressMonitor, statusLineManager);
	}

	@Override
	public Boolean onAttribute(final List<String> fields)
	{
		if (fields.get(1).equalsIgnoreCase(CONCERN_LIST_ATTR_NAME))
		{
			if (concernListCol != -1)
				return true; // Already assigned, ignore
			else if (!verifyAttributeDataType(fields, "string"))
				return false;
			else
				concernListCol = currentFieldIndex;
		}
		else if (fields.get(1).equalsIgnoreCase(ENTITY_NAME_ATTR_NAME))
		{
			if (entityNameCol != -1)
				return true; // Already assigned, ignore
			if (!verifyAttributeDataType(fields, "string"))
				return false;
			else
				entityNameCol = currentFieldIndex;
		}
		else if (fields.get(1).equalsIgnoreCase(ENTITY_TYPE_ATTR_NAME))
		{
			if (entityTypeCol != -1)
				return true; // Already assigned, ignore

			entityTypes = parseNominalAttribute(ENTITY_TYPE_ATTR_NAME, fields);

			if (entityTypes == null || entityTypes.length == 0)
				return false;

			for (String entityType : entityTypes)
			{
				if (!isValidEntityType(entityType))
				{
					ProblemManager.reportError("Invalid Entity Type",
							"Unrecognized entity type: '" + entityType +
							"'. We only support 'method', 'field', and 'type'.",
							"File: " + path + ", Line: " + currentLine, 
							true);
					return false;
				}
			}

			entityTypeCol = currentFieldIndex;
		}

		return true;
	}

	@Override
	public Boolean onDataInstance(final List<String> cols, final String raw_line)
	{
		if (concernListCol < 0 || entityTypeCol < 0 || entityNameCol < 0)
		{
			ProblemManager.reportError("Invalid ARFF File", 
					"Expected attributes '" + ENTITY_NAME_ATTR_NAME + "', '" +
					ENTITY_TYPE_ATTR_NAME + "', and '" +
					CONCERN_LIST_ATTR_NAME + "'.", 
					"File: " + path + ", Line: " + currentLine, 
					true);
			return false; // Halt further processing
		}

		assert currentFieldIndex >= 3;

		// Make sure there are enough columns

		int maxCol = Math.max(Math.max(concernListCol, entityTypeCol),
				entityNameCol);

		if (maxCol >= cols.size())
		{
			ProblemManager.reportError("Invalid ARFF Data Instance",
					"Not enough columns for data instance '" + raw_line +
					"'. Got " + cols.size() +
					", expected " + (maxCol + 1) + ". Ignoring.",
					"File: " + path + ", Line: " + currentLine, 
					true);
			return true; // Continue processing
		}

		// Parse List of concerns associated with the entity

		String concernList = cols.get(concernListCol);
		if (concernList.isEmpty())
			return true; // Ignore empty concerns
		else if (IsNullOrEmpty(concernList, raw_line, CONCERN_LIST_ATTR_NAME))
			return true; // Continue processing

		// Parse Entity Name (e.g., String.toString(int\, boolean))
		// May contain escaped characters.

		String entityName = cols.get(entityNameCol);
		if (IsNullOrEmpty(entityName, raw_line, ENTITY_NAME_ATTR_NAME))
			return true; // Continue processing

		// Parse Entity Type (e.g., "method")

		String entityType = cols.get(entityTypeCol);
		if (IsNullOrEmpty(entityType, raw_line, ENTITY_TYPE_ATTR_NAME))
		{
			return true; // Continue processing
		}
		
		if (!isValidEntityType(entityType))
		{
			ProblemManager.reportError("Invalid Entity Type",
					"Unrecognized entity type: '" + entityType + 
					"'. We only support 'method', 'field', and 'type'.", 
					"File: " + path + ", Line: " + currentLine, 
					true);
			return true; // Continue processing
		}

		Component component = findComponent(entityName, entityType);
		if (component == null)
			return true; // Already reported error, continue processing

		for (String concernPath : parseDelimitedAndQuotedString(concernList, ','))
		{
			Concern concern;
			
			// If concern path is a hierarchy, this will create multiple
			// concerns
			try
			{
				concern = provider.getModel().createConcernPath(concernPath, "");
				if (concern == null)
				{
					// See if it's because of an invalid name
					ProblemManager.reportError("Failed to Create Concern", 
							"Failed to create concern '" + concernPath + "', ignoring.",
							"Data instance: " + raw_line + 
								", File: " + path + ", Line: " + currentLine,
							true);
					
					continue; // Skip
				}
			}
			catch (InvalidConcernNameException e)
			{
				ProblemManager.reportError("Invalid Concern Name", 
						"Concern path '" + concernPath + "' is invalid, ignoring.",
						e + "\n" +
							"Data instance: " + raw_line + "\n" + 
							"File: " + path + ", Line: " + currentLine,
						true);
				continue; // Skip
			}

			if (progressMonitor != null)
				progressMonitor.subTask(concern.getSafeShortName() + " -> " + component);
			
			if (concern.link(component, provider.getLinkType()))
				++validInstances;
		}

		return true;
	}

	public void save()
	{
		FileOutputStream stream;
		try
		{
			stream = new FileOutputStream(path);
		}
		catch (FileNotFoundException e)
		{
			ProblemManager.reportException(e);
			return;
		}
		
		PrintStream out = new PrintStream(stream);
		
		ConcernModel concernModel = provider.getModel();
		EdgeKind relation = provider.getLinkType(); 

		out.print("@RELATION \"Component-");
		
		if (concernModel.getConcernDomain().isDefault())
		{
			out.print("Concern");
		}
		else
		{
			out.print(concernModel.getConcernDomain());
		}
		
		out.println(" Map (Relation: " + relation.toString() + ")\"");
		
		out.println();
		out.println("@ATTRIBUTE entity-name string");
		out.println("@ATTRIBUTE entity-type {method,field,type}");
		out.println("@ATTRIBUTE concern-list string");
		out.println();
		
		out.println("@DATA");
		
		List<Component> allComponents = provider.getModel().getComponents();
		
		if (progressMonitor != null)
			progressMonitor.beginTask("Exporting", allComponents.size());
		
		for(Component component : allComponents)
		{
			if (progressMonitor != null)
			{
				if (progressMonitor.isCanceled())
						return;
				else
					progressMonitor.subTask(component.getName());
			}
			
			if (!component.isKind(ComponentKind.METHOD) &&
				!component.isKind(ComponentKind.FIELD) &&
				!component.isKind(ComponentKind.CLASS))
			{
				continue;
			}
			
			IJavaElement javaElement = component.getJavaElement();

			String entityName = getFullyQualifiedSignature(javaElement);
			
			String entityType;
			if (component.isKind(ComponentKind.CLASS))
			{
				entityType = "type";
			}
			else
			{
				entityType = component.getKind().name().toLowerCase();
			}
			
			out.print(	"\"" + entityName + "\"," +
						entityType + ",\"");
			
			Collection<Concern> concernsLinkedToComponent =
				concernModel.getLinkedConcerns(component, relation);
			
			if (concernsLinkedToComponent != null)
			{
				boolean first = true;
				for(Concern concernLinkedToComponent : concernsLinkedToComponent)
				{
					if (!first)
						out.print(',');
					
					out.print(concernLinkedToComponent.getQualifiedName());
					
					first = false;
				}
			}
			
			out.println('\"');

			++this.validInstances;
		}

		out.close();
		out = null;
		
		onSaveEnd();
	}

	private Component findComponent(String signature, final String entityType)
	{
		// Signature may contain escaped characters (e.g.,
		// String.toString(int\, boolean)) which we must unescape
		signature = unescape(signature);
		
		IJavaElement javaElement = parseFullyQualifiedSignature(signature, entityType);
		if (javaElement == null)
			return null; // Already reported error
		
		return getComponent(javaElement, signature); 
	}

	private Component getComponent(final IJavaElement element, 
	                               final String signature)
	{
		Component component = provider.getModel().getComponent(element.getHandleIdentifier());
		if (component == null)
		{
			ProblemManager.reportError("Component Not Found",
					"Component for Java element '" + signature + "'" +
					" was not found, ignoring.",
					"Java element handle: " + element.getHandleIdentifier() +
						", File: " + path + ", Line: " + currentLine, 
					true);
		}

		return component;
	}

	public IJavaElement parseFullyQualifiedSignature(	final String signature,
	                                                    final String entityType)
	{
		JavaElementInfo elementInfo = new JavaElementInfo();
		if (!elementInfo.parseSignature(signature, entityType))
			return null;

		IType type = findType(elementInfo._fullyQualifiedType, signature);
		if (type == null)
			return null; // We already reported the error

		if (elementInfo._isType)
		{
			return type;
		}
		
		IMember member = elementInfo.findMember(type);
		if (member == null)
		{
			if (elementInfo._specialConstructorName != null)
			{
				ProblemManager.reportInfo(
						elementInfo._specialConstructorName + " constructor for type '" + elementInfo._fullyQualifiedType + "'" +
						" was not found, ignoring. " +
						"This can happen if it was generated automatically by the compiler.",
						"Signature: " + elementInfo._fullSignature +
							", File: " + path + ", Line: " + currentLine);
			}
			else
			{
				ProblemManager.reportError("Member Not Found",
						"Member '" + elementInfo._fullSignature + "'" +
						" was not found, ignoring.",
						"File: " + path + ", Line: " + currentLine, 
						true);
			}
			
			return null;
		}
		
		return member; 
	}

	private IType findType(final String fullyQualifiedName, final String signature)
	{
		// Note: We store nulls so we don't have to fail the search again
		if (typeCache.containsKey(fullyQualifiedName))
			return typeCache.get(fullyQualifiedName);
		
		// Obtain the code model
		if (javaModel == null)
			javaModel = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
		
		try
		{
			// For each project
			for(IJavaProject project : javaModel.getJavaProjects())
			{
				IType type = project.findType(fullyQualifiedName, (IProgressMonitor) null);
				if (type != null)
				{
					typeCache.put(fullyQualifiedName, type);
					return type;
				}
			}

			ProblemManager.reportError("Type Not Found",
					"Type '" + fullyQualifiedName +
					"' not found, ignoring.",
					"Signature: " + signature + ", File: " + path + ", Line: " + currentLine, 
					true);
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e, 
					"Type '" + fullyQualifiedName +
					"' not found, ignoring.", true);
		}

		typeCache.put(fullyQualifiedName, null);
		return null;
	}
	
	static public String getFullyQualifiedSignature(IJavaElement javaElement)
	{
		int elementType = javaElement.getElementType();
		
		switch(elementType)
		{
		case IJavaElement.FIELD: 
		case IJavaElement.INITIALIZER:
		case IJavaElement.METHOD: 
			
			IMember member = (IMember) javaElement;
			
			StringBuffer buf = new StringBuffer();
			buf.append(member.getDeclaringType().getFullyQualifiedName());
			buf.append('.');
			
			if (elementType == IJavaElement.INITIALIZER)
			{
				buf.append("<static initializer>");
			}
			else
			{
				buf.append(member.getElementName());
				
				if (elementType == IJavaElement.METHOD)
				{
					IMethod method = (IMethod) javaElement;
					
					buf.append('(');
					
					boolean first = true;
					
					for(String parameterTypeBizarro : method.getParameterTypes())
					{
						if (!first)
							buf.append(", ");
						
						String parameterType = Signature.toString(parameterTypeBizarro);
						buf.append(parameterType);
						
						first = false;
					}
					buf.append(')');
				}
			}
			
			return buf.toString();
			
		case IJavaElement.TYPE: 
			return ((IType) javaElement).getFullyQualifiedName();
		case IJavaElement.JAVA_PROJECT: 		// Fall thru
		case IJavaElement.PACKAGE_FRAGMENT: 	// Fall thru
		case IJavaElement.COMPILATION_UNIT:
			return javaElement.getElementName();
		default:
		{
			assert false; 
			return null;
		}
		}
	}
	
	private Boolean IsNullOrEmpty(final String value, 
	                              final String line, 
	                              final String name)
	{
		if (value == null || value.isEmpty())
		{
			ProblemManager.reportError("Invalid ARFF Data Instance", 
					"Data instance '" + line + "' has an empty " + name +
					". Ignoring.", 
					"File: " + path + ", Line: " + currentLine, 
					true);

			return true;
		}
		else
		{
			return false;
		}
	}

	private static boolean isValidEntityType(final String entityType)
	{
		if (entityType.equalsIgnoreCase("method") ||
			entityType.equalsIgnoreCase("field") ||
			entityType.equalsIgnoreCase("type") ||
			entityType.equalsIgnoreCase("initializer"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}

class JavaElementInfo
{
	public String _packageName;
	public String _typeName;
	public String _fullyQualifiedType;
	public String _fullSignature;
	public String _memberName = null;
	public String[] _args = null;

	public boolean _isType = false;
	public boolean _isMethod = false;
	public boolean _isConstructor = false;
	public boolean _isStaticConstructor = false;
	public String _specialConstructorName = null;

	public boolean parseSignature(final String signature, final String entityType)
	{
		if (signature == null || signature.isEmpty())
			return false;
	
		assert entityType != null && !entityType.isEmpty();
		
		_isType = entityType.equals("type");
		
		_fullSignature = signature;

		String methodArgs = null;
		
		if (_isType)
		{
			// Parse <package>.<type>
			
			_fullyQualifiedType = _fullSignature;
		}
		else
		{
			// Parse <package>.<type>.<field>
			// and   <package>.<type>."<static initializer>"
			// and   <package>.<type>.<method>(<args>)

			int lastDot;
			
			int firstParen = _fullSignature.indexOf('(');
			if (firstParen != -1)
			{
				_isMethod = true; // method
				
				// Make sure we don't accidentally find a dot within
				// the argument list
				lastDot = _fullSignature.lastIndexOf('.', firstParen-1);
			}
			else
			{
				_isMethod = false; // field
				lastDot = _fullSignature.lastIndexOf('.');
			}
				
			assert lastDot != -1; // Members always have at least one dot

			// Extract <package>.<type>
			_fullyQualifiedType = _fullSignature.substring(0, lastDot);
			
			if (_isMethod)
			{
				// Extract <method>
				_memberName = _fullSignature.substring(lastDot+1, firstParen);
				
				// Extract <args> (sans parenthesis)
				methodArgs = _fullSignature.substring(firstParen+1, 
						_fullSignature.length()-1);;
			}
			else
			{
				// Extract <field> or static ctor
				_memberName = _fullSignature.substring(lastDot+1);
				
				if (_memberName.equals("<static initializer>"))
				{
					_isStaticConstructor = true;
					_specialConstructorName = "Static";
				}
			}
		}

		// Ignore anonymous classes (E.g., MyClass$12)
		if (isAnonymous(_fullyQualifiedType))
			return false;

		// Extract <package> from <package>.<type>
		
		int lastDot = _fullyQualifiedType.lastIndexOf('.');

		// For package "x.y", class "A", and nested class "B",
		// JDT expects "x.y.A.B" but the format we use in the
		// ARFF file is "x.y.A$B".  Note, we do this _after_ searching
		// for the last '.' above.
		_fullyQualifiedType = _fullyQualifiedType.replace('$', '.');
		
		if (lastDot == -1)
		{
			_packageName = "";
			_typeName = _fullyQualifiedType;
		}
		else
		{
			_packageName = _fullyQualifiedType.substring(0, lastDot);
			_typeName = _fullyQualifiedType.substring(lastDot+1);
		}

		// For types, fields, and static constructors, we're done
		if (_isType || !_isMethod)
		{
			return true;
		}

		// Parse the method args 
		
		assert methodArgs != null;
		
		int dollar = _memberName.lastIndexOf('$');
		if (dollar >= 0)
		{
			_memberName = _memberName.substring(dollar+1);
			_isConstructor = true;
		}
		else if (_memberName.equals(_typeName))
		{
			_isConstructor = true;
		}
		
		// Access is a synthesized method used to allow nested
		// classes to access private methods of the outer class.
		// Just ignore these.
		if (_memberName.startsWith("access$"))
			return false;
		
		if (!methodArgs.isEmpty())
		{
			// The JDT doesn't expect fully qualified nested
			// types for the arguments so remove the outer type
			methodArgs = methodArgs.replace('$', '.');

			if (_isConstructor && methodArgs.equals(_typeName))
			{
				// A ctor whose only arg is the ctor's type is
				// a copy constructor
				_specialConstructorName = "Copy";
			}
			
			_args = methodArgs.split(", ");

			for(int i = 0; i < _args.length; ++i)
			{
				int dot = _args[i].indexOf('.');
				if (dot == -1)
					continue; // Arg is not a nested type

				String outerType = _args[i].substring(0, dot);

				// When a nested class is referred to within
				// its own methods or its outer type's methods,
				// it doesn't use a nested name
				if (outerType.equals(_typeName) || 
					_args[i].equals(_typeName))
				{
					String innerType = _args[i].substring(dot+1);
					_args[i] = innerType;
				}
			}
		}
		else if (_isConstructor)
		{
			// A no arg constructor is a default constructor
			_specialConstructorName = "Default";
		}
		
		return true;
	}

	private boolean isAnonymous(final String fullyQualifiedType)
	{
		int len = fullyQualifiedType.length();
		
		for(int i = len-1; i >= 0; --i) // Walk backwards
		{
			char c = fullyQualifiedType.charAt(i);
			if (c == '$')
			{
				// If i < len-1 then the only things after
				// $ are digits (i.e., an anonymous class) 
				return i < len-1;
			}
			else if (!Character.isDigit(c))
			{
				return false;
			}
		}

		// Bizarre cases: "", "1234"
		return false;
	}

	public IMember findMember(final IType type)
	{
		// Don't use IType.getMethod() or IType.getField().
		// This actually *creates* a new dummy method or field
		// instead of returning an existing one.
		
		try
		{
			if (_isStaticConstructor)
			{
				if (type.getInitializers().length > 0)
					return type.getInitializers()[0];
				else
					return null;
				
			}
			else if (_isMethod)
			{
				for(IMethod method : type.getMethods())
				{
					if (!method.getElementName().equals(_memberName))
						continue;
					
					String[] methodArgs = method.getParameterTypes();

					int argsLen = _args == null ? 0 : _args.length;

					if (argsLen != methodArgs.length)
						continue;
					
					int i = 0;
					for(i = 0; i < argsLen; ++i)
					{
						if (!_args[i].equals(Signature.toString(methodArgs[i])))
							break;
					}
					
					if (i == argsLen)
						return method;
				}
			}
			else
			{
				for(IField field : type.getFields())
				{
					if (field.getElementName().equals(_memberName))
						return field;
				}
			}
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e, true);
		}

		return null;
	}
}
