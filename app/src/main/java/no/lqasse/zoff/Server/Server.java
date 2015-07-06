package no.lqasse.zoff.Server;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import no.lqasse.zoff.ChannelChooserActivity;
import no.lqasse.zoff.Models.Channel;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.ZoffSettings;

/**
 * Created by lassedrevland on 15.04.15.
 */
public class Server {

    private static final int PING_TIMEOUT_DELAY = (int) TimeUnit.SECONDS.toMillis(3);
    private static final String ZOFF_URL = "https://zoff.no:3000";
    private static final String LOG_IDENTIFIER = "Server";

    private static final String SOCKET_KEY_EMIT_FRONTPAGE_LIST = "frontpage_lists";
    private static final String SOCKET_KEY_ON_FRONTPAGE_LIST = "playlists";

    private static final String SOCKET_KEY_EMIT_SKIP = "skip";
    private static final String SOCKET_KEY_EMIT_SETTINGS = "conf";
    private static final String SOCKET_KEY_EMIT_PASSWORD = "password";
    private static final String SOCKET_KEY_EMIT_VOTE = "vote";
    private static final String SOCKET_KEY_EMIT_SHUFFLE = "shuffle";
    private static final String SOCKET_KEY_EMIT_GET_LIST = "list";

    private static final String SOCKET_KEY_ON_SKIPPED = "skipping";
    private static final String SOCKET_KEY_ON_CHANNEL_REFRESH = "channel";
    private static final String SOCKET_KEY_ON_VIEWCOUNT_CHANGED = "viewers";
    private static final String SOCKET_KEY_ON_TOAST = "toast";
    private static final String SOCKET_KEY_ON_CORRECT_PASSWORD = "pw";
    private static final String SOCKET_KEY_ON_PING_CALLBACK = "ok";
    private static final String SOCKET_KEY_ON_SETTINGS_SAVED = "savedsettings";
    private static final String SOCKET_KEY_ON_NOW_PLAYING_CHANGED = "np";
    private static final String SOCKET_KEY_ON_CONFIGURATION_CHANGED = "conf";

    private static final String CHANNEL_REFRESH_VOTE_ADDED = "vote";
    private static final String CHANNEL_REFRESH_VIDEO_DELETED = "deleted";
    private static final String CHANNEL_REFRESH = "list";
    private static final String CHANNEL_REFRESH_NOW_PLAYING_CHANGED = "song_change";
    private static final String CHANNEL_REFRESH_VIDEO_ADDED = "added";

