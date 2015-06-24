package no.lqasse.zoff.Models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by lassedrevland on 05.06.15.
 */
public class ZoffModel {
    public static final String BUNDLE_KEY_CHANNEL = "channel";
    public static final String BUNDLE_KEY_IS_NEW_CHANNEL = "NEW";

    private ZoffSettings settings = new ZoffSettings();
    private String channel;
    private String adminpass = "";
    private String android_id = "";
    private int currentViewers = 0;
    private int currentSkips = 0;
    private Boolean isUnlocked = false;


    private HashMap<String,Video> idMap = new HashMap<>();
    private ArrayList<Video> videos = new ArrayList<>();
    private ArrayList<Video> nextVideos = new ArrayList<>();

    public ZoffModel(String channel){
        this.channel = channel;
    }

    public void setCurrentViewers(int currentViewers) {
        this.currentViewers = currentViewers;
    }

    public String getAndroid_id() {
        return android_id;
    }

    public void setAndroid_id(String android_id) {
        this.android_id = android_id;
    }

    public void setCurrentSkips(int currentSkips) {
        this.currentSkips = currentSkips;
    }

    public void setVideos(ArrayList<Video> videos) {

        this.videos.clear();
        this.videos.addAll(videos);

        idMap.clear();
        for (Video v:videos){
            idMap.put(v.getId(),v);
        }

        Collections.sort(this.videos);



    }



    public void setNextVideos(ArrayList<Video> nextVideos) {
        this.nextVideos = nextVideos;
    }

    public void addVideo(Video video){
        videos.add(video);
        Collections.sort(videos);
    }

    public void addVote(String id){
        idMap.get(id).addVote();
        Collections.sort(videos);

    }

    public void deleteVideo(String videoID){
        Video v = idMap.get(videoID);
        videos.remove(v);
        idMap.remove(v);
        Collections.sort(videos);

    }

    public void setNextNowPlaying(){
        videos.get(0).setIsNowPlaying(false);
        videos.get(1).setIsNowPlaying(true);
        Collections.sort(videos);
    }


    public void setSettings(ZoffSettings settings) {
        this.settings = settings;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setAdminpass(String adminpass) {
        this.adminpass = adminpass;
        isUnlocked = true;
    }

    public ZoffSettings getSettings() {
        return settings;
    }

    public String getChannel() {
        return channel;
    }

    public String getAdminpass() {
        return adminpass;
    }

    public String getCurrentViewers() {
        if (currentViewers < 2 || currentViewers != 0){
            return currentViewers + " viewer";
        } else {
            return currentViewers + " viewers";
        }
    }

    public String getCurrentSkips() {
        if (currentSkips > 0){
            return currentSkips +"/"+ currentViewers + " skipped";
        } else {
            return "";
        }
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public ArrayList<Video> getNextVideos() {
        return nextVideos;
    }

    public Video getPlayingVideo(){
        if (videos.isEmpty() == false){
            return videos.get(0);
        }

        return new Video();
    }

    public Video getNextVideo(){
        if ((videos.isEmpty() == false) && (videos.size() > 2)){
            return videos.get(1);
        }

        return new Video();
    }

    public float getPlayProgress(){
        long startTime = settings.getNowPlayingStartTimeMillis();


        long duration = getPlayingVideo().getDurationMillis();
        long elapsed = Calendar.getInstance().getTimeInMillis() - startTime;


        if (elapsed > duration) return 1;

        return (float)elapsed/(float)duration;

    }

    public String getCurrentPlaytime(){

        long startTime = settings.getNowPlayingStartTimeMillis();
        long duration = getPlayingVideo().getDurationMillis();

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

    public Boolean isUnlocked(){

        return isUnlocked;


    }

    public boolean hasVideos() {
        return !videos.isEmpty();
    }

}
