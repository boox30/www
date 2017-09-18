package com.onyx.android.dr.activity;

import android.support.v7.widget.DividerItemDecoration;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyx.android.dr.DRApplication;
import com.onyx.android.dr.R;
import com.onyx.android.dr.adapter.GroupAdapter;
import com.onyx.android.dr.bean.GroupInfoBean;
import com.onyx.android.dr.common.ActivityManager;
import com.onyx.android.dr.common.CommonNotices;
import com.onyx.android.dr.interfaces.JoinGroupView;
import com.onyx.android.dr.presenter.JoinGroupPresenter;
import com.onyx.android.sdk.device.Device;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.ui.view.PageRecyclerView;
import com.onyx.android.sdk.utils.NetworkUtil;
import com.onyx.android.sdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by zhouzhiming on 2017/8/28.
 */
public class JoinGroupActivity extends BaseActivity implements JoinGroupView {
    @Bind(R.id.join_group_activity_group_name)
    EditText groupName;
    @Bind(R.id.join_group_activity_search_group)
    ImageView searchGroup;
    @Bind(R.id.image_view_back)
    ImageView imageViewBack;
    @Bind(R.id.title_bar_title)
    TextView title;
    @Bind(R.id.image)
    ImageView image;
    @Bind(R.id.join_group_activity_recycler_view)
    PageRecyclerView recyclerView;
    private JoinGroupPresenter joinGroupPresenter;
    private DividerItemDecoration dividerItemDecoration;
    private GroupAdapter groupAdapter;
    private List<GroupInfoBean> groupList;
    private ArrayList<Boolean> listCheck;

    @Override
    protected Integer getLayoutId() {
        return R.layout.activity_join_group;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        dividerItemDecoration =
                new DividerItemDecoration(DRApplication.getInstance(), DividerItemDecoration.VERTICAL);
        groupAdapter = new GroupAdapter();
        recyclerView.setLayoutManager(new DisableScrollGridManager(DRApplication.getInstance()));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void initData() {
        joinGroupPresenter = new JoinGroupPresenter(this);
        groupList = new ArrayList<GroupInfoBean>();
        listCheck = new ArrayList<>();
        initTitleData();
        initEvent();
    }

    private void initTitleData() {
        image.setImageResource(R.drawable.ic_group);
        title.setText(getString(R.string.group_home_page_activity_join));
    }

    public void initEvent() {
        groupAdapter.setOnItemListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void setOnItemClick(int position, boolean isCheck) {
                listCheck.set(position, isCheck);
            }

            @Override
            public void setOnItemCheckedChanged(int position, boolean isCheck) {
                listCheck.set(position, isCheck);
            }
        });
    }

    @OnClick({R.id.image_view_back,
            R.id.join_group_activity_search_group})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_view_back:
                finish();
                break;
            case R.id.join_group_activity_search_group:
                searchGroupRequest();
                break;
        }
    }

    private void searchGroupRequest() {
        String recommendGroupName = groupName.getText().toString();
        if (StringUtils.isNullOrEmpty(recommendGroupName)) {
            CommonNotices.showMessage(this, getString(R.string.input_group_name_hint));
            return;
        }
        if (!NetworkUtil.isWiFiConnected(this)){
            connectNetwork();
            return;
        }
        joinGroupPresenter.searchGroup(recommendGroupName);
    }

    @Override
    public void setSearchGroupResult(List<GroupInfoBean> list, ArrayList<Boolean> checkList) {
        if (list == null || list.size() <= 0) {
            return;
        }
        groupList = list;
        listCheck = checkList;
        groupAdapter.setDataList(groupList, listCheck);
        recyclerView.setAdapter(groupAdapter);
    }

    @Override
    public void setJoinGroupResult(boolean result) {
        if (result) {
            ActivityManager.startGroupHomePageActivity(this);
            CommonNotices.showMessage(this, getString(R.string.join_group_success));
        }
    }

    private void connectNetwork() {
        Device.currentDevice().enableWifiDetect(this);
        NetworkUtil.enableWiFi(this, true);
        CommonNotices.showMessage(this, getString(R.string.network_not_connected));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
