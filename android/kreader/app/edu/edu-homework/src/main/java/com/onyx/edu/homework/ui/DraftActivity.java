package com.onyx.edu.homework.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.onyx.android.sdk.api.device.epd.EpdController;
import com.onyx.android.sdk.api.device.epd.UpdateMode;
import com.onyx.android.sdk.data.model.Question;
import com.onyx.android.sdk.utils.Base64ImageParser;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.edu.homework.DataBundle;
import com.onyx.edu.homework.R;
import com.onyx.edu.homework.base.BaseActivity;
import com.onyx.edu.homework.data.Constant;
import com.onyx.edu.homework.databinding.ActivityAnswerBinding;
import com.onyx.edu.homework.event.CloseScribbleEvent;
import com.onyx.edu.homework.event.SaveNoteEvent;
import com.onyx.edu.homework.utils.TextUtils;

import org.greenrobot.eventbus.Subscribe;

/**
 * Created by lxm on 2017/12/6.
 */

public class DraftActivity extends BaseActivity {

    private ActivityAnswerBinding binding;
    private Question question;
    private ScribbleFragment scribbleFragment;
    private NoteToolFragment toolFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EpdController.invalidate(getWindow().getDecorView(), UpdateMode.GC);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_answer);
        question = (Question) getIntent().getSerializableExtra(Constant.TAG_QUESTION);
        initView(question);
    }

    private void initView(final Question question) {
        int questionIndex = Math.max(question.QuesType - 1, 0);
        String questionType = getResources().getStringArray(R.array.question_type_list)[questionIndex];
        binding.questionType.setText(getString(R.string.question_type_str, questionType));
        binding.content.setText(TextUtils.fromHtml(question.content, new Base64ImageParser(this), null));
        initFragment();
        DataBundle.getInstance().register(this);
    }

    private void initFragment() {
        DataBundle.getInstance().resetNoteViewHelper();
        scribbleFragment = ScribbleFragment.newInstance(question);
        getSupportFragmentManager().beginTransaction().replace(R.id.scribble_layout, scribbleFragment).commit();
        toolFragment = NoteToolFragment.newInstance(binding.subMenuLayout, 1, RelativeLayout.ALIGN_PARENT_BOTTOM);
        getSupportFragmentManager().beginTransaction().replace(R.id.tool_layout, toolFragment).commit();
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataBundle.getInstance().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (!DataBundle.getInstance().isDoing()) {
            finish();
            return;
        }
        DataBundle.getInstance().post(new SaveNoteEvent(true));
    }

    @Subscribe
    public void onCloseScribbleEvent(CloseScribbleEvent event) {
        finish();
    }
}
