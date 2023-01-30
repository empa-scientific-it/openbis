/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import java.util.Date;
import java.util.List;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.CodeRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.common.MetaProjectWithEntityId;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.TypeMapper;

/**
 * A {@link TransactionQuery} interface for obtaining large sets of dataset-related entities from the database.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is needed for creating a dynamic proxy by the EOD SQL
 * library.
 * 
 * @author Tomasz Pylak
 */
@Private
@Friend(toClasses = { DataStoreRecord.class })
public interface IDatasetListingQuery extends BaseQuery, IPropertyListingQuery
{

    public final static String EXTERNAL_DATA_COLUMNS =
            "external_data.location, "
                    + "external_data.ffty_id, "
                    + "external_data.loty_id, "
                    + "external_data.cvte_id_stor_fmt, "
                    + "external_data.is_complete, "
                    + "external_data.cvte_id_store, "
                    + "external_data.status, "
                    + "external_data.share_id, "
                    + "external_data.size, "
                    + "external_data.present_in_archive, "
                    + "external_data.speed_hint, "
                    + "external_data.storage_confirmation, "
                    + "external_data.h5_folders, "
                    + "external_data.h5ar_folders, "
                    + "external_data.archiving_requested";

    public final static String SELECT_ALL =
            "select data.*, " + EXTERNAL_DATA_COLUMNS + ", link_data.id as link_data_id, content_copies.external_code, content_copies.edms_id, "
                    + "prdq.id IS NULL as is_post_registered "
                    + "from data left outer join external_data on data.id = external_data.id "
                    + "left outer join link_data on data.id = link_data.id "
                    + "left outer join post_registration_dataset_queue prdq on data.id = prdq.ds_id "
                    + "left outer join content_copies on data.id = content_copies.data_id";

    public final static String SELECT_ALL_EXTERNAL_DATAS =
            "select data.*, " + EXTERNAL_DATA_COLUMNS + ", prdq.id IS NULL as is_post_registered "
                    + "from data "
                    + "join external_data on data.id = external_data.id "
                    + "left outer join post_registration_dataset_queue prdq on data.id = prdq.ds_id ";

    /**
     * Returns the datasets for the given experiment id.
     */
    @Select(sql = SELECT_ALL + " WHERE data.expe_id = ?{1}")
    public DataIterator<DatasetRecord> getDatasetsForExperiment(long experimentId);

    /**
     * Returns the datasets for the given metaproject id.
     */
    @Select(sql = SELECT_ALL
            + " JOIN metaproject_assignments ma ON data.id=ma.data_id WHERE ma.mepr_id = ?{1}")
    public DataIterator<DatasetRecord> getDatasetsForMetaproject(long metaprojectId);

    /**
     * Returns the datasets for the given metaproject id and archival state.
     */
    @Select(sql = SELECT_ALL
            + " JOIN metaproject_assignments ma ON data.id=ma.data_id WHERE ma.mepr_id = ?{1} AND external_data.present_in_archive = ?{2}")
    public DataIterator<DatasetRecord> getDatasetsForMetaprojectAndArchivalState(long metaprojectId, boolean isArchived);

    public final static String ED_COLUMNS =
            "ed.id as external_data_id, ed.location, "
                    + "ed.ffty_id, "
                    + "ed.loty_id, "
                    + "ed.cvte_id_stor_fmt, "
                    + "ed.is_complete, "
                    + "ed.cvte_id_store, "
                    + "ed.status, "
                    + "ed.share_id, "
                    + "ed.size, "
                    + "ed.present_in_archive, "
                    + "ed.speed_hint, "
                    + "ed.storage_confirmation, "
                    + "ed.h5_folders, "
                    + "ed.h5ar_folders, "
                    + "ed.archiving_requested";

