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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Criteria for listing <i>samples</i>. This class offers 3 kinds of filters, but only one can be
 * used at the same time:
 * <ol>
 * <li>samples of particular type in a specified space and/or shared
 * <li>samples belonging to a container sample
 * <li>samples derived from a parent sample
 * <li>samples from the experiment
 * </ol>
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class ListSampleCriteria implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // --------- filter 1 fields
    private SampleType sampleType;

    private String spaceCode;

    private boolean includeSpace;

    private boolean includeInstance;

    private boolean excludeWithoutExperiment;

    // --------- filter 2 fields
    private TechId containerSampleId;

    // --------- filter 3 fields
    private TechId parentSampleId;

    // --------- filter 4 fields
    private TechId experimentId;

    // ----

    public static ListSampleCriteria createForContainer(final TechId containerSampleId)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setContainerId(containerSampleId);
        return criteria;
    }

    public static ListSampleCriteria createForParent(final TechId parentSampleId)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setParentId(parentSampleId);
        return criteria;
    }

    public static ListSampleCriteria createForExperiment(final TechId experimentId)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setExperimentId(experimentId);
        return criteria;
    }

    public TechId getContainerSampleId()
    {
        return containerSampleId;
    }

    private final void setContainerId(final TechId containerSampleId)
    {
        this.containerSampleId = containerSampleId;
    }

    public TechId getParentSampleId()
    {
        return parentSampleId;
    }

    private final void setParentId(final TechId parentSampleId)
    {
        this.parentSampleId = parentSampleId;
    }

    public SampleType getSampleType()
    {
        return sampleType;
    }

    public void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }

    public void setSpaceCode(final String spaceCode)
    {
        this.spaceCode = spaceCode;
    }

    public boolean isIncludeSpace()
    {
        return includeSpace;
    }

    public void setIncludeSpace(final boolean includeSpace)
    {
        this.includeSpace = includeSpace;
    }

    public boolean isIncludeInstance()
    {
        return includeInstance;
    }

    public void setIncludeInstance(final boolean includeInstance)
    {
        this.includeInstance = includeInstance;
    }

    public boolean isExcludeWithoutExperiment()
    {
        return excludeWithoutExperiment;
    }

    public void setExcludeWithoutExperiment(final boolean excludeWithoutExperiment)
    {
        this.excludeWithoutExperiment = excludeWithoutExperiment;
    }

    public TechId getExperimentId()
    {
        return experimentId;
    }

    private void setExperimentId(final TechId experimentId)
    {
        this.experimentId = experimentId;
    }

}
