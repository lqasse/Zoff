package no.lqasse.zoff;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class SettingsActivity extends ActionBarActivity {
    private HashMap<String,Boolean> settings = new HashMap<>();
    private String PASS;
    private String ROOM_NAME;
    private String PHP_URL;
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";

    private SharedPreferences sharedPreferences;


    private CheckBox voteCB;
    private CheckBox addsongsCB;
    private CheckBox longsongsCB;
    private CheckBox frontpageCB;
    private CheckBox allvideosCB;
    private CheckBox removeplayCB;

    private Button postSettingsBtn;
    private EditText pwField;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent i = getIntent();
        Bundle b = i.getExtras();

        settings.put("vote", b.getBoolean("vote"));
        settings.put("addsongs", b.getBoolean("addsongs"));
        settings.put("longsongs", b.getBoolean("longsongs"));
        settings.put("frontpage", b.getBoolean("frontpage"));
        settings.put("allvideos", b.getBoolean("allvideos"));
        settings.put("removeplay", b.getBoolean("removeplay"));

        ROOM_NAME = b.getString("ROOM_NAME");
        PHP_URL = "http://zoff.no/"+ROOM_NAME+"/php/change.php";
        PASS = getPASS();

        voteCB = (CheckBox) findViewById(R.id.vote);
        addsongsCB = (CheckBox) findViewById(R.id.addsongs);
        longsongsCB = (CheckBox) findViewById(R.id.longsongs);
        frontpageCB = (CheckBox) findViewById(R.id.frontpage);
        allvideosCB = (CheckBox) findViewById(R.id.allvideos);
        removeplayCB = (CheckBox) findViewById(R.id.removeplay);

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

        if (!PASS.equals("")){
            pwField.setText(PASS);
        }




        postSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PASS = pwField.getText().toString();
                String[] input =  {PHP_URL};

                postSettings postSettings = new postSettings();


                postSettings.execute(input);
                progressBar.setVisibility(View.VISIBLE);
                postSettingsBtn.setEnabled(false);
            }
        });









    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra("ROOM_NAME",ROOM_NAME);
        startActivity(i);
    }

    private class postSettings extends AsyncTask<String,Void,String>{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            postSettingsBtn.setEnabled(true);


            if (s.contains("correct")){
                Toast.makeText(getBaseContext(),"Saved!",Toast.LENGTH_SHORT).show();
                setPass(PASS);


            } else {
                Toast.makeText(getBaseContext(),"Wrong password, try again!",Toast.LENGTH_SHORT).show();

            }



        }

        @Override
        protected String doInBackground(String... params) {

            String vote = Boolean.toString(!voteCB.isChecked()); //Reverse back to match zoff
            String addsongs = Boolean.toString(!addsongsCB.isChecked());//Reverse back to match zoff
            String longsongs = Boolean.toString(longsongsCB.isChecked());
            String frontpage = Boolean.toString(frontpageCB.isChecked());
            String allvideos = Boolean.toString(allvideosCB.isChecked());
            String removeplay = Boolean.toString(removeplayCB.isChecked());



            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(params[0]);

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(8);
                nameValuePairs.add(new BasicNameValuePair("conf","start"));
                nameValuePairs.add(new BasicNameValuePair("vote",vote));
                nameValuePairs.add(new BasicNameValuePair("addsongs",addsongs));
                nameValuePairs.add(new BasicNameValuePair("longsongs",longsongs));
                nameValuePairs.add(new BasicNameValuePair("frontpage",frontpage));
                nameValuePairs.add(new BasicNameValuePair("allvideos",allvideos));
                nameValuePairs.add(new BasicNameValuePair("removeplay",removeplay));
                nameValuePairs.add(new BasicNameValuePair("pass",pwField.getText().toString()));



                httppost.setEntity((new UrlEncodedFormEntity(nameValuePairs, "UTF-8")));
                httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
                //.setEntity(new StringEntity("conf=start&vote=false&addsongs=false&longsongs=true&frontpage=true&allvideos=true&removeplay=false&pass=admin","UTF-8"));

                //Execute!
                HttpResponse response = httpClient.execute(httppost);
                HttpEntity resEntity = response.getEntity();
                if (resEntity!=null){
                    return EntityUtils.toString(resEntity);


                }
                return "";



            } catch (Exception e){
                e.printStackTrace();
                throw new IllegalStateException();
            }







        }
    }

    private String getPASS(){
        String PASS;
        sharedPreferences = getSharedPreferences(PREFS_FILE,0);
        PASS = sharedPreferences.getString(ROOM_NAME,"");

        return PASS;

    }

    private void setPass(String PASS){
        sharedPreferences = getSharedPreferences(PREFS_FILE,0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ROOM_NAME,PASS);
        editor.commit();
    }








}
