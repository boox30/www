package com.onyx.kreader.host.impl;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.onyx.kreader.api.ReaderPluginOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuzeng on 10/5/15.
 */
public class ReaderPluginOptionsImpl implements ReaderPluginOptions {

    private List<String> fontDirectories;
    private float screenDensity = 1.0f;
    private AssetManager assetManager;
    private Resources resources;

    public ReaderPluginOptionsImpl() {
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public Resources getResources() {
        return resources;
    }

    public List<String> getFontDirectories() {
        return fontDirectories;
    }

    public void setScreenDensity(final float d) {
        screenDensity = d;
    }

    public float getScreenDensity() {
        return screenDensity;
    }

    public static ReaderPluginOptionsImpl create(final Context context) {
        ReaderPluginOptionsImpl pluginOptions = new ReaderPluginOptionsImpl();
        pluginOptions.setScreenDensity(context.getResources().getDisplayMetrics().density);
        pluginOptions.fontDirectories = new ArrayList<>();
        pluginOptions.fontDirectories.add("/system/fonts");
        pluginOptions.assetManager = context.getAssets();
        pluginOptions.resources = context.getResources();
        return pluginOptions;
    }
}
