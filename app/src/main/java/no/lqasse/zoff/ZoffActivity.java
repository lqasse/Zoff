package no.lqasse.zoff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import no.lqasse.zoff.ImageTools.BackgroundGenerator;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.Models.ZoffController;

/**
 * Created by lassedrevland on 04.04.15.
 */
public abstract class ZoffActivity extends ActionBarActivity {
    private final String LOG_IDENTIFIER = "ZoffActivity";
    protected String channel;
    protected String ROOM_PASS;
    protected ZoffController zoffController;

    protected Boolean homePressed = true;
    protected Boolean backPressed = false;

    protected  Bitmap currentBackground;

    public ZoffController getZoffController() {
        return this.zoffController;
    }

    protected void startNotificationService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.putExtra(ZoffController.BUNDLEKEY_CHANNEL, channel);
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
                zoffController.skip();
                break;
            case search:

                break;
            case shuffle:
                zoffController.shuffle();

                break;
            case play_here:
                break;
            case settings:

                break;

        }
    }

    public void setBackgroundImage(final String videoId) {


        final ImageView background = (ImageView) findViewById(R.id.backgroundImage);
        final ImageView oldBackground = (ImageView) findViewById(R.id.backgroundImageOLD);

        if (ImageCache.has(videoId, ImageCache.ImageSize.BACKGROUND)){

            Bitmap nextBackgroundImage = ImageCache.get(videoId, ImageCache.ImageSize.BACKGROUND);
            if (currentBackground != nextBackgroundImage) {




                final Bitmap bg = nextBackgroundImage;
                if (currentBackground != null) {

                    fadeInNewBackgroundBitmap(nextBackgroundImage);

                } else {
                    Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
                    fadeIN.setInterpolator(new DecelerateInterpolator());
                    fadeIN.setDuration(1000);
                    background.setImageBitmap(bg);
                    background.setAnimation(fadeIN);
                    fadeIN.start();

                }
                currentBackground = nextBackgroundImage;



            }

        } else if (ImageCache.has(videoId)){

            BackgroundGenerator.generateBackground(ImageCache.get(videoId), videoId, new BackgroundGenerator.Callback() {
                @Override
                public void onBackgroundCreated(Bitmap bitmap) {
                    fadeInNewBackgroundBitmap(bitmap);

                }
            });


        } else {

            BitmapDownloader.download(videoId, ImageCache.ImageSize.REG, true, new BitmapDownloader.Callback() {
                @Override
                public void onImageDownloaded(Bitmap image, ImageCache.ImageSize type) {
                    BackgroundGenerator.generateBackground(image, videoId, new BackgroundGenerator.Callback() {
                        @Override
                        public void onBackgroundCreated(Bitmap bitmap) {
                            fadeInNewBackgroundBitmap(bitmap);
                        }
                    });
                }
            });

        }







    }

    private void fadeInNewBackgroundBitmap(Bitmap nextBackgroundBitmap){

        final ImageView background = (ImageView) findViewById(R.id.backgroundImage);
        final ImageView oldBackground = (ImageView) findViewById(R.id.backgroundImageOLD);

        oldBackground.setImageBitmap(currentBackground);
        Animation fadeIN = new AlphaAnimation(0.00f, 1.00f);
        fadeIN.setInterpolator(new DecelerateInterpolator());
        fadeIN.setDuration(1000);
        background.setVisibility(View.INVISIBLE);
        background.setImageBitmap(nextBackgroundBitmap);
        background.setAnimation(fadeIN);
        fadeIN.start();
        background.setVisibility(View.VISIBLE);

        currentBackground = nextBackgroundBitmap;
    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER, log);

    }



}
