package com.onyx.jdread.reader.request;

import android.content.Context;

import com.onyx.android.sdk.data.ReaderTextStyle;
import com.onyx.android.sdk.reader.api.ReaderNavigator;
import com.onyx.android.sdk.reader.common.ReaderViewInfo;
import com.onyx.android.sdk.reader.reflow.ImageReflowSettings;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.jdread.reader.common.ReaderUserDataInfo;
import com.onyx.jdread.reader.data.Reader;
import com.onyx.jdread.reader.highlight.ReaderSelectionInfo;

import io.reactivex.Scheduler;

/**
 * Created by huxiaomao on 2017/12/26.
 */

public abstract class ReaderBaseRequest extends RxRequest {
    private ReaderViewInfo readerViewInfo;
    private ReaderUserDataInfo readerUserDataInfo;
    private ReaderSelectionInfo selectionInfoManager;
    private ReaderTextStyle style;
    private ImageReflowSettings settings;
    public boolean isSuccess = true;

    public final ReaderUserDataInfo getReaderUserDataInfo() {
        if (readerUserDataInfo == null) {
            readerUserDataInfo = new ReaderUserDataInfo();
        }
        return readerUserDataInfo;
    }

    public ReaderViewInfo getReaderViewInfo() {
        if (readerViewInfo == null) {
            readerViewInfo = new ReaderViewInfo();
        }
        return readerViewInfo;
    }

    public ReaderSelectionInfo getSelectionInfoManager() {
        if(selectionInfoManager == null){
            selectionInfoManager = new ReaderSelectionInfo();
        }
        return selectionInfoManager;
    }

    @Override
    public Scheduler subscribeScheduler() {
        return ReaderSchedulers.readerScheduler();
    }

    public ReaderTextStyle getStyle() {
        return style;
    }

    public ImageReflowSettings getSettings() {
        return settings;
    }

    public void updateSetting(Reader reader){
        if(getReaderViewInfo() != null && getReaderViewInfo().isTextPages()) {
            ReaderTextStyle srcStyle = reader.getReaderHelper().getTextStyleManager().getStyle();
            style = ReaderTextStyle.copy(srcStyle);
        }
        if(getReaderViewInfo() != null && getReaderViewInfo().supportScalable) {
            ImageReflowSettings srcSettings = reader.getReaderHelper().getImageReflowManager().getSettings();
            settings = ImageReflowSettings.copy(srcSettings);
        }

        loadUserData(reader);
    }

    private void loadUserData(Reader reader) {
        getReaderUserDataInfo().setDocumentPath(reader.getDocumentInfo().getBookPath());
        getReaderUserDataInfo().setDocumentCategory(reader.getReaderHelper().getDocumentOptions().getDocumentCategory());
        getReaderUserDataInfo().setDocumentCodePage(reader.getReaderHelper().getDocumentOptions().getCodePage());
        getReaderUserDataInfo().setChineseConvertType(reader.getReaderHelper().getDocumentOptions().getChineseConvertType());
        getReaderUserDataInfo().setDocumentMetadata(reader.getReaderHelper().getDocumentMetadata());


        boolean isSupportScale = reader.getReaderHelper().getRendererFeatures().supportScale();
        String displayName = reader.getReaderHelper().getPlugin().displayName();
        String md5 = reader.getReaderHelper().getDocumentMd5();
        ReaderNavigator navigator = reader.getReaderHelper().getNavigator();

        Context context = reader.getReaderHelper().getContext();
        if (readerViewInfo != null) {
            getReaderUserDataInfo().loadPageBookmarks(context, isSupportScale, displayName, md5, navigator, readerViewInfo.getVisiblePages());
        }
        if (readerViewInfo != null) {
            getReaderUserDataInfo().loadPageBookmarks(context, isSupportScale, displayName, md5, navigator, readerViewInfo.getVisiblePages());
        }
        if (readerViewInfo != null) {
            getReaderUserDataInfo().loadPageLinks(context, navigator, readerViewInfo.getVisiblePages());
        }
        if (readerViewInfo != null) {
            getReaderUserDataInfo().loadPageImages(context, navigator, readerViewInfo.getVisiblePages());
        }
    }
}
