/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.reflection.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Criteria describing which samples should be listed.
 * 
 * @author Tomasz Pylak
 */
public class ListSampleCriteriaDTO extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    // list samples belonging to a given group or database instance
    private List<SampleOwnerIdentifier> ownerIdentifiers;

    // list samples of particular type
    private SampleTypePE sampleType;

    private TechId containerSampleId;

    private TechId experimentId;

    private boolean excludeWithoutExperiment;

    private String baseIndexUrl;

    private ListSampleCriteriaDTO()
    {
    }

    public final static ListSampleCriteriaDTO createContainerSampleId(final TechId containerSampleId)
    {
        ListSampleCriteriaDTO result = new ListSampleCriteriaDTO();
        result.containerSampleId = containerSampleId;
        return result;
    }

    public final static ListSampleCriteriaDTO createOwnerIdentifiers(
            final List<SampleOwnerIdentifier> ownerIdentifiers, final SampleTypePE sampleType)
    {
        ListSampleCriteriaDTO result = new ListSampleCriteriaDTO();
        result.ownerIdentifiers = ownerIdentifiers;
        result.sampleType = sampleType;
        return result;
    }

    public final static ListSampleCriteriaDTO createExperimentId(final TechId experimentId)
    {
        ListSampleCriteriaDTO result = new ListSampleCriteriaDTO();
        result.experimentId = experimentId;
        return result;
    }

    public final TechId getContainerSampleId()
    {
        return containerSampleId;
    }

    public List<SampleOwnerIdentifier> getOwnerIdentifiers()
    {
        return ownerIdentifiers;
    }

    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public TechId getExperimentId()
    {
        return experimentId;
    }

    public boolean isExcludeWithoutExperiment()
    {
        return excludeWithoutExperiment;
    }

    public void setExcludeWithoutExperiment(final boolean excludeWithoutExperiment)
    {
        this.excludeWithoutExperiment = excludeWithoutExperiment;
    }

    public String getBaseIndexUrl()
    {
        return baseIndexUrl;
    }

    public void setBaseIndexUrl(String baseIndexUrl)
    {
        this.baseIndexUrl = baseIndexUrl;
    }

}
