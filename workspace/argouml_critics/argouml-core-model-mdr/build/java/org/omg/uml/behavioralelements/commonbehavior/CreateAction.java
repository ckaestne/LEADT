package org.omg.uml.behavioralelements.commonbehavior;

/**
 * CreateAction object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface CreateAction extends org.omg.uml.behavioralelements.commonbehavior.Action {
    /**
     * Returns the value of reference instantiation.
     * @return Value of reference instantiation.
     */
    public org.omg.uml.foundation.core.Classifier getInstantiation();
    /**
     * Sets the value of reference instantiation. See {@link #getInstantiation} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setInstantiation(org.omg.uml.foundation.core.Classifier newValue);
}
