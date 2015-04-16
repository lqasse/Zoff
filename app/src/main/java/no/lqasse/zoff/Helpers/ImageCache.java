package no.lqasse.zoff.Helpers;

import android.graphics.Bitmap;

import java.util.HashMap;

import no.lqasse.zoff.Interfaces.ImageListener;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class ImageCache {
    private static final String HUGE_APPENDIX = "_huge";
    private static final String BLUR_APPENDIX = "_blur";

    public enum ImageType{BLUR,HUGE,REG}

    private static Bitmap currentBlurBG;

    private static ImageListener listener;
    private static String listenID;

    private static HashMap<String,Bitmap> ImageMap = new HashMap<>();

    public static boolean has(String id){
        return ImageMap.containsKey(id);
    }

    public static boolean has(String id,ImageType type){

        String appendix = "";
        switch (type){
            case HUGE:
                appendix = HUGE_APPENDIX;
                break;
            case BLUR:
                appendix = BLUR_APPENDIX;
                break;
        }
        return ImageMap.containsKey(id+ appendix);


    }

    public static Bitmap get(String id,ImageType type){
        String appendix = "";
        switch (type){
            case HUGE:
                appendix = HUGE_APPENDIX;
                break;
            case BLUR:
                appendix = BLUR_APPENDIX;
                break;
        }

        if (ImageMap.containsKey(id + appendix)){
            return ImageMap.get(id + appendix);
        } else{
            return null;
        }




    }

    public static void put(String id,Bitmap image){
        ImageMap.put(id,image);
        if (id.contains("_blur") && currentBlurBG!=image){
            currentBlurBG = image;
        }

        if(listener !=null && id.equals(listenID)){
            listener.imageInCache(image);
            listener = null;

        }


    }

    public static void put(String id, ImageType type, Bitmap bitmap){
        String appendix = "";
        switch (type){
            case HUGE:
                appendix = HUGE_APPENDIX;
                break;
            case BLUR:
                appendix = BLUR_APPENDIX;
                if (currentBlurBG!=bitmap){
                    currentBlurBG = bitmap;
                }
                break;
        }

        ImageMap.put(id + appendix,bitmap);



        if(listener !=null && id.equals(listenID)){
            listener.imageInCache(bitmap);
            listener = null;

        }
    }

    public static Bitmap get(String id){
        return ImageMap.get(id);
    }

    public static Bitmap getCurrentBlurBG() {
        return currentBlurBG;
    }

    public static void registerImageListener(ImageListener listener, String idToListenFor){
        ImageCache.listener = listener;
        listenID = idToListenFor;
    }
}
