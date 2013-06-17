package com.oakonell.chaotictactoe.model;

public interface PlayerStrategy {
	Cell move(Board board, Marker me, Marker toPlay);
}
