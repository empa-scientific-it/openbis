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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.tag;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.ObjectToPersonTranslator;

/**
 * @author pkupczyk
 */
@Component
public class TagOwnerTranslator extends ObjectToPersonTranslator implements ITagOwnerTranslator
{

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet objectIds)
    {
        TagQuery query = QueryTool.getManagedQuery(TagQuery.class);
        return query.getOwnerIds(objectIds);
    }

}