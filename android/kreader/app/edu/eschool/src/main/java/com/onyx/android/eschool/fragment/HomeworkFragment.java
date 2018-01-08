package com.onyx.android.eschool.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.alibaba.fastjson.TypeReference;
import com.onyx.android.eschool.R;
import com.onyx.android.eschool.SchoolApp;
import com.onyx.android.eschool.custom.CustomRadioGroup;
import com.onyx.android.eschool.custom.PageIndicator;
import com.onyx.android.eschool.databinding.FragmentHomeworkBinding;
import com.onyx.android.eschool.databinding.ItemHomeworkBinding;
import com.onyx.android.eschool.events.TabSwitchEvent;
import com.onyx.android.eschool.holder.LibraryDataHolder;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.CloudManager;
import com.onyx.android.sdk.data.Constant;
import com.onyx.android.sdk.data.GPaginator;
import com.onyx.android.sdk.data.QueryBase;
import com.onyx.android.sdk.data.QueryResult;
import com.onyx.android.sdk.data.model.common.FetchPolicy;
import com.onyx.android.sdk.data.model.v2.GroupContainer;
import com.onyx.android.sdk.data.model.v2.Homework;
import com.onyx.android.sdk.data.model.v2.HomeworkQuery;
import com.onyx.android.sdk.data.model.v2.Homework_Table;
import com.onyx.android.sdk.data.model.v2.ResourceCode;
import com.onyx.android.sdk.data.model.v2.Subject;
import com.onyx.android.sdk.data.request.cloud.v2.HomeworkListLoadRequest;
import com.onyx.android.sdk.data.utils.JSONObjectParseUtils;
import com.onyx.android.sdk.data.utils.MetadataUtils;
import com.onyx.android.sdk.data.utils.QueryBuilder;
import com.onyx.android.sdk.ui.dialog.DialogProgressHolder;
import com.onyx.android.sdk.ui.utils.ToastUtils;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.ui.view.PageRecyclerView;
import com.onyx.android.sdk.utils.ActivityUtil;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.NetworkUtil;
import com.onyx.android.sdk.utils.ViewDocumentUtils;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by suicheng on 2017/12/7.
 */
public class HomeworkFragment extends Fragment {
    private static final String TAG = "HomeworkFragment";

    private static SimpleDateFormat DATE_FORMAT_MMDD_HHMM = new SimpleDateFormat("MM月dd日HH时mm分", Locale.SIMPLIFIED_CHINESE);
    private PageRecyclerView contentPageView;
    private CustomRadioGroup subjectGroupView;

    private FragmentHomeworkBinding binding;
    private PageIndicator pageIndicator;

    private DialogProgressHolder dialogProgressHolder = new DialogProgressHolder();

    private int pageCol = 3;
    private int pageRow = 2;
    private int queryLimit = 20;

    private boolean isVisibleToUser;

    private List<Subject> subjectList = new ArrayList<>();
    private List<Homework> homeworkDataList = new ArrayList<>();

    private int subjectIndex = 0;

    public static HomeworkFragment newInstance() {
        HomeworkFragment fragment = new HomeworkFragment();
        fragment.setArguments(resetArguments(new Bundle(), null));
        return fragment;
    }

    @NonNull
    private static Bundle resetArguments(@NonNull Bundle bundle, List<Subject> subjectList) {
        if (CollectionUtils.isNullOrEmpty(subjectList)) {
            return bundle;
        }
        bundle.putString(Constant.ARGS_TAG, String.valueOf(JSONObjectParseUtils.toJson(subjectList)));
        return bundle;
    }

    private static List<Subject> restoreArguments(@NonNull Bundle bundle) {
        return JSONObjectParseUtils.parseObject(bundle.getString(Constant.ARGS_TAG), new TypeReference<List<Subject>>() {
        });
    }

