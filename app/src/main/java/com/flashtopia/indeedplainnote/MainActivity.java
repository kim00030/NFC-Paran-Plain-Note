package com.flashtopia.indeedplainnote;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flashtopia.indeedplainnote.alarmnotemodule.AlarmNote;
import com.flashtopia.indeedplainnote.alarmnotemodule.AlarmNotesDataSource;
import com.flashtopia.indeedplainnote.components.CustomProgressDialog;
import com.flashtopia.indeedplainnote.components.DeleteNoteConfirmationDialog;
import com.flashtopia.indeedplainnote.components.MyCustomToast;
import com.flashtopia.indeedplainnote.components.ThankYouDialog;
import com.flashtopia.indeedplainnote.data.Action;
import com.flashtopia.indeedplainnote.handler.CustomActionBarHandler;
import com.flashtopia.indeedplainnote.handler.FontLoadHandler;
import com.flashtopia.indeedplainnote.handler.NoteAlarmHandler;
import com.flashtopia.indeedplainnote.handler.UpdateNoteIconHandler;
import com.flashtopia.indeedplainnote.interfaces.IDeleteNoteConfirmationDialog;

import java.util.List;

/*
* This Main class implements LoaderCallbacks ,Interface Callbacks located in NoteListFragment
* and EditorFragment
*
* */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks,
        NoteListFragment.Callbacks,
        EditorFragment.Callbacks,
        IDeleteNoteConfirmationDialog
{
    //request code that is used in Intent to go to EditorActivity
    private static final int EDITOR_REQUEST_CODE = 1000;
    //It's like ArrayAdapter to bridge between your data and ListView. But it associated with SQLite DB
    private CursorAdapter cursorAdapter;
    //For reference of ListView
    private View myListView;
    //Flag to identify if screen is large
    private  boolean isTwoPane = false;
    //Fragment class for note editor part for large screen
    private  EditorFragment editorFragment;

    //title on my custom action bar
    private TextView titleOnActionBar;
    //for ProgressDialog
    private CustomProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /*get SharedPreferences to figure out if Thank you alert dialog needs to be shown up*/
        SharedPreferences showingPref = getSharedPreferences(ThankYouDialog.SHOWING_PREFERENCES,MODE_PRIVATE);
        //load progress dialog if there is notes saved
        loadProgressDialog();

        //for getting custom action bar
        titleOnActionBar = new CustomActionBarHandler(this, false).getTextOnActionBar();
        //format with font loaded and apply title in tool bar
        titleOnActionBar.setText(FontLoadHandler.getInstance().apply(this, getString(R.string.app_name)));

        /*
        * Get layout file. REMINDER: there is alternative layout file for 2 screen.
        * see@values/layout.xml
        * */
        setContentView(R.layout.activity_main);

        /*activity_main_large.xml has frame called detailContainer.
        * activity_main has not, so if "detailContainer" found, it means activity_main_large.xml
        * */
        if (findViewById(R.id.detailContainer) != null) {
            isTwoPane = true;
        }

        if (cursorAdapter == null) {

            cursorAdapter = new NotesCursorAdapter(this);
            ListView listView = (ListView) myListView.findViewById(android.R.id.list);
            listView.setAdapter(cursorAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    goToEditorPageBy(id);
                }
            });
            /*Must need this to be called for initialing load */
            getLoaderManager().initLoader(0, null, this);
        }

        //initialize DB designed for storing alarm notes for case of reboot
        AlarmNotesDataSource.getInstance().init(this);

        //save ContentResolver and this class in  UpdateNoteIconHandler class to be able to update note icon if need
        UpdateNoteIconHandler.getInstance().init(this);
        /*show thank you dialog to new user who installs on their device*/
        boolean needToShow = showingPref.getBoolean(ThankYouDialog.SHOWING_STATUS,true);
        if (needToShow) {
           new ThankYouDialog(this).show();
        }

    }

    /*
     * Method to go to Editor page
     * */
    private void goToEditorPageBy(long id) {

        Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);

        if(isTwoPane){

            Intent intent = new Intent();
            intent.putExtra(NotesProvider.CONTENT_TYPE_ITEM,uri);
            Bundle b = new Bundle();
            b.putParcelable(Action.EDIT_NOTE, intent);
            editorFragment = new EditorFragment();
            editorFragment.setArguments(b);

            getFragmentManager().beginTransaction()
                    .replace(R.id.detailContainer,editorFragment)
                    .commit();

            //change title on action bar as "Edit Note"
            titleOnActionBar.setText(FontLoadHandler.getInstance().apply(this, getString(R.string.edit_note)));

        }else{

            Intent intent = new Intent(this,EditorActivity.class);
            intent.putExtra(NotesProvider.CONTENT_TYPE_ITEM, uri);
            startActivityForResult(intent, EDITOR_REQUEST_CODE);
            //push from bottom to top
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            //slide from right to left
            //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

           case R.id.action_delete_all:
              // deleteAllNotes();
               if (!hasNote()){
                   MyCustomToast.getInstance().show(this, getString(R.string.you_have_no_notes), Toast.LENGTH_SHORT);

               }else {

                   openDeleteNoteConfirmationDialog(R.id.action_delete_all,null);
               }

        }
        return false;// set to false because it's not working onOptionsItemSelected() in Fragment
    }
    /*show Dialog for confirmation of deleting either all notes or single note depending on action passed*/
    public void openDeleteNoteConfirmationDialog(int action,String noteFilter){
        new DeleteNoteConfirmationDialog(this, action,noteFilter).show();
    }
    /*Override method in IDeleteNoteConfirmationDialog
    * This will be called from DeleteNoteConfirmationDialog in case of large screen
    * */
    @Override
    public void deleteNote(String noteFilter){

        getContentResolver().delete(NotesProvider.CONTENT_URI, noteFilter, null);
        removeEditorFragment();//Remove this fragment screen
        restartLoader();//need to restart Loader/*Override method in IDeleteNoteConfirmationDialog*/
    }

    /*Override method in IDeleteNoteConfirmationDialog
    * This will be called from DeleteNoteConfirmationDialog in case of large screen
    * */
    @Override
    public void deleteAllNotes() {

        getContentResolver().delete(NotesProvider.CONTENT_URI, null, null);
        removeEditorFragment();
        //Set the label on ActionBar as app Name if it's been changed to "New note" or "Edit note"
        int stringId =getApplicationInfo().labelRes;
        String appName = getString(stringId);
        if (!(getTitle().equals(appName))) {
            titleOnActionBar.setText(FontLoadHandler.getInstance().apply(this, getString(R.string.app_name)));
        }
        restartLoader();

    }

    public void removeEditorFragment() {
        if (isTwoPane && editorFragment != null){
            getFragmentManager().beginTransaction()
                    .remove(editorFragment)
                    .commit();
        }
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /*
    * method is called whenever data is needed from the content provider.
    * To do this, we're going to use an instance of the CursorLoader class.
    * The cursor loader is specifically designed to manage a cursor.
    * */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,NotesProvider.CONTENT_URI,null,null,null,null);
    }

   /*
   * When you create the cursor loader object it executes the Query method on the background thread.
    And when the data comes back, onLoadFinished is called for you. Your job is to take the data represented
     by the cursor object, named data, and pass it to the cursor adaptor. We'll do that with cursorAdaptor.swapCursor
     and then pass in the data object.
   * */
    @Override
    public void onLoadFinished(Loader loader, Object data) {

        //If loading finished, dismiss ProgressBar
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
            progressDialog = null;
        }

        cursorAdapter.swapCursor((Cursor) data);

        //get alarm notes from AlarmNoteDB
        final List<AlarmNote> alarmNoteList = AlarmNotesDataSource.getInstance().findAll();
        // if there is any alarm notes saved in DB
        if (alarmNoteList != null && alarmNoteList.size() > 0) {
            NoteAlarmHandler.getInstance().executeUnreadAlarmNote(this, alarmNoteList);
        }

    }
    /*
    * method is called whenever the data needs to be wiped out.
    * And we'll do that with cursorAdaptor.swapCursor and pass in a null value.
    * */
    @Override
    public void onLoaderReset(Loader loader) {

        cursorAdapter.swapCursor(null);
    }

    /*
    * Method to get View where ListView is defined. It's defined in  Interface Callbacks in NoteListFragment class
    * */
    @Override
    public void setMyListView(View myListView) {

        this.myListView = myListView;
    }

    /*
        Plus(+) button handler to create new note
     */
    @SuppressWarnings("UnusedParameters")
    public void openEditorForNewNote(View view) {

        if (isTwoPane){

            editorFragment = new EditorFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.detailContainer, editorFragment)
                    .commit();

            titleOnActionBar.setText(FontLoadHandler.getInstance().apply(this, getString(R.string.new_note)));
        }else{

            startActivityForResult(new Intent(this,EditorActivity.class), EDITOR_REQUEST_CODE);
            //slide from right to left
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK){
            //restarts the loader from the LoaderManager and refills the data from
            // the database and displays it in the list
            restartLoader();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //open database where stores unread alarm note
        AlarmNotesDataSource.getInstance().open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //close database where stores unread alarm note
        AlarmNotesDataSource.getInstance().close();
    }
    /*method to check if there is notes saved*/
    private boolean hasNote(){

        Cursor cursor = getContentResolver().query(NotesProvider.CONTENT_URI, DBOpenHelper.ALL_COLUMNS, null, null, null);
        cursor.moveToFirst();
        int noteCount = cursor.getCount();
        cursor.close();
        return (noteCount >0);
    }
    /*method to load progress dialog*/
    private void loadProgressDialog(){
        if (hasNote()){
            //ProgressSpinner start running
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog == null) {
                        progressDialog = new CustomProgressDialog(MainActivity.this);
                    }
                }
            });
        }
    }

}
