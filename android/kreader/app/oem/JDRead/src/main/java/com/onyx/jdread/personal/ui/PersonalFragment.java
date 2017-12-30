package com.onyx.jdread.personal.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.ui.view.OnyxPageDividerItemDecoration;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.main.common.BaseFragment;
import com.onyx.jdread.databinding.PersonalBinding;
import com.onyx.jdread.personal.adapter.PersonalAdapter;
import com.onyx.jdread.personal.event.GiftCenterEvent;
import com.onyx.jdread.personal.event.PersonalAccountEvent;
import com.onyx.jdread.personal.event.PersonalBookEvent;
import com.onyx.jdread.personal.event.PersonalNoteEvent;
import com.onyx.jdread.personal.event.PersonalTaskEvent;
import com.onyx.jdread.personal.event.ReadPreferenceEvent;
import com.onyx.jdread.personal.model.PersonalBundle;
import com.onyx.jdread.personal.model.PersonalModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by huxiaomao on 2017/12/7.
 */

public class PersonalFragment extends BaseFragment {
    private PersonalBinding binding;
    private PersonalAdapter personalAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = (PersonalBinding) DataBindingUtil.inflate(inflater, R.layout.fragment_personal, container, false);
        initView();
        initData();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        PersonalBundle.getBundle().getEventBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        PersonalBundle.getBundle().getEventBus().unregister(this);
    }

    private void initData() {
        PersonalModel personalModel = PersonalBundle.getBundle().getPersonalModel();
        if (personalAdapter != null) {
            personalAdapter.setData(personalModel.getPersonalData(), personalModel.getEvents());
        }
    }

    private void initView() {
        binding.personalRecycler.setLayoutManager(new DisableScrollGridManager(JDReadApplication.getInstance()));
        binding.personalRecycler.addItemDecoration(new OnyxPageDividerItemDecoration(JDReadApplication.getInstance(), OnyxPageDividerItemDecoration.VERTICAL));
        personalAdapter = new PersonalAdapter(PersonalBundle.getBundle().getEventBus());
        binding.personalRecycler.setAdapter(personalAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGiftCenterEvent(GiftCenterEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPersonalAccountEvent(PersonalAccountEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPersonalBookEvent(PersonalBookEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPersonalNoteEvent(PersonalNoteEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPersonalTaskEvent(PersonalTaskEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadPreferenceEvent(ReadPreferenceEvent event) {
    }
}