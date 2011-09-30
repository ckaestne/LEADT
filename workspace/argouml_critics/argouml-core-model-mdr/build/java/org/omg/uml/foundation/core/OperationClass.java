package org.omg.uml.foundation.core;

/**
 * Operation class proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface OperationClass extends javax.jmi.reflect.RefClass {
    /**
     * The default factory operation used to create an instance object.
     * @return The created instance object.
     */
    public Operation createOperation();
    /**
     * Creates an instance object having attributes initialized by the passed 
     * values.
     * @param name 
     * @param visibility 
     * @param isSpecification 
     * @param ownerScope 
     * @param isQuery 
     * @param concurrency 
     * @param isRoot 
     * @param isLeaf 
     * @param isAbstract 
     * @param specification 
     * @return The created instance object.
     */
    public Operation createOperation(java.lang.String name, org.omg.uml.foundation.datatypes.VisibilityKind visibility, boolean isSpecification, org.omg.uml.foundation.datatypes.ScopeKind ownerScope, boolean isQuery, org.omg.uml.foundation.datatypes.CallConcurrencyKind concurrency, boolean isRoot, boolean isLeaf, boolean isAbstract, java.lang.String specification);
}
