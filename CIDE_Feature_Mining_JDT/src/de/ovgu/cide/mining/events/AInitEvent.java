package de.ovgu.cide.mining.events;

import java.util.EventObject;

import org.eclipse.core.resources.IProject;

public class AInitEvent extends EventObject {
	final IProject project;

	public IProject getProject() {
		return project;
	}

	public AInitEvent(Object source, IProject project) {
		super(source);
		this.project = project;
	}

}
