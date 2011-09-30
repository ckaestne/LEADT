package org.omg.uml.foundation.datatypes;

/**
 * MultiplicityRange object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface MultiplicityRange extends javax.jmi.reflect.RefObject {
    /**
     * Returns the value of attribute lower.
     * @return Value of attribute lower.
     */
    public int getLower();
    /**
     * Sets the value of lower attribute. See {@link #getLower} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setLower(int newValue);
    /**
     * Returns the value of attribute upper.
     * @return Value of attribute upper.
     */
    public int getUpper();
    /**
     * Sets the value of upper attribute. See {@link #getUpper} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setUpper(int newValue);
    /**
     * Returns the value of reference multiplicity.
     * @return Value of reference multiplicity.
     */
    public org.omg.uml.foundation.datatypes.Multiplicity getMultiplicity();
    /**
     * Sets the value of reference multiplicity. See {@link #getMultiplicity} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setMultiplicity(org.omg.uml.foundation.datatypes.Multiplicity newValue);
}
