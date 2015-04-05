package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.net.URL;
import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ImageBlur;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.Zoff;
/**
 * Created by lassedrevland on 04.04.15.
 */
public abstract class ZoffListAdapter extends ArrayAdapter<Video> {
        //private final String[] values;
        protected final ArrayList<Video> videoList;
        protected Context context;
        protected Zoff zoff;


        public ZoffListAdapter(Context context, ArrayList<Video> videoList, Zoff zoff) {
            super(context, R.layout.now_playing_row, videoList);
            this.context = context;
            this.videoList = videoList;
            this.zoff = zoff;


        }



        public class ViewHolder {

            ImageView imageView;
            String imageURL;
            Bitmap bitmap;
            int position;
            ProgressBar progressBar;
            Video video;

        }

        protected class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder> {


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
                    viewHolder.progressBar.setVisibility(View.GONE);
                    //Animate fade in <3
                    Animation a = new AlphaAnimation(0.00f, 1.00f);
                    a.setInterpolator(new DecelerateInterpolator());
                    a.setDuration(700);
                    viewHolder.imageView.setImageBitmap(viewHolder.bitmap);
                    viewHolder.imageView.setAnimation(a);
                    viewHolder.imageView.startAnimation(a);

                    viewHolder.imageView.setImageBitmap(viewHolder.bitmap);
                    ImageCache.put(viewHolder.video.getId(), viewHolder.bitmap);

                    if (!ImageCache.has(viewHolder.video.getId()+"_blur") && (viewHolder.position == 0)){
                        ImageBlur.createAndSetBlurBG(viewHolder.bitmap,(RemoteActivity) context,viewHolder.video.getId());
                    } else if (ImageCache.has(viewHolder.video.getId()+"_blur")){
                        ((RemoteActivity)context).setBackgroundImage(ImageCache.get(viewHolder.video.getId()+"_blur"));
                    }
                }
            }


        }


}
