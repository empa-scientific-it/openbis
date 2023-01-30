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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ArchivingStatus;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.StatusSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.EnumFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProviderFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;

/**
 * @author pkupczyk
 */
public class StatusSearchCriteriaTranslator extends EnumFieldSearchCriteriaTranslator
{

    public StatusSearchCriteriaTranslator(IDAOFactory daoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(daoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof StatusSearchCriteria;
    }

    @Override
    protected Map<ArchivingStatus, String> getValueToIndexedValueMapping()
    {
        Map<ArchivingStatus, String> map = new HashMap<ArchivingStatus, String>();
        map.put(ArchivingStatus.AVAILABLE, DataSetArchivingStatus.AVAILABLE.name());
        map.put(ArchivingStatus.LOCKED, DataSetArchivingStatus.LOCKED.name());
        map.put(ArchivingStatus.ARCHIVED, DataSetArchivingStatus.ARCHIVED.name());
        map.put(ArchivingStatus.UNARCHIVE_PENDING, DataSetArchivingStatus.UNARCHIVE_PENDING.name());
        map.put(ArchivingStatus.ARCHIVE_PENDING, DataSetArchivingStatus.ARCHIVE_PENDING.name());
        map.put(ArchivingStatus.BACKUP_PENDING, DataSetArchivingStatus.BACKUP_PENDING.name());
        return map;
    }

}
