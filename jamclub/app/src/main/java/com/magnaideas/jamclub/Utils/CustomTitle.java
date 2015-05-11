package com.magnaideas.jamclub.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by edoardomoreni on 11/05/15.
 */
public class CustomTitle extends TextView {

    public CustomTitle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustomTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTitle(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "lobster.ttf");
            setTypeface(tf);
        }
    }

}