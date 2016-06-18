package com.onyx.kreader.tests;

import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import com.onyx.kreader.api.ReaderPluginOptions;
import com.onyx.kreader.api.ReaderSelection;
import com.onyx.kreader.api.ReaderViewOptions;
import com.onyx.kreader.common.BaseRequest;
import com.onyx.kreader.common.ReaderViewInfo;
import com.onyx.kreader.common.BaseCallback;
import com.onyx.kreader.common.BaseReaderRequest;
import com.onyx.kreader.host.impl.ReaderPluginOptionsImpl;
import com.onyx.kreader.host.impl.ReaderViewOptionsImpl;
import com.onyx.kreader.host.math.PageInfo;
import com.onyx.kreader.host.navigation.NavigationArgs;
import com.onyx.kreader.host.options.BaseOptions;
import com.onyx.kreader.host.options.ReaderConstants;
import com.onyx.kreader.host.request.*;
import com.onyx.kreader.host.wrapper.Reader;
import com.onyx.kreader.host.wrapper.ReaderManager;
import com.onyx.kreader.plugins.pdfium.PdfiumJniWrapper;
import com.onyx.kreader.plugins.pdfium.PdfiumSelection;
import com.onyx.kreader.scribble.data.RawInputReader;
import com.onyx.kreader.text.*;
import com.onyx.kreader.utils.BitmapUtils;
import com.onyx.kreader.R;
import com.onyx.kreader.utils.ImageUtils;
import com.onyx.kreader.utils.TestUtils;

import java.util.*;

/**
 * Created by zhuzeng on 10/5/15.
 */
public class ReaderTestActivity extends Activity {

    private boolean testPrerender = false;

    private enum TestCase { Original, ToPage, ToWidth, PageList, ContinuousList  }
    private enum EmboldenLevel { L0, L1, L2, L3 }
    private enum ContrastLevel { L0, L100, L150, L200 }

