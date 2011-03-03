package de.ovgu.cide.mining.autoeval;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;

import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;

public class Projects {

}

abstract class EvalProject {
	abstract IProject getProject() throws CoreException;

	abstract String[] getFeatures();

	String[] getAllFeatures() {
		return getFeatures();
	}

	abstract Map<String, String[]> getSeeds();

	IFeature getFeatureByName(String name) throws CoreException,
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
}

class MobileMediaProject extends EvalProject {

	public String[] getFeaturesAll() {
		return new String[] { "Copy_Media", "Count_and_Sort", "Favourites",
				"SMS_Transfer", "Play_Music", "View_Photo", "SMS_or_Copy" };
	}

	Map<String, String[]> getSeeds() {

		HashMap<String, String[]> seeds = new HashMap<String, String[]>();
		seeds.put("Copy_Media", new String[] { "seed_Copy_Media_1_1.log",
				"seed_Copy_Media_2_0961.log", "seed_Copy_Media_3_0694.log",
				"seed_Copy_Media_4_0579.log", "seed_Copy_Media_5_0507.log",
				"seed_Copy_Media_domain.log" });
		seeds.put("Count_and_Sort", new String[] {
				"seed_Count_and_Sort_1_1.log",
				"seed_Count_and_Sort_2_0707.log",
				"seed_Count_and_Sort_3_066.log",
				"seed_Count_and_Sort_4_0577.log",
				"seed_Count_and_Sort_5_0412.log",
				"seed_Count_and_Sort_domain.log" });
		seeds.put("Favourites", new String[] { "seed_Favourites_1_1.log",
				"seed_Favourites_2_0875.log", "seed_Favourites_3_075.log",
				"seed_Favourites_4_0438.log", "seed_Favourites_5_025.log",
				"seed_Favourites_domain.log" });
		seeds.put("Play_Music", new String[] { "seed_Play_Music_1_1.log",
				"seed_Play_Music_2_08.log", "seed_Play_Music_3_08.log",
				"seed_Play_Music_4_0707.log", "seed_Play_Music_5_0707.log",
				"seed_Play_Music_domain.log" });
		seeds
				.put("SMS_Transfer", new String[] {
						"seed_SMS_Transfer_2_0373.log",
						"seed_SMS_Transfer_3_0323.log",
						"seed_SMS_Transfer_4_0311.log",
						"seed_SMS_Transfer_5_0308.log",
						"seed_SMS_Transfer_6_308.log",
						"seed_SMS_Transfer_domain.log" });
		return seeds;
	}

	@Override
	String[] getFeatures() {
		return new String[] { "Copy_Media", "Count_and_Sort", "Favourites",
				"SMS_Transfer", "Play_Music" };
	}

	@Override
	IProject getProject() throws CoreException {
		return EvalHelper.getProject("MobileMedia_Eval");
	}

}

class PrevalerProject extends EvalProject {

	String[] getFeatures() {
		return new String[] { "Snapshot", "Censor", "GZip", "Monitor",
				"Replication" };
	}

	Map<String, String[]> seedsPrevayler = new HashMap<String, String[]>();
	{
		seedsPrevayler.put("Censor", new String[] { "seed_Censor_1_1.log",
				"seed_Censor_package.log" });
		seedsPrevayler.put("GZip", new String[] { "seed_GZip_1_x.log",
				"seed_GZip_package.log" });
		seedsPrevayler.put("Monitor", new String[] { "seed_Monitor_1_1.log",
				"seed_Monitor_package.log" });
		seedsPrevayler.put("Replication", new String[] {
				"seed_Replication_1_1.log", "seed_Replication_package.log" });
		seedsPrevayler.put("Snapshot", new String[] { "seed_Snapshot_1_1.log",
				"seed_Censor_package.log" });
	}

	@Override
	public Map<String, String[]> getSeeds() {
		return seedsPrevayler;
	}

	@Override
	IProject getProject() throws CoreException {
		return EvalHelper.getProject("Prevayler_Eval");
	}
}

class LampiroProject extends EvalProject {

	String[] getFeatures() {
		return new String[] { "Compression", "TLS" };
	}

	Map<String, String[]> seeds = new HashMap<String, String[]>();
	{
		seeds.put("Compression",
				new String[] { "seed_Compression.log" });
		seeds.put("TLS", new String[] { "seed_TLS.log" });
	}

	@Override
	public Map<String, String[]> getSeeds() {
		return seeds;
	}

	@Override
	IProject getProject() throws CoreException {
		return EvalHelper.getProject("Lampiro_Eval");
	}
}
