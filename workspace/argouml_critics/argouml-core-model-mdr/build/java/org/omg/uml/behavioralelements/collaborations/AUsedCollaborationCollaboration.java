package org.omg.uml.behavioralelements.collaborations;

/**
 * A_usedCollaboration_collaboration association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AUsedCollaborationCollaboration extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param usedCollaboration Value of the first association end.
     * @param collaboration Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.behavioralelements.collaborations.Collaboration usedCollaboration, org.omg.uml.behavioralelements.collaborations.Collaboration collaboration);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param collaboration Required value of the second association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getUsedCollaboration(org.omg.uml.behavioralelements.collaborations.Collaboration collaboration);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param usedCollaboration Required value of the first association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getCollaboration(org.omg.uml.behavioralelements.collaborations.Collaboration usedCollaboration);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param usedCollaboration Value of the first association end.
     * @param collaboration Value of the second association end.
     */
    public boolean add(org.omg.uml.behavioralelements.collaborations.Collaboration usedCollaboration, org.omg.uml.behavioralelements.collaborations.Collaboration collaboration);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param usedCollaboration Value of the first association end.
     * @param collaboration Value of the second association end.
     */
    public boolean remove(org.omg.uml.behavioralelements.collaborations.Collaboration usedCollaboration, org.omg.uml.behavioralelements.collaborations.Collaboration collaboration);
}
