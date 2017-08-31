package com.onyx.android.dr.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.onyx.android.dr.R;
import com.onyx.android.dr.common.ActivityManager;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by zhouzhiming on 2017/8/28.
 */
public class GroupHomePageActivity extends BaseActivity {
    @Bind(R.id.group_home_page_activity_create)
    TextView createGroup;
    @Bind(R.id.group_home_page_activity_join)
    TextView joinGroup;
    @Bind(R.id.group_home_page_activity_exit)
    TextView exitGroup;
    @Bind(R.id.group_home_page_activity_manage)
    TextView groupMemberManage;
    @Bind(R.id.image_view_back)
    ImageView imageViewBack;
    @Bind(R.id.title_bar_title)
    TextView title;
    @Bind(R.id.image)
    ImageView image;

    @Override
    protected Integer getLayoutId() {
        return R.layout.activity_group_home_page;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        initTitleData();
        initEvent();
    }

    private void initTitleData() {
        image.setImageResource(R.drawable.ic_group);
        title.setText(getString(R.string.group));
    }

    public void initEvent() {
    }

    @OnClick({R.id.group_home_page_activity_create,
            R.id.group_home_page_activity_join,
            R.id.group_home_page_activity_exit,
            R.id.image_view_back,
            R.id.group_home_page_activity_manage})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_view_back:
                finish();
                break;
            case R.id.group_home_page_activity_create:
                ActivityManager.startCreateGroupActivity(this);
                break;
            case R.id.group_home_page_activity_join:
                ActivityManager.startJoinGroupActivity(this);
                break;
            case R.id.group_home_page_activity_exit:
                ActivityManager.startExitGroupActivity(this);
                break;
            case R.id.group_home_page_activity_manage:
                ActivityManager.startGroupMemberManageActivity(this);
                break;
        }
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
