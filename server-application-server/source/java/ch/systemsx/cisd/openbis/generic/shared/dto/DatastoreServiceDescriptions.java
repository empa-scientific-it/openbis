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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * @author Tomasz Pylak
 */
public class DatastoreServiceDescriptions implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private final List<DatastoreServiceDescription> reportingServiceDescriptions;

    private final List<DatastoreServiceDescription> processingServiceDescriptions;

    public DatastoreServiceDescriptions(
            List<DatastoreServiceDescription> reportingServiceDescriptions,
            List<DatastoreServiceDescription> processingServiceDescriptions)
    {
        this.reportingServiceDescriptions = reportingServiceDescriptions;
        this.processingServiceDescriptions = processingServiceDescriptions;
    }

    public List<DatastoreServiceDescription> getReportingServiceDescriptions()
    {
        return reportingServiceDescriptions;
    }

    public List<DatastoreServiceDescription> getProcessingServiceDescriptions()
    {
        return processingServiceDescriptions;
    }

}
