package de.ovgu.cide.editor.inlineprojection;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Position;

import de.ovgu.cide.features.IFeature;

public class ColoredInlineProjectionAnnotation extends
		InlineProjectionAnnotation {

	
	private IProject project;

	public ColoredInlineProjectionAnnotation(Set<IFeature> features, IProject project, Position pos){
		this.colors=features;
		this.project=project;
		this.position=pos;
	}
	
	private Set<IFeature> colors;

	private Position position;

	public void setColors(Set<IFeature> colors) {
		this.colors = colors;
	}

	public boolean adjustCollapsing(Set<IFeature> selectedColors) {
		boolean expanded = selectedColors.containsAll(colors);
		if (isCollapsed() && expanded) {
			this.markExpanded();
			return true;
		}
		if (!isCollapsed() && !expanded) {
			this.markCollapsed();
			return true;
		}
		return false;
	}

	public void setPosition(Position pos) {
		this.position = pos;
	}

	public Position getPosition() {
		return position;
	}

	public Set<IFeature> getColors() {
		return colors;
	}
	
	public IProject getProject(){
		return project;
	}

}
