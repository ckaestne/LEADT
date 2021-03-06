package de.ovgu.cide.af;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;

import de.ovgu.cide.ColoredIDEImages;

/**
 * Annotation (not coloring, but a marker on the vertical ruler) that indicates the existance of alternative code-fragments.
 * 
 * @author Malte Rosenthal
 */
public class AlternativeAnnotation extends Annotation {
	public static String ALTERNATIVE_TYPE = "alternative.type";
	
	public AlternativeAnnotation(String text) {
		super(ALTERNATIVE_TYPE, false, text);
	}
	
	public Image getImage() {
        return ColoredIDEImages.getImage("sample.gif");
    }

    public int getLayer() {
        return 3;
    }
    
    public String getType() {
        return ALTERNATIVE_TYPE;
    }
}