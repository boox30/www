package com.onyx.edu.reader.note.request;

import android.databinding.tool.util.L;

import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.utils.JSONObjectParseUtils;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.edu.reader.note.NoteManager;
import com.onyx.edu.reader.note.data.ReaderNotePageNameMap;
import com.onyx.edu.reader.note.model.ReaderFormShapeModel;
import com.onyx.edu.reader.note.model.ReaderNoteDataProvider;
import com.onyx.edu.reader.ui.data.ReviewDocumentData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ming on 2017/6/12.
 */

public class SaveReviewDataRequest extends ReaderBaseNoteRequest {

    private String documentUniqueId;
    private String reviewDocumentData;
    private List<PageInfo> pages;
    private boolean resume;

    public SaveReviewDataRequest(String reviewDocumentData, String documentUniqueId, List<PageInfo> pages, boolean resume) {
        this.reviewDocumentData = reviewDocumentData;
        this.documentUniqueId = documentUniqueId;
        this.pages = pages;
        this.resume = resume;
    }

    @Override
    public void execute(NoteManager noteManager) throws Exception {
        setResumeRawInputProcessor(resume && noteManager.isDFBForCurrentShape());
        ReviewDocumentData data = JSONObjectParseUtils.parseObject(reviewDocumentData, ReviewDocumentData.class);
        if (data == null) {
            throw new Exception("empty data");
        }

        setVisiblePages(pages);
        ReaderNotePageNameMap pageNameMap = data.getReaderNotePageNameMap();
        List<ReaderFormShapeModel> formShapeModels = data.getReaderFormShapes();
        if (pageNameMap == null && (formShapeModels == null || formShapeModels.size() == 0)) {
            throw new Exception("empty data");
        }

        ensureDocumentOpened(noteManager);
        List<ReaderFormShapeModel> newFormShapeModels = new ArrayList<>();
        for (ReaderFormShapeModel formShapeModel : formShapeModels) {
            if (!ReaderNoteDataProvider.hasFormShape(getContext(), formShapeModel.getShapeUniqueId())) {
                formShapeModel.setDocumentUniqueId(documentUniqueId);
                newFormShapeModels.add(formShapeModel);
            }
        }
        ReaderNoteDataProvider.saveFormShapeList(getContext(), newFormShapeModels);
        noteManager.getNoteDocument().addPageIndex(pageNameMap);
        noteManager.getNoteDocument().save(getContext(), "title");
        getNoteDataInfo().setContentRendered(renderVisiblePages(noteManager));
    }
}