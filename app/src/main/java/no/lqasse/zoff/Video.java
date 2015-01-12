package no.lqasse.zoff;

import android.graphics.Bitmap;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class Video{
    private String title;
    private String id;
    private String votes;
    private String added;
    private boolean voted = false;
    private Bitmap imgSmall;
    private String thumbSmall;
    private String thumbMed;
    private String thumbBig;
    private double weight;

    public Video(String title, String id, String votes, String added){
        this.title = title;
        this.id = id;
        this.votes = votes;
        this.added = added;
        makeImageUrl(id);
        makeWeight(votes, added);
    }
    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getAdded(){
        return this.added;
    }

    public String getVotes() {
        return votes;
    }



    public Bitmap getImgSmall() {
        return imgSmall;
    }

    public String getThumbMed() {
        return thumbMed;
    }

    public String getThumbSmall() {
        return thumbSmall;
    }

    public String getThumbBig() {
        return thumbBig;
    }

    public double getWeight() {
        return weight;
    }

    public int getVotesInt(){
        return Integer.valueOf(votes);
    }

    public long getAddedLong(){
        return Long.valueOf(added);
    }

    private void makeImageUrl(String videoID){
        thumbSmall = "https://i.ytimg.com/vi/" + videoID +"/default.jpg";
        thumbMed = "https://i.ytimg.com/vi/" + videoID +"/mqdefault.jpg";
        thumbBig = "https://i.ytimg.com/vi/" + videoID +"/hqdefault.jpg";
    }

    public void setImgSmall(Bitmap imgSmall) {
        this.imgSmall = imgSmall;
    }

    private void makeWeight(String votesS,String addedS){
        double votes = Double.valueOf(votesS);
        double added = Double.valueOf(addedS);

        votes ++;
        weight = (votes/added)*1000000000;





    }

}