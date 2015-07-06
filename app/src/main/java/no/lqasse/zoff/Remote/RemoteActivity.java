package no.lqasse.zoff.Remote;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import no.lqasse.zoff.Helpers.ScreenStateReceiver;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.ImageTools.BitmapColor;
import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.LoadingAnimation;
import no.lqasse.zoff.ZoffController;
import no.lqasse.zoff.Models.Zoff;
import no.lqasse.zoff.Models.ZoffSettings;
import no.lqasse.zoff.PlaylistFragment;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Search.SearchFragment;
import no.lqasse.zoff.SettingsFragment;
import no.lqasse.zoff.ZoffActivity;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ZoffActivity implements SettingsFragment.Listener, SearchFragment.Host, PlaylistFragment.Host {
    private static final String LOG_IDENTIFIER = "RemoteActivity";

    private SettingsFragment settingsFragment;
    private SearchFragment searchFragment;
    private PlaylistFragment playlistFragment;
    private FragmentManager fragmentManager;
    private Zoff zoff;

    private Menu menu;

    private RelativeLayout mainLayout;
    private DrawerLayout drawerLayout;
    private android.support.v7.widget.Toolbar toolBar;
    private EditText toolBarSearchField;
    private TextView toolBarTitle;
    private ProgressBar loadingProgressbar;
    private ActionBarDrawerToggle drawerToggle;
    private LoadingAnimation loadingAnimation;


    private Handler handler = new Handler();

    private Runnable listUpdater = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 250);
        }
    };


    private boolean isBigScreen = false;
    private boolean isNewChannel = false;
    private boolean isInBackground = false;
    private boolean screenOrientationChanged = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_remote);
        toolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);
        handler.post(listUpdater);

        fragmentManager = getFragmentManager();



        log("onCreate");

        Intent i = getIntent();
        Bundle b = i.getExtras();

        if (b != null && b.containsKey(ZoffController.BUNDLEKEY_CHANNEL)) {

            channel = b.getString(ZoffController.BUNDLEKEY_CHANNEL);
            isNewChannel = b.getBoolean(ZoffController.BUNDLEKEY_IS_NEW_CHANNEL, false);


        }



        zoffController = ZoffController.getInstance(channel, this);


        zoff = zoffController.getZoff();
        setControllerCallbacks(zoffController);

        zoffController.refreshPlaylist();
        displayPlaylistFragment();






        drawerLayout = (DrawerLayout) findViewById(R.id.topLayout);
        mainLayout = (RelativeLayout) findViewById(R.id.listContainer);
        toolBarSearchField = (EditText) findViewById(R.id.tool_bar_search_edittext);
        toolBarTitle = (TextView) findViewById(R.id.toolbarTitle);
        loadingAnimation = (LoadingAnimation) findViewById(R.id.activity_remote_loading_animation);
        isBigScreen = checkIsBigScreen();


        zoffController.setPlaylistCallback(new ZoffController.PlaylistCallback() {
            @Override
            public void onGotPlaylist() {
                loadingAnimation.setVisibility(View.INVISIBLE);
            }
        });




        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.remote_drawer_open, R.string.remote_drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }


        };


        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();


        if (isBigScreen) {


            final ImageView skip = (ImageView) findViewById(R.id.skipButton);
            ImageView settings = (ImageView) findViewById(R.id.settingsButton);
            ImageView shuffle = (ImageView) findViewById(R.id.shuffleButton);
            ImageView search = (ImageView) findViewById(R.id.searchButton);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {

                        case R.id.skipButton:
                            handleEvent(event_type.skip);
                            break;
                        case R.id.settingsButton:
                            handleEvent(event_type.settings);
                            break;
                        case R.id.shuffleButton:
                            zoffController.shuffle();
                            break;
                        case R.id.searchButton:
                            displaySearchFragment();
                            break;

                    }
                }
            };

            skip.setOnClickListener(onClickListener);
            settings.setOnClickListener(onClickListener);
            shuffle.setOnClickListener(onClickListener);
            search.setOnClickListener(onClickListener);


        }


    }

    private void refreshViewData(Zoff zoff) {

        playlistFragment.notifyDataChange(zoff);
        settingsFragment.setSettings(zoff.getSettings());

        setBackgroundImage(zoff.getPlayingVideo().getId());

        if (!ImageCache.has(zoff.getNextVideo().getId(), ImageCache.ImageSize.HUGE)) {
            BitmapDownloader.download(zoff.getNextVideo().getId(), ImageCache.ImageSize.HUGE, true, null);
        }


    }




    @Override
    protected void onResume() {
        super.onResume();
        stopNotificationService();


        zoffController = ZoffController.getInstance(channel,this);
        zoff = zoffController.getZoff();
        refreshViewData(zoff);
        toolBarTitle.setText(zoff.getChannelRaisedFirstLetter());

        setControllerCallbacks(zoffController);



    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isInBackground) {
            isInBackground = false;

        }

        if ((ScreenStateReceiver.wasScreenOn || homePressed) && !backPressed && !screenOrientationChanged) {
            startNotificationService();
            backPressed = false;

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        this.menu = menu;


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case (R.id.action_skip):
                zoffController.skip();

                break;
            case (R.id.action_search):
                displaySearchFragment();


                break;


            case (R.id.action_shuffle):
                zoffController.shuffle();

                break;
            case (R.id.action_play):

                break;
            case (R.id.action_close_searchfield):

                if (toolBarSearchField.getText().toString().equals("")) {
                    fragmentManager.popBackStack();
                } else {
                    toolBarSearchField.setText("");
                }

        }


        return super.onOptionsItemSelected(item);
    }


    private void displaySearchFragment() {
        searchFragment = new SearchFragment();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.listContainer, searchFragment)
                .commit();
    }

    private void displayPlaylistFragment(){
        playlistFragment = new PlaylistFragment();
        fragmentManager
                .beginTransaction()
                .replace(R.id.listContainer, playlistFragment)
                .commit();

    }

    private void setControllerCallbacks(ZoffController controller){
        controller.setOnRefreshListener(new ZoffController.RefreshCallback() {
            @Override
            public void onZoffRefreshed(Zoff zoff) {
                refreshViewData(zoff);
                playlistFragment.notifyDataChange(zoff);

            }
        });


        zoffController.setCorrectPasswordCallback(new ZoffController.CorrectPasswordCallback() {
            @Override
            public void onCorrectPassword(String password) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                settingsFragment.enableSettings();
                playlistFragment.invalidateListviewViews();
            }
        });

        zoffController.setToastMessageCallback(new ZoffController.ToastMessageCallback() {
            @Override
            public void onToastReceived(String toastkeyword) {
                ToastMaster.showToast(RemoteActivity.this, toastkeyword);
            }
        });

    }

    private void setToolbarBackground(){
        if (ImageCache.has(zoff.getPlayingVideo().getId(), ImageCache.ImageSize.BACKGROUND)){

            Bitmap currentBackground = ImageCache.get(zoff.getPlayingVideo().getId(), ImageCache.ImageSize.BACKGROUND);
            BitmapDrawable drawable = BitmapColor.darkenBitmap(currentBackground);
            toolBar.setBackground(drawable);
            //drawable.setAlpha(155);
        } else {
            ImageCache.registerListener(zoff.getPlayingVideo().getId(), ImageCache.ImageSize.BACKGROUND, new ImageCache.ImageInCacheListener() {
                @Override
                public void ImageInCache(Bitmap image) {
                    BitmapDrawable drawable = BitmapColor.darkenBitmap(image);
                    //drawable.setAlpha(155);
                    toolBar.setBackground(drawable);
                }
            });
        }

    }


    @Override
    public void onBackPressed() {
        homePressed = false;
        backPressed = true;

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            zoffController.disconnect();
            finish();
            super.onBackPressed();
        }


    }

    @Override
    public void saveSettings(ZoffSettings settings) {
        zoffController.saveSettings(settings);
    }

    @Override
    public void setFragment(SettingsFragment fragment) {
        settingsFragment = fragment;
    }

    @Override
    public void savePassword(String password) {
        zoffController.savePassword(password);
    }


    @Override
    protected void onUserLeaveHint() {

        homePressed = true;
        super.onUserLeaveHint();
    }


    @Override
    protected void onDestroy() {
        if (!screenOrientationChanged){
            zoffController.disconnect();
        }

        super.onDestroy();
    }

    private void log(String log) {
        Log.i(LOG_IDENTIFIER, log);
    }

    @Override
    public String toString() {
        return "RemoteActivity";
    }

    private boolean checkIsBigScreen() {
        if (findViewById(R.id.listContainer).getTag() != null) {
            return (findViewById(R.id.listContainer).getTag().equals("big_screen"));
        }

        return false;
    }

    @Override
    public Toolbar getToolbar() {
        return toolBar;
    }

    @Override
    public ZoffController getZoffController() {
        return zoffController;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        screenOrientationChanged = true;

        super.onConfigurationChanged(newConfig);
    }
}





