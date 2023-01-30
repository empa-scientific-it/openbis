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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractCommonServer<T extends IServer> extends AbstractServer<T>
{
    private final IAuthenticationService authenticationService;

    protected final ICommonBusinessObjectFactory businessObjectFactory;

    public AbstractCommonServer(IAuthenticationService authenticationService,
            IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            ICommonBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.authenticationService = authenticationService;
        this.businessObjectFactory = businessObjectFactory;
    }

    protected List<PersonPE> registerPersons(final String sessionToken, final List<String> userIDs)
    {
        final Session session = getSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listByCodes(userIDs);
        if (persons.size() > 0)
        {
            throw UserFailureException.fromTemplate("Following persons already exist: [%s]",
                    StringUtils.join(userIDs, ","));
        }
        List<String> unknownUsers = new ArrayList<String>();
        final DisplaySettings defaultDisplaySettings = getDefaultDisplaySettings(sessionToken);
        List<PersonPE> newPersons = new ArrayList<PersonPE>();
        for (String userID : userIDs)
        {
            try
            {
                final Principal principal = authenticationService.getPrincipal(userID);
                newPersons.add(createPerson(principal, session.tryGetPerson(),
                        defaultDisplaySettings));
            } catch (final IllegalArgumentException e)
            {
                unknownUsers.add(userID);
            }
        }
        if (unknownUsers.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Following persons unknown by the authentication service: [%s]",
                    StringUtils.join(unknownUsers, ","));
        } else
        {
            return newPersons;
        }

    }

    @Deprecated
    /** @deprecated this is legacy code permanently deleting data sets one by one omitting trash */
    protected void permanentlyDeleteDataSets(Session session, IDataSetTable dataSetTable,
            List<String> dataSetCodes, String reason, boolean forceDisallowedTypes)
    {
        // TODO 2011-06-21, Piotr Buczek: loading less for deletion would probably be faster

        // first load by codes to get the ids
        dataSetTable.loadByDataSetCodes(dataSetCodes, false, false);

        List<DataPE> dataSets = dataSetTable.getDataSets();

        if (atLeastOneOfDataSetsIsContainer(dataSets))
        {
            // get recursively all datasets that are contained and contained
            List<TechId> ids = DataSetUtils.getAllDeletableComponentsRecursively(
                    TechId.createList(dataSetTable.getDataSets()), true, createDatasetLister(session),
                    getDAOFactory());

            dataSetTable.loadByIds(ids);

            dataSets = dataSetTable.getDataSets();
        }
        Map<DataSetTypePE, List<DataPE>> groupedDataSets =
                new LinkedHashMap<DataSetTypePE, List<DataPE>>();

        // Check all data sets before deleting group by group. If we delete one group of data sets
        // and find an incorrect data set in another group then we cannot roll back already made
        // deletions in data store server. Therefore we'd better make a check now to minimize a
        // chance of failure.
        DataSetTable.assertDatasetsAreDeletable(dataSets);
        DataSetTable.assertDatasetsWithDisallowedTypes(dataSets, forceDisallowedTypes);

        for (DataPE dataSet : dataSets)
        {
            DataSetTypePE dataSetType = dataSet.getDataSetType();
            List<DataPE> list = groupedDataSets.get(dataSetType);
            if (list == null)
            {
                list = new ArrayList<DataPE>();
                groupedDataSets.put(dataSetType, list);
            }
            list.add(dataSet);
        }
        for (Map.Entry<DataSetTypePE, List<DataPE>> entry : groupedDataSets.entrySet())
        {
            DataSetTypePE dataSetType = entry.getKey();
            IDataSetTypeSlaveServerPlugin plugin = getDataSetTypeSlaveServerPlugin(dataSetType);
            plugin.permanentlyDeleteDataSets(session, entry.getValue(), reason,
                    forceDisallowedTypes);
        }
    }

    private boolean atLeastOneOfDataSetsIsContainer(Collection<DataPE> dataSets)
    {
        for (DataPE dataSet : dataSets)
        {
            if (dataSet.isContainer())
            {
                return true;
            }
        }
        return false;
    }

    public int archiveDatasets(String sessionToken, List<String> datasetCodes,
            boolean deleteFromDataStore, Map<String, String> options)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.archiveDatasets(deleteFromDataStore, options);
    }

    public int unarchiveDatasets(String sessionToken, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.unarchiveDatasets();
    }

    protected IDatasetLister createDatasetLister(Session session)
    {
        return businessObjectFactory.createDatasetLister(session);
    }

}
