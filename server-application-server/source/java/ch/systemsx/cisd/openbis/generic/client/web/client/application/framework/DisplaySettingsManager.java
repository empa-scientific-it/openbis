/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailViewConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PortletConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RealNumberFormatingParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.ColumnDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;

/**
 * Manager of {@link DisplaySettings}. The manager itself is stateless. It only changes the wrapped {@link DisplaySettings} object. The attributes of
 * this class are assumed to be de facto singletons. The display setting manager will be created after the user logs into application.
 * 
 * @author Franz-Josef Elmer
 */
public class DisplaySettingsManager
{

    private static final int QUITE_TIME_BEFORE_SETTINGS_SAVED_MS = 10000;

    private final DisplaySettings displaySettings;

    private final IDisplaySettingsDelayedUpdater updater;

    private final WebClientConfiguration webClientConfiguration;

    public interface IDisplaySettingsUpdater
    {
        void execute();

        void execute(IDisplaySettingsUpdate update);
    }

    /**
     * Private, we need this interface to make tests easier. We wrap {@link DelayedTask} which requires the access to the browser.
     */
    public interface IDisplaySettingsDelayedUpdater
    {
        /** Cancels any running timers and starts a new one. */
        void executeDelayed(int delayMs);

        void executeDelayed(IDisplaySettingsUpdate update, int delayMs);
    }

    public DisplaySettingsManager(DisplaySettings displaySettings,
            IDisplaySettingsUpdater settingsUpdater, IViewContext<?> viewContext)
    {
        this(displaySettings, createDelayedUpdater(settingsUpdater, viewContext), viewContext
                .getModel().getApplicationInfo().getWebClientConfiguration());
    }

    private static IDisplaySettingsDelayedUpdater createDelayedUpdater(
            final IDisplaySettingsUpdater settingsUpdater, IViewContext<?> viewContext)
    {
        if (viewContext.getModel().isDisplaySettingsSaving() == false)
        {
            return new IDisplaySettingsDelayedUpdater()
                {
                    // in simple view mode or anonymous login settings are temporary - don't
                    // save them at all

                    @Override
                    public void executeDelayed(int delayMs)
                    {
                    }

                    @Override
                    public void executeDelayed(IDisplaySettingsUpdate update, int delayMs)
                    {
                    }
                };
        } else
        {
            return new IDisplaySettingsDelayedUpdater()
                {
                    @Override
                    public void executeDelayed(int delayMs)
                    {
                        new DelayedTask(new Listener<BaseEvent>()
                            {
                                @Override
                                public void handleEvent(BaseEvent event)
                                {
                                    settingsUpdater.execute();
                                }
                            }).delay(delayMs);
                    }

                    @Override
                    public void executeDelayed(final IDisplaySettingsUpdate update,
                            final int delayMs)
                    {
                        new DelayedTask(new Listener<BaseEvent>()
                            {
                                @Override
                                public void handleEvent(BaseEvent event)
                                {
                                    settingsUpdater.execute(update);
                                }
                            }).delay(delayMs);
                    }
                };
        }
    }

    /**
     * Private, for tests only
     */
    public DisplaySettingsManager(DisplaySettings displaySettings,
            final IDisplaySettingsDelayedUpdater updater,
            WebClientConfiguration webClientConfiguration)
    {
        if (displaySettings == null)
        {
            throw new IllegalArgumentException("Unspecified display manager.");
        }
        this.displaySettings = displaySettings;
        this.webClientConfiguration = webClientConfiguration;
        this.updater = updater;
    }

