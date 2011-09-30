package org.omg.uml.behavioralelements.usecases;

/**
 * UseCase object instance interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface UseCase extends org.omg.uml.foundation.core.Classifier {
    /**
     * Returns the value of reference extend.
     * @return Value of reference extend. Element type: {@link org.omg.uml.behavioralelements.usecases.Extend}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.usecases.Extend> getExtend();
    /**
     * Returns the value of reference include.
     * @return Value of reference include. Element type: {@link org.omg.uml.behavioralelements.usecases.Include}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.usecases.Include> getInclude();
    /**
     * Returns the value of reference extensionPoint.
     * @return Value of reference extensionPoint. Element type: {@link org.omg.uml.behavioralelements.usecases.ExtensionPoint}
     */
    public java.util.Collection<org.omg.uml.behavioralelements.usecases.ExtensionPoint> getExtensionPoint();
}
