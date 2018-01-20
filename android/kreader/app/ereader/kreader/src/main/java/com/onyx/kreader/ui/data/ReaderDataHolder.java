package com.onyx.kreader.ui.data;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.RectF;

import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.data.PageConstants;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.WindowParameters;
import com.onyx.android.sdk.data.model.Annotation;
import com.onyx.android.sdk.data.model.DocumentInfo;
import com.onyx.android.sdk.reader.api.ReaderDocumentMetadata;
import com.onyx.android.sdk.reader.host.request.ChangeViewConfigRequest;
import com.onyx.android.sdk.reader.host.request.StartSideNodeRequest;
import com.onyx.android.sdk.reader.host.request.StopSideNodeRequest;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.android.sdk.reader.common.BaseReaderRequest;
import com.onyx.android.sdk.reader.common.ReaderUserDataInfo;
import com.onyx.android.sdk.reader.common.ReaderViewInfo;
import com.onyx.android.sdk.reader.host.math.PageUtils;
import com.onyx.android.sdk.reader.host.request.CloseRequest;
import com.onyx.android.sdk.reader.host.request.PreRenderRequest;
import com.onyx.android.sdk.reader.host.request.RenderRequest;
import com.onyx.android.sdk.reader.host.request.SaveDocumentOptionsRequest;
import com.onyx.android.sdk.reader.host.wrapper.Reader;
import com.onyx.android.sdk.reader.host.wrapper.ReaderManager;
import com.onyx.kreader.device.DeviceConfig;
import com.onyx.kreader.note.NoteManager;
import com.onyx.kreader.note.actions.CloseNoteMenuAction;
import com.onyx.kreader.note.receiver.DeviceReceiver;
import com.onyx.kreader.tts.ReaderTtsManager;
import com.onyx.kreader.ui.ReaderBroadcastReceiver;
import com.onyx.kreader.ui.ReaderPainter;
import com.onyx.kreader.ui.actions.ExportAnnotationAction;
import com.onyx.kreader.ui.actions.ShowReaderMenuAction;
import com.onyx.kreader.ui.events.TextSelectionEvent;
import com.onyx.kreader.ui.events.*;
import com.onyx.kreader.ui.handler.HandlerManager;
import com.onyx.kreader.ui.highlight.ReaderSelectionManager;
import com.onyx.android.sdk.reader.utils.PagePositionUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ming on 16/7/27.
 */
public class ReaderDataHolder {

    public enum DocumentOpenState { INIT, OPENING, OPENED }

    public enum SideNoteArea { LEFT, RIGHT }

    private Context context;
    private String documentPath;
    private Reader reader;
    private ReaderViewInfo readerViewInfo;
    private ReaderUserDataInfo readerUserDataInfo;

    private HandlerManager handlerManager;
    private ReaderSelectionManager selectionManager;
    private ReaderTtsManager ttsManager;
    private NoteManager noteManager;
    private DataManager dataManager;
    private DeviceReceiver deviceReceiver = new DeviceReceiver();
    private EventBus eventBus = new EventBus();
    private EventReceiver eventReceiver;

    private boolean preRender = true;
    private boolean preRenderNext = true;
    private DocumentOpenState documentOpenState = DocumentOpenState.INIT;
    private boolean documentInitRendered = false;

    private int displayWidth;
    private int displayHeight;
    private int optionsSkippedTimes = 0;
    private int lastRequestSequence;

    private boolean isSideReadingMode = false;

    private boolean enteringSideNote = false;
    private int orientationBeforeSideNote = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private boolean sideNoting = false;
    private SideNoteArea sideNoteArea;
    private int sideNotePage = 0;

    private ReaderPainter readerPainter = new ReaderPainter();

    private WindowParameters windowParameters = new WindowParameters();

    private boolean buttonShowTabWidgetVisible = false;

    /**
     * can be either Dialog or DialogFragment, so we store it as basic Object
     */
    private Set<Object> activeDialogs = new HashSet<>();

    public ReaderDataHolder(Context context) {
        this.context = context;
        ReaderBroadcastReceiver.setEventBus(eventBus);
        sideNoteArea = SingletonSharedPreference.getSideNotePos();
    }

