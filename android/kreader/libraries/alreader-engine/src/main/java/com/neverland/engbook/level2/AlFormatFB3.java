package com.neverland.engbook.level2;


import com.neverland.engbook.forpublic.AlBookOptions;
import com.neverland.engbook.forpublic.AlOneContent;
import com.neverland.engbook.forpublic.TAL_CODE_PAGES;
import com.neverland.engbook.level1.AlFiles;
import com.neverland.engbook.level1.AlFilesEPUB;
import com.neverland.engbook.level1.AlFilesFB3;
import com.neverland.engbook.level1.AlOneZIPRecord;
import com.neverland.engbook.util.AlOneImage;
import com.neverland.engbook.util.AlPreferenceOptions;
import com.neverland.engbook.util.AlStyles;
import com.neverland.engbook.util.AlStylesOptions;
import com.neverland.engbook.util.InternalFunc;

import java.util.HashMap;

public class AlFormatFB3 extends AlAXML {

    private static final String LEVELE2_FB3_COVER_TYPE = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail";
    private static final String LEVELE2_FB3_CORE_TYPE = "application/vnd.openxmlformats-package.core-properties+xml";
    private static final String LEVELE2_FB3_DESCRIPTION_TYPE = "application/fb3-description+xml";
    private static final String LEVELE2_FB3_BODY_TYPE = "application/fb3-body+xml";


    protected int active_file = UNKNOWN_FILE_SOURCE_NUM;
    protected int active_type = 0x00;

    private int section_count;
    private int content_start;

    private boolean isGenre;
    private boolean isAuthor;
    private boolean isAuthorFirst;
    private boolean isAuthorLast;
    //private boolean isAuthorNick;
    private boolean isAuthorMiddle;
    private boolean isBookTitle;
    //private boolean isProgramUsed;
    private boolean isTitle0;
    private boolean isGenreList;

    private String firstAuthor = null;
    private String middleAuthor = null;
    private String lastAuthor = null;
    //private String nickAuthor = null;

    @Override
    public void initState(AlBookOptions bookOptions, AlFiles myParent, AlPreferenceOptions pref, AlStylesOptions stl) {
        allState.isOpened = true;

        xml_mode = true;
        ident = "FB2";

        aFiles = myParent;

        if ((bookOptions.formatOptions & AlFiles.LEVEL1_BOOKOPTIONS_NEED_UNPACK_FLAG) != 0)
            aFiles.needUnpackData();

        preference = pref;
        styles = stl;

        size = 0;
        noUseCover = bookOptions.noUseCover;

        autoCodePage = false;
        use_cpR0 = TAL_CODE_PAGES.CP65001;

        allState.state_parser = STATE_XML_SKIP;
        allState.state_skipped_flag = true;

        allState.state_parser = 0;
        parser(0, -1);//aFiles.getSize());
        newParagraph();

        allState.isOpened = false;
    }

    public static boolean isFB3(AlFiles a) {
        if (a.getIdentStr().contentEquals("fb3"))
            return true;
        return false;
    }

    public AlFormatFB3() {
        section_count = 0;

        isGenre = false;
        isAuthor = false;
        isAuthorFirst = false;
        isAuthorLast = false;
        //isAuthorNick = false;
        isAuthorMiddle = false;
        isBookTitle = false;
        //isProgramUsed = false;
        isTitle0 = false;
        isGenreList = false;
        content_start = 0;
        firstAuthor = null;
        middleAuthor = null;
        lastAuthor = null;
        //nickAuthor = null;

        active_file = UNKNOWN_FILE_SOURCE_NUM;
        active_type = AlOneZIPRecord.SPECIAL_UNKNOWN;
    }

    private final HashMap<String, String> allId = new HashMap<>();

