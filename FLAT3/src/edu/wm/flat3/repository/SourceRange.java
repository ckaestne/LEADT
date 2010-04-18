package edu.wm.flat3.repository;

import java.util.List;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.wm.flat3.util.LineSet;
import edu.wm.flat3.util.ProblemManager;
import edu.wm.flat3.util.StatementVisitor;

public class SourceRange implements Comparable<SourceRange>
{
	private int beginLine = 0;
	private int beginColumn = 0;
	private int endLine = 0;
	private int endColumn = 0;
	private int numLines = 0;

	public SourceRange(int beginLine, 
	                   int beginColumn, 
	                   int endLine, 
	                   int endColumn,
	                   int numLines)
	{
		this.beginLine = beginLine;
		this.beginColumn = beginColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
		this.numLines = numLines;
	}
	
	public int getBeginLine()
	{
		return beginLine;
	}
	
	public int getBeginColumn()
	{
		return beginColumn;
	}

	public int getEndLine()
	{
		return endLine;
	}

	public int getEndColumn()
	{
		return endColumn;
	}

	public int getNumSourceLines()
	{
		return numLines;
	}
	
	public static SourceRange create(CompilationUnit compilationUnit, 
	                                 IMember member,
	                                 String name,
	                                 LineSet sourceLineAccumulator)
	{
		try
		{
			ISourceRange iSourceRange = member.getSourceRange();
			
			SourceRange sourceRange = createHelper(compilationUnit, 
					iSourceRange.getOffset(), 
					iSourceRange.getLength());
			
			// Keep track that we are claiming these source lines so
			// we don't double count
			sourceLineAccumulator.setDebugInfo(name);
			sourceLineAccumulator.setReportDoubleCounting(true);
			sourceLineAccumulator.add(sourceRange.beginLine, sourceRange.endLine);
			sourceLineAccumulator.setReportDoubleCounting(false);
			
			return sourceRange;
			
		}
		catch (JavaModelException e)
		{
			ProblemManager.reportException(e,
					"Failed to obtain line number info for member: " +
					member.getDeclaringType().getFullyQualifiedName() +
					"." + member.getElementName(), true);
			return null;
		}
	}
	
	public static SourceRange create(	CompilationUnit compilationUnit, 
	                                 	Type fieldTypeDecl,
	                                 	VariableDeclarationFragment fieldFragment,
	                                    String fieldName,
	                                    LineSet sourceLineAccumulator)
	{
		SourceRange sourceRange = createHelper(compilationUnit, 
				(ASTNode) fieldFragment);

		if (fieldTypeDecl != null)
		{
			// TODO: find way to get to the char pos of first modifier
			
			int typeBeginLine = compilationUnit.getLineNumber(
					fieldTypeDecl.getStartPosition());
			
			assert typeBeginLine <= sourceRange.beginLine;

			sourceRange.beginLine = typeBeginLine; 
		}

		sourceLineAccumulator.setDebugInfo(fieldName);
		sourceLineAccumulator.setReportDoubleCounting(true);
		sourceRange.numLines = sourceLineAccumulator.add(	sourceRange.beginLine, 
															sourceRange.endLine);
		sourceLineAccumulator.setReportDoubleCounting(false);
		
		// Note: We double count when multiple fields are on the same line:
		// int i, j;  Make sure each component has at least one line associated
		// with it.
		
		if (sourceRange.numLines == 0)
			sourceRange.numLines = 1;
		
		return sourceRange;
	}
	
	public static SourceRange create(	CompilationUnit compilationUnit, 
	                                    MethodDeclaration methodDeclaration,
	                                    String methodName,
	                                    LineSet sourceLineAccumulator)
	{
		SourceRange sourceRange = createHelper(compilationUnit, 
				(ASTNode) methodDeclaration);

		sourceLineAccumulator.setDebugInfo(methodName);
		
		// TODO: find way to get to the char pos of first modifier
		
		int nameBeginLine = compilationUnit.getLineNumber(
				methodDeclaration.getName().getStartPosition());
		
		// beginLine may start at the JavaDoc comment so advance it to
		// the type portion
		assert sourceRange.beginLine <= nameBeginLine;
		
		sourceRange.beginLine = nameBeginLine; 

		Block body = methodDeclaration.getBody();
		if (body != null)
		{
			StatementVisitor bodyVisitor = new StatementVisitor(compilationUnit,
					sourceLineAccumulator);
			
			int methodDeclEndLineNum = compilationUnit.getLineNumber(
					body.getStartPosition());
			
			body.accept(bodyVisitor);
			
			if (sourceRange.beginLine != methodDeclEndLineNum)
			{
				sourceLineAccumulator.add(sourceRange.beginLine, 
						methodDeclEndLineNum - 1);
			}
			
			sourceRange.numLines = bodyVisitor.getSourceLineCount();
		}
		else
		{
			sourceLineAccumulator.setReportDoubleCounting(true);
			sourceRange.numLines = sourceLineAccumulator.add(sourceRange.beginLine, 
					sourceRange.endLine);
			sourceLineAccumulator.setReportDoubleCounting(false);
		}
		
		assert sourceRange.numLines > 0;
		
		return sourceRange;
	}

