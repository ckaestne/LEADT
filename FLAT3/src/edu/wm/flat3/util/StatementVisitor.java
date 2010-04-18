package edu.wm.flat3.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * Visitor to calculate the number of lines for a statement. Uses the visitor
 * pattern's dispatch mechanism to recursively calculate the number of lines if
 * required.
 * 
 * @author vgarg
 * 
 */
public class StatementVisitor extends ASTVisitor
{
	// The compilation unit ass
	private CompilationUnit cu;

	private LineSet sourceLineCounter;
	
	private int linesAtStart;

	// Don't make this static!
	public StatementVisitor(CompilationUnit cu, LineSet sourceLineCounter)
	{
		this.cu = cu;
		this.sourceLineCounter = sourceLineCounter;
		
		// Keep track of how many source lines were marked when we
		// start so we know how many unique lines were visited
		this.linesAtStart = sourceLineCounter.size();
	}
	
	public int getSourceLineCount()
	{
		return sourceLineCounter.size() - linesAtStart;
	}
	
	/**
	 * @param statement
	 */
	private void markLinesForStatement(ASTNode statementNode, 
	                                   int statementStartLine)
	{
		int startPos = statementNode.getStartPosition();
		int startLine = cu.getLineNumber(startPos);
		int endLine = sourceLineCounter.getEndLineNumberHelper(statementNode);

		//if (startLine != statementStartLine)
		//{
		//	return endLine - startLine + 2;
		//}
		//else
		//{
		//	return endLine - startLine + 1;
		//}
		
		sourceLineCounter.add(startLine, endLine);
	}

	/**
	 * Checks if the statement is a single line statement.
	 * 
	 * @param node
	 * @return
	 */
	private boolean isSingleLine(ASTNode node)
	{
		int startPos = node.getStartPosition();
		int startLine = cu.getLineNumber(startPos);
		int endLine = sourceLineCounter.getEndLineNumberHelper(node);
		
		return startLine == endLine;
	}
	
	/**
	 * Returns the number of lines for the statement and expression without
	 * body. e.g. <code>
	 *    if(a == b)
	 *    {
	 *      a+=b;
	 *    }
	 * </code> will return 1, since the
	 * body (starting with '{' starts at the next line
	 * 
	 * @param statement
	 * @param node
	 * @return
	 */
	private boolean startsOnSameLine(ASTNode statement, ASTNode node)
	{
		int statStartLine = cu.getLineNumber(statement.getStartPosition());
		int nodeStartLine = cu.getLineNumber(node.getStartPosition());

		return statStartLine == nodeStartLine;
	}

	private void markStartOfStatementAndBody(ASTNode statement, ASTNode node)
	{
		int statStartLine = cu.getLineNumber(statement.getStartPosition());
		int nodeStartLine = cu.getLineNumber(node.getStartPosition());

		if (statStartLine != nodeStartLine)
		{
			sourceLineCounter.add(statStartLine);
		}
	}
	
	/**
	 * Same as above but checks for the end
	 * 
	 * @param statement
	 * @param node
	 * @return
	 */
	private void markEndOfStatementAndBody(ASTNode statement, ASTNode node)
	{
		int statEndLine = sourceLineCounter.getEndLineNumberHelper(statement);
		int nodeEndLine = sourceLineCounter.getEndLineNumberHelper(node);

		if (statEndLine != nodeEndLine)
		{
			sourceLineCounter.add(statEndLine);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.IfStatement)
	 */
	@Override
	public boolean visit(IfStatement statement)
	{
		// if single line statement, increment totalLines by 1 and return.
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}

		// if statement and its body dont start on the same line, calculate the
		// number
		// of lines used by the start of statement ('if' node and expression )
		// and add tot totalLines, otherwise disregard becoz we use the body
		// as the starting point.
		if (!startsOnSameLine(statement, statement.getThenStatement()))
		{
			markLinesForStatement(statement.getExpression(), 
					cu.getLineNumber(statement.getStartPosition()));
		}
		// recursively calculate num lines for 'then' part
		statement.getThenStatement().accept(this);
		// if 'else' exists, recursively calculate num lines for it.
		Statement elseStatement = statement.getElseStatement();
		if (elseStatement != null)
		{
			elseStatement.accept(this);
		}

		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ForStatement)
	 */
	@Override
	public boolean visit(ForStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
		}

		// calculate num lines for expression line. This will also contain
		// comments if any in the expression line of for statement
		markStartOfStatementAndBody(statement, statement.getBody());

