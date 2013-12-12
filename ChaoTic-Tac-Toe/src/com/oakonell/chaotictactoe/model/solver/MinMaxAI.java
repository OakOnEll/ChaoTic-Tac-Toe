package com.oakonell.chaotictactoe.model.solver;

import android.net.Uri;

import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;

public class MinMaxAI extends PlayerStrategy {
	private final MiniMaxAlg minmax;

	public MinMaxAI(String name, Marker player, int depth, MarkerChance chance) {
		super(name, player, getImageUri(depth));
		minmax = new MiniMaxAlg(player, depth, chance);
	}

	private static Uri getImageUri(int depth) {
		Uri parse = Uri.parse("android.resource://com.oakonell.chaotictactoe/"
				+ R.drawable.einstein);
		return parse;
	}

	@Override
	public boolean isAI() {
		return true;
	}

	public Cell move(Board board, Marker toPlay) {
		return minmax.solve(board, toPlay);
	}
}
