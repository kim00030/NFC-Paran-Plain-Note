package com.flashtopia.indeedplainnote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flashtopia.indeedplainnote.components.DeleteNoteConfirmationDialog;
import com.flashtopia.indeedplainnote.components.MyCustomToast;
import com.flashtopia.indeedplainnote.data.Action;
import com.flashtopia.indeedplainnote.data.NFCData;
import com.flashtopia.indeedplainnote.handler.CustomActionBarHandler;
import com.flashtopia.indeedplainnote.handler.EMailHandler;
import com.flashtopia.indeedplainnote.handler.FontLoadHandler;
import com.flashtopia.indeedplainnote.handler.NFCHandler;
import com.flashtopia.indeedplainnote.handler.NoteAlarmHandler;
import com.flashtopia.indeedplainnote.handler.UpdateNoteIconHandler;
import com.flashtopia.indeedplainnote.interfaces.IDeleteNoteConfirmationDialog;
import com.flashtopia.indeedplainnote.interfaces.INoteAlarmCallback;


public class EditorActivity extends AppCompatActivity implements INoteAlarmCallback,IDeleteNoteConfirmationDialog{

    public static final String STATE_VIEW_MODE_MENU = "state_view_mode_menu";
    //to remember whether or not inserting or updating note
    private String action;
    //for reference of EditorText component here
    private EditText editor;
    //for WHERE clause used in queueing data from DB
    private String noteFilter;

    private String oldText;

    private String currentNoteId;

    private Uri uri;

    private boolean isCheckedOnViewModeMenu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        //for getting title in custom action bar
        TextView titleOnToolBar = new CustomActionBarHandler(this,true).getTextOnActionBar();

        //get EditText from this layout file
        editor = (EditText) findViewById(R.id.editText);

        if (savedInstanceState != null && editor != null){
            isCheckedOnViewModeMenu = savedInstanceState.getBoolean(STATE_VIEW_MODE_MENU);
            editor.setEnabled(!isCheckedOnViewModeMenu);
        }
        //register NoteAlarm callback
        NoteAlarmHandler.getInstance().registerNoteAlarmCallback(this);

        //***figure out whether user clicks on + button to create new note or clicks on
        //list item for its update.
        //get Intent object passed by MainActivity
        Intent intent = getIntent();

        uri = intent.getParcelableExtra(NotesProvider.CONTENT_TYPE_ITEM);

        //user clicks on + button to insert create new note
        if (uri == null) {
            action = Intent.ACTION_INSERT;
            //change Title on ActionBar that indicates it's New Note
            titleOnToolBar.setText(FontLoadHandler.getInstance().apply(this, getString(R.string.new_note)));
        } else {//update process
            action = Intent.ACTION_EDIT;
            //***save current Note Id that generated from Main DB,DBOpenHelper
            this.currentNoteId = uri.getLastPathSegment();
            noteFilter = DBOpenHelper.NOTE_ID + "=" + this.currentNoteId;

            try {
                Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);

                cursor.moveToFirst();
                oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
                editor.setText(oldText);
                editor.requestFocus();
                titleOnToolBar.setText(FontLoadHandler.getInstance().apply(this, getString(R.string.edit_note)));
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(STATE_VIEW_MODE_MENU, isCheckedOnViewModeMenu);
        super.onSaveInstanceState(outState);
    }

    /*
        * Method to be called when user presses on Home button or back button on the device
        * to go back to Main
        * */
    private void finishEditing() {

        //get user's input in EditText
        String newText = editor.getText().toString().trim();

        //evaluate current action
        switch (action) {
            //if user presses + button to insert new note
            case Intent.ACTION_INSERT:
                //user dose not type anything. blank
                if (newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText);
                }
                break;

            case Intent.ACTION_EDIT:

                if (newText.length() == 0) {
                    deleteNote(null);
                    MyCustomToast.getInstance().show(this, getString(R.string.action_delete_current_note),Toast.LENGTH_SHORT);
                } else if (oldText.equals(newText)) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote(newText);
                }
                break;

            case Action.SET_ALARM_NOTE:

                if (newText.length() == 0){
                    deleteNote(null);
                    MyCustomToast.getInstance().show(this, getString(R.string.action_delete_current_note),Toast.LENGTH_SHORT);
                }else if(oldText.equals(newText)){
                    setResult(RESULT_OK);
                } else {
                    updateNote(newText);
                }
                break;

        }
        finish();

        //push from top to bottom
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        //slide from left to right
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void updateNote(String noteText) {

        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        setResult(RESULT_OK);
        MyCustomToast.getInstance().show(this,getString(R.string.action_save),Toast.LENGTH_SHORT);
    }

    private void insertNote(String noteText) {

        //insert new note user types
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        values.put(DBOpenHelper.NOTE_ICON, R.drawable.ic_tag_text);
        //This returns URI but I don't care
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    /*
    * When back button on the device  presses
    * */
    @Override
    public void onBackPressed() {
        finishEditing();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        try {

            MenuItem viewModeMenu = menu.findItem(R.id.action_view_mode);
            viewModeMenu.setChecked(isCheckedOnViewModeMenu);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //I want to display delete icon menu only when  user wants to edit note
        if (action.equals(Intent.ACTION_EDIT)) {

            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                /*show Dialog for confirmation of deleting note*/
                new DeleteNoteConfirmationDialog(this, R.id.action_delete,this.noteFilter).show();
                break;

            case R.id.action_email_note:
                EMailHandler.getInstance().send(this, editor.getText().toString().trim());
                break;

            case R.id.action_nfc_note:

                NFCData nfcData = new NFCData();
                nfcData.setActivity(this);
                nfcData.setContentToBeSend(editor.getText().toString().trim());
                NFCHandler.getInstance().sendFileToConnectedDevice(nfcData);
                break;

            case R.id.action_alarm_note:
                //check if the note has already set alarm
                boolean hasAlreadySetAlarmed = NoteAlarmHandler.getInstance().hasAlreadySetAlarmed(uri, getContentResolver(), noteFilter);
                if (hasAlreadySetAlarmed){
                    MyCustomToast.getInstance().show(this,getString(R.string.note_has_already_alarm),Toast.LENGTH_SHORT);
                    return true;
                }

                NoteAlarmHandler.getInstance().openTimePickerDialog(
                        this,this.currentNoteId,
                        editor.getText().toString().trim()
                );
                break;
            case R.id.action_view_mode:
                    isCheckedOnViewModeMenu = !isCheckedOnViewModeMenu;
                    item.setChecked(isCheckedOnViewModeMenu);
                    editor.setEnabled(!isCheckedOnViewModeMenu);

                break;
        }
        return true;
    }

    /*Override method in IDeleteNoteConfirmationDialog interface*/
    @Override
    public void deleteNote(String noteFilter) {

        if (noteFilter == null) noteFilter = this.noteFilter;
        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        setResult(RESULT_OK);
        finish();
    }
    /*Override method in IDeleteNoteConfirmationDialog interface
    * ******************NO NEED TO USE*******************************
    * */
    @Override
    public void deleteAllNotes() {}

    /*
    * This callback method will be called from NoteAlarmHandler when alarm time is set.
    * see@setAlarm(Calendar targetCal),iNoteAlarmCallback.noteAlarmNotify(Action.SET_ALARM_NOTE)
     * in NoteAlarmHandler
    *
    * */
    @Override
    public void noteAlarmNotify(String date) {

        this.action = Action.SET_ALARM_NOTE;
        UpdateNoteIconHandler.getInstance().setAlarmNoteIcon(date,noteFilter);
    }

}