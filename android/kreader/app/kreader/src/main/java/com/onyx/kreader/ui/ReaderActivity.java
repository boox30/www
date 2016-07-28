package com.onyx.kreader.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelXorXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.scribble.request.navigation.PageListRenderRequest;
import com.onyx.android.sdk.ui.data.ReaderStatusInfo;
import com.onyx.android.sdk.ui.view.ReaderStatusBar;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kreader.R;
import com.onyx.kreader.api.ReaderDocumentOptions;
import com.onyx.kreader.api.ReaderPluginOptions;
import com.onyx.kreader.common.Debug;
import com.onyx.kreader.common.ReaderViewInfo;
import com.onyx.kreader.device.ReaderDeviceManager;
import com.onyx.kreader.host.impl.ReaderDocumentOptionsImpl;
import com.onyx.kreader.host.impl.ReaderPluginOptionsImpl;
import com.onyx.kreader.host.wrapper.Reader;
import com.onyx.kreader.host.wrapper.ReaderManager;
import com.onyx.kreader.ui.actions.BackwardAction;
import com.onyx.kreader.ui.actions.ChangeViewConfigAction;
import com.onyx.kreader.ui.actions.ForwardAction;
import com.onyx.kreader.ui.actions.GotoPageAction;
import com.onyx.kreader.ui.actions.GotoPageDialogAction;
import com.onyx.kreader.ui.actions.OpenDocumentAction;
import com.onyx.kreader.ui.actions.SearchContentAction;
import com.onyx.kreader.ui.actions.ShowReaderMenuAction;
import com.onyx.kreader.ui.actions.ShowSearchMenuAction;
import com.onyx.kreader.ui.actions.ShowTextSelectionMenuAction;
import com.onyx.kreader.ui.gesture.MyOnGestureListener;
import com.onyx.kreader.ui.gesture.MyScaleGestureListener;
import com.onyx.kreader.ui.handler.HandlerManager;
import com.onyx.kreader.utils.TreeObserverUtils;

/**
 * Created by Joy on 2016/4/14.
 */
public class ReaderActivity extends ActionBarActivity {
    private final static String TAG = ReaderActivity.class.getSimpleName();
    private static final String DOCUMENT_PATH_TAG = "document";

    private String documentPath;
    private Reader reader;
    private NoteViewHelper noteViewHelper;

    private SurfaceView surfaceView;
    private SurfaceHolder.Callback surfaceHolderCallback;
    private SurfaceHolder holder;

    private ReaderStatusBar statusBar;

    private HandlerManager handlerManager;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;

    private final PixelXorXfermode xorMode = new PixelXorXfermode(Color.WHITE);

