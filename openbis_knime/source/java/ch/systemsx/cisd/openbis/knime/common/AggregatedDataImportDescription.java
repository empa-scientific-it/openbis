/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.AggregationServiceDescription;

/**
 * Wrapper of {@link AggregationServiceDescription}.
 *
 * @author Franz-Josef Elmer
 */
public class AggregatedDataImportDescription implements Serializable
{
    public static final String AGGREGATION_DESCRIPTION_KEY = "aggregation-description";
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Adds specified description to descriptions list if its service key starts with 'knime-'.
     */
    public static void addDescriptionIfDataTable(
            List<AggregatedDataImportDescription> descriptions,
            AggregationServiceDescription description)
    {
        addDescription(descriptions, description, "knime-");
    }

    /**
     * Adds specified description to descriptions list if its service key starts with 'knime-file-'.
     */
    public static void addDescriptionIfDataFile(List<AggregatedDataImportDescription> descriptions,
            AggregationServiceDescription description)
    {
        addDescription(descriptions, description, "knime-file-");
    }

    private static void addDescription(List<AggregatedDataImportDescription> descriptions,
            AggregationServiceDescription description, String keyPrefix)
    {
        String serviceKey = description.getServiceKey();
        if (serviceKey.startsWith(keyPrefix))
        {
            descriptions.add(new AggregatedDataImportDescription(description, keyPrefix));
        }
    }

    private final AggregationServiceDescription aggregationServiceDescription;

    private final String name;

    private AggregatedDataImportDescription(
            AggregationServiceDescription aggregationServiceDescription, String keyPrefix)
    {
        this.aggregationServiceDescription = aggregationServiceDescription;
        name = aggregationServiceDescription.getServiceKey().substring(keyPrefix.length());
    }
    
    public AggregationServiceDescription getAggregationServiceDescription()
    {
        return aggregationServiceDescription;
    }

    /**
     * Returns the service key with the prefix used for filtering.
     */
    @Override
    public String toString()
    {
        return name;
    }
    
    
}
