package com.onyx.jdread.reader.menu.actions;

import com.onyx.android.sdk.data.ReaderTextStyle;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.jdread.reader.actions.BaseAction;
import com.onyx.jdread.reader.data.ReaderDataHolder;
import com.onyx.jdread.reader.menu.request.SettingTextStyleRequest;

/**
 * Created by huxiaomao on 2017/12/30.
 */

public class SettingRowSpacingAction extends BaseAction {
    private int lineSpacing;

    public SettingRowSpacingAction(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    @Override
    public void execute(ReaderDataHolder readerDataHolder) {
        ReaderTextStyle style = readerDataHolder.getReader().getReaderHelper().getTextStyleManager().getStyle();
        ReaderTextStyle.Percentage oldLineSpacing = style.getLineSpacing();
        oldLineSpacing.setPercent(lineSpacing);
        new SettingTextStyleRequest(readerDataHolder).execute(new RxCallback() {
            @Override
            public void onNext(Object o) {
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }
}
