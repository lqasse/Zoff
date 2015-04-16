package no.lqasse.zoff.Models;

import no.lqasse.zoff.Zoff;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class Video implements Comparable<Video>{
    private String _id = "Error";
    private String id = "Error";
    private String title = "Error";
    private int votes = 999;
    private int duration = 0;
    private int added = 0;
    private String[] guids;
    private Boolean now_playing = false;

    public Video(String title, String id, String votes, String added){
        this.title = title;
        this.id = id;
        this.votes = Integer.valueOf(votes);
        this.added = Integer.valueOf(added);


    }

    public Video(){

    }

    public Video(String _id, String id, String title, int votes, int added, int duration, String[] guids,Boolean now_playing) {
        this._id = _id;
        this.id = id;
        this.title = title;
        this.votes = votes;
        this.added = added;
        this.duration = duration;
        this.guids = guids;
        this.now_playing = now_playing;
    }

    @Override
    public int compareTo(Video another) {

        if (this.getVotesInt() != another.getVotesInt()){
            return another.getVotesInt() - this.getVotesInt(); //Descending on votes 1, 2 ,3 etc

        } else {
            return (int) (this.getAddedLong()- another.getAddedLong()); //Ascending on time, ie added earliger gives higher position
        }

    }


    public String getVotesString() {
        return Integer.toString(this.votes);
    }


    public static String getThumbMed(String id){
        return "https://i.ytimg.com/vi/" + id +"/mqdefault.jpg";
    }

    public String getThumbMed() {

        return "https://i.ytimg.com/vi/" + id +"/mqdefault.jpg";
    }

    public String getThumbSmall() {
        return "https://i.ytimg.com/vi/" + id +"/default.jpg";
    }

    public String getImageBig() {
        return "https://i.ytimg.com/vi/" + id +"/hqdefault.jpg";
    }

    public  String getThumbHuge() {
        return "https://i.ytimg.com/vi/" + id +"/maxresdefault.jpg";
    }

    public static String getThumbHuge(String id) {
        return "https://i.ytimg.com/vi/" + id +"/maxresdefault.jpg";
    }

    public int getVotesInt(){
        return Integer.valueOf(votes);
    }

    public long getAddedLong(){
        return Long.valueOf(added);
    }

    public String get_id() {
        return _id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getVotes() {
        return votes;
    }

    public int getDuration() {
        return duration;
    }

    public int getAdded() {
        return added;
    }

    public String[] getGuids() {
        return guids;
    }

    public Boolean getNow_playing() {
        return now_playing;
    }
}
