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
import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Criteria for listing <i>samples</i>. This class offers 3 kinds of filters, but only one can be used at the same time:
 * <ol>
 * <li>samples of particular type in a specified space and/or shared
 * <li>samples belonging to a container sample
 * <li>samples derived from a parent sample
 * <li>samples connected with a child sample
 * <li>samples from the experiment
 * </ol>
 * 
 * @author Izabela Adamczyk
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class ListSampleCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // --------- filter 1 fields
    private SampleType sampleType;

    private String spaceCode;

    private boolean includeSpace;

    private boolean includeInstance;

    private boolean excludeWithoutExperiment;

    // --------- filter 2 fields
    private Collection<Long> containerSampleIds;

    // --------- filter 3 fields
    private TechId parentSampleId;

    // --------- filter 4 fields
    private TechId childSampleId;

    // --------- filter 5 fields
    private TechId experimentId;

    private boolean onlyDirectlyConnected = true;

    // --------- filter 6 fields
    private Collection<Long> childrenSampleIds;

    // ----

    public static ListSampleCriteria createForContainer(final TechId containerSampleId)
    {
        return createForContainers(Arrays.asList(containerSampleId.getId()));
    }

    public static ListSampleCriteria createForContainers(final Collection<Long> containerSampleIds)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setContainerIds(containerSampleIds);
        return criteria;
    }

    public static ListSampleCriteria createForParent(final TechId parentSampleId)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setParentId(parentSampleId);
        return criteria;
    }

    public static ListSampleCriteria createForChild(final TechId childSampleId)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setChildId(childSampleId);
        return criteria;
    }

    public static ListSampleCriteria createForChildren(final Collection<Long> childrenSampleIds)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setChildrenSampleIds(childrenSampleIds);
        return criteria;
    }

    public static ListSampleCriteria createForExperiment(final TechId experimentId)
    {
        final ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setExperimentId(experimentId);
        return criteria;
    }

    public Collection<Long> getContainerSampleIds()
    {
        return containerSampleIds;
    }

    private final void setContainerIds(final Collection<Long> containerSampleIds)
    {
        this.containerSampleIds = containerSampleIds;
    }

    public TechId getParentSampleId()
    {
        return parentSampleId;
    }

    private final void setParentId(final TechId parentSampleId)
    {
        this.parentSampleId = parentSampleId;
    }

    public TechId getChildSampleId()
    {
        return childSampleId;
    }

    private final void setChildId(final TechId childSampleId)
    {
        this.childSampleId = childSampleId;
    }

    public Collection<Long> getChildrenSampleIds()
    {
        return childrenSampleIds;
    }

    public final void setChildrenSampleIds(Collection<Long> childrenSampleIds)
    {
        this.childrenSampleIds = childrenSampleIds;
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

    public boolean isOnlyDirectlyConnected()
    {
        return onlyDirectlyConnected;
    }

    public void setOnlyDirectlyConnected(boolean onlyDirectlyConnected)
    {
        this.onlyDirectlyConnected = onlyDirectlyConnected;
    }

}
