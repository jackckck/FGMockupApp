package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // preserves the list of servers that were last displayed in the serverbrowser
    public boolean hasServerListStored;
    public String completeServerListString;

    // preserves the text within the server browser's search bar
    public String serverSearchBarText;

    // field that makes the master key easily available to all fragments
    private String master;

    // userdata fields to be retrieved by fragments
    private String myUuid, myUsername, myEmail, myFirstName, myLastName, myKey, myActivationCode;

    // arrays that contain all registered games and their corresponding uuids, plus a map that
    // connects each pair
    private String[] allGamesArray, allGamesUUIDsArray;
    Map<String, String> allGamesMap = new HashMap<>();

    // SharedPreferences reserved for loginData
    public SharedPreferences loginSharedPreferences;

    // TabLayout used in FragUser and FragContacts
    public TabLayout fgTabs;
    public TabLayout.Tab tab0, tab1, tab2;

    // remembers which tab was selected last in the user fragment and contacts fragment
    public int contactsFragTabSelected, userFragTabSelected;

    // arraylist that stores the order of all previously selected tabs
    public ArrayList<Integer> previousFragId = new ArrayList<>();
    // to restore a fragment of FragUserInfo or FragServerInfo, the user uuids and ServerObjects
    // that were used in these fragments must also be stored
    public ArrayList<String> onBackUUIDs = new ArrayList<>();
    public ArrayList<ServerObject> onBackServerObjects = new ArrayList<>();

    // a spinning animation shown during most asynchronous processes in this app. it is 'turned on
    // and off' by changing its visibility
    public ProgressBar mainProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiate widgets and fields
        previousFragId = new ArrayList<>();

        fgTabs = findViewById(R.id.mainTabs);
        tab0 = fgTabs.getTabAt(0);
        tab1 = fgTabs.getTabAt(1);
        tab2 = fgTabs.getTabAt(2);

        setLoginDatafields();
        setGamesDataFields();

        mainProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // code that was generated automatically
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // the default starting fragment is FragServerBrowser
        previousFragId.add(0, R.id.nav_server_browser);
        DisplayFragment(R.id.nav_server_browser);
    }

    // retrieves the logindata from SharedPreferences, and stores these in various MainActivity
    // fields
    private void setLoginDatafields() {
        loginSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);

        this.myUuid = loginSharedPreferences.getString("uuid", null);
        this.myUsername = loginSharedPreferences.getString("username", null);
        this.myEmail = loginSharedPreferences.getString("email", null);
        this.myFirstName = loginSharedPreferences.getString("firstName", "None");
        this.myLastName = loginSharedPreferences.getString("lastName", "None");
        this.myKey = loginSharedPreferences.getString("key", null);
        this.master = getResources().getString(R.string.master);
        this.myActivationCode = loginSharedPreferences.getString("activationCode", null);
    }

    // gets a JSONArray from the server, containing all registered games and their uuids. these are
    // stored in arrays, and a map is created between them
    private void setGamesDataFields() {
        RequestQueue requestQueue = RequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        requestQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/game/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    allGamesArray = new String[jsonArray.length()];
                    allGamesUUIDsArray = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject gameObject = jsonArray.getJSONObject(i);
                        String gameName = gameObject.getString("name");
                        String gameUUID = gameObject.getString("uuid");
                        allGamesMap.put(gameUUID, gameName);
                        allGamesArray[i] = gameName;
                        allGamesUUIDsArray[i] = gameUUID;
                    }
                } catch (JSONException exception) {
                    Log.e("JSONarray error", "Couldn't format the response string to JSON array");
                    exception.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }));
    }

    public String[] getAllGamesArray() {
        return allGamesArray;
    }

    public String[] getAllGamesUUIDsArray() {
        return allGamesUUIDsArray;
    }

    // retrieves the name belonging to a game with a specified uuid
    public String getGameNameFromUUID(String uuid) {
        return allGamesMap.get(uuid);
    }

    // retrieves the uuid of a given game
    public String getGameUUIDFromName(String name) throws ArrayIndexOutOfBoundsException {
        int indexOf = Arrays.asList(this.allGamesArray).indexOf(name);
        return allGamesUUIDsArray[indexOf];
    }

    // override that determines the app's behavior when the back button is pressed
    @Override
    public void onBackPressed() {
        // if the navigation drawer is open, it gets closed
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        // retrieves the currently running fragment. if it is an instance of FragServerBrowserFilter,
        // an instance of FragServerBrowser is immediately opened. otherwise, the previous fragment
        // id is used to open the previous fragment
        Fragment f = this.getFragmentManager().findFragmentById(R.id.MyFrameLayout);

        if (f instanceof FragServerBrowserFilter) {
            Fragment fragment = new FragServerBrowser();
            executeFragmentTransaction(fragment);
        } else if (previousFragId.size() > 1) {
            previousFragId.remove(0);
            DisplayFragment(previousFragId.get(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // code that was generated automatically
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // switches to a fragment based on the given (navigation drawer) id. unless an instance of
    // FragUser or FragContacts is opened, the TabLayout is hidden
    private void DisplayFragment(int id) {
        Fragment fragment = null;

        fgTabs.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.GONE);

        // closes the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        switch (id) {
            case R.id.nav_server_browser:
                fragment = new FragServerBrowser();
                break;
            case R.id.nav_profile:
                fragment = new FragUser();
                setTabNames("User");
                break;
            case R.id.nav_contacts:
                fragment = new FragContacts();
                setTabNames("Friends");
                break;
            case R.id.nav_settings:
                fragment = new FragPreferences();
                break;
            case R.integer.display_fragment_server_info_id:
                FragServerInfo fragServerInfo = new FragServerInfo();
                fragServerInfo.setServer(onBackServerObjects.get(0));
                onBackServerObjects.remove(0);
                executeFragmentTransaction(fragServerInfo);
                return;
            case R.integer.display_fragment_user_info_id:
                FragUserInfo fragUserInfo = new FragUserInfo();
                fragUserInfo.setUserUUID(onBackUUIDs.get(0));
                onBackUUIDs.remove(0);
                executeFragmentTransaction(fragUserInfo);
                return;
        }

        executeFragmentTransaction(fragment);
    }

    // method that opens the given fragment
    public void executeFragmentTransaction(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            ft.commit();
        }
    }

    // method that handles selections in the top right options menu, from which one can open the
    // settings screen or log out
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.action_settings) {
            fgTabs.setVisibility(View.GONE);
            fragment = new FragPreferences();
            previousFragId.add(0, R.id.nav_settings);
        } else if (id == R.id.action_logOut) {
            logOut();
        }

        executeFragmentTransaction(fragment);

        return super.onOptionsItemSelected(item);
    }

    // method that handles selections in the navigation drawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (previousFragId.size() > 0 && previousFragId.get(0) == id) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        previousFragId.add(0, id);
        DisplayFragment(id);
        return true;
    }

    // method that logs the current user out by clearing their userdata from SharedPreferences, and
    // moving them to an instance of AuthActivity
    private void logOut() {
        SharedPreferences.Editor loginEditor = loginSharedPreferences.edit();

        loginEditor.clear();
        loginEditor.apply();

        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void setTabNames(String s) {
        fgTabs.setVisibility(View.VISIBLE);
        if (s.equals("Friends")) {
            tab0.setText("Friends");
            tab1.setText("Pending");
            tab2.setText("Blocked");
        }
        if (s.equals("User")) {
            tab0.setText("Profile");
            tab1.setText("My servers");
            tab2.setText("Favourites");
        }
    }

    public String getMaster() {
        return master;
    }

    public String getMyUuid() {
        return myUuid;
    }

    public String getMyUsername() {
        return myUsername;
    }

    public String getMyEmail() {
        return myEmail;
    }

    public String getMyFirstName() {
        return myFirstName;
    }

    public String getMyLastName() {
        return myLastName;
    }

    public String getMyKey() {
        return myKey;
    }

    public String getMyActivationCode() {
        return myActivationCode;
    }

    public void setMyUsername(String username) {
        this.myUsername = username;
    }

    public void setMyEmail(String email) {
        this.myEmail = email;
    }

    public void setMyFirstName(String firstName) {
        this.myFirstName = firstName;
    }

    public void setMyLastName(String lastName) {
        this.myLastName = lastName;
    }
}
