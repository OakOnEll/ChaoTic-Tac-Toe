package com.oakonell.chaotictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.oakonell.chaotictactoe.googleapi.BaseGameActivity;
import com.oakonell.chaotictactoe.googleapi.GameHelper;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.ui.game.GameFragment;
import com.oakonell.chaotictactoe.ui.menu.MenuFragment;
import com.oakonell.utils.Utils;
import com.oakonell.utils.activity.AppLaunchUtils;

public class MainActivity extends BaseGameActivity {
	private static final String FRAG_TAG_GAME = "game";
	private static final String FRAG_TAG_MENU = "menu";
	private static final String TAG = MainActivity.class.getName();

	private RoomListener roomListener;
	private InterstitialAd mInterstitialAd;
	private AdView mAdView;

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		if (request == MenuFragment.RC_WAITING_ROOM) {
			// TODO currently specially launched from listener, with access to
			// activity only
			getMenuFragment().onActivityResult(request, response, data);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.enableStrictMode();
		setContentView(R.layout.main_activity);

		initializeInterstitialAd();

		mAdView = (AdView) findViewById(R.id.adView);
		// mAdView.setAdListener(new ToastAdListener(this));
		mAdView.loadAd(new AdRequest.Builder().build());

		AppLaunchUtils.appLaunched(this, null);

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		// ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Fragment menuFrag = getSupportFragmentManager().findFragmentByTag(
				FRAG_TAG_MENU);
		if (menuFrag == null) {
			menuFrag = new MenuFragment();
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.add(R.id.main_frame, menuFrag, FRAG_TAG_MENU);
			transaction.commit();
		}

		setSignInMessages(getString(R.string.signing_in),
				getString(R.string.signing_out));

	}

	private void initializeInterstitialAd() {
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd
				.setAdUnitId(getResources().getString(R.string.admob_id));
		mInterstitialAd.loadAd(new AdRequest.Builder().build());
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
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onSignInFailed() {
		getMenuFragment().onSignInFailed();
	}

	@Override
	public void signOut() {
		getMenuFragment().signOut();
		super.signOut();
	}

	@Override
	public void onSignInSucceeded() {
		getMenuFragment().onSignInSucceeded();
		
		ChaoTicTacToe app = (ChaoTicTacToe) getApplication();

		Intent settingsIntent = getGamesClient().getSettingsIntent();
		app.setSettingsIntent(settingsIntent);
		
		Achievements achievements = app
				.getAchievements();
		if (achievements.hasPending()) {
			achievements.pushToGoogle(getGameHelper(), this);
		}

		// if we received an invite via notification, accept it; otherwise, go
		// to main screen
		if (getInvitationId() != null) {
			getMenuFragment().acceptInviteToRoom(getInvitationId());
			return;
		}
	}

	@Override
	public GameHelper getGameHelper() {
		return super.getGameHelper();
	}

	@Override
	public GamesClient getGamesClient() {
		return super.getGamesClient();
	}

	public MenuFragment getMenuFragment() {
		return (MenuFragment) getSupportFragmentManager().findFragmentByTag(
				FRAG_TAG_MENU);
	}

	public GameFragment getGameFragment() {
		return (GameFragment) getSupportFragmentManager().findFragmentByTag(
				FRAG_TAG_GAME);
	}

	public void onlineMoveReceived(Marker marker, Cell cell) {
		getGameFragment().onlineMakeMove(marker, cell);
	}

	public void messageRecieved(Participant opponentParticipant, String string) {
		getGameFragment().messageRecieved(opponentParticipant, string);
	}

	public void gameEnded() {
		possiblyShowInterstitialAd();
	}

	public RoomListener getRoomListener() {
		return roomListener;
	}

	public void setRoomListener(RoomListener roomListener) {
		this.roomListener = roomListener;
	}

	@Override
	public void onBackPressed() {
		if (getGameFragment() != null && getGameFragment().isVisible()) {
			// TODO localize these strings
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Leave game?");
			builder.setMessage("Leave the game in progress?");
			builder.setPositiveButton("Yes", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					// TODO inform possible opponent of leaving room
					if (roomListener != null) {
						roomListener.leaveRoom();
					}
					MainActivity.super.onBackPressed();

					// show an ad
					possiblyShowInterstitialAd();
				}
			});
			builder.setNegativeButton("No", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		mAdView.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();
	}

	@Override
	protected void onDestroy() {
		mAdView.destroy();
		super.onDestroy();
	}

	private void possiblyShowInterstitialAd() {
		// show an ad
		// possibly only show with some probability (50%?)
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdClosed() {
					super.onAdClosed();
					initializeInterstitialAd();
				}
			});
		}
	}

}
