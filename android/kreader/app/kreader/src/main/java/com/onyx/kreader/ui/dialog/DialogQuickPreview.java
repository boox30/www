package com.onyx.kreader.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.GPaginator;
import com.onyx.android.sdk.data.Size;
import com.onyx.kreader.R;
import com.onyx.kreader.api.ReaderDocumentTableOfContent;
import com.onyx.kreader.api.ReaderDocumentTableOfContentEntry;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.common.Debug;
import com.onyx.kreader.ui.actions.GetTableOfContentAction;
import com.onyx.kreader.ui.actions.GotoPageAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.data.SingletonSharedPreference;
import com.onyx.kreader.utils.PagePositionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by joy on 7/15/16.
 */
public class DialogQuickPreview extends Dialog {

    public static abstract class Callback {
        public abstract void abort();
        public abstract void requestPreview(final List<Integer> pages, final Size desiredSize);
    }

    static class GridType{
        final static int Four = 4;
        final static int Nine = 9;
    }

    private static class Grid {
        private int grid = GridType.Four;

        public void setGridType(int grid) {
            this.grid = grid;
        }

        public int getGridType() {
            return grid;
        }

        public int getRows() {
            return grid == GridType.Four ? 2 : 3;
        }

        public int getColumns() {
            return grid == GridType.Four ? 2 : 3;
        }

        public int getGridSize() {
            return getRows() * getColumns();
        }
    }

    private class PreviewViewHolder extends RecyclerView.ViewHolder {
        private int page;
        private ImageView imageView;
        private TextView pageTextView;
        private Button btnPage;
        private RelativeLayout container;

