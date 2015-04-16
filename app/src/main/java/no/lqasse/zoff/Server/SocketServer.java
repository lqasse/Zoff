package no.lqasse.zoff.Server;

import android.app.Activity;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;

import no.lqasse.zoff.MainActivity;
import no.lqasse.zoff.Models.ChanSuggestion;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Zoff;

/**
 * Created by lassedrevland on 15.04.15.
 */
public class SocketServer  {

    private static final String ZOFF_URL = "http://dev.zoff.no:3000";
    Socket socket;
    Zoff zoff;
    Activity context;
    String chan;
    String guid = "1337";

    public  SocketServer(String chan,Zoff zoff, Activity context){
        this.zoff = zoff;
        this.context = context;
        this.chan = chan;
        Log.d("SocketServer", "Started");


        try {
            socket = IO.socket(ZOFF_URL);

        } catch (Exception e){
            e.printStackTrace();
            Log.d("SocketServer", "Failed to connect");
        }
        socket.connect();

        socket.emit("list", chan + "," + guid);

        socket.on(chan              ,onChannelRefresh);
        socket.on(chan+",np"        ,onNewVideo);
        socket.on("skipping"        ,onSkip);
        socket.on(chan+",viewers"   ,onViewersChanged);



    }

    ///////////Socket Listeners\\\\\\\\\\\\\\



    private Emitter.Listener onChannelRefresh = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            context.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.d("SocketServer", "onChannelRefresh");
                    JSONArray array;
                     array = (JSONArray) args[0];
                     zoff.socketRefreshed(array);



                }
            });





        }
    };

    private Emitter.Listener onNewVideo = new Emitter.Listener() {



        @Override
        public void call(Object... args) {
            context.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.d("SocketServer", "onNewVideo");
                }
            });

            }
        };

    private Emitter.Listener onSkip = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("SocketServer", "onSkip");

        }
    };

    private Emitter.Listener onViewersChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            context.runOnUiThread(new Runnable() {

                @Override
                public void run() {


                    int viewers = (int) args[0];
                    zoff.viewersChanged(viewers);


                    Log.d("SocketServer", "onViewesChanged: " + Integer.toString(viewers));
                }
            });
        }
    };


    public void off(){
        socket.off(chan);
        socket.off(chan+",np");
        socket.off("skipping");
        socket.off(chan+",viewers");
        socket.disconnect();
    }

    public static void getSuggestions(final MainActivity main){
        final Socket tempSocket;
        try {
            tempSocket = IO.socket(ZOFF_URL);
        } catch (URISyntaxException e){
            e.printStackTrace();
            return;
        }

        tempSocket.connect();
        tempSocket.emit("frontpage_lists");
        tempSocket.on("playlists",new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ArrayList<ChanSuggestion> suggestions = new ArrayList<>();
                            JSONArray array = (JSONArray) args[0];

                            for (int i = 0;i<array.length();i++){

                                JSONArray item = array.getJSONArray(i);
                                // [viewers,nowPlayingId,nowPLayingTitle,chanName,noOfSongs]
                                //[0,"6Cp6mKbRTQY","Avicii - Hey Brother","lqasse",4]
                                suggestions.add(new ChanSuggestion(item.getInt(0),item.getString(1),item.getString(2),item.getString(3),item.getInt(4)));

                            }

                            main.setSuggestions(suggestions);
                            tempSocket.off("playlists");
                            tempSocket.disconnect();





                        } catch (JSONException e){
                            e.printStackTrace();
                            Log.d("SocketServer", "Suggestions failed");
                        }
                    }
                });

            }
        });


    }

    ////////////Emitters


    public void vote(Video video, String adminpass){

        //vote, [chan, id, vote, guid, adminpass]:


        JSONArray jsonmessage = new JSONArray();

        jsonmessage.put(chan);
        jsonmessage.put(video.getId());
        jsonmessage.put("pos");
        jsonmessage.put(guid);
        jsonmessage.put(adminpass);


        Log.d("SocketServer", "Connected status " + Boolean.toString(socket.connected()));
        Log.d("SocketServer", "vote, " + jsonmessage.toString());


        socket.emit("vote", jsonmessage);



    }

    public void delete(Video video, String adminpass){

        //vote, [chan, id, vote, guid, adminpass]:


        JSONArray jsonmessage = new JSONArray();

        jsonmessage.put(chan);
        jsonmessage.put(video.getId());
        jsonmessage.put("del");
        jsonmessage.put(guid);
        jsonmessage.put(adminpass);


        Log.d("SocketServer", "Connected status " + Boolean.toString(socket.connected()));
        Log.d("SocketServer", "vote, " + jsonmessage.toString());

        socket.emit("vote", jsonmessage);



    }

    public void add(String id,String title, String adminpass, String duration){

        JSONArray jsonmessage = new JSONArray();
        jsonmessage.put(id);
        jsonmessage.put(title);
        jsonmessage.put(adminpass);
        jsonmessage.put(duration);

        Log.d("SocketServer", "Connected status " + Boolean.toString(socket.connected()));
        Log.d("SocketServer", "add" + jsonmessage.toString());
        socket.emit("add", jsonmessage);

    }

    public void skip(String adminpass){

        JSONArray jsonmessage = new JSONArray();
        jsonmessage.put(chan);
        jsonmessage.put(guid);
        jsonmessage.put(adminpass);

        Log.d("SocketServer", "Connected status " + Boolean.toString(socket.connected()));
        Log.d("SocketServer", "Skip");
        socket.emit("skip",jsonmessage);
    }












}
