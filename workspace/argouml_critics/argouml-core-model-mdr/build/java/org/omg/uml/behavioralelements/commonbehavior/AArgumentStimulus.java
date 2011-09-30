package org.omg.uml.behavioralelements.commonbehavior;

/**
 * A_argument_stimulus association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AArgumentStimulus extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param argument Value of the first association end.
     * @param stimulus Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.behavioralelements.commonbehavior.Instance argument, org.omg.uml.behavioralelements.commonbehavior.Stimulus stimulus);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param stimulus Required value of the second association end.
     * @return List of related objects.
     */
    public java.util.List getArgument(org.omg.uml.behavioralelements.commonbehavior.Stimulus stimulus);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param argument Required value of the first association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getStimulus(org.omg.uml.behavioralelements.commonbehavior.Instance argument);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param argument Value of the first association end.
     * @param stimulus Value of the second association end.
     */
    public boolean add(org.omg.uml.behavioralelements.commonbehavior.Instance argument, org.omg.uml.behavioralelements.commonbehavior.Stimulus stimulus);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param argument Value of the first association end.
     * @param stimulus Value of the second association end.
     */
    public boolean remove(org.omg.uml.behavioralelements.commonbehavior.Instance argument, org.omg.uml.behavioralelements.commonbehavior.Stimulus stimulus);
}
