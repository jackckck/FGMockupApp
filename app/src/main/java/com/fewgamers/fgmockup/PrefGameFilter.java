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
import android.widget.Toast;

/**
 * Created by Administrator on 1/12/2018.
 */

// extension of DialogPreference, used in the settings' server filter subscreen to allow a user to
// select a list of games. when the server browser is opened, only servers that host one of the
// selected games are shown
public class PrefGameFilter extends DialogPreference {
    private String[] allGames, selectedGamesUUIDs;
    // a StringBuilder that builds a String containing all selected games, which is stored to
    // SharedPreferences once the dialog is closed
    private StringBuilder selectedGameUUIDsBuilder;
    // selectedGamesLayout is a vertical LinearLayout that's wrapped by the custom dialog layout,
    // which will contain a horizontal layout for every three selected games. the dialog's layout
    // is thus divided into rows which can contain up to three TextViews that display a selected
    // game's name. when a row (horizontal linearlayout) is added, currentRow is modified to refer
    // to that new row
    private LinearLayout selectedGamesLayout, currentRow;
    private MultiAutoCompleteTextView filterAutoComplete;
    private ImageButton clearButton;
    private int selectedCount = 0;

    private MainActivity mainActivity;

    private Context context;

    public PrefGameFilter(Context c, AttributeSet attrs) {
        super(c, attrs);

        mainActivity = (MainActivity) c;
        allGames = mainActivity.getAllGamesArray();

        setDialogLayoutResource(R.layout.pref_game_filter);
        setPositiveButtonText("Apply");
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        context = getContext();

        selectedCount = 0;
        selectedGameUUIDsBuilder = new StringBuilder("");

        // autocomplete textbox that suggests registered games to the user. only by clicking those
        // suggestions can a game be added to the selected games
        filterAutoComplete = (MultiAutoCompleteTextView) view.findViewById(R.id.filtered_games_edit_add);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, allGames);
        filterAutoComplete.setAdapter(filterAdapter);
        filterAutoComplete.setThreshold(1);
        filterAutoComplete.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        filterAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                filterAutoComplete.setText("");
                String selectedGame = (String) parent.getItemAtPosition(position);
                addGameText(selectedGame, mainActivity.getGameUUIDFromName(selectedGame));
            }
        });
        selectedGamesLayout = (LinearLayout) view.findViewById(R.id.filtered_games_texts_linear_layout);

        // the clear button will remove all selected games from the builded string, and removes all
        // visual representations of these selected games as well
        clearButton = new AppCompatImageButton(context);
        LinearLayout.LayoutParams buttomParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        clearButton.setLayoutParams(buttomParams);
        clearButton.setImageResource(R.drawable.ic_game_filter_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedGameUUIDsBuilder = new StringBuilder("");
                selectedGamesLayout.removeAllViews();
                selectedCount = 0;
            }
        });

        // adds visual representation of all previously selected games
        if (selectedGamesUUIDs != null) {
            for (String gameUUID : selectedGamesUUIDs) {
                addGameText(mainActivity.getGameNameFromUUID(gameUUID), gameUUID);
            }
        }
    }

    // adds a TextView that has the name of the selected game, with its own background that separates
    // it visually from the other selections
    private void addGameText(String selectedGame, String selectedGameUUID) {
        if (selectedGame == null || selectedGame.length() == 0) {
            return;
        }

        // adds a game to the selected games string
        selectedGameUUIDsBuilder.append(",,");
        selectedGameUUIDsBuilder.append(selectedGameUUID);

        // the clear button will be displayed behind the last TextView. to accomplish this, the button
        // is removed and added after the new TextView
        try {
            currentRow.removeView(clearButton);
        } catch (NullPointerException e) {
            // when the first TextView is added, no clear button will be present
        }
        // when the currentRow has three TextViews in it, a new row is created
        if (selectedCount % 3 == 0) {
            currentRow = new LinearLayout(context);
            selectedGamesLayout.addView(currentRow);
        }
        // calls method that returns an appropriately formatted TextView
        TextView gameText = getGameText(selectedGame);
        currentRow.addView(gameText);
        ((LinearLayout.LayoutParams) gameText.getLayoutParams()).setMargins(15, 10, 0, 0);
        // adds the clear button at the end of the list of games
        currentRow.addView(clearButton);

        selectedCount++;
    }

    // returns a TextView according to the format used in the PrefGameFilter dialog
    private TextView getGameText(String selectedGame) {
        TextView gameText = new TextView(context);
        gameText.setText(selectedGame);

        gameText.setBackground(context.getResources().getDrawable(R.drawable.chat_date_rounded_textview));
        gameText.setPadding(10, 10, 10, 10);

        return gameText;
    }

    // preserves the list of selected games
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String persist = selectedGameUUIDsBuilder.toString();
            persistString(persist);
            selectedGamesUUIDs = persist.split(",,");

            Log.d("Persisted selection", persist);
        }
        super.onDialogClosed(positiveResult);
    }

    // retrieves the previously stored list of selected games
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            selectedGamesUUIDs = getPersistedString("").split(",,");
            Log.d("Persisted got", getPersistedString(""));
        } else {
            selectedGamesUUIDs = new String[allGames.length];
        }
    }

    @Override
    protected String onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
}
