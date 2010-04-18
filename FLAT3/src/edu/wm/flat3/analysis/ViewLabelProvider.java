/**
 * 
 */
package edu.wm.flat3.analysis;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
/*import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.core.JRipplesPlugin;
import org.severe.jripples.defaultmodules.JRipplesDefaultModulesConstants;
import org.severe.jripples.eig.JRipplesEIGNode;
import org.severe.jripples.modules.interfaces.JRipplesICModuleInterface;
import org.severe.jripples.modules.manager.ModuleProxy;
*/
public class ViewLabelProvider extends LabelProvider implements
		ITableLabelProvider,ILightweightLabelDecorator, ITableColorProvider  {

	static DecimalFormat probabilityFormater;

	/**
	 * @param table
	 */
	public ViewLabelProvider() {
		probabilityFormater = new DecimalFormat();
		probabilityFormater.setMaximumFractionDigits(3);
	}

	public Image getColumnImage(Object element, int column) {
		if ((column!=0)) return null;
		if (element instanceof FLATTTMember)
			return ((FLATTTMember) element).getImage();
		
		return null;
		//return NodeImageRegistry.getImage((JRipplesEIGNode) element);
	}
	
	public Image getImage(Object element) {
		
		return getColumnImage( element,0);
		
		
	}
	public String getColumnText(Object obj, int index) {

		switch (index) {
		case 1:
			if (obj instanceof FLATTTMember)
				return ((FLATTTMember) obj).getShortName();
			if (obj != null)
				return obj.toString();
			return FLATTTViewsConstants.STRING_BLANK;
		case 2:
			if (obj instanceof FLATTTMember)
				return ((FLATTTMember) obj).getClassName();
			if (obj != null)
				return obj.toString();
			return FLATTTViewsConstants.STRING_BLANK;
		case 4:
			if (obj instanceof FLATTTMember)
				if (((FLATTTMember) obj).getFullName() != null)
					return ((FLATTTMember) obj).getFullName();
			if (obj != null)
				return obj.toString();
			return FLATTTViewsConstants.STRING_BLANK;
		case 3:
			if (obj instanceof FLATTTMember)
				return probabilityFormater.format(new Double(((FLATTTMember) obj).getProbability()));
			if (obj != null)
				return obj.toString();
			return FLATTTViewsConstants.STRING_BLANK;
		case 5:
			if (obj instanceof FLATTTMember)
				return ((FLATTTMember) obj).getFeatureName();
			if (obj != null)
				return obj.toString();
			return FLATTTViewsConstants.STRING_BLANK;
		default:
			return FLATTTViewsConstants.STRING_BLANK;
		
		}

	}

	public void decorate(Object element, IDecoration decoration) {
	//	decoration.addOverlay(JRipplesPlugin.imageDescriptorFromPlugin (JRipplesDefaultModulesConstants.ID_JRIPPLES_DEFAULT_MODULES_PLUGIN, FLATTTViewsConstants.ICON_IA_PASSIVE));
	//	decoration.addOverlay(JRipplesPlugin.imageDescriptorFromPlugin (JRipplesDefaultModulesConstants.ID_JRIPPLES_DEFAULT_MODULES_PLUGIN, FLATTTViewsConstants.ICON_MARKS_FILTER_ACTIVE));
		
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		if (columnIndex!=1) return null;
		if (element==null) return null;
	//	if (ModuleProxy.getActiveCategoryModule(FLATTTViewsConstants.CATEGORY_MODULE_INCREMENTAL_CHANGE)!=null) {
	//			return ((JRipplesICModuleInterface) ModuleProxy.getActiveCategoryModule(FLATTTViewsConstants.CATEGORY_MODULE_INCREMENTAL_CHANGE)).getColorForMark(((JRipplesEIGNode)element).getMark());
	//	}
		
		return null;
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}



}