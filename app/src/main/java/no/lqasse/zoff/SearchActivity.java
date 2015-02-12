package no.lqasse.zoff;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Date;

import no.lqasse.zoff.Datatypes.searchResult;
import no.lqasse.zoff.Datatypes.searchResultAdapter;


/**
 * Created by lassedrevland on 14.01.15.
 */
public class SearchActivity extends ActionBarActivity {

    private final String API_KEY = "AIzaSyD3aXvu3LeE4mLwUOYU3UIIUbb0Z4v41NY";
    private final String YOUTUBE_MAX_RESULTS = "25";
    private String VIDEO_CATEGORIES = "&videoCategoryId=10";
    private final String URL_YOUTUBE_QUERY_PT1 = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=" + YOUTUBE_MAX_RESULTS + "&q=";
    private final String URL_YOUTUBE_QUERY_PT2 = "&type=video&key=" + API_KEY;
    private final String URL_YOUTUBE_DETAILS_PT1 = "https://www.googleapis.com/youtube/v3/videos?id=";
    private final String URL_YOUTUBE_DETAILS_PT2 = "&part=contentDetails,statistics&key=" + API_KEY;

    private final int AUTOSEARCH_DELAY_MILLIS = 600;
    private final int YOUTUBE_SEARCH = 0;
    private final int YOUTUBE_DETAILS = 1;
    private final int ZOFF_ADD = 2;
    private String ROOM_NAME = "ROOM_NAME";
    private String ROOM_PASS = "ROOM_PASS";
    private Boolean ALL_VIDEOS = true;
    private Boolean LONG_SONGS = true;
    private String ZOFF_URL = "http://www.zoff.no/";
    private ArrayList<searchResult> results;

    private ProgressBar progressBar;
    private EditText queryView;

    private Handler handler = new Handler();
    private Runnable delaySearch;
    private doGetRequest doGetRequest = new doGetRequest();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        ROOM_NAME = b.getString(ROOM_NAME);
        ROOM_PASS = b.getString(ROOM_PASS);
        ALL_VIDEOS = b.getBoolean("ALL_VIDEOS");
        LONG_SONGS = b.getBoolean("LONG_SONGS");
        ZOFF_URL = ZOFF_URL + ROOM_NAME + "/php/change.php?";
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Suppress notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();



