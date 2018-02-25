package com.onyx.android.note.note.scribble;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.onyx.android.note.NoteDataBundle;
import com.onyx.android.note.R;
import com.onyx.android.note.common.base.BaseFragment;
import com.onyx.android.note.databinding.FragmentScribbleBinding;

/**
 * Created by lxm on 2018/2/2.
 */

public class ScribbleFragment extends BaseFragment {

    private FragmentScribbleBinding binding;

    public static ScribbleFragment newInstance() {
        return new ScribbleFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scribble, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private NoteDataBundle getNoteBundle() {
        return NoteDataBundle.getInstance();
    }
}
