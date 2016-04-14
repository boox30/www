package com.neverland.engbook.level2;

import com.neverland.engbook.forpublic.AlBookOptions;
import com.neverland.engbook.forpublic.TAL_CODE_PAGES;
import com.neverland.engbook.level1.AlFiles;
import com.neverland.engbook.util.AlOneImage;
import com.neverland.engbook.util.AlPreferenceOptions;
import com.neverland.engbook.util.AlStyles;
import com.neverland.engbook.util.AlStylesOptions;

import com.neverland.engbook.forpublic.TAL_CODE_PAGES;

public class AlFormatNativeImages extends AlFormat {
    public static boolean isImage(AlFiles a, String ext) {
        byte[]	data = new byte[32];
        if (a.getByteBuffer(0, data, 32) == 32) {

            if ((ext == null || ".jpg".contentEquals(ext) || ".jpeg".contentEquals(ext)) &&
                    (data[0] & 0xff) == 0xff &&
                    (data[1] & 0xff) >= 0xc0 &&
                    (data[2] & 0xff) == 0xff &&
                    (data[3] & 0xff) >= 0x00)
                return true;

            if ((ext == null || ".png".contentEquals(ext)) &&
                    (data[0] & 0xff) == 137 &&
                    (data[1] & 0xff) >= 80 &&
                    (data[2] & 0xff) == 78 &&
                    (data[3] & 0xff) == 71 &&
                    (data[4] & 0xff) == 13 &&
                    (data[5] & 0xff) == 10 &&
                    (data[6] & 0xff) == 26 &&
                    (data[7] & 0xff) == 10)
                return true;

            if ((ext == null || ".bmp".contentEquals(ext)) &&
                    (data[0] & 0xff) == 66 &&
                    (data[1] & 0xff) >= 77)
                return true;

            if ((ext == null || ".gif".contentEquals(ext)) &&
                    (data[0] & 0xff) == 0x47 &&
                    (data[1] & 0xff) == 0x49 &&
                    (data[2] & 0xff) == 0x46 &&
                    (data[3] & 0xff) == 0x38)
                return true;

            /*if ((ext == null || ".wmf".contentEquals(ext)) &&
                    (data[0] & 0xff) == 0xd7 &&
                    (data[1] & 0xff) == 0xcd &&
                    (data[2] & 0xff) == 0xc6 &&
                    (data[3] & 0xff) == 0x9a)
                return true;

            if ((ext == null || ".emf".contentEquals(ext)) &&
                    (data[0] & 0xff) == 0x20 &&
                    (data[23] & 0xff) == 0x45 &&
                    (data[24] & 0xff) == 0x4d &&
                    (data[25] & 0xff) == 0x46)
                return true;*/

            if ((ext == null || ".tif".contentEquals(ext) || ".tiff".contentEquals(ext)) &&
                    (data[0] & 0xff) == 0x49 &&
                    (data[1] & 0xff) == 0x49 &&
                    (data[2] & 0xff) == 0x2a &&
                    (data[3] & 0xff) == 0x00)
                return true;

        }
        return false;
    }

    @Override
    public void initState(AlBookOptions bookOptions, AlFiles myParent, AlPreferenceOptions pref, AlStylesOptions stl) {
        allState.isOpened = true;

        ident = "IMAGE";

        aFiles = myParent;

        preference = pref;
        styles = stl;

        autoCodePage = true;
        use_cpR0 = TAL_CODE_PAGES.AUTO;
        allState.state_parser = 0;
        allState.state_skipped_flag = false;

        size = 0;
        parser(0, aFiles.getSize());
        newParagraph();

        allState.isOpened = false;
    }

    @Override
    protected void doTextChar(char ch, boolean addSpecial) {
        if (allState.isOpened) {
            if (allState.text_present) {
                size++;
                parPositionE = allState.start_position;
                allState.letter_present = (allState.letter_present) || (ch != 0xa0 && ch != 0x20);
            } else {
                parPositionS = allState.start_position_par;
                formatAddonInt();
                parStart = size;
                allState.text_present = true;
                allState.letter_present = (allState.letter_present) || (ch != 0xa0 && ch != 0x20);
                size++;
                parPositionE = allState.start_position;
            }
        } else {
            if (allState.text_present) {
                stored_par.data[stored_par.cpos++] = ch;
            } else {
                stored_par.data[stored_par.cpos++] = ch;
                allState.text_present = true;
            }
        }
    }

    @Override
    protected void prepareCustom() {

    }

    @Override
    protected void parser(int start_pos, int stop_pos) {
        allState.start_position = 0;
        coverName = aFiles.fileName;

        addCharFromTag((char) AlStyles.CHAR_IMAGE_S, false);
        addCharFromTag(LEVEL2_COVERTOTEXT, false);
        addCharFromTag((char)AlStyles.CHAR_IMAGE_E, false);

        if (allState.isOpened) {
            im.add(AlOneImage.add(coverName, 0, stop_pos, AlOneImage.IMG_MEMO));
            newParagraph();
        }
    }

}
