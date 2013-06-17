package com.oakonell.chaotictactoe.model;

public class InvalidMoveException extends RuntimeException{

	public InvalidMoveException(String string) {
		super(string);
	}

}
