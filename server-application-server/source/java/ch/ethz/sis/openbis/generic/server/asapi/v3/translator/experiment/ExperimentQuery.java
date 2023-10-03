/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.MaterialPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyAssignmentRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.SamplePropertyRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.HistoryPropertyRecordDataObjectBinding;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.PropertyRecordDataObjectBinding;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface ExperimentQuery extends ObjectQuery
{

    @Select(sql = "select e.id, e.code, e.perm_id as permId, p.code as projectCode, sp.code as spaceCode, "
            + "e.registration_timestamp as registrationDate, e.modification_timestamp as modificationDate, "
            + "e.frozen as frozen, e.frozen_for_data as frozenForDataSets, e.frozen_for_samp as frozenForSamples, "
            + "e.meta_data as metaData "
            + "from experiments e join projects p on e.proj_id = p.id "
            + "join spaces sp on p.space_id = sp.id "
            + "where e.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ExperimentBaseRecord> getExperiments(LongSet experimentIds);

    @Select(sql = "select e.id as objectId, e.exty_id as relatedId from experiments e where e.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTypeIds(LongSet experimentIds);

    @Select(sql = "select et.id, et.code, et.description, et.modification_timestamp as modificationDate, et.meta_data as metaData "
            + "from experiment_types et where et.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ExperimentTypeBaseRecord> getTypes(LongSet experimentTypeIds);

    @Select(sql = "select e.id as objectId, e.proj_id as relatedId from experiments e where e.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getProjectIds(LongSet experimentIds);

    @Select(sql = "select s.expe_id as objectId, s.id as relatedId from samples s where s.expe_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleIds(LongSet experimentIds);

    @Select(sql = "select d.expe_id as objectId, d.id as relatedId from data d where d.expe_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getDataSetIds(LongSet experimentIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select p.id as id, p.expe_id as objectId, p.pers_id_author AS authorId, p.modification_timestamp AS modificationTimestamp, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, "
                    + "p.value as propertyValue, m.code as materialPropertyValueCode, mt.code as materialPropertyValueTypeCode, "
                    + "s.perm_id as sample_perm_id, s.id as sample_id, "
                    + "cvt.code as vocabularyPropertyValue, "
                    + "cv.code as vocabularyPropertyValueTypeCode, "
                    + "p.integer_array_value as integerArrayPropertyValue, "
                    + "p.real_array_value as realArrayPropertyValue, "
                    + "p.string_array_value as stringArrayPropertyValue, "
                    + "p.timestamp_array_value as timestampArrayPropertyValue, "
                    + "p.json_value as jsonPropertyValue "
                    + "from experiment_properties p "
                    + "left join samples s on p.samp_prop_id = s.id "
                    + "left join materials m on p.mate_prop_id = m.id "
                    + "left join controlled_vocabulary_terms cvt on p.cvte_id = cvt.id "
                    + "left join controlled_vocabularies cv on cvt.covo_id = cv.id "
                    + "left join material_types mt on m.maty_id = mt.id "
                    + "join experiment_type_property_types etpt on p.etpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.expe_id = any(?{1}) order by p.id asc", parameterBindings = { LongSetMapper.class },
            resultSetBinding = PropertyRecordDataObjectBinding.class, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> getProperties(LongSet experimentIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select p.expe_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.mate_prop_id as propertyValue "
                    + "from experiment_properties p "
                    + "join experiment_type_property_types etpt on p.etpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.mate_prop_id is not null and p.expe_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialPropertyRecord> getMaterialProperties(LongSet experimentIds);

    @Select(sql =
            "select p.expe_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.samp_prop_id as propertyValue "
                    + "from experiment_properties p "
                    + "join experiment_type_property_types etpt on p.etpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.samp_prop_id is not null and p.expe_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SamplePropertyRecord> getSampleProperties(LongSet experimentIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select ph.id as id, ph.expe_id as objectId, ph.pers_id_author as authorId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, ph.value as propertyValue, ph.material as materialPropertyValue, ph.sample as samplePropertyValue, ph.vocabulary_term as vocabularyPropertyValue, ph.valid_from_timestamp as validFrom, ph.valid_until_timestamp as validTo, "
                    + "ph.integer_array_value as integerArrayPropertyValue, "
                    + "ph.real_array_value as realArrayPropertyValue, "
                    + "ph.string_array_value as stringArrayPropertyValue, "
                    + "ph.timestamp_array_value as timestampArrayPropertyValue, "
                    + "ph.json_value as jsonPropertyValue "
                    + "from experiment_properties_history ph "
                    + "join experiment_type_property_types etpt on ph.etpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where ph.expe_id = any(?{1})", parameterBindings = { LongSetMapper.class },
            resultSetBinding = HistoryPropertyRecordDataObjectBinding.class, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet experimentIds);

    public static final String RELATIONSHIP_HISTORY_QUERY =
            "select erh.id as id, erh.main_expe_id as objectId, erh.pers_id_author as authorId, erh.relation_type as relationType, "
                    + "erh.entity_kind as entityKind, "
                    + "erh.entity_perm_id as relatedObjectId, erh.valid_from_timestamp as validFrom, erh.valid_until_timestamp as validTo, "
                    + "erh.proj_id as projectId, erh.samp_id as sampleId, erh.data_id as dataSetId "
                    + "from experiment_relationships_history erh where erh.main_expe_id = any(?{1}) ";

    @Select(sql = RELATIONSHIP_HISTORY_QUERY + "and erh.entity_kind = ?{2}", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ExperimentRelationshipRecord> getRelationshipsHistory(LongSet experimentIds, String entityKind);

    @Select(sql = RELATIONSHIP_HISTORY_QUERY + "and erh.entity_kind is null", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ExperimentRelationshipRecord> getUnknownRelationshipsHistory(LongSet experimentIds);

    @Select(sql = "select e.id as objectId, e.pers_id_registerer as relatedId from experiments e where e.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet experimentIds);

    @Select(sql = "select e.id as objectId, e.pers_id_modifier as relatedId from experiments e where e.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getModifierIds(LongSet experimentIds);

    @Select(sql = "select ma.expe_id as objectId, ma.mepr_id as relatedId from metaproject_assignments ma where ma.expe_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTagIds(LongSet experimentIds);

    @Select(sql = "select exty_id as objectId, id as relatedId from experiment_type_property_types where exty_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPropertyAssignmentIds(LongSet experimentTypeIds);

    @Select(sql = "select t.id as objectId, t.validation_script_id as relatedId from experiment_types t where t.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getValidationPluginIds(LongSet experimentTypeIds);

    @Select(sql = "select pt.code as prty_code, pt.is_managed_internally as prty_is_managed_internally, 'EXPERIMENT' as kind_code, et.id as type_id, et.code as type_code, etpt.* from experiment_type_property_types etpt, property_types pt, experiment_types et where etpt.id = any(?{1}) and etpt.prty_id = pt.id and etpt.exty_id = et.id", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyAssignmentRecord> getPropertyAssignments(LongSet experimentTypePropertyTypeIds);
}
