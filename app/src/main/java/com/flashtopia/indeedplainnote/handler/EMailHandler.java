package com.flashtopia.indeedplainnote.handler;


import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.flashtopia.indeedplainnote.R;
import com.flashtopia.indeedplainnote.components.MyCustomToast;

/*
* This class is to manage for sending note by email
* */
public class EMailHandler {

    private static EMailHandler instance;

    public static EMailHandler getInstance(){

        if (instance == null){
            instance = new EMailHandler();
        }

        return instance;
    }
    /*
    * Method to send note by email
    * */
    public void send(Activity activity, String note){

        if (note.length()<=0){
            MyCustomToast.getInstance().show(activity, activity.getString(R.string.unable_to_open_email),Toast.LENGTH_SHORT);
        }else{

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, note);
            intent.setType("message/rfc822");
            //intent.setType("text/plain");

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                // FOLLOWING STATEMENT CHECKS WHETHER THERE IS ANY APP THAT CAN HANDLE OUR EMAIL INTENT
                activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.send_email_using)));
            }else{
                MyCustomToast.getInstance().show(activity,activity.getString(R.string.you_dont_have_app_for_email),Toast.LENGTH_LONG);
            }

        }
    }
}
