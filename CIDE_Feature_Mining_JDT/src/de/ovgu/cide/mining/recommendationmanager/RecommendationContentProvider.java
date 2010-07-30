package de.ovgu.cide.mining.recommendationmanager;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.ovgu.cide.mining.recommendationmanager.RecommendationManagerView.Recommendation;

class RecommendationContentProvider implements IStructuredContentProvider {

	private Recommendation[] recommendations;

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		recommendations = (Recommendation[]) newInput;
	}

	public void dispose() {
	}

	// <-- STATISTICS
	// final Set<String> featureExpElements =
	// Statistics.loadFeatureElements(true);
	// final Set<String> featureOrgElements =
	// Statistics.loadFeatureElements(false);
	//
	//
	// private void printForStatistics() {
	// StringBuilder line = new StringBuilder();
	// StringBuilder row = new StringBuilder();
	//
	//
	// int items =
	// Math.min(recommendationManager.getTreeViewer().getTree().getItemCount(),
	// 50);
	//
	// for (int i = 0; i < items ; i++) {
	//
	// RecommendationTreeNode node = (RecommendationTreeNode)
	// recommendationManager.getTreeViewer().getTree().getItem(i).getData();
	// boolean isFeatureExpElement =
	// featureExpElements.contains(node.getElement().getId());
	// boolean isFeatureOrignalElement =
	// featureOrgElements.contains(node.getElement().getId());
	//
	//
	// if (i==0) {
	// recommendationManager.setInfoMessage(node.getDisplayName()+"   +++ "+isFeatureExpElement+
	// " +++"+"   +++ "+isFeatureOrignalElement+ " +++" , MESSAGE_TYPE.INFO);
	//
	// line.append(AC.getElementsOfColor(node.getColor()).size());
	// line.append("\t");
	//
	// line.append(AC.getElementsOfNonColor(node.getColor()).size());
	// line.append("\t");
	//
	// if (isFeatureOrignalElement) {
	// line.append("true");
	// line.append("\t");
	// line.append(node.getSupportValueAsString());
	// line.append("\t");
	// }
	// else {
	// line.append("\t\t");
	//
	// }
	//
	//
	// if (!isFeatureOrignalElement && isFeatureExpElement){
	// line.append("true");
	// line.append("\t");
	// line.append(node.getSupportValueAsString());
	// line.append("\t");
	// }
	// else {
	// line.append("\t\t");
	//
	// }
	//
	// if (!isFeatureExpElement){
	// line.append("false");
	// line.append("\t");
	// line.append(node.getSupportValueAsString());
	// line.append("\t");
	// }
	// else {
	// line.append("\t\t");
	// }
	//
	// row.append(node.getSupportValueAsString());
	// row.append("\t");
	//
	// row.append(isFeatureExpElement);
	// row.append(System.getProperty("line.separator"));
	//
	// }
	// else {
	//
	// line.append(isFeatureExpElement);
	// line.append("\t");
	//
	// line.append(node.getSupportValueAsString());
	// line.append("\t");
	//
	// }
	//
	//
	//
	// }
	//
	// //print line!
	// Statistics.writeRecommendations(line.toString(), row.toString());
	//
	// }
	// STATISTICS-->

	public Object[] getElements(Object parent) {
		return recommendations;
	}
	//
	// public Object getParent(Object child) {
	//
	// if (child instanceof RecommendationTreeNode) {
	// return ((RecommendationTreeNode) child).getParent();
	// }
	// return null;
	// }

	// public Object[] getChildren(Object parent) {
	//
	// if (parent instanceof RecommendationTreeNode) {
	// return ((RecommendationTreeNode) parent).getChildren();
	// }
	// return new Object[0];
	// }
	//
	// public boolean hasChildren(Object parent) {
	//
	// if (parent instanceof RecommendationTreeNode)
	// return ((RecommendationTreeNode) parent).hasChildren();
	// return false;
	// }

}