	public static SourceRange create(	CompilationUnit compilationUnit, 
	                                    AbstractTypeDeclaration typeDeclaration,
	                                    int childrenSlocs,
	                                    String typeName,
	                                    LineSet sourceLineAccumulator)
	{
		SourceRange sourceRange = createHelper(compilationUnit, 
				(ASTNode) typeDeclaration);

		assert childrenSlocs <= sourceRange.numLines;
		
		// TODO: find way to get to the char pos of first modifier
		
		int nameBeginLine = compilationUnit.getLineNumber(
				typeDeclaration.getName().getStartPosition());
		
		// beginLine may start at the JavaDoc comment so advance it to
		// the type portion
		assert sourceRange.beginLine <= nameBeginLine;

		sourceRange.beginLine = nameBeginLine;

		List bodyDeclarations = typeDeclaration.bodyDeclarations();
		if (bodyDeclarations == null || bodyDeclarations.isEmpty())
		{
			sourceLineAccumulator.setReportDoubleCounting(true);
			sourceRange.numLines = sourceLineAccumulator.add(sourceRange.beginLine, 
					sourceRange.endLine);
			sourceLineAccumulator.setReportDoubleCounting(false);
			return sourceRange;
		}

		BodyDeclaration firstBodyDecl = (BodyDeclaration) bodyDeclarations.get(0);
		BodyDeclaration lastBodyDecl = (BodyDeclaration) bodyDeclarations.get(bodyDeclarations.size()-1);;
		
		int childrenBeginLine = compilationUnit.getLineNumber(
				firstBodyDecl.getStartPosition());
		assert childrenBeginLine > 0 && childrenBeginLine >= sourceRange.beginLine;
		
		int childrenEndLine = compilationUnit.getLineNumber(
				lastBodyDecl.getStartPosition() + lastBodyDecl.getLength() - 1);
		assert childrenEndLine > 0 && childrenEndLine >= childrenBeginLine;
		assert childrenEndLine <= sourceRange.endLine;

		sourceLineAccumulator.setDebugInfo(typeName);
		sourceLineAccumulator.setReportDoubleCounting(true);
		
		int totalSlocs = childrenSlocs;

		if (sourceRange.beginLine != childrenBeginLine)
		{
			totalSlocs += sourceLineAccumulator.add(sourceRange.beginLine, 
													childrenBeginLine - 1);
		}
		
		if (sourceRange.endLine != childrenEndLine)
		{
			totalSlocs += sourceLineAccumulator.add(childrenEndLine + 1, 
													sourceRange.endLine);
		}
		
		sourceLineAccumulator.setReportDoubleCounting(false);
		
		assert totalSlocs >= childrenSlocs;
		assert totalSlocs <= sourceRange.numLines;
		
		sourceRange.numLines = totalSlocs;
		assert sourceRange.numLines > 0;
		
		return sourceRange;
	}
	
	private static SourceRange createHelper(	CompilationUnit compilationUnit, 
	                                        	ASTNode node)
	{
		return createHelper(compilationUnit, node.getStartPosition(), 
				node.getLength());
	}

	private static SourceRange createHelper(	CompilationUnit compilationUnit, 
	                                            int startPosition,
	                                            int length)
	{
		int endPosition = startPosition + length - 1;
		
		int beginLine = compilationUnit.getLineNumber(startPosition);
		assert beginLine > 0;
		
		int endLine = compilationUnit.getLineNumber(endPosition);
		assert endLine > 0 && endLine >= beginLine;

		int beginColumn = compilationUnit.getColumnNumber(startPosition);
		assert beginColumn >= 0;

		int endColumn = compilationUnit.getColumnNumber(endPosition); 
		assert endColumn >= 0; // && endColumn >= beginColumn; // Not True!

		// Includes comments and blank lines
		int numLines = endLine - beginLine + 1;
		
		return new SourceRange(beginLine, beginColumn, endLine, endColumn, numLines);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof SourceRange))
			return false;
		
		return equals((SourceRange) obj);
	}
	
	public boolean equals(SourceRange obj)
	{
		return this.beginLine == obj.beginLine &&
			this.beginColumn == obj.beginColumn &&
			this.endLine == obj.endLine &&
			this.endColumn == obj.endColumn &&
			this.numLines == obj.numLines;
	}
	
	@Override
	public String toString()
	{
		return "StartLine: " + beginLine + ", StartCol: " + beginColumn +
			", EndLine: " + endLine + ", EndCol: " + endColumn + ", Lines: " +
			numLines;
	}

	@Override
	public int compareTo(SourceRange rhs)
	{
		return sumAll() - rhs.sumAll();
	}
	
	private int sumAll()
	{
		return beginLine + beginColumn + endLine + endColumn + numLines;
	}
}
