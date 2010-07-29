package de.ovgu.cide.mining.recommendationmanager;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.ovgu.cide.mining.database.model.AICategories;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.nonfeaturemanager.model.NonFeatureTreeNode;
import de.ovgu.cide.mining.recommendationmanager.model.RecommendationTreeNode;



public class RecommendationLabelProvider extends LabelProvider  implements ITableLabelProvider {

	
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex > 0)
			return null;
		
		String imageKey = ISharedImages.IMG_OBJ_ADD;
		
		if (element instanceof RecommendationTreeNode) {
			
			
			switch (((RecommendationTreeNode)element).getKind()) {
			case CONTEXT:
				
				AElement supporter = ((RecommendationTreeNode)element).getElement();
				if (supporter.getCategory() == AICategories.FEATURE)
					imageKey = ISharedImages.IMG_OBJ_ELEMENT;
				break;
			
			}
		   
		}
		
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);

	}

	
	public String getColumnText(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		
		
		RecommendationTreeNode node  = (RecommendationTreeNode) element;
		switch (columnIndex) {
			case 0: 
				return node.getDisplayName();
			case 1: 
				return node.getTypePriorityString();
			case 2: 
				return node.getSupportValueAsString();
			case 3: 
				return node.getReasons();
			case 4: 
				return node.getSupportersCountString();
			case 5: 
				return node.getMaxSupportFeature();
			case 6: 
				return node.getRange();
			case 7:  
				return node.getViewCountAsString();
			
			
		} 
		
		
		return "";
	}
	
	

}
