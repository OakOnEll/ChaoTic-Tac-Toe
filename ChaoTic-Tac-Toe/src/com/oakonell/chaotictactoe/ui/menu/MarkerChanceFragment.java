package com.oakonell.chaotictactoe.ui.menu;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.utils.StringUtils;

public class MarkerChanceFragment extends SherlockFragment {

	private static final int MAX_REMOVAL_PERCENT = 50;
	private SeekBar myMarkerSeek;
	private SeekBar opponentMarkerSeek;
	private SeekBar removeMarkerSeek;

	private TextView myMarkerPercent;
	private TextView opponentMarkerPercent;
	private TextView removeMarkerPercent;

	private boolean isAdjusting;
	private Spinner spinner;

	private boolean allowCustom = true;

	public void allowCustomType(boolean allowCustom) {
		this.allowCustom = allowCustom;
	}

	public MarkerChance getChance() {
		int position = spinner.getSelectedItemPosition();
		if (position == 0) {
			return MarkerChance.NORMAL;
		}
		if (position == 1) {
			return MarkerChance.REVERSE;
		}
		if (position == 2) {
			return MarkerChance.CHAOTIC;
		}

		// else custom
		int myMarker = myMarkerSeek.getProgress();
		int opponentMarker = opponentMarkerSeek.getProgress();
		int removeMarker = removeMarkerSeek.getProgress();

		return new MarkerChance(myMarker, opponentMarker, removeMarker);
	}

	protected void updatePercentages() {
		myMarkerPercent.setText(displayablePercentage(myMarkerSeek));
		opponentMarkerPercent
				.setText(displayablePercentage(opponentMarkerSeek));
		removeMarkerPercent.setText(displayablePercentage(removeMarkerSeek));

		double removalPercent = (double) removeMarkerSeek.getProgress() / 3;
		if (removalPercent > MAX_REMOVAL_PERCENT) {
			markRemovalTooHigh();
		} else {
			removeMarkerPercent.setError(null);
		}
	}
	
	private DecimalFormat format = new DecimalFormat("###.#");
	private String displayablePercentage(SeekBar seek) {
		StringBuilder builder = new StringBuilder();
		builder.append(format.format(((double) seek.getProgress() / 3)));
		builder.append("%");
		return builder.toString();
	}

	public static class GameTypeDropDownItem {
		private final String text;
		private final int level;

		public GameTypeDropDownItem(String string, int level) {
			this.level = level;
			this.text = string;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_marker_chance,
				container, false);

		final View custom = view.findViewById(R.id.custom_layout);
		custom.setVisibility(View.GONE);

		spinner = (Spinner) view.findViewById(R.id.choice_chance);

		// <string-array name="chance_array">
		// <item>Normal</item>
		// <item>Reverse</item>
		// <item>Chaotic</item>
		// <item>Custom</item>
		// </string-array>
		List<GameTypeDropDownItem> aiLevels = new ArrayList<GameTypeDropDownItem>();
		aiLevels.add(new GameTypeDropDownItem("Normal", -0));
		aiLevels.add(new GameTypeDropDownItem("Reverse", 1));
		aiLevels.add(new GameTypeDropDownItem("Chaotic", 2));
		if (allowCustom) {
			aiLevels.add(new GameTypeDropDownItem("Custom", 3));
		}

		ArrayAdapter<GameTypeDropDownItem> aiLevelAdapter = new ArrayAdapter<GameTypeDropDownItem>(
				getActivity(), android.R.layout.simple_spinner_dropdown_item,
				aiLevels);
		spinner.setAdapter(aiLevelAdapter);
		spinner.setSelection(0);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				boolean isCustom = pos == 3;
				custom.setVisibility(isCustom ? View.VISIBLE : View.GONE);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// empty
			}
		});

		myMarkerSeek = (SeekBar) view.findViewById(R.id.chance_seek_my_marker);
		opponentMarkerSeek = (SeekBar) view
				.findViewById(R.id.chance_seek_opponent_marker);
		removeMarkerSeek = (SeekBar) view
				.findViewById(R.id.chance_seek_remove_marker);

		myMarkerSeek.setProgress(100);
		opponentMarkerSeek.setProgress(100);
		removeMarkerSeek.setProgress(100);

		myMarkerPercent = (TextView) view
				.findViewById(R.id.chance_percent_my_marker);
		opponentMarkerPercent = (TextView) view
				.findViewById(R.id.chance_percent_opponent_marker);
		removeMarkerPercent = (TextView) view
				.findViewById(R.id.chance_percent_remove_marker);

		updatePercentages();

		OnSeekBarChangeListener seekChangeListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (isAdjusting)
					return;
				isAdjusting = true;
				int total = 300;
				SeekBar adj1;
				SeekBar adj2;

				if (seekBar == myMarkerSeek) {
					adj1 = opponentMarkerSeek;
					adj2 = removeMarkerSeek;
				} else if (seekBar == opponentMarkerSeek) {
					adj1 = myMarkerSeek;
					adj2 = removeMarkerSeek;
				} else if (seekBar == removeMarkerSeek) {
					adj1 = opponentMarkerSeek;
					adj2 = myMarkerSeek;
				} else {
					throw new RuntimeException("Inavlid seekbar");
				}

				int change = (total - (adj1.getProgress() + adj2.getProgress() + progress)) / 2;
				int change2 = total
						- (adj1.getProgress() + change + progress + adj2
								.getProgress());

				adj1.setProgress(adj1.getProgress() + change);
				adj2.setProgress(adj2.getProgress() + change2);
				updatePercentages();
				isAdjusting = false;
			}
		};
		myMarkerSeek.setOnSeekBarChangeListener(seekChangeListener);
		opponentMarkerSeek.setOnSeekBarChangeListener(seekChangeListener);
		removeMarkerSeek.setOnSeekBarChangeListener(seekChangeListener);

		return view;
	}

	public boolean validate() {
		MarkerChance chance = getChance();
		if (chance.getRemoveMarkerPercentage() * 100 > MAX_REMOVAL_PERCENT) {
			markRemovalTooHigh();
			return false;
		}
		return true;
	}

	private void markRemovalTooHigh() {
		removeMarkerPercent
				.setError("Cannot set remove chance to be larger than "
						+ MAX_REMOVAL_PERCENT + "%");
		removeMarkerPercent.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String errorText = removeMarkerPercent.getError().toString();
				if (StringUtils.isEmpty(errorText))
					return false;

				int[] pos = new int[2];
				removeMarkerPercent.getLocationInWindow(pos);

				Toast t = Toast.makeText(getActivity(), errorText,
						Toast.LENGTH_SHORT);
				t.setGravity(Gravity.TOP | Gravity.LEFT,
						pos[0] - ((errorText.length() / 2) * 12), pos[1] - 128);
				t.show();
				return true;
			}
		});

	}

}
