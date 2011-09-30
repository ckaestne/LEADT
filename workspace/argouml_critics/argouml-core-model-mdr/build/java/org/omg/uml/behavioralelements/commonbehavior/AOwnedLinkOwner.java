package org.omg.uml.behavioralelements.commonbehavior;

/**
 * A_ownedLink_owner association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AOwnedLinkOwner extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param ownedLink Value of the first association end.
     * @param owner Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.behavioralelements.commonbehavior.Link ownedLink, org.omg.uml.behavioralelements.commonbehavior.Instance owner);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param owner Required value of the second association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getOwnedLink(org.omg.uml.behavioralelements.commonbehavior.Instance owner);
    /**
     * Queries the instance object that is related to a particular instance object 
     * by a link in the current associations link set.
     * @param ownedLink Required value of the first association end.
     * @return Related object or <code>null</code> if none exists.
     */
    public org.omg.uml.behavioralelements.commonbehavior.Instance getOwner(org.omg.uml.behavioralelements.commonbehavior.Link ownedLink);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param ownedLink Value of the first association end.
     * @param owner Value of the second association end.
     */
    public boolean add(org.omg.uml.behavioralelements.commonbehavior.Link ownedLink, org.omg.uml.behavioralelements.commonbehavior.Instance owner);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param ownedLink Value of the first association end.
     * @param owner Value of the second association end.
     */
    public boolean remove(org.omg.uml.behavioralelements.commonbehavior.Link ownedLink, org.omg.uml.behavioralelements.commonbehavior.Instance owner);
}
