package no.lqasse.zoff.Helpers;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 24.03.15.
 */
public class ToastMaster {

    public enum TYPE{
        NEEDS_PASS_TO_VOTE,
        NEEDS_PASS_TO_ADD,
        NEEDS_PASS_TO_SHUFFLE,
        VIDEO_ADDED,
        HOLD_TO_VOTE,
        SHUFFLED,
        EMBEDDING_DISABLED,
        VIDEO_VOTED,
        HOLD_TO_ADD,
        HOLD_TO_DELETE,
        BACKGROUND,
        FOREGROUND,
        PAUSE,
        STOP,
        SKIP_DISABLED,
        SHUFFLING_DISABLED,
        WRONG_PASSWORD_SETTINGS,
        SAVED_SETTINGS,
        VIDEO_DELETED
    }



    public static void showToast(Object context,TYPE type){
        showToast(context,type,"");

    }


    public static void showToast(Object context, String type){
        String toastText = "";

        switch (type){
            case "savedsettings":
                toastText="Saved settings";
                break;
            case "wrongpass":
                toastText="Wrong password";
                break;
            case "shuffled":
                toastText="Shuffled playlist";
                break;
            case "deletesong":
                toastText="Deleted song";
                break;
            case "voted":
                toastText="Voted on video";
                break;
            case "alreadyvoted":
                toastText="You've already voted on that video";
                break;
            case "listhaspass":
                toastText="The list is password protected";
                break;
            case "noskip":
                toastText="Only admin can skip songs on this channel";
                break;
            case "alreadyskip":
                toastText="You've already voted to skip!";
                break;
        }



        Toast t = Toast.makeText((Activity) context, toastText, Toast.LENGTH_SHORT);
        View v = t.getView();
        v.setBackgroundResource(R.drawable.toast_background);

        t.setGravity(Gravity.CENTER_VERTICAL&Gravity.CENTER_HORIZONTAL,50,0);
        t.show();




    }

    public static void showToast(Object context, TYPE type, String CONTEXTUAL_STRING){



        Toast t;
        String toastText = "Toast error";

        switch (type){
            case NEEDS_PASS_TO_VOTE:
                toastText = "This room is password protected, set a password to vote";
                break;
            case NEEDS_PASS_TO_ADD:
                toastText = "This room is password protected, set a password to add videos";
                break;
            case VIDEO_ADDED:
                toastText = CONTEXTUAL_STRING + " was added";
                break;
            case VIDEO_VOTED:
                toastText = "+1 to " + CONTEXTUAL_STRING;
                break;
            case HOLD_TO_VOTE:
                toastText = "Tap and hold to vote";
                break;
            case SHUFFLED:
                toastText = "Shuffled!";
                break;
            case NEEDS_PASS_TO_SHUFFLE:
                toastText = "This room is password protected, set a password to shuffle";
                break;
            case EMBEDDING_DISABLED:
                toastText = CONTEXTUAL_STRING + " could not be played, embedded playback disabled.";
                break;
            case HOLD_TO_ADD:
                toastText = "Click and hold to add videos";
                break;
            case HOLD_TO_DELETE:
                toastText = "Tap and hold to delete";
                break;
            case BACKGROUND:
                toastText ="WENT TO BACKGROUND";
                break;
            case FOREGROUND:
                toastText = "Came to foreground";
                break;
            case STOP:
                toastText = "Stopped";
                break;
            case PAUSE:
                toastText = "Paused";
                break;
            case SKIP_DISABLED:
                toastText = "Skipping is disabled for this channel";
                break;
            case SHUFFLING_DISABLED:
                toastText = "Shuffling is disabled for this channel";
                break;
            case SAVED_SETTINGS:
                toastText = "Saved";
                break;
            case WRONG_PASSWORD_SETTINGS:
                toastText = "Incorrect password";
                break;
            case VIDEO_DELETED:
                toastText = CONTEXTUAL_STRING + " was deleted";
                break;



        }

        if (context instanceof Activity) {

            t = Toast.makeText((Activity) context, toastText, Toast.LENGTH_SHORT);
            View v = t.getView();
            v.setBackgroundResource(R.drawable.toast_background);

            t.setGravity(Gravity.CENTER_VERTICAL&Gravity.CENTER_HORIZONTAL,50,0);
            t.show();
        } else {
            Log.d("TOAST", "No COntext");
        }


    }
}
