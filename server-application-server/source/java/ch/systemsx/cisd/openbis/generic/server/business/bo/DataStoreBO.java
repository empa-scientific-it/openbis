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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Pawel Glyzewski
 */
public class DataStoreBO implements IDataStoreBO
{
    private IDAOFactory daoFactory;

    private Session session;

    private IDataStoreServiceFactory dssFactory;

    private DataStorePE dataStore;

    private String dataStoreCode;

    public DataStoreBO(IDAOFactory daoFactory, Session session, IDataStoreServiceFactory dssFactory)
    {
        this.daoFactory = daoFactory;
        this.session = session;
        this.dssFactory = dssFactory;
    }

    @Override
    public void loadByCode(String dssCode)
    {
        this.dataStoreCode = dssCode;
        this.dataStore = daoFactory.getDataStoreDAO().tryToFindDataStoreByCode(dssCode);
    }

    @Override
    public void uploadFile(String dropboxName, CustomImportFile customImportFile)
    {
        if (dataStore == null)
        {
            throw new ConfigurationFailureException("The data store " + this.dataStoreCode + " for custom import of a dropbox " + dropboxName
                    + " doesn't exist.");
        }
        IDataStoreService service = dssFactory.create(dataStore.getRemoteUrl());
        service.putDataSet(session.getSessionToken(), dropboxName, customImportFile);
    }
}
