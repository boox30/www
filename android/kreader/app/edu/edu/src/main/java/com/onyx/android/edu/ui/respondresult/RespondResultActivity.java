package com.onyx.android.edu.ui.respondresult;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onyx.android.edu.R;
import com.onyx.android.edu.base.BaseActivity;
import com.onyx.android.edu.bean.PaperResult;
import com.onyx.android.edu.utils.ActivityUtils;
import com.onyx.android.edu.utils.JsonUtils;

import butterknife.Bind;

/**
 * Created by ming on 16/6/24.
 */
public class RespondResultActivity extends BaseActivity {

    public static final String RESULT = "result";
    @Bind(R.id.back)
    ImageView mBack;
    @Bind(R.id.left_title)
    TextView mLeftTitle;
    @Bind(R.id.toolbar_title)
    TextView mToolbarTitle;
    @Bind(R.id.right_title)
    TextView mRightTitle;
    @Bind(R.id.toolbar_content)
    RelativeLayout mToolbarContent;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.contentFrame)
    FrameLayout mContentFrame;
    private String message;
    private CommitResultFragment commitResultFragment;

    @Override
    protected Integer getLayoutId() {
        return R.layout.activity_general;
    }

    @Override
    protected void initView() {
        mRightTitle.setVisibility(View.GONE);
        mLeftTitle.setText(getString(R.string.commit_result));
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        message = intent.getStringExtra(RESULT);
//        if (!TextUtils.isEmpty(message)) {
        if (true) {
            commitResultFragment = (CommitResultFragment) getFragmentManager().findFragmentById(R.id.contentFrame);
            if(commitResultFragment == null) {
                commitResultFragment = CommitResultFragment.newInstance();
                ActivityUtils.addFragmentToActivity(getFragmentManager(), commitResultFragment, R.id.contentFrame);
            }
        }
    }

    public void switchFragment() {
        PaperResult paperResult = JsonUtils.toBean(message, PaperResult.class);
        mLeftTitle.setText(getString(R.string.train_result));
        ExaminationResultFragment respondResultFragment = ExaminationResultFragment.newInstance();
        ActivityUtils.switchFragment(getFragmentManager(), commitResultFragment, respondResultFragment, R.id.contentFrame);
        RespondResultPresenter respondResultPresenter = new RespondResultPresenter(respondResultFragment);
        respondResultPresenter.setPaperResult(paperResult);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
