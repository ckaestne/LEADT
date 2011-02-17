package de.ovgu.cide.mining.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import cide.gparser.ParseException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.SourceFileColorManager;
import de.ovgu.cide.language.jdt.ASTBridge;
import de.ovgu.cide.language.jdt.JDTParserWrapper;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.database.model.AFlyweightElementFactory;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.ARelationKind;
import de.ovgu.cide.mining.database.recommendationengine.AElementColorManager;
import de.ovgu.cide.util.MethodPathItem;
import de.ovgu.cide.util.OverridingRelationUtils;
import de.ovgu.cide.util.TypePathItem;

/**
 * @author A.Dreiling
 * 
 */
public class AAccessRelationBuilder implements Serializable {

	private static final long serialVersionUID = 3L;

	private int cuHash;

	private AbstractProgramDatabase aDB;
	private AFlyweightElementFactory elementFactory;

	private AElement curCUElement;
	private AElement curImport;
	private AElement curType;
	private AElement curMethod;
	private AElement curField;
	private AElement curLocalVariable;
	private AElement curExtendsAccess;

	private Stack<AElement> curTypeReminder;

	private Set<AElement> curParameter;
	private LocalContextElement curContext;
	private Stack<LocalContextElement> curContextReminder;

	private Map<String, AElement> importMap;

	private AElementColorManager elementColorManager;

	private SourceFileColorManager sourceColorManager;

	private class LocalContextElement {
		private ASTNode node;

		private AElement element;
		private ASTNode accessNode;

		public LocalContextElement(ASTNode node, ASTNode accessNode,
				AElement element) {
			this.node = node;
			this.element = element;
			this.accessNode = accessNode;
		}

		public ASTNode getNode() {
			return node;
		}

		public AElement getElement() {
			return element;
		}

		public ASTNode getAccessNode() {
			return accessNode;
		}

	}

	/**
	 * Clear class fields and be ready to work on a different compilation unit.
	 */
	private void reset() {
		curCUElement = null;
		curImport = null;
		curTypeReminder = new Stack<AElement>();

		curContextReminder = new Stack<LocalContextElement>();

		importMap = new HashMap<String, AElement>();

		curType = null;
		curMethod = null;
		curField = null;
		curLocalVariable = null;
		curContext = null;
		curExtendsAccess = null;
		curParameter = null;

	}

	public AAccessRelationBuilder(AbstractProgramDatabase aDB,
			AFlyweightElementFactory elementFactory) {
		this.aDB = aDB;
		this.elementFactory = elementFactory;
	}

