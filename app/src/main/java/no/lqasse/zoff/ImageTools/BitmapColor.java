package no.lqasse.zoff.ImageTools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 25.06.15.
 */
public class BitmapColor {


    public static BitmapDrawable darkenBitmap(Bitmap bitmap){
        String darkenColor = "#50000000";

        Bitmap coloredBitmap  = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(coloredBitmap);
        int color = Color.parseColor(darkenColor);
        canvas.drawColor(color, PorterDuff.Mode.DARKEN);


        return new BitmapDrawable(coloredBitmap);

    }


    public static Bitmap lightenBitmap(Bitmap bitmap){
        String lightenColor = "#20FFFFFF";

        Bitmap coloredBitmap  = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(coloredBitmap);
        int color = Color.parseColor(lightenColor);
        canvas.drawColor(color, PorterDuff.Mode.LIGHTEN);
        return coloredBitmap;
    }
}
