package no.lqasse.zoff.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by lassedrevland on 07.07.15.
 */
public class Playlist {

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

    public ArrayList getPlaylist(){
        return videoList;
    }

    public Video getNowPlaying(){
        if (nowPlaying!=null){
            return nowPlaying;
        }
        throw new NullPointerException("Now playing was null");

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
        videoMap.get(message.videoid).addVote();
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

}
