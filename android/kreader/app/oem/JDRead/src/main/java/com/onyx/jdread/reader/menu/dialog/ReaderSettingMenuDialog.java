package com.onyx.jdread.reader.menu.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.RatingBar;
import android.widget.SeekBar;

import com.onyx.android.sdk.data.ReaderTextStyle;
import com.onyx.android.sdk.reader.common.ReaderViewInfo;
import com.onyx.android.sdk.reader.reflow.ImageReflowSettings;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.ui.view.PageRecyclerView;
import com.onyx.android.sdk.utils.PreferenceManager;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.databinding.ReaderSettingMenuBinding;
import com.onyx.jdread.main.adapter.FunctionBarAdapter;
import com.onyx.jdread.main.model.FunctionBarModel;
import com.onyx.jdread.main.model.SystemBarModel;
import com.onyx.jdread.reader.actions.InitReaderViewFunctionBarAction;
import com.onyx.jdread.reader.common.ReaderUserDataInfo;
import com.onyx.jdread.reader.data.ReaderDataHolder;
import com.onyx.jdread.reader.menu.actions.UpdatePageInfoAction;
import com.onyx.jdread.reader.menu.event.GotoPageEvent;
import com.onyx.jdread.reader.menu.event.ReaderSettingMenuDialogHandler;
import com.onyx.jdread.reader.menu.model.ReaderMarginModel;
import com.onyx.jdread.reader.menu.model.ReaderImageModel;
import com.onyx.jdread.reader.menu.model.ReaderPageInfoModel;
import com.onyx.jdread.reader.menu.model.ReaderSettingModel;
import com.onyx.jdread.reader.menu.model.ReaderTextModel;
import com.onyx.jdread.reader.menu.model.ReaderTitleBarModel;
import com.onyx.jdread.setting.model.BrightnessModel;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by huxiaomao on 17/5/10.
 */

public class ReaderSettingMenuDialog extends Dialog implements ReaderSettingViewBack{
    private static final String TAG = ReaderSettingMenuDialog.class.getSimpleName();
    private ReaderSettingMenuBinding binding;
    private ReaderDataHolder readerDataHolder;
    private FunctionBarModel functionBarModel;
    private BrightnessModel brightnessModel;
    private FunctionBarAdapter functionBarAdapter;
    private ReaderSettingMenuDialogHandler readerSettingMenuDialogHandler;

    public ReaderSettingMenuDialog(ReaderDataHolder readerDataHolder, @NonNull Activity activity) {
        super(activity, android.R.style.Theme_Translucent_NoTitleBar);
        this.readerDataHolder = readerDataHolder;

        readerSettingMenuDialogHandler = new ReaderSettingMenuDialogHandler(readerDataHolder,this);
    }

    public ReaderSettingMenuDialogHandler getReaderSettingMenuDialogHandler() {
        return readerSettingMenuDialogHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        initView();
        registerListener();
        initData();
    }

    private void initData() {
        initReaderSettingMenu();
        initFunctionBar();
        initSystemBar();
        initReaderPageInfoBar();
        initReaderTitleBar();
        initBrightnessBar();
        initTextBar();
        initImageBar();
        initCustomizeBar();
    }

