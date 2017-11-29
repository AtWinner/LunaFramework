package com.hemaapp.hm_FrameWork.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hemaapp.hm_FrameWork.PoplarObject;
import com.hemaapp.hm_FrameWork.R;

public class ButtonDialog extends PoplarObject {
    private Dialog mDialog;
    private ViewGroup mContent;
    private TextView mTextView;
    private Button btnLeft;
    private Button btnRight;
    private OnButtonListener buttonListener;

    public ButtonDialog(Context context) {
        mDialog = new Dialog(context, R.style.dialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_with_button, null);
        mContent = (ViewGroup) view.findViewById(R.id.content);
        mTextView = (TextView) view.findViewById(R.id.textView);
        btnLeft = (Button) view.findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonListener != null)
                    buttonListener.onLeftButtonClick(ButtonDialog.this);
            }
        });
        btnRight = (Button) view.findViewById(R.id.btnRight);
        btnRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (buttonListener != null)
                    buttonListener.onRightButtonClick(ButtonDialog.this);
            }
        });
        mDialog.setCancelable(false);
        mDialog.setContentView(view);
        mDialog.show();
    }

    /**
     * 给弹框添加自定义View
     *
     * @param v 自定义View
     */
    public void setView(View v) {
        mContent.removeAllViews();
        mContent.addView(v);
    }

    public void setText(String text) {
        mTextView.setText(text);
    }

    public void setText(int textID) {
        mTextView.setText(textID);
    }

    public void setLeftButtonText(String text) {
        btnLeft.setText(text);
    }

    public void setLeftButtonText(int textID) {
        btnLeft.setText(textID);
    }

    public void setRightButtonText(String text) {
        btnRight.setText(text);
    }

    public void setRightButtonText(int textID) {
        btnRight.setText(textID);
    }

    public void setRightButtonTextColor(int color) {
        btnRight.setTextColor(color);
    }

    public void show() {
        mDialog.show();
    }

    public void cancel() {
        mDialog.cancel();
    }

    public OnButtonListener getButtonListener() {
        return buttonListener;
    }

    public void setButtonListener(OnButtonListener buttonListener) {
        this.buttonListener = buttonListener;
    }

    public interface OnButtonListener {
        public void onLeftButtonClick(ButtonDialog dialog);

        public void onRightButtonClick(ButtonDialog dialog);
    }

}
