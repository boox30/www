package com.onyx.edu.homework.action.note;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.model.homework.Question;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.data.TextLayoutArgs;
import com.onyx.edu.homework.DataBundle;
import com.onyx.edu.homework.base.BaseNoteAction;
import com.onyx.edu.homework.request.HomeworkPagesRenderRequest;
import com.onyx.edu.homework.utils.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lxm on 2017/12/8.
 */

public class HomeworkPagesRenderAction extends BaseNoteAction {

    private Map<String, List<String>> pageUniqueMap;
    private boolean saveAsFile;
    private Map<String, List<Bitmap>> pageBitmaps = new HashMap<>();
    private Map<String, List<String>> pageFilePaths = new HashMap<>();
    private List<String> documentIds = new ArrayList<>();
    private int pageCount = -1;
    private int documentRenderCount = 0;
    private Rect size;

    public HomeworkPagesRenderAction(Map<String, List<String>> pageUniqueMap,
                                     Rect s,
                                     int pageCount,
                                     boolean saveAsFile) {
        this.pageUniqueMap = pageUniqueMap;
        this.saveAsFile = saveAsFile;
        this.pageCount = pageCount;
        size = s;
    }

    public HomeworkPagesRenderAction(Map<String, List<String>> pageUniqueMap,
                                     Rect s,
                                     boolean saveAsFile) {
        this.pageUniqueMap = pageUniqueMap;
        this.saveAsFile = saveAsFile;
        size = s;
    }

    @Override
    public void execute(final NoteViewHelper noteViewHelper, final BaseCallback baseCallback) {
        if (pageUniqueMap != null) {
            documentIds.addAll(pageUniqueMap.keySet());
        }
        renderPageBitmap(noteViewHelper, baseCallback);
    }

    private void renderPageBitmap(final NoteViewHelper noteViewHelper, final BaseCallback baseCallback) {
        if (isFinished(noteViewHelper)) {
            BaseCallback.invoke(baseCallback, null, null);
            return;
        }
        final String docId = documentIds.get(0);
        TextLayoutArgs textLayoutArgs = null;

        Question question = getQuestion(docId);
        if (question == null || (!question.isChoiceQuestion() && !question.doneAnswer)) {
            documentIds.remove(docId);
            documentRenderCount = 0;
            renderPageBitmap(noteViewHelper, baseCallback);
            return;
        }

        if (!question.isChoiceQuestion()) {
            textLayoutArgs = TextLayoutArgs.create(question.content, TextUtils.getTextSpacingAdd(question));
        }

        List<String> pageUniqueIds = pageUniqueMap.get(docId);

        String pageUniqueId = pageUniqueIds.remove(0);
        List<PageInfo> pageInfoList = new ArrayList<>();
        PageInfo pageInfo = new PageInfo(pageUniqueId, size.width(), size.height());
        pageInfo.updateDisplayRect(new RectF(0, 0, size.width(), size.height()));
        pageInfoList.add(pageInfo);

        final HomeworkPagesRenderRequest renderRequest = new HomeworkPagesRenderRequest(docId,
                pageInfoList,
                size,
                textLayoutArgs,
                saveAsFile);
        renderRequest.setQuestion(question);
        noteViewHelper.submit(getAppContext(), renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e != null) {
                    BaseCallback.invoke(baseCallback, request, e);
                    return;
                }
                documentRenderCount++;
                if (saveAsFile) {
                    addFile(docId, renderRequest.getFilePath());
                }else {
                    addBitmap(docId, renderRequest.getRenderBitmap());
                }
                renderPageBitmap(noteViewHelper, baseCallback);
            }
        });
    }

    private Question getQuestion(String docId) {
        return DataBundle.getInstance().getQuestion(docId);
    }

    private boolean isFinished(final NoteViewHelper noteViewHelper) {
        if (documentIds == null || documentIds.size() == 0) {
            return true;
        }
        String documentId = documentIds.get(0);
        List<String> pageUniqueIds = pageUniqueMap.get(documentId);
        if (pageUniqueIds == null || pageUniqueIds.size() == 0 || documentRenderCount == pageCount) {
            documentIds.remove(0);
            documentRenderCount = 0;
        }
        return (documentIds == null || documentIds.size() == 0);
    }

    private void addBitmap(String docId, Bitmap bitmap) {
        List<Bitmap> bitmaps;
        if (pageBitmaps.containsKey(docId)) {
            bitmaps = pageBitmaps.get(docId);
            bitmaps.add(bitmap);
        }else {
            bitmaps = new ArrayList<>();
            bitmaps.add(bitmap);
            pageBitmaps.put(docId, bitmaps);
        }
    }

    private void addFile(String docId, String base64) {
        List<String> base64s;
        if (pageFilePaths.containsKey(docId)) {
            base64s = pageFilePaths.get(docId);
            base64s.add(base64);
        }else {
            base64s = new ArrayList<>();
            base64s.add(base64);
            pageFilePaths.put(docId, base64s);
        }
    }

    @NonNull
    public Map<String, List<Bitmap>> getPageBitmaps() {
        return pageBitmaps;
    }

    @NonNull
    public Map<String, List<String>> getPageFilePaths() {
        return pageFilePaths;
    }

    @Nullable
    public Map<String, List<String>> getUnRenderPageUniqueMap() {
        return pageUniqueMap;
    }
}
