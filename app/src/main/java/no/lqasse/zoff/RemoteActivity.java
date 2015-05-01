package no.lqasse.zoff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
public class RemoteActivity extends ZoffActivity implements ZoffListener,YouTubeListener,ImageListener,SettingsFragment.Listener {
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";
    private final String LOG_IDENTIFIER = "RemoteActivity";

    private SettingsFragment settingsFragment;

    private Menu menu;

    private ListView                videoList;
    private DrawerLayout            drawerLayout;
    private ListAdapter             listAdapter;
    private SearchResultListAdapter searchAdapter;
    private android.support.v7.widget.Toolbar toolBar;
    private EditText                toolBarSearchField;
    private TextView                toolBarTitle;
    private ProgressBar             loadingProgressbar;



    private MenuItem skip;
    private MenuItem shuffle;
    private MenuItem search;
    private MenuItem close;


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



    private boolean isNewChannel = false;
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
            isNewChannel = b.getBoolean("NEW",false);


        }




        zoff = new Zoff(ROOM_NAME,this);






        //Find views
        videoList           = (ListView) findViewById(R.id.videoList);
        drawerLayout        = (DrawerLayout) findViewById(R.id.topLayout);
        toolBarSearchField  = (EditText) findViewById(R.id.tool_bar_search_edittext);
        toolBarTitle        = (TextView) findViewById(R.id.toolbarTitle);
        loadingProgressbar  = (ProgressBar) findViewById(R.id.loadingProgressbar);


        //Set up drawertoggle




        if (findViewById(R.id.layout).getTag() != null){
            BIG_SCREEN = (findViewById(R.id.layout).getTag().equals("big_screen"));
        }

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getChannelName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolBarTitle.setText(zoff.getChannelName());

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
        if (!isNewChannel){
            zoff.ping(this);
        }

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
        skip    = menu.findItem(R.id.action_skip);
        search  = menu.findItem(R.id.action_search);
        shuffle = menu.findItem(R.id.action_shuffle);
        close   = menu.findItem(R.id.action_close_searchfield);

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



        settingsFragment.setSettings(zoff.getSettings());


    }

    public void onCorrectPassword() {

        InputMethodManager imm = (InputMethodManager) getSystemService(RemoteActivity.this.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        settingsFragment.enableSettings(zoff.getSettings());
    }




    //Communication from ZOFF instance END


    private void toggleSearchLayout() {


        searchViewOpen = toolBarSearchField.getVisibility() == View.VISIBLE;


        skip.setVisible(searchViewOpen);
        shuffle.setVisible(searchViewOpen);
        search.setVisible(searchViewOpen);
        shuffle.setVisible(searchViewOpen);
        close.setVisible(!searchViewOpen);



            if (searchViewOpen){

                //Searchbar is closed
                toolBarTitle.setVisibility(View.VISIBLE);
                toolBarSearchField.setVisibility(View.GONE);
                videoList.setAdapter(listAdapter);
                videoList.invalidateViews();
                listAdapter.notifyDataSetChanged();



                //Close softkeyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(RemoteActivity.this.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                //Searchbar is opened
                toolBarTitle.setVisibility(View.GONE);
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
    public void saveSettings(Boolean[] settings) {
        zoff.saveSettings(settings);
    }

    @Override
    public void setFragment(SettingsFragment fragment) {
        settingsFragment = fragment;
    }

    @Override
    public void savePassword(String password) {
        zoff.savePassword(password);
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


}





