/*
 * Copyright 2010 ETH Zuerich, CISD
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

/**
 * Different kinds of reporting plug-ins that are supported. Also keeps track of which IReportingPluginTask methods can be invoked on the different
 * types of plugin types.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public enum ReportingPluginType implements Serializable
{

    TABLE_MODEL(ImplementedPluginMethods.CREATE_REPORT),

    DSS_LINK(new ImplementedPluginMethods[]
    { ImplementedPluginMethods.CREATE_REPORT, ImplementedPluginMethods.CREATE_LINK }),

    AGGREGATION_TABLE_MODEL(ImplementedPluginMethods.CREATE_AGGREGATION_REPORT);

    /**
     * An enum representing the methods implemented by the ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask interface.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public enum ImplementedPluginMethods
    {
        CREATE_REPORT, CREATE_LINK, CREATE_AGGREGATION_REPORT
    }

    private final ImplementedPluginMethods[] implementedMethods;

    private ReportingPluginType(ImplementedPluginMethods implementedMethods)
    {
        this(new ImplementedPluginMethods[]
        { implementedMethods });
    }

    private ReportingPluginType(ImplementedPluginMethods[] implementedMethods)
    {
        this.implementedMethods = implementedMethods;
    }

    public ImplementedPluginMethods[] getImplementedMethods()
    {
        return implementedMethods;
    }
}
