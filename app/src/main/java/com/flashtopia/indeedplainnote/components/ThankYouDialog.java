
/*This is Alert dialog for welcoming to user. only one time must be shown up.
* */
package com.flashtopia.indeedplainnote.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.flashtopia.indeedplainnote.R;
import com.flashtopia.indeedplainnote.handler.FontLoadHandler;
import com.flashtopia.indeedplainnote.handler.NoteAlarmHandler;

public class ThankYouDialog extends AlertDialog.Builder{

    public static final String SHOWING_PREFERENCES = "showing preference";
    public static final String SHOWING_STATUS = "showing status";

    public ThankYouDialog(final Context context) {
        super(context);

        String message;
        String language = NoteAlarmHandler.getInstance().getLanguageSet();
        switch (language){
              
            case FontLoadHandler.KOREAN:
            case FontLoadHandler.JAPANESE:
                message = context.getString(R.string.msg_prefix_in_thank_you_dialog) +
                          context.getString(R.string.msg_postfix_in_thank_you_dialog);
                break;

            default:
                /*English*/
                message = context.getString(R.string.msg_prefix_in_thank_you_dialog) + context.getString(R.string.app_name) +
                          context.getString(R.string.msg_postfix_in_thank_you_dialog);
        }
          
        setTitle(context.getString(R.string.app_name));//display 'app name'
        setIcon(R.mipmap.ic_launcher);
        setMessage(message);
        setCancelable(false);/*prevent dismissing it when you click on outside*/
        setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /*save showing status in order to show only one time after it installs on device*/
                SharedPreferences showingPref = context.getSharedPreferences(SHOWING_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = showingPref.edit();
                editor.putBoolean(SHOWING_STATUS, false);
                editor.apply();
            }
        });

    }
}
