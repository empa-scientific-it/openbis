/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.search.PhysicalDataSearchCriteria")
public class PhysicalDataSearchCriteria extends AbstractCompositeSearchCriteria
{

    private static final long serialVersionUID = 1L;

    public ShareIdSearchCriteria withShareId()
    {
        return with(new ShareIdSearchCriteria());
    }

    public LocationSearchCriteria withLocation()
    {
        return with(new LocationSearchCriteria());
    }

    public SizeSearchCriteria withSize()
    {
        return with(new SizeSearchCriteria());
    }

    public StorageFormatSearchCriteria withStorageFormat()
    {
        return with(new StorageFormatSearchCriteria());
    }

    @Deprecated
    public FileFormatTypeSearchCriteria withFileFormatType()
    {
        return with(new FileFormatTypeSearchCriteria());
    }

    public LocatorTypeSearchCriteria withLocatorType()
    {
        return with(new LocatorTypeSearchCriteria());
    }

    public CompleteSearchCriteria withComplete()
    {
        return with(new CompleteSearchCriteria());
    }

    public StatusSearchCriteria withStatus()
    {
        return with(new StatusSearchCriteria());
    }

    public ArchivingRequestedSearchCriteria withArchivingRequested()
    {
        return with(new ArchivingRequestedSearchCriteria());
    }

    public PresentInArchiveSearchCriteria withPresentInArchive()
    {
        return with(new PresentInArchiveSearchCriteria());
    }

    public StorageConfirmationSearchCriteria withStorageConfirmation()
    {
        return with(new StorageConfirmationSearchCriteria());
    }

    public SpeedHintSearchCriteria withSpeedHint()
    {
        return with(new SpeedHintSearchCriteria());
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("PHYSICAL_DATA");
        return builder;
    }

}