    Socket socket;
    Server.Listener listener;
    String channel;
    Handler handler;
    private boolean pinging = false;
    Runnable pingTimer = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(onPingTimeout, TimeUnit.SECONDS.toMillis(3));
        }
    };
    private Emitter.Listener onChannelRefresh = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    JSONArray array;
                    if (args.length > 0) {

                        try {
                            array = (JSONArray) args[0];
                            log("onChannelRefresh: " + array.getString(0));
                            switch (array.getString(0)) {
                                case CHANNEL_REFRESH:
                                    listener.onListRefreshed(array);
                                    break;
                                case CHANNEL_REFRESH_VOTE_ADDED:
                                    listener.onVideoGotVote(array);
                                    break;
                                case CHANNEL_REFRESH_VIDEO_DELETED:
                                    listener.onVideoDeleted(array);
                                    break;
                                case CHANNEL_REFRESH_NOW_PLAYING_CHANGED:
                                    listener.onVideoChanged(array);
                                    break;
                                case CHANNEL_REFRESH_VIDEO_ADDED:
                                    listener.onVideoAdded(array);
                                    break;

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }


                    connectionOK();


                }
            });


        }
    };


    private Emitter.Listener onConf = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    log("onConf");


                    JSONArray conf = (JSONArray) args[0];

                    listener.onConfigurationChanged(conf);


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
                    log("onNewVideo");


                }
            });
        }
    };
    private Emitter.Listener onSkip = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            log("onSkip");
        }
    };
    private Emitter.Listener onToast = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    String toast = (String) args[0];
                    connectionOK();

                    log("onToast: " + toast);
                    listener.onToast(toast);


                }
            });
        }
    };
    private Emitter.Listener onViewCountChanged = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {


                    int viewers = (int) args[0];

                    listener.onViewersChanged(viewers);


                    log("onViewsChanged: " + viewers);
                    connectionOK();


                }
            });
        }
    };
    private Emitter.Listener onPw = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    String data = args[0].toString();
                    log("onPw: " + data);
                    listener.onCorrectPassword(data);
                    connectionOK();


                }
            });
        }
    };
    private Emitter.Listener onSavedSettings = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {


                    String data = args[0].toString();
                    log("Settings saved" + data);


                }
            });
        }
    };

    public Server(String channel, Listener listener) {
        this.listener = listener;
        this.channel = channel;
        handler = new Handler(Looper.getMainLooper());
        connect();
    }



    public static void getChannelSuggestions(final ChannelChooserActivity main, final SuggestionsCallback suggestionsCallback) {
        try {
            IO.Options options = new IO.Options();

            options.secure = true;


            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            }, null);

            options.sslContext = ctx;
            final Socket tempSocket = IO.socket(ZOFF_URL, options);


            tempSocket.connect();
            tempSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("CONNECT", "OK");
                }
            });

            tempSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("ERROR", args[0].toString());
                }
            });


            tempSocket.emit(SOCKET_KEY_EMIT_FRONTPAGE_LIST);
            tempSocket.on(SOCKET_KEY_ON_FRONTPAGE_LIST, new Emitter.Listener() {
                @Override
                public void call(final Object... args) {


                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ArrayList<Channel> suggestions = new ArrayList<>();
                                JSONArray array = (JSONArray) args[0];

                                for (int i = 0; i < array.length(); i++) {

                                    JSONArray item = array.getJSONArray(i);
                                    // [numberOfViewers,nowPlayingId,nowPLayingTitle,chanName,noOfSongs]
                                    //[0,"6Cp6mKbRTQY","Avicii - Hey Brother","lqasse",4]
                                    suggestions.add(new Channel(item.getInt(0), item.getString(1), item.getString(2), item.getString(3), item.getInt(4)));

                                }

                                suggestionsCallback.onResponse(suggestions);
                                tempSocket.off(SOCKET_KEY_ON_FRONTPAGE_LIST);
                                tempSocket.disconnect();


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("SocketServer", "Suggestions failed");
                            }
                        }
                    });

                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void connect() {

        try {
            IO.Options options = new IO.Options();
            options.secure = true;
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            }, null);
            options.sslContext = ctx;
            options.forceNew = true;
            socket = IO.socket(ZOFF_URL, options);

        } catch (Exception e) {
            e.printStackTrace();
            log("Failed to connect");
        }
        socket.connect();
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                log("Connected");
            }
        });
        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                log("Failed to connect, " + args[0].toString());
            }
        });

        socket = setSocketListeners(socket);
    }

    public void ping() {
        socket.emit("ping");
        handler.postDelayed(onPingTimeout,PING_TIMEOUT_DELAY);
        log("ping...");

        handler.postDelayed(pingTimer, TimeUnit.SECONDS.toMillis(1));
    }

    private Emitter.Listener onPingResponse = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            handler.post(new Runnable() {

                @Override
                public void run() {

                    handler.removeCallbacks(onPingTimeout);
                    log("ping OK");


                }
            });
        }
    };
    Runnable onPingTimeout = new Runnable() {
        @Override
        public void run() {
            log("Trying to connect...");
            connect();
        }
    };


    public void shuffle(String adminpass) {
        socket.emit(SOCKET_KEY_EMIT_SHUFFLE, adminpass);
    }

    public void vote(Video video, String adminpass) {

        //vote, [channel, id, vote, guid, adminpass]:


        JSONArray jsonmessage = new JSONArray();

        jsonmessage.put(channel);
        jsonmessage.put(video.getId());
        jsonmessage.put("pos");
        jsonmessage.put(adminpass);


        log("vote, " + jsonmessage.toString());


        socket.emit(SOCKET_KEY_EMIT_VOTE, jsonmessage);


    }

    public void delete(Video video, String adminpass) {

        //vote, [channel, id, vote, guid, adminpass]:


        JSONArray jsonmessage = new JSONArray();

        jsonmessage.put(channel);
        jsonmessage.put(video.getId());
        jsonmessage.put("del");
        jsonmessage.put(adminpass);


        log("vote, " + jsonmessage.toString());

        socket.emit(SOCKET_KEY_EMIT_VOTE, jsonmessage);


    }

    public void add(String id, String title, String adminpass, String duration) {

        JSONArray jsonmessage = new JSONArray();
        jsonmessage.put(id);
        jsonmessage.put(title);
        jsonmessage.put(adminpass);
        jsonmessage.put(duration);

        log("Connection status: " + Boolean.toString(socket.connected()));
        log("add" + jsonmessage.toString());

        socket.emit("add", jsonmessage);

    }

    public void skip(String adminpass) {

        JSONArray jsonmessage = new JSONArray();
        jsonmessage.put(channel);
        jsonmessage.put(adminpass);


        log("Connection status: " + Boolean.toString(socket.connected()));
        log("Skip");

        Ack ack = new Ack() {
            @Override
            public void call(Object... args) {
                Log.d("SocketServer", "ack: " + args[0].toString());
            }
        };
        socket.emit(SOCKET_KEY_EMIT_SKIP, jsonmessage, ack, "lol");


    }

    public void getPlaylist() {
        socket.emit(SOCKET_KEY_EMIT_GET_LIST, channel);
    }

    public void savePassword(String password) {
        JSONArray data = new JSONArray();
        data.put(password);
        data.put(channel);
        socket.emit(SOCKET_KEY_EMIT_PASSWORD, data);
    }

    public void saveSettings(String adminpass, ZoffSettings settings) {

        JSONArray data = new JSONArray();
        data.put(settings.isVote());
        data.put(settings.isAddsongs());
        data.put(settings.isFrontpage());
        data.put(settings.isLongsongs());
        data.put(settings.isAllvideos());
        data.put(settings.isRemoveplay());
        data.put(adminpass);
        data.put(settings.isSkip());
        data.put(settings.isShuffle());

        socket.emit(SOCKET_KEY_EMIT_SETTINGS, data);
        log("Saving settings:" + data.toString());


    }

    private void log(String data) {
        Log.i(LOG_IDENTIFIER + ": ", data);

    }

    public void off() {
        log("disconnect");

        socket = removeSocketListeners(socket);

        socket.disconnect();
        socket.close();

    }

    private void connectionOK() {
        if (pinging = true) {
            //log("connection OK");
            handler.removeCallbacks(pingTimer);
            handler.removeCallbacks(onPingTimeout);

        }

    }

    private Socket setSocketListeners(Socket socket) {
        socket.on(SOCKET_KEY_ON_CHANNEL_REFRESH, onChannelRefresh);
        socket.on(channel + SOCKET_KEY_ON_NOW_PLAYING_CHANGED, onNewVideo);
        socket.on(SOCKET_KEY_ON_SKIPPED, onSkip);
        socket.on(SOCKET_KEY_ON_VIEWCOUNT_CHANGED, onViewCountChanged);
        socket.on(SOCKET_KEY_ON_TOAST, onToast);
        socket.on(SOCKET_KEY_ON_CORRECT_PASSWORD, onPw);
        socket.on(channel + SOCKET_KEY_ON_SETTINGS_SAVED, onSavedSettings);
        socket.on(SOCKET_KEY_ON_PING_CALLBACK, onPingResponse);
        socket.on(SOCKET_KEY_ON_CONFIGURATION_CHANGED, onConf);

        return socket;
    }

    private Socket removeSocketListeners(Socket socket) {
        socket.off(SOCKET_KEY_ON_CHANNEL_REFRESH);
        socket.off(channel + SOCKET_KEY_ON_NOW_PLAYING_CHANGED);
        socket.off(SOCKET_KEY_ON_SKIPPED);
        socket.off(SOCKET_KEY_ON_VIEWCOUNT_CHANGED);
        socket.off(SOCKET_KEY_ON_TOAST);
        socket.off(SOCKET_KEY_ON_CORRECT_PASSWORD);
        socket.off(channel + SOCKET_KEY_ON_SETTINGS_SAVED);
        socket.off(SOCKET_KEY_ON_PING_CALLBACK);
        socket.off(SOCKET_KEY_ON_CONFIGURATION_CHANGED);
        socket.off(Socket.EVENT_CONNECT);
        socket.off(Socket.EVENT_CONNECT_ERROR);


        return socket;
    }

    public interface SuggestionsCallback {
        void onResponse(ArrayList<Channel> response);
    }


    public interface Listener{
        void onCorrectPassword(String data);
        void onViewersChanged(int viewers);
        void onVideoAdded(JSONArray video);
        void onVideoChanged(JSONArray newSong);
        void onVideoDeleted(JSONArray deletedVideo);
        void onListRefreshed(JSONArray list);
        void onConfigurationChanged(JSONArray configuration);
        void onVideoGotVote(JSONArray video);
        void onToast(String toast);




    }


}
