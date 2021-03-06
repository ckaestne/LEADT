// $Id: UMLCallActionOperationComboBoxModel.java 15869 2008-10-03 00:17:47Z bobtarling $
// Copyright (c) 2008 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.core.propertypanels.ui;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.argouml.model.AttributeChangeEvent;
import org.argouml.model.Model;
import org.argouml.ui.targetmanager.TargetManager;

class UMLCallActionOperationComboBoxModel extends UMLComboBoxModel {
    /**
     * The constructor.
     */
    public UMLCallActionOperationComboBoxModel() {
        super("operation", true);
    }

    /**
     * The list of operations shall contain
     * all operations of all classifiers
     * contained in the same package as the callaction itself. <p>
     *
     * TODO: In fact, we also should include operations of imported
     * clasifiers.
     *
     * @see org.argouml.uml.ui.UMLComboBoxModel2#buildModelList()
     */
    protected void buildModelList() {
        Object target = TargetManager.getInstance().getModelTarget();
        Collection ops = new ArrayList();
        if (Model.getFacade().isACallAction(target)) {
            Object ns = Model.getFacade().getModelElementContainer(target);
            while (!Model.getFacade().isAPackage(ns)) {
                ns = Model.getFacade().getModelElementContainer(ns);
                if (ns == null) {
                    break;
                }
            }
            if (Model.getFacade().isANamespace(ns)) {
                Collection c =
                    Model.getModelManagementHelper()
                    .getAllModelElementsOfKind(
                            ns,
                            Model.getMetaTypes().getClassifier());
                Iterator i = c.iterator();
                while (i.hasNext()) {
                    ops.addAll(Model.getFacade().getOperations(i.next()));
                }
            }
            /* To be really sure, let's add the operation
             * that is linked to the action in the model,
             * too - if it is not listed yet.
             * We need this, incase an operation is moved
             * out of the package,
             * or maybe with imported XMI...
             */
            Object current = Model.getFacade().getOperation(target);
            if (Model.getFacade().isAOperation(current)) {
                if (!ops.contains(current)) {
                    ops.add(current);
                }
            }
        }
        setElements(ops);
    }

    /*
     * @see org.argouml.uml.ui.UMLComboBoxModel2#getSelectedModelElement()
     */
    protected Object getSelectedModelElement() {
        Object target = TargetManager.getInstance().getModelTarget();
        if (Model.getFacade().isACallAction(target)) {
            return Model.getFacade().getOperation(target);
        }
        return null;
    }

    /*
     * @see org.argouml.uml.ui.UMLComboBoxModel2#isValidElement(java.lang.Object)
     */
    protected boolean isValidElement(Object element) {
        Object target = TargetManager.getInstance().getModelTarget();
        if (Model.getFacade().isACallAction(target)) {
            return element == Model.getFacade().getOperation(target);
        }
        return false;
    }

    /**
     * The function in the parent removes items from the list
     * when deselected. We do not need that here. <p>
     *
     *  This function is only needed when another operation is connected to
     *  the action in the model, to select it in the combo. <p>
     *
     *  It is e.g. not usefull to update the combo for removed operations,
     *  since you can only remove operations by changing the target,
     *  and selecting the action again re-generates the complete list.
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt instanceof AttributeChangeEvent) {
            if (evt.getPropertyName().equals("operation")) {
                if (evt.getSource() == getTarget()
                        && (getChangedElement(evt) != null)) {
                    Object elem = getChangedElement(evt);
                    setSelectedItem(elem);
                }
            }
        }
    }

    /**
     * The UID.
     */
    private static final long serialVersionUID = 7752478921939209157L;
}