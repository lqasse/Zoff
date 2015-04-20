package no.lqasse.zoff.Search;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import no.lqasse.zoff.Models.SearchResult;
import no.lqasse.zoff.SearchActivity;

/**
 * Created by lassedrevland on 24.03.15.
 */
public class YouTubeServer {

    public enum TYPE{INITIAL_QUERY,DETAILS_QUERY, APPENDING_QUERY}
    private static final String API_KEY = "AIzaSyD3aXvu3LeE4mLwUOYU3UIIUbb0Z4v41NY";
    private static final String YOUTUBE_MAX_RESULTS = "25";
    private static final String VIDEO_CATEGORIES = "&videoCategoryId=10";
    private static final String URL_YOUTUBE_QUERY_PT1 = "https://www.googleapis.com/youtube/v3/search?part=snippet&nextPageT&maxResults=" + YOUTUBE_MAX_RESULTS + "&q=";
    private static final String URL_YOUTUBE_QUERY_PT2 = "&type=video&key=" + API_KEY;
    private static final String URL_YOUTUBE_DETAILS_PT1 = "https://www.googleapis.com/youtube/v3/videos?id=";
    private static final String URL_YOUTUBE_DETAILS_PT2 = "&part=contentDetails,statistics&key=" + API_KEY;

    private static Get get = new Get();
    private static boolean cancelled = false;

    private static class getHolder{
        String url;
        TYPE type;
        String response;
        String nextPageToken;
        SearchActivity searchActivity;
        Context context;

    }


    public static void search(Context context, String query, Boolean allVideos, Boolean longSongs){
        String categoryLimit = "";
        String lenghtLimit = "";

        //LIMIT search to only music videos
        if (!allVideos){
            categoryLimit = "&videoCategoryId=10";
        }

        //filters out songs over 20min
        if (!longSongs)
           lenghtLimit =  "&videoDuration=short";

        String encodedQuery ="";
        try {
            encodedQuery = URLEncoder.encode(query,"UTF-8");
        } catch (Exception e){
            e.printStackTrace();
        }


        getHolder holder = new getHolder();
        holder.type = TYPE.INITIAL_QUERY;
        holder.url = URL_YOUTUBE_QUERY_PT1 + encodedQuery + URL_YOUTUBE_QUERY_PT2 + categoryLimit + lenghtLimit;
        holder.context = context;


        if (get.getStatus() == AsyncTask.Status.RUNNING){
            get.cancel(true);
            cancelled = true;

        }
        get = new Get();
        get.execute(holder);



    }

    public static void getDetails(Context context,  ArrayList<SearchResult> results){
        String idList ="";
        for (SearchResult r: results){
            idList += r.getVideoID()+",";
        }
        getHolder holder = new getHolder();
        holder.type =TYPE.DETAILS_QUERY;
        holder.url = URL_YOUTUBE_DETAILS_PT1 + idList + URL_YOUTUBE_DETAILS_PT2;

        holder.context = context;


        get = new Get();
        get.execute(holder);




    }

    public static void getNextPage(Context context, String query, Boolean allVideos, Boolean longSongs, String nextPageToken){
        String categoryLimit = "";
        String lenghtLimit = "";

        //LIMIT search to only music videos
        if (!allVideos){
            categoryLimit = "&videoCategoryId=10";
        }

        //filters out songs over 20min
        if (!longSongs)
            lenghtLimit =  "&videoDuration=short";

        String encodedQuery ="";
        try {
            encodedQuery = URLEncoder.encode(query,"UTF-8");
        } catch (Exception e){
            e.printStackTrace();
        }


        getHolder holder = new getHolder();
        holder.type = TYPE.APPENDING_QUERY;
        holder.url = URL_YOUTUBE_QUERY_PT1 + encodedQuery + URL_YOUTUBE_QUERY_PT2 + categoryLimit + lenghtLimit + "&pageToken="+nextPageToken;
        holder.context = context;

        get = new Get();
        get.execute(holder);

    }



    private static class Get extends AsyncTask<getHolder, Void, getHolder> {
        StringBuilder sb;


        @Override
        protected getHolder doInBackground(getHolder... params) {


            getHolder holder = params[0];

            BufferedReader r;
            InputStream inputStream;
            try {

                String url = holder.url;
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
                    holder.response = sb.toString();


                }

            } catch (Exception e) {
                e.printStackTrace();

            }

            return holder;
        }

        @Override
        protected void onPostExecute(getHolder holder) {
            ArrayList<SearchResult> results = results = new ArrayList<>();
            String nextPageToken;
            switch (holder.type){
                case INITIAL_QUERY:

                    results.addAll(YouTubeJSONTranslator.toSearchResults(holder.response));
                    nextPageToken = YouTubeJSONTranslator.toNextPageToken(holder.response);

                    YouTube.firstPageReceived(holder.context,results, nextPageToken);



                    break;
                case DETAILS_QUERY:
                    YouTube.detailsReceived(holder.context,
                    YouTubeJSONTranslator.toDetails(holder.response));

                    break;
                case APPENDING_QUERY:

                    results.addAll(YouTubeJSONTranslator.toSearchResults(holder.response));
                    nextPageToken = YouTubeJSONTranslator.toNextPageToken(holder.response);

                    //getDetails(holder.searchActivity,results);

                    YouTube.pageReceived(holder.context,results, nextPageToken);
                    break;
            }



        }


    }
}
