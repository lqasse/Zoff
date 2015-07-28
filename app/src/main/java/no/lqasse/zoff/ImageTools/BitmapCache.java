package no.lqasse.zoff.ImageTools;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by lassedrevland on 23.03.15.
 */
public class BitmapCache {
    private static final String LOG_IDENTIFIER = "ImageCache";
    private static final String HUGE_APPENDIX = "_huge";
    private static final String BLUR_APPENDIX = "_blur";

    private static final int LOWER_MEMORY_THRESHOLD = 10 * 1024; //10mb
    final static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final static int cacheSize = maxMemory / 8;

    private static Map<String,ImageInCacheListener> cacheListeners = new HashMap<>();



    private static ArrayList<String> hugeImagesInCache = new ArrayList<>();
    private static LruCache<String, Bitmap> mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() /1024;
        }

    };

    public static void removeImage(String id, ImageSize size){
        mMemoryCache.remove(getIDWithTypeSuffix(id,size));
    }

    public static void empty(){
        log();
        if (mMemoryCache != null){
            mMemoryCache.evictAll();
        }
        log("Emptied:" + Integer.toString(mMemoryCache.evictionCount()));
    }



    public enum ImageSize {BACKGROUND,HUGE,REG}

    private static Bitmap currentBlurBG;





    public static boolean has(String id){
        return mMemoryCache.get(id) != null;
    }

    public static boolean has(String id,ImageSize type){


        return mMemoryCache.get(id + getSuffix(type)) != null;




    }

    public static Bitmap get(String id,ImageSize type){


        if (has(id,type)){
            return mMemoryCache.get(id + getSuffix(type));
        } else{
            return null;
        }




    }

    public static void put(String id, ImageSize type, Bitmap bitmap, Boolean flagScaleDown){
        Boolean aggressive = false;
        Boolean scaled = false;
        Boolean hadLister = false;



        if (cacheSize < LOWER_MEMORY_THRESHOLD){

            BitmapScaler.setAggressiveScaling(0.75f);
            aggressive = true;
        }



        switch (type){

            case BACKGROUND:

                if (currentBlurBG!=bitmap){
                    currentBlurBG = bitmap;
                }
                break;
            case HUGE:
                hugeImagesInCache.add(id);
                break;
        }

        if (flagScaleDown){
            bitmap = BitmapScaler.Scale(bitmap, type);
            scaled = true;
        }



        mMemoryCache.put(id + getSuffix(type),bitmap);

        if (hasListener(id,type)){
            cacheListeners.get(getIDWithTypeSuffix(id,type)).ImageInCache(bitmap);
            hadLister = true;
        }

        int FILL_PERCENTAGE =  (int) (((float)mMemoryCache.size()/(float)mMemoryCache.maxSize()*100));
        if (FILL_PERCENTAGE > 70){
            log();
        }

        log("id: " + id + " type: " + type + ", listener: "+hadLister + ", scaled: "+scaled+", agressive: "+aggressive);


    }



    public static Bitmap get(String id){
        return mMemoryCache.get(id);
    }

    public static Bitmap getCurrentBlurBG() {
        return currentBlurBG;
    }



    @Override
    public String toString() {
        return LOG_IDENTIFIER + " " + mMemoryCache.size() + "/" + mMemoryCache.maxSize();
    }


    private static void log(){


        int FILL_PERCENTAGE =  (int) (((float)mMemoryCache.size()/(float)mMemoryCache.maxSize()*100));
        Log.i(LOG_IDENTIFIER,  FILL_PERCENTAGE +"%: " + mMemoryCache.size() + "/" + mMemoryCache.maxSize() +" Size: " + mMemoryCache.putCount());
    }


    public static String getIDWithTypeSuffix(String id, ImageSize type){
        return id + getSuffix(type);
    }

    private static String getSuffix(ImageSize type){
        switch (type){
            case HUGE:
                return HUGE_APPENDIX;

            case BACKGROUND:
                return BLUR_APPENDIX;


        }
        return "";
    }

    public interface ImageInCacheListener{
        void ImageInCache(Bitmap image);
    }

    public static void registerListener(String videoId, ImageSize size, ImageInCacheListener listener){

        if (BitmapCache.has(videoId, size)){
            listener.ImageInCache(BitmapCache.get(videoId, size));
        } else {
            cacheListeners.put(getIDWithTypeSuffix(videoId,size),listener);
        }

    }

    private static boolean hasListener(String videoID, ImageSize type){
        return cacheListeners.containsKey(getIDWithTypeSuffix(videoID,type));
    }

    private static void log(String data){
       // Log.i(LOG_IDENTIFIER, data);
    }



}
