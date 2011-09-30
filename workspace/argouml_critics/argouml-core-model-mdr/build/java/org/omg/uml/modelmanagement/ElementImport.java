package org.omg.uml.modelmanagement;

/**
 * ElementImport object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ElementImport extends javax.jmi.reflect.RefObject {
    /**
     * Returns the value of attribute visibility.
     * @return Value of attribute visibility.
     */
    public org.omg.uml.foundation.datatypes.VisibilityKind getVisibility();
    /**
     * Sets the value of visibility attribute. See {@link #getVisibility} for 
     * description on the attribute.
     * @param newValue New value to be set.
     */
    public void setVisibility(org.omg.uml.foundation.datatypes.VisibilityKind newValue);
    /**
     * Returns the value of attribute alias.
     * @return Value of attribute alias.
     */
    public java.lang.String getAlias();
    /**
     * Sets the value of alias attribute. See {@link #getAlias} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setAlias(java.lang.String newValue);
    /**
     * Returns the value of attribute isSpecification.
     * @return Value of attribute isSpecification.
     */
    public boolean isSpecification();
    /**
     * Sets the value of isSpecification attribute. See {@link #isSpecification} 
     * for description on the attribute.
     * @param newValue New value to be set.
     */
    public void setSpecification(boolean newValue);
    /**
     * Returns the value of reference package.
     * @return Value of reference package.
     */
    public org.omg.uml.modelmanagement.UmlPackage getUmlPackage();
    /**
     * Sets the value of reference package. See {@link #getUmlPackage} for description 
     * on the reference.
     * @param newValue New value to be set.
     */
    public void setUmlPackage(org.omg.uml.modelmanagement.UmlPackage newValue);
    /**
     * Returns the value of reference importedElement.
     * @return Value of reference importedElement.
     */
    public org.omg.uml.foundation.core.ModelElement getImportedElement();
    /**
     * Sets the value of reference importedElement. See {@link #getImportedElement} 
     * for description on the reference.
     * @param newValue New value to be set.
     */
    public void setImportedElement(org.omg.uml.foundation.core.ModelElement newValue);
}