        doYoutubeSearch();
        queryView = (EditText) findViewById(R.id.searchQueryView);

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
                    doYoutubeSearch();
                    handled = true;
                }
                return handled;
            }
        });

        delaySearch = new Runnable() {
            @Override
            public void run() {
                doYoutubeSearch();
            }
        };


    }


    private void doYoutubeSearch() {


        progressBar.setVisibility(View.VISIBLE);
        ListView resultsView = (ListView) findViewById(R.id.searchResultsView);
        resultsView.setVisibility(View.GONE);


        if (doGetRequest.getStatus() == AsyncTask.Status.RUNNING) {
            doGetRequest.cancel(true);
            Log.d("Search", "Search Cancelled!");
        }


        String[] input = new String[2];
        EditText et = (EditText) findViewById(R.id.searchQueryView);
        Log.d("Search", "Searched for " + et.getText().toString());

        //Hide keyboard
        /*
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        */

        String query = et.getText().toString();
        //random query generator
        Date dato = new Date();
        if (query.equals("")) {
            query = Integer.toString(dato.getDate());

        }
        query = query.replace(" ", "%20");

        String searchString = URL_YOUTUBE_QUERY_PT1 + query + URL_YOUTUBE_QUERY_PT2 + allVideos() + longSongs();


        input[0] = searchString;
        input[1] = Integer.toString(YOUTUBE_SEARCH); //Define get result

        doGetRequest = new doGetRequest();
        doGetRequest.execute(input);


    }

    private void handleYTdata(String s) {

        JSONObject json = null;
        ArrayList<String> ids = new ArrayList();

        try {

            json = new JSONObject(s);


        } catch (Exception e) {
            Log.d("DowddnloadFinished exception", e.getLocalizedMessage());
        }

        results = new ArrayList<searchResult>();


        searchResult searchResult;
        try {
            JSONArray jArray = json.getJSONArray("items");


            for (int i = 0; i < jArray.length() - 1; i++) {
                String title = "";
                String description = "";
                String videoID = "";
                String publishedAt = "";
                String thumbDefault = "";
                String thumbMedium = "";
                String thumbHigh = "";

                json = jArray.getJSONObject(i);

                JSONObject idObject = json.getJSONObject("id");

                videoID = idObject.getString("videoId");
                ids.add(videoID);


                json = json.getJSONObject("snippet");
                title = json.getString("title");

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


                searchResult = new searchResult(title, description, videoID, thumbDefault, thumbMedium, thumbHigh);
                results.add(searchResult);
            }

            String idQuery = "";
            for (String id : ids) {
                idQuery += id + ",";

            }

            getVideoDetails(idQuery);

            //Get Statistics and details for video



            populateSearchResults(results);


        } catch (Exception e) {
            Log.d("JSON ERROR", e.getLocalizedMessage());

            //e.printStackTrace();
        }


    }

    private void handleYTVideoDetails(String s) {
        try {
            JSONObject json = new JSONObject(s);
            JSONArray items = json.getJSONArray("items");
            String duration = "";
            String views = "";
            for (int i = 0; i < items.length(); i++) {
                JSONObject details = items.getJSONObject(i).getJSONObject("contentDetails");
                JSONObject statistics = items.getJSONObject(i).getJSONObject("statistics");

                duration = details.getString("duration");
                views = statistics.getString("viewCount");
                results.get(i).setViews(views);
                results.get(i).setDuration(duration);


            }
            populateSearchResults(results);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void populateSearchResults(final ArrayList<searchResult> results) {
        progressBar.setVisibility(View.GONE);


        ListView resultsView = (ListView) findViewById(R.id.searchResultsView);
        resultsView.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        String[] titles = new String[results.size()];

        for (int i = 0; i < results.size(); i++) {
            titles[i] = results.get(i).getTitle();

        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.search_row, R.id.title, titles);


        final searchResultAdapter myAdapter = new searchResultAdapter(this, results);
        resultsView.setAdapter(myAdapter);



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
                Toast.makeText(getBaseContext(),"Click and hold to add videos",Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getVideoDetails(String idQ) {
        String[] input = new String[2];

        input[0] = URL_YOUTUBE_DETAILS_PT1 + idQ + URL_YOUTUBE_DETAILS_PT2;
        input[1] = Integer.toString(YOUTUBE_DETAILS);
        doGetRequest = new doGetRequest();
        doGetRequest.execute(input);
    }

    private void addVideo(int index) {

        Toast.makeText(this, results.get(index).getTitle() + " was added", Toast.LENGTH_SHORT).show();
        String php = "/php/change.php?";

        String titlePrefix = "&n=";
        String passPrefix = "&pass=";
        String vidIdPrefix = "v=";
        String videoID = results.get(index).getVideoID();
        String videoTitle = results.get(index).getTitle();
        try {
            videoTitle = URLEncoder.encode(videoTitle,"UTF-8");
        } catch (Exception e){

        }



        doGetRequest task = new doGetRequest();
        String URL = ZOFF_URL + vidIdPrefix + videoID + titlePrefix + videoTitle + passPrefix + ROOM_PASS;
        Log.d("URl", URL);
        String[] input = new String[3];
        input[0] = URL;
        input[1] = Integer.toString(ZOFF_ADD);
        task.execute(input);


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

                throw new IllegalStateException();
            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            switch (type) {
                case YOUTUBE_SEARCH:
                    handleYTdata(result);
                    break;
                case YOUTUBE_DETAILS:
                    handleYTVideoDetails(result);
                    break;
                case ZOFF_ADD:
                    break;
            }


        }


    }


}
