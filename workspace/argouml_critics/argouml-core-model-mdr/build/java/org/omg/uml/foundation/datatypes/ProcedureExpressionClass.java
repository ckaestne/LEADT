package org.omg.uml.foundation.datatypes;

/**
 * ProcedureExpression class proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ProcedureExpressionClass extends javax.jmi.reflect.RefClass {
    /**
     * The default factory operation used to create an instance object.
     * @return The created instance object.
     */
    public ProcedureExpression createProcedureExpression();
    /**
     * Creates an instance object having attributes initialized by the passed 
     * values.
     * @param language 
     * @param body 
     * @return The created instance object.
     */
    public ProcedureExpression createProcedureExpression(java.lang.String language, java.lang.String body);
}
