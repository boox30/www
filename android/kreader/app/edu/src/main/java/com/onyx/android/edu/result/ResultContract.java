package com.onyx.android.edu.result;


import com.onyx.android.edu.base.BasePresenter;
import com.onyx.android.edu.base.BaseView;
import com.onyx.android.edu.bean.PaperResult;

/**
 * Created by ming on 16/6/28.
 */
public interface ResultContract {
    interface View extends BaseView<Presenter> {
        void showResult(PaperResult paperResult);
    }

    interface Presenter extends BasePresenter {

    }
}