    @Select(sql = "with recursive connected_data as ( "
            + "select d.*," + ED_COLUMNS + ", ld.id as link_data_id, prdq.id IS NULL as is_post_registered "
            + "from data as d "
            + "left outer join external_data as ed on d.id = ed.id "
            + "left outer join link_data as ld on d.id = ld.id "
            + "left outer join post_registration_dataset_queue prdq on d.id = prdq.ds_id "
            + "where expe_id = ?{1} "
            + "   or samp_id in (with recursive connected_samples as "
            + "                    (select id from samples where expe_id = ?{1} "
            + "                     union all select s.id from connected_samples as cs "
            + "                                       inner join sample_relationships as sr on sr.sample_id_parent = cs.id "
            + "                                       left join samples as s on s.id = sr.sample_id_child) "
            + "                  select * from connected_samples) "
            + "union all select d.*, " + ED_COLUMNS + ", "
            + " ld.id, prdq.id IS NULL as is_post_registered from connected_data as cd "
            + "                       inner join data_set_relationships as dr on dr.data_id_parent = cd.id "
            + "                       left join data as d on d.id = dr.data_id_child "
            + "                       left outer join external_data as ed on d.id = ed.id"
            + "                       left outer join link_data as ld on d.id = ld.id "
            + "left outer join post_registration_dataset_queue prdq on d.id = prdq.ds_id "
            + "where dr.relationship_id = ?{2}) "
            + "select * from connected_data")
    public DataIterator<DatasetRecord> getDataSetsForExperimentAndDescendents(long experimentId, long relationshipTypeId);

