package com.onyx.android.sdk.ui.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.onyx.android.sdk.data.ReaderMenu;
import com.onyx.android.sdk.ui.R;
import com.onyx.android.sdk.ui.view.AutofitRecyclerView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by joy on 6/28/16.
 */
public class ReaderLayerMenuViewFactory {

    private static class MainMenuItemViewHolder extends RecyclerView.ViewHolder {
        private View view;

        public MainMenuItemViewHolder(View itemView, final ReaderMenu.ReaderMenuCallback callback) {
            super(itemView);
            view = itemView;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onMenuItemClicked((ReaderLayerMenuItem)v.getTag());
                }
            });
        }

        public void setMenuItem(ReaderLayerMenuItem item) {
            ((ImageView)view.findViewById(R.id.imageview_icon)).setImageResource(item.getDrawableResourceId());
            ((TextView)view.findViewById(R.id.textview_title)).setText(item.getTitle());
            view.setTag(item);
        }
    }

    public static View createMainMenuContainerView(final Context context, final List<ReaderLayerMenuItem> items, ReaderLayerMenuState state, final ReaderMenu.ReaderMenuCallback callback) {
        return createSimpleButtonContainerView(context, items, state, callback);
    }

    public static View createSubMenuContainerView(final Context context, final ReaderLayerMenuItem parent, final List<ReaderLayerMenuItem> items, final ReaderLayerMenuState state, final ReaderMenu.ReaderMenuCallback callback) {
        if (parent.getURI().getRawPath().compareTo("/Font") == 0) {
            return createFontStyleView(context, items, state, callback);
        } else if (parent.getURI().getRawPath().compareTo("/TTS") == 0) {
            return createTTSView(context, items, state, callback);
        }
        return createSimpleButtonContainerView(context, items, state, callback);
    }

    private static View createSimpleButtonContainerView(final Context context, final List<ReaderLayerMenuItem> items, final ReaderLayerMenuState state, final ReaderMenu.ReaderMenuCallback callback) {
        AutofitRecyclerView view = (AutofitRecyclerView)LayoutInflater.from(context).inflate(R.layout.reader_layer_menu_simple_button_container_recylerview, null);
        final LayoutInflater inflater = LayoutInflater.from(context);
        view.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new MainMenuItemViewHolder(inflater.inflate(R.layout.reader_layer_menu_button_item, parent, false), callback);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((MainMenuItemViewHolder)holder).setMenuItem(items.get(position));
            }

            @Override
            public int getItemCount() {
                return items.size();
            }
        });
        return view;
    }

    private static ReaderLayerMenuItem findItem(final List<ReaderLayerMenuItem> items, final String uri) {
        for (ReaderLayerMenuItem item : items) {
            if (item.getURI().getRawPath().compareTo(uri) == 0) {
                return item;
            }
        }
        return null;
    }

    private static final HashMap<Integer, String> fontSizeViewItemMap;
    private static final HashMap<Integer, String> fontStyleViewItemMap;
    static {
        fontSizeViewItemMap = new HashMap<>();
        fontSizeViewItemMap.put(R.id.text_view_font_size_0, "/Font/SetFontSize");
        fontSizeViewItemMap.put(R.id.text_view_font_size_1, "/Font/SetFontSize");
        fontSizeViewItemMap.put(R.id.text_view_font_size_2, "/Font/SetFontSize");
        fontSizeViewItemMap.put(R.id.text_view_font_size_3, "/Font/SetFontSize");
        fontSizeViewItemMap.put(R.id.text_view_font_size_4, "/Font/SetFontSize");
        fontSizeViewItemMap.put(R.id.text_view_font_size_5, "/Font/SetFontSize");
        fontSizeViewItemMap.put(R.id.text_view_font_size_6, "/Font/SetFontSize");
        fontSizeViewItemMap.put(R.id.text_view_font_size_7, "/Font/SetFontSize");

        fontStyleViewItemMap = new HashMap<>();
        fontStyleViewItemMap.putAll(fontSizeViewItemMap);
        fontStyleViewItemMap.put(R.id.image_view_decrease_font_size, "/Font/DecreaseFontSize");
        fontStyleViewItemMap.put(R.id.image_view_increase_font_size, "/Font/IncreaseFontSize");
        fontStyleViewItemMap.put(R.id.button_set_font_face, "/Font/SetFontFace");
        fontStyleViewItemMap.put(R.id.image_view_indent, "/Font/SetIndent");
        fontStyleViewItemMap.put(R.id.image_view_no_indent, "/Font/SetNoIndent");
        fontStyleViewItemMap.put(R.id.image_view_small_line_spacing, "/Font/SetSmallLineSpacing");
        fontStyleViewItemMap.put(R.id.image_view_middle_line_spacing, "/Font/SetMiddleLineSpacing");
        fontStyleViewItemMap.put(R.id.image_view_large_line_spacing, "/Font/SetLargeLineSpacing");
        fontStyleViewItemMap.put(R.id.image_view_decrease_line_spacing, "/Font/DecreaseLineSpacing");
        fontStyleViewItemMap.put(R.id.image_view_increase_line_spacing, "/Font/IncreaseLineSpacing");
        fontStyleViewItemMap.put(R.id.image_view_small_page_margins, "/Font/SetSmallPageMargins");
        fontStyleViewItemMap.put(R.id.image_view_middle_page_margins, "/Font/SetMiddlePageMargins");
        fontStyleViewItemMap.put(R.id.image_view_large_page_margins, "/Font/SetLargePageMargins");
        fontStyleViewItemMap.put(R.id.image_view_decrease_page_margins, "/Font/DecreasePageMargins");
        fontStyleViewItemMap.put(R.id.image_view_increase_page_margins, "/Font/IncreasePageMargins");
    }

    private static void mapViewMenuItemFunction(final View fontStyleView, final List<ReaderLayerMenuItem> items, final ReaderLayerMenuState state, final ReaderMenu.ReaderMenuCallback callback) {
        for (final HashMap.Entry<Integer, String> entry : fontStyleViewItemMap.entrySet()) {
            final View view = fontStyleView.findViewById(entry.getKey());
            final ReaderLayerMenuItem item = findItem(items, entry.getValue());
            if (view == null || item == null) {
                assert false;
                continue;
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fontSizeViewItemMap.containsKey(entry.getKey())) {
                        // TODO pass back font size value
                        callback.onMenuItemClicked(item);
                    } else {
                        callback.onMenuItemClicked(item);
                    }
                }
            });
        }
    }

    private static View createFontStyleView(final Context context, final List<ReaderLayerMenuItem> items, final ReaderLayerMenuState state, final ReaderMenu.ReaderMenuCallback callback) {
        View view = LayoutInflater.from(context).inflate(R.layout.reader_layer_menu_font_style_view, null);
        mapViewMenuItemFunction(view, items, state, callback);
        return view;
    }

    private static View createTTSView(final Context context, final List<ReaderLayerMenuItem> items, final ReaderLayerMenuState state, final ReaderMenu.ReaderMenuCallback callback) {
        View view = LayoutInflater.from(context).inflate(R.layout.reader_layer_menu_tts_view, null);
        return view;
    }

}
