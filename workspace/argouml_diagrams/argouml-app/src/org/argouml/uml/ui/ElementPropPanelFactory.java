// $Id: ElementPropPanelFactory.java 16262 2008-12-06 19:42:00Z tfmorris $
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

package org.argouml.uml.ui;

import org.argouml.model.Model;
import org.argouml.uml.ui.behavior.activity_graphs.PropPanelActionState;
import org.argouml.uml.ui.behavior.activity_graphs.PropPanelActivityGraph;
import org.argouml.uml.ui.behavior.activity_graphs.PropPanelCallState;
import org.argouml.uml.ui.behavior.activity_graphs.PropPanelClassifierInState;
import org.argouml.uml.ui.behavior.activity_graphs.PropPanelObjectFlowState;
import org.argouml.uml.ui.behavior.activity_graphs.PropPanelPartition;
import org.argouml.uml.ui.behavior.activity_graphs.PropPanelSubactivityState;
import org.argouml.uml.ui.behavior.collaborations.PropPanelAssociationEndRole;
import org.argouml.uml.ui.behavior.collaborations.PropPanelAssociationRole;
import org.argouml.uml.ui.behavior.collaborations.PropPanelClassifierRole;
import org.argouml.uml.ui.behavior.collaborations.PropPanelCollaboration;
import org.argouml.uml.ui.behavior.collaborations.PropPanelInteraction;
import org.argouml.uml.ui.behavior.collaborations.PropPanelMessage;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelAction;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelActionSequence;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelArgument;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelCallAction;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelComponentInstance;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelCreateAction;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelDestroyAction;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelException;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelLink;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelLinkEnd;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelNodeInstance;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelObject;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelReception;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelReturnAction;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelSendAction;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelSignal;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelStimulus;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelTerminateAction;
import org.argouml.uml.ui.behavior.common_behavior.PropPanelUninterpretedAction;
import org.argouml.uml.ui.behavior.state_machines.PropPanelCallEvent;
import org.argouml.uml.ui.behavior.state_machines.PropPanelChangeEvent;
import org.argouml.uml.ui.behavior.state_machines.PropPanelCompositeState;
import org.argouml.uml.ui.behavior.state_machines.PropPanelFinalState;
import org.argouml.uml.ui.behavior.state_machines.PropPanelGuard;
import org.argouml.uml.ui.behavior.state_machines.PropPanelPseudostate;
import org.argouml.uml.ui.behavior.state_machines.PropPanelSignalEvent;
import org.argouml.uml.ui.behavior.state_machines.PropPanelSimpleState;
import org.argouml.uml.ui.behavior.state_machines.PropPanelStateMachine;
import org.argouml.uml.ui.behavior.state_machines.PropPanelStateVertex;
import org.argouml.uml.ui.behavior.state_machines.PropPanelStubState;
import org.argouml.uml.ui.behavior.state_machines.PropPanelSubmachineState;
import org.argouml.uml.ui.behavior.state_machines.PropPanelSynchState;
import org.argouml.uml.ui.behavior.state_machines.PropPanelTimeEvent;
import org.argouml.uml.ui.behavior.state_machines.PropPanelTransition;
import org.argouml.uml.ui.behavior.use_cases.PropPanelActor;
import org.argouml.uml.ui.behavior.use_cases.PropPanelExtend;
import org.argouml.uml.ui.behavior.use_cases.PropPanelExtensionPoint;
import org.argouml.uml.ui.behavior.use_cases.PropPanelInclude;
import org.argouml.uml.ui.behavior.use_cases.PropPanelUseCase;
import org.argouml.uml.ui.foundation.core.PropPanelAbstraction;
import org.argouml.uml.ui.foundation.core.PropPanelAssociation;
import org.argouml.uml.ui.foundation.core.PropPanelAssociationClass;
import org.argouml.uml.ui.foundation.core.PropPanelAssociationEnd;
import org.argouml.uml.ui.foundation.core.PropPanelAttribute;
import org.argouml.uml.ui.foundation.core.PropPanelClass;
import org.argouml.uml.ui.foundation.core.PropPanelClassifier;
import org.argouml.uml.ui.foundation.core.PropPanelComment;
import org.argouml.uml.ui.foundation.core.PropPanelComponent;
import org.argouml.uml.ui.foundation.core.PropPanelConstraint;
import org.argouml.uml.ui.foundation.core.PropPanelDataType;
import org.argouml.uml.ui.foundation.core.PropPanelDependency;
import org.argouml.uml.ui.foundation.core.PropPanelEnumeration;
import org.argouml.uml.ui.foundation.core.PropPanelEnumerationLiteral;
import org.argouml.uml.ui.foundation.core.PropPanelFlow;
import org.argouml.uml.ui.foundation.core.PropPanelGeneralization;
import org.argouml.uml.ui.foundation.core.PropPanelInterface;
import org.argouml.uml.ui.foundation.core.PropPanelMethod;
import org.argouml.uml.ui.foundation.core.PropPanelNode;
import org.argouml.uml.ui.foundation.core.PropPanelOperation;
import org.argouml.uml.ui.foundation.core.PropPanelParameter;
import org.argouml.uml.ui.foundation.core.PropPanelPermission;
import org.argouml.uml.ui.foundation.core.PropPanelRelationship;
import org.argouml.uml.ui.foundation.core.PropPanelUsage;
import org.argouml.uml.ui.foundation.extension_mechanisms.PropPanelStereotype;
import org.argouml.uml.ui.foundation.extension_mechanisms.PropPanelTagDefinition;
import org.argouml.uml.ui.foundation.extension_mechanisms.PropPanelTaggedValue;
import org.argouml.uml.ui.model_management.PropPanelModel;
import org.argouml.uml.ui.model_management.PropPanelPackage;
import org.argouml.uml.ui.model_management.PropPanelSubsystem;

