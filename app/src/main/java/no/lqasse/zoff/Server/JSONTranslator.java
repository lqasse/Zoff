package no.lqasse.zoff.Server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import no.lqasse.zoff.Models.VideoChangeMessage;
import no.lqasse.zoff.Models.VoteMessage;
import no.lqasse.zoff.Models.Settings;
import no.lqasse.zoff.Models.Video;

/**
 * Created by lassedrevland on 16.04.15.
 */
public  class JSONTranslator {

    private static final int JSON_SETTINGS_INDEX_OFFSET = -1;
    private static final int JSON_SETTINGS_INDEX = 0;


    public static VideoChangeMessage getVideoChangeMessage(JSONArray data){
        VideoChangeMessage message = new VideoChangeMessage();
        try {
            message.videoId = data.getString(0);
        }catch ( JSONException e){
            e.printStackTrace();
        }
        message.timeChanged = getTimeAdded(data);

        return message;

    }

    public static int getTimeAdded(JSONArray data){

        try {
            return data.getInt(1);
        }catch (JSONException e){
            e.printStackTrace();
        }

        return 0;
    }
    public static String getContentID(JSONArray data){
        try {
            return data.getString(1);
        } catch (JSONException e){
            e.printStackTrace();
            return "";
        }

    }

    public static VoteMessage getVoteMessage(JSONArray data){

        VoteMessage message = new VoteMessage();
        try {
            message.added = data.getInt(2);
            message.videoid = data.getString(1);
        }catch ( JSONException e){
            e.printStackTrace();
        }

        return message;

    }

    public static Settings createSettingsFromJSON(JSONArray data){
        JSONObject object;
        try {
            object = data.getJSONObject(JSON_SETTINGS_INDEX);
            return new Settings.Builder()
                    ._id(object.getString("_id"))
                    .numberOfSkips(object.getJSONArray("skips").length())
                    .numberOfViewers(object.getJSONArray("views").length())
                    .startTimeSeconds(object.getInt("startTime"))
                    .allowsAddsongs(object.getBoolean(Settings.KEY_ADD_SONGS))
                    .allvideos(object.getBoolean(Settings.KEY_ALL_VIDEOS))
                    .longsongs(object.getBoolean(Settings.KEY_LONG_SONGS))
                    .frontpage(object.getBoolean(Settings.KEY_FRONTPAGE))
                    .removeplay(object.getBoolean(Settings.KEY_REMOVE_PLAY))
                    .shuffle(object.getBoolean(Settings.KEY_SHUFFLE))
                    .skip(object.getBoolean(Settings.KEY_SKIP))
                    .vote(object.getBoolean(Settings.KEY_VOTE))
                    .build();


        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Video> createVideoListFromJSON(JSONArray array){
        ArrayList<Video> videos = new ArrayList<>();

        try {

            JSONArray videosJSONArray = array.getJSONArray(1);
            for (int i = 0;i<videosJSONArray.length();i++){
                Video current = createVideoFromJSON(videosJSONArray.getJSONObject(i));
                    if (current.isNowPlaying()){
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

    public static Video createVideoFromJSON(JSONArray array){
        try {
            JSONObject o = array.getJSONObject(1);
            return createVideoFromJSON(o);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Video createVideoFromJSON(JSONObject object){
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




            return new Video.Builder()
                    ._id(_id)
                    .id(id)
                    .title(title)
                    .votesCount(votes)
                    .durationSecs(duration)
                    .addedMillis(added)
                    .isNowPlaying(now_playing)
                    .build();


        } catch (JSONException e){
            e.printStackTrace();
            return new Video();
        }


    }




}
