package com.onyx.jdread.reader.data;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import com.onyx.android.sdk.data.PageInfo;
import com.onyx.android.sdk.data.ReaderTextStyle;
import com.onyx.android.sdk.reader.api.ReaderDocument;
import com.onyx.android.sdk.reader.api.ReaderDocumentMetadata;
import com.onyx.android.sdk.reader.api.ReaderException;
import com.onyx.android.sdk.reader.api.ReaderFormManager;
import com.onyx.android.sdk.reader.api.ReaderHitTestManager;
import com.onyx.android.sdk.reader.api.ReaderNavigator;
import com.onyx.android.sdk.reader.api.ReaderPlugin;
import com.onyx.android.sdk.reader.api.ReaderPluginOptions;
import com.onyx.android.sdk.reader.api.ReaderRenderer;
import com.onyx.android.sdk.reader.api.ReaderRendererFeatures;
import com.onyx.android.sdk.reader.api.ReaderSearchManager;
import com.onyx.android.sdk.reader.api.ReaderSelection;
import com.onyx.android.sdk.reader.api.ReaderTextStyleManager;
import com.onyx.android.sdk.reader.api.ReaderView;
import com.onyx.android.sdk.reader.cache.BitmapReferenceLruCache;
import com.onyx.android.sdk.reader.cache.ReaderBitmapReferenceImpl;
import com.onyx.android.sdk.reader.common.ReaderViewInfo;
import com.onyx.android.sdk.reader.host.impl.ReaderDocumentOptionsImpl;
import com.onyx.android.sdk.reader.host.impl.ReaderViewOptionsImpl;
import com.onyx.android.sdk.reader.host.math.PageUtils;
import com.onyx.android.sdk.reader.host.options.BaseOptions;
import com.onyx.android.sdk.reader.plugins.alreader.AlReaderPlugin;
import com.onyx.android.sdk.reader.plugins.comic.ComicReaderPlugin;
import com.onyx.android.sdk.reader.plugins.djvu.DjvuReaderPlugin;
import com.onyx.android.sdk.reader.plugins.images.ImagesReaderPlugin;
import com.onyx.android.sdk.reader.plugins.jeb.JEBReaderPlugin;
import com.onyx.android.sdk.reader.plugins.neopdf.NeoPdfJniWrapper;
import com.onyx.android.sdk.reader.plugins.neopdf.NeoPdfReaderPlugin;
import com.onyx.android.sdk.reader.plugins.netnovel.NetNovelReaderPlugin;
import com.onyx.android.sdk.reader.reflow.ImageReflowManager;
import com.onyx.android.sdk.reader.utils.ImageUtils;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.RectUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.reader.common.DocumentInfo;
import com.onyx.jdread.reader.layout.ReaderLayoutManager;
import com.onyx.jdread.reader.menu.common.ReaderConfig;

import org.apache.lucene.analysis.cn.AnalyzerAndroidWrapper;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huxiaomao on 2017/12/20.
 */

public class ReaderHelper {
    private static final String TAG = ReaderHelper.class.getSimpleName();
    private String documentMd5;
    private DocumentInfo documentInfo;
    private ReaderDocument readerDocument;
    private ReaderViewOptionsImpl viewOptions = new ReaderViewOptionsImpl();
    private ReaderPlugin plugin;
    private ReaderView view;
    private ReaderNavigator navigator;
    private ReaderRenderer renderer;
    private ReaderRendererFeatures rendererFeatures;
    private ReaderTextStyleManager textStyleManager;
    private ReaderSearchManager searchManager;
    private ReaderFormManager formManager;
    private ReaderHitTestManager hitTestManager;
    private ReaderLayoutManager readerLayoutManager = null;
    private BaseOptions documentOptions;
    private ImageReflowManager imageReflowManager;
    private BitmapReferenceLruCache bitmapCache;
    private ReaderBitmapReferenceImpl currentPageBitmap;
    private Context context;
    private boolean isLoadComplete = false;

    public ReaderHelper(Context context,DocumentInfo documentInfo) {
        this.context = context;
        this.documentInfo = documentInfo;
    }

    public boolean isLoadComplete() {
        return isLoadComplete;
    }

    public void setLoadComplete(boolean loadComplete) {
        isLoadComplete = loadComplete;
    }

    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public Context getContext() {
        return context;
    }