    private Reader reader;
    //String path = "/mnt/sdcard/cityhunter/cityhunter10.pdf";
     String path = "/mnt/sdcard/Books/a.pdf";
//   String path = "/mnt/sdcard/Books/lz.13.pdf";
//    String path = "/mnt/sdcard/Pictures/normal.jpg";
    private int pn = 0;
    private int next = 0;
    private EditText searchEdit;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private SurfaceHolder.Callback surfaceHolderCallback;
    private Button button;
    private int index = 0;
    private String sourceZhCN = "八年来，我研究了我的朋友歇洛克·福尔摩斯的破案方法，记录了七十多个案例。我粗略地翻阅一下这些案例的记录，发现许多案例是悲剧性的，也有一些是喜剧性的，其中很大一部分仅仅是离奇古怪而已，但是却没有一例是平淡无奇的。这是因为，他做工作与其说是为了获得酬金，还不如说是出于对他那门技艺的爱好。除了显得独特或甚至于是近乎荒诞无稽的案情外，他对其它案情从来是不屑一顾，拒不参与任何侦查的。可是，在所有这些变化多端的案例中，我却回忆不起有哪一例会比萨里郡斯托克莫兰的闻名的罗伊洛特家族①那一例更具有异乎寻常的特色了。现在谈论的这件事，发生在我和福尔摩斯交往的早期。那时，我们都是单身汉，在贝克街合住一套寓所。本来我早就可以把这件事记录下来，但是，当时我曾作出严守秘密的保证，直至上月，由于我为之作出过保证的那位女士不幸过早地逝世，方始解除了这种约束。现在，大概是使真相大白于天下的时候了，因为我确实知道，外界对于格里姆斯比·罗伊洛特医生之死众说纷纭，广泛流传着各种谣言。这些谣言使得这桩事情变得比实际情况更加骇人听闻。①英格兰东南部一郡。——译者注事情发生在一八八三年四月初的时候。一天早上，我一觉醒来，发现歇洛克·福尔摩斯穿得整整齐齐在我的床边。一般来说，他是一个爱睡懒觉的人，而壁炉架上的时钟，才刚七点一刻，我有些诧异地朝他眨了眨眼睛，心里还有点不乐意，因为我自己的生活习惯是很有规律的。“对不起，把你叫醒了，华生，\"他说，“但是，你我今天早上都命该如此，先是赫德森太太被敲门声吵醒，接着她报复似地来吵醒我，现在是我来把你叫醒。”“那么，什么事——失火了吗？”“不，是一位委托人。好象是一位年轻的女士来临，她情绪相当激动，坚持非要见我不可。现在她正在起居室里等候。你瞧，如果有些年轻的女士这么一清早就徘徊于这个大都市，甚至把还在梦乡的人从床上吵认为那必定是一件紧急的事情，她们不得不找人商量。假如这件事将是一件有趣的案子，那么，我肯定你一定希望从一开始就能有所了解。我认为无论如何应该把你叫醒，给予你这个机会。”“我的老兄，那我是无论如何也不肯失掉这个机会的。”我最大的乐趣就是观察福尔摩斯进行专业性的调查工作，欣赏他迅速地做出推论，他推论之敏捷，犹如是单凭直觉而做出的，但却总是建立在逻辑的基础之上。他就是依靠这些解决了委托给他的疑难问题。我匆匆地穿上衣服，几分钟后就准备就绪，随同我的朋友来到楼下的起居室。一位女士端坐窗前，她身穿黑色衣服，蒙着厚厚的面。她在我们走进房间时站起身来。“早上好，小姐，\"福尔摩斯愉快地说道，“我的名字是歇洛克·福尔摩斯。这位是我的挚友和伙伴华生医生。在他面前，你可以象在我面前一样地谈话，不必顾虑。哈！赫德森太太想得很周到，我很高兴看到她已经烧旺了壁炉。请凑近炉火坐坐，我叫人给你端一杯热咖啡，我看你在发抖。”“我不是因为觉得冷才发抖的，\"那个女人低声地说，同时，她按照福尔摩斯的请求换了个座位。“那么，是为什么呢？”";
    private String source = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,;.'";
    private String TAG = ReaderTestActivity.class.getSimpleName();
    private Bitmap bitmap;
    private int currentPage = 0;
    private PdfiumJniWrapper wrapper = new PdfiumJniWrapper();
    private float xScale, yScale;
    private float startX, startY, endX, endY;
    private ReaderViewInfo readerViewInfo;
    private RectF selectionRect;
    private TestCase testCase = TestCase.Original;
    private EmboldenLevel emboldenLevel = EmboldenLevel.L0;
    private ContrastLevel contrastLevel = ContrastLevel.L0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initSurfaceView();
        OnyxHyphen.reinit_hyph(this, OnyxHyphen.HYPH_ENGLISH);
        test();
    }

   private void test() {
        final RawInputReader input = new RawInputReader();
        final Matrix matrix = new Matrix();

        // read config later.
        // left matrix m * pt
        matrix.postRotate(90);
        matrix.postTranslate(1200, 0);
        // right matrix pt * scale.
        matrix.preScale(1600.0f / 10206.0f, 1200.0f / 7422.0f);
        input.setMatrix(matrix);
        input.start();
    }

    private <E extends Enum<E>> E loopNextEnumValue(E current) {
        int next = current.ordinal() + 1;
        if (next >= EnumSet.allOf(current.getClass()).size()) {
            next = 0;
        }
        return (E)EnumSet.allOf(current.getClass()).toArray()[next];
    }

    private void initSurfaceView() {
        button = (Button)findViewById(R.id.open);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    path = ((EditText)findViewById(R.id.path)).getText().toString();
                    testReaderOpen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        button = (Button)findViewById(R.id.update);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    testReaderOpen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final Button btnTestCase = (Button)findViewById(R.id.switch_test);
        btnTestCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testCase = loopNextEnumValue(testCase);
                btnTestCase.setText("switch: " + testCase.toString());
            }
        });
        btnTestCase.setText("switch: " + testCase.toString());

        final Button btnContrast = (Button)findViewById(R.id.set_contrast);
        btnContrast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contrastLevel = loopNextEnumValue(contrastLevel);
                btnContrast.setText("contrast: " + contrastLevel.toString());
            }
        });
        btnContrast.setText("contrast: " + contrastLevel.toString());

        final Button btnEmbolden = (Button)findViewById(R.id.set_embloden);
        btnEmbolden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emboldenLevel = loopNextEnumValue(emboldenLevel);
                btnEmbolden.setText("bold: " + emboldenLevel.toString());
            }
        });
        btnEmbolden.setText("bold: " + emboldenLevel.toString());

        button = (Button)findViewById(R.id.hyphen);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testPdfiumWrapper();
                testHyphen();
            }
        });

        searchEdit = (EditText)findViewById(R.id.search);

        findViewById(R.id.prev_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textPreviousScreen();
            }
        });

        findViewById(R.id.next_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testNextScreen();
            }
        });

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolderCallback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas canvas = holder.lockCanvas();
                canvas.drawColor(Color.WHITE);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Canvas canvas = holder.lockCanvas();
                canvas.drawColor(Color.WHITE);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                holder.removeCallback(surfaceHolderCallback);
            }
        };

        surfaceView.getHolder().addCallback(surfaceHolderCallback);
        holder = surfaceView.getHolder();
        surfaceView.setFocusable(true);
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.requestFocusFromTouch();

        surfaceView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                surfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = event.getX();
                    startY = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    endX = event.getX();
                    endY = event.getY();
