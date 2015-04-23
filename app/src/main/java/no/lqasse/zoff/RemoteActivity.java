package no.lqasse.zoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Map;

import no.lqasse.zoff.Adapters.ListAdapter;
import no.lqasse.zoff.Adapters.ListAdapterWPlaying;
import no.lqasse.zoff.ImageTools.ImageBlur;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.ImageDownload;
import no.lqasse.zoff.Interfaces.ImageListener;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Adapters.SearchResultListAdapter;
import no.lqasse.zoff.Search.YouTube;
import no.lqasse.zoff.Interfaces.YouTubeListener;
import no.lqasse.zoff.Helpers.SpotifyServer;
import no.lqasse.zoff.Models.Zoff;
import no.lqasse.zoff.Interfaces.ZoffListener;
import no.lqasse.zoff.Helpers.ToastMaster;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ZoffActivity implements ZoffListener,YouTubeListener,ImageListener {
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";
    private final String LOG_IDENTIFIER = "RemoteActivity";

    private Menu menu;

    private ListView                videoList;
    private DrawerLayout            drawerLayout;
    private ListAdapter             listAdapter;
    private SearchResultListAdapter searchAdapter;
    private TextView                passwordField;
    private android.support.v7.widget.Toolbar toolBar;
    private EditText                toolBarSearchField;
    private ProgressBar             loadingProgressbar;

    private Switch voteSwitch;
    private Switch addsongsSwitch;
    private Switch longsongsSwitch;
    private Switch frontpageSwitch;
    private Switch allvideosSwitch;
    private Switch removeplaySwitch;
    private Switch skipSwitch;
    private Switch shuffleSwitch;
    private TextView voteDescription;
    private TextView addsongsDescription;
    private TextView longsongsDescription;
    private TextView frontpageDescription;
    private TextView allvideosDescription;
    private TextView removeplayDescription;
    private TextView skipDescription;
    private TextView shuffleDescription;

    private String voteEnabled;
    private String addsongsEnabled;
    private String longsongsEnabled;
    private String frontpageEnabled;
    private String allvideosEnabled;
    private String removeplayEnabled;
    private String skipEnabled;
    private String shuffleEnabled;
    private String voteDisabled;
    private String addsongsDisabled;
    private String longsongsDisabled;
    private String frontpageDisabled;
    private String allvideosDisabled;
    private String removeplayDisabled;
    private String skipDisabled;
    private String shuffleDisabled;


    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private Bitmap currentPlayingImage;

    private ActionBarDrawerToggle drawerToggle;


    private Handler h = new Handler();
    private Runnable r;
    private Handler handler = new Handler();
    private Runnable delaySearch = new Runnable() {
        @Override
        public void run() {
            YouTube.search(RemoteActivity.this, toolBarSearchField.getText().toString(), zoff.allVideosAllowed(), zoff.LONG_SONGS());
            loadingProgressbar.setVisibility(View.VISIBLE);
        }
    };

    private boolean BIG_SCREEN = false;



    private boolean appInBackGround = false;
    private boolean searchViewOpen = false;
    private final int AUTOSEARCH_DELAY_MILLIS = 600;




    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        toolBar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        log("onCreate");

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null && b.containsKey("ROOM_NAME")) {

            ROOM_NAME = b.getString("ROOM_NAME");


        }




        zoff = new Zoff(ROOM_NAME,this);






        //Find views
        videoList           = (ListView) findViewById(R.id.videoList);
        drawerLayout        = (DrawerLayout) findViewById(R.id.drawerLayout);
        passwordField       = (TextView) findViewById(R.id.passwordField);
        toolBarSearchField  = (EditText) findViewById(R.id.tool_bar_search_edittext);
        loadingProgressbar  = (ProgressBar) findViewById(R.id.loadingProgressbar);

        voteSwitch          = (Switch) findViewById(R.id.vote);
        addsongsSwitch      = (Switch) findViewById(R.id.addsongs);
        longsongsSwitch     = (Switch) findViewById(R.id.longsongs);
        frontpageSwitch     = (Switch) findViewById(R.id.frontpage);
        allvideosSwitch     = (Switch) findViewById(R.id.allvideos);
        removeplaySwitch    = (Switch) findViewById(R.id.removeplay);
        skipSwitch          = (Switch) findViewById(R.id.skip);
        shuffleSwitch       = (Switch) findViewById(R.id.shuffle);

        voteDescription     = (TextView) findViewById(R.id.voteLabel);
        addsongsDescription = (TextView) findViewById(R.id.addsongsLabel);
        longsongsDescription= (TextView) findViewById(R.id.longsongsLabel);
        frontpageDescription= (TextView) findViewById(R.id.frontpageLabel);
        allvideosDescription= (TextView) findViewById(R.id.allvideosLabel);
        removeplayDescription=(TextView) findViewById(R.id.removeplayLabel);
        skipDescription     = (TextView) findViewById(R.id.skipLabel);
        shuffleDescription  = (TextView) findViewById(R.id.shuffleLabel);

        voteSwitch.setEnabled(false);
        addsongsSwitch.setEnabled(false);
        allvideosSwitch.setEnabled(false);
        longsongsSwitch.setEnabled(false);
        frontpageSwitch.setEnabled(false);
        removeplaySwitch.setEnabled(false);
        skipSwitch.setEnabled(false);
        shuffleSwitch.setEnabled(false);

        voteEnabled         = getResources().getString(R.string.switch_vote_description_enabled);
        addsongsEnabled     = getResources().getString(R.string.switch_addsongs_description_enabled);
        longsongsEnabled    = getResources().getString(R.string.switch_longsongs_description_enabled);
        frontpageEnabled    = getResources().getString(R.string.switch_frontpage_description_enabled);
        allvideosEnabled    = getResources().getString(R.string.switch_allvideos_description_enabled);
        removeplayEnabled   = getResources().getString(R.string.switch_removeplay_description_enabled);
        skipEnabled         = getResources().getString(R.string.switch_skip_description_enabled);
        shuffleEnabled      = getResources().getString(R.string.switch_shuffle_description_enabled);
        voteDisabled        = getResources().getString(R.string.switch_vote_description_disabled);
        addsongsDisabled    = getResources().getString(R.string.switch_addsongs_description_disabled);
        longsongsDisabled   = getResources().getString(R.string.switch_longsongs_description_disabled);
        frontpageDisabled   = getResources().getString(R.string.switch_frontpage_description_disabled);
        allvideosDisabled   = getResources().getString(R.string.switch_allvideos_description_disabled);
        removeplayDisabled  = getResources().getString(R.string.switch_removeplay_description_disabled);
        skipDisabled        = getResources().getString(R.string.switch_skip_description_disabled);
        shuffleDisabled     = getResources().getString(R.string.switch_shuffle_description_disabled);



        for (CheckBox cb :checkBoxes) {
            cb.setEnabled(false);
        }


        //Listeners
        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO){
                    zoff.savePassword(passwordField.getText().toString());
                }

                return true;
            }
        });

        //Set up drawertoggle




        if (findViewById(R.id.layout).getTag() != null){
            BIG_SCREEN = (findViewById(R.id.layout).getTag().equals("big_screen"));
        }

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getChannelName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolBar,R.string.remote_drawer_open,R.string.remote_drawer_closed){
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


        //Autosearch in searchfield listener
        toolBarSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


                Boolean textFieldEmpty = s.toString().equals("");


                if (s.toString().contains("open.spotify.com/track/")){
                    SpotifyServer.getSearchString(s.toString(),toolBarSearchField);
                }

                handler.removeCallbacks(delaySearch);
                if (!textFieldEmpty) {

                    handler.postDelayed(delaySearch, AUTOSEARCH_DELAY_MILLIS);

                }





            }
        });






        //Handle everything listview related
        if (BIG_SCREEN){
            listAdapter = new ListAdapter(this, zoff.getNextVideos(), zoff);


            final ImageView skip = (ImageView)findViewById(R.id.skipButton);
            ImageView settings = (ImageView)findViewById(R.id.settingsButton);
            ImageView shuffle = (ImageView)findViewById(R.id.shuffleButton);
            ImageView search = (ImageView)findViewById(R.id.searchButton);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()){

                        case R.id.skipButton:
                            handleEvent(event_type.skip);
                            break;
                        case R.id.settingsButton:
                            handleEvent(event_type.settings);
                            break;
                        case R.id.shuffleButton:
                            handleEvent(event_type.shuffle);
                            break;
                        case R.id.searchButton:
                            toggleSearchLayout();
                            break;

                    }
                }
            };

            skip.setOnClickListener(onClickListener);
            settings.setOnClickListener(onClickListener);
            shuffle.setOnClickListener(onClickListener);
            search.setOnClickListener(onClickListener);



        } else {
            listAdapter = new ListAdapterWPlaying(this, zoff.getVideos(), zoff);
        }

        searchAdapter = new SearchResultListAdapter(this, YouTube.getSearchResults());

        videoList.setAdapter(listAdapter);


        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                searchViewOpen = toolBarSearchField.getVisibility() == View.VISIBLE;


                if (searchViewOpen) {
                    String videoTitle = YouTube.getSearchResults().get(position).getTitle();
                    String videoID = (YouTube.getSearchResults().get(position)).getVideoID();
                    String duration = (YouTube.getSearchResults().get(position)).getDuration();

                    zoff.add(videoID, videoTitle, duration);
                    //ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.VIDEO_ADDED, videoTitle);

                } else if (position != 0) { //Cant vote for current video duh
                    Video selectedVideo = listAdapter.getItem(position);
                    zoff.vote(selectedVideo);

                }


                return true;
            }
        });

        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (searchViewOpen) {
                    ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.HOLD_TO_ADD);

                } else if (position != 0) { //Currently playing video cant be voted on
                    ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.HOLD_TO_VOTE);
                }


            }
        });





        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (videoList.getLastVisiblePosition() == YouTube.getSearchResults().size()-10){

                    YouTube.getNextPage(RemoteActivity.this);
                    Log.d("Scroll", "Loading next page");

                }
            }
        });


    }

    @Override
    protected void onResume() {
        stopNotificationService();
        zoff.ping(this);
        super.onResume();
    }

    @Override
    protected void onPause() {

        if (appInBackGround) {
            appInBackGround = false;

        }


        super.onPause();


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
                zoff.skip();

                break;
            case (R.id.action_search):
                toggleSearchLayout();


                break;


            case (R.id.action_shuffle):

                break;
            case (R.id.action_play):
                homePressed = false;
                Intent playerIntent = new Intent(this, PlayerActivity.class);
                playerIntent.putExtra("ROOM_NAME", zoff.getChannelName());
                startActivity(playerIntent);
                break;
            case (R.id.action_close_searchfield):
                if (toolBarSearchField.getText().toString().equals("")){
                    toggleSearchLayout();
                } else {
                    toolBarSearchField.setText("");
                }

        }


        return super.onOptionsItemSelected(item);
    }

    public void notifySearchResultChange() {
        loadingProgressbar.setVisibility(View.INVISIBLE);
        searchAdapter.notifyDataSetChanged();
    }


    //Communication from ZOFF instance START

    public void onViewersChanged(){
        listAdapter.notifyDataSetChanged();
    }

    public void onZoffRefreshed() {
        loadingProgressbar.setVisibility(View.INVISIBLE);
        listAdapter.notifyDataSetChanged();

        if (BIG_SCREEN){
            TextView titleText = (TextView)findViewById(R.id.videoTitleView);
            TextView skipsText = (TextView)findViewById(R.id.skipsLabel);
            TextView viewsText = (TextView)findViewById(R.id.viewsLabel);

            titleText.setText(zoff.getNowPlayingTitle());
            skipsText.setText(zoff.getSkips());
            viewsText.setText(zoff.getViewersCount());

            ImageView imageView = (ImageView) findViewById(R.id.imageView);

            String imageURL = zoff.getNowPlayingVideo().getThumbHuge();
            String altImageUrl = zoff.getNowPlayingVideo().getThumbMed();
            String videoID = zoff.getNowPlayingID();



            if (!ImageCache.has(zoff.getNowPlayingID(), ImageCache.ImageType.HUGE)){
                ImageDownload.downloadAndSet(imageURL, altImageUrl, videoID, imageView, ImageCache.ImageType.HUGE);
            }else if (currentPlayingImage != ImageCache.get(zoff.getNowPlayingID(), ImageCache.ImageType.HUGE)){
                imageView.setImageBitmap(ImageCache.get(zoff.getNowPlayingID(), ImageCache.ImageType.HUGE));
            }



        }



        //Set background image

        Video currentVideo = zoff.getNowPlayingVideo();

            if (ImageCache.has(currentVideo.getId()) && !ImageCache.has(currentVideo.getId(), ImageCache.ImageType.BLUR)){
                ImageBlur.createAndSetBlurBG(ImageCache.get(currentVideo.getId()), this, currentVideo.getId());

            }else if (ImageCache.has(currentVideo.getId(), ImageCache.ImageType.BLUR)){
                setBackgroundImage(ImageCache.get(currentVideo.getId(), ImageCache.ImageType.BLUR));
            } else {
                ImageDownload.downloadToCache(currentVideo.getId());
                ImageCache.registerImageListener(this, currentVideo.getId());
            }

        if (!ImageCache.has(zoff.getNextId(), ImageCache.ImageType.HUGE)){
            ImageDownload.downloadToCache(zoff.getNextId(), ImageCache.ImageType.HUGE);


        }

        //Handle settings

        Map settings = zoff.getSettings();

        //Switches are inverted to reflect positive labels

        voteSwitch.setChecked(      !(boolean) settings.get(Zoff.SETTINGS_KEY_VOTE));
        addsongsSwitch.setChecked(  !(boolean) settings.get(Zoff.SETTINGS_KEY_ADD_SONGS));
        allvideosSwitch.setChecked( !(boolean) settings.get(Zoff.SETTINGS_KEY_ALL_VIDEOS));
        longsongsSwitch.setChecked( !(boolean) settings.get(Zoff.SETTINGS_KEY_LONG_SONGS));
        frontpageSwitch.setChecked( !(boolean) settings.get(Zoff.SETTINGS_KEY_FRONTPAGE));
        skipSwitch.setChecked(      !(boolean) settings.get(Zoff.SETTINGS_KEY_SKIP));
        shuffleSwitch.setChecked(   !(boolean) settings.get(Zoff.SETTINGS_KEY_SHUFFLE));

        //Does not need inverting
        removeplaySwitch.setChecked((boolean) settings.get(Zoff.SETTINGS_KEY_REMOVE_PLAY));

        toggleSettingsLabels();






    }

    public void onCorrectPassword(){

        //Close softkeyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(RemoteActivity.this.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


        voteSwitch.setEnabled(true);
        addsongsSwitch.setEnabled(true);
        allvideosSwitch.setEnabled(true);
        longsongsSwitch.setEnabled(true);
        frontpageSwitch.setEnabled(true);
        removeplaySwitch.setEnabled(true);
        skipSwitch.setEnabled(true);
        shuffleSwitch.setEnabled(true);




        CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Revert switchvalues to reflect server values
                Boolean vote         = (!voteSwitch.isChecked());
                Boolean addsongs     = (!addsongsSwitch.isChecked());
                Boolean longsongs    = (!longsongsSwitch.isChecked());
                Boolean frontpage    = (!frontpageSwitch.isChecked());
                Boolean allvideos    = (!allvideosSwitch.isChecked());
                Boolean removeplay   = (removeplaySwitch.isChecked());
                Boolean skip         = (!skipSwitch.isChecked());
                Boolean shuffle      = (!shuffleSwitch.isChecked());

                Boolean[] settings = {vote, addsongs, longsongs, frontpage, allvideos, removeplay, skip, shuffle};

                zoff.saveSettings(settings);

                toggleSettingsLabels();


            }



        };

        voteSwitch.setOnCheckedChangeListener(changeListener);
        addsongsSwitch.setOnCheckedChangeListener(changeListener);
        allvideosSwitch.setOnCheckedChangeListener(changeListener);
        longsongsSwitch.setOnCheckedChangeListener(changeListener);
        frontpageSwitch.setOnCheckedChangeListener(changeListener);
        removeplaySwitch.setOnCheckedChangeListener(changeListener);
        skipSwitch.setOnCheckedChangeListener(changeListener);
        shuffleSwitch.setOnCheckedChangeListener(changeListener);






    }

    //Communication from ZOFF instance END


    private void toggleSearchLayout() {


        searchViewOpen = toolBarSearchField.getVisibility() == View.VISIBLE;


        menu.findItem(R.id.action_skip).setVisible(searchViewOpen);
        menu.findItem(R.id.action_search).setVisible(searchViewOpen);
        menu.findItem(R.id.action_shuffle).setVisible(searchViewOpen);
        menu.findItem(R.id.action_close_searchfield).setVisible(!searchViewOpen);



            if (searchViewOpen){

                //Searchbar is closed
                toolBarSearchField.setVisibility(View.INVISIBLE);

                videoList.setAdapter(listAdapter);
                videoList.invalidateViews();
                listAdapter.notifyDataSetChanged();


                //Close softkeyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(RemoteActivity.this.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                //Searchbar is opened
                toolBarSearchField.setVisibility(View.VISIBLE);
                videoList.setAdapter(searchAdapter);
                videoList.invalidateViews();
                searchAdapter.notifyDataSetChanged();


                //Open softkeyboard
                toolBarSearchField.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(RemoteActivity.this.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


            }








    }

    @Override
    public void onBackPressed() {
        homePressed = false;

        searchViewOpen = toolBarSearchField.getVisibility() == View.VISIBLE;

        if (searchViewOpen) {
            toggleSearchLayout();
        } else {
            zoff.disconnect();
            super.onBackPressed();

        }


    }

    @Override
    protected void onUserLeaveHint() {
        if (homePressed){
            startNotificationService();
            zoff.disconnect();

        }
        homePressed = true;
        super.onUserLeaveHint();
    }

    @Override
    public void imageInCache(Bitmap bitmap) {
        ImageBlur.createAndSetBlurBG(bitmap, this, zoff.getNowPlayingID());

    }

    @Override
    protected void onDestroy() {
        if (zoff != null){
            zoff.disconnect();
        }
        ImageCache.empty();

        super.onDestroy();
    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER,log);
    }

    private void toggleSettingsLabels(){
        Map settings = zoff.getSettings();

        if (addsongsSwitch.isChecked()){
            addsongsDescription.setText(addsongsEnabled);
        } else {
            addsongsDescription.setText(addsongsDisabled);
        }
        if (voteSwitch.isChecked()){
            voteDescription.setText(voteEnabled);
        } else {
            voteDescription.setText(voteDisabled);
        }

        if (shuffleSwitch.isChecked()){
            shuffleDescription.setText(shuffleEnabled);
        } else {
            shuffleDescription.setText(shuffleDisabled);
        }

        if (skipSwitch.isChecked()){
            skipDescription.setText(skipEnabled);
        } else {
            skipDescription.setText(skipDisabled);
        }

        if (longsongsSwitch.isChecked()){
            longsongsDescription.setText(longsongsEnabled);
        } else {
            longsongsDescription.setText(longsongsDisabled);
        }

        if (allvideosSwitch.isChecked()){
            allvideosDescription.setText(allvideosEnabled);
        } else {
            allvideosDescription.setText(allvideosDisabled);
        }

        if (frontpageSwitch.isChecked()){
            frontpageDescription.setText(frontpageEnabled);
        } else {
            frontpageDescription.setText(frontpageDisabled);
        }

        if (removeplaySwitch.isChecked()){
            removeplayDescription.setText(removeplayEnabled);
        } else {
            removeplayDescription.setText(removeplayDisabled);
        }


    }
}





