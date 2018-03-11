package com.onyx.jdread.shop.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.jdread.R;
import com.onyx.jdread.databinding.FragmentBookShopBinding;
import com.onyx.jdread.library.ui.SearchBookFragment;
import com.onyx.jdread.main.common.BaseFragment;
import com.onyx.jdread.main.common.Constants;
import com.onyx.jdread.main.common.JDPreferenceManager;
import com.onyx.jdread.main.common.ResManager;
import com.onyx.jdread.shop.action.ShopMainConfigAction;
import com.onyx.jdread.shop.adapter.ShopMainConfigAdapter;
import com.onyx.jdread.shop.cloud.entity.jdbean.BookModelConfigResultBean;
import com.onyx.jdread.shop.event.BannerItemClickEvent;
import com.onyx.jdread.shop.event.BookItemClickEvent;
import com.onyx.jdread.shop.event.CategoryViewClick;
import com.onyx.jdread.shop.event.EnjoyReadViewClick;
import com.onyx.jdread.shop.event.GoShopingCartEvent;
import com.onyx.jdread.shop.event.NewBookViewClick;
import com.onyx.jdread.shop.event.RankViewClick;
import com.onyx.jdread.shop.event.SaleViewClick;
import com.onyx.jdread.shop.event.SearchViewClickEvent;
import com.onyx.jdread.shop.event.ShopBakcTopClick;
import com.onyx.jdread.shop.event.ShopMainViewAllBookEvent;
import com.onyx.jdread.shop.event.ViewAllClickEvent;
import com.onyx.jdread.shop.model.BookShopViewModel;
import com.onyx.jdread.shop.model.ShopDataBundle;
import com.onyx.jdread.shop.view.CustomRecycleView;
import com.onyx.jdread.shop.view.SpaceItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by huxiaomao on 2017/12/7.
 */

public class ShopFragment extends BaseFragment {

