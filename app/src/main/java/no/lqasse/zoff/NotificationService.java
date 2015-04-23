package no.lqasse.zoff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.ImageDownload;
import no.lqasse.zoff.Interfaces.ImageListener;
import no.lqasse.zoff.Interfaces.ZoffListener;
import no.lqasse.zoff.Models.Zoff;

/**
 * Created by lassedrevland on 04.02.15.
 */
public class NotificationService extends Service implements ZoffListener,ImageListener {
    private String ROOM_NAME = "";

    private String TAG = "MediaSessionTAG1227";
    private static final String LOG_IDENTIFIER = "NotificationService";
    public static final String INTENT_KEY_SKIP = "SKIP";
    public static final String INTENT_KEY_CLOSE = "CLOSE";
    public static final String INTENT_KEY_OPEN_REMOTE = "OPEN";
    public static final String INTENT_KEY_START = "START";
    public static final String INTENT_KEY_CHAN_NAME = "CHAN_NAME";

    private Zoff zoff;

    private MediaSession mediaSession;

    private void log(String data){
        Log.i(LOG_IDENTIFIER, data);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle b = intent.getExtras();


        log("onStartCommand");


        switch (intent.getAction()){
            case INTENT_KEY_CLOSE:
                log("Closing");
                if (zoff!=null){
                    zoff.disconnect();
                    zoff = null;
                }

                clearNotification();

                stopSelf();
                break;
            case INTENT_KEY_SKIP:
                log("skip");
                if (zoff == null){
                    zoff = new Zoff(ROOM_NAME,this);
                }
                zoff.skip();
                break;
            case INTENT_KEY_OPEN_REMOTE:
                if (zoff != null){
                    zoff.disconnect();
                }

                zoff = null;

                intent = new Intent(this, RemoteActivity.class);
                intent.putExtra("ROOM_NAME",ROOM_NAME);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                stopSelf();



                break;
            case INTENT_KEY_START:
                if (b.containsKey("ROOM_NAME")) {
                    log("Starting");
                    ROOM_NAME = b.getString("ROOM_NAME");
                    zoff = new Zoff(ROOM_NAME, this);
                } else {
                    stopSelf();
                }
                break;

        }



        return START_STICKY;



    }



    //communication FROM Zof instance START
    public void onZoffRefreshed() {
        showNotification();
    }

    @Override
    public void onViewersChanged() {
        showNotification();

    }

    @Override
    public void onCorrectPassword() {

    }

    //Communication FROM Zoff instance END

    private void showNotification() {
        log("Showing notification");

        RemoteViews view = new RemoteViews(getApplicationContext().getPackageName(),R.layout.notification);
        RemoteViews bigView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.notification_big);


        if (ImageCache.has(zoff.getNowPlayingID())){
            view.setImageViewBitmap(R.id.imageView, ImageCache.get(zoff.getNowPlayingID()));
            bigView.setImageViewBitmap(R.id.imageView, ImageCache.get(zoff.getNowPlayingID()));

        } else{
            ImageDownload.downloadToCache(zoff.getNowPlayingID());
            ImageCache.registerImageListener(this,zoff.getNowPlayingID());
        }

        view.setTextViewText(R.id.titleTextView, zoff.getNowPlayingTitle());
        view.setTextViewText(R.id.viewersTextView, zoff.getViewersCount());
        bigView.setTextViewText(R.id.titleTextView, zoff.getNowPlayingTitle());
        bigView.setTextViewText(R.id.viewersTextView, zoff.getViewersCount());
        bigView.setTextViewText(R.id.channelTextView, zoff.getChannelName());

        //Closes notification
        Intent stopIntent = new Intent(this,NotificationService.class);
        stopIntent.putExtra(INTENT_KEY_CLOSE,true);
        stopIntent.setAction(INTENT_KEY_CLOSE);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0 , stopIntent,0);


        view.setOnClickPendingIntent(R.id.closeImageView, pendingStopIntent);
        bigView.setOnClickPendingIntent(R.id.closeImageView, pendingStopIntent);

        //Invokes skip
        Intent skipIntent = new Intent(this,NotificationService.class);
        skipIntent.putExtra(INTENT_KEY_SKIP,true);
        skipIntent.setAction(INTENT_KEY_SKIP);
        PendingIntent pendingSkipIntent = PendingIntent.getService(this, 0 , skipIntent,0);


        view.setOnClickPendingIntent(R.id.skipImageView, pendingSkipIntent);
        bigView.setOnClickPendingIntent(R.id.skipImageView, pendingSkipIntent);





        Intent intent = new Intent(this, NotificationService.class);
        intent.setAction(INTENT_KEY_OPEN_REMOTE);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(pendingIntent)
                .setContent(view)
                .setPriority(Notification.PRIORITY_MAX);



        Notification notification = builder.build();

        notification.bigContentView = bigView;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

        startMediaSession();




    }

    @Override
    public void onDestroy() {
        if (zoff!=null){
            zoff.disconnect();
        }
        clearNotification();
        releaseMediaSession();

        super.onDestroy();
    }

    private void clearNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        log("Notification cleared");
    }

    @Override
    public void imageInCache(Bitmap bitmap) {
        showNotification();
        log("Image in cache");

    }

    private void startMediaSession(){
        if (Build.VERSION.SDK_INT >= 21){
            if (mediaSession == null){
                mediaSession = new MediaSession(getBaseContext(), TAG);
                mediaSession.setCallback(new MediaSession.Callback() {

                    @Override
                    public void onSkipToNext() {
                        zoff.skip();
                        super.onSkipToNext();
                    }


                });

            }

            MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();


            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, zoff.getNowPlayingTitle());
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST,zoff.getChannelName());


            if (ImageCache.has(zoff.getNowPlayingID(), ImageCache.ImageType.HUGE)){
                metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, ImageCache.get(zoff.getNowPlayingID(), ImageCache.ImageType.HUGE));

            } else{
                ImageDownload.downloadToCache(zoff.getNowPlayingID(), ImageCache.ImageType.HUGE);
                ImageCache.registerImageListener(this, zoff.getNowPlayingID(), ImageCache.ImageType.HUGE);


            }

            if (!ImageCache.has(zoff.getNextId(), ImageCache.ImageType.HUGE)){
                ImageDownload.downloadToCache(zoff.getNextId(), ImageCache.ImageType.HUGE);
            }

            mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS|MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            mediaSession.setMetadata(metadataBuilder.build());

            PlaybackState.Builder stateBuilder = new PlaybackState.Builder();
            stateBuilder.setActions(PlaybackState.ACTION_SKIP_TO_NEXT);



            //stateBuilder.addCustomAction(actionBuilder.build());
            stateBuilder.setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f);
            mediaSession.setPlaybackState(stateBuilder.build());


            mediaSession.setActive(true);




        }
    }


    private void releaseMediaSession(){
        if (Build.VERSION.SDK_INT >= 21) {
            if (mediaSession != null) {
                mediaSession.release();
            }
        }
    }
}
