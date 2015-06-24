package no.lqasse.zoff.Player;

/*
public class PlayerActivity extends ZoffActivity implements ImageListener {
    private String NOW_PLAYING_ID = "";
    private YouTube_Player player;
    private final Handler handler = new Handler();
    private ListAdapter listAdapter;
    private ArrayList<Video> videoList = new ArrayList<>();

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
        channel = b.getString(Zoff.BUNDLEKEY_CHANNEL);
        zoff = new Zoff(channel, this);


        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getChannelName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        titleLabel = (TextView) findViewById(R.id.videoTitleView);
        videoDurationLabel = (TextView) findViewById(R.id.video_length_view);
        currentTimeLabel = (TextView) findViewById(R.id.video_current_time_view);
        videoListView = (ListView) findViewById(R.id.videoList);


        listAdapter = new ListAdapter(this, zoff.getNextVideos(), zoff.getZoff());


        videoListView.setAdapter(listAdapter);

        videoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Video selectedVideo = listAdapter.getItem(position);
                zoff.vote(selectedVideo);


                return true;
            }
        });

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.HOLD_TO_VOTE);


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
                homePressed = false;
                break;
            case (R.id.action_skip):
                zoff.skip();

                break;
            case (R.id.action_togglePlay):
                togglePlay();
                break;
            case (R.id.action_shuffle):

          zoff.shuffle();
                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        player.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopNotificationService();

        if (player == null) {
            player = new YouTube_Player(this);

        } else {
            player.loadVideos(zoff.getVideoIDs());

        }


    }


    public void onZoffRefreshed() {



        listAdapter.notifyDataSetChanged();

        titleLabel.setText(zoff.getNowPlayingTitle());
        //currentTimeLabel.setText(zoff.getViewersCount());


        setBackgroundImage(zoff.getNowPlayingVideoID());
        /*
        if (!ImageCache.has(zoff.getNowPlayingVideoID() + "_blur") && ImageCache.has(zoff.getNowPlayingVideoID())){
            ImageBlur.createAndSetBlurBG(ImageCache.get(zoff.getNowPlayingVideoID()), this, zoff.getNowPlayingVideoID());
        } else if (ImageCache.has(zoff.getNowPlayingVideoID()+"_blur")){
            setBackgroundImage(ImageCache.getCurrentBlurBG());
        } else {
            ImageCache.registerImageListener(this,zoff.getNowPlayingVideoID());
            ImageDownload.downloadToCache(zoff.getNowPlayingVideoID());

        }


        //play next video if current playing != zoff-currentplaying
        if (!NOW_PLAYING_ID.equals(zoff.getNowPlayingVideoID()) && !NOW_PLAYING_ID.equals("")) {
            //videoEnded();
            player.loadVideos(zoff.getVideoIDs());

            player.next();
            NOW_PLAYING_ID = zoff.getNowPlayingVideoID();
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
            NOW_PLAYING_ID = zoff.getNowPlayingVideoID();
            handler.removeCallbacks(r, this);
        } else {

            handler.postDelayed(r, 100);


        }
    }


    public void videoEnded() {

        zoff.skip();
        //zoff.refreshData();


    }

    public void videoError(YouTubePlayer.ErrorReason errorReason) {
        switch (errorReason) {
            case NOT_PLAYABLE:

                videoEnded();
                ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.EMBEDDING_DISABLED,zoff.getNowPlayingTitle());

                break;
        }

    }

    public void playerInitialized() {
        loadFirstVideo();

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

    @Override
    public void imageInCache(Bitmap bitmap) {
        ImageBlur.createAndSetBlurBG(bitmap,this,zoff.getNowPlayingVideoID());
    }




}
*/
