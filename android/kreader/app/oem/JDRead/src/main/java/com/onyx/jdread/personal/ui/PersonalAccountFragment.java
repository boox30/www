package com.onyx.jdread.personal.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.databinding.PersonalAccountBinding;
import com.onyx.jdread.library.view.DashLineItemDivider;
import com.onyx.jdread.main.common.BaseFragment;
import com.onyx.jdread.main.common.ResManager;
import com.onyx.jdread.main.model.TitleBarModel;
import com.onyx.jdread.personal.action.GetOrderUrlAction;
import com.onyx.jdread.personal.adapter.PersonalAccountAdapter;
import com.onyx.jdread.personal.cloud.entity.jdbean.GetOrderUrlResultBean;
import com.onyx.jdread.personal.cloud.entity.jdbean.UserInfo;
import com.onyx.jdread.personal.dialog.TopUpDialog;
import com.onyx.jdread.personal.event.ConsumptionRecordEvent;
import com.onyx.jdread.personal.event.FiftyYuanEvent;
import com.onyx.jdread.personal.event.FiveHundredYuanEvent;
import com.onyx.jdread.personal.event.HundredYuanEvent;
import com.onyx.jdread.personal.event.OneYuanEvent;
import com.onyx.jdread.personal.event.PaidRecordEvent;
import com.onyx.jdread.personal.event.PointsForEvent;
import com.onyx.jdread.personal.event.ReadVipEvent;
import com.onyx.jdread.personal.event.TenYuanEvent;
import com.onyx.jdread.personal.event.TopUpEvent;
import com.onyx.jdread.personal.event.TwoHundredYuanEvent;
import com.onyx.jdread.personal.model.PersonalAccountModel;
import com.onyx.jdread.personal.model.PersonalDataBundle;
import com.onyx.jdread.setting.event.BackToSettingFragmentEvent;
import com.onyx.jdread.shop.ui.BookVIPReadFragment;
import com.onyx.jdread.util.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by li on 2017/12/29.
 */

public class PersonalAccountFragment extends BaseFragment {
    private PersonalAccountBinding binding;
    private PersonalAccountAdapter personalAccountAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = (PersonalAccountBinding) DataBindingUtil.inflate(inflater, R.layout.fragment_personal_account, container, false);
        initView();
        initData();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.ensureRegister(PersonalDataBundle.getInstance().getEventBus(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.ensureUnregister(PersonalDataBundle.getInstance().getEventBus(), this);
    }

    private void initData() {
        TitleBarModel titleModel = PersonalDataBundle.getInstance().getTitleModel();
        titleModel.title.set(ResManager.getString(R.string.personal_account));
        titleModel.backEvent.set(new BackToSettingFragmentEvent());
        binding.accountTitleBar.setTitleModel(titleModel);

        PersonalAccountModel personalAccountModel = PersonalDataBundle.getInstance().getPersonalAccountModel();
        if (personalAccountAdapter != null) {
            personalAccountAdapter.setData(personalAccountModel.getAccountTitles(), personalAccountModel.getAccountEvents());
        }

        UserInfo userInfo = PersonalDataBundle.getInstance().getUserInfo();
        if (userInfo != null) {
            binding.setUserInfo(userInfo);
        }
    }

    private void initView() {
        binding.accountRecycler.setLayoutManager(new DisableScrollGridManager(JDReadApplication.getInstance()));
        DashLineItemDivider divider = new DashLineItemDivider();
        binding.accountRecycler.addItemDecoration(divider);
        personalAccountAdapter = new PersonalAccountAdapter(PersonalDataBundle.getInstance().getEventBus());
        binding.accountRecycler.setAdapter(personalAccountAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopUpEvent(TopUpEvent event) {
        TopUpDialog dialog = new TopUpDialog();
        dialog.show(getActivity().getFragmentManager(), "");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConsumptionRecordEvent(ConsumptionRecordEvent event) {
        viewEventCallBack.gotoView(ConsumptionRecordFragment.class.getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPaidRecordEvent(PaidRecordEvent event) {
        viewEventCallBack.gotoView(TopUpRecordFragment.class.getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadVipEvent(ReadVipEvent event) {
        viewEventCallBack.gotoView(BookVIPReadFragment.class.getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackToSettingFragmentEvent(BackToSettingFragmentEvent event) {
        viewEventCallBack.viewBack();
    }
}