    private void initView() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.reader_setting_menu, null, false);
        setContentView(binding.getRoot());
        readerSettingMenuDialogHandler.setBinding(binding);
    }

    private void initReaderSettingMenu(){
        binding.setReaderSettingModel(new ReaderSettingModel());
    }

    private void initReaderPageInfoBar(){
        binding.readerSettingPageInfoBar.setReaderPageInfoModel(new ReaderPageInfoModel());
        new UpdatePageInfoAction(binding,readerDataHolder.getReaderViewInfo()).execute(readerDataHolder,null);
        initReaderPageInfoEvent();
    }

    private void initReaderPageInfoEvent(){
        binding.readerSettingPageInfoBar.readerPageInfoMenuReadProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.readerSettingPageInfoBar.getReaderPageInfoModel().setCurrentPage(seekBar.getProgress());
                GotoPageEvent event = new GotoPageEvent(seekBar.getProgress());
                EventBus.getDefault().post(event);
            }
        });
    }

    private void initSystemBar() {
        binding.readerSettingSystemBar.setSystemBarModel(new SystemBarModel());
    }

    private void initTextBar(){
        binding.readerSettingTextSettingBar.setReaderTextModel(new ReaderTextModel());
    }

    private void initImageBar(){
        binding.readerSettingImageSettingBar.setReaderImageModel(new ReaderImageModel());
    }

    private void initCustomizeBar(){
        binding.readerSettingCustomizeFormatBar.setReaderMarginModel(new ReaderMarginModel());
        initCustomizeEvent();
    }

    private void initCustomizeEvent() {
        binding.readerSettingCustomizeFormatBar.readerLineSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.readerSettingCustomizeFormatBar.getReaderMarginModel().setLineSpacingProgress(seekBar.getProgress());
            }
        });

        binding.readerSettingCustomizeFormatBar.readerSegmentSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.readerSettingCustomizeFormatBar.getReaderMarginModel().setSegmentProgress(seekBar.getProgress());
            }
        });

        binding.readerSettingCustomizeFormatBar.readerLefAndRightSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.readerSettingCustomizeFormatBar.getReaderMarginModel().setLeftAndRightProgress(seekBar.getProgress());
            }
        });

        binding.readerSettingCustomizeFormatBar.readerUpAndDownSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.readerSettingCustomizeFormatBar.getReaderMarginModel().setUpAndDownProgress(seekBar.getProgress());
            }
        });
    }

    private void initBrightnessBar(){
        brightnessModel = new BrightnessModel();
        binding.readerSettingBrightnessBar.setBrightnessModel(brightnessModel);
        initEvent();
    }

    private void initEvent() {
        binding.readerSettingBrightnessBar.ratingbarLightSettings.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                brightnessModel.setBrightness(ratingBar.getProgress());
            }
        });
    }

    private void initReaderTitleBar() {
        binding.readerSettingTitleBar.setReaderTitleBarModel(new ReaderTitleBarModel());
        updateBookmarkState();
    }

    public void updateBookmarkState(){
        boolean isBookmark = readerSettingMenuDialogHandler.hasBookmark();
        binding.readerSettingTitleBar.getReaderTitleBarModel().setBookMarkImageId(isBookmark);
    }

    private void initFunctionBar() {
        functionBarModel = new FunctionBarModel();
        binding.readerSettingFunctionBar.setFunctionBarModel(functionBarModel);
        PageRecyclerView functionBarRecycler = getFunctionBarRecycler();
        functionBarRecycler.setLayoutManager(new DisableScrollGridManager(getContext()));
        functionBarAdapter = new FunctionBarAdapter();
        setFunctionAdapter(functionBarRecycler);
    }

    private void setFunctionAdapter(PageRecyclerView functionBarRecycler) {
        boolean show = PreferenceManager.getBooleanValue(JDReadApplication.getInstance(), R.string.show_back_tab_key, false);
        PreferenceManager.setBooleanValue(JDReadApplication.getInstance(), R.string.show_back_tab_key, show);
        int col = getContext().getResources().getInteger(R.integer.function_bar_col);
        functionBarAdapter.setRowAndCol(functionBarAdapter.getRowCount(), show ? col : col - 1);
        functionBarRecycler.setAdapter(functionBarAdapter);
        updateFunctionBar();
    }

    private void updateFunctionBar() {
        InitReaderViewFunctionBarAction initReaderViewFunctionBarAction = new InitReaderViewFunctionBarAction(functionBarModel, new RxCallback() {
            @Override
            public void onNext(Object o) {
                updateFunctionBarView();
            }
        });
        initReaderViewFunctionBarAction.execute(readerDataHolder,null);
    }

    private void updateFunctionBarView() {
        PageRecyclerView barRecycler = getFunctionBarRecycler();
        if (barRecycler == null) {
            return;
        }
        barRecycler.getAdapter().notifyDataSetChanged();
    }

    private PageRecyclerView getFunctionBarRecycler() {
        return binding.readerSettingFunctionBar.functionBarRecycler;
    }

    private void registerListener() {
        readerSettingMenuDialogHandler.registerListener();
    }

    @Override
    public void dismiss() {
        readerSettingMenuDialogHandler.unregisterListener();
        super.dismiss();
    }

    @Override
    public Dialog getContent() {
        return this;
    }
}