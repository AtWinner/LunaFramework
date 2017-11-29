package com.hemaapp.hm_FrameWork.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.Serializable;

/**
 * Created by huhu on 2016/6/28.
 */
public class HemaIntent extends Intent implements Serializable {

    private static final long serialVersionUID = 1L;

    public HemaIntent() {
        super();
    }

    public HemaIntent(Context packageContext, Class<?> cls) {
        super(packageContext, cls);
    }

    public HemaIntent(Intent o) {
        super(o);
    }

    public HemaIntent(String action, Uri uri, Context packageContext,
                      Class<?> cls) {
        super(action, uri, packageContext, cls);
    }

    public HemaIntent(String action, Uri uri) {
        super(action, uri);
    }

    public HemaIntent(String action) {
        super(action);
    }
}
