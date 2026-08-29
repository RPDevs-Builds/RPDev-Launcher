/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.model.data;

import static android.text.TextUtils.isEmpty;

import static androidx.core.util.Preconditions.checkNotNull;

import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_APP_PAIR;
import static com.android.launcher3.LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT;
import static com.android.launcher3.folder.FolderIcon.inflateIcon;
import static com.android.launcher3.logger.LauncherAtom.Attribute.EMPTY_LABEL;
import static com.android.launcher3.logger.LauncherAtom.Attribute.MANUAL_LABEL;
import static com.android.launcher3.logger.LauncherAtom.Attribute.SUGGESTED_LABEL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.folder.Folder;
import com.android.launcher3.folder.FolderNameInfos;
import com.android.launcher3.icons.BitmapInfo;
import com.android.launcher3.icons.BitmapRenderer;
import com.android.launcher3.icons.cache.CacheLookupFlag;
import com.android.launcher3.logger.LauncherAtom;
import com.android.launcher3.logger.LauncherAtom.Attribute;
import com.android.launcher3.logger.LauncherAtom.FolderIcon;
import com.android.launcher3.logger.LauncherAtom.FromState;
import com.android.launcher3.logger.LauncherAtom.ToState;
import com.android.launcher3.model.ModelWriter;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ContentWriter;

import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * Represents a folder containing shortcuts or apps.
 */
public class FolderInfo extends CollectionInfo {

    /**
     * The multi-page animation has run for this folder
     */
    public static final int FLAG_MULTI_PAGE_ANIMATION = 0x00000004;

    public static final int FLAG_MANUAL_FOLDER_NAME = 0x00000008;

    public static final int FLAG_COVER_MODE = 0x00000010;
    public static final int FLAG_COVER_REVERSE_ACTIONS = 0x00000020;
    public static final int FLAG_COVER_SHOW_FOLDER_NAME = 0x00000040;
    public static final int FLAG_COVER_INDICATOR_NOTCH = 0x00000080;
    public static final int FLAG_COVER_INDICATOR_DOT = 0x00000100;
    public static final int FLAG_COVER_INDICATOR_COUNT = 0x00000200;
    private static final int MASK_COVER_INDICATOR = FLAG_COVER_INDICATOR_NOTCH | FLAG_COVER_INDICATOR_DOT | FLAG_COVER_INDICATOR_COUNT;

    public static final int FLAG_SORT_AZ = 0x00000400;
    public static final int FLAG_SORT_ZA = 0x00000800;
    public static final int FLAG_SORT_MOST_USED = 0x00001000;
    public static final int FLAG_SORT_BY_INSTALL_DATE = 0x00002000;
    public static final int FLAG_SORT_BY_COLOR = 0x00004000;
    private static final int MASK_SORT_MODE = FLAG_SORT_AZ | FLAG_SORT_ZA | FLAG_SORT_MOST_USED
            | FLAG_SORT_BY_INSTALL_DATE | FLAG_SORT_BY_COLOR;

    public static final int FLAG_PREVIEW_GRID = 0x00008000;
    public static final int FLAG_PREVIEW_RADIAL = 0x00010000;
    public static final int FLAG_PREVIEW_STACKED = 0x00020000;
    public static final int FLAG_PREVIEW_FAN = 0x00040000;
    private static final int MASK_PREVIEW_STYLE = FLAG_PREVIEW_GRID | FLAG_PREVIEW_RADIAL
            | FLAG_PREVIEW_STACKED | FLAG_PREVIEW_FAN;

    public static final int COVER_INDICATOR_NONE = 0;
    public static final int COVER_INDICATOR_NOTCH = 1;
    public static final int COVER_INDICATOR_DOT = 2;
    public static final int COVER_INDICATOR_COUNT = 3;

    public static final int SORT_MANUAL = 0;
    public static final int SORT_AZ = 1;
    public static final int SORT_ZA = 2;
    public static final int SORT_MOST_USED = 3;
    public static final int SORT_BY_INSTALL_DATE = 4;
    public static final int SORT_BY_COLOR = 5;

