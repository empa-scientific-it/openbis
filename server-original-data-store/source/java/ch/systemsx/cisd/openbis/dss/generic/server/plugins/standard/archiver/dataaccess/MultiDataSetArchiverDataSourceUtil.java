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

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetArchiverDataSourceUtil
{
    private static DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource("multi-dataset-archiver-db");

    static IMultiDataSetArchiverQueryDAO getTransactionalQuery()
    {
        return QueryTool.getQuery(dataSource, IMultiDataSetArchiverQueryDAO.class);
    }

    public static IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQueryDAO()
    {
        return QueryTool.getQuery(dataSource, IMultiDataSetArchiverReadonlyQueryDAO.class);
    }

    public static List<String> getContainerList()
    {
        List<MultiDataSetArchiverContainerDTO> containerDTOs = getReadonlyQueryDAO().listContainers();
        List<String> containers = new ArrayList<String>();
        if (containerDTOs != null)
        {
            for (MultiDataSetArchiverContainerDTO containerDTO : containerDTOs)
            {
                containers.add(containerDTO.getPath());
            }
        }
        return containers;
    }

    public static Boolean isDataSetInContainer(String dataSetCode)
    {
        MultiDataSetArchiverDataSetDTO dataSetDTO = getReadonlyQueryDAO().getDataSetForCode(dataSetCode);
        return dataSetDTO != null && dataSetDTO.getContainerId() > 0;
    }

}
