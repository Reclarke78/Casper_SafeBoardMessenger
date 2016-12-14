package ru.olgathebest.casper.activities;

import android.preference.PreferenceActivity;

/**
 * Created by Ольга on 14.12.2016.
 */
import android.os.Bundle;
import android.preference.PreferenceFragment;

import ru.olgathebest.casper.R;

public class MyPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}