    private void addRelationShip() {
        if (active_type == AlOneZIPRecord.SPECIAL_FIRST) {
            StringBuilder sId = tag.getATTRValue(AlFormatTag.TAG_ID);
            StringBuilder sTarget = tag.getATTRValue(AlFormatTag.TAG_TARGET);
            String target;

            if (sId != null && sTarget != null && sId.length() > 0 && sTarget.length() > 0) {

                if (sTarget.toString().indexOf(':') != -1
                    /*sTarget.startsWith("http:") ||
                    sTarget.startsWith("https:") ||
                    sTarget.startsWith("ftp:") ||
                    sTarget.startsWith("file:") ||
                    sTarget.startsWith("mailto:")*/) {
                    target = sTarget.toString();
                } else {
                    target = aFiles.getAbsoluteName("/fb3/", sTarget.toString());
                }

                allId.put(sId.toString(), target);
            }
        } else
        if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {
            StringBuilder sId = tag.getATTRValue(AlFormatTag.TAG_TYPE);
            StringBuilder sTarget = tag.getATTRValue(AlFormatTag.TAG_TARGET);
            if (sId != null && sTarget != null) {
                if (sId.indexOf(LEVELE2_FB3_COVER_TYPE) == 0 && sId.length() > 0 && sTarget.length() > 0) {
                    if (sTarget.indexOf(":") == -1) {
                        coverName = aFiles.getAbsoluteName("/", sTarget.toString());
                    }
                }
                return;
            }

            sId = tag.getATTRValue(AlFormatTag.TAG_CONTENTTYPE);
            sTarget = tag.getATTRValue(AlFormatTag.TAG_PARTNAME);
            if (sId != null && sTarget != null) {
                if (sId.indexOf(LEVELE2_FB3_CORE_TYPE) == 0 && sId.length() > 0 && sTarget.length() > 0) {
                    if (sTarget.indexOf(":") == -1) {
                        String addFiles = AlFiles.getAbsoluteName("/", sTarget.toString());
                        ((AlFilesFB3)aFiles).addFilesToRecord(addFiles, AlOneZIPRecord.SPECIAL_CONTENT);
                        if (dinamicSize)
                            stop_posUsed = aFiles.getSize();
                    }
                }
                return;
            }
        }
    }

    @Override
    void doSpecialGetParagraph(long iType, int addon, long level, long[] stk, int[] cpl) {
        paragraph = iType;
        allState.state_parser = 0;
        active_type = addon & 0xffff;
        active_file = (addon >> 16) & 0x0fff;
        paragraph_level = (int) (level & LEVEL2_MASK_FOR_LEVEL);
        paragraph_tag = (int) ((level >> 31) & 0xffffffff);
        allState.state_skipped_flag = (addon & LEVEL2_FRM_ADDON_SKIPPEDTEXT) != 0;
        allState.state_code_flag = (addon & LEVEL2_FRM_ADDON_CODETEXT) != 0;
        //allState.state_special_flag = (addon & LEVEL2_FRM_ADDON_SPECIALTEXT) != 0;
    }

    @Override
    void formatAddonInt() {
        pariType = paragraph;
        parAddon = active_type & 0xffff;
        parAddon += (active_file & 0x0fff) << 16;

        parLevel = paragraph_level | (((long)paragraph_tag) << 31);

        if (allState.state_skipped_flag)
            parAddon += LEVEL2_FRM_ADDON_SKIPPEDTEXT;
        if (allState.state_code_flag)
            parAddon += LEVEL2_FRM_ADDON_CODETEXT;
		/*if (allState.state_special_flag)
			parAddon += LEVEL2_FRM_ADDON_SPECIALTEXT;*/
    }

    @Override
    boolean isNeedAttribute(int atr) {
        switch (atr) {
            case AlFormatTag.TAG_NUMBER:
            case AlFormatTag.TAG_NUMFILES:
            case AlFormatTag.TAG_CONTENT_TYPE:
            case AlFormatTag.TAG_CONTENTTYPE:
            case AlFormatTag.TAG_TARGET:
            case AlFormatTag.TAG_PARTNAME:
            case AlFormatTag.TAG_TITLE:
            case AlFormatTag.TAG_LINK:
                return true;
        }
        return super.isNeedAttribute(atr);
    }

    private void addTestContent(String s, int level) {
        if (s == null)
            return;
        s = s.trim();
        if (s.length() == 0)
            return;

        if ((paragraph & AlStyles.PAR_NOTE) == 0)
            addContent(AlOneContent.add(s, content_start, level));
    }

    private void addSeries() {
        StringBuilder s = new StringBuilder();
        s.setLength(0);

        StringBuilder s1 = tag.getATTRValue(AlFormatTag.TAG_NAME);
        if (s1 != null)
            s.append(s1);

        s1 = tag.getATTRValue(AlFormatTag.TAG_NUMBER);
        if (s1 != null) {
            s.append(" \u2022 ");
            s.append(s1);
        }

        if (s.length() > 0) {
            if (allState.isOpened)
                bookSeries.add(s.toString());
            boolean saved2 = allState.state_skipped_flag;
            allState.state_skipped_flag = false;
            addTextFromTag(s, true);
            allState.state_skipped_flag = saved2;
        }
    }


