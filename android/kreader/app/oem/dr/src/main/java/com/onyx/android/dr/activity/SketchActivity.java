package com.onyx.android.dr.activity;

import android.support.v7.widget.DividerItemDecoration;
import android.view.View;
import android.widget.TextView;

import com.onyx.android.dr.DRApplication;
import com.onyx.android.dr.R;
import com.onyx.android.dr.adapter.InfromalEssayAdapter;
import com.onyx.android.dr.common.ActivityManager;
import com.onyx.android.dr.data.database.InfromalEssayEntity;
import com.onyx.android.dr.interfaces.InfromalEssayView;
import com.onyx.android.dr.presenter.InfromalEssayPresenter;
import com.onyx.android.dr.util.ExportToHtmlUtils;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.ui.view.PageRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by zhouzhiming on 2017/7/24.
 */
public class SketchActivity extends BaseActivity implements InfromalEssayView {
    @Bind(R.id.sketch_activity_recyclerview)
    PageRecyclerView recyclerView;
    @Bind(R.id.sketch_activity_share)
    TextView shareSketch;
    @Bind(R.id.sketch_activity_export)
    TextView exportSketch;
    @Bind(R.id.sketch_activity_delete)
    TextView deleteSketch;
    @Bind(R.id.sketch_activity_new)
    TextView newSketch;
    private DividerItemDecoration dividerItemDecoration;
    private InfromalEssayAdapter infromalEssayAdapter;
    private InfromalEssayPresenter infromalEssayPresenter;
    private List<InfromalEssayEntity> newWordList;
    private ArrayList<Boolean> listCheck;

    @Override
    protected Integer getLayoutId() {
        return R.layout.activity_sketch;
    }

    @Override
    protected void initConfig() {
    }

    @Override
    protected void initView() {
        initRecylcerView();
    }

    private void initRecylcerView() {
        dividerItemDecoration =
                new DividerItemDecoration(DRApplication.getInstance(), DividerItemDecoration.VERTICAL);
        infromalEssayAdapter = new InfromalEssayAdapter();
        recyclerView.setLayoutManager(new DisableScrollGridManager(DRApplication.getInstance()));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void initData() {
        newWordList = new ArrayList<InfromalEssayEntity>();
        listCheck = new ArrayList<>();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        infromalEssayPresenter = new InfromalEssayPresenter(getApplicationContext(), this);
        infromalEssayPresenter.getAllInfromalEssayData();
    }

    @Override
    public void setInfromalEssayData(List<InfromalEssayEntity> dataList) {
        if (dataList == null || dataList.size() <= 0) {
            return;
        }
        newWordList = dataList;
        for (int i = 0; i < newWordList.size(); i++) {
            listCheck.add(false);
        }
        infromalEssayAdapter.setDataList(newWordList, listCheck);
        recyclerView.setAdapter(infromalEssayAdapter);
    }

    public void initEvent() {
        infromalEssayAdapter.setOnItemListener(new InfromalEssayAdapter.OnItemClickListener() {
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

    @OnClick({R.id.sketch_activity_share,
            R.id.image_view_back,
            R.id.sketch_activity_delete,
            R.id.sketch_activity_new,
            R.id.sketch_activity_export})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_view_back:
                finish();
                break;
            case R.id.sketch_activity_delete:
                remoteAdapterDatas();
                break;
            case R.id.sketch_activity_export:
                exportData();
                break;
            case R.id.sketch_activity_new:
                ActivityManager.startAddInfromalEssayActivity(this);
                break;
        }
    }

    public void remoteAdapterDatas() {
        int length = listCheck.size();
        for (int i = length - 1; i >= 0; i--) {
            if (listCheck.get(i)) {
                //delete basedata data
                InfromalEssayEntity bean = newWordList.get(i);
                infromalEssayPresenter.deleteNewWord(bean.currentTime);
                newWordList.remove(i);
                listCheck.remove(i);
                infromalEssayAdapter.notifyItemRemoved(i);
                infromalEssayAdapter.notifyItemRangeChanged(0, newWordList.size());
            }
        }
    }

    public void exportData() {
        ArrayList<String> htmlTitle = new ArrayList<String>();
        htmlTitle.add(getString(R.string.infromal_essay_activity_time));
        htmlTitle.add(getString(R.string.infromal_essay_activity_title));
        htmlTitle.add(getString(R.string.infromal_essay_activity_word_number));
        htmlTitle.add(getString(R.string.infromal_essay_activity_content));
        ExportToHtmlUtils.exportInfromalEssayToHtml(htmlTitle, getString(R.string.infromal_essay_html), newWordList);
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
