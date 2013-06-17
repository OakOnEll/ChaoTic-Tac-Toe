package com.oakonell.chaotictactoe.model;

import java.util.Random;

import android.content.Intent;

public class MarkerChance {
	public static final String MY_CHANCE_KEY = "myChance";
	public static final String OPPONENT_CHANCE_KEY = "opponentChance";
	public static final String REMOVE_CHANCE_KEY = "removeChance";

	public static final MarkerChance CHAOTIC = new MarkerChance(1, 1, 1);
	public static final MarkerChance NORMAL = new MarkerChance(1, 0, 0);
	public static final MarkerChance REVERSE = new MarkerChance(0, 1, 0);

	public static final Random random = new Random();

	private int myMarker;
	private int opponentMarker;
	private int removeMarker;

	// TODO differentiate between remove myMarker/opponentMarker
	// will require some structural changes

	private int total;

	public MarkerChance(int myMarker, int opponentMarker, int removeMarker) {
		super();
		this.myMarker = myMarker;
		this.opponentMarker = opponentMarker;
		this.removeMarker = removeMarker;
		total = myMarker + opponentMarker + removeMarker;

	}

	public int getMyMarker() {
		return myMarker;
	}

	public int getOpponentMarker() {
		return opponentMarker;
	}

	public int getRemoveMarker() {
		return removeMarker;
	}

	public Marker pickMove(Board board, Marker player) {
		int chance;
		boolean isEmpty = board.isEmpty();
		if (isEmpty) {
			chance = getMyMarker() + getOpponentMarker();
		} else {
			chance = total;
		}

		int val = random.nextInt(chance);
		if (val < getMyMarker()) {
			return player;
		}

		val -= getMyMarker();
		if (val < getOpponentMarker()) {
			return player.opponent();
		}

		if (isEmpty) {
			throw new RuntimeException("Shouldn't get here!");
		}

		return Marker.EMPTY;
	}

	public void putIntentExtras(Intent intent) {
		intent.putExtra(MY_CHANCE_KEY, getMyMarker());
		intent.putExtra(OPPONENT_CHANCE_KEY, getOpponentMarker());
		intent.putExtra(REMOVE_CHANCE_KEY, getRemoveMarker());
	}

	public static MarkerChance fromIntentExtras(Intent intent) {
		int myChance = intent.getIntExtra(MY_CHANCE_KEY, 1);
		int opponentChance = intent.getIntExtra(OPPONENT_CHANCE_KEY, 1);
		int removeChance = intent.getIntExtra(REMOVE_CHANCE_KEY, 1);
		return new MarkerChance(myChance, opponentChance, removeChance);
	}

}
