package com.neverland.engbook.level2;

import com.neverland.engbook.level1.AlFiles;

public class AlSlotData {
	public int 		active 	= 0;
	public int 		shtamp 	= -1;
	public final int[] 	start 	= { 0,  0};
	public final int[] 	end	= {-1, -1};
	public final char[][] 	txt 	= {null, null};
	public final long[][] 	stl 	= {null, null};
	public final boolean[]  dataState = {false,false};
	
	public final void initBuffer() {
		if (txt[active] == null) {
			txt[active] = new char [AlFiles.LEVEL1_FILE_BUF_SIZE];
		}
		if (stl[active] == null) {
			stl[active] = new long [AlFiles.LEVEL1_FILE_BUF_SIZE];
		}
		dataState[active] = false;
	}
}
