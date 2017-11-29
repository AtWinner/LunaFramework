package com.hemaapp.hm_FrameWork.presenter;

/**
 * 与HemaBasePresenter用于交互的View
 */

public interface BaseView {
    void showProgressDialog(int text);

    void showProgressDialog(String text);

    void showProgressDialog(String text, boolean cancelable);

    void showProgressDialog(int text, boolean cancelable);

    void cancelProgressDialog();

    void showTextDialog(int text);

    void showTextDialog(String text);

    void cancelTextDialog();

    void destroy();
}