    public boolean selectPlugin(final Context context, final DocumentInfo documentInfo, final ReaderPluginOptions pluginOptions) {
        if (NetNovelReaderPlugin.accept(documentInfo.getBookPath())) {
            plugin = new NetNovelReaderPlugin(context, pluginOptions);
        } else if (NeoPdfReaderPlugin.accept(documentInfo.getBookPath())) {
            plugin = new NeoPdfReaderPlugin(context, pluginOptions);
        } else if (AlReaderPlugin.accept(documentInfo.getBookPath())) {
            plugin = new AlReaderPlugin(context, pluginOptions);
        } else if (JEBReaderPlugin.accept(documentInfo.getBookPath())) {
            plugin = new JEBReaderPlugin(context, pluginOptions);
        } else if (ImagesReaderPlugin.accept(documentInfo.getBookPath())) {
            plugin = new ImagesReaderPlugin(context, pluginOptions);
        } else if (DjvuReaderPlugin.accept(documentInfo.getBookPath())) {
            plugin = new DjvuReaderPlugin(context, pluginOptions);
        } else if (ComicReaderPlugin.accept(documentInfo.getBookPath())) {
            plugin = new ComicReaderPlugin(context, pluginOptions);
        }
        return (plugin != null);
    }

    public void saveReaderDocument(ReaderDocument readerDocument,DocumentInfo documentInfo) {
        this.readerDocument = readerDocument;
        setFileMd5(documentInfo);
        initData(context);
    }

