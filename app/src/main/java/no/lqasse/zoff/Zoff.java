package no.lqasse.zoff;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Server.JSONTranslator;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Server.Server;

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
    private static String POST_URL;
    private int VIEWERS_COUNT = 0;
    private int SKIPS_COUNT = 0;
    private Boolean IS_PASS_PROTECTED = false;
    private static String NOWPLAYING_URL;




    private ArrayList<Video> videoList = new ArrayList<>();
    private ArrayList<Video> nextVideosList = new ArrayList<>();
    private Object listener;
    private Runnable Refresher;


    private Map<String, Boolean> settings = new HashMap<>();



    public Zoff(String ROOM_NAME, Object listener) {
        init(ROOM_NAME);


        //((Zoff_Listener) listener).zoffRefreshed(true);
        this.listener = listener;
    }



    private void init(String ROOM_NAME){

        setROOM_NAME(ROOM_NAME);
        this.NOWPLAYING_URL = "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";
        this.POST_URL =  "http://zoff.no/" + ROOM_NAME + "/php/change.php";
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
        Server.refresh(this);
    }


    public void stopRefresh() {
        handler.removeCallbacks(Refresher);
        Log.d("Zoff REFRESH", "Stopped");
    }

    public void resumeRefresh(){
        handler.removeCallbacks(Refresher);
        handler.post(Refresher);
        Log.d("Zoff REFRESH", "Restarted");
    }
    public void refreshed(Boolean hasInetAccess,String data) {

        videoList.clear();

        videoList.addAll(JSONTranslator.toZoffVideos(data));
        nextVideosList.clear();
        nextVideosList.addAll(videoList);
        nextVideosList.remove(0);

        settings.clear();
        settings.putAll(JSONTranslator.toSettingsMap(data));

        VIEWERS_COUNT = JSONTranslator.toViews(data);
        SKIPS_COUNT = JSONTranslator.toSkips(data);
        IS_PASS_PROTECTED = JSONTranslator.hasAdminPass(data);




        ((ZoffListener) listener).zoffRefreshed(true);



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

    public void vote(Video selectedVideo) {

        String videoID = selectedVideo.getId();
        String title = selectedVideo.getTitle();


        if (this.ANYONE_CAN_VOTE())
        {

            ToastMaster.showToast(listener, ToastMaster.TYPE.VIDEO_VOTED,title);

            Server.vote(videoID);
        }
        else if (IS_PASS_PROTECTED && this.hasROOM_PASS())
        {

            ToastMaster.showToast(listener, ToastMaster.TYPE.VIDEO_VOTED,title);
            Server.vote(videoID);
        }
        else {

            ToastMaster.showToast(listener, ToastMaster.TYPE.NEEDS_PASS_TO_VOTE);

        }



    }

    public void shuffle(){
        Server.shuffle();

    }

    public void voteSkip() {

        if (hasVideos()){
            Server.skip(getNowPlayingID());

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

    public Boolean allVideosAllowed(){

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
        return !videoList.isEmpty();
    }

    public Bundle getSettingsBundle(){
        Bundle b = new Bundle();

        try {

            for (String key : settings.keySet()){
                b.putBoolean(key, settings.get(key));
            }

            b.putString("adminpass", ROOM_PASS);
            b.putString("ROOM_NAME", ROOM_NAME);
        } catch (Exception e){
            e.printStackTrace();
        }

        return b;


    }



    public String getViewers(){


        if (VIEWERS_COUNT < 2){
            return VIEWERS_COUNT + " viewer";
        } else {
            return VIEWERS_COUNT + " viewers";
        }

    }

    public String getSkips(){
        if (SKIPS_COUNT > 0){
            return SKIPS_COUNT+"/"+VIEWERS_COUNT + " skipped";
        } else {
            return "";
        }

    }

    public boolean allowSkip(){

        if (settings.containsKey("skip")){
            return (settings.get("skip"));


        }

        return false;



    }

    public boolean allowShuffle(){
        if (settings.containsKey("shuffle")){
            return settings.get("shuffle");
        }
        return false;
    }

    public String getNextId() {
        Video v = videoList.get(1);

        return v.getId();
    }

    public List<String> getVideoIDs() {
        ArrayList<String> ids = new ArrayList<>();
        for (Video v : videoList) {
            ids.add(v.getId());
        }

        return ids;


    }

    public static String getROOM_NAME() {
        return ROOM_NAME;
    }

    public String getNowPlayingTitle() {
        if (hasVideos())
            return videoList.get(0).getTitle();
        return "No videos";

    }

    public Video getNowPlayingVideo() {
        return videoList.get(0);
    }

    public ArrayList<Video> getVideos() {
        return videoList;
    }

    public ArrayList<Video> getNextVideos() {



        return nextVideosList;
    }



    public List<String> getNextVideoIDs() {
        ArrayList<String> nextVideos = new ArrayList();
        nextVideos.addAll(getVideoIDs());
        nextVideos.remove(0);

        return nextVideos;
    }


    public String getNowPlayingID() {
        if (videoList.isEmpty()){
            return null;
        } else {
            return videoList.get(0).getId();
        }

    }

    public static String getUrl(){
        return "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";
    }
    public static String getPOST_URL(){
        return POST_URL;
    }

    public static String getRoomPass(){
        return ROOM_PASS;
    }



    @Override
    public String toString() {
        return getROOM_NAME() + ": " + videoList.size();
    }










}




