package com.flashtopia.indeedplainnote.data;

import android.app.Activity;

/*
* This class is POJO that contains reference of Activity object and content of note to be send to
* target device in NFC
* */
public class NFCData {

    private Activity activity;

    private String contentToBeSend;

    public String getContentToBeSend() {
        return contentToBeSend;
    }

    public void setContentToBeSend(String contentToBeSend) {
        this.contentToBeSend = contentToBeSend;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
