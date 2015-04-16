package no.lqasse.zoff.Remote;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import no.lqasse.zoff.Adapters.ListAdapter;
import no.lqasse.zoff.Adapters.ListAdapterWPlaying;
import no.lqasse.zoff.Helpers.ImageBlur;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ImageDownload;
import no.lqasse.zoff.Interfaces.ImageListener;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.Search.SearchResultListAdapter;
import no.lqasse.zoff.Search.YouTube;
import no.lqasse.zoff.Interfaces.YouTubeListener;
import no.lqasse.zoff.Helpers.SpotifyServer;
import no.lqasse.zoff.Zoff;
import no.lqasse.zoff.ZoffActivity;
import no.lqasse.zoff.Interfaces.ZoffListener;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ZoffActivity implements ZoffListener,YouTubeListener,ImageListener {
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";

    private Menu menu;
    private Boolean paused = false;
    private Boolean keyboardVisible = false;
    private ListView videoList;
    private ListAdapter listAdapter;
    private SearchResultListAdapter searchAdapter;
    private SharedPreferences sharedPreferences;
    private TextView searchText;
    private ImageView removeQueryButton;

    private Bitmap currentPlayingImage;


    private Handler h = new Handler();
    private Runnable r;
    private Handler handler = new Handler();
    private Runnable delaySearch = new Runnable() {
        @Override
        public void run() {
            YouTube.search(RemoteActivity.this, searchText.getText().toString(), zoff.allVideosAllowed(), zoff.LONG_SONGS());
        }
    };

    private boolean BIG_SCREEN = false;










    private boolean appInBackGround = false;
    private boolean searchViewOpen = false;
    private final int AUTOSEARCH_DELAY_MILLIS = 600;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null && b.containsKey("ROOM_NAME")) {

            ROOM_NAME = b.getString("ROOM_NAME");
            ROOM_PASS = getPASS();
            zoff = new Zoff(ROOM_NAME, this);
            zoff.setROOM_PASS(getPASS());
        }





        setContentView(R.layout.activity_remote);


        if (findViewById(R.id.layout).getTag() != null){
            BIG_SCREEN = (findViewById(R.id.layout).getTag().equals("big_screen"));
        }

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_default_layout);

        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.titleText);
        title.setText(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);




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
        videoList = (ListView) findViewById(R.id.videoList);
        videoList.setAdapter(listAdapter);


        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Video selectedVideo = listAdapter.getItem(position);


                if (searchViewOpen) {
                    String videoTitle = YouTube.getSearchResults().get(position).getTitle();
                    String videoID = (YouTube.getSearchResults().get(position)).getVideoID();
                    String duration = (YouTube.getSearchResults().get(position)).getDuration();
                    //Server.add(videoID, videoTitle);

                    zoff.add(videoID, videoTitle, duration);
                    //ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.VIDEO_ADDED, videoTitle);

                } else if (position != 0) { //Cant vote for current video duh

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

        r = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getBaseContext(), NotificationService.class);

            }
        };


    }

    @Override
    protected void onResume() {


        stopNotificationService();
        zoff.resumeRefresh();
        super.onResume();
    }

    @Override
    protected void onPause() {
        zoff.stopRefresh();
        if (appInBackGround) {
            appInBackGround = false;
            //startNotificationService();
        }


        super.onPause();


    }

    @Override
    protected void onStop() {


        paused = true;


        super.onStop();
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

            case (R.id.action_settings):
                break;
            case (R.id.action_skip):
                handleEvent(event_type.skip);



                break;
            case (R.id.action_search):
                //handleEvent(event_type.search);
                toggleSearchLayout();




                break;

            case (R.id.action_zoff_settings):
                //Display ZOff settings
                handleEvent(event_type.settings);

                break;
            case (R.id.action_shuffle):
                handleEvent(event_type.shuffle);

                break;
            case (R.id.action_play):
                homePressed = false;
                Intent playerIntent = new Intent(this, PlayerActivity.class);
                playerIntent.putExtra("ROOM_NAME", Zoff.getROOM_NAME());
                startActivity(playerIntent);
                break;


        }


        return super.onOptionsItemSelected(item);
    }

    public void notifyDatasetChanged() {
        searchAdapter.notifyDataSetChanged();


    }


    public void viewersChanged(){
        listAdapter.notifyDataSetChanged();
    }

    public void zoffRefreshed(Boolean hasInetAccess) {
        listAdapter.notifyDataSetChanged();

        if (BIG_SCREEN){
            TextView titleText = (TextView)findViewById(R.id.videoTitleView);
            TextView skipsText = (TextView)findViewById(R.id.skipsLabel);
            TextView viewsText = (TextView)findViewById(R.id.viewsLabel);

            titleText.setText(zoff.getNowPlayingTitle());
            skipsText.setText(zoff.getSkips());
            viewsText.setText(zoff.getViewers());

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



    }

    private String getPASS() {
        String PASS;
        sharedPreferences = getSharedPreferences(PREFS_FILE, 0);
        PASS = sharedPreferences.getString(ROOM_NAME, "");
        return PASS;
    }



    private void toggleSearchLayout() {

        if (zoff.hasROOM_PASS()||zoff.ANYONE_CAN_ADD()){



        menu.findItem(R.id.action_skip).setVisible(searchViewOpen);
        menu.findItem(R.id.action_zoff_settings).setVisible(searchViewOpen);
        menu.findItem(R.id.action_search).setVisible(searchViewOpen);

        View customView = getSupportActionBar().getCustomView();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        int layoutWidth = layout.getWidth();
        layoutWidth = layoutWidth - (int) (layoutWidth*0.4);



        if (searchViewOpen) {


                            getSupportActionBar().setCustomView(R.layout.actionbar_default_layout);
                            TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.titleText);
                            title.setText(zoff.getROOM_NAME());







        } else {

            getSupportActionBar().getCustomView().animate()
                    .setDuration(200)
                    .alpha(0)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
            getSupportActionBar().setCustomView(R.layout.actionbar_search_layout);




            searchText = (EditText) getSupportActionBar().getCustomView().findViewById(R.id.etSearch);
            final String hintText = searchText.getHint().toString();
            searchText.setHint("");
            searchText.setX(layoutWidth);
            searchText.animate()
                    .setInterpolator(new DecelerateInterpolator())
                    .translationX(0)
                    .setDuration(200)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            searchText.setHint(hintText);

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .start();
            removeQueryButton = (ImageView) getSupportActionBar().getCustomView().findViewById(R.id.removeTextButton);

            searchText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(RemoteActivity.this.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


            removeQueryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (searchText.getText().toString().equals("")) {
                        toggleSearchLayout();


                        InputMethodManager imm = (InputMethodManager) getSystemService(RemoteActivity.this.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


                    } else {
                        searchText.setText("");
                    }

                }
            });


            searchText.addTextChangedListener(new TextWatcher() {
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
                        SpotifyServer.getSearchString(s.toString(),searchText);
                    }

                    if (!textFieldEmpty) {
                        handler.removeCallbacks(delaySearch);
                        handler.postDelayed(delaySearch, AUTOSEARCH_DELAY_MILLIS);

                    }

                    toggleListAdapter(textFieldEmpty);

                }
            });


        }



        searchViewOpen = !searchViewOpen;
        } else{
            ToastMaster.showToast(this, ToastMaster.TYPE.NEEDS_PASS_TO_ADD);

        }
    }

    public void toggleListAdapter(Boolean searchLayout) {

        if (searchLayout) {
            videoList.setAdapter(listAdapter);
            videoList.invalidateViews();
            listAdapter.notifyDataSetChanged();
        } else {

            videoList.setAdapter(searchAdapter);

            videoList.invalidateViews();
            searchAdapter.notifyDataSetChanged();
        }


    }


    @Override
    public void onBackPressed() {
        homePressed = false;
        if (searchViewOpen) {
            toggleSearchLayout();
            toggleListAdapter(true);
        } else {
            super.onBackPressed();

        }


    }

    @Override
    protected void onUserLeaveHint() {
        if (homePressed){
            startNotificationService();
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
        zoff.disconnect();
        super.onDestroy();
    }
}





