package org.omg.uml.foundation.core;

/**
 * A_constrainedElement_constraint association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface AConstrainedElementConstraint extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param constrainedElement Value of the first association end.
     * @param constraint Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.foundation.core.ModelElement constrainedElement, org.omg.uml.foundation.core.Constraint constraint);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param constraint Required value of the second association end.
     * @return List of related objects.
     */
    public java.util.List getConstrainedElement(org.omg.uml.foundation.core.Constraint constraint);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param constrainedElement Required value of the first association end.
     * @return Collection of related objects.
     */
    public java.util.Collection getConstraint(org.omg.uml.foundation.core.ModelElement constrainedElement);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param constrainedElement Value of the first association end.
     * @param constraint Value of the second association end.
     */
    public boolean add(org.omg.uml.foundation.core.ModelElement constrainedElement, org.omg.uml.foundation.core.Constraint constraint);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param constrainedElement Value of the first association end.
     * @param constraint Value of the second association end.
     */
    public boolean remove(org.omg.uml.foundation.core.ModelElement constrainedElement, org.omg.uml.foundation.core.Constraint constraint);
}
