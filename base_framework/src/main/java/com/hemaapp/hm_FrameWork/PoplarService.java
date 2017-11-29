package com.hemaapp.hm_FrameWork;

import android.app.Service;

import com.hemaapp.hm_FrameWork.presenter.BasePresenter;
import com.hemaapp.hm_FrameWork.presenter.BaseView;

/**
 *
 */

public abstract class PoplarService<T extends BasePresenter> extends Service implements BaseView {

    public T mPresenter;

    @Override
    public void showProgressDialog(int text) {

    }

    @Override
    public void showProgressDialog(String text) {

    }

    @Override
    public void showProgressDialog(String text, boolean cancelable) {

    }

    @Override
    public void showProgressDialog(int text, boolean cancelable) {

    }

    @Override
    public void cancelProgressDialog() {

    }

    @Override
    public void showTextDialog(int text) {

    }

    @Override
    public void showTextDialog(String text) {

    }

    @Override
    public void cancelTextDialog() {

    }

    @Override
    public void destroy() {

    }
}
