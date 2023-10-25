/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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

import java.util.List;

import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;
import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

/**
 * @author Jakub Straszewski
 */
public interface IMultiDataSetArchiverReadonlyQueryDAO extends BaseQuery
{

    /*
     * SELECT CONTAINER
     */
    final static String SELECT_CONTAINER =
            " SELECT id, path, unarchiving_requested "
                    + "FROM containers ";

    @Select(sql = SELECT_CONTAINER + "WHERE id = ?{1}")
    public MultiDataSetArchiverContainerDTO getContainerForId(long containerId);

    @Select(sql = SELECT_CONTAINER)
    public List<MultiDataSetArchiverContainerDTO> listContainers();

    @Select(sql = SELECT_CONTAINER + " ORDER BY ID ASC")
    public List<MultiDataSetArchiverContainerDTO> listContainersInChronologicalOrder();

    @Select(sql = SELECT_CONTAINER + " ORDER BY random()")
    public List<MultiDataSetArchiverContainerDTO> listContainersInRandomOrder();

    @Select(sql = SELECT_CONTAINER + "WHERE unarchiving_requested = 't'")
    public List<MultiDataSetArchiverContainerDTO> listContainersForUnarchiving();

    @Select(sql = SELECT_CONTAINER + "WHERE id in (" +
            "select ctnr_id " +
            "from data_sets " +
            "where code = any(?{1})" +
            ")", parameterBindings = { StringArrayMapper.class })
    public List<MultiDataSetArchiverContainerDTO> listContainersWithDataSets(String[] dataSetCodes);

    /*
     * SELECT DATA_SET
     */

    final static String SELECT_DATA_SET =
            " SELECT id, code, ctnr_id, size_in_bytes "
                    + "FROM data_sets ";

    @Select(sql = SELECT_DATA_SET + "WHERE id = ?{1}")
    public MultiDataSetArchiverDataSetDTO getDataSetForId(long dataSetId);

    @Select(sql = SELECT_DATA_SET + "WHERE code = ?{1}")
    public MultiDataSetArchiverDataSetDTO getDataSetForCode(String code);

    @Select(sql = SELECT_DATA_SET + "WHERE ctnr_id = ?{1}")
    public List<MultiDataSetArchiverDataSetDTO> listDataSetsForContainerId(long containerId);

    @Select(sql = "SELECT SUM(size_in_bytes) FROM data_sets, containers "
            + "WHERE data_sets.ctnr_id = containers.id"
            + " AND unarchiving_requested = 't'")
    public long getTotalNoOfBytesInContainersWithUnarchivingRequested();
}
