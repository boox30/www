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
import com.onyx.android.sdk.utils.PreferenceManager;
import com.onyx.jdread.JDReadApplication;
import com.onyx.jdread.R;
import com.onyx.jdread.databinding.FragmentCategoryBookListBinding;
import com.onyx.jdread.library.view.DashLineItemDivider;
import com.onyx.jdread.main.common.BaseFragment;
import com.onyx.jdread.main.common.Constants;
import com.onyx.jdread.shop.action.BookCategoryLevel2BooksAction;
import com.onyx.jdread.shop.adapter.CategorySubjectAdapter;
import com.onyx.jdread.shop.adapter.SubjectListAdapter;
import com.onyx.jdread.shop.cloud.entity.jdbean.CategoryListResultBean;
import com.onyx.jdread.shop.common.CloudApiContext;
import com.onyx.jdread.shop.event.BookItemClickEvent;
import com.onyx.jdread.shop.event.CategoryItemClickEvent;
import com.onyx.jdread.shop.event.HideAllDialogEvent;
import com.onyx.jdread.shop.event.LoadingDialogEvent;
import com.onyx.jdread.shop.event.SubjectListSortKeyChangeEvent;
import com.onyx.jdread.shop.event.TopBackEvent;
import com.onyx.jdread.shop.event.TopRightTitle2Event;
import com.onyx.jdread.shop.event.TopRightTitle3Event;
import com.onyx.jdread.shop.model.AllCategoryViewModel;
import com.onyx.jdread.shop.model.BookShopViewModel;
import com.onyx.jdread.shop.model.ShopDataBundle;
import com.onyx.jdread.shop.model.CategoryBookListViewModel;
import com.onyx.jdread.shop.model.TitleBarViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by jackdeng on 2017/12/30.
 */

public class CategoryBookListFragment extends BaseFragment {

