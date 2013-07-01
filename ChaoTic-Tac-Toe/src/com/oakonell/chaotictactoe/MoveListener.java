package com.oakonell.chaotictactoe;

import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;

public interface MoveListener {

	void makeMove(Marker marker, Cell cell);

}