/**
 * This factory creates the right PropPanelModelElement for a given UML Element.
 * <p>
 * 
 * Constraint: Every UML element shall have a proppanel. We throw an exception
 * if one is not found.
 * 
 * @author Michiel
 */
class ElementPropPanelFactory implements PropPanelFactory {

	public PropPanel createPropPanel(Object element) {
		if (Model.getFacade().isAElement(element)) {
			// A Subsytem is a Classifier also, so it needs to come before
			// the classifier check
			if (Model.getFacade().isASubsystem(element)) {
				return new PropPanelSubsystem();
			}
			if (Model.getFacade().isAClassifier(element)) {
				return getClassifierPropPanel(element);
			}
			if (Model.getFacade().isARelationship(element)) {
				return getRelationshipPropPanel(element);
			}
			if (Model.getFacade().isAStateVertex(element)) {
				return getStateVertexPropPanel(element);
			}
			if (Model.getFacade().isAActionSequence(element)) {
				// This is not a subtype of PropPanelAction,
				// so it must come first
				return new PropPanelActionSequence();
			}
			if (Model.getFacade().isAAction(element)) {
				return getActionPropPanel(element);
				/*
				 * TODO: This needs to be in type hierarchy order to work
				 * properly and create the most specific property panel
				 * properly. Everything which has been factored out of this
				 * method has been reviewed. Anything below this point still
				 * needs to be reviewed - tfm
				 */
			}
			if (Model.getFacade().isAActivityGraph(element)) {
				return new PropPanelActivityGraph();
			}
			if (Model.getFacade().isAArgument(element)) {
				return new PropPanelArgument();
			}
			if (Model.getFacade().isAAssociationEndRole(element)) {
				return new PropPanelAssociationEndRole();
			}
			if (Model.getFacade().isAAssociationEnd(element)) {
				return new PropPanelAssociationEnd();
			}
			if (Model.getFacade().isAAttribute(element)) {
				return new PropPanelAttribute();
			}
			if (Model.getFacade().isACollaboration(element)) {
				return new PropPanelCollaboration();
			}
			if (Model.getFacade().isAComment(element)) {
				return new PropPanelComment();
			}
			if (Model.getFacade().isAComponentInstance(element)) {
				return new PropPanelComponentInstance();
			}
			if (Model.getFacade().isAConstraint(element)) {
				return new PropPanelConstraint();
			}
			if (Model.getFacade().isAEnumerationLiteral(element)) {
				return new PropPanelEnumerationLiteral();
			}
			if (Model.getFacade().isAExtensionPoint(element)) {
				return new PropPanelExtensionPoint();
			}
			if (Model.getFacade().isAGuard(element)) {
				return new PropPanelGuard();
			}
			if (Model.getFacade().isAInteraction(element)) {
				return new PropPanelInteraction();
			}
			if (Model.getFacade().isALink(element)) {
				return new PropPanelLink();
			}
			if (Model.getFacade().isALinkEnd(element)) {
				return new PropPanelLinkEnd();
			}
			if (Model.getFacade().isAMessage(element)) {
				return new PropPanelMessage();
			}
			if (Model.getFacade().isAMethod(element)) {
				return new PropPanelMethod();
			}
			if (Model.getFacade().isAModel(element)) {
				return new PropPanelModel();
			}
			if (Model.getFacade().isANodeInstance(element)) {
				return new PropPanelNodeInstance();
			}
			if (Model.getFacade().isAObject(element)) {
				return new PropPanelObject();
			}
			if (Model.getFacade().isAOperation(element)) {
				return new PropPanelOperation();
			}
			if (Model.getFacade().isAPackage(element)) {
				return new PropPanelPackage();
			}
			if (Model.getFacade().isAParameter(element)) {
				return new PropPanelParameter();
			}
			if (Model.getFacade().isAPartition(element)) {
				return new PropPanelPartition();
			}
			if (Model.getFacade().isAReception(element)) {
				return new PropPanelReception();
			}
			if (Model.getFacade().isAStateMachine(element)) {
				return new PropPanelStateMachine();
			}
			if (Model.getFacade().isAStereotype(element)) {
				return new PropPanelStereotype();
			}
			if (Model.getFacade().isAStimulus(element)) {
				return new PropPanelStimulus();
			}
			if (Model.getFacade().isATaggedValue(element)) {
				return new PropPanelTaggedValue();
			}
			if (Model.getFacade().isATagDefinition(element)) {
				return new PropPanelTagDefinition();
			}
			if (Model.getFacade().isATransition(element)) {
				return new PropPanelTransition();
			}
			if (Model.getFacade().isACallEvent(element)) {
				return new PropPanelCallEvent();
			}
			if (Model.getFacade().isAChangeEvent(element)) {
				return new PropPanelChangeEvent();
			}
			if (Model.getFacade().isASignalEvent(element)) {
				return new PropPanelSignalEvent();
			}
			if (Model.getFacade().isATimeEvent(element)) {
				return new PropPanelTimeEvent();
			}
			if (Model.getFacade().isADependency(element)) {
				return new PropPanelDependency();
			}
			throw new IllegalArgumentException("Unsupported Element type");
		}
		return null;
	}

