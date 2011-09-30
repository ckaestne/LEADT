package org.omg.uml;

/**
 * UML package interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface UmlPackage extends javax.jmi.reflect.RefPackage {
    public org.omg.uml.foundation.datatypes.DataTypesPackage getDataTypes();
    public org.omg.uml.foundation.core.CorePackage getCore();
    public org.omg.uml.behavioralelements.commonbehavior.CommonBehaviorPackage getCommonBehavior();
    public org.omg.uml.behavioralelements.usecases.UseCasesPackage getUseCases();
    public org.omg.uml.behavioralelements.statemachines.StateMachinesPackage getStateMachines();
    public org.omg.uml.behavioralelements.collaborations.CollaborationsPackage getCollaborations();
    public org.omg.uml.behavioralelements.activitygraphs.ActivityGraphsPackage getActivityGraphs();
    public org.omg.uml.modelmanagement.ModelManagementPackage getModelManagement();
}
