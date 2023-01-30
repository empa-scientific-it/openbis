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

import java.util.Collection;
import java.util.Collections;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * Extended {@link ListSampleCriteria} with 2 new filter options:
 * <ul>
 * <li>detailed sample search
 * <li>tracking new samples of particular type
 * </ul>
 * Additionally one can decide if dependent samples that are loaded (parents and containers) should be enriched with properties. By default only
 * 'primary' samples are enriched.<br>
 * <br>
 * NOTE: This bean is not serializable.
 * 
 * @author Piotr Buczek
 */
public final class ListOrSearchSampleCriteria extends ListSampleCriteria
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private ListSampleCriteria listCriteria;

    private TrackingSampleCriteria newTrackingCriteria;

    private MetaprojectCriteria metaprojectCriteria;

    private Collection<Long> sampleIds;

    private final String[] sampleCodes;

    private final String[] permIds;

    private final boolean searchForContainerSamplesOnly;

    private boolean enrichDependentSamplesWithProperties = false;

    /** Creates criteria that delegates to given {@link ListSampleCriteria}. */
    public ListOrSearchSampleCriteria(ListSampleCriteria listCriteria)
    {
        assert listCriteria != null;
        this.listCriteria = listCriteria;
        this.sampleCodes = null;
        this.permIds = null;
        this.searchForContainerSamplesOnly = false;
    }

    /** Creates criteria that delegates to given {@link TrackingSampleCriteria}. */
    public ListOrSearchSampleCriteria(TrackingSampleCriteria newTrackingCriteria)
    {
        assert newTrackingCriteria != null;
        this.newTrackingCriteria = newTrackingCriteria;
        this.sampleCodes = null;
        this.permIds = null;
        this.searchForContainerSamplesOnly = false;
    }

    /** Creates criteria that delegates to given {@link MetaprojectCriteria}. */
    public ListOrSearchSampleCriteria(MetaprojectCriteria metaprojectCriteria)
    {
        this.metaprojectCriteria = metaprojectCriteria;
        this.sampleCodes = null;
        this.permIds = null;
        this.searchForContainerSamplesOnly = false;
    }

    /** Creates criteria for detailed search of samples with given ids. */
    public ListOrSearchSampleCriteria(final Collection<Long> sampleIds)
    {
        assert sampleIds != null;
        this.sampleIds = sampleIds;
        this.sampleCodes = null;
        this.permIds = null;
        this.searchForContainerSamplesOnly = false;
    }

    /** Creates criteria for detailed search of samples with codes. */
    public ListOrSearchSampleCriteria(final String[] codes, final boolean codesArePermIds)
    {
        this(codes, codesArePermIds, false);
    }

    /** Creates criteria for detailed search of samples with codes. */
    public ListOrSearchSampleCriteria(String[] codes, boolean codesArePermIds,
            boolean searchForContainerSamplesOnly)
    {
        // Need to add the type to disambiguate method signatures for erased generic types.
        assert codes != null;
        this.searchForContainerSamplesOnly = searchForContainerSamplesOnly;

        if (codesArePermIds)
        {
            this.sampleCodes = null;
            this.permIds = codes;
        } else
        {
            this.sampleCodes = codes;
            this.permIds = null;
        }
    }

    /** Creates criteria for detailed search of samples with codes. */
    public ListOrSearchSampleCriteria(final String[] codes, final String[] permIds)
    {
        // Need to add the type to disambiguate method signatures for erased generic types.
        assert codes != null;
        assert permIds != null;

        this.sampleCodes = codes;
        this.permIds = permIds;
        this.searchForContainerSamplesOnly = false;
    }

    // search

    public Collection<Long> getSampleIds()
    {
        return sampleIds;
    }

    public String[] trySampleCodes()
    {
        return sampleCodes;
    }

    public String[] trySamplePermIds()
    {
        return permIds;
    }

    // delegation to NewTrackingSampleCriteria

    public String getSampleTypeCode()
    {
        return newTrackingCriteria == null ? null : newTrackingCriteria.getSampleTypeCode();
    }

    public String getPropertyTypeCode()
    {
        return newTrackingCriteria == null ? null : newTrackingCriteria.getPropertyTypeCode();
    }

    public String getPropertyValue()
    {
        return newTrackingCriteria == null ? null : newTrackingCriteria.getPropertyValue();
    }

    public Collection<Long> getAlreadyTrackedSampleIds()
    {
        return newTrackingCriteria == null ? null : newTrackingCriteria
                .getAlreadyTrackedSampleIds();
    }

    // delegation to MetaprojectCriteria

    public Long getMetaprojectId()
    {
        return metaprojectCriteria == null ? null : metaprojectCriteria.getMetaprojectId();
    }

    // delegation to ListSampleCriteria

    @Override
    public Collection<Long> getContainerSampleIds()
    {
        return listCriteria == null ? null : listCriteria.getContainerSampleIds();
    }

    @Override
    public TechId getParentSampleId()
    {
        return listCriteria == null ? null : listCriteria.getParentSampleId();
    }

    @Override
    public TechId getChildSampleId()
    {
        return listCriteria == null ? null : listCriteria.getChildSampleId();
    }

    @Override
    public boolean isOnlyDirectlyConnected()
    {
        return listCriteria == null ? super.isOnlyDirectlyConnected() : listCriteria
                .isOnlyDirectlyConnected();
    }

    @Override
    public Collection<Long> getChildrenSampleIds()
    {
        if (listCriteria == null)
        {
            return Collections.<Long> emptySet();
        }
        Collection<Long> childrenSampleIds = listCriteria.getChildrenSampleIds();
        if (childrenSampleIds != null)
        {
            return childrenSampleIds;
        }
        TechId childSampleId = listCriteria.getChildSampleId();
        if (childSampleId == null)
        {
            return Collections.<Long> emptySet();
        }
        return Collections.<Long> singleton(childSampleId.getId());
    }

    @Override
    public TechId getExperimentId()
    {
        return listCriteria == null ? null : listCriteria.getExperimentId();
    }

    @Override
    public String getSpaceCode()
    {
        return listCriteria == null ? null : listCriteria.getSpaceCode();
    }

    @Override
    public SampleType getSampleType()
    {
        return listCriteria == null ? null : listCriteria.getSampleType();
    }

    @Override
    public boolean isExcludeWithoutExperiment()
    {
        return listCriteria == null ? false : listCriteria.isExcludeWithoutExperiment();
    }

    @Override
    public boolean isIncludeSpace()
    {
        return listCriteria == null ? false : listCriteria.isIncludeSpace();
    }

    @Override
    public boolean isIncludeInstance()
    {
        return listCriteria == null ? false : listCriteria.isIncludeInstance();
    }

    public boolean isEnrichDependentSamplesWithProperties()
    {
        return enrichDependentSamplesWithProperties;
    }

    public void setEnrichDependentSamplesWithProperties(boolean enrichDependentSamplesWithProperties)
    {
        this.enrichDependentSamplesWithProperties = enrichDependentSamplesWithProperties;
    }

    public boolean isSearchForContainerSamplesOnly()
    {
        return searchForContainerSamplesOnly;
    }
}
