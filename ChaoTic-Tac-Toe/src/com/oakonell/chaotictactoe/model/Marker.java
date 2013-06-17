package com.oakonell.chaotictactoe.model;

public enum Marker {
	X(1),O(-1),EMPTY(0);
	
	private final int val;
	
	private Marker(int val) {
		this.val = val;
	}
	
	public int getVal(){
		return val;
	}
	
	public Marker opponent() {
		if (this == X) return O;
		if (this == O) return X;
		return EMPTY;		
	}
}