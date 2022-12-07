package com.nttdata.bc39.grupo04.api.product.enumerator;

public enum TypeProductEnum {
	CUENTA_BANCARIA("CUENTA BANCARIA"), CREDITO("CREDITO");

	private String name;

	private TypeProductEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