    public static final int PREVIEW_STYLE_DEFAULT = 0;
    public static final int PREVIEW_STYLE_GRID = 1;
    public static final int PREVIEW_STYLE_RADIAL = 2;
    public static final int PREVIEW_STYLE_STACKED = 3;
    public static final int PREVIEW_STYLE_FAN = 4;

    /**
     * Different states of folder label.
     */
    public enum LabelState {
        // Folder's label is not yet assigned( i.e., title == null). Eligible for auto-labeling.
        UNLABELED(Attribute.UNLABELED),

        // Folder's label is empty(i.e., title == ""). Not eligible for auto-labeling.
        EMPTY(EMPTY_LABEL),

        // Folder's label is one of the non-empty suggested values.
        SUGGESTED(SUGGESTED_LABEL),

        // Folder's label is non-empty, manually entered by the user
        // and different from any of suggested values.
        MANUAL(MANUAL_LABEL);

        private final LauncherAtom.Attribute mLogAttribute;

        LabelState(Attribute logAttribute) {
            this.mLogAttribute = logAttribute;
        }
    }

    public int options;

    public String coverComponentUri = null;

    public String linkedDrawerFolderId = null;

    public FolderNameInfos suggestedFolderNames;

    /**
     * The apps and shortcuts
     */
    public ArrayList<ItemInfo> contents = new ArrayList<>();

