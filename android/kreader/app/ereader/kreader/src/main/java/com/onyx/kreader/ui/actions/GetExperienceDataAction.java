package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.data.model.StatisticsResult;
import com.onyx.android.sdk.utils.DeviceUtils;
import com.onyx.android.sdk.utils.NetworkUtil;
import com.onyx.kreader.ui.data.ReaderDataHolder;

/**
 * Created by ming on 2017/2/14.
 */

public class GetExperienceDataAction extends BaseAction {

    private StatisticsResult statisticsResult;

    @Override
    public void execute(ReaderDataHolder readerDataHolder, BaseCallback baseCallback) {
        if (!NetworkUtil.isWiFiConnected(readerDataHolder.getContext())) {

        }
    }

}