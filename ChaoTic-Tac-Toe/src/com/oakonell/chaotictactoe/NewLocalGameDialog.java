package com.oakonell.chaotictactoe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.utils.StringUtils;

public class NewLocalGameDialog extends SherlockFragmentActivity {
	public static final String X_NAME_KEY = "X-name";
	public static final String O_NAME_KEY = "O-name";

	private String xName;
	private String oName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		defaultNamesFromPreferences();
		setContentView(R.layout.dialog_local_game);

		ImageButton switchPlayers = (ImageButton) findViewById(R.id.switch_players);
		final EditText xNameText = (EditText) findViewById(R.id.player_x_name);
		final EditText oNameText = (EditText) findViewById(R.id.player_o_name);

		final MarkerChanceFragment frag = new MarkerChanceFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.fragmentContainer, frag);
		transaction.commit();

		xNameText.setText(xName);
		oNameText.setText(oName);

		OnFocusChangeListener onNameFocusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					validate(xNameText, oNameText, frag);
				}
			}
		};
		xNameText.setOnFocusChangeListener(onNameFocusChangeListener);
		xNameText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				xNameText.setError(null);
			}
		});
		oNameText.setOnFocusChangeListener(onNameFocusChangeListener);
		oNameText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				oNameText.setError(null);
			}
		});

		switchPlayers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Editable temp = xNameText.getText();
				xNameText.setText(oNameText.getText());
				oNameText.setText(temp);
			}
		});

		Button start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!validate(xNameText, oNameText, frag)) {
					return;
				}
				xName = xNameText.getText().toString();
				oName = oNameText.getText().toString();
				MarkerChance chance = frag.getChance();
				writeNamesToPreferences();

				Intent intent = new Intent();
				chance.putIntentExtras(intent);

				intent.putExtra(X_NAME_KEY, xName);
				intent.putExtra(O_NAME_KEY, oName);

				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});

	}

	protected boolean validate(EditText xNameText, EditText oNameText,
			MarkerChanceFragment frag) {
		boolean isValid = true;
		String xName = xNameText.getText().toString();
		if (StringUtils.isEmpty(xName)) {
			isValid = false;
			xNameText.setError("Enter a name for X");
		}
		String oName = oNameText.getText().toString();
		if (StringUtils.isEmpty(oName)) {
			isValid = false;
			oNameText.setError("Enter a name for O");
		}

		if (xName.equals(oName)) {
			isValid = false;
			oNameText.setError("Enter a unique name for O");
		}

		isValid &= frag.validate();
		return isValid;
	}

	private static final String X_NAME = "x-name";
	private static final String O_NAME = "o-name";

	private void defaultNamesFromPreferences() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		// TODO store multiple names of the last players, and hook "search" into
		// the text entry
		xName = sharedPrefs.getString(X_NAME, "");
		oName = sharedPrefs.getString(O_NAME, "");
	}

	private void writeNamesToPreferences() {
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edit = sharedPrefs.edit();
		edit.putString(X_NAME, xName);
		edit.putString(O_NAME, oName);
		edit.commit();
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
