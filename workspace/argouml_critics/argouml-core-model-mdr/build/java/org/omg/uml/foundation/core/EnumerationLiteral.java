package org.omg.uml.foundation.core;

/**
 * EnumerationLiteral object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface EnumerationLiteral extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of reference enumeration.
     * @return Value of reference enumeration.
     */
    public org.omg.uml.foundation.core.Enumeration getEnumeration();
    /**
     * Sets the value of reference enumeration. See {@link #getEnumeration} for 
     * description on the reference.
     * @param newValue New value to be set.
     */
    public void setEnumeration(org.omg.uml.foundation.core.Enumeration newValue);
}
