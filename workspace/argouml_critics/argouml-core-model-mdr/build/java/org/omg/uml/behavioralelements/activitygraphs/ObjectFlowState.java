package org.omg.uml.behavioralelements.activitygraphs;

/**
 * ObjectFlowState object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ObjectFlowState extends org.omg.uml.behavioralelements.statemachines.SimpleState {
    /**
     * Returns the value of attribute isSynch.
     * @return Value of attribute isSynch.
     */
    public boolean isSynch();
    /**
     * Sets the value of isSynch attribute. See {@link #isSynch} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setSynch(boolean newValue);
    /**
     * Returns the value of reference parameter.
     * @return Value of reference parameter. Element type: {@link org.omg.uml.foundation.core.Parameter}
     */
    public java.util.Collection<org.omg.uml.foundation.core.Parameter> getParameter();
    /**
     * Returns the value of reference type.
     * @return Value of reference type.
     */
    public org.omg.uml.foundation.core.Classifier getType();
    /**
     * Sets the value of reference type. See {@link #getType} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setType(org.omg.uml.foundation.core.Classifier newValue);
}
