package com.oakonell.chaotictactoe.ui.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationBuffer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.OnInvitationsLoadedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.oakonell.chaotictactoe.ChaoTicTacToe;
import com.oakonell.chaotictactoe.MainActivity;
import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.RoomListener;
import com.oakonell.chaotictactoe.googleapi.GameHelper;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.chaotictactoe.model.ScoreCard;
import com.oakonell.chaotictactoe.model.solver.MinMaxAI;
import com.oakonell.chaotictactoe.model.solver.RandomAI;
import com.oakonell.chaotictactoe.settings.SettingsActivity;
import com.oakonell.chaotictactoe.ui.game.GameFragment;
import com.oakonell.chaotictactoe.ui.game.HumanStrategy;
import com.oakonell.chaotictactoe.ui.menu.NewAIGameDialog.LocalAIGameModeListener;
import com.oakonell.chaotictactoe.ui.menu.NewLocalGameDialog.LocalGameModeListener;
import com.oakonell.chaotictactoe.ui.menu.OnlineGameModeDialog.OnlineGameModeListener;
import com.oakonell.chaotictactoe.utils.DevelopmentUtil.Info;

public class MenuFragment extends SherlockFragment {
	private String TAG = MenuFragment.class.getName();

	// Request codes for the UIs that we show with startActivityForResult:
	final static int RC_UNUSED = 1;
	// online play request codes
	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_INVITATION_INBOX = 10001;
	public final static int RC_WAITING_ROOM = 10002;

	// public static final int RC_SELECT_ONLINE_INVITE_TYPE = 10005;

	private View signInView;
	private View signOutView;
	private Button invitesButton;

	private View playRelated;

	@Override
	public void onActivityResult(int request, int response, Intent data) {
		switch (request) {

		case RC_SELECT_PLAYERS: {
			if (response == Activity.RESULT_OK) {
				final ArrayList<String> invitees = data
						.getStringArrayListExtra(GamesClient.EXTRA_PLAYERS);
				int minAutoMatchPlayers = data.getIntExtra(
						GamesClient.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
				int maxAutoMatchPlayers = data.getIntExtra(
						GamesClient.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

				createOnlineRoom(invitees, minAutoMatchPlayers,
						maxAutoMatchPlayers);
			} else {
				Toast.makeText(getActivity(), "Select players canceled",
						Toast.LENGTH_SHORT).show();
			}
		}
			break;

		case RC_WAITING_ROOM:
			// ignore result if we dismissed the waiting room from code:
			// if (mWaitRoomDismissedFromCode)
			// break;

			Toast.makeText(getActivity(), "Returned from waiting room",
					Toast.LENGTH_SHORT).show();
			// we got the result from the "waiting room" UI.
			if (response == Activity.RESULT_OK) {
				getMainActivity().getRoomListener().backFromWaitingRoom();
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
				Toast.makeText(getActivity(),
						"Returned from invitation- NOT OK", Toast.LENGTH_SHORT)
						.show();
				return;
			}
			Toast.makeText(getActivity(), "Returned from invitation",
					Toast.LENGTH_SHORT).show();
			Invitation inv = data.getExtras().getParcelable(
					GamesClient.EXTRA_INVITATION);

			// accept invitation
			acceptInviteToRoom(inv.getInvitationId());
			break;
		}
		// super.onActivityResult(request, response, data);
	}

	void leaveRoom() {
		Log.d(TAG, "Leaving room.");
		RoomListener roomListener = getMainActivity().getRoomListener();
		if (roomListener != null) {
			roomListener.leaveRoom();
			getMainActivity().setRoomListener(null);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_menu, container,
				false);
		setHasOptionsMenu(true);

		signInView = view.findViewById(R.id.sign_in_bar);
		signOutView = view.findViewById(R.id.sign_out_bar);
		invitesButton = (Button) view.findViewById(R.id.invites);

		playRelated = view.findViewById(R.id.google_play_related);
		playRelated.setEnabled(false);

		Button newGameOnSameDevice = (Button) view
				.findViewById(R.id.new_game_same_device);
		newGameOnSameDevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectLocalGame();
			}

		});

