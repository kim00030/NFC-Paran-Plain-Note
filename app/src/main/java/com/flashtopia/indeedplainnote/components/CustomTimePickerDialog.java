/*This class is to create Time picker dialog*/
package com.flashtopia.indeedplainnote.components;

import android.app.TimePickerDialog;
import android.content.Context;

import com.flashtopia.indeedplainnote.R;

public class CustomTimePickerDialog extends TimePickerDialog{

    public CustomTimePickerDialog(Context context, int styleResId,OnTimeSetListener callBack, int hourOfDay, int minute) {

        super(context, styleResId, callBack, hourOfDay, minute, false);
        setTitle(context.getString(R.string.set_time_for_alarm));

    }


}