    private void setSpecialText(boolean flag) {
        if (flag) {
            allState.state_special_flag0 = true;
            state_specialBuff0.setLength(0);
        } else {

            if (isAuthorFirst) {
                firstAuthor = state_specialBuff0.toString();
                isAuthorFirst = false;
            } else
            if (isAuthorMiddle) {
                middleAuthor = state_specialBuff0.toString();
                isAuthorMiddle = false;
            } else
            if (isAuthorLast) {
                lastAuthor = state_specialBuff0.toString();
                isAuthorLast = false;
            } /*else
            if (isAuthorNick) {
                if (state_specialBuff0.length() > 0) {
                    nickAuthor = '\"' + state_specialBuff0.toString() + '\"';
                }
                isAuthorNick = false;
            }*/ else
            if (isGenre) {
                if (allState.isOpened)
                    bookGenres.add(state_specialBuff0.toString());
                isGenre = false;
            } else
            if (isGenreList) {
                if (allState.isOpened) {
                    String[] spl = state_specialBuff0.toString().split(" ");
                    for (int i = 0; i < spl.length; i++)
                        bookGenres.add(spl[i]);
                }
                isGenreList = false;
            } else
            if (isBookTitle) {
                if (allState.isOpened) {
                    bookTitle = state_specialBuff0.toString().trim();
                    addTestContent(bookTitle, section_count);
                }
                isBookTitle = false;
            } else
            if (isTitle0) {
                addTestContent(state_specialBuff0.toString().trim(), section_count);
                isTitle0 = false;
            } /*else
            if (isTitle1) {
                addTestContent(state_specialBuff0.toString().trim(), section_count + 1);
                isTitle1 = false;
            } else
            if (isProgramUsed) {
                if (program_used_position == -2) {
                    program_used_position = allState.start_position_par;
                    if (state_specialBuff0.indexOf(LEVEL2_PRGUSEDTEST) != -1)
                        program_used_position = -1;
                }
            }*/
            allState.state_special_flag0 = false;
        }
    }

    private void addAuthor() {
        StringBuilder s = new StringBuilder();
        s.setLength(0);

        if (lastAuthor != null)
            s.append(lastAuthor.trim());
        if (firstAuthor != null)
            if (s.length() == 0) s.append(firstAuthor.trim()); else {s.append(' '); s.append(firstAuthor.trim());}
        if (middleAuthor != null)
            if (s.length() == 0) s.append(middleAuthor.trim()); else {s.append(' '); s.append(middleAuthor.trim());}
        /*if (nickAuthor != null)
            if (s.length() == 0) s.append(nickAuthor.trim()); else {s.append(' '); s.append(nickAuthor.trim());}*/

        if (s.length() > 0) {
            bookAuthors.add(s.toString());
        }

        firstAuthor = null;
        middleAuthor = null;
        lastAuthor = null;
        //nickAuthor = null;
    }

    private boolean addNotes() {
        StringBuilder s;

        s = tag.getATTRValue(AlFormatTag.TAG_HREF);

        if (s != null) {
            addCharFromTag((char) AlStyles.CHAR_LINK_S, false);
            addTextFromTag(s, false);
            addCharFromTag((char) AlStyles.CHAR_LINK_E, false);
            return true;
        }

        return false;
    }


    private boolean addImages() {
        if (active_file != UNKNOWN_FILE_SOURCE_NUM) {
            StringBuilder sId = tag.getATTRValue(AlFormatTag.TAG_SRC);

            if (sId == null)
                sId = tag.getATTRValue(AlFormatTag.TAG_ID);

            if (sId != null) {

                String sTarget = allId.get(sId.toString());

                if (sTarget != null) {
                    addCharFromTag((char) AlStyles.CHAR_IMAGE_S, false);
                    addTextFromTag(sTarget, false);
                    addCharFromTag((char) AlStyles.CHAR_IMAGE_E, false);

                    if (allState.isOpened)
                        im.add(AlOneImage.add(sTarget, 0, 0, AlOneImage.IMG_MEMO));
                }
            }
        }
        return false;
    }

