package de.ovgu.cide.mining.relationmanager;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode;
import de.ovgu.cide.mining.relationmanager.model.RelationTreeNode.NODE_KIND;

public class RelationLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex > 0)
			return null;

		String imageKey = ISharedImages.IMG_OBJ_FOLDER;

		if (element instanceof RelationTreeNode) {
			if (((RelationTreeNode) element).getKind() == NODE_KIND.ELEMENT)
				imageKey = ISharedImages.IMG_OBJ_ADD;
		}

		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);

	}

	public String getColumnText(Object element, int columnIndex) {

		if (element instanceof RelationTreeNode) {
			RelationTreeNode node = (RelationTreeNode) element;
			switch (columnIndex) {
			case 0:
				return node.getDisplayName();
			case 1:
				return node.getRange();
			case 2:
				return node.getViewCountString();
			}
		}

		return "";
	}

}
