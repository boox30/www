package com.neverland.engbook.level2;

import android.util.Log;

import com.neverland.engbook.bookobj.FileBlockInfo;
import com.neverland.engbook.bookobj.TxtParserHelp;
import com.neverland.engbook.forpublic.AlBookOptions;
import com.neverland.engbook.forpublic.AlOneContent;
import com.neverland.engbook.forpublic.EngBookMyType;
import com.neverland.engbook.forpublic.TAL_CODE_PAGES;
import com.neverland.engbook.level1.AlFiles;
import com.neverland.engbook.unicode.AlUnicode;
import com.neverland.engbook.unicode.CP932;
import com.neverland.engbook.unicode.CP936;
import com.neverland.engbook.unicode.CP949;
import com.neverland.engbook.unicode.CP950;
import com.neverland.engbook.util.AlPreferenceOptions;
import com.neverland.engbook.util.AlStyles;
import com.neverland.engbook.util.AlStylesOptions;
import com.neverland.engbook.util.ChineseTextSectionRecognizer;

public class AlFormatTXT extends AlFormat {
	
	static final int STATE_TXT_NORMAL = 0;
	static final int STATE_TXT_WAIT = 1;

	static final int TXT_MODE_NORMAL = 0;
	static final int TXT_MODE_DOUBLEA = 1;
	static final int TXT_MODE_SPACE = 2;

	private int txt_mode;
	public FileBlockInfo loadFileBlockInfo;
	public boolean isOneParser = false;
	public boolean isStopParser = false;

	private void initParserParameter	(){
		isOneParser = false;
		isStopParser = false;
		blockSize = 0;
	}

	private ChineseTextSectionRecognizer chineseTextSectionRecognizer = ChineseTextSectionRecognizer.create();

	public void parserAfterData(final int start_pos){
		initParserParameter();
		if(loadFileBlockInfo != null) {
			FileBlockInfo.loadTwoBlockData(this, loadFileBlockInfo.twoBlock);
			FileBlockInfo.loadThreeBlockData(this, loadFileBlockInfo.threeBlock);
		}
		size = customSize;

		FileBlockInfo.saveParagraphData(fileBlocks, this);
	}

	@Override
	public String getFileName() {
		return  aFiles.fileName;
	}

	@Override
	public String getFileMD5(){
		return aFiles.getFileMD5();
	}

	public void initState(AlBookOptions bookOptions, AlFiles myParent, 
			AlPreferenceOptions pref, AlStylesOptions stl) {
		ident = "TEXT";

		aFiles = myParent;
		preference = pref;
		styles = stl;

		size = 0;
				
		autoCodePage = bookOptions.codePage == TAL_CODE_PAGES.AUTO;
		aFiles.applicationDirectory = bookOptions.applicationDirectory;
		
		allState.state_parser = 0;
		if (autoCodePage) {
			setCP(getBOMCodePage(true, true, true, true));
			if (use_cpR0 == TAL_CODE_PAGES.AUTO)
				setCP(bookOptions.codePageDefault);
		} else {
			setCP(bookOptions.codePage);
		}
		
		txt_mode = TXT_MODE_NORMAL;
		if ((bookOptions.formatOptions & 0x01) != 0)
			detectTXTMode();

		allState.state_parser = STATE_TXT_NORMAL;
		blockLoading(bookOptions.readPosition);
	}

	private void blockLoading(int readPosition){
		initParserParameter();
		customSize = 0;
		if(!FileBlockInfo.isCacheData(this)) {
			isOneParser = true;
			isStopParser = false;
			parser(0, aFiles.getSize());
			size = customSize;
			if(stop_posUsed + 1< aFiles.getSize()){
				loadFileBlockInfo = TxtParserHelp.getBlockLoadInfo(stop_posUsed + 1,aFiles.getSize());
			}
		}else{
			loadFileBlockInfo = TxtParserHelp.parserHelp(readPosition,this);
			FileBlockInfo.loadOneBlockData(this,loadFileBlockInfo.oneBlock);
		}
	}

