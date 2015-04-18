package no.lqasse.zoff.Server;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.MainActivity;
import no.lqasse.zoff.Models.ChanSuggestion;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Zoff;

/**
 * Created by lassedrevland on 15.04.15.
 */
public class SocketServer  {

    private static final String ZOFF_URL = "http://dev.zoff.no:3000";
    private static String LOG_IDENTIFIER = "SocketServer";
    Socket socket;
    Zoff zoff;
    Activity context;
    String chan;
    String guid = "1337";
    Handler handler;





    public  SocketServer(String chan,Zoff zoff, String android_id){


        this.zoff = zoff;
        this.chan = chan;
        this.guid = android_id;
        handler = new Handler(Looper.getMainLooper());
        log("Started on: " + chan + " ID: " + guid);


        try {
            socket = IO.socket(ZOFF_URL);

        } catch (Exception e){
            e.printStackTrace();
            log("Failed to connect");
        }
        socket.connect();

        socket.emit("list", chan + "," + guid);

        socket.on(chan              ,onChannelRefresh);
        socket.on(chan+",np"        ,onNewVideo);
        socket.on("skipping"        ,onSkip);
        socket.on(chan+",viewers"   ,onViewersChanged);
        socket.on("toast"           ,onToast);


    }

    ///////////Socket Listeners\\\\\\\\\\\\\\



    private Emitter.Listener onChannelRefresh = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {




            handler.post(new Runnable() {

                @Override
                public void run() {
                    log( "onChannelRefresh");
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
            handler.post(new Runnable() {

                @Override
                public void run() {
                    log( "onNewVideo");
                }
            });

            }
        };

    private Emitter.Listener onSkip = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            log( "onSkip");

        }
    };

    private Emitter.Listener onToast = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {


                    String toast = (String) args[0];
                    zoff.showToast(toast);



                    log("onToast: " + toast);
                }
            });
        }
    };

    private Emitter.Listener onViewersChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {


                    int viewers = (int) args[0];
                    zoff.viewersChanged(viewers);


                    log("onViewesChanged: " + viewers);


                }
            });
        }
    };


    public void off()  {
        log("disconnect");
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


        log("vote, " + jsonmessage.toString());


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


        log( "vote, " + jsonmessage.toString());

        socket.emit("vote", jsonmessage);



    }

    public void add(String id,String title, String adminpass, String duration){

        JSONArray jsonmessage = new JSONArray();
        jsonmessage.put(id);
        jsonmessage.put(title);
        jsonmessage.put(adminpass);
        jsonmessage.put(duration);

        log("Connection status: " + Boolean.toString(socket.connected()));
        log("add" + jsonmessage.toString());

        socket.emit("add", jsonmessage);

    }

    public void skip(String adminpass){

        JSONArray jsonmessage = new JSONArray();
        jsonmessage.put(chan);
        jsonmessage.put(guid);
        jsonmessage.put(adminpass);


        log("Connection status: " + Boolean.toString(socket.connected()));
        log("Skip");

        Ack ack = new Ack() {
            @Override
            public void call(Object... args) {
                Log.d("SocketServer", "ack: " + args[0].toString());
            }
        };
        socket.emit("skip",jsonmessage,ack,"lol");




    }

    private void log(String data){
        Log.i(LOG_IDENTIFIER, data);

    }














}
