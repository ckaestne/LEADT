package de.ovgu.cide.mining.nonfeaturemanager.model;

public class CUDummy {


	private String cuName;
	private int cuHashCode;
	
	public CUDummy(String name, int hashCode) {
		
		this.cuHashCode = hashCode;
		cuName = name;
	}
	
	public String getName() {
		return cuName;
	}

	public int getHashCode() {
		return cuHashCode;
	}


}

