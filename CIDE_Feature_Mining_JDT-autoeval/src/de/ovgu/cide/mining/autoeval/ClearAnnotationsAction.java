package de.ovgu.cide.mining.autoeval;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;

import de.ovgu.cide.features.source.ColoredSourceFileIteratorAction;

public class ClearAnnotationsAction extends ColoredSourceFileIteratorAction {

	@Override
	protected WorkspaceJob createJob(IProject[] p) {
		assert p.length == 1;
		return new LoadSeedsJob(p[0], new HashSet<SeedInfo>());
	}
}

