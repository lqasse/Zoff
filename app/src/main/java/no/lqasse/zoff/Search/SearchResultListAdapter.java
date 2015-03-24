package no.lqasse.zoff.Search;

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

import no.lqasse.zoff.Models.Searchresult;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 09.12.14.
 */
public class SearchResultListAdapter extends ArrayAdapter<Searchresult> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<Searchresult> Searchresults;

    public SearchResultListAdapter(Context context, ArrayList<Searchresult> Searchresults) {
        super(context, R.layout.search_row, Searchresults);
        this.context = context;
        this.Searchresults = Searchresults;
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
        viewHolder.imageURL = Searchresults.get(position).getThumbSmall();
        viewHolder.position = position;
        viewHolder.progressBar = progressBar;

        if (Searchresults.get(position).getImgSmall() == null){
            new downloadImage().execute(viewHolder);
        } else {
            imageView.setImageBitmap(Searchresults.get(position).getImgSmall());
            progressBar.setVisibility(View.GONE);
        }


        //convertView.setTag(viewHolder);

        TextView title = (TextView) rowView.findViewById(R.id.titleView);
        TextView channelTitle = (TextView) rowView.findViewById(R.id.channelTitle);
        TextView viewCount = (TextView) rowView.findViewById(R.id.viewCount);
        TextView duration = (TextView) rowView.findViewById(R.id.durationView);



        title.setText(Searchresults.get(position).getTitle());
        channelTitle.setText(Searchresults.get(position).getChannelTitle());
        String views = Searchresults.get(position).getViewCountLocalized();
        duration.setText(Searchresults.get(position).getDuration());
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
               Searchresults.get(result.position).setImgSmall(result.bitmap);
            }
        }

    }



    }
