package com.oakonell.chaotictactoe.ui.game;

import android.net.Uri;

import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.Player;

public class OnlineStrategy extends PlayerStrategy {

	public static Player createPlayer(String name, Marker marker,
			Uri iconImageUri) {
		Player player = new Player(name, iconImageUri, new OnlineStrategy(
				marker));
		return player;
	}

	public OnlineStrategy(Marker marker) {
		super(marker);
	}

}
