package no.lqasse.zoff.Models;

import android.graphics.Bitmap;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lassedrevland on 04.04.15.
 */
public class SearchResult {
    private String title;
    private String channelTitle;
    private String description;
    private String thumbSmall;
    private String thumbMed;
    private String thumbBig;
    private String thumbHuge;
    private String videoID;
    private String publishedAt;
    private String views = "";
    private String duration = "";
    private String durationLocalized;
    private Bitmap imgSmall;
    private String viewCountLocalized = "";



    public SearchResult(String title, String channelTitle,String description, String publishedAt, String videoID, String thumbSmall, String thumbMed, String thumbBig){
        this.title = title;
        this.channelTitle = channelTitle;
        this.description = description;
        this.publishedAt = publishedAt;
        this.videoID = videoID;
        this.thumbSmall = thumbSmall;
        this.thumbMed = thumbMed;
        this.thumbBig = thumbBig;





    }

    public String getTitle(){
        return this.title;
    }

    public String getChannelTitle() {
        return channelTitle;
    }

    public String getDescription(){
        return this.description;
    }
    public String getVideoID(){
        return this.videoID;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getThumbSmall(){
        return this.thumbSmall;
    }
    public String getThumbMed() {
        return thumbMed;
    }
    public String getThumbBig() {
        return thumbBig;
    }
    public void setImgSmall(Bitmap img){
        this.imgSmall = img;
    }
    public Bitmap getImgSmall(){
        return this.imgSmall;
    }
    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
        createNiceViewcount(views);
    }

    public String getDurationLocalized() {
        return durationLocalized;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {

        String convert = duration.replace("PT", "");

        String H = "";
        String M = "";
        String S = "";


        Pattern pattern = Pattern.compile("\\d+\\w{1}");
        Matcher m = pattern.matcher(convert);

        while (m.find()){

            if (m.group().contains("H")) H = m.group();
            if (m.group().contains("M")) M = m.group();
            if (m.group().contains("S")) S = m.group();
        }

        H = H.replace("H","");
        M = M.replace("M","");
        S = S.replace("S","");


        if (!M.equals("") && M.length()==1 && !H.equals("")){

            M =  "0" + M;
        } else if (M.equals("")){
            M = "0";
        }


        if (!S.equals("") && S.length()==1) {

            S =  "0" + S;
        }


        int hoursSecs = 0;
        int minutesSecs = 0;
        int secondsSecs = 0;



        if (!H.equals("")){
            hoursSecs =  (int) TimeUnit.HOURS.toSeconds(Long.valueOf((H)));
            H = H + ":";
        }
        if (!M.equals("")){
            minutesSecs =  (int) TimeUnit.MINUTES.toSeconds(Long.valueOf((M)));
            M = M + ":";
        }

        if (S.equals("")){

            S = "00";
        } else{
            secondsSecs =  (int) TimeUnit.SECONDS.toSeconds(Long.valueOf((S)));
        }

        int durationSecss =  hoursSecs + minutesSecs + secondsSecs;




        this.durationLocalized = H + M + S;
        this.duration = Integer.toString(durationSecss);


    }

    private void createNiceViewcount(String s) {
        String out = "";
        String verdi = "";
        char[] buffer = new char[5];
        long count = Long.valueOf(s);
        int endIndex = 0;

        if (count < 10){
            endIndex = 1;
        }
        else if (count < 100){
            endIndex = 2;
        }
        else if (count < 1000) {
            endIndex = 3;
        } else if (count < 10000){
            endIndex = 2;
            verdi = " K";
        }
        else if (count < 100000){
            endIndex = 2;
            verdi = " K";
        }
        else if (count < 1000000){
            endIndex = 3;
            verdi = " K";
        }
        else if (count < 10000000){
            endIndex = 1;
            verdi = " mill";
        }
        else if (count < 100000000){
            endIndex = 2;
            verdi = " mill";
        }
        else if (count < 1000000000){
            endIndex = 3;
            verdi = " mill";
        }

        s.getChars(0,endIndex,buffer,0);

        out += "Sett ";
        for (char c:buffer){
            out += c;
        }
        out += verdi + " ganger";
        viewCountLocalized = out;
        //Log.d("views", out + ", Endindex " + Integer.toString(endIndex)+ ", views: "+ s);


    }

    public String getViewCountLocalized() {
        return viewCountLocalized;
    }
}
