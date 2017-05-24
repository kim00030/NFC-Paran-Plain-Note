/*
* This class is to create Dialog for confirmation of deleting all notes
* */
package com.flashtopia.indeedplainnote.components;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.flashtopia.indeedplainnote.R;
import com.flashtopia.indeedplainnote.interfaces.IDeleteNoteConfirmationDialog;


public class DeleteNoteConfirmationDialog extends AlertDialog.Builder{

    private final Activity activity;
    private final IDeleteNoteConfirmationDialog callback;
    private final int action;
    private final String noteFilter;

    public DeleteNoteConfirmationDialog(IDeleteNoteConfirmationDialog callback, int action,String noteFilter){

        super((Context)callback);
        this.callback = callback;
        this.activity = (Activity)callback;
        this.action = action;
        this.noteFilter = noteFilter;

        init();
    }

    private void init() {

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int button) {

                        if (button == DialogInterface.BUTTON_POSITIVE) {

                            String toastMsg = null;

                           switch (action){

                               case R.id.action_delete_all:
                                   callback.deleteAllNotes();
                                   toastMsg = activity.getString(R.string.all_deleted);
                                   break;

                               case R.id.action_delete:
                                   callback.deleteNote(noteFilter);
                                   toastMsg = activity.getString(R.string.action_delete_current_note);
                                   break;
                           }
                            MyCustomToast.getInstance().show(activity, toastMsg,Toast.LENGTH_SHORT);
                }
    }

                };

        setMessage(getContext().getString(R.string.are_you_sure));
        setPositiveButton(getContext().getString(android.R.string.yes), dialogClickListener);
        setNegativeButton(getContext().getString(android.R.string.no), dialogClickListener);
    }
}
