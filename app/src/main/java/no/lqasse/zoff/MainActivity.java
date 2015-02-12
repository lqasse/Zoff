package no.lqasse.zoff;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

import no.lqasse.zoff.Player.PlayerActivity;


public class MainActivity extends ActionBarActivity  {


    private String ZOFF_ACTIVECHANNELS_URL = "http://zoff.no/Proggis/php/activechannels.php";
    private AutoCompleteTextView acEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final getSuggestions suggestions = new getSuggestions();



        String[] input = {ZOFF_ACTIVECHANNELS_URL};
        suggestions.execute(input);





        acEditText = (AutoCompleteTextView) findViewById(R.id.acEditText);

        //Get room suggestions








        //initializeDebug();
        //initialize();

        final Toast t = Toast.makeText(this,"Name must be letters or digits ONLY", Toast.LENGTH_SHORT);



        acEditText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {

                    if (isValidRoom()){

                        initialize();
                    } else {
                        t.show();

                    }

                }
                return handled;
            }


        });







    }


    private void initialize(){

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        Intent i;
        if (checkBox.isChecked()){
            //Starter PLayerActivity
                i = new Intent(this, PlayerActivity.class);
        } else {
            //Starter RemoteActivity

            i = new Intent(this, RemoteActivity.class);
        }
        //EditText text = (EditText) findViewById(R.id.editText);


        i.putExtra("ROOM_NAME", acEditText.getText().toString());

        startActivity(i);

        /*

        new Player(this,text.getText().toString());
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
        */


    }


    private void initializeDebug(){

        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra("ROOM_NAME", "lqasse");

        startActivity(i);

        /*

        new Player(this,text.getText().toString());
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
        */


    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isValidRoom(){


        String room = acEditText.getText().toString();
        String r = room.replace(" ","");

        acEditText.setText(r);
        if (r.equals("")) {
            Toast.makeText(this,"Enter name",Toast.LENGTH_SHORT).show();
            return false;
        }

        for (char c:r.toCharArray()){
            if (!Character.isLetterOrDigit(c)){
                Toast.makeText(this,Character.toString(c) + " is not a valid character",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }



    private class getSuggestions extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();

            try {
                InputStream inputStream = null;
                BufferedReader r;

                String url = params[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
                inputStream = httpResponse.getEntity().getContent();
                if(inputStream != null){
                    r = new BufferedReader(new InputStreamReader(inputStream));
                    sb = new StringBuilder();
                    String line;
                    while((line = r.readLine()) !=null){
                        sb.append(line);

                    }



                }
            } catch (Exception e){
                throw new IllegalStateException();

            }



            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayAdapter<String> adapter;
            String[] activeRooms = new String[0];
            try{
                JSONArray array = new JSONArray(s);
                activeRooms = new String[array.length()];
                for (int i = 0;i<array.length();i++){
                    activeRooms[i] = array.get(i).toString();
                    adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, activeRooms);
                    acEditText.setAdapter(adapter);
                }


            } catch (Exception e){
                e.printStackTrace();
            }


        }
    }




}
