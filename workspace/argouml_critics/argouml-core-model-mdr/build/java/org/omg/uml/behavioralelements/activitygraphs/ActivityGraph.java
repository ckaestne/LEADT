package org.omg.uml.behavioralelements.activitygraphs;

/**
 * ActivityGraph object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ActivityGraph extends org.omg.uml.behavioralelements.statemachines.StateMachine {
    /**
     * Returns the value of reference partition.
     * @return Value of reference partition. Element type: {@link org.omg.uml.behavioralelements.activitygraphs.Partition}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.activitygraphs.Partition> getPartition();
}
