package com.flashtopia.indeedplainnote.handler;


import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;

import com.flashtopia.indeedplainnote.components.TypefaceSpan;
/*
* Refer to https://gist.github.com/twaddington/b91341ea5615698b53b8
* */
public class FontLoadHandler {


    private static final String DEFAULT_FONT_FILE = "Grundschrift-Bold.otf";
    private static final String KOREAN_FONT_FILE = "UnJamoSora.ttf";
    public static final String  KOREAN = "ko_KR";
    public static final String  JAPANESE = "ja_JP";

    private static FontLoadHandler instance;

    public static FontLoadHandler getInstance(){

        if (instance == null){
            instance = new FontLoadHandler();
        }

        return instance;
    }

    public SpannableString apply( Context context,String s ){

        try {

            String fontType = DEFAULT_FONT_FILE;
            if (NoteAlarmHandler.getInstance().getLanguageSet().equals(KOREAN) ||
                    NoteAlarmHandler.getInstance().getLanguageSet().equals(JAPANESE)){
                /*I applied this font to Japanese local as well*/
                fontType = KOREAN_FONT_FILE;
            }

            SpannableString ss = new SpannableString(s);
            ss.setSpan(new TypefaceSpan(context, fontType), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            return ss;

        } catch (Exception e) {
            e.printStackTrace();
            return new SpannableString(s);
        }
    }
}
