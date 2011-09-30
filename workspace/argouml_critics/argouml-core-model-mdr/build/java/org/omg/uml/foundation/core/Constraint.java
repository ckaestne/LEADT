package org.omg.uml.foundation.core;

/**
 * Constraint object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface Constraint extends org.omg.uml.foundation.core.ModelElement {
    /**
     * Returns the value of attribute body.
     * @return Value of attribute body.
     */
    public org.omg.uml.foundation.datatypes.BooleanExpression getBody();
    /**
     * Sets the value of body attribute. See {@link #getBody} for description 
     * on the attribute.
     * @param newValue New value to be set.
     */
    public void setBody(org.omg.uml.foundation.datatypes.BooleanExpression newValue);
    /**
     * Returns the value of reference constrainedElement.
     * @return Value of reference constrainedElement. Element type: {@link org.omg.uml.foundation.core.ModelElement}
     */
    public java.util.List<org.omg.uml.foundation.core.ModelElement> getConstrainedElement();
}
