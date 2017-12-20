package com.fewgamers.fgmockup;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // de default fragment (bij het opstarten) is home
        DisplayFragment(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        Fragment f = this.getFragmentManager().findFragmentById(R.id.MyFrameLayout);

        if (f instanceof FragServerInfo) {
            Fragment fragment = new FragServerBrowser();
            executeFragmentTransaction(fragment);
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

        switch (id) {
            case R.id.nav_blocked:
                fragment = new FragBlocked();
                break;
            case R.id.nav_favourites:
                fragment = new FragFavourites();
                break;
            case R.id.nav_friends:
                fragment = new FragFriends();
                break;
            case R.id.nav_home:
                fragment = new FragHome();
                break;
            case R.id.nav_myservers:
                fragment = new FragMyServers();
                break;
            case R.id.nav_profile:
                fragment = new FragProfile();
                break;
            case R.id.nav_server_browser:
                fragment = new FragServerBrowser();
                break;
            case R.id.nav_settings:
                fragment = new FragSettings();
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

        if (id == R.id.action_exit0 || id == R.id.action_exit1) {
            finish();
        } else if (id == R.id.action_settings0 || id == R.id.action_settings1) {
            fragment = new FragSettings();
        } else if (id == R.id.action_profile) {
            fragment = new FragProfile();
        } else if (id == R.id.action_logOut) {
            logOut();
        }

            executeFragmentTransaction(fragment);

            return super.onOptionsItemSelected(item);
        }

        // kijkt welke frienditem is aangeklikt. in DisplayFragment wordt het meeste werk gedaan.
        @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected (MenuItem item){

            DisplayFragment(item.getItemId());
            return true;
        }

        private void logOut() {
            SharedPreferences loginSharedPreferences = getSharedPreferences("LoginData", Context.MODE_PRIVATE);
            SharedPreferences.Editor loginEditor = loginSharedPreferences.edit();

            loginEditor.putBoolean("stayLogged", false);
            loginEditor.commit();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
