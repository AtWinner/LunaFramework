package com.hemaapp.hm_FrameWork.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.R;

public class TextDialog extends PoplarObject {
    private Dialog mDialog;
    private TextView mTextView;

    private Runnable cancelRunnable = new Runnable() {

        @Override
        public void run() {
            cancel();
        }
    };

    public TextDialog(Context context) {
        mDialog = new Dialog(context, R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_text, null);
        mTextView = (TextView) view.findViewById(R.id.textView);
        mDialog.setCancelable(false);
        mDialog.setContentView(view);
        mDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mTextView.removeCallbacks(cancelRunnable);
            }
        });
        mDialog.show();
    }

    public void setText(String text) {
        mTextView.setText(text);
    }

    public void setText(int textID) {
        mTextView.setText(textID);
    }

    public void show() {
        mDialog.show();
        mTextView.postDelayed(cancelRunnable, 2000);
    }

    public void setCancelable(boolean cancelable) {
        mDialog.setCancelable(cancelable);
    }

    public void cancel() {
        mDialog.cancel();
    }

}
