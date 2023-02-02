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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Points to an experiment.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentReference implements Serializable, IEntityInformationHolderWithIdentifier
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private long experimentId;

    private String experimentPermId;

    private String experimentCode;

    private String experimentTypeCode;

    private String projectCode;

    private String spaceCode;

    // GWT only
    @SuppressWarnings("unused")
    private ExperimentReference()
    {
    }

    public ExperimentReference(long experimentId, String experimentPermId, String experimentCode,
            String experimentTypeCode, String projectCode, String spaceCode)
    {
        this.experimentId = experimentId;
        this.experimentPermId = experimentPermId;
        this.experimentCode = experimentCode;
        this.experimentTypeCode = experimentTypeCode;
        this.projectCode = projectCode;
        this.spaceCode = spaceCode;
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    public BasicEntityType getEntityType()
    {
        return new BasicEntityType(experimentTypeCode);
    }

    @Override
    public Long getId()
    {
        return experimentId;
    }

    @Override
    public String getCode()
    {
        return experimentCode;
    }

    /** @return experiment identifier */
    @Override
    public String getIdentifier()
    {
        String SEP = "/";
        return SEP + spaceCode + SEP + projectCode + SEP + experimentCode;
    }

    @Override
    public String getPermId()
    {
        return experimentPermId;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }

    @Override
    public String toString()
    {
        return getIdentifier();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (experimentId ^ (experimentId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExperimentReference other = (ExperimentReference) obj;
        if (experimentId != other.experimentId)
            return false;
        return true;
    }

}
