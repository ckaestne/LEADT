package org.omg.uml.foundation.core;

/**
 * A_implementation_implementationLocation association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AImplementationImplementationLocation extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param implementation Value of the first association end.
     * @param implementationLocation Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.foundation.core.Artifact implementation, org.omg.uml.foundation.core.Component implementationLocation);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param implementationLocation Required value of the second association 
     * end.
     * @return Collection of related objects.
     */
    public java.util.Collection getImplementation(org.omg.uml.foundation.core.Component implementationLocation);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param implementation Required value of the first association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getImplementationLocation(org.omg.uml.foundation.core.Artifact implementation);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param implementation Value of the first association end.
     * @param implementationLocation Value of the second association end.
     */
    public boolean add(org.omg.uml.foundation.core.Artifact implementation, org.omg.uml.foundation.core.Component implementationLocation);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param implementation Value of the first association end.
     * @param implementationLocation Value of the second association end.
     */
    public boolean remove(org.omg.uml.foundation.core.Artifact implementation, org.omg.uml.foundation.core.Component implementationLocation);
}
