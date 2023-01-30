/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.space;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface SpaceQuery extends ObjectQuery
{

    @Select(sql = "select s.id, s.code from spaces s where s.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SpaceAuthorizationRecord> getAuthorizations(LongSet spaceIds);

    @Select(sql = "select s.id, s.code, s.description, s.registration_timestamp as registrationDate, frozen, "
            + "frozen_for_proj as frozenForProjects, frozen_for_samp as frozenForSamples "
            + "from spaces s where s.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SpaceBaseRecord> getSpaces(LongSet spaceIds);

    @Select(sql = "select s.space_id as objectId, s.id as relatedId from samples s where s.space_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleIds(LongSet spaceIds);

    @Select(sql = "select s.id as objectId, s.pers_id_registerer as relatedId from spaces s where s.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet spaceIds);

    @Select(sql = "select p.space_id as objectId, p.id as relatedId from projects p where p.space_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getProjectIds(LongSet spaceIds);

}
