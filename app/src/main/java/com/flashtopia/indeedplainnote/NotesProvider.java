package com.flashtopia.indeedplainnote;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class NotesProvider extends ContentProvider{

    //globally unique string that identifies the content provider to the Android framework
    private static final String AUTHORITY =
            "com.flashtopia.indeedplainnote.notesprovider";

    //This represents the entire data set.
    // My database only has one table, so I've given the base path the name of the table, notes.
    private static final String BASE_PATH = "notes";
    //a uniform resource identifier that identifies the content provider. It includes the authority and the base path.
    public static final Uri CONTENT_URI = Uri.parse(

            "content://"+AUTHORITY+"/"+BASE_PATH
    );
    //represent operations, things you can do with this content provider
    //NOTES=> give me data
    //NOTES_ID=>operation to deal with only one single record.
    private static final int NOTES = 1;
    private static final int NOTES_ID = 2;

    private static final UriMatcher uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    //to parse a URI and then tell you which operation has been requested
    static{

        uriMatcher.addURI(AUTHORITY,BASE_PATH,NOTES);
        //I'm looking for a particular note, a particular row in the database table.
        uriMatcher.addURI(AUTHORITY,BASE_PATH+"/#",NOTES_ID);

    }

    public static final String CONTENT_TYPE_ITEM = "note";
    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        DBOpenHelper dbOpenHelper = new DBOpenHelper(getContext());
        database = dbOpenHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //compare uri that passed here with my uri pattern, BASE_PATH+"/#
        //And looks for a uri that ends with / and a numeric value, and it assigns an ID of NOTES_ID.
        if (uriMatcher.match(uri) == NOTES_ID){

            //So if I got a uri ending with a numeric value, I know I only want a single row.
            // And I'll accomplish that by resetting the selection. I'll start with the name of the primary key field.
            // I'll use selection value DBOpenHelper.NOTE_ID, then I'll append an = operator. And then,
            // the primary key value, which I'll get from the expression uri.getLastPathSegment,
            // so that will return just the numeric value after the /.
            selection = DBOpenHelper.NOTE_ID+"="+uri.getLastPathSegment();

        }

        //REMINDER: DBOpenHelper.NOTE_CREATED column in DB is for TimeStamp
        return database.query(DBOpenHelper.TABLE_NOTES,DBOpenHelper.ALL_COLUMNS,selection,null,null,null,DBOpenHelper.NOTE_CREATED+" DESC");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }



    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long id = database.insert(DBOpenHelper.TABLE_NOTES, null, values);
        return Uri.parse(BASE_PATH+"/"+id);//notes/id
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        return database.delete(DBOpenHelper.TABLE_NOTES, selection, selectionArgs);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return database.update(DBOpenHelper.TABLE_NOTES, values, selection, selectionArgs);
    }


}
