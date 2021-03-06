package de.ovgu.cide.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;

import de.ovgu.cide.features.IFeature;

public class ToggleAllFeatureSubmenu extends MenuManager implements
		IContributionItem {

	public ToggleAllFeatureSubmenu(SelectionActionsContext context,
			Collection<IFeature> features) {

		super("All features");
		ArrayList<IFeature> list = new ArrayList<IFeature>(features);
		Collections.sort(list);
		for (IFeature feature : list) {
			this.add(new ToggleTextColorAction(context, feature));
		}

	}

}
