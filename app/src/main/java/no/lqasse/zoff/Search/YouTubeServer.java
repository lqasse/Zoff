package no.lqasse.zoff.Search;

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
import no.lqasse.zoff.Models.ZoffSettings;

/**
 * Created by lassedrevland on 24.03.15.
 */
public class YouTubeServer {

    private enum TYPE{QUERY, DETAILS}
    private static final String API_KEY = "AIzaSyD3aXvu3LeE4mLwUOYU3UIIUbb0Z4v41NY";
    private static final String YOUTUBE_MAX_RESULTS = "25";
    private static final String VIDEO_CATEGORIES = "&videoCategoryId=10";
    private static final String URL_YOUTUBE_QUERY_PT1 = "https://www.googleapis.com/youtube/v3/search?part=snippet&nextPageT&maxResults=" + YOUTUBE_MAX_RESULTS + "&q=";
    private static final String URL_YOUTUBE_QUERY_PT2 = "&type=video&key=" + API_KEY;
    private static final String URL_YOUTUBE_DETAILS_PT1 = "https://www.googleapis.com/youtube/v3/videos?id=";
    private static final String URL_YOUTUBE_DETAILS_PT2 = "&part=contentDetails,statistics&key=" + API_KEY;

    private static Get get = new Get();
    private static boolean cancelled = false;

    private static class GetHolder {
        String url;
        TYPE type;
        String response;
        Callback callback;

    }

    public static void doSearch(String query, String pageToken, ZoffSettings settings, Callback callback){

        String categoryLimit = "";
        String lenghtLimit = "";



        //LIMIT search to only music videos
        if (!settings.isAllvideos()){
            categoryLimit = "&videoCategoryId=10";
        }

        //filters out songs over 20min
        if (!settings.isLongsongs())
            lenghtLimit =  "&videoDuration=short";

        String encodedQuery ="";
        try {
            encodedQuery = URLEncoder.encode(query,"UTF-8");
        } catch (Exception e){
            e.printStackTrace();
        }


        GetHolder holder = new GetHolder();
        holder.callback = callback;
        holder.type = TYPE.QUERY;
        holder.url = URL_YOUTUBE_QUERY_PT1 + encodedQuery + URL_YOUTUBE_QUERY_PT2 + categoryLimit + lenghtLimit + "&pageToken="+ pageToken;


        if (get.getStatus() == AsyncTask.Status.RUNNING){
            get.cancel(true);
            cancelled = true;

        }
        get = new Get();
        get.execute(holder);

    }

    private static void getDetails(String jsonPage, Callback callback){
        GetHolder holder = new GetHolder();

        ArrayList<SearchResult> results = YouTubeJSONTranslator.toSearchResults(jsonPage);

        String idList ="";
        for (SearchResult r: results){
            idList += r.getVideoID()+",";
        }
        holder.type =TYPE.DETAILS;
        holder.url = URL_YOUTUBE_DETAILS_PT1 + idList + URL_YOUTUBE_DETAILS_PT2;

        holder.callback = callback;


        get = new Get();
        get.execute(holder);




    }

    private static class Get extends AsyncTask<GetHolder, Void, GetHolder> {
        StringBuilder sb;


        @Override
        protected GetHolder doInBackground(GetHolder... params) {


            GetHolder holder = params[0];

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
        protected void onPostExecute(GetHolder holder) {
            switch (holder.type){
                case QUERY:

                    holder.callback.onGotResults(holder.response);
                    getDetails(holder.response,holder.callback);



                    break;
                case DETAILS:
                    holder.callback.onGotDetails(holder.response);



                    break;

            }



        }


    }

    public interface Callback{
        void onGotResults(String results);
        void onGotDetails(String details);
    }


}
