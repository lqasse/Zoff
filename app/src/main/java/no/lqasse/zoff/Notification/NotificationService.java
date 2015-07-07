package no.lqasse.zoff.Notification;

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

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.Zoff;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.ZoffController;

/**
 * Created by lassedrevland on 04.02.15.
 */
public class NotificationService extends Service {
    private static final String LOG_IDENTIFIER = "NotificationService";
    public static final String INTENT_KEY_SKIP = "SKIP";
    public static final String INTENT_KEY_CLOSE = "CLOSE";
    public static final String INTENT_KEY_OPEN_REMOTE = "OPEN";
    public static final String INTENT_KEY_START = "START";
    public static final String INTENT_KEY_CHAN_NAME = "CHAN_NAME";
    private String TAG = "MediaSessionTAG1227";

    private ZoffController zoffController;
    private Zoff zoff;
    private String channel = "";
    private MediaSession mediaSession;
    private String currentToast = "";
    private boolean shouldBeVisible = false;


    public static void start(Context context, String channel){
        Intent notificationIntent = new Intent(context, NotificationService.class);
        notificationIntent.putExtra(ZoffController.BUNDLEKEY_CHANNEL, channel);
        notificationIntent.setAction("START");
        context.startService(notificationIntent);
    }

    public static void stop(Context context){
        Intent notificationIntent = new Intent(context, NotificationService.class);
        notificationIntent.setAction(NotificationService.INTENT_KEY_CLOSE);
        context.startService(notificationIntent);
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
                closeNotification();
                break;
            case INTENT_KEY_SKIP:
                skipCurrentVideo();
                break;
            case INTENT_KEY_OPEN_REMOTE:
                openRemoteActivity();
                break;
            case INTENT_KEY_START:
                if (b.containsKey(ZoffController.BUNDLEKEY_CHANNEL)) {
                    startService(b.getString(ZoffController.BUNDLEKEY_CHANNEL));
                } else {
                    stopSelf();
                }
                break;
        }
        return START_STICKY;
    }

    private void skipCurrentVideo(){
        log("skip");
        if (zoffController == null){
            zoffController = new ZoffController(channel);
        }

        zoffController.skip();
    }



    private void startService(String channel){

        shouldBeVisible = true;
        this.channel = channel;
        zoffController = ZoffController.getInstance(channel);
        ImageCache.removeImage(zoffController.getZoff().getPlayingVideo().getId(), ImageCache.ImageSize.HUGE);

        showNotification();
        zoffController.setOnRefreshListener(new ZoffController.RefreshCallback() {
            @Override
            public void onZoffRefreshed(Zoff zoff) {
                if (shouldBeVisible){
                    showNotification();
                }

            }
        });

        zoffController.setToastMessageCallback(new ZoffController.ToastMessageCallback() {
            @Override
            public void showToast(String toastkeyword) {
                if (toastkeyword.contains("more are needed to skip")){
                    currentToast = toastkeyword;
                    showNotification();
                } else if (toastkeyword.equals("alreadyskip")){
                    currentToast = "You've already voted to skip";
                    showNotification();
                }
            }

            @Override
            public void showToast(ToastMaster.TYPE type, String contextual) {

            }
        });
    }

    private void openRemoteActivity(){
        closeNotification();
        Intent startRemoteIntent = new Intent(this, RemoteActivity.class);
        startRemoteIntent.putExtra(zoffController.BUNDLEKEY_CHANNEL, channel);
        startRemoteIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startRemoteIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startRemoteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startRemoteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(startRemoteIntent);
        stopSelf();
    }


    private void showNotification() {
        log("Showing notification");
        RemoteViews view = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        RemoteViews bigView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.notification_big);
        String currentlyPlayingVideoID = zoffController.getZoff().getPlayingVideo().getId();
        String nextVideoID = zoffController.getZoff().getNextVideo().getId();

        if (ImageCache.has(currentlyPlayingVideoID)){
            view.setImageViewBitmap(R.id.playlistHeaderImage, ImageCache.get(currentlyPlayingVideoID));
            bigView.setImageViewBitmap(R.id.playlistHeaderImage, ImageCache.get(currentlyPlayingVideoID));

        } else {
            BitmapDownloader.download(nextVideoID, ImageCache.ImageSize.REG, true, new BitmapDownloader.Callback() {
                @Override
                public void onImageDownloaded(Bitmap image, ImageCache.ImageSize type) {
                    if (shouldBeVisible){
                        showNotification();
                    }
                }
            });
        }

        if (nextVideoID != null){

            BitmapDownloader.download(nextVideoID, ImageCache.ImageSize.REG, true, null);

        }

        Zoff zoffmodel = zoffController.getZoff();


        view.setTextViewText(R.id.notification_title, zoffmodel.getPlayingVideo().getTitle());
        view.setTextViewText(R.id.notification_viewers, zoffmodel.getCurrentViewers());
        view.setTextViewText(R.id.notification_toast, currentToast);
        bigView.setTextViewText(R.id.notification_title, zoffmodel.getPlayingVideo().getTitle());
        bigView.setTextViewText(R.id.notification_viewers, zoffmodel.getCurrentViewers());
        bigView.setTextViewText(R.id.channelTextView, zoffmodel.getChannelRaisedFirstLetter());
        bigView.setTextViewText(R.id.notification_toast, currentToast);
        currentToast = "";

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
        closeNotification();
        releaseMediaSession();

        super.onDestroy();
    }

    private void closeNotification(){
        shouldBeVisible = false;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        stopSelf();
        log("Notification cleared");
    }


    private void startMediaSession(){
        if (Build.VERSION.SDK_INT >= 21){
            if (mediaSession == null){
                mediaSession = new MediaSession(getBaseContext(), TAG);
                mediaSession.setCallback(new MediaSession.Callback() {

                    @Override
                    public void onSkipToNext() {
                        zoffController.skip();
                        super.onSkipToNext();
                    }


                });

            }



            MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

            Video currentlyPlayingVideo = zoffController.getZoff().getPlayingVideo();


            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, currentlyPlayingVideo.getTitle());
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, zoffController.getZoff().getChannel());


            if (ImageCache.has(currentlyPlayingVideo.getId(), ImageCache.ImageSize.HUGE)){
                metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, ImageCache.get(currentlyPlayingVideo.getId(), ImageCache.ImageSize.HUGE));

            } else{

                BitmapDownloader.download(currentlyPlayingVideo.getId(), ImageCache.ImageSize.HUGE, false, new BitmapDownloader.Callback() {
                    @Override
                    public void onImageDownloaded(Bitmap image, ImageCache.ImageSize type) {
                        if (shouldBeVisible) {
                            showNotification();
                        }

                    }
                });





            }

            Video nextVideo = zoffController.getZoff().getNextVideo();


            if (!ImageCache.has(nextVideo.getId(), ImageCache.ImageSize.HUGE)){
                BitmapDownloader.download(nextVideo.getId(), ImageCache.ImageSize.HUGE, false, null);

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

    @Override
    public String toString() {
        return "NotificationService";
    }

    private void log(String data){
        Log.i(LOG_IDENTIFIER, data);
    }
}
