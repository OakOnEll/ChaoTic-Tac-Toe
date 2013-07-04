package com.oakonell.chaotictactoe;


public class HumanStrategy extends PlayerStrategy {

	public HumanStrategy(String playerName) {
		super(playerName);
	}

	@Override
	public boolean isHuman() {
		return true;
	}

}
