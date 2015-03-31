package no.lqasse.zoff.Datatypes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Remote.RemoteActivity;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff  {

    private final int NOTIFY_ZOFF_REFRESHED = 1;
    private final int NEEDS_PASS_VOTE = 2;
    private final int NEEDS_PASS_ADD = 3;
    private final int ZOFF_REFRESHED = 1;


    private final int UPDATE_INTERVAL_MILLIS = (int) TimeUnit.SECONDS.toMillis(10);
    private final Handler handler = new Handler();
    private String ROOM_NAME;
    private String ROOM_PASS;
    private int VIEWERS_COUNT = 0;
    private Boolean IS_PASS_PROTECTED = false;
    private String NOWPLAYING_URL;
    private ArrayList<Video> VIDEOLIST = new ArrayList<>();
    private PlayerActivity player;
    private RemoteActivity remote;
    private NotificationService service;
    private Runnable Refresher;
    private Bitmap blurred_bg;
    private Map<String, Bitmap> imageMap = new HashMap<>();
    private Map<String, Boolean> settings = new HashMap<>();



    public Zoff(String ROOM_NAME, RemoteActivity remote) {
        init(ROOM_NAME);
        this.remote = remote;

    }

    public Zoff(String ROOM_NAME, PlayerActivity player) {
        init(ROOM_NAME);
        this.player = player;

    }

    public Zoff(String ROOM_NAME, NotificationService service) {
        init(ROOM_NAME);
        this.service = service;
    }

    private void init(String ROOM_NAME){

        setROOM_NAME(ROOM_NAME);
        this.NOWPLAYING_URL = "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";
        refreshData();

        //Schedules refreshes
        Refresher = new Runnable() {
            @Override
            public void run() {
                refreshData();
                handler.postDelayed(this, UPDATE_INTERVAL_MILLIS);
            }
        };
        handler.postDelayed(Refresher, UPDATE_INTERVAL_MILLIS);

    }

    public void refreshData() {
        String[] input = {NOWPLAYING_URL};
        doGetRequest task = new doGetRequest();
        task.execute(input);
        Log.d("REFRESH", "Refreshing");
    }

    public void forceRefresh() {
        refreshData();
    }

    public void pauseRefresh() {
        handler.removeCallbacks(Refresher);
        Log.d("REFRESH", "Paused");
    }

    public void resumeRefresh(){
        handler.removeCallbacks(Refresher);
        handler.post(Refresher);
        Log.d("REFRESH", "Restarted");
    }
    private void refreshed(Boolean hasInetAccess) {

        if (player != null) {
            player.zoffRefreshed();
        } else if (remote != null) {
            remote.zoffRefreshed(hasInetAccess);
        } else if (service != null){
            service.zoffRefreshed();
        }

    }

    public void setData(String s) {
        VIDEOLIST.clear();
        JSONObject json = null;
        JSONObject nowPlaying ;
        JSONObject songs;
        JSONObject conf;
        String id;
        String title;
        String votes = "0";
        String added = "0";
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
            if (nowPlaying.has("votes"))
                votes = nowPlaying.getString("votes");
            if (nowPlaying.has("added"))
                added = nowPlaying.getString("added");

            //Decode string from html #magic
            title = Html.fromHtml(title).toString();

            v = new Video(title, id, votes, added,this);
            if (imageMap.containsKey(id+"_hq")){
                Bitmap cachedImage = imageMap.get(id+"_hq");
                v.setImgBig(cachedImage); //Big image for now playing
                if (imageMap.containsKey(id+"_blur")){
                    blurred_bg = imageMap.get(id+"_blur");

                } else {
                    createBlurBg(cachedImage,id);
                }

            }

            VIDEOLIST.add(v);

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
                            VIDEOLIST.add(v);
                        }
                    }
                } catch (Exception e){
                    Log.d("Zoff", "No songs");
                }
            }

            //Saves settings from zoff to this

            String[] confLabels = {"vote","addsongs","longsongs","frontpage","allvideos","removeplay"};

            for (String label:confLabels){
                if (!conf.has(label)) {
                    settings.put(label, false);
                }else if (!conf.get(label).equals("null")&&(!conf.get(label).equals(""))){
                    settings.put(label, conf.getBoolean(label));
                }else {
                    settings.put(label, false);
                }
            }

            if (conf.has("views")){
                JSONArray views = conf.getJSONArray("views");
                VIEWERS_COUNT = views.length();
            }


            IS_PASS_PROTECTED = conf.has("adminpass"); //Is protected


            //sortVideos();
            Video video = VIDEOLIST.get(0);
            VIDEOLIST.remove(video);
            Collections.sort(VIDEOLIST);
            VIDEOLIST.add(0, video);

            refreshed(true);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setROOM_PASS(String PASS){
        this.ROOM_PASS = PASS;
    }

    private void setROOM_NAME(String ROOM_NAME){
        ROOM_NAME = ROOM_NAME.replace(" ","");
        char[] nameArray = ROOM_NAME.toCharArray();
        nameArray[0] = Character.toUpperCase(nameArray[0]);
        this.ROOM_NAME = new String(nameArray);
    }

    public void vote(int i) {

        String videoID = VIDEOLIST.get(i).getId();
        String title = VIDEOLIST.get(i).getTitle();
        String[] voteUrl = {NOWPLAYING_URL + "vote=pos&id=" + videoID + "&pass="+ROOM_PASS};
        if (i == 0){
            //Do nothing, this is the currently playing video

        }else if (this.ANYONE_CAN_VOTE())
        {
            showToast(TOAST_TYPES.VIDEO_VOTED, title);
            sendGet task = new sendGet();
            task.execute(voteUrl);
        }
        else if (IS_PASS_PROTECTED && this.hasROOM_PASS())
        {
            showToast(TOAST_TYPES.VIDEO_VOTED, title);
            sendGet task = new sendGet();
            task.execute(voteUrl);
        }
        else {
            showToast(TOAST_TYPES.NEEDS_PASS_VOTE, title);
        }



    }

    public void shuffle(){
        sendGet get = new sendGet();
        String[] input = {NOWPLAYING_URL + "shuffle=true&pass=" + ROOM_PASS};
        get.execute(input);
    }

    public void voteSkip() {

        if (hasVideos()){
            imageMap.remove(getNowPlayingID()); //Clears low quality image from cache
            String[] input = {NOWPLAYING_URL + "skip"};

            sendGet get = new sendGet();
            get.execute(input);
        }




    }

    public Boolean hasROOM_PASS(){

        if (this.ROOM_PASS != null){ //Redundant? Zoff doesnt save "" as pw anymore
            if (this.ROOM_PASS.equals("")){
                return true;
            }
        }

        return this.ROOM_PASS != null;




    }

    public Boolean ALL_VIDEOS_ALLOWED(){

        return settings.get("allvideos");
    }

    public Boolean ANYONE_CAN_VOTE(){
        if (settings.containsKey("vote")){
            return !settings.get("vote"); //Reversed due to zoff confusion
        }
        return true;

    }

    public Boolean LONG_SONGS(){
        return settings.get("longsongs");
    }

    public Boolean ANYONE_CAN_ADD(){

        if (settings.containsKey("addsongs")){
            return !settings.get("addsongs"); //Reversed due to zoff confusion
        }
        return true;

    }

    public boolean hasVideos() {
        return !VIDEOLIST.isEmpty();
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

    public String getVIEWERS_STRING() {


        if (VIEWERS_COUNT < 2){
            return VIEWERS_COUNT + " viewer";
        } else {
            return VIEWERS_COUNT + " viewers";
        }

    }

    public String getNextId() {
        Video v = VIDEOLIST.get(1);

        return v.getId();
    }

    public List<String> getVideoIDs() {
        ArrayList<String> ids = new ArrayList<>();
        for (Video v : VIDEOLIST) {
            ids.add(v.getId());
        }

        return ids;


    }

    public String getROOM_NAME() {
        return this.ROOM_NAME;
    }

    public String getNowPlayingTitle() {
        if (hasVideos())
            return VIDEOLIST.get(0).getTitle();
        return "No videos";

    }

    public Video getNowPlayingVideo() {
        return VIDEOLIST.get(0);
    }

    public ArrayList<Video> getVideos() {
        return VIDEOLIST;
    }

    public ArrayList<Video> getNextVideos() {

        ArrayList<Video> nextVideos = new ArrayList<>();
        nextVideos.addAll(VIDEOLIST);

        if (VIDEOLIST.size()!= 0){
            nextVideos.remove(0);
        }




        return nextVideos;
    }

    public List<String> getNextVideoIDs() {
        ArrayList<String> nextVideos = new ArrayList();
        nextVideos.addAll(getVideoIDs());
        nextVideos.remove(0);

        return nextVideos;
    }

    public String getNowPlayingID() {
        return VIDEOLIST.get(0).getId();
    }

    public void createBlurBg(Bitmap bitmap,String id){
        if (imageMap.containsKey(id + "_blur")){
            Bitmap blurbg = imageMap.get(id + "_blur");

            if (remote != null){
                remote.setBlurBg(blurbg);
            } else if (player != null){
                player.setBlurBg(blurbg);
            }
        } else if (bitmap!=null){

            blurBg blurBgTask = new blurBg();
            blurBgTask.execute(bitmap);
        }

    }

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
        private Bitmap blurredBG;

        public Video(String title, String id, String votes, String added, Zoff zoff){
            this.title = title;
            this.id = id;
            this.votes = votes;
            this.added = added;
            this.zoff = zoff;
            makeImageUrl(id);

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

        public void setImg(Bitmap img) {
            this.img = img;

            zoff.saveImage(this.getImg(), this.getId());
        }

        public Bitmap getImgBig() {
            return imgBig;
        }

        public void setImgBig(Bitmap imgBig) {
            this.imgBig = imgBig;
            zoff.saveImage(this.getImgBig(), this.getId()+"_hq");
            zoff.createBlurBg(imgBig,id);

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

        public int getVotesInt(){
            return Integer.valueOf(votes);
        }

        public long getAddedLong(){
            return Long.valueOf(added);
        }

        public Bitmap getBlurredBG() {
            return blurredBG;
        }

        public void setBlurredBG(Bitmap blurredBG) {
            this.blurredBG = blurredBG;
        }

        private void makeImageUrl(String videoID){
            thumbSmall = "https://i.ytimg.com/vi/" + videoID +"/default.jpg";
            thumbMed = "https://i.ytimg.com/vi/" + videoID +"/mqdefault.jpg";
            thumbBig = "https://i.ytimg.com/vi/" + videoID +"/hqdefault.jpg";
            thumbHuge ="https://i.ytimg.com/vi/"+videoID+"/maxresdefault.jpg";
        }

    }

    private class doGetRequest extends AsyncTask<String, Void, String> {
        JSONObject json;
        StringBuilder sb;

        @Override
        protected String doInBackground(String... params) {
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

                return sb.toString();


                }

            } catch (Exception e) {
                return "ERROR";
            }

            return "ERROR";


        }

        @Override
        protected void onPostExecute(String result) {

            if (!result.equals("ERROR")){
                setData(result);
            } else if (result.equals("ERROR")){
                //Happens if server is unreachable or response is empty
                refreshed(false);
                VIDEOLIST.clear();
                VIDEOLIST.add(new Video("Cannot access server","","","",null));
            }

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




            } catch (Exception e) {
                // Log.d("InputStream", e.getLocalizedMessage());
                Log.e("SEND GET ERROR ", e.toString());
            }

            return null;
        }


    }

    private class blurBg extends AsyncTask<Bitmap,Void,Bitmap>{


        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

            Integer radius = 80;

            Bitmap bitmap = params[0].copy(params[0].getConfig(), true);

            if (radius < 1) {
                return (null);
            }

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }


            bitmap.setPixels(pix, 0, w, 0, 0, w, h);



            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            getNowPlayingVideo().setBlurredBG(bitmap);
            saveImage(bitmap,getNowPlayingID() + "_blur");
            if (remote != null){
                remote.setBlurBg(bitmap);
            } else if (player != null){
                player.setBlurBg(bitmap);
            }

            super.onPostExecute(bitmap);
        }
    }


    public void showToast(TOAST_TYPES TYPE){
        showToast(TYPE,"");

    }

    public void showToast(TOAST_TYPES TYPE, String CONTEXTUAL_STRING){

        Toast t;
        String toastText = "Toast error";

        switch (TYPE){
            case NEEDS_PASS_VOTE:
                toastText = "This room is password protected, set a password to vote";
                break;
            case NEEDS_PASS_ADD:
                toastText = "This room is password protected, set a password to add videos";
                break;
            case VIDEO_ADDED:
                toastText = CONTEXTUAL_STRING + " was added";
                break;
            case VIDEO_VOTED:
                toastText = "+1 to " + CONTEXTUAL_STRING;
                break;
            case HOLD_TO_VOTE:
                toastText = "Click and hold to vote";
                break;
            case SHUFFLED:
                toastText = "Shuffled!";
                break;
            case NEEDS_PASS_SHUFFLE:
                toastText = "This room is password protected, set a password to shuffle";
                break;
            case EMBEDDING_DISABLED:
                toastText = CONTEXTUAL_STRING + " could not be played, embedded playback disabled.";


        }

        if (remote!=null){
            t = Toast.makeText(remote, toastText, Toast.LENGTH_SHORT);
        } else {
            t = Toast.makeText(player, toastText, Toast.LENGTH_SHORT);
        }

        View v = t.getView();
        v.setBackgroundResource(R.drawable.toast_background);
        t.show();



    }

    @Override
    public String toString() {
        return getROOM_NAME() + ": " + VIDEOLIST.size();
    }







}




