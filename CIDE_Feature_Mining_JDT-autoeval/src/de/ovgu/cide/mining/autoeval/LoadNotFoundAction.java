package de.ovgu.cide.mining.autoeval;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;

import cide.gast.ASTVisitor;
import cide.gast.IASTNode;
import cide.gparser.ParseException;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.features.source.ColoredSourceFileIteratorAction;
import de.ovgu.cide.features.source.ColoredSourceFileIteratorJob;
import de.ovgu.cide.features.source.SourceFileColorManager;

/**
 * loads not-found elements and incorrect recommendations from the database for
 * a given run
 * 
 * assigns those code fragments to features NotFound and IncorrectRecommend
 * respecively.
 * 
 * (assuming that these features exists)
 * 
 * @author kaestner
 * 
 */
public class LoadNotFoundAction extends ColoredSourceFileIteratorAction {

	@Override
	protected WorkspaceJob createJob(IProject[] p) {
		try {
			assert p.length == 1;
			Shell shell = new Shell();
			NumberInputDialog dialog = new NumberInputDialog(shell);
			int runNr = dialog.open();

			Connection db = EvalHelper.getDBConnection();
			final Set<String> notFound = loadNFFromDatabase(db, runNr);
			final Set<String> incorrRec = loadIRFromDatabase(db, runNr);

			return new ColoredSourceFileIteratorJob(p, "load", "load") {

				@Override
				protected void processSource(ColoredSourceFile source)
						throws CoreException {
					final IFeature featureNF = getFeature("NotFound", source
							.getFeatureModel().getFeatures());
					final IFeature featureIR = getFeature("IncorrectRecommend",
							source.getFeatureModel().getFeatures());

					try {
						final SourceFileColorManager colorManager = source
								.getColorManager();
						// clear all colors
						source.getAST().accept(new ASTVisitor() {
							@Override
							public boolean visit(IASTNode node) {
								Set<IFeature> colors = colorManager
										.getColors(node);
								if (colors.contains(featureNF))
									colorManager.removeColor(node, featureNF);
								if (notFound.contains(node.getId()))
									colorManager.addColor(node, featureNF);
								if (colors.contains(featureIR))
									colorManager.removeColor(node, featureIR);
								if (incorrRec.contains(node.getId()))
									colorManager.addColor(node, featureIR);
								return super.visit(node);
							}
						});

					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	private IFeature getFeature(String featureName, Set<IFeature> features) {
		IFeature color = null;
		for (IFeature f : features)
			if (f.getName().equals(featureName))
				color = f;
		Assert.assertNotNull(color);
		return color;
	}

	private Set<String> loadNFFromDatabase(Connection db, int runNr)
			throws SQLException {
		ResultSet failedIds = db.createStatement().executeQuery(
				"select astid from notfound where run=" + runNr);
		Set<String> result = new HashSet<String>();
		while (failedIds.next())
			result.add(failedIds.getString(1));
		return result;
	}

	private Set<String> loadIRFromDatabase(Connection db, int runNr)
			throws SQLException {
		ResultSet failedIds = db.createStatement().executeQuery(
				"select astid from datapointsu10 where run=" + runNr
						+ " and iscorrect=FALSE");
		Set<String> result = new HashSet<String>();
		while (failedIds.next())
			result.add(failedIds.getString(1));
		return result;
	}
}
