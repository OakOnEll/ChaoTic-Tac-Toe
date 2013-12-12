package com.oakonell.chaotictactoe.model;

import java.nio.ByteBuffer;
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

	public double getOpponentMarkerPercentage() {
		return ((double) opponentMarker) / total;
	}

	public double getMyMarkerPercentage() {
		return ((double) myMarker) / total;
	}

	public double getRemoveMarkerPercentage() {
		return ((double) removeMarker) / total;
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

	public boolean isNormal() {
		return myMarker == total;
	}

	public boolean isReverse() {
		return opponentMarker == total;
	}

	public int getTotal() {
		return total;
	}

	public boolean isChaotic() {
		return (myMarker == opponentMarker) && (opponentMarker == removeMarker);
	}

	public boolean isCustom() {
		return !isNormal() && !isReverse() && !isChaotic();
	}

	public int type() {
		if (isNormal()) {
			return 1;
		}
		if (isReverse()) {
			return 2;
		}
		if (isChaotic()) {
			return 3;
		}
		return 4;

	}

	public void writeToMsgBuffer(ByteBuffer buffer) {
		buffer.putInt(type());
		if (isCustom()) {
			buffer.putInt(myMarker);
			buffer.putInt(opponentMarker);
			buffer.putInt(removeMarker);
		}
	}

	public static MarkerChance fromMsgBuffer(ByteBuffer buffer) {
		int gameMode = buffer.getInt();
		if (gameMode == 1) {
			return NORMAL;
		}
		if (gameMode == 2) {
			return REVERSE;
		}
		if (gameMode == 3) {
			return CHAOTIC;
		}
		// custom, read the chances
		int mine = buffer.getInt();
		int opponent = buffer.getInt();
		int removal = buffer.getInt();
		return new MarkerChance(mine, opponent, removal);
	}

}
