package no.lqasse.zoff.Datatypes;

import android.app.Activity;
import android.content.Context;
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
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Models.ZoffVideo;
import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.Search.SearchActivity;
import no.lqasse.zoff.Server;

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
    private Boolean IS_PASS_PROTECTED = false;
    private static String NOWPLAYING_URL;




    private ArrayList<ZoffVideo> videoList = new ArrayList<>();
    private Context context;
    private Runnable Refresher;


    private Map<String, Boolean> settings = new HashMap<>();



    public Zoff(String ROOM_NAME, Context context) {
        init(ROOM_NAME);
        this.context = context;
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

        videoList.clear();
        videoList.addAll(JSONTranslator.toZoffVideos(data));

        settings.clear();
        settings.putAll(JSONTranslator.toSettingsMap(data));

        VIEWERS_COUNT = JSONTranslator.toViews(data);
        IS_PASS_PROTECTED = JSONTranslator.hasAdminPass(data);



        ((Zoff_Listener) context).zoffRefreshed(true);



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

    public void vote(ZoffVideo selectedVideo) {

        String videoID = selectedVideo.getId();
        String title = selectedVideo.getTitle();

        if (this.ANYONE_CAN_VOTE())
        {
            ToastMaster.showToast(context, ToastMaster.TYPE.VIDEO_VOTED,title);

            Server.vote(videoID);
        }
        else if (IS_PASS_PROTECTED && this.hasROOM_PASS())
        {

            ToastMaster.showToast(context, ToastMaster.TYPE.VIDEO_VOTED,title);
            Server.vote(videoID);
        }
        else {

            ToastMaster.showToast(context, ToastMaster.TYPE.NEEDS_PASS_VOTE);

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
        return !videoList.isEmpty();
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
        ZoffVideo v = videoList.get(1);

        return v.getId();
    }

    public List<String> getVideoIDs() {
        ArrayList<String> ids = new ArrayList<>();
        for (ZoffVideo v : videoList) {
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

    public ZoffVideo getNowPlayingVideo() {
        return videoList.get(0);
    }

    public ArrayList<ZoffVideo> getVideos() {
        return videoList;
    }

    public ArrayList<ZoffVideo> getNextVideos() {

        ArrayList<ZoffVideo> nextZoffVideos = new ArrayList<>();
        nextZoffVideos.addAll(videoList);

        if (videoList.size()!= 0){
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
        return videoList.get(0).getId();
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




