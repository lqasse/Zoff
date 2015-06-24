package no.lqasse.zoff.Helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
        VIDEO_DELETED,
        VIDEO_VOTED,
        HAS_SKIPPED,
        HAS_VOTED,
        HOLD_TO_VOTE,
        HOLD_TO_ADD,
        HOLD_TO_DELETE,
        SHUFFLED,
        EMBEDDING_DISABLED,
        PAUSE,
        STOP,
        SKIPPED,
        NO_SKIP,
        WRONG_PASSWORD,
        SAVED_SETTINGS,
        PROTECTED_LIST,


    }



    public static void showToast(Object context,TYPE type){
        showToast(context,type,"");

    }


    public static void showToast(Object context, String type){
        String toastText = "";

        if (type.contains("more are needed to skip")){
            toastText = type;
            show(((Activity)context), type, null);
        }

        Drawable toastIcon = null;

        switch (type){
            case "addedsong":

                //Do nothing handled locally
                break;
            case "savedsettings":


                showToast(context, TYPE.SAVED_SETTINGS);
                break;
            case "wrongpass":
                showToast(context, TYPE.WRONG_PASSWORD);
                break;
            case "shuffled":
                showToast(context, TYPE.SHUFFLED);

                break;
            case "deletesong":

                //Do nothing handled locally

                break;
            case "voted":
                //Do nothing handle locally
                break;
            case "alreadyvoted":
                showToast(context, TYPE.HAS_VOTED);
                break;
            case "listhaspass":
                showToast(context, TYPE.PROTECTED_LIST);
                break;
            case "noskip":

                showToast(context, TYPE.NO_SKIP);
                break;
            case "alreadyskip":
                showToast(context, TYPE.HAS_SKIPPED);

                break;
            case "skip":


        }














    }

    public static void showToast(Object context, TYPE type, String CONTEXTUAL_STRING){



        Toast t;
        String toastText = "Toast error";
        Drawable toastIcon = null;

        switch (type){


            case NEEDS_PASS_TO_VOTE:
                toastText = "This room is password protected, set a password to vote";
                break;
            case NEEDS_PASS_TO_ADD:
                toastText = "This room is password protected, set a password to add videos";
                break;
            case VIDEO_ADDED:
                toastText = CONTEXTUAL_STRING + " was added";
                toastIcon = ((Activity)context).getResources().getDrawable(R.drawable.plus);
                break;
            case VIDEO_VOTED:
                toastText = "+1 to " + CONTEXTUAL_STRING;
                toastIcon = ((Activity)context).getResources().getDrawable(R.drawable.plus);
                break;
            case HOLD_TO_VOTE:
                toastText = "Tap and hold to vote";
                break;
            case SHUFFLED:
                toastText = "Shuffled!";
                toastIcon = ((Activity) context).getResources().getDrawable(R.drawable.shuffle);
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

            case STOP:
                toastText = "Stopped";
                break;
            case PAUSE:
                toastText = "Paused";
                break;

            case SAVED_SETTINGS:
                toastText = "Saved settings";
                break;
            case WRONG_PASSWORD:
                toastText = "Incorrect password";
                break;
            case VIDEO_DELETED:
                toastText = CONTEXTUAL_STRING + " was deleted";
                toastIcon = ((Activity)context).getResources().getDrawable(R.drawable.cross);
                break;
            case HAS_SKIPPED:
                toastIcon = ((Activity)context).getResources().getDrawable(R.drawable.cross);
                toastText ="You've already voted to skip";
                break;
            case HAS_VOTED:
                toastText="You've already voted on that video";
                toastIcon = ((Activity)context).getResources().getDrawable(R.drawable.cross);
                break;
            case SKIPPED:
                toastText = "Skipped";
                toastIcon = ((Activity) context).getResources().getDrawable(R.drawable.skip);
                break;
            case NO_SKIP:
                toastText="Only admin can skip songs on this channel";
                break;
            case PROTECTED_LIST:
                toastText="Only admin can skip songs on this channel";
                break;





        }

        if (context instanceof Activity) {


                show((Activity) context,toastText,toastIcon);


        } else {
            Log.d("TOAST", "No COntext");
        }


    }


    private static void show(Context context, String text, Drawable icon){
        Toast t = new Toast(context);


        t.setGravity(Gravity.TOP|Gravity.LEFT, 60, 210);

        View toast  =((Activity) context).getLayoutInflater().inflate(R.layout.toast, null);

        t.setView(toast);
        ((TextView) toast.findViewById(R.id.toastTitle)).setText(text);
        ((ImageView) toast.findViewById(R.id.toastIcon)).setImageDrawable(icon);

        t.show();


    }
}
