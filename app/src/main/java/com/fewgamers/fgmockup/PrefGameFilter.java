package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Administrator on 1/12/2018.
 */

public class PrefGameFilter extends DialogPreference {
    private String[] allGames, selectedGames;
    private String selectedGameString;
    private LinearLayout selectedGamesLayout, currentRow;
    private MultiAutoCompleteTextView filterEdit;
    private ArrayAdapter<String> filterAdapter;
    private ImageButton clearButton;
    private int selectedCount = 0;

    public PrefGameFilter(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.pref_game_filter);
        setPositiveButtonText("Apply");

        allGames = getAllGames();
    }

    private String[] getAllGames() {
        return new String[]{
                "Boata", "Cod2", "Day of Defeat", "StarCraft:BW", "Red Orchestra"
        };
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        selectedCount = 0;
        selectedGameString = "";

        filterEdit = (MultiAutoCompleteTextView) view.findViewById(R.id.filtered_games_edit_add);
        filterAdapter = new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_dropdown_item_1line, allGames);
        filterEdit.setAdapter(filterAdapter);
        filterEdit.setThreshold(1);
        filterEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        filterEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filterEdit.setText("");
                String selectedGame = (String) parent.getItemAtPosition(position);

                addGameText(selectedGame);
            }
        });
        selectedGamesLayout = (LinearLayout) view.findViewById(R.id.filtered_games_texts_linear_layout);

        clearButton = new AppCompatImageButton(getContext());
        LinearLayout.LayoutParams buttomParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        clearButton.setImageResource(R.drawable.ic_game_filter_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGameString = "";
                selectedGamesLayout.removeAllViews();
                selectedCount = 0;
            }
        });

        for (String game : selectedGames) {
            addGameText(game);
        }
    }

    private void addGameText(String selectedGame) {
        if (selectedGame.length() == 0) {
            return;
        }
        selectedGameString += ",," + selectedGame;

        try {
            currentRow.removeView(clearButton);
        } catch (NullPointerException e) {
        }
        if (selectedCount % 3 == 0) {
            currentRow = new LinearLayout(getContext());
            selectedGamesLayout.addView(currentRow);
        }
        TextView gameText = getGameText(selectedGame);
        currentRow.addView(gameText);
        ((LinearLayout.LayoutParams) gameText.getLayoutParams()).setMargins(15, 10, 0, 0);
        currentRow.addView(clearButton);

        selectedCount++;
    }

    private TextView getGameText(String selectedGame) {
        TextView gameText = new TextView(getContext());
        gameText.setText(selectedGame);

        gameText.setBackground(getContext().getResources().getDrawable(R.drawable.chat_date_rounded_textview));
        gameText.setPadding(10, 10, 10, 10);

        return gameText;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String persist = "";
            if (selectedGameString.length() > 0) {
                persist = selectedGameString.substring(2);
            }
            persistString(persist);
            selectedGames = persist.split(",,");
            Log.d("Persisted string", persist);
        } else {
        }
        super.onDialogClosed(positiveResult);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        Log.d("Initial value", "Initial value is called");
        if (restorePersistedValue) {
            selectedGames = getPersistedString("").split(",,");
            Log.d("Persisted got", getPersistedString(""));
        } else {
            selectedGames = new String[allGames.length];
        }
    }

    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
}
