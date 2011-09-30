package org.omg.uml.foundation.core;

/**
 * A_deploymentLocation_deployedComponent association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ADeploymentLocationDeployedComponent extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param deploymentLocation Value of the first association end.
     * @param deployedComponent Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.foundation.core.Node deploymentLocation, org.omg.uml.foundation.core.Component deployedComponent);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param deployedComponent Required value of the second association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getDeploymentLocation(org.omg.uml.foundation.core.Component deployedComponent);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param deploymentLocation Required value of the first association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getDeployedComponent(org.omg.uml.foundation.core.Node deploymentLocation);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param deploymentLocation Value of the first association end.
     * @param deployedComponent Value of the second association end.
     */
    public boolean add(org.omg.uml.foundation.core.Node deploymentLocation, org.omg.uml.foundation.core.Component deployedComponent);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param deploymentLocation Value of the first association end.
     * @param deployedComponent Value of the second association end.
     */
    public boolean remove(org.omg.uml.foundation.core.Node deploymentLocation, org.omg.uml.foundation.core.Component deployedComponent);
}
