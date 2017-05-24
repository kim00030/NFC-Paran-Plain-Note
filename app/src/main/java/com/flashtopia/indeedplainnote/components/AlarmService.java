/*
* This is Service class that handles note alarms
 *
 * once user set the alarm on note, it will trigger at set of alarm time
 * and AlarmReceiver receive it and send it to here
* */
package com.flashtopia.indeedplainnote.components;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.flashtopia.indeedplainnote.DBOpenHelper;
import com.flashtopia.indeedplainnote.MainActivity;
import com.flashtopia.indeedplainnote.R;
import com.flashtopia.indeedplainnote.handler.NoteAlarmHandler;
import com.flashtopia.indeedplainnote.handler.UpdateNoteIconHandler;

public class AlarmService extends Service{

    private DBOpenHelper dbOpenHelper;

   @Override
    public void onCreate() {

        super.onCreate();
        openNoteDatabase();

    }
    /*
    * method to open DB that note user saved stored in.
    * Why need? This is running even though app is closed.
    * App closed or device reboot means DB is available
    * */
    private SQLiteDatabase openNoteDatabase() {
        if (dbOpenHelper == null){
            dbOpenHelper = new DBOpenHelper(this.getApplicationContext());
        }
        return dbOpenHelper.getWritableDatabase();
    }

   @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        try {
            createNotification(
                    this.getApplicationContext(),
                    intent.getStringExtra(NoteAlarmHandler.MESSAGE),
                    intent.getStringExtra(NoteAlarmHandler.MESSAGE_TEXT),
                    intent.getStringExtra(NoteAlarmHandler.MESSAGE_ALERT),
                    intent.getStringExtra(NoteAlarmHandler.ALARM_NOTE_ID),
                    intent.getIntExtra(NoteAlarmHandler.ALARM_REQUEST_CODE, -1)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*method to create notification */
    private void createNotification(final Context context, final String msg, final String msgText,
                                    final String msgAlert,final String alarmNoteId,
                                    final int alertRequestCode) {

        PendingIntent notificIntent;
        Intent intent = new Intent(context, MainActivity.class);

        /*If launcher Activity which is MainActivity in my case is running,send the current id of note alarm to launcher activity
        * to set Note icon back
        * */
        if (UpdateNoteIconHandler.getInstance().launcherIsRunning()){
            UpdateNoteIconHandler.getInstance().setNoteIconBack(alarmNoteId);
            //Do not open activity when user clicks on alarm note
            notificIntent = PendingIntent.getService(context, 0, intent, 0);

        }else{//case when app closed. App is not running

            /*get note database to update note icon back*/
            SQLiteDatabase database = openNoteDatabase();
            String noteFilter = DBOpenHelper.NOTE_ID + "=" + alarmNoteId;
            ContentValues values = new ContentValues();
            values.put(DBOpenHelper.NOTE_ICON, R.drawable.ic_tag_text);
            values.put(DBOpenHelper.NOTE_ALARM_DATE, "");
            database.update(DBOpenHelper.TABLE_NOTES, values, noteFilter, null);
            //open Activity when user clicks on note alarm
            notificIntent = PendingIntent.getActivity(context, 0, intent, 0);
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText);


        mBuilder.setContentIntent(notificIntent);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(alertRequestCode, mBuilder.build());

    }

}