    public FolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
    }

    @Override
    public void add(@NonNull ItemInfo item) {
        if (!willAcceptItemType(item.itemType)) {
            throw new RuntimeException("tried to add an illegal type into a folder");
        }
        if (item == this || (item instanceof FolderInfo fi && (fi.id == this.id || wouldCreateCycle(fi)))) {
            return;
        }
        getContents().add(item);
    }

    public boolean wouldCreateCycle(FolderInfo child) {
        if (child == null || child == this || child.id == this.id) return true;
        for (ItemInfo item : child.contents) {
            if (item instanceof FolderInfo sub) {
                if (sub == this || sub.id == this.id || wouldCreateCycle(sub)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the folder's contents as an unsorted ArrayList of {@link ItemInfo}. Includes
     * {@link WorkspaceItemInfo} and {@link AppPairInfo}s.
     */
    @NonNull
    @Override
    public ArrayList<ItemInfo> getContents() {
        return contents;
    }

    /**
     * Returns the folder's contents as an ArrayList of {@link WorkspaceItemInfo}. Note: Does not
     * return any {@link AppPairInfo}s contained in the folder, instead collects *their* contents
     * and adds them to the ArrayList.
     */
    @Override
    public ArrayList<WorkspaceItemInfo> getAppContents()  {
        ArrayList<WorkspaceItemInfo> workspaceItemInfos = new ArrayList<>();
        for (ItemInfo item : contents) {
            if (item instanceof WorkspaceItemInfo wii) {
                workspaceItemInfos.add(wii);
            } else if (item instanceof AppPairInfo api) {
                workspaceItemInfos.addAll(api.getAppContents());
            } else if (item instanceof FolderInfo fi) {
                workspaceItemInfos.addAll(fi.getAppContents());
            }
        }
        return workspaceItemInfos;
    }

    @Override
    public void onAddToDatabase(@NonNull ContentWriter writer) {
        super.onAddToDatabase(writer);
        writer.put(LauncherSettings.Favorites.OPTIONS, options);
        writer.put(LauncherSettings.Favorites.INTENT, coverComponentUri);
        writer.put(LauncherSettings.Favorites.APPWIDGET_PROVIDER, linkedDrawerFolderId != null ? linkedDrawerFolderId : "");
    }

    public boolean hasOption(int optionFlag) {
        return (options & optionFlag) != 0;
    }

    /**
     * @param option flag to set or clear
     * @param isEnabled whether to set or clear the flag
     * @param writer if not null, save changes to the db.
     */
    public void setOption(int option, boolean isEnabled, ModelWriter writer) {
        int oldOptions = options;
        if (isEnabled) {
            options |= option;
        } else {
            options &= ~option;
        }
        if (writer != null && oldOptions != options) {
            writer.updateItemInDatabase(this);
        }
    }

    public boolean isCoverMode() {
        return hasOption(FLAG_COVER_MODE);
    }

    public void setCoverMode(boolean enable, ModelWriter modelWriter) {
        setOption(FLAG_COVER_MODE, enable, modelWriter);
        onIconChanged();
    }

    public boolean isCoverReverseActions() {
        return hasOption(FLAG_COVER_REVERSE_ACTIONS);
    }

    public void setCoverReverseActions(boolean enable, ModelWriter modelWriter) {
        setOption(FLAG_COVER_REVERSE_ACTIONS, enable, modelWriter);
        onIconChanged();
    }

    public boolean isCoverShowFolderName() {
        return hasOption(FLAG_COVER_SHOW_FOLDER_NAME);
    }

    public void setCoverShowFolderName(boolean enable, ModelWriter modelWriter) {
        setOption(FLAG_COVER_SHOW_FOLDER_NAME, enable, modelWriter);
        onIconChanged();
    }

    public int getCoverIndicatorStyle() {
        if (hasOption(FLAG_COVER_INDICATOR_NOTCH)) return COVER_INDICATOR_NOTCH;
        if (hasOption(FLAG_COVER_INDICATOR_DOT)) return COVER_INDICATOR_DOT;
        if (hasOption(FLAG_COVER_INDICATOR_COUNT)) return COVER_INDICATOR_COUNT;
        return COVER_INDICATOR_NONE;
    }

    public void setCoverIndicatorStyle(int style, ModelWriter modelWriter) {
        int oldOptions = options;
        options &= ~MASK_COVER_INDICATOR;
        if (style == COVER_INDICATOR_NOTCH) {
            options |= FLAG_COVER_INDICATOR_NOTCH;
        } else if (style == COVER_INDICATOR_DOT) {
            options |= FLAG_COVER_INDICATOR_DOT;
        } else if (style == COVER_INDICATOR_COUNT) {
            options |= FLAG_COVER_INDICATOR_COUNT;
        }
        if (modelWriter != null && oldOptions != options) {
            modelWriter.updateItemInDatabase(this);
        }
        onIconChanged();
    }

    public int getSortMode() {
        if (hasOption(FLAG_SORT_AZ)) return SORT_AZ;
        if (hasOption(FLAG_SORT_ZA)) return SORT_ZA;
        if (hasOption(FLAG_SORT_MOST_USED)) return SORT_MOST_USED;
        if (hasOption(FLAG_SORT_BY_INSTALL_DATE)) return SORT_BY_INSTALL_DATE;
        if (hasOption(FLAG_SORT_BY_COLOR)) return SORT_BY_COLOR;
        return SORT_MANUAL;
    }

    public void setSortMode(int mode, ModelWriter modelWriter) {
        int oldOptions = options;
        options &= ~MASK_SORT_MODE;
        if (mode == SORT_AZ) {
            options |= FLAG_SORT_AZ;
        } else if (mode == SORT_ZA) {
            options |= FLAG_SORT_ZA;
        } else if (mode == SORT_MOST_USED) {
            options |= FLAG_SORT_MOST_USED;
        } else if (mode == SORT_BY_INSTALL_DATE) {
            options |= FLAG_SORT_BY_INSTALL_DATE;
        } else if (mode == SORT_BY_COLOR) {
            options |= FLAG_SORT_BY_COLOR;
        }
        if (modelWriter != null && oldOptions != options) {
            modelWriter.updateItemInDatabase(this);
        }
        onIconChanged();
    }

    public int getPreviewStyle() {
        if (hasOption(FLAG_PREVIEW_GRID)) return PREVIEW_STYLE_GRID;
        if (hasOption(FLAG_PREVIEW_RADIAL)) return PREVIEW_STYLE_RADIAL;
        if (hasOption(FLAG_PREVIEW_STACKED)) return PREVIEW_STYLE_STACKED;
        if (hasOption(FLAG_PREVIEW_FAN)) return PREVIEW_STYLE_FAN;
        return PREVIEW_STYLE_DEFAULT;
    }

    public void setPreviewStyle(int style, ModelWriter modelWriter) {
        int oldOptions = options;
        options &= ~MASK_PREVIEW_STYLE;
        if (style == PREVIEW_STYLE_GRID) {
            options |= FLAG_PREVIEW_GRID;
        } else if (style == PREVIEW_STYLE_RADIAL) {
            options |= FLAG_PREVIEW_RADIAL;
        } else if (style == PREVIEW_STYLE_STACKED) {
            options |= FLAG_PREVIEW_STACKED;
        } else if (style == PREVIEW_STYLE_FAN) {
            options |= FLAG_PREVIEW_FAN;
        }
        if (modelWriter != null && oldOptions != options) {
            modelWriter.updateItemInDatabase(this);
        }
        onIconChanged();
    }

    /**
     * Designates a specific child application as the cover item for this folder
     * without altering the order or ranks of items inside the folder.
     */
    public void setCoverApp(@Nullable WorkspaceItemInfo coverApp, @Nullable Launcher launcher) {
        if (coverApp == null) {
            coverComponentUri = null;
        } else if (coverApp.getIntent() != null) {
            coverComponentUri = coverApp.getIntent().toUri(0);
        } else if (coverApp.getTargetComponent() != null) {
            coverComponentUri = coverApp.getTargetComponent().flattenToString();
        }

        ModelWriter writer = launcher != null ? launcher.getModelWriter() : null;
        if (writer != null) {
            writer.updateItemInDatabase(this);
        }
        onIconChanged();
    }

    /**
     * Retrieves the designated cover application, or the first application item
     * contained in the folder to act as the cover item.
     */
    @Nullable
    public WorkspaceItemInfo getCoverInfo() {
        if (coverComponentUri != null) {
            for (ItemInfo item : getContents()) {
                if (item instanceof WorkspaceItemInfo wii) {
                    if (wii.getIntent() != null && TextUtils.equals(wii.getIntent().toUri(0), coverComponentUri)) {
                        return wii;
                    }
                    if (wii.getTargetComponent() != null && TextUtils.equals(wii.getTargetComponent().flattenToString(), coverComponentUri)) {
                        return wii;
                    }
                }
            }
        }
        for (ItemInfo item : getContents()) {
            if (item instanceof WorkspaceItemInfo wii) return wii;
        }
        return null;
    }

    public com.android.launcher3.util.ComponentKey getFolderComponentKey() {
        return new com.android.launcher3.util.ComponentKey(
                new android.content.ComponentName("com.neoapps.neolauncher.folder", "folder_" + id), user);
    }

    /**
     * Returns the icon drawable for this folder.
     * When Cover Mode is active, returns the custom folder icon or the themed icon of the cover app.
     */
    public Drawable getIcon(Context context) {
        Launcher launcher = Launcher.getLauncher(context);
        if (isCoverMode()) {
            com.android.launcher3.util.ComponentKey folderKey = getFolderComponentKey();
            com.neoapps.neolauncher.data.models.IconPickerItem override =
                    com.neoapps.neolauncher.data.IconOverrideRepository.INSTANCE.get(context).getOverridesMap().get(folderKey);
            if (override != null) {
                try {
                    Drawable customDrawable = com.neoapps.neolauncher.iconpack.IconPackProvider.INSTANCE.get(context)
                            .getDrawable(override.toIconEntry(), 0, user);
                    if (customDrawable != null) {
                        return customDrawable;
                    }
                } catch (Exception ignored) {
                }
            }
            WorkspaceItemInfo cover = getCoverInfo();
            return cover != null ? cover.newIcon(context, BitmapInfo.FLAG_THEMED) : null;
        }
        return getFolderIcon(launcher);
    }

    public Drawable getFolderIcon(Launcher launcher) {
        int iconSize = launcher.getDeviceProfile().folderIconSizePx;
        LinearLayout dummy = new LinearLayout(launcher, null);
        com.android.launcher3.folder.FolderIcon icon =  inflateIcon(R.layout.folder_icon, launcher, dummy, this);
        icon.isCustomIcon = false;
        icon.getFolderBackground().setStartOpacity(1f);
        Bitmap b = BitmapRenderer.createHardwareBitmap(iconSize, iconSize, out -> {
            out.translate(iconSize / 2f, 0);
            icon.draw(out);
        });
        icon.unbind();
        return new BitmapDrawable(launcher.getResources(), b);
    }

    /**
     * Returns the appropriate title to display for this folder on the workspace.
     * Handles null folders, cover title mode, and empty coverInfo safely.
     */
    public CharSequence getIconTitle(Folder folder) {
        if (!isCoverMode() || isCoverShowFolderName()) {
            if (folder != null && !TextUtils.equals(folder.getDefaultFolderName(), title)) {
                return title;
            } else if (folder != null) {
                return folder.getDefaultFolderName();
            } else {
                return title;
            }
        } else {
            WorkspaceItemInfo info = getCoverInfo();
            return info != null && info.title != null ? info.title : title;
        }
    }

    @Override
    protected String dumpProperties() {
        return String.format("%s; labelState=%s", super.dumpProperties(), getLabelState());
    }

    @NonNull
    @Override
    public LauncherAtom.ItemInfo buildProto(@Nullable CollectionInfo cInfo, Context context) {
        FolderIcon.Builder folderIcon = FolderIcon.newBuilder()
                .setCardinality(getContents().size());
        if (LabelState.SUGGESTED.equals(getLabelState())) {
            folderIcon.setLabelInfo(title.toString());
        }
        return getDefaultItemInfoBuilder(context)
                .setFolderIcon(folderIcon)
                .setRank(rank)
                .addItemAttributes(getLabelState().mLogAttribute)
                .setContainerInfo(getContainerInfo())
                .build();
    }

    public void setTitle(@Nullable CharSequence title, ModelWriter modelWriter) {
        // Updating label from null to empty is considered as false touch.
        // Retaining null title(ie., UNLABELED state) allows auto-labeling when new items added.
        if (isEmpty(title) && this.title == null) {
            return;
        }

        // Updating title to same value does not change any states.
        if (title != null && title.equals(this.title)) {
            return;
        }

        this.title = title;
        LabelState newLabelState =
                title == null ? LabelState.UNLABELED
                        : title.length() == 0 ? LabelState.EMPTY :
                                getAcceptedSuggestionIndex().isPresent() ? LabelState.SUGGESTED
                                        : LabelState.MANUAL;

        if (newLabelState.equals(LabelState.MANUAL)) {
            options |= FLAG_MANUAL_FOLDER_NAME;
        } else {
            options &= ~FLAG_MANUAL_FOLDER_NAME;
        }
        if (modelWriter != null) {
            modelWriter.updateItemInDatabase(this);
        }
    }

    /**
     * Returns current state of the current folder label.
     */
    public LabelState getLabelState() {
        return title == null ? LabelState.UNLABELED
                : title.length() == 0 ? LabelState.EMPTY :
                        hasOption(FLAG_MANUAL_FOLDER_NAME) ? LabelState.MANUAL
                                : LabelState.SUGGESTED;
    }

    @NonNull
    @Override
    public ItemInfo makeShallowCopy() {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.copyFrom(this);
        return folderInfo;
    }

    @Override
    public void copyFrom(@NonNull ItemInfo info) {
        super.copyFrom(info);
        if (info instanceof FolderInfo fi) {
            contents.addAll(fi.getContents());
            coverComponentUri = fi.coverComponentUri;
            linkedDrawerFolderId = fi.linkedDrawerFolderId;
        }
    }

    /**
     * Returns index of the accepted suggestion.
     */
    public OptionalInt getAcceptedSuggestionIndex() {
        String newLabel = checkNotNull(title,
                "Expected valid folder label, but found null").toString();
        if (suggestedFolderNames == null || !suggestedFolderNames.hasSuggestions()) {
            return OptionalInt.empty();
        }
        CharSequence[] labels = suggestedFolderNames.getLabels();
        return IntStream.range(0, labels.length)
                .filter(index -> !isEmpty(labels[index])
                        && newLabel.equalsIgnoreCase(
                        labels[index].toString()))
                .sequential()
                .findFirst();
    }

    /**
     * Returns {@link FromState} based on current {@link #title}.
     */
    public LauncherAtom.FromState getFromLabelState() {
        switch (getLabelState()){
            case EMPTY:
                return LauncherAtom.FromState.FROM_EMPTY;
            case MANUAL:
                return LauncherAtom.FromState.FROM_CUSTOM;
            case SUGGESTED:
                return LauncherAtom.FromState.FROM_SUGGESTED;
            case UNLABELED:
            default:
                return LauncherAtom.FromState.FROM_STATE_UNSPECIFIED;
        }
    }

    /**
     * Returns {@link ToState} based on current {@link #title}.
     */
    public LauncherAtom.ToState getToLabelState() {
        if (title == null) {
            return LauncherAtom.ToState.TO_STATE_UNSPECIFIED;
        }

        // TODO: if suggestedFolderNames is null then it infrastructure issue, not
        // ranking issue. We should log these appropriately.
        if (suggestedFolderNames == null || !suggestedFolderNames.hasSuggestions()) {
            return title.length() > 0
                    ? LauncherAtom.ToState.TO_CUSTOM_WITH_EMPTY_SUGGESTIONS
                    : LauncherAtom.ToState.TO_EMPTY_WITH_EMPTY_SUGGESTIONS;
        }

        boolean hasValidPrimary = suggestedFolderNames != null && suggestedFolderNames.hasPrimary();
        if (title.length() == 0) {
            return hasValidPrimary ? LauncherAtom.ToState.TO_EMPTY_WITH_VALID_PRIMARY
                    : LauncherAtom.ToState.TO_EMPTY_WITH_VALID_SUGGESTIONS_AND_EMPTY_PRIMARY;
        }

        OptionalInt accepted_suggestion_index = getAcceptedSuggestionIndex();
        if (!accepted_suggestion_index.isPresent()) {
            return hasValidPrimary ? LauncherAtom.ToState.TO_CUSTOM_WITH_VALID_PRIMARY
                    : LauncherAtom.ToState.TO_CUSTOM_WITH_VALID_SUGGESTIONS_AND_EMPTY_PRIMARY;
        }

        switch (accepted_suggestion_index.getAsInt()) {
            case 0:
                return LauncherAtom.ToState.TO_SUGGESTION0;
            case 1:
                return hasValidPrimary ? LauncherAtom.ToState.TO_SUGGESTION1_WITH_VALID_PRIMARY
                        : LauncherAtom.ToState.TO_SUGGESTION1_WITH_EMPTY_PRIMARY;
            case 2:
                return hasValidPrimary ? LauncherAtom.ToState.TO_SUGGESTION2_WITH_VALID_PRIMARY
                        : LauncherAtom.ToState.TO_SUGGESTION2_WITH_EMPTY_PRIMARY;
            case 3:
                return hasValidPrimary ? LauncherAtom.ToState.TO_SUGGESTION3_WITH_VALID_PRIMARY
                        : LauncherAtom.ToState.TO_SUGGESTION3_WITH_EMPTY_PRIMARY;
            default:
                // fall through
        }
        return LauncherAtom.ToState.TO_STATE_UNSPECIFIED;
    }

    public boolean useIconMode() {
        return isCoverMode();
    }

    /**
     * Checks if {@code itemType} is a type that can be placed in folders.
     */
    public static boolean willAcceptItemType(int itemType) {
        return itemType == ITEM_TYPE_APPLICATION
                || itemType == ITEM_TYPE_DEEP_SHORTCUT
                || itemType == ITEM_TYPE_APP_PAIR
                || itemType == LauncherSettings.Favorites.ITEM_TYPE_FOLDER;
    }

    public interface CoverModeListener {
        void onIconChanged();
    }

    private final ArrayList<CoverModeListener> mCoverListeners = new ArrayList<>();

    public void addCoverModeListener(CoverModeListener listener) {
        mCoverListeners.add(listener);
    }

    public void removeCoverModeListener(CoverModeListener listener) {
        mCoverListeners.remove(listener);
    }

    public void onIconChanged() {
        for (CoverModeListener l : mCoverListeners) l.onIconChanged();
    }
}
