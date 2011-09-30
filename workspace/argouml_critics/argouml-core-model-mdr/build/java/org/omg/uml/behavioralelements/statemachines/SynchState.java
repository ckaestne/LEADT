package org.omg.uml.behavioralelements.statemachines;

/**
 * SynchState object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface SynchState extends org.omg.uml.behavioralelements.statemachines.StateVertex {
    /**
     * Returns the value of attribute bound.
     * @return Value of attribute bound.
     */
    public int getBound();
    /**
     * Sets the value of bound attribute. See {@link #getBound} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setBound(int newValue);
}
