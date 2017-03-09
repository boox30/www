package com.onyx.kreader.ui;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.common.request.WakeLockHolder;
import com.onyx.android.sdk.data.DataManager;
import com.onyx.android.sdk.reader.common.Debug;
import com.onyx.android.sdk.reader.host.options.BaseOptions;
import com.onyx.android.sdk.reader.utils.TreeObserverUtils;
import com.onyx.android.sdk.utils.DeviceUtils;
import com.onyx.android.sdk.utils.FileUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kreader.R;
import com.onyx.kreader.device.DeviceConfig;
import com.onyx.kreader.ui.data.SingletonSharedPreference;
import com.onyx.kreader.ui.requests.LoadDocumentOptionsRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReaderTabHostActivity extends OnyxBaseActivity {

    private static final String TAG = ReaderTabHostActivity.class.getSimpleName();

    private static final String TAG_TAB_MANAGER = "tab_manager";

    private WakeLockHolder startupWakeLock = new WakeLockHolder();

    private ReaderTabManager tabManager = ReaderTabManager.create();
    private TabHost tabHost;
    private TabWidget tabWidget;
    private ImageView btnSwitch;
    private String pathToContinueOpenAfterRotation;

    private boolean insideTabChanging = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        acquireStartupWakeLock();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_host);
        initComponents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        syncFullScreenState();

