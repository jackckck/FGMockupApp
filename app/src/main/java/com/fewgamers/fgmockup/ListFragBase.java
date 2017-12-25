package com.fewgamers.fgmockup;

import android.app.ListFragment;

/**
 * Created by Administrator on 12/24/2017.
 */

public class ListFragBase extends ListFragment {
    public String formatStringToJSONArray(String string) {
        String res = string;
        for (int i = res.length() - 1; i > 1; i--) {
            if (res.charAt(i) == '{') {
                res = res.substring(0, i) + "," + res.substring(i);
            }
        }
        res = "[" + res + "]";
        return res;
    }
}
