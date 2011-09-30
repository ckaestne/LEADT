package org.omg.uml.foundation.core;

/**
 * A_container_residentElement association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AContainerResidentElement extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param container Value of the first association end.
     * @param residentElement Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.foundation.core.Component container, org.omg.uml.foundation.core.ElementResidence residentElement);
    /**
     * Queries the instance object that is related to a particular instance object 
     * by a link in the current associations link set.
     * @param residentElement Required value of the second association end.
     * @return Related object or <code>null</code> if none exists.
     */
    public org.omg.uml.foundation.core.Component getContainer(org.omg.uml.foundation.core.ElementResidence residentElement);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param container Required value of the first association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getResidentElement(org.omg.uml.foundation.core.Component container);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param container Value of the first association end.
     * @param residentElement Value of the second association end.
     */
    public boolean add(org.omg.uml.foundation.core.Component container, org.omg.uml.foundation.core.ElementResidence residentElement);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param container Value of the first association end.
     * @param residentElement Value of the second association end.
     */
    public boolean remove(org.omg.uml.foundation.core.Component container, org.omg.uml.foundation.core.ElementResidence residentElement);
}
