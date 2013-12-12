package com.oakonell.chaotictactoe.test;

import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.State;
import com.oakonell.chaotictactoe.model.solver.MinMaxAI;

import junit.framework.TestCase;

public class MinMaxTest extends TestCase {
	public void testMinMax() {
		Board board = new Board(3);
		MinMaxAI aiX = new MinMaxAI("X", Marker.X, 5);
		MinMaxAI aiO = new MinMaxAI("O", Marker.O, 5);

		while (true) {
			Cell move = aiX.move(board, Marker.X);
			State result = board.placeMarker(move, Marker.X);
			printBoard(board);
			if (result.isOver())
				break;

			move = aiO.move(board, Marker.O);
			result = board.placeMarker(move, Marker.O);
			printBoard(board);
			if (result.isOver())
				break;
		}

	}

	private void printBoard(Board board) {
		for (int x=0; x< board.getSize(); x++) {
			if (x!=0) {
				System.out.println("-----");
			}
			StringBuilder builder = new StringBuilder();
			for (int y=0; y < board.getSize(); y++) {
				if (y!=0) {
					builder.append("|");
				}
				Marker cell = board.getCell(x, y);
				if (cell == Marker.X) {
					builder.append("X");
				} else if (cell == Marker.O) {
					builder.append("O");
				} else {
					builder.append(" ");
				}
			}
			System.out.println(builder.toString());			
		}
		System.out.println();
	}
}