    /**
     * Returns the directly connected datasets for the given sample id.
     */
    @Select(sql = SELECT_ALL + " WHERE data.samp_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForSample(long sampleId);

    /**
     * Returns the directly connected datasets for the given sample ids.
     */
    @Select(sql = SELECT_ALL + " WHERE data.samp_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsForSamples(LongSet sampleIds);

    /**
     * Returns data sets that are newer than the data set of specified id.
     */
    @Select(sql = SELECT_ALL + " WHERE data.id > ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getNewDataSets(long lastSeenDatasetId);

    /**
     * Returns datasets that are newer than dataset with given id (<var>lastSeenDatasetId</var>) and are directly connected with samples of sample
     * type with given <var>sampleTypeId</var>.
     */
    @Select(sql = SELECT_ALL
            + " WHERE data.id > ?{2} AND data.samp_id IN (SELECT id FROM samples s WHERE s.saty_id=?{1})", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getNewDataSetsForSampleType(long sampleTypeId,
            long lastSeenDatasetId);

    /**
     * Returns datasets from store with given id that have status equal 'AVAILABLE' and were modified before given date.
     */
    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.registration_timestamp < ?{2} AND external_data.present_in_archive=?{3}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableExtDatasRegisteredBefore(long dataStoreId,
            Date lastModificationDate, boolean presentInArchive);

    /**
     * Like {@link #getAvailableExtDatasRegisteredBefore(long, Date, boolean)} with additional condition for data set type id.
     */
    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.registration_timestamp < ?{2} AND external_data.present_in_archive=?{3} "
            + "    AND data.dsty_id = ?{4}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableExtDatasRegisteredBeforeWithDataSetType(
            long dataStoreId, Date lastModificationDate, boolean presentInArchive,
            long dataSetTypeId);

    /**
     * Returns datasets from store with given id that have status equal 'AVAILABLE' and were accessed before given date.
     */
    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.access_timestamp < ?{2} AND external_data.present_in_archive=?{3}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableExtDatasAccessedBefore(long dataStoreId,
            Date lastAccessDate, boolean presentInArchive);

    /**
     * Like {@link #getAvailableExtDatasAccessedBefore(long, Date, boolean)} with additional condition for data set type id.
     */
    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + "    WHERE data.dast_id = ?{1} AND external_data.status = 'AVAILABLE' "
            + "    AND data.access_timestamp < ?{2} AND external_data.present_in_archive=?{3} "
            + "    AND data.dsty_id = ?{4}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getAvailableExtDatasAccessedBeforeWithDataSetType(
            long dataStoreId, Date lastAccessDate, boolean presentInArchive,
            long dataSetTypeId);

    /**
     * Returns the directly connected dataset ids for the given sample id.
     */
    @Select(sql = "select id from data where data.samp_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetIdsForSample(long sampleId);

    /**
     * Returns ids of datasets directly connected to samples with given ids.
     */
    @Select(sql = "select id from data where data.samp_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetIdsForSamples(LongSet sampleIds);

    @Select(sql = "select * from data_set_relationships where data_id_child = any(?{1}) and relationship_id = ?{2}", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRelationRecord> listParentDataSetIds(LongSet ids, long relationShipTypeId);

    @Select(sql = "select * from data_set_relationships where data_id_parent = any(?{1}) and relationship_id = ?{2}", parameterBindings = {
            LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRelationRecord> listChildrenDataSetIds(LongSet ids, long relationShipTypeId);

    @Select(sql = SELECT_ALL + " where data.id in (select data_id_child from data_set_relationships "
            + "where data_id_parent = any(?{1}) and relationship_id = ?{2})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getChildrenOf(LongSet ids, Long relationshipTypeId);

    @Select(sql = SELECT_ALL + " where data.id in (select data_id_parent from data_set_relationships "
            + "where data_id_child = any(?{1}) and relationship_id = ?{2})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getParentsOf(LongSet ids, Long relationshipTypeId);

    /**
     * Returns the datasets for the given <var>datasetId</var>.
     */
    @Select(SELECT_ALL + " where data.id=?{1}")
    public DatasetRecord getDataset(long datasetId);

    /**
     * Returns all datasets in the database.
     */
    @Select(sql = SELECT_ALL, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets();

    @Select(sql = "select id, code, deletion_disallow from data_set_types")
    public DataSetTypeRecord[] getDatasetTypes();

    @Select(sql = "select id, code, download_url from data_stores")
    public DataStoreRecord[] getDataStores();

    @Select(sql = "select id, code from file_format_types")
    public CodeRecord[] getFileFormatTypes();

    @Select(sql = "select id, code from locator_types")
    public CodeRecord[] getLocatorTypes();

    @Select(sql = "select id, code, label, address, address_type as addressType from external_data_management_systems")
    public ExternalDataManagementSystemRecord[] getExternalDataManagementSystems();

    //
    // Datasets
    //

    /**
     * Returns the datasets for the given <var>entityIds</var>.
     */
    @Select(sql = SELECT_ALL + " where data.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(LongSet entityIds);

    @Select(sql = SELECT_ALL + " where data.code = any(?{1})", parameterBindings = { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(String[] datasetCodes);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + " where data.dast_id = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasetsByDataStoreId(long dataStoreId);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + " where data.dast_id = ?{1}"
            + " order by registration_timestamp limit ?{2}", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreId(long dataStoreId, int limit);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + " where data.dast_id = ?{1} and registration_timestamp > ?{2}"
            + " order by registration_timestamp limit ?{3}", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreId(long dataStoreId, Date youngerThan,
            int limit);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + " where data.dast_id = ?{1} and registration_timestamp = ?{2}"
            + " order by registration_timestamp", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreId(long dataStoreId, Date at);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + " where data.dast_id = ?{1} and size is null"
            + " order by data.code limit ?{2}", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreIdWithUnknownSize(long dataStoreID, int limit);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + " where data.dast_id = ?{1} and size is null and data.code > ?{3}"
            + " order by data.code limit ?{2}", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreIdWithUnknownSize(long dataStoreID, int limit, String dataSetCodeLowerLimit);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS + " where data.dast_id = ?{1} and external_data.status = ?{2}", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreIdWithArchivingStatus(long dataStoreID, String archivingStatus);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS + " where data.dast_id = ?{1} and external_data.present_in_archive = ?{2}", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreIdWithPressentInArchive(long dataStoreID, Boolean presentInArchive);

    @Select(sql = SELECT_ALL_EXTERNAL_DATAS
            + " where data.dast_id = ?{1} and external_data.status = ?{2} and external_data.present_in_archive = ?{3}", fetchSize = FETCH_SIZE)
    public List<DatasetRecord> getDatasetsByDataStoreIdWithArchivingStatusAndPressentInArchive(long dataStoreID, String archivingStatus,
            Boolean presentInArchive);

    // NOTE: we list ALL data sets (even those in trash) using data_all table here
    @Select(sql = "SELECT code, share_id FROM data_all LEFT OUTER JOIN external_data "
            + "ON data_all.id = external_data.id WHERE data_all.dast_id = ?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetCodeWithShareIdRecord> getAllDatasetsWithShareIdsByDataStoreId(
            long dataStoreId);

    /**
     * Returns the children/component dataset ids of the specified datasets.
     */
    @Select(sql = "select data_id_child from data_set_relationships "
            + "where data_id_parent = any(?{1}) and relationship_id = ?{2}", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getDatasetChildrenIds(LongSet sampleId, long relationshipTypeId);

    @Select(sql = "select comp.code from data as comp "
            + "join data_set_relationships as r on r.data_id_child = comp.id "
            + "join data as cont on r.data_id_parent = cont.id "
            + "where cont.code = ?{1} and relationship_id = ?{2}", fetchSize = FETCH_SIZE)
    public DataIterator<String> getContainedDataSetCodes(String dataSetCode, long relationshipTypeId);

    //
    // Entity Properties
    //

    /**
     * Returns all generic property values of all datasets specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT pr.ds_id as entity_id, etpt.prty_id, etpt.script_id, pr.value, "
            + "           pr.cvte_id, pr.mate_prop_id, pr.samp_prop_id, sc.script_type "
            + "      FROM data_set_properties pr"
            + "      JOIN data_set_type_property_types etpt ON pr.dstpt_id=etpt.id"
            + "      LEFT OUTER JOIN scripts sc ON etpt.script_id = sc.id"
            + "     WHERE pr.ds_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds);

    /**
     * Returns property values for specified type code of all datasets specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of sample ids to get the property values for.
     * @param propertyTypeCode type code f properties we want to fetch
     */
    @Select(sql = "SELECT pr.ds_id as entity_id, etpt.prty_id, etpt.script_id, pr.value "
            + "      FROM data_set_properties pr"
            + "      JOIN data_set_type_property_types etpt ON pr.dstpt_id=etpt.id"
            + "      JOIN property_types pt ON etpt.prty_id=pt.id"
            + "     WHERE pr.value is not null AND pr.ds_id = any(?{1}) AND pt.code = ?{2}", parameterBindings = { LongSetMapper.class,
                    TypeMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds, String propertyTypeCode);

    @Select(sql = "with recursive connected_data(id,code,container_id,ordinal,dast_id,location,link_info) as ("
            + "  select distinct d.id,d.code,nullif(r.data_id_parent,d.id),nullif(r.ordinal,r.ordinal),d.dast_id,ed.location,ld.id"
            + "  from data as d left outer join external_data as ed on ed.id = d.id"
            + "  left outer join link_data as ld on ld.id = d.id"
            + "  left outer join data_set_relationships as r on r.data_id_parent=d.id"
            + "  where d.code = ?{1}"
            + "  union all"
            + "    select distinct d.id,d.code,r.data_id_parent,r.ordinal,d.dast_id,ed.location,ld.id"
            + "    from connected_data as cd inner join data_set_relationships as r on r.data_id_parent=cd.id"
            + "    inner join data as d on r.data_id_child=d.id left outer join external_data as ed on ed.id = d.id left outer join link_data as ld on ld.id = d.id"
            + "    where r.relationship_id = ?{2}"
            + ") " +
            "select cd.id,cd.code,cd.container_id,cd.ordinal,cd.location,d.code as data_store_code, d.remote_url as data_store_url, cd.link_info "
            + "from connected_data as cd join data_stores as d on cd.dast_id = d.id", fetchSize = FETCH_SIZE)
    public DataIterator<DatasetLocationNodeRecord> listLocationsByDatasetCode(String datasetCode, long relationshipTypeId);

    @Select(sql = "select m.id as id, m.name as name, m.description as description, p.user_id as owner_name, "
            + " m.private as is_private, m.creation_date as creation_date, ma.data_id as entity_id "
            + " from metaprojects m, metaproject_assignments ma, persons p "
            + " where ma.data_id = any(?{1}) and m.owner = ?{2} and m.id = ma.mepr_id and m.owner = p.id", parameterBindings = {
                    LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MetaProjectWithEntityId> getMetaprojects(LongSet entityIds, Long userId);

    @Select(sql = "SELECT edms.id AS edms_id, edms.code AS edms_code, edms.label AS edms_label, edms.address AS edms_address, "
            + " cc.external_code AS external_code, cc.path as path, cc.git_commit_hash AS hash, cc.git_repository_id AS repository_id "
            + " FROM content_copies cc, external_data_management_systems edms "
            + " WHERE cc.edms_id = edms.id AND cc.data_id = ?{1}")
    public List<ContentCopyRecord> getContentCopiesOf(long id);

}
