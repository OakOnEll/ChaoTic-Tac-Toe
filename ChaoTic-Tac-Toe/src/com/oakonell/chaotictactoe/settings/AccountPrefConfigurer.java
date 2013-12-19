package com.oakonell.chaotictactoe.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.oakonell.chaotictactoe.ChaoTicTacToe;
import com.oakonell.utils.preference.PrefsActivity.PreferenceConfigurer;
import com.oakonell.utils.preference.PrefsActivity.PreferenceFinder;

public class AccountPrefConfigurer implements PreferenceConfigurer {
	private PreferenceFinder finder;
	private Activity activity;

	AccountPrefConfigurer(Activity activity, PreferenceFinder finder) {
		this.finder = finder;
		this.activity = activity;
	}

	@Override
	public void configure() {
		ChaoTicTacToe app = (ChaoTicTacToe) activity.getApplication();
		final Intent settingsIntent = app.getSettingsIntent();
		Preference settings = finder.findPreference("settings");
		if (settingsIntent == null) {
			settings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					(new AlertDialog.Builder(activity))
							.setMessage("Not logged in")
							.setNeutralButton(android.R.string.ok, null)
							.create().show();
					return true;
				}
			});
		} else {
			settings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					activity.startActivityForResult(settingsIntent, 0);
					Toast.makeText(activity, "Launched settings?", Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		}

	}
}
