package de.ovgu.cide.editor;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import de.ovgu.cide.ColoredIDEImages;
import de.ovgu.cide.af.AlternativeAnnotation;

/**
 * Ermöglicht Annotationen in Form von Markern auf den vertical ruler zu setzen.
 * 
 * @author Malte Rosenthal
 */
public class AnnotationMarkerAccess implements IAnnotationAccess, IAnnotationAccessExtension {
	
    public Object getType(Annotation annotation) {
        return (annotation ==  null) ? null : annotation.getType();
    }

    public boolean isMultiLine(Annotation annotation) {
        return true;
    }

    public boolean isTemporary(Annotation annotation) {
        return !annotation.isPersistent();
    }

    public String getTypeLabel(Annotation annotation) {
        if (annotation instanceof AlternativeAnnotation)
            return "Alternatives";

        return null;
    }

    public int getLayer(Annotation annotation) {
        if (annotation instanceof AlternativeAnnotation)
            return ((AlternativeAnnotation) annotation).getLayer();

        return 0;
    }

    public void paint(Annotation annotation, GC gc, Canvas canvas, Rectangle bounds) {
    	Image image = (annotation instanceof AlternativeAnnotation) ? ((AlternativeAnnotation) annotation).getImage() 
    																: ColoredIDEImages.getImage("sample.gif");
        ImageUtilities.drawImage(image, gc, canvas, bounds, SWT.CENTER, SWT.TOP);
    }

    public boolean isPaintable(Annotation annotation) {
        if (annotation instanceof AlternativeAnnotation)
            return (((AlternativeAnnotation) annotation).getImage() != null);

        return false;
    }

    public boolean isSubtype(Object annotationType, Object potentialSupertype) {
    	return annotationType.equals(potentialSupertype);
    }

    public Object[] getSupertypes(Object annotationType) {
        return new Object[0];
    }
}