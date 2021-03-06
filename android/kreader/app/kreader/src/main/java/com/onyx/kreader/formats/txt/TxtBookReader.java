package com.onyx.kreader.formats.txt;

import com.onyx.kreader.formats.encodings.Decoder;
import com.onyx.kreader.formats.model.*;
import com.onyx.kreader.formats.model.entry.TextParagraphEntry;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Created by zengzhu on 2/28/16.
 * string builder save text that not finished. when end-of-line found, it flush all text collected
 * to the paragraph.
 */
public class TxtBookReader implements BookReader {

    private static String TAG = TxtBookReader.class.getSimpleName();
    private static final int limit = 2 * 1024;

    private PlainFileReader fileReader;
    private ByteBuffer fileData = ByteBuffer.allocate(limit);
    private CharBuffer decodeResult = CharBuffer.allocate(limit);
    private StringBuilder stringBuilder = new StringBuilder();
    private Paragraph paragraph;


    public boolean open(final BookReaderContext bookReaderContext, final BookModel bookModel) {
        createFileReader(bookReaderContext.path);
        if (getFileReader().open()) {
            bookModel.getTextModel().reset();
            return true;
        }
        return false;
    }

    private PlainFileReader createFileReader(final String path) {
        if (fileReader == null) {
            fileReader = new PlainFileReader(path);
        }
        return fileReader;
    }

    public final PlainFileReader getFileReader() {
        return fileReader;
    }

    public boolean processNext(final BookModel bookModel) {
        if (getFileReader().read(fileData) <= 0) {
            return false;
        }

        if (fileData.limit() < limit) {
            bookModel.getTextModel().setLoadFinished(true);
        }
        if (!getFileReader().isEncodingDetected()) {
            detectEncoding(bookModel);
        }

        decodeBuffer(bookModel);
        processNextData(bookModel);
        return true;
    }

    public boolean close(final BookModel bookModel) {
        fileData.clear();
        decodeResult.clear();
        return getFileReader().close();
    }

    private void detectEncoding(final BookModel bookModel) {
        getFileReader().detectEncoding(fileData);
        fileData.rewind();
    }

    private void decodeBuffer(final BookModel bookModel) {
        getFileReader().decodeBuffer(fileData, decodeResult, fileData.limit() < limit);
    }

    private void processNextData(final BookModel bookModel) {
        int start = 0;
        int end = decodeResult.length();
        int ptr = start;
        while (ptr < end) {
            char value = decodeResult.get(ptr);
            if (value == Decoder.NL || value == Decoder.CR) {
                boolean skipNewLine = false;
                if (value == Decoder.CR && ptr + 1 < end && decodeResult.get(ptr + 1) == Decoder.NL) {
                    skipNewLine = true;
                    decodeResult.put(ptr, Decoder.NL);
                }
                if (start != ptr) {
                    onReceivedData(bookModel, decodeResult, start, ptr + 1);
                }
                if (skipNewLine) {
                    ++ptr;
                }
                start = ptr + 1;
                onReceivedEndOfLine(bookModel);
            } else if ((value & 0x80) == 0 && Character.isSpaceChar(value)) {
                if (value != Decoder.TAB) {
                    decodeResult.put(ptr, Decoder.SPACE);
                }
            } else {
            }
            ++ptr;
        }
        if (start != end) {
            onReceivedData(bookModel, decodeResult, start, end);
        }
    }


    /**
     * flush data to current paragraph, called when it received end-of-line.
     * add the paragraph to the model.
     */
    private void onReceivedEndOfLine(final BookModel bookModel) {
        ensureCurrentParagraph(bookModel);
        flushToCurrentParagraph();
        bookModel.getTextModel().addParagraph(currentParagraph());
        resetCurrentParagraph();
    }

    /**
     * Called when it received data.
     * @param buffer
     * @param start
     * @param end
     * @return
     */
    private void onReceivedData(final BookModel bookModel, final CharBuffer buffer, int start, int end) {
        ensureCurrentParagraph(bookModel);
        stringBuilder.append(buffer.subSequence(start, end));
    }

    /**
     * ensure current paragraph exist.
     * @param bookModel
     */
    private void ensureCurrentParagraph(final BookModel bookModel) {
        if (paragraph == null) {
            paragraph = Paragraph.create(Paragraph.ParagraphKind.TEXT_PARAGRAPH);
        }
    }

    private Paragraph currentParagraph() {
        return paragraph;
    }

    private void resetCurrentParagraph() {
        paragraph = null;
    }

    private void flushToCurrentParagraph() {
        final String text = stringBuilder.toString();
        TextParagraphEntry entry = new TextParagraphEntry(text);
        currentParagraph().addEntry(entry);
        stringBuilder.setLength(0);
    }




}
