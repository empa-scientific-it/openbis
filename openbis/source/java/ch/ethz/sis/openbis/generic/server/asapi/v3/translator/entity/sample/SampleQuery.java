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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.sample;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.property.MaterialPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.property.PropertyAssignmentRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.property.PropertyRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface SampleQuery extends ObjectQuery
{

    @Select(sql = "select s.id, s.code, sp.code as spaceCode, sc.code as containerCode "
            + "from samples s left join spaces sp on s.space_id = sp.id "
            + "left join samples sc on s.samp_id_part_of = sc.id "
            + "where s.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleAuthorizationRecord> getAuthorizations(LongSet sampleIds);

    @Select(sql = "select s.id, s.code, s.perm_id as permId, sp.code as spaceCode, p.code as projectCode, "
            + "sc.code as containerCode, s.registration_timestamp as registrationDate, "
            + "s.modification_timestamp as modificationDate "
            + "from samples s left join spaces sp on s.space_id = sp.id "
            + "left join projects p on s.proj_id = p.id "
            + "left join samples sc on s.samp_id_part_of = sc.id "
            + "where s.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleBaseRecord> getSamples(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.saty_id as relatedId from samples s where s.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTypeIds(LongSet sampleIds);

    @Select(sql = "select st.id, st.code, st.description, st.is_listable as listable, st.is_subcode_unique as subcodeUnique, st.is_auto_generated_code as autoGeneratedCode, "
            + "st.show_parent_metadata as showParentMetadata, st.generated_code_prefix as generatedCodePrefix, st.modification_timestamp as modificationDate "
            + "from sample_types st where st.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleTypeBaseRecord> getTypes(LongSet sampleTypeIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql = "select p.samp_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.value as propertyValue, m.code as materialPropertyValueCode, mt.code as materialPropertyValueTypeCode, cvt.code as vocabularyPropertyValue "
            + "from sample_properties p "
            + "left join materials m on p.mate_prop_id = m.id "
            + "left join controlled_vocabulary_terms cvt on p.cvte_id = cvt.id "
            + "left join material_types mt on m.maty_id = mt.id "
            + "join sample_type_property_types etpt on p.stpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where p.samp_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> getProperties(LongSet sampleIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql = "select p.samp_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.mate_prop_id as propertyValue "
            + "from sample_properties p "
            + "join sample_type_property_types etpt on p.stpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where p.mate_prop_id is not null and p.samp_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialPropertyRecord> getMaterialProperties(LongSet sampleIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql = "select ph.samp_id as objectId, ph.pers_id_author as authorId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, ph.value as propertyValue, ph.material as materialPropertyValue, ph.vocabulary_term as vocabularyPropertyValue, ph.valid_from_timestamp as validFrom, ph.valid_until_timestamp as validTo "
            + "from sample_properties_history ph "
            + "join sample_type_property_types etpt on ph.stpt_id = etpt.id "
            + "join property_types pt on etpt.prty_id = pt.id "
            + "where ph.samp_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet sampleIds);

    @Select(sql = "select srh.main_samp_id as objectId, srh.pers_id_author as authorId, srh.relation_type as relationType, "
            + "srh.entity_perm_id as relatedObjectId, srh.valid_from_timestamp as validFrom, srh.valid_until_timestamp as validTo, "
            + "srh.space_id as spaceId, srh.expe_id as experimentId, srh.samp_id as sampleId, srh.data_id as dataSetId "
            + "from sample_relationships_history srh where srh.valid_until_timestamp is not null and srh.main_samp_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SampleRelationshipRecord> getRelationshipsHistory(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.expe_id as relatedId from samples s where s.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExperimentIds(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.samp_id_part_of as relatedId from samples s where s.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getContainerIds(LongSet sampleIds);

    @Select(sql = "select s.samp_id_part_of as objectId, s.id as relatedId from samples s where s.samp_id_part_of = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getComponentsIds(LongSet sampleIds);

    @Select(sql = "select sr.sample_id_child as objectId, sr.sample_id_parent as relatedId from "
            + "sample_relationships sr, relationship_types rt "
            + "where sr.relationship_id = rt.id and rt.code = 'PARENT_CHILD' and sr.sample_id_child = any(?{1}) order by sr.id", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getParentIds(LongSet sampleIds);

    @Select(sql = "select sr.sample_id_parent as objectId, sr.sample_id_child as relatedId from "
            + "sample_relationships sr, relationship_types rt "
            + "where sr.relationship_id = rt.id and rt.code = 'PARENT_CHILD' and sr.sample_id_parent = any(?{1}) order by sr.id", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getChildIds(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.pers_id_registerer as relatedId from samples s where s.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.pers_id_modifier as relatedId from samples s where s.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getModifierIds(LongSet sampleIds);

    @Select(sql = "select ma.samp_id as objectId, ma.mepr_id as relatedId from metaproject_assignments ma where ma.samp_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTagIds(LongSet sampleIds);

    @Select(sql = "select d.samp_id as objectId, d.id as relatedId from data d where d.samp_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getDataSetIds(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.space_id as relatedId from samples s where s.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSpaceIds(LongSet sampleIds);

    @Select(sql = "select s.id as objectId, s.proj_id as relatedId from samples s where s.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getProjectIds(LongSet sampleIds);

    @Select(sql = "select saty_id as objectId, id as relatedId from sample_type_property_types where saty_id = any(?{1})",
            parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPropertyAssignmentIds(LongSet sampleTypeIds);

    @Select(sql = "select * from sample_type_property_types where id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyAssignmentRecord> getPropertyAssignments(LongSet sampleTypePropertyTypeIds);
}
