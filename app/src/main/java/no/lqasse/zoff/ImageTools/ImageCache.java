package no.lqasse.zoff.ImageTools;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import java.util.ArrayList;
import java.util.HashMap;

import no.lqasse.zoff.Interfaces.ImageListener;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class ImageCache {
    private static final String LOG_IDENTIFIER = "ImageCache";
    private static final String HUGE_APPENDIX = "_huge";
    private static final String BLUR_APPENDIX = "_blur";

    private static final int LOWER_MEMORY_THRESHOLD = 10 * 1024; //10mb
    final static int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final static int cacheSize = maxMemory / 8;



    private static ArrayList<String> hugeImagesInCache = new ArrayList<>();
    private static LruCache<String, Bitmap> mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() /1024;
        }

    };

    public static void empty(){
        log();
        if (mMemoryCache != null){
            mMemoryCache.evictAll();
        }
        log("Emptied:" + Integer.toString(mMemoryCache.evictionCount()));
    }



    public enum ImageType{BLUR,HUGE,REG}

    private static Bitmap currentBlurBG;

    private static ImageListener listener;


    private static String listenID;

    private static HashMap<String,ImageListener> listenersMap = new HashMap<>();

    //private static HashMap<String,Bitmap> ImageMap = new HashMap<>();


    public static boolean has(String id){
        return mMemoryCache.get(id) != null;
    }

    public static boolean has(String id,ImageType type){


        return mMemoryCache.get(id + getSuffix(type)) != null;




    }

    public static Bitmap get(String id,ImageType type){


        if (has(id,type)){
            return mMemoryCache.get(id + getSuffix(type));
        } else{
            return null;
        }




    }

    public static void put(String id,Bitmap image){

        mMemoryCache.put(id,image);
        if (id.contains("_blur") && currentBlurBG!=image){
            currentBlurBG = image;
        }


        if(listener !=null && id.equals(listenID)){
            listener.imageInCache(image);
            listener = null;

        }


    }

    public static void put(String id, ImageType type, Bitmap bitmap){

        if (cacheSize < LOWER_MEMORY_THRESHOLD){

            ImageScaler.setAggressiveScaling(0.75f);
        }



        switch (type){

            case BLUR:

                if (currentBlurBG!=bitmap){
                    currentBlurBG = bitmap;
                }
                break;
            case HUGE:
                hugeImagesInCache.add(id);
                break;
        }






        mMemoryCache.put(id + getSuffix(type),bitmap);
        notifyListeners(id + getSuffix(type), bitmap);

        int FILL_PERCENTAGE =  (int) (((float)mMemoryCache.size()/(float)mMemoryCache.maxSize()*100));
        if (FILL_PERCENTAGE > 70){
            log();
        }







        if(listener !=null && id.equals(listenID)){
            listener.imageInCache(bitmap);
            listener = null;

        }
    }

    public static void cleanHugeImages(String... keep){
        log();
        for(int i = 0;i<keep.length;i++){
            hugeImagesInCache.remove(keep[i]);
        }

        for(String id:hugeImagesInCache){
            mMemoryCache.remove(getIDWithTypeSuffix(id,ImageType.HUGE));
            log(id);
        }

        for(int i = 0;i<keep.length;i++){
           hugeImagesInCache.add(keep[i]);
        }
        log("Cleaned");
        log();
    }

    public static Bitmap get(String id){
        return mMemoryCache.get(id);
    }

    public static Bitmap getCurrentBlurBG() {
        return currentBlurBG;
    }

    public static void registerImageListener(ImageListener listener, String idToListenFor){
        ImageCache.listener = listener;
        listenID = idToListenFor;


        listenersMap.put(idToListenFor,listener);


    }


    public static void registerImageListener(ImageListener listener, String idToListenFor, ImageType type){
        ImageCache.listener = listener;
        listenID = idToListenFor;



        listenersMap.put(idToListenFor + getSuffix(type),listener );


    }

    private static void notifyListeners(String id, Bitmap bitmap){

        if (listenersMap.containsKey(id)){
            listenersMap.get(id).imageInCache(bitmap);
            listenersMap.remove(id);
        }

    }



    @Override
    public String toString() {
        return LOG_IDENTIFIER + " " + mMemoryCache.size() + "/" + mMemoryCache.maxSize();
    }


    private static void log(){


        int FILL_PERCENTAGE =  (int) (((float)mMemoryCache.size()/(float)mMemoryCache.maxSize()*100));
        Log.i(LOG_IDENTIFIER,  FILL_PERCENTAGE +"%: " + mMemoryCache.size() + "/" + mMemoryCache.maxSize() +" Size: " + mMemoryCache.putCount());
    }
    private static void log(String data){
        Log.i(LOG_IDENTIFIER, data);
    }

    public static String getIDWithTypeSuffix(String id, ImageCache.ImageType type){
        return id + getSuffix(type);
    }

    private static String getSuffix(ImageCache.ImageType type){
        switch (type){
            case HUGE:
                return HUGE_APPENDIX;

            case BLUR:
                return BLUR_APPENDIX;



        }
        return "";
    }
}