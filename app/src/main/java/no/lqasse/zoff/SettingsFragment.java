package no.lqasse.zoff;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import no.lqasse.zoff.Models.ZoffSettings;

/**
 * Created by lassedrevland on 01.05.15.
 */
public class SettingsFragment extends Fragment {

    private Listener activity;

    private TextView                passwordField;

    private Switch voteSwitch;
    private Switch addsongsSwitch;
    private Switch longsongsSwitch;
    private Switch frontpageSwitch;
    private Switch allvideosSwitch;
    private Switch removeplaySwitch;
    private Switch skipSwitch;
    private Switch shuffleSwitch;

    private TextView voteDescription;
    private TextView addsongsDescription;
    private TextView longsongsDescription;
    private TextView frontpageDescription;
    private TextView allvideosDescription;
    private TextView removeplayDescription;
    private TextView skipDescription;
    private TextView shuffleDescription;

    private String voteEnabled;
    private String addsongsEnabled;
    private String longsongsEnabled;
    private String frontpageEnabled;
    private String allvideosEnabled;
    private String removeplayEnabled;
    private String skipEnabled;
    private String shuffleEnabled;
    private String voteDisabled;
    private String addsongsDisabled;
    private String longsongsDisabled;
    private String frontpageDisabled;
    private String allvideosDisabled;
    private String removeplayDisabled;
    private String skipDisabled;
    private String shuffleDisabled;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View layout = inflater.inflate(R.layout.preferences_fragment,container,false);


        passwordField       = (TextView) layout.findViewById(R.id.passwordField);

        voteSwitch          = (Switch) layout.findViewById(R.id.vote);
        addsongsSwitch      = (Switch) layout.findViewById(R.id.addsongs);
        longsongsSwitch     = (Switch) layout.findViewById(R.id.longsongs);
        frontpageSwitch     = (Switch) layout.findViewById(R.id.frontpage);
        allvideosSwitch     = (Switch) layout.findViewById(R.id.allvideos);
        removeplaySwitch    = (Switch) layout.findViewById(R.id.removeplay);
        skipSwitch          = (Switch) layout.findViewById(R.id.skip);
        shuffleSwitch       = (Switch) layout.findViewById(R.id.shuffle);

        voteDescription     = (TextView) layout.findViewById(R.id.voteLabel);
        addsongsDescription = (TextView) layout.findViewById(R.id.addsongsLabel);
        longsongsDescription= (TextView) layout.findViewById(R.id.longsongsLabel);
        frontpageDescription= (TextView) layout.findViewById(R.id.frontpageLabel);
        allvideosDescription= (TextView) layout.findViewById(R.id.allvideosLabel);
        removeplayDescription=(TextView) layout.findViewById(R.id.removeplayLabel);
        skipDescription     = (TextView) layout.findViewById(R.id.skipLabel);
        shuffleDescription  = (TextView) layout.findViewById(R.id.shuffleLabel);

        voteSwitch.setEnabled(false);
        addsongsSwitch.setEnabled(false);
        allvideosSwitch.setEnabled(false);
        longsongsSwitch.setEnabled(false);
        frontpageSwitch.setEnabled(false);
        removeplaySwitch.setEnabled(false);
        skipSwitch.setEnabled(false);
        shuffleSwitch.setEnabled(false);

        voteEnabled         = getResources().getString(R.string.switch_vote_description_enabled);
        addsongsEnabled     = getResources().getString(R.string.switch_addsongs_description_enabled);
        longsongsEnabled    = getResources().getString(R.string.switch_longsongs_description_enabled);
        frontpageEnabled    = getResources().getString(R.string.switch_frontpage_description_enabled);
        allvideosEnabled    = getResources().getString(R.string.switch_allvideos_description_enabled);
        removeplayEnabled   = getResources().getString(R.string.switch_removeplay_description_enabled);
        skipEnabled         = getResources().getString(R.string.switch_skip_description_enabled);
        shuffleEnabled      = getResources().getString(R.string.switch_shuffle_description_enabled);

        voteDisabled        = getResources().getString(R.string.switch_vote_description_disabled);
        addsongsDisabled    = getResources().getString(R.string.switch_addsongs_description_disabled);
        longsongsDisabled   = getResources().getString(R.string.switch_longsongs_description_disabled);
        frontpageDisabled   = getResources().getString(R.string.switch_frontpage_description_disabled);
        allvideosDisabled   = getResources().getString(R.string.switch_allvideos_description_disabled);
        removeplayDisabled  = getResources().getString(R.string.switch_removeplay_description_disabled);
        skipDisabled        = getResources().getString(R.string.switch_skip_description_disabled);
        shuffleDisabled     = getResources().getString(R.string.switch_shuffle_description_disabled);


