package no.lqasse.zoff.Search;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Models.SearchResult;
import no.lqasse.zoff.Server.Server;


/**
 * Created by lassedrevland on 14.01.15.
 */
public class SearchActivity extends ActionBarActivity {

    private String NEXT_PAGE_TOKEN = "";


    private final int AUTOSEARCH_DELAY_MILLIS = 600;

    private String ROOM_NAME = "ROOM_NAME";
    private String ROOM_PASS = "ROOM_PASS";
    private Boolean ALL_VIDEOS = true;
    private Boolean LONG_SONGS = true;



    private ProgressBar progressBar;
    private EditText queryView;
    private ListView resultsView;
    private ArrayList<SearchResult> searchResults = new ArrayList<>();
    private SearchResultListAdapter searchResultListAdapter;

    private SearchActivity searchActivity = this;


    private Handler handler = new Handler();
    private Runnable delaySearch;
    private HashMap<String,SearchResult> searchResultHashMap = new HashMap<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Search...");
        Intent i = getIntent();
        Bundle b = i.getExtras();
        ROOM_NAME = b.getString(ROOM_NAME);
        ROOM_PASS = b.getString(ROOM_PASS);
        ALL_VIDEOS = b.getBoolean("allVideosAllowed");
        LONG_SONGS = b.getBoolean("LONG_SONGS");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        //Suppress notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();




        queryView = (EditText) findViewById(R.id.searchQueryView);
        //YouTubeServer.search(this,"",ALL_VIDEOS,LONG_SONGS);


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
        searchResultListAdapter = new SearchResultListAdapter(this, searchResults);
        resultsView = (ListView) findViewById(R.id.searchResultsView);
        resultsView.setAdapter(searchResultListAdapter);




        resultsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                addVideo(position);
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

                if (resultsView.getLastVisiblePosition() == searchResults.size()-10 && !NEXT_PAGE_TOKEN.equals("")){
                    NEXT_PAGE_TOKEN = "";

                    String query = queryView.getText().toString();
                    YouTubeServer.getNextPage(searchActivity,query,ALL_VIDEOS,LONG_SONGS,NEXT_PAGE_TOKEN);
                    Log.d("Scroll", "Loading next page");

                }
            }
        });


    }

    private void doYoutubeSearch(Boolean ignoreEmpty, Boolean nextPage) {


        progressBar.setVisibility(View.VISIBLE);

        String query = queryView.getText().toString();
        query = URLEncoder.encode(query);


        //YouTubeServer.search(this,query,ALL_VIDEOS,LONG_SONGS);



    }




    public void firstPageReceived(ArrayList<SearchResult> results, String nextPageToken){


        searchResultHashMap.clear();
        searchResults.clear();
        progressBar.setVisibility(View.GONE);
        resultsView.setSelectionAfterHeaderView();

        pageReceived(results, nextPageToken);


    }
    public void pageReceived(ArrayList<SearchResult> results, String nextPageToken){
        NEXT_PAGE_TOKEN = nextPageToken;
        searchResults.addAll(results);

        for (SearchResult r : results){
            searchResultHashMap.put(r.getVideoID(), r);
        }

        progressBar.setVisibility(View.GONE);
        searchResultListAdapter.notifyDataSetChanged();

    }

    public void detailsReceived(ArrayList<String[]> details){

        for (String[] s : details){
            SearchResult result = searchResultHashMap.get(s[0]);
            result.setDuration(s[1]);
            result.setViews(s[2]);
        }

        searchResultListAdapter.notifyDataSetChanged();


    }


    private void addVideo(int index) {

        Toast t = Toast.makeText(getBaseContext(), searchResults.get(index).getTitle() + " was added",Toast.LENGTH_SHORT);
        View v = t.getView();
        v.setBackgroundResource(R.drawable.toast_background);
        t.show();

        String videoID = searchResults.get(index).getVideoID();
        String videoTitle = searchResults.get(index).getTitle();
        try {
            videoTitle = URLEncoder.encode(videoTitle,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

         Server.add(videoID,videoTitle);

    }





}
