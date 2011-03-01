package de.ovgu.cide.mining.autoeval;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import cide.gast.ASTVisitor;
import cide.gast.IASTNode;
import cide.gparser.ParseException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.features.source.ColoredSourceFileIteratorJob;
import de.ovgu.cide.features.source.SourceFileColorManager;

class LoadSeedsJob extends ColoredSourceFileIteratorJob {

	private Map<IFeature, Set<String>> seeds;

	public LoadSeedsJob(IProject p, Set<SeedInfo> seedInfos) {
		super(new IProject[] { p }, "Loading seeds", "loadseed");
		this.seeds = new HashMap<IFeature, Set<String>>();

		for (SeedInfo seedInfo : seedInfos) {
			Set<String> elements = AutoEval.readElements(p
					.getFile(seedInfo.filename));
			if (!elements.isEmpty())
				if (seeds.get(seedInfo.feature) != null)
					seeds.get(seedInfo.feature).addAll(elements);
				else
					seeds.put(seedInfo.feature, elements);
		}

	}

	@Override
	protected void processSource(final ColoredSourceFile source)
			throws CoreException {
		try {
			final SourceFileColorManager colorManager = source
					.getColorManager();
			colorManager.beginBatch();
			try {
				// clear all colors
				source.getAST().accept(new ASTVisitor() {
					@Override
					public boolean visit(IASTNode node) {
						colorManager.clearColor(node);
						return super.visit(node);
					}
				});
				// then load seeds
				for (final Entry<IFeature, Set<String>> entry : seeds
						.entrySet()) {

					source.getAST().accept(new ASTVisitor() {
						@Override
						public boolean visit(IASTNode node) {
							if (entry.getValue().contains(node.getId()))
								source.getColorManager().addColor(node,
										entry.getKey());
							return super.visit(node);
						}
					});
				}

			} finally {
				colorManager.endBatch();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}