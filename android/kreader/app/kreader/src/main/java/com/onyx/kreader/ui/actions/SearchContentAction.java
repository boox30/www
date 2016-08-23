package com.onyx.kreader.ui.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.kreader.api.ReaderSelection;
import com.onyx.kreader.host.request.SearchRequest;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.utils.PagePositionUtils;

import java.util.List;

/**
 * Created by Joy on 2016/5/31.
 */
public class SearchContentAction extends BaseAction {
    private static final String TAG = SearchContentAction.class.getSimpleName();

    private String query;
    private OnSearchContentCallBack onSearchContentCallBack;
    private boolean stopSearch = false;
    private int contentLength;
    private int startPage;
    private int searchCount;
    private int currentCount;

    public interface OnSearchContentCallBack{
        void OnNext(List<ReaderSelection> results);
        void OnFinishedSearch(int endPage);
    }

    public SearchContentAction(final String query, int contentLength, int startPage, int searchCount) {
        this.query = query;
        this.contentLength = contentLength;
        this.startPage = startPage;
        this.searchCount = searchCount;
    }

    @Override
    public void execute(final ReaderDataHolder readerDataHolder) {
        execute(readerDataHolder,onSearchContentCallBack);
    }

    public void execute(final ReaderDataHolder readerDataHolder, final OnSearchContentCallBack onSearchContentCallBack) {
        this.onSearchContentCallBack = onSearchContentCallBack;
        stopSearch = false;
        requestSearchBySequence(readerDataHolder,startPage,query);
    }

    private void requestSearchBySequence(final ReaderDataHolder readerDataHolder, final int page, final String query){
        if (page >= readerDataHolder.getPageCount() || stopSearch || currentCount >= searchCount){
            onSearchContentCallBack.OnFinishedSearch(page);
            return;
        }
        SearchRequest request = new SearchRequest(PagePositionUtils.fromPageNumber(page), query, false, false, contentLength, readerDataHolder);
        readerDataHolder.getReader().submitRequest(readerDataHolder.getContext(), request,
                new BaseCallback() {
            @Override
            public void done(final BaseRequest request, Throwable e) {
                List<ReaderSelection> selections = readerDataHolder.getReaderUserDataInfo().getSearchResults();
                if (onSearchContentCallBack != null){
                    onSearchContentCallBack.OnNext(selections);
                }
                if (selections != null){
                    currentCount += selections.size();
                }
                int next = page + 1;
                requestSearchBySequence(readerDataHolder,next,query);
            }
        });
    }

    public void proceedSearch(final ReaderDataHolder readerDataHolder,final int startPage){
        currentCount = 0;
        requestSearchBySequence(readerDataHolder,startPage, query);
    }

    public void stopSearch(){
        currentCount = 0;
        stopSearch = true;
    }
}
