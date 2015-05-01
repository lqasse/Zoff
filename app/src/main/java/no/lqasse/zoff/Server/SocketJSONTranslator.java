package no.lqasse.zoff.Server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.Zoff;

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


            String[] labels = {"_id","id","title","votes","duration","added","now_playing","guids"};
            Boolean[] hasLabel = {true,true,true,true,true,true,true,true};
            int index = 0;

            for (String label : labels){
                hasLabel[index] = object.has(label);
                index++;
            }





            String _id      = "";
            String id       = "";
            String title    = "";
            int votes       = -1;
            int duration    = 0;
            int added       = 0;
            boolean now_playing = false;

            if (hasLabel[0]){_id        = object.getString("_id"); }
            if (hasLabel[1]){id         = object.getString("id");}
            if (hasLabel[2]){title      = object.getString("title");}
            if (hasLabel[3]){votes      = object.getInt("votes");}
            if (hasLabel[4]){duration   = object.getInt("duration");}
            if (hasLabel[5]){added      = object.getInt("added");}
            if (hasLabel[6]){now_playing= object.getBoolean("now_playing");}

            JSONArray guidsJSON;
            String[] guids = {};
            if (hasLabel[7]){
                guidsJSON = object.getJSONArray("guids");
                guids = new String[guidsJSON.length()];

                for (int i = 0;i<guidsJSON.length();i++){
                    guids[i] = guidsJSON.getString(i);

                }
            }




            return new Video(_id,id,title,votes,duration,added,guids,now_playing);


        } catch (JSONException e){
            e.printStackTrace();
            return new Video();
        }


    }

    public static HashMap<String,Boolean> toSettingsMap(JSONArray data){

        JSONObject object;
        try {
            object = data.getJSONObject(data.length()-1);

        for (String label : Zoff.SETTINGS_KEYS){
            if (!object.has(label)){
                //a Setting is missing
                return null;
            }
        }


        HashMap<String,Boolean> settings = new HashMap<>();

        for (String setting:Zoff.SETTINGS_KEYS){
            settings.put(setting,object.getBoolean(setting));
        }

            return settings;

        }catch (JSONException e){
            e.printStackTrace();
            return null;

        }


    }

}
