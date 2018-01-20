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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public String completeServerListString;
    public String completeContactsListString;
    public String serverSearchFilter;
    public String master;
    public boolean hasServerListStored, hasFriendListStored;

    public String uuid, username, email, firstName, lastName, key, activactionCode, urlKey;

    public Integer[] playerCountLimit = new Integer[4];

    private String[] allGames, allGamesUUIDs;

    public SharedPreferences mainSharedPreferences;

    public TabLayout friendsTabs;
    public TabLayout.Tab tab0, tab1, tab2;

    public int friendsTabSelected, userTabSelected;

    private ArrayList<Integer> previousFragId;

    public ProgressBar mainProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previousFragId = new ArrayList<>();


        friendsTabs = findViewById(R.id.friendsTabs);
        tab0 = friendsTabs.getTabAt(0);
        tab1 = friendsTabs.getTabAt(1);
        tab2 = friendsTabs.getTabAt(2);

        playerCountLimit[0] = 0;
        playerCountLimit[1] = 999;
        playerCountLimit[2] = 0;
        playerCountLimit[3] = 999;

        setLoginDatafields();
        setGamesDataFields();

        mainProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // de default fragment (bij het opstarten) is home
        previousFragId.add(0, R.id.nav_home);
        DisplayFragment(R.id.nav_home);
    }

    private void setLoginDatafields() {
        mainSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);

        this.uuid = mainSharedPreferences.getString("uuid", null);
        this.username = mainSharedPreferences.getString("username", null);
        this.email = mainSharedPreferences.getString("email", null);
        this.firstName = mainSharedPreferences.getString("firstName", "None");
        this.lastName = mainSharedPreferences.getString("lastName", "None");
        this.key = mainSharedPreferences.getString("key", null);
        this.master = "d54e4e7f04284ffb8662be06337cd09f";
        this.activactionCode = mainSharedPreferences.getString("activationCode", null);

        this.urlKey = "&key=" + this.key;

        Log.d("mijn uuid", this.uuid);
    }

    private void setGamesDataFields() {
        RequestQueue requestQueue = RequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        requestQueue.add(new StringRequest(Request.Method.GET, "https://fewgamers.com/api/game/", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    allGames = new String[jsonArray.length()];
                    allGamesUUIDs = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        allGames[i] = jsonArray.getJSONObject(i).getString("name");
                        allGamesUUIDs[i] = jsonArray.getJSONObject(i).getString("uuid");
                    }
                } catch (JSONException exception) {
                    Log.e("JSONarray error", "Couldn't format the response string to JSON array");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }));
    }

    public String[] getAllGames() {
        return allGames;
    }

    public String[] getAllGamesUUIDs() {
        return allGamesUUIDs;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        Fragment f = this.getFragmentManager().findFragmentById(R.id.MyFrameLayout);

        if (f instanceof FragServerInfo || f instanceof FragServerBrowserFilter) {
            Fragment fragment = new FragServerBrowser();
            executeFragmentTransaction(fragment);
        } else if (f instanceof FragUserInfo) {
            Fragment fragment = new FragContacts();
            executeFragmentTransaction(fragment);
        } else if (previousFragId.size() > 1) {
            previousFragId.remove(0);
            DisplayFragment(previousFragId.get(0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // laat je switchen naar een andere fragment aan de hand van de opgegeven fragment id.
    private void DisplayFragment(int id) {
        Fragment fragment = null;

        friendsTabs.setVisibility(View.GONE);
        mainProgressBar.setVisibility(View.GONE);

        switch (id) {
            case R.id.nav_contacts:
                fragment = new FragContacts();
                setTabNames("Friends");
                break;
            case R.id.nav_home:
                fragment = new FragHome();
                break;
            case R.id.nav_profile:
                fragment = new FragUser();
                setTabNames("User");
                break;
            case R.id.nav_server_browser:
                fragment = new FragServerBrowser();
                break;
            case R.id.nav_settings:
                fragment = new FragPreferences();
                break;
        }

        executeFragmentTransaction(fragment);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void executeFragmentTransaction(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.MyFrameLayout, fragment);
            ft.commit();
        }
    }

    // hetzelfde als de nav view, maar zonder een aparte methode in de body.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar frienditem clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.action_settings) {
            fragment = new FragPreferences();
            previousFragId.add(0, R.id.nav_settings);
        } else if (id == R.id.action_logOut) {
            logOut();
        }

        executeFragmentTransaction(fragment);

        return super.onOptionsItemSelected(item);
    }

    // kijkt welke frienditem is aangeklikt. in DisplayFragment wordt het meeste werk gedaan.
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        previousFragId.add(0, id);
        DisplayFragment(id);
        return true;
    }

    private void logOut() {
        SharedPreferences loginSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = loginSharedPreferences.edit();

        loginEditor.clear();
        loginEditor.apply();

        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void setTabNames(String s) {
        friendsTabs.setVisibility(View.VISIBLE);
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
}
