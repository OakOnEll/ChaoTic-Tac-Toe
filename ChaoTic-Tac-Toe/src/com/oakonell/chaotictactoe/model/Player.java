package com.oakonell.chaotictactoe.model;

import android.net.Uri;
import android.widget.ImageView;

import com.google.android.gms.common.images.ImageManager;
import com.oakonell.chaotictactoe.R;

public class Player {
	private final String name;
	private final Uri iconImageUri;
	private final PlayerStrategy strategy;
	private Player opponent;

	public Player(String name, Uri iconImageUri, PlayerStrategy strategy) {
		this.name = name;
		this.iconImageUri = iconImageUri;
		this.strategy = strategy;
	}

	public void setOpponent(Player opponent) {
		this.opponent = opponent;
	}

	public String getName() {
		return name;
	}

	public Uri getIconImageUri() {
		return iconImageUri;
	}

	public Marker getMarker() {
		return strategy.getMarker();
	}

	public Player opponent() {
		return opponent;
	}

	public PlayerStrategy getStrategy() {
		return strategy;
	}

	public void updatePlayerImage(ImageManager imgManager, ImageView xImage) {
		int defaultResource = strategy.getMarker() == Marker.X ? R.drawable.system_cross
				: R.drawable.system_dot;
		if (iconImageUri == null
				|| iconImageUri.getEncodedSchemeSpecificPart().contains(
						"gms.games")) {
			imgManager.loadImage(xImage, iconImageUri, defaultResource);
		} else {
			xImage.setImageURI(iconImageUri);

		}
	}

}
