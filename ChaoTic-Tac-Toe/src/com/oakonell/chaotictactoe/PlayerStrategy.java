package com.oakonell.chaotictactoe;

import android.net.Uri;

import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;

public class PlayerStrategy {
	private final String name;
	private final Marker marker;
	private final Uri iconImageUri;

	protected PlayerStrategy(String name, Marker marker, Uri iconImageUri) {
		this.name = name;
		this.marker = marker;
		this.iconImageUri = iconImageUri;
	}

	public boolean isHuman() {
		return false;
	}

	public String getName() {
		return name;
	}

	public boolean isAI() {
		return false;
	}

	public Marker getMarker() {
		return marker;
	}

	public Cell move(Board board, Marker toPlay) {
		return null;
	}

	public Uri getIconImageUri() {
		return iconImageUri;
	}

}