		// get num lines for body.
		statement.getBody().accept(this);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EnhancedForStatement)
	 */
	@Override
	public boolean visit(EnhancedForStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}

		if (!startsOnSameLine(statement, statement.getBody()))
		{
			calSlocForExpression(statement, statement.getExpression());
		}
		statement.getBody().accept(this);
		return false;
	}

	public boolean visit(VariableDeclaration declaration)
	{
		if (isSingleLine(declaration))
		{
			sourceLineCounter.add(declaration);
			return false;
		}

		markLinesForStatement(declaration, 
				cu.getLineNumber(declaration.getInitializer().getStartPosition()));

		return false;
	}

	@Override
	public boolean visit(WhileStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		if (!startsOnSameLine(statement, statement.getBody()))
		{
			calSlocForExpression(statement, statement.getExpression());
		}
		statement.getBody().accept(this);

		return false;
	}

	@Override
	public boolean visit(DoStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		if (!startsOnSameLine(statement, statement.getBody()))
		{
			calSlocForExpression(statement, statement.getExpression());
		}
		statement.getBody().accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TryStatement)
	 */
	@Override
	public boolean visit(TryStatement statement)
	{
		// num lines for try itself
		markStartOfStatementAndBody(statement, statement.getBody());
		// add lines for all 'catch' clauses

		for (Object object : statement.catchClauses())
		{
			CatchClause catchClause = (CatchClause) object;
			catchClause.accept(this);
		}
		// num lines for body of 'try'
		statement.getBody().accept(this);
		// num lines for 'finally' if it exists.
		Block finallyClause = statement.getFinally();
		if (finallyClause != null)
		{
			finallyClause.accept(this);
		}
		return false;
	}

	@Override
	public boolean visit(CatchClause catchClause)
	{
		if (isSingleLine(catchClause))
		{
			sourceLineCounter.add(catchClause);
			return false;
		}
		markStartOfStatementAndBody(catchClause, catchClause.getException());
		catchClause.getException().accept(this);
		catchClause.getBody().accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchStatement)
	 */
	@Override
	public boolean visit(SwitchStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		// get num lines for expression of switch statement
		calSlocForExpression(statement, statement.getExpression());
		
		// go through all the statements in the 'switch' statement.
		// these will include the 'case' statements and statements within
		// the cases.

		Statement firstStatement = null;
		Statement lastStatement = null;
		for (Object object : statement.statements())
		{
			lastStatement = (Statement) object;
			if (firstStatement == null)
			{
				firstStatement = lastStatement;
			}
			
			lastStatement.accept(this);
		}

		// check if first statement is on the same line as 'switch'
		if (firstStatement != null)
		{
			markStartOfStatementAndBody(statement, firstStatement);
		}
		
		// check if last statement is on the same line as 'switch'
		if (lastStatement != null)
		{
			markEndOfStatementAndBody(statement, lastStatement);
		}
		
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchCase)
	 */
	@Override
	public boolean visit(SwitchCase statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		// calculate sloc for case.
		Expression expression = statement.getExpression();
		calSlocForExpression(statement, expression);
		return false;
	}

	@Override
	public boolean visit(SynchronizedStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		calSlocForExpression(statement, statement.getExpression());
		statement.getBody().accept(this);
		return false;
	}

	@Override
	public boolean visit(ReturnStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		calSlocForExpression(statement, statement.getExpression());
		return false;
	}

	/**
	 * @param statement
	 */
	private void calSlocForExpression(ASTNode statement, Expression expression)
	{
		if (expression != null)
		{
			markStartOfStatementAndBody(statement, expression);
		}
		ExpressionVisitor visitor = new ExpressionVisitor(cu, sourceLineCounter);
		expression.accept(visitor);
	}

	@Override
	public boolean visit(ThrowStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		calSlocForExpression(statement, statement.getExpression());
		return false;
	}

	@Override
	public boolean visit(BreakStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		calSlocForExpression(statement, statement.getLabel());
		return false;
	}

	@Override
	public boolean visit(ContinueStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		calSlocForExpression(statement, statement.getLabel());
		return false;
	}

	@Override
	public boolean visit(EmptyStatement statement)
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ExpressionStatement)
	 */
	@Override
	public boolean visit(ExpressionStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		
		ExpressionVisitor visitor = new ExpressionVisitor(cu, sourceLineCounter);
		// visit expression
		statement.getExpression().accept(visitor);

		return false;
	}

	@Override
	public boolean visit(LabeledStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}

		statement.getBody().accept(this);
		return false;
	}

	@Override
	public boolean visit(AssertStatement statement)
	{
		markStartOfStatementAndBody(statement, statement.getExpression());
		statement.getExpression().accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
	 */
	@Override
	public boolean visit(VariableDeclarationStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
			return false;
		}
		// a variable declaration statement can have multiple
		// declarations, so we calculate sloc for each. we also
		// check if the first declaration is on the same line
		// as the main declaration.

		VariableDeclarationFragment firstFrag = null;
		int startLine = -1;
		for (Object object : statement.fragments())
		{
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) object;
			int fragLineNum = cu.getLineNumber(fragment.getStartPosition());
			if (fragLineNum != startLine)
			{
				fragment.accept(this);
			}
			if (startLine == -1)
			{
				startLine = fragLineNum;
				firstFrag = fragment;

			}
		}
		markStartOfStatementAndBody(statement, firstFrag);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment)
	{
		if (isSingleLine(fragment))
		{
			sourceLineCounter.add(fragment);
			return false;
		}
		calSlocForExpression(fragment, fragment.getInitializer());
		return false;
	}

	@Override
	public boolean visit(TypeDeclarationStatement statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
		}
		
		return false;
	}

	@Override
	public boolean visit(ConstructorInvocation statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
		}
		
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Block)
	 */
	@Override
	public boolean visit(Block block)
	{
		// get sloc for each line in the block and also check
		// the first and the last lines in the block
		if (isSingleLine(block) || block.statements().isEmpty())
		{
			sourceLineCounter.add(block);
			return false;
		}

		Statement firstStatement = null;
		Statement lastStatement = null;
		
		for (Object object : block.statements())
		{
			lastStatement = (Statement) object;
			if (firstStatement == null)
			{
				firstStatement = lastStatement;
			}
			lastStatement.accept(this);
		}

		if (firstStatement != null)
		{
			markStartOfStatementAndBody(block, firstStatement);
		}
		
		if (lastStatement != null)
		{
			markEndOfStatementAndBody(block, lastStatement);
		}
		
		return false;

	}

	@Override
	public boolean visit(SuperConstructorInvocation statement)
	{
		if (isSingleLine(statement))
		{
			sourceLineCounter.add(statement);
		}

		return false;
	}
	
	@Override
	public String toString()
	{
		return sourceLineCounter.toString();
	}
}
