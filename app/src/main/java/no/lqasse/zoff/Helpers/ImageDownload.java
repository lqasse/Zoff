package no.lqasse.zoff.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;

import no.lqasse.zoff.Models.Video;

/**
 * Created by lassedrevland on 25.03.15.
 */
public class ImageDownload {


    public static void downloadAndSet(String videoID, ImageView imageView){
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.imageURL = Video.getThumbMed(videoID);
        viewHolder.imageView = imageView;


        Downloader downloader = new Downloader();
        downloader.execute(viewHolder);



    }

    public static void downloadToCache(String id){
        ViewHolder holder = new ViewHolder();
        holder.imageURL = Video.getThumbMed(id);
        holder.videoId = id;

        Downloader downloader = new Downloader();
        downloader.execute(holder);


    }

    private static class ViewHolder{
        Bitmap bitmap;
        ImageView imageView;
        String imageURL;
        String videoId;
    }
    private static class Downloader extends AsyncTask<ViewHolder, Void, ViewHolder> {


        @Override
        protected ViewHolder doInBackground(ViewHolder... params) {
            ViewHolder viewHolder = params[0];

            try {
                URL imageURL = new URL(viewHolder.imageURL);
                viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
            } catch (Exception e) {
                Log.d("IMG", "Failed");

                viewHolder.bitmap = null;
            }
            return viewHolder;
        }

        @Override
        protected void onPostExecute(ViewHolder viewHolder) {
            if (viewHolder.bitmap == null) {

            } else {

               ImageCache.put(viewHolder.videoId,viewHolder.bitmap);
            }
        }


    }
}
