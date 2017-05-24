/*This is class to create my custom Toast*/
package com.flashtopia.indeedplainnote.components;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.flashtopia.indeedplainnote.R;

public class MyCustomToast {

    private static MyCustomToast instance;

    public static MyCustomToast getInstance(){

        if (instance == null){
            instance = new MyCustomToast();
        }
        return instance;
    }

    public void show(Context context,String msg,int displayLength){

        View view;
        TextView text;
        Toast toast;

        toast = Toast.makeText(context,msg, displayLength);
        view = toast.getView();
        text = (TextView) view.findViewById(android.R.id.message);
        text.setTextColor(context.getResources().getColor(android.R.color.white));
        //text.setShadowLayer(0,0,0,0);
        view.setBackgroundResource(R.color.colorAccent);
        toast.show();

    }
}

