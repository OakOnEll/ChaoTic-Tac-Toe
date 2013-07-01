package com.oakonell.chaotictactoe;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationBuffer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.OnInvitationsLoadedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.oakonell.chaotictactoe.googleapi.BaseGameActivity;
import com.oakonell.chaotictactoe.utils.Utils;

public class MainActivity extends BaseGameActivity {
	private String TAG = MainActivity.class.getName();

	// Request codes for the UIs that we show with startActivityForResult:
	final static int RC_UNUSED = 1;
	// online play request codes
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_INVITATION_INBOX = 10001;
	public final static int RC_WAITING_ROOM = 10002;


	private RoomListener roomListener;

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(request, response, data);
		switch (request) {
		case RC_SELECT_PLAYERS: {
			if (response == Activity.RESULT_OK) {
				handleSelectPlayer(data);
			} else {
				Toast.makeText(this, "Select players canceled",
						Toast.LENGTH_SHORT).show();
			}
		}
			break;
		case RC_WAITING_ROOM:
			// ignore result if we dismissed the waiting room from code:
			// if (mWaitRoomDismissedFromCode)
			// break;

			Toast.makeText(this, "Returned from waiting room",
					Toast.LENGTH_SHORT).show();
			// we got the result from the "waiting room" UI.
			if (response == Activity.RESULT_OK) {
				roomListener.backFromWaitingRoom();
			} else if (response == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
				// player actively indicated that they want to leave the room
				leaveRoom();
			} else if (response == Activity.RESULT_CANCELED) {
				/*
				 * Dialog was cancelled (user pressed back key, for instance).
				 * In our game, this means leaving the room too. In more
				 * elaborate games,this could mean something else (like
				 * minimizing the waiting room UI but continue in the handshake
				 * process).
				 */
				leaveRoom();
			}
			break;
		case RC_INVITATION_INBOX:
			if (response != Activity.RESULT_OK) {
				Toast.makeText(this, "Returned from invitation- NOT OK", Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(this, "Returned from invitation", Toast.LENGTH_SHORT).show();
			Invitation inv = data.getExtras().getParcelable(
					GamesClient.EXTRA_INVITATION);

			// accept invitation
			acceptInviteToRoom(inv.getInvitationId());
			break;
		}
	}

	void leaveRoom() {
		Log.d(TAG, "Leaving room.");
		if (roomListener != null) {
			roomListener.leaveRoom();
			roomListener = null;
		} else {
		}
	}

	private void handleSelectPlayer(Intent data) {
		final ArrayList<String> invitees = data
				.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
		Log.d(TAG, "Invitee count: " + invitees.size());

		StringBuilder stringBuilder = new StringBuilder();
		for (String each : invitees) {
			stringBuilder.append(each);
			stringBuilder.append(",\n");
		}

		// new AlertDialog.Builder(this).setTitle("Invited to play...")
		// .setMessage(stringBuilder.toString()).show();

		// get the automatch criteria
		Bundle autoMatchCriteria = null;
		int minAutoMatchPlayers = data.getIntExtra(
				GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
		int maxAutoMatchPlayers = data.getIntExtra(
				GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
		if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
					minAutoMatchPlayers, maxAutoMatchPlayers, 0);
			Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
		}

		roomListener = new RoomListener(this, getGameHelper());
		// create the room
		Log.d(TAG, "Creating room...");
		RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(roomListener);
		rtmConfigBuilder.addPlayersToInvite(invitees);
		rtmConfigBuilder.setMessageReceivedListener(roomListener);
		rtmConfigBuilder.setRoomStatusUpdateListener(roomListener);
		if (autoMatchCriteria != null) {
			rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		}
		getGamesClient().createRoom(rtmConfigBuilder.build());
		Log.d(TAG, "Room created, waiting for it to be ready...");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.enableStrictMode();
		setContentView(R.layout.activity_main);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		// ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Button newGameOnSameDevice = (Button) findViewById(R.id.new_game_same_device);
		newGameOnSameDevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// prompt for player names, and then in onDialogPositiveClick
				// callback, start the game activity
				Intent intent = new Intent(MainActivity.this,
						NewLocalGameDialog.class);
				startActivity(intent);
			}
		});

		Button viewAchievements = (Button) findViewById(R.id.view_achievements);
		viewAchievements.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSignedIn()) {
					startActivityForResult(getGamesClient()
							.getAchievementsIntent(), RC_UNUSED);
				} else {
					showAlert(getString(R.string.achievements_not_available));
				}
			}
		});

		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				beginUserInitiatedSignIn();
			}
		});

		Button signOutButton = (Button) findViewById(R.id.sign_out_button);
		signOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				signOut();

				// show login button
				findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
				// Sign-in failed, so show sign-in button on main menu
				findViewById(R.id.sign_out_bar).setVisibility(View.INVISIBLE);
			}
		});

		Button invite = (Button) findViewById(R.id.new_game_live);
		invite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = getGamesClient().getSelectPlayersIntent(1, 1);
				startActivityForResult(intent, RC_SELECT_PLAYERS);
			}
		});

		Button quick = (Button) findViewById(R.id.new_quick_play);
		quick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
				Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
						MIN_OPPONENTS, MAX_OPPONENTS, 0);
				roomListener = new RoomListener(MainActivity.this, getGameHelper());				
				RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(roomListener);
				rtmConfigBuilder.setMessageReceivedListener(roomListener);
				rtmConfigBuilder.setRoomStatusUpdateListener(roomListener);
				rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
				getGamesClient().createRoom(rtmConfigBuilder.build());
			}
		});

		
		Button viewInvites = (Button) findViewById(R.id.invites);
		viewInvites.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = getGamesClient().getInvitationInboxIntent();
				startActivityForResult(intent, RC_INVITATION_INBOX);
			}
		});

		
		setSignInMessages(getString(R.string.signing_in),
				getString(R.string.signing_out));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// switch (item.getItemId()) {
		// case R.id.action_settings:
		// Intent intent = new Intent(this, Preferences.class);
		// startActivity(intent);
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		RoomListener appListener = ((ChaoTicTacToe)getApplication()).getRoomListener();
		if (appListener != null) {
			appListener.clearActivity();
		}
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onSignInFailed() {
		// notify user with dialog?
		// show login button
		findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
		// Sign-in failed, so show sign-in button on main menu
		findViewById(R.id.sign_out_bar).setVisibility(View.INVISIBLE);

		// mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
		// mMainMenuFragment.setShowSignInButton(true);
		// mWinFragment.setShowSignInButton(true);
	}

	@Override
	public void onSignInSucceeded() {
		// store game helper in application
		// ((ChaoTicTacToe) getApplication()).setGameHelper(getGameHelper());
		// disable/hide login button
		findViewById(R.id.sign_in_bar).setVisibility(View.INVISIBLE);
		// show sign out button
		findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);

		// install invitation listener so we get notified if we receive an
		// invitation to play
		// a game.
		getGamesClient().registerInvitationListener(new OnInvitationReceivedListener() {
			@Override
			public void onInvitationReceived(Invitation invite) {
				refreshInvites();
				Toast.makeText(MainActivity.this, "Got an invite from " + invite.getParticipants().get(0).getDisplayName(), Toast.LENGTH_SHORT).show();				
			}
		});

		refreshInvites();

		// if we received an invite via notification, accept it; otherwise, go
		// to main screen
		if (getInvitationId() != null) {
			acceptInviteToRoom(getInvitationId());
			return;
		}

		
		/*
		 * // Set the greeting appropriately on main menu Player p =
		 * getGamesClient().getCurrentPlayer(); String displayName; if (p ==
		 * null) { Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
		 * displayName = "???"; } else { displayName = p.getDisplayName(); }
		 * mMainMenuFragment.setGreeting("Hello, " + displayName);
		 * 
		 * 
		 * // if we have accomplishments to push, push them if
		 * (!mOutbox.isEmpty()) { pushAccomplishments(); Toast.makeText(this,
		 * getString(R.string.your_progress_will_be_uploaded),
		 * Toast.LENGTH_LONG).show(); }
		 */
	}

	private void refreshInvites() {
		getGamesClient().loadInvitations(new OnInvitationsLoadedListener() {
			@Override
			public void onInvitationsLoaded(int statusCode,
					InvitationBuffer buffer) {
				if (statusCode == GamesClient.STATUS_OK) {
					// TODO update the online invites button with the count and enable it
					int count = buffer.getCount();
					Toast.makeText(MainActivity.this, "Got " + count + " invitations", Toast.LENGTH_SHORT).show();
					Button button = (Button)findViewById(R.id.invites);
					button.setText(count + " invitation(s)");
				} else if (statusCode == GamesClient.STATUS_NETWORK_ERROR_STALE_DATA) {

				} else if (statusCode == GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED) {

				} else if (statusCode == GamesClient.STATUS_INTERNAL_ERROR) {

				}
			}
		});
	}
	// Accept the given invitation.
	void acceptInviteToRoom(String invId) {
		roomListener = new RoomListener(this, getGameHelper());
		// accept the invitation
		Log.d(TAG, "Accepting invitation: " + invId);
		RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(roomListener);
		roomConfigBuilder.setInvitationIdToAccept(invId)
				.setMessageReceivedListener(roomListener)
				.setRoomStatusUpdateListener(roomListener);
		getGamesClient().joinRoom(roomConfigBuilder.build());
	}
}
