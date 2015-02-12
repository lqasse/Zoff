package no.lqasse.zoff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

import no.lqasse.zoff.Datatypes.Zoff;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ActionBarActivity {
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";
    private String ROOM_NAME;
    private String ROOM_PASS;

    private Zoff zoff;
    private Menu menu;
    private Boolean paused = false;
    private ListView videoList;
    private remoteListAdapter adapter;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            ROOM_NAME = b.getString("ROOM_NAME");
        }

        ROOM_PASS = getPASS();


        zoff = new Zoff(ROOM_NAME, this);
        zoff.setROOM_PASS(getPASS());

        setContentView(R.layout.activity_remote);
        getSupportActionBar().setIcon(R.drawable.logo);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(ROOM_NAME);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        adapter = new remoteListAdapter(this, zoff.getVideos(),zoff);
        videoList = (ListView) findViewById(R.id.videoList);
        videoList.setAdapter(adapter);

        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                zoff.voteVideo(position);
                return true;
            }
        });

        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(),"Click and hold to vote",Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        zoff.cancelRefresh();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        this.menu = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case (R.id.action_settings):
                break;
            case (R.id.action_next):
                zoff.videoPlayed(zoff.getNowPlayingID());
                //player.loadVideo(zoff.getNextId());
                zoff.refreshData();

                break;
            case (R.id.action_search):
                //Display youtub

                if (zoff.ANYONE_CAN_ADD()) {
                    Intent i = new Intent(this, SearchActivity.class);
                    i.putExtra("ROOM_NAME", ROOM_NAME);
                    i.putExtra("ROOM_PASS", ROOM_PASS);
                    i.putExtra("ALL_VIDEOS", zoff.ALL_VIDEOS());
                    i.putExtra("LONG_SONGS",zoff.LONG_SONGS());
                    startActivity(i);
                }else if (!zoff.ANYONE_CAN_ADD() && zoff.hasROOM_PASS()){
                    Intent i = new Intent(this, SearchActivity.class);
                    i.putExtra("ROOM_NAME", ROOM_NAME);
                    i.putExtra("ROOM_PASS", ROOM_PASS);
                    i.putExtra("ALL_VIDEOS", zoff.ALL_VIDEOS());
                    i.putExtra("LONG_SONGS",zoff.LONG_SONGS());

                    startActivity(i);
                } else {

                    Toast.makeText(this, "This room is password protected, set a password to vote", Toast.LENGTH_LONG).show();

                }


                break;

            case (R.id.action_zoff_settings):
                //Display ZOff settings

                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtras(zoff.getSettingsBundle());


                startActivity(i);
                break;
            case (R.id.action_shuffle):
                if (zoff.hasROOM_PASS()){
                    Toast.makeText(this, "Shuffled!",Toast.LENGTH_SHORT).show();
                    zoff.shuffle();
                } else {
                    Toast.makeText(this, "This room is password protected, set a password to shuffle",Toast.LENGTH_SHORT).show();
                }
                break;


        }


        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStop() {
        paused = true;
        showNotification();

        super.onStop();
    }

    @Override
    protected void onResume() {
        paused = false;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        super.onResume();
    }



    public void zoffRefreshed() {

        //adapter.clear();
       // adapter.addAll(zoff.getVideos());
        adapter.notifyDataSetChanged();



        if (paused) {
            showNotification();
        }


    }

    private String getPASS(){
        String PASS;
        sharedPreferences = getSharedPreferences(PREFS_FILE,0);
        PASS = sharedPreferences.getString(ROOM_NAME,null);

        return PASS;

    }

    private void showNotification() {

        Intent intent = new Intent(this, RemoteActivity.class);
        Bundle b = new Bundle();
        b.putString("ROOM_NAME", zoff.getROOM_NAME());
        intent.putExtras(b);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(zoff.getROOM_NAME())
                .setContentText(zoff.getNowPlayingTitle())
                .setOngoing(true)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent);


        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);


    }

    public Zoff getZoff(){
        return this.zoff;
    }


    /**
     * Created by lassedrevland on 12.12.14.
     */
    public static class remoteListAdapter extends ArrayAdapter<Zoff.Video> {
        private final Context context;
        //private final String[] values;
        private final ArrayList<Zoff.Video> results;
        private Zoff zoff;



        public remoteListAdapter(Context context, ArrayList<Zoff.Video> results, Zoff zoff) {
            super(context, R.layout.now_playing_row, results);
            this.context = context;
            this.results = results;
            this.zoff = zoff;


        }



        private static
        class ViewHolder{
            ImageView imageView;
            String imageURL;
            Bitmap bitmap;
            int position;
            ProgressBar progressBar;
            Boolean hqImage = false;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            viewHolder = new ViewHolder();

            View rowView;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            if (position == 0){ //Top of list, Now playing!
                rowView = inflater.inflate(R.layout.now_playing_row_top, parent, false);
                viewHolder.imageURL = results.get(position).getThumbBig();




            } else {
                rowView = inflater.inflate(R.layout.now_playing_row, parent, false);
                viewHolder.imageURL = results.get(position).getThumbMed();
            }

            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
            ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

            viewHolder.imageView = imageView;
            viewHolder.position = position;
            viewHolder.progressBar = progressBar;


            if (position == 0 && results.get(position).getImgBig() == null){ //TOp image is not in hq
                viewHolder.hqImage = true;
                new downloadImage().execute(viewHolder);
            } else if (position == 0 && results.get(position).getImgBig() != null){ //TOp image is in hq
                imageView.setImageBitmap(results.get(position).getImgBig());
                progressBar.setVisibility(View.GONE);
            } else if (results.get(position).getImg() == null){ //Not top, not downloaded
                new downloadImage().execute(viewHolder);
            } else {
                imageView.setImageBitmap(results.get(position).getImg());//Not top,  downloaded
                progressBar.setVisibility(View.GONE);
            }


            TextView title = (TextView) rowView.findViewById(R.id.title);
            TextView votes = (TextView) rowView.findViewById(R.id.votesView);






            title.setText(results.get(position).getTitle());

            if (votes != null){

                votes.setText(results.get(position).getVotes());

            }

            //textView.setText(values[position]);
            // change the icon for Windows and iPhone
            //String s = values[position];


            return rowView;
        }



        private class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder> {





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

                    //Animate fade in <3
                    Animation a = new AlphaAnimation(0.00f,1.00f);
                    a.setInterpolator(new DecelerateInterpolator());
                    a.setDuration(700);
                    result.imageView.setImageBitmap(result.bitmap);
                    result.imageView.setAnimation(a);
                    result.imageView.startAnimation(a);

                    result.imageView.setImageBitmap(result.bitmap);
                    if (result.hqImage){
                        results.get(result.position).setImgBig(result.bitmap);
                    } else {
                        results.get(result.position).setImg(result.bitmap);
                    }




                }
            }



        }

    }





}


