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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryContentCopyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.history.HistoryPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.MaterialPropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyAssignmentRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.PropertyRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property.SamplePropertyRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.HistoryPropertyRecordDataObjectBinding;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.PropertyRecordDataObjectBinding;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface DataSetQuery extends ObjectQuery
{

    @Select(sql = "select d.id, d.code, d.is_derived as isDerived, d.data_producer_code as dataProducer, "
            + "d.production_timestamp as dataProductionDate, d.access_timestamp as accessDate, "
            + "d.modification_timestamp as modificationDate, d.registration_timestamp as registrationDate, "
            + "d.data_set_kind as dataSetKind, d.frozen as frozen, d.frozen_for_children as frozenForChildren, "
            + "d.frozen_for_parents as frozenForParents, d.frozen_for_comps as frozenForComponents, "
            + "d.frozen_for_conts as frozenForContainers, d.meta_data as metaData "
            + "from data d where d.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetBaseRecord> getDataSets(LongSet dataSetIds);

    @Select(sql = "select ed.id as objectId, ed.id as relatedId from external_data ed where ed.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPhysicalDataIds(LongSet dataSetIds);

    @Select(sql =
            "select ed.id as id, ed.share_id as shareId, ed.location, ed.size, ed.status, ed.is_complete as isComplete, ed.present_in_archive as isPresentInArchive, ed.storage_confirmation as isStorageConfirmed, ed.speed_hint as speedHint, ed.archiving_requested as isArchivingRequested "
                    + "from external_data ed where ed.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PhysicalDataBaseRecord> getPhysicalDatas(LongSet dataSetIds);

    @Select(sql = "select ld.id as objectId, ld.id as relatedId from link_data ld where ld.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getLinkedDataIds(LongSet dataSetIds);

    @Select(sql = "select ld.id as id, cc.external_code as externalCode from link_data ld, content_copies cc where cc.data_id = ld.id and ld.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<LinkedDataBaseRecord> getLinkedDatas(LongSet dataSetIds);

    @Select(sql = "select cc.id as objectId, cc.edms_id as relatedId from content_copies cc where cc.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExternalDmsIds(LongSet dataSetIds);

    @Select(sql = "select ed.id as objectId, ed.ffty_id as relatedId from external_data ed where ed.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getFileFormatTypeIds(LongSet dataSetIds);

    @Select(sql = "select fft.id, fft.code, fft.description from file_format_types fft where fft.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<FileFormatTypeBaseRecord> getFileFormatTypes(LongSet fileFormatTypeIds);

    @Select(sql = "select ed.id as objectId, ed.loty_id as relatedId from external_data ed where ed.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getLocatorTypeIds(LongSet dataSetIds);

    @Select(sql = "select lt.id, lt.code, lt.description from locator_types lt where lt.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<LocatorTypeBaseRecord> getLocatorTypes(LongSet locatorTypeIds);

    @Select(sql = "select ed.id as objectId, ed.cvte_id_stor_fmt as relatedId from external_data ed where ed.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getStorageFormatIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.dsty_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTypeIds(LongSet dataSetIds);

    @Select(sql = "select dt.id, dt.code, dt.description, dt.main_ds_pattern as mainDataSetPattern, dt.main_ds_path as mainDataSetPath, "
            + "dt.deletion_disallow as disallowDeletion, dt.modification_timestamp as modificationDate, dt.meta_data as metaData from data_set_types dt where dt.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetTypeBaseRecord> getTypes(LongSet dataSetTypeIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select p.id as id, p.ds_id as objectId, p.pers_id_author AS authorId, p.modification_timestamp AS modificationTimestamp, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, "
                    + "p.value as propertyValue, m.code as materialPropertyValueCode, mt.code as materialPropertyValueTypeCode, "
                    + "s.perm_id as sample_perm_id, s.id as sample_id, "
                    + "cvt.code as vocabularyPropertyValue, "
                    + "cv.code as vocabularyPropertyValueTypeCode, "
                    + "p.integer_array_value as integerArrayPropertyValue, "
                    + "p.real_array_value as realArrayPropertyValue, "
                    + "p.string_array_value as stringArrayPropertyValue, "
                    + "p.timestamp_array_value as timestampArrayPropertyValue, "
                    + "p.json_value as jsonPropertyValue "
                    + "from data_set_properties p "
                    + "left join samples s on p.samp_prop_id = s.id "
                    + "left join materials m on p.mate_prop_id = m.id "
                    + "left join controlled_vocabulary_terms cvt on p.cvte_id = cvt.id "
                    + "left join controlled_vocabularies cv on cvt.covo_id = cv.id "
                    + "left join material_types mt on m.maty_id = mt.id "
                    + "join data_set_type_property_types etpt on p.dstpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.ds_id = any(?{1}) order by id", parameterBindings = { LongSetMapper.class },
            resultSetBinding = PropertyRecordDataObjectBinding.class, fetchSize = FETCH_SIZE)
    public List<PropertyRecord> getProperties(LongSet dataSetIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select p.ds_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.mate_prop_id as propertyValue "
                    + "from data_set_properties p "
                    + "join data_set_type_property_types etpt on p.dstpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.mate_prop_id is not null and p.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialPropertyRecord> getMaterialProperties(LongSet dataSetIds);

    @Select(sql =
            "select p.ds_id as objectId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, p.samp_prop_id as propertyValue "
                    + "from data_set_properties p "
                    + "join data_set_type_property_types etpt on p.dstpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where p.samp_prop_id is not null and p.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<SamplePropertyRecord> getSampleProperties(LongSet dataSetIds);

    // PropertyQueryGenerator was used to generate this query
    @Select(sql =
            "select ph.id as id, ph.ds_id as objectId, ph.pers_id_author as authorId, case pt.is_managed_internally when FALSE then pt.code else '$' || pt.code end as propertyCode, ph.value as propertyValue, ph.material as materialPropertyValue, ph.sample as samplePropertyValue, ph.vocabulary_term as vocabularyPropertyValue, ph.valid_from_timestamp as validFrom, ph.valid_until_timestamp as validTo, "
                    + "ph.integer_array_value as integerArrayPropertyValue, "
                    + "ph.real_array_value as realArrayPropertyValue, "
                    + "ph.string_array_value as stringArrayPropertyValue, "
                    + "ph.timestamp_array_value as timestampArrayPropertyValue, "
                    + "ph.json_value as jsonPropertyValue "
                    + "from data_set_properties_history ph "
                    + "join data_set_type_property_types etpt on ph.dstpt_id = etpt.id "
                    + "join property_types pt on etpt.prty_id = pt.id "
                    + "where ph.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class },
            resultSetBinding = HistoryPropertyRecordDataObjectBinding.class, fetchSize = FETCH_SIZE)
    public List<HistoryPropertyRecord> getPropertiesHistory(LongSet dataSetIds);

    public static final String RELATIONSHIP_HISTORY_QUERY =
            "select drh.id as id, drh.main_data_id as objectId, drh.pers_id_author as authorId, drh.relation_type as relationType, "
                    + "drh.entity_kind as entityKind, "
                    + "drh.entity_perm_id as relatedObjectId, drh.valid_from_timestamp as validFrom, drh.valid_until_timestamp as validTo, "
                    + "drh.expe_id as experimentId, drh.samp_id as sampleId, drh.data_id as dataSetId "
                    + "from data_set_relationships_history drh where drh.main_data_id = any(?{1}) ";

    @Select(sql = RELATIONSHIP_HISTORY_QUERY + "and drh.entity_kind = ?{2} and drh.relation_type = ?{3}", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetRelationshipRecord> getRelationshipsHistory(LongSet dataSetIds, String entityKind, String relationType);

    @Select(sql = RELATIONSHIP_HISTORY_QUERY + "and drh.entity_kind is null", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetRelationshipRecord> getUnknownRelationshipsHistory(LongSet dataSetIds);

    @Select(sql =
            "select dsch.id as id, dsch.data_id as dataSetId, dsch.external_code as externalCode, dsch.path as path, dsch.git_commit_hash as gitCommitHash, "
                    + "dsch.git_repository_id as gitRepositoryId, dsch.edms_id as externalDmsId, dsch.edms_code as externalDmsCode, dsch.edms_label as externalDmsLabel, "
                    + "dsch.edms_address as externalDmsAddress, dsch.pers_id_author as authorId, dsch.valid_from_timestamp as validFrom, dsch.valid_until_timestamp as validTo "
                    + "from data_set_copies_history dsch "
                    + "where dsch.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class
    }, fetchSize = FETCH_SIZE)
    public List<HistoryContentCopyRecord> getContentCopyHistory(LongSet dataSetIds);

    @Select(sql = "select ds_id from post_registration_dataset_queue where ds_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<Long> getNotPostRegisteredDataSets(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.samp_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getSampleIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.expe_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getExperimentIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.dast_id as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getDataStoreIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_child as objectId, dr.data_id_parent as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'PARENT_CHILD' and dr.data_id_child = any(?{1}) order by dr.ordinal", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getParentIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_parent as objectId, dr.data_id_child as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'PARENT_CHILD' and dr.data_id_parent = any(?{1}) order by dr.ordinal", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getChildIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_child as objectId, dr.data_id_parent as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'CONTAINER_COMPONENT' and dr.data_id_child = any(?{1}) order by dr.ordinal", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getContainerIds(LongSet dataSetIds);

    @Select(sql = "select dr.data_id_parent as objectId, dr.data_id_child as relatedId from "
            + "data_set_relationships dr, relationship_types rt "
            + "where dr.relationship_id = rt.id and rt.code = 'CONTAINER_COMPONENT' and dr.data_id_parent = any(?{1}) order by dr.ordinal", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getComponentIds(LongSet dataSetIds);

    @Select(sql = "select ma.data_id as objectId, ma.mepr_id as relatedId from metaproject_assignments ma where ma.data_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTagIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.pers_id_registerer as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet dataSetIds);

    @Select(sql = "select d.id as objectId, d.pers_id_modifier as relatedId from data d where d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getModifierIds(LongSet dataSetIds);

    @Select(sql = "select dsty_id as objectId, id as relatedId from data_set_type_property_types where dsty_id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getPropertyAssignmentIds(LongSet dataSetTypeIds);

    @Select(sql = "select pt.code as prty_code, pt.is_managed_internally as prty_is_managed_internally, 'DATA_SET' as kind_code, dt.id as type_id, dt.code as type_code, dtpt.* from data_set_type_property_types dtpt, property_types pt, data_set_types dt where dtpt.id = any(?{1}) and dtpt.prty_id = pt.id and dtpt.dsty_id = dt.id", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<PropertyAssignmentRecord> getPropertyAssignments(LongSet dataSetTypePropertyTypeIds);

    @Select(sql = "select d.id as objectId, cc.id as relatedId from data d, content_copies cc where cc.data_id = d.id and d.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getContentCopyIds(LongOpenHashSet dataSetIds);

    @Select(sql = "select id, external_code as externalCode, path, git_commit_hash as gitCommitHash, git_repository_id as gitRepositoryId from content_copies where id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ContentCopyRecord> getContentCopies(LongOpenHashSet longOpenHashSet);

    @Select(sql = "select t.id as objectId, t.validation_script_id as relatedId from data_set_types t where t.id = any(?{1})", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getValidationPluginIds(LongSet dataSetTypeIds);

}
