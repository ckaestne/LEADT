package edu.wm.flat3.actions;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.ConcernEvent;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;

public class CreateRandomLinksAction
	extends Action
{
	IConcernModelProvider concernModelProvider;
	
	public CreateRandomLinksAction(IConcernModelProvider concernModelProvider)
	{
		this.concernModelProvider = concernModelProvider;
	
		setText(FLATTT.getResourceString(
				"actions.CreateRandomLinksAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/hourglass_link.png"));
		setToolTipText(FLATTT.getResourceString(
				"actions.CreateRandomLinksAction.ToolTip"));
	}
	
	@Override
	public void run()
	{
		if (!shouldProceed())
			return;
		
		Job job = new Job("Creating random links...")
		{
			@Override
			protected IStatus run(IProgressMonitor progressMonitor)
			{
				IStatus status = null;
				
				try
				{
					concernModelProvider.getModel().disableNotifications();

					progressMonitor.beginTask("Removing existing links...", 
							IProgressMonitor.UNKNOWN);
					
					concernModelProvider.getModel().removeLinks(
							concernModelProvider.getLinkType());

					progressMonitor.done();
					
					status = createRandomLinks(progressMonitor);
				}
				finally
				{
					concernModelProvider.getModel().clearQueuedEvents();
					concernModelProvider.getModel().enableNotifications();
					concernModelProvider.getModel().modelChanged(
							ConcernEvent.createAllConcernsChanged());
				}

				return status;
			}
	
		};
			
		job.setUser(true);
		job.schedule();
	}
	
	private IStatus createRandomLinks(IProgressMonitor progressMonitor)
	{
		List<Component> allComponents = concernModelProvider.getModel().getComponents();
		Component[] allComponentsCopy = allComponents.toArray(new Component[]{});
		
		for(Component component : allComponentsCopy)
		{
			if (Component.validateAndConvertJavaElement(component.getJavaElement()) == null)
				allComponents.remove(component);
		}
		
		int totalComponents = allComponents.size();

		Random random = new Random();
		
		Collection<Concern> allConcerns =
			concernModelProvider.getModel().getConcernDomain().getRoot().getSelfAndDescendants();
		
		progressMonitor.beginTask("Linking", allConcerns.size()-1);
		
		EdgeKind linkType = concernModelProvider.getLinkType();

		for(Concern concern : allConcerns)
		{
			if (concern.isRoot())
				continue;
			
			int numLinks = random.nextInt(totalComponents);

			Component[] componentsToLink = new Component[numLinks];
			
			progressMonitor.subTask(concern.getDisplayName() + " -> " +
					numLinks + " links");
			
			while(--numLinks >= 0)
			{
				if (progressMonitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				int index = random.nextInt(totalComponents);
			
				componentsToLink[numLinks] = allComponents.get(index); 
			}
			
			concern.link(componentsToLink, linkType);
			
			progressMonitor.worked(1);
		}
		
		progressMonitor.done();

		return Status.OK_STATUS;
	}

	private boolean shouldProceed()
	{
		return MessageDialog.openQuestion(
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
			FLATTT.getResourceString("actions.CreateRandomLinksAction.QuestionDialogTitle"),
			FLATTT.getResourceString("actions.CreateRandomLinksAction.WarningOverwrite"));
	}
}
