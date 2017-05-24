package com.flashtopia.indeedplainnote;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
*
* This class a Fragment that contains ListView and floating button.
* present on left side of screen
* */
public class NoteListFragment extends Fragment {

    private Callbacks activity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myListView = inflater.inflate(R.layout.note_list_fragment,container,false);
        //pass it to MainActivity
        this.activity.setMyListView(myListView);

        return myListView;
    }

    public interface Callbacks{
        void setMyListView(View v);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (Callbacks) activity;
    }
}
