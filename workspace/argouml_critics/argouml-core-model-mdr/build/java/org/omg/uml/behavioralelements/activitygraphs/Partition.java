package org.omg.uml.behavioralelements.activitygraphs;

/**
 * Partition object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Partition extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of reference contents.
     * @return Value of reference contents. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.Collection<org.omg.uml.foundation.core.ModelElement> getContents();
    /**
     * Returns the value of reference activityGraph.
     * @return Value of reference activityGraph.
     */
    public org.omg.uml.behavioralelements.activitygraphs.ActivityGraph getActivityGraph();
    /**
     * Sets the value of reference activityGraph. See {@link #getActivityGraph} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setActivityGraph(org.omg.uml.behavioralelements.activitygraphs.ActivityGraph newValue);
}