    public Context getContext() {
        return context;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void saveReaderViewInfo(final BaseReaderRequest request) {
        readerViewInfo = request.getReaderViewInfo();
    }

    public void saveReaderUserDataInfo(final BaseReaderRequest request) {
        readerUserDataInfo = request.getReaderUserDataInfo();
    }

    public final ReaderViewInfo getReaderViewInfo() {
        return readerViewInfo;
    }

    public boolean supportTextPage() {
        return getReaderViewInfo() != null && getReaderViewInfo().supportTextPage;
    }

    public boolean isFixedPageDocument() {
        return getReaderViewInfo() != null && getReaderViewInfo().isFixedDocument;
    }

    public boolean isFlowDocument() {
        return !isFixedPageDocument();
    }

    public boolean supportNoteFunc() {
        return !DeviceConfig.sharedInstance(getContext()).isDisableNoteFunc() && isFixedPageDocument();
    }

    public boolean supportScalable() {
        return getReaderViewInfo() != null && getReaderViewInfo().supportScalable;
    }

    public boolean supportFontSizeAdjustment() {
        return getReaderViewInfo() != null && getReaderViewInfo().supportFontSizeAdjustment;
    }

    public boolean supportTypefaceAdjustment() {
        return getReaderViewInfo() != null && getReaderViewInfo().supportTypefaceAdjustment;
    }

    public boolean canPan() {
        return getReaderViewInfo() != null && getReaderViewInfo().canPan();
    }

    public boolean supportSearchByPage() {
        return isFixedPageDocument() && supportScalable();
    }

    public final List<PageInfo> getVisiblePages() {
        ArrayList<PageInfo> pages = new ArrayList<>();

        PageInfo firstPage = getFirstPageInfo();
        if (firstPage == null) {
            return pages;
        }
        if (!supportScalable()) {
            firstPage.setSubPage(-1);
        }

        pages.add(firstPage);

        if (sideNoting) {
            if (sideNoteArea == SideNoteArea.LEFT) {
                float width = firstPage.getDisplayRect().width();
                firstPage.getDisplayRect().left = getDocPageLeft();
                firstPage.getDisplayRect().right = firstPage.getDisplayRect().left + width;

                RectF viewportRect = new RectF(getDocPageLeft(), 0, getDocPageRight(), displayHeight);
                PageUtils.updateVisibleRect(firstPage, viewportRect);
            }

            PageInfo subNotePage = new PageInfo(firstPage.getName(),
                    firstPage.getRange().startPosition,
                    firstPage.getRange().endPosition,
                    firstPage.getOriginWidth(),
                    firstPage.getOriginHeight());

            RectF pageRect = new RectF(0, 0, subNotePage.getOriginWidth(),
                    subNotePage.getOriginHeight());
            RectF viewportRect = new RectF(getSideNotePageLeft(), 0, getSideNotePageRight(), displayHeight);
            float scale = PageUtils.scaleToFitRect(pageRect, viewportRect);

            subNotePage.setScale(scale);
            subNotePage.updateDisplayRect(pageRect);

            PageUtils.updateVisibleRect(subNotePage, viewportRect);

            subNotePage.setSubPage(getSubPageIndex());
            pages.add(subNotePage);
        }

        return pages;
    }

    public final ReaderUserDataInfo getReaderUserDataInfo() {
        return readerUserDataInfo;
    }

    public void setPreRenderNext(boolean preRenderNext) {
        this.preRenderNext = preRenderNext;
    }

    public Reader getReader() {
        return reader;
    }

    public void initReaderFromPath(final String path) {
        documentOpenState = DocumentOpenState.OPENING;
        documentPath = path;
        reader = ReaderManager.getReader(documentPath);
    }

    public boolean isDocumentOpening() {
        return documentOpenState == DocumentOpenState.OPENING;
    }

    public boolean isDocumentOpened() {
        return documentOpenState == DocumentOpenState.OPENED && reader != null;
    }

    public boolean isNoteWritingProvider() {
        return getHandlerManager().getActiveProviderName().equals(HandlerManager.SCRIBBLE_PROVIDER) ||
                getHandlerManager().getActiveProviderName().equals(HandlerManager.SIDE_NOTE_PROVIDER);
    }

    public boolean inReadingProvider() {
        return getHandlerManager().getActiveProviderName().equals(HandlerManager.READING_PROVIDER);
    }

    public boolean isNoteDirty() {
        return noteManager != null && getNoteManager().isNoteDirty();
    }

    public int getPageCount() {
        return reader.getNavigator().getTotalPage();
    }

    public String getCurrentPageName() {
        return getReaderViewInfo().getFirstVisiblePage().getName();
    }

    public int getCurrentPage() {
        return PagePositionUtils.getPageNumber(getCurrentPageName());
    }

    public String getCurrentPagePosition() {
        return getReaderViewInfo().getFirstVisiblePage().getPositionSafely();
    }

    public final PageInfo getFirstPageInfo() {
        return getReaderViewInfo().getFirstVisiblePage();
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public Rect getDisplayRect() {
        return new Rect(0, 0, getDisplayWidth(), getDisplayHeight());
    }

    public void setDisplaySize(int width, int height) {
        displayWidth = width;
        displayHeight = height;
    }

    public int getDocumentViewportWidth() {
        return sideNoting ? displayWidth / 2 : displayWidth;
    }

    public int getDocumentViewportHeight() {
        return displayHeight;
    }

    public boolean canCurrentPageScaleDown() {
        final float toPageScale = PageUtils.scaleToPage(getFirstPageInfo().getOriginWidth(),
                getFirstPageInfo().getOriginHeight(),
                getReader().getViewOptions().getViewWidth(),
                getReader().getViewOptions().getViewHeight());
        return getReaderViewInfo().getFirstVisiblePage().getActualScale() > toPageScale;
    }

    public boolean canCurrentPageScaleUp() {
        return getReaderViewInfo().getFirstVisiblePage().getActualScale() < PageConstants.MAX_SCALE;
    }

    private void registerDeviceReceiver() {
        deviceReceiver.setSystemUIChangeListener(new DeviceReceiver.SystemUIChangeListener() {
            @Override
            public void onSystemUIChanged(String type, boolean open) {
                getEventBus().post(new SystemUIChangedEvent(open));
            }

            @Override
            public void onHomeClicked() {
                getEventBus().post(new HomeClickEvent());
            }
        });
        deviceReceiver.registerReceiver(getContext());
    }

    private void unregisterReceiver() {
        deviceReceiver.unregisterReceiver(getContext());
    }

    public final HandlerManager getHandlerManager() {
        if (handlerManager == null) {
            handlerManager = new HandlerManager(this);
        }
        return handlerManager;
    }

    public ReaderSelectionManager getSelectionManager() {
        if (selectionManager == null) {
            selectionManager = new ReaderSelectionManager();
        }
        return selectionManager;
    }

    public ReaderTtsManager getTtsManager() {
        if (ttsManager == null) {
            ttsManager = new ReaderTtsManager(this);
        }
        return ttsManager;
    }

    public void notifyTtsStateChanged() {
        getEventBus().post(new TtsStateChangedEvent());
    }

    public void notifyUpdateSlideshowStatusBar() {
        getEventBus().post(new UpdateSlideshowEvent());
    }

    public void notifyTtsRequestSentence() {
        getEventBus().post(new TtsRequestSentenceEvent());
    }

    public void notifyTtsError() {
        getEventBus().post(new TtsErrorEvent());
    }

    private void updateReaderMenuState() {
        if (ShowReaderMenuAction.isReaderMenuShown()) {
            new ShowReaderMenuAction().execute(ReaderDataHolder.this, null);
        }
    }

    public NoteManager getNoteManager() {
        if (noteManager == null) {
            noteManager = new NoteManager(getContext(), getEventBus());
        }
        return noteManager;
    }

    public DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }
        return dataManager;
    }