    @Override
    protected boolean externPrepareTAG() {
        StringBuilder param;

        if (active_type == AlOneZIPRecord.SPECIAL_NONE && allState.isOpened && tag.tag != AlFormatTag.TAG_BINARY) {
            param = tag.getATTRValue(AlFormatTag.TAG_ID);
            if (param != null)
                addtestLink(param.toString());
        }


        switch (tag.tag) {
            case AlFormatTag.TAG_FB3_DESCRIPTION:

                return true;
            case AlFormatTag.TAG_OVERRIDE:
            case AlFormatTag.TAG_RELATIONSHIP:
                if (tag.closed) {

                } else
                /*if (!tag.ended) {
                    addRelationShip();
                } else*/ {
                    addRelationShip();
                }
                return true;
            case AlFormatTag.TAG_EXTFILE:
                if (tag.closed) {
                    closeOpenNotes();
                    active_file = UNKNOWN_FILE_SOURCE_NUM;
                    active_type = AlOneZIPRecord.SPECIAL_UNKNOWN;
                    clearParagraphStyle(AlStyles.PAR_NOTE);
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_BREAKPAGE);
                } else
                if (!tag.ended) {
                    param = tag.getATTRValue(AlFormatTag.TAG_IDREF);
                    if (param != null) {
                        switch (InternalFunc.str2int(param, 10)) {
                            case AlOneZIPRecord.SPECIAL_FIRST:
                                active_type = AlOneZIPRecord.SPECIAL_FIRST;

                                if (coverName.length() > 0 && !noUseCover) {
                                    boolean bs = allState.state_skipped_flag;
                                    allState.state_skipped_flag = false;
                                    if (allState.isOpened) {
                                        newParagraph();
                                        setParagraphStyle(AlStyles.PAR_COVER);
                                    }
                                    addCharFromTag((char) AlStyles.CHAR_IMAGE_S, false);
                                    addTextFromTag(LEVEL2_COVERTOTEXT_STR, false);
                                    addCharFromTag((char) AlStyles.CHAR_IMAGE_E, false);

                                    allState.state_skipped_flag = bs;
                                    if (allState.isOpened) {
                                        newParagraph();
                                        clearParagraphStyle(AlStyles.PAR_COVER);
                                    }
                                }

                                break;
                            case AlOneZIPRecord.SPECIAL_CONTENT:
                                active_type = AlOneZIPRecord.SPECIAL_CONTENT;
                                allState.state_skipped_flag = true;
                                break;
                            default:
                                active_type = AlOneZIPRecord.SPECIAL_NONE;
                                allState.state_skipped_flag = false;
                                break;
                        }

                        param = tag.getATTRValue(AlFormatTag.TAG_NUMFILES);
                        if (param != null) {
                            active_file = InternalFunc.str2int(param, 10);
                        } else {
                            active_file = UNKNOWN_FILE_SOURCE_NUM;
                            active_type = AlOneZIPRecord.SPECIAL_NONE;
                        }
                    } else {
                        active_file = UNKNOWN_FILE_SOURCE_NUM;
                        active_type = AlOneZIPRecord.SPECIAL_NONE;
                    }
                } else {

                }
                return true;
            case AlFormatTag.TAG_CUSTOM_INFO:
            case AlFormatTag.TAG_FB3_CLASSIFICATION:
            case AlFormatTag.TAG_CLASSIFICATION:
            case AlFormatTag.TAG_PERIODICAL:
            case AlFormatTag.TAG_WRITTEN:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {
                    if (tag.closed) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION4)
                            clearParagraphStyle(AlStyles.PAR_DESCRIPTION4);
                    } else
                    if (!tag.ended) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == 0x00)
                            setParagraphStyle(AlStyles.PAR_DESCRIPTION4);
                    } else {

                    }
                }
                return true;
            case AlFormatTag.TAG_SEQUENCE:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {
                    if (tag.closed) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION3)
                            clearParagraphStyle(AlStyles.PAR_DESCRIPTION3);
                    } else
                    if (!tag.ended) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == 0x00)
                            setParagraphStyle(AlStyles.PAR_DESCRIPTION3);
                    } else {

                    }
                }
                return true;
            case AlFormatTag.TAG_FB3_RELATIONS:
            case AlFormatTag.TAG_RELATIONS:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {
                    if (tag.closed) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTION1) == AlStyles.PAR_DESCRIPTION1)
                            clearParagraphStyle(AlStyles.PAR_DESCRIPTION1);
                    } else
                    if (!tag.ended) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTION1) == 0)
                            setParagraphStyle(AlStyles.PAR_DESCRIPTION1);
                    } else {

                    }
                }
                return true;
            case AlFormatTag.TAG_LANG:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {
                    if (tag.closed) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION2)
                            clearParagraphStyle(AlStyles.PAR_DESCRIPTION3);
                    } else
                    if (!tag.ended) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == 0x00)
                            setParagraphStyle(AlStyles.PAR_DESCRIPTION2);
                    } else {

                    }
                }
                return true;
            case AlFormatTag.TAG_CATEGORY:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {
                    if (tag.closed) {
                        setSpecialText(false);
                    } else
                    if (!tag.ended) {
                        isGenreList = true;
                        setSpecialText(true);
                    } else {

                    }
                }
                return true;
            case AlFormatTag.TAG_SUBJECT:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {
                    if (tag.closed) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == (AlStyles.PAR_DESCRIPTION1 | AlStyles.PAR_DESCRIPTION2)) {
                            addAuthor();
                            clearParagraphStyle(AlStyles.PAR_DESCRIPTION2);
                        } else
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION4) {
                            setSpecialText(false);
                        }
                    } else
                    if (!tag.ended) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION1) {
                            StringBuilder s1 = tag.getATTRValue(AlFormatTag.TAG_LINK);
                            if (s1 != null) {
                                String s = s1.toString().toLowerCase();
                                if (s.contentEquals("author"))
                                    setParagraphStyle(AlStyles.PAR_DESCRIPTION1 | AlStyles.PAR_DESCRIPTION2);
                            }
                        } else
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION4) {
                            isGenre = true;
                            setSpecialText(true);
                        }
                    } else {

                    }
                }
                return true;
            case AlFormatTag.TAG_FIRST_NAME:
            case AlFormatTag.TAG_MIDDLE_NAME:
            case AlFormatTag.TAG_LAST_NAME:
                if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == (AlStyles.PAR_DESCRIPTION1 | AlStyles.PAR_DESCRIPTION2)) {

                    if (tag.closed) {
                        setSpecialText(false);
                    } else
                    if (!tag.ended) {
                        if (tag.tag == AlFormatTag.TAG_FIRST_NAME)
                            isAuthorFirst = true;
                        if (tag.tag == AlFormatTag.TAG_MIDDLE_NAME)
                            isAuthorMiddle = true;
                        if (tag.tag == AlFormatTag.TAG_LAST_NAME)
                            isAuthorLast = true;
                        setSpecialText(true);
                    }
                }
                return true;
            case AlFormatTag.TAG_MAIN:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {

                    if (tag.closed) {
                        setSpecialText(false);
                    } else
                    if (!tag.ended) {
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == 0x00) {
                            // title
                            isBookTitle = true;
                        }
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION2) {
                            // series
                        }
                        if ((paragraph & AlStyles.PAR_DESCRIPTIONMASK) == AlStyles.PAR_DESCRIPTION3) {
                            // lang
                        }

                        setSpecialText(true);
                    }
                }
                return true;
            case AlFormatTag.TAG_TITLE:
                if (active_type == AlOneZIPRecord.SPECIAL_CONTENT) {

                } else {
                    if (tag.closed) {
                        clearParagraphStyle(AlStyles.PAR_TITLE);// | AlStyles::PAR_BREAKPAGE);
                        newParagraph();
                        newEmptyStyleParagraph();
                        if (allState.isOpened)
                            setSpecialText(false);
                    } else
                    if (!tag.ended) {
                        newParagraph();
                        newEmptyStyleParagraph();
                        setParagraphStyle(AlStyles.PAR_TITLE);
                        if (allState.isOpened) {
                            isTitle0 = true;
                            content_start = size;
                            setSpecialText(true);
                        }
                    } else {

                    }
                }
                return true;
            case AlFormatTag.TAG_ANNOTATION:
                if (tag.closed) {
                    if (active_type == AlOneZIPRecord.SPECIAL_CONTENT)
                        allState.state_skipped_flag = true;
                    clearParagraphStyle(AlStyles.PAR_ANNOTATION);
                    newParagraph();
                    newEmptyStyleParagraph();
                } else
                if (!tag.ended) {
                    if (active_type == AlOneZIPRecord.SPECIAL_CONTENT)
                        allState.state_skipped_flag = false;
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_ANNOTATION);
                } else {

                }
                return true;
            case AlFormatTag.TAG_PREAMBLE:
                if (tag.closed) {
                    if (active_type == AlOneZIPRecord.SPECIAL_CONTENT)
                        allState.state_skipped_flag = true;
                    clearParagraphStyle(AlStyles.PAR_ANNOTATION);
                    newParagraph();
                    newEmptyStyleParagraph();
                } else
                if (!tag.ended) {
                    if (active_type == AlOneZIPRecord.SPECIAL_CONTENT)
                        allState.state_skipped_flag = false;
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_BREAKPAGE);
                    setParagraphStyle(AlStyles.PAR_ANNOTATION);
                } else {

                }
                return true;

            /////////////////////////////////////////////////////////////////

