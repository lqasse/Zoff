package no.lqasse.zoff.Player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubePlayer;

import java.net.URL;
import java.util.ArrayList;

import no.lqasse.zoff.Datatypes.TOAST_TYPES;
import no.lqasse.zoff.Datatypes.Zoff;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Search.SearchActivity;
import no.lqasse.zoff.SettingsActivity;

public class PlayerActivity extends ActionBarActivity {
    private String ROOM_NAME;
    private String NOW_PLAYING_ID = "";

    private YouTube_Player player;
    private Zoff zoff;
    private final Handler handler = new Handler();
    private ArrayList<Zoff.Video> videoList = new ArrayList<>();
    private playerListAdapter adapter;

    private Menu menu;
    private ListView videoListView;
    private TextView titleLabel;
    private TextView videoDurationLabel;
    private TextView currentTimeLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        ROOM_NAME = b.getString("ROOM_NAME");
        zoff = new Zoff(ROOM_NAME, this);


        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        titleLabel = (TextView) findViewById(R.id.titleView);
        videoDurationLabel = (TextView) findViewById(R.id.video_length_view);
        currentTimeLabel = (TextView) findViewById(R.id.video_current_time_view);
        videoListView = (ListView) findViewById(R.id.videoList);


        adapter = new playerListAdapter(this, videoList);
        videoListView.setAdapter(adapter);

        videoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                zoff.vote(position + 1);


                return true;
            }
        });

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                zoff.showToast(TOAST_TYPES.HOLD_TO_VOTE);


            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);


        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        MenuItem play = menu.findItem(R.id.action_togglePlay);


        switch (id) {
            case (R.id.action_settings):
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("ROOM_NAME", ROOM_NAME);
                startActivity(settingsIntent);
                break;
            case (R.id.action_next):
                zoff.voteSkip();
                zoff.refreshData();
                break;
            case (R.id.action_togglePlay):
                togglePlay();


                break;
            case (R.id.action_search):
                player.pause();
                Intent searchIntent = new Intent(this, SearchActivity.class);
                searchIntent.putExtra("ROOM_NAME", ROOM_NAME);
                startActivity(searchIntent);


                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zoff.pauseRefresh();

    }

    @Override
    protected void onPause() {
        player.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //player.play();

        if (player == null) {
            player = new YouTube_Player(this);

        } else {
            player.loadVideos(zoff.getVideoIDs());

        }


    }


    public void zoffRefreshed() {


        videoList.clear();
        videoList.addAll(zoff.getNextVideos());

        adapter.notifyDataSetChanged();
        titleLabel.setText(zoff.getNowPlayingTitle());
        currentTimeLabel.setText(zoff.getVIEWERS_STRING());


        if (zoff.getNowPlayingVideo().getImgBig() == null) {
            downloadBG downloadBG = new downloadBG();
            downloadBG.execute();

        }


        //play next video if current playing != zoff-currentplaying
        if (!NOW_PLAYING_ID.equals(zoff.getNowPlayingID()) && !NOW_PLAYING_ID.equals("")) {
            //videoEnded();
            player.loadVideos(zoff.getVideoIDs());

            player.next();
            NOW_PLAYING_ID = zoff.getNowPlayingID();
        }


    }


    private void loadFirstVideo() {

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                loadFirstVideo();
            }
        };

        if (zoff.hasVideos() && (player.isInitialized())) {
            player.loadVideos(zoff.getVideoIDs());
            NOW_PLAYING_ID = zoff.getNowPlayingID();


            handler.removeCallbacks(r, this);
        } else {

            handler.postDelayed(r, 100);


        }
    }


    public void videoEnded() {

        zoff.voteSkip();
        zoff.forceRefresh();


    }

    public void videoError(YouTubePlayer.ErrorReason errorReason) {
        switch (errorReason) {
            case NOT_PLAYABLE:

                videoEnded();
                zoff.showToast(TOAST_TYPES.EMBEDDING_DISABLED, zoff.getNowPlayingTitle());

                break;
        }

    }

    public void playerInitialized() {
        loadFirstVideo();

    }

    public void setBlurBg(Bitmap blurBg) {
        LinearLayout l = (LinearLayout) findViewById(R.id.layout);
        l.setBackground(new BitmapDrawable(getBaseContext().getResources(), blurBg));
    }


    public static class playerListAdapter extends ArrayAdapter<Zoff.Video> {
        private final Context context;
        private final ArrayList<Zoff.Video> results;

        public playerListAdapter(Context context, ArrayList<Zoff.Video> results) {
            super(context, R.layout.now_playing_row, results);
            this.context = context;
            this.results = results;


        }


        private static class ViewHolder {
            ImageView imageView;
            String imageURL;
            Bitmap bitmap;
            int position;
            ProgressBar progressBar;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            viewHolder = new ViewHolder();

            View rowView;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.now_playing_row, parent, false);
            viewHolder.imageURL = results.get(position).getThumbMed();


            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
            ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

            viewHolder.imageView = imageView;
            viewHolder.position = position;
            viewHolder.progressBar = progressBar;


            if (results.get(position).getImg() == null) {
                new downloadImage().execute(viewHolder);
            } else {
                imageView.setImageBitmap(results.get(position).getImg());
                progressBar.setVisibility(View.GONE);
            }


            TextView title = (TextView) rowView.findViewById(R.id.titleView);
            TextView votes = (TextView) rowView.findViewById(R.id.votesView);


            title.setText(results.get(position).getTitle());
            if (votes != null) {
                votes.setText(results.get(position).getVotes());
            }


            return rowView;
        }

        private class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder> {


            @Override
            protected ViewHolder doInBackground(ViewHolder... params) {
                ViewHolder viewHolder = params[0];

                try {
                    URL imageURL = new URL(viewHolder.imageURL);
                    viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
                } catch (Exception e) {
                    Log.d("ERROR", e.getLocalizedMessage());
                    e.printStackTrace();
                    viewHolder.bitmap = null;
                }
                return viewHolder;
            }

            @Override
            protected void onPostExecute(ViewHolder result) {
                if (result.bitmap == null) {
                    Log.d("FAIL", "NO IMAGE");
                } else {


                    result.progressBar.setVisibility(View.GONE);

                    //Animate fade in <3
                    Animation a = new AlphaAnimation(0.00f, 1.00f);
                    a.setInterpolator(new DecelerateInterpolator());
                    a.setDuration(700);
                    result.imageView.setImageBitmap(result.bitmap);
                    result.imageView.setAnimation(a);
                    result.imageView.startAnimation(a);

                    results.get(result.position).setImg(result.bitmap);


                }
            }

        }

    }


    private class downloadBG extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap b;
            try {
                URL imageURL = new URL(zoff.getNowPlayingVideo().getThumbBig());
                b = BitmapFactory.decodeStream(imageURL.openStream());
            } catch (Exception e) {
                Log.d("ERROR", e.getLocalizedMessage());
                e.printStackTrace();
                b = null;
            }
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            zoff.getNowPlayingVideo().setImgBig(bitmap);

            super.onPostExecute(bitmap);


        }


    }


    public void updatePlaytime() {
        videoDurationLabel.setText(player.getPlaytime());
    }

    public void togglePlayIcon() {
        MenuItem menuItem = menu.findItem(R.id.action_togglePlay);
        if (player != null) {
            if (player.isPlaying()) {
                menuItem.setIcon(R.drawable.play);
            } else {
                menuItem.setIcon(R.drawable.pause);
            }
        }//777
    }

    public void togglePlay() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();

            } else {
                player.play();
            }
            togglePlayIcon();

        }
    }
}