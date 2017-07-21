package com.onyx.kreader.ui.dialog;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.onyx.android.sdk.ui.view.PageRecyclerView;
import com.onyx.kreader.R;
import com.onyx.kreader.ui.ReaderTabManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by joy on 11/1/16.
 */
public class DialogTabHostMenu extends DialogBase {

    public static abstract class Callback {
        public abstract void onDoubleOpen(ReaderTabManager.ReaderTab tab);
        public abstract void onLinkedOpen(ReaderTabManager.ReaderTab tab);
        public abstract void onSideOpen(ReaderTabManager.ReaderTab left, ReaderTabManager.ReaderTab right);
        public abstract void onClosing(List<ReaderTabManager.ReaderTab> list);
    }

    private class TabViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private CheckBox checkBox;

        private ReaderTabManager.ReaderTab currentTab;

        public TabViewHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.textview);
            checkBox = (CheckBox)itemView.findViewById(R.id.checkbox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedTabs.add(currentTab);
                    } else {
                        selectedTabs.remove(currentTab);
                    }

                    disableAllButtons();
                    if (selectedTabs.size() > 0) {
                        buttonClose.setEnabled(true);
                    }
                    if (selectedTabs.size() == 1) {
                        buttonDoubleOpen.setEnabled(true);
                        buttonDoubleLink.setEnabled(true);
                    }
                    if (selectedTabs.size() == 2) {
                        buttonSideOpen.setEnabled(true);
                    }
                }
            });
        }

        public void bindView(ReaderTabManager.ReaderTab tab, String path) {
            currentTab = tab;
            textView.setText(path);
        }
    }

    @Bind(R.id.button_double_open)
    Button buttonDoubleOpen;
    @Bind(R.id.button_double_link)
    Button buttonDoubleLink;
    @Bind(R.id.button_side_open)
    Button buttonSideOpen;
    @Bind(R.id.button_close)
    Button buttonClose;

    private ReaderTabManager tabManager;
    private ArrayList<ReaderTabManager.ReaderTab> tabList = new ArrayList<>();
    private ArrayList<ReaderTabManager.ReaderTab> selectedTabs = new ArrayList<>();

    private Callback callback;

    public DialogTabHostMenu(final Context context, final ReaderTabManager tabManager, Callback callback) {
        super(context);
        setContentView(R.layout.dialog_tab_host_menu);

        ButterKnife.bind(this);

        this.tabManager = tabManager;
        this.callback = callback;

        for (LinkedHashMap.Entry<ReaderTabManager.ReaderTab, String> entry : tabManager.getOpenedTabs().entrySet()) {
            tabList.add(0, entry.getKey());
        }

        init();
    }

    @OnClick(R.id.button_double_open)
    void onButtonDoubleOpenClicked() {
        if (callback != null) {
            callback.onDoubleOpen(selectedTabs.get(0));
        }
    }

    @OnClick(R.id.button_double_link)
    void onButtonDoubleLinkClicked() {
        if (callback != null) {
            callback.onLinkedOpen(selectedTabs.get(0));
        }
    }

    @OnClick(R.id.button_side_open)
    void onButtonSideOpenClicked() {
        if (callback != null) {
            callback.onSideOpen(selectedTabs.get(0), selectedTabs.get(1));
        }
    }

    @OnClick(R.id.button_close)
    void onButtonCloseClicked() {
        if (callback != null) {
            callback.onClosing(selectedTabs);
        }
    }

    private void init() {
        PageRecyclerView recyclerView = (PageRecyclerView)findViewById(R.id.recycler_view_tab);
        recyclerView.setAdapter(new PageRecyclerView.PageAdapter() {
            @Override
            public int getRowCount() {
                return tabManager.getOpenedTabs().size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public int getDataCount() {
                return tabManager.getOpenedTabs().size();
            }

            @Override
            public RecyclerView.ViewHolder onPageCreateViewHolder(ViewGroup parent, int viewType) {
                return new TabViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.dialog_tab_host_menu_item_view, parent, false));
            }

            @Override
            public void onPageBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ReaderTabManager.ReaderTab tab = tabList.get(position);
                ((TabViewHolder)holder).bindView(tab, tabManager.getOpenedTabs().get(tab));
            }

        });

        disableAllButtons();
    }

    private void disableAllButtons() {
        buttonDoubleOpen.setEnabled(false);
        buttonDoubleLink.setEnabled(false);
        buttonSideOpen.setEnabled(false);
        buttonClose.setEnabled(false);
    }

}
