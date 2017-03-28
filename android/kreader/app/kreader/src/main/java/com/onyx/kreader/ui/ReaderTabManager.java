package com.onyx.kreader.ui;

import com.alibaba.fastjson.JSON;
import com.onyx.android.sdk.utils.StringUtils;
import com.onyx.kreader.device.DeviceConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by joy on 3/1/17.
 */

public class ReaderTabManager {

    public enum ReaderTab {
        TAB_1, TAB_2, TAB_3, TAB_4
    }

    private HashMap<ReaderTab, Class<?>> tabActivityList = new HashMap<>();
    private HashMap<ReaderTab, Class<?>> tabReceiverList = new HashMap<>();

    private Queue<ReaderTab> freeTabList = new LinkedList<>();
    private LinkedHashMap<ReaderTab, String> openedTabs = new LinkedHashMap<>();

    public static boolean supportMultipleTabs() {
        return DeviceConfig.sharedInstance(KReaderApp.instance()).isSupportMultipleTabs();
    }

    private ReaderTabManager() {
        tabActivityList.put(ReaderTab.TAB_1, ReaderTab1Activity.class);
        tabActivityList.put(ReaderTab.TAB_2, ReaderTab2Activity.class);
        tabActivityList.put(ReaderTab.TAB_3, ReaderTab3Activity.class);
        tabActivityList.put(ReaderTab.TAB_4, ReaderTab4Activity.class);

        tabReceiverList.put(ReaderTab.TAB_1, ReaderTab1BroadcastReceiver.class);
        tabReceiverList.put(ReaderTab.TAB_2, ReaderTab2BroadcastReceiver.class);
        tabReceiverList.put(ReaderTab.TAB_3, ReaderTab3BroadcastReceiver.class);
        tabReceiverList.put(ReaderTab.TAB_4, ReaderTab4BroadcastReceiver.class);

        freeTabList.add(ReaderTab.TAB_1);
        freeTabList.add(ReaderTab.TAB_2);
        freeTabList.add(ReaderTab.TAB_3);
        freeTabList.add(ReaderTab.TAB_4);
    }

    public static ReaderTabManager create() {
        ReaderTabManager manager = new ReaderTabManager();
        return manager;
    }

    public static ReaderTabManager createFromJson(String json) {
        ReaderTabManager manager = new ReaderTabManager();
        if (StringUtils.isBlank(json)) {
            return buildForMultipleTabState(manager);
        }

        LinkedHashMap<String, String> map = JSON.parseObject(json, manager.openedTabs.getClass());
        for (LinkedHashMap.Entry<String, String> entry : map.entrySet()) {
            manager.openedTabs.put(Enum.valueOf(ReaderTab.class, entry.getKey()), entry.getValue());
        }
        for (LinkedHashMap.Entry<ReaderTab, String> entry : manager.openedTabs.entrySet()) {
            manager.freeTabList.remove(entry.getKey());
        }

        return buildForMultipleTabState(manager);
    }

    private static ReaderTabManager buildForMultipleTabState(ReaderTabManager manager) {
        if (supportMultipleTabs()) {
            return manager;
        }

        LinkedHashMap.Entry<ReaderTab, String> lastEntry = null;
        for (LinkedHashMap.Entry<ReaderTab, String> entry : manager.getOpenedTabs().entrySet()) {
            lastEntry = entry;
        }

        manager.openedTabs.clear();
        manager.freeTabList.clear();
        if (lastEntry == null) {
            manager.freeTabList.add(ReaderTab.TAB_1);
        } else {
            manager.openedTabs.put(lastEntry.getKey(), lastEntry.getValue());
        }
        return manager;
    }

    public String toJson() {
        return JSON.toJSONString(openedTabs);
    }

    public Class getTabActivity(ReaderTab tab) {
        return tabActivityList.get(tab);
    }

    public Class getTabReceiver(ReaderTab tab) {
        return tabReceiverList.get(tab);
    }

    public LinkedHashMap<ReaderTab, String> getOpenedTabs() {
        return openedTabs;
    }

    public ReaderTab pollFreeTab() {
        if (!freeTabList.isEmpty()) {
            return freeTabList.poll();
        }
        return null;
    }

    public ReaderTab reuseOpenedTab() {
        ReaderTab tab = openedTabs.keySet().iterator().next();
        openedTabs.remove(tab);
        return tab;
    }

    public void addOpenedTab(ReaderTab tab, String path) {
        openedTabs.put(tab, path);
    }

    public void removeOpenedTab(ReaderTab tab) {
        freeTabList.add(tab);
        openedTabs.remove(tab);
    }

    public boolean isTabOpened(ReaderTab tab) {
        return openedTabs.containsKey(tab);
    }

    public ReaderTab findOpenedTabByPath(String path) {
        for (Map.Entry<ReaderTab, String> entry : openedTabs.entrySet()) {
            if (entry.getValue().compareTo(path) == 0) {
                return entry.getKey();
            }
        }
        return null;
    }
}
