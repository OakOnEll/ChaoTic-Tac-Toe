package com.oakonell.chaotictactoe.ui.game;

import android.net.Uri;

import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.model.Marker;

public class OnlineStrategy extends PlayerStrategy {

	public OnlineStrategy(String playerName, Marker marker, Uri uri) {
		super(playerName, marker, uri);
	}

}
