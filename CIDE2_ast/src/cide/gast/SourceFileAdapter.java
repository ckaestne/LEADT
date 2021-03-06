package cide.gast;

import java.util.List;

import cide.gast.IASTNode;
import cide.gast.IASTVisitor;
import cide.gast.ISourceFile;
import cide.gast.Property;

/**
 * adapter that adapts a IASTNode to an ISourceFile; just for typing reasons.
 * 
 * @author ckaestne
 * 
 */
public class SourceFileAdapter implements ISourceFile {

	public IASTNode ast;

	public SourceFileAdapter(IASTNode ast) {
		this.ast = ast;
	}

	public void accept(IASTVisitor visitor) {
		ast.accept(visitor);
	}

	public IASTNode deepCopy() {
		return ast.deepCopy();
	}

	public String getId() {
		return ast.getId();
	}

	public int getLength() {
		return ast.getLength();
	}

	public IASTNode getParent() {
		return ast.getParent();
	}

	public Property getProperty(String name) {
		return ast.getProperty(name);
	}

	public ISourceFile getRoot() {
		return ast.getRoot();
	}

	public int getStartPosition() {
		return ast.getStartPosition();
	}

	public String getDisplayName() {

		return ast.getDisplayName();
	}

	public Property getLocationInParent() {
		return ast.getLocationInParent();
	}

	public List<Property> getProperties() {
		return ast.getProperties();
	}

	public boolean isOptional() {
		return ast.isOptional();
	}

	public void notifyPropertyChanged(Property property) {
		ast.notifyPropertyChanged(property);
	}

	public String render() {
		return ast.render();
	}

	public void setParent(IASTNode parentNode, Property parentProperty) {
		ast.setParent(parentNode, parentProperty);
	}

	public void setParentProperty(Property parentProperty) {
		ast.setParentProperty(parentProperty);
	}

	public void remove() {
		ast.remove();
	}

	public List<IASTNode> getChildren() {
		return ast.getChildren();
	}

	public void replaceSubtreeWith(IASTNode newNode) {
		ast.replaceSubtreeWith(newNode);
	}

	public void setId(String id) {
		ast.setId(id);
	}

	public IASTNode getWrappee() {
		return null;
	}

	public boolean isWrapper() {
		return false;
	}

	public int getStartLine() {
		return ast.getStartLine();
	}

	public int getEndLine() {
		return ast.getEndLine();
	}
}
