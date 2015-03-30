package no.lqasse.zoff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

import java.util.concurrent.TimeUnit;

import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ImageDownload;
import no.lqasse.zoff.Remote.RemoteActivity;

/**
 * Created by lassedrevland on 04.02.15.
 */
public class NotificationService extends Service implements Zoff_Listener {
    private String ROOM_NAME = "";
    private Zoff zoff;
    public static Boolean inBackground = false;
    Handler h = new Handler();


    Runnable inBackgroundChecker = new Runnable() {
        @Override
        public void run() {
            Log.d("Service", "Background check...");


            if (inBackground){
                showNotification();

            } else {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();


            }

            h.postDelayed(this, 5000);
        }
    };


    public static Boolean getInBackground() {
        return inBackground;
    }

    public static void setInBackground(Boolean inBackground) {
        NotificationService.inBackground = inBackground;
        Log.d("Service","inBackground: " + Boolean.toString(inBackground));
    }

    @Override
    public void onCreate() {
       Log.d("Service","Started");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle b = intent.getExtras();


        Log.d("Service","onStartCommand");


        if (b!=null){
            if (b.containsKey("CLOSE")){
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                h.removeCallbacksAndMessages(null);
                stopSelf();
            } else if (b.containsKey("ROOM_NAME")){
                Log.d("Service","Has extras");
                ROOM_NAME = b.getString("ROOM_NAME");
                zoff = new Zoff(ROOM_NAME,this);
                h.removeCallbacksAndMessages(null);
                h.post(inBackgroundChecker);
            } else if (b.containsKey("RESTART")){
                h.post(inBackgroundChecker);
            }


        }




        return START_STICKY;



    }


    @Override
    public void zoffRefreshed(Boolean hasInetAccess) {

    }

    public void zoffRefreshed(){
        showNotification();

    }

    private void showNotification() {

        RemoteViews view = new RemoteViews("no.lqasse.zoff",R.layout.notification);

        if (ImageCache.has(zoff.getNowPlayingID())){
            view.setImageViewBitmap(R.id.imageView, ImageCache.get(zoff.getNowPlayingID()));
        } else{
            ImageDownload.downloadToCache(zoff.getNowPlayingID());
        }

        view.setTextViewText(R.id.titleTextView, zoff.getNowPlayingTitle());
        view.setTextViewText(R.id.viewersTextView, zoff.getVIEWERS_STRING());

        Intent stopIntent = new Intent(this,NotificationService.class);
        stopIntent.putExtra("CLOSE",true);

        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0 , stopIntent,0);

       view.setOnClickPendingIntent(R.id.closeImageView, pendingStopIntent);





        Intent intent = new Intent(this, RemoteActivity.class);
        Bundle b = new Bundle();
        b.putString("ROOM_NAME", zoff.getROOM_NAME());
        intent.putExtras(b);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                //.setContentTitle(zoff.getROOM_NAME())
                //.setContentText(zoff.getNowPlayingTitle())
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(pendingIntent)
                .setContent(view)
                ;


        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);


    }

    @Override
    public void onDestroy() {


        clearNotification();

        super.onDestroy();
    }

    private void clearNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }





}
