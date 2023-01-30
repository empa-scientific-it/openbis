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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Piotr Buczek
 */
public class ExperimentBatchUpdatesDTO extends ExperimentUpdatesDTO
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ExperimentIdentifier oldExperimentIdentifier;

    private ExperimentBatchUpdateDetails details;

    private boolean isProjectUpdateRequested;

    public ExperimentBatchUpdatesDTO(ExperimentIdentifier oldExperimentIdentifier,
            List<IEntityProperty> properties, ExperimentIdentifier experimentIdentifier,
            ExperimentBatchUpdateDetails details, boolean isProjectUpdateRequested)
    {

        this.oldExperimentIdentifier = oldExperimentIdentifier;
        setProperties(properties);
        setProjectIdentifier(experimentIdentifier);
        this.details = details;
        this.isProjectUpdateRequested = isProjectUpdateRequested;
    }

    public ExperimentIdentifier getOldExperimentIdentifier()
    {
        return oldExperimentIdentifier;
    }

    public ExperimentBatchUpdateDetails getDetails()
    {
        return details;
    }

    public boolean isProjectUpdateRequested()
    {
        return isProjectUpdateRequested;
    }

}
