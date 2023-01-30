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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import java.util.List;

import ch.systemsx.cisd.common.db.mapper.LongSetMapper;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface IDataSetListingQuery extends BaseQuery
{
    public static final int FETCH_SIZE = 1000;

    public static final String RELATIONS_SQL =
            "select dp.code as dp_code, dc.code as dc_code from data_set_relationships r inner join data dp on r.data_id_parent = dp.id inner join data dc on r.data_id_child = dc.id";

    @Select(sql = "select"
            + " ds.*," // This line is here so that we can potentially read access_timestamp field, which might not be present in the database
            + " ds.id as ds_id, ds.code as ds_code, ds.data_set_kind as ds_data_set_kind, ds.registration_timestamp as ds_registration_timestamp,"
            + " ds.modification_timestamp as ds_modification_timestamp,"
            + " prdq.id IS NULL as ds_is_post_registered, "
            + " dt.code as dt_code,"
            + " ex.code as ex_code, "
            + " ed.storage_confirmation as ed_sc,"
            + " sa.code as sa_code, sa.sample_identifier as sa_identifier, sac.code as sac_code,"
            + " pe.first_name as pe_first_name, pe.last_name as pe_last_name, pe.email as pe_email, pe.user_id as pe_user_id,"
            + " mod.first_name as mod_first_name, mod.last_name as mod_last_name, mod.email as mod_email, mod.user_id as mod_user_id,"
            + " pre.code as pre_code, spe.code as spe_code, sps.code as sps_code,"
            + " cc.external_code as ld_external_code, edms.id as edms_id, edms.code as edms_code, edms.label as edms_label, edms.address as edms_address, edms.address_type as edms_address_type"
            + " from data ds inner join data_set_types dt on ds.dsty_id = dt.id"
            + " left outer join experiments ex on ds.expe_id = ex.id"
            + " left outer join external_data ed on ds.id = ed.id"
            + " left outer join link_data ld on ds.id = ld.id"
            + " left outer join content_copies cc on ds.id = cc.data_id"
            + " left outer join external_data_management_systems edms on cc.edms_id = edms.id"
            + " left outer join samples sa on ds.samp_id = sa.id"
            + " left outer join persons pe on ds.pers_id_registerer = pe.id"
            + " left outer join persons mod on ds.pers_id_modifier = mod.id"
            + " left outer join projects pre on ex.proj_id = pre.id"
            + " left outer join spaces spe on pre.space_id = spe.id"
            + " left outer join spaces sps on sa.space_id = sps.id"
            + " left outer join samples sac on sa.samp_id_part_of = sac.id"
            + " left outer join post_registration_dataset_queue prdq on ds.id = prdq.ds_id "
            + " where ds.code = any(?{1})", parameterBindings = { StringArrayMapper.class })
    public List<DataSetRecord> getDataSetMetaData(String[] dataSetCodes);

    // Below the post registration status is not returned as this seems to be only used to return container
    // for which post registration status is not applicable
    @Select(sql = "select r.data_id_child as ds_id, cont.id as ctnr_id, cont.code as ctnr_code "
            + "from data as cont join data_set_relationships as r on r.data_id_parent = cont.id "
            + "where r.data_id_child = any(?{1}) and relationship_id = ?{2}", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<DataSetRecord> getContainers(LongSet ids, long relationShipTypeId);

    @Select(sql = RELATIONS_SQL + " where dc.code = any(?{1}) and r.relationship_id = ?{2}", parameterBindings = { StringArrayMapper.class })
    public List<DataSetRelationRecord> getDataSetParentsCodes(String[] dataSetCodes, long relationshipTypeId);

    @Select(sql = RELATIONS_SQL + " where dp.code = any(?{1}) and r.relationship_id = ?{2}", parameterBindings = { StringArrayMapper.class })
    public List<DataSetRelationRecord> getDataSetChildrenCodes(String[] dataSetCodes, long relationshipTypeId);

    @Select(sql = " select ds.download_url as url, array_agg(d.code::text) as data_set_codes"
            + " from data d left join data_stores ds on ds.id = d.dast_id"
            + " where d.code = any(?{1}) group by ds.download_url", parameterBindings = { StringArrayMapper.class })
    public List<DataSetDownloadRecord> getDownloadURLs(String[] dataSetCodes);

    @Select(sql = " select ds.remote_url as url, array_agg(d.code::text) as data_set_codes"
            + " from data d left join data_stores ds on ds.id = d.dast_id"
            + " where d.code = any(?{1}) group by ds.remote_url", parameterBindings = { StringArrayMapper.class })
    public List<DataSetDownloadRecord> getRemoteURLs(String[] dataSetCodes);

}
