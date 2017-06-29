package com.onyx.android.dr.data;

import android.content.Context;

import com.onyx.android.dr.R;
import com.onyx.android.dr.common.Constants;
import com.onyx.android.dr.event.ApplicationEvent;
import com.onyx.android.dr.event.ArticlePushMenuEvent;
import com.onyx.android.dr.event.DictMenuEvent;
import com.onyx.android.dr.event.GradedBooksEvent;
import com.onyx.android.dr.event.ListenAndSayMenuEvent;
import com.onyx.android.dr.event.MyBooksMenuEvent;
import com.onyx.android.dr.event.NotesMenuEvent;
import com.onyx.android.dr.event.ProfessionalMaterialsMenuEvent;
import com.onyx.android.dr.event.RealTimeBooksMenuEvent;
import com.onyx.android.dr.event.SchoolBasedMaterialsMenuEvent;
import com.onyx.android.dr.event.SettingsMenuEvent;
import com.onyx.android.dr.event.TeachingAidsMenuEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hehai on 17-6-28.
 */

public class MainTabMenuConfig {
    private static final int MAIN_TAB_ROW = 1;
    private static final int MAIN_TAB_COLUMN = 6;
    private static List<MenuData> highSchoolMenuData = new ArrayList<>();
    private static List<MenuData> universityMenuData = new ArrayList<>();
    private static List<MenuData> teacherMenuData = new ArrayList<>();

    public static void loadMenuInfo(Context context) {
        MenuData menuData = new MenuData(context.getResources().getString(R.string.graded_books), R.drawable.ic_books, new GradedBooksEvent());
        highSchoolMenuData.add(menuData);
        menuData = new MenuData(context.getResources().getString(R.string.my_books), R.drawable.ic_books, new MyBooksMenuEvent());
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.real_time_articles), R.drawable.ic_real_time_books, new RealTimeBooksMenuEvent());
        highSchoolMenuData.add(menuData);
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.school_based_materials), R.drawable.ic_professional_materials, new SchoolBasedMaterialsMenuEvent());
        highSchoolMenuData.add(menuData);
        menuData = new MenuData(context.getResources().getString(R.string.professional_materials), R.drawable.ic_professional_materials, new ProfessionalMaterialsMenuEvent());
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.dict), R.drawable.ic_dict, new DictMenuEvent());
        highSchoolMenuData.add(menuData);
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.notes), R.drawable.ic_note, new NotesMenuEvent());
        highSchoolMenuData.add(menuData);
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.listen_and_say), R.drawable.ic_listen, new ListenAndSayMenuEvent());
        highSchoolMenuData.add(menuData);
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.application),R.drawable.ic_application,new ApplicationEvent());
        highSchoolMenuData.add(menuData);
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.settings), R.drawable.ic_settings, new SettingsMenuEvent());
        highSchoolMenuData.add(menuData);
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);

        menuData = new MenuData(context.getResources().getString(R.string.article_push), R.drawable.ic_add, new ArticlePushMenuEvent());
        highSchoolMenuData.add(menuData);
        universityMenuData.add(menuData);
        teacherMenuData.add(menuData);
    }

    public static List<MenuData> getMenuData(int UserType) {
        switch (UserType) {
            case Constants.ACCOUNT_TYPE_HIGH_SCHOOL:
                return highSchoolMenuData;
            case Constants.ACCOUNT_TYPE_UNIVERSITY:
                return universityMenuData;
            case Constants.ACCOUNT_TYPE_TEACHER:
                return teacherMenuData;
        }
        return highSchoolMenuData;
    }
}
