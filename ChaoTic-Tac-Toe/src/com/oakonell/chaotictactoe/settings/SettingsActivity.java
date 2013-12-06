package com.oakonell.chaotictactoe.settings;

import com.oakonell.chaotictactoe.R;
import com.oakonell.utils.activity.GenericAboutActivity;
import com.oakonell.utils.preference.CommonPreferences;
import com.oakonell.utils.preference.PrefsActivity;

public class SettingsActivity extends PrefsActivity {

	@Override
	protected int[] getPreV11PreferenceResources() {
		return new int[] { R.xml.prefs_account,R.xml.prefs_develop, R.xml.prefs_about };
	}

	@Override
	protected int getV11HeaderResourceId() {
		return R.xml.header;
	}

	@Override
	protected PreferenceConfigurer getPreV11PreferenceConfigurer() {
		return configureMultiple(new DevelopPrefConfigurer(getParent(),
				getPrefFinder()), new CommonPreferences(this, getPrefFinder(), GenericAboutActivity.class));
	}

}
