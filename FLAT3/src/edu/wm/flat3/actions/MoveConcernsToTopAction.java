package edu.wm.flat3.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProvider;
import edu.wm.flat3.repository.Concern;

public class MoveConcernsToTopAction extends Action
{
	IConcernModelProvider concernModelProvider;
	List<Concern> concerns = new ArrayList<Concern>();
	
	public MoveConcernsToTopAction(IConcernModelProvider concernModelProvider)
	{
		this.concernModelProvider = concernModelProvider;

		setText(FLATTT
				.getResourceString("actions.MoveConcernToRootAction.Top.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/arrow_up.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.MoveConcernToRootAction.Top.ToolTip"));
	}
	
	public void addConcern(Concern concern)
	{
		concerns.add(concern);
	}
	
	public boolean hasWork()
	{
		return !concerns.isEmpty();
	}

	@Override
	public void run()
	{
		for(Concern concern : concerns)
		{
			concernModelProvider.getModel().getRoot().addChild(concern);
		}
	}

}