// paragraph
            case AlFormatTag.TAG_DIV:
                if (tag.closed) {

                } else
                if (!tag.ended) {

                } else {

                }
                return true;
            case AlFormatTag.TAG_P:
                newParagraph();
                return true;
            case AlFormatTag.TAG_LI:
                if (tag.closed) {
                    newParagraph();
                } else
                if (!tag.ended) {
                    newParagraph();
                } else {

                }
                return true;
            case AlFormatTag.TAG_BR:
            case AlFormatTag.TAG_EMPTY_LINE:
                newParagraph();
                newEmptyTextParagraph();
                return true;
            case AlFormatTag.TAG_PRE:
                if (tag.closed) {
                    clearParagraphStyle(AlStyles.PAR_PRE);
                    newParagraph();
                    allState.state_code_flag = false;
                } else
                if (!tag.ended) {
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_PRE);
                    allState.state_code_flag = true;
                } else {

                }
                return true;

            case AlFormatTag.TAG_EPIGRAPH:
                if (tag.closed) {
                    clearParagraphStyle(AlStyles.PAR_EPIGRAPH);
                    newParagraph();
                    newEmptyStyleParagraph();
                } else
                if (!tag.ended) {
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_EPIGRAPH);
                } else {

                }
                return true;
            case AlFormatTag.TAG_POEM:
                if (tag.closed) {
                    clearParagraphStyle(AlStyles.PAR_POEM);
                    newParagraph();
                    newEmptyStyleParagraph();
                } else
                if (!tag.ended) {
                    newParagraph();
                    newEmptyStyleParagraph();
                    setParagraphStyle(AlStyles.PAR_POEM);
                } else {

                }
                return true;
            case AlFormatTag.TAG_V:
                if (tag.closed) {
                    clearParagraphStyle(AlStyles.PAR_V);
                } else
                if (!tag.ended) {
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_V);
                } else {
                    newParagraph();
                }
                return true;
            case AlFormatTag.TAG_STANZA:
                if (tag.closed) {
                    clearParagraphStyle(AlStyles.PAR_STANZA);
                    newParagraph();
                } else
                if (!tag.ended) {
                    newParagraph();
                    newEmptyStyleParagraph();
                    setParagraphStyle(AlStyles.PAR_STANZA);
                } else {

                }
                return true;

            case AlFormatTag.TAG_DATE:
                if (tag.closed) {

                } else
                if (!tag.ended) {

                } else {

                }
                return true;
            case AlFormatTag.TAG_SUBSCRIPTION:
                if (tag.closed) {
                    clearParagraphStyle(AlStyles.PAR_AUTHOR);
                    newParagraph();
                    newEmptyStyleParagraph();
                } else
                if (!tag.ended) {
                    newParagraph();
                    if ((paragraph & (AlStyles.PAR_STANZA | AlStyles.PAR_POEM)) != 0)
                        newEmptyStyleParagraph();
                    setParagraphStyle(AlStyles.PAR_AUTHOR);
                } else {

                }
                return true;
            case AlFormatTag.TAG_CODE:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_CODE);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_CODE);
                } else {

                }
                return true;
            case AlFormatTag.TAG_BLOCKQUOTE:
                if (tag.closed) {
                    clearParagraphStyle(AlStyles.PAR_CITE);
                    newParagraph();
                    newEmptyStyleParagraph();
                } else
                if (!tag.ended) {
                    newParagraph();
                    newEmptyStyleParagraph();
                    setParagraphStyle(AlStyles.PAR_CITE);
                } else {

                }
                return true;
