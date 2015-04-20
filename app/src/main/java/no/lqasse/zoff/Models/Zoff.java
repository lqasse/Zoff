package no.lqasse.zoff.Models;

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

import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Interfaces.ZoffListener;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Server.SocketJSONTranslator;
import no.lqasse.zoff.Server.SocketServer;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff {

    private static final String LOG_IDENTIFIER         = "Zoff_LOG";

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


    private String channelName;
    private String adminpass = "";
    private int viewersCount = 0;
    private int skipsCount = 0;
    private SocketServer server;

    private ArrayList<Video> videoList = new ArrayList<>();
    private ArrayList<Video> nextVideosList = new ArrayList<>();
    private Map<String, Boolean> settings = new HashMap<>();

    private ZoffListener listener;

    private String android_id;






    public Zoff(String channelName, ZoffListener listener) {
        this.channelName = channelName;
        log(channelName);

        if (listener instanceof Activity){
            android_id = Settings.Secure.getString( ((Activity) listener).getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else if (listener instanceof Service){
            android_id = Settings.Secure.getString( ((Service) listener).getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }

        server = new SocketServer(channelName,this,android_id);

        this.listener = listener;


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

        if (!isEmpty() && listener != null ){
            (listener).onZoffRefreshed();
        }


    }

    public void viewersChanged(int viewers){
        this.viewersCount = viewers;
        if (!isEmpty() && listener !=null){
            (listener).onViewersChanged();
        }


    }

    public void onCorrectPassword(String password){
        adminpass = password;
        listener.onCorrectPassword();

    }

    //Communication FROM server END

    public void setAdminpass(String PASS){
        this.adminpass = PASS;
    }

    //Communication TO server START
    public void vote(Video video) {
        server.vote(video, adminpass);

    }

    public void shuffle(){
       server.shuffle(adminpass);
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
            b.putString("channelName", channelName);
        } catch (Exception e){
            e.printStackTrace();
        }

        return b;


    }

    public String getViewersCount(){


        if (viewersCount < 2){
            return viewersCount + " viewer";
        } else {
            return viewersCount + " viewers";
        }

    }

    public String getSkips(){
        if (skipsCount > 0){
            return skipsCount +"/"+ viewersCount + " skipped";
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

    public String getChannelName() {
        return channelName;
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

    @Override
    public String toString() {
        return getChannelName() + ": " + videoList.size();
    }

    public void disconnect(){

       server.off();
       listener = null;
       ImageCache.empty();


    }

    private void log(String data){
        Log.i(LOG_IDENTIFIER,data);
    }

    public boolean isEmpty(){
        return videoList.isEmpty();
    }

    public Map<String,Boolean> getSettings(){
        return this.settings;
    }










}




