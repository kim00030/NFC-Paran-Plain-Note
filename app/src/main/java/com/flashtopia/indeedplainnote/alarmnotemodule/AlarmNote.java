
/*
*
* This is POJO class that stores information being used for making alarm note from DB
*
* */
package com.flashtopia.indeedplainnote.alarmnotemodule;


public class AlarmNote {

    //NoteId that doing note alarm
    private int noteId;

    //for time where alarm note will be triggered in form of milliseconds
    private long timeInMills;
    //message in alarm note
    private String notableText;
    //alert request code generated when you set the time. see@setAlarm()method in NoteAlarmHandler class
    private int alertRequestCode;

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getNotableText() {
        return notableText;
    }

    public void setNotableText(String notableText) {
        this.notableText = notableText;
    }

    public long getTimeInMills() {

        return timeInMills;
    }

    public void setTimeInMills(long timeInMills) {

        this.timeInMills = timeInMills;
    }

    public int getAlertRequestCode() {
        return alertRequestCode;
    }

    public void setAlertRequestCode(int alertRequestCode) {
        this.alertRequestCode = alertRequestCode;
    }

}
