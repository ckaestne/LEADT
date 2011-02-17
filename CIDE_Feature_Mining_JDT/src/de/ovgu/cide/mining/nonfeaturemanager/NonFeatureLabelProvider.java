package de.ovgu.cide.mining.nonfeaturemanager;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode;

public class NonFeatureLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex > 0)
			return null;

		String imageKey = null;

		if (element instanceof NonFeatureTreeNode) {

			switch (((NonFeatureTreeNode) element).getKind()) {
			case FEATURE:
				imageKey = ISharedImages.IMG_OBJ_ELEMENT;
				break;

			case COMPILATION_UNIT:
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
				break;

			case ELEMENT:
				imageKey = ISharedImages.IMG_OBJ_ADD;
				break;
			}

		}

		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);

	}

	public String getColumnText(Object element, int columnIndex) {
		// TODO Auto-generated method stub

		if (element instanceof NonFeatureTreeNode) {
			NonFeatureTreeNode node = (NonFeatureTreeNode) element;
			switch (columnIndex) {
			case 0:
				return node.getDisplayName();
			case 1:
				return node.getRange();
			case 2:
				return node.getType();
			case 3:
				return node.getAddCountString();
			case 4:
				return node.getViewCountString();
			case 5:
				return node.getElementsCountString();

				// case 1: return String.valueOf(obj.getViewCount());
			}
		}

		return "";
	}

}
