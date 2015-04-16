package no.lqasse.zoff.Server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import no.lqasse.zoff.Models.Video;

/**
 * Created by lassedrevland on 16.04.15.
 */
public  class SocketJSONTranslator {


    public static ArrayList<Video> toVideos(JSONArray array){
        ArrayList<Video> videos = new ArrayList<>();

        try {
            for (int i = 0;i<array.length()-1;i++){
                Video current = toVideo(array.getJSONObject(i));


                    if (current.getNow_playing()){
                        videos.add(0,current);
                    } else {
                        videos.add(current);
                    }




            }

            return videos;

        } catch (JSONException e){
            e.printStackTrace();

            Log.d("SocketJSONTranslator", "ERROR COnverting from JSON");
            return null;
        }



    }

    public static Video toVideo(JSONObject object){
        try{


            String[] lables = {"_id","id","title","title","votes","added","now_playing"};

            for (String label :lables){
                if (!object.has(label)){
                    //The Video is broken
                    return new Video();
                }
            }

            String _id      = object.getString("_id");
            String id       = object.getString("id");
            String title    = object.getString("title");
            int votes       = object.getInt("votes");
            int duration    = object.getInt("duration");
            int added       = object.getInt("added");
            boolean now_playing = object.getBoolean("now_playing");



            JSONArray guidsJSON = object.getJSONArray("guids");

            String[] guids = new String[guidsJSON.length()];

            for (int i = 0;i<guidsJSON.length();i++){
                guids[i] = guidsJSON.getString(i);

            }

            return new Video(_id,id,title,votes,duration,added,guids,now_playing);


        } catch (JSONException e){
            e.printStackTrace();
            return new Video();
        }


    }

    public static HashMap<String,Boolean> toSettingsMap(JSONArray data){
        String[] labels = {"addsongs","allvideos","frontpage","longsongs","removeplay","shuffle","skip","vote"};
        JSONObject object;
        try {
            object = data.getJSONObject(data.length()-1);

        for (String label :labels){
            if (!object.has(label)){
                //a Setting is missing
                return null;
            }
        }


        HashMap<String,Boolean> settings = new HashMap<>();

        for (String setting:labels){
            settings.put(setting,object.getBoolean(setting));
        }

            return settings;

        }catch (JSONException e){
            e.printStackTrace();
            return null;

        }


    }

}
