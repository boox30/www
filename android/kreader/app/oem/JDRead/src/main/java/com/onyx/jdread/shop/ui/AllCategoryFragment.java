package com.onyx.jdread.shop.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.sdk.data.GPaginator;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.ui.view.DisableScrollGridManager;
import com.onyx.android.sdk.ui.view.PageRecyclerView;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.databinding.FragmentBookAllCategoryBinding;
import com.onyx.jdread.library.view.DashLineItemDivider;
import com.onyx.jdread.main.common.BaseFragment;
import com.onyx.jdread.main.common.Constants;
import com.onyx.jdread.main.common.JDPreferenceManager;
import com.onyx.jdread.shop.action.BookCategoryAction;
import com.onyx.jdread.shop.adapter.AllCategoryTopAdapter;
import com.onyx.jdread.shop.adapter.CategorySubjectAdapter;
import com.onyx.jdread.shop.cloud.entity.jdbean.CategoryListResultBean;
import com.onyx.jdread.shop.event.CategoryItemClickEvent;
import com.onyx.jdread.shop.event.CategoryTitleOneClick;
import com.onyx.jdread.shop.event.CategoryTitleThreeClick;
import com.onyx.jdread.shop.event.CategoryTitleTwoClick;
import com.onyx.jdread.shop.event.HideAllDialogEvent;
import com.onyx.jdread.shop.event.LoadingDialogEvent;
import com.onyx.jdread.shop.event.TopBackEvent;
import com.onyx.jdread.shop.model.AllCategoryViewModel;
import com.onyx.jdread.shop.model.BookShopViewModel;
import com.onyx.jdread.shop.model.ShopDataBundle;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by jackdeng on 2017/12/30.
 */

public class AllCategoryFragment extends BaseFragment {

