package no.lqasse.zoff;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import no.lqasse.zoff.Search.SearchActivity;
import no.lqasse.zoff.Search.YouTube;

/**
 * Created by lassedrevland on 05.04.15.
 */
public class SpotifyServer {

    private enum TYPE{getAndSearch, getAndSet

    }


    public static void getAndSearchYoutube(String url, Context context){

        String spotifyAPI = "https://api.spotify.com/v1/tracks/";
        String baseUrlToRemove = "http://open.spotify.com/track/";

        getHolder holder = new getHolder();



        if (url.contains(baseUrlToRemove)){
            holder.url = spotifyAPI + url.split("/")[url.split("/").length-1];
            holder.type = TYPE.getAndSearch;
            holder.context = context;

            getInfo get = new getInfo();
            get.execute(holder);

        }

    }

    public static void getSearchString(String url, TextView textView){
        String spotifyAPI = "https://api.spotify.com/v1/tracks/";
        String baseUrlToRemove = "http://open.spotify.com/track/";

        getHolder holder = new getHolder();



        if (url.contains(baseUrlToRemove)){
            holder.url = spotifyAPI + url.split("/")[url.split("/").length-1];
            holder.textView = textView;
            holder.type = TYPE.getAndSet;

            getInfo get = new getInfo();
            get.execute(holder);


        }






    }

    private static class getHolder{
        String url;
        TextView textView;
        String response;
        TYPE type;
        Context context;

    }

    private static class getInfo extends AsyncTask<getHolder,Void,getHolder>{
        @Override
        protected getHolder doInBackground(getHolder... params) {

            getHolder holder = params[0];
            HttpClient client = new DefaultHttpClient();
            String responseString = "";
            try {
                HttpResponse response = client.execute(new HttpGet(holder.url));

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line;
                while((line = reader.readLine())!=null){
                    responseString = responseString + line;

                }

                holder.response = responseString;




            } catch (Exception e){
                e.printStackTrace();
            }



            return holder;
        }

        @Override
        protected void onPostExecute(getHolder getHolder) {
            super.onPostExecute(getHolder);
            String artist = "";
            String track = "";
            if (getHolder.response!=null){
                try{


                    JSONObject json = new JSONObject(getHolder.response);
                    JSONArray array = json.getJSONArray("artists");
                    artist = ((JSONObject)array.get(0)).getString("name");
                    track = json.getString("name");



                }catch (JSONException e){

                }
            }

            switch (getHolder.type){
                case getAndSearch:
                    YouTube.search(getHolder.context,artist + track,true,true);
                    break;
                case getAndSet:
                    getHolder.textView.setText(artist +" " + track);

            }






        }
    }
}
