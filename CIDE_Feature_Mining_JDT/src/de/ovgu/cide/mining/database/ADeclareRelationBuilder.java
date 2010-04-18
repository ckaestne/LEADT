package de.ovgu.cide.mining.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import cide.gparser.ParseException;
import de.ovgu.cide.language.jdt.JDTParserWrapper;
import de.ovgu.cide.mining.database.model.ACompilationUnitElement;
import de.ovgu.cide.mining.database.model.AFieldElement;
import de.ovgu.cide.mining.database.model.AFlyweightElementFactory;
import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AIElement;
import de.ovgu.cide.mining.database.model.AImportElement;
import de.ovgu.cide.mining.database.model.ALocalVariableElement;
import de.ovgu.cide.mining.database.model.AMethodElement;
import de.ovgu.cide.mining.database.model.ARelation;
import de.ovgu.cide.mining.database.model.ATypeElement;

/**
 * @author A.Dreiling
 * 
 */
public class ADeclareRelationBuilder implements Serializable {

	private static final long serialVersionUID = 3L;


	private ProgramDatabase aDB;
	private AFlyweightElementFactory elementFactory;
	private int cuHash;
	
	private ACompilationUnitElement curCUElement; 
	private ATypeElement 	curType;
	private AMethodElement 	curMethod;
	private int curParamIndex;
	
	
	private Stack<ATypeElement>	curTypeReminder; 
	
	public ADeclareRelationBuilder(ProgramDatabase aDB, AFlyweightElementFactory elementFactory) {
		this.aDB = aDB;
		this.elementFactory = elementFactory;
		curTypeReminder = new Stack<ATypeElement>();
		curType = null;
		curMethod = null;
		curParamIndex = -1;
	}

	void traverseAST(ASTNode node) {
		
		node.accept(new ASTVisitor() {
			
			@Override
			public boolean visit(CompilationUnit node) {
				//create the CU element and store it 
				curCUElement = (ACompilationUnitElement)elementFactory.createElement(AICategories.COMPILATION_UNIT, null,cuHash, node);
				aDB.addElement(curCUElement);
				
				return super.visit(node);
			}
			
			@Override
			public boolean visit(ImportDeclaration node) {
				
				AImportElement curImport = (AImportElement)elementFactory.createElement(AICategories.IMPORT, null ,cuHash, node); 
				aDB.addElement(curImport);
				
				aDB.addRelationAndTranspose(curCUElement, ARelation.DECLARES_IMPORT, curImport);
				
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
			
				//backup the current type
				ATypeElement oldType = curType;
								
				curType = (ATypeElement)elementFactory.createElement(AICategories.TYPE, binding,cuHash, node); 
				aDB.addElement(curType, binding.getModifiers());
				
				
				
				
				
				if (!binding.isTopLevel()) {
					
					//ADD DECLARE RELATIONSHIP FOR COMPILTATTION UNIT
					aDB.addRelationAndTranspose(curCUElement, ARelation.DECLARES_TYPE_TRANSITIVE, curType);
					
					//ADD DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(oldType, ARelation.DECLARES_TYPE, curType);
						
					//ADD TRANSITIVE DECLARE RELATIONSHIP FOR SUPER TYPES
					for (ATypeElement remType : curTypeReminder) {
						aDB.addRelationAndTranspose(remType, ARelation.DECLARES_TYPE_TRANSITIVE, curType);	
					}
				
					curTypeReminder.push( oldType );
				
				} 
				else {
					//ADD DECLARE RELATIONSHIP FOR COMPILTATTION UNIT
					aDB.addRelationAndTranspose(curCUElement, ARelation.DECLARES_TYPE, curType);
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
				
				//restore current type and temp method
				if( !curTypeReminder.isEmpty() ) {
					curType = (ATypeElement) curTypeReminder.pop();
				}
				else {
					curType = null;
				}
			}

			
			public boolean visit(MethodDeclaration node) {

				IMethodBinding binding = node.resolveBinding();
				if (binding != null) {
								
					curMethod = (AMethodElement)elementFactory.createElement(AICategories.METHOD, binding, cuHash, node); 
					aDB.addElement(curMethod, binding.getModifiers());
					
					curParamIndex = 0;
					
					//ADD DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(curType, ARelation.DECLARES_METHOD, curMethod);
					
					//ADD TRANSITIVE DECLARE RELATIONSHIP FOR COMPILTATTION UNIT
					aDB.addRelationAndTranspose(curCUElement, ARelation.DECLARES_METHOD_TRANSITIVE, curMethod);
						
					//ADD TRANSITIVE DECLARE RELATIONSHIP FOR SUPER TYPES
					for (ATypeElement remType : curTypeReminder) {
						aDB.addRelationAndTranspose(remType, ARelation.DECLARES_METHOD_TRANSITIVE, curMethod);	
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


			public void visitFieldOrVariable(ASTNode node, IVariableBinding binding) {
				
				if (binding == null)
					return;
				
				AIElement curElement = null;
				ARelation curRelation = null;
				ARelation curTransitiveRelation = null;
				
				if (binding.isField() || binding.isEnumConstant()) {
				
					curElement = (AFieldElement)elementFactory.createElement(AICategories.FIELD, binding,cuHash, node); 
					aDB.addElement(curElement, binding.getModifiers());
					curRelation = ARelation.DECLARES_FIELD;
					curTransitiveRelation = ARelation.DECLARES_FIELD_TRANSITIVE;
					
					//ADD  DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(curType, curRelation, curElement);
									
					
				}
				else {
					//is "Parameter" or "Local Variable"
				    curElement = (ALocalVariableElement)elementFactory.createElement(AICategories.LOCAL_VARIABLE, binding,cuHash, node); 
				    
				    if (binding.isParameter())
				    	((ALocalVariableElement)curElement).setParamIndex(curParamIndex++);
				    
				    aDB.addElement(curElement, binding.getModifiers());
					curRelation = ARelation.DECLARES_LOCAL_VARIABLE;
					curTransitiveRelation = ARelation.DECLARES_LOCAL_VARIABLE_TRANSITIVE;
					
					//ADD  DECLARE RELATIONSHIP FOR TYPE
					aDB.addRelationAndTranspose(curType, curTransitiveRelation, curElement);
					
									
					//check if null as block could also be an intializer 
					if (curMethod != null) {
						//ADD DECLARE RELATIONSHIP FOR METHOD
						aDB.addRelationAndTranspose(curMethod, curRelation, curElement);
					}
					
					
				}
				
				
				//ADD TRANSITIVE DECLARE RELATIONSHIP FOR COMPILTATTION UNIT
				aDB.addRelationAndTranspose(curCUElement, curTransitiveRelation, curElement);
					
				//ADD TRANSITIVE DECLARE RELATIONSHIP FOR SUPER TYPES
				for (ATypeElement remType : curTypeReminder) {
					aDB.addRelationAndTranspose(remType, curTransitiveRelation, curElement);	
				}
				
	
			}
					
		});
	}


	public void createElementsAndDeclareRelations(ICompilationUnit cu, int cuHash) {
		this.cuHash = cuHash;

		try {
					
			CompilationUnit ast = JDTParserWrapper
					.parseCompilationUnit(cu);
			traverseAST(ast);
		
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	

}
