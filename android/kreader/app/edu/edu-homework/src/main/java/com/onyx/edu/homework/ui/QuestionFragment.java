package com.onyx.edu.homework.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.android.sdk.common.request.BaseRequest;
import com.onyx.android.sdk.data.model.Question;
import com.onyx.android.sdk.data.model.QuestionOption;
import com.onyx.android.sdk.scribble.NoteViewHelper;
import com.onyx.android.sdk.utils.Debug;
import com.onyx.edu.homework.DataBundle;
import com.onyx.edu.homework.R;
import com.onyx.edu.homework.action.note.HomeworkPagesRenderActionChain;
import com.onyx.edu.homework.base.BaseFragment;
import com.onyx.edu.homework.data.Constant;
import com.onyx.edu.homework.databinding.FragmentQuestionBinding;
import com.onyx.edu.homework.event.DoneAnswerEvent;
import com.onyx.edu.homework.utils.TextUtils;
import com.onyx.edu.homework.view.Base64ImageParser;

import java.util.List;

/**
 * Created by lxm on 2017/12/9.
 */

public class QuestionFragment extends BaseFragment {

    private FragmentQuestionBinding binding;
    private Question question;

    public static QuestionFragment newInstance(Question question) {
        QuestionFragment fragment = new QuestionFragment();
        fragment.setQuestion(question);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_question, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(question);
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    private void initView(final Question question) {
        int questionIndex = Math.max(question.QuesType - 1, 0);
        String questionType = getResources().getStringArray(R.array.question_type_list)[questionIndex];
        binding.questionType.setText(getString(R.string.question_type_str, questionType));
        binding.content.setText(TextUtils.fromHtml(question.content, new Base64ImageParser(getActivity()), null));
        binding.answer.setVisibility(question.isChoiceQuestion() ? View.GONE : View.VISIBLE);
        bindQuestionOption(binding.option, question);
        binding.answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAnswerActivity(question);
            }
        });
        binding.scribbleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAnswerActivity(question);
            }
        });
        updateScribbleImage();
    }

    public NoteViewHelper getNoteViewHelper() {
        return DataBundle.getInstance().getNoteViewHelper();
    }

    private void bindQuestionOption(RadioGroup group, Question question) {
        group.removeAllViews();
        if (!question.isChoiceQuestion()) {
            group.setVisibility(View.GONE);
            return;
        }
        group.setVisibility(View.VISIBLE);
        List<QuestionOption> options = question.options;
        for (QuestionOption option : options) {
            CompoundButton button = createCompoundButton(question, option);
            group.addView(button);
            if (option.checked) {
                group.check(button.getId());
            }
        }
    }

    private CompoundButton createCompoundButton(final Question question, final QuestionOption option) {
        final boolean single = question.isSingleChoiceQuestion();
        CompoundButton button = single ? new RadioButton(getActivity()) : new CheckBox(getActivity());
        button.setText(Html.fromHtml(option.value, new Base64ImageParser(getActivity()), null));
        button.setTextSize(getResources().getDimension(R.dimen.question_option_text_size));
        button.setChecked(option.checked);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (single && isChecked) {
                    unCheckOption(question);
                }
                if (isChecked) {
                    question.setDoneAnswer(true);
                    DataBundle.getInstance().post(new DoneAnswerEvent(question));
                }
                option.setChecked(isChecked);
            }
        });
        return button;
    }

    private void unCheckOption(Question question) {
        List<QuestionOption> options = question.options;
        if (options == null) {
            return;
        }
        for (QuestionOption option : options) {
            option.setChecked(false);
        }
    }

    private void gotoAnswerActivity(final Question question) {
        Intent intent = new Intent(getActivity(), AnswerActivity.class);
        intent.putExtra(Constant.TAG_QUESTION, question);
        startActivity(intent);
    }

    public void updateScribbleImage() {
        loadScribbleImage(question, binding.scribbleImage);
    }

    private void loadScribbleImage(final Question question, final ImageView imageView) {
        if (question.isChoiceQuestion()) {
            return;
        }
        int width = (int) getResources().getDimension(R.dimen.scribble_view_width);
        int height = (int) getResources().getDimension(R.dimen.scribble_view_height);
        Rect size = new Rect(0, 0, width, height);
        final HomeworkPagesRenderActionChain pageAction = new HomeworkPagesRenderActionChain(question._id, size);
        pageAction.execute(getNoteViewHelper(), new BaseCallback() {
            @Override
            public void done(BaseRequest request, Throwable e) {
                List<Bitmap> bitmaps = pageAction.getPageMap().get(question._id);
                if (bitmaps != null && bitmaps.size() > 0) {
                    imageView.setImageBitmap(bitmaps.get(0));
                }else {
                    imageView.setImageResource(android.R.color.white);
                }
            }
        });
    }
}