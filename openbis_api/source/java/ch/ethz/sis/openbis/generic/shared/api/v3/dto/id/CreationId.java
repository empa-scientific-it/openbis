/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.shared.api.v3.dto.id;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.IMaterialId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("CreationId")
public class CreationId implements ISampleId, IDataSetId, IExperimentId, IProjectId, ISpaceId, IMaterialId
{
    private static final long serialVersionUID = 1L;

    private String creationId;

    public CreationId(String creationId)
    {
        this.creationId = creationId;
    }

    public String getCreationId()
    {
        return this.creationId;
    }

    public void setCreationId(String creationId)
    {
        this.creationId = creationId;
    }

    @Override
    public String toString()
    {
        return getCreationId();
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private CreationId()
    {
    }

    @Override
    public int hashCode()
    {
        return ((getCreationId() == null) ? 0 : getCreationId().hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        CreationId other = (CreationId) obj;
        if (getCreationId() == null)
        {
            if (other.getCreationId() != null)
            {
                return false;
            }
        } else if (!getCreationId().equals(other.getCreationId()))
        {
            return false;
        }
        return true;
    }

}
