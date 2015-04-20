package no.lqasse.zoff.ImageTools;

import android.graphics.Bitmap;

/**
 * Created by lassedrevland on 20.04.15.
 */
public class ImageScaler {


    private static final float IMAGE_RATIO = (16f/9f);
    private static  float AGGRESSIVE_DOWNSCALE_RATIO = 1.0f;
    private static int TARGET_IMAGE_WIDTH_BIG   = 640;
    private static int TARGET_IMAGE_HEIGHT_BIG  = (int)( TARGET_IMAGE_WIDTH_BIG / IMAGE_RATIO);
    private static int TARGET_IMAGE_WIDTH       = 200;
    private static int TARGET_IMAGE_HEIGHT      = (int)( TARGET_IMAGE_WIDTH / IMAGE_RATIO);

    public static Bitmap Scale(Bitmap bitmap, ImageCache.ImageType type){

        if (bitmap.getWidth() >= 640){

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (TARGET_IMAGE_WIDTH_BIG*AGGRESSIVE_DOWNSCALE_RATIO), (int) (TARGET_IMAGE_HEIGHT_BIG*AGGRESSIVE_DOWNSCALE_RATIO),true);

        } else if (type != ImageCache.ImageType.HUGE){ //Dont scale if Image should be huge but is small (Fallback if big image doesnt exist)

            bitmap = Bitmap.createScaledBitmap(bitmap, (int)(TARGET_IMAGE_WIDTH*AGGRESSIVE_DOWNSCALE_RATIO), (int) (TARGET_IMAGE_HEIGHT*AGGRESSIVE_DOWNSCALE_RATIO),true);
        }

        return bitmap;
    }

    public static void setAggressiveScaling(Float scaleRatio){
        AGGRESSIVE_DOWNSCALE_RATIO = scaleRatio;

    }
}
