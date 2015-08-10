/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryRelation;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.sql.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.history.HistoryEntryFetchOptions;

/**
 * @author pkupczyk
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MaterialHistoryRelation extends HistoryRelation
{

    public MaterialHistoryRelation(TranslationContext context, Collection<Long> entityIds, HistoryEntryFetchOptions fetchOptions)
    {
        super(context, entityIds, fetchOptions);
    }

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<HistoryRelationshipRecord> loadRelationshipHistory(Collection<Long> entityIds)
    {
        return null;
    }

}