//                    draw();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    endX = event.getX();
                    endY = event.getY();
                    selectionRect = new RectF(startX, startY, endX, endY);
                    testScaleByRect();
                }
                return true;
            }
        });

        ((EditText)findViewById(R.id.path)).setText(path);
    }

    public ReaderViewOptions getViewOptions() {
        return new ReaderViewOptionsImpl(surfaceView.getWidth(), surfaceView.getHeight());
    }

    public BaseOptions getDocumentOptions() {
        return new BaseOptions();
    }

    public ReaderPluginOptions getPluginOptions() {
        return new ReaderPluginOptionsImpl();
    }

    public void testReaderOpen() {
        reader = ReaderManager.getReader(path);
        BaseReaderRequest open = new OpenRequest(path, getDocumentOptions());
        reader.submitRequest(this, open, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                testConfigView();
            }
        });
    }

    public void testConfigView() {
        final BaseReaderRequest config = new CreateViewRequest(surfaceView.getWidth(), surfaceView.getHeight());
        reader.submitRequest(this, config, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                readerViewInfo = config.getReaderViewInfo();
                switch (testCase) {
                    case Original:
                        testOriginScale();
                        break;
                    case ToPage:
                        testScaleToPage();
                        break;
                    case ToWidth:
                        testScaleToWidth();
                        break;
                    case PageList:
                        testPageNavigationList();
                        break;
                    case ContinuousList:
                        testContinuousList();
                        break;
                    default:
                        testOriginScale();
                        break;
                }
            }
        });
    }

    public void testPageNavigationList() {
        NavigationArgs navigationArgs = new NavigationArgs();
        RectF limit = new RectF(0, 0, 0, 0);
        navigationArgs.rowsLeftToRight(NavigationArgs.Type.ALL, 3, 3, limit);
        final BaseReaderRequest readerRequest = new ChangeLayoutRequest(ReaderConstants.SINGLE_PAGE_NAVIGATION_LIST, navigationArgs);
        reader.submitRequest(this, readerRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                dumpBitmap(readerRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/singlePage.png", false);
                //testReaderGoto();
            }
        });
    }

    public void testContinuousList() {
        NavigationArgs navigationArgs = new NavigationArgs();
        RectF limit = new RectF(0.3f, 0.05f, 0.7f, 0.95f);
        navigationArgs.rowsLeftToRight(NavigationArgs.Type.ALL, 3, 3, limit);
        final BaseReaderRequest readerRequest = new ChangeLayoutRequest(ReaderConstants.CONTINUOUS_PAGE, navigationArgs);
        reader.submitRequest(this, readerRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                dumpBitmap(readerRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/singlePage.png", false);
                testContinuousScale();
            }
        });
    }

    public void testContinuousScale() {
        final BaseReaderRequest readerRequest = new ScaleRequest(String.valueOf(0), 2.0f, 0, 0);
        reader.submitRequest(this, readerRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                readerViewInfo = readerRequest.getReaderViewInfo();
                dumpBitmap(readerRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/scale.png", false);
            }
        });
    }

    public void testReaderGoto() {
        final BaseReaderRequest gotoPosition = new GotoLocationRequest(pn);
        reader.submitRequest(this, gotoPosition, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                readerViewInfo = gotoPosition.getReaderViewInfo();
                dumpBitmap(gotoPosition.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/goto.png", false);
//                testScaleToPage();
            }
        });
    }

    public void testScaleToPage() {
        final ScaleToPageRequest renderRequest = new ScaleToPageRequest(String.valueOf(pn));
        reader.submitRequest(this, renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                dumpBitmap(renderRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/scaleToPage.png", true);
            }
        });
    }

    public void testScaleToWidth() {
        final ScaleToWidthRequest renderRequest = new ScaleToWidthRequest(String.valueOf(pn));
        reader.submitRequest(this, renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                dumpBitmap(renderRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/scaleToWidth.png", true);
            }
        });
    }

    public void testActualScale() {
        final ScaleRequest renderRequest = new ScaleRequest(String.valueOf(pn), 0.5f, 0, 0);
        reader.submitRequest(this, renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                dumpBitmap(renderRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/scale.png", true);
                testScaleByRect();
            }
        });
    }

    public void testScaleByRect() {
        if (readerViewInfo == null || readerViewInfo.getVisiblePages() == null) {
            return;
        }
        String pn = null;
        for(PageInfo pageInfo : readerViewInfo.getVisiblePages()) {
            if (pageInfo.getDisplayRect().contains(selectionRect)) {
                pn = pageInfo.getName();
                selectionRect = ScaleByRectRequest.rectInDocument(pageInfo, selectionRect);
                break;
            }
        }
        if (pn == null) {
            return;
        }

        final ScaleByRectRequest renderRequest = new ScaleByRectRequest(pn, selectionRect);
        reader.submitRequest(this, renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                dumpBitmap(renderRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/scaleByRect.png", true);
//                testOriginScale();
            }
        });
    }

    public void testOriginScale() {
        final BaseReaderRequest scaleRequest = new ScaleRequest(String.valueOf(0), 1.0f, 0, 0);
        reader.submitRequest(this, scaleRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                dumpBitmap(scaleRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/originScale.png", true);
            }
        });
    }

    public void testNextScreen() {
        final NextScreenRequest renderRequest = new NextScreenRequest();
        reader.submitRequest(this, renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e == null) {
                    readerViewInfo = renderRequest.getReaderViewInfo();
                    dumpBitmap(renderRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/next.png", false);
                    textPrerenderScreen();
                }
            }
        });
    }

    public void textPrerenderScreen() {
        if (testPrerender) {
            final PreRenderRequest renderRequest = new PreRenderRequest(true);
            reader.submitRequest(this, renderRequest, new BaseCallback() {
                @Override
                public void done(BaseRequest request, Throwable e) {
                    if (e == null) {
                        //dumpBitmap(renderRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/next.png", false);
                    }
                }
            });
        }
    }

    public void textPreviousScreen() {
        final PreviousScreenRequest renderRequest = new PreviousScreenRequest();
        reader.submitRequest(this, renderRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e == null) {
                    readerViewInfo = renderRequest.getReaderViewInfo();
                    dumpBitmap(renderRequest.getRenderBitmap().getBitmap(), "/mnt/sdcard/Books/next.png", false);
                }
            }
        });
    }

    public void testHitTestWithoutRendering() {
        final AnnotationRequest annotationRequest = new AnnotationRequest(String.valueOf(currentPage), new PointF(200, 200), new PointF(250, 300));
        reader.submitRequest(this, annotationRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest baseReaderRequest, Throwable e) {
                assert(e == null);
                ReaderSelection selection = annotationRequest.getSelection();
                testReaderClose();
            }
        });
    }


    public void testReaderClose() {
        final CloseRequest closeRequest = new CloseRequest();
        reader.submitRequest(this, closeRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                assert(e == null);
                testReaderOpen();
            }
        });
    }





    private Style randStyle() {
        Paint paint = new Paint();
        paint.setTextSize(35);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        int value = TestUtils.randInt(10, 20);
        if (value > 10 && value < 13) {
            paint.setTypeface(Typeface.create(Typeface.SERIF,Typeface.ITALIC));
        } else if (value > 13 && value < 16) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        paint.setStyle(Paint.Style.STROKE);
        return TextStyle.create(paint);
    }

    private void testHyphen() {
        final String source = "representatives";
        List<Pair<String, String>> list = OnyxHyphen.getHyphList(source);
        for(Pair<String, String> pair : list) {
            Log.d(TAG, "after: " + pair.first + "-" + pair.second);
        }
    }

    private void dumpBitmap(final Bitmap bmp, final String path, boolean save) {
        bitmap = bmp;
        Canvas canvas = holder.lockCanvas();
        drawBitmap(canvas);
        drawPagesInfo(canvas);
        holder.unlockCanvasAndPost(canvas);
        if (save) {
            BitmapUtils.saveBitmap(bitmap, path);
        }
    }

    private void drawBitmap(final Bitmap bitmap) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        holder.unlockCanvasAndPost(canvas);
    }

    private void draw() {
        Canvas canvas = holder.lockCanvas();
        float [] size = new float[2];
        wrapper.pageSize(currentPage, size);
        xScale = bitmap.getWidth() / size[0];
        yScale = bitmap.getHeight() / size[1];
        xScale = Math.min(xScale, yScale);
        yScale = xScale;
        drawBitmap(canvas);
        drawHitTest(canvas);
        drawSearchResult(canvas);
        drawSentences(canvas);

        holder.unlockCanvasAndPost(canvas);
    }

    private void applyContrast(Bitmap bitmap) {
        int contrast = 0;
        switch (contrastLevel) {
            case L100:
                contrast = 100;
                break;
            case L150:
                contrast = 150;
                break;
            case L200:
                contrast = 200;
                break;
            default:
                break;
        }
        if (contrast > 0) {
            ImageUtils.applyGammaCorrection(bitmap, contrast);
        }
    }

    private void applyEmbolden(Bitmap bitmap) {
        int bold = 0;
        switch (emboldenLevel) {
            case L1:
                bold = 1;
                break;
            case L2:
                bold = 2;
                break;
            case L3:
                bold = 3;
                break;
            default:
                break;
        }
        if (bold > 0) {
            ImageUtils.applyBitmapEmbolden(bitmap, bold);
        }

    }

    private void drawBitmap(final Canvas canvas) {
        applyContrast(bitmap);
        applyEmbolden(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void drawPagesInfo(final Canvas canvas) {
        if (readerViewInfo == null || readerViewInfo.getVisiblePages() == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setStrokeWidth(3.0f);
        paint.setStyle(Paint.Style.STROKE);
        for(PageInfo pageInfo: readerViewInfo.getVisiblePages()) {
            canvas.drawRect(pageInfo.getDisplayRect(), paint);
            if (selectionRect != null) {
                RectF rect = new RectF(selectionRect);
                rect.offset(pageInfo.getDisplayRect().left, pageInfo.getDisplayRect().top);
                canvas.drawRect(rect, paint);
            }
        }
    }

    private void testPdfiumWrapper() {
        wrapper.nativeInitLibrary();
        if (wrapper.openDocument("/mnt/sdcard/Books/text.pdf", "") != PdfiumJniWrapper.NO_ERROR) {
            return;
        }
        bitmap = Bitmap.createBitmap(surfaceView.getWidth(), surfaceView.getHeight(), Bitmap.Config.ARGB_8888);
        wrapper.drawPage(currentPage, 0, 0, bitmap.getWidth(), bitmap.getHeight(), 0, bitmap);
        draw();
    }

    private void drawHitTest(final Canvas canvas) {
        PdfiumSelection selection = new PdfiumSelection();
        int size = wrapper.hitTest(currentPage, 0, 0, bitmap.getWidth(), bitmap.getHeight(), 0, (int) startX, (int) startY, (int) endX, (int) endY, selection);
        if (size <= 0) {
            return;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        PixelXorXfermode xorMode = new PixelXorXfermode(Color.WHITE);
        paint.setXfermode(xorMode);
        xScale = yScale = 1;
        for(int j = 0; j < selection.getRectangles().size(); ++j) {
            final RectF rectangle = selection.getRectangles().get(j);
            canvas.drawRect(rectangle, paint);
        }
    }

    private void drawSearchResult(final Canvas canvas) {
        List<ReaderSelection> list = new ArrayList<ReaderSelection>();
        wrapper.searchInPage(currentPage, 0, 0, bitmap.getWidth(), bitmap.getHeight(), 0, searchEdit.getText().toString(), false, false, list);
        if (list.size() <= 0) {
            return;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        PixelXorXfermode xorMode = new PixelXorXfermode(Color.WHITE);
        paint.setXfermode(xorMode);
        for(ReaderSelection selection: list) {
            for (int j = 0; j < selection.getRectangles().size(); ++j) {
                final RectF rectangle = selection.getRectangles().get(j);
                canvas.drawRect(rectangle, paint);
            }
        }
    }

    private void drawSentences(final Canvas canvas) {
        String text = wrapper.getPageText(currentPage);
        int start = 0;
        int end = -1;
        List<ReaderSelection> list = new ArrayList<ReaderSelection>();
        while ((end = text.indexOf("\n", start)) > 0) {
            PdfiumSelection selection = new PdfiumSelection();
            wrapper.selection(currentPage, 0, 0, bitmap.getWidth(), bitmap.getHeight(), 0, start, end, selection);
            list.add(selection);
            start = end + 1;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        PixelXorXfermode xorMode = new PixelXorXfermode(Color.WHITE);
        paint.setXfermode(xorMode);
        for(ReaderSelection selection: list) {
            for (int j = 0; j < selection.getRectangles().size(); ++j) {
                final RectF rectangle = selection.getRectangles().get(j);
                canvas.drawLine(rectangle.left, rectangle.bottom, rectangle.right, rectangle.bottom, paint);
            }
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            testNextScreen();
        } else if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
            textPreviousScreen();
        }

        //draw();
        return super.onKeyDown(keyCode, event);
    }
}
