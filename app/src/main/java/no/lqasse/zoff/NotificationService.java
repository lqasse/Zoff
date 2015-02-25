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

import no.lqasse.zoff.Datatypes.Zoff;
import no.lqasse.zoff.Remote.RemoteActivity;

/**
 * Created by lassedrevland on 04.02.15.
 */
public class NotificationService extends Service {
    private String ROOM_NAME = "";
    private Zoff zoff;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle b = intent.getExtras();
        ROOM_NAME = b.getString("ROOM_NAME");

        zoff = new Zoff(ROOM_NAME,this);
        return super.onStartCommand(intent, flags, startId);


    }

    public void zoffRefreshed(){
        showNotification();

    }

    private void showNotification() {

        Intent intent = new Intent(this, RemoteActivity.class);
        Bundle b = new Bundle();
        b.putString("ROOM_NAME", zoff.getROOM_NAME());
        intent.putExtras(b);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(zoff.getROOM_NAME())
                .setContentText(zoff.getNowPlayingTitle())
                .setOngoing(true)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent);


        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);


    }

    @Override
    public void onDestroy() {
        Log.d("Notification Service", "Stopped");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        super.onDestroy();
    }


}
