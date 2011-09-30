package org.omg.uml.behavioralelements.commonbehavior;

/**
 * ComponentInstance object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ComponentInstance extends org.omg.uml.behavioralelements.commonbehavior.Instance {
    /**
     * Returns the value of reference nodeInstance.
     * @return Value of reference nodeInstance.
     */
    public org.omg.uml.behavioralelements.commonbehavior.NodeInstance getNodeInstance();
    /**
     * Sets the value of reference nodeInstance. See {@link #getNodeInstance} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setNodeInstance(org.omg.uml.behavioralelements.commonbehavior.NodeInstance newValue);
    /**
     * Returns the value of reference resident.
     * @return Value of reference resident. Element type: {@link org.omg.uml.behavioralelements.commonbehavior.Instance}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.commonbehavior.Instance> getResident();
}
