package com.oakonell.chaotictactoe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.oakonell.chaotictactoe.model.MarkerChance;

public class MarkerChanceFragment extends SherlockFragment {

	private SeekBar myMarkerSeek;
	private SeekBar opponentMarkerSeek;
	private SeekBar removeMarkerSeek;
	
	private TextView myMarkerPercent;
	private TextView opponentMarkerPercent;
	private TextView removeMarkerPercent;

	private boolean isAdjusting;
	private Spinner spinner;

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
		myMarkerPercent.setText((myMarkerSeek.getProgress() / 3) + "%");
		opponentMarkerPercent.setText((opponentMarkerSeek.getProgress() / 3) + "%");
		removeMarkerPercent.setText((removeMarkerSeek.getProgress() / 3) + "%");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_marker_chance, container,
				false);

		final View custom = view.findViewById(R.id.custom_layout);		
		custom.setVisibility(View.GONE);

		spinner = (Spinner)view.findViewById(R.id.choice_chance);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				boolean isCustom = pos==3;
				custom.setVisibility(isCustom? View.VISIBLE : View.GONE);

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub				
			}
		});
		
		myMarkerSeek = (SeekBar) view.findViewById(R.id.chance_seek_my_marker);
		opponentMarkerSeek = (SeekBar) view.findViewById(R.id.chance_seek_opponent_marker);
		removeMarkerSeek = (SeekBar) view.findViewById(R.id.chance_seek_remove_marker);
		
		myMarkerSeek.setProgress(100);
		opponentMarkerSeek.setProgress(100);
		removeMarkerSeek.setProgress(100);

		myMarkerPercent= (TextView) view.findViewById(R.id.chance_percent_my_marker);
		opponentMarkerPercent= (TextView) view.findViewById(R.id.chance_percent_opponent_marker);
		removeMarkerPercent = (TextView) view.findViewById(R.id.chance_percent_remove_marker);

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
				if (isAdjusting) return;
				isAdjusting = true;
				int total = 300;
				SeekBar adj1 ;
				SeekBar adj2 ;
				if (seekBar == myMarkerSeek ) {
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
				int change2 = total  - (adj1.getProgress() +change + progress + adj2.getProgress());
				
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

	public void validate() {
		// TODO validate that the removeMarker chance is not "TOO" high. 100% is right out...
		
	}
	
}
