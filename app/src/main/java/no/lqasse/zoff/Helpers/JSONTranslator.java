package no.lqasse.zoff.Helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import no.lqasse.zoff.Datatypes.Zoff;
import no.lqasse.zoff.Models.ZoffVideo;

/**
 * Created by lassedrevland on 19.03.15.
 */
public  class JSONTranslator {

    public static ArrayList<ZoffVideo> toZoffVideos(String JSONString){
        JSONObject json;
        JSONObject videoObject;
        JSONObject songs;
        JSONArray songsArray;

        ArrayList<ZoffVideo> videos = new ArrayList<>();


        try {
            json = new JSONObject(JSONString);
            songs = json.getJSONObject("songs");
            songsArray = songs.names();
            for (int i = 0;i<songsArray.length();i++){
                videoObject = songs.getJSONObject(songsArray.get(i).toString());
                videos.add(toZoffVideo(videoObject));

            }

            Collections.sort(videos);

            //Get currently playing video from now playing object
            videoObject = json.getJSONObject("nowPlaying");
            JSONArray items = videoObject.names();
            videoObject = videoObject.getJSONObject(items.get(0).toString());

            videos.add(0,toZoffVideo(videoObject));


        }catch (JSONException e){
            e.printStackTrace();
        }


        return videos;
    };

    private static ZoffVideo toZoffVideo(JSONObject videoObject) throws JSONException{
        String title;
        String id;
        String added = "0";
        String votes = "0";

        id = videoObject.getString("id");
        title = videoObject.getString("title");
        if (videoObject.has("votes"))
            votes = videoObject.getString("votes");
        if (videoObject.has("added"))
            added = videoObject.getString("added");

        //Decode string from html #magic
        title = Html.fromHtml(title).toString();

        return new ZoffVideo(title,id,votes,added);

    };

    public static HashMap<String,Boolean> toSettingsMap(String JSONString){
        JSONObject conf;
        JSONObject json;
        HashMap<String,Boolean> settings = new HashMap<>();
        try {
            json = new JSONObject(JSONString);
            conf = json.getJSONObject("conf");
            String[] confLabels = {"vote","addsongs","longsongs","frontpage","allvideos","removeplay"};

            for (String label:confLabels){
                settings.put(label,conf.getBoolean(label));
            }

            if (conf.has("views")){
                JSONArray views = conf.getJSONArray("views");
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return settings;

    }

    public static int toViews(String JSONString){
        try {
            JSONObject json = new JSONObject(JSONString);
            JSONObject conf = json.getJSONObject("conf");
            JSONArray views;

            if (conf.has("views")){
                return conf.getJSONArray("views").length();

            }

        } catch (JSONException e){
            e.printStackTrace();
        }
     return -1;
    }

    public static Boolean hasAdminPass(String JSONString){
        try {
            JSONObject json = new JSONObject(JSONString);
            JSONObject conf = json.getJSONObject("conf");
            String adminpass = conf.getString("adminpass");

            return !adminpass.equals("");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }

}
