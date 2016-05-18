package com.onyx.kreader.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.onyx.android.cropimage.CropImage;
import com.onyx.android.cropimage.CropImageResultReceiver;
import com.onyx.android.cropimage.data.CropArgs;
import com.onyx.kreader.R;
import com.onyx.kreader.actions.ChangeOrientationAction;
import com.onyx.kreader.actions.NextScreenAction;
import com.onyx.kreader.actions.PreviousScreenAction;
import com.onyx.kreader.api.ReaderDocumentOptions;
import com.onyx.kreader.api.ReaderException;
import com.onyx.kreader.api.ReaderPluginOptions;
import com.onyx.kreader.common.BaseCallback;
import com.onyx.kreader.common.BaseRequest;
import com.onyx.kreader.common.ReaderViewInfo;
import com.onyx.kreader.device.DeviceController;
import com.onyx.kreader.host.impl.ReaderDocumentOptionsImpl;
import com.onyx.kreader.host.impl.ReaderPluginOptionsImpl;
import com.onyx.kreader.host.math.PageInfo;
import com.onyx.kreader.host.navigation.NavigationArgs;
import com.onyx.kreader.host.options.ReaderConstants;
import com.onyx.kreader.host.options.BaseOptions;
import com.onyx.kreader.host.request.*;
import com.onyx.kreader.host.wrapper.Reader;
import com.onyx.kreader.host.wrapper.ReaderManager;
import com.onyx.kreader.reflow.ImageReflowSettings;
import com.onyx.kreader.ui.data.ReaderScalePresets;
import com.onyx.kreader.ui.dialog.DialogReflowSettings;
import com.onyx.kreader.ui.dialog.DialogSetValue;
import com.onyx.kreader.ui.gesture.MyOnGestureListener;
import com.onyx.kreader.ui.gesture.MyScaleGestureListener;
import com.onyx.kreader.ui.handler.HandlerManager;
import com.onyx.kreader.ui.menu.ReaderMenu;
import com.onyx.kreader.ui.menu.ReaderMenuItem;
import com.onyx.kreader.ui.menu.ReaderSideMenu;
import com.onyx.kreader.ui.menu.ReaderSideMenuItem;
import com.onyx.kreader.utils.FileUtils;
import com.onyx.kreader.utils.PagePositionUtils;
import com.onyx.kreader.utils.RawResourceUtil;
import com.onyx.kreader.utils.StringUtils;

import java.io.File;
import java.util.List;

/**
 * Created by Joy on 2016/4/14.
 */
public class ReaderActivity extends ActionBarActivity {
    private final static String TAG = ReaderActivity.class.getSimpleName();

    private Reader reader;
    private ReaderViewInfo readerViewInfo;

    private SurfaceView surfaceView;
    private SurfaceHolder.Callback surfaceHolderCallback;
    private SurfaceHolder holder;

    private ReaderMenu readerMenu;

    private HandlerManager handlerManager;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;

