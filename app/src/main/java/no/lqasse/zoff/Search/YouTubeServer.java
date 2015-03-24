package no.lqasse.zoff.Search;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    private static class getHolder{
        String url;
        TYPE type;
        String response;
        SearchActivity searchActivity;

    }

    public static void search(SearchActivity searchActivity, String query, Boolean allVideos, Boolean longSongs){
        String categoryLimit = "";
        String lenghtLimit = "";

        //LIMIT search to only music videos
        if (!allVideos){
            categoryLimit = "&videoCategoryId=10";
        }

        //filters out songs over 20min
        if (!longSongs)
           lenghtLimit =  "&videoDuration=short";


        getHolder holder = new getHolder();
        holder.type = TYPE.INITIAL_QUERY;
        holder.url = URL_YOUTUBE_QUERY_PT1 + query + URL_YOUTUBE_QUERY_PT2 + categoryLimit + lenghtLimit;
        holder.searchActivity = searchActivity;



    }

    public static void getDetails(){

    }

    public static void getNextPage(){

    }



    private class Get extends AsyncTask<getHolder, Void, getHolder> {
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

            switch (holder.type){
                case INITIAL_QUERY:


                    break;
                case DETAILS_QUERY:
                    break;
                case APPENDING_QUERY:
                    break;
            }



        }


    }
}