// addon
            case AlFormatTag.TAG_IMG:
                if (tag.closed) {

                } else
                /*if (!tag.ended) {
                    addImages();
                } else*/ {
                    addImages();
                }
                return true;

	/**/
            case AlFormatTag.TAG_UL:
            case AlFormatTag.TAG_OL:
                if (tag.closed) {
                    decULNumber();
                } else
                if (!tag.ended) {
                    incULNumber();
                } else {

                }
                return true;
// manage
            case AlFormatTag.TAG_SECTION:
                if (tag.closed) {
                    clearULNumber();
                    section_count--;
                    newParagraph();
                    if ((paragraph & AlStyles.PAR_NOTE) == 0)
                        setParagraphStyle(AlStyles.PAR_BREAKPAGE);
                    closeOpenNotes();
                } else
                if (!tag.ended) {
                    section_count++;
                    newParagraph();
                    isFirstParagraph = true;
                } else {

                }
                return true;
            case AlFormatTag.TAG_NOTES:
                if (tag.closed) {
                    closeOpenNotes();
                    section_count = 0;
                    clearParagraphStyle(AlStyles.PAR_NOTE);
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_BREAKPAGE);
                } else
                if (!tag.ended) {
                    content_start = size;
                    addTestContent("Notes", section_count);
                    setParagraphStyle(AlStyles.PAR_NOTE);
                    newParagraph();
                } else {

                }
                return true;

            case AlFormatTag.TAG_BODY:
            case AlFormatTag.TAG_FB3_BODY:
                if (tag.closed) {
                    closeOpenNotes();
                    section_count = 0;
                    clearParagraphStyle(AlStyles.PAR_NOTE);
                    newParagraph();
                    setParagraphStyle(AlStyles.PAR_BREAKPAGE);
                } else
                if (!tag.ended) {
                    section_count = 0;
                    allState.state_skipped_flag = false;
                    newParagraph();
                } else {

                }
                return true;

            /*case AlFormatTag.TAG_LINK:
                if (tag.closed) {

                } else
                if (!tag.ended) {

                } else {

                }
                return true;*/

