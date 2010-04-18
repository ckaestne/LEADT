package edu.wm.flat3.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProviderEx;
import edu.wm.flat3.repository.EdgeKind;

public class SetLinkTypeAction 
	extends Action 
	implements IMenuCreator
{
	private Menu menu = null;
	private IConcernModelProviderEx concernModelProvider;
	
	public SetLinkTypeAction(
	              IConcernModelProviderEx concernModelProvider)
	{
		this.concernModelProvider = concernModelProvider;
		
		setText(FLATTT
				.getResourceString("actions.SetLinkTypeAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/link_go.png"));
		setToolTipText(FLATTT
				.getResourceString("actions.SetLinkTypeAction.ToolTip"));

		setMenuCreator(this);
	}

	//-----------------------------------------------------
	// IMenuCreator implementation
	//-----------------------------------------------------
	
	@Override
	public void dispose()
	{
		if (menu != null && !menu.isDisposed())
		{
			menu.dispose();
			menu = null;
		}
	}

	@Override
	public Menu getMenu(Control parent)
	{
		return null;
	}

	@Override
	public Menu getMenu(Menu parent)
	{
		dispose();

		menu = new Menu(parent);
		updateMenu();
		return menu;
	}
	
	public void updateMenu()
	{
		// Ignore update requests that occur before the menu
		// is initialized
		if (menu == null || menu.isDisposed())
			return;
		
		for(MenuItem child : menu.getItems())
		{
			assert !child.isDisposed();
			child.dispose();
		}
		
		for(EdgeKind edgeKind : EdgeKind.values())
		{
			MenuItem lMenuItem = new MenuItem(menu, SWT.PUSH);
			lMenuItem.setText(edgeKind.toString().replace('-', ' '));
			lMenuItem.addSelectionListener( new SetRelationMenuListener() );
			
			if (concernModelProvider.getLinkType().equals(edgeKind))
			{
				lMenuItem.setEnabled(false);
			}
		}
		
		setEnabled(menu.getItems().length > 0);
	}

	private final class SetRelationMenuListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent selectionEvent)
		{
			String edgeKindString = ((MenuItem) selectionEvent.widget).getText();

			edgeKindString = edgeKindString.replace(' ', '_');
			
			EdgeKind edgeKind = EdgeKind.valueOfIgnoreCase(edgeKindString);
			
			concernModelProvider.setLinkType(edgeKind);

			// Update the enabled/disabled state
			updateMenu();
		}
	}
}
