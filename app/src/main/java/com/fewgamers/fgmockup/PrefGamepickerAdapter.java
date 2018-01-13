package com.fewgamers.fgmockup;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 1/9/2018.
 */

public class PrefGamepickerAdapter extends BaseAdapter {
    private Context context;
    private boolean[] selectedGames;

    public PrefGamepickerAdapter(Context c, boolean[] b) {
        this.context = c;
        this.selectedGames = b;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View resView = inflater.inflate(R.layout.pref_gamepicker_list_item, parent, false);

        ImageView gameIcon = resView.findViewById(R.id.pref_gamepicker_icon);
        TextView gameName = resView.findViewById(R.id.pref_gamepicker_name);

        Integer pos = (Integer) position;
        gameIcon.setImageResource(mThumbIds[position]);
        gameName.setText(mThumbTexts[position]);

        if (selectedGames[position]) {
            resView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }

        return resView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.ic_profile_placeholder, R.drawable.ic_profile_placeholder,
            R.drawable.ic_profile_placeholder, R.drawable.ic_profile_placeholder,
            R.drawable.ic_profile_placeholder, R.drawable.ic_profile_placeholder,
            R.drawable.ic_profile_placeholder
    };

    private String[] mThumbTexts = {
            "Red Alert", "Day of Defeat", "CS:1.6", "CoD2", "Dawn of War", "Doki Doki", "All items"
    };
}