	void update(ASTNode node) {

		node.accept(new ASTVisitor() {

			private void addElement(AElement element, Set<IFeature> colors) {
				aDB.addElement(element);
				for (IFeature color : colors)
					elementColorManager.addElementToColor(color, element);
			}

			private Set<IFeature> getColor(ASTNode node) {
				return sourceColorManager.getColors(ASTBridge.bridge(node));
			}

			// COMPILATION UNIT CONTEXT//
			@Override
			public boolean visit(CompilationUnit node) {
				// create the CU element and store it
				curCUElement = (AElement) elementFactory.getElement(node);
				return super.visit(node);
			}

			// IMPORT CONTEXT//
			@Override
			public boolean visit(ImportDeclaration node) {
				curImport = (AElement) elementFactory.getElement(node);

				IBinding binding = node.resolveBinding();
				if (binding instanceof ITypeBinding) {
					importMap.put(((ITypeBinding) binding).getKey(), curImport);
				}

				return super.visit(node);
			}

			public void endVisit(ImportDeclaration node) {
				curImport = null;
			}

			// TYPE CONTEXT//
			@Override
			public boolean visit(EnumDeclaration node) {
				ITypeBinding binding = node.resolveBinding();
				visitType(node, binding);
				return super.visit(node);
			}

			@Override
			public boolean visit(TypeDeclaration node) {

				ITypeBinding binding = node.resolveBinding();
				visitType(node, binding);
				return super.visit(node);

			}

			public void visitType(ASTNode node, ITypeBinding binding) {

				if (binding == null)
					return;

				if (!binding.isTopLevel()) {
					curTypeReminder.push(curType);
				}

				curType = (AElement) elementFactory.getElement(binding);

				if (curType == null)
					return;

				createExtendsAndImplementsTypeRelations(binding);

				// define extends access node
				if (node.getNodeType() != ASTNode.TYPE_DECLARATION)
					return;

				Object curExtendsType = node
						.getStructuralProperty(TypeDeclaration.SUPERCLASS_TYPE_PROPERTY);

				if (curExtendsType == null)
					return;

				curExtendsAccess = (AElement) elementFactory.createElement(
						AICategories.TYPE_ACCESS, null, cuHash,
						(ASTNode) curExtendsType);

				// StructuralPropertyDescriptor descriptor =
				// parent.getLocationInParent();
				//
				// if (descriptor == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY )
				// {
				// aDB.addRelationAndTranspose(curType, ARelation.EXTENDS_TYPE,
				// typeAccessElement);
				//
				// }
				// else if (descriptor ==
				// TypeDeclaration.SUPER_INTERFACE_TYPES_PROPERTY ||
				// descriptor == EnumDeclaration.SUPER_INTERFACE_TYPES_PROPERTY)
				// {
				// aDB.addRelationAndTranspose(curType,
				// ARelation.IMPLEMENTS_TYPE, typeAccessElement);
				// }
				// else {
				// aDB.addRelationAndTranspose(curType, ARelation.ACCESS_TYPE,
				// typeAccessElement);
				// }
				//

			}

			private void createExtendsAndImplementsTypeRelations(
					ITypeBinding binding) {

				List<TypePathItem> directTypes = new ArrayList<TypePathItem>();
				List<TypePathItem> transitiveTypes = new ArrayList<TypePathItem>();

				if (collectExtendsAndImplementsTypeRelations(binding,
						directTypes, transitiveTypes)) {

					AElement typeElement;
					for (TypePathItem pathItem : directTypes) {

						typeElement = (AElement) elementFactory
								.getElement(pathItem.getBinding());

						if (typeElement != null) {

							if (pathItem.isInterface()) {
								aDB.addRelationAndTranspose(curType,
										ARelationKind.IMPLEMENTS_TYPE,
										typeElement);
							} else {
								aDB
										.addRelationAndTranspose(curType,
												ARelationKind.EXTENDS_TYPE,
												typeElement);
							}
						}

					}

					for (TypePathItem pathItem : transitiveTypes) {

						typeElement = (AElement) elementFactory
								.getElement(pathItem.getBinding());

						if (typeElement != null) {

							if (pathItem.isInterface()) {
								aDB
										.addRelationAndTranspose(
												curType,
												ARelationKind.IMPLEMENTS_TYPE_TRANSITIVE,
												typeElement);
							} else {
								aDB.addRelationAndTranspose(curType,
										ARelationKind.EXTENDS_TYPE_TRANSITIVE,
										typeElement);
							}
						}

					}
				}
			}

			private boolean collectExtendsAndImplementsTypeRelations(
					ITypeBinding declTypeBinding,
					List<TypePathItem> directTypes,
					List<TypePathItem> transitiveTypes) {

				if (declTypeBinding == null || transitiveTypes == null
						|| directTypes == null)
					return false;

				Set<String> checkedInterfaces = new HashSet<String>();
				ITypeBinding[] interfaces = declTypeBinding.getInterfaces();

				for (ITypeBinding tmpInterface : interfaces) {
					directTypes.add(new TypePathItem(tmpInterface, true));
					checkedInterfaces.add(tmpInterface.getKey());
					OverridingRelationUtils
							.collectExtendedAndImplementedTypesInInterfaces(
									tmpInterface, transitiveTypes,
									checkedInterfaces);
				}

				ITypeBinding superClass = declTypeBinding.getSuperclass();
				if (superClass != null) {
					directTypes.add(new TypePathItem(superClass, false));
					OverridingRelationUtils
							.collectExtendedAndImplementedTypesInSuperClasses(
									superClass, transitiveTypes,
									checkedInterfaces);
				}

				if (directTypes.size() == 0 && transitiveTypes.size() == 0)
					return false;

				return true;
			}

			@Override
			public void endVisit(EnumDeclaration node) {
				ITypeBinding binding = node.resolveBinding();
				endVisitType(binding);

			}

			@Override
			public void endVisit(TypeDeclaration node) {
				ITypeBinding binding = node.resolveBinding();
				endVisitType(binding);

			}

			public void endVisitType(ITypeBinding binding) {

				if (binding == null)
					return;

				// restore current type and temp method
				if (!curTypeReminder.isEmpty()) {
					curType = (AElement) curTypeReminder.pop();
				} else {
					curType = null;
				}
			}

			// METHOD CONTEXT//
			public boolean visit(MethodDeclaration node) {

				IMethodBinding binding = node.resolveBinding();
				if (binding != null) {
					curMethod = elementFactory.getElement(binding);

					createInherritedAndOverriddenMethodRelations(binding);

				}

				return super.visit(node);
			}

			private void createInherritedAndOverriddenMethodRelations(
					IMethodBinding binding) {

				List<MethodPathItem> inhMethods = new ArrayList<MethodPathItem>();

				if (collectInherritedOrOverridenMethods(binding, inhMethods)) {

					boolean first = true;
					AElement superMethod;

					ARelationKind overridesRelation = ARelationKind.OVERRIDES_METHOD;
					ARelationKind implementsRelation = ARelationKind.IMPLEMENTS_METHOD;

					for (MethodPathItem methodPathItem : inhMethods) {

						superMethod = (AElement) elementFactory
								.getElement(methodPathItem.getBinding());

						if (superMethod != null) {

							if (!methodPathItem.isAbstract()) {
								aDB.addRelationAndTranspose(curMethod,
										overridesRelation, superMethod);
							} else {
								aDB.addRelationAndTranspose(curMethod,
										implementsRelation, superMethod);
							}
						}

						if (first) {
							first = false;
							overridesRelation = ARelationKind.OVERRIDES_METHOD_TRANSITIVE;
							implementsRelation = ARelationKind.IMPLEMENTS_METHOD_TRANSITIVE;
						}

					}
				}
			}

			private boolean collectInherritedOrOverridenMethods(
					IMethodBinding binding, List<MethodPathItem> inhMethods) {

				ITypeBinding declTypeBinding = binding.getDeclaringClass();

				if (declTypeBinding == null)
					return false;

				Set<String> checkedInterfaces = new HashSet<String>();

				// (recursively) collects all keys of methods in abstract
				// classes which
				// belongs to this declaration
				OverridingRelationUtils.collectSimilarMethodKeysInSuperClasses(
						binding, declTypeBinding.getSuperclass(), inhMethods,
						checkedInterfaces);

				// (recursively) collects all keys of methods in interfaces
				// which
				// belongs to this declaration
				OverridingRelationUtils.collectSimilarMethodKeysInInterfaces(
						binding, declTypeBinding.getInterfaces(), inhMethods,
						checkedInterfaces);

				// the set should contain at least one inherited method
				if (inhMethods.size() == 0)
					return false;

				return true;
			}

			@Override
			public void endVisit(MethodDeclaration node) {
				curMethod = null;
			}

			@Override
			public boolean visit(ConstructorInvocation node) {
				IMethodBinding binding = node.resolveConstructorBinding();
				List args = node.arguments();
				handleMethodCall(node, binding, args);
				return super.visit(node);
			}

			@Override
			public void endVisit(ConstructorInvocation node) {

				handleEndMethodCall(node);
			}

			@Override
			public boolean visit(ClassInstanceCreation node) {
				IMethodBinding binding = node.resolveConstructorBinding();
				List args = node.arguments();
				handleMethodCall(node, binding, args);
				return super.visit(node);
			}

			@Override
			public void endVisit(ClassInstanceCreation node) {

				handleEndMethodCall(node);
			}

			@Override
			public boolean visit(SuperConstructorInvocation node) {
				IMethodBinding binding = node.resolveConstructorBinding();
				List args = node.arguments();
				handleMethodCall(node, binding, args);
				return super.visit(node);
			}

			@Override
			public void endVisit(SuperConstructorInvocation node) {

				handleEndMethodCall(node);
			}

			@Override
			public boolean visit(SuperMethodInvocation node) {
				IMethodBinding binding = node.resolveMethodBinding();
				List args = node.arguments();
				handleMethodCall(node, binding, args);
				return super.visit(node);
			}

			@Override
			public void endVisit(SuperMethodInvocation node) {

				handleEndMethodCall(node);
			}

			@Override
			public boolean visit(MethodInvocation node) {

				IMethodBinding binding = node.resolveMethodBinding();
				List args = node.arguments();
				handleMethodCall(node, binding, args);
				return super.visit(node);

			}

			@Override
			public void endVisit(MethodInvocation node) {
				handleEndMethodCall(node);
			}

			private void handleMethodCall(ASTNode node, IMethodBinding binding,
					List arguments) {

				if (binding != null) {

					// DEFINING CONTEXT FOR METHOD
					AElement curElement = elementFactory.getElement(binding);

					if (curContext != null) {
						curContextReminder.push(curContext);
					}

					// cur element could also be null!
					curContext = new LocalContextElement(node, null, curElement);

					Set<AElement> localVars = null;
					if (curElement != null) {
						localVars = aDB.getRange(curElement,
								ARelationKind.DECLARES_LOCAL_VARIABLE);
					}

					curParameter = new HashSet<AElement>();

					// create param access elements
					for (int i = 0; i < arguments.size(); i++) {
						ASTNode tmpArg = (ASTNode) arguments.get(i);
						AElement paramAccessElement = (AElement) elementFactory
								.createElement(AICategories.PARAMETER_ACCESS,
										null, cuHash, tmpArg);
						addElement(paramAccessElement, getColor(tmpArg));

						curParameter.add(paramAccessElement);

						if (localVars == null)
							continue;

						// aDB.addRelationAndTranspose(curElement,
						// ARelation.ACCESS_PARAMETER, paramAccessElement);

						for (AElement localVar : localVars) {
							if (((AElement) localVar).getParamIndex() == i) {
								aDB.addRelationAndTranspose(localVar,
										ARelationKind.REQUIRES,
										paramAccessElement);

							}
						}

					}

					if (curParameter.size() == 0)
						curParameter = null;

					handleMethodAccess(node, curElement);

				}
			}

			private void handleEndMethodCall(ASTNode node) {
				handleEndVisitContext(node);

				curParameter = null;

			}

			// LOCAL VARIABLE AND FIELD CONTEXT//
			public boolean visit(VariableDeclarationFragment node) {
				IVariableBinding binding = node.resolveBinding();
				visitFieldOrVariable(node, binding);
				return super.visit(node);
			}

			public boolean visit(SingleVariableDeclaration node) {
				IVariableBinding binding = node.resolveBinding();
				visitFieldOrVariable(node, binding);
				return super.visit(node);
			}

			public boolean visit(EnumConstantDeclaration node) {
				IVariableBinding binding = node.resolveVariable();
				visitFieldOrVariable(node, binding);
				return super.visit(node);
			}

			public void visitFieldOrVariable(ASTNode node,
					IVariableBinding binding) {

				if (binding == null)
					return;

				if (binding.isField() || binding.isEnumConstant()) {
					curField = (AElement) elementFactory.getElement(binding);
				} else {
					curLocalVariable = (AElement) elementFactory
							.getElement(binding);

				}

				// VariableDeclarationFragment extra handling is needed!
				if (node.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {

					handleTypeAccessForVarDeclFragment(node);
				}

			}

			public void endVisit(EnumConstantDeclaration node) {
				IVariableBinding binding = node.resolveVariable();
				endVisitFieldOrVariable(node, binding);
			}

			public void endVisit(VariableDeclarationFragment node) {
				IVariableBinding binding = node.resolveBinding();
				endVisitFieldOrVariable(node, binding);
			}

			public void endVisit(SingleVariableDeclaration node) {
				IVariableBinding binding = node.resolveBinding();
				endVisitFieldOrVariable(node, binding);
			}

			public void endVisitFieldOrVariable(ASTNode node,
					IVariableBinding binding) {

				if (binding == null)
					return;

				if (binding.isField() || binding.isEnumConstant()) {
					curField = null;
				} else {
					curLocalVariable = null;

				}
			}

			public boolean visit(CastExpression node) {
				final ASTNode curNode = node;

				node.getExpression().accept(new ASTVisitor() {

					public boolean visit(SimpleName nameNode) {

						addContextForFieldorVariable(curNode, nameNode);
						return super.visit(nameNode);
					}

				});

				return super.visit(node);
			}

			public void endVisit(CastExpression node) {
				handleEndVisitContext(node);
			}

			public boolean visit(InstanceofExpression node) {
				final ASTNode curNode = node;

				node.getLeftOperand().accept(new ASTVisitor() {

					public boolean visit(SimpleName nameNode) {

						addContextForFieldorVariable(curNode, nameNode);
						return super.visit(nameNode);
					}

				});

				return super.visit(node);
			}

			public void endVisit(InstanceofExpression node) {
				handleEndVisitContext(node);
			}

			@Override
			public boolean visit(Assignment node) {

				final ASTNode curNode = node;

				node.getLeftHandSide().accept(new ASTVisitor() {

					public boolean visit(SimpleName nameNode) {

						addContextForFieldorVariable(curNode, nameNode);
						return super.visit(nameNode);
					}

				});

				return super.visit(node);

			}

			private void addContextForFieldorVariable(ASTNode contextNode,
					SimpleName nameNode) {
				IBinding binding = nameNode.resolveBinding();

				if (binding != null && binding instanceof IVariableBinding) {

					// DEFINING CONTEXT FOR FIELD OR LOCAL VARIABLE!
					AElement curElement = elementFactory.getElement(binding);

					if (curContext != null) {
						curContextReminder.push(curContext);
					}

					curContext = new LocalContextElement(contextNode, nameNode,
							curElement);

				}
			}

			public void endVisit(Assignment node) {
				handleEndVisitContext(node);
			}

			private void handleEndVisitContext(ASTNode node) {
				if (curContext == null)
					return;

				while (curContext != null && curContext.getNode().equals(node)) {

					if (!curContextReminder.isEmpty()) {
						curContext = curContextReminder.pop();
					} else {
						curContext = null;
					}

				}
			}

			// ACCESS HANDLING//

			@Override
			public boolean visit(SimpleName node) {
				visitName(node);
				return super.visit(node);
			}

			// HANDLE ACCESS
			public void visitName(Name node) {

				IBinding binding = node.resolveBinding();

				if (binding == null)
					return;

				if (binding instanceof ITypeBinding) {
					handleTypeAccess(node, (ITypeBinding) binding);
				} else if (binding instanceof IVariableBinding) {
					handleFieldOrVariableAccess(node,
							(IVariableBinding) binding);
				}
				// else if (binding instanceof IMethodBinding) {
				// handleMethodAccess(node,(IMethodBinding) binding);
				// }

			}

			// HANDLE TYPE ACCESS
			private void handleTypeAccess(Name node, ITypeBinding binding) {

				ASTNode parent = node.getParent();

				// don't check type or enum declarations
				if (parent instanceof TypeDeclaration)
					return;

				if (parent instanceof EnumDeclaration)
					return;

				ASTNode elementNode = node;

				if (parent instanceof Type) {
					elementNode = parent;
				}

				AElement typeAccessElement = (AElement) elementFactory
						.createElement(AICategories.TYPE_ACCESS, null, cuHash,
								elementNode);
				addElement(typeAccessElement, getColor(elementNode));

				AElement typeElement = (AElement) elementFactory
						.getElement(binding);

				// ADD ACCESS TO ACTUAL TYPE
				if (typeElement != null)
					aDB.addRelationAndTranspose(typeAccessElement,
							ARelationKind.BELONGS_TO, typeElement);

				AElement importElement = importMap.get(binding.getKey());
				if (importElement != null && !importElement.equals(curImport))
					aDB.addRelationAndTranspose(typeAccessElement,
							ARelationKind.BELONGS_TO, importElement);

				// ADD ALL ELEMENTS WHO ACCESS THIS ELEMENT

				// ADD ALWAYS ACCESS RELATION FOR COMP. UNIT
				aDB
						.addRelationAndTranspose(curCUElement,
								ARelationKind.ACCESS_TYPE_TRANSITIVE,
								typeAccessElement);

				// ADD ACCESS ELEMENT TO OTHER ELEMENTS DEPENDING ON CURRENT
				// CONTEXT
				if (curImport != null)
					aDB.addRelationAndTranspose(curImport,
							ARelationKind.ACCESS_TYPE, typeAccessElement);

				if (curType != null)
					aDB.addRelationAndTranspose(curType,
							ARelationKind.ACCESS_TYPE_TRANSITIVE,
							typeAccessElement);

				for (AElement tmpType : curTypeReminder) {
					aDB.addRelationAndTranspose(tmpType,
							ARelationKind.ACCESS_TYPE_TRANSITIVE,
							typeAccessElement);
				}

				// if (curParameter != null) {
				// for (AIElement paramAccess : curParameter) {
				// aDB.addRelationAndTranspose(typeAccessElement,
				// ARelation.DECLARES_PARAMETER, paramAccess);
				// }
				// curParameter = null;
				// }

				boolean directRelationAdded = false;

				// if is one of them, will be handled separately later on
				switch (elementNode.getParent().getNodeType()) {
				case ASTNode.VARIABLE_DECLARATION_STATEMENT:
					directRelationAdded = true;
					break;
				case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
					directRelationAdded = true;
					break;
				case ASTNode.FIELD_DECLARATION:
					directRelationAdded = true;
					break;
				}

				if (!directRelationAdded && curContext != null) {

					directRelationAdded = true;
					if (curContext.getElement() != null)
						aDB.addRelationAndTranspose(curContext.getElement(),
								ARelationKind.ACCESS_TYPE, typeAccessElement);

				}

				for (LocalContextElement tmpContext : curContextReminder) {
					if (tmpContext.getElement() != null)
						aDB.addRelationAndTranspose(tmpContext.getElement(),
								ARelationKind.ACCESS_TYPE_TRANSITIVE,
								typeAccessElement);
				}

				if (curLocalVariable != null) {
					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curLocalVariable,
								ARelationKind.ACCESS_TYPE_TRANSITIVE,
								typeAccessElement);
					} else {
						aDB.addRelationAndTranspose(curLocalVariable,
								ARelationKind.ACCESS_TYPE, typeAccessElement);
						directRelationAdded = true;
					}
				}

				if (curField != null) {
					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curField,
								ARelationKind.ACCESS_TYPE_TRANSITIVE,
								typeAccessElement);
					} else {
						aDB.addRelationAndTranspose(curField,
								ARelationKind.ACCESS_TYPE, typeAccessElement);
						directRelationAdded = true;
					}
				}

				if (curMethod != null) {
					aDB.addRelationAndTranspose(curMethod,
							ARelationKind.DECLARES_TYPE_ACCESS,
							typeAccessElement);

					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curMethod,
								ARelationKind.ACCESS_TYPE_TRANSITIVE,
								typeAccessElement);
					} else {
						aDB.addRelationAndTranspose(curMethod,
								ARelationKind.ACCESS_TYPE, typeAccessElement);
					}
				}

			}

			// in VariableDeclarationFragment the type is stored
			// in parent node, which was already handled
			private void handleTypeAccessForVarDeclFragment(ASTNode node) {

				ASTNode parentNode = node.getParent();

				if (parentNode == null)
					return;

				Type type = null;

				switch (parentNode.getNodeType()) {
				case ASTNode.VARIABLE_DECLARATION_STATEMENT:
					type = ((VariableDeclarationStatement) parentNode)
							.getType();
					break;
				case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
					type = ((VariableDeclarationExpression) parentNode)
							.getType();
					break;
				case ASTNode.FIELD_DECLARATION:
					type = ((FieldDeclaration) parentNode).getType();
					break;
				}

				if (type == null)
					return;

				AElement typeAccessElement = (AElement) elementFactory
						.getElement(type);

				if (typeAccessElement == null)
					return;

				if (curField != null)
					aDB.addRelationAndTranspose(curField,
							ARelationKind.ACCESS_TYPE, typeAccessElement);

				if (curLocalVariable != null)
					aDB.addRelationAndTranspose(curLocalVariable,
							ARelationKind.ACCESS_TYPE, typeAccessElement);

			}

			// HANDLE FieldOrVariableAccess
			private void handleFieldOrVariableAccess(Name node,
					IVariableBinding binding) {

				ASTNode parent = node.getParent();

				// don't check field / variable access in declaration
				if (parent instanceof VariableDeclarationFragment)
					return;

				if (parent instanceof EnumConstantDeclaration)
					return;

				if (parent instanceof SingleVariableDeclaration)
					return;

				AElement element = elementFactory.getElement(binding);

				ARelationKind accessRelation = ARelationKind.ACCESS_FIELD;
				ARelationKind accessTransitiveRelation = ARelationKind.ACCESS_FIELD_TRANSITIVE;
				ARelationKind declaresRelation = ARelationKind.DECLARES_FIELD_ACCESS;
				AICategories cat = AICategories.FIELD_ACCESS;
				boolean isField = true;

				if (element != null
						&& element.getCategory() != AICategories.FIELD) {
					accessRelation = ARelationKind.ACCESS_LOCAL_VARIABLE;
					accessTransitiveRelation = ARelationKind.ACCESS_LOCAL_VARIABLE_TRANSITIVE;
					declaresRelation = ARelationKind.DECLARES_LOCAL_VARIABLE_ACCESS;
					cat = AICategories.LOCAL_VARIABLE_ACCESS;
				}

				AElement accessElement = elementFactory.createElement(cat,
						null, cuHash, node);
				addElement(accessElement, getColor(node));

				// ADD ACCESS TO ACTUAL Element
				if (element != null)
					aDB.addRelationAndTranspose(accessElement,
							ARelationKind.BELONGS_TO, element);

				// ADD ALL ELEMENTS WHO ACCESS THIS ELEMENT

				// ADD ALWAYS TRANS. ACCESS RELATION FOR COMP. UNIT
				aDB.addRelationAndTranspose(curCUElement,
						accessTransitiveRelation, accessElement);

				// ADD ALWAYS TRANS. ACCESS RELATION FOR TYPE
				if (curType != null) {

					aDB.addRelationAndTranspose(curType,
							accessTransitiveRelation, accessElement);

					// TODO: CHECK SUPER ACCESS FOR OUT OF CONTEXT ELEMENTS!

					// check if field access is a super field access
					if (curExtendsAccess != null && element != null && isField) {

						if (isSuperAccess(element,
								ARelationKind.T_DECLARES_FIELD)) {
							aDB.addRelationAndTranspose(accessElement,
									ARelationKind.BELONGS_TO, curExtendsAccess);
						}

					}
				}

				for (AElement tmpType : curTypeReminder) {
					aDB.addRelationAndTranspose(tmpType,
							accessTransitiveRelation, accessElement);
				}

				boolean directRelationAdded = false;
				// do not add variable access for it self
				if (curContext != null) {
					if (curContext.getElement() == null) {
						if (!node.equals(curContext.getAccessNode())) {
							directRelationAdded = true;
						}
					} else if (!curContext.getElement().equals(element)) {
						aDB.addRelationAndTranspose(curContext.getElement(),
								accessRelation, accessElement);
						directRelationAdded = true;
					}
				}

				LocalContextElement tmpContext;
				int size = curContextReminder.size();
				for (int i = 0; i < size; i++) {
					tmpContext = curContextReminder.get(i);

					// letzte eintrag!
					if (!directRelationAdded && size - i == 1) {

						directRelationAdded = true;

						if (tmpContext.getElement() == null)
							continue;

						aDB.addRelationAndTranspose(tmpContext.getElement(),
								accessRelation, accessElement);

						continue;

					}
					if (tmpContext.getElement() != null)
						aDB.addRelationAndTranspose(tmpContext.getElement(),
								accessTransitiveRelation, accessElement);

				}

				if (curLocalVariable != null) {
					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curLocalVariable,
								accessTransitiveRelation, accessElement);
					} else {
						aDB.addRelationAndTranspose(curLocalVariable,
								accessRelation, accessElement);
						directRelationAdded = true;
					}
				}

				if (curField != null) {
					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curField,
								accessTransitiveRelation, accessElement);
					} else {
						aDB.addRelationAndTranspose(curField, accessRelation,
								accessElement);
						directRelationAdded = true;
					}
				}

				if (curMethod != null) {
					aDB.addRelationAndTranspose(curMethod, declaresRelation,
							accessElement);

					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curMethod,
								accessTransitiveRelation, accessElement);
					} else {
						aDB.addRelationAndTranspose(curMethod, accessRelation,
								accessElement);
					}
				}

			}

			// HANDLE METHOD ACCESS
			private void handleMethodAccess(ASTNode node, AElement element) {

				AElement accessElement = elementFactory.createElement(
						AICategories.METHOD_ACCESS, null, cuHash, node);
				addElement(accessElement, getColor(node));

				// ADD BELONGS TO RELATION
				if (element != null)
					aDB.addRelationAndTranspose(accessElement,
							ARelationKind.BELONGS_TO, element);

				// ADD ALL ELEMENTS WHO ACCESS THIS ELEMENT

				// HANDLE CURRENT PARAMS
				if (curParameter != null) {
					for (AElement paramAccess : curParameter) {
						aDB.addRelationAndTranspose(accessElement,
								ARelationKind.DECLARES_PARAMETER, paramAccess);
					}
					curParameter = null;
				}

				// ADD ALWAYS TRANS. ACCESS RELATION FOR COMP. UNIT
				aDB.addRelationAndTranspose(curCUElement,
						ARelationKind.ACCESS_METHOD_TRANSITIVE, accessElement);

				// ADD ALWAYS TRANS. ACCESS RELATION FOR TYPE
				if (curType != null) {
					aDB.addRelationAndTranspose(curType,
							ARelationKind.ACCESS_METHOD_TRANSITIVE,
							accessElement);

					// TODO: CHECK IF IS SUPER ACCESS FOR OUT OF CONTEXT
					// ELEMENTS (element == null)
					// check if access is a super access
					if (curExtendsAccess != null && element != null) {

						if (isSuperAccess(element,
								ARelationKind.T_DECLARES_METHOD)) {
							aDB.addRelationAndTranspose(accessElement,
									ARelationKind.BELONGS_TO, curExtendsAccess);
						}

					}

				}

				for (AElement tmpType : curTypeReminder) {
					aDB.addRelationAndTranspose(tmpType,
							ARelationKind.ACCESS_METHOD_TRANSITIVE,
							accessElement);
				}

				boolean directRelationAdded = false;

				LocalContextElement tmpContext;
				int size = curContextReminder.size();
				for (int i = 0; i < size; i++) {
					tmpContext = curContextReminder.get(i);

					// set in last entry the direct relation
					if (size - i == 1) {

						directRelationAdded = true;

						// OUT OF CONTEXT ELEMENT!
						if (tmpContext.getElement() == null)
							continue;

						aDB.addRelationAndTranspose(tmpContext.getElement(),
								ARelationKind.ACCESS_METHOD, accessElement);

						continue;

					}

					// OUT OF CONTEXT ELEMENT!
					if (tmpContext.getElement() == null)
						continue;

					aDB.addRelationAndTranspose(tmpContext.getElement(),
							ARelationKind.ACCESS_METHOD_TRANSITIVE,
							accessElement);

				}

				if (curLocalVariable != null) {
					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curLocalVariable,
								ARelationKind.ACCESS_METHOD_TRANSITIVE,
								accessElement);
					} else {
						aDB.addRelationAndTranspose(curLocalVariable,
								ARelationKind.ACCESS_METHOD, accessElement);
						directRelationAdded = true;
					}
				}

				if (curField != null) {
					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curField,
								ARelationKind.ACCESS_METHOD_TRANSITIVE,
								accessElement);
					} else {
						aDB.addRelationAndTranspose(curField,
								ARelationKind.ACCESS_METHOD, accessElement);
						directRelationAdded = true;
					}
				}

				if (curMethod != null) {
					aDB
							.addRelationAndTranspose(curMethod,
									ARelationKind.DECLARES_METHOD_ACCESS,
									accessElement);

					if (directRelationAdded) {
						aDB.addRelationAndTranspose(curMethod,
								ARelationKind.ACCESS_METHOD_TRANSITIVE,
								accessElement);
					} else {
						aDB.addRelationAndTranspose(curMethod,
								ARelationKind.ACCESS_METHOD, accessElement);
					}
				}

			}

			private boolean isSuperAccess(AElement declElement,
					ARelationKind declRelation) {

				Set<AElement> declareRange = aDB.getRange(declElement,
						declRelation);

				if (!declareRange.contains(curType)) {
					Set<AElement> superTypes = new HashSet<AElement>();
					superTypes.addAll(aDB.getRange(curType,
							ARelationKind.EXTENDS_TYPE));
					superTypes.addAll(aDB.getRange(curType,
							ARelationKind.EXTENDS_TYPE_TRANSITIVE));

					for (AElement tmpSuperType : superTypes) {
						if (declareRange.contains(tmpSuperType)) {
							return true;
						}

					}

				}

				return false;

			}

		});
	}

	public void buildRelations(ICompilationUnit cu, int cuHash,
			AElementColorManager elementColorManager,
			SourceFileColorManager sourceColorManager) {

		this.cuHash = cuHash;
		this.elementColorManager = elementColorManager;
		this.sourceColorManager = sourceColorManager;

		try {

			CompilationUnit ast = JDTParserWrapper.parseCompilationUnit(cu);
			reset();
			update(ast);

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
