package no.lqasse.zoff.Datatypes;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import no.lqasse.zoff.Helpers.JSONTranslator;
import no.lqasse.zoff.Models.ZoffVideo;
import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.ZoffClient;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff {

    private final int NOTIFY_ZOFF_REFRESHED = 1;
    private final int NEEDS_PASS_VOTE = 2;
    private final int NEEDS_PASS_ADD = 3;
    private final int ZOFF_REFRESHED = 1;


    private final int UPDATE_INTERVAL_MILLIS = (int) TimeUnit.SECONDS.toMillis(10);
    private final Handler handler = new Handler();
    private static String ROOM_NAME;
    private static String ROOM_PASS;
    private int VIEWERS_COUNT = 0;
    private Boolean IS_PASS_PROTECTED = false;
    private static String NOWPLAYING_URL;


    private ArrayList<ZoffVideo> VIDEOLIST = new ArrayList<>();
    private PlayerActivity player;
    private RemoteActivity remote;
    private NotificationService service;
    private Runnable Refresher;


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
        ZoffClient.refresh(this);
        /*
        String[] input = {NOWPLAYING_URL};
        doGetRequest task = new doGetRequest();
        task.execute(input);
        Log.d("REFRESH", "Refreshing");
        */
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
    public void refreshed(Boolean hasInetAccess,String data) {


        VIDEOLIST.clear();
        VIDEOLIST.addAll(JSONTranslator.toZoffVideos(data));
        settings.clear();
        settings.putAll(JSONTranslator.toSettingsMap(data));
        VIEWERS_COUNT = JSONTranslator.toViews(data);
        IS_PASS_PROTECTED = JSONTranslator.hasAdminPass(data);


        if (player != null) {
            player.zoffRefreshed();
        } else if (remote != null) {
            remote.zoffRefreshed(hasInetAccess);
        } else if (service != null){
            service.zoffRefreshed();
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
            /*
            sendGet task = new sendGet();
            task.execute(voteUrl);
            */
            ZoffClient.vote(videoID);
        }
        else if (IS_PASS_PROTECTED && this.hasROOM_PASS())
        {

            showToast(TOAST_TYPES.VIDEO_VOTED, title);
            /*
            sendGet task = new sendGet();

            task.execute(voteUrl);
            */
            ZoffClient.vote(videoID);
        }
        else {
            showToast(TOAST_TYPES.NEEDS_PASS_VOTE, title);
        }



    }

    public void shuffle(){
        ZoffClient.shuffle();
        /*
        sendGet get = new sendGet();
        String[] input = {NOWPLAYING_URL + "shuffle=true&pass=" + ROOM_PASS};
        get.execute(input);
        */
    }

    public void voteSkip() {

        if (hasVideos()){
            ZoffClient.skip(getNowPlayingID());

            /*
            String[] input = {NOWPLAYING_URL + "thisUrl=" + getNowPlayingID() + "&act=save"};
            sendGet get = new sendGet();
            get.execute(input);
            */
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



    public String getVIEWERS_STRING() {


        if (VIEWERS_COUNT < 2){
            return VIEWERS_COUNT + " viewer";
        } else {
            return VIEWERS_COUNT + " viewers";
        }

    }

    public String getNextId() {
        ZoffVideo v = VIDEOLIST.get(1);

        return v.getId();
    }

    public List<String> getVideoIDs() {
        ArrayList<String> ids = new ArrayList<>();
        for (ZoffVideo v : VIDEOLIST) {
            ids.add(v.getId());
        }

        return ids;


    }

    public static String getROOM_NAME() {
        return ROOM_NAME;
    }

    public String getNowPlayingTitle() {
        if (hasVideos())
            return VIDEOLIST.get(0).getTitle();
        return "No videos";

    }

    public ZoffVideo getNowPlayingVideo() {
        return VIDEOLIST.get(0);
    }

    public ArrayList<ZoffVideo> getVideos() {
        return VIDEOLIST;
    }

    public ArrayList<ZoffVideo> getNextVideos() {

        ArrayList<ZoffVideo> nextZoffVideos = new ArrayList<>();
        nextZoffVideos.addAll(VIDEOLIST);

        if (VIDEOLIST.size()!= 0){
            nextZoffVideos.remove(0);
        }




        return nextZoffVideos;
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

    public static String getUrl(){
        return "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";
    }

    public static String getRoomPass(){
        return ROOM_PASS;
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




