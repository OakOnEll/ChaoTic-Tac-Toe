package com.oakonell.chaotictactoe.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.PreferenceActivity;

import com.oakonell.chaotictactoe.R;
import com.oakonell.utils.activity.GenericAboutActivity;
import com.oakonell.utils.preference.CommonPreferences;
import com.oakonell.utils.preference.PrefsActivity.PreferenceConfigurer;
import com.oakonell.utils.preference.PrefsFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Preferences extends PrefsFragment {

	@Override
	protected PreferenceConfigurer getPreferenceConfigurer(int resource) {
		if (resource == R.xml.prefs_develop) {
			return new DevelopPrefConfigurer(getActivity(),
					getPreferenceFinder());
		}
		if (resource == R.xml.prefs_about) {
			return new CommonPreferences((PreferenceActivity)getActivity(), getPreferenceFinder(),
					GenericAboutActivity.class);
		}
		return null;
	}

}
