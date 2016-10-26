package com.onyx.android.sdk.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onyx.android.sdk.ui.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by joy on 7/6/16.
 */
public class TreeRecyclerView extends PageRecyclerView {

    public static abstract class Callback {
        public abstract void onTreeNodeClicked(TreeNode node);
        public abstract void onItemCountChanged(int position,int itemCount);
    }

    public static class TreeNode {
        private TreeNode parent;
        private int treeDepth;
        private ArrayList<TreeNode> children;
        private String title;
        private String description;
        private Object tag;

        public TreeNode(TreeNode parent, String title, String description, Object tag) {
            this.parent = parent;
            this.title = title;
            this.description = description;
            this.tag = tag;

            treeDepth = parent == null ? 0 : parent.treeDepth + 1;
        }

        public boolean hasChildren() {
            return children != null && children.size() > 0;
        }

        public ArrayList<TreeNode> getChildren() {
            return children;
        }

        public void addChild(TreeNode node) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(node);
        }

        public Object getTag() {
            return tag;
        }
    }

    private static class FlattenTreeNodeDataList {
        private static abstract class Callback {
            public abstract void notifyItemRangeInserted(int position, int size);
            public abstract void notifyItemRangeRemoved(int position, int size);
        }

        private ArrayList<TreeNode> list = new ArrayList<>();
        private HashSet<TreeNode> expandedSet = new HashSet<>();
        private Callback callback;

        public void init(Collection<TreeNode> rootNodes) {
            list.clear();
            expandedSet.clear();
            list.addAll(rootNodes);
        }

        public void registerCallback(Callback callback) {
            this.callback = callback;
        }

        public int size() {
            return list.size();
        }

        public TreeNode get(int index) {
            return list.get(index);
        }

        public boolean isNodeExpanded(TreeNode node) {
            return expandedSet.contains(node);
        }

        public void expand(TreeNode parent) {
            if (!parent.hasChildren()) {
                assert false;
                return;
            }

            int idx = list.indexOf(parent);
            if (idx < 0) {
                return;
            }
            list.addAll(idx + 1, parent.children);
            expandedSet.add(parent);

            if (callback != null) {
                callback.notifyItemRangeInserted(idx + 1, parent.children.size());
            }
        }

        public void expandTo(TreeNode node) {
            Stack<TreeNode> stack = new Stack<>();
            while (node.parent != null) {
                stack.push(node.parent);
                node = node.parent;
            }
            while (!stack.isEmpty()) {
                expand(stack.pop());
            }
        }

        public void collapse(TreeNode parent) {
            if (!isNodeExpanded(parent)) {
                assert false;
                return;
            }

            int idx = list.indexOf(parent);
            if (idx < 0) {
                return;
            }
            int size = summarizeFlattenChildrenSize(parent);
            for (int i = 0; i < size; i++) {
                expandedSet.remove(list.get(idx + 1));
                list.remove(idx + 1);
            }
            expandedSet.remove(parent);

            if (callback != null) {
                callback.notifyItemRangeRemoved(idx + 1, size);
            }
        }

        private int summarizeFlattenChildrenSize(TreeNode parent) {
            int size = parent.children.size();
            for (TreeNode node : parent.children) {
                if (expandedSet.contains(node)) {
                    size += summarizeFlattenChildrenSize(node);
                }
            }
            return size;
        }

        private ArrayList<TreeNode> flatTreeNodes(Collection<TreeNode> nodes) {
            ArrayList<TreeNode> list = new ArrayList<>();
            for (TreeNode node : nodes) {
                list.add(node);
                if (node.hasChildren()) {
                    expandedSet.add(node);
                    list.addAll(flatTreeNodes(node.children));
                }
            }
            return list;
        }
    }

    private static class TreeNodeViewHolder extends PageRecyclerView.ViewHolder {
        private Callback callback;

        private ImageView imageViewIndicator;
        private TextView textViewTitle;
        private TextView textViewDescription;
        private View splitLine;

        public TreeNodeViewHolder(View itemView, Callback callback) {
            super(itemView);

            this.callback = callback;

            imageViewIndicator = (ImageView)itemView.findViewById(R.id.image_view_indicator);
            textViewTitle = (TextView)itemView.findViewById(R.id.text_view_title);
            textViewDescription = (TextView)itemView.findViewById(R.id.text_view_description);
            splitLine = itemView.findViewById(R.id.split_line);
        }

        public void bindView(final FlattenTreeNodeDataList list, final int position,int rowCount,ViewGroup parent,TreeNode currentNode) {
            final TreeNode node = list.get(position);

            final RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams)imageViewIndicator.getLayoutParams();
            final RelativeLayout.LayoutParams lineParams= (RelativeLayout.LayoutParams)splitLine.getLayoutParams();
            Resources res = parent.getContext().getResources();
            float imageSize = res.getDimension(R.dimen.image_view_indicator_size);
            float marginRight = res.getDimension(R.dimen.image_view_indicator_margin_right);
            int paddingLeftPx = (int)(imageSize + marginRight);
            imageParams.leftMargin = paddingLeftPx * node.treeDepth;
            imageViewIndicator.setLayoutParams(imageParams);
            lineParams.leftMargin = (int)(imageSize + marginRight);
            splitLine.setLayoutParams(lineParams);

            textViewTitle.setText(node.title);
            textViewDescription.setText(node.description);
            textViewTitle.getPaint().setUnderlineText(currentNode.equals(node));
            splitLine.setVisibility(VISIBLE);

            if (!node.hasChildren()) {
                imageViewIndicator.setVisibility(INVISIBLE);
            } else {
                imageViewIndicator.setVisibility(VISIBLE);
            }
            if (list.isNodeExpanded(node)) {
                imageViewIndicator.setImageResource(R.drawable.ic_tree_recycler_item_view_indicator_expand);
            } else {
                imageViewIndicator.setImageResource(R.drawable.ic_tree_recycler_item_view_indicator);
            }
            imageViewIndicator.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list.isNodeExpanded(node)) {
                        imageViewIndicator.setImageResource(R.drawable.ic_tree_recycler_item_view_indicator);
                        list.collapse(node);
                    } else {
                        imageViewIndicator.setImageResource(R.drawable.ic_tree_recycler_item_view_indicator_expand);
                        list.expand(node);
                    }
                }
            });

            textViewTitle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onTreeNodeClicked(node);
                    }
                }
            });

        }

    }

    private static class TreeAdapter extends PageRecyclerView.PageAdapter<TreeNodeViewHolder> {

        private FlattenTreeNodeDataList list;
        private Callback callback;
        private int mRowCount;
        private TreeNode currentNode;

        public TreeAdapter(FlattenTreeNodeDataList list, final Callback callback, int rowCount) {
            this.list = list;
            this.callback = callback;
            this.mRowCount = rowCount;

            this.list.registerCallback(new FlattenTreeNodeDataList.Callback() {
                @Override
                public void notifyItemRangeInserted(int position, int size) {
                    TreeAdapter.this.notifyItemRangeInserted(position, size);
                    if (callback != null){
                        callback.onItemCountChanged(position - 1,getItemCount());
                    }
                }

                @Override
                public void notifyItemRangeRemoved(int position, int size) {
                    TreeAdapter.this.notifyItemRangeRemoved(position, size);
                    if (callback != null){
                        callback.onItemCountChanged(position - 1,getItemCount());
                    }
                }
            });
        }

        public void setCurrentNode(TreeNode currentNode) {
            this.currentNode = currentNode;
        }

        @Override
        public int getRowCount() {
            return mRowCount;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getDataCount() {
            return list.size();
        }

        @Override
        public TreeNodeViewHolder onPageCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tree_recycler_item_view, parent, false);
            return new TreeNodeViewHolder(view, callback);
        }

        @Override
        public void onPageBindViewHolder(TreeNodeViewHolder holder, int position) {
            holder.bindView(list, position,mRowCount, pageRecyclerView,currentNode);
        }

    }

    FlattenTreeNodeDataList list = new FlattenTreeNodeDataList();
    TreeAdapter adapter;

    public TreeRecyclerView(Context context) {
        super(context);
    }

    public TreeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TreeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bindTree(Collection<TreeNode> rootNodes, Callback callback,int row) {
        list.init(rootNodes);
        adapter = new TreeAdapter(list, callback,row);
        this.setAdapter(adapter);
    }

    public void expandTo(TreeNode node) {
        list.expandTo(node);
    }

    public void setCurrentNode(TreeNode node){
        adapter.setCurrentNode(node);
    }
}
