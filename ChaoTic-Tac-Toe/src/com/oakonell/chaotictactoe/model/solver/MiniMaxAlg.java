package com.oakonell.chaotictactoe.model.solver;

import java.util.ArrayList;
import java.util.List;

import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.State;

public class MiniMaxAlg {
	private final Marker player;
	private final int depth;

	public MiniMaxAlg(Marker player, int depth) {
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
		MoveAndScore solve = solve(copy, depth, toPlay);
		if (solve.move == null) {
			throw new RuntimeException("Move should not be null!");
		}
		return solve.move;
	}

	private MoveAndScore solve(Board board, int depth, Marker toPlay) {
		State state = board.getState();
		if (state.isOver()) {
			// how can moves be empty is state is not over?!
			// game is over
			Marker winner = state.getWinner();
			if (winner != null) {
				// someone won, give the heuristic score
				return new MoveAndScore(null, getHeuristicScore(board));
			}
			// game was a draw, score is 0
			return new MoveAndScore(null, 0);
		}
		// reached the search depth
		if (depth == 0) {
			return new MoveAndScore(null, getHeuristicScore(board));
		}

		Cell bestMove = null;
		int bestScore = (player == toPlay) ? Integer.MIN_VALUE
				: Integer.MAX_VALUE;
		int currentScore;

		List<Cell> moves = getValidMoves(board, toPlay);
		for (Cell move : moves) {
			board.placeMarker(move, toPlay);
			if (toPlay == player) {
				currentScore = solve(board, depth - 1, toPlay.opponent()).score;
				if (currentScore > bestScore) {
					bestScore = currentScore;
					bestMove = move;
				}
			} else {
				currentScore = solve(board, depth - 1, toPlay.opponent()).score;
				if (currentScore < bestScore) {
					bestScore = currentScore;
					bestMove = move;
				}
			}
			board.clearMarker(move, toPlay);
		}
		if (bestMove == null) {
			throw new RuntimeException("best move is null!");
		}
		return new MoveAndScore(bestMove, bestScore);
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

	private int scoreLine(int numMine, int numOpponent) {
		if (numMine == 3) {
			return 1000;
		}
		if (numOpponent == 3) {
			return -1000;
		}

		if (numMine == 2 && numOpponent == 0)
			return 100;
		if (numMine == 1 && numOpponent == 0)
			return 10;

		if (numMine == 0 && numOpponent == 2)
			return -100;
		if (numMine == 0 && numOpponent == 1)
			return -10;

		return 0;
	}

	private int getHeuristicScore(Board board) {
		int size = board.getSize();
		int score = 0;
		Marker opponent = player.opponent();

		// Inspect the columns
		for (int x = 0; x < size; ++x) {
			int numMine = 0;
			int numOpponent = 0;

			for (int y = 0; y < size; ++y) {
				Marker cell = board.getCell(x, y);
				if (cell == player) {
					numMine++;
				}
				if (cell == opponent) {
					numOpponent++;
				}
			}

			score += scoreLine(numMine, numOpponent);
		}

		// Inspect the rows
		for (int y = 0; y < size; ++y) {
			int numMine = 0;
			int numOpponent = 0;

			for (int x = 0; x < size; ++x) {
				Marker cell = board.getCell(x, y);
				if (cell == player) {
					numMine++;
				}
				if (cell == opponent) {
					numOpponent++;
				}
			}

			score += scoreLine(numMine, numOpponent);
		}

		// Inspect the top-left/bottom-right diagonal
		int numMine = 0;
		int numOpponent = 0;
		for (int x = 0; x < size; ++x) {
			Marker cell = board.getCell(x, x);
			if (cell == player) {
				numMine++;
			}
			if (cell == opponent) {
				numOpponent++;
			}
		}

		score += scoreLine(numMine, numOpponent);

		numMine = 0;
		numOpponent = 0;
		// Inspect the bottom-right/top-right
		for (int x = 0; x < size; ++x) {
			Marker cell = board.getCell(x, size - x - 1);
			if (cell == player) {
				numMine++;
			}
			if (cell == opponent) {
				numOpponent++;
			}
		}

		score += scoreLine(numMine, numOpponent);

		return score;
	}

	public static class MoveAndScore {
		Cell move;
		int score;

		MoveAndScore(Cell move, int score) {
			this.score = score;
			this.move = move;
		}
	}

}
