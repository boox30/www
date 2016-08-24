package com.onyx.kreader.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kreader.R;
import com.onyx.kreader.api.ReaderSentence;
import com.onyx.kreader.common.Debug;
import com.onyx.kreader.device.ReaderDeviceManager;
import com.onyx.kreader.host.request.GetSentenceRequest;
import com.onyx.kreader.host.request.RenderRequest;
import com.onyx.kreader.host.request.ScaleToPageRequest;
import com.onyx.kreader.tts.ReaderTtsManager;
import com.onyx.kreader.ui.actions.GotoPageAction;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.handler.HandlerManager;
import com.onyx.kreader.utils.PagePositionUtils;

/**
 * Created by ming on 16/8/12.
 */
public class DialogTts extends Dialog implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = DialogTts.class.getSimpleName();

    public static final int VOLUME_SPAN = 2;

    @Bind(R.id.tts_play)
    ImageButton ttsPlay;
    @Bind(R.id.tts_stop)
    ImageButton ttsStop;
    @Bind(R.id.tts_voice)
    ImageButton ttsVoice;
    @Bind(R.id.tts_speed)
    ImageButton ttsSpeed;
    @Bind(R.id.tts_close)
    ImageButton ttsClose;
    @Bind(R.id.tts_button_layout)
    LinearLayout ttsButtonLayout;
    @Bind(R.id.seek_bar_tts)
    SeekBar seekBarTts;
    @Bind(R.id.voice_size_layout)
    LinearLayout voiceSizeLayout;
    @Bind(R.id.fast_speed)
    CheckBox fastSpeed;
    @Bind(R.id.more_fast_speed)
    CheckBox moreFastSpeed;
    @Bind(R.id.normal_speed)
    CheckBox normalSpeed;
    @Bind(R.id.more_slow_speed)
    CheckBox moreSlowSpeed;
    @Bind(R.id.slow_speed)
    CheckBox slowSpeed;
    @Bind(R.id.voice_speed_layout)
    RadioGroup voiceSpeedLayout;
    @Bind(R.id.minus_voice)
    ImageButton minusVoice;
    @Bind(R.id.plus_voice)
    ImageButton plusVoice;

    private int maxVolume;
    private AudioManager audioMgr;
    private ReaderDataHolder readerDataHolder;
    private ReaderSentence currentSentence;

    private int[] speedCheckBoxIds = {R.id.fast_speed,R.id.more_fast_speed,R.id.normal_speed,R.id.more_slow_speed,R.id.slow_speed};

    private int gcInterval = 0;

    public DialogTts(ReaderDataHolder readerDataHolder) {
        super(readerDataHolder.getContext(), R.style.dialog_transparent_no_title);
        this.readerDataHolder = readerDataHolder;
        setContentView(R.layout.dialog_tts);
        ButterKnife.bind(this);
        initView();
        initData();

        // force to be false, or else it will be turned on for unknown reason
        setCanceledOnTouchOutside(false);
    }

    public void show() {
        readerDataHolder.submitRenderRequest(new ScaleToPageRequest(readerDataHolder.getCurrentPageName()), new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                ttsPlay(readerDataHolder);
            }
        });
        super.show();
    }

    private void initView() {
        voiceSizeLayout.setVisibility(View.INVISIBLE);
        voiceSpeedLayout.setVisibility(View.INVISIBLE);

        ttsVoice.setOnClickListener(this);
        ttsSpeed.setOnClickListener(this);
        ttsClose.setOnClickListener(this);
        ttsPlay.setOnClickListener(this);
        ttsStop.setOnClickListener(this);

        minusVoice.setOnClickListener(this);
        plusVoice.setOnClickListener(this);

        fastSpeed.setOnCheckedChangeListener(this);
        moreFastSpeed.setOnCheckedChangeListener(this);
        normalSpeed.setOnCheckedChangeListener(this);
        slowSpeed.setOnCheckedChangeListener(this);
        moreSlowSpeed.setOnCheckedChangeListener(this);

        voiceSizeLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = voiceSizeLayout.getMeasuredWidth();
                int height = voiceSizeLayout.getMeasuredHeight();
                voiceSizeLayout.setY(voiceSizeLayout.getY() - width / 2 + height / 2);
            }
        });

        voiceSpeedLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = voiceSpeedLayout.getMeasuredWidth();
                int btnWidth = ttsSpeed.getMeasuredWidth();
                float speedButtonX = ttsSpeed.getX();
                voiceSpeedLayout.setX(speedButtonX - width / 2 + btnWidth / 2);
            }
        });

        seekBarTts.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                controlVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ReaderDeviceManager.setGcInterval(gcInterval);
                ttsStop(readerDataHolder);
            }
        });

    }

    private void initData(){
        audioMgr = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekBarTts.setMax(maxVolume);
        seekBarTts.setProgress(curVolume);

        readerDataHolder.initTtsManager(new ReaderTtsManager.Callback() {

            @Override
            public void requestSentence() {
                requestSentenceForTts();
            }

            @Override
            public void onStateChanged() {
                if (readerDataHolder.getTtsManager().isSpeaking()) {
                    ttsPlay.setImageResource(R.drawable.ic_dialog_tts_suspend);
                } else {
                    ttsPlay.setImageResource(R.drawable.ic_dialog_tts_play);
                }
            }

            @Override
            public void onError() {
                ttsPlay.setImageResource(R.drawable.ic_dialog_tts_play);
                readerDataHolder.submitRenderRequest(new RenderRequest());
            }
        });

        gcInterval = ReaderDeviceManager.getGcInterval();
        ReaderDeviceManager.setGcInterval(Integer.MAX_VALUE);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(ttsVoice)) {
            voiceSizeLayout.setVisibility(voiceSizeLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            voiceSpeedLayout.setVisibility(View.GONE);
        } else if (v.equals(ttsSpeed)) {
            voiceSizeLayout.setVisibility(View.GONE);
            voiceSpeedLayout.setVisibility(voiceSpeedLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        } else if (v.equals(ttsClose)) {
            ttsStop(readerDataHolder);
            dismiss();
        }else if (v.equals(ttsPlay)){
            if (readerDataHolder.getTtsManager().isSpeaking()) {
                ttsPause(readerDataHolder);
            } else {
                ttsPlay(readerDataHolder);
            }
        }else if (v.equals(ttsStop)){
            ttsStop(readerDataHolder);
        }else if (v.equals(minusVoice)){
            setSeekBarValue(false);
        }else if (v.equals(plusVoice)){
            setSeekBarValue(true);
        }
    }

    private void setSeekBarValue(boolean plus){
        int volumeProgress = seekBarTts.getProgress();
        if (plus){
            volumeProgress = volumeProgress + VOLUME_SPAN;
            volumeProgress = Math.min(maxVolume,volumeProgress);
        }else {
            volumeProgress = volumeProgress - VOLUME_SPAN;
            volumeProgress = Math.max(0,volumeProgress);
        }
        seekBarTts.setProgress(volumeProgress);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(!buttonView.isPressed()){
            return;
        }
        boolean checked = false;
        int length = speedCheckBoxIds.length;
        for (int i = 0; i < length; i++) {
            if (buttonView.getId() == speedCheckBoxIds[i]){
                controlSpeedOfSound(length - i);
                checked = true;
            }
            ((CompoundButton)findViewById(speedCheckBoxIds[i])).setChecked(checked);
        }
    }

    private void ttsPlay(final ReaderDataHolder readerDataHolder) {
        readerDataHolder.getTtsManager().play();
    }

    private void ttsPause(final ReaderDataHolder readerDataHolder) {
        readerDataHolder.getTtsManager().pause();
    }

    private void ttsStop(final ReaderDataHolder readerDataHolder) {
        currentSentence = null;
        readerDataHolder.getTtsManager().stop();
        readerDataHolder.getHandlerManager().setActiveProvider(HandlerManager.READING_PROVIDER);
        readerDataHolder.submitRenderRequest(new RenderRequest());
    }

    //调节音量
    private void controlVolume(int volume){
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume,
                AudioManager.FLAG_PLAY_SOUND);
    }

    //调节声音速度(1-5档)
    private void controlSpeedOfSound(int speed){

    }

    private boolean requestSentenceForTts() {
        if (currentSentence != null) {
            if (currentSentence.isEndOfDocument()) {
                Debug.d(TAG, "end of document");
                readerDataHolder.getTtsManager().stop();
                return false;
            }
            if (currentSentence.isEndOfScreen()) {
                Debug.d(TAG, "end of page");
                currentSentence = null;
                String next = PagePositionUtils.fromPageNumber(readerDataHolder.getCurrentPage() + 1);
                new GotoPageAction(next).execute(readerDataHolder, new BaseCallback() {
                    @Override
                    public void done(BaseRequest request, Throwable e) {
                        if (e != null) {
                            Log.w(TAG, e);
                            return;
                        }
                        requestSentenceForTts();
                    }
                });
                return true;
            }
        }

        String startPosition = currentSentence == null ? "" : currentSentence.getNextPosition();
        final GetSentenceRequest sentenceRequest = new GetSentenceRequest(readerDataHolder.getCurrentPage(), startPosition);
        readerDataHolder.submitRenderRequest(sentenceRequest, new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                if (e != null) {
                    Log.w(TAG, e);
                    return;
                }
                currentSentence = sentenceRequest.getSentenceResult();
                if (currentSentence == null) {
                    Log.w(TAG, "get sentence failed");
                    return;
                }
                dumpCurrentSentence();
                if (StringUtils.isNullOrEmpty(currentSentence.getReaderSelection().getText())) {
                    requestSentenceForTts();
                    return;
                }
                readerDataHolder.getTtsManager().supplyText(currentSentence.getReaderSelection().getText());
                readerDataHolder.getTtsManager().play();
            }
        });
        return true;
    }

    private void dumpCurrentSentence() {
        Debug.d("current sentence: " +
                StringUtils.deleteNewlineSymbol(currentSentence.getReaderSelection().getText()) +
                ", " + currentSentence.isEndOfScreen() +
                ", " + currentSentence.isEndOfDocument());
    }
}
