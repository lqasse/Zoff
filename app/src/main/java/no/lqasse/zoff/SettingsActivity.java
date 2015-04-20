package no.lqasse.zoff;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class SettingsActivity extends ZoffActivity {
    /*
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";
    private HashMap<String, Boolean> settings = new HashMap<>();
    private String password;
    private SharedPreferences sharedPreferences;
    private boolean homePressed = true;


    private CheckBox voteCB;
    private CheckBox addsongsCB;
    private CheckBox longsongsCB;
    private CheckBox frontpageCB;
    private CheckBox allvideosCB;
    private CheckBox removeplayCB;
    private CheckBox skipCB;
    private CheckBox shuffleCB;


    private Button postSettingsBtn;
    private EditText pwField;
    private ProgressBar progressBar;


    private Activity settingsActivity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        getSupportActionBar().setIcon(R.drawable.settings);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        Intent i = getIntent();
        Bundle b = i.getExtras();

        settings.put("vote", b.getBoolean("vote"));
        settings.put("addsongs", b.getBoolean("addsongs"));
        settings.put("longsongs", b.getBoolean("longsongs"));
        settings.put("frontpage", b.getBoolean("frontpage"));
        settings.put("allvideos", b.getBoolean("allvideos"));
        settings.put("removeplay", b.getBoolean("removeplay"));
        settings.put("skip",b.getBoolean("skip"));
        settings.put("shuffle",b.getBoolean("shuffle"));

        ROOM_NAME = b.getString("ROOM_NAME");
        password = getPassword();


        voteCB      = (CheckBox) findViewById(R.id.vote);
        addsongsCB = (CheckBox) findViewById(R.id.addsongs);
        longsongsCB = (CheckBox) findViewById(R.id.longsongs);
        frontpageCB = (CheckBox) findViewById(R.id.frontpage);
        allvideosCB = (CheckBox) findViewById(R.id.allvideos);
        removeplayCB = (CheckBox) findViewById(R.id.removeplay);
        skipCB = (CheckBox) findViewById(R.id.skip);
        shuffleCB  = (CheckBox) findViewById(R.id.shuffle);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        postSettingsBtn = (Button) findViewById(R.id.postSettingsBtn);
        pwField = (EditText) findViewById(R.id.pwEditText);


//Første er reversert pga lolzoff, husk å reversere når innstillinger settes

        voteCB.setChecked(!settings.get("vote")); //Reversed
        addsongsCB.setChecked(!settings.get("addsongs")); //Reversed
        longsongsCB.setChecked(settings.get("longsongs"));
        frontpageCB.setChecked(settings.get("frontpage"));
        allvideosCB.setChecked(settings.get("allvideos"));
        removeplayCB.setChecked(settings.get("removeplay"));
        skipCB.setChecked(settings.get("skip"));
        shuffleCB.setChecked(settings.get("shuffle"));

        if (!password.equals("")) {
            pwField.setText(password);
        }





        postSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = pwField.getText().toString();

                Boolean vote =       (!voteCB.isChecked());         //Reverse back to match zoff
                Boolean addsongs =   (!addsongsCB.isChecked());     //Reverse back to match zoff
                Boolean longsongs =  (longsongsCB.isChecked());
                Boolean frontpage =  (frontpageCB.isChecked());
                Boolean allvideos =  (allvideosCB.isChecked());
                Boolean removeplay = (removeplayCB.isChecked());
                Boolean skip =       (skipCB.isChecked());
                Boolean shuffle =    (shuffleCB.isChecked());

                //Server.postSettings(settingsActivity, password, vote, addsongs, longsongs, frontpage, allvideos, removeplay, skip, shuffle);

                progressBar.setVisibility(View.VISIBLE);
                postSettingsBtn.setEnabled(false);
            }
        });


        if (ImageCache.getCurrentBlurBG() != null){
            RelativeLayout settingsLayout = (RelativeLayout) findViewById(R.id.settingsLayout);
            settingsLayout.setBackground(new BitmapDrawable(getBaseContext().getResources(), ImageCache.getCurrentBlurBG()));

        }


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getPassword() {
        String password;
        sharedPreferences = getSharedPreferences(PREFS_FILE, 0);
        password = sharedPreferences.getString(ROOM_NAME, "");

        return password;

    }





    @Override
    protected void onUserLeaveHint() {
        if (homePressed){
            Log.d("Button", "HOME pressed");
            startNotificationService();
        } else {
            Log.d("BUtton", "BACK pressed");
        }
        homePressed = true;
        super.onUserLeaveHint();
    }
    */


}
