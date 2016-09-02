package com.onyx.kreader.host.impl;

import com.onyx.kreader.api.ReaderHitTestOptions;

/**
 * Created by joy on 9/2/16.
 */
public class ReaderHitTestOptionsImpl implements ReaderHitTestOptions {

    private boolean selectingWord;

    private ReaderHitTestOptionsImpl() {

    }

    public static ReaderHitTestOptionsImpl create(boolean selectingWord) {
        ReaderHitTestOptionsImpl options = new ReaderHitTestOptionsImpl();
        options.selectingWord = selectingWord;
        return options;
    }

    @Override
    public boolean isSelectingWord() {
        return selectingWord;
    }
}
