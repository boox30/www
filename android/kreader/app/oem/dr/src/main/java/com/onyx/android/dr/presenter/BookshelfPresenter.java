package com.onyx.android.dr.presenter;

import com.onyx.android.dr.DRApplication;
import com.onyx.android.dr.data.BookshelfData;
import com.onyx.android.dr.holder.LibraryDataHolder;
import com.onyx.android.dr.interfaces.BookshelfView;
import com.onyx.android.dr.util.DRPreferenceManager;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.QueryArgs;
import com.onyx.android.sdk.data.model.Library;
import com.onyx.android.sdk.data.model.Metadata;
import com.onyx.android.sdk.data.request.cloud.v2.CloudChildLibraryListLoadRequest;
import com.onyx.android.sdk.data.request.cloud.v2.CloudContentListRequest;
import com.onyx.android.sdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hehai on 17-8-4.
 */

public class BookshelfPresenter {
    private BookshelfView bookshelfView;
    private BookshelfData bookshelfData;

    public BookshelfPresenter(BookshelfView bookshelfView) {
        this.bookshelfView = bookshelfView;
        this.bookshelfData = new BookshelfData();
    }

    public void getBookshelf(final String language, final LibraryDataHolder databaseHolder) {
        final CloudChildLibraryListLoadRequest req = new CloudChildLibraryListLoadRequest(DRPreferenceManager.loadLibraryParentId(DRApplication.getInstance(), ""));
        bookshelfData.getLibraryList(req, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                List<Library> libraryList = req.getLibraryList();
                getLibraryList(databaseHolder, language, libraryList);
            }
        });

    }

    private void getLibraryList(LibraryDataHolder databaseHolder, String language, List<Library> libraryList) {
        bookshelfData.getLanguageBooks(databaseHolder, language, libraryList, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                Map<String, List<Metadata>> map = bookshelfData.getLanguageCategoryMap();
                bookshelfView.setLanguageCategory(map);
            }
        });
    }

    public void getLibrary(final QueryArgs queryArgs) {
        final CloudContentListRequest req = new CloudContentListRequest(queryArgs);
        bookshelfData.getLibraryBooks(req, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                Map<String, List<Metadata>> map = bookshelfData.getLanguageCategoryMap();
                bookshelfView.setLanguageCategory(map);
            }
        });
    }

    public void getBooks(String language) {
        bookshelfView.setBooks(bookshelfData.getLanguageCategoryMap().get(language));
    }


    public void getLibraryList() {
        String libraryParentId = DRPreferenceManager.loadLibraryParentId(DRApplication.getInstance(), "");
        final CloudChildLibraryListLoadRequest req = new CloudChildLibraryListLoadRequest(libraryParentId);
        bookshelfData.getLibraryList(req, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                List<Library> libraryNewList = new ArrayList<>();
                List<Library> libraryList = req.getLibraryList();
                if (!CollectionUtils.isNullOrEmpty(libraryList)) {
                    for (int i = libraryList.size() - 1; i >= 0; i--) {
                        libraryNewList.add(libraryList.get(i));
                    }
                    bookshelfView.setLibraryList(libraryNewList);
                }
            }
        });
    }

    public void getLanguageList() {
        bookshelfView.setLanguageList(bookshelfData.getLanguageList());
    }
}