    /**
     * Register listeners which monitors all the column configuration changes and makes them persistent.
     */
    public <C> void registerGridSettingsChangesListener(final String displayTypeID,
            final IDisplaySettingsGetter grid)
    {
        Listener<ColumnModelEvent> listener = new Listener<ColumnModelEvent>()
            {
                @Override
                public void handleEvent(ColumnModelEvent event)
                {
                    if (event.getType() == Events.ColumnMove)
                    {
                        // Update full column model from event triggered by change in trimmed model.
                        // In trimmed model there are only visible columns.
                        final int newIndexInTrimmedModel = event.getColIndex();
                        final ColumnModel trimmedModel = event.getColumnModel();
                        final ColumnConfig movedColumn =
                                trimmedModel.getColumn(newIndexInTrimmedModel);
                        updateColumnOrderInFullModel(movedColumn, newIndexInTrimmedModel);
                    } else if (isFakeWidthChangeEvent(event))
                    {
                        // When FAKE width change event is fired display settings are NOT updated.
                        // check: AbstractBrowserGrid.refreshColumnHeaderWidths()
                        return;
                    }
                    storeSettings(displayTypeID, grid, true);
                }

                private void updateColumnOrderInFullModel(final ColumnConfig movedColumn,
                        int newIndexInTrimmedModel)
                {
                    int oldIndexInFullModel = 0;
                    int newIndexInFullModel = 0;
                    int index = 0;
                    int visibleIndex = 0;
                    for (ColumnConfig c : grid.getColumnModel().getColumns())
                    {
                        if (c.equals(movedColumn))
                        {
                            oldIndexInFullModel = index;
                        }
                        if (c.isHidden() == false)
                        {
                            if (visibleIndex == newIndexInTrimmedModel)
                            {
                                newIndexInFullModel = index;
                            }
                            visibleIndex++;
                        }
                        index++;
                    }
                    if (oldIndexInFullModel < newIndexInFullModel)
                    {
                        // In this case the value in event.getColIndex() was decremented
                        // in ColumnModel.moveColumn(int, int) so we need to increment it.
                        newIndexInFullModel++;
                    }
                    // trigger move in full model
                    grid.getColumnModel().moveColumn(oldIndexInFullModel, newIndexInFullModel);
                }

                /**
                 * Is specified <code>event</code> a fake width change event that does not change width?
                 */
                private boolean isFakeWidthChangeEvent(ColumnModelEvent event)
                {
                    if (event.getType() == Events.WidthChange)
                    {
                        List<ColumnSetting> colSettings = getColumnSettings(displayTypeID);
                        if (colSettings != null && colSettings.get(event.getColIndex()) != null)
                        {
                            int oldWidth = colSettings.get(event.getColIndex()).getWidth();
                            int newWidth = event.getWidth();
                            return oldWidth == newWidth;
                        }
                    }

                    return false;
                }
            };
        ColumnModel columnModel = grid.getColumnModel();
        columnModel.addListener(Events.WidthChange, listener);
        // drag&drop is not added here but in AbstractBrowserGrid
    }

    /**
     * Synchronizes the initial grid display settings with the settings stored at the specified display type ID. Stored settings (if any) override the
     * current settings.
     */
    public GridDisplaySettings tryApplySettings(String displayTypeID, ColumnModel columnModel,
            List<String> filteredColumnIds, SortInfo sortInfo)
    {
        List<ColumnSetting> columnSettings = getColumnSettings(displayTypeID);
        if (columnSettings == null)
        {
            return null;
        }
        return tryApplySettings(columnSettings, columnModel, filteredColumnIds, sortInfo);
    }

    public static class GridDisplaySettings
    {
        private List<ColumnConfig> columnConfigs;

        private List<String> filteredColumnIds;

        private String sortField;

        private SortDir sortDir;

        public GridDisplaySettings(List<ColumnConfig> columnConfigs,
                List<String> filteredColumnIds, String sortField, SortDir sortDirection)
        {
            this.columnConfigs = columnConfigs;
            this.filteredColumnIds = filteredColumnIds;
            this.sortField = sortField;
            this.sortDir = sortDirection;
        }

        public List<ColumnConfig> getColumnConfigs()
        {
            return columnConfigs;
        }

        public void setColumnConfigs(List<ColumnConfig> columnConfigs)
        {
            this.columnConfigs = columnConfigs;
        }

        public List<String> getFilteredColumnIds()
        {
            return filteredColumnIds;
        }

        public void setFilteredColumnIds(List<String> filteredColumnIds)
        {
            this.filteredColumnIds = filteredColumnIds;
        }

        public String getSortField()
        {
            return sortField;
        }

        public void setSortField(String sortField)
        {
            this.sortField = sortField;
        }

        public SortDir getSortDir()
        {
            return sortDir;
        }

        public void setSortDir(SortDir sortDir)
        {
            this.sortDir = sortDir;
        }
    }

