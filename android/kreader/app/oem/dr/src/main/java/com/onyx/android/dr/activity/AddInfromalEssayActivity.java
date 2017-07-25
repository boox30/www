package com.onyx.android.dr.activity;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.onyx.android.dr.R;
import com.onyx.android.dr.bean.InfromalEssayBean;
import com.onyx.android.dr.common.CommonNotices;
import com.onyx.android.dr.data.database.InfromalEssayEntity;
import com.onyx.android.dr.interfaces.InfromalEssayView;
import com.onyx.android.dr.presenter.InfromalEssayPresenter;
import com.onyx.android.dr.util.Utils;
import com.onyx.android.dr.view.DefaultEditText;
import com.onyx.android.sdk.utils.StringUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by zhouzhiming on 2017/7/21.
 */
public class AddInfromalEssayActivity extends BaseActivity implements InfromalEssayView {
    @Bind(R.id.add_infromal_essay_activity_title)
    EditText titleEditText;
    @Bind(R.id.add_infromal_essay_activity_file)
    TextView correctFile;
    @Bind(R.id.title_bar_right_menu)
    TextView rightMenu;
    @Bind(R.id.add_infromal_essay_activity_content)
    DefaultEditText contentEditText;
    private InfromalEssayPresenter infromalEssayPresenter;

    @Override
    protected Integer getLayoutId() {
        return R.layout.activity_add_infromal_essay;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        infromalEssayPresenter = new InfromalEssayPresenter(getApplicationContext(), this);
        setTitleData();
    }

    private void setTitleData() {
        rightMenu.setVisibility(View.VISIBLE);
        rightMenu.setText(R.string.save_button);
        rightMenu.setTextSize(getResources().getDimension(R.dimen.level_two_font_size));
    }

    @Override
    public void setInfromalEssayData(List<InfromalEssayEntity> dataList) {
    }

    @OnClick({R.id.add_infromal_essay_activity_file,
            R.id.image_view_back,
            R.id.title_bar_right_menu})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_view_back:
                finish();
                break;
            case R.id.title_bar_right_menu:
                insertData();
                break;
            case R.id.add_infromal_essay_activity_file:
                break;
        }
    }

    private void insertData() {
        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        if (StringUtils.isNullOrEmpty(title)) {
            CommonNotices.showMessage(this, getString(R.string.input_title));
            return;
        }
        if (StringUtils.isNullOrEmpty(content)) {
            CommonNotices.showMessage(this, getString(R.string.input_infromal_essay_content));
            return;
        }
        InfromalEssayBean bean = new InfromalEssayBean();
        bean.setTitle(title);
        bean.setContent(content);
        bean.setWordNumber(Utils.getStringLength(content));
        infromalEssayPresenter.insertInfromalEssay(bean);
        finish();
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
