package edu.wm.flat3.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.model.IConcernModelProviderEx;
import edu.wm.flat3.repository.ConcernDomain;
import edu.wm.flat3.util.ProblemManager;

public class OpenConcernDomainAction 
	extends Action 
	implements IMenuCreator
{
	private Menu menu = null;
	private IConcernModelProviderEx concernModelProvider;
	private boolean disbaleSelectedDomain;
	
	public static final String ID_DOMAIN_SEP = "$";
	public static final String ID_COUNT_SEP = "#";
	
	public OpenConcernDomainAction(IConcernModelProviderEx concernModelProvider,
	                               boolean disableSelectedDomain)
	{
		this.concernModelProvider = concernModelProvider;
		this.disbaleSelectedDomain = disableSelectedDomain;
		
		setText(FLATTT
				.getResourceString("actions.OpenConcernDomainAction.Label"));
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/concern_domain.gif"));
		setToolTipText(FLATTT
				.getResourceString("actions.OpenConcernDomainAction.ToolTip"));

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
		
		List<ConcernDomain> concernDomains = 
			concernModelProvider.getModel().getConcernDomains(null);
		
		for(ConcernDomain concernDomain : concernDomains)
		{
			MenuItem lMenuItem = new MenuItem(menu, SWT.PUSH);
			lMenuItem.setText(concernDomain.getName());
			lMenuItem.addSelectionListener( new OpenConcernDomainMenuListener() );
			
			if (disbaleSelectedDomain && 
				concernModelProvider.getModel().getConcernDomain().equals(concernDomain))
			{
				lMenuItem.setEnabled(false);
			}
		}
		
		setEnabled(menu.getItems().length > 0);
	}

	class OpenConcernDomainMenuListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent selectionEvent)
		{
			String concernDomain = ((MenuItem) selectionEvent.widget).getText();
			assert concernDomain != null && !concernDomain.isEmpty();
			concernModelProvider.setConcernDomain(concernDomain);

			// Update the enabled/disabled state
			updateMenu();
		}
	}
	
	public static String createSecondaryId(String viewId, String concernDomain)
	{
		return viewId + ID_DOMAIN_SEP + concernDomain;
	}
	
	public static String extractConcernDomainFromSecondaryId(String viewId)
	{
		if (viewId == null)
			return null;
		
		int findDomainSeparator = viewId.indexOf(ID_DOMAIN_SEP);
		int findCountSeparator = viewId.indexOf(ID_COUNT_SEP);
		if (findDomainSeparator >= 0)
		{
			if (findCountSeparator >= 0)
				return viewId.substring(findDomainSeparator+1, findCountSeparator);
			else
				return viewId.substring(findDomainSeparator+1);
		}
		else
			return null;
	}
	
	public static void openConcernDomainHelper(IWorkbenchPartSite site, String concernDomain)
	{
		try
		{
			String secondaryId = createSecondaryId(site.getId(), concernDomain);
			int count = 0;
			String secondaryIdToUse; 
			IViewReference view = null;
			
			// Set arbitrary limit of 10 to prevent infinite loop
			do
			{
				secondaryIdToUse = secondaryId + ID_COUNT_SEP + count; 
				view = site.getPage().findViewReference(site.getId(), secondaryIdToUse);
				++count;
				
			} while (view != null && count < 10);

			// Always create a new view even if one exists for the concern domain
			
			//if (view != null)
			//{
			//	site.getPage().activate(view.getPart(false));
			//}
			//else
			{
				IViewPart viewPart = site.getPage().showView(site.getId(), secondaryIdToUse, 
						IWorkbenchPage.VIEW_ACTIVATE | IWorkbenchPage.VIEW_CREATE);
				
				site.getPage().activate(viewPart);
			}
		}
		catch (PartInitException e)
		{
			ProblemManager.reportException(e);
		}
	}
}
