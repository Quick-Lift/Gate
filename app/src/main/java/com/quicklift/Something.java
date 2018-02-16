package com.quicklift;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

/**
 * Created by adarsh on 15/2/18.
 */

public class Something extends ArrayAdapter {
    public Something(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }
}