    public void updateRawEventProcessor() {
        if (!supportScalable()) {
            getNoteManager().stopRawEventProcessor();
            return;
        }
        getNoteManager().startRawEventProcessor();
        getNoteManager().pauseRawEventProcessor();
    }

    public void stopRawEventProcessor() {
        if (!supportScalable()) {
            return;
        }
        getNoteManager().stopRawEventProcessor();
    }

    public void resumeRawEventProcessor() {
        if (!supportScalable()) {
            return;
        }
        getNoteManager().resumeRawEventProcessor(getContext());
    }

    public void enablePenShortcut()  {
        if (!supportScalable()) {
            return;
        }
        getNoteManager().setEnableShortcutDrawing(true);
        getNoteManager().setEnableShortcutErasing(true);
    }

    public void disablePenShortcut() {
        if (!supportScalable()) {
            return;
        }
        getNoteManager().setEnableShortcutDrawing(false);
        getNoteManager().setEnableShortcutErasing(false);
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public String getBookName() {
        return FileUtils.getFileName(getDocumentPath());
    }

    public String getBookTitle() {
        if (getReaderUserDataInfo().getDocumentMetadata() == null) {
            return null;
        }
        return getReaderUserDataInfo().getDocumentMetadata().getTitle();
    }

    public boolean supportNoteExport() {
        if (StringUtils.isNullOrEmpty(documentPath) ||
                !documentPath.toLowerCase().endsWith(".pdf")) {
            return false;
        }
        return true;
    }

    public boolean hasBookmark() {
        return getFirstVisiblePageWithBookmark() != null;
    }

    public PageInfo getFirstVisiblePageWithBookmark() {
        for (PageInfo pageInfo : getVisiblePages()) {
            if (getReaderUserDataInfo().hasBookmark(pageInfo)) {
                return pageInfo;
            }
        }
        return null;
    }

    public void submitNonRenderRequest(final BaseReaderRequest request) {
        submitNonRenderRequest(request, null);
    }

    public void submitNonRenderRequest(final BaseReaderRequest request, final BaseCallback callback) {
        beforeSubmitRequest();
        reader.submitRequest(context, request, new BaseCallback() {
            @Override
            public void beforeDone(BaseRequest request, Throwable e) {
                BaseCallback.invokeBeforeDone(callback, request, e);
            }

            @Override
            public void done(BaseRequest request, Throwable e) {
                callback.invoke(callback, request, e);
            }
        });
    }

    public void submitRenderRequest(final BaseReaderRequest renderRequest) {
        submitRenderRequest(renderRequest, null);
    }

    public void submitRenderRequest(final BaseReaderRequest renderRequest, final BaseCallback callback) {
        beforeSubmitRequest();
        reader.submitRequest(context, renderRequest, new BaseCallback() {
            @Override
            public void beforeDone(BaseRequest request, Throwable e) {
                BaseCallback.invokeBeforeDone(callback, request, e);
            }

            @Override
            public void done(BaseRequest request, Throwable e) {
                onRenderRequestFinished(renderRequest, e);
                callback.invoke(callback, request, e);
                onPageDrawFinished(renderRequest, e);
            }
        });
    }

    private void beforeSubmitRequest() {
        getNoteManager().resetNoteDataInfo();
    }

    private void onPageDrawFinished(BaseReaderRequest request, Throwable e) {
        if (e != null || request.isAbort()) {
            return;
        }
        updateReaderMenuState();
        saveDocumentOptions(request);
        preRenderNext();
    }

    private void saveDocumentOptions(final BaseReaderRequest request) {
        if (!request.isSaveOptions()) {
            return;
        }
        final int MAX_SKIP_TIMES = 5;
        if (optionsSkippedTimes < MAX_SKIP_TIMES) {
            optionsSkippedTimes++;
        } else {
            submitNonRenderRequest(new SaveDocumentOptionsRequest());
            optionsSkippedTimes = 0;
        }
    }

    public void preRenderNext() {
        if (!preRender) {
            return;
        }
        final PreRenderRequest request = new PreRenderRequest(preRenderNext);
        getReader().submitRequest(context, request, null);
    }

    public void onRenderRequestFinished(final BaseReaderRequest request, Throwable e) {
        onRenderRequestFinished(request, e, true, true);
    }

    public void changeEpdUpdateMode(final UpdateMode mode) {
        getEventBus().post(new ChangeEpdUpdateModeEvent(mode));
    }

    public void resetEpdUpdateMode() {
        getEventBus().post(new ResetEpdUpdateModeEvent());
    }

    public void onRenderRequestFinished(final BaseReaderRequest request,
                                        Throwable e,
                                        boolean applyGCIntervalUpdate,
                                        boolean renderShapeData) {
        Debug.d(getClass(), "onRenderRequestFinished: " + request);
        if (e != null || request.isAbort()) {
            return;
        }

        if (sideNoting && !isSamePage(readerViewInfo, request.getReaderViewInfo())) {
            sideNotePage = 0;
        }

        boolean disableRegal = needDisableRegal(request);

        saveReaderViewInfo(request);
        saveReaderUserDataInfo(request);
        setLastRequestSequence(request.getRequestSequence());
        getEventBus().post(RequestFinishEvent.createEvent(request.getRequestSequence(), applyGCIntervalUpdate, renderShapeData, false, disableRegal));
        if (getReaderViewInfo() != null && getReaderViewInfo().layoutChanged) {
            getEventBus().post(new LayoutChangeEvent());
        }
    }

    private boolean needDisableRegal(final BaseReaderRequest request) {
        // disable regal to avoid screen flash
        return request instanceof RenderRequest ||
                request instanceof ChangeViewConfigRequest ||
                request instanceof StartSideNodeRequest ||
                request instanceof StopSideNodeRequest;
    }

    private boolean isSamePage(ReaderViewInfo left, ReaderViewInfo right) {
        if (left == null || right == null) {
            return false;
        }
        String leftPage = left.getFirstVisiblePage().getPositionSafely();
        String rightPage = right.getFirstVisiblePage().getPositionSafely();
        return leftPage.compareTo(rightPage) == 0;
    }

    public void updatePinchZoomMenu(final PinchZoomEvent event) {
        getEventBus().post(event);
    }

    public void redrawPage() {
        if (getReader() != null) {
            submitRenderRequest(new RenderRequest());
        }
    }

    public void redrawPage(BaseCallback callback) {
        if (getReader() != null) {
            submitRenderRequest(new RenderRequest(), callback);
        }
    }

    public void showReaderSettings() {
        getEventBus().post(new ShowReaderSettingsEvent());
    }

    public WindowParameters getWindowParameters() {
        return windowParameters;
    }

    public boolean isButtonShowTabWidgetVisible() {
        return buttonShowTabWidgetVisible;
    }

    public void setButtonShowTabWidgetVisible(boolean buttonShowTabWidgetVisible) {
        this.buttonShowTabWidgetVisible = buttonShowTabWidgetVisible;
    }

    public ReaderPainter getReaderPainter() {
        return readerPainter;
    }

    public void addActiveDialog(Dialog dialog) {
        activeDialogs.add(dialog);
    }

    public void removeActiveDialog(Dialog dialog) {
        activeDialogs.remove(dialog);
    }

    public void trackDialog(final Dialog dialog) {
        if (dialog == null) {
            return;
        }
        addActiveDialog(dialog);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                removeActiveDialog(dialog);
            }
        });
    }

    public void closeActiveDialogs() {
        for (Object dialog : activeDialogs) {
            if (dialog instanceof Dialog) {
                ((Dialog) dialog).dismiss();
            }
            if (dialog instanceof DialogFragment) {
                ((DialogFragment) dialog).dismiss();
            }
        }
        activeDialogs.clear();
    }

    public boolean isAnyActiveDialog() {
        return activeDialogs.size() > 0;
    }

    public void destroy(final BaseCallback callback) {
        unregisterReceiver();
        closeActiveDialogs();
        closeTts();
        closeNoteManager();
        closeNoteMenu();
        resetHandlerManager();
        closeDocument(callback);
    }

    public void resetHandlerManager() {
        getHandlerManager().resetToDefaultProvider();
    }

    private void closeDocument(final BaseCallback callback) {
        documentInitRendered = false;
        documentOpenState = DocumentOpenState.INIT;
        if (reader == null || reader.getDocument() == null) {
            BaseCallback.invoke(callback, null, null);
            return;
        }

        final CloseRequest closeRequest = new CloseRequest();
        submitNonRenderRequest(closeRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                onDocumentClosed();
                ReaderManager.releaseReader(documentPath);
                BaseCallback.invoke(callback, request, e);
            }
        });
    }

    public void closeNoteMenu() {
        new CloseNoteMenuAction().execute(this, null);
    }

    public void closeTts() {
        if (ttsManager != null) {
            ttsManager.shutdown();
            ttsManager = null;
        }
    }

    private void closeNoteManager() {
        if (noteManager == null) {
            return;
        }
        getNoteManager().enableRawEventProcessor(false);
        getNoteManager().stopRawEventProcessor();
    }

    public int getLastRequestSequence() {
        return lastRequestSequence;
    }

    public void setLastRequestSequence(int lastRequestSequence) {
        this.lastRequestSequence = lastRequestSequence;
    }

    public boolean isSideReadingMode() {
        return isSideReadingMode;
    }

    public void setSideReadingMode(boolean sideReadingMode) {
        isSideReadingMode = sideReadingMode;
    }

    public boolean supportSideNote() {
        return !isSideReadingMode() && DeviceConfig.sharedInstance(getContext()).isSupportSideNote();
    }

    public boolean isEnteringSideNote() {
        return enteringSideNote;
    }

    public void setEnteringSideNote(boolean enteringSideNote) {
        this.enteringSideNote = enteringSideNote;
    }

    public int getOrientationBeforeSideNote() {
        return orientationBeforeSideNote;
    }

    public void setOrientationBeforeSideNote(int orientationBeforeSideNote) {
        this.orientationBeforeSideNote = orientationBeforeSideNote;
    }

    public boolean isSideNoting() {
        return sideNoting;
    }

    public void setSideNoting(boolean sideNoting) {
        this.sideNoting = sideNoting;
        if (!sideNoting) {
            sideNotePage = 0;
        }
    }

    public SideNoteArea getSideNoteArea() {
        return sideNoteArea;
    }

    public void setSideNoteArea(SideNoteArea targetArea) {
        sideNoteArea = targetArea;
        SingletonSharedPreference.setSideNotePos(sideNoteArea);
    }

    public void toggleSideNoteArea() {
        setSideNoteArea(sideNoteArea == SideNoteArea.LEFT
                ? SideNoteArea.RIGHT : SideNoteArea.LEFT);
    }

    public int getDocPageDialogPosX(){
        int posX = (getDisplayWidth() / 4);
        posX = sideNoteArea == SideNoteArea.LEFT ? posX : -posX;
        return posX;
    }

    public int getSideNoteDialogPosX() {
        int posX = (getDisplayWidth() / 4);
        posX = sideNoteArea == SideNoteArea.LEFT ? -posX : posX;
        return posX;
    }

    public int getDocPageLeft() {
        if (!sideNoting) {
            return 0;
        }
        return sideNoteArea == SideNoteArea.RIGHT ? 0 : getDisplayWidth() / 2;
    }

    public int getDocPageRight() {
        if (!sideNoting) {
            return getDisplayWidth();
        }

        return sideNoteArea == SideNoteArea.RIGHT ? getDisplayWidth() / 2 : getDisplayWidth();
    }

    public int getSideNotePageLeft() {
        return sideNoteArea == SideNoteArea.LEFT ? 0 : getDisplayWidth() / 2;
    }

    public int getSideNotePageRight() {
        return sideNoteArea == SideNoteArea.LEFT ? getDisplayWidth() / 2 : getDisplayWidth();
    }

    public boolean isInDocPageRegion(int x, int y) {
        return getDocPageLeft() <= x && x <= getDocPageRight() &&
                0 <= y && y <= getDisplayHeight();
    }

    public int getSideNoteStartSubPageIndex() {
        return supportScalable() ? 1 : 0;
    }

    public int getSideNotePage() {
        return sideNotePage;
    }

    public int getSubPageIndex() {
        return supportScalable() ? sideNotePage + 1 : sideNotePage;
    }

    public void setSideNotePage(int sideNotePage) {
        this.sideNotePage = sideNotePage;
    }

    public void setSideNotePageBySubPageIndex(int subPageIndex) {
        this.sideNotePage = subPageIndex - getSideNoteStartSubPageIndex();
    }

    public boolean isSideNotePage(PageInfo pageInfo) {
        return pageInfo.getSubPage() >= getSideNoteStartSubPageIndex();
    }

    public int getSideNotePageCount() {
        int count = noteManager.getNoteDocument().getSubPageCount(getFirstPageInfo().getRange());
        if (supportScalable()) {
            count -= 1;
        }
        return count <= 0 ? 1 : count;
    }

    public void previousSideNotePage() {
        if (sideNotePage > 0) {
            sideNotePage--;
        }
    }

    public void nextSideNotePage() {
        if (sideNotePage < getSideNotePageCount() - 1) {
            sideNotePage++;
        }
    }

    public void gotoSideNotePage(int page) {
        if (page >= 0 && page <= getSideNotePageCount() - 1) {
            sideNotePage = page;
        }
    }

    public void prepareEventReceiver() {
        if (eventReceiver == null) {
            eventReceiver = new EventReceiver(getContext());
            getEventBus().register(eventReceiver);
        }
    }

    public void onDocumentOpened() {
        prepareEventReceiver();
        registerDeviceReceiver();
        documentOpenState = DocumentOpenState.OPENED;
        ReaderDocumentMetadata metadata = getReader().getDocumentMetadataSafely();
        DocumentInfo documentInfo = DocumentInfo.create(metadata.getAuthors(),
                getReader().getDocumentMd5(),
                getBookName(),
                documentPath,
                metadata.getTitle());
        getEventBus().post(new DocumentOpenEvent(getContext(), documentInfo));
    }

    public void onDocumentClosed() {
        getEventBus().post(new DocumentCloseEvent(getContext()));
        if (eventReceiver != null) {
            getEventBus().unregister(eventReceiver);
            eventReceiver = null;
        }
    }

    public void onActivityPause() {
        getEventBus().post(new ActivityPauseEvent(getContext()));
    }

    public void onActivityResume() {
        getEventBus().post(new ActivityResumeEvent(getContext()));
    }

    public void onDocumentInitRendered() {
        documentInitRendered = true;
        getEventBus().post(new DocumentInitRenderedEvent());
    }

    public boolean isDocumentInitRendered() {
        return documentInitRendered;
    }

    public final PageChangedEvent beforePageChange() {
        final PageChangedEvent pageChangedEvent = PageChangedEvent.beforePageChange(this);
        return pageChangedEvent;
    }

    public void afterPageChange(final PageChangedEvent pageChangedEvent) {
        pageChangedEvent.afterPageChange(this);
        getEventBus().post(pageChangedEvent);
        if (!getReaderViewInfo().canNextScreen) {
            // TODO: 2017/2/16 default value because  not finish comment
            getEventBus().post(new DocumentFinishEvent(getContext(), "", 100));
        }
    }

    public void onTextSelected(final Annotation annotation) {
        if (annotation == null) {
            return;
        }
        String originText = annotation.getQuote();
        String userNote = annotation.getNote();
        if (StringUtils.isBlank(userNote)) {
            final TextSelectionEvent event = TextSelectionEvent.onTextSelected(getContext(), originText);
            getEventBus().post(event);
            return;
        }
        final AnnotationEvent event = AnnotationEvent.onAddAnnotation(getContext(), originText, userNote);
        getEventBus().post(event);
    }

    public void exportAnnotation(final Annotation annotation) {
        if (annotation == null) {
            return;
        }
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(annotation);
        new ExportAnnotationAction(annotations, true, false).execute(this, null);
    }

    public void onDictionaryLookup(final String text) {
        final DictionaryLookupEvent event = DictionaryLookupEvent.create(getContext(), text);
        getEventBus().post(event);
    }

    public void onNetworkChanged(boolean connected, int networkType) {
        final NetworkChangedEvent event = NetworkChangedEvent.create(getContext(), connected, networkType);
        getEventBus().post(event);
    }

    public void enterSlideshow() {
        getEventBus().post(new SlideshowStartEvent());
    }
}


