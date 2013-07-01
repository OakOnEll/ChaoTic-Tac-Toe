package com.oakonell.chaotictactoe;

import android.app.Application;

import com.oakonell.chaotictactoe.googleapi.GameHelper;

public class ChaoTicTacToe extends Application {
	private Achievements achievements = new Achievements();
	private GameHelper gameHelper;
	private RoomListener roomListener;

	public GameHelper getGameHelper() {
		return gameHelper;
	}

	public void setGameHelper(GameHelper helper) {
		this.gameHelper = helper;
	}

	public Achievements getAchievements() {
		return achievements;
	}

	public void setAchievements(Achievements achievements) {
		this.achievements = achievements;
	}

	public void setRoomListener(RoomListener roomListener) {
		this.roomListener = roomListener;		
	}

	public RoomListener getRoomListener() {
		return roomListener;
	}
	
	
}