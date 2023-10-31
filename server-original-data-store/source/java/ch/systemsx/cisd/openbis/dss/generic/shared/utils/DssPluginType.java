/*
 * Copyright ETH 2012 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.util.Properties;

import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.IPluginType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.PluginType;

public enum DssPluginType implements IPluginType
{
    DROP_BOXES("drop-boxes", Constants.INPUT_THREAD_NAMES),
    DATA_SOURCES("data-sources", Constants.DATA_SOURCES_KEY),
    SERVICES("services", Constants.PLUGIN_SERVICES_LIST_KEY),
    CUSTOM("custom-services", Constants.CUSTOM_SERVICES_LIST_KEY),
    IMAGE_OVERVIEW_PLUGINS("image-overview-plugins", Constants.OVERVIEW_PLUGINS_SERVICES_LIST_KEY),
    REPORTING_PLUGINS("reporting-plugins", Constants.REPORTING_PLUGIN_NAMES),
    PROCESSING_PLUGINS("processing-plugins", Constants.PROCESSING_PLUGIN_NAMES),
    SEARCH_DOMAIN_SERVICES("search-domain-services", Constants.SEARCH_DOMAIN_SERVICE_NAMES),
    MAINTENANCE_TASKS("maintenance-tasks", MaintenanceTaskUtils.DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME),
    DSS_FS_PLUGINS("file-system-plugins", Constants.DSS_FS_PLUGIN_NAMES),
    MISCELLANEOUS("miscellaneous", null);

    private PluginType pluginType;

    DssPluginType(String subFolderName, String keyOfKeyListPropertyOrNull)
    {
        pluginType = new PluginType(subFolderName, keyOfKeyListPropertyOrNull);
    }

    @Override
    public String getSubFolderName()
    {
        return pluginType.getSubFolderName();
    }

    @Override
    public String getKeyOfKeyListPropertyOrNull()
    {
        return pluginType.getKeyOfKeyListPropertyOrNull();
    }

    @Override
    public boolean isUniquePluginNameRequired()
    {
        return pluginType.isUniquePluginNameRequired();
    }

    @Override
    public String getPluginKey(String technology, String pluginFolderName, Properties properties)
    {
        return pluginFolderName;
    }

    @Override
    public String getPrefix()
    {
        return "";
    }

}