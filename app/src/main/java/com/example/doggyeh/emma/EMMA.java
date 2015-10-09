package com.example.doggyeh.emma;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;
import android.support.v7.widget.Toolbar;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

//Main activity, home page

public class EMMA extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    static int prev = 1;    //last selected navigation drawer item
    public static Toolbar toolbar;
    String[] menuarray;     //items for navigation drawer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emm);
        menuarray = getResources().getStringArray(R.array.string_array);
        toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if(position == 0)
            return;
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
        if (prev != position){
            FrameLayout container = (FrameLayout)findViewById(R.id.container);
            if(container!=null && container.getChildCount()>0)
                container.removeAllViewsInLayout();
        }
        prev = position;
    }

    public void onSectionAttached(int number) {

        switch (number) {
            case 2:
                //mTitle = getString(R.string.title_section1);
                mTitle = "EMMA";
                View inflatedView = View.inflate(this, R.layout.activity_main, null);
                FrameLayout container = (FrameLayout)findViewById(R.id.container);
                container.addView(inflatedView);

                //Add animation for DEMO
                Animation scale = AnimationUtils.loadAnimation(this, R.anim.scale);
                /*
                scale.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        Animation scale = AnimationUtils.loadAnimation(EMMA.this, R.anim.scale);
                        scale.setAnimationListener(this);
                        findViewById(R.id.titleImage2).startAnimation(scale);
                    }
                    @Override
                    public void onAnimationRepeat(Animation arg0) {
                    }
                    @Override
                    public void onAnimationStart(Animation arg0) {
                    }
                });
                */
                findViewById(R.id.titleImage2).startAnimation(scale);

                Animation fade = AnimationUtils.loadAnimation(this, R.anim.fade);
                findViewById(R.id.text1).startAnimation(fade);
                findViewById(R.id.text2).startAnimation(fade);

                break;
            /*
            case 2:
                //mTitle = getString(R.string.title_section2);
                break;
            */
            default:
                mTitle = menuarray[number-1];
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
        //actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionbar_title_color)));
        //actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        actionBar.setElevation(0);
        actionBar.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Log.d("Robin", "" + mTitle);
        if (mTitle.equals(getString(R.string.title_section1))){
            getMenuInflater().inflate(R.menu.global, menu);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);
            return true;
        }
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.emm, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_emm, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((EMMA) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
    private static final String KEY_ID = "ViewTransitionValues:id";
    public void clicked(View v) {
        //mNavigationDrawerFragment.mDrawerLayout.openDrawer(findViewById(R.id.navigation_drawer));

        Intent intent = new Intent(this, MessageActivity.class);
        startActivity(intent);
        overridePendingTransition(R.transition.slide_in_left,R.transition.slide_out_left);

    }
}
