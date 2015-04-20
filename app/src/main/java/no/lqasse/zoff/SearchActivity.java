package no.lqasse.zoff;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.URLEncoder;

import no.lqasse.zoff.Adapters.SearchResultListAdapter;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Interfaces.YouTubeListener;
import no.lqasse.zoff.Helpers.SpotifyServer;
import no.lqasse.zoff.Search.YouTube;
import no.lqasse.zoff.Search.YouTubeServer;


/**
 * Created by lassedrevland on 14.01.15.
 */
public class SearchActivity extends ActionBarActivity implements YouTubeListener {

    private String NEXT_PAGE_TOKEN = "";


    private final int AUTOSEARCH_DELAY_MILLIS = 600;

    private String ROOM_NAME = "";
    private String ROOM_PASS = "";
    private Boolean ALL_VIDEOS = true;
    private Boolean LONG_SONGS = true;



    private ProgressBar progressBar;
    private EditText queryView;
    private ListView resultsView;
    private SearchResultListAdapter searchResultListAdapter;

    private SearchActivity searchActivity = this;


    private Handler handler = new Handler();
    private Runnable delaySearch;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
       if (zoff.getChannelName() == null){
            finish();

        }
        */
        Intent i = getIntent();
        String EXTRA_TEXT = i.getStringExtra(Intent.EXTRA_TEXT);


        if (EXTRA_TEXT.contains("http://youtu.be")){
            //Intent was from YouTube app!

            String videoID = EXTRA_TEXT.split(": http://youtu.be/")[1];
            String title = EXTRA_TEXT.split(": http://youtu.be/")[0];
           // Server.add(videoID,title);
            ToastMaster.showToast(this, ToastMaster.TYPE.VIDEO_ADDED,title);
            finish();


        }
        setContentView(R.layout.activity_search);

        if (EXTRA_TEXT.contains("open.spotify.com")){

            //Came from spotify app
            SpotifyServer.getAndSearchYoutube(EXTRA_TEXT, SearchActivity.this);





        }






        //setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Search...");


        if (ImageCache.getCurrentBlurBG()!=null){
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.settingsLayout);
            layout.setBackground(new BitmapDrawable(getResources(),ImageCache.getCurrentBlurBG()));
        }
        //Bundle b = i.getExtras();
        /*ROOM_NAME = Zoff.getChannelName();*/
        //ROOM_PASS = b.getString("ROOM_PASS");
        //ALL_VIDEOS = b.getBoolean("allVideosAllowed");
        //LONG_SONGS = b.getBoolean("LONG_SONGS");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);




        //Suppress notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();




        queryView = (EditText) findViewById(R.id.searchQueryView);
        SpotifyServer.getSearchString(EXTRA_TEXT, queryView);


        Log.d("EXTRA_TEXT",EXTRA_TEXT);


        searchResultListAdapter = new SearchResultListAdapter(this, YouTube.getSearchResults());
        resultsView = (ListView) findViewById(R.id.searchResultsView);
        resultsView.setAdapter(searchResultListAdapter);


        queryView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (queryView.getText() != null) {
                    handler.removeCallbacks(delaySearch);
                    handler.postDelayed(delaySearch, AUTOSEARCH_DELAY_MILLIS);
                }


            }
        });

        queryView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    handler.removeCallbacks(delaySearch);

                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(queryView.getWindowToken(), 0);

                    doYoutubeSearch(false,false);
                    handled = true;
                }
                return handled;
            }
        });

        delaySearch = new Runnable() {
            @Override
            public void run() {
                doYoutubeSearch(false,false);
            }
        };





        resultsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String videoId = YouTube.getSearchResults().get(position).getVideoID();
                String title = YouTube.getSearchResults().get(position).getTitle();
                //Server.add(videoId,title);
                ToastMaster.showToast(SearchActivity.this, ToastMaster.TYPE.VIDEO_ADDED, title);
                finish();
                return true;
            }
        });

        resultsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.HOLD_TO_ADD);


            }
        });

        resultsView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (resultsView.getLastVisiblePosition() == YouTube.getSearchResults().size()-10 && !NEXT_PAGE_TOKEN.equals("")){
                    NEXT_PAGE_TOKEN = "";

                    String query = queryView.getText().toString();
                    YouTubeServer.getNextPage(searchActivity, query, ALL_VIDEOS, LONG_SONGS, NEXT_PAGE_TOKEN);
                    Log.d("Scroll", "Loading next page");

                }
            }
        });


    }

    private void doYoutubeSearch(Boolean ignoreEmpty, Boolean nextPage) {


        progressBar.setVisibility(View.VISIBLE);

        String query = queryView.getText().toString();
        query = URLEncoder.encode(query);


        YouTubeServer.search(this,query,ALL_VIDEOS,LONG_SONGS);



    }








    @Override
    public void notifyDatasetChanged() {
        progressBar.setVisibility(View.INVISIBLE);
        searchResultListAdapter.notifyDataSetChanged();
    }
}
