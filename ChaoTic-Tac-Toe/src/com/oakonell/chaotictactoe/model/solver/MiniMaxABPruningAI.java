package com.oakonell.chaotictactoe.model.solver;




public class MiniMaxABPruningAI {
/*	private static final int infinite = Integer.MAX_VALUE;
	private int depth = 9;
	private Cell move;

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth)  {
		if (depth < 0)
			throw new RuntimeException("Search-tree depth cannot be negative");
		this.depth = depth;
	}
	
	
	

	public Cell solve(Board board, Marker player) {
		solve(board, true, depth, -infinite, infinite, player);
		return move;
	}

	private int solve(Board board, boolean isFirst, int depth, int alpha, int beta, Marker player) {
		State outcome = board.getState();
		if (depth ==0) {
			return outcome.getScore();
		}
		if (outcome.isOver()) {
			if (outcome.getWinner()!=null) {
				
			}
			return outcome.getScore();
		}
		
		boolean stop = false;

		int size = board.getSize();
		for (int x = 0; x < size && !stop; ++x) {
			for (int y = 0; y < size && !stop; ++y) {
				if (!board.isEmpty(x, y)) {
					continue;
				}
				
				Outcome outcome = board.placeMarker(new Cell(x,y), player);

				if (outcome.getWinner() != null) {
					// In a win situation weight the path with the least moves
					// higher to
					// prevent the player blocking a move instead of performing
					// a
					// winning move
					alpha = ((player == outcome.getWinner()) ? 1 : -1) * depth;
				} else if (depth == 0) {
					alpha = outcome.getScore();
				} else if (outcome.isDraw()) {
					alpha = 0;
				}

				int value = -solve(board, null, depth - 1, -beta, -alpha, player.opponent());
				// Undo the move
				board.removeMarker(new Cell(x, y), player);

				if (value > alpha) {
					alpha = value;

					if (cell != null) {
						// Update the cell only for the top-level call
						cell.setColumn(x);
						cell.setRow(y);
					}
				}

				if (alpha >= beta)
					stop = true;
			}
		}

		return alpha;
	}
*/
}
