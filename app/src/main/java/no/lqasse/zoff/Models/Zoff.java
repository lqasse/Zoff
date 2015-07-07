package no.lqasse.zoff.Models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by lassedrevland on 05.06.15.
 */
public class Zoff {
    public static final String BUNDLE_KEY_CHANNEL = "channel";
    public static final String BUNDLE_KEY_IS_NEW_CHANNEL = "NEW";

    private Settings settings = new Settings();
    private Playlist playlist = new Playlist();
    private String channel;
    private String adminpass = "";
    private String android_id = "";
    private int currentViewers = 0;
    private int currentSkips = 0;
    private Boolean isUnlocked = false;


    private HashMap<String,Video> idMap = new HashMap<>();
    private ArrayList<Video> videos = new ArrayList<>();
    private ArrayList<Video> nextVideos = new ArrayList<>();

    public Zoff(String channel){
        this.channel = channel;
    }

    public void setCurrentViewers(int currentViewers) {
        this.currentViewers = currentViewers;
    }

    public void setCurrentSkips(int currentSkips) {
        this.currentSkips = currentSkips;
    }

    public void setVideos(ArrayList<Video> videos) {
        playlist.setPlaylist(videos);
    }


    public void addVideo(Video video){
        playlist.addVideo(video);
    }

    public void addVote(VoteMessage message){
        playlist.addVote(message);

    }

    public void deleteVideo(String videoID){
        playlist.delete(videoID);

    }

    public void setNowPlaying(Video nowPlaying){

    }


    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setAdminpass(String adminpass) {
        this.adminpass = adminpass;
        isUnlocked = true;
    }

    public Settings getSettings() {
        return settings;
    }

    public String getChannel() {
        return channel;
    }

    public String getChannelRaisedFirstLetter(){
        return Character.toUpperCase(channel.charAt(0)) + channel.substring(1);
    }

    public String getAdminpass() {
        return adminpass;
    }

    public String getCurrentViewers() {
        if (currentViewers < 2 && currentViewers != 0){
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

    public Playlist getPlaylist() {
        return playlist;
    }

    public ArrayList<Video> getNextVideos() {
        return nextVideos;
    }

    public Video getPlayingVideo(){
       return playlist.getNowPlaying();
    }

    public String getNextVideoId(){
         return  playlist.getNextVideo().getId();


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
        long duration = playlist.getNowPlaying().getDurationMillis();

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
