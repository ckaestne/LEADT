package org.omg.uml.modelmanagement;

/**
 * ElementImport class proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ElementImportClass extends javax.jmi.reflect.RefClass {
    /**
     * The default factory operation used to create an instance object.
     * @return The created instance object.
     */
    public ElementImport createElementImport();
    /**
     * Creates an instance object having attributes initialized by the passed 
     * values.
     * @param visibility 
     * @param alias 
     * @param isSpecification 
     * @return The created instance object.
     */
    public ElementImport createElementImport(org.omg.uml.foundation.datatypes.VisibilityKind visibility, java.lang.String alias, boolean isSpecification);
}
