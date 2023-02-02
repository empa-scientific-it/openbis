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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.db;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSourceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningDAOFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * @author Piotr Buczek
 */
public class ScreeningDAOFactory implements IScreeningDAOFactory
{
    private static final String TECHNOLOGY = "screening";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ScreeningDAOFactory.class);

    private final IDataSourceProvider dataSourceProvider;

    private final Map<DataSource, IImagingReadonlyQueryDAO> daos =
            new HashMap<DataSource, IImagingReadonlyQueryDAO>();

    public ScreeningDAOFactory(IDataSourceProvider dataSourceProvider)
    {
        this.dataSourceProvider = dataSourceProvider;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("DAO factory for Screening created. Data source provider: "
                    + dataSourceProvider.getClass().getName());
        }
    }

    @Override
    public IImagingReadonlyQueryDAO getImagingQueryDAO(String dssCode)
    {
        DataSource dataSource =
                dataSourceProvider.getDataSourceByDataStoreServerCode(dssCode, TECHNOLOGY);
        IImagingReadonlyQueryDAO dao = daos.get(dataSource);
        if (dao == null)
        {
            dao = QueryTool.getQuery(dataSource, IImagingReadonlyQueryDAO.class);
            daos.put(dataSource, dao);
        }
        return dao;
    }
}