    private static List<Subject> restoreArguments(@NonNull Bundle bundle, @NonNull List<Subject> targetList) {
        List<Subject> list = restoreArguments(bundle);
        if (CollectionUtils.isNullOrEmpty(list)) {
            return targetList;
        }
        targetList.clear();
        targetList.addAll(list);
        return targetList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DATE_FORMAT_MMDD_HHMM = new SimpleDateFormat(getContext().getString(R.string.date_format), Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_homework, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        initPageView();
        initPageIndicator();
        initSubjectGroupView();
    }

    private void initPageIndicator() {
        pageIndicator = new PageIndicator(binding.pageIndicatorLayout.getRoot(), contentPageView.getPaginator());
        pageIndicator.setTotalFormat(getString(R.string.total_format));
        pageIndicator.setPageChangedListener(new PageIndicator.PageChangedListener() {
            @Override
            public void prev() {
                prevPage();
            }

            @Override
            public void next() {
                nextPage();
            }

            @Override
            public void gotoPage(int currentPage) {
            }
        });
        pageIndicator.setDataRefreshListener(new PageIndicator.DataRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    private void initSubjectGroupView() {
        subjectGroupView = binding.subjectGroupView;
        subjectGroupView.setDivideIndicatorAverage(false);
        subjectGroupView.setBackgroundMaxRadius(0);
        subjectGroupView.setOnCheckedChangeListener(new CustomRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId, int checkedIndex) {
                subjectIndex = checkedIndex;
                loadData(getQueryArgs(queryLimit, 0), true, null);
            }
        });
    }

    private void initPageView() {
        contentPageView = binding.contentPageView;
        contentPageView.setLayoutManager(new DisableScrollGridManager(getContext()));
        contentPageView.setAdapter(new PageRecyclerView.PageAdapter<ItemViewHolder>() {
            @Override
            public int getRowCount() {
                return pageRow;
            }

            @Override
            public int getColumnCount() {
                return pageCol;
            }

            @Override
            public int getDataCount() {
                return getTotalCount();
            }

            @Override
            public ItemViewHolder onPageCreateViewHolder(ViewGroup parent, int viewType) {
                ItemHomeworkBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.item_homework, parent, false);
                return new ItemViewHolder(binding);
            }

            @Override
            public void onPageBindViewHolder(ItemViewHolder holder, final int position) {
                holder.itemView.setTag(position);
                Homework item = homeworkDataList.get(position);
                holder.bind(item);
            }
        });
        contentPageView.setOnPagingListener(new PageRecyclerView.OnPagingListener() {
            @Override
            public void onPageChange(int position, int itemCount, int pageSize) {
                updatePageIndicator();
                if (!contentPageView.getPaginator().hasNextPage()) {
                    loadMoreData();
                }
            }
        });
        contentPageView.setOnLoadListener(new PageRecyclerView.OnLoadListener() {
            @Override
            public void onRefresh() {
                prevPage();
            }

            @Override
            public void onLoadMore() {
                nextPage();
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateSubjectGroupView(restoreArguments(getArguments(), subjectList));
    }

    private void prevPage() {
        if (getPagination().isFirstPage()) {
            postPrevTab();
            return;
        }
        contentPageView.prevPage();
    }

    private void nextPage() {
        if (getPagination().isLastPage()) {
            postNextTab();
            return;
        }
        contentPageView.nextPage();
    }

    private void postNextTab() {
        EventBus.getDefault().post(TabSwitchEvent.createNextTabSwitch());
    }

    private void postPrevTab() {
        EventBus.getDefault().post(TabSwitchEvent.createPrevTabSwitch());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onKeyEvent(KeyEvent keyEvent) {
        if (contentPageView != null && isVisibleToUser) {
            contentPageView.dispatchKeyEvent(keyEvent);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isVisibleToUser) {
            updateContentView(homeworkDataList);
        }
    }

    private boolean isVisibleToUser() {
        return isVisibleToUser;
    }

    private GPaginator getPagination() {
        return contentPageView.getPaginator();
    }

    private String getSubjectId() {
        if (CollectionUtils.isNullOrEmpty(subjectList)) {
            return null;
        }
        if (subjectIndex >= subjectList.size()) {
            subjectIndex = 0;
        }
        return subjectList.get(subjectIndex)._id;
    }

    private QueryBase getQueryArgs(int limit, int offset) {
        QueryBase args = new QueryBase();
        HomeworkQuery query = new HomeworkQuery(ResourceCode.MY_HOMEWORK, ResourceCode.MY_HOMEWORK_REF);
        query.setSubject(getSubjectId());
        args.limit = limit;
        args.offset = offset;
        args.query = JSONObjectParseUtils.toJson(query);
        args.conditionGroup = OperatorGroup.clause().and(QueryBuilder.matchLike(Homework_Table.child, query.getSubject()));
        return args;
    }

    private void refreshData() {
        if (NetworkUtil.enableWifiOpenAndDetect(getContext().getApplicationContext())) {
            ToastUtils.showToast(getContext(), R.string.open_wifi);
            return;
        }
        final QueryBase args = getQueryArgs(queryLimit, 0);
        args.fetchPolicy = FetchPolicy.CLOUD_ONLY;
        loadData(args, true, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                gotoPage(0);
            }
        });
    }

    private void loadMoreData() {
        QueryBase args = getQueryArgs(queryLimit, CollectionUtils.getSize(homeworkDataList));
        loadData(args, false, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                HomeworkListLoadRequest loadRequest = (HomeworkListLoadRequest) request;
                if (!QueryResult.isValidQueryResult(loadRequest.getQueryResult())) {
                    showTipsMessage(R.string.no_more_items);
                }
            }
        });
    }

