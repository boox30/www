package com.onyx.android.note.note.scribble;

import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.onyx.android.note.NoteDataBundle;
import com.onyx.android.note.NoteUIBundle;
import com.onyx.android.note.R;
import com.onyx.android.note.action.CreateDocumentAction;
import com.onyx.android.note.common.base.BaseFragment;
import com.onyx.android.note.databinding.FragmentScribbleBinding;
import com.onyx.android.note.event.BuildSpanTextShapeEvent;
import com.onyx.android.note.event.SpanViewEnableEvent;
import com.onyx.android.note.event.SpanViewEvent;
import com.onyx.android.note.event.menu.CheckMenuRectEvent;
import com.onyx.android.note.event.menu.CloseKeyboardEvent;
import com.onyx.android.note.event.menu.DeleteSpanShapeEvent;
import com.onyx.android.note.handler.HandlerManager;
import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.note.NoteManager;
import com.onyx.android.sdk.note.event.PauseRawDrawingEvent;
import com.onyx.android.sdk.note.event.SetDrawExcludeRectEvent;
import com.onyx.android.sdk.note.widget.LinedEditText;
import com.onyx.android.sdk.scribble.data.Background;
import com.onyx.android.sdk.scribble.data.DocumentOptionArgs;
import com.onyx.android.sdk.scribble.data.NoteBackgroundType;
import com.onyx.android.sdk.utils.TreeObserverUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lxm on 2018/2/2.
 */

public class ScribbleFragment extends BaseFragment {

    private FragmentScribbleBinding binding;
    private SurfaceHolder.Callback surfaceCallback;

    public static ScribbleFragment newInstance() {
        return new ScribbleFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getNoteManager().getEventBus().register(this);
        getNoteBundle().getPenEventHandler().subscribe();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scribble, container, false);
        binding.setModel(getUIBundle().getNoteMenuModel());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getNoteManager().getEventBus().unregister(this);
        getNoteBundle().getPenEventHandler().unSubscribe();
    }

    private void setup() {
        getNoteBundle().getHandlerManager().activeProvider(HandlerManager.EPD_SHAPE_PROVIDER);
        initSurfaceView();
        initSpanView();
    }

    @Subscribe
    public void onCheckMenuRectEvent(CheckMenuRectEvent event) {
        binding.workLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                TreeObserverUtils.removeGlobalOnLayoutListener(binding.workLayout.getViewTreeObserver(), this);
                updateDrawLimitRect();
                postDrawMenuExcludeRect();
            }
        });
    }

    private void postDrawMenuExcludeRect() {
        List<Rect> rectList = new ArrayList<>();
        addExcludeRect(rectList, binding.expand);
        getNoteBundle().post(new SetDrawExcludeRectEvent(rectList));
    }

    private void addExcludeRect(List<Rect> rectList, View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        rectList.add(rect);
    }

    private void updateDrawLimitRect() {
        Rect rect = new Rect();
        binding.surfaceView.getGlobalVisibleRect(rect);
        List<Rect> rectList = new ArrayList<>();
        rectList.add(rect);
        getNoteManager().getTouchHelper().setLimitRect(rectList);
    }

    @Subscribe
    public void onSpanViewEnable(SpanViewEnableEvent event) {
        binding.spanTextView.setVisibility(event.enable ? View.VISIBLE : View.GONE);
        if (event.enable) {
            getNoteManager().post(new SpanViewEvent(binding.spanTextView));
        }
    }

    private void initSpanView() {
        binding.spanTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DEL:
                            getNoteBundle().post(new DeleteSpanShapeEvent());
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            getNoteBundle().post(new CloseKeyboardEvent());
                            return false;
                        case KeyEvent.KEYCODE_1:
                        case KeyEvent.KEYCODE_2:
                        case KeyEvent.KEYCODE_3:
                        case KeyEvent.KEYCODE_4:
                        case KeyEvent.KEYCODE_5:
                        case KeyEvent.KEYCODE_6:
                        case KeyEvent.KEYCODE_7:
                        case KeyEvent.KEYCODE_8:
                        case KeyEvent.KEYCODE_9:
                        case KeyEvent.KEYCODE_0:
                            char displayLabel = keyEvent.getDisplayLabel();
                            buildTextShape(String.valueOf(displayLabel));
                            return true;
                    }
                }
                return false;
            }
        });

        binding.spanTextView.setOnKeyPreImeListener(new LinedEditText.OnKeyPreImeListener() {
            @Override
            public void onKeyPreIme(int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        getNoteBundle().post(new CloseKeyboardEvent());
                        break;
                }
            }
        });

        binding.spanTextView.setInputConnectionListener(new LinedEditText.InputConnectionListener() {
            @Override
            public void commitText(CharSequence text, int newCursorPosition) {
                buildTextShape(text.toString());
            }
        });
    }

    private void buildTextShape(String text) {
        getNoteManager().post(new BuildSpanTextShapeEvent(text));
    }

    private void initSurfaceView() {
        if (surfaceCallback == null) {
            surfaceCallback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    clearSurfaceView();
                    getNoteBundle().getNoteManager().start(binding.surfaceView);
                    setBackgroundMap();
                    createTestDocument();
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    holder.removeCallback(surfaceCallback);
                    surfaceCallback = null;

                }
            };
        }
        binding.surfaceView.getHolder().addCallback(surfaceCallback);
    }

    private void setBackgroundMap() {
        SparseArray<Background> map = new SparseArray<>();
        map.put(NoteBackgroundType.GRID, new Background(R.drawable.scribble_back_ground_grid));
        getNoteManager().setBackgroundMap(map);
    }

    private void clearSurfaceView() {
        Rect rect = getViewportSize();
        EpdController.resetUpdateMode(getScribbleView());
        Canvas canvas = getScribbleView().getHolder().lockCanvas(rect);
        if (canvas == null) {
            return;
        }

        Paint paint = new Paint();
        cleanup(canvas, paint, rect);
        getScribbleView().getHolder().unlockCanvasAndPost(canvas);
    }

    private SurfaceView getScribbleView() {
        return binding.surfaceView;
    }

    private void cleanup(final Canvas canvas, final Paint paint, final Rect rect) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(rect, paint);
    }

    private Rect getViewportSize() {
        return new Rect(0, 0, getScribbleView().getWidth(), getScribbleView().getHeight());
    }

    private void createTestDocument() {
        String docId = UUID.randomUUID().toString();
        String parentId = UUID.randomUUID().toString();
        new CreateDocumentAction(getNoteManager())
                .setDocumentUniqueId(docId)
                .setParentUniqueId(parentId)
                .setOptionArgs(DocumentOptionArgs.create())
                .execute(null);
    }

    @Override
    public boolean onBackPressedSupport() {
        getNoteManager().post(new PauseRawDrawingEvent());
        getNoteManager().quit();
        return false;
    }

    private NoteDataBundle getNoteBundle() {
        return NoteDataBundle.getInstance();
    }

    private NoteManager getNoteManager() {
        return getNoteBundle().getNoteManager();
    }

    private NoteUIBundle getUIBundle() {
        return NoteUIBundle.getInstance();
    }
}