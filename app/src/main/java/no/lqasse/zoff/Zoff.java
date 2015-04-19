package no.lqasse.zoff;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Interfaces.ZoffListener;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Server.SocketJSONTranslator;
import no.lqasse.zoff.Server.SocketServer;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff {

    public static final String SETTINGS_KEY_ADD_SONGS  = "addsongs";
    public static final String SETTINGS_KEY_ALL_VIDEOS = "allvideos";
    public static final String SETTINGS_KEY_FRONTPAGE  = "frontpage";
    public static final String SETTINGS_KEY_LONG_SONGS = "longsongs";
    public static final String SETTINGS_KEY_SHUFFLE    = "shuffle";
    public static final String SETTINGS_KEY_SKIP       = "skip";
    public static final String SETTINGS_KEY_VOTE       = "vote";
    public static final String SETTINGS_KEY_REMOVE_PLAY= "removeplay";

    public static final String[] SETTINGS_KEYS =
            {       SETTINGS_KEY_ADD_SONGS,
                    SETTINGS_KEY_ALL_VIDEOS,
                    SETTINGS_KEY_FRONTPAGE,
                    SETTINGS_KEY_LONG_SONGS,
                    SETTINGS_KEY_SHUFFLE,
                    SETTINGS_KEY_SKIP,
                    SETTINGS_KEY_VOTE,
                    SETTINGS_KEY_REMOVE_PLAY
            };

    private static final String LOG_IDENTIFIER = "Zoff_LOG";
    private static String ROOM_NAME;
    private  String adminpass = "";
    private static String POST_URL;
    private int viewers = 0;
    private int SKIPS_COUNT = 0;
    private Boolean IS_PASS_PROTECTED = false;
    private static String NOWPLAYING_URL;
    private SocketServer server;

    private ArrayList<Video> videoList = new ArrayList<>();
    private ArrayList<Video> nextVideosList = new ArrayList<>();


    private ZoffListener listener;

    private String android_id;





    private Map<String, Boolean> settings = new HashMap<>();




    public Zoff(String ChannelName, ZoffListener listener) {
        init(ChannelName);
        log(ChannelName);

        if (listener instanceof Activity){
            android_id = Settings.Secure.getString( ((Activity) listener).getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else if (listener instanceof Service){
            android_id = Settings.Secure.getString( ((Service) listener).getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }

        server = new SocketServer(ChannelName,this,android_id);

        this.listener = listener;


    }



    private void init(String ROOM_NAME){

        setROOM_NAME(ROOM_NAME);
        this.NOWPLAYING_URL = "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";
        this.POST_URL =  "http://zoff.no/" + ROOM_NAME + "/php/change.php";


    }

    //Communication FROM server START

    public void showToast(String toastKeyword){
        if (listener instanceof Activity && listener!=null){
            ToastMaster.showToast(listener,toastKeyword);
        }

    }

    public void socketRefreshed(JSONArray data){

        videoList.clear();
        videoList.addAll(SocketJSONTranslator.toVideos(data));
        settings.clear();
        settings.putAll(SocketJSONTranslator.toSettingsMap(data));

        if (!empty() && listener != null ){
            (listener).onZoffRefreshed();
        }


    }

    public void viewersChanged(int viewers){
        this.viewers = viewers;
        if (!empty() && listener !=null){
            (listener).onViewersChanged();
        }


    }

    public void onCorrectPassword(String password){
        adminpass = password;
        listener.onCorrectPassword();

    }

    //Communication FROM server END



    public void setROOM_PASS(String PASS){
        this.adminpass = PASS;
    }

    private void setROOM_NAME(String ROOM_NAME){
        ROOM_NAME = ROOM_NAME.replace(" ","");
        char[] nameArray = ROOM_NAME.toCharArray();
        nameArray[0] = Character.toUpperCase(nameArray[0]);
        this.ROOM_NAME = new String(nameArray);
    }


    //Communication TO server START
    public void vote(Video video) {
        server.vote(video, adminpass);

    }

    public void shuffle(){
       // Server.shuffle();
    }

    public void add(String id, String title, String duration){
        server.add(id,title,adminpass, duration);
    }

    public void skip() {
        server.skip(adminpass);

    }

    public void delete(Video video){
        server.delete(video,adminpass);
    }

    public void savePassword(String password){
        server.savePassword(password);
    }

    public void saveSettings(Boolean[] settings){

        //Settings should be = [voting, addsongs, longsongs, frontpage, allvideos, removeplay, skipping, shuffling];
        server.saveSettings(adminpass,settings);


    }

    //Communication TO server END

    public Boolean hasPassword(){

        return !adminpass.equals("");


    }

    public Boolean allVideosAllowed(){

        return settings.get("allvideos");
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

            b.putString("adminpass", adminpass);
            b.putString("ROOM_NAME", ROOM_NAME);
        } catch (Exception e){
            e.printStackTrace();
        }

        return b;


    }



    public String getViewers(){


        if (viewers < 2){
            return viewers + " viewer";
        } else {
            return viewers + " viewers";
        }

    }

    public String getSkips(){
        if (SKIPS_COUNT > 0){
            return SKIPS_COUNT+"/"+ viewers + " skipped";
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

        if (videoList.size() > 1){
            return videoList.get(1).getId();
        }

        return "";
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

    public String getAdminpass(){
        return adminpass;
    }



    @Override
    public String toString() {
        return getROOM_NAME() + ": " + videoList.size();
    }

    public void disconnect(){

       server.off();
       listener = null;
       ImageCache.empty();


    }

    private void log(String data){
        Log.i(LOG_IDENTIFIER,data);
    }

    public boolean empty(){
        return videoList.isEmpty();
    }

    public Map<String,Boolean> getSettings(){
        return this.settings;
    }










}




