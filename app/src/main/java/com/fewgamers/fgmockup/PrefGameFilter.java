package com.fewgamers.fgmockup;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

/**
 * Created by Administrator on 1/12/2018.
 */

public class PrefGameFilter extends DialogPreference {
    private String[] allGames, allGamesUUIDs, selectedGames;
    private String allGamesString;
    private StringBuilder selectedGameBuilder;
    private LinearLayout selectedGamesLayout, currentRow;
    private MultiAutoCompleteTextView filterAutoComplete;
    private ArrayAdapter<String> filterAdapter;
    private ImageButton clearButton;
    private int selectedCount = 0;

    private MainActivity mainActivity;

    private Context context;

    public PrefGameFilter(Context c, AttributeSet attrs) {
        super(c, attrs);

        mainActivity = (MainActivity) c;
        allGames = mainActivity.getAllGamesArray();
        allGamesUUIDs = mainActivity.getAllGamesUUIDsArray();

        setDialogLayoutResource(R.layout.pref_game_filter);
        setPositiveButtonText("Apply");
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        context = getContext();

        selectedCount = 0;
        selectedGameBuilder = new StringBuilder("");

        filterAutoComplete = (MultiAutoCompleteTextView) view.findViewById(R.id.filtered_games_edit_add);
        filterAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, allGames);
        filterAutoComplete.setAdapter(filterAdapter);
        filterAutoComplete.setThreshold(1);
        filterAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        filterAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filterAutoComplete.setText("");
                String selectedGame = (String) parent.getItemAtPosition(position);
                addGameText(selectedGame);
                filterAdapter.remove(selectedGame);
            }
        });
        selectedGamesLayout = (LinearLayout) view.findViewById(R.id.filtered_games_texts_linear_layout);

        clearButton = new AppCompatImageButton(context);
        LinearLayout.LayoutParams buttomParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        clearButton.setImageResource(R.drawable.ic_game_filter_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGameBuilder = new StringBuilder("");
                selectedGamesLayout.removeAllViews();
                selectedCount = 0;
                filterAdapter.clear();
                filterAdapter.addAll(allGames);
            }
        });

        if (selectedGames != null) {
            for (String game : selectedGames) {
                addGameText(game);
            }
        }
    }

    private void addGameText(String selectedGame) {
        if (selectedGame.length() == 0) {
            return;
        }
        selectedGameBuilder.append(",,");
        selectedGameBuilder.append(selectedGame);

        try {
            currentRow.removeView(clearButton);
        } catch (NullPointerException e) {
            // de eerste iteratie is er geen clearButton toegevoegd
        }
        if (selectedCount % 3 == 0) {
            currentRow = new LinearLayout(context);
            selectedGamesLayout.addView(currentRow);
        }
        TextView gameText = getGameText(selectedGame);
        currentRow.addView(gameText);
        ((LinearLayout.LayoutParams) gameText.getLayoutParams()).setMargins(15, 10, 0, 0);
        currentRow.addView(clearButton);

        selectedCount++;
    }

    private TextView getGameText(String selectedGame) {
        TextView gameText = new TextView(context);
        gameText.setText(selectedGame);

        gameText.setBackground(context.getResources().getDrawable(R.drawable.chat_date_rounded_textview));
        gameText.setPadding(10, 10, 10, 10);

        return gameText;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String persist = selectedGameBuilder.toString();
            if (persist.length() > 0) {
                persist = persist.substring(2);
            } else {
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
