/*
* This class handles note alarming
* */

package com.flashtopia.indeedplainnote.handler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.TimePicker;

import com.flashtopia.indeedplainnote.DBOpenHelper;
import com.flashtopia.indeedplainnote.R;
import com.flashtopia.indeedplainnote.alarmnotemodule.AlarmNote;
import com.flashtopia.indeedplainnote.alarmnotemodule.AlarmNotesDataSource;
import com.flashtopia.indeedplainnote.components.AlarmReceiver;
import com.flashtopia.indeedplainnote.components.CustomTimePickerDialog;
import com.flashtopia.indeedplainnote.interfaces.INoteAlarmCallback;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NoteAlarmHandler {
    //keys for intent to be send to AlarmReceiver
    public  static final String MESSAGE = "MESSAGE";
    public  static final String MESSAGE_TEXT = "MESSAGE_TEXT";
    public  static final String MESSAGE_ALERT = "MESSAGE_ALERT";
    public  static final String ALARM_REQUEST_CODE = "ALARM_REQUEST_CODE";
    public  static final String ALARM_NOTE_ID = "ALARM_NOTE_ID";

    private static NoteAlarmHandler instance;
    //reference of activity that calls this handler
    private Activity activity;
    //for note to be send for alarm
    private String msgContent;
    //for keeping language set in device
    private String language;
    //for showing time that alarming alert occurs
    private AlertDialog alertDialog;
    //id of note that attempts to set to be alarm note
    private String alarmNoteId;

    private INoteAlarmCallback iNoteAlarmCallback;

    public static NoteAlarmHandler getInstance(){

        if (instance == null){

            instance = new NoteAlarmHandler();
        }
        return instance;
    }

    public void registerNoteAlarmCallback(INoteAlarmCallback iNoteAlarmCallback){
        this.iNoteAlarmCallback = iNoteAlarmCallback;
    }
    /**
    * Method to open TimePickerDialog
    * @param activity => activity that calls this NoteAlarmHandler
    * @param msgContent => note to be set as alarming alert
    * */
    public void openTimePickerDialog(Activity activity, String alarmNoteId,String msgContent){

        this.activity = activity;
        this.msgContent = msgContent;
        this.alarmNoteId = alarmNoteId;

        Calendar calendar = Calendar.getInstance();

        //set style depending on android version
        int androidVersion = Build.VERSION.SDK_INT;
        int dialogStyleResId = androidVersion >= Build.VERSION_CODES.LOLLIPOP ? R.style.StyledDialog:0;

        //to allow user to set time for alarming alert
        CustomTimePickerDialog timePickerDialog = new CustomTimePickerDialog(
                activity,
                dialogStyleResId,
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        timePickerDialog.show();

    }
    /*
    * Listener for time set
    * */
    private final CustomTimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            //To prevent onTimeSet calls even if cancel clicks. That's android bug
            if (timePicker.isShown()){
                //get language  set in device
                language = getLanguageSet();

                Calendar calNow = Calendar.getInstance(new Locale(language));
                Calendar calSet  = (Calendar) calNow.clone();
                calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calSet.set(Calendar.MINUTE, minute);
                calSet.set(Calendar.SECOND, 0);
                calSet.set(Calendar.MILLISECOND, 0);

                if ( calSet.compareTo(calNow) <= 0) {
                    // Today Set time passed, count to tomorrow
                   calSet.add(Calendar.DATE, 1);
                }
                openDialogForConfirmation(calSet);
            }
        }
    };
    /*
    *
    * To open dialog for confirming alarm alert set
    * */
    private String date;
    private void openDialogForConfirmation(Calendar pCalSet){

        final Calendar calSet = pCalSet;
        //date format
        date = new java.text.SimpleDateFormat(this.activity.getString(R.string.alarm_date),java.util.Locale.getDefault()).format(calSet.getTime());

        //*Korean and Japanese Date format is different from english so ..*//*
        String message;
        switch(language){

            case FontLoadHandler.KOREAN:
            case FontLoadHandler.JAPANESE:
                message = date.concat(this.activity.getString(R.string.note_will_be_alarmed_on));
                break;
            default:
                message = this.activity.getString(R.string.note_will_be_alarmed_on).concat(date);
        }



        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.activity);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(this.activity.getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //set alarm
                setAlarm(calSet,date);
            }
        });

        alertDialogBuilder.setNegativeButton(this.activity.getString(android.R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    /*
    * To set alarm
    * */
    private void setAlarm(Calendar targetCal, String date) {

        final int alertRequestCode = (int) System.currentTimeMillis();
        //********************************************save in AlarmNoteDB
        saveInAlarmNoteDB(this.alarmNoteId, this.msgContent, targetCal.getTimeInMillis(), alertRequestCode);

        Intent alertIntent = new Intent(this.activity, AlarmReceiver.class);
        alertIntent.putExtra(MESSAGE, activity.getString(R.string.my_note));
        alertIntent.putExtra(ALARM_NOTE_ID,this.alarmNoteId);//this.alarmNoteId is String type here
        alertIntent.putExtra(MESSAGE_TEXT, this.msgContent);
        alertIntent.putExtra(MESSAGE_ALERT, activity.getString(R.string.alert));
        alertIntent.putExtra(ALARM_REQUEST_CODE, alertRequestCode);

        AlarmManager alarmManager = (AlarmManager) this.activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), PendingIntent.getBroadcast(this.activity, alertRequestCode, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        //send back to EditorActivity for case of small screen or
        //to EditorFragment for large screen
        iNoteAlarmCallback.noteAlarmNotify(date);
    }
    /*
    * Different version of setAlarm() method that calls from MainActivity for situation of reboot
    * */
    @SuppressWarnings("WeakerAccess")
    public void setAlarm(Context context,Calendar targetCal,int noteId,String msgContent,int alertRequestCode){

        //set alarm below
        Intent alertIntent = new Intent(context, AlarmReceiver.class);
        alertIntent.putExtra(MESSAGE, context.getString(R.string.my_note));
        alertIntent.putExtra(ALARM_NOTE_ID,String.valueOf(noteId));
        alertIntent.putExtra(MESSAGE_TEXT, msgContent);
        alertIntent.putExtra(MESSAGE_ALERT, context.getString(R.string.alert));
        alertIntent.putExtra(ALARM_REQUEST_CODE, alertRequestCode);
        //when time comes, AlarmReceiver will catch it
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), PendingIntent.getBroadcast(context, alertRequestCode, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

        /*
        To get language set in device
    * */
    public String getLanguageSet()  {

        return java.util.Locale.getDefault().toString();
    }
    /*
    *
    * method to save alarm note in DB in order to trigger it even if device is reboot
    * */
    private void saveInAlarmNoteDB(String alarmNoteId,String note,long timeInMills,int alertRequestCode){

        AlarmNote alarmNote = new AlarmNote();
        alarmNote.setNoteId(Integer.valueOf(alarmNoteId));// change it to integer
        alarmNote.setNotableText(note);
        alarmNote.setTimeInMills(timeInMills);
        alarmNote.setAlertRequestCode(alertRequestCode);

        AlarmNotesDataSource.getInstance().insert(alarmNote);
    }
    /*
    * method to check if note has already set alarm
    * */
    public boolean hasAlreadySetAlarmed(Uri uri, ContentResolver contentResolver, String noteFilter){

        int iconId = 0;
        try {
            Cursor cursor = contentResolver.query(uri, DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            iconId = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_ICON));
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //true if note has already set alarm, otherwise false
        return (iconId == R.drawable.ic_action_alarmed_note);
    }
    /*method to execute unread alarm notes*/
    public void executeUnreadAlarmNote(Context context,List<AlarmNote> alarmNoteList){

        for (AlarmNote alarmNote:alarmNoteList){
            //create Calendar object and set the time in mill that represents alarm - time
            Calendar targetCal = Calendar.getInstance(new Locale(getLanguageSet()));
            targetCal.setTimeInMillis(alarmNote.getTimeInMills());
            //reset alarm
            setAlarm(context,targetCal,alarmNote.getNoteId(),alarmNote.getNotableText(),alarmNote.getAlertRequestCode());

        }
    }


}
