package com.oakonell.chaotictactoe.model;

import java.util.ArrayList;
import java.util.List;

import com.oakonell.chaotictactoe.model.State.Win;
import com.oakonell.chaotictactoe.ui.game.WinOverlayView.WinStyle;

public class Board {
	private final int size;
	private final Marker[][] board;
	private State state = State.open(null);

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

	public State placeMarker(Cell cell, Player player, Marker mark) {
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
		Move move = new Move(player, mark, cell, Marker.EMPTY);
		return evaluateAndStore(move);
	}

	public State clearMarker(Cell cell, Player player) {
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
		Marker previousMarker = board[cell.x][cell.y];
		board[cell.x][cell.y] = Marker.EMPTY;

		// removing a marker shouldn't expose a win?
		Move move = new Move(player, Marker.EMPTY, cell, previousMarker);
		return evaluateAndStore(move);

	}

	private State evaluateAndStore(Move move) {
		state = evaluate(move);
		return state;
	}

	public State removeMarker(Cell cell, Player player) {
		if (state.isOver()) {
			throw new IllegalArgumentException(
					"Game is already over, unable to make move");
		}
		return clearMarker(cell, player);
	}

	private State evaluate(Move move) {
		Player player = move.getPlayer();
		List<Win> wins = new ArrayList<Win>();
		// Inspect the columns
		for (int x = 0; x < size; ++x) {
			int sum = 0;

			for (int y = 0; y < size; ++y) {
				sum += board[x][y].getVal();
			}

			int score = sum;
			if (Math.abs(score) == size) {
				wins.add(new Win(new Cell(x, 0), new Cell(x, size - 1),
						WinStyle.row(x)));
			}
		}

		// Inspect the rows
		for (int y = 0; y < size; ++y) {
			int sum = 0;

			for (int x = 0; x < size; ++x) {
				sum += board[x][y].getVal();
			}

			int score = sum;
			if (Math.abs(score) == size) {
				wins.add(new Win(new Cell(0, y), new Cell(size - 1, y),
						WinStyle.column(y)));
			}
		}

		int sum = 0;

		// Inspect the top-left/bottom-right diagonal
		for (int x = 0; x < size; ++x) {
			sum += board[x][x].getVal();
		}

		int score = sum;
		if (Math.abs(score) == size) {
			wins.add(new Win(new Cell(0, 0), new Cell(size - 1, size - 1),
					WinStyle.TOP_LEFT_DIAG));
		}

		sum = 0;
		// Inspect the bottom-right/top-right
		for (int x = 0; x < size; ++x) {
			sum += board[x][size - x - 1].getVal();
		}

		score = sum;
		if (Math.abs(score) == size) {
			wins.add(new Win(new Cell(0, size - 1), new Cell(size - 1, 0),
					WinStyle.TOP_RIGHT_DIAG));
		}
		if (!wins.isEmpty()) {
			Player winner;
			if (player.getMarker() == move.getPlayedMarker()) {
				winner = player;
			} else {
				winner = player.opponent();
			}
			return State.winner(move, wins, winner);
		}

		// check for draw- no empty spots
		sum = 0;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (board[x][y] == Marker.EMPTY)
					sum++;
			}
		}
		if (sum == 0) {
			return State.draw(move);
		}

		return State.open(move);
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

	public boolean isFull() {
		int sum = 0;
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (board[x][y] == Marker.EMPTY)
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
					copy.board[x][y] = marker;
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