	private PropPanelClassifier getClassifierPropPanel(Object element) {
		if (Model.getFacade().isAActor(element)) {
			return new PropPanelActor();
		}
		if (Model.getFacade().isAAssociationClass(element)) {
			return new PropPanelAssociationClass();
		}
		if (Model.getFacade().isAClass(element)) {
			return new PropPanelClass();
		}
		if (Model.getFacade().isAClassifierInState(element)) {
			return new PropPanelClassifierInState();
		}
		if (Model.getFacade().isAClassifierRole(element)) {
			return new PropPanelClassifierRole();
		}
		if (Model.getFacade().isAComponent(element)) {
			return new PropPanelComponent();
		}
		if (Model.getFacade().isADataType(element)) {
			if (Model.getFacade().isAEnumeration(element)) {
				return new PropPanelEnumeration();
			} else {
				return new PropPanelDataType();
			}
		}
		if (Model.getFacade().isAInterface(element)) {
			return new PropPanelInterface();
		}
		if (Model.getFacade().isANode(element)) {
			return new PropPanelNode();
		}
		if (Model.getFacade().isASignal(element)) {
			if (Model.getFacade().isAException(element)) {
				return new PropPanelException();
			} else {
				return new PropPanelSignal();
			}
		}
		if (Model.getFacade().isAUseCase(element)) {
			return new PropPanelUseCase();
		}

		// TODO: A Subsystem is a Classifier, but its PropPanel is derived from
		// PropPanelPackage
		// else if (Model.getFacade().isASubsystem(element)) {
		// return new PropPanelSubsystem();
		// }

		// TODO: In UML 2.x Associations will fall through here because they
		// are Classifiers as well as Relationships, but we test for Classifier
		// first.

		throw new IllegalArgumentException("Unsupported Element type");
	}

