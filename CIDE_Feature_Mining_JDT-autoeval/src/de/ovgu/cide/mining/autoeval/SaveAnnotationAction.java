package de.ovgu.cide.mining.autoeval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;

import cide.gast.ASTVisitor;
import cide.gast.IASTNode;
import cide.gparser.ParseException;
import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.features.source.ColoredSourceFileIteratorAction;
import de.ovgu.cide.features.source.ColoredSourceFileIteratorJob;

public class SaveAnnotationAction extends ColoredSourceFileIteratorAction {

	@Override
	protected WorkspaceJob createJob(IProject[] p) {
		if (p.length == 1)
			try {
				return new SaveAnnotationJob(p[0]);
			} catch (FeatureModelNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		else
			throw new RuntimeException("select one project only");
	}
}

class SaveAnnotationJob extends ColoredSourceFileIteratorJob {

	final Map<IFeature, Set<String>> ids = new HashMap<IFeature, Set<String>>();
	private IFeatureModel fm;

	public SaveAnnotationJob(IProject p) throws FeatureModelNotFoundException {
		super(p, "Loading seeds", "loadseed");
		fm = FeatureModelManager.getInstance().getFeatureModel(p);
		for (IFeature feature : fm.getFeatures())
			ids.put(feature, new HashSet<String>());
	}

	@Override
	protected void processSource(final ColoredSourceFile source)
			throws CoreException {

		try {
			source.getAST().accept(new ASTVisitor() {
				@Override
				public boolean visit(IASTNode node) {
					for (IFeature feature : source.getColorManager().getColors(
							node))
						ids.get(feature).add(node.getId());
					return super.visit(node);
				}
			});
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void finish() {
		// get instance and init the database
		for (IFeature feature : fm.getFeatures()) {
			if (!ids.get(feature).isEmpty())
				AutoEval.writeElements("target_" + feature.getName() + ".log",
						ids.get(feature));
		}
		super.finish();
	}
}