	void detectTXTMode() {
		/*int i;
		char[]	buf_uc = new char [8192];
		for (i = 0; i < 8192; i++)
			buf_uc[i] = 0x00;
		getTestBuffer(aFiles, use_cpR, buf_uc);
		
		int lengthLines = 0;
		int countChars = 0;
		int style = 0;
		
		ArrayList<Integer> items = new ArrayList<Integer>();
		items.clear();
		
		char ch;
		for (i = 0; i < 8192; i++) {
			ch = buf_uc[i];
			if (ch == 0x00 || ch == 0x0d)				
				continue;
			
			switch (ch) {
			case 0x09: case 0x20: case 0xa0:
				if (lengthLines == 0)
					style |= TEST_START_SPACE;
				lengthLines++;
				countChars++;
				break;
			case 0x0a:
				if (lengthLines == 0)
					style |= TEST_EMPTY_LINE;
				if (lengthLines > 80)
					style |= TEST_80_CHAR;
				
				items.add(style);
				
				style = TEST_ITEM;
				lengthLines = 0;
				break;
			default:
				if (ch > 0x20) {
					countChars++;
					lengthLines++;
					style |= TEST_LINE_WITHCHAR;
				}
			}
		}
		
		int countNormalParagraph = items.size();
		int averageParagraphLength = (int)((float)countChars / ( 0.001f + countNormalParagraph));		
		int countPrevEmptyAParagraph = 0;
		int countIdentParagraph = 0;
		int count80char = 0;
		
		// test normal paragraph
		
		// test paragraph with previous empty
		
		for (i = 0; i < items.size(); i++)
			if (i == 0 || (items.get(i - 1) & TEST_EMPTY_LINE) != 0)
				countPrevEmptyAParagraph++;
		
		// test paragraph with start space 
		for (i = 0; i < items.size(); i++)
			if (i == 0 || (items.get(i) & TEST_START_SPACE) != 0)
				countIdentParagraph++;
		
		// teset 80 chars
		for (i = 0; i < items.size(); i++)
			if ((items.get(i) & TEST_80_CHAR) != 0)
				count80char++;
		//
		
		if (count80char == 0) {
			if (countPrevEmptyAParagraph > countIdentParagraph) {
				if (countPrevEmptyAParagraph * 30 > countNormalParagraph)
					txt_mode |= TXT_MODE_DOUBLEA;
			} else {
				if (countIdentParagraph * 30 > countNormalParagraph)
					txt_mode |= TXT_MODE_SPACE;
			}	
		}
		
		items.clear();*/
	}

	@Override
	protected void doTextChar(char ch, boolean addSpecial) {
		chineseTextSectionRecognizer.onNewCharacter(ch);
		
		if (parText.length > 0) {

			if (ch == 0xad)
				softHyphenCount++;

			parText.add(ch);

			customSize++;
			parText.positionE = allState.start_position;
			parText.haveLetter = parText.haveLetter || (ch != 0xa0 && ch != 0x20);

			if (parText.length > EngBookMyType.AL_MAX_PARAGRAPH_LEN) {
				if (!AlUnicode.isLetterOrDigit(ch) && !allState.insertFromTag && allState.state_parser == 0)
					addNewParagraph();
			}
		} else
		if (ch != 0x20) {
			parText.positionS = parText.positionE = allState.start_position;

			parText.paragraph = styleStack.getActualParagraph();
			parText.prop = styleStack.getActualProp();
			parText.tableStart = currentTable.start;
			parText.tableCounter = currentTable.counter;
			parText.sizeStart = customSize;

			parText.haveLetter = (ch != 0xa0 && ch != 0x20);
			customSize++;
			parText.add(ch);
		}
	}

	int stop_posUsed;
	private void addNewParagraph(){
		newParagraph();
		if(isOneParser) {
			if (parText.positionE > TxtParserHelp.TXT_BASE_SIZE) {
				stop_posUsed = parText.positionE;
				isStopParser = true;
			}
		}
	}

