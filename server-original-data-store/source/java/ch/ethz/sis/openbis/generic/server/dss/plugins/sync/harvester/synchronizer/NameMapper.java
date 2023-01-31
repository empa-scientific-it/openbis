/*
 * Copyright 2020 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

class NameMapper
{
    private final INameTranslator nameTranslator;

    private final Map<String, String> harvesterNameByDataSourceName = new HashMap<>();

    NameMapper(INameTranslator nameTranslator)
    {
        this.nameTranslator = nameTranslator;

    }

    String registerName(String originalDataSourceName, Boolean managedInternally, String registrator)
    {
        boolean internalNamespace = Boolean.TRUE.equals(managedInternally);
        String dataSourceName = CodeConverter.tryToBusinessLayer(originalDataSourceName, internalNamespace);
        String harvesterName = internalNamespace && "system".equals(registrator)
                ? dataSourceName
                : nameTranslator.translate(originalDataSourceName);
        String previous = harvesterNameByDataSourceName.put(dataSourceName, harvesterName);
        System.out.println(dataSourceName + "=" + harvesterName + " " + previous);
        if (previous != null)
        {
            throw new IllegalArgumentException("There is already a mapping defined for " + dataSourceName);
        }
        return originalDataSourceName;
    }

    String getHarvesterName(String originalDataSourceName)
    {
        String harvesterName = harvesterNameByDataSourceName.get(originalDataSourceName);
        if (harvesterName == null)
        {
            throw new IllegalArgumentException("No mapping found for " + originalDataSourceName + ".");
        }
        return harvesterName;
    }
}