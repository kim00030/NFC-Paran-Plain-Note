package com.flashtopia.indeedplainnote.handler;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;

import com.flashtopia.indeedplainnote.DBOpenHelper;
import com.flashtopia.indeedplainnote.MainActivity;
import com.flashtopia.indeedplainnote.NotesProvider;
import com.flashtopia.indeedplainnote.R;

public class UpdateNoteIconHandler {

    private static UpdateNoteIconHandler instance;

    private Activity activity;

    private ContentResolver contentResolver;

    public static UpdateNoteIconHandler getInstance(){

        if (instance == null){

            instance = new UpdateNoteIconHandler();
        }
        return instance;
    }

    public void init(Activity activity){

        this.activity = activity;
        this.contentResolver = activity.getContentResolver();
    }
    /*method to check if launcher activity is running*/
    public boolean launcherIsRunning(){
        return (this.activity != null);
    }

    /*
   * method to set note icon back after alarm icon shows in note alarm set
   * */
    public void setNoteIconBack(final String alarmNoteId){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    //where clause to access to proper row in Main DB
                    String noteFilter = DBOpenHelper.NOTE_ID + "=" + alarmNoteId;
                    ContentValues values = new ContentValues();
                    values.put(DBOpenHelper.NOTE_ICON, R.drawable.ic_tag_text);
                    values.put(DBOpenHelper.NOTE_ALARM_DATE, "");

                    contentResolver.update(NotesProvider.CONTENT_URI, values, noteFilter, null);
                    ((MainActivity)activity).restartLoader();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setAlarmNoteIcon(String date, String noteFilter){

        try {
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.NOTE_ICON, R.drawable.ic_action_alarmed_note);
            values.put(DBOpenHelper.NOTE_ALARM_DATE,date);
            contentResolver.update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
