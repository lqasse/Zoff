package no.lqasse.zoff.Datatypes;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.RemoteActivity;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff  {

    private final int UPDATE_INTERVAL_MILLIS = 5000;
    private final Handler handler = new Handler();
    private String ROOM_NAME;
    private String ROOM_PASS;
    private Boolean IS_PASS_PROTECTED = false;
    private String nowPlayingURL;
    private String nowPlayingPHP;
    private String addSongUrl;
    private ArrayList<Video> videoList = new ArrayList<>();
    private PlayerActivity player;
    private RemoteActivity remote;
    private Runnable Refresh;
    private Map<String, Bitmap> imageMap = new HashMap<>();


    private Map<String, Boolean> settings = new HashMap<>();


    public Zoff(String ROOM_NAME, RemoteActivity remote) {
        ROOM_NAME.replace(" ", "");

        this.remote = remote;

        this.ROOM_NAME = ROOM_NAME;


        this.nowPlayingURL = "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";

        refreshData();


        //Schedules refreshes

        Refresh = new Runnable() {
            @Override
            public void run() {
                refreshData();
                handler.postDelayed(this, UPDATE_INTERVAL_MILLIS);
            }
        };
        handler.postDelayed(Refresh, UPDATE_INTERVAL_MILLIS);


    }

    public Zoff(String ROOM_NAME, PlayerActivity player) {
        ROOM_NAME.replace(" ", "");

        this.player = player;

        this.ROOM_NAME = ROOM_NAME;


        this.nowPlayingURL = "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";
        this.nowPlayingPHP = "http://www.zoff.no/" + ROOM_NAME + "/php/change.php";
        refreshData();


        //Schedules refreshes

        Refresh = new Runnable() {
            @Override
            public void run() {
                refreshData();
                handler.postDelayed(this, UPDATE_INTERVAL_MILLIS);
            }
        };
        handler.postDelayed(Refresh, UPDATE_INTERVAL_MILLIS);


    }


    public void refreshData() {

        Log.d("REFRESH", "Data refreshed");
        String[] input = new String[2];
        input[0] = nowPlayingURL;
        doGetRequest task = new doGetRequest();
        task.execute(input);
    }

    public void setData(String s) {
        ArrayList<Video> prevList = new ArrayList<>();


        prevList.addAll(videoList);


        videoList.clear();
        JSONObject json = null;
        JSONObject nowPlaying = null;
        JSONObject songs = null;
        JSONObject conf = null;
        String id = "";
        String title = "";
        String votes = "";
        String added = "";
        Video v;
        try {
            json = new JSONObject(s);
        } catch (Exception e) {
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

            //Decode string from html #magic
            title = Html.fromHtml(title).toString();

            v = new Video(title, id, votes, added,this);
            if (imageMap.containsKey(id)){
                Bitmap cachedImage = imageMap.get(id);
                v.setImgBig(cachedImage); //Big image for now playing
            }
            videoList.add(v);
            conf = json.getJSONObject("conf");

            if (json.has("songs")) {
                try {
                    songs = json.getJSONObject("songs");
                    if (songs.length() > 0) {


                        items = songs.names();

                        for (int i = 0; i < items.length(); i++) {
                            nowPlaying = songs.getJSONObject(items.get(i).toString());
                            id = nowPlaying.getString("id");
                            title = nowPlaying.getString("title");
                            votes = nowPlaying.getString("votes");
                            added = nowPlaying.getString("added");

                            //Decode string from html #magic
                            title = Html.fromHtml(title).toString();

                            v = new Video(title, id, votes, added, this);

                            if (imageMap.containsKey(id)) {
                                Bitmap cachedImage = imageMap.get(id);
                                v.setImg(cachedImage);
                            }
                            videoList.add(v);
                        }
                    }
                } catch (Exception e){
                    Log.d("Zoff", "No songs");
                }
            }

            //Saves settings from zoff to this

            if (!conf.has("vote")) {
                settings.put("vote", false);
            }else if (!conf.get("vote").equals("null")&&(!conf.get("vote").equals(""))){
                settings.put("vote", conf.getBoolean("vote"));
            }else {
                settings.put("vote", false);
            }

            if (!conf.has("addsongs")){
                settings.put("addsongs", false);
            } else if (!conf.get("addsongs").equals("null")&&(!conf.get("addsongs").equals(""))){
                settings.put("addsongs", conf.getBoolean("addsongs"));
            } else {
                settings.put("addsongs", false);
            }

            if (!conf.has("longsongs")){
                settings.put("longsongs", false);
            }else if (!conf.get("longsongs").equals("null")&&(!conf.get("longsongs").equals(""))){
                settings.put("longsongs", conf.getBoolean("longsongs"));
            } else {
                settings.put("longsongs", false);
            }


            if (!conf.has("frontpage")){
                settings.put("frontpage", false);
            }else if (!conf.get("frontpage").equals("null")&&(!conf.get("frontpage").equals(""))){
                settings.put("frontpage", conf.getBoolean("frontpage"));
            } else {
                settings.put("frontpage", false);
            }

            if (!conf.has("allvideos")){
                settings.put("allvideos", false);
            } else if (!conf.get("allvideos").equals("null")&&(!conf.get("allvideos").equals(""))){
                settings.put("allvideos", conf.getBoolean("allvideos"));
            } else {
                settings.put("allvideos", false);
            }

            if (!conf.has("removeplay")){
                settings.put("removeplay", false);
            }
            else if (!conf.get("removeplay").equals("null")&&(!conf.get("removeplay").equals(""))){
                settings.put("removeplay", conf.getBoolean("removeplay"));
            } else {
                settings.put("removeplay", false);
            }

            loaded();


            IS_PASS_PROTECTED = conf.has("adminpass"); //Is protected


            //sortVideos();
            Video video = videoList.get(0);
            videoList.remove(video);
            Collections.sort(videoList);
            videoList.add(0,video);








        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void setROOM_PASS(String PASS){
        this.ROOM_PASS = PASS;
    }

    public Boolean hasROOM_PASS(){

        if (this.ROOM_PASS != null){ //Redundant? Zoff doesnt save "" as pw anymore
            if (this.ROOM_PASS.equals("")){
                return true;
            }
        }

        return this.ROOM_PASS != null;




    }



    public Boolean ALL_VIDEOS(){

        return settings.get("allvideos");
    }


    public Boolean ANYONE_CAN_VOTE(){
        return !settings.get("vote");
    }

    public Boolean LONG_SONGS(){
        return settings.get("longsongs");
    }

    public Boolean ANYONE_CAN_ADD(){
        return !settings.get("addsongs"); //Reversed due to zoff confusion
    }

    private void loaded() {
        //Invoked by finished getRequest
        //player.loadVideos(this.getVideoIDs());

        if (player != null) {
            player.zoffRefreshed();
        } else if (remote != null) {
            remote.zoffRefreshed();
        }


    }

    public String getNextId() {
        Video v = videoList.get(1);
        videoList.remove(v);
        return v.getId();
    }

    public List<String> getVideoIDs() {
        ArrayList<String> ids = new ArrayList<>();
        for (Video v : videoList) {
            ids.add(v.getId());
        }

        return ids;


    }

    public String getROOM_NAME() {
        return this.ROOM_NAME;
    }

    @Override
    public String toString() {
        return getROOM_NAME() + ": " + videoList.size();
    }

    public boolean hasVideos() {
        return !videoList.isEmpty();
    }

    public ArrayList<Video> getVideos() {
        return videoList;
    }

    public ArrayList<Video> getNextVideos() {
        ArrayList<Video> nextVideos = new ArrayList<>();
        nextVideos.addAll(videoList);

        nextVideos.remove(0);


        return nextVideos;
    }

    public void L() {
        Log.d("ZOFF", this.toString());
    }

    public List<String> getNextVideoIDs() {
        ArrayList<String> nextVideos = new ArrayList();
        nextVideos.addAll(getVideoIDs());
        nextVideos.remove(0);

        return nextVideos;
    }

    public String getNowPlayingID() {
        return videoList.get(0).getId();
    }

    public void videoPlayed(String videoID) {

       imageMap.remove(videoID); //Clears low quality image from cache
        String[] input = new String[3];
        input[0] = nowPlayingURL + "thisUrl=" + videoID + "&act=save";
        sendGet get = new sendGet();
        get.execute(input);


    }

    public void forceRefresh() {
        refreshData();
    }

    public void cancelRefresh() {
        handler.removeCallbacks(Refresh);
        Log.d("REFRESH", "Cancelled");
    }

    public String getNowPlayingTitle() {
        if (hasVideos())
            return videoList.get(0).getTitle();
        return "No videos";

    }

    public Video getNowPlayingVideo() {
        return videoList.get(0);
    }

    public void voteVideo(int i) {

        if (this.ANYONE_CAN_VOTE())
        {

            String videoID = videoList.get(i).getId();
            String title = videoList.get(i).getTitle();

            String[] input = new String[2];
            input[0] = nowPlayingURL + "vote=pos&id=" + videoID + "&pass="+ROOM_PASS;
            Log.d("vote", input[0]);


            if (remote != null) {
                Toast.makeText(remote, "+1 til " + title, Toast.LENGTH_SHORT).show();
            } else if (player != null) {
                Toast.makeText(player, "+1 til " + title, Toast.LENGTH_SHORT).show();
            }
            sendGet task = new sendGet();
            task.execute(input);

        }
        else if (IS_PASS_PROTECTED && this.hasROOM_PASS())
        {
            String videoID = videoList.get(i).getId();
            String title = videoList.get(i).getTitle();

            String[] input = new String[2];
            input[0] = nowPlayingURL + "vote=pos&id=" + videoID + "&pass="+ROOM_PASS;
            Log.d("vote", input[0]);


            if (remote != null) {
                Toast.makeText(remote, "+1 til " + title, Toast.LENGTH_SHORT).show();
            } else if (player != null) {
                Toast.makeText(player, "+1 til " + title, Toast.LENGTH_SHORT).show();
            }
            sendGet task = new sendGet();
            task.execute(input);
        }
        else {
            if (remote != null) {
                Toast.makeText(remote, "This room is password protected, set a password to vote", Toast.LENGTH_LONG).show();
            } else if (player != null) {
                Toast.makeText(player, "This room is password protected, set a password to vote", Toast.LENGTH_LONG).show();
            }
        }



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
                // Log.d("InputStream", e.getLocalizedMessage());
                Log.d("ERROR", e.toString());
            }

            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {


            setData(result);
            //loaded();


        }


    }

    private class sendGet extends AsyncTask<String, Void, Void> { //IGNORE RESPONSE
        JSONObject json;
        StringBuilder sb;

        @Override
        protected Void doInBackground(String... params) {
            Log.d("BACK", params[0]);
            BufferedReader r;
            InputStream inputStream = null;
            String result = "";
            try {
                String url = (params[0]);

                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                httpclient.execute(new HttpGet(url));

                /*// make GET request to the given URL
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
                    */


            } catch (Exception e) {
                // Log.d("InputStream", e.getLocalizedMessage());
                Log.d("ERROR", e.toString());
            }

            return null;
        }


        public String sanitizeName(String name) {

            String sanitizedName = name;
            sanitizedName = sanitizedName.replaceAll(" ,", "");
            return "";
        }


    }

    public Bundle getSettingsBundle(){
        Bundle b = new Bundle();

        try {
            b.putBoolean("vote", settings.get("vote"));
            b.putBoolean("addsongs", settings.get("addsongs"));
            b.putBoolean("longsongs", settings.get("longsongs"));
            b.putBoolean("frontpage", settings.get("frontpage"));
            b.putBoolean("allvideos", settings.get("allvideos"));
            b.putBoolean("removeplay", settings.get("removeplay"));
            b.putString("adminpass", ROOM_PASS);
            b.putString("ROOM_NAME", ROOM_NAME);
        } catch (Exception e){
            e.printStackTrace();
        }

        return b;


    }

    public void saveImage(Bitmap b,String id){
        imageMap.put(id,b);

    }


    /**
     * Created by lassedrevland on 11.01.15.
     */
    public static class Video implements Comparable<Video>{
        private String title;
        private String id;
        private String votes;
        private String added;
        private boolean voted = false;
        private Bitmap img;
        private Bitmap imgBig;
        private String thumbSmall;
        private String thumbMed;
        private String thumbBig;
        private String thumbHuge;
        private double weight;
        private Zoff zoff;

        public Video(String title, String id, String votes, String added,Zoff zoff){
            this.title = title;
            this.id = id;
            this.votes = votes;
            this.added = added;
            this.zoff = zoff;
            makeImageUrl(id);
            makeWeight(votes, added);
        }

        @Override
        public int compareTo(Video another) {

            if (this.getVotesInt() != another.getVotesInt()){
                return another.getVotesInt() - this.getVotesInt(); //Descending on votes 1, 2 ,3 etc

            } else {
                return (int) (this.getAddedLong()- another.getAddedLong()); //Ascending on time, ie added earliger gives higher position
            }

        }



        public String getTitle() {
            if (title.equals("null")){
                return "There are no videos here yet!";
            }
            return title;
        }

        public String getId() {
            return id;
        }

        public String getAdded(){
            return this.added;
        }

        public String getVotes() {
            return votes;
        }



        public Bitmap getImg() {
            return img;
        }

        public Bitmap getImgBig() {
            return imgBig;
        }

        public String getThumbMed() {
            return thumbMed;
        }

        public String getThumbSmall() {
            return thumbSmall;
        }

        public String getThumbBig() {
            return thumbBig;
        }

        public String getThumbHuge() {
            return thumbHuge;
        }

        public double getWeight() {
            return weight;
        }

        public int getVotesInt(){
            return Integer.valueOf(votes);
        }

        public long getAddedLong(){
            return Long.valueOf(added);
        }

        private void makeImageUrl(String videoID){
            thumbSmall = "https://i.ytimg.com/vi/" + videoID +"/default.jpg";
            thumbMed = "https://i.ytimg.com/vi/" + videoID +"/mqdefault.jpg";
            thumbBig = "https://i.ytimg.com/vi/" + videoID +"/hqdefault.jpg";
            thumbHuge ="https://i.ytimg.com/vi/"+videoID+"/maxresdefault.jpg";
        }

        public void setImg(Bitmap img) {
            this.img = img;
            zoff.saveImage(this.getImg(), this.getId());
        }

        public void setImgBig(Bitmap imgBig) {
            this.imgBig = imgBig;
            zoff.saveImage(this.getImgBig(), this.getId());
        }


        private void makeWeight(String votesS,String addedS){

            if (!votesS.equals(null) && !addedS.equals(null)&&!votesS.equals("null") && !addedS.equals("null")){
                double votes = Double.valueOf(votesS);
                double added = Double.valueOf(addedS);

                votes ++;
                weight = (votes/added)*1000000000;
            } else {
                weight = 1;
            }






        }

    }

    public void shuffle(){
        sendGet get = new sendGet();
        String[] input = {nowPlayingURL + "shuffle=true&pass=" + ROOM_PASS};
        get.execute(input);
    }


}




