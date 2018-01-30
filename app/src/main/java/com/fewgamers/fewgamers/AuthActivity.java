package com.fewgamers.fewgamers;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

// activity for logging in and registering. the two functions are located in different fragments
public class AuthActivity extends AppCompatActivity {

    private SectionsPagerAdapter mySectionsPagerAdapter;
    private ViewPager myViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // automatically generated code
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mySectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        myViewPager = (ViewPager) findViewById(R.id.container);
        myViewPager.setAdapter(mySectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.authTabs);

        myViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(myViewPager));
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AuthFragLogin();
                case 1:
                    return new AuthFragRegister();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
