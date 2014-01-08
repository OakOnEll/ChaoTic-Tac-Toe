package com.oakonell.chaotictactoe.model;

import java.nio.ByteBuffer;
import java.util.Random;

import com.oakonell.chaotictactoe.R;

import android.content.Context;

public class MarkerChance {
	public static final MarkerChance CHAOTIC = new MarkerChance(1, 1, 1);
	public static final MarkerChance NORMAL = new MarkerChance(1, 0, 0);
	public static final MarkerChance REVERSE = new MarkerChance(0, 1, 0);

	public static final Random random = new Random();

	private final int myMarker;
	private final int opponentMarker;
	private final int removeMarker;

	private final int total;

	public MarkerChance(int myMarker, int opponentMarker, int removeMarker) {
		this.myMarker = myMarker;
		this.opponentMarker = opponentMarker;
		this.removeMarker = removeMarker;
		this.total = myMarker + opponentMarker + removeMarker;

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

	public Marker pickMove(Board board, Player player) {
		int chance;
		boolean isEmpty = board.isEmpty();
		if (isEmpty) {
			chance = getMyMarker() + getOpponentMarker();
		} else {
			chance = total;
		}

		int val = random.nextInt(chance);
		if (val < getMyMarker()) {
			return player.getMarker();
		}

		val -= getMyMarker();
		if (val < getOpponentMarker()) {
			return player.opponent().getMarker();
		}

		if (isEmpty) {
			throw new RuntimeException("Shouldn't get here!");
		}

		return Marker.EMPTY;
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

	public String getLabel(Context context) {
		if (isNormal()) {
			return context.getString(R.string.chance_normal);
		}
		if (isReverse()) {
			return context.getString(R.string.chance_reverse);
		}
		if (isChaotic()) {
			return context.getString(R.string.chance_chaotic);
		}
		return context.getString(R.string.chance_custom);
	}

	public String toString() {
		if (isNormal()) {
			return "normal";
		}
		if (isReverse()) {
			return "reverse";
		}
		if (isChaotic()) {
			return "chaotic";
		}

		return "custom: (my=" + myMarker + ", opponent=" + opponentMarker
				+ ", remove=" + removeMarker + ")";
	}
}
