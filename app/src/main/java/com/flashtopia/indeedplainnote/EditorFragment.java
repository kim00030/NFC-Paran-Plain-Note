package com.flashtopia.indeedplainnote;


import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.flashtopia.indeedplainnote.components.MyCustomToast;
import com.flashtopia.indeedplainnote.data.Action;
import com.flashtopia.indeedplainnote.data.NFCData;
import com.flashtopia.indeedplainnote.handler.EMailHandler;
import com.flashtopia.indeedplainnote.handler.FontLoadHandler;
import com.flashtopia.indeedplainnote.handler.NFCHandler;
import com.flashtopia.indeedplainnote.handler.NoteAlarmHandler;
import com.flashtopia.indeedplainnote.handler.UpdateNoteIconHandler;
import com.flashtopia.indeedplainnote.interfaces.INoteAlarmCallback;

/*
* This is class to handle right side on large screen
* */
public class EditorFragment extends Fragment implements INoteAlarmCallback{

    //Reference of EditText resides on this Fragment
    private EditText editText;
    //to assign type of action either "Edit Note" or "New Note" mode
    private  String action;
    //Interface to callback to MainActivity
    private Callbacks activity;
    //For filtering string literal used in retrieving data from SQLite
    private String noteFilter;
    //for content of note before user edits
    private String oldText;

    private String currentNoteId;

    private Uri uri;
    private boolean isCheckedOnViewModeMenu = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //get Bundle object passed from MainActivity
        Bundle bundleArg = getArguments();
        //If the bundle object is not null, it means that user selects edit mode for existing note
        if (bundleArg != null) {

            action = Action.EDIT_NOTE;
            //parse the Bundle object and get note from SQLite DB
            Intent intent = bundleArg.getParcelable(Action.EDIT_NOTE);
            assert intent != null;
            uri = intent.getParcelableExtra(NotesProvider.CONTENT_TYPE_ITEM);
            if (uri != null)this.currentNoteId = uri.getLastPathSegment();
            this.noteFilter = DBOpenHelper.NOTE_ID + "=" + this.currentNoteId;

            try {
                Cursor cursor = getActivity().getContentResolver().query(

                        NotesProvider.CONTENT_URI,
                        DBOpenHelper.ALL_COLUMNS,
                        this.noteFilter,
                        null,
                        null
                );
                cursor.moveToFirst();
                oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else{//If Bundle object is null, it means that user clicks on "+" button to create new note
            action = Action.CREATE_NEW_NOTE;
        }
        //register NoteAlarm callback
        NoteAlarmHandler.getInstance().registerNoteAlarmCallback(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);//need for making onOptionsItemSelected to be worked

        View view = inflater.inflate(R.layout.activity_editor, container, false);
        //get EditText component in activity_editor.xml
        editText = (EditText) view.findViewById(R.id.editText);

        if (action.equals(Action.EDIT_NOTE)){
            editText.setText(oldText);
            editText.requestFocus();//place cursor at the end of note.
            if (savedInstanceState != null){
                isCheckedOnViewModeMenu = savedInstanceState.getBoolean(EditorActivity.STATE_VIEW_MODE_MENU);
                editText.setEnabled(!isCheckedOnViewModeMenu);
            }
        }


        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //save state of View mode menu
        outState.putBoolean(EditorActivity.STATE_VIEW_MODE_MENU,isCheckedOnViewModeMenu);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        //Basically, what it does below is that I want to show menus of delete,email,nfc only when user selects edit mode.
        //They will not show in creation of new note.
        try {
            menu.findItem(R.id.action_delete).setVisible(action.equals(Action.EDIT_NOTE));
            menu.findItem(R.id.action_email_note).setVisible(action.equals(Action.EDIT_NOTE));
            menu.findItem(R.id.action_nfc_note).setVisible(action.equals(Action.EDIT_NOTE));
            menu.findItem(R.id.action_alarm_note).setVisible(action.equals(Action.EDIT_NOTE));
            menu.findItem(R.id.action_view_mode).setVisible(action.equals(Action.EDIT_NOTE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor_large_screen, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_save_note:
                saveNote();
                break;

            case R.id.action_delete:
                setTitleOnActionBar(null);//set Default title which is app name "Paran note" on action bar
                ((MainActivity)this.activity).openDeleteNoteConfirmationDialog(R.id.action_delete,this.noteFilter);
                break;

            case R.id.action_email_note:
                EMailHandler.getInstance().send(getActivity(),editText.getText().toString().trim());
                break;

            case R.id.action_nfc_note:
                NFCData nfcData = new NFCData();
                nfcData.setActivity(getActivity());
                nfcData.setContentToBeSend(editText.getText().toString().trim());
                NFCHandler.getInstance().sendFileToConnectedDevice(nfcData);
                break;

            case R.id.action_alarm_note:

                boolean hasAlreadySetAlarmed = NoteAlarmHandler.getInstance().hasAlreadySetAlarmed(
                        uri, getActivity().getContentResolver(), noteFilter);

                if (hasAlreadySetAlarmed){
                    MyCustomToast.getInstance().show(getActivity(),getString(R.string.note_has_already_alarm),Toast.LENGTH_SHORT);
                    return true;
                }
                NoteAlarmHandler.getInstance().openTimePickerDialog(
                        getActivity(),this.currentNoteId,
                        editText.getText().toString().trim());

                break;

            case R.id.action_view_mode:
                isCheckedOnViewModeMenu = !isCheckedOnViewModeMenu;
                item.setChecked(isCheckedOnViewModeMenu);
                editText.setEnabled(!isCheckedOnViewModeMenu);
                break;
        }
        return false;// set to false because it's not working onOptionsItemSelected() in Fragment
    }

    /*
   * Method to save note
   * */
    private void saveNote() {

        //get current note
        String newText = editText.getText().toString().trim();
        if (newText.length() == 0){
            MyCustomToast.getInstance().show(getActivity(),getString(R.string.unable_to_save),Toast.LENGTH_SHORT);
            return;
        }else if(oldText != null && oldText.equals(newText)){
            return;//No need to take save process because oldText and newText are identical.
        }
        //save or update note to SQLite DB, depending on action
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, newText);

        if (action.equals(Action.CREATE_NEW_NOTE)){

            values.put(DBOpenHelper.NOTE_ICON, R.drawable.ic_tag_text);
            getActivity().getContentResolver().insert(NotesProvider.CONTENT_URI, values);
            this.activity.removeEditorFragment();
            this.activity.restartLoader();
            setTitleOnActionBar(null);//set Default title which is app name "Indeed note" on action bar

        }else if(action.equals(Action.EDIT_NOTE)|| action.equals(Action.SET_ALARM_NOTE)){
            oldText = newText;
            getActivity().getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
            this.activity.restartLoader();
            setTitleOnActionBar(getString(R.string.edit_note));
        }
        MyCustomToast.getInstance().show(getActivity(),getString(R.string.action_save),Toast.LENGTH_SHORT);
    }

    /*
    *
    * Method to set title on Action bar, depending on action
    * */
    private void setTitleOnActionBar(String title) {

        if (title == null) {
            //set App name
            int stringId = getActivity().getApplicationInfo().labelRes;
            String appName = getString(stringId);
            try {
                getActivity().setTitle(FontLoadHandler.getInstance().apply(getActivity(),appName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{// set title with given title
            try {
                getActivity().setTitle(FontLoadHandler.getInstance().apply(getActivity(),title));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (Callbacks) activity;
    }

    public interface Callbacks{

        void restartLoader();
        void removeEditorFragment();
    }

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
        this.activity.restartLoader();
    }
}
