package com.flashtopia.indeedplainnote.components;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.util.LruCache;


public class TypefaceSpan extends MetricAffectingSpan{

    /** An <code>LruCache</code> for previously loaded typefaces. */
    private static final LruCache<String, Typeface> sTypefaceCache =
            new LruCache<>(12);

    private Typeface mTypeface;

    public TypefaceSpan(Context context, String typefaceName) {
        mTypeface = sTypefaceCache.get(typefaceName);

        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(context.getApplicationContext()
                    .getAssets(), String.format("fonts/%s", typefaceName));

            // Cache the loaded Typeface
            sTypefaceCache.put(typefaceName, mTypeface);
        }
    }

    @Override
    public void updateMeasureState(TextPaint textPaint) {

        textPaint.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        textPaint.setFlags(textPaint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);

    }

    @Override
    public void updateDrawState(TextPaint textPaint) {

        textPaint.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        textPaint.setFlags(textPaint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}
