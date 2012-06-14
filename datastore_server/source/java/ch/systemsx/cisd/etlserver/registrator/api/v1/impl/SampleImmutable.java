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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleImmutable implements ISampleImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample;

    private final boolean existingSample;

    public SampleImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample)
    {
        this(sample, true);
    }

    public SampleImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample,
            boolean existingSample)
    {
        this.sample = sample;
        this.existingSample = existingSample;
    }

    @Override
    public IExperimentImmutable getExperiment()
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment =
                sample.getExperiment();
        return (null != experiment) ? new ExperimentImmutable(experiment) : null;
    }

    @Override
    public String getSampleIdentifier()
    {
        String identifier = sample.getIdentifier();
        return identifier == null ? null : identifier.toUpperCase();
    }

    public ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample getSample()
    {
        return sample;
    }

    @Override
    public boolean isExistingSample()
    {
        return existingSample;
    }

    /**
     * Throw an exception if the sample does not exist
     */
    protected void checkExists()
    {
        if (false == isExistingSample())
        {
            throw new UserFailureException("Sample does not exist.");
        }
    }

    @Override
    public String getPropertyValue(String propertyCode)
    {
        return EntityHelper.tryFindPropertyValue(sample, propertyCode);
    }

    @Override
    public String getSampleType()
    {
        if (sample.getSampleType() != null)
        {
            return sample.getSampleType().getCode();
        }
        return null;
    }

    @Override
    public String getSpace()
    {
        return sample.getSpace().getCode();
    }

    @Override
    public String getCode()
    {
        return sample.getCode();
    }

    @Override
    public String getPermId()
    {
        return sample.getPermId();
    }

    @Override
    public List<ISampleImmutable> getContainedSamples()
    {
        List<ISampleImmutable> result = new ArrayList<ISampleImmutable>();
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> containedSamples =
                sample.tryGetContainedSamples();
        if (containedSamples != null)
        {
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample contained : containedSamples)
            {
                result.add(new SampleImmutable(contained));
            }
        }
        return result;
    }

    @Override
    public List<String> getParentSampleIdentifiers()
    {
        ArrayList<String> parentIdentifiers = new ArrayList<String>();
        Set<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> sampleParents =
                sample.getParents();
        for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample parent : sampleParents)
        {
            parentIdentifiers.add(parent.getIdentifier());
        }

        return parentIdentifiers;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((getSampleIdentifier() == null) ? 0 : getSampleIdentifier().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass().isAssignableFrom(obj.getClass()) == false)
            return false;
        SampleImmutable other = (SampleImmutable) obj;
        if (getSampleIdentifier() == null)
        {
            if (other.getSampleIdentifier() != null)
                return false;
        } else if (!getSampleIdentifier().equals(other.getSampleIdentifier()))
            return false;
        return true;
    }

}
