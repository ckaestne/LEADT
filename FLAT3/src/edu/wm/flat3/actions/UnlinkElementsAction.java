package edu.wm.flat3.actions;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.EdgeKind;

public class UnlinkElementsAction
	extends MultiElementAction
{
	protected UnlinkItemListener clickListener = new UnlinkItemListener();

	/**
	 * Populate the dynamic menu
	 */
	@Override
	protected void fillMenu(Menu parent, List<Concern> concerns)
	{
		assert parent != null;
		assert !parent.isDisposed();

		// Always recreate the Unlink menu since it is small
		for (MenuItem child : parent.getItems())
		{
			assert !child.isDisposed();
			child.dispose();
		}

		if (!selectedJavaElements.isEmpty())
		{
			fillMenuRecursive(parent, concerns, selectedJavaElements,
					concernModelProvider.getLinkType());

			// If there were concerns in the model, add a separator before the New
			// Concern item
			if (parent.getItemCount() > 0)
			{
				parent.setEnabled(true);

				new MenuItem(parent, SWT.SEPARATOR);

				// Add the "Unlink All" item
				MenuItem lNewConcernItem = new MenuItem(parent, SWT.PUSH);
				lNewConcernItem.addSelectionListener(clickListener);
				lNewConcernItem.setText("Unlink All");
			}
			else
			{
				// parent.setEnabled(false);
			}
		}
		else
		{
			// parent.setEnabled(false);
		}
	}

	/**
	 * Builds the flat 'Unlink' menu
	 */
	private void fillMenuRecursive(Menu parent, List<Concern> concerns, List<IJavaElement> selectedJavaElements, EdgeKind concernComponentRelation)
	{
		Set<Character> mnemonicsUsed = new HashSet<Character>();

		for (Concern concern : concerns)
		{
			// See if any of the selected elements are already
			// linked to the concern
			boolean isLinked = isLinked(concern, selectedJavaElements,
					concernComponentRelation);

			// For the 'Unlink' menu, only show linked concerns

			if (isLinked)
			{
				MenuItem lMenuItem = new MenuItem(parent, SWT.PUSH);
				lMenuItem.setData(concern);
				lMenuItem.setText(getConcernNameWithMnemonic(concern,
						mnemonicsUsed));
				lMenuItem.addSelectionListener(clickListener);
			}

			List<Concern> children = concern.getChildren();

			if (!children.isEmpty())
			{
				// The 'Unlink' menu is flat, so we pass in the same
				// parent menu

				fillMenuRecursive(parent, children, selectedJavaElements,
						concernComponentRelation);
			}

			children = null; // Helps GC
		}

		mnemonicsUsed = null; // Helps GC
	}

	private class UnlinkItemListener
		extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent pEvent)
		{
			MenuItem menuItemClicked = (MenuItem) pEvent.widget;
			assert menuItemClicked != null;

			Concern targetConcern = null;

			Object data = menuItemClicked.getData();
			if (data != null)
			{
				targetConcern = (Concern) data;
				assert targetConcern != null;
			}

			// Use the RemoveMultipleItemsAction for unlinking since
			// it provides a confirmation prompt
			RemoveMultipleItemsAction removeAction = new RemoveMultipleItemsAction(
					concernModelProvider);

			EdgeKind linkType = concernModelProvider.getLinkType();

			for (IJavaElement javaElement : selectedJavaElements)
			{
				if (targetConcern != null)
				{
					// False means, if the element is a type, only unlink
					// the type, not its members
					removeAction.addItemToUnlink(targetConcern, javaElement,
							linkType);
				}
				else
				{
					Collection<Concern> linkedConcerns = concernModelProvider.getModel().getLinkedConcerns(
							javaElement, linkType);

					if (linkedConcerns == null)
						continue;

					for (Concern linkedConcern : linkedConcerns)
					{
						// False means, if the element is a type, only unlink
						// the type, not its members

						removeAction.addItemToUnlink(linkedConcern,
								javaElement, linkType);
					}
				}
			}

			removeAction.run();
		}
	}
}
