package de.ovgu.cide.mining.autoeval;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.ovgu.cide.features.FeatureModelManager;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementPreviewEvent;

public class MissingAnnotationView extends ViewPart {

	private TreeViewer tree;

	public MissingAnnotationView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		tree = new TreeViewer(parent);

		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());

		tree.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				TreeItem[] sel = tree.getTree().getSelection();
				assert sel.length == 1;
				assert sel[0].getData() instanceof AElement;
				AElement element = (AElement) sel[0].getData();

				int cuHash, start, len;

				ApplicationController AC = ApplicationController.getInstance();
				cuHash = element.getCompelationUnitHash();
				ICompilationUnit cu = AC.getICompilationUnit(cuHash);
				start = element.getStartPosition();
				len = element.getLength();

				try {

					AC.fireEvent(new AElementPreviewEvent(
							MissingAnnotationView.this));

					IEditorPart javaEditor;
					javaEditor = JavaUI.openInEditor(cu);

					if ((start >= 0) && (javaEditor instanceof ITextEditor)) {
						((ITextEditor) javaEditor).selectAndReveal(start, len);

					}

				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (JavaModelException e) {
					e.printStackTrace();
				}

			}
		});
	}

	private void fillLocalToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(new Action("load") {
			public void run() {
				try {
					init();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	protected void init() throws Exception {
		String projectName = "MobileMedia_Eval";
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		project.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
		Set<IFeature> features = FeatureModelManager.getInstance()
				.getFeatureModel(project).getFeatures();

		for (IFeature f : features) {
			TreeItem featureNode = new TreeItem(tree.getTree(), SWT.DEFAULT);
			featureNode.setText(f.getName());
			featureNode.setData(f);

			String targetAnnotationFile = "target_" + f.getName() + ".log";
			Set<String> targetNodes = AutoEval.readElements(project
					.getFile(targetAnnotationFile));

			ApplicationController lDB = ApplicationController.getInstance();
			for (AElement element : lDB.getAllElements())
				if (targetNodes.contains(element.getId()))
					if (!lDB.getElementColors(element).contains(f)) {
						TreeItem elementNode = new TreeItem(featureNode,
								SWT.DEFAULT);
						elementNode.setText(element.getDisplayName());
						elementNode.setData(element);
					}
		}

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