    private final ReaderPainter readerPainter = new ReaderPainter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen(true);
        setContentView(R.layout.activity_reader);
        initActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getReaderDataHolder().redrawPage();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ViewTreeObserver observer = surfaceView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                removeGlobalOnLayoutListener(this);
                new ChangeViewConfigAction().execute(getReaderDataHolder());
            }
        });

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void startActivity(Intent intent) {
        // check if search intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            intent.putExtra(DOCUMENT_PATH_TAG, documentPath);
        }

        super.startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            setIntent(intent);
            handleActivityIntent();
        } catch (java.lang.Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        resetMenus();
        super.onDestroy();
    }

    private void resetMenus() {
        ShowReaderMenuAction.resetReaderMenu(this);
        ShowSearchMenuAction.resetSearchMenu();
        ShowTextSelectionMenuAction.resetSelectionMenu();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return processKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return processKeyUp(keyCode, event);
    }

    private final com.onyx.kreader.ui.data.ReaderDataHolder getReaderDataHolder(){
        return handlerManager.getReaderDataHolder();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActivity() {
        initStatusBar();
        initToolbar();
        initHandlerManager();
        initSurfaceView();
        initShapeViewDelegate();
    }

    private void initStatusBar() {
        statusBar = (ReaderStatusBar)findViewById(R.id.status_bar);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_bottom);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.findViewById(R.id.toolbar_backward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backward();
            }
        });
        toolbar.findViewById(R.id.toolbar_forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forward();
            }
        });
        toolbar.findViewById(R.id.toolbar_progress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GotoPageDialogAction().execute(getReaderDataHolder());
            }
        });

        toolbar = (Toolbar)findViewById(R.id.toolbar_top);
        toolbar.findViewById(R.id.toolbar_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowReaderMenuAction.hideReaderMenu();
                onSearchRequested();
            }
        });
    }

    private void initSurfaceView() {
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        surfaceHolderCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                getReaderDataHolder().setDisplayHeight(surfaceView.getHeight());
                getReaderDataHolder().setDisplayWidth(surfaceView.getWidth());
                clearCanvas(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                clearCanvas(holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                holder.removeCallback(surfaceHolderCallback);
                Log.i(TAG, "surface destroyed");
            }
        };

        surfaceView.getHolder().addCallback(surfaceHolderCallback);
        holder = surfaceView.getHolder();
        gestureDetector = new GestureDetector(this, new MyOnGestureListener(getReaderDataHolder()));
        scaleDetector = new ScaleGestureDetector(this, new MyScaleGestureListener(getReaderDataHolder()));
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                handlerManager.setTouchStartEvent(event);
                scaleDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    handlerManager.onActionUp(getReaderDataHolder(), event);
                    handlerManager.resetTouchStartPosition();
                }

                handlerManager.onTouchEvent(getReaderDataHolder(), event);
                return true;
            }
        });

        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.requestFocusFromTouch();

        // make sure we openFileFromIntent the doc after surface view is layouted correctly.
        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeGlobalOnLayoutListener(this);
                handleActivityIntent();
            }
        });
    }

    private void removeGlobalOnLayoutListener(ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < 16) {
            TreeObserverUtils.removeLayoutListenerPre16(surfaceView.getViewTreeObserver(), listener);
        } else {
            TreeObserverUtils.removeLayoutListenerPost16(surfaceView.getViewTreeObserver(), listener);
        }
    }

    private void initHandlerManager() {
        handlerManager = new HandlerManager(this);
        getReaderDataHolder().setCallBack(new com.onyx.kreader.ui.data.ReaderDataHolder.CallBack() {
            @Override
            public void onRenderRequestFinished() {
                updateToolbarProgress();
                updateStatusBar();

                //ReaderDeviceManager.applyGCInvalidate(surfaceView);
                drawPage(reader.getViewportBitmap().getBitmap());
                renderShapeDataInBackground();
            }
        });
        handlerManager.setEnable(false);
    }

    private void initShapeViewDelegate() {
//        getNoteViewHelper().setView(this, surfaceView, null);
        // when page changed, choose to flush
        //noteViewHelper.flushPendingShapes();

    }

    private void clearCanvas(SurfaceHolder holder) {
        if (holder == null) {
            return;
        }
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private boolean handleActivityIntent() {
        try {
            String action = getIntent().getAction();
            if (StringUtils.isNullOrEmpty(action)) {
                openBuiltInDoc();
            } else if (action.equals(Intent.ACTION_MAIN)) {
                quitApplication();
            } else if (action.equals(Intent.ACTION_VIEW)) {
                openFileFromIntent();
            } else if (action.equals(Intent.ACTION_SEARCH)) {
                searchContent(getIntent(), true);
            }
            return true;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void searchContent(Intent intent, boolean forward) {
        final String query = intent.getStringExtra(SearchManager.QUERY);
        if (StringUtils.isNotBlank(query)) {
            new SearchContentAction(getReaderDataHolder().getCurrentPageName(), query, forward).execute(getReaderDataHolder());
        }
    }

    private void openFileFromIntent() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            return;
        }

        final String path = FileUtils.getRealFilePathFromUri(ReaderActivity.this, uri);
        reader = ReaderManager.getReader(path);
        getReaderDataHolder().setReader(reader);
        final OpenDocumentAction action = new OpenDocumentAction(path,this);
        action.execute(getReaderDataHolder());
    }

    private void gotoPage(int page) {
        final GotoPageAction action = new GotoPageAction(String.valueOf(page));
        action.execute(getReaderDataHolder());
    }

    public void onDocumentOpened(String path) {
        documentPath = path;
        hideToolbar();
        updateToolbarTitle();
    }

    public void backward() {
        final BackwardAction backwardAction = new BackwardAction();
        backwardAction.execute(getReaderDataHolder());
    }

    public void forward() {
        final ForwardAction forwardAction = new ForwardAction();
        forwardAction.execute(getReaderDataHolder());
    }

    private void drawPage(final Bitmap pageBitmap) {
        ReaderDeviceManager.applyWithGCInterval(surfaceView);
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        readerPainter.drawPage(this, canvas, pageBitmap, getReaderDataHolder().getReaderUserDataInfo(),
                getReaderDataHolder().getReaderViewInfo(),
                getReaderDataHolder().getSelectionManager(),
                getNoteViewHelper(),
                getReaderDataHolder().getShapeDataInfo());
        holder.unlockCanvasAndPost(canvas);
    }

    private NoteViewHelper getNoteViewHelper() {
        if (noteViewHelper == null) {
            noteViewHelper = new NoteViewHelper();
        }
        return noteViewHelper;
    }

    private boolean isShapeBitmapReady() {
        // TODO
//        if (!hasShapes()) {
//            return false;
//        }

        final Bitmap bitmap = getNoteViewHelper().getViewBitmap();
        if (bitmap == null) {
            return false;
        }
        return true;
    }

    private void renderShapeDataInBackground() {
        if (true || getReaderDataHolder().hasShapes()) {
            return;
        }

        final PageListRenderRequest loadRequest = new PageListRenderRequest(reader.getDocumentMd5(), getReaderDataHolder().getReaderViewInfo().getVisiblePages(), getReaderDataHolder().getDisplayRect());
        getNoteViewHelper().submit(this, loadRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e != null || request.isAbort()) {
                    return;
                }
                getReaderDataHolder().saveShapeDataInfo(loadRequest);
                drawPage(reader.getViewportBitmap().getBitmap());
            }
        });
    }

    public ReaderPluginOptions getPluginOptions() {
        return new ReaderPluginOptionsImpl();
    }

    public ReaderDocumentOptions getDocumentOptions() {
        return new ReaderDocumentOptionsImpl("", "");
    }

    public SurfaceHolder getHolder() {
        return holder;
    }

    public void quitApplication() {

    }

    private void openBuiltInDoc() {
    }

    private boolean hasPopupWindow() {
        return ShowReaderMenuAction.isReaderMenuShown();
    }

    private void hideAllPopupMenu() {
        ShowReaderMenuAction.hideReaderMenu();
    }

    protected boolean askForClose() {
        return false;
    }

    private boolean processKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!hasPopupWindow()){
                if (askForClose()) {
                    return true;
                }
            } else {
                hideAllPopupMenu();
                return true;
            }
        }
        return handlerManager.onKeyDown(getReaderDataHolder(), keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private boolean processKeyUp(int keyCode, KeyEvent event) {
        return handlerManager.onKeyUp(getReaderDataHolder(), keyCode, event) || super.onKeyUp(keyCode, event);
    }

    public final HandlerManager getHandlerManager() {
        return handlerManager;
    }

    public void showToolbar() {
        findViewById(R.id.toolbar_top).setVisibility(View.VISIBLE);
        findViewById(R.id.toolbar_bottom).setVisibility(View.VISIBLE);
    }

    public void hideToolbar() {
        findViewById(R.id.toolbar_top).setVisibility(View.GONE);
        findViewById(R.id.toolbar_bottom).setVisibility(View.GONE);
    }

    private void updateToolbarTitle() {
        String name = FileUtils.getFileName(documentPath);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_top);
        ((TextView)toolbar.findViewById(R.id.toolbar_title)).setText(name);
    }

    private void updateToolbarProgress() {
        ReaderViewInfo readerViewInfo = getReaderDataHolder().getReaderViewInfo();
        if (readerViewInfo != null && readerViewInfo.getFirstVisiblePage() != null) {
            int pn = Integer.parseInt(readerViewInfo.getFirstVisiblePage().getName());
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_bottom);
            ((TextView) toolbar.findViewById(R.id.toolbar_progress)).setText((pn + 1) + "/" + getReaderDataHolder().getPageCount());
        }
    }

    private void updateStatusBar() {
        PageInfo pageInfo = getReaderDataHolder().getFirstPageInfo();
        Rect pageRect = new Rect();
        Rect displayRect = new Rect();
        pageInfo.getPositionRect().round(pageRect);
        translateDisplayRectToViewportRect(pageInfo.getDisplayRect()).round(displayRect);
        Debug.d("pageRect: " + JSON.toJSON(pageRect));
        Debug.d("displayRect: " + JSON.toJSON(displayRect));
        int current = getReaderDataHolder().getCurrentPage() + 1;
        int total = getReaderDataHolder().getPageCount();
        String title = getReaderDataHolder().getBookName();
        statusBar.updateStatusBar(new ReaderStatusInfo(pageRect, displayRect,
                current, total, 0, title));
    }

    private RectF translateDisplayRectToViewportRect(RectF displayRect) {
        RectF rect = new RectF(displayRect);
        rect.intersect(0, 0, getReaderDataHolder().getDisplayWidth(), getReaderDataHolder().getDisplayHeight());
        rect.offset(-displayRect.left, -displayRect.top);
        return rect;
    }

    public void setFullScreen(boolean fullScreen) {
        ReaderDeviceManager.setFullScreen(this, fullScreen);
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }
}