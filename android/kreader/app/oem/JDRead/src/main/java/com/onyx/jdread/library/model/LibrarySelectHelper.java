package com.onyx.jdread.library.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hehai on 17-12-20.
 */

public class LibrarySelectHelper {
    private Map<String, LibrarySelectedModel> childLibrarySelectedMap = new HashMap<>();
    private LibrarySelectedModel librarySelectedModel = new LibrarySelectedModel(0);

    public Map<String, LibrarySelectedModel> getChildLibrarySelectedMap() {
        return childLibrarySelectedMap;
    }

    public void putLibrarySelectedModelMap(String libraryId, int childCount) {
        if (childLibrarySelectedMap.containsKey(libraryId)) {
            return;
        }
        LibrarySelectedModel selectedModel = new LibrarySelectedModel(childCount);
        childLibrarySelectedMap.put(libraryId, selectedModel);
    }

    public void removeLibrarySelectedModelMap(String libraryId) {
        childLibrarySelectedMap.remove(libraryId);
    }

    public LibrarySelectedModel getLibrarySelectedModel(String libraryId) {
        LibrarySelectedModel librarySelectedModel = childLibrarySelectedMap.get(libraryId);
        if (librarySelectedModel != null) {
            return librarySelectedModel;
        }
        return this.librarySelectedModel;
    }

    public void clearSelectedData() {
        for (LibrarySelectedModel selectedModel : childLibrarySelectedMap.values()) {
            selectedModel.getSelectedList().clear();
            selectedModel.setSelectedAll(false);
        }
        librarySelectedModel.setSelectedAll(false);
        librarySelectedModel.getSelectedList().clear();
    }

    public boolean haveSelected() {
        boolean haveSelected = false;
        if (librarySelectedModel.haveSelected()) {
            haveSelected = true;
        }

        for (LibrarySelectedModel selectedModel : childLibrarySelectedMap.values()) {
            if (selectedModel.haveSelected()) {
                haveSelected = true;
            }
        }

        return haveSelected;
    }
}