//        int orientation = SingletonSharedPreference.getScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        if (orientation != DeviceUtils.getScreenOrientation(this)) {
//            setRequestedOrientation(orientation);
//        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        tabHost.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                TreeObserverUtils.removeGlobalOnLayoutListener(tabHost.getViewTreeObserver(), this);
                onScreenOrientationChanged();
            }
        });
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseStartupWakeLock();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleActivityIntent();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TAG_TAB_MANAGER, tabManager.toJson());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tabManager = ReaderTabManager.createFromJson(savedInstanceState.getString(TAG_TAB_MANAGER));
        rebuildTabWidget();
    }

    private void initComponents() {
        initTabHost();
        initReceiver();
    }

    private void initTabHost() {
        tabWidget = (TabWidget) findViewById(android.R.id.tabs);
        btnSwitch = (ImageView) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isShowed = tabWidget.getVisibility() == View.VISIBLE;
                tabWidget.setVisibility(isShowed ? View.INVISIBLE : View.VISIBLE);
                btnSwitch.setImageResource(isShowed ? R.drawable.ic_pack_up : R.drawable.ic_unfold);
            }
        });

        tabHost = (TabHost) findViewById(R.id.tab_host);
        tabHost.setup();

        tabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        tabHost.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // delay the handling of activity intent from onCreate()
                TreeObserverUtils.removeGlobalOnLayoutListener(tabHost.getViewTreeObserver(), this);
                handleActivityIntent();
            }
        });

        if (!tabManager.supportMultipleTabs()) {
            return;
        }

        addTabToHost(ReaderTabManager.ReaderTab.TAB_1, "");
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                ReaderTabManager.ReaderTab tab = Enum.valueOf(ReaderTabManager.ReaderTab.class, tabId);
                if (!insideTabChanging && tabManager.isTabOpened(tab)) {
                    reopenReaderTab(tab);
                }
                onTabSwitched(tab);
                insideTabChanging = false;
            }
        });
        tabHost.setCurrentTab(0);
    }

    private void initReceiver() {
        ReaderTabHostBroadcastReceiver.setCallback(new ReaderTabHostBroadcastReceiver.Callback() {
            @Override
            public void onChangeOrientation(final int orientation) {
                final int current = DeviceUtils.getScreenOrientation(ReaderTabHostActivity.this);
                Debug.d("onChangeOrientation, current: " + current + ", target: " + orientation);
                setRequestedOrientation(orientation);
                SingletonSharedPreference.setScreenOrientation(orientation);
                if (current != orientation && isReverseOrientation(current, orientation)) {
                    onScreenOrientationChanged();
                }
            }

            @Override
            public void onEnterFullScreen() {
                syncFullScreenState();
            }

            @Override
            public void onQuitFullScreen() {
                syncFullScreenState();
            }
        });
    }

    private boolean isReverseOrientation(int current, int target) {
        return (current == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && target == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) ||
                (current == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT && target == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) ||
                (current == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && target == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) ||
                (current == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE && target == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void onScreenOrientationChanged() {
        Debug.d(TAG, "onScreenOrientationChanged");
        if (isShowingTabWidget()) {
            showTabWidget();
        } else {
            hideTabWidget();
        }
        updateReaderTabWindowHeight();

        if (StringUtils.isNotBlank(pathToContinueOpenAfterRotation)) {
            openDocWithTab(pathToContinueOpenAfterRotation);
            pathToContinueOpenAfterRotation = null;
        }
    }

    private void addTabToHost(final ReaderTabManager.ReaderTab tab, final String path) {
        final String name = FileUtils.getFileName(path);

        final TabWidget tabWidget = tabHost.getTabWidget();
        for (int i = 0; i < tabWidget.getTabCount(); i++) {
            if (tabWidget.getChildTabViewAt(i).getTag() == tab) {
                ((TextView)tabWidget.getChildAt(i).findViewById(R.id.text_view_title)).setText(name);
                return;
            }
        }

        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.view_reader_host_tab_indicator, null);
        ((TextView)tabIndicator.findViewById(R.id.text_view_title)).setText(name);

        tabHost.addTab(tabHost.newTabSpec(tab.toString())
                .setIndicator(tabIndicator)
                .setContent(android.R.id.tabcontent));
        tabWidget.getChildTabViewAt(tabWidget.getTabCount() - 1).setTag(tab);
        tabIndicator.findViewById(R.id.image_button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeReaderTab(tab);
            }
        });
    }

    private void rebuildTabWidget() {
        insideTabChanging = true;
        tabHost.clearAllTabs();

        ArrayList<LinkedHashMap.Entry<ReaderTabManager.ReaderTab, String>> reverseList = new ArrayList<>();
        for (LinkedHashMap.Entry<ReaderTabManager.ReaderTab, String> entry : tabManager.getOpenedTabs().entrySet()) {
            reverseList.add(0, entry);
        }

        Debug.d(TAG, "rebuilding tab widget:");
        for (LinkedHashMap.Entry<ReaderTabManager.ReaderTab, String> entry : reverseList) {
            Debug.d(TAG, "rebuilding: " + entry.getKey());
            addTabToHost(entry.getKey(), entry.getValue());
        }
    }

    private void updateCurrentTabInHost(ReaderTabManager.ReaderTab tab) {
        if (tabHost.getTabWidget().getTabCount() > 0 &&
                !tabHost.getCurrentTabTag().equals(tab.toString())) {
            insideTabChanging = true;
            tabHost.setCurrentTabByTag(tab.toString());
        }
    }

    private ReaderTabManager.ReaderTab getCurrentTabInHost() {
        return Enum.valueOf(ReaderTabManager.ReaderTab.class, tabHost.getCurrentTabTag());
    }

    private boolean isShowingTabWidget() {
        return tabManager.getOpenedTabs().size() > 1;
    }

    private void showTabWidget() {
        tabHost.getTabWidget().setVisibility(View.VISIBLE);
        btnSwitch.setVisibility(View.VISIBLE);
    }

    private void hideTabWidget() {
        tabHost.getTabWidget().setVisibility(View.INVISIBLE);
        btnSwitch.setVisibility(View.INVISIBLE);
    }

    private int getTabContentHeight() {
        Debug.d(TAG, "tab host height: " + tabHost.getHeight() +
                ", tab widget height: " + tabHost.getTabWidget().getHeight() +
                ", tab content height: " + tabHost.getTabContentView().getHeight());
        if (!isShowingTabWidget()) {
            return tabHost.getHeight();
        }
        return tabHost.getHeight() - tabHost.getTabWidget().getHeight();
    }

    private void acquireStartupWakeLock() {
        if (startupWakeLock == null) {
            startupWakeLock = new WakeLockHolder();
        }

        startupWakeLock.acquireWakeLock(this, ReaderActivity.class.getSimpleName());
    }

    private void releaseStartupWakeLock() {
        startupWakeLock.releaseWakeLock();
    }

    private void syncFullScreenState() {
        boolean fullScreen = !SingletonSharedPreference.isSystemStatusBarEnabled(this) || DeviceConfig.sharedInstance(this).isSupportColor();
        Debug.d(TAG, "syncFullScreenState: " + fullScreen);
        DeviceUtils.setFullScreenOnResume(this, fullScreen);
        tabHost.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                Debug.d(TAG, "syncFullScreenState -> onGlobalLayout");
                TreeObserverUtils.removeGlobalOnLayoutListener(tabHost.getViewTreeObserver(), this);
                updateReaderTabWindowHeight();
            }
        });
    }

    private boolean handleActivityIntent() {
        try {
            String action = getIntent().getAction();
            if (StringUtils.isNullOrEmpty(action)) {
            } else if (action.equals(Intent.ACTION_MAIN)) {
            } else if (action.equals(Intent.ACTION_VIEW)) {
                handleViewActionIntent();
                return true;
            }
            finish();
            return true;
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        } finally {
            releaseStartupWakeLock();
        }
        return false;
    }

    private void handleViewActionIntent() {
        final String path = FileUtils.getRealFilePathFromUri(this, getIntent().getData());
        final LoadDocumentOptionsRequest loadDocumentOptionsRequest = new LoadDocumentOptionsRequest(path,
                null);
        DataManager dataProvider = new DataManager();
        dataProvider.submit(this, loadDocumentOptionsRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e != null) {
                    return;
                }
                if (waitScreenOrientationChanging(loadDocumentOptionsRequest.getDocumentOptions())) {
                    pathToContinueOpenAfterRotation = path;
                    return;
                }
                openDocWithTab(path);
            }
        });

    }

    private boolean waitScreenOrientationChanging(final BaseOptions options) {
        int target = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (options != null && options.getOrientation() >= 0) {
            target = options.getOrientation();
        }
        int current = DeviceUtils.getScreenOrientation(this);
        Debug.d("current orientation: " + current + ", target orientation: " + target);
        if (current != target) {
            setRequestedOrientation(target);
            if (isReverseOrientation(current, target)) {
                // reverse orientation will not trigger onConfigurationChanged() in activity,
                // so we process as orientation not changed
                return false;
            }
            return true;
        }
        return false;
    }

    private void openDocWithTab(String path) {
        ReaderTabManager.ReaderTab tab = tabManager.findOpenedTabByPath(path);
        if (tab != null) {
            Debug.d(TAG, "file already opened in tab: " + tab + ", " + path);
            reopenReaderTab(tab);
            return;
        }

        tab = getFreeReaderTab();
        openDocWithTab(tab, path);
    }

    private void openDocWithTab(ReaderTabManager.ReaderTab tab, String path) {
        Debug.d(TAG, "openDocWithTab: " + tab + ", " + path);
        addReaderTab(tab, path);

        Intent intent = new Intent(this, tabManager.getTabActivity(tab));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), getIntent().getType());
        intent.putExtras(getIntent());
        final int tabContentHeight = getTabContentHeight();
        intent.putExtra(ReaderBroadcastReceiver.TAG_WINDOW_HEIGHT, tabContentHeight);
        startActivity(intent);
    }

    private ReaderTabManager.ReaderTab getFreeReaderTab() {
        ReaderTabManager.ReaderTab tab = tabManager.pollFreeTab();
        if (tab != null) {
            return tab;
        }
        tab = tabManager.reuseOpenedTab();
//        closeTabActivity(tab);
        return tab;
    }

    private void addReaderTab(ReaderTabManager.ReaderTab tab, String path) {
        tabManager.addOpenedTab(tab, path);
        if (tabManager.supportMultipleTabs()) {
            rebuildTabWidget();
            updateCurrentTabInHost(tab);
            updateReaderTabWindowHeight(tab);

            if (isShowingTabWidget()) {
                showTabWidget();
            }
        }
    }

    private void closeReaderTab(ReaderTabManager.ReaderTab tab) {
        closeTabActivity(tab);
        tabManager.removeOpenedTab(tab);
        rebuildTabWidget();

        if (!isShowingTabWidget()) {
            hideTabWidget();
        }

        if (tabManager.getOpenedTabs().size() <= 0) {
            finish();
        } else {
            reopenReaderTab(getCurrentTabInHost());
        }
    }

    private void closeTabActivity(ReaderTabManager.ReaderTab tab) {
        Intent intent = new Intent(this, tabManager.getTabReceiver(tab));
        intent.setAction(ReaderBroadcastReceiver.ACTION_CLOSE_READER);
        Debug.d(TAG, "sendBroadcast: " + intent);
        sendBroadcast(intent);
    }

    private void reopenReaderTab(ReaderTabManager.ReaderTab tab) {
        if (!bringReaderTabToFront(tab)) {
            openDocWithTab(tab, tabManager.getOpenedTabs().get(tab));
        }
    }

    private boolean bringReaderTabToFront(ReaderTabManager.ReaderTab tab) {
        String clzName = tabManager.getTabActivity(tab).getName();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasksList = am.getRunningTasks(Integer.MAX_VALUE);
        if (!tasksList.isEmpty()) {
            int nSize = tasksList.size();
            for (int i = 0; i < nSize; i++) {
                if (tasksList.get(i).topActivity.getClassName().equals(clzName)) {
                    Debug.d(TAG, "reader tab already in task list, bring it to front: " + tab);
                    updateCurrentTabInHost(tab);
                    updateReaderTabWindowHeight(tab);
                    am.moveTaskToFront(tasksList.get(i).id, 0);
                    return true;
                }
            }
        }
        return false;
    }

    private void updateReaderTabWindowHeight() {
        if (tabHost.getTabWidget().getTabCount() > 0) {
            updateReaderTabWindowHeight(getCurrentTabInHost());
        }
    }

    private void updateReaderTabWindowHeight(ReaderTabManager.ReaderTab tab) {
        final int tabContentHeight = getTabContentHeight();
        Intent intent = new Intent(this, tabManager.getTabReceiver(tab));
        intent.setAction(ReaderBroadcastReceiver.ACTION_RESIZE_WINDOW);
        intent.putExtra(ReaderBroadcastReceiver.TAG_WINDOW_HEIGHT, tabContentHeight);
        sendBroadcast(intent);
    }

    private void onTabSwitched(final ReaderTabManager.ReaderTab tab) {
        final String path = tabManager.getOpenedTabs().get(tab);
        for (LinkedHashMap.Entry<ReaderTabManager.ReaderTab, String> entry : tabManager.getOpenedTabs().entrySet()) {
            Intent intent = new Intent(this, tabManager.getTabReceiver(entry.getKey()));
            intent.setAction(ReaderBroadcastReceiver.ACTION_DOCUMENT_ACTIVATED);
            intent.putExtra(ReaderBroadcastReceiver.TAG_DOCUMENT_PATH, path);
            sendBroadcast(intent);
        }
    }
}
