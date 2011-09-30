package org.omg.uml.foundation.core;

/**
 * BehavioralFeature object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface BehavioralFeature extends org.omg.uml.foundation.core.Feature {
    /**
     * Returns the value of attribute isQuery.
     * @return Value of attribute isQuery.
     */
    public boolean isQuery();
    /**
     * Sets the value of isQuery attribute. See {@link #isQuery} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setQuery(boolean newValue);
    /**
     * Returns the value of reference parameter.
     * @return Value of reference parameter. Element type: {@link org.omg.uml.foundation.core.Parameter}
     */
    public java.util.List<org.omg.uml.foundation.core.Parameter> getParameter();
}
