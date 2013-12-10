package com.oakonell.chaotictactoe.model.solver;

import java.util.ArrayList;
import java.util.List;

import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.State;

public class MiniMaxABPruningAlg {
	private static final int infinite = Integer.MAX_VALUE;
	private final Marker player;
	private final int depth;

	private Cell move;

	MiniMaxABPruningAlg(Marker player, int depth) {
		this.player = player;
		if (depth < 0)
			throw new RuntimeException("Search-tree depth cannot be negative");
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public Cell solve(Board board, Marker toPlay) {
		Board copy = board.copy();
		if (toPlay != player) {
			throw new RuntimeException("Only works for normal play for now");
		}
		solve(copy, true, depth, -infinite, infinite, toPlay);
		return move;
	}

	private int solve(Board board, boolean needMax, int depth, int alpha,
			int beta, Marker toPlay) {
		State state = board.getState();
		Cell bestMove = null;
		if (state.isOver()) {
			// game is over
			Marker winner = state.getWinner();
			if (winner != null) {
				// someone won, give the heuristic score- allowing that multiple
				// wins in one move is better?
				return getHeuristicScore(board);
			}
			// game was a draw, score is 0
			return 0;
		}
		// reached the search depth
		if (depth == 0) {
			return getHeuristicScore(board);
		}

		List<Cell> moves = getValidMoves(board, toPlay);
		for (Cell move : moves) {
			board.placeMarker(move, toPlay);
			int value = solve(board, !needMax, depth - 1,  alpha,beta,
					toPlay.opponent());
			board.removeMarker(move, toPlay);

			if (!needMax) {
				if (beta > value) {
					beta = value;
					bestMove = move;
					if (alpha >= beta) {
						break;
					}
				}
			} else {
				if (alpha < value) {
					alpha = value;
					bestMove = move;
					if (alpha >= beta) {
						break;
					}
				}
			}

		}
		this.move = bestMove;
		return needMax ? alpha : beta;
	}

	private List<Cell> getValidMoves(Board board, Marker marker) {
		List<Cell> result = new ArrayList<Cell>();
		int size = board.getSize();
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				if (board.getCell(x, y) == Marker.EMPTY) {
					result.add(new Cell(x, y));
				}
			}
		}
		return result;
	}

	private int scoreLine(int numX, int numO) {
		if (player == Marker.X) {
			if (numX == 3) {
				return 1000;
			} else if (numO == 0) {
				if (numX == 2) {
					return 100;
				} else if (numX == 1) {
					return 10;
				} else {
					return 1;
				}
			}
			return 0;
		}

		if (numO == 3) {
			return 1000;
		} else if (numX == 0) {
			if (numO == 2) {
				return 100;
			} else if (numO == 1) {
				return 10;
			} else {
				return 1;
			}
		}
		return 0;

	}

	private int getHeuristicScore(Board board) {
		int size = board.getSize();
		int score = 0;

		// Inspect the columns
		for (int x = 0; x < size; ++x) {
			int numX = 0;
			int numO = 0;

			for (int y = 0; y < size; ++y) {
				Marker cell = board.getCell(x, y);
				if (cell == Marker.X) {
					numX++;
				}
				if (cell == Marker.O) {
					numO++;
				}
			}

			score += scoreLine(numX, numO);
		}

		// Inspect the rows
		for (int y = 0; y < size; ++y) {
			int numX = 0;
			int numO = 0;

			for (int x = 0; x < size; ++x) {
				Marker cell = board.getCell(x, y);
				if (cell == Marker.X) {
					numX++;
				}
				if (cell == Marker.O) {
					numO++;
				}
			}

			score += scoreLine(numX, numO);
		}

		// Inspect the top-left/bottom-right diagonal
		int numX = 0;
		int numO = 0;
		for (int x = 0; x < size; ++x) {
			Marker cell = board.getCell(x, x);
			if (cell == Marker.X) {
				numX++;
			}
			if (cell == Marker.O) {
				numO++;
			}
		}

		score += scoreLine(numX, numO);

		numX = 0;
		numO = 0;
		// Inspect the bottom-right/top-right
		for (int x = 0; x < size; ++x) {
			Marker cell = board.getCell(x, size - x - 1);
			if (cell == Marker.X) {
				numX++;
			}
			if (cell == Marker.O) {
				numO++;
			}
		}

		score += scoreLine(numX, numO);
		
		return score;
	}

}
