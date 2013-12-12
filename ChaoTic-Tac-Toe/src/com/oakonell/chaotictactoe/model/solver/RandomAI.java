package com.oakonell.chaotictactoe.model.solver;

import android.net.Uri;

import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;

public class RandomAI extends PlayerStrategy {

	public RandomAI(String name, Marker marker) {
		super(name, marker, Uri.parse("android.resource://com.oakonell.chaotictactoe/"
				+ R.drawable.dice));
	}

	@Override
	public boolean isAI() {
		return true;
	}

	public Cell move(Board board, Marker toPlay) {
		if (toPlay == Marker.EMPTY) {
			return pickMarkerToRemove(board);
		}
		int numEmpty = 0;
		int size = board.getSize();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Marker cell = board.getCell(x, y);
				if (cell == Marker.EMPTY) {
					numEmpty++;
				}
			}
		}
		int cellNum = MarkerChance.random.nextInt(numEmpty);
		int count = cellNum;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Marker cell = board.getCell(x, y);
				if (cell == Marker.EMPTY) {
					if (count == 0) {
						return new Cell(x, y);
					}
					count--;
				}
			}
		}
		throw new RuntimeException("Can't get here");
	}

	private Cell pickMarkerToRemove(Board board) {
		int numEmpty = 0;
		int size = board.getSize();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Marker cell = board.getCell(x, y);
				if (cell == Marker.EMPTY) {
					numEmpty++;
				}
			}
		}
		int cellNum = MarkerChance.random.nextInt(9 - numEmpty);
		int count = cellNum;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Marker cell = board.getCell(x, y);
				if (cell != Marker.EMPTY) {
					if (count == 0) {
						return new Cell(x, y);
					}
					count--;
				}
			}
		}
		throw new RuntimeException("Can't get here");
	}

}
