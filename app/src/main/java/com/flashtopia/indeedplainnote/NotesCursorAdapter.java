package com.flashtopia.indeedplainnote;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/*
*The CursorAdapter class is in the package android.widget
* and it's the super class of the SimpleCursorAdapter
*
* I have some methods I need to override, which are newView and bindView
*
* */

class NotesCursorAdapter extends CursorAdapter {

    public NotesCursorAdapter(Context context) {
        super(context, null, 0);

    }

    /*
    * It returns a view, and that view will be created based on the layout that defines the list item display.
    * And that will be my custom list item layout, note_list_item. So in the newView method,
    * I'll use this statement, return LayoutInflater.from.
    *
    * */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.note_list_item, parent, false);
    }

    /*
    *
    * When you bind the view, you receive an instance of the cursor object, and
    * it will already point to the particular row of your database that's supposed to be displayed.
    * */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String noteText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
        int imageId = cursor.getInt(cursor.getColumnIndex(DBOpenHelper.NOTE_ICON));
        /*alarming date stored in DB after user set to alarm
        * Default value is null or empty string("")
        * */
        String alarmDate = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_ALARM_DATE));

        //find line feed character, ASCII code is 10
        int pos = noteText.indexOf(10);
        if (pos != -1) {
            noteText = noteText.substring(0, pos) + "...";
        }
        TextView noteTv = (TextView) view.findViewById(R.id.tvNote);
        noteTv.setText(noteText);
        //load image by code
        ImageView iv = (ImageView) view.findViewById(R.id.imageDocIcon);
        iv.setImageResource(imageId);
        /*TextView for alarm date*/
        TextView alarmDateTv = (TextView) view.findViewById(R.id.alarmDate);
        /*Basically, alarm is set to show its date, otherwise do not show it*/
        if (alarmDate == null||alarmDate.trim().equals("")){
            alarmDateTv.setVisibility(View.GONE);//like visible= false in flash
        }else{
            alarmDateTv.setVisibility(View.VISIBLE);
            alarmDateTv.setText(alarmDate);
        }
    }
}
