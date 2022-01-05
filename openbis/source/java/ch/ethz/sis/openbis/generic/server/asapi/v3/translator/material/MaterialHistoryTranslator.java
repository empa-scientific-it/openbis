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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryRelationshipRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class MaterialHistoryTranslator extends HistoryTranslator implements IMaterialHistoryTranslator
{

    @Override protected List<? extends PropertyRecord> loadProperties(final Collection<Long> entityIds)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        return query.getProperties(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<HistoryPropertyRecord> loadPropertyHistory(Collection<Long> entityIds)
    {
        MaterialQuery query = QueryTool.getManagedQuery(MaterialQuery.class);
        return query.getPropertiesHistory(new LongOpenHashSet(entityIds));
    }

    @Override
    protected List<HistoryRelationshipRecord> loadRelationshipHistory(TranslationContext context, Collection<Long> entityIds)
    {
        return null;
    }

}
