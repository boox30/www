package com.neverland.engbook.util;

import com.neverland.engbook.forpublic.AlBitmap;




public class AlProfileOptions {
	public boolean					useCT;
	public boolean					style_summ;
	public boolean					classicFirstLetter;
	public float					font_space;
	
	public final int[]					font_sizes = new int [InternalConst.TAL_PROFILE_FONT_COUNT];
	public final int[]					colors = new int [InternalConst.TAL_PROFILE_COLOR_COUNT];
	public final int[]					font_widths = new int [InternalConst.TAL_PROFILE_FONT_COUNT];
	public final int[]					font_weigths = new int [InternalConst.TAL_PROFILE_FONT_COUNT];
	public final String[]					font_names = new String[InternalConst.TAL_PROFILE_FONT_COUNT];
	public final boolean[]				font_bold = new boolean [InternalConst.TAL_PROFILE_FONT_COUNT];
	public final boolean[]				font_italic = new boolean [InternalConst.TAL_PROFILE_FONT_COUNT];
	public final int[]					font_interline = new int [InternalConst.TAL_PROFILE_FONT_COUNT];

	public int						marginL;
	public int						marginT;
	public int						marginR;
	public int						marginB;

	public int						showFirstLetter;
	public boolean					twoColumnRequest;
	public boolean					twoColumnUsed;

	public AlBitmap					background;
	public int						backgroundMode;

	public boolean					isTransparentImage;
	
	public int						DPIMultiplex;
	public boolean					specialModeRoll;

}
