package no.lqasse.zoff;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Created by lassedrevland on 04.04.15.
 */
public abstract class ZoffActivity extends ActionBarActivity {
    protected String ROOM_NAME;
    protected String ROOM_PASS;
    protected Zoff zoff;
    protected Boolean homePressed = true;

    public Zoff getZoff() {
        return this.zoff;
    }

    protected void startNotificationService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.putExtra("ROOM_NAME", ROOM_NAME);
        startService(notificationIntent);
    }

    protected void stopNotificationService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        stopService(notificationIntent);
    }

    @Override
    protected void onResume() {
        stopNotificationService();
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        homePressed = false;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (zoff != null){
            zoff.stopRefresh();
        }
        stopNotificationService();
        super.onDestroy();

    }
}
