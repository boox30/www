package com.neverland.engbook.forpublic;

import android.app.Application;


import com.neverland.engbook.forpublic.EngBookMyType.TAL_HYPH_LANG;
import com.neverland.engbook.forpublic.EngBookMyType.TAL_SCREEN_DPI;
import com.neverland.engbook.forpublic.EngBookMyType.TAL_SCREEN_PAGES_COUNT;

/**
 * параметры инициализации библиотеки. Все (кроме языка переносов) перечисленные здесь значения не могут быть изменены ПОСЛЕ инициализации
 */
public class AlEngineOptions {
	public static final int AL_DEFAULT_PAGESIZE0 =	1024;
	public static final int AL_USEAUTO_PAGESIZE  = 	-1;
	public static final int AL_USEDEF_PAGESIZE	 = 	0;

	public int									magic = 0x1812;
	/**
	 * способ подсчета количества страниц
	 */
	public TAL_SCREEN_PAGES_COUNT				useScreenPages = TAL_SCREEN_PAGES_COUNT.SIZE;
	/**
	 * дпи сновного экрана. см. описание TAL_SCREEN_DPI
	 */
	public TAL_SCREEN_DPI						DPI = TAL_SCREEN_DPI.TAL_SCREEN_DPI_160; // for Win32 - only 160
	/**
	 * каталог, откуда библиотека считывает шрифты
	 */
	public String								font_catalog; // not to use for win32
	/**
	 * язык переносов, который использует библиотека по умолчанию
	 */
	public TAL_HYPH_LANG						hyph_lang = TAL_HYPH_LANG.NONE;
	/**
	 *
	 */
	public Application							appInstance;
	/**
	 * некоторые ньюансы форматирования текста на странице - устанавливать в TRUE только если локаль устройства - китайская
	 */
	public boolean								chinezeFormatting = true;
	/**
	 * используемый размер странцы в случае если useScreenPages равно TAL_SCREEN_PAGES_COUNT_BY_SIZE
	 pageSize4Use = AL_USEAUTO_PAGESIZE - вариант автоматического подсчета размера страницы
	 */
	public int									pageSize4Use = AL_USEDEF_PAGESIZE;
}
