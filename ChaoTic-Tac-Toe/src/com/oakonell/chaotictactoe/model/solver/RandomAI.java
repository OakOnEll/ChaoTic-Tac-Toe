package com.oakonell.chaotictactoe.model.solver;

import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.chaotictactoe.model.PlayerStrategy;

public class RandomAI implements PlayerStrategy {	

	@Override
	public Cell move(Board board, Marker me, Marker toPlay) {
		int numEmpty = 0;
		int size = board.getSize();
		for (int x=0;x<size; x++) {
			for (int y=0;y<size;y++) {
				Marker cell = board.getCell(x, y);
				if (cell == Marker.EMPTY) {
					numEmpty++;
				}
			}
		}
		int cellNum = MarkerChance.random.nextInt(numEmpty);		
		// TODO convert to single access
		int count=0;
		for (int x=0;x<size; x++) {
			for (int y=0;y<size;y++) {
				if (count==0) {
					return new Cell(x, y);
				}
			}
		}
		throw new RuntimeException("Can't get here");
	}

}
