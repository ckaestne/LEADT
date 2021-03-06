package de.ovgu.cide.colorfilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Display;

import de.ovgu.cide.ASTColorChangedEvent;
import de.ovgu.cide.ChangeType;
import de.ovgu.cide.ColorListChangedEvent;
import de.ovgu.cide.FileColorChangedEvent;
import de.ovgu.cide.IColorChangeListener;

/**
 * listens to color changes and updates the view (including the filter) at the
 * according positions
 * 
 * 
 * @author cKaestner
 * 
 */
public class ColorFilterUpdater implements IColorChangeListener {

	private StructuredViewer viewer;
	private IAction filterAction;

	ColorFilterUpdater(EnableColorFilterAction action, StructuredViewer viewer) {
		this.viewer = viewer;
		this.filterAction = action;
	}

	public void astColorChanged(ASTColorChangedEvent event) {
		if (filterAction.isChecked())
			update(Collections.singleton(event.getColoredSourceFile()
					.getResource().getParent()));
	}

	public void fileColorChanged(FileColorChangedEvent event) {
		if (filterAction.isChecked()) {
			Set<IContainer> parentFolders = new HashSet<IContainer>();
			for (IContainer container : event.getAffectedFolders())
				parentFolders.add(container.getParent());
			update(parentFolders);
		}
	}

	private void update(final Collection<? extends IResource> resources) {
		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				try {
					viewer.getControl().setRedraw(false);
					for (IResource res : resources)
						if (res != null)
							viewer.refresh(res, false);
				} finally {
					viewer.getControl().setRedraw(true);
				}
			}
		});
	}

	public void colorListChanged(ColorListChangedEvent event) {
		if (filterAction.isChecked())
			if (event.anyChangeOf(ChangeType.COLOR)
					|| event.anyChangeOf(ChangeType.VISIBILITY))
				update(Collections.singleton(event.getProject()));
	}

}
