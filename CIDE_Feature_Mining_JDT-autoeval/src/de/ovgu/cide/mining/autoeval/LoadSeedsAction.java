package de.ovgu.cide.mining.autoeval;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;

import cide.gast.ASTVisitor;
import cide.gast.IASTNode;
import cide.gparser.ParseException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.features.source.ColoredSourceFileIteratorAction;
import de.ovgu.cide.features.source.ColoredSourceFileIteratorJob;
import de.ovgu.cide.features.source.SourceFileColorManager;

public class LoadSeedsAction extends ColoredSourceFileIteratorAction {

	@Override
	protected WorkspaceJob createJob(IProject[] p) {

		return new LoadSeedsJob(p);
	}

}

class LoadSeedsJob extends ColoredSourceFileIteratorJob {

	public LoadSeedsJob(IProject[] p) {
		super(p, "Loading seeds", "loadseed");
	}

	@Override
	protected void processSource(final ColoredSourceFile source)
			throws CoreException {
		try {
			final SourceFileColorManager colorManager = source
					.getColorManager();
			// clear all colors
			source.getAST().accept(new ASTVisitor() {
				@Override
				public boolean visit(IASTNode node) {
					colorManager.clearColor(node);
					return super.visit(node);
				}
			});
			// then load seeds
			for (final IFeature feature : source.getFeatureModel()
					.getFeatures()) {

				final Set<String> elements = AutoEval.readElements(source
						.getProject().getFile(
								"seed_" + feature.getName() + ".log"));
				if (!elements.isEmpty())
					source.getAST().accept(new ASTVisitor() {
						@Override
						public boolean visit(IASTNode node) {
							if (elements.contains(node.getId()))
								source.getColorManager()
										.addColor(node, feature);
							return super.visit(node);
						}
					});
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
