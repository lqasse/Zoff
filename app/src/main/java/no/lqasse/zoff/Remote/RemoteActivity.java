package no.lqasse.zoff.Remote;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.Adapters.RemoteListAdapter;
import no.lqasse.zoff.Search.SearchResultListAdapter;
import no.lqasse.zoff.Search.YouTube;
import no.lqasse.zoff.Search.YouTubeListener;
import no.lqasse.zoff.Server.Server;
import no.lqasse.zoff.Helpers.SpotifyServer;
import no.lqasse.zoff.Zoff;
import no.lqasse.zoff.ZoffActivity;
import no.lqasse.zoff.ZoffListener;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.R;
import no.lqasse.zoff.SettingsActivity;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ZoffActivity implements ZoffListener,YouTubeListener {
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";


    private Menu menu;
    private Boolean paused = false;
    private Boolean keyboardVisible = false;
    private ListView videoList;
    private RemoteListAdapter remoteListAdapter;
    private SearchResultListAdapter searchAdapter;
    private SharedPreferences sharedPreferences;
    private TextView searchText;
    private ImageView removeQueryButton;
    private Bitmap currentBackground;

    private Handler h = new Handler();
    private Runnable r;
    private Handler handler = new Handler();
    private Runnable delaySearch = new Runnable() {
        @Override
        public void run() {
            YouTube.search(RemoteActivity.this, searchText.getText().toString(), zoff.allVideosAllowed(), zoff.LONG_SONGS());
        }
    };


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
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_default_layout);
        TextView title = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.titleText);
        title.setText(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        //Handle everything listview related
        remoteListAdapter = new RemoteListAdapter(this, zoff.getVideos(), zoff);
        searchAdapter = new SearchResultListAdapter(this, YouTube.getSearchResults());
        videoList = (ListView) findViewById(R.id.videoList);
        videoList.setAdapter(remoteListAdapter);


        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                if (searchViewOpen) {
                    String videoTitle = YouTube.getSearchResults().get(position).getTitle();
                    String videoID = (YouTube.getSearchResults().get(position)).getVideoID();
                    Server.add(videoID, videoTitle);
                    ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.VIDEO_ADDED, videoTitle);

                } else if (position != 0)  { //Cant vote for current video duh
                    Video selectedVideo = remoteListAdapter.getItem(position);
                    zoff.vote(selectedVideo);
                }
                ;


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
                if (zoff.allowSkip()) {
                    zoff.voteSkip();
                    zoff.refreshData();
                } else {
                    ToastMaster.showToast(this, ToastMaster.TYPE.SKIP_DISABLED);
                }


                break;
            case (R.id.action_search):
                if (zoff.hasROOM_PASS()||zoff.ANYONE_CAN_ADD()){
                    toggleSearchLayout();
                } else{
                    ToastMaster.showToast(this, ToastMaster.TYPE.NEEDS_PASS_TO_ADD);

                }



                break;

            case (R.id.action_zoff_settings):
                //Display ZOff settings


                homePressed = false;
                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtras(zoff.getSettingsBundle());


                startActivity(i);
                break;
            case (R.id.action_shuffle):
                if (zoff.allowShuffle()) {
                    if (zoff.hasROOM_PASS()) {
                        ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.SHUFFLED);
                        zoff.shuffle();
                    } else {
                        ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.NEEDS_PASS_TO_SHUFFLE);
                    }
                } else {
                    ToastMaster.showToast(this, ToastMaster.TYPE.SHUFFLING_DISABLED);
                }

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

    public void invalidateViews() {
        videoList.invalidateViews();
    }



    public void zoffRefreshed(Boolean hasInetAccess) {
        remoteListAdapter.notifyDataSetChanged();
    }

    private String getPASS() {
        String PASS;
        sharedPreferences = getSharedPreferences(PREFS_FILE, 0);
        PASS = sharedPreferences.getString(ROOM_NAME, null);
        return PASS;
    }

    public void setBackgroundImage(Bitmap bitmap) {


        if (currentBackground != bitmap){


            final Bitmap bg = bitmap;
            final ImageView background = (ImageView) findViewById(R.id.backgroundImage);
            final ImageView oldBackground = (ImageView) findViewById(R.id.backgroundImageOLD);

            if (currentBackground != null){
                oldBackground.setImageBitmap(currentBackground);
                Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
                fadeIN.setInterpolator(new DecelerateInterpolator());
                fadeIN.setDuration(1000);
                background.setVisibility(View.INVISIBLE);
                background.setImageBitmap(bg);
                background.setAnimation(fadeIN);
                fadeIN.start();
                background.setVisibility(View.VISIBLE);

            } else {
                Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
                fadeIN.setInterpolator(new DecelerateInterpolator());
                fadeIN.setDuration(1000);
                background.setImageBitmap(bg);
                background.setAnimation(fadeIN);
                fadeIN.start();

            }
            currentBackground = bitmap;




        }


    }


    private void toggleSearchLayout() {

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
    }

    public void toggleListAdapter(Boolean searchLayout) {

        if (searchLayout) {
            videoList.setAdapter(remoteListAdapter);
            videoList.invalidateViews();
            remoteListAdapter.notifyDataSetChanged();
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
            Log.d("Button", "HOME pressed");
            startNotificationService();
        } else {
            Log.d("BUtton", "BACK pressed");
        }
        homePressed = true;
        super.onUserLeaveHint();
    }

}





