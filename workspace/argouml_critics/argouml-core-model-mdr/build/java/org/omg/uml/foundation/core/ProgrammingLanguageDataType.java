package org.omg.uml.foundation.core;

/**
 * ProgrammingLanguageDataType object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ProgrammingLanguageDataType extends org.omg.uml.foundation.core.DataType {
    /**
     * Returns the value of attribute expression.
     * @return Value of attribute expression.
     */
    public org.omg.uml.foundation.datatypes.TypeExpression getExpression();
    /**
     * Sets the value of expression attribute. See {@link #getExpression} for 
     * description on the attribute.
     * @param newValue New value to be set.
     */
    public void setExpression(org.omg.uml.foundation.datatypes.TypeExpression newValue);
}
