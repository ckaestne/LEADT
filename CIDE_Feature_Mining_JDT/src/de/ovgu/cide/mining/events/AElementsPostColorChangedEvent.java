package de.ovgu.cide.mining.events;

import java.util.EventObject;
import java.util.Set;

import cide.gast.IASTNode;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.model.AElement;

public class AElementsPostColorChangedEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private final String cuName;
	private final int cuHashCode;

	private final Set<ColorUpdate> addedColors;

	private final Set<ColorUpdate> removedColors;

	public static class ColorUpdate {
		public ColorUpdate(Set<IFeature> colors, Set<AElement> elements,
				IASTNode node) {
			this.colors = colors;
			this.elements = elements;
			this.node = node;
		}

		public final Set<IFeature> colors;
		public final Set<AElement> elements;
		public final IASTNode node;

	}

	public AElementsPostColorChangedEvent(Object source, String cuName,
			int cuHashCode, Set<ColorUpdate> addedColors,
			Set<ColorUpdate> removedColors) {
		super(source);

		this.cuName = cuName;
		this.cuHashCode = cuHashCode;
		this.addedColors = addedColors;
		this.removedColors = removedColors;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCuName() {
		return cuName;
	}

	public int getCuHashCode() {
		return cuHashCode;
	}

	public Set<ColorUpdate> getAddedColors() {
		return addedColors;
	}

	public Set<ColorUpdate> getRemovedColors() {
		return removedColors;
	}

}