    public void setFileMd5(DocumentInfo documentInfo){
        try {
            if (StringUtils.isNotBlank(getDocumentOptions().getMd5())) {
                documentMd5 = getDocumentOptions().getMd5();
            } else {
                documentMd5 = FileUtils.computeMD5(new File(documentInfo.getBookPath()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateView() {
        view = readerDocument.getView(viewOptions);
        renderer = view.getRenderer();
        navigator = view.getNavigator();
        rendererFeatures = renderer.getRendererFeatures();
        textStyleManager = view.getTextStyleManager();
        hitTestManager = view.getReaderHitTestManager();
        searchManager = view.getSearchManager();
        formManager = view.getFormManager();
    }

    public ReaderDocument openDocument(final String path, final BaseOptions baseOptions, final ReaderPluginOptions pluginOptions) throws Exception {
        this.documentOptions = baseOptions;
        ReaderDocumentOptionsImpl readerDocumentOptions = documentOptions.documentOptions();

        if (NeoPdfReaderPlugin.isJEB(path)) {
            //jeb(epub)
            readerDocumentOptions.setDocumentKey(documentInfo.getSecurityInfo().getKey());
            readerDocumentOptions.setDocumentDeviceUUID(documentInfo.getSecurityInfo().getUuId());
            readerDocumentOptions.setDocumentRandom(documentInfo.getSecurityInfo().getRandom());
            //jeb(pdf)
            readerDocumentOptions.setDocumentPassword(getJebPassword(documentInfo.getSecurityInfo().getKey(),
                    documentInfo.getSecurityInfo().getUuId(), documentInfo.getSecurityInfo().getRandom()));
        }

        return plugin.open(path,readerDocumentOptions , pluginOptions);
    }

    private String getJebPassword(String key,String uuid,String random){
        JSONObject object = new JSONObject();
        String password = "";
        try {
            object.put(NeoPdfJniWrapper.TAG_KEY, key);
            object.put(NeoPdfJniWrapper.TAG_UU_ID, uuid);
            object.put(NeoPdfJniWrapper.TAG_RANDOM, random);
            password = object.toString();
        }catch (Exception e){

        }
        return password;
    }

    public boolean closeDocument() {
        if (getDocument() == null) {
            return false;
        }
        getDocument().close();
        onDocumentClosed();
        return true;
    }

    public ReaderLayoutManager getReaderLayoutManager() {
        if (readerLayoutManager == null) {
            readerLayoutManager = new ReaderLayoutManager(this,
                    getDocument(),
                    getNavigator(),
                    getRendererFeatures(),
                    getTextStyleManager(),
                    getViewOptions());
        }
        return readerLayoutManager;
    }



    public boolean nextScreen() throws ReaderException {
        return getReaderLayoutManager().nextScreen();
    }

    public boolean previousScreen() throws ReaderException{
        return getReaderLayoutManager().prevScreen();
    }

    public boolean gotoPosition(String position) throws ReaderException{
        return getReaderLayoutManager().gotoPosition(position);
    }

    public void onDocumentClosed() {
        clearBitmapCache();
        clearImageReflowManager();
        releaseWordAnalyzer();
    }

    public void updateViewportSize(int newWidth, int newHeight) throws ReaderException {
        getViewOptions().setSize(newWidth, newHeight);
        updateView();
        getReaderLayoutManager().init();
        getReaderLayoutManager().updateViewportSize();
    }

    public ImageReflowManager getImageReflowManager() {
        return imageReflowManager;
    }

    public void onLayoutChanged() {
        setLayoutChanged(true);
    }

    public void onPositionChanged() {
    }

    public void beforeDraw(ReaderBitmapReferenceImpl bitmap) {
    }

    public void afterDraw(ReaderBitmapReferenceImpl bitmap) {
    }

    public final ReaderBitmapReferenceImpl getCurrentPageBitmap() {
        return currentPageBitmap;
    }

    public ReaderPlugin getPlugin() {
        return plugin;
    }

    public void setAbortFlag() {
        getPlugin().abortCurrentJob();
    }

    public void clearAbortFlag() {
        getPlugin().clearAbortFlag();
    }

    public boolean isReaderLayoutManagerCreated() {
        return (readerLayoutManager != null);
    }

    public ReaderHitTestManager getHitTestManager() {
        return hitTestManager;
    }

    public void saveToCache(ReaderBitmapReferenceImpl renderBitmap) {
        if (currentPageBitmap != null && currentPageBitmap.isValid()) {
            addToCache(currentPageBitmap);
        }
        currentPageBitmap = renderBitmap;
    }

    public void addToCache(ReaderBitmapReferenceImpl bitmap) {
        if (bitmapCache != null) {
            bitmapCache.put(bitmap.getKey(), bitmap);
        }
    }

    public void dumpCache() {
        if (bitmapCache == null) {
            return;
        }
        for (Map.Entry<String, ReaderBitmapReferenceImpl> entry : bitmapCache.snapshot().entrySet()) {
            Log.e("bitmap cache########", "key: " + entry.getKey() + " value: " + entry.getValue());
        }
    }

    public BitmapReferenceLruCache getBitmapCache() {
        return bitmapCache;
    }

    public void setLayoutChanged(boolean layoutChanged) {

    }

    public boolean isLayoutChanged() {
        return false;
    }

    public void initData(Context context) {
        initImageReflowManager(context);
        initBitmapCache();
        initWordAnalyzerInBackground(context);
    }

    private void initLayoutManager() {

    }

    private void initWordAnalyzerInBackground(final Context context) {
        AnalyzerAndroidWrapper.initialize(context.getApplicationContext(), true);
    }

    private void initImageReflowManager(Context context) {
        if (imageReflowManager == null) {
            File cacheLocation = new File(context.getCacheDir(), ImageReflowManager.class.getCanonicalName());
            if (!cacheLocation.exists()) {
                cacheLocation.mkdirs();
            }
            imageReflowManager = new ImageReflowManager(documentMd5,
                    cacheLocation,
                    getViewOptions().getViewWidth(),
                    getViewOptions().getViewHeight());
        }
    }

    private void clearImageReflowManager() {
        if (imageReflowManager != null) {
            imageReflowManager.release();
        }
    }

    private void initBitmapCache() {
        if (bitmapCache == null) {
            bitmapCache = new BitmapReferenceLruCache(3);
        }
    }

    public void clearBitmapCache() {
        if (currentPageBitmap != null && currentPageBitmap.isValid()) {
            currentPageBitmap.close();
            currentPageBitmap = null;
        }
        if (bitmapCache != null) {
            bitmapCache.clear();
        }
    }

    private void releaseWordAnalyzer() {
        AnalyzerAndroidWrapper.release();
    }

    public ReaderPluginOptions getPluginOptions() {
        return null;
    }

    public ReaderViewOptionsImpl getViewOptions() {
        return viewOptions;
    }

    public BaseOptions getDocumentOptions() {
        return documentOptions;
    }

    public ReaderDocument getDocument() {
        return readerDocument;
    }

    public ReaderView getView() {
        return null;
    }

    public ReaderNavigator getNavigator() {
        return navigator;
    }

    public ReaderRenderer getRenderer() {
        return renderer;
    }

    public ReaderRendererFeatures getRendererFeatures() {
        return rendererFeatures;
    }

    public ReaderTextStyleManager getTextStyleManager() {
        return textStyleManager;
    }

    public ReaderSearchManager getSearchManager() {
        return searchManager;
    }

    public ReaderFormManager getFormManager() {
        return formManager;
    }

    private void translateToScreen(PageInfo pageInfo, List<RectF> list) {
        for (RectF rect : list) {
            PageUtils.translate(pageInfo.getDisplayRect().left,
                    pageInfo.getDisplayRect().top,
                    pageInfo.getActualScale(),
                    rect);
        }
    }

    public List<RectF> collectTextRectangleList(final ReaderViewInfo viewInfo) {
        final List<ReaderSelection> selectionList = getHitTestManager().allText(viewInfo.getFirstVisiblePageName());
        if (selectionList == null) {
            return null;
        }
        final List<RectF> list = new ArrayList<>();
        for (ReaderSelection selection : selectionList) {
            list.addAll(selection.getRectangles());
        }
        translateToScreen(viewInfo.getFirstVisiblePage(), list);
        return list;
    }

    public void applyPostBitmapProcess(final ReaderViewInfo viewInfo, ReaderBitmapReferenceImpl bitmap) {
        applyGamma(viewInfo, bitmap);
        applyEmbolden(bitmap);
        applySaturation(bitmap);
    }

    private void applyGamma(final ReaderViewInfo viewInfo, final ReaderBitmapReferenceImpl bitmap) {
        boolean applyImageGammaOnly = true;
        final RectF parent = new RectF(0, 0, bitmap.getBitmap().getWidth(), bitmap.getBitmap().getHeight());
        if (applyImageGammaOnly) {
            final List<RectF> imageRegions = new ArrayList();
            imageRegions.add(parent);
            applyImageGamma(bitmap, imageRegions);
        } else {
            final List<RectF> regions = collectTextRectangleList(viewInfo);
            final List<RectF> imageRegions = RectUtils.cutRectByExcludingRegions(parent, regions);
            applyTextGamma(bitmap);
            applyImageGamma(bitmap, imageRegions);
        }
    }

    private void applyTextGamma(final ReaderBitmapReferenceImpl bitmap) {
        if (getDocumentOptions().isTextGamaCorrectionEnabled() &&
                getRenderer().getRendererFeatures().supportFontGammaAdjustment()) {
            bitmap.setTextGammaCorrection(getDocumentOptions().getTextGammaLevel());
        }
    }

    private void applyImageGamma(final ReaderBitmapReferenceImpl bitmap, final List<RectF> regions) {
        float level = getDocumentOptions().getGammaLevel();
        if (getDocumentOptions().isGammaCorrectionEnabled() && !bitmap.isGammaApplied(level)) {
            if (ImageUtils.applyGammaCorrection(bitmap.getBitmap(), level, regions)) {
                bitmap.setGammaCorrection(level);
            }
        }
    }

    private void applyEmbolden(final ReaderBitmapReferenceImpl bitmap) {
        if (getDocumentOptions().isEmboldenLevelEnabled() &&
                bitmap.getEmboldenLevel() != getDocumentOptions().getEmboldenLevel()) {
            if (ImageUtils.applyBitmapEmbolden(bitmap.getBitmap(), getDocumentOptions().getEmboldenLevel())) {
                bitmap.setEmboldenLevel(getDocumentOptions().getEmboldenLevel());
            }
        }
    }

    private void applySaturation(final ReaderBitmapReferenceImpl bitmap) {
    }

    public ReaderDocumentMetadata getDocumentMetadata() {
        return null;
    }

    /**
     * collect all options from reader components to BaseOptions.
     */
    public void saveOptions() {
        if (!isReaderLayoutManagerCreated()) {
            return;
        }
        try {
            final ReaderLayoutManager layoutManager = getReaderLayoutManager();
            getDocumentOptions().setLayoutType(layoutManager.getCurrentLayoutType());
            getDocumentOptions().setSpecialScale(layoutManager.getSpecialScale());
            getDocumentOptions().setActualScale(layoutManager.getActualScale());
            getDocumentOptions().setCurrentPage(layoutManager.getCurrentPagePosition());
            getDocumentOptions().setTotalPage(getNavigator().getTotalPage());
            getDocumentOptions().setViewport(layoutManager.getViewportRect());
            getDocumentOptions().setNavigationArgs(layoutManager.getCurrentLayoutProvider().getNavigationArgs());
            getDocumentOptions().setReflowOptions(getImageReflowManager().getSettings().jsonString());
            getDocumentOptions().setReadProgress(getNavigator().getProgress(getReaderLayoutManager().getCurrentPagePosition()));
        } catch (Exception e) {

        }
    }

    public void saveStyle(final SettingInfo settingInfo) {
        final ReaderTextStyle style = getTextStyleManager().getStyle();
        saveReaderTextStyle(style,settingInfo);
    }

    private void saveReaderTextStyle(final ReaderTextStyle style,final SettingInfo settingInfo) {
        if (style == null) {
            return;
        }
        getDocumentOptions().setFontFace(style.getFontFace());
        getDocumentOptions().setFontSize(style.getFontSize().getValue());
        getDocumentOptions().setParagraphSpacing(style.getParagraphSpacing().getPercent());
        getDocumentOptions().setLineSpacing(style.getLineSpacing().getPercent());
        getDocumentOptions().setParagraphIndent(style.getIndent().getIndent());
        getDocumentOptions().setLeftMargin(style.getPageMargin().getLeftMargin().getPercent());
        getDocumentOptions().setTopMargin(style.getPageMargin().getTopMargin().getPercent());
        getDocumentOptions().setRightMargin(style.getPageMargin().getRightMargin().getPercent());
        getDocumentOptions().setBottomMargin(style.getPageMargin().getBottomMargin().getPercent());
        ReaderConfig.saveUserSetting(style,settingInfo);
        ReaderConfig.setReaderChineseConvertType(getDocumentOptions().getChineseConvertType());
        ReaderConfig.setEmboldenLevel(getDocumentOptions().getEmboldenLevel());
    }

    public String getDocumentMd5() {
        return documentMd5;
    }
}