		Button viewAchievements = (Button) view
				.findViewById(R.id.view_achievements);
		viewAchievements.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getMainActivity().isSignedIn()) {
					startActivityForResult(getMainActivity().getGamesClient()
							.getAchievementsIntent(), RC_UNUSED);
				} else {
					// TODO display pending achievements
					getMainActivity().showAlert(
							getString(R.string.achievements_not_available));
				}
			}
		});

		Button viewLeaderboards = (Button) view
				.findViewById(R.id.view_leaderboards);
		viewLeaderboards.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getMainActivity().isSignedIn()) {
					startActivityForResult(getMainActivity().getGamesClient()
							.getAllLeaderboardsIntent(), RC_UNUSED);
				} else {
					// TODO display pending achievements
					getMainActivity().showAlert(
							getString(R.string.achievements_not_available));
				}
			}
		});

		SignInButton signInButton = (SignInButton) view
				.findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMainActivity().beginUserInitiatedSignIn();
			}
		});

		Button signOutButton = (Button) view.findViewById(R.id.sign_out_button);
		signOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMainActivity().signOut();

				// show login button
				view.findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
				// Sign-in failed, so show sign-in button on main menu
				view.findViewById(R.id.sign_out_bar).setVisibility(
						View.INVISIBLE);
			}
		});

		Button invite = (Button) view.findViewById(R.id.new_game_live);
		invite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectOnlineGameMode();
			}
		});

		Button quick = (Button) view.findViewById(R.id.new_quick_play);
		quick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectQuickMode();
			}
		});

		invitesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = getMainActivity().getGamesClient()
						.getInvitationInboxIntent();
				startActivityForResult(intent, RC_INVITATION_INBOX);
			}
		});

		Button ai = (Button) view.findViewById(R.id.new_game_vs_ai);
		ai.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectAIGame();
			}
		});

		if (getMainActivity().isSignedIn()) {
			showLogout();
		} else {
			showLogin();
		}

		return view;
	}

	private void selectAIGame() {
		// prompt for AI level and game mode
		// then start the game activity
		NewAIGameDialog dialog = new NewAIGameDialog();
		dialog.initialize(new LocalAIGameModeListener() {
			@Override
			public void chosenMode(MarkerChance chance, String aiName, int level) {
				startAIGame(chance, aiName, level);

			}
		});
		dialog.show(getFragmentManager(), "aidialog");
	}

	private void selectLocalGame() {
		// prompt for player names, and game type
		// then start the game activity
		NewLocalGameDialog dialog = new NewLocalGameDialog();
		dialog.initialize(new LocalGameModeListener() {
			@Override
			public void chosenMode(MarkerChance chance, String xName,
					String oName) {
				startLocalTwoPlayerGame(chance, xName, oName);
			}
		});
		dialog.show(getFragmentManager(), "localgame");
	}

	private void selectQuickMode() {
		OnlineGameModeDialog dialog = new OnlineGameModeDialog();
		dialog.initialize(true, new OnlineGameModeListener() {
			@Override
			public void chosenMode(MarkerChance chance) {
				// TODO use the chance argument as a flag to the auto match
				// criteria
				int modeMask = 0;
				final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
				Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
						MIN_OPPONENTS, MAX_OPPONENTS, modeMask);
				RoomListener roomListener = new RoomListener(getMainActivity(),
						getMainActivity().getGameHelper());
				getMainActivity().setRoomListener(roomListener);
				RoomConfig.Builder rtmConfigBuilder = RoomConfig
						.builder(roomListener);
				rtmConfigBuilder.setMessageReceivedListener(roomListener);
				rtmConfigBuilder.setRoomStatusUpdateListener(roomListener);
				rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
				getMainActivity().getGamesClient().createRoom(
						rtmConfigBuilder.build());
			}
		});
		dialog.show(getSherlockActivity().getSupportFragmentManager(),
				"gameMode");

	}

	private void selectOnlineGameMode() {
		// first choose Game mode
		OnlineGameModeDialog dialog = new OnlineGameModeDialog();
		dialog.initialize(false, new OnlineGameModeListener() {
			@Override
			public void chosenMode(MarkerChance chance) {
				Intent intent = getMainActivity().getGamesClient()
						.getSelectPlayersIntent(1, 1);
				startActivityForResult(intent, RC_SELECT_PLAYERS);
			}
		});
		dialog.show(getSherlockActivity().getSupportFragmentManager(),
				"gameMode");
	}

	private void startLocalTwoPlayerGame(MarkerChance chance, String xName,
			String oName) {
		GameFragment gameFragment = new GameFragment();
		Game game = new Game(3, Marker.X, chance);
		ScoreCard score = new ScoreCard(0, 0, 0);
		gameFragment.startGame(new HumanStrategy(xName, Marker.X),
				new HumanStrategy(oName, Marker.O), game, score);

		FragmentManager manager = getActivity().getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.main_frame, gameFragment, "game");
		transaction.addToBackStack(null);
		transaction.commit();
	}

	private void startAIGame(MarkerChance chance, String oName, int aiDepth) {
		GameFragment gameFragment = new GameFragment();
		Game game = new Game(3, Marker.X, chance);
		ScoreCard score = new ScoreCard(0, 0, 0);
		String xName = "Me";
		PlayerStrategy ai;
		if (aiDepth < 0) {
			ai = new RandomAI(oName, Marker.O);
		} else {
			ai = new MinMaxAI(oName, Marker.O, aiDepth, game.getMarkerChance());
		}
		gameFragment.startGame(new HumanStrategy(xName, Marker.X), ai, game,
				score);

		FragmentManager manager = getActivity().getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.replace(R.id.main_frame, gameFragment, "game");
		transaction.addToBackStack(null);
		transaction.commit();
	}

	private void createOnlineRoom(final ArrayList<String> invitees,
			int minAutoMatchPlayers, int maxAutoMatchPlayers) {
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
		if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
					minAutoMatchPlayers, maxAutoMatchPlayers, 0);
			Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
		}

		RoomListener roomListener = new RoomListener(getMainActivity(),
				getMainActivity().getGameHelper());
		getMainActivity().setRoomListener(roomListener);
		// create the room
		Log.d(TAG, "Creating room...");
		RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(roomListener);
		rtmConfigBuilder.addPlayersToInvite(invitees);
		rtmConfigBuilder.setMessageReceivedListener(roomListener);
		rtmConfigBuilder.setRoomStatusUpdateListener(roomListener);
		if (autoMatchCriteria != null) {
			rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
		}
		getMainActivity().getGamesClient().createRoom(rtmConfigBuilder.build());
		Log.d(TAG, "Room created, waiting for it to be ready...");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			// create special intent
			Intent prefIntent = new Intent(getActivity(),
					SettingsActivity.class);

			GameHelper helper = getMainActivity().getGameHelper();
			Info info = null;
			ChaoTicTacToe app = (ChaoTicTacToe) getActivity()
					.getApplication();
			if (helper.isSignedIn()) {
				info = new Info(helper);
			}
			app.setDevelopInfo(info);

			getActivity().startActivity(prefIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSignInFailed() {
		// notify user with dialog?
		showLogin();

		// mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
		// mMainMenuFragment.setShowSignInButton(true);
		// mWinFragment.setShowSignInButton(true);
	}

	private void showLogin() {
		// show login button
		signInView.setVisibility(View.VISIBLE);
		// Sign-in failed, so show sign-in button on main menu
		signOutView.setVisibility(View.INVISIBLE);
	}

	public void onSignInSucceeded() {
		showLogout();
		playRelated.setEnabled(true);

		// install invitation listener so we get notified if we receive an
		// invitation to play
		// a game.
		getMainActivity().getGamesClient().registerInvitationListener(
				new OnInvitationReceivedListener() {
					@Override
					public void onInvitationReceived(Invitation invite) {
						refreshInvites();
						Toast.makeText(
								getActivity(),
								"Got an invite from "
										+ invite.getParticipants().get(0)
												.getDisplayName(),
								Toast.LENGTH_SHORT).show();
					}
				});

		refreshInvites();
	}

	public void signOut() {
		playRelated.setEnabled(false);
	}

	private void showLogout() {
		// disable/hide login button
		signInView.setVisibility(View.INVISIBLE);
		// show sign out button
		signOutView.setVisibility(View.VISIBLE);
		TextView signedInAsText = (TextView) getActivity().findViewById(
				R.id.signed_in_as_text);
		if (signedInAsText == null)
			return;
		signedInAsText.setText("You are signed into Google+ as "
				+ getMainActivity().getGamesClient().getCurrentAccountName());
	}

	private void refreshInvites() {
		getMainActivity().getGamesClient().loadInvitations(
				new OnInvitationsLoadedListener() {
					@Override
					public void onInvitationsLoaded(int statusCode,
							InvitationBuffer buffer) {
						if (statusCode == GamesClient.STATUS_OK) {
							// TODO update the online invites button with the
							// count and
							// enable it
							int count = buffer.getCount();
							Toast.makeText(getActivity(),
									"Got " + count + " invitations",
									Toast.LENGTH_SHORT).show();
							invitesButton.setText(count + " invitation(s)");
						} else if (statusCode == GamesClient.STATUS_NETWORK_ERROR_STALE_DATA) {

						} else if (statusCode == GamesClient.STATUS_CLIENT_RECONNECT_REQUIRED) {

						} else if (statusCode == GamesClient.STATUS_INTERNAL_ERROR) {

						}
					}
				});
	}

	// Accept the given invitation.
	public void acceptInviteToRoom(String invId) {
		RoomListener roomListener = new RoomListener(getMainActivity(),
				getMainActivity().getGameHelper());
		getMainActivity().setRoomListener(roomListener);
		// accept the invitation
		Log.d(TAG, "Accepting invitation: " + invId);
		RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(roomListener);
		roomConfigBuilder.setInvitationIdToAccept(invId)
				.setMessageReceivedListener(roomListener)
				.setRoomStatusUpdateListener(roomListener);
		getMainActivity().getGamesClient().joinRoom(roomConfigBuilder.build());
	}

	public MainActivity getMainActivity() {
		return (MainActivity) super.getActivity();
	}

}
