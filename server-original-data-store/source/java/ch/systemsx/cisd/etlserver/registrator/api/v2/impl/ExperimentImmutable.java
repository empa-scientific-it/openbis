/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.Date;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ExperimentImmutable implements IExperimentImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment;

    public ExperimentImmutable(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment)
    {
        this.experiment = experiment;
    }

    @Override
    public String getExperimentIdentifier()
    {
        String identifier = experiment.getIdentifier();
        return identifier == null ? null : identifier.toUpperCase();
    }

    public Long getId()
    {
        return experiment.getId();
    }

    @Override
    public IObjectId getEntityId()
    {
        return new ExperimentIdentifierId(getExperimentIdentifier());
    }

    @Override
    public boolean isExistingExperiment()
    {
        return true;
    }

    /**
     * Throw an exception if the sample does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingExperiment())
        {
            throw new UserFailureException("Experiment does not exist.");
        }
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment getExperiment()
    {
        return experiment;
    }

    @Override
    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(experiment, propertyCode);
    }

    @Override
    public String getExperimentType()
    {
        if (experiment.getExperimentType() != null)
        {
            return experiment.getExperimentType().getCode();
        }
        return null;
    }

    @Override
    public String getPermId()
    {
        return experiment.getPermId();
    }

    @Override
    public Date getRegistrationDate()
    {
        return experiment.getRegistrationDate();
    }

    @Override
    public Date getModificationDate()
    {
        return experiment.getModificationDate();
    }

    @Override
    public int hashCode()
    {
        return getExperimentIdentifier().hashCode();
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
        if (false == (obj instanceof ExperimentImmutable))
        {
            return false;
        }
        ExperimentImmutable other = (ExperimentImmutable) obj;
        if (getExperimentIdentifier() == null)
        {
            if (other.getExperimentIdentifier() != null)
            {
                return false;
            }
        } else if (getExperimentIdentifier().equals(
                other.getExperimentIdentifier()) == false)
        {
            return false;
        }
        return true;
    }
}
