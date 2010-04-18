package edu.wm.flat3.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.texteditor.PropertyEventDispatcher;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;

/**
 * Class to implement vertical columns. Currently unused
 * 
 * @author vgarg
 * 
 */
public class ContributedRulerColorColumn extends AbstractContributedRulerColumn
		implements IVerticalRulerInfo, IVerticalRulerInfoExtension
{

	/**
	 * The delegate and implemenation of the ruler.
	 */
	private IVerticalRulerColumn fDelegate;

	/**
	 * Preference dispatcher that registers a single listener so we don't have
	 * to manage every single preference listener.
	 */
	private PropertyEventDispatcher fDispatcher;
	private ISourceViewer fViewer;
	private ISharedTextColors sharedColors;

	public ContributedRulerColorColumn()
	{
		sharedColors = getSharedColors();
		fDelegate = new ColorColumn();
	}

	public Control createControl(CompositeRuler parentRuler,
			Composite parentControl)
	{
		Assert.isTrue(fDelegate != null);
		ITextViewer viewer = parentRuler.getTextViewer();
		Assert.isLegal(viewer instanceof ISourceViewer);
		fViewer = (ISourceViewer) viewer;

		initialize();
		Control control = fDelegate.createControl(parentRuler, parentControl);
		return control;
	}

	private void initialize()
	{
		fDelegate.redraw();

		// listen to changes
		/*
		 * fDispatcher= new PropertyEventDispatcher(store);
		 * 
		 * fDispatcher.addPropertyChangeListener(FG_COLOR_KEY, new
		 * IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateForegroundColor(store, fDelegate); fDelegate.redraw(); } });
		 * IPropertyChangeListener backgroundHandler= new
		 * IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateBackgroundColor(store, fDelegate); fDelegate.redraw(); } };
		 * fDispatcher.addPropertyChangeListener(BG_COLOR_KEY,
		 * backgroundHandler);
		 * fDispatcher.addPropertyChangeListener(USE_DEFAULT_BG_KEY,
		 * backgroundHandler);
		 * 
		 * fDispatcher.addPropertyChangeListener(LINE_NUMBER_KEY, new
		 * IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) { // only handle quick diff
		 * on/off information, but not ruler visibility (handled by
		 * AbstractDecoratedTextEditor) updateLineNumbersVisibility(fDelegate); }
		 * });
		 * 
		 * fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_CHARACTER_MODE,
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateCharacterMode(store, fDelegate); } });
		 * 
		 * fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_RENDERING_MODE,
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateRevisionRenderingMode(store, fDelegate); } });
		 * 
		 * fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_AUTHOR,
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateRevisionAuthorVisibility(store, fDelegate); } });
		 * 
		 * fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.REVISION_RULER_SHOW_REVISION,
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateRevisionIdVisibility(store, fDelegate); } });
		 * 
		 * fDispatcher.addPropertyChangeListener(AbstractDecoratedTextEditorPreferenceConstants.QUICK_DIFF_ALWAYS_ON,
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateQuickDiffVisibility(fDelegate); } });
		 * 
		 * if (changedPref != null) {
		 * fDispatcher.addPropertyChangeListener(changedPref.getColorPreferenceKey(),
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateChangedColor(changedPref, store, fDelegate);
		 * fDelegate.redraw(); } }); } if (addedPref != null) {
		 * fDispatcher.addPropertyChangeListener(addedPref.getColorPreferenceKey(),
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateAddedColor(addedPref, store, fDelegate); fDelegate.redraw(); }
		 * }); } if (deletedPref != null) {
		 * fDispatcher.addPropertyChangeListener(deletedPref.getColorPreferenceKey(),
		 * new IPropertyChangeListener() { public void
		 * propertyChange(PropertyChangeEvent event) {
		 * updateDeletedColor(deletedPref, store, fDelegate);
		 * fDelegate.redraw(); } }); }
		 */

	}

	public Control getControl()
	{
		return fDelegate.getControl();
	}

	public int getWidth()
	{
		return fDelegate.getWidth();
	}

	public void redraw()
	{
		fDelegate.redraw();

	}

	public void setFont(Font font)
	{
		fDelegate.setFont(font);

	}

	public void setModel(IAnnotationModel model)
	{
		// if (getQuickDiffPreference())
		fDelegate.setModel(model);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#removeVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void removeVerticalRulerListener(IVerticalRulerListener listener)
	{
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			((IVerticalRulerInfoExtension) fDelegate)
					.removeVerticalRulerListener(listener);
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn#columnRemoved()
	 */
	@Override
	public void columnRemoved()
	{
		if (fDispatcher != null)
		{
			fDispatcher.dispose();
			fDispatcher = null;
		}
	}

	private IPreferenceStore getPreferenceStore()
	{
		return EditorsUI.getPreferenceStore();
	}

	private ISharedTextColors getSharedColors()
	{
		return EditorsUI.getSharedTextColors();
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#getLineOfLastMouseButtonActivity()
	 */
	public int getLineOfLastMouseButtonActivity()
	{
		if (fDelegate instanceof IVerticalRulerInfo)
			((IVerticalRulerInfo) fDelegate).getLineOfLastMouseButtonActivity();
		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfo#toDocumentLineNumber(int)
	 */
	public int toDocumentLineNumber(int y_coordinate)
	{
		if (fDelegate instanceof IVerticalRulerInfo)
			((IVerticalRulerInfo) fDelegate).toDocumentLineNumber(y_coordinate);
		return -1;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#addVerticalRulerListener(org.eclipse.jface.text.source.IVerticalRulerListener)
	 */
	public void addVerticalRulerListener(IVerticalRulerListener listener)
	{
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			((IVerticalRulerInfoExtension) fDelegate)
					.addVerticalRulerListener(listener);
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getHover()
	 */
	public IAnnotationHover getHover()
	{
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			return ((IVerticalRulerInfoExtension) fDelegate).getHover();
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension#getModel()
	 */
	public IAnnotationModel getModel()
	{
		if (fDelegate instanceof IVerticalRulerInfoExtension)
			return ((IVerticalRulerInfoExtension) fDelegate).getModel();
		return null;
	}

}
