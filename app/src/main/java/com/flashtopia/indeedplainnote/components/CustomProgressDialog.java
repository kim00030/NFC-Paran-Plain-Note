
/*This is class to create Progress Dialog showing when loading notes that user saved*/
package com.flashtopia.indeedplainnote.components;

import android.app.ProgressDialog;
import android.content.Context;

import com.flashtopia.indeedplainnote.R;

public class CustomProgressDialog extends ProgressDialog{

    public CustomProgressDialog(Context context) {
        super(context);
        //setTitle(context.getString(R.string.loading));
        setMessage(context.getString(R.string.wait_while_loading));
        show();
    }
}
