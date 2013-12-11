package com.oakonell.chaotictactoe.ui.game;

import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;

public interface MoveListener {

	void makeMove(Marker marker, Cell cell);

}
