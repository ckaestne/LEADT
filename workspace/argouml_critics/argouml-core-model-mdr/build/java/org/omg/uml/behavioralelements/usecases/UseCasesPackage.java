package org.omg.uml.behavioralelements.usecases;

/**
 * Use_Cases package interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface UseCasesPackage extends javax.jmi.reflect.RefPackage {
    public org.omg.uml.foundation.datatypes.DataTypesPackage getDataTypes();
    public org.omg.uml.foundation.core.CorePackage getCore();
    public org.omg.uml.behavioralelements.commonbehavior.CommonBehaviorPackage getCommonBehavior();
    /**
     * Returns UseCase class proxy object.
     * @return UseCase class proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.UseCaseClass getUseCase();
    /**
     * Returns Actor class proxy object.
     * @return Actor class proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.ActorClass getActor();
    /**
     * Returns UseCaseInstance class proxy object.
     * @return UseCaseInstance class proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.UseCaseInstanceClass getUseCaseInstance();
    /**
     * Returns Extend class proxy object.
     * @return Extend class proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.ExtendClass getExtend();
    /**
     * Returns Include class proxy object.
     * @return Include class proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.IncludeClass getInclude();
    /**
     * Returns ExtensionPoint class proxy object.
     * @return ExtensionPoint class proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.ExtensionPointClass getExtensionPoint();
    /**
     * Returns ABaseExtender association proxy object.
     * @return ABaseExtender association proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.ABaseExtender getABaseExtender();
    /**
     * Returns AExtensionExtend association proxy object.
     * @return AExtensionExtend association proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.AExtensionExtend getAExtensionExtend();
    /**
     * Returns AIncluderAddition association proxy object.
     * @return AIncluderAddition association proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.AIncluderAddition getAIncluderAddition();
    /**
     * Returns AIncludeBase association proxy object.
     * @return AIncludeBase association proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.AIncludeBase getAIncludeBase();
    /**
     * Returns AExtensionPointUseCase association proxy object.
     * @return AExtensionPointUseCase association proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.AExtensionPointUseCase getAExtensionPointUseCase();
    /**
     * Returns AExtensionPointExtend association proxy object.
     * @return AExtensionPointExtend association proxy object.
     */
    public org.omg.uml.behavioralelements.usecases.AExtensionPointExtend getAExtensionPointExtend();
}
