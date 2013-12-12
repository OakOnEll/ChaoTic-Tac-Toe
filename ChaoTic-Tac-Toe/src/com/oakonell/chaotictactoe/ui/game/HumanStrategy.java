package com.oakonell.chaotictactoe.ui.game;

import android.net.Uri;

import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Marker;


public class HumanStrategy extends PlayerStrategy {

	public HumanStrategy(String playerName, Marker marker ) {
		super(playerName,marker, getImage(marker));
	}

	private static Uri getImage(Marker marker) {
		if (marker == Marker.X) return Uri.parse("android.resource://com.oakonell.chaotictactoe/"
				+ R.drawable.system_cross_faded);
		return Uri.parse("android.resource://com.oakonell.chaotictactoe/"
				+ R.drawable.system_dot_faded);
	}

	public HumanStrategy(String string, Marker x, Uri iconImageUri) {
		super(string, x, iconImageUri);
	}

	@Override
	public boolean isHuman() {
		return true;
	}

}
