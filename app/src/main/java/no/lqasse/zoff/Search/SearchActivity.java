package no.lqasse.zoff.Search;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Models.SearchResult;
import no.lqasse.zoff.Server;


/**
 * Created by lassedrevland on 14.01.15.
 */
public class SearchActivity extends ActionBarActivity {

    private final String API_KEY = "AIzaSyD3aXvu3LeE4mLwUOYU3UIIUbb0Z4v41NY";
    private final String YOUTUBE_MAX_RESULTS = "25";
    private String VIDEO_CATEGORIES = "&videoCategoryId=10";
    private final String URL_YOUTUBE_QUERY_PT1 = "https://www.googleapis.com/youtube/v3/search?part=snippet&nextPageT&maxResults=" + YOUTUBE_MAX_RESULTS + "&q=";
    private final String URL_YOUTUBE_QUERY_PT2 = "&type=video&key=" + API_KEY;
    private final String URL_YOUTUBE_DETAILS_PT1 = "https://www.googleapis.com/youtube/v3/videos?id=";
    private final String URL_YOUTUBE_DETAILS_PT2 = "&part=contentDetails,statistics&key=" + API_KEY;
    private String NEXT_PAGE_TOKEN = "";


    private final int AUTOSEARCH_DELAY_MILLIS = 600;
    private final int YOUTUBE_SEARCH = 0;
    private final int YOUTUBE_DETAILS = 1;
    private final int ZOFF_ADD = 2;
    private final int APPEND_VIDEOS =3;
    private String ROOM_NAME = "ROOM_NAME";
    private String ROOM_PASS = "ROOM_PASS";
    private Boolean ALL_VIDEOS = true;
    private Boolean LONG_SONGS = true;
    private String ZOFF_URL = "http://www.zoff.no/";



    private ProgressBar progressBar;
    private EditText queryView;
    private ListView resultsView;
    private ArrayList<SearchResult> results = new ArrayList<>();
    private SearchResultListAdapter SearchResultListAdapter;


    private Handler handler = new Handler();
    private Runnable delaySearch;
    private doGetRequest doGetRequest = new doGetRequest();




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
        ALL_VIDEOS = b.getBoolean("ALL_VIDEOS_ALLOWED");
        LONG_SONGS = b.getBoolean("LONG_SONGS");
        ZOFF_URL = ZOFF_URL + ROOM_NAME + "/php/change.php?";
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Suppress notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();




        queryView = (EditText) findViewById(R.id.searchQueryView);
        YouTubeServer.search(this,"",ALL_VIDEOS,LONG_SONGS);

        //doYoutubeSearch(true,false);


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




        SearchResultListAdapter = new SearchResultListAdapter(this, results);
        resultsView = (ListView) findViewById(R.id.searchResultsView);
        resultsView.setAdapter(SearchResultListAdapter);




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


