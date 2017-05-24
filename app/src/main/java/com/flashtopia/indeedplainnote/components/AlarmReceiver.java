package com.flashtopia.indeedplainnote.components;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.flashtopia.indeedplainnote.alarmnotemodule.AlarmNote;
import com.flashtopia.indeedplainnote.alarmnotemodule.AlarmNotesDataSource;
import com.flashtopia.indeedplainnote.handler.NoteAlarmHandler;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver{

    //flag to determine whether or not database is accessible
    private boolean dbAccessible = false;

    /*In manifest,
    I set
     <receiver
            android:name=".components.AlarmReceiver"
            android:enabled="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <!--For HTC Device-->
                <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
            </intent-filter>
        </receiver>
    * */
    private static final String BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
    private static final String BOOT_COMPLETE_FOR_HTC = "android.intent.action.QUICKBOOT_POWERON";

    @Override
    public void onReceive(Context context, Intent intent){

        /*If device is reboot*/
        if (BOOT_COMPLETE.equalsIgnoreCase(intent.getAction()) ||
             BOOT_COMPLETE_FOR_HTC.equalsIgnoreCase(intent.getAction())){
            try {
                /*Basically make sure that AlarmNote DB should be opened before accessing it
                * When device is reboot , app is not running so that DB is not available
                * */
                openAlarmNoteDatabase(context);
                //get alarm notes from AlarmNoteDB
                List<AlarmNote> alarmNoteList = AlarmNotesDataSource.getInstance().findAll();
                // if there is any alarm notes saved in DB
                if (alarmNoteList != null && alarmNoteList.size() > 0) {
                    dbAccessible = true;
                    NoteAlarmHandler.getInstance().executeUnreadAlarmNote(context, alarmNoteList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            try {
                dbAccessible = true;
                startAlarmService(
                        context,
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

    }
    /*method to create notification */
    private void startAlarmService(Context context, String msg,String msgText,
                                   String msgAlert, String alarmNoteId, int alertRequestCode) {
        //we need to make sure if alarm note DB is opened to delete method below.
        //because when app closes, the DB is also closed
        openAlarmNoteDatabase(context);

        //if database can be accessible AND deleting the current note in db is successful
        //SEND to Service
        if (dbAccessible && AlarmNotesDataSource.getInstance().delete(alertRequestCode)) {

            Intent service = new Intent(context,AlarmService.class);
            service.putExtra(NoteAlarmHandler.MESSAGE,msg);
            service.putExtra(NoteAlarmHandler.MESSAGE_TEXT,msgText);
            service.putExtra(NoteAlarmHandler.MESSAGE_ALERT,msgAlert);
            service.putExtra(NoteAlarmHandler.ALARM_NOTE_ID,alarmNoteId);
            service.putExtra(NoteAlarmHandler.ALARM_REQUEST_CODE, alertRequestCode);

            context.startService(service);

        }
    }
    /*method to open alarmNote database that stores unread notes*/
    private void openAlarmNoteDatabase(Context context){

        if (AlarmNotesDataSource.getInstance().getAlarmNotesDb() == null){
            AlarmNotesDataSource.getInstance().init(context);
        }
    }


}