    private FragmentBookShopBinding bookShopBinding;
    private int space = ResManager.getInteger(R.integer.custom_recycle_view_space);
    private CustomRecycleView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bookShopBinding = FragmentBookShopBinding.inflate(inflater, container, false);
        initView();
        initLibrary();
        initData();
        return bookShopBinding.getRoot();
    }

    private void initLibrary() {
        if (!getEventBus().isRegistered(this)) {
            getEventBus().register(this);
        }
    }

    private void initData() {
        getShopMainConfigData();
    }

    private void initView() {
        bookShopBinding.setViewModel(getBookShopViewModel());
        ShopMainConfigAdapter mainConfigdapter = new ShopMainConfigAdapter();
        recyclerView = bookShopBinding.shopMainConfigRecycleView;
        recyclerView.addItemDecoration(new SpaceItemDecoration(space));
        recyclerView.setAdapter(mainConfigdapter);
        recyclerView.setOnPagingListener(new CustomRecycleView.OnPagingListener() {
            @Override
            public void onPageChange(int curIndex) {
                setScrollbarFocusPosition(curIndex);
            }
        });
        checkWifi("");
    }

    private void setScrollbarFocusPosition(int curIndex) {
        if (bookShopBinding.scrollBar != null) {
            bookShopBinding.scrollBar.setFocusPosition(curIndex);
        }
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

    private BookShopViewModel getBookShopViewModel() {
        return getShopDataBundle().getShopViewModel();
    }

    private EventBus getEventBus() {
        return getShopDataBundle().getEventBus();
    }

    private void getShopMainConfigData() {
        ShopMainConfigAction configAction = new ShopMainConfigAction(Constants.BOOK_SHOP_MAIN_CONFIG_CID);
        configAction.execute(getShopDataBundle(), new RxCallback<ShopMainConfigAction>() {
            @Override
            public void onNext(ShopMainConfigAction configAction) {
                bookShopBinding.scrollBar.setTotal(getBookShopViewModel().getTotalPages());
                scrollToTop();
            }

            @Override
            public void onError(Throwable throwable) {
                super.onError(throwable);
            }
        });
    }

    private void scrollToTop() {
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShopBakcTopClick(ShopBakcTopClick event) {
        scrollToTop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRankViewClick(RankViewClick event) {
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().gotoView(BookRankFragment.class.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnjoyReadViewClick(EnjoyReadViewClick event) {
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().gotoView(BookVIPReadFragment.class.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSaleViewClick(SaleViewClick event) {
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().gotoView(BookSaleFragment.class.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewBookViewClick(NewBookViewClick event) {
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().gotoView(BookNewBooksFragment.class.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCategoryViewClick(CategoryViewClick event) {
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().gotoView(AllCategoryFragment.class.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchViewClickEvent(SearchViewClickEvent event) {
        if (getViewEventCallBack() != null) {
            getViewEventCallBack().gotoView(SearchBookFragment.class.getName());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBookItemClickEvent(BookItemClickEvent event) {
        gotoBookDetailPage(event.getBookBean().ebook_id);
    }

    private void gotoBookDetailPage(long ebookId) {
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.SP_KEY_BOOK_ID, ebookId);
        getViewEventCallBack().gotoView(BookDetailFragment.class.getName(), bundle);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBannerItemClickEvent(BannerItemClickEvent event) {
        BookModelConfigResultBean.DataBean.AdvBean advBean = event.advBean;
        if (advBean != null) {
            if (advBean.relate_type == Constants.RELATE_TYPE_BOOK_LIST) {
                JDPreferenceManager.setStringValue(Constants.SP_KEY_SUBJECT_NAME, advBean.show_name);
                JDPreferenceManager.setIntValue(Constants.SP_KEY_BOOK_LIST_TYPE, Constants.BOOK_LIST_TYPE_BOOK_MODEL);
                JDPreferenceManager.setIntValue(Constants.SP_KEY_SUBJECT_MODEL_ID, advBean.id);
                JDPreferenceManager.setIntValue(Constants.SP_KEY_SUBJECT_MODEL_TYPE, advBean.f_type);
                if (getViewEventCallBack() != null) {
                    getViewEventCallBack().gotoView(ViewAllBooksFragment.class.getName());
                }
            } else if (advBean.relate_type == Constants.RELATE_TYPE_LINK) {
                if (getViewEventCallBack() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.BANNER_URL,advBean.relate_link);
                    setBundle(bundle);
                    getViewEventCallBack().gotoView(BannerWebFragment.class.getName());
                }
            } else if (advBean.relate_type == Constants.RELATE_TYPE_BOOK_DETAIL) {
                gotoBookDetailPage(Long.valueOf(advBean.relate_link));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGoShopingCartEvent(GoShopingCartEvent event) {
        getViewEventCallBack().gotoView(ShopCartFragment.class.getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShopMainViewAllBookEvent(ShopMainViewAllBookEvent event) {
        getViewEventCallBack().gotoView(AllCategoryFragment.class.getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onViewAllClickEvent(ViewAllClickEvent event) {
        BookModelConfigResultBean.DataBean.ModulesBean modulesBean = event.modulesBean;
        if (modulesBean != null) {
            JDPreferenceManager.setStringValue(Constants.SP_KEY_SUBJECT_NAME, modulesBean.show_name);
            JDPreferenceManager.setIntValue(Constants.SP_KEY_BOOK_LIST_TYPE, Constants.BOOK_LIST_TYPE_BOOK_MODEL);
            JDPreferenceManager.setIntValue(Constants.SP_KEY_SUBJECT_MODEL_ID, modulesBean.id);
            JDPreferenceManager.setIntValue(Constants.SP_KEY_SUBJECT_MODEL_TYPE, modulesBean.f_type);
            if (getViewEventCallBack() != null) {
                getViewEventCallBack().gotoView(ViewAllBooksFragment.class.getName());
            }
        }
    }

    public ShopDataBundle getShopDataBundle() {
        return ShopDataBundle.getInstance();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hideLoadingDialog();
    }
}