                if (resultsView.getLastVisiblePosition() == results.size()-1 && !NEXT_PAGE_TOKEN.equals("")){
                    doYoutubeSearch(true,true);
                    Log.d("Scroll", "Loading next page");

                }
            }
        });


    }

    private void doYoutubeSearch(Boolean ignoreEmpty, Boolean nextPage) {


        progressBar.setVisibility(View.VISIBLE);

        String type;
        if (!nextPage){
            NEXT_PAGE_TOKEN = "";
            type = Integer.toString(YOUTUBE_SEARCH);
        } else {
            type = Integer.toString(APPEND_VIDEOS);

        }


        if (doGetRequest.getStatus() == AsyncTask.Status.RUNNING) {
            doGetRequest.cancel(true);
            Log.d("Search", "Search Cancelled!");
        }


        String[] input = new String[2];


        String query = queryView.getText().toString();

        query = query.replace(" ", "%20");

        String searchString = URL_YOUTUBE_QUERY_PT1 + query + URL_YOUTUBE_QUERY_PT2 + allVideos() + longSongs() + nextPage();


        input[0] = searchString;
        input[1] = type; //Define get result

        if (!query.equals("")) {
            doGetRequest = new doGetRequest();
            doGetRequest.execute(input);

        } else if (ignoreEmpty) {
            doGetRequest = new doGetRequest();
            doGetRequest.execute(input);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }





    }

    private void handleYTdata(String s,Boolean appendResults) {

        JSONObject json = null;
        ArrayList<String> ids = new ArrayList();

        try {

            json = new JSONObject(s);


        } catch (Exception e) {
            Log.d(" exception", e.getLocalizedMessage());
        }

        if (!appendResults){
            results.clear();
        }




        SearchResult searchResult;
        try {
            if (json.has("nextPageToken")){
                NEXT_PAGE_TOKEN = json.getString("nextPageToken");
            } else {
                NEXT_PAGE_TOKEN = "";
            }

            JSONArray jArray = json.getJSONArray("items");


            for (int i = 0; i < jArray.length() - 1; i++) {

                String title;
                String channelTitle;
                String description;
                String videoID;
                String publishedAt;
                String thumbDefault;
                String thumbMedium;
                String thumbHigh;

                json = jArray.getJSONObject(i);

                JSONObject idObject = json.getJSONObject("id");

                videoID = idObject.getString("videoId");
                ids.add(videoID);


                json = json.getJSONObject("snippet");
                publishedAt = json.getString("publishedAt");
                title = json.getString("title");
                channelTitle = json.getString("channelTitle");

                //Decode string from html
                title = Html.fromHtml(title).toString();

                description = json.getString("description");

                JSONObject thumbnailsObject = json.getJSONObject("thumbnails");
                JSONObject thumbDefaultObject = thumbnailsObject.getJSONObject("default");
                JSONObject thumbMedObject = thumbnailsObject.getJSONObject("medium");
                JSONObject thumbHighObject = thumbnailsObject.getJSONObject("high");

                thumbDefault = thumbDefaultObject.getString("url");
                thumbMedium = thumbMedObject.getString("url");
                thumbHigh = thumbHighObject.getString("url");


                searchResult = new SearchResult(title, channelTitle,description, publishedAt,videoID, thumbDefault, thumbMedium, thumbHigh);
                results.add(searchResult);
            }

            String idQuery = "";
            for (String id : ids) {
                idQuery += id + ",";

            }

            getVideoDetails(idQuery);

            //Get Statistics and details for video


            //Notify changes to listview
            progressBar.setVisibility(View.GONE);
            SearchResultListAdapter.notifyDataSetChanged();

            if (!appendResults){
                resultsView.setSelectionAfterHeaderView();
            }




        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    private void handleYTVideoDetails(String s) {
        try {
            JSONObject json = new JSONObject(s);
            JSONArray items = json.getJSONArray("items");
            String duration = "";
            String views = "";
            int offset = results.size() - 24;

            int endInt = results.size();
            for (int i = 0; i < items.length(); i++) {
                JSONObject details = items.getJSONObject(i).getJSONObject("contentDetails");
                JSONObject statistics = items.getJSONObject(i).getJSONObject("statistics");

                duration = details.getString("duration");
                views = statistics.getString("viewCount");
                results.get(i + offset).setViews(views);
                results.get(i + offset).setDuration(duration);


            }


            //Notify changes to listview
            progressBar.setVisibility(View.GONE);
            SearchResultListAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void intitalResultsReceived(String response){

    }

    public void resultDetailsReceived(String response){

    }

    public void nextPageReceived(String response){

    }

    private void getVideoDetails(String idQ) {
        String[] input = new String[2];

        input[0] = URL_YOUTUBE_DETAILS_PT1 + idQ + URL_YOUTUBE_DETAILS_PT2;
        input[1] = Integer.toString(YOUTUBE_DETAILS);
        doGetRequest = new doGetRequest();
        doGetRequest.execute(input);
    }

    private void addVideo(int index) {

        Toast t = Toast.makeText(getBaseContext(),results.get(index).getTitle() + " was added",Toast.LENGTH_SHORT);
        View v = t.getView();
        v.setBackgroundResource(R.drawable.toast_background);
        t.show();

        String videoID = results.get(index).getVideoID();
        String videoTitle = results.get(index).getTitle();
        try {
            videoTitle = URLEncoder.encode(videoTitle,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }



            Server.add(videoID,videoTitle);



    }

    private String allVideos(){

        if (!ALL_VIDEOS){
            //LIMIT search to only music videos
            return  "&videoCategoryId=10";
        } else {
            return "";
        }

    }

    private String longSongs(){
        if (!LONG_SONGS){
            //filters out songs over 20min
            return  "&videoDuration=short";
        } else {
            return "";
        }
    }

    private String nextPage(){
        if (!NEXT_PAGE_TOKEN.equals("")){
            return "&pageToken="+NEXT_PAGE_TOKEN;
        } else {
            return "";
        }
    }

    private class doGetRequest extends AsyncTask<String, Void, String> {
        JSONObject json;
        StringBuilder sb;
        private int type;


        @Override
        protected String doInBackground(String... params) {
            type = Integer.valueOf(params[1]);
            BufferedReader r;
            InputStream inputStream = null;
            String result = "";
            try {

                String url = (params[0]);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
                inputStream = httpResponse.getEntity().getContent();
                if (inputStream != null) {
                    r = new BufferedReader(new InputStreamReader(inputStream));
                    sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);


                    }


                } else
                    result = "Did not work!";

            } catch (Exception e) {
                e.printStackTrace();

            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            switch (type) {
                case YOUTUBE_SEARCH:
                    handleYTdata(result,false);
                    break;
                case YOUTUBE_DETAILS:
                    handleYTVideoDetails(result);
                    break;
                case ZOFF_ADD:
                    break;
                case APPEND_VIDEOS:
                    handleYTdata(result,true);
                    break;
            }


        }


    }

}