    /**
     * Update grid columns and filters by applying the specified settings.
     * 
     * @param filteredColumnIds used only to check if the user settings are different form the defaults
     */
    private static GridDisplaySettings tryApplySettings(List<ColumnSetting> columnSettings,
            ColumnModel columnModel, List<String> filteredColumnIds, SortInfo sortInfo)
    {
        boolean refreshNeeded = false;
        List<ColumnConfig> newColumnConfigList = new ArrayList<ColumnConfig>();
        Set<String> ids = new HashSet<String>();
        List<String> newFilteredColumnIds = new ArrayList<String>();

        String sortField =
                sortInfo == null ? null : sortInfo.getSortField() == null ? null : sortInfo
                        .getSortField();
        SortDir sortDirection = sortInfo == null ? null : sortInfo.getSortDir();

        for (int i = 0; i < columnSettings.size(); i++)
        {
            ColumnSetting columnSetting = columnSettings.get(i);
            // update column using the settings stored for it
            String columnID = columnSetting.getColumnID();
            SortDir settingSortDirection = columnSetting.getSortDir();
            if (settingSortDirection != null)
            {
                sortField = columnID;
                sortDirection = settingSortDirection;
            }
            ColumnConfig columnConfig = columnModel.getColumnById(columnID);
            if (columnConfig != null)
            {
                if (i != columnModel.getIndexById(columnID))
                {
                    refreshNeeded = true;
                }
                ids.add(columnID);
                boolean hidden = columnSetting.isHidden();
                if (columnConfig.isHidden() != hidden)
                {
                    columnConfig.setHidden(hidden);
                    refreshNeeded = true;
                }
                int width = columnSetting.getWidth();
                if (columnConfig.getWidth() != width)
                {
                    columnConfig.setWidth(width);
                    refreshNeeded = true;
                }
                newColumnConfigList.add(columnConfig);
                if (columnSetting.hasFilter())
                {
                    newFilteredColumnIds.add(columnID);
                }

                if (settingSortDirection != null)
                {
                    if (sortInfo == null || settingSortDirection != sortInfo.getSortDir()
                            || columnID.equals(sortInfo.getSortField()) == false)
                    {
                        refreshNeeded = true;
                    }
                }
            } else
            {
                // LMS-2711
                // For columns that are saved in the settings but are not found in the model
                // create a column with a ColumnConfig object basing on a ColumnSetting object.
                // Hide this column now but save the information whether it was originally hidden
                // and had a filter in a style name. This information will be used for storing the
                // settings back in the DB (see createColumnsSettings() method).

                ColumnStyle columnStyle = new ColumnStyle();
                columnStyle.setHidden(columnSetting.isHidden());
                columnStyle.setHasFilter(columnSetting.hasFilter());

                columnConfig = new ColumnConfig();
                columnConfig.setColumnStyleName(ColumnStyle.format(columnStyle));
                columnConfig.setId(columnSetting.getColumnID());
                columnConfig.setWidth(columnSetting.getWidth());
                columnConfig.setHidden(true);
                newColumnConfigList.add(columnConfig);
            }
        }
        // add columns for which no settings were stored at the end
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            ColumnConfig column = columnModel.getColumn(i);
            if (ids.contains(column.getId()) == false)
            {
                newColumnConfigList.add(column);
            }
        }
        if (newFilteredColumnIds.equals(filteredColumnIds) == false)
        {
            refreshNeeded = true;
        }
        if (refreshNeeded)
        {
            return new GridDisplaySettings(newColumnConfigList, newFilteredColumnIds, sortField,
                    sortDirection);
        } else if (sortDirection != null)
        {
            return new GridDisplaySettings(null, null, sortField, sortDirection);
        } else
        {
            return null;
        }
    }

    public void storeSettings(final String displayTypeID, final IDisplaySettingsGetter grid,
            boolean delayed)
    {
        int delayMs = delayed ? QUITE_TIME_BEFORE_SETTINGS_SAVED_MS : 1; // zero not allowed
        storeSettings(displayTypeID, grid.getColumnModel(), grid.getFilteredColumnIds(),
                grid.getModifier(), grid.getSortState(), delayMs);
    }

    public void storeActiveTabSettings(String tabGroupDisplayID, String selectedTabDisplayID,
            Object modifier)
    {
        updateActiveTabSettings(tabGroupDisplayID, selectedTabDisplayID, modifier);
        updater.executeDelayed(QUITE_TIME_BEFORE_SETTINGS_SAVED_MS);
    }

    private <C> void storeSettings(String displayTypeID, ColumnModel columnModel,
            List<String> filteredColumnIds, Object modifier, SortInfo sortInfo, int delayMs)
    {
        List<ColumnSetting> columnSettings =
                createColumnsSettings(columnModel, filteredColumnIds, sortInfo);
        updateColumnSettings(displayTypeID, columnSettings, modifier);
        updater.executeDelayed(new ColumnDisplaySettingsUpdate(displayTypeID, columnSettings),
                delayMs);
    }

    public void storeDropDownSettings(String dropDownSettingsID, String newValue)
    {
        updateDropDownSettings(dropDownSettingsID, newValue);
        updater.executeDelayed(QUITE_TIME_BEFORE_SETTINGS_SAVED_MS);
    }

    public void storeSettings()
    {
        updater.executeDelayed(1); // 0 not allowed
    }

    private static List<ColumnSetting> createColumnsSettings(ColumnModel columnModel,
            List<String> filteredColumnIdsList, SortInfo sortInfo)
    {
        Set<String> filteredColumnIds = new HashSet<String>(filteredColumnIdsList);
        List<ColumnSetting> columnSettings = new ArrayList<ColumnSetting>();
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            ColumnConfig columnConfig = columnModel.getColumn(i);
            ColumnSetting columnSetting = new ColumnSetting();
            columnSetting.setColumnID(columnConfig.getId());
            columnSetting.setWidth(columnConfig.getWidth());

            ColumnStyle columnStyle = ColumnStyle.parse(columnConfig.getColumnStyleName());
            if (columnStyle != null)
            {
                columnSetting.setHidden(columnStyle.isHidden());
                columnSetting.setHasFilter(columnStyle.isHasFilter());
            } else
            {
                columnSetting.setHidden(columnConfig.isHidden());
                columnSetting.setHasFilter(filteredColumnIds.contains(columnConfig.getId()));
            }

            if (sortInfo != null && sortInfo.getSortField() != null
                    && columnSetting.getColumnID().equals(sortInfo.getSortField()))
            {
                columnSetting.setSortDir(sortInfo.getSortDir());
            }
            columnSettings.add(columnSetting);
        }
        return columnSettings;
    }

    // delegator

    /** @deprecated Should be used only by specific display settings manager */
    @Deprecated
    public final Serializable tryGetTechnologySpecificSettings(String technologyName)
    {
        return displaySettings.getTechnologySpecificSettings().get(technologyName);
    }

    /** @deprecated Should be used only by specific display settings manager */
    @Deprecated
    public final void setTechnologySpecificSettings(String technologyName, Serializable newSettings)
    {
        displaySettings.getTechnologySpecificSettings().put(technologyName, newSettings);
    }

    /** @returns columns settings for given display id */
    @SuppressWarnings("deprecation")
    public final List<ColumnSetting> getColumnSettings(String gridDisplayTypeID)
    {
        return displaySettings.getColumnSettings().get(gridDisplayTypeID);
    }

    /** update column settings for given display id */
    @SuppressWarnings("deprecation")
    public final void updateColumnSettings(String gridDisplayTypeID,
            List<ColumnSetting> newSettings, Object modifier)
    {
        displaySettings.getColumnSettings().put(gridDisplayTypeID, newSettings);
    }

    /** @returns tab settings for given panel - which tab should be selected */
    @SuppressWarnings("deprecation")
    public final String getActiveTabSettings(String tabGroupDisplayTypeID)
    {
        return displaySettings.getTabSettings().get(tabGroupDisplayTypeID);
    }

    /**
     * @returns hidden tabs for given panel - which tab should be selected<br>
     * <br>
     *          NOTE: Returned value should be used read only
     */
    public final DetailViewConfiguration tryGetDetailViewSettings(String entityDetailViewID)
    {
        Map<String, DetailViewConfiguration> views = webClientConfiguration.getViews();
        for (Entry<String, DetailViewConfiguration> entry : views.entrySet())
        {
            String keyPattern = entry.getKey();
            if (entityDetailViewID.matches(keyPattern))
            {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * update section settings for given display id
     */
    @SuppressWarnings("deprecation")
    private final void updateActiveTabSettings(String tabGroupDisplayID,
            String selectedTabDisplayID, Object modifier)
    {
        displaySettings.getTabSettings().put(tabGroupDisplayID, selectedTabDisplayID);
    }

    // TODO 2010-09-27, Piotr Buczek: store is not invoked
    /**
     * @return True if the given section is collapsed
     */
    @SuppressWarnings("deprecation")
    public final Boolean tryGetPanelCollapsedSetting(String panelId)
    {
        return displaySettings.getPanelCollapsedSettings().get(panelId);
    }

    @SuppressWarnings("deprecation")
    public final void updatePanelCollapsedSetting(String panelId, Boolean value)
    {
        displaySettings.getPanelCollapsedSettings().put(panelId, value);
    }

    @SuppressWarnings("deprecation")
    public final Integer tryGetPanelSizeSetting(String panelId)
    {
        return displaySettings.getPanelSizeSettings().get(panelId);
    }

    @SuppressWarnings("deprecation")
    public final void updatePanelSizeSetting(String panelId, Integer value)
    {
        displaySettings.getPanelSizeSettings().put(panelId, value);
    }

    //

    @SuppressWarnings("deprecation")
    public final boolean isDebuggingModeEnabled()
    {
        return displaySettings.isDebuggingModeEnabled();
    }

    @SuppressWarnings("deprecation")
    public final void setDebuggingModeEnabled(boolean isDebugging)
    {
        displaySettings.setDebuggingModeEnabled(isDebugging);
    }

    @SuppressWarnings("deprecation")
    public final boolean isReopenLastTabOnLogin()
    {
        return displaySettings.isIgnoreLastHistoryToken() == false;
    }

    @SuppressWarnings("deprecation")
    public final void setReopenLastTabOnLogin(boolean isReopen)
    {
        displaySettings.setIgnoreLastHistoryToken(isReopen == false);
    }

    @SuppressWarnings("deprecation")
    public final String getDefaultProject()
    {
        return displaySettings.getDefaultProject();
    }

    @SuppressWarnings("deprecation")
    public final void setDefaultProject(String defaultProject)
    {
        displaySettings.setDefaultProject(defaultProject);
    }

    @SuppressWarnings("deprecation")
    public final boolean isLegacyMedadataUIEnabled()
    {
        return displaySettings.isLegacyMedadataUIEnabled();
    }

    @SuppressWarnings("deprecation")
    public final void setLegacyMedadataUIEnabled(boolean legacyMedadataUIEnabled)
    {
        displaySettings.setLegacyMedadataUIEnabled(legacyMedadataUIEnabled);
    }

    @SuppressWarnings("deprecation")
    public final RealNumberFormatingParameters getRealNumberFormatingParameters()
    {
        return displaySettings.getRealNumberFormatingParameters();
    }

    @SuppressWarnings("deprecation")
    public String getDropDownSettings(String dropDownID)
    {
        return displaySettings.getDropDownSettings().get(dropDownID);
    }

    @SuppressWarnings("deprecation")
    private void updateDropDownSettings(String dropDownSettingsID, String newValue)
    {
        displaySettings.getDropDownSettings().put(dropDownSettingsID, newValue);
    }

    @SuppressWarnings("deprecation")
    public void rememberVisit(EntityVisit visit)
    {
        displaySettings.addEntityVisit(visit);
    }

    @SuppressWarnings("deprecation")
    public List<EntityVisit> getVisits()
    {
        return displaySettings.getVisits();
    }

    @SuppressWarnings("deprecation")
    public void addPortlet(PortletConfiguration portletConfiguration)
    {
        displaySettings.addPortlet(portletConfiguration);
    }

    @SuppressWarnings("deprecation")
    public Map<String, PortletConfiguration> getPortletConfigurations()
    {
        return displaySettings.getPortletConfigurations();
    }

    private static class ColumnStyle
    {

        private static final String SEPARATOR = "&&";

        private boolean hidden;

        private boolean hasFilter;

        public boolean isHidden()
        {
            return hidden;
        }

        public void setHidden(boolean hidden)
        {
            this.hidden = hidden;
        }

        public boolean isHasFilter()
        {
            return hasFilter;
        }

        public void setHasFilter(boolean hasFilter)
        {
            this.hasFilter = hasFilter;
        }

        public static String format(ColumnStyle styleObject)
        {
            if (styleObject == null)
            {
                return null;
            }
            return styleObject.isHidden() + SEPARATOR + styleObject.isHasFilter();
        }

        public static ColumnStyle parse(String styleString)
        {
            if (styleString == null)
            {
                return null;
            }
            String[] parts = styleString.split(SEPARATOR);
            if (parts.length == 2)
            {
                ColumnStyle styleObject = new ColumnStyle();
                styleObject.setHidden(Boolean.valueOf(parts[0]));
                styleObject.setHasFilter(Boolean.valueOf(parts[1]));
                return styleObject;
            } else
            {
                return null;
            }
        }

    }

}
