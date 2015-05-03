package no.lqasse.zoff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Models.Zoff;

/**
 * Created by lassedrevland on 04.04.15.
 */
public abstract class ZoffActivity extends ActionBarActivity {
    private final String LOG_IDENTIFIER = "ZoffActivity";
    protected String ROOM_NAME;
    protected String ROOM_PASS;
    protected Zoff zoff;
    protected Boolean homePressed = true;
    protected Boolean backPressed = false;

    private Bitmap currentBackground;

    public Zoff getZoff() {
        return this.zoff;
    }

    protected void startNotificationService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.putExtra("ROOM_NAME", ROOM_NAME);
        notificationIntent.setAction("START");
        startService(notificationIntent);
    }

    protected void stopNotificationService() {
        log("Killing service");
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.setAction(NotificationService.INTENT_KEY_CLOSE);
        startService(notificationIntent);
    }




    @Override
    public void onBackPressed() {
        homePressed = false;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopNotificationService();
        super.onDestroy();

    }

    protected enum event_type{skip,search,settings,shuffle,play_here}

    protected void handleEvent(event_type type){
        switch (type){
            case skip:
                zoff.skip();
                break;
            case search:

                break;
            case shuffle:
                zoff.shuffle();

                break;
            case play_here:
                break;
            case settings:

                break;

        }
    }

    public void setBackgroundImage(Bitmap bitmap) {
/*


         int averageColor = (bitmap.getPixel(bitmap.getWidth()/2,bitmap.getHeight()/2));
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);

        layout.setBackgroundColor(averageColor);

        */


        if (currentBackground != bitmap) {


            final Bitmap bg = bitmap;
            final ImageView background = (ImageView) findViewById(R.id.backgroundImage);
            final ImageView oldBackground = (ImageView) findViewById(R.id.backgroundImageOLD);

            if (currentBackground != null) {
                oldBackground.setImageBitmap(currentBackground);
                Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
                fadeIN.setInterpolator(new DecelerateInterpolator());
                fadeIN.setDuration(1000);
                background.setVisibility(View.INVISIBLE);
                background.setImageBitmap(bg);
                background.setAnimation(fadeIN);
                fadeIN.start();
                background.setVisibility(View.VISIBLE);

            } else {
                Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
                fadeIN.setInterpolator(new DecelerateInterpolator());
                fadeIN.setDuration(1000);
                background.setImageBitmap(bg);
                background.setAnimation(fadeIN);
                fadeIN.start();

            }
            currentBackground = bitmap;



        }


    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER, log);

    }



}
