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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.samplelister;

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TypeMapper;

import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.EntityMetaprojectRelationRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaprojectRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.PropertyRecord;

/**
 * @author Franz-Josef Elmer
 */
public interface ISampleListingQuery extends BaseQuery
{
    public int FETCH_SIZE = 1000;

    @Select(sql = "select * from relationship_types where code=?{1} and is_managed_internally=?{2}")
    public long getRelationshipTypeId(String code, boolean managedInternally);

    @Select(sql = "Select s.id as s_id, s.perm_id as s_perm_id, s.code as s_code, "
            + "cs.code as container_code, s.registration_timestamp as s_registration_timestamp, "
            + "s.modification_timestamp as s_modification_timestamp, sp.code as sp_code, "
            + "st.id as st_id, st.code as st_code, pe.first_name as pe_first_name, "
            + "pe.last_name as pe_last_name, pe.user_id as pe_user_id, pe.email as pe_email, "
            + "e.code as exp_code, p.code as proj_code, ps.code as proj_space_code, sap.code as samp_proj_code, "
            + "mod.first_name as mod_first_name, mod.last_name as mod_last_name, "
            + "mod.user_id as mod_user_id, mod.email as mod_email "
            + "from samples as s join sample_types as st on s.saty_id = st.id "
            + "left join samples as cs on s.samp_id_part_of = cs.id "
            + "left join spaces as sp on s.space_id = sp.id "
            + "left join projects as sap on s.proj_id = sap.id "
            + "left join experiments as e on s.expe_id = e.id "
            + "left join projects as p on e.proj_id = p.id "
            + "left join spaces as ps on p.space_id = ps.id "
            + "left join persons as mod on s.pers_id_modifier = mod.id "
            + "join persons as pe on s.pers_id_registerer = pe.id where s.id = any(?{1}) ", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleRecord> listSamplesByIds(LongSet sampleIDs);

    @Select(sql = "Select * from sample_relationships "
            + "where relationship_id = ?{1} and sample_id_parent = any(?{2})", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleRelationshipRecord> getChildren(Long relationshipID, LongSet sampleIDs);

    @Select(sql = "Select * from sample_relationships "
            + "where relationship_id = ?{1} and sample_id_child = any(?{2})", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleRelationshipRecord> getParents(Long relationshipID, LongSet sampleIDs);

    @Select(sql = "with recursive connected_relationships as (select * from sample_relationships "
            + "where relationship_id = ?{1} and sample_id_parent = any(?{2}) "
            + "union select sr.* from connected_relationships as cr "
            + "join sample_relationships as sr on cr.sample_id_child = sr.sample_id_parent) "
            + "select * from connected_relationships", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleRelationshipRecord> getDescendants(Long relationshipID, LongSet sampleIDs);

    @Select(sql = "with recursive connected_relationships as (select * from sample_relationships "
            + "where relationship_id = ?{1} and sample_id_child = any(?{2}) "
            + "union select sr.* from connected_relationships as cr "
            + "join sample_relationships as sr on cr.sample_id_parent = sr.sample_id_child) "
            + "select * from connected_relationships", parameterBindings =
    { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleRelationshipRecord> getAncestors(Long relationshipID, LongSet sampleIDs);

    @Select(sql = "select p.samp_id as entity_id, pt.code as code, dt.code as data_type, p.value, "
            + "m.code as material, mt.code as material_type, vt.code as voca_term "
            + "from sample_properties as p "
            + "join sample_type_property_types as stpt on p.stpt_id = stpt.id "
            + "join property_types as pt on stpt.prty_id = pt.id "
            + "join data_types as dt on pt.daty_id = dt.id "
            + "left join materials as m on p.mate_prop_id = m.id "
            + "left join material_types as mt on m.maty_id = mt.id "
            + "left join controlled_vocabulary_terms as vt on p.cvte_id = vt.id "
            + "where p.samp_id = any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> getProperties(LongSet entityIDs);

    @Select(sql = "select ma.mepr_id as metaproject_id, ma.samp_id as entity_id "
            + " from metaproject_assignments as ma "
            + " join metaprojects as m on ma.mepr_id = m.id "
            + "where ma.samp_id = any(?{1}) and m.owner = ?{2}", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<EntityMetaprojectRelationRecord> getMetaprojectAssignments(LongSet sampleIDs,
            Long ownerId);

    @Select(sql = "select id as id, name as name, description as description, "
            + " private as is_private, creation_date as creation_date " + " from metaprojects"
            + " where id = any(?{1})", parameterBindings =
    { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MetaprojectRecord> getMetaprojects(LongSet metaprojectIDs);
}
