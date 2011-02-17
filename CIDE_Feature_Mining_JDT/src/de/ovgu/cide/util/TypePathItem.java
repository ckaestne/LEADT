package de.ovgu.cide.util;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class TypePathItem {

	final private String key;
	final private boolean isInterface;
	private final ITypeBinding binding;

	public TypePathItem(ITypeBinding binding, boolean isInterface) {
		this.binding = binding;
		this.isInterface = isInterface;

		key = binding.getKey();

	}

	public ITypeBinding getBinding() {
		return binding;

	}

	public String getKey() {
		return key;
	}

	public boolean isInterface() {
		return isInterface;
	}

}
