package edu.wm.flat3.util;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/**
 * Visitor class for the expressions in a statement. This visitor will only be
 * used if a statement spans multiple lines. It calculates the number of lines
 * spanned by the expression. It does not take into account the fact that the
 * expression could itself have some comments.
 * 
 * @author vgarg
 * 
 */
public class ExpressionVisitor extends ASTVisitor
{
	// the compilation unit associated with the expression
	private CompilationUnit cu;

	private LineSet sourceLineCounter;
	
	public ExpressionVisitor(CompilationUnit cu, LineSet sourceLineCounter)
	{
		this.cu = cu;
		this.sourceLineCounter = sourceLineCounter;
	}

	@Override
	public boolean visit(BooleanLiteral expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(StringLiteral expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(TypeLiteral expression)
	{
		// dont think we dont need to anything here
		return false;
	}

	@Override
	public boolean visit(ThisExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(FieldAccess expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Assignment)
	 */
	@Override
	public boolean visit(Assignment expression)
	{
		// if we get here we should only need to calcualate num lines
		// for RHS because the LHS has been calculated in the StatementVisitor
		// the only issue is the operator since we dont know where it starts.
		int startLine = cu.getLineNumber(expression.getStartPosition());
		Expression rhs = expression.getRightHandSide();
		int rhsLine = cu.getLineNumber(rhs.getStartPosition());
		
		sourceLineCounter.add(startLine, rhsLine);
		
		rhs.accept(this);
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(ArrayCreation expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(MethodInvocation expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(ArrayAccess expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(InfixExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(InstanceofExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(PostfixExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(PrefixExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(CastExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationExpression expression)
	{
		sourceLineCounter.add(expression);
		return false;
	}

	public LineSet getLinesSeen()
	{
		return sourceLineCounter;
	}

	@Override
	public String toString()
	{
		return sourceLineCounter.toString();
	}
}
