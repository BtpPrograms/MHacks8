package brbsolutions.myo_muscle;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.squareup.otto.Subscribe;

import emgvisualizer.model.EventBusProvider;
import emgvisualizer.ui.*;
import emgvisualizer.ui.fragments.ControlFragment;
import emgvisualizer.ui.fragments.GraphFragment;
import emgvisualizer.ui.fragments.HomeFragment;
import emgvisualizer.ui.fragments.MyoListFragment;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DefaultItemAnimator;
/**
 * Main activity for handling main drawer menu with fragment changes
 * @author Nicola
 */
public class MainActivity extends AppCompatActivity {

    /** TAG for debugging purpose */
    private static final String TAG = "MainActivity";

    /** Arrays of menu title strings */
    private String TITLES[] = {
            "Home",
            "Thalmic Myo",
            "Search for Myo",
            "Control Panel",
            "Run Tests",
            "View History"};

    /** Array of icons reference ID, -1 if a divider */
    private int ICONS[] = {
            R.drawable.ic_home_grey600_24dp,
            -1,
            R.drawable.ic_magnify_grey600_24dp,
            R.drawable.ic_myo_grey600_24dp,
            R.drawable.ic_action,
            R.drawable.ic_toc_black_48dp};

    /** Constant for Home menu position */
    private static final int POSIT_HOME = 1;
    /** Constant for Search menu position */
    private static final int POSIT_SEARCH = 3;
    /** Constant for Control menu position */
    private static final int POSIT_CONTROL = 4;
    /** Constant for Graph menu position */
    private static final int POSIT_GRAPH = 5;

    private static final int POSIT_HISTORY = 6;
    /** REMOVE THIS LATER */
    private Data_Handler data_handler;
    /** App Toolbar */
    private Toolbar toolbar;
    /** Recycler view reference */
    private RecyclerView mRecyclerView;
    /** Menu adapter */
    private MenuAdapter mAdapter;
    /** Layout manager for Recycler view */
    private RecyclerView.LayoutManager mLayoutManager;
    /** Drawer Layout */
    private DrawerLayout mDrawerLayout;
    /** Drawer toogle */
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        /*
        Button button_begin_test = (Button) findViewById(R.id.begin_test);
        button_begin_test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("clicks","You Clicked Begin Test");
                Intent i=new Intent(
                        MainActivity.this,
                        Test_Runner.class);
                startActivity(i);
            }
        });

        Button button_view_history = (Button) findViewById(R.id.view_history);
        button_view_history.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("clicks","You Clicked Begin Test");
                /* Intent i=new Intent(
                        MainActivity.this,
                        Data_Display.class);
                startActivity(i);
                Intent i = new Intent(
                        MainActivity.this,
                        MainActivity2.class);
                startActivity(i);
                */
        //    }
//        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        //toolbar = (Toolbar) findViewById(R.id.main_tool_bar);
        //setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_left_menu);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new MenuAdapter(TITLES, ICONS);

        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        changeFragment(new HomeFragment(), POSIT_HOME);
        EventBusProvider.register(this);

        DatabaseHelper dbh = new DatabaseHelper(this);

        Routine hackerTests = new Routine("Hacker Ergonomics", "Clench fist:3", 1);
        dbh.storeRoutine(hackerTests);

        dbh.close();
    }

    /**
     * Private method for triggering a fragment switch at runtime
     * @param fragment Fragment involved
     * @param position Position of the menu to be selected
     */
    private void changeFragment(Fragment fragment, int position) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.main_content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        setTitle(TITLES[position - 1]);
        mAdapter.updateSelectedItem(position);
        mDrawerLayout.closeDrawers();
    }

    @Subscribe
    public void changeFragment(PositionEvent evt) {

        int position = evt.getPosition();

        // Myo alert message if not setted
        MySensorManager mngr = MySensorManager.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.no_myo));
        builder.setMessage(getString(R.string.you_must_perform_a_scan));
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Fragment fragment = new MyoListFragment();
                changeFragment(fragment, POSIT_SEARCH);
                mDrawerLayout.closeDrawers();
                dialogInterface.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDrawerLayout.closeDrawers();
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();

        Fragment fragment = null;
        // Menu selector
        switch (position) {
            case POSIT_HOME:
                fragment = new HomeFragment();
                break;
            case POSIT_SEARCH:
                fragment = new MyoListFragment();
                break;
            case POSIT_CONTROL:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    fragment = new ControlFragment();

                }
                break;
            case POSIT_GRAPH:
                if (!mngr.isMyoFound()) {
                    fragment = null;
                    alert.show();
                } else {
                    // Benny's messy stuff
                    fragment = new GraphFragment();
                    //data_handler = new Data_Handler(this);
                    //EventBusProvider.register(data_handler);
                    //Trial[] tempTrials = {data_handler.collectData(3000, 50)};

                }
                break;
            case POSIT_HISTORY:
                fragment = new HistoryFragment();
                break;
        }
        if (fragment != null) {
            changeFragment(fragment, position);
        }
    }


    /**
     * Public method for opening Myo control windows
     */
    public void changeFragmentMyoControl() {
        changeFragment(new ControlFragment(), POSIT_CONTROL);
    }
}
