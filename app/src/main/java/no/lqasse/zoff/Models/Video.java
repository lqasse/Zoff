package no.lqasse.zoff.Models;

import android.support.v7.widget.RecyclerView;

import java.util.concurrent.TimeUnit;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class Video implements Comparable<Video>{



    private String _id = "Error";
    private String id = "Error";
    private String title = "Error";
    private int votes = 999;
    private int durationSecs = 0;
    private int added = 0;
    private String[] guids;
    private Boolean isNowPlaying = false;

    public Video(String title, String id, String votes, String added){
        this.title = title;
        this.id = id;
        this.votes = Integer.valueOf(votes);
        this.added = Integer.valueOf(added);


    }

    public Video(){

    }

    public Video(Video.Builder builder){
        this._id            = builder.nested_id;
        this.id             = builder.nestedId;
        this.title          = builder.nestedTitle;
        this.votes          = builder.nestedVotes;
        this.added          = builder.nestedAdded;
        this.durationSecs   = builder.nestedDurationSecs;
        this.guids          = builder.nestedGuids;
        this.isNowPlaying   = builder.nestedNowPlaying;

    }



    public Video(String _id, String id, String title, int votes, int added, int duration, String[] guids,Boolean isNowPlaying) {
        this._id = _id;
        this.id = id;
        this.title = title;
        this.votes = votes;
        this.added = added;
        this.durationSecs = duration;
        this.guids = guids;
        this.isNowPlaying = isNowPlaying;
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


    public void addVote(){
        votes++;

    }

    public void setAdded(int added){
        this.added = added;

    }

    public void setNullVotes(){
        votes = 0;
    }

    public void setIsNowPlaying(Boolean isNowPlaying){
        this.isNowPlaying = isNowPlaying;
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

    public int getDurationSecs() {
        return durationSecs;
    }

    public long getDurationMillis(){
        return TimeUnit.SECONDS.toMillis(durationSecs);
    }

    public int getAdded() {
        return added;
    }

    public String[] getGuids() {
        return guids;
    }

    public Boolean isNowPlaying() {
        return isNowPlaying;
    }

    public static class Builder{

        String      nested_id;
        String      nestedId;
        String      nestedTitle;
        int         nestedVotes;
        int         nestedDurationSecs;
        int         nestedAdded;
        String[]    nestedGuids;
        Boolean     nestedNowPlaying;

        public Video.Builder _id(String _id){
            nested_id = _id;
            return this;
        }

        public Video.Builder id(String id){
            nestedId = id;
            return this;
        }

        public Video.Builder title(String title){
            nestedTitle = title;
            return this;
        }

        public Video.Builder votesCount(int votes){
            nestedVotes = votes;
            return this;
        }

        public Video.Builder durationSecs(int durationSecs){
            nestedDurationSecs = durationSecs;
            return this;
        }

        public Video.Builder addedMillis(int added){
            nestedAdded = added;
            return this;
        }

        public Video.Builder guids(String[] guids){
            nestedGuids = guids;
            return  this;
        }

        public Video.Builder isNowPlaying(boolean nowPlaying){
            nestedNowPlaying = nowPlaying;
            return  this;
        }

        public Video build(){
            return new Video(this);
        }












    }

    @Override
    public int compareTo(Video another) {


        if (this.isNowPlaying){
            return -1;
        } else if (another.isNowPlaying){
            return 1;
        }


        if (this.getVotesInt() != another.getVotesInt()){
            return another.getVotes() - this.getVotes(); //Descending on votes 1, 2 ,3 etc

        } else {
            return (this.getAdded() - another.getAdded()); //Ascending on time, ie added earlier gives higher position
        }

    }
}
