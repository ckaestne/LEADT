package de.ovgu.cide.mining.events;

import java.util.EventObject;
import java.util.Map;
import java.util.Set;

import cide.gast.IASTNode;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.mining.database.model.AElement;

public class AElementsPostColorChangedEvent extends EventObject {


	private static final long serialVersionUID = 1L;

	private String cuName;
	private int cuHashCode;
	private Map<IASTNode, Set<IFeature>>  node2AddColors;
	private Map<IASTNode, Set<IFeature>>  node2RemoveColors;
	private Map<IASTNode, Set<AElement>>  node2elements;


	public AElementsPostColorChangedEvent(Object source, String cuName, int cuHashCode,  Map<IASTNode, Set<IFeature>>  node2AddColors, Map<IASTNode, Set<IFeature>>  node2RemoveColors, Map<IASTNode, Set<AElement>>  node2elements ) {
		super(source);
		
		this.cuName = cuName;
		this.cuHashCode = cuHashCode;
		this.node2AddColors = node2AddColors;
		this.node2RemoveColors = node2RemoveColors;
		this.node2elements = node2elements;
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

	public Map<IASTNode, Set<IFeature>> getNode2AddColors() {
		return node2AddColors;
	}

	public Map<IASTNode, Set<IFeature>> getNode2RemoveColors() {
		return node2RemoveColors;
	}

	public Map<IASTNode, Set<AElement>> getNode2elements() {
		return node2elements;
	}


}
