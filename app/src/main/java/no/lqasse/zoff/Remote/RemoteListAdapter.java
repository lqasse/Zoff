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


import no.lqasse.zoff.Datatypes.Zoff;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Models.ZoffVideo;
import no.lqasse.zoff.R;

public class RemoteListAdapter extends ArrayAdapter<ZoffVideo> {
        private Context context;
        //private final String[] values;
        private final ArrayList<ZoffVideo> videoList;
        private Zoff zoff;



        public RemoteListAdapter(Context context, ArrayList<ZoffVideo> videoList, Zoff zoff) {
            super(context, R.layout.now_playing_row, videoList);
            this.context = context;
            this.videoList = videoList;
            this.zoff = zoff;


        }



        private static
        class ViewHolder{
            ImageView imageView;
            String imageURL;
            Bitmap bitmap;
            int position;
            ProgressBar progressBar;
            ZoffVideo video;
            Boolean hqImage = false;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            viewHolder = new ViewHolder();

            ZoffVideo currentVideo = videoList.get(position);

            View rowView;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            if (position == 0){ //Top of list, Now playing!
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


            /*
            if (position == 0 && videoList.get(position).getImgBig() == null){ //TOp image is not in hq
                viewHolder.hqImage = true;
                new downloadImage().execute(viewHolder);
            } else if (position == 0 && videoList.get(position).getImgBig() != null){ //TOp image is in hq
                imageView.setImageBitmap(videoList.get(position).getImgBig());
                progressBar.setVisibility(View.GONE);
            } else if (videoList.get(position).getImg() == null){ //Not top, not downloaded
                new downloadImage().execute(viewHolder);
            } else {
                imageView.setImageBitmap(videoList.get(position).getImg());//Not top,  downloaded
                progressBar.setVisibility(View.GONE);
            }

            */


                if (ImageCache.has(currentVideo.getId())){
                    imageView.setImageBitmap(ImageCache.get(currentVideo.getId()));
                    progressBar.setVisibility(View.GONE);
                } else {
                    new downloadImage().execute(viewHolder);
                }



            TextView title = (TextView) rowView.findViewById(R.id.titleView);
            TextView votes = (TextView) rowView.findViewById(R.id.votesView);
            TextView views = (TextView) rowView.findViewById(R.id.viewsLabel);
            TextView playtime = (TextView) rowView.findViewById(R.id.playtimeLabel);






            title.setText(videoList.get(position).getTitle());

            if (votes != null){

                votes.setText(videoList.get(position).getVotes());

            } else {
                views.setText(zoff.getVIEWERS_STRING());
                playtime.setText(""); //Implement later?

            }




            return rowView;
        }



        private  class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder> {





            @Override
            protected ViewHolder doInBackground(ViewHolder... params){
                ViewHolder viewHolder = params[0];

                try {
                    URL imageURL = new URL(viewHolder.imageURL);
                    viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
                } catch (Exception e){
                    Log.d("ERROR", e.getLocalizedMessage());
                    e.printStackTrace();
                    viewHolder.bitmap = null;
                }
                return viewHolder;
            }

            @Override
            protected void onPostExecute(ViewHolder viewHolder){
                if (viewHolder.bitmap == null){
                    Log.d("FAIL", "NO IMAGE");
                } else {
                    viewHolder.progressBar.setVisibility(View.GONE);

                    //Animate fade in <3
                    Animation a = new AlphaAnimation(0.00f,1.00f);
                    a.setInterpolator(new DecelerateInterpolator());
                    a.setDuration(700);
                    viewHolder.imageView.setImageBitmap(viewHolder.bitmap);
                    viewHolder.imageView.setAnimation(a);
                    viewHolder.imageView.startAnimation(a);

                    viewHolder.imageView.setImageBitmap(viewHolder.bitmap);

                    ImageCache.put(viewHolder.video.getId(),viewHolder.bitmap);








                }
            }



        }

    }