package no.lqasse.zoff.Remote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ImageBlur;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Zoff;

public class RemoteListAdapter extends ArrayAdapter<Video> {
    //private final String[] values;
    private final ArrayList<Video> videoList;
    private Context context;
    private Zoff zoff;


    public RemoteListAdapter(Context context, ArrayList<Video> videoList, Zoff zoff) {
        super(context, R.layout.now_playing_row, videoList);
        this.context = context;
        this.videoList = videoList;
        this.zoff = zoff;


    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        viewHolder = new ViewHolder();

        Video currentVideo = videoList.get(position);

        View rowView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        if (position == 0) { //Top of list, Now playing!
            rowView = inflater.inflate(R.layout.now_playing_row_top, parent, false);
            viewHolder.imageURL = videoList.get(position).getImageBig();



        } else {
            rowView = inflater.inflate(R.layout.now_playing_row, parent, false);
            viewHolder.imageURL = videoList.get(position).getThumbMed();
        }

        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

        viewHolder.imageView = imageView;
        viewHolder.position = position;
        viewHolder.progressBar = progressBar;
        viewHolder.video = currentVideo;


        if (ImageCache.has(currentVideo.getId())) {
            imageView.setImageBitmap(ImageCache.get(currentVideo.getId()));
            progressBar.setVisibility(View.GONE);
        } else {
            new downloadImage().execute(viewHolder);
        }

        if (ImageCache.has(currentVideo.getId()) && position == 0 && !ImageCache.has(currentVideo.getId()+"_blur")){
            ImageBlur.createAndSetBlurBG(ImageCache.get(currentVideo.getId()),(RemoteActivity)context,currentVideo.getId());


        }


        TextView title = (TextView) rowView.findViewById(R.id.titleView);
        TextView votes = (TextView) rowView.findViewById(R.id.votesView);
        TextView views = (TextView) rowView.findViewById(R.id.viewsLabel);
        TextView playtime = (TextView) rowView.findViewById(R.id.playtimeLabel);


        title.setText(videoList.get(position).getTitle());

        if (votes != null) {

            votes.setText(videoList.get(position).getVotes());

        } else {
            views.setText(zoff.getVIEWERS_STRING());
            playtime.setText(""); //Implement later?

        }


        return rowView;
    }

    private static class ViewHolder {
        ImageView imageView;
        String imageURL;
        Bitmap bitmap;
        int position;
        ProgressBar progressBar;
        Video video;

    }

    private class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder> {


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
                }
            }
        }


    }

}