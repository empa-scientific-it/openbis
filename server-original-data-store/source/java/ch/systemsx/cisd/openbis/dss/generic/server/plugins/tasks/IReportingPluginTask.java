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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Interface of the reporting plugin task. Implementations will be singletons serving all the requests, thus implementors of this interface must be
 * thread safe, as the methods may be invoked in multiple threads.
 * <p>
 * Users should check the result of getReportingPluginType to determine which methods on the interface they can invoke. Not all methods make sense for
 * all types.
 * 
 * @author Tomasz Pylak
 */
public interface IReportingPluginTask
{
    /**
     * Get the type of this reporting plugin. The type determines which of the interface methods are valid.
     */
    ReportingPluginType getReportingPluginType();

    /**
     * Creates a report for the specified datasets. This method should be safe for use in multiple threads.
     * <p>
     * Implemented by all ReportingPluginTypes.
     */
    TableModel createReport(List<DatasetDescription> datasets, DataSetProcessingContext context);

    /**
     * Returns a link that refers to a particular file within the data set.
     * <p>
     * Currently only implemented by the DSS_LINK type.
     */
    LinkModel createLink(DatasetDescription dataset);

    /**
     * Creates a report for the specified datasets. This method should be safe for use in multiple threads.
     * <p>
     * Implemented by all ReportingPluginTypes.
     */
    TableModel createAggregationReport(Map<String, Object> parameters, DataSetProcessingContext context);
}