	@Override
	public void parserData(int start_pos, int stop_posRequest) {
		parser(start_pos,stop_posRequest);
	}

	@Override
	protected void parser(final int start_pos, final int stop_posRequest) {
		// this code must be in any parser without change!!!
		int 	buf_cnt;
		char 	ch, ch1;
		stop_posUsed = stop_posRequest;

		int j;
		//AlIntHolder jVal = new AlIntHolder(0);
		
		for (int i = start_pos; i < stop_posRequest;) {
			if(isStopParser){
				return;
			}
			buf_cnt = AlFiles.LEVEL1_FILE_BUF_SIZE;
			if (i + buf_cnt > stop_posRequest) {
				buf_cnt = aFiles.getByteBuffer(i, parser_inBuff, stop_posRequest - i + 2);
				if (buf_cnt > stop_posRequest - i)
					buf_cnt = stop_posRequest - i;
			} else {
				buf_cnt = aFiles.getByteBuffer(i, parser_inBuff, buf_cnt + 2);
				buf_cnt -= 2;				
			}
			
			for (j = 0; j < buf_cnt;) {
				if(isStopParser){
					return;
				}
				allState.start_position = i + j;	
				
				/*jVal.value = j;
				ch = AlUnicode.byte2Wide(use_cpR0, parser_inBuff, jVal);
				j = jVal.value;*/
				
				ch = (char)parser_inBuff[j++];
				ch &= 0xff;
				//if (ch >= 0x80) {
					switch (use_cpR0) {
					case TAL_CODE_PAGES.CP65001:
                        if ((ch & 0x80) == 0) { } else
						if ((ch & 0x20) == 0) {				
							ch = (char)((ch & 0x1f) << 6);				
							ch1 = (char)parser_inBuff[j++];
							ch += (char)(ch1 & 0x3f);
						} else
						if ((ch & 0x10) == 0) {
							ch = (char)((ch & 0x1f) << 6);				
							ch1 = (char)parser_inBuff[j++];							
							ch += (char)(ch1 & 0x3f);											
							ch <<= 6;				
							ch1 = (char)parser_inBuff[j++];
							ch += (char)(ch1 & 0x3f);
						} else {
							if ((ch & 0x08) == 0) {
								j += 3;
							} else if ((ch & 0x04) == 0) {
								j += 4;
							} else if ((ch & 0x02) == 0) {
								j += 5;
							}
							ch = '?';
						}
						break;
					case TAL_CODE_PAGES.CP1201:
						ch <<= 8;
						ch1 = (char)parser_inBuff[j++];
						ch |= ch1 & 0xff;
						break;
					case TAL_CODE_PAGES.CP1200:			
						ch1 = (char)parser_inBuff[j++];					
						ch |= ch1 << 8;
						break;
					case 932:
                        if (ch >= 0x80) {
                            switch (ch) {
                                case 0x80:
                                case 0xfd:
                                case 0xfe:
                                case 0xff:
                                    ch = 0x0000;
                                    break;
                                default:
                                    if (ch >= 0xa1 && ch <= 0xdf) {
                                        ch = (char) (ch + 0xfec0);
                                        break;
                                    }
                                    ch1 = (char) (parser_inBuff[j++] & 0xff);
                                    ch = (ch1 >= 0x40 && ch1 <= 0xfc) ? CP932.getChar(ch, ch1) : 0x00;
                                    break;
                            }
                        }
						break;
					case 936:
                        if (ch >= 0x80) {
                            switch (ch) {
                                case 0x80:
                                    ch = 0x20AC;
                                    break;
                                case 0xff:
                                    ch = 0x0000;
                                    break;
                                default:
                                    ch1 = (char) (parser_inBuff[j++] & 0xff);
                                    ch = (ch1 >= 0x40 && ch1 <= 0xfe) ? CP936.getChar(ch, ch1) : 0x00;
                                    break;
                            }
                        }
						break;	
					case 949:
                        if (ch >= 0x80) {
                            switch (ch) {
                                case 0x80:
                                case 0xff:
                                    ch = 0x0000;
                                    break;
                                default:
                                    ch1 = (char) (parser_inBuff[j++] & 0xff);
                                    ch = (ch1 >= 0x41 && ch1 <= 0xfe) ? CP949.getChar(ch, ch1) : 0x00;
                                    break;
                            }
                        }
						break;
					case 950:
                        if (ch >= 0x80) {
                            switch (ch) {
                                case 0x80:
                                case 0xff:
                                    ch = 0x0000;
                                    break;
                                default:
                                    ch1 = (char) (parser_inBuff[j++] & 0xff);
                                    ch = (ch1 >= 0x40 && ch1 <= 0xfe) ? CP950.getChar(ch, ch1) : 0x00;
                                    break;
                            }
                        }
						break;
						
					default:
						if (ch >= 0x80)
						    ch = data_cp[ch - 0x80];
						break;
					}
				//}
				if ((ch & AlStyles.STYLE_MASK_4CODECONVERT) == AlStyles.STYLE_BASE_4CODECONVERT)
					ch = 0x00;
				
		// end must be code				
				/////////////////// Begin Real Parser
				switch (txt_mode) {				
				case TXT_MODE_DOUBLEA:
					switch (allState.state_parser) {
					case STATE_TXT_NORMAL:
						if (ch < 0x20) {
							if (ch == 0x0a) {
								allState.state_parser = STATE_TXT_WAIT;
							} else	
							if (ch == 0x09) {
								doTextChar(' ', true);
							}
						} else {
							doTextChar(ch, true);
						}
						break;
					case STATE_TXT_WAIT:
						if (ch < 0x20) {
							if (ch == 0x0a) {
								if (parText.length > 0) {
									addNewParagraph();
								} else {
									newEmptyTextParagraph();
								}
								allState.state_parser = STATE_TXT_NORMAL;
							} else	
							if (ch == 0x09) {
								doTextChar(' ', true);
								allState.state_parser = STATE_TXT_NORMAL;
							}
						} else {
							doTextChar(' ', true);
							doTextChar(ch, true);
							allState.state_parser = STATE_TXT_NORMAL;
						}
						break;
					}				
					break;	
				case TXT_MODE_SPACE:
					switch (allState.state_parser) {
					case STATE_TXT_NORMAL:
						if (ch < 0x20) {
							if (ch == 0x0a) {
								allState.state_parser = STATE_TXT_WAIT;
							} else	
							if (ch == 0x09) {
								doTextChar(' ', true);
							}
						} else {
							doTextChar(ch, true);
						}
						break;
					case STATE_TXT_WAIT:
						if (ch == 0x20 || ch == 0xa0 || ch == 0x09) {
							if (parText.length > 0) {
								addNewParagraph();
							} else {
								newEmptyTextParagraph();
							}
							allState.state_parser = STATE_TXT_NORMAL;
						} else
						if (ch < 0x20) {
							
						} else {
							doTextChar(' ', true);
							doTextChar(ch, true);
							allState.state_parser = STATE_TXT_NORMAL;
						}
						break;
					}				
					break;	
				default:				
					if (ch < 0x20) {
						if (ch == 0x0a) {
							if (parText.length > 0) {
								addNewParagraph();
							} else {
								newEmptyTextParagraph();
							}
						} else	
						if (ch == 0x09) {
							doTextChar(' ', true);
						}
					} else {
						doTextChar(ch, true);
					}
					break;
				}
				/////////////////// End Real Parser
		// this code must be in any parser without change!!!
			}
			i += j;
		}
		addNewParagraph();
		// end must be cod
	}

	@Override
	void newParagraph() {
		if (chineseTextSectionRecognizer.matches()) {
			addContent(AlOneContent.add(chineseTextSectionRecognizer.getSectionText(), parText.sizeStart, 0));
		}

		chineseTextSectionRecognizer.reset();
		super.newParagraph();
	}

	@Override
	void newEmptyTextParagraph() {
		chineseTextSectionRecognizer.reset();
		super.newEmptyTextParagraph();
	}
}
