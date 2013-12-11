package com.oakonell.chaotictactoe.ui.game;

import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.model.Marker;


public class HumanStrategy extends PlayerStrategy {

	public HumanStrategy(String playerName, Marker marker ) {
		super(playerName,marker );
	}

	@Override
	public boolean isHuman() {
		return true;
	}

}
