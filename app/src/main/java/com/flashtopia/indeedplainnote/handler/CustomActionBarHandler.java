
/*
* This class handles for setting my custom action bar
* */
package com.flashtopia.indeedplainnote.handler;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.flashtopia.indeedplainnote.R;


@SuppressWarnings("ConstantConditions")
public class CustomActionBarHandler {

    private final AppCompatActivity activity;

    public CustomActionBarHandler(AppCompatActivity activity, boolean displayHomeAsUpEnabled){
        this.activity = activity;
        try {

            LayoutInflater inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.actionbar_title, null);
            this.activity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
            this.activity.getSupportActionBar().setCustomView(v);
            /*I passed false to setDisplayHomeAsUpEnabled(false) because I don't want to show home button(<-)
            * in MainActivity
            * */
            this.activity.getSupportActionBar().setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /*method to get title field on my custom action bar*/
    public TextView getTextOnActionBar(){

        return (TextView) this.activity.findViewById(R.id.myCustomToolBarText);
    }
}