    private boolean preRender = true;
    private CropImageResultReceiver selectionZoomAreaReceiver;
    private DialogSetValue contrastDialog;
    private DialogSetValue emboldenDialog;
    private DialogReflowSettings reflowSettingsDialog;
    private DialogSetValue gotoPageDialog;
    private DialogSetValue cropValueDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen(true);
        setContentView(R.layout.activity_reader);
        initActivity();
    }

    @Override
    protected void onResume() {
        redrawPage();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        resetEventListener();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return processKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return processKeyUp(keyCode, event);
    }

    public boolean tryHitTest(float x, float y) {
        if (getReaderMenu().isShown()) {
            hideReaderMenu();
            return true;
        }
        return false;
    }

    public int displayWidth() {
        return surfaceView.getWidth();
    }

    public int displayHeight() {
        return surfaceView.getHeight();
    }

    public void beforePageChangeByUser() {
    }

    public void nextScreen() {
        final NextScreenAction action = new NextScreenAction();
        action.execute(this);
    }

    public void prevScreen() {
        final PreviousScreenAction action = new PreviousScreenAction();
        action.execute(this);
    }

    public void preRenderNext() {
        if (!preRender) {
            return;
        }

        final PrerenderRequest request = new PrerenderRequest(true);
        reader.submitRequest(this, request, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Exception e) {
                if (e != null) {
                    return;
                }
            }
        });
    }

    public void nextPage() {
        nextScreen();
    }

    public void prevPage() {
        prevScreen();
    }

    public void scaleEnd() {

    }

    public void scaleBegin(ScaleGestureDetector detector) {

    }

    public void scaling(ScaleGestureDetector detector) {

    }

    public void panFinished(int offsetX, int offsetY) {

    }

    public void panning(int offsetX, int offsetY) {

    }

    public void highlight(float x1, float y1, float x2, float y2) {

    }

    public void selectWord(float x1, float y1, float x2, float y2, boolean b) {

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
        initToolBar();
        initSurfaceView();
        initReaderMenu();
        initHandlerManager();
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.findViewById(R.id.toolbar_progress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGotoDialog();
            }
        });
    }

    private void initSurfaceView() {
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        surfaceHolderCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
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
        gestureDetector = new GestureDetector(this, new MyOnGestureListener(this));
        scaleDetector = new ScaleGestureDetector(this, new MyScaleGestureListener(this));
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                handlerManager.setTouchStartEvent(event);
                scaleDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    handlerManager.onActionUp(ReaderActivity.this, event);
                    handlerManager.resetTouchStartPosition();
                }

                handlerManager.onTouchEvent(ReaderActivity.this, event);
                return true;
            }
        });

        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.requestFocusFromTouch();

        // make sure we openLocalFile the doc after surface view is layouted correctly.
        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                surfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                handleActivityIntent();
            }
        });
    }

    private void initHandlerManager() {
        handlerManager = new HandlerManager(this);
        handlerManager.setEnable(false);
    }

    private void initReaderMenu() {
        LinearLayout layout = (LinearLayout)findViewById(R.id.left_drawer);
        createReaderSideMenu(layout);
    }

    private void createReaderSideMenu(LinearLayout drawerLayout) {
        readerMenu = new ReaderSideMenu(this, drawerLayout);
        readerMenu.setReaderMenuCallback(new ReaderMenu.ReaderMenuCallback() {
            @Override
            public void onMenuItemClicked(ReaderMenuItem menuItem) {
                Log.d(TAG, "onMenuItemClicked: " + menuItem.getURI().getRawPath());
                switch (menuItem.getURI().getRawPath()) {
                    case "/Rotation/Rotation0":
                        rotateScreen(0);
                        break;
                    case "/Rotation/Rotation90":
                        rotateScreen(90);
                        break;
                    case "/Rotation/Rotation180":
                        rotateScreen(180);
                        break;
                    case "/Rotation/Rotation270":
                        rotateScreen(270);
                        break;
                    case "/Zoom/ZoomIn":
                        scaleUp();
                        break;
                    case "/Zoom/ZoomOut":
                        scaleDown();
                        break;
                    case "/Zoom/ToPage":
                        scaleToPage();
                        break;
                    case "/Zoom/ToWidth":
                        scaleToWidth();
                        break;
                    case "/Zoom/Crop":
                        scaleByCrop();
                        break;
                    case "/Zoom/ByRect":
                        scaleByRect();
                        break;
                    case "/Navigation/ArticleMode":
                        switchNavigationToArticleMode();
                        break;
                    case "/Navigation/ComicMode":
                        switchNavigationToComicMode();
                        break;
                    case "/Navigation/Reset":
                        resetNavigationMode();
                        break;
                    case "/Navigation/MoreSetting":
                        break;
                    case "/Spacing/DecreaseSpacing":
                        break;
                    case "/Spacing/EnlargeSpacing":
                        break;
                    case "/Spacing/NormalSpacing":
                        break;
                    case "/Spacing/SmallSpacing":
                        break;
                    case "/Spacing/LargeSpacing":
                        break;
                    case "/Spacing/Indent":
                        break;
                    case "/Font/DecreaseSpacing":
                        break;
                    case "/Font/IncreaseSpacing":
                        break;
                    case "/Font/Gamma":
                        adjustContrast();
                        break;
                    case "/Font/Embolden":
                        adjustEmbolden();
                        break;
                    case "/Font/FontReflow":
                        adjustReflowSettings();
                        break;
                    case "/Font/TOC":
                        break;
                    case "/Font/Bookmark":
                        break;
                    case "/Font/Note":
                        break;
                    case "/Font/Scribble":
                        break;
                    case "/Font/Export":
                        break;
                    case "/Exit":
                        onBackPressed();
                        break;
                }
            }
        });
        List<ReaderSideMenuItem> items = createReaderSideMenuItems();
        readerMenu.fillItems(items);
    }

    private void rotateScreen(int rotationOperation) {
        final ChangeOrientationAction action = new ChangeOrientationAction(rotationOperation);
        action.execute(this);
    }

    private List<ReaderSideMenuItem> createReaderSideMenuItems() {
        JSONObject json = JSON.parseObject(RawResourceUtil.contentOfRawResource(this, R.raw.reader_menu));
        JSONArray array = json.getJSONArray("menu_list");
        return ReaderSideMenuItem.createFromJSON(this, array);
    }

    private void resetEventListener() {
        if (selectionZoomAreaReceiver != null) {
            unregisterReceiver(selectionZoomAreaReceiver);
            selectionZoomAreaReceiver = null;
        }
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
                Uri uri = getIntent().getData();
                if (uri == null) {
                    return false;
                }
                openLocalFile(FileUtils.getRealFilePathFromUri(ReaderActivity.this, uri));
            } else if (action.equals(Intent.ACTION_SEARCH)) {
                searchContent(getIntent(), true);
            }
            return true;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void searchContent(Intent intent, boolean b) {

    }

    private void openLocalFile(String path) {
        reader = ReaderManager.getReader(path);
        BaseRequest open = new OpenRequest(path, getDocumentOptions(), getPluginOptions());
        reader.submitRequest(this, open, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Exception e) {
                if (e != null) {
                    return;
                }
                onFileOpenSucceed();
            }
        });
    }

    private void onFileOpenSucceed() {
        hideReaderMenu();
        String name = FileUtils.getFileName(reader.getReaderHelper().getPlugin().getFilePath());
        updateToolbarTitle(name);
        handlerManager.setEnable(true);
        BaseRequest config = new CreateViewRequest(displayWidth(), displayHeight());
        reader.submitRequest(this, config, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Exception e) {
                if (e != null) {
                    return;
                }
                gotoPage(reader.getNavigator().getInitPosition());
            }
        });
    }

    private void gotoPage(int page) {
        gotoPage(String.valueOf(page));
    }

    private void gotoPage(String pageName) {
        BaseRequest gotoPosition = new GotoLocationRequest(pageName);
        submitRenderRequest(gotoPosition);
    }

    private void scaleUp() {
        try {
            float actualScale = reader.getReaderLayoutManager().getActualScale();
            scaleByValue(ReaderScalePresets.scaleUp(actualScale));
        } catch (ReaderException e) {
            e.printStackTrace();
        }
    }

    private void scaleDown() {
        try {
            float actualScale = reader.getReaderLayoutManager().getActualScale();
            scaleByValue(ReaderScalePresets.scaleDown(actualScale));
        } catch (ReaderException e) {
            e.printStackTrace();
        }
    }

    private void scaleByValue(float scale) {
        final ScaleRequest request = new ScaleRequest(getCurrentPageName(), scale, displayWidth() / 2, displayHeight() / 2);
        submitRenderRequest(request);
    }

    private void scaleToPage() {
        final ScaleToPageRequest request = new ScaleToPageRequest(getCurrentPageName());
        submitRenderRequest(request);
    }

    private void scaleToWidth() {
        final ScaleToWidthRequest request = new ScaleToWidthRequest(getCurrentPageName());
        submitRenderRequest(request);
    }

    private void scaleByCrop() {
        showCropValueDialog();
    }

    public DialogSetValue showCropValueDialog() {
        final int minValue = 1;
        final int maxValue = 20;
        final int defaultValue = minValue;
        if (cropValueDialog == null) {
            DialogSetValue.DialogCallback cropCallback = new DialogSetValue.DialogCallback() {
                @Override
                public void valueChange(int newValue) {
                    cropWithValue((double)newValue / maxValue);
                }

                @Override
                public void done(boolean isValueChange, int oldValue, int newValue) {
                    if (!isValueChange) {
                    }
                    hideCropValueDialog();
                }
            };
            cropValueDialog = new DialogSetValue(this, defaultValue, minValue, maxValue, true, true,  getString(R.string.dialog_crop_tittle), getString(R.string.dialog_crop_tittle), cropCallback);
            cropWithValue((double)defaultValue / maxValue);
        }
        cropValueDialog.show();
        return cropValueDialog;
    }

    public void hideCropValueDialog()  {
        if (cropValueDialog != null) {
            cropValueDialog.hide();
            cropValueDialog = null;
        }
    }

    private void cropWithValue(double value) {

    }

    private void showSelectionActivity(final CropArgs args, final CropImage.SelectionCallback callback) {
        Uri outputUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "cropped.png"));
        IntentFilter filter = new IntentFilter();
        filter.addAction(CropImage.INTENT_ACTION_SELECT_ZOOM_RECT);

        if (selectionZoomAreaReceiver != null) {
            unregisterReceiver(selectionZoomAreaReceiver);
        }
        selectionZoomAreaReceiver = new CropImageResultReceiver() {
            @Override
            public void onSelectionFinished(final CropArgs navigationArgs) {
                if (callback != null) {
                    callback.onSelectionFinished(navigationArgs);
                }
            }
        };
        ReaderActivity.this.registerReceiver(selectionZoomAreaReceiver, filter);
        CropImage crop = new CropImage(reader.getViewportBitmap().getBitmap());
        crop.output(outputUri).start(ReaderActivity.this, false, false, false, args);
    }

    private void scaleByRect() {
        CropArgs args = new CropArgs();
        args.manualCropPage = true;

        showSelectionActivity(args, new CropImage.SelectionCallback() {
            @Override
            public void onSelectionFinished(CropArgs args) {
                scaleByRect(new RectF(args.selectionRect));
            }
        });
    }

    private void scaleByRect(RectF rect) {
        if (readerViewInfo == null || readerViewInfo.getVisiblePages() == null ||
                readerViewInfo.getVisiblePages().size() <= 0) {
            return;
        }
        String pn = null;
        for(PageInfo pageInfo : readerViewInfo.getVisiblePages()) {
            if (pageInfo.getDisplayRect().contains(rect)) {
                pn = pageInfo.getName();
                rect = ScaleByRectRequest.rectInDocument(pageInfo, rect);
                break;
            }
        }
        if (pn == null) {
            return;
        }

        final ScaleByRectRequest request = new ScaleByRectRequest(pn, rect);
        submitRenderRequest(request);
    }

    private void switchNavigationToArticleMode() {
        NavigationArgs args = new NavigationArgs();
        RectF limit = new RectF(0, 0, 0, 0);
        args.columnsLeftToRight(NavigationArgs.Type.ALL, 2, 2, limit);
        switchPageNavigationMode(args);
    }

    private void switchNavigationToComicMode() {
        NavigationArgs args = new NavigationArgs();
        RectF limit = new RectF(0, 0, 0, 0);
        args.rowsRightToLeft(NavigationArgs.Type.ALL, 2, 2, limit);
        switchPageNavigationMode(args);
    }

    private void switchPageNavigationMode(NavigationArgs args) {
        BaseRequest request = new ChangeLayoutRequest(ReaderConstants.SINGLE_PAGE_NAVIGATION_LIST, args);
        submitRenderRequest(request);
    }

    private void resetNavigationMode() {
        BaseRequest request = new ChangeLayoutRequest(ReaderConstants.SINGLE_PAGE, new NavigationArgs());
        submitRenderRequest(request);
    }

    private void adjustContrast() {
        showContrastDialog();
    }

    public DialogSetValue showContrastDialog() {
        if (contrastDialog == null) {
            DialogSetValue.DialogCallback callback = new DialogSetValue.DialogCallback() {
                @Override
                public void valueChange(int newValue) {
                    GammaCorrectionRequest request = new GammaCorrectionRequest(newValue);
                    submitRenderRequest(request);
                }

                @Override
                public void done(boolean isValueChange, int oldValue, int newValue) {
                    if (!isValueChange) {
                        GammaCorrectionRequest request = new GammaCorrectionRequest(oldValue);
                        submitRenderRequest(request);
                    }
                    hideContrastDialog();
                }
            };
            float current = reader.getBaseOptions().getGammaLevel();
            contrastDialog = new DialogSetValue(ReaderActivity.this, (int)current, BaseOptions.minGammaLevel(), BaseOptions.maxGammaLevel(), true, true,
                    getString(R.string.dialog_reflow_settings_contrast), getString(R.string.contrast_level), callback);

        }
        contrastDialog.show();
        return contrastDialog;
    }

    private void hideContrastDialog() {
        if (contrastDialog != null) {
            contrastDialog.hide();
            contrastDialog = null;
        }
    }

    private void adjustEmbolden() {
        showEmboldenDialog();
    }

    public DialogSetValue showEmboldenDialog()  {
        if (emboldenDialog == null) {
            DialogSetValue.DialogCallback callback = new DialogSetValue.DialogCallback() {
                @Override
                public void valueChange(int newValue) {
                    EmboldenGlyphRequest request = new EmboldenGlyphRequest(newValue);
                    submitRenderRequest(request);
                }

                @Override
                public void done(boolean isValueChange, int oldValue, int newValue) {
                    if (!isValueChange) {
                        EmboldenGlyphRequest request = new EmboldenGlyphRequest(oldValue);
                        submitRenderRequest(request);
                    }
                    hideEmboldenDialog();
                }
            };
            int current = reader.getBaseOptions().getEmboldenLevel();
            emboldenDialog = new DialogSetValue(ReaderActivity.this, current, BaseOptions.minEmboldenLevel(), BaseOptions.maxEmboldenLevel(), true, true,
                    getString(R.string.embolden), getString(R.string.embolden_level), callback);
        }
        emboldenDialog.show();
        return emboldenDialog;
    }

    private void hideEmboldenDialog() {
        if (emboldenDialog != null) {
            emboldenDialog.hide();
            emboldenDialog = null;
        }
    }

    private void adjustReflowSettings() {
        showReflowSettingsDialog();
    }

    private void showReflowSettingsDialog() {
        if (reflowSettingsDialog == null) {
            ImageReflowSettings settings = reader.getImageReflowSettings();
            settings.dev_width = surfaceView.getWidth();
            settings.dev_height = surfaceView.getHeight();
            reflowSettingsDialog = new DialogReflowSettings(this, settings, new DialogReflowSettings.ReflowCallback() {
                @Override
                public void onFinished(boolean confirm, ImageReflowSettings settings) {
                    if (confirm && settings != null) {
                        BaseRequest request = new ChangeLayoutRequest(ReaderConstants.IMAGE_REFLOW_PAGE, new NavigationArgs());
                        submitRenderRequest(request);
                    }
                    hideReflowSettingsDialog();
                }
            });
        }
        reflowSettingsDialog.show();
    }

    private void hideReflowSettingsDialog() {
        if (reflowSettingsDialog != null) {
            reflowSettingsDialog.hide();
            reflowSettingsDialog = null;
        }
    }

    public void submitRenderRequest(BaseRequest request) {
        reader.submitRequest(this, request, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Exception e) {
                handleRenderRequestFinished(request, e);
                if (preRender) {
                    preRenderNext();
                }
            }
        });
    }

    private void handleRenderRequestFinished(BaseRequest request, Exception e) {
        if (e != null) {
            return;
        }
        readerViewInfo = request.getReaderViewInfo();
        // TODO avoid null pointer error
        String page = request.getReaderViewInfo().getVisiblePages().get(0).getName();
        int pn = Integer.parseInt(page);
        updateToolbarProgress((pn + 1) + "/" + getPageCount());
        DeviceController.applyGCInvalidate(surfaceView);
        drawPage(reader.getViewportBitmap().getBitmap());
        surfaceView.invalidate();
    }

    public void redrawPage() {
        if (reader != null) {
            submitRenderRequest(new RenderRequest());
        }
    }

    private void drawPage(Bitmap bitmap) {
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        Paint paint = new Paint();
        drawBackground(canvas, paint);
        if (bitmap != null) {
            drawBitmap(canvas, paint, bitmap);
        }
        holder.unlockCanvasAndPost(canvas);
    }

    private void drawBackground(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
    }

    private void drawBitmap(Canvas canvas, Paint paint, Bitmap bitmap) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

    private String getCurrentPageName() {
        return reader.getReaderLayoutManager().getCurrentPageName();
    }

    private int getCurrentPage() {
        return PagePositionUtils.getPosition(getCurrentPageName());
    }

    private int getPageCount() {
        return reader.getNavigator().getTotalPage();
    }

    public ReaderPluginOptions getPluginOptions() {
        return new ReaderPluginOptionsImpl();
    }

    public ReaderDocumentOptions getDocumentOptions() {
        return new ReaderDocumentOptionsImpl("", "");
    }

    private void quitApplication() {

    }

    private void openBuiltInDoc() {
        hideLoadingDialog();
    }

    private void hideLoadingDialog() {

    }

    private boolean hasPopupWindow() {
        return getReaderMenu().isShown();
    }

    private void hideAllPopupMenu() {
        hideReaderMenu();
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
        return handlerManager.onKeyDown(this, keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private boolean processKeyUp(int keyCode, KeyEvent event) {
        return handlerManager.onKeyUp(this, keyCode, event) || super.onKeyUp(keyCode, event);
    }

    private ReaderMenu getReaderMenu() {
        if (readerMenu == null) {
            // delay init of reader menu to speed up activity startup
            initReaderMenu();
        }
        return readerMenu;
    }

    public final HandlerManager getHandlerManager() {
        return handlerManager;
    }

    public void showReaderMenu() {
        showToolbar();
        getReaderMenu().show();
    }

    public void hideReaderMenu() {
        hideToolbar();
        getReaderMenu().hide();
    }

    private void showToolbar() {
        findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
    }

    private void hideToolbar() {
        findViewById(R.id.toolbar).setVisibility(View.GONE);
    }

    private void updateToolbarTitle(String title) {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        ((TextView)toolbar.findViewById(R.id.toolbar_title)).setText(title);
    }

    private void updateToolbarProgress(String progress) {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        ((TextView)toolbar.findViewById(R.id.toolbar_progress)).setText(progress);
    }

    public DialogSetValue showGotoDialog() {
        if (gotoPageDialog == null) {
            DialogSetValue.DialogCallback callback = new DialogSetValue.DialogCallback() {
                @Override
                public void valueChange(int newValue) {
                    gotoPage(newValue - 1);
                }

                @Override
                public void done(boolean isValueChange, int oldValue, int newValue) {
                    if (!isValueChange) {
                        gotoPage(oldValue - 1);
                    }
                    hideGotoDialog();
                }
            };
            gotoPageDialog = new DialogSetValue(ReaderActivity.this, getCurrentPage(), 1, getPageCount(), true, true,
                    getString(R.string.go_to_page), getString(R.string.go_to_page), callback);
        }
        gotoPageDialog.show();
        return gotoPageDialog;
    }

    private void hideGotoDialog() {
        if (gotoPageDialog != null) {
            gotoPageDialog.hide();
            gotoPageDialog = null;
        }
    }

    public void setFullScreen(boolean fullScreen) {
        DeviceController.setFullScreen(this, fullScreen);
    }
}