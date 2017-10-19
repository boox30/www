package com.onyx.android.dr.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onyx.android.dr.DRApplication;
import com.onyx.android.dr.R;
import com.onyx.android.dr.dialog.SelectAlertDialog;
import com.onyx.android.dr.presenter.JoinGroupPresenter;
import com.onyx.android.dr.util.Utils;
import com.onyx.android.dr.view.DefaultEditText;
import com.onyx.android.sdk.data.model.CreatorBean;
import com.onyx.android.sdk.data.model.v2.JoinGroupBean;
import com.onyx.android.sdk.data.model.v2.SearchGroupBean;
import com.onyx.android.sdk.ui.view.PageRecyclerView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zhouzhiming on 2017/8/31.
 */
public class GroupAdapter extends PageRecyclerView.PageAdapter<GroupAdapter.ViewHolder> {
    private List<SearchGroupBean> dataList;
    private OnItemClickListener onItemClickListener;
    private JoinGroupPresenter presenter;
    private Context context;
    private SelectAlertDialog selectTimeDialog;
    private Button cancel;
    private Button confirm;
    private DefaultEditText content;
    private TextView title;

    public void setDataList(Context context, List<SearchGroupBean> dataList, JoinGroupPresenter presenter) {
        this.dataList = dataList;
        this.presenter = presenter;
        this.context = context;
        loadDialog();
    }

    @Override
    public int getRowCount() {
        return DRApplication.getInstance().getResources().getInteger(R.integer.item_group_info_row);
    }

    @Override
    public int getColumnCount() {
        return DRApplication.getInstance().getResources().getInteger(R.integer.good_sentence_tab_column);
    }

    @Override
    public int getDataCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public ViewHolder onPageCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = View.inflate(DRApplication.getInstance(), R.layout.item_group_info, null);
        return new ViewHolder(inflate);
    }

    @Override
    public void onPageBindViewHolder(final ViewHolder holder, final int position) {
        final SearchGroupBean bean = dataList.get(position);
        CreatorBean creator = bean.creator;
        holder.groupName.setText(bean.name);
        if (creator != null) {
            holder.groupOwnerName.setText(creator.name);
        }
        holder.joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDialogData(bean);
            }
        });
    }

    private void loadDialog() {
        LinearLayout view = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.dialog_application_note, null);
        selectTimeDialog = new SelectAlertDialog(context);
        // find id
        title = (TextView) view.findViewById(R.id.application_note_dialog_title);
        content = (DefaultEditText) view.findViewById(R.id.application_note_dialog_content);
        confirm = (Button) view.findViewById(R.id.application_note_dialog_confirm);
        cancel = (Button) view.findViewById(R.id.application_note_dialog_cancel);
        //set data
        WindowManager.LayoutParams attributes = selectTimeDialog.getWindow().getAttributes();
        Float heightProportion = Float.valueOf(content.getResources().getString(R.string.application_note_dialog_height));
        Float widthProportion = Float.valueOf(content.getResources().getString(R.string.application_note_dialog_width));
        attributes.height = (int) (Utils.getScreenHeight(DRApplication.getInstance()) * heightProportion);
        attributes.width = (int) (Utils.getScreenWidth(DRApplication.getInstance()) * widthProportion);
        selectTimeDialog.getWindow().setAttributes(attributes);
        selectTimeDialog.setView(view);
    }

    private void loadDialogData(final SearchGroupBean bean) {
        title.setText(DRApplication.getInstance().getString(R.string.application_note));
        content.setText("");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String info = content.getText().toString();
                JoinGroupBean joinGroupBean = new JoinGroupBean();
                String[] array = new String[]{bean._id};
                joinGroupBean.setGroups(array);
                joinGroupBean.setInfo(info);
                presenter.joinGroup(joinGroupBean);
                selectTimeDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectTimeDialog.isShowing()) {
                    selectTimeDialog.dismiss();
                }
            }
        });
        selectTimeDialog.show();
    }

    public interface OnItemClickListener {
        void setOnItemClick(int position, boolean isCheck);
        void setOnItemCheckedChanged(int position, boolean isCheck);
    }

    public void setOnItemListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.info_item_group_name)
        TextView groupName;
        @Bind(R.id.info_item_group_owner_name)
        TextView groupOwnerName;
        @Bind(R.id.info_item_group_join)
        TextView joinGroup;
        View rootView;

        ViewHolder(View view) {
            super(view);
            this.rootView = view;
            ButterKnife.bind(this, view);
        }
    }
}
