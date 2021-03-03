package com.example.FD.User_Fall_Detection_kash;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.companion_phonenumber)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.threshold_value)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.notification_method)));

    }

    @Override
    public boolean onPreferenceChange(Preference prefvalue, Object value) {
        String value_entered = value.toString();

        if (prefvalue instanceof ListPreference)
        {
            ListPreference lp = (ListPreference) prefvalue;
            int listindex = lp.findIndexOfValue(value_entered);
            if (listindex >= 0)
            {
                prefvalue.setSummary(lp.getEntries()[listindex]);
            }
        }
        else {
            prefvalue.setSummary(value_entered);
        }
        return true;
    }

    private void bindPreferenceSummaryToValue(Preference prefvalue) {
        prefvalue.setOnPreferenceChangeListener(this);
        onPreferenceChange(prefvalue, PreferenceManager.getDefaultSharedPreferences(prefvalue.getContext()).getString(prefvalue.getKey(), ""));
    }
}