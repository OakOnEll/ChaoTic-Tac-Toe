package com.oakonell.chaotictactoe;

public class PlayerStrategy {
	private String name;

	protected PlayerStrategy(String name) {
		this.name = name;
	}

	public boolean isHuman() {
		return false;
	}

	public String getName() {
		return name;
	}

}