        //Listeners
        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO){
                    activity.savePassword(passwordField.getText().toString());
                }

                return true;
            }
        });




        return layout;



    }

    @Override
    public void onAttach(Activity activity) {
        this.activity = (Listener) activity;
        this.activity.setFragment(this);
        super.onAttach(activity);
    }

    public void enableSettings(){
        voteSwitch.setEnabled(true);
        addsongsSwitch.setEnabled(true);
        allvideosSwitch.setEnabled(true);
        longsongsSwitch.setEnabled(true);
        frontpageSwitch.setEnabled(true);
        removeplaySwitch.setEnabled(true);
        skipSwitch.setEnabled(true);
        shuffleSwitch.setEnabled(true);






        CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {



                ZoffSettings settings = new ZoffSettings.Builder()
                        .allowsAddsongs(addsongsSwitch.isChecked())
                        .allvideos(allvideosSwitch.isChecked())
                        .frontpage(frontpageSwitch.isChecked())
                        .longsongs(longsongsSwitch.isChecked())
                        .removeplay(removeplaySwitch.isChecked())
                        .shuffle(shuffleSwitch.isChecked())
                        .skip(skipSwitch.isChecked())
                        .vote(voteSwitch.isChecked())
                        .build();
                activity.saveSettings(settings);

                toggleSettingsLabels();


            }



        };

        voteSwitch.setOnCheckedChangeListener(changeListener);
        addsongsSwitch.setOnCheckedChangeListener(changeListener);
        allvideosSwitch.setOnCheckedChangeListener(changeListener);
        longsongsSwitch.setOnCheckedChangeListener(changeListener);
        frontpageSwitch.setOnCheckedChangeListener(changeListener);
        removeplaySwitch.setOnCheckedChangeListener(changeListener);
        skipSwitch.setOnCheckedChangeListener(changeListener);
        shuffleSwitch.setOnCheckedChangeListener(changeListener);

    }

    public void setSettings(ZoffSettings settings){

        voteSwitch.setChecked(          settings.isVote());
        addsongsSwitch.setChecked(      settings.isAddsongs());
        allvideosSwitch.setChecked(     settings.isAllvideos());
        longsongsSwitch.setChecked(     settings.isLongsongs());
        frontpageSwitch.setChecked(     settings.isFrontpage());
        skipSwitch.setChecked(          settings.isSkip());
        shuffleSwitch.setChecked(       settings.isShuffle());
        removeplaySwitch.setChecked(    settings.isRemoveplay());


        toggleSettingsLabels();
    }

    private void toggleSettingsLabels(){

        if (addsongsSwitch.isChecked()){
            addsongsDescription.setText(addsongsEnabled);
        } else {
            addsongsDescription.setText(addsongsDisabled);
        }
        if (voteSwitch.isChecked()){
            voteDescription.setText(voteEnabled);
        } else {
            voteDescription.setText(voteDisabled);
        }

        if (shuffleSwitch.isChecked()){
            shuffleDescription.setText(shuffleEnabled);
        } else {
            shuffleDescription.setText(shuffleDisabled);
        }

        if (skipSwitch.isChecked()){
            skipDescription.setText(skipEnabled);
        } else {
            skipDescription.setText(skipDisabled);
        }

        if (longsongsSwitch.isChecked()){
            longsongsDescription.setText(longsongsEnabled);
        } else {
            longsongsDescription.setText(longsongsDisabled);
        }

        if (allvideosSwitch.isChecked()){
            allvideosDescription.setText(allvideosEnabled);
        } else {
            allvideosDescription.setText(allvideosDisabled);
        }

        if (frontpageSwitch.isChecked()){
            frontpageDescription.setText(frontpageEnabled);
        } else {
            frontpageDescription.setText(frontpageDisabled);
        }

        if (removeplaySwitch.isChecked()){
            removeplayDescription.setText(removeplayEnabled);
        } else {
            removeplayDescription.setText(removeplayDisabled);
        }


    }

    public interface Listener {
        void savePassword(String password);
        void setFragment(SettingsFragment fragment);
        void saveSettings(ZoffSettings settings);

    }
}
