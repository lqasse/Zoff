package no.lqasse.zoff.ImageTools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import no.lqasse.zoff.Models.Video;

/**
 * Created by lassedrevland on 25.03.15.
 */
public class ImageDownload {

    private static final ArrayList<String> downloading = new ArrayList<>();
    private static final String LOG_IDENTIFIER = "ImageDownload";

    private enum TYPE{downlaodAndSet,downloadToCache, downloadAndBlur}


    public static void downloadAndSet(String url, String altUrl, String videoID, ImageView imageView, ImageCache.ImageType imageType){
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imageURL = url;
        viewHolder.altimageUrl = altUrl;
        viewHolder.imageView = imageView;
        viewHolder.videoId = videoID;
        viewHolder.type = TYPE.downlaodAndSet;
        viewHolder.imageType = imageType;



        if (!isDownloading(videoID,imageType)) {
            downloading(videoID,imageType);
            Downloader downloader = new Downloader();
            downloader.execute(viewHolder);
        }




    }

    public static void downloadToCache(String videoID,ImageCache.ImageType type, Boolean scale){
        ViewHolder holder = new ViewHolder();

        if (type == ImageCache.ImageType.HUGE){
            holder.imageURL = Video.getThumbHuge(videoID);
            holder.altimageUrl = Video.getThumbMed(videoID);
        } else {
            holder.imageURL = Video.getThumbMed(videoID);
        }

        holder.videoId = videoID;
        holder.type = TYPE.downloadToCache;
        holder.imageType = type;
        holder.scale = scale;


        if (!isDownloading(videoID,type)) {
            downloading(videoID,type);
            Downloader downloader = new Downloader();
            downloader.execute(holder);
        }
    }



    public static void downloadToCache(String id){


        downloadToCache(id, ImageCache.ImageType.REG);
    }

    public static void downloadToCache(String id, ImageCache.ImageType type){
        downloadToCache(id, type, true);


    }

    public static void downloadAndBlur(String videoID, ImageCache.ImageType type){
        ViewHolder holder = new ViewHolder();

        if (type == ImageCache.ImageType.HUGE){
            holder.imageURL = Video.getThumbHuge(videoID);
            holder.altimageUrl = Video.getThumbMed(videoID);
        } else {
            holder.imageURL = Video.getThumbMed(videoID);
        }

        holder.videoId = videoID;
        holder.type = TYPE.downloadAndBlur;
        holder.imageType = type;


        if (!isDownloading(videoID,type)) {
            downloading(videoID,type);
            Downloader downloader = new Downloader();
            downloader.execute(holder);
        }

    }

    private static class ViewHolder{
        Bitmap bitmap;
        ImageView imageView;
        String imageURL;
        String altimageUrl;
        String videoId;
        TYPE type;
        ImageCache.ImageType imageType;
        Boolean scale;
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
        if (viewHolder.bitmap!=null){

                finishedDownloading(viewHolder.videoId, viewHolder.imageType);
               switch (viewHolder.type){
                   case downlaodAndSet:
                       if (viewHolder.imageView.getTag() != null){
                           if ((viewHolder.imageView.getTag().toString()).equals(viewHolder.videoId)){
                               viewHolder.bitmap = ImageScaler.Scale(viewHolder.bitmap,viewHolder.imageType);


                               Animation a = new AlphaAnimation(0.00f, 1.00f);
                               a.setInterpolator(new DecelerateInterpolator());
                               a.setDuration(700);
                               viewHolder.imageView.startAnimation(a);
                               a.start();
                               viewHolder.imageView.setImageBitmap(viewHolder.bitmap);




                           }
                       }

                       ImageCache.put(viewHolder.videoId, viewHolder.imageType, viewHolder.bitmap);







                       break;
                   case downloadToCache:
                       if (viewHolder.scale){
                           viewHolder.bitmap = ImageScaler.Scale(viewHolder.bitmap,viewHolder.imageType);
                       }


                       ImageCache.put(viewHolder.videoId, viewHolder.imageType, viewHolder.bitmap);
                       break;
                   case downloadAndBlur:
                       ImageBlur.create(viewHolder.bitmap,viewHolder.videoId);
               }


            }
        }


    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER,log);
    }

    private static boolean isDownloading(String id, ImageCache.ImageType type){


        return downloading.contains(ImageCache.getIDWithTypeSuffix(id, type));
    }

    private static void downloading(String id, ImageCache.ImageType type){
        downloading.add(ImageCache.getIDWithTypeSuffix(id,type));
    }

    private static void finishedDownloading(String id, ImageCache.ImageType type){
        downloading.remove(ImageCache.getIDWithTypeSuffix(id,type));
    }
}
