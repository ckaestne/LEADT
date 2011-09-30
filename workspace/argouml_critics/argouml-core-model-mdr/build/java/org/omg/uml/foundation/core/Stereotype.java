package org.omg.uml.foundation.core;

/**
 * Stereotype object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Stereotype extends org.omg.uml.foundation.core.GeneralizableElement {
    /**
     * Returns the value of attribute icon.
     * @return Value of attribute icon.
     */
    public java.lang.String getIcon();
    /**
     * Sets the value of icon attribute. See {@link #getIcon} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setIcon(java.lang.String newValue);
    /**
     * Returns the value of attribute baseClass.
     * @return Value of baseClass attribute. Element type: {@link java.lang.String}
     */
    public java.util.Collection<java.lang.String> getBaseClass();
    /**
     * Returns the value of reference definedTag.
     * @return Value of reference definedTag. Element type: {@link org.omg.uml.foundation.core.TagDefinition}
     */
    public java.util.Collection<org.omg.uml.foundation.core.TagDefinition> getDefinedTag();
    /**
     * Returns the value of reference stereotypeConstraint.
     * @return Value of reference stereotypeConstraint. Element type: {@link org.omg.uml.foundation.core.Constraint}
     */
    public java.util.Collection<org.omg.uml.foundation.core.Constraint> getStereotypeConstraint();
}
