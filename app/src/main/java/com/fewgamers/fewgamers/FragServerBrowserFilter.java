package com.fewgamers.fewgamers;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Administrator on 12/26/2017.
 */

// duplicate of the server filter subscreen found in the app's main setting screen, to allow for
// direct access from the server browser
public class FragServerBrowserFilter extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_server_filter);
    }
}
