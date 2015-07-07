package no.lqasse.zoff.Models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by lassedrevland on 07.07.15.
 */
public class Playlist {
    private static final String LOG_IDENTIFIER = "Playlist";

    private Map<String,Video>  videoMap  = new HashMap<>();
    private ArrayList<Video> videoList = new ArrayList<>();
    private Video nowPlaying;


    public void setPlaylist(ArrayList<Video> videos){
        this.videoList.clear();
        this.videoList.addAll(videos);
        sortList();
        addToMap(videos);
        nowPlaying = findNowPlaying(videos);
    }

    public Video get(int index){
        return videoList.get(index);
    }

    public int size(){
        return videoList.size();
    }



    public void setNowPlaying(VideoChangeMessage message){

        getNowPlaying().nullify();

        Video nowPlaying = getNextVideo();
        nowPlaying.setIsNowPlaying(true);
        nowPlaying.setAdded(message.timeChanged);
        sortList();

    }


    public Video getNowPlaying(){
        if (videoList.isEmpty() == false){
            return videoList.get(0);
        }

        return Video.getPlaceholderVideo();
    }

    public Video getNextVideo(){
        if (videoList.size()>0){
            return videoList.get(1);
        }

        return Video.getPlaceholderVideo();
    }

    public void addVideo(Video video){
        videoMap.put(video.getId(),video);
        videoList.add(video);
        sortList();
    }

    public void addVote(VoteMessage message){
        Video video = videoMap.get(message.videoid);
        if (video == null){
            throw new NullPointerException("No such video");
        }
        videoMap.get(message.videoid).addVote(message.added);
        sortList();
    }

    public void delete(String videoID){
        Video video = videoMap.get(videoID);
        if (video == null){
            throw new NullPointerException("No such video");
        }
        videoList.remove(video);
        videoMap.remove(videoID);
        sortList();
    }

    private void addToMap(ArrayList<Video> videos){
        for(Video v:videos){
            videoMap.put(v.getId(),v);
        }
    }

    private void sortList(){
        Collections.sort(videoList);
    }

    private Video findNowPlaying(ArrayList<Video> videos){
        for (Video v:videos){
            if (v.isNowPlaying()){
                return v;
            }
        }

        throw new NoSuchElementException("isNowPlaying was false for all videos");
    }

    private void log(String message){
        Log.i(LOG_IDENTIFIER, message);
    }

}
