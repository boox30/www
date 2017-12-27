package com.onyx.jdread.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.ui.view.PageRecyclerView;
import com.onyx.android.sdk.utils.PreferenceManager;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.action.InitFunctionBarAction;
import com.onyx.jdread.adapter.FunctionBarAdapter;
import com.onyx.jdread.common.BaseFragment;
import com.onyx.jdread.common.ViewConfig;
import com.onyx.jdread.databinding.ActivityMainBinding;
import com.onyx.jdread.event.ChangeChildViewEvent;
import com.onyx.jdread.event.PopCurrentChildViewEvent;
import com.onyx.jdread.event.PushChildViewToStackEvent;
import com.onyx.jdread.event.UsbDisconnectedEvent;
import com.onyx.jdread.library.ui.LibraryFragment;
import com.onyx.jdread.model.FunctionBarItem;
import com.onyx.jdread.model.FunctionBarModel;
import com.onyx.jdread.model.MainViewModel;
import com.onyx.jdread.model.SystemBarModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentManager fragmentManager = null;
    private FragmentTransaction transaction;
    private BaseFragment currentFragment;
    private String currentChildViewName;
    private Map<String, BaseFragment> childViewList = new HashMap<>();
    private ActivityMainBinding binding;
    private FunctionBarModel functionBarModel;
    private FunctionBarAdapter functionBarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            if (! Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,10);
            }
        }
        initView();
        initLibrary();
        initData();
    }

    private void initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    private void initLibrary() {
        EventBus.getDefault().register(this);
    }

    private void initData() {
        binding.setMainViewModel(new MainViewModel());
        initSystemBar();
        initFunctionBar();
        switchCurrentFragment(LibraryFragment.class.getName());
    }

    private void initSystemBar() {
        binding.mainSystemBar.setSystemBarModel(new SystemBarModel());
    }

    private void initFunctionBar() {
        functionBarModel = new FunctionBarModel();
        binding.mainFunctionBar.setFunctionBarModel(functionBarModel);
        PageRecyclerView functionBarRecycler = getFunctionBarRecycler();
        functionBarRecycler.setLayoutManager(new DisableScrollGridManager(getApplicationContext()));
        functionBarAdapter = new FunctionBarAdapter();
        setFunctionAdapter(functionBarRecycler);
    }

    private void setFunctionAdapter(PageRecyclerView functionBarRecycler) {
        boolean show = PreferenceManager.getBooleanValue(JDReadApplication.getInstance(), R.string.show_back_tab_key, true);
        PreferenceManager.setBooleanValue(JDReadApplication.getInstance(), R.string.show_back_tab_key, show);
        int col = getResources().getInteger(R.integer.function_bar_col);
        functionBarAdapter.setRowAndCol(functionBarAdapter.getRowCount(), show ? col : col - 1);
        functionBarRecycler.setAdapter(functionBarAdapter);
        updateFunctionBar();
    }

    private void updateFunctionBar() {
        InitFunctionBarAction initFunctionBarAction = new InitFunctionBarAction(functionBarModel);
        initFunctionBarAction.execute(JDReadApplication.getDataBundle(), new RxCallback() {
            @Override
            public void onNext(Object o) {
                updateFunctionBarView();
            }
        });
    }

    private void isShowBackTab(boolean show) {
        PreferenceManager.setBooleanValue(JDReadApplication.getInstance(), R.string.show_back_tab_key, show);
        setFunctionAdapter(getFunctionBarRecycler());
    }

    private void updateFunctionBarView() {
        PageRecyclerView barRecycler = getFunctionBarRecycler();
        if (barRecycler == null) {
            return;
        }
        barRecycler.getAdapter().notifyDataSetChanged();
    }

    private PageRecyclerView getFunctionBarRecycler() {
        return binding.mainFunctionBar.functionBarRecycler;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void switchCurrentFragment(String childViewName) {
        if (StringUtils.isNotBlank(currentChildViewName) && currentChildViewName.equals(childViewName)) {
            return;
        }
        initFragmentManager();
        notifyChildViewChangeWindow();
        BaseFragment baseFragment = getPageView(childViewName);

        transaction.replace(R.id.main_content_view, baseFragment);
        transaction.commitAllowingStateLoss();
        functionBarModel.changeTabSelection(childViewName);
        saveChildViewInfo(childViewName, baseFragment);
    }

    private void notifyChildViewChangeWindow() {
        if (currentFragment != null) {
            currentFragment.hideWindow();
        }
        if (currentFragment != null && currentFragment.isVisible()) {
            transaction.hide(currentFragment);
        }
    }

    private void initFragmentManager() {
        if (fragmentManager == null) {
            fragmentManager = getSupportFragmentManager();
        }
        transaction = fragmentManager.beginTransaction();
    }

    private void saveChildViewInfo(String childViewName, BaseFragment baseFragment) {
        childViewList.put(childViewName, baseFragment);
        currentFragment = baseFragment;
        currentChildViewName = childViewName;
    }

    private BaseFragment getPageView(String childViewName) {
        BaseFragment baseFragment = childViewList.get(childViewName);
        if (baseFragment == null) {
            try {
                Class clazz = Class.forName(childViewName);
                baseFragment = (BaseFragment) clazz.newInstance();
                baseFragment.setViewEventCallBack(childViewEventCallBack);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        if (baseFragment != null) {
            setChildViewInfo(baseFragment);
        }
        return baseFragment;
    }

    private void setChildViewInfo(BaseFragment baseFragment) {
        baseFragment.setViewEventCallBack(childViewEventCallBack);
    }

    private BaseFragment.ChildViewEventCallBack childViewEventCallBack = new BaseFragment.ChildViewEventCallBack() {
        @Override
        public void gotoView(String childClassName) {
            PushChildViewToStackEvent event = new PushChildViewToStackEvent();
            event.childClassName = childClassName;
            EventBus.getDefault().post(event);
        }

        @Override
        public void viewBack() {
            PopCurrentChildViewEvent event = new PopCurrentChildViewEvent();
            EventBus.getDefault().post(event);
        }

        @Override
        public void hideOrShowSystemBar(boolean flags) {

        }

        @Override
        public void hideOrShowFunctionBar(boolean flags) {
            functionBarModel.setIsShow(flags);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPushChildViewToStackEvent(PushChildViewToStackEvent event) {
        ViewConfig.FunctionModule functionModule = ViewConfig.findChildViewParentId(event.childClassName);
        switchCurrentFragment(event.childClassName);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPopCurrentChildViewEvent(PopCurrentChildViewEvent event) {
        String childClassName = currentFragment.getClass().getName();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangeChildViewEvent(ChangeChildViewEvent event) {
        switchCurrentFragment(event.childViewName);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFunctionBarTabModel(FunctionBarItem event) {
        functionBarModel.changeTabSelection(event.fragmentName.get());
    }

    @Subscribe
    public void onUsbDisconnectedEvent(UsbDisconnectedEvent event){
        JDReadApplication.getInstance().dealWithMptBuffer();
    }
}