    private FragmentCategoryBookListBinding categoryBookListBinding;
    private int row = JDReadApplication.getInstance().getResources().getInteger(R.integer.subject_list_recycle_viw_row);
    private int col = JDReadApplication.getInstance().getResources().getInteger(R.integer.subject_list_recycle_viw_col);
    private int catRow = JDReadApplication.getInstance().getResources().getInteger(R.integer.subject_list_category_recycle_viw_row);
    private int catCol = JDReadApplication.getInstance().getResources().getInteger(R.integer.subject_list_category_recycle_viw_col);
    private PageRecyclerView recyclerView;
    private GPaginator paginator;
    private int currentPage = 1;
    private String currentCatName;
    private int sortkey = CloudApiContext.CategoryLevel2BookList.SORT_KEY_DEFAULT_VALUES;
    private int sortType = CloudApiContext.CategoryLevel2BookList.SORT_TYPE_DEFAULT_VALUES;
    private boolean typeFree;
    private int catOneId;
    private int catTwoId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        categoryBookListBinding = FragmentCategoryBookListBinding.inflate(inflater, container, false);
        initView();
        initLibrary();
        initData();
        return categoryBookListBinding.getRoot();
    }

    private void initLibrary() {
        if (!getEventBus().isRegistered(this)) {
            getEventBus().register(this);
        }
    }

    private void initData() {
        catOneId = PreferenceManager.getIntValue(getContextJD(), Constants.SP_KEY_CATEGORY_LEVEL_ONE_ID, 0);
        catTwoId = PreferenceManager.getIntValue(getContextJD(), Constants.SP_KEY_CATEGORY_LEVEL_TWO_ID, 0);
        currentCatName = PreferenceManager.getStringValue(getContextJD(), Constants.SP_KEY_CATEGORY_NAME, "");
        typeFree = PreferenceManager.getBooleanValue(getContextJD(), Constants.SP_KEY_CATEGORY_ISFREE, false);
        getCategoryBookListViewModel().getTitleBarViewModel().leftText = currentCatName;
        getCategoryBookListViewModel().getTitleBarViewModel().showRightText2 = true;
        getCategoryBookListViewModel().getTitleBarViewModel().showRightText3 = true;
        getCategoryBookListViewModel().getTitleBarViewModel().rightText2 = getString(R.string.subject_list_all);
        getCategoryBookListViewModel().getTitleBarViewModel().rightText3 = getString(R.string.subject_list_sort_type_hot);
        setSortButtonIsOpen(false);
        setAllCatIsOpen(false);
        setRightText2Icon();
        setRightText3Icon();
        getBooksData(getFinalCatId(), currentPage, sortkey, sortType);
        setCategoryV2Data();
    }

    private String getFinalCatId() {
        return catOneId + "_" + catTwoId;
    }

    private void getBooksData(String catid, int currentPage, int sortKey, int sortType) {
        BookCategoryLevel2BooksAction booksAction = new BookCategoryLevel2BooksAction(catid, currentPage, sortKey, sortType);
        booksAction.execute(getShopDataBundle(), new RxCallback<BookCategoryLevel2BooksAction>() {
            @Override
            public void onNext(BookCategoryLevel2BooksAction categoryBooksAction) {
                setBooksData();
                updateContentView();
            }
        });
    }

    private void setCategoryV2Data() {
        List<CategoryListResultBean.CategoryBeanLevelOne.CategoryBeanLevelTwo> allCategoryItems = getAllCategoryViewModel().getAllCategoryItems();
        getCategoryBookListViewModel().setCategoryItems(allCategoryItems);
        getCategoryBookListViewModel().isFree.set(typeFree);
    }

    private void setBooksData() {
        recyclerView.gotoPage(0);
    }

    private void initView() {
        SubjectListAdapter adapter = new SubjectListAdapter(getEventBus());
        DashLineItemDivider itemDecoration = new DashLineItemDivider();
        recyclerView = categoryBookListBinding.recyclerViewSubjectList;
        recyclerView.setLayoutManager(new DisableScrollGridManager(JDReadApplication.getInstance()));
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapter(adapter);
        paginator = recyclerView.getPaginator();
        recyclerView.setOnPagingListener(new PageRecyclerView.OnPagingListener() {
            @Override
            public void onPageChange(int position, int itemCount, int pageSize) {
                if (paginator != null) {
                    int curPage = paginator.getCurrentPage();
                    setCurrentPage(curPage);
                }
            }
        });
        categoryBookListBinding.setViewModel(getCategoryBookListViewModel());
        CategorySubjectAdapter categorySubjectAdapter = new CategorySubjectAdapter(getEventBus(), true);
        categorySubjectAdapter.setRowAndCol(catRow, catCol);
        PageRecyclerView recyclerViewCategoryList = categoryBookListBinding.recyclerViewCategoryList;
        recyclerViewCategoryList.setLayoutManager(new DisableScrollGridManager(JDReadApplication.getInstance()));
        recyclerViewCategoryList.setAdapter(categorySubjectAdapter);
        recyclerViewCategoryList.addItemDecoration(itemDecoration);
    }

    private void initPageIndicator() {
        int size = 0;
        if (getCategoryBookListViewModel().getBookList() != null) {
            size = getCategoryBookListViewModel().getBookList().size();
        }
        paginator.resize(row, col, size);
        getCategoryBookListViewModel().setTotalPage(paginator.pages());
        setCurrentPage(paginator.getCurrentPage());
    }

    private void updateContentView() {
        if (recyclerView == null) {
            return;
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        initPageIndicator();
    }

    private void setCurrentPage(int currentPage) {
        getCategoryBookListViewModel().setCurrentPage(currentPage + Constants.PAGE_STEP);
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

    private CategoryBookListViewModel getCategoryBookListViewModel() {
        return getAllCategoryViewModel().getCategoryBookListViewModel();
    }

    private TitleBarViewModel getTitleBarViewModel() {
        return getCategoryBookListViewModel().getTitleBarViewModel();
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
    public void onBookItemClickEvent(BookItemClickEvent event) {
        PreferenceManager.setLongValue(JDReadApplication.getInstance(), Constants.SP_KEY_BOOK_ID, event.getBookBean().ebook_id);
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().gotoView(BookDetailFragment.class.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCategoryItemClickEvent(CategoryItemClickEvent event) {
        CategoryListResultBean.CategoryBeanLevelOne.CategoryBeanLevelTwo categoryBean = event.getCategoryBean();
        this.catTwoId = categoryBean.id;
        this.currentCatName = categoryBean.name;
        this.currentPage = 1;
        this.sortkey = CloudApiContext.CategoryLevel2BookList.SORT_KEY_DEFAULT_VALUES;
        getCategoryBookListViewModel().getTitleBarViewModel().leftText = currentCatName;
        getBooksData(getFinalCatId(), currentPage, sortkey, sortType);
        showOrCloseAllCatButton();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopRight2Event(TopRightTitle2Event event) {
        showOrCloseAllCatButton();
    }

    private void showOrCloseAllCatButton() {
        if (getCategoryBookListViewModel().sortButtonIsOpen.get()) {
            showOrCloseSortButton();
        }
        boolean allCatIsOpen = getCategoryBookListViewModel().allCatIsOpen.get();
        setAllCatIsOpen(!allCatIsOpen);
        setRightText2Icon();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTopRight3Event(TopRightTitle3Event event) {
        showOrCloseSortButton();
    }

    private void showOrCloseSortButton() {
        if (getCategoryBookListViewModel().allCatIsOpen.get()) {
            showOrCloseAllCatButton();
        }
        boolean sortButtonIsOpen = getCategoryBookListViewModel().sortButtonIsOpen.get();
        setSortButtonIsOpen(!sortButtonIsOpen);
        setRightText3Icon();
    }

    private void setAllCatIsOpen(boolean allCatIsOpen) {
        getCategoryBookListViewModel().allCatIsOpen.set(allCatIsOpen);
    }

    private void setSortButtonIsOpen(boolean sortButtonIsOpen) {
        getCategoryBookListViewModel().sortButtonIsOpen.set(sortButtonIsOpen);
    }

    private void setRightText2Icon() {
        getTitleBarViewModel().rightText2IconId.set(getCategoryBookListViewModel().allCatIsOpen.get() ? R.mipmap.ic_up : R.mipmap.ic_down);
    }

    private void setRightText3Icon() {
        getTitleBarViewModel().rightText3IconId.set(getCategoryBookListViewModel().sortButtonIsOpen.get() ? R.mipmap.ic_up : R.mipmap.ic_down);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubjectListSortKeyChangeEvent(SubjectListSortKeyChangeEvent event) {
        if (sortkey != event.sortKey) {
            sortkey = event.sortKey;
            getBooksData(getFinalCatId(), currentPage, sortkey, sortType);
        }
        showOrCloseSortButton();
    }

    @Subscribe
    public void onLoadingDialogEvent(LoadingDialogEvent event) {
        showLoadingDialog(getString(event.getResId()));
    }

    @Subscribe
    public void onHideAllDialogEvent(HideAllDialogEvent event) {
        hideLoadingDialog();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hideLoadingDialog();
    }
}
