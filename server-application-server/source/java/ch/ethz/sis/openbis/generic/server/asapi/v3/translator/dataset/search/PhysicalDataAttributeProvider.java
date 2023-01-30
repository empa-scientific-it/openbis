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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ArchivingRequestedSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.CompleteSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.LocationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.PresentInArchiveSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.ShareIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SizeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.SpeedHintSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StatusSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StorageConfirmationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
public class PhysicalDataAttributeProvider implements IObjectAttributeProvider
{

    @Override
    public IAttributeSearchFieldKind getAttribute(ISearchCriteria criteria)
    {
        if (criteria instanceof ShareIdSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.SHARE_ID;
        } else if (criteria instanceof LocationSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.LOCATION;
        } else if (criteria instanceof SizeSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.SIZE;
        } else if (criteria instanceof CompleteSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.COMPLETE;
        } else if (criteria instanceof StatusSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.STATUS;
        } else if (criteria instanceof ArchivingRequestedSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.ARCHIVING_REQUESTED;
        } else if (criteria instanceof PresentInArchiveSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.PRESENT_IN_ARCHIVE;
        } else if (criteria instanceof StorageConfirmationSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.STORAGE_CONFIRMATION;
        } else if (criteria instanceof SpeedHintSearchCriteria)
        {
            return DataSetAttributeSearchFieldKind.SPEED_HINT;
        } else
        {
            throw new IllegalArgumentException("Unknown attribute criteria: " + criteria);
        }
    }

}
