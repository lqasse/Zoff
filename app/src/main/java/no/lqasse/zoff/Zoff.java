package no.lqasse.zoff;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Interfaces.ZoffListener;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Server.JSONTranslator;
import no.lqasse.zoff.Server.Server;
import no.lqasse.zoff.Server.SocketJSONTranslator;
import no.lqasse.zoff.Server.SocketServer;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff {


    private static final String LOG_IDENTIFIER = "Zoff_LOG";
    private static String ROOM_NAME;
    private static String adminpass = "";
    private static String POST_URL;
    private int viewers = 0;
    private int SKIPS_COUNT = 0;
    private Boolean IS_PASS_PROTECTED = false;
    private static String NOWPLAYING_URL;
    private SocketServer server;

    private ArrayList<Video> videoList = new ArrayList<>();
    private ArrayList<Video> nextVideosList = new ArrayList<>();
    private Object listener;

    private String android_id;





    private Map<String, Boolean> settings = new HashMap<>();




    public Zoff(String ROOM_NAME, Object listener) {
        init(ROOM_NAME);
        log(ROOM_NAME);

        if (listener instanceof Activity){
            android_id = Settings.Secure.getString( ((Activity) listener).getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else if (listener instanceof Service){
            android_id = Settings.Secure.getString( ((Service) listener).getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        }

        server = new SocketServer(ROOM_NAME,this,android_id);

        this.listener = listener;


    }



    private void init(String ROOM_NAME){

        setROOM_NAME(ROOM_NAME);
        this.NOWPLAYING_URL = "http://www.zoff.no/" + ROOM_NAME + "/php/change.php?";
        this.POST_URL =  "http://zoff.no/" + ROOM_NAME + "/php/change.php";


    }

    public void showToast(String toastKeyword){

        if (listener instanceof Activity){
            ToastMaster.showToast(listener,toastKeyword);
        }




    }

    public void socketRefreshed(JSONArray data){

        videoList.clear();
        videoList.addAll(SocketJSONTranslator.toVideos(data));
        settings.clear();
        settings.putAll(SocketJSONTranslator.toSettingsMap(data));

        if (!empty()){
            ((ZoffListener) listener).zoffRefreshed();
        }


    }

    public void viewersChanged(int viewers){
        this.viewers = viewers;

        if (!empty()){
            ((ZoffListener) listener).viewersChanged();
        }


    }
    public void refreshed(Boolean hasInetAccess,String data) {



        videoList.clear();

        videoList.addAll(JSONTranslator.toZoffVideos(data));
        nextVideosList.clear();
        nextVideosList.addAll(videoList);
        nextVideosList.remove(0);

        settings.clear();
        settings.putAll(JSONTranslator.toSettingsMap(data));

        viewers = JSONTranslator.toViews(data);
        SKIPS_COUNT = JSONTranslator.toSkips(data);
        IS_PASS_PROTECTED = JSONTranslator.hasAdminPass(data);




        ((ZoffListener) listener).zoffRefreshed();



    }


    public void setROOM_PASS(String PASS){
        this.adminpass = PASS;
    }

    private void setROOM_NAME(String ROOM_NAME){
        ROOM_NAME = ROOM_NAME.replace(" ","");
        char[] nameArray = ROOM_NAME.toCharArray();
        nameArray[0] = Character.toUpperCase(nameArray[0]);
        this.ROOM_NAME = new String(nameArray);
    }

    public void vote(Video video) {
        server.vote(video, adminpass);

    }

    public void shuffle(){
        Server.shuffle();
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

    public Boolean hasROOM_PASS(){

        return Zoff.adminpass.equals("");


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

    public static String getAdminpass(){
        return adminpass;
    }



    @Override
    public String toString() {
        return getROOM_NAME() + ": " + videoList.size();
    }

    public void disconnect(){

       server.off();


    }

    private void log(String data){
        Log.i(LOG_IDENTIFIER,data);
    }

    public boolean empty(){
        return videoList.isEmpty();
    }










}




