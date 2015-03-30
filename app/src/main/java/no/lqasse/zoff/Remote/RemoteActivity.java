package no.lqasse.zoff.Remote;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Search.SearchResultListAdapter;
import no.lqasse.zoff.Search.YouTube;
import no.lqasse.zoff.Server.Server;
import no.lqasse.zoff.Zoff;
import no.lqasse.zoff.Zoff_Listener;
import no.lqasse.zoff.Helpers.ImageBlur;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Search.SearchActivity;
import no.lqasse.zoff.SettingsActivity;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ActionBarActivity implements Zoff_Listener {
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";
    private String ROOM_NAME;
    private String ROOM_PASS;

    private Zoff zoff;
    private Menu menu;
    private Boolean paused = false;
    private Boolean keyboardVisible = false;
    private ListView videoList;
    private RemoteListAdapter adapter;
    private SearchResultListAdapter searchAdapter;
    private SharedPreferences sharedPreferences;
    private TextView searchText;
    private ImageView removeQueryButton;

    private Handler h = new Handler();
    private Runnable r;
    private Handler handler = new Handler();
    private Runnable delaySearch = new Runnable() {
        @Override
        public void run() {
            YouTube.search(RemoteActivity.this, searchText.getText().toString(), zoff.ALL_VIDEOS_ALLOWED(), zoff.LONG_SONGS());
        }
    };
    private boolean searchViewOpen = false;
    private final int AUTOSEARCH_DELAY_MILLIS = 600;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            ROOM_NAME = b.getString("ROOM_NAME");
        }


        ROOM_PASS = getPASS();


        zoff = new Zoff(ROOM_NAME, this);
        zoff.setROOM_PASS(getPASS());
        setContentView(R.layout.activity_remote);
        getSupportActionBar().setIcon(R.drawable.logo);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        //Handle everything listview related
        adapter = new RemoteListAdapter(this, zoff.getVideos(), zoff);
        searchAdapter = new SearchResultListAdapter(this, YouTube.getSearchResults());
        videoList = (ListView) findViewById(R.id.videoList);
        videoList.setAdapter(adapter);


        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.putExtra("ROOM_NAME", ROOM_NAME);

        startService(notificationIntent);


        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                if (searchViewOpen) {
                    String videoTitle = YouTube.getSearchResults().get(position).getTitle();
                    String videoID = YouTube.getSearchResults().get(position).getVideoID();
                    Server.add(videoID, videoTitle);
                    ToastMaster.showToast(RemoteActivity.this, ToastMaster.TYPE.VIDEO_ADDED, videoTitle);

                } else if ((position != 0) && (!searchViewOpen)) { //Cant vote for current video duh
                    Video selectedVideo = adapter.getItem(position);
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

                } else if ((position != 0) && (!searchViewOpen)) { //Currently playing video cant be voted on
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


        startNotificationService();
        zoff.resumeRefresh();
        NotificationService.setInBackground(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        zoff.pauseRefresh();
        NotificationService.setInBackground(true);

        super.onPause();


    }

    @Override
    protected void onStop() {


        paused = true;


        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        zoff.pauseRefresh();
        Intent intent = new Intent(this, NotificationService.class);
        stopService(intent);
        NotificationService.setInBackground(true);

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
            case (R.id.action_next):
                zoff.voteSkip();
                zoff.refreshData();

                break;
            case (R.id.action_search):
                toggleSearchLayout();


                break;

            case (R.id.action_zoff_settings):
                //Display ZOff settings

                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtras(zoff.getSettingsBundle());


                startActivity(i);
                break;
            case (R.id.action_shuffle):
                if (zoff.hasROOM_PASS()) {
                    ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.SHUFFLED);
                    zoff.shuffle();
                } else {
                    ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.NEEDS_PASS_TO_SHUFFLE);
                }
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


    @Override
    public void zoffRefreshed(Boolean hasInetAccess) {
        adapter.notifyDataSetChanged();


    }

    private String getPASS() {
        String PASS;
        sharedPreferences = getSharedPreferences(PREFS_FILE, 0);
        PASS = sharedPreferences.getString(ROOM_NAME, null);
        return PASS;

    }


    public Zoff getZoff() {
        return this.zoff;
    }


    public void setBackgroundImage(Bitmap bitmap) {


        LinearLayout l = (LinearLayout) findViewById(R.id.layout);
        l.setBackground(new BitmapDrawable(getBaseContext().getResources(), bitmap));
    }

    public void startNotificationService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.putExtra("ROOM_NAME", ROOM_NAME);
        startService(notificationIntent);
    }

    private void toggleSearchLayout() {

        menu.findItem(R.id.action_next).setVisible(searchViewOpen);
        menu.findItem(R.id.action_zoff_settings).setVisible(searchViewOpen);
        menu.findItem(R.id.action_search).setVisible(searchViewOpen);


        if (searchViewOpen) {
            getSupportActionBar().setDisplayShowCustomEnabled(false);

        } else {

            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.actionbar_search_layout);
            searchText = (EditText) getSupportActionBar().getCustomView().findViewById(R.id.etSearch);
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
            videoList.setAdapter(adapter);
            videoList.invalidateViews();
            adapter.notifyDataSetChanged();
        } else {

            videoList.setAdapter(searchAdapter);

            videoList.invalidateViews();
            searchAdapter.notifyDataSetChanged();
        }


    }


    @Override
    public void onBackPressed() {
        if (searchViewOpen) {
            toggleSearchLayout();
            toggleListAdapter(true);
        } else {
            super.onBackPressed();
        }


    }
}




