package no.lqasse.zoff.Datatypes;

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

import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 09.12.14.
 */
public class searchResultAdapter extends ArrayAdapter<searchResult> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<searchResult> searchResults;

    public searchResultAdapter(Context context, ArrayList<searchResult> searchResults) {
        super(context, R.layout.search_row, searchResults);
        this.context = context;
        this.searchResults = searchResults;
    }

    private static
    class ViewHolder{
        ImageView imageView;
        String imageURL;
        Bitmap bitmap;
        int position;
        ProgressBar progressBar;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;




        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.search_row, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

        viewHolder = new ViewHolder();
        viewHolder.imageView = imageView;
        viewHolder.imageURL = searchResults.get(position).getThumbSmall();
        viewHolder.position = position;
        viewHolder.progressBar = progressBar;

        if (searchResults.get(position).getImgSmall() == null){
            new downloadImage().execute(viewHolder);
        } else {
            imageView.setImageBitmap(searchResults.get(position).getImgSmall());
            progressBar.setVisibility(View.GONE);
        }


        //convertView.setTag(viewHolder);

        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView viewCount = (TextView) rowView.findViewById(R.id.viewCount);
        TextView duration = (TextView) rowView.findViewById(R.id.durationView);



        title.setText(searchResults.get(position).getTitle());
        String views = searchResults.get(position).getViewCountLocalized();
        duration.setText(searchResults.get(position).getDuration());
        viewCount.setText(views);
        //textView.setText(values[position]);
        // change the icon for Windows and iPhone
        //String s = values[position];





        return rowView;
    }



    private class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder>{

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
        protected void onPostExecute(ViewHolder result){
            if (result.bitmap == null){
                Log.d("FAIL", "NO IMAGE");
            } else {
               result.progressBar.setVisibility(View.GONE);

                Animation a = new AlphaAnimation(0.00f,1.00f);
                a.setInterpolator(new DecelerateInterpolator());
                a.setDuration(700);
                result.imageView.setImageBitmap(result.bitmap);
                result.imageView.setAnimation(a);
                result.imageView.startAnimation(a);
               searchResults.get(result.position).setImgSmall(result.bitmap);
            }
        }

    }



    }
