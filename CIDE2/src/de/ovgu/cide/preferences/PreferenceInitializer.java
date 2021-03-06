package de.ovgu.cide.preferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.ovgu.cide.CIDECorePlugin;
import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.FeatureModelProviderProxy;
import de.ovgu.cide.languages.LanguageExtensionManager;
import de.ovgu.cide.languages.LanguageExtensionProxy;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CIDECorePlugin.getDefault()
				.getPreferenceStore();
		Set<String> supportedFileExtensions = new HashSet<String>();
		for (LanguageExtensionProxy language : LanguageExtensionManager
				.getInstance().getAllLanguageExtensions()) {

			boolean languageEnabled = true;
			for (String fileExtension : language.getFileExtensions()) {
				if (supportedFileExtensions.contains(fileExtension)) {
					languageEnabled = false;
				} else
					supportedFileExtensions.add(fileExtension);
			}
			store.setDefault(PreferenceConstants.P_LANGPREFIX
					+ language.getId(), languageEnabled);
		}

		String defaultModel = "de.ovgu.cide.fm.list";
		List<FeatureModelProviderProxy> providers = FeatureModelManager
				.getInstance().getFeatureModelProviders();
		for (FeatureModelProviderProxy provider : providers)
			if (provider.getId().equals("de.ovgu.cide.fm.guidsl"))
				defaultModel = "de.ovgu.cide.fm.guidsl";
		store.setDefault(PreferenceConstants.P_FEATUREMODELPROVIDER,
				defaultModel);
	}

}
