package org.omg.uml.foundation.core;

/**
 * A_template_templateParameter association proxy interface.
 *  
 * <p><em><strong>Note:</strong> This type should not be subclassed or implemented 
 * by clients. It is generated from a MOF metamodel and automatically implemented 
 * by MDR (see <a href="http://mdr.netbeans.org/">mdr.netbeans.org</a>).</em></p>
 */
public interface ATemplateTemplateParameter extends javax.jmi.reflect.RefAssociation {
    /**
     * Queries whether a link currently exists between a given pair of instance 
     * objects in the associations link set.
     * @param template Value of the first association end.
     * @param templateParameter Value of the second association end.
     * @return Returns true if the queried link exists.
     */
    public boolean exists(org.omg.uml.foundation.core.ModelElement template, org.omg.uml.foundation.core.TemplateParameter templateParameter);
    /**
     * Queries the instance object that is related to a particular instance object 
     * by a link in the current associations link set.
     * @param templateParameter Required value of the second association end.
     * @return Related object or <code>null</code> if none exists.
     */
    public org.omg.uml.foundation.core.ModelElement getTemplate(org.omg.uml.foundation.core.TemplateParameter templateParameter);
    /**
     * Queries the instance objects that are related to a particular instance 
     * object by a link in the current associations link set.
     * @param template Required value of the first association end.
     * @return List of related objects.
     */
    public java.util.List getTemplateParameter(org.omg.uml.foundation.core.ModelElement template);
    /**
     * Creates a link between the pair of instance objects in the associations 
     * link set.
     * @param template Value of the first association end.
     * @param templateParameter Value of the second association end.
     */
    public boolean add(org.omg.uml.foundation.core.ModelElement template, org.omg.uml.foundation.core.TemplateParameter templateParameter);
    /**
     * Removes a link between a pair of instance objects in the current associations 
     * link set.
     * @param template Value of the first association end.
     * @param templateParameter Value of the second association end.
     */
    public boolean remove(org.omg.uml.foundation.core.ModelElement template, org.omg.uml.foundation.core.TemplateParameter templateParameter);
}
