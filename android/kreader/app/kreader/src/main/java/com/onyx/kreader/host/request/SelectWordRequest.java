package com.onyx.kreader.host.request;

import android.graphics.PointF;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.kreader.api.ReaderHitTestArgs;
import com.onyx.kreader.api.ReaderHitTestManager;
import com.onyx.kreader.api.ReaderHitTestOptions;
import com.onyx.kreader.api.ReaderSelection;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.host.layout.LayoutProviderUtils;
import com.onyx.kreader.host.math.PageUtils;
import com.onyx.kreader.host.wrapper.Reader;

/**
 * Created by zhuzeng on 2/15/16.
 */
public class SelectWordRequest extends BaseReaderRequest {

    private PointF start = new PointF();
    private PointF end = new PointF();
    private PointF touchPoint = new PointF();
    private ReaderSelection selection;
    private String pagePosition;
    private ReaderHitTestOptions hitTestOptions;

    public SelectWordRequest(final String pagePosition, final PointF startPoint, final PointF endPoint, final PointF touchPoint,final ReaderHitTestOptions hitTestOptions) {
        start.set(startPoint.x, startPoint.y);
        end.set(endPoint.x, endPoint.y);
        this.touchPoint.set(touchPoint.x, touchPoint.y);
        this.pagePosition = pagePosition;
        this.hitTestOptions = hitTestOptions;
    }

    public PointF getstart() {
        return start;
    }

    public PointF getEnd() {
        return end;
    }

    // check page at first. and then goto the location.
    public void execute(final Reader reader) throws Exception {
        setLoadBookmark(false);
        setLoadPageAnnotation(false);
        setTransferBitmap(false);
        createReaderViewInfo();
        if (!reader.getReaderLayoutManager().getCurrentLayoutProvider().canHitTest()) {
            return;
        }
        ReaderHitTestManager hitTestManager = reader.getReaderHelper().getHitTestManager();
        PageInfo pageInfo = reader.getReaderLayoutManager().getPageManager().getPageInfo(pagePosition);
        ReaderHitTestArgs argsStart = new ReaderHitTestArgs(pagePosition, pageInfo.getDisplayRect(), 0, start);
        ReaderHitTestArgs argsEnd = new ReaderHitTestArgs(pagePosition, pageInfo.getDisplayRect(), 0, end);
        selection = hitTestManager.select(argsStart, argsEnd, hitTestOptions);
        LayoutProviderUtils.updateReaderViewInfo(getReaderViewInfo(), reader.getReaderLayoutManager());
        if (selection != null && selection.getRectangles().size() > 0) {
            getReaderUserDataInfo().saveHighlightResult(translateToScreen(pageInfo, selection));
            getReaderUserDataInfo().setTouchPoint(touchPoint);
        }
    }

    private ReaderSelection translateToScreen(PageInfo pageInfo, ReaderSelection selection) {
        int size = selection.getRectangles().size();
        for (int i = 0; i < size; i++) {
            PageUtils.translate(pageInfo.getDisplayRect().left,
                    pageInfo.getDisplayRect().top,
                    pageInfo.getActualScale(),
                    selection.getRectangles().get(i));
        }
        return selection;
    }

}
