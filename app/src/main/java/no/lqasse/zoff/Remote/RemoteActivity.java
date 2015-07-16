package no.lqasse.zoff.Remote;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import no.lqasse.zoff.Helpers.ScreenStateReceiver;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.ImageTools.BackgroundGenerator;
import no.lqasse.zoff.ImageTools.BitmapCache;
import no.lqasse.zoff.ImageTools.BitmapColor;
import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.LoadingAnimation;
import no.lqasse.zoff.Models.Settings;
import no.lqasse.zoff.Models.Zoff;
import no.lqasse.zoff.Notification.NotificationService;
import no.lqasse.zoff.PlaylistFragment;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Search.SearchFragment;
import no.lqasse.zoff.SettingsFragment;
import no.lqasse.zoff.ZoffController;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ActionBarActivity implements SettingsFragment.Listener, SearchFragment.Host, PlaylistFragment.Host {
    private static final String LOG_IDENTIFIER = "RemoteActivity";

    private SettingsFragment settingsFragment;
    private SearchFragment searchFragment;
    private PlaylistFragment playlistFragment;
    private FragmentManager fragmentManager;
    private Zoff zoff;

    private DrawerLayout drawerLayout;
    private android.support.v7.widget.Toolbar toolBar;
    private EditText toolBarSearchField;
    private TextView toolBarTitle;
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
    private boolean wasHomePressed = true;
    private boolean wasBackPressed = false;
    private boolean wasPowerPressed = false;
    private boolean isInBackground = false;
    private boolean wasOrientationChanged = false;
    private String channel;
    private ZoffController zoffController;
    private Bitmap currentBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        toolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        handler.post(listUpdater);
        fragmentManager = getFragmentManager();
        Intent i = getIntent();
        Bundle b = i.getExtras();

        if (b != null && b.containsKey(ZoffController.BUNDLEKEY_CHANNEL)) {
            channel = b.getString(ZoffController.BUNDLEKEY_CHANNEL);
            isNewChannel = b.getBoolean(ZoffController.BUNDLEKEY_IS_NEW_CHANNEL, false);
        }

        zoffController = ZoffController.getInstance(channel);
        zoff = zoffController.getZoff();
        setControllerCallbacks(zoffController);
        zoffController.refreshPlaylist();
        displayPlaylistFragment();

        drawerLayout = (DrawerLayout) findViewById(R.id.topLayout);
        toolBarSearchField = (EditText) findViewById(R.id.tool_bar_search_edittext);
        toolBarTitle = (TextView) findViewById(R.id.toolbarTitle);
        loadingAnimation = (LoadingAnimation) findViewById(R.id.activity_remote_loading_animation);
        isBigScreen = checkIsBigScreen();

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
    }

    private void refreshViewData(Zoff zoff) {
        playlistFragment.notifyDataChange(zoff);
        settingsFragment.setSettings(zoff.getSettings());

        setBackground(zoffController.getCurrentlyPlayingVideo().getId());

        if (!BitmapCache.has(zoff.getNextVideoId(), BitmapCache.ImageSize.HUGE)) {
            BitmapDownloader.download(zoff.getNextVideoId(), BitmapCache.ImageSize.HUGE, true, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationService.stop(this);
        zoffController = ZoffController.getInstance(channel);
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

        if ((ScreenStateReceiver.wasScreenOn || wasHomePressed || wasPowerPressed) && !wasBackPressed && !wasOrientationChanged) {
            NotificationService.start(this, channel);
            wasBackPressed = false;
            wasPowerPressed = false;
            wasHomePressed = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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

    private void displayPlaylistFragment() {
        playlistFragment = new PlaylistFragment();
        fragmentManager
                .beginTransaction()
                .replace(R.id.listContainer, playlistFragment)
                .commit();
    }

    private void setControllerCallbacks(ZoffController controller) {
        controller.setOnRefreshListener(new ZoffController.RefreshCallback() {
            @Override
            public void onZoffRefreshed(Zoff zoff) {
                refreshViewData(zoff);
                playlistFragment.notifyDataChange(zoff);
            }
        });

        controller.setCorrectPasswordCallback(new ZoffController.CorrectPasswordCallback() {
            @Override
            public void onCorrectPassword(String password) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                settingsFragment.enableSettings();
                playlistFragment.invalidateListviewViews();
            }
        });

        controller.setToastMessageCallback(new ZoffController.ToastMessageCallback() {
            @Override
            public void showToast(String toastkeyword) {
                ToastMaster.showToast(RemoteActivity.this, toastkeyword);
            }

            @Override
            public void showToast(ToastMaster.TYPE type, String contextual) {
                ToastMaster.showToast(RemoteActivity.this, type, contextual);
            }
        });

        controller.setPlaylistCallback(new ZoffController.PlaylistCallback() {
            @Override
            public void onGotPlaylist() {
                loadingAnimation.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void setToolbarBackground() {
        if (BitmapCache.has(zoff.getPlayingVideo().getId(), BitmapCache.ImageSize.BACKGROUND)) {

            Bitmap currentBackground = BitmapCache.get(zoff.getPlayingVideo().getId(), BitmapCache.ImageSize.BACKGROUND);
            BitmapDrawable drawable = BitmapColor.darkenBitmap(currentBackground);
            toolBar.setBackground(drawable);
            //drawable.setAlpha(155);
        } else {
            BitmapCache.registerListener(zoff.getPlayingVideo().getId(), BitmapCache.ImageSize.BACKGROUND, new BitmapCache.ImageInCacheListener() {
                @Override
                public void ImageInCache(Bitmap image) {
                    BitmapDrawable drawable = BitmapColor.darkenBitmap(image);
                    //drawable.setAlpha(155);
                    toolBar.setBackground(drawable);
                }
            });
        }
    }


    private void setBackground(final String videoId) {

        final ImageView background = (ImageView) findViewById(R.id.backgroundImage);
        final ImageView oldBackground = (ImageView) findViewById(R.id.backgroundImageOLD);

        if (BitmapCache.has(videoId, BitmapCache.ImageSize.BACKGROUND)) {

            Bitmap nextBackgroundImage = BitmapCache.get(videoId, BitmapCache.ImageSize.BACKGROUND);
            if (currentBackground != nextBackgroundImage) {
                final Bitmap bg = nextBackgroundImage;
                if (currentBackground != null) {
                    fadeInNewBackgroundBitmap(nextBackgroundImage);
                } else {
                    Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
                    fadeIN.setInterpolator(new DecelerateInterpolator());
                    fadeIN.setDuration(1000);
                    background.setImageBitmap(bg);
                    background.setAnimation(fadeIN);
                    fadeIN.start();
                }
                currentBackground = nextBackgroundImage;
            }

        } else if (BitmapCache.has(videoId)) {
            BackgroundGenerator.generateBackground(BitmapCache.get(videoId), videoId, new BackgroundGenerator.Callback() {
                @Override
                public void onBackgroundCreated(Bitmap bitmap) {
                    fadeInNewBackgroundBitmap(bitmap);
                }
            });

        } else {
            BitmapDownloader.download(videoId, BitmapCache.ImageSize.REG, true, new BitmapDownloader.Callback() {
                @Override
                public void onImageDownloaded(Bitmap image, BitmapCache.ImageSize type) {
                    BackgroundGenerator.generateBackground(image, videoId, new BackgroundGenerator.Callback() {
                        @Override
                        public void onBackgroundCreated(Bitmap bitmap) {
                            fadeInNewBackgroundBitmap(bitmap);
                        }
                    });
                }
            });
        }
    }

    private void fadeInNewBackgroundBitmap(Bitmap nextBackgroundBitmap) {
        final ImageView background = (ImageView) findViewById(R.id.backgroundImage);
        final ImageView oldBackground = (ImageView) findViewById(R.id.backgroundImageOLD);
        oldBackground.setImageBitmap(currentBackground);
        Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
        fadeIN.setInterpolator(new DecelerateInterpolator());
        fadeIN.setDuration(1000);
        background.setVisibility(View.INVISIBLE);
        background.setImageBitmap(nextBackgroundBitmap);
        background.setAnimation(fadeIN);
        fadeIN.start();
        background.setVisibility(View.VISIBLE);
        currentBackground = nextBackgroundBitmap;
    }


    @Override
    public void saveSettings(Settings settings) {
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
    public String toString() {
        return "RemoteActivity";
    }

    private boolean checkIsBigScreen() {
        if (drawerLayout.getTag()!=null){
            return drawerLayout.getTag().equals("big_screen");
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
        wasOrientationChanged = true;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        wasHomePressed = false;
        wasBackPressed = true;

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            zoffController.disconnect();
            finish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        wasHomePressed = true;
        super.onUserLeaveHint();
    }


    @Override
    protected void onDestroy() {
        NotificationService.stop(this);
        if (!wasOrientationChanged) {
            zoffController.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER){
            wasPowerPressed = true;
        }
        return super.onKeyDown(keyCode, event);
    }
}





