package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

/**
 * Created by Administrator on 1/9/2018.
 */

public class PrefFragGamePicker extends Fragment {
    private boolean[] selectedGames = new boolean[7];
    private String selectedGamesString;
    private PrefGamepickerAdapter pickAdapter;
    private SharedPreferences gamePickPreferences;
    private SharedPreferences.Editor gamePickEditor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pref_frag_gamepicker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gamePickPreferences = getActivity().getSharedPreferences("pickedGames", Context.MODE_PRIVATE);
        setSelectedGames();
        gamePickEditor = gamePickPreferences.edit();
        gamePickEditor.apply();

        GridView gameGrid = (GridView) view.findViewById(R.id.pref_game_grid);
        pickAdapter = new PrefGamepickerAdapter(getActivity(), selectedGames);
        gameGrid.setAdapter(pickAdapter);

        gameGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int lastIndex = selectedGames.length - 1;
                if (position == lastIndex) {
                    clearSelections();
                    selectedGames[lastIndex] = true;
                } else {
                    selectedGames[lastIndex] = false;
                    selectedGames[position] = !selectedGames[position];
                }
                pickAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setSelectedGames() {
        String storedPicks = gamePickPreferences.getString("storedPicks", "fffffff");
        for (int i = 0; i < 6; i++) {
            selectedGames[i] = (storedPicks.substring(i, i + 1).equals("t"));
        }
    }

    private void clearSelections() {
        for (int i = 0; i < selectedGames.length; i++) {
            selectedGames[i] = false;
        }
        pickAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        StringBuilder builder = new StringBuilder();
        for (boolean b : selectedGames) {
            if (b) {
                builder.append('t');
            } else {
                builder.append('f');
            }
        }
        gamePickEditor.putString("storedPicks", builder.toString());
        Log.d("dit wordt opgeslagen", builder.toString());
        gamePickEditor.apply();
    }
}
