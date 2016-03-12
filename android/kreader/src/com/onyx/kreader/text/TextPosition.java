package com.onyx.kreader.text;

import com.onyx.kreader.formats.model.BookModel;
import com.onyx.kreader.formats.model.BookReader;
import com.onyx.kreader.formats.model.Paragraph;
import com.onyx.kreader.formats.model.entry.ParagraphEntry;
import com.onyx.kreader.formats.model.entry.TextParagraphEntry;

import java.util.List;

/**
 * Created by zengzhu on 3/11/16.
 */
public class TextPosition {

    private int currentParagraph;
    private int paragraphCount;

    private int currentEntry = -1;
    private int entryCount;

    private int currentRunOffset;
    private int length;

    private final BookModel bookModel;
    private final BookReader reader;

    public TextPosition(final BookReader r, final BookModel parent) {
        bookModel = parent;
        reader = r;
    }

    public final String getCurrentText() {
        final Paragraph paragraph = bookModel.getTextModel().getParagraph(currentParagraph);
        if (paragraph == null) {
            return null;
        }

        entryCount = paragraph.getEntryCount();
        final ParagraphEntry entry = paragraph.getEntry(currentEntry);
        if (entry == null) {
            return null;
        }

        if (entry instanceof TextParagraphEntry) {
            TextParagraphEntry textParagraphEntry = (TextParagraphEntry) entry;
            return textParagraphEntry.getText();
        }
        return null;
    }

    public boolean hasNext() {
        return !(isLastParagraph() && isLastEntry());
    }

    public final String next() {
        if (isLastEntry()) {
            return nextParagraph();
        }
        return nextEntry();
    }

    private final String nextParagraph() {
        if (!hasNext()) {
            return null;
        }
        ++currentParagraph;
        currentEntry = 0;
        final Paragraph paragraph = bookModel.getTextModel().getParagraph(currentParagraph);
        entryCount = paragraph.getEntryCount();
        return getCurrentText();
    }

    private final String nextEntry() {
        if (isLastEntry()) {
            return nextParagraph();
        }
        ++currentEntry;
        return getCurrentText();
    }

    public boolean isFirstParagraph() {
        return (currentParagraph == 0);
    }

    public boolean isLastParagraph() {
        return (paragraphCount > 0 && currentParagraph >= paragraphCount - 1);
    }

    public boolean isFirstEntry() {
        return (currentEntry == 0);
    }

    public boolean isLastEntry() {
        return (currentEntry >= entryCount - 1 && entryCount > 0);
    }

    public boolean isLastRun() {
        return (currentRunOffset >= length && length > 0);
    }

    /**
     * get available text length in model that has been read.
     * @return
     */
    public int available() {
        int available = 0;
        final List<Paragraph> paragraphList = bookModel.getTextModel().getParagraphList();
        if (paragraphList.size() <= 0) {
            return available;
        }
        Paragraph paragraph = paragraphList.get(currentParagraph);
        available += paragraph.available(currentEntry);

        for(int i = currentParagraph + 1; i < paragraphList.size(); ++i) {
            paragraph = paragraphList.get(i);
            available += paragraph.available(0);
        }
        return available;
    }

    public boolean fetchMore() {
        if (!reader.processNext(bookModel)) {
            return false;
        }
        update();
        return true;
    }

    private void update() {
        final List<Paragraph> paragraphList = bookModel.getTextModel().getParagraphList();
        paragraphCount = paragraphList.size();
        if (paragraphCount <= 0) {
            return;
        }
        entryCount = paragraphList.get(currentParagraph).getEntryList().size();
    }

}
