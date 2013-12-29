package com.oakonell.chaotictactoe.ui.game;

import android.net.Uri;

import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.Player;
import com.oakonell.chaotictactoe.model.PlayerStrategy;


public class HumanStrategy extends PlayerStrategy {

	public static Player createPlayer(String name, Marker marker) {
		Player player = new Player(name, getImage(marker), new HumanStrategy(marker));
		return player;
	}
	
	public static Player createPlayer(String name, Marker marker, Uri iconImageUri) {
		Player player = new Player(name, iconImageUri, new HumanStrategy(marker));
		return player;
	}
	
	private HumanStrategy(Marker marker ) {
		super(marker);
	}

	public static Uri getImage(Marker marker) {
		if (marker == Marker.X) return Uri.parse("android.resource://com.oakonell.chaotictactoe/"
				+ R.drawable.system_cross);
		return Uri.parse("android.resource://com.oakonell.chaotictactoe/"
				+ R.drawable.system_dot);
	}

	@Override
	public boolean isHuman() {
		return true;
	}



}