    private void loadData(final QueryBase args, final boolean clearLocal, final BaseCallback callback) {
        final HomeworkListLoadRequest loadRequest = new HomeworkListLoadRequest(args, clearLocal);
        getCloudManager().submitRequest(getContext().getApplicationContext(), loadRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                dialogProgressHolder.dismissProgressDialog(loadRequest);
                QueryResult<Homework> result = loadRequest.getQueryResult();
                if (e != null || result.hasException()) {
                    showTipsMessage(R.string.refresh_fail);
                    return;
                }
                notificationDataChanged(result, clearLocal);
                BaseCallback.invoke(callback, loadRequest, null);
            }
        });
        dialogProgressHolder.showProgressDialog(getContext(), loadRequest, R.string.loading, null);
    }

    private void showTipsMessage(int resId) {
        if (!isVisibleToUser()) {
            return;
        }
        ToastUtils.showToast(getContext().getApplicationContext(), resId);
    }

    private void notificationDataChanged(QueryResult<Homework> result, boolean clear) {
        if (result == null) {
            return;
        }
        if (clear) {
            homeworkDataList.clear();
        }
        if (!CollectionUtils.isNullOrEmpty(result.list)) {
            homeworkDataList.addAll(result.list);
        }
        updateContentView(homeworkDataList);
    }

    private void updateContentView(List<Homework> list) {
        updateContentView();
    }

    private void updateContentView() {
        if (contentPageView == null) {
            return;
        }
        contentPageView.getAdapter().notifyDataSetChanged();
        updatePageIndicator();
    }

    private int getTotalCount() {
        return CollectionUtils.getSize(homeworkDataList);
    }

    private void updatePageIndicator() {
        int totalCount = getTotalCount();
        getPagination().resize(pageRow, pageCol, totalCount);
        pageIndicator.resetGPaginator(getPagination());
    }

    private void gotoPage(int selectPage) {
        contentPageView.gotoPage(selectPage);
        updatePageIndicator();
    }

    private void updateSubjectGroupView(List<Subject> list) {
        if (CollectionUtils.isNullOrEmpty(list)) {
            return;
        }
        List<String> nameList = new ArrayList<>();
        List<Subject> validList = new ArrayList<>();
        for (Subject subject : list) {
            if (!Subject.isValid(subject)) {
                continue;
            }
            validList.add(subject);
            nameList.add(subject.name);
        }
        subjectList.clear();
        subjectList.addAll(validList);
        if (subjectIndex >= CollectionUtils.getSize(subjectList)) {
            subjectIndex = 0;
        }
        subjectGroupView.updateItemsText(subjectIndex, nameList);
        subjectGroupView.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupContainer(GroupContainer groupContainer) {
        resetArguments(getArguments(), groupContainer.subjectList);
        updateSubjectGroupView(groupContainer.subjectList);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void processItemClick(int position) {
        Intent intent = new Intent();
        intent.setComponent(ViewDocumentUtils.getHomeworkAppComponent());
        MetadataUtils.putIntentExtraData(intent, homeworkDataList.get(position));
        boolean success = ActivityUtil.startActivitySafely(getContext(), intent);
        if (!success) {
            ToastUtils.showToast(getContext().getApplicationContext(), R.string.app_no_installed);
        }
    }

    private CloudManager getCloudManager() {
        return getDataHolder().getCloudManager();
    }

    private LibraryDataHolder getDataHolder() {
        return SchoolApp.getLibraryDataHolder();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeworkBinding binding;

        public ItemViewHolder(ItemHomeworkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processItemClick((Integer) v.getTag());
                }
            });
        }

        public void bind(Homework item) {
            binding.setViewModel(item);
            binding.textViewDate.setText(getDateString(item));
            binding.executePendingBindings();
        }

        private String getDateString(Homework item) {
            if (item.getUpdatedAt() == null) {
                return null;
            }
            return DATE_FORMAT_MMDD_HHMM.format(item.getUpdatedAt());
        }
    }
}
