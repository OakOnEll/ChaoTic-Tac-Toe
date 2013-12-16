package com.oakonell.chaotictactoe.model;

import com.oakonell.chaotictactoe.ui.game.WinOverlayView.WinStyle;

public class Board {
	private final int size;
	private final Marker[][] board;
	private State state = State.open(null, null, null, 0);

	public Board(int size) {
		this.size = size;
		board = new Marker[size][size];
		initializeBoard();
	}

	private void initializeBoard() {
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				board[x][y] = Marker.EMPTY;
			}
		}
	}

	public Marker getCell(int x, int y) {
		if (x < 0 || x >= size) {
			throw new IllegalArgumentException("x value (" + x
					+ " invalid, should be between [0," + size + "]");
		}
		if (y < 0 || y >= size) {
			throw new IllegalArgumentException("y value (" + y
					+ " invalid, should be between [0," + size + "]");
		}
		return board[x][y];
	}

	public State placeMarker(Cell cell, Marker mark) {
		if (state.isOver()) {
			throw new IllegalArgumentException(
					"Game is already over, unable to make move");
		}
		if (cell.x < 0 || cell.x >= size) {
			throw new IllegalArgumentException("x value (" + cell.x
					+ " invalid, should be between [0," + size + "]");
		}
		if (cell.y < 0 || cell.y >= size) {
			throw new IllegalArgumentException("y value (" + cell.y
					+ " invalid, should be between [0," + size + "]");
		}
		Marker existing = board[cell.x][cell.y];
		if (existing != Marker.EMPTY) {
			throw new InvalidMoveException("Cell " + cell.x + ", " + cell.y
					+ " already has a Marker " + existing
					+ ", unable to place a " + mark + " there");
		}
		board[cell.x][cell.y] = mark;
		return evaluateAndStore(mark);
	}

	public State clearMarker(Cell cell, Marker mark) {
		if (cell.x < 0 || cell.x >= size) {
			throw new IllegalArgumentException("x value (" + cell.x
					+ " invalid, should be between [0," + size + "]");
		}
		if (cell.y < 0 || cell.y >= size) {
			throw new IllegalArgumentException("y value (" + cell.y
					+ " invalid, should be between [0," + size + "]");
		}
		Marker existing = board[cell.x][cell.y];
		if (existing == Marker.EMPTY) {
			throw new InvalidMoveException("Cell " + cell.x + ", " + cell.y
					+ " does not have a Marker, unable to remove");
		}
		board[cell.x][cell.y] = Marker.EMPTY;

		// removing a marker shouldn't expose a win?
		return evaluateAndStore(mark);

	}

	private State evaluateAndStore(Marker mark) {
		state = evaluate(mark);
		return state;
	}

	public State removeMarker(Cell cell, Marker mark) {
		if (state.isOver()) {
			throw new IllegalArgumentException(
					"Game is already over, unable to make move");
		}
		return clearMarker(cell, mark);
	}

	private State evaluate(Marker mark) {
		int multiplyer = mark.getVal() == 1 ? 1 : -1;
		State result = null;
		// Inspect the columns
		for (int x = 0; x < size; ++x) {
			int sum = 0;

			for (int y = 0; y < size; ++y) {
				sum += board[x][y].getVal();
			}

			int score = multiplyer * sum;
			result = createResult(new Cell(x, 0), new Cell(x, size - 1), mark,
					score, result, WinStyle.row(x));
			if (result.getWinner() != null)
				return result;
		}

		// Inspect the rows
		for (int y = 0; y < size; ++y) {
			int sum = 0;

			for (int x = 0; x < size; ++x) {
				sum += board[x][y].getVal();
			}

			int score = multiplyer * sum;
			result = createResult(new Cell(0, y), new Cell(size - 1, y), mark,
					score, result, WinStyle.column(y));
			if (result.getWinner() != null)
				return result;
		}

		int sum = 0;

		// Inspect the top-left/bottom-right diagonal
		for (int x = 0; x < size; ++x) {
			sum += board[x][x].getVal();
		}

		int score = multiplyer * sum;
		result = createResult(new Cell(0, 0), new Cell(size - 1, size - 1),
				mark, score, result, WinStyle.TOP_LEFT_DIAG);
		if (result.getWinner() != null)
			return result;

		sum = 0;
		// Inspect the bottom-right/top-right
		for (int x = 0; x < size; ++x) {
			sum += board[x][size - x - 1].getVal();
		}

		score = multiplyer * sum;
		result = createResult(new Cell(0, size - 1), new Cell(size - 1, 0),
				mark, score, result, WinStyle.TOP_RIGHT_DIAG);
		if (result.getWinner() != null)
			return result;

		// check for draw- no empty spots
		sum = 0;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (board[x][y] == Marker.EMPTY)
					sum++;
			}
		}
		if (sum == 0) {
			return State.draw(mark);
		}

		return result;
	}

	private State createResult(Cell cell, Cell cell2, Marker mark, int score,
			State currentBest, WinStyle winStyle) {
		if (Math.abs(score) == size) {
			if (mark != board[cell.x][cell.y]) {
				// player made opponent win?
				// throw new RuntimeException("You made the opponent win?!");
			}
			return State.winner(cell, cell2, board[cell.x][cell.y], score,
					winStyle);
		}
		if (currentBest == null) {
			return State.open(cell, cell2, mark, score);
		}
		if (Math.abs(score) > Math.abs(currentBest.getScore())) {
			return State.open(cell, cell2, mark, score);
		}
		return currentBest;
	}

	public int getSize() {
		return size;
	}

	public State getState() {
		return state;
	}

	public boolean isEmpty() {
		int sum = 0;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (board[x][y] != Marker.EMPTY)
					sum++;
			}
		}
		return sum == 0;
	}

	public Board copy() {
		// return this;
		Board copy = new Board(size);
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				Marker marker = board[x][y];
				if (marker != Marker.EMPTY) {
					copy.placeMarker(new Cell(x, y), marker);
				}
			}
		}
		return copy;
	}

	public long getBoardStateAsLong() {
		StringBuilder builder = new StringBuilder();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				String squareChar = "0";
				if (board[x][y] == Marker.X) {
					squareChar = "1";
				} else if (board[x][y] == Marker.O) {
					squareChar = "2";
				}
				builder.append(squareChar);
			}
		}
		return Long.parseLong(builder.toString());

	}

}
