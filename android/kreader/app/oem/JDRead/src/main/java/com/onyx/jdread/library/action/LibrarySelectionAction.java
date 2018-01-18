package com.onyx.jdread.library.action;

import android.content.Context;
import android.databinding.ObservableList;

import com.onyx.android.sdk.data.QueryArgs;
import com.onyx.android.sdk.data.SortBy;
import com.onyx.android.sdk.data.SortOrder;
import com.onyx.android.sdk.data.model.DataModel;
import com.onyx.android.sdk.data.model.Metadata;
import com.onyx.android.sdk.data.rxrequest.data.db.RxLibraryLoadRequest;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.ui.utils.ToastUtils;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.jdread.R;
import com.onyx.jdread.library.model.LibraryDataBundle;
import com.onyx.jdread.library.view.LibraryBuildDialog;
import com.onyx.jdread.library.view.MoveToLibraryListDialog;
import com.onyx.jdread.main.action.BaseAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by suicheng on 2017/4/29.
 */

public class LibrarySelectionAction extends BaseAction<LibraryDataBundle> {

    private Map<String, List<Metadata>> chosenItemsMap;
    private List<DataModel> libraryList = new ArrayList<>();
    private DataModel librarySelected;
    private Context context;

    public LibrarySelectionAction(Map<String, List<Metadata>> chosenItemsMap, Context context) {
        this.context = context;
        this.chosenItemsMap = chosenItemsMap;
    }

    @Override
    public void execute(final LibraryDataBundle dataHolder, final RxCallback baseCallback) {
        loadRequest(dataHolder, baseCallback);
    }

    private void loadRequest(final LibraryDataBundle libraryDataBundle, final RxCallback callback) {
        final QueryArgs queryArgs = new QueryArgs(SortBy.CreationTime, SortOrder.Desc);
        RxLibraryLoadRequest request = new RxLibraryLoadRequest(libraryDataBundle.getDataManager(), queryArgs, libraryList, false, libraryDataBundle.getEventBus(), false);
        request.setLoadFromCache(false);
        request.execute(new RxCallback<RxLibraryLoadRequest>() {
            @Override
            public void onNext(RxLibraryLoadRequest loadRequest) {
                libraryList.clear();
                libraryList.addAll(loadRequest.getModels());
                filterLibrary(libraryDataBundle);
                showLibraryEntryTocDialog(libraryDataBundle, callback);
            }
        });
    }

    private void filterLibrary(LibraryDataBundle libraryDataBundle) {
        DataModel dataModel = new DataModel(libraryDataBundle.getEventBus());
        dataModel.title.set(libraryDataBundle.getAppContext().getString(R.string.library_name));
        dataModel.idString.set("");
        libraryList.add(0, dataModel);
        if (!CollectionUtils.isNullOrEmpty(chosenItemsMap) && chosenItemsMap.size() == 1) {
            for (String libraryId : chosenItemsMap.keySet()) {
                Iterator<DataModel> iterator = libraryList.iterator();
                while (iterator.hasNext()) {
                    DataModel next = iterator.next();
                    if (libraryId.equals(next.idString.get())) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private void showLibraryEntryTocDialog(final LibraryDataBundle libraryDataBundle, final RxCallback callback) {
        MoveToLibraryListDialog.DialogModel model = new MoveToLibraryListDialog.DialogModel(libraryDataBundle.getAppContext().getResources().getInteger(R.integer.library_list_dialog_row)
                , libraryDataBundle.getAppContext().getResources().getInteger(R.integer.library_list_dialog_col), libraryList);
        MoveToLibraryListDialog.Builder builder = new MoveToLibraryListDialog.Builder(context, model);
        final MoveToLibraryListDialog libraryListDialog = builder.create();
        model.setListener(new MoveToLibraryListDialog.DialogModel.ClickListener() {
            @Override
            public void onItemClicked(DataModel dataModel) {
                librarySelected = dataModel;
                callback.onNext(librarySelected);
            }

            @Override
            public void onBuildLibraryClicked() {
                libraryListDialog.dismiss();
                showBuildLibrary(libraryDataBundle, callback);
            }
        });
        libraryListDialog.show();
    }

    private void showBuildLibrary(final LibraryDataBundle libraryDataBundle, final RxCallback callback) {
        final LibraryBuildDialog.DialogModel model = new LibraryBuildDialog.DialogModel();
        LibraryBuildDialog.Builder builder = new LibraryBuildDialog.Builder(context, model);
        final LibraryBuildDialog libraryBuildDialog = builder.create();
        model.setPositiveClickLister(new LibraryBuildDialog.DialogModel.OnClickListener() {
            @Override
            public void onClicked() {
                if (StringUtils.isNotBlank(model.libraryName.get())) {
                    if (isExist(libraryDataBundle, model.libraryName.get())) {
                        ToastUtils.showToast(libraryDataBundle.getAppContext(), R.string.group_exist);
                        return;
                    }
                    librarySelected = new DataModel(libraryDataBundle.getEventBus());
                    librarySelected.idString.set(UUID.randomUUID().toString());
                    librarySelected.title.set(model.libraryName.get());
                    callback.onNext(librarySelected);
                    libraryBuildDialog.dismiss();
                } else {
                    ToastUtils.showToast(libraryDataBundle.getAppContext(), R.string.please_enter_group_name);
                }
            }
        });

        model.setNegativeClickLister(new LibraryBuildDialog.DialogModel.OnClickListener() {
            @Override
            public void onClicked() {
                libraryBuildDialog.dismiss();
            }
        });
        libraryBuildDialog.show();
    }

    private boolean isExist(LibraryDataBundle libraryDataBundle, String newLibraryName) {
        for (DataModel dataModel : libraryList) {
            if (newLibraryName.equals(dataModel.title.get())) {
                return true;
            }
        }
        ObservableList<DataModel> libraryPathList = libraryDataBundle.getLibraryViewDataModel().libraryPathList;
        if (!CollectionUtils.isNullOrEmpty(libraryPathList)) {
            DataModel currentLibrary = libraryPathList.get(libraryPathList.size() - 1);
            if (newLibraryName.equals(currentLibrary.title.get())) {
                return true;
            }
        }
        return false;
    }

    public DataModel getLibrarySelected() {
        return librarySelected;
    }
}
