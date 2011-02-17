package de.ovgu.cide.mining.events;

import java.util.EventObject;

import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.model.AElement;

public class ARecommenderElementSelectedEvent extends EventObject {

	private AElement element;

	public enum EVENT_TYPE {
		FEATURE, COMPILATION_UNIT, AST, ELEMENT
	}

	private final EVENT_TYPE type;
	private int cuHash, start, end;
	private IFeature color;

	public AElement getElement() {
		return element;
	}

	public ARecommenderElementSelectedEvent(Object source, IFeature color,
			AElement element) {
		super(source);
		this.type = EVENT_TYPE.ELEMENT;
		this.element = element;
		this.color = color;
	}

	public ARecommenderElementSelectedEvent(Object source, IFeature color,
			int cuHash) {
		this(source, EVENT_TYPE.COMPILATION_UNIT, color, -1, -1, cuHash);
	}

	public ARecommenderElementSelectedEvent(Object source, IFeature color) {
		this(source, EVENT_TYPE.FEATURE, color, -1, -1, -1);
	}

	public ARecommenderElementSelectedEvent(Object source, IFeature color,
			int start, int end, int cuHash) {
		this(source, EVENT_TYPE.AST, color, start, end, cuHash);
	}

	public ARecommenderElementSelectedEvent(Object source, EVENT_TYPE type,
			IFeature color, int start, int end, int cuHash) {
		super(source);
		this.type = type;
		this.start = start;
		this.end = end;
		this.cuHash = cuHash;
		this.color = color;
	}

	public EVENT_TYPE getType() {
		return type;
	}

	public IFeature getColor() {
		return color;
	}

	public int getCuHash() {
		return cuHash;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

}
