package no.lqasse.zoff.Helpers;

import android.graphics.Bitmap;

import java.util.HashMap;

import no.lqasse.zoff.Zoff;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class ImageCache {
    private static HashMap<String,Bitmap> ImageMap = new HashMap<>();

    public static boolean has(String id){
        return ImageMap.containsKey(id);
    }
    public static void put(String id,Bitmap image){
        ImageMap.put(id,image);
    }

    public static Bitmap get(String id){
        return ImageMap.get(id);
    }





}
