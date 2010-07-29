package de.ovgu.cide.mining.database.recommendationengine;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

import de.ovgu.cide.CIDECorePlugin;
import de.ovgu.cide.language.jdt.UnifiedASTNode;
import de.ovgu.cide.mining.database.ApplicationController;
import de.ovgu.cide.mining.database.model.AElement;
import de.ovgu.cide.mining.events.AElementPreviewEvent;
import de.ovgu.cide.mining.events.AElementViewCountChangedEvent;
import de.ovgu.cide.util.EditorUtilityJava;
import de.ovgu.cide.utils.EditorUtility;

public class AElementViewCountManager implements ISelectionListener, Observer {

	// private static ElementColorManager elementColorManager = null;
	private Map<AElement, Integer> element2Views;
	private ApplicationController AC;

	private ISelection oldSelection;

	private Object previewSource;

	public AElementViewCountManager(ApplicationController AC) {
		this.AC = AC;
		oldSelection = null;
		element2Views = new HashMap<AElement, Integer>();
		previewSource = null;

		AC.addObserver(this);
		try {
			ISelectionService service = CIDECorePlugin.getDefault()
					.getWorkbench().getActiveWorkbenchWindow()
					.getSelectionService();
			service.addPostSelectionListener(this);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof ITextSelection) {

			ITextSelection textSelection = (ITextSelection) selection;

			if (oldSelection != null && oldSelection.equals(textSelection)) {
				oldSelection = textSelection;
				return;
			}

			oldSelection = textSelection;

			// show element for editor selection

			int offset = textSelection.getOffset();
			int length = textSelection.getLength();

			if (length == 0)
				return;

			IEditorPart editor = EditorUtility.getActiveEditor();
			if (editor == null)
				return;

			ICompilationUnit CU = EditorUtilityJava
					.getCompilationUnitFromInput(editor);
			if (CU == null)
				return;

			int CUHash = CU.hashCode();
			UnifiedASTNode node;

			for (AElement element : AC.getAllElements()) {
				if (element.getCompelationUnitHash() != CUHash)
					continue;

				// node = element.getUnifiedASTNode();
				if (element.getStartPosition() != offset)
					continue;

				if (element.getLength() != length)
					continue;

				// found element!

				Integer viewCounter = element2Views.get(element);

				if (viewCounter == null) {
					element2Views.put(element, 1);
				} else {
					element2Views.put(element, ++viewCounter);
				}

				AC.fireEvent(new AElementViewCountChangedEvent(this, element,
						previewSource));

				previewSource = null;
				return;
			}

			previewSource = null;

		}

	}

	public int getViewCountForElement(AElement element) {

		if (element == null)
			return 0;

		Integer viewCounter = element2Views.get(element);

		if (viewCounter == null)
			return 0;

		return viewCounter;

	}

	public void update(Observable o, Object arg) {
		if (o.equals(AC)) {
			if (arg instanceof AElementPreviewEvent) {
				previewSource = ((AElementPreviewEvent) arg).getSource();
			}
		}
	}

}
