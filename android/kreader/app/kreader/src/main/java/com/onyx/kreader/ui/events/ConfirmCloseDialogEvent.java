package com.onyx.kreader.ui.events;

/**
 * Created by zhuzeng on 25/03/2017.
 */

public class ConfirmCloseDialogEvent {

    private boolean open;
    public ConfirmCloseDialogEvent(boolean o) {
        open = o;
    }

    public boolean isOpen() {
        return open;
    }

}
