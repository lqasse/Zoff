package no.lqasse.zoff;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff {
    private String room;
    private String nowPlayingURL;
    private String addSongUrl;
    private ArrayList<Video> videoList = new ArrayList<>();
    private Player player;
    private int kek;

    public Zoff(String room, Player player){
       room.replace(" ","");

        this.player = player;

        this.room = room;


        this.nowPlayingURL = "http://www.zoff.no/" + room + "/php/change.php?";
        refreshData();


    }

    public void refreshData(){
        String[] input = new String[2];
        input[0] = nowPlayingURL;
        doGetRequest task = new doGetRequest();
        task.execute(input);
    }

    public void setData(String s){

        JSONObject json = null;
        JSONObject nowPlaying = null;
        JSONObject songs = null;
        String id = "";
        String title = "";
        String votes = "";
        String added = "";
        try{
            json = new JSONObject(s);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            nowPlaying = json.getJSONObject("nowPlaying");
            JSONArray items = nowPlaying.names();
            nowPlaying = nowPlaying.getJSONObject(items.get(0).toString());
            id = nowPlaying.getString("id");
            title = nowPlaying.getString("title");
            votes = nowPlaying.getString("votes");
            added = nowPlaying.getString("added");

            videoList.add(new Video(title, id, votes, added));


            songs = json.getJSONObject("songs");
            items = songs.names();

            for (int i = 0;i<items.length();i++){
                nowPlaying = songs.getJSONObject(items.get(i).toString());
                id = nowPlaying.getString("id");
                title = nowPlaying.getString("title");
                votes = nowPlaying.getString("votes");
                added = nowPlaying.getString("added");
                videoList.add(new Video(title, id, votes, added));
            }
            sortVideos();
            //populateNowPlaying();




        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sortVideos() {

        String votes;
        String added;
        long maxAdded = 2000000000;
        int minVote = 0;
        double weight = 0;

        Video selected = null;
        ArrayList<Video> sortedList = new ArrayList<>();
        sortedList.add(videoList.get(0));
        videoList.remove(0);
        int endLoop = videoList.size();
        for (int i = 0; i < endLoop; i++) {
            for (Video v : videoList) {
                if(v.getWeight() >= weight){
                    minVote = v.getVotesInt();
                    maxAdded = v.getAddedLong();
                    weight = v.getWeight();
                    selected = v;
                }



            }
            maxAdded = 2000000000;
            minVote = 0;
            weight = 0;
            sortedList.add(selected);
            videoList.remove(selected);
            selected = null;




        }
        videoList = sortedList;
    }

    private void loaded(){
        //Invoked by finished getRequest
        player.loadVideos(this.getVideoIDs());

    }



    private class doGetRequest extends AsyncTask<String, Void, String> {
        JSONObject json;
        StringBuilder sb;

        @Override
        protected String doInBackground(String... params) {
            Log.d("BACK", params[0]);
            BufferedReader r;
            InputStream inputStream = null;
            String result = "";
            try {
                String url = (params[0]);

                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null){
                    r = new BufferedReader(new InputStreamReader(inputStream));
                    sb = new StringBuilder();
                    String line;
                    while((line = r.readLine()) !=null){
                        sb.append(line);


                    }



                }

                else
                    result = "Did not work!";

            } catch (Exception e) {
                // Log.d("InputStream", e.getLocalizedMessage());
                Log.d("ERROR", e.toString());
            }

            return sb.toString();
        }
        @Override
        protected void onPostExecute(String result){


            setData(result);
            loaded();


        }


    }

    public String getNextId(){
        Video v = videoList.get(0);
        videoList.remove(v);
        return v.getId();
    }

    public List<String> getVideoIDs(){
            ArrayList<String> ids = new ArrayList<>();
        for (Video v:videoList){
            ids.add(v.getId());
        }

        return ids;



    }

    public String getRoom(){
        return this.room;
    }

    @Override
    public String toString() {
        return getRoom() + ": " + videoList.size();
    }

    public boolean hasVideos(){
        return !videoList.isEmpty();
    }

    public ArrayList<Video> getVideos(){
        return videoList;
    }

    public void L(){
        Log.d("ZOFF", this.toString());
    }


}