        public PreviewViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            pageTextView = (TextView) itemView.findViewById(R.id.text_view_page);
            btnPage = (Button) itemView.findViewById(R.id.btn_page);
            container = (RelativeLayout) itemView.findViewById(R.id.item_container);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogQuickPreview.this.dismiss();
                    new GotoPageAction(PagePositionUtils.fromPageNumber(page), true).execute(readerDataHolder);
                }
            });
        }

        public void bindPreview(Bitmap bitmap,Grid grid, ViewGroup parent) {
            imageView.setImageBitmap(bitmap);
            if (grid.getGridType() == GridType.Four){
                pageTextView.setVisibility(View.VISIBLE);
                btnPage.setVisibility(View.GONE);
                String str = String.format(parent.getContext().getString(R.string.page),page + 1);
                pageTextView.setText(str);
            }else if (grid.getGridType() == GridType.Nine){
                pageTextView.setVisibility(View.GONE);
                btnPage.setVisibility(View.VISIBLE);
                btnPage.setText(String.valueOf(page + 1));
            }
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }
    }

    private class PreviewAdapter extends RecyclerView.Adapter<PreviewViewHolder> {

        private final Bitmap BlankBitmap = Bitmap.createBitmap(300, 400, Bitmap.Config.ARGB_8888);

        private ViewGroup parent;
        private Grid grid;
        private Size childSize = new Size(300, 400);
        private ArrayList<Bitmap> bitmapList = new ArrayList<>();
        private HashMap<Integer, Bitmap> bitmapCache = new HashMap<>();

        public PreviewAdapter() {
            BlankBitmap.eraseColor(Color.WHITE);
        }

        public void requestMissingBitmaps() {
            ArrayList<Integer> toRequest = new ArrayList<>();
            HashMap<Integer, Bitmap> cache = new HashMap<>();
            for (int i = 0; i < bitmapList.size(); i++) {
                int page = paginator.indexByPageOffset(i);
                if (bitmapCache.containsKey(page)) {
                    cache.put(page, bitmapCache.get(page));
                    setBitmap(i, cache.get(page));
                    bitmapCache.remove(page);
                    continue;
                }
                toRequest.add(page);
            }
            for (Bitmap bitmap : bitmapCache.values()) {
                bitmap.recycle();
            }
            bitmapCache.clear();
            bitmapCache.putAll(cache);

            callback.requestPreview(toRequest, childSize);
        }

        public void resetListSize(int size) {
            bitmapList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                bitmapList.add(BlankBitmap);
            }
            notifyDataSetChanged();
        }

        public Size getDesiredSize() {
            return childSize;
        }

        public void setBitmap(int index, Bitmap bitmap) {
            Debug.d("setBitmap: " + bitmap);
            bitmapList.set(index, bitmap);
            bitmapCache.put(paginator.indexByPageOffset(index), bitmap);
            // notifyItemChanged() will cause delayed update on UI, not sure why
            notifyDataSetChanged();
        }

        public void setGridType(Grid grid) {
            this.grid = grid;
        }

        @Override
        public PreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            this.parent = parent;
            return new PreviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_quick_preview_list_item_view, parent, false));
        }

        @Override
        public void onBindViewHolder(PreviewViewHolder holder, int position) {
            GridLayoutManager.LayoutParams params = (GridLayoutManager.LayoutParams)holder.itemView.getLayoutParams();
            if (params == null) {
                params = new GridLayoutManager.LayoutParams(GridLayoutManager.LayoutParams.MATCH_PARENT,
                        GridLayoutManager.LayoutParams.MATCH_PARENT);
            }
            int spaceHeight = ((GridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).topMargin +
                    ((GridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).bottomMargin;
            spaceHeight = spaceHeight * grid.getRows();
            int spaceWidth = ((GridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).leftMargin +
                    ((GridLayoutManager.LayoutParams) holder.itemView.getLayoutParams()).rightMargin;
            spaceWidth = spaceWidth * grid.getColumns();
            int itemWidth = (parent.getMeasuredWidth() - spaceWidth)  / grid.getColumns();
            int itemHeight = (parent.getMeasuredHeight() - spaceHeight) / grid.getRows();
            childSize.width = itemWidth;
            childSize.height = itemHeight;

            params.height = itemHeight;
            params.width = itemWidth;
            holder.itemView.setLayoutParams(params);
            Bitmap bmp = bitmapList.get(position);
            if (bmp == null) {
                bmp = BlankBitmap;
            }

            holder.setPage(paginator.indexByPageOffset(position));
            holder.bindPreview(bmp,grid,parent);
            holder.container.setActivated(readerDataHolder.getCurrentPage() == paginator.indexByPageOffset(position));
        }

        @Override
        public int getItemCount() {
            return bitmapList.size();
        }
    }

    private RecyclerView gridRecyclerView;
    private TextView textViewProgress;
    private SeekBar seekBarProgress;
    private ImageView fourImageGrid;
    private ImageView nineImageGrid;
    private ImageButton chapterBack;
    private ImageButton chapterForward;

    private Grid grid = new Grid();
    private GPaginator paginator;
    private PreviewAdapter adapter = new PreviewAdapter();

    private ReaderDataHolder readerDataHolder;
    private int pageCount;
    private int currentPage;
    private Callback callback;
    private List<Integer> tocChapterNodeList = new ArrayList<>();

    public DialogQuickPreview(@NonNull final ReaderDataHolder readerDataHolder, final int pageCount, final int currentPage,
                              Callback callback) {
        super(readerDataHolder.getContext(), R.style.dialog_no_title);
        setContentView(R.layout.dialog_quick_preview);

        this.readerDataHolder = readerDataHolder;
        this.pageCount = pageCount;
        this.currentPage = currentPage;
        this.callback = callback;

        fitDialogToWindow();
        grid.setGridType(SingletonSharedPreference.getQuickViewGridType(getContext(), GridType.Four));
        setupLayout();
        setupContent(pageCount, currentPage);
    }

    private void fitDialogToWindow() {
        Window mWindow = getWindow();
        WindowManager.LayoutParams mParams = mWindow.getAttributes();
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.gravity = Gravity.BOTTOM;
        mWindow.setAttributes(mParams);
        //force use all space in the screen.
        mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void setupLayout() {
        gridRecyclerView = (RecyclerView)findViewById(R.id.grid_view_preview);
        gridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), grid.getColumns()));
        gridRecyclerView.setAdapter(adapter);

        textViewProgress = (TextView)findViewById(R.id.text_view_progress);
        seekBarProgress = (SeekBar)findViewById(R.id.seek_bar_page);
        fourImageGrid = (ImageView) findViewById(R.id.image_view_four_grids);
        nineImageGrid = (ImageView)findViewById(R.id.image_view_nine_grids);
        chapterBack = (ImageButton) findViewById(R.id.chapter_back);
        chapterForward = (ImageButton)findViewById(R.id.chapter_forward);

        findViewById(R.id.image_view_prev_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevPage();
            }
        });

        findViewById(R.id.image_view_next_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage();
            }
        });

        findViewById(R.id.image_view_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogQuickPreview.this.dismiss();
            }
        });

        fourImageGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grid.setGridType(GridType.Four);
                SingletonSharedPreference.setQuickViewGridType(getContext(), GridType.Four);
                paginator.resize(grid.getRows(), grid.getColumns(), pageCount);
                paginator.gotoPageByIndex(currentPage);
                gridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), grid.getColumns()));
                adapter.setGridType(grid);
                onPageDataChanged();
                onPressedImageView(true);
            }
        });

        nineImageGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grid.setGridType(GridType.Nine);
                SingletonSharedPreference.setQuickViewGridType(getContext(), GridType.Nine);
                paginator.resize(grid.getRows(), grid.getColumns(), pageCount);
                paginator.gotoPageByIndex(currentPage);
                gridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), grid.getColumns()));
                adapter.setGridType(grid);
                onPageDataChanged();
                onPressedImageView(false);
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (callback != null) {
                    callback.abort();
                }
            }
        });

        chapterBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoChapterIndex(true);
            }
        });

        chapterForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoChapterIndex(false);
            }
        });
    }

    private int getGridPage(int page){
        return page / grid.getGridType();
    }

    private void loadDocumentTableOfContent(){
        GetTableOfContentAction.execute(readerDataHolder, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                BaseReaderRequest readerRequest = (BaseReaderRequest)request;
                ReaderDocumentTableOfContent toc = readerRequest.getReaderUserDataInfo().getTableOfContent();
                chapterBack.setEnabled(toc != null && toc.getRootEntry() != null);
                chapterForward.setEnabled(toc != null && toc.getRootEntry() != null);
                buildChapterNodeList(toc);
                updateChapterButtonState();
            }
        });
    }

    private void gotoChapterIndex(boolean back){
        int chapterPosition;
        if (back){
            int pageBegin = paginator.getCurrentPageBegin();
            chapterPosition = getChapterPositionByPage(pageBegin, back);
        }else {
            int pageEnd = paginator.getCurrentPageEnd();
            chapterPosition = getChapterPositionByPage(pageEnd, back);
        }

        paginator.gotoPage(getGridPage(chapterPosition));
        onPageDataChanged();
    }
    private void updateChapterButtonState(){
        chapterBack.setEnabled(paginator.canPrevPage() && tocChapterNodeList.size() > 0);
        chapterForward.setEnabled(paginator.canNextPage() && tocChapterNodeList.size() > 0);
    }

    private int getChapterPositionByPage(int page, boolean back){
        int size = tocChapterNodeList.size();
        for (int i = 0; i < size; i++) {
            if (page < tocChapterNodeList.get(i)){
                if (back){
                    int position = tocChapterNodeList.get(Math.max(0, i - 1));
                    if (paginator.isItemInCurrentPage(position)){
                        return getChapterPositionByPage(page - 1, back);
                    }else {
                        return position;
                    }
                }else {
                    int position = tocChapterNodeList.get(i);
                    if (paginator.isItemInCurrentPage(position)){
                        return getChapterPositionByPage(page + 1, back);
                    }else {
                        return position;
                    }
                }
            }
        }

        if (back){
            return page - 1;
        }else {
            return page + 1;
        }

    }

    private void buildChapterNodeList(ReaderDocumentTableOfContent toc){
        ReaderDocumentTableOfContentEntry rootEntry = toc.getRootEntry();
        if (rootEntry.getChildren() != null){
            buildChapterNode(rootEntry.getChildren());
        }
    }

    private void buildChapterNode(List<ReaderDocumentTableOfContentEntry> entries){
        for (ReaderDocumentTableOfContentEntry entry : entries) {
            if (entry.getChildren() != null){
                buildChapterNode(entry.getChildren());
            }else {
                int position = Integer.valueOf(entry.getPosition());
                if (!tocChapterNodeList.contains(position)){
                    tocChapterNodeList.add(Integer.valueOf(entry.getPosition()));
                }
            }
        }
    }

    private void onPressedImageView(boolean pressedFourImage){
        nineImageGrid.setImageResource(pressedFourImage ? R.drawable.ic_dialog_reader_page_nine_white_focused
                : R.drawable.ic_dialog_reader_page_nine_black_focused);
        fourImageGrid.setImageResource(pressedFourImage ? R.drawable.ic_dialog_reader_page_four_black_focused
                : R.drawable.ic_dialog_reader_page_four_white_focused);
    }

    private void setupContent(int pageCount, int currentPage) {
        paginator = new GPaginator(grid.getRows(), grid.getColumns(), pageCount);
        paginator.gotoPageByIndex(currentPage);
        adapter.setGridType(grid);
        adapter.resetListSize(paginator.itemsInCurrentPage());
        if (callback != null) {
            adapter.requestMissingBitmaps();
        }
        onPressedImageView(grid.getGridType() == GridType.Four);
        initPageProgress();
        loadDocumentTableOfContent();
    }

    /**
     * will clone a copy of passed in bitmap
     * @param page
     * @param bitmap
     */
    public void updatePreview(int page, Bitmap bitmap) {
        if (paginator.isItemInCurrentPage(page)) {
            adapter.setBitmap(paginator.offsetInCurrentPage(page), getScaledPreview(bitmap));
        }
    }

    private Bitmap getScaledPreview(Bitmap pageBitmap) {
        return Bitmap.createScaledBitmap(pageBitmap, adapter.getDesiredSize().width, adapter.getDesiredSize().height, false);
    }

    private void onPageDataChanged() {
        currentPage = paginator.getCurrentPageBegin();
        adapter.resetListSize(paginator.itemsInCurrentPage());
        adapter.requestMissingBitmaps();
        updatePageProgress();
        updateChapterButtonState();
    }

    private void initPageProgress() {
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                int page = progress - 1;
                paginator.gotoPage(page);
                onPageDataChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updatePageProgress();
    }

    private void updatePageProgress() {
        seekBarProgress.setMax(paginator.pages());
        seekBarProgress.setProgress(paginator.getCurrentPage() + 1);
        textViewProgress.setText((paginator.getCurrentPage() + 1) + "/" + paginator.pages());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_PAGE_DOWN:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                nextPage();
                return true;
            case KeyEvent.KEYCODE_PAGE_UP:
            case KeyEvent.KEYCODE_VOLUME_UP:
                prevPage();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void nextPage(){
        if (paginator.nextPage()) {
            onPageDataChanged();
        }
    }

    private void prevPage(){
        if (paginator.prevPage()) {
            onPageDataChanged();
        }
    }
}
