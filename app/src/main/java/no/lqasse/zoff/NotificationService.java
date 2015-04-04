package no.lqasse.zoff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ImageDownload;
import no.lqasse.zoff.Remote.RemoteActivity;

/**
 * Created by lassedrevland on 04.02.15.
 */
public class NotificationService extends Service implements ZoffListener {
    private String ROOM_NAME = "";
    private Zoff zoff;





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
                Log.d("Service", "Closing");
                clearNotification();
                stopSelf();
            } else if (b.containsKey("ROOM_NAME")){
                Log.d("Service","Starting");
                ROOM_NAME = b.getString("ROOM_NAME");
                zoff = new Zoff(ROOM_NAME,this);
            }


        }




        return START_STICKY;



    }



    public void zoffRefreshed(Boolean hasInetAccess) {
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
        view.setTextViewText(R.id.viewersTextView, zoff.getViewers());

        Intent stopIntent = new Intent(this,NotificationService.class);
        stopIntent.putExtra("CLOSE",true);

        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0 , stopIntent,0);

       view.setOnClickPendingIntent(R.id.closeImageView, pendingStopIntent);





        Intent intent = new Intent(this, RemoteActivity.class);
        Bundle b = new Bundle();
        b.putString("ROOM_NAME", zoff.getROOM_NAME());
        intent.putExtras(b);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(pendingIntent)
                .setContent(view);


        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);


    }

    @Override
    public void onDestroy() {

        zoff.stopRefresh();
        clearNotification();
        Log.d("Service", "Destroyed");
        super.onDestroy();
    }

    private void clearNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.d("Service", "Notification cleared");
    }





}
