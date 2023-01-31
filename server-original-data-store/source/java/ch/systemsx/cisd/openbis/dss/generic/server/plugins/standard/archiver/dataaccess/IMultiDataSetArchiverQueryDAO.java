/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;

/**
 * @author Jakub Straszewski
 */
public interface IMultiDataSetArchiverQueryDAO extends TransactionQuery, IMultiDataSetArchiverReadonlyQueryDAO
{
    /*
     * UPDATES / INSERTS
     */

    final static String INSERT_CONTAINER = "INSERT INTO containers "
            + "(path) "
            + "values "
            + "(?{1.path}) "
            + "RETURNING ID";

    @Select(sql = INSERT_CONTAINER)
    public long addContainer(MultiDataSetArchiverContainerDTO container);

    // updates

    final static String UPDATE_CONTAINER = "UPDATE containers "
            + "SET "
            + "path = ?{1.path}, "
            + "where ID = ?{1.id}";

    @Update(UPDATE_CONTAINER)
    public void updateContainer(MultiDataSetArchiverContainerDTO container);

    final static String INSERT_DATA_SET = "INSERT INTO data_sets "
            + "(code, ctnr_id, size_in_bytes) "
            + "VALUES "
            + "(?{1.code}, ?{1.containerId}, ?{1.sizeInBytes}) "
            + "RETURNING ID";

    @Select(sql = INSERT_DATA_SET)
    public long addDataSet(MultiDataSetArchiverDataSetDTO dataSet);

    final static String DELETE_CONTAINER = "DELETE FROM containers where path = ?{1}";

    @Update(sql = DELETE_CONTAINER)
    public void deleteContainer(String containerPath);

    final static String DELETE_CONTAINER_BY_ID = "DELETE FROM containers where id = ?{1}";

    @Update(sql = DELETE_CONTAINER_BY_ID)
    public void deleteContainer(long containerId);

    final static String REQUEST_UNARCHIVING = "UPDATE containers SET unarchiving_requested = 't' "
            + "WHERE id in (SELECT ctnr_id FROM data_sets WHERE code = any(?{1}))";

    @Update(sql = REQUEST_UNARCHIVING, parameterBindings = { StringArrayMapper.class })
    public void requestUnarchiving(String[] dataSetCodes);

    final static String RESET_REQUEST_UNARCHIVING = "UPDATE containers SET unarchiving_requested = 'f' WHERE id = ?{1}";

    @Update(sql = RESET_REQUEST_UNARCHIVING)
    public void resetRequestUnarchiving(long containerId);
}
