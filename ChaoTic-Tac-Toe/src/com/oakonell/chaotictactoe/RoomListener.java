package com.oakonell.chaotictactoe;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeReliableMessageSentListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.oakonell.chaotictactoe.googleapi.GameHelper;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.chaotictactoe.model.ScoreCard;
import com.oakonell.chaotictactoe.ui.game.GameFragment;
import com.oakonell.chaotictactoe.ui.game.HumanStrategy;
import com.oakonell.chaotictactoe.ui.game.OnlineStrategy;
import com.oakonell.chaotictactoe.ui.menu.MenuFragment;

public class RoomListener implements RoomUpdateListener,
		RealTimeMessageReceivedListener, RoomStatusUpdateListener {
	private static final Random random = new Random();
	private static final String TAG = RoomListener.class.getName();

	private MainActivity activity;
	private GameHelper helper;

	private String mRoomId;
	private ArrayList<Participant> mParticipants;
	private String mMyParticipantId;

	private static final byte MSG_WHO_IS_X = 1;
	private static final byte MSG_MOVE = 2;
	private static final byte MSG_MESSAGE = 3;
	private static final byte MSG_SEND_CHANCE = 4;

	private volatile Long myRandom;
	private volatile Long theirRandom;
	private MarkerChance chance;

	private boolean isQuick;

	GamesClient getGamesClient() {
		return helper.getGamesClient();
	}

	public RoomListener(MainActivity activity, GameHelper helper,
			MarkerChance chance, boolean isQuick) {
		this.activity = activity;
		this.helper = helper;
		this.chance = chance;
		this.isQuick = isQuick;
	}

	// Called when we are connected to the room. We're not ready to play yet!
	// (maybe not everybody
	// is connected yet).
	@Override
	public void onConnectedToRoom(Room room) {
		announce("onConnectedToRoom");

		// get room ID, participants and my ID:
		mRoomId = room.getRoomId();
		mParticipants = room.getParticipants();
		mMyParticipantId = room.getParticipantId(getGamesClient()
				.getCurrentPlayerId());

		// print out the list of participants (for debug purposes)
		Log.d(TAG, "Room ID: " + mRoomId);
		Log.d(TAG, "My ID " + mMyParticipantId);
		Log.d(TAG, "<< CONNECTED TO ROOM>>");
	}

	// Called when we get disconnected from the room. We return to the main
	// screen.
	@Override
	public void onDisconnectedFromRoom(Room arg0) {
		announce("onDisconnectedFromRoom");

		// TODO pop if the current is game
		activity.getSupportFragmentManager().popBackStack();
	}

	// We treat most of the room update callbacks in the same way: we update our
	// list of
	// participants and update the display. In a real game we would also have to
	// check if that
	// change requires some action like removing the corresponding player avatar
	// from the screen,
	// etc.
	@Override
	public void onPeerDeclined(Room room, List<String> arg1) {
		announce("onPeerDeclined");
		updateRoom(room);
	}

	@Override
	public void onPeerInvitedToRoom(Room room, List<String> arg1) {
		announce("onPeerInvitedToRoom");
		updateRoom(room);
	}

	@Override
	public void onPeerJoined(Room room, List<String> arg1) {
		announce("onPeerJoined");
		updateRoom(room);
	}

	@Override
	public void onPeerLeft(Room room, List<String> peersWhoLeft) {
		String message = activity.getResources().getString(
				R.string.peer_left_the_game, getOpponentName());
		(new AlertDialog.Builder(activity)).setMessage(message)
				.setNeutralButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.possiblyShowInterstitialAd();
						dialog.dismiss();
					}
				}).create().show();

		announce("onPeerLeft");
		updateRoom(room);
	}

	@Override
	public void onRoomAutoMatching(Room room) {
		announce("onPeerRoomAutoMatching");
		updateRoom(room);
	}

	@Override
	public void onRoomConnecting(Room room) {
		announce("onRoomConnecting");
		updateRoom(room);
	}

	@Override
	public void onPeersConnected(Room room, List<String> peers) {
		announce("onPeersConnected");
		updateRoom(room);
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> peers) {
		announce("onPeerDisconnected");
		updateRoom(room);
	}

	@Override
	public void onP2PConnected(String arg0) {
		announce("Connected to P2P " + arg0);
	}

	@Override
	public void onP2PDisconnected(String arg0) {
		announce("Disconnected from P2P " + arg0);
	}

	void updateRoom(Room room) {
		mParticipants = room.getParticipants();
	}

	// Called when we receive a real-time message from the network.
	@Override
	public void onRealTimeMessageReceived(RealTimeMessage message) {
		byte[] messageData = message.getMessageData();
		ByteBuffer buffer = ByteBuffer.wrap(messageData);
		byte type = buffer.get();
		if (type == MSG_WHO_IS_X) {
			theirRandom = buffer.getLong();
			if (myRandom == null) {
				// ignore, let the later backFromWaitingRoom receiver handle
				// this
				return;
			}
			checkWhoIsFirstAndAttemptToStart(false);
		} else if (type == MSG_MOVE) {
			Marker marker = Marker.fromInt(buffer.getInt());
			int x = buffer.getInt();
			int y = buffer.getInt();
			// TODO is it possible that the moveListener is null?
			// should we store the pending move, until a move listener is set
			activity.onlineMoveReceived(marker, new Cell(x, y));
		} else if (type == MSG_MESSAGE) {
			int numBytes = buffer.getInt();
			byte[] bytes = new byte[numBytes];
			buffer.get(bytes);
			String string;
			try {
				string = new String(bytes, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("UTF-8 charset not present!?");
			}

			activity.messageRecieved(getOpponentParticipant(), string);
		} else if (type == MSG_SEND_CHANCE) {
			MarkerChance sentChance = MarkerChance.fromMsgBuffer(buffer);
			if (chance != null) {
				// verify that the chances agree
				if (chance.getMyMarker() != sentChance.getMyMarker()
						|| chance.getOpponentMarker() != sentChance
								.getOpponentMarker()
						|| chance.getRemoveMarker() != sentChance
								.getRemoveMarker()) {
					throw new RuntimeException(
							"Opponent's chance setting does not match!");
				}
			} else {
				chance = sentChance;
				announce("Received chance");
			}
		} else {
			throw new RuntimeException("unexpected message type! " + type);
		}

	}

	private void startGame(boolean iAmX) {
		// TODO if we have account permission, can get account name
		GameFragment gameFragment = new GameFragment();
		gameFragment.setMode(GameMode.ONLINE);
		Game game = new Game(3, Marker.X, chance);
		ScoreCard score = new ScoreCard(0, 0, 0);
		PlayerStrategy xStrategy;
		PlayerStrategy oStrategy;
		if (iAmX) {
			xStrategy = new HumanStrategy("You", Marker.X, getMe()
					.getIconImageUri());
			oStrategy = new OnlineStrategy(getOpponentName(), Marker.O,
					getOpponentParticipant().getIconImageUri());
		} else {
			xStrategy = new OnlineStrategy(getOpponentName(), Marker.X,
					getOpponentParticipant().getIconImageUri());
			oStrategy = new HumanStrategy("You", Marker.O, getMe()
					.getIconImageUri());
		}
		gameFragment.startGame(xStrategy, oStrategy, game, score);
		FragmentManager manager = activity.getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.main_frame, gameFragment, "game");
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		announce("onJoinedRoom");
		if (statusCode != GamesClient.STATUS_OK) {
			Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
			showGameError();
			return;
		}

		// show the waiting room UI
		showWaitingRoom(room);
	}

	// Called when we've successfully left the room (this happens a result of
	// voluntarily leaving
	// via a call to leaveRoom(). If we get disconnected, we get
	// onDisconnectedFromRoom()).
	@Override
	public void onLeftRoom(int arg0, String arg1) {
		announce("onLeftRoom");
		// TODO pop if the current is game
		activity.getSupportFragmentManager().popBackStack();
	}

	// Called when room is fully connected.
	@Override
	public void onRoomConnected(int statusCode, Room room) {
		announce("onRoomConnected");

		if (statusCode != GamesClient.STATUS_OK) {
			Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
			showGameError();
			return;
		}
		updateRoom(room);
	}

	// Called when room has been created
	@Override
	public void onRoomCreated(int statusCode, Room room) {
		announce("onRoomCreated");

		if (statusCode != GamesClient.STATUS_OK) {
			Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
			showGameError();
			return;
		}

		// show the waiting room UI
		showWaitingRoom(room);
	}

	private void announce(String string) {
		Log.d(TAG, string);
	}

	private void showGameError() {
		// TODO Auto-generated method stub

	}

	// Show the waiting room UI to track the progress of other players as they
	// enter the
	// room and get connected.
	void showWaitingRoom(Room room) {
		// mWaitRoomDismissedFromCode = false;

		// minimum number of players required for our game
		int minPlayersToStart = 2;
		Intent intent = getGamesClient().getRealTimeWaitingRoomIntent(room,
				minPlayersToStart);

		// show waiting room UI
		activity.startActivityForResult(intent, MenuFragment.RC_WAITING_ROOM);
	}

	public void leaveRoom() {
		if (mRoomId != null) {
			getGamesClient().leaveRoom(this, mRoomId);
		}
	}

	public String getOpponentName() {
		if (isQuick) {
			return "Anonymous";
		}
		return getOpponentParticipant().getDisplayName();
	}

	private Participant getOpponentParticipant() {
		if (!mParticipants.get(0).getParticipantId().equals(mMyParticipantId)) {
			return mParticipants.get(0);
		}
		return mParticipants.get(1);
	}

	public String getOpponentId() {
		return getOpponentParticipant().getParticipantId();
	}

	public String getRoomId() {
		return mRoomId;
	}

	public void backFromWaitingRoom() {
		// player wants to start playing
		Log.d(TAG, "Starting game because user requested via waiting room UI.");

		// let other players know we're starting.
		// Toast.makeText(context, "Start the game!",
		// Toast.LENGTH_SHORT).show();

		if (chance != null) {
			ByteBuffer buffer = ByteBuffer
					.allocate(GamesClient.MAX_RELIABLE_MESSAGE_LEN);
			buffer.put(MSG_SEND_CHANCE);
			chance.writeToMsgBuffer(buffer);
			getGamesClient().sendReliableRealTimeMessage(
					new RealTimeReliableMessageSentListener() {
						@Override
						public void onRealTimeMessageSent(int statusCode,
								int token, String recipientParticipantId) {
							if (statusCode == GamesClient.STATUS_OK) {

							} else if (statusCode == GamesClient.STATUS_REAL_TIME_MESSAGE_SEND_FAILED) {

							} else if (statusCode == GamesClient.STATUS_REAL_TIME_ROOM_NOT_JOINED) {

							} else {

							}
						}
					}, buffer.array(), getRoomId(), getOpponentId());
		}

		myRandom = random.nextLong();
		checkWhoIsFirstAndAttemptToStart(true);
	}

	private void checkWhoIsFirstAndAttemptToStart(boolean send) {
		boolean start = false;
		boolean iAmX = true;
		if (theirRandom != null) {
			start = true;
			while (true) {
				// keep the move random seeds in sync, as a checksum (no
				// cheating!)
				if (myRandom < theirRandom) {
					// I'm X, they're O
					iAmX = true;
					MarkerChance.random.setSeed(myRandom);
					break;
				} else if (myRandom > theirRandom) {
					// I'm O, they're X
					iAmX = false;
					MarkerChance.random.setSeed(theirRandom);
					break;
				} else {
					// try again
					send = true;
					myRandom = random.nextLong();
					theirRandom = null;
				}
			}
		}
		if (send) {
			ByteBuffer buffer = ByteBuffer
					.allocate(GamesClient.MAX_RELIABLE_MESSAGE_LEN);
			buffer.put(MSG_WHO_IS_X);
			buffer.putLong(myRandom);
			getGamesClient().sendReliableRealTimeMessage(
					new RealTimeReliableMessageSentListener() {
						@Override
						public void onRealTimeMessageSent(int statusCode,
								int token, String recipientParticipantId) {
							if (statusCode == GamesClient.STATUS_OK) {

							} else if (statusCode == GamesClient.STATUS_REAL_TIME_MESSAGE_SEND_FAILED) {

							} else if (statusCode == GamesClient.STATUS_REAL_TIME_ROOM_NOT_JOINED) {

							} else {

							}
						}
					}, buffer.array(), getRoomId(), getOpponentId());
		}
		if (start && chance != null) {
			startGame(iAmX);
		}
	}

	public void sendMove(Marker marker, Cell cell) {
		ByteBuffer buffer = ByteBuffer
				.allocate(GamesClient.MAX_RELIABLE_MESSAGE_LEN);
		buffer.put(MSG_MOVE);
		buffer.putInt(marker.getVal());
		buffer.putInt(cell.getX());
		buffer.putInt(cell.getY());
		getGamesClient().sendReliableRealTimeMessage(
				new RealTimeReliableMessageSentListener() {
					@Override
					public void onRealTimeMessageSent(int statusCode,
							int token, String recipientParticipantId) {
						if (statusCode == GamesClient.STATUS_OK) {

						} else if (statusCode == GamesClient.STATUS_REAL_TIME_MESSAGE_SEND_FAILED) {
							showGameError();
						} else if (statusCode == GamesClient.STATUS_REAL_TIME_ROOM_NOT_JOINED) {
							showGameError();
						} else {
							showGameError();
						}
					}
				}, buffer.array(), getRoomId(), getOpponentId());
	}

	public void sendMessage(String string) {
		ByteBuffer buffer = ByteBuffer
				.allocate(GamesClient.MAX_RELIABLE_MESSAGE_LEN);
		buffer.put(MSG_MESSAGE);

		byte[] bytes;
		try {
			bytes = string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Unsupported UTF-8?!");
		}

		buffer.putInt(bytes.length);
		buffer.put(bytes);
		getGamesClient().sendReliableRealTimeMessage(
				new RealTimeReliableMessageSentListener() {
					@Override
					public void onRealTimeMessageSent(int statusCode,
							int token, String recipientParticipantId) {
						if (statusCode == GamesClient.STATUS_OK) {

						} else if (statusCode == GamesClient.STATUS_REAL_TIME_MESSAGE_SEND_FAILED) {
							showGameError();
						} else if (statusCode == GamesClient.STATUS_REAL_TIME_ROOM_NOT_JOINED) {
							showGameError();
						} else {
							showGameError();
						}
					}
				}, buffer.array(), getRoomId(), getOpponentId());

	}

	public Participant getMe() {
		if (mParticipants.get(0).getParticipantId().equals(mMyParticipantId)) {
			return mParticipants.get(0);
		}
		return mParticipants.get(1);
	}

}
