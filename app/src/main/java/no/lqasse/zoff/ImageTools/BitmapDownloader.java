package no.lqasse.zoff.ImageTools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.net.URL;
import java.util.ArrayList;

import no.lqasse.zoff.Models.Video;

/**
 * Created by lassedrevland on 25.03.15.
 */
public class BitmapDownloader {

    private static final ArrayList<String> downloading = new ArrayList<>();
    private static final String LOG_IDENTIFIER = "ImageDownload";



    private static class ViewHolder{
        Bitmap bitmap;
        ImageView imageView;
        String imageURL;
        String altimageUrl;
        String videoId;
        TYPE type;
        BitmapCache.ImageSize imageSize;
        Callback callback;
        SetImageCallback setImageCallback;
        Boolean scaleDownFlag;

    }

    public static void download(String videoId, BitmapCache.ImageSize type, Boolean flagScaleDown, Callback callback) throws IllegalArgumentException{

        if (videoId.equals("")){
            throw new IllegalArgumentException("VideoID can not be empty");
        }
        ViewHolder viewHolder = new ViewHolder();
        if (type == BitmapCache.ImageSize.HUGE){
            viewHolder.imageURL = Video.getThumbHuge(videoId);
            viewHolder.altimageUrl = Video.getThumbMed(videoId);
        } else {
            viewHolder.imageURL = Video.getThumbMed(videoId);
        }

        viewHolder.videoId = videoId;
        viewHolder.callback = callback;
        viewHolder.imageSize = type;
        viewHolder.type = TYPE.download;
        viewHolder.scaleDownFlag = flagScaleDown;

        if (!isDownloading(videoId,type)) {
            downloading(videoId,type);
            Downloader downloader = new Downloader();
            downloader.execute(viewHolder);
        }
    }

    public static void downloadTo(String id, ImageView target, Boolean flagScaleDown, BitmapCache.ImageSize type, SetImageCallback callback){

        ViewHolder viewHolder = new ViewHolder();
        if (type == BitmapCache.ImageSize.HUGE){
            viewHolder.imageURL = Video.getThumbHuge(id);
            viewHolder.altimageUrl = Video.getThumbMed(id);
        } else {
            viewHolder.imageURL = Video.getThumbMed(id);
        }

        viewHolder.imageView = target;
        viewHolder.setImageCallback = callback;
        viewHolder.imageSize = type;
        viewHolder.type = TYPE.download;
        viewHolder.scaleDownFlag = flagScaleDown;
        viewHolder.videoId = id;

        if (!isDownloading(id,type)) {
            downloading(id,type);
            Downloader downloader = new Downloader();
            downloader.execute(viewHolder);
        }


    }

    public static void downloadAndSet(String url, String altUrl, String videoID, ImageView imageView, BitmapCache.ImageSize imageSize,Boolean scaleDownFlag){
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imageURL = url;
        viewHolder.altimageUrl = altUrl;
        viewHolder.imageView = imageView;
        viewHolder.videoId = videoID;
        viewHolder.type = TYPE.downlaodAndSet;
        viewHolder.imageSize = imageSize;
        viewHolder.scaleDownFlag = scaleDownFlag;




        if (!isDownloading(videoID, imageSize)) {
            downloading(videoID, imageSize);
            Downloader downloader = new Downloader();
            downloader.execute(viewHolder);
        }




    }

    private static boolean isDownloading(String id, BitmapCache.ImageSize type){
        return downloading.contains(BitmapCache.getIDWithTypeSuffix(id, type));
    }

    private static void downloading(String id, BitmapCache.ImageSize type){
        downloading.add(BitmapCache.getIDWithTypeSuffix(id, type));
    }

    private static void finishedDownloading(String id, BitmapCache.ImageSize type){
        downloading.remove(BitmapCache.getIDWithTypeSuffix(id, type));
    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER,log);
    }

    private enum TYPE{downlaodAndSet,download}





    private static class Downloader extends AsyncTask<ViewHolder, Void, ViewHolder> {
        @Override
        protected ViewHolder doInBackground(ViewHolder... params) {
            ViewHolder viewHolder = params[0];



            try {
                URL imageURL = new URL(viewHolder.imageURL);
                viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
            } catch (Exception e) {
                try {
                    viewHolder.bitmap = null;
                    URL imageURL = new URL(viewHolder.altimageUrl);
                    viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
                } catch (Exception e2){

                    Log.i(LOG_IDENTIFIER, "Download failed: " + viewHolder.imageURL + " ; " + viewHolder.altimageUrl);
                    //e2.printStackTrace();
                    //e.printStackTrace();

                    viewHolder.bitmap = null;
                }


            }
            return viewHolder;
        }

        @Override
        protected void onPostExecute(ViewHolder viewHolder) {
            if (viewHolder.bitmap!=null){

                finishedDownloading(viewHolder.videoId, viewHolder.imageSize);

                BitmapCache.put(viewHolder.videoId, viewHolder.imageSize, viewHolder.bitmap, viewHolder.scaleDownFlag);

                if (viewHolder.callback != null){

                    viewHolder.callback.onImageDownloaded(viewHolder.bitmap, viewHolder.imageSize);
                }



                if (viewHolder.setImageCallback != null){
                    viewHolder.setImageCallback.onImageDownloaded(viewHolder.bitmap,viewHolder.videoId);

                }



                switch (viewHolder.type){
                    case downlaodAndSet:
                        if (viewHolder.imageView.getTag() != null){
                            if ((viewHolder.imageView.getTag().toString()).equals(viewHolder.videoId)){
                                viewHolder.bitmap = BitmapScaler.Scale(viewHolder.bitmap, viewHolder.imageSize);
                                Animation a = new AlphaAnimation(0.00f, 1.00f);
                                a.setInterpolator(new DecelerateInterpolator());
                                a.setDuration(700);
                                viewHolder.imageView.startAnimation(a);
                                a.start();
                                viewHolder.imageView.setImageBitmap(viewHolder.bitmap);

                            }
                        }

                        break;

                }


            }
        }


    }

    public interface Callback{
        void onImageDownloaded(Bitmap image,BitmapCache.ImageSize type);
    }

    public interface SetImageCallback {
        void onImageDownloaded(Bitmap image, String videoId);
    }
}
