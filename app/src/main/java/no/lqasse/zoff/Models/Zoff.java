package no.lqasse.zoff.Models;

import android.app.Activity;
import android.app.Service;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Interfaces.ZoffListener;
import no.lqasse.zoff.Server.JSONTranslator;
import no.lqasse.zoff.Server.Server;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Zoff {

    private static final String LOG_IDENTIFIER         = "Zoff_LOG";
    private ZoffSettings settings;



    private String channelName;
    private String adminpass = "";
    private int viewersCount = 0;
    private int skipsCount = 0;
    private Server server;

    private ArrayList<Video> videoList = new ArrayList<>();
    private ArrayList<Video> nextVideosList = new ArrayList<>();

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

        server = new Server(channelName,this,android_id);

        this.listener = listener;


    }





    //Communication FROM server START

    public void showToast(String toastKeyword){
        if (listener instanceof Activity && listener!=null){
            ToastMaster.showToast(listener, toastKeyword);
        }

    }

    public void socketRefreshed(JSONArray data){

        videoList.clear();
        videoList.addAll(JSONTranslator.toVideos(data));


        settings = JSONTranslator.getSettings(data);

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

    public void ping(ZoffListener listener){
        server.ping();
        if (this.listener == null){
            this.listener = listener;
        }
    }


    public void vote(Video video) {
        server.vote(video, adminpass);
    }

    public void shuffle(){
       server.shuffle(adminpass);
    }

    public void add(String id, String title, String duration){
        server.add(id, title, adminpass, duration);
    }

    public void skip() {
        server.skip(adminpass);

    }

    public void delete(Video video){
        server.delete(video, adminpass);
    }

    public void savePassword(String password){
        server.savePassword(password);
    }


    public void saveSettings(ZoffSettings settings){
        server.saveSettings(adminpass,settings);

    }

    //Communication TO server END

    public Boolean hasPassword(){

        return !adminpass.equals("");


    }


    public boolean hasVideos() {
        return !videoList.isEmpty();
    }



    public String getViewersCount(){


        if (viewersCount < 2){
            return viewersCount + " viewer";
        } else {
            return viewersCount + " viewers";
        }

    }

    public float getPlayProgress(){
        long startTime = settings.getNowPlayingStartTimeMillis();


        long duration = getNowPlayingVideo().getDurationMillis();
        long elapsed = Calendar.getInstance().getTimeInMillis() - startTime;


        if (elapsed > duration) return 1;

        return (float)elapsed/(float)duration;

    }



    public String getCurrentPlaytime(){

        long startTime = settings.getNowPlayingStartTimeMillis();
        long duration = getNowPlayingVideo().getDurationMillis();

        long elapsed = Calendar.getInstance().getTimeInMillis() - startTime;



        long millis = elapsed;

        long hoursS = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hoursS);
        long minutesS = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutesS);
        long secondsS = TimeUnit.MILLISECONDS.toSeconds(millis) ;


        String elapsedString = "";

        if (hoursS > 0) {
            elapsedString += String.format("%02d",hoursS)+ ":";
        }

        elapsedString += String.format("%02d",minutesS) +":" + String.format("%02d",secondsS);


        millis = duration;
        long hoursD = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hoursD);
        long minutesD = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutesD);
        long secondsD = TimeUnit.MILLISECONDS.toSeconds(millis) - 1;


        String durationString = "";

        if (hoursD > 0) {
            durationString += String.format("%02d",hoursD)+ ":";
        }

        durationString += String.format("%02d",minutesD) +":" + String.format("%02d",secondsD);



        if (elapsed>duration) return durationString + " / " + durationString;

        return  elapsedString + " / " + durationString;





    }

    public String getSkips(){
        if (skipsCount > 0){
            return skipsCount +"/"+ viewersCount + " skipped";
        } else {
            return "";
        }

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



    }

    private void log(String data){
        Log.i(LOG_IDENTIFIER, data);
    }

    public boolean isEmpty(){
        return videoList.isEmpty();
    }


    public String getListener(){
        return listener.toString();
    }

    public ZoffSettings getSettings(){
        return settings;
    }










}




