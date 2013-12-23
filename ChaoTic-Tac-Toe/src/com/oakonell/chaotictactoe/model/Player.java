package com.oakonell.chaotictactoe.model;

import com.oakonell.chaotictactoe.PlayerStrategy;

import android.net.Uri;

public class Player {

	private final String name;
	private final Uri iconImageUri;
	private Player opponent;
	private final PlayerStrategy strategy;

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

}