    private FragmentBookAllCategoryBinding allCategoryBinding;
    private int space = JDReadApplication.getInstance().getResources().getInteger(R.integer.all_category_recycle_view_space);
    private int row = JDReadApplication.getInstance().getResources().getInteger(R.integer.all_category_recycle_view_row);
    private int col = JDReadApplication.getInstance().getResources().getInteger(R.integer.all_category_recycle_view_col);
    private int topCol = JDReadApplication.getInstance().getResources().getInteger(R.integer.all_category_top_recycle_view_col);
    private PageRecyclerView recyclerView;
    private GPaginator paginator;
    private static final int TYPE_INDEX_ZERO = 0;
    private static final int TYPE_INDEX_ONE = 1;
    private static final int TYPE_INDEX_TWO = 2;
    private int currentType = TYPE_INDEX_ZERO;
    private BookCategoryAction bookCategoryAction;
    private PageRecyclerView topRecyclerView;
    private List<CategoryListResultBean.CategoryBeanLevelOne> categoryBeanLevelOneList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        allCategoryBinding = FragmentBookAllCategoryBinding.inflate(inflater, container, false);
        initView();
        initLibrary();
        initData();
        return allCategoryBinding.getRoot();
    }

    private void initLibrary() {
        if (!getEventBus().isRegistered(this)) {
            getEventBus().register(this);
        }
    }

    private void initData() {
        getCategoryData();
    }

    private void getCategoryData() {
        bookCategoryAction = new BookCategoryAction(false);
        bookCategoryAction.execute(getShopDataBundle(), new RxCallback<BookCategoryAction>() {
            @Override
            public void onNext(BookCategoryAction bookCategoryAction) {
                categoryBeanLevelOneList = bookCategoryAction.getCategoryBeanLevelOneList();
                List<String> titleList = bookCategoryAction.getTitleList();
                getAllCategoryViewModel().titleOne.set(titleList.get(Constants.SHOP_MAIN_INDEX_ZERO));
                getAllCategoryViewModel().titleTwo.set(titleList.get(Constants.SHOP_MAIN_INDEX_ONE));
                getAllCategoryViewModel().titleThree.set(titleList.get(Constants.SHOP_MAIN_INDEX_TWO));
                changeData(Constants.SHOP_MAIN_INDEX_ZERO);
            }
        });
    }

    private void changeData(int index) {
        if (categoryBeanLevelOneList.size() > index) {
            List<CategoryListResultBean.CategoryBeanLevelOne.CategoryBeanLevelTwo> categoryBeanLevelTwoList = categoryBeanLevelOneList.get(index).sub_category;
            setCategoryData(categoryBeanLevelTwoList);
        }
    }

    private void setCategoryData(List<CategoryListResultBean.CategoryBeanLevelOne.CategoryBeanLevelTwo> categorySubjectItems) {
        if (categorySubjectItems != null) {
            getAllCategoryViewModel().setAllCategoryItems(categorySubjectItems);
            getAllCategoryViewModel().setTopCategoryItems(categorySubjectItems.subList(Constants.SHOP_MAIN_INDEX_ZERO, topCol));
            updateContentView(getAllCategoryViewModel().getAllCategoryItems());
            recyclerView.gotoPage(0);
        }
    }

    private void initView() {
        CategorySubjectAdapter adapter = new CategorySubjectAdapter(getEventBus(), true);
        DashLineItemDivider itemDecoration = new DashLineItemDivider();
        adapter.setRowAndCol(row, col);
        recyclerView = allCategoryBinding.recyclerViewAllCategorys;
        recyclerView.setLayoutManager(new DisableScrollGridManager(JDReadApplication.getInstance()));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(itemDecoration);
        paginator = recyclerView.getPaginator();
        getAllCategoryViewModel().getTitleBarViewModel().leftText = getString(R.string.all_category);
        recyclerView.setOnPagingListener(new PageRecyclerView.OnPagingListener() {
            @Override
            public void onPageChange(int position, int itemCount, int pageSize) {
                if (paginator != null) {
                    setCurrentPage(paginator.getCurrentPage());
                }
                getAllCategoryViewModel().setTopCategoryItems(getAllCategoryViewModel().getAllCategoryItems().subList(position, position + topCol));
            }
        });

        AllCategoryTopAdapter topAdapter = new AllCategoryTopAdapter(getEventBus());
        topRecyclerView = allCategoryBinding.recyclerViewCategoryTop;
        topRecyclerView.setLayoutManager(new DisableScrollGridManager(JDReadApplication.getInstance()));
        topRecyclerView.setAdapter(topAdapter);
        allCategoryBinding.setCategoryViewModel(getAllCategoryViewModel());
    }

    private void initPageIndicator(List<CategoryListResultBean.CategoryBeanLevelOne.CategoryBeanLevelTwo> resultBean) {
        int size = 0;
        if (resultBean != null) {
            size = resultBean.size();
        }
        paginator.resize(row, col, size);
        getAllCategoryViewModel().setTotalPage(paginator.pages());
        setCurrentPage(paginator.getCurrentPage());
    }

    private void updateContentView(List<CategoryListResultBean.CategoryBeanLevelOne.CategoryBeanLevelTwo> resultBean) {
        if (recyclerView == null) {
            return;
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        initPageIndicator(resultBean);
    }

    private void setCurrentPage(int currentPage) {
        getAllCategoryViewModel().setCurrentPage(currentPage + Constants.PAGE_STEP);
    }

    private ShopDataBundle getShopDataBundle() {
        return ShopDataBundle.getInstance();
    }

    private BookShopViewModel getShopViewModel() {
        return getShopDataBundle().getShopViewModel();
    }

    private AllCategoryViewModel getAllCategoryViewModel() {
        return getShopViewModel().getAllCategoryViewModel();
    }

    private EventBus getEventBus() {
        return getShopDataBundle().getEventBus();
    }

    private Context getContextJD() {
        return JDReadApplication.getInstance().getApplicationContext();
    }

    @Override
    public void onResume() {
        super.onResume();
        initLibrary();
    }

    @Override
    public void onStop() {
        super.onStop();
        getEventBus().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopBackEvent(TopBackEvent event) {
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().viewBack();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCategoryTitleOneClick(CategoryTitleOneClick event) {
        if (currentType != TYPE_INDEX_ZERO) {
            currentType = TYPE_INDEX_ZERO;
            changeData(Constants.SHOP_MAIN_INDEX_ZERO);
            changeCategoryButtonState();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCategoryTitleTwoClick(CategoryTitleTwoClick event) {
        if (currentType != TYPE_INDEX_ONE) {
            currentType = TYPE_INDEX_ONE;
            changeData(Constants.SHOP_MAIN_INDEX_ONE);
            changeCategoryButtonState();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCategoryTitleThreeClick(CategoryTitleThreeClick event) {
        if (currentType != TYPE_INDEX_TWO) {
            currentType = TYPE_INDEX_TWO;
            changeData(Constants.SHOP_MAIN_INDEX_TWO);
            changeCategoryButtonState();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCategoryItemClickEvent(CategoryItemClickEvent event) {
        CategoryListResultBean.CategoryBeanLevelOne.CategoryBeanLevelTwo categoryBean = event.getCategoryBean();
        if (categoryBean != null) {
            categoryBean.isSelect = true;
            JDPreferenceManager.setIntValue(Constants.SP_KEY_CATEGORY_LEVEL_TWO_ID, categoryBean.id);
            JDPreferenceManager.setStringValue(Constants.SP_KEY_CATEGORY_NAME, categoryBean.name);
            getViewEventCallBack().gotoView(CategoryBookListFragment.class.getName());
        }
    }

    public void saveCurCategoryLevelOneCateId(int index) {
        if (categoryBeanLevelOneList != null) {
            int catId = categoryBeanLevelOneList.get(index).id;
            JDPreferenceManager.setIntValue(Constants.SP_KEY_CATEGORY_LEVEL_ONE_ID, catId);
        }
    }

    public void changeCategoryButtonState() {
        allCategoryBinding.titleOne.setChecked(currentType == TYPE_INDEX_ZERO);
        allCategoryBinding.titleTwo.setChecked(currentType == TYPE_INDEX_ONE);
        allCategoryBinding.titleThree.setChecked(currentType == TYPE_INDEX_TWO);
        saveCurCategoryLevelOneCateId(currentType);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoadingDialogEvent(LoadingDialogEvent event) {
        if (isAdded()) {
            showLoadingDialog(getString(event.getResId()));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHideAllDialogEvent(HideAllDialogEvent event) {
        hideLoadingDialog();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hideLoadingDialog();
    }
}