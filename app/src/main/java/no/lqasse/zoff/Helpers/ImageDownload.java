package no.lqasse.zoff.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.net.URL;

import no.lqasse.zoff.Models.Video;

/**
 * Created by lassedrevland on 25.03.15.
 */
public class ImageDownload {
    private static final String LOG_IDENTIFIER = "ImageDownload";

    private enum TYPE{downlaodAndSet,downloadToCache}


    public static void downloadAndSet(String url, String altUrl, String videoID, ImageView imageView, ImageCache.ImageType imageType){
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imageURL = url;
        viewHolder.altimageUrl = altUrl;
        viewHolder.imageView = imageView;
        viewHolder.videoId = videoID;
        viewHolder.type = TYPE.downlaodAndSet;
        viewHolder.imageType = imageType;


        Downloader downloader = new Downloader();
        downloader.execute(viewHolder);



    }



    public static void downloadToCache(String id){

        downloadToCache(id, ImageCache.ImageType.REG);
    }

    public static void downloadToCache(String id, ImageCache.ImageType type){
        ViewHolder holder = new ViewHolder();

        if (type == ImageCache.ImageType.HUGE){
            holder.imageURL = Video.getThumbHuge(id);
            holder.altimageUrl = Video.getThumbMed(id);
        } else {
            holder.imageURL = Video.getThumbMed(id);
        }

        holder.videoId = id;
        holder.type = TYPE.downloadToCache;
        holder.imageType = type;

        Downloader downloader = new Downloader();
        downloader.execute(holder);


    }

    private static class ViewHolder{
        Bitmap bitmap;
        ImageView imageView;
        String imageURL;
        String altimageUrl;
        String videoId;
        TYPE type;
        ImageCache.ImageType imageType;
    }
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
            if (viewHolder.bitmap == null) {

            } else {
               switch (viewHolder.type){
                   case downlaodAndSet:

                       ImageCache.put(viewHolder.videoId, viewHolder.imageType, viewHolder.bitmap);
                       Animation a = new AlphaAnimation(0.00f, 1.00f);
                       a.setInterpolator(new DecelerateInterpolator());
                       a.setDuration(700);
                       viewHolder.imageView.setAnimation(a);
                       viewHolder.imageView.startAnimation(a);
                       viewHolder.imageView.setImageBitmap(viewHolder.bitmap);


                       break;
                   case downloadToCache:
                       ImageCache.put(viewHolder.videoId, viewHolder.imageType, viewHolder.bitmap);
                       break;
               }


            }
        }


    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER,log);
    }
}
