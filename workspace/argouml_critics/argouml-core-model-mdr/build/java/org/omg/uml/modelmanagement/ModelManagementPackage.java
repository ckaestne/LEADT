package org.omg.uml.modelmanagement;

/**
 * Model_Management package interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ModelManagementPackage extends javax.jmi.reflect.RefPackage {
    public org.omg.uml.foundation.datatypes.DataTypesPackage getDataTypes();
    public org.omg.uml.foundation.core.CorePackage getCore();
    /**
     * Returns UmlPackage class proxy object.
     * @return UmlPackage class proxy object.
     */
    public org.omg.uml.modelmanagement.UmlPackageClass getUmlPackage();
    /**
     * Returns Model class proxy object.
     * @return Model class proxy object.
     */
    public org.omg.uml.modelmanagement.ModelClass getModel();
    /**
     * Returns Subsystem class proxy object.
     * @return Subsystem class proxy object.
     */
    public org.omg.uml.modelmanagement.SubsystemClass getSubsystem();
    /**
     * Returns ElementImport class proxy object.
     * @return ElementImport class proxy object.
     */
    public org.omg.uml.modelmanagement.ElementImportClass getElementImport();
    /**
     * Returns AImportedElementElementImport association proxy object.
     * @return AImportedElementElementImport association proxy object.
     */
    public org.omg.uml.modelmanagement.AImportedElementElementImport getAImportedElementElementImport();
    /**
     * Returns APackageElementImport association proxy object.
     * @return APackageElementImport association proxy object.
     */
    public org.omg.uml.modelmanagement.APackageElementImport getAPackageElementImport();
}
