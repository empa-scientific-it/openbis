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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class storing personalized display settings. This class implements {@link Serializable} not only for transferring it's content remotely but also to
 * store it in the database. Thus, CHANGES IN THIS CLASS MIGHT LEAD TO A LOST OF PERSONAL SETTINGS. In all cases deserialization leads to an exception
 * the default settings is used.
 * <p>
 * NOTE: This class has to be Java serializable and GWT serializable.
 * <p>
 * NOTE: Object of this class should be always managed by DisplaySettingsManager on client side.
 * 
 * @author Franz-Josef Elmer
 */
public class DisplaySettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    //
    // NOTE: if you add new fields to this class, you also need to add it to the copy constructor!
    //

    private Map<String, List<ColumnSetting>> columnSettings =
            new HashMap<String, List<ColumnSetting>>();

    private Map<String, Serializable> technologySpecificSettings =
            new HashMap<String, Serializable>();

    private Map<String, String> tabSettings = new HashMap<String, String>();

    private Map<String, String> dropDownSettings = new HashMap<String, String>();

    // When debugging mode is on more information will be shown in UI:
    // - error messages for custom columns or managed properties will contain technical details
    // - raw output and input for managed properties is visible
    // For new users with clean display settings the debugging mode is disabled
    private boolean debugging = false;

    private String lastHistoryTokenOrNull;

    private boolean ignoreLastHistoryToken = false;

    private boolean legacyMedadataUIEnabled = false;

    private String defaultProject;

    private RealNumberFormatingParameters realNumberFormatingParameters =
            new RealNumberFormatingParameters();

    private Map<String, Boolean> panelCollapsedSettings = new HashMap<String, Boolean>();

    private Map<String, Integer> panelSizeSettings = new HashMap<String, Integer>();

    private List<EntityVisit> visits;

    private Map<String, PortletConfiguration> portletConfigurations;

    private Map<String, Map<String, String>> customWebAppDisplaySettings;

    public DisplaySettings()
    {
    }

    /**
     * Copy constructor (shallow copy).
     */
    public DisplaySettings(DisplaySettings other)
    {
        this.columnSettings = other.columnSettings;
        this.technologySpecificSettings = other.technologySpecificSettings;
        this.tabSettings = other.tabSettings;
        this.dropDownSettings = other.dropDownSettings;
        this.debugging = other.debugging;
        this.lastHistoryTokenOrNull = other.lastHistoryTokenOrNull;
        this.ignoreLastHistoryToken = other.ignoreLastHistoryToken;
        this.realNumberFormatingParameters = other.realNumberFormatingParameters;
        this.panelCollapsedSettings = other.panelCollapsedSettings;
        this.panelSizeSettings = other.panelSizeSettings;
        this.visits = other.visits;
        this.portletConfigurations = other.portletConfigurations;
        this.customWebAppDisplaySettings = other.customWebAppDisplaySettings;
        this.legacyMedadataUIEnabled = other.legacyMedadataUIEnabled;
        this.defaultProject = other.defaultProject;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, Serializable> getTechnologySpecificSettings()
    {
        if (technologySpecificSettings == null)
        {
            technologySpecificSettings = new HashMap<String, Serializable>();
        }
        return technologySpecificSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public final Map<String, List<ColumnSetting>> getColumnSettings()
    {
        if (columnSettings == null)
        {
            columnSettings = new HashMap<String, List<ColumnSetting>>();
        }
        return columnSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, String> getTabSettings()
    {
        if (tabSettings == null)
        {
            tabSettings = new HashMap<String, String>();
        }
        return tabSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, Boolean> getPanelCollapsedSettings()
    {
        if (panelCollapsedSettings == null)
        {
            panelCollapsedSettings = new HashMap<String, Boolean>();
        }
        return panelCollapsedSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, Integer> getPanelSizeSettings()
    {
        if (panelSizeSettings == null)
        {
            panelSizeSettings = new HashMap<String, Integer>();
        }
        return panelSizeSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, String> getDropDownSettings()
    {
        if (dropDownSettings == null)
        {
            dropDownSettings = new HashMap<String, String>();
        }
        return dropDownSettings;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public RealNumberFormatingParameters getRealNumberFormatingParameters()
    {
        if (realNumberFormatingParameters == null)
        {
            realNumberFormatingParameters = new RealNumberFormatingParameters();
        }
        return realNumberFormatingParameters;
    }

    // for serialization

    @SuppressWarnings("unused")
    private void setTechnologySpecificSettings(Map<String, Serializable> technologySpecificSettings)
    {
        this.technologySpecificSettings = technologySpecificSettings;
    }

    @SuppressWarnings("unused")
    private final void setColumnSettings(Map<String, List<ColumnSetting>> columnSettings)
    {
        this.columnSettings = columnSettings;
    }

    @SuppressWarnings("unused")
    private final void setPanelCollapsedSettings(Map<String, Boolean> panelCollapsedSettings)
    {
        this.panelCollapsedSettings = panelCollapsedSettings;
    }

    @SuppressWarnings("unused")
    private final void setPanelSizeSettings(Map<String, Integer> panelSizeSettings)
    {
        this.panelSizeSettings = panelSizeSettings;
    }

    @SuppressWarnings("unused")
    private final void setTabSettings(Map<String, String> tabSettings)
    {
        this.tabSettings = tabSettings;
    }

    @SuppressWarnings("unused")
    private final void setDropDownSettings(Map<String, String> dropDownSettings)
    {
        this.dropDownSettings = dropDownSettings;
    }

    @SuppressWarnings("unused")
    private void setRealNumberFormatingParameters(
            RealNumberFormatingParameters realNumberFormatingParameters)
    {
        this.realNumberFormatingParameters = realNumberFormatingParameters;
    }

    /**
     * Are error messages in debugging format or user format?
     * 
     * @deprecated Should be used only by DisplaySettingsManager.
     */
    @Deprecated
    public boolean isDebuggingModeEnabled()
    {
        return debugging;
    }

    /**
     * Are error messages in debugging format or user format?
     * 
     * @deprecated Should be used only by DisplaySettingsManager.
     */
    @Deprecated
    public void setDebuggingModeEnabled(boolean isDebugging)
    {
        debugging = isDebugging;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public String getLastHistoryTokenOrNull()
    {
        return lastHistoryTokenOrNull;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public void setLastHistoryTokenOrNull(String lastHistoryOrNull)
    {
        this.lastHistoryTokenOrNull = lastHistoryOrNull;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public boolean isIgnoreLastHistoryToken()
    {
        return ignoreLastHistoryToken;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    public String getDefaultProject()
    {
        return defaultProject;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public void setDefaultProject(String defaultProject)
    {
        this.defaultProject = defaultProject;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public boolean isLegacyMedadataUIEnabled()
    {
        return legacyMedadataUIEnabled;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public void setLegacyMedadataUIEnabled(boolean legacyMedadataUIEnabled)
    {
        this.legacyMedadataUIEnabled = legacyMedadataUIEnabled;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public void setIgnoreLastHistoryToken(boolean ignoreLastHistoryToken)
    {
        this.ignoreLastHistoryToken = ignoreLastHistoryToken;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public void addEntityVisit(EntityVisit entityVisit)
    {
        getVisits().add(entityVisit);
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public List<EntityVisit> getVisits()
    {
        if (visits == null)
        {
            visits = new ArrayList<EntityVisit>();
        }
        return visits;
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public void addPortlet(PortletConfiguration portletConfiguration)
    {
        addPortlet(getPortletConfigurations(), portletConfiguration);
    }

    /** @deprecated Should be used only by DisplaySettingsManager. */
    @Deprecated
    public Map<String, PortletConfiguration> getPortletConfigurations()
    {
        if (portletConfigurations == null)
        {
            portletConfigurations = new HashMap<String, PortletConfiguration>();
            // Default portlets
            addPortlet(portletConfigurations,
                    new PortletConfiguration(StandardPortletNames.WELCOME));
            addPortlet(portletConfigurations,
                    new PortletConfiguration(StandardPortletNames.HISTORY));
        }
        return portletConfigurations;
    }

    private void addPortlet(Map<String, PortletConfiguration> configurations,
            PortletConfiguration portletConfiguration)
    {
        configurations.put(portletConfiguration.getName(), portletConfiguration);
    }

    public Collection<String> getCustomWebAppIds()
    {
        if (customWebAppDisplaySettings != null)
        {
            return Collections.unmodifiableSet(customWebAppDisplaySettings.keySet());
        } else
        {
            return Collections.emptySet();
        }
    }

    public void removeCustomWebAppSettings(String webAppId)
    {
        if (customWebAppDisplaySettings != null)
        {
            customWebAppDisplaySettings.remove(webAppId);
        }
    }

    /**
     * @deprecated Don't use in generic web client - will be overwritten.
     */
    @Deprecated
    public Map<String, String> getCustomWebAppSettings(String webAppId)
    {
        if (customWebAppDisplaySettings == null)
        {
            customWebAppDisplaySettings = new HashMap<String, Map<String, String>>();
        }
        Map<String, String> settings = customWebAppDisplaySettings.get(webAppId);
        if (settings == null)
        {
            settings = new HashMap<String, String>();
            customWebAppDisplaySettings.put(webAppId, settings);
        }
        return settings;
    }

    /**
     * @deprecated Don't use in generic web client - will be overwritten.
     */
    @Deprecated
    public void setCustomWebAppSettings(String webAppId, Map<String, String> settings)
    {
        if (customWebAppDisplaySettings == null)
        {
            customWebAppDisplaySettings = new HashMap<String, Map<String, String>>();
        }
        customWebAppDisplaySettings.put(webAppId, settings);
    }

    /**
     * @deprecated Don't use in generic web client - will be overwritten.
     */
    @Deprecated
    public void overwriteCustomWebAppSettings(DisplaySettings customDisplaySettings)
    {
        synchronized (customDisplaySettings)
        {
            this.customWebAppDisplaySettings = customDisplaySettings.customWebAppDisplaySettings;
        }
    }

    /**
     * @deprecated Don't use in generic web client - will be overwritten.
     */
    @Deprecated
    public void overwriteColumnSettings(DisplaySettings customDisplaySettings)
    {
        synchronized (customDisplaySettings)
        {
            this.columnSettings = customDisplaySettings.columnSettings;
        }
    }

    @Deprecated
    public void clearCustomWebAppSettings()
    {
        customWebAppDisplaySettings = null;
    }

}
