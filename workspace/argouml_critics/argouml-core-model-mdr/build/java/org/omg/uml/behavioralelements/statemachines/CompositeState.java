package org.omg.uml.behavioralelements.statemachines;

/**
 * CompositeState object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface CompositeState extends org.omg.uml.behavioralelements.statemachines.State {
    /**
     * Returns the value of attribute isConcurrent.
     * @return Value of attribute isConcurrent.
     */
    public boolean isConcurrent();
    /**
     * Sets the value of isConcurrent attribute. See {@link #isConcurrent} for 
     * description on the attribute.
     * @param newValue New value to be set.
     */
    public void setConcurrent(boolean newValue);
    /**
     * Returns the value of reference subvertex.
     * @return Value of reference subvertex. Element type: {@link org.omg.uml.behavioralelements.statemachines.StateVertex}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.statemachines.StateVertex> getSubvertex();
}
