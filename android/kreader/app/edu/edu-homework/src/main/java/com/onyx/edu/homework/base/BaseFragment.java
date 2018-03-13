package com.onyx.edu.homework.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.sdk.utils.Debug;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by lxm on 2017/12/5.
 */

public abstract class BaseFragment extends SupportFragment {

    private boolean isRunning = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Debug.d(getClass(), "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Debug.d(getClass(), "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        Debug.d(getClass(), "onResume");
        isRunning = true;
        super.onResume();
    }

    @Override
    public void onPause() {
        Debug.d(getClass(), "onPause");
        isRunning = false;
        super.onPause();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