///////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////
// styles
            case AlFormatTag.TAG_B:
            case AlFormatTag.TAG_STRONG:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_BOLD);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_BOLD);
                }
                else {

                }
                return true;
            case AlFormatTag.TAG_I:
            case AlFormatTag.TAG_EM:
            case AlFormatTag.TAG_EMPHASIS:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_ITALIC);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_ITALIC);
                } else {

                }
                return true;
            case AlFormatTag.TAG_STRIKE:
            case AlFormatTag.TAG_DEL:
            case AlFormatTag.TAG_STRIKETHROUGH:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_STRIKE);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_STRIKE);
                } else {

                }
                return true;
            case AlFormatTag.TAG_S:
            case AlFormatTag.TAG_INS:
            case AlFormatTag.TAG_U:
            case AlFormatTag.TAG_UNDERLINE:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_UNDER);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_UNDER);
                } else {

                }
                return true;
            case AlFormatTag.TAG_SPACING:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_RAZR);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_RAZR);
                } else {

                }
                return true;
            case AlFormatTag.TAG_NOTE:
            case AlFormatTag.TAG_A:
                if (tag.closed) {
                    if ((paragraph & AlStyles.STYLE_LINK) != 0)
                        clearTextStyle(AlStyles.STYLE_LINK);
                } else
                if (!tag.ended) {
                    if (addNotes())
                        setTextStyle(AlStyles.STYLE_LINK);
                } else {

                }
                return true;
            case AlFormatTag.TAG_SUP:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_SUP);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_SUP);
                } else {

                }
                return true;
            case AlFormatTag.TAG_SUB:
                if (tag.closed) {
                    clearTextStyle(AlStyles.STYLE_SUB);
                } else
                if (!tag.ended) {
                    setTextStyle(AlStyles.STYLE_SUB);
                } else {

                }
                return true;

            case AlFormatTag.TAG_TABLE:
            case AlFormatTag.TAG_TH:
            case AlFormatTag.TAG_TD:
            case AlFormatTag.TAG_TR:
                return prepareTable();
        }

        return false;

    }
}
