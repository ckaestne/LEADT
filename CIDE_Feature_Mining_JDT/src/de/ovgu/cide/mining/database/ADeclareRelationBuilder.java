package de.ovgu.cide.mining.database;

import java.io.Serializable;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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

/**
 * @author A.Dreiling
 * 
 */
public class ADeclareRelationBuilder implements Serializable {

	private static final long serialVersionUID = 3L;

	private AbstractProgramDatabase aDB;
	private AFlyweightElementFactory elementFactory;
	private int cuHash;

	private AElement curCUElement;
	private AElement curType;
	private AElement curMethod;
	private int curParamIndex;

	private Stack<AElement> curTypeReminder;

	private AElementColorManager elementColorManager;

	private SourceFileColorManager sourceColorManager;

	public ADeclareRelationBuilder(AbstractProgramDatabase aDB,
			AFlyweightElementFactory elementFactory) {
		this.aDB = aDB;
		this.elementFactory = elementFactory;
		curTypeReminder = new Stack<AElement>();
		curType = null;
		curMethod = null;
		curParamIndex = -1;
	}

	void traverseAST(ASTNode node) {

		node.accept(new ASTVisitor() {

			@Override
			public boolean visit(CompilationUnit node) {
				// create the CU element and store it
				curCUElement = (AElement) elementFactory.createElement(
						AICategories.COMPILATION_UNIT, null, cuHash, node);
				addElement(curCUElement, getColor(node));

				return super.visit(node);
			}

			private void addElement(AElement element, Set<IFeature> colors) {
				aDB.addElement(element);
				for (IFeature color : colors)
					elementColorManager.addElementToColor(color, element);
			}

			private Set<IFeature> getColor(ASTNode node) {
				return sourceColorManager.getColors(ASTBridge.bridge(node));
			}

			@Override
			public boolean visit(ImportDeclaration node) {

				AElement curImport = (AElement) elementFactory.createElement(
						AICategories.IMPORT, null, cuHash, node);
				addElement(curImport, getColor(node));

				aDB.addRelationAndTranspose(curCUElement,
						ARelationKind.DECLARES_IMPORT, curImport);

				return super.visit(node);
			}

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

				// backup the current type
				AElement oldType = curType;

				curType = (AElement) elementFactory.createElement(
						AICategories.TYPE, binding, cuHash, node);
				addElement(curType, getColor(node));

				if (!binding.isTopLevel()) {

					// ADD DECLARE RELATIONSHIP FOR COMPILTATTION UNIT
					aDB.addRelationAndTranspose(curCUElement,
							ARelationKind.DECLARES_TYPE_TRANSITIVE, curType);

					// ADD DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(oldType,
							ARelationKind.DECLARES_TYPE, curType);

					// ADD TRANSITIVE DECLARE RELATIONSHIP FOR SUPER TYPES
					for (AElement remType : curTypeReminder) {
						aDB
								.addRelationAndTranspose(remType,
										ARelationKind.DECLARES_TYPE_TRANSITIVE,
										curType);
					}

					curTypeReminder.push(oldType);

				} else {
					// ADD DECLARE RELATIONSHIP FOR COMPILTATTION UNIT
					aDB.addRelationAndTranspose(curCUElement,
							ARelationKind.DECLARES_TYPE, curType);
				}

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

			public boolean visit(MethodDeclaration node) {

				IMethodBinding binding = node.resolveBinding();
				if (binding != null) {

					curMethod = (AElement) elementFactory.createElement(
							AICategories.METHOD, binding, cuHash, node);
					addElement(curMethod, getColor(node));

					curParamIndex = 0;

					// ADD DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(curType,
							ARelationKind.DECLARES_METHOD, curMethod);

					// ADD TRANSITIVE DECLARE RELATIONSHIP FOR COMPILTATTION
					// UNIT
					aDB
							.addRelationAndTranspose(curCUElement,
									ARelationKind.DECLARES_METHOD_TRANSITIVE,
									curMethod);

					// ADD TRANSITIVE DECLARE RELATIONSHIP FOR SUPER TYPES
					for (AElement remType : curTypeReminder) {
						aDB.addRelationAndTranspose(remType,
								ARelationKind.DECLARES_METHOD_TRANSITIVE,
								curMethod);
					}

				}
				return super.visit(node);
			}

			@Override
			public void endVisit(MethodDeclaration node) {
				curMethod = null;

			}

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

				AElement curElement = null;
				ARelationKind curRelation = null;
				ARelationKind curTransitiveRelation = null;

				if (binding.isField() || binding.isEnumConstant()) {

					curElement = (AElement) elementFactory.createElement(
							AICategories.FIELD, binding, cuHash, node);
					addElement(curElement, getColor(node));
					curRelation = ARelationKind.DECLARES_FIELD;
					curTransitiveRelation = ARelationKind.DECLARES_FIELD_TRANSITIVE;

					// ADD DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(curType, curRelation,
							curElement);

				} else {
					// is "Parameter" or "Local Variable"
					curElement = (AElement) elementFactory.createElement(
							AICategories.LOCAL_VARIABLE, binding, cuHash, node);

					if (binding.isParameter())
						((AElement) curElement).setParamIndex(curParamIndex++);

					addElement(curElement, getColor(node));
					curRelation = ARelationKind.DECLARES_LOCAL_VARIABLE;
					curTransitiveRelation = ARelationKind.DECLARES_LOCAL_VARIABLE_TRANSITIVE;

					// ADD DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(curType, curTransitiveRelation,
							curElement);

					// check if null as block could also be an intializer
					if (curMethod != null) {
						// ADD DECLARE RELATIONSHIP FOR METHOD
						aDB.addRelationAndTranspose(curMethod, curRelation,
								curElement);
					}

				}

				// ADD TRANSITIVE DECLARE RELATIONSHIP FOR COMPILTATTION UNIT
				aDB.addRelationAndTranspose(curCUElement,
						curTransitiveRelation, curElement);

				// ADD TRANSITIVE DECLARE RELATIONSHIP FOR SUPER TYPES
				for (AElement remType : curTypeReminder) {
					aDB.addRelationAndTranspose(remType, curTransitiveRelation,
							curElement);
				}

			}

		});
	}

	public void createElementsAndDeclareRelations(ICompilationUnit cu,
			int cuHash, AElementColorManager elementColorManager,
			SourceFileColorManager sourceColorManager) {
		this.cuHash = cuHash;
		this.elementColorManager = elementColorManager;
		this.sourceColorManager = sourceColorManager;

		try {

			CompilationUnit ast = JDTParserWrapper.parseCompilationUnit(cu);
			traverseAST(ast);

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
