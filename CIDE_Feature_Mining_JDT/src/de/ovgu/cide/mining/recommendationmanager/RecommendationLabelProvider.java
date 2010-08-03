package de.ovgu.cide.mining.recommendationmanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.recommendationengine.ARecommendationContextCollection;
import de.ovgu.cide.mining.recommendationmanager.RecommendationManagerView.Recommendation;

public class RecommendationLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	private RecommendationManagerView manager;

	RecommendationLabelProvider(RecommendationManagerView manager) {
		this.manager = manager;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex > 0)
			return null;

		String imageKey = ISharedImages.IMG_OBJ_ADD;

		// if (element instanceof Recommendation) {
		//
		//
		// switch (((RecommendationTreeNode)element).getKind()) {
		// case CONTEXT:
		//
		// AElement supporter = ((RecommendationTreeNode)element).getElement();
		// if (supporter.getCategory() == AICategories.FEATURE)
		// imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		// break;
		//
		// }
		//
		// }

		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);

	}

	public String getColumnText(Object element, int columnIndex) {
		// TODO Auto-generated method stub

		Recommendation node = (Recommendation) element;
		switch (columnIndex) {
		case 0:
			return node.element.getDisplayName();
		case 1:
			return "" + getTypePriority(node);
		case 2:
			return getSupportValueAsString(node);
		case 3:
			return node.context.getSupportReasons();
		case 4:
			return "" + node.context.getContexts().size();
		case 5:
			return getMaxSupportFeature(node);
		case 6:
			return node.element.getStartPosition()
					+ "-"
					+ (node.element.getStartPosition() + node.element
							.getLength());
		case 7:
			return "" + node.element.getLength();
		case 8:
			return String.valueOf(getViewCount(node));

		}

		return "";
	}

	static int getViewCount(Recommendation node) {
		return ApplicationController.getInstance().getViewCountForElement(
				node.element);
	}

	static int getTypePriority(Recommendation node) {
		switch (node.element.getCategory()) {
		case COMPILATION_UNIT:
			return 0;
		case TYPE:
			return 1;
		case METHOD:
			return 2;
		case FIELD:
			return 3;
		case LOCAL_VARIABLE:
			return 4;
		case IMPORT:
			return 5;
		case METHOD_ACCESS:
			return 6;
		}
		return 7;
	}

	private double roundSupportValue(double supportValue) {

		return (double) ((int) (supportValue * 100)) / (double) 100;
	}

	String getSupportValueAsString(Recommendation node) {

		return String
				.valueOf(roundSupportValue(node.context.getSupportValue()));
	}

	String getMaxSupportFeature(Recommendation node) {

		double supportValue = node.context.getSupportValue();
		Set<String> betterRecommendations = new TreeSet<String>();

		Map<IFeature, ARecommendationContextCollection> recommendationMap = manager.AC
				.getAllRecommendations(node.element);
		for (Entry<IFeature, ARecommendationContextCollection> entry : recommendationMap
				.entrySet()) {
			IFeature color = entry.getKey();
			if (color.equals(manager.currentColor))
				continue;

			ARecommendationContextCollection collection = entry.getValue();
			double tmpSupportValue = collection.getSupportValue();

			if (tmpSupportValue > supportValue) {
				betterRecommendations.add(color.getName() + " ["
						+ roundSupportValue(tmpSupportValue) + "]");
			}
		}

		String result = "";
		for (String tmp : betterRecommendations) {
			result += tmp + "; ";
		}

		if (result.length() > 0) {
			result = result.substring(0, result.length() - 2);
		}

		return result;

	}
}