	private PropPanelRelationship getRelationshipPropPanel(Object element) {
		if (Model.getFacade().isAAssociation(element)) {
			if (Model.getFacade().isAAssociationRole(element)) {
				return new PropPanelAssociationRole();
			} else {
				return new PropPanelAssociation();
			}
		}
		if (Model.getFacade().isADependency(element)) {
			if (Model.getFacade().isAAbstraction(element)) {
				return new PropPanelAbstraction();
			}
			if (Model.getFacade().isAPackageImport(element)) {
				return new PropPanelPermission();
			}
			if (Model.getFacade().isAUsage(element)) {
				return new PropPanelUsage();
				// } if(Model.getFacade().isABinding(element)) {
				// return new PropPanelBinding();
			} else {
				return new PropPanelDependency();
			}
		}
		if (Model.getFacade().isAExtend(element)) {
			return new PropPanelExtend();
		}
		if (Model.getFacade().isAFlow(element)) {
			return new PropPanelFlow();
		}
		if (Model.getFacade().isAGeneralization(element)) {
			return new PropPanelGeneralization();
		}
		if (Model.getFacade().isAInclude(element)) {
			return new PropPanelInclude();
		}
		throw new IllegalArgumentException("Unsupported Relationship type");
	}

	private PropPanelAction getActionPropPanel(Object action) {
		if (Model.getFacade().isACallAction(action)) {
			return new PropPanelCallAction();
		}
		if (Model.getFacade().isACreateAction(action)) {
			return new PropPanelCreateAction();
		}
		if (Model.getFacade().isADestroyAction(action)) {
			return new PropPanelDestroyAction();
		}
		if (Model.getFacade().isAReturnAction(action)) {
			return new PropPanelReturnAction();
		}
		if (Model.getFacade().isASendAction(action)) {
			return new PropPanelSendAction();
		}
		if (Model.getFacade().isATerminateAction(action)) {
			return new PropPanelTerminateAction();
		}
		if (Model.getFacade().isAUninterpretedAction(action)) {
			return new PropPanelUninterpretedAction();
		}
		throw new IllegalArgumentException("Unsupported Action type");
	}

	private PropPanelStateVertex getStateVertexPropPanel(Object element) {
		if (Model.getFacade().isAState(element)) {
			if (Model.getFacade().isACallState(element)) {
				return new PropPanelCallState();
			}
			if (Model.getFacade().isAActionState(element)) {
				return new PropPanelActionState();
			}
			if (Model.getFacade().isACompositeState(element)) {
				if (Model.getFacade().isASubmachineState(element)) {
					if (Model.getFacade().isASubactivityState(element)) {
						return new PropPanelSubactivityState();
					}
					return new PropPanelSubmachineState();

				} else {
					return new PropPanelCompositeState();
				}
			}
			if (Model.getFacade().isAFinalState(element)) {
				return new PropPanelFinalState();
			}
			if (Model.getFacade().isAObjectFlowState(element)) {
				return new PropPanelObjectFlowState();
			}
			if (Model.getFacade().isASimpleState(element)) {
				return new PropPanelSimpleState();
			}
		}
		if (Model.getFacade().isAPseudostate(element)) {
			return new PropPanelPseudostate();
		}
		if (Model.getFacade().isAStubState(element)) {
			return new PropPanelStubState();
		}
		if (Model.getFacade().isASynchState(element)) {
			return new PropPanelSynchState();
		}
		throw new IllegalArgumentException("Unsupported State type");
	}

}
