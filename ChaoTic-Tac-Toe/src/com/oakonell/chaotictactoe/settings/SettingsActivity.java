package com.oakonell.chaotictactoe.settings;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.widget.Toast;

import com.oakonell.chaotictactoe.ChaoTicTacToe;
import com.oakonell.chaotictactoe.R;
import com.oakonell.utils.activity.GenericAboutActivity;
import com.oakonell.utils.preference.CommonPreferences;
import com.oakonell.utils.preference.PrefsActivity;

public class SettingsActivity extends PrefsActivity {

	protected void beforePreV11BuildFromResource() {
		PreferenceCategory cat1 = new PreferenceCategory(this);
		Preference preference = new Preference(this);
		preference.setTitle("Account settings");
		preference.setSummary("Account settings");
		preference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						launchGooglePlaySettings();
						return true;
					}

				});
		cat1.addPreference(preference);
	}

	@Override
	protected int[] getPreV11PreferenceResources() {
		return new int[] { R.xml.prefs_develop, R.xml.prefs_about };
	}

	@Override
	protected int getV11HeaderResourceId() {
		return R.xml.header;
	}

	@Override
	protected PreferenceConfigurer getPreV11PreferenceConfigurer() {
		return configureMultiple(new DevelopPrefConfigurer(getParent(),
				getPrefFinder()), new CommonPreferences(this, getPrefFinder(),
				GenericAboutActivity.class));
	}

	protected boolean isValidFragment(String fragmentName) {
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onHeaderClick(Header header, int position) {
		if (header.id == R.id.account_settings) {
			launchGooglePlaySettings();
			return;
		}
		super.onHeaderClick(header, position);
	}

	private void launchGooglePlaySettings() {
		ChaoTicTacToe app = (ChaoTicTacToe) getApplication();
		final Intent settingsIntent = app.getSettingsIntent();
		if (settingsIntent == null) {
			(new AlertDialog.Builder(SettingsActivity.this))
					.setMessage("Not logged in")
					.setNeutralButton(android.R.string.ok, null).create()
					.show();
		} else {
			startActivityForResult(settingsIntent, 0);
			Toast.makeText(SettingsActivity.this, "Launched settings?",
					Toast.LENGTH_SHORT).show();
		}
	}

}
