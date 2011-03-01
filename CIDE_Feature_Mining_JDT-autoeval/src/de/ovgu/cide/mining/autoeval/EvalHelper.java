package de.ovgu.cide.mining.autoeval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Assert;

import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;

public class EvalHelper {
	static IProject getProject() throws CoreException {
		String projectName = "MobileMedia_Eval";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		if (!project.isOpen())
			project.open(new NullProgressMonitor());
		project.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
		return project;
	}

	static IFeature getFeatureByName(String name) throws CoreException,
			FeatureModelNotFoundException {
		IProject project = getProject();
		Set<IFeature> features = FeatureModelManager.getInstance()
				.getFeatureModel(project).getFeatures();
		return getFeature(name, features);
	}

	private static IFeature getFeature(String featureName,
			Set<IFeature> features) {
		IFeature color = null;
		for (IFeature f : features)
			if (f.getName().equals(featureName))
				color = f;
		Assert.assertNotNull(color);
		return color;
	}

	private static Connection connection = null;

	static Connection getDBConnection() throws SQLException,
			ClassNotFoundException {
		if (connection == null)
			connection = connectToDatabase();
		return connection;
	}

	static Connection connectToDatabase() throws SQLException,
			ClassNotFoundException {
		Class.forName("org.postgresql.Driver");

		String url = "jdbc:postgresql://localhost/autoeval";
		Properties props = new Properties();
		props.setProperty("user", "dude");
		props.setProperty("password", "supersecret");
		// props.setProperty("ssl", "false");
		return DriverManager.getConnection(url, props);

	}
}
