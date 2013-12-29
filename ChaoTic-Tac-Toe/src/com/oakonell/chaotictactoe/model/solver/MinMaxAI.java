package com.oakonell.chaotictactoe.model.solver;

import android.net.Uri;

import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.chaotictactoe.model.Player;
import com.oakonell.chaotictactoe.model.PlayerStrategy;

public class MinMaxAI extends PlayerStrategy {
	private MiniMaxAlg minmax;

	public static Player createPlayer(String oName, Marker marker, int aiDepth,
			MarkerChance chance) {
		MinMaxAI strategy = new MinMaxAI(marker);
		Player player = new Player(oName, getImageUri(aiDepth), strategy);
		strategy.setAlg(new MiniMaxAlg(player, aiDepth, chance));

		return player;
	}

	private MinMaxAI(Marker marker) {
		super(marker);
	}

	private void setAlg(MiniMaxAlg alg) {
		minmax = alg;
	}

	private static Uri getImageUri(int depth) {
		if (depth <= 1) {
			return Uri.parse("android.resource://com.oakonell.chaotictactoe/"
					+ R.drawable.dim_bulb_icon_122);
		} else if (depth == 2) {
			return Uri.parse("android.resource://com.oakonell.chaotictactoe/"
					+ R.drawable.light_bulb_icon_23392);
		} else
			return Uri.parse("android.resource://com.oakonell.chaotictactoe/"
					+ R.drawable.einstein_icon_16422);
	}

	@Override
	public boolean isAI() {
		return true;
	}

	public Cell move(Board board, Marker toPlay) {
		return minmax.solve(board, toPlay);
	}

}
