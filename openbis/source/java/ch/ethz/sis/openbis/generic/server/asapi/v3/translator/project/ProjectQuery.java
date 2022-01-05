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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.project;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface ProjectQuery extends ObjectQuery
{

    @Select(sql = "select p.id, p.code, sp.code as spaceCode "
            + "from projects p join spaces sp on p.space_id = sp.id "
            + "where p.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ProjectAuthorizationRecord> getAuthorizations(LongSet longSet);

    @Select(sql = "select p.id, p.code, p.perm_id as permId, sp.code as spaceCode, p.description, "
            + "p.registration_timestamp as registrationDate, p.modification_timestamp as modificationDate, "
            + "p.frozen as frozen, p.frozen_for_exp as frozenForExperiments, p.frozen_for_samp as frozenForSamples "
            + "from projects p left outer join spaces sp on p.space_id = sp.id "
            + "where p.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ProjectBaseRecord> getProjects(LongSet projectIds);

    @Select(sql = "select p.id as objectId, p.space_id as relatedId from projects p where p.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSpaceIds(LongSet projectIds);

    @Select(sql = "select e.proj_id as objectId, e.id as relatedId from experiments e where e.proj_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExperimentIds(LongSet projectIds);

    @Select(sql = "select p.id as objectId, p.pers_id_registerer as relatedId from projects p where p.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet projectIds);

    @Select(sql = "select p.id as objectId, p.pers_id_modifier as relatedId from projects p where p.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getModifierIds(LongSet projectIds);

    @Select(sql = "select p.id as objectId, p.pers_id_leader as relatedId from projects p where p.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getLeaderIds(LongSet projectIds);

    @Select(sql = "select prh.main_proj_id as objectId, prh.pers_id_author as authorId, prh.relation_type as relationType, "
            + "prh.entity_kind as entityKind, "
            + "prh.entity_perm_id as relatedObjectId, prh.valid_from_timestamp as validFrom, prh.valid_until_timestamp as validTo, "
            + "prh.space_id as spaceId, prh.expe_id as experimentId, prh.samp_id as sampleId "
            + "from project_relationships_history prh where prh.main_proj_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ProjectRelationshipRecord> getRelationshipsHistory(LongSet projectIds);

    @Select(sql = "select s.proj_id as objectId, s.id as relatedId from samples s where s.proj_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleIds(LongSet projectIds);

}
