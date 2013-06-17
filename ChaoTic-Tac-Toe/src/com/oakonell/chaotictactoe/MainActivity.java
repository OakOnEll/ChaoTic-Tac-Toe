package com.oakonell.chaotictactoe;

import java.util.prefs.Preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.oakonell.chaotictactoe.utils.Utils;

public class MainActivity extends SherlockFragmentActivity {
	public static final int PLAY_UPDATE_REQUEST_CODE = 1;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PLAY_UPDATE_REQUEST_CODE) {
			Toast.makeText(this, "Updated the Google Play services!",
					Toast.LENGTH_SHORT).show();
		}
		throw new RuntimeException("Unexpected activity result!");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.enableStrictMode();
		setContentView(R.layout.activity_main);

//		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//		if (ConnectionResult.SERVICE_MISSING == result
//				|| ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED == result
//				|| ConnectionResult.SERVICE_DISABLED == result) {
//			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(result, this,
//					PLAY_UPDATE_REQUEST_CODE);
//			errorDialog.show();
//		} else if (result != ConnectionResult.SUCCESS) {
//			Toast.makeText(this,
//					"Error connecting to Google Play services " + result,
//					Toast.LENGTH_SHORT).show();
//		}

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(false);
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		// ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Button newGame = (Button) findViewById(R.id.new_game);
		newGame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// prompt for player names, and then in onDialogPositiveClick
				// callback, start the game activity
				Intent intent = new Intent(MainActivity.this,
						NewLocalGameDialog.class);
				startActivity(intent);
			}
		});

		// Button viewAchievements = (Button)
		// findViewById(R.id.view_achievements);
		// viewAchievements.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// SKApplication.getInstance().getGameManager()
		// .showAchievements(MainActivity.this);
		// }
		// });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.action_settings:
//			Intent intent = new Intent(this, Preferences.class);
//			startActivity(intent);
//			return true;
//		}
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

}
