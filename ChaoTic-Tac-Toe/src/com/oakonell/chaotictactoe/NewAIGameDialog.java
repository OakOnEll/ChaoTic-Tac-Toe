package com.oakonell.chaotictactoe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.oakonell.chaotictactoe.model.MarkerChance;

public class NewAIGameDialog extends SherlockFragmentActivity {
	public static final String AI_NAME_KEY = "aiName";
	public static final String AI_DEPTH = "aiDepth";
	
	private String oName;

	public static class AiDropDownItem {
		private final String text;
		private final int level;

		public AiDropDownItem(String string, int level) {
			this.level =level;
			this.text = string;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_local_ai);

		List<AiDropDownItem> aiLevels = new ArrayList<NewAIGameDialog.AiDropDownItem>();
		aiLevels.add(new AiDropDownItem("Random", -1));
		aiLevels.add(new AiDropDownItem("Easy", 1));
		aiLevels.add(new AiDropDownItem("Medium", 2));
		aiLevels.add(new AiDropDownItem("Hard", 3));
		
		final Spinner aiLevelSpinner = (Spinner) findViewById(R.id.ai_level);
		ArrayAdapter<AiDropDownItem> aiLevelAdapter = new ArrayAdapter<AiDropDownItem>(
				this, android.R.layout.simple_spinner_item, aiLevels);
		aiLevelSpinner.setAdapter(aiLevelAdapter);
		aiLevelSpinner.setSelection(1);

		final MarkerChanceFragment frag = new MarkerChanceFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.fragmentContainer, frag);
		transaction.commit();

		Button start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!validate(frag)) {
					return;
				}

				AiDropDownItem selectedItem = (AiDropDownItem) aiLevelSpinner.getSelectedItem();
				
				oName = selectedItem.text + " AI";
				
				MarkerChance chance = frag.getChance();

				Intent intent = new Intent();
				chance.putIntentExtras(intent);

				intent.putExtra(AI_NAME_KEY, oName);				
				intent.putExtra(AI_DEPTH, selectedItem.level);

				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});

	}

	protected boolean validate(MarkerChanceFragment frag) {
		boolean isValid = true;

		isValid &= frag.validate();
		return isValid;
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
