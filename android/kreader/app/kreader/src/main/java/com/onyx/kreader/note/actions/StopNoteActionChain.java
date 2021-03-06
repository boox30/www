package com.onyx.kreader.note.actions;

import com.onyx.android.sdk.common.request.BaseCallback;
import com.onyx.kreader.host.request.SaveDocumentOptionsRequest;
import com.onyx.kreader.ui.actions.ActionChain;
import com.onyx.kreader.ui.data.ReaderDataHolder;
import com.onyx.kreader.ui.handler.HandlerManager;

/**
 * Created by zhuzeng on 9/22/16.
 */
public class StopNoteActionChain  {

    private boolean render;
    private boolean transfer;
    private boolean save;
    private boolean show;
    private boolean quit;
    private boolean closeMenu;

    public StopNoteActionChain(boolean render, boolean transfer, boolean saveToDatabase, boolean showDialog, boolean stop, boolean closeMenu) {
        this.render = render;
        this.transfer = transfer;
        save = saveToDatabase;
        show = showDialog;
        this.quit = stop;
        this.closeMenu = closeMenu;
    }

    public void execute(final ReaderDataHolder readerDataHolder, final BaseCallback callback) {
        final ActionChain actionChain = new ActionChain();
        if (quit) {
            readerDataHolder.getNoteManager().stopRawEventProcessor();
            actionChain.addAction(new StopNoteAction());
        }
        actionChain.addAction(new FlushNoteAction(readerDataHolder.getVisiblePages(), render, transfer, save, show));
        if (closeMenu) {
            actionChain.addAction(new CloseNoteMenuAction());
        }
        actionChain.execute(readerDataHolder, callback);
    }

}
