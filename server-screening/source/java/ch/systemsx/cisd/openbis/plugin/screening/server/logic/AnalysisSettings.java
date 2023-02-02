/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Helper class which knows data set types which assume that the data set has the analysis summary as a CSV/TSV file. The file is provided by a
 * reporting plugin. All information comes from the property {@value #KEY} of the AS service.properties which has the form
 * 
 * <pre>
 * &lt;data set type code 1&gt;:&lt;reporting plugin key 1&gt;, &lt;data set type code 2&gt;:&lt;reporting plugin key 2&gt;,  ...
 * </pre>
 * 
 * @author Franz-Josef Elmer
 */
public class AnalysisSettings
{
    public static final String KEY = "data-set-types-with-available-analysis-summary";

    private final Map<String, String> dataSetType2reportingPluginMap = new HashMap<String, String>();

    public AnalysisSettings(Properties properties)
    {
        String property = properties.getProperty(KEY);
        if (property != null)
        {
            String[] splittedProperty = property.split(",");
            for (String dataSetTypeSetting : splittedProperty)
            {
                dataSetTypeSetting = dataSetTypeSetting.trim();
                int indexOfColon = dataSetTypeSetting.indexOf(':');
                if (indexOfColon < 0)
                {
                    throw new ConfigurationFailureException("Invalid property '" + KEY
                            + "': missing ':' in '" + dataSetTypeSetting + "'.");
                }
                String dataSetTypeCode = dataSetTypeSetting.substring(0, indexOfColon);
                String reportingPluginKey = dataSetTypeSetting.substring(indexOfColon + 1);
                dataSetType2reportingPluginMap.put(dataSetTypeCode, reportingPluginKey);
            }
        }
    }

    /**
     * Returns <code>true</code> if there are no data set types with available analysis summary.
     */
    public boolean noAnalysisSettings()
    {
        return dataSetType2reportingPluginMap.isEmpty();
    }

    /**
     * Returns the reporting plugin key for the specified data set or <code>null</code> if for the data set type of the specified data set no
     * reporting plugin providing an analysis summary has been configured.
     */
    public String tryToGetReportingPluginKey(AbstractExternalData ds)
    {
        return dataSetType2reportingPluginMap.get(ds.getDataSetType().getCode());
    }

}