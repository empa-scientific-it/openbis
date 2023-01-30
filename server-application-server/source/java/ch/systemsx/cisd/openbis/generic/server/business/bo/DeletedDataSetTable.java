package ch.systemsx.cisd.openbis.generic.server.business.bo;

/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.exception.DataSetDeletionDisallowedTypesException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;

/**
 * BO for handling {@link DataPE}.
 * 
 * @author Piotr Buczek
 */
public final class DeletedDataSetTable extends AbstractDataSetBusinessObject implements
        IDeletedDataSetTable
{

    private static void assertDatasetsAreDeletable(List<DeletedDataPE> datasets)
    {
        List<String> notDeletableDatasets = new ArrayList<String>();
        for (DeletedDataPE dataSet : datasets)
        {
            if (dataSet.isDeletable() == false)
            {
                notDeletableDatasets.add(dataSet.getCode());
            }
        }
        if (notDeletableDatasets.isEmpty() == false)
        {
            throw UserFailureException.fromTemplate(
                    "Deletion failed because the following data sets are required "
                            + "by a background process (their status is pending): %s. ",
                    CollectionUtils.abbreviate(notDeletableDatasets, 10));
        }
    }

    private static void assertDatasetsWithDisallowedTypes(List<DeletedDataPE> datasets,
            boolean forceDisallowedTypes)
    {
        if (forceDisallowedTypes)
        {
            return;
        }

        List<String> datasetsWithDisallowedTypes = new ArrayList<String>();
        for (DeletedDataPE dataSet : datasets)
        {
            if (dataSet.getDataSetType().isDeletionDisallow())
            {
                datasetsWithDisallowedTypes.add(dataSet.getCode());
            }
        }
        if (datasetsWithDisallowedTypes.isEmpty() == false)
        {
            throw new DataSetDeletionDisallowedTypesException(datasetsWithDisallowedTypes);
        }
    }

    private List<DeletedDataPE> deletedDataSets;

    public DeletedDataSetTable(final IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory,
            final Session session, IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        super(daoFactory, session, relationshipService, conversationClient, entityInformationProvider,
                managedPropertyEvaluatorFactory, new DataSetTypeWithoutExperimentChecker(new Properties()));
    }

    //
    // IExternalDataTable
    //

    @Override
    public void loadByDataSetCodes(List<String> dataSetCodes)
    {
        IDataDAO dataDAO = getDataDAO();

        deletedDataSets = new ArrayList<DeletedDataPE>();
        deletedDataSets.addAll(dataDAO.tryToFindDeletedDataSetsByCodes(dataSetCodes));
    }

    @Override
    public void permanentlyDeleteLoadedDataSets(String reason, boolean forceDisallowedTypes)
    {
        assertDatasetsAreDeletable(deletedDataSets);
        assertDatasetsWithDisallowedTypes(deletedDataSets, forceDisallowedTypes);

        Map<DataStorePE, List<DeletedDataPE>> allToBeDeleted = groupDataSetsByDataStores();
        for (Map.Entry<DataStorePE, List<DeletedDataPE>> entry : allToBeDeleted.entrySet())
        {
            List<DeletedDataPE> allDataSets = entry.getValue();
            deleteLocallyFromDB(reason, allDataSets);
        }
    }

    private void deleteLocallyFromDB(String reason, List<DeletedDataPE> dataSetsToDelete)
    {
        deleteByTechIds(TechId.createList(dataSetsToDelete), reason);
    }

    private void deleteByTechIds(List<TechId> dataSetIds, String reason)
            throws UserFailureException
    {
        try
        {
            getSessionFactory().getCurrentSession().flush();
            getSessionFactory().getCurrentSession().clear();
            getDataDAO().delete(dataSetIds, session.tryGetPerson(), reason);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Data Set", EntityKind.DATA_SET);
        }
    }

    /**
     * groups all deleted data sets (both virtual and non-virtual) by data stores
     * 
     * @param deletedDataSets
     */
    private Map<DataStorePE, List<DeletedDataPE>> groupDataSetsByDataStores()
    {
        Map<DataStorePE, List<DeletedDataPE>> map =
                new LinkedHashMap<DataStorePE, List<DeletedDataPE>>();
        for (DeletedDataPE dataSet : deletedDataSets)
        {
            DataStorePE dataStore = dataSet.getDataStore();
            List<DeletedDataPE> list = map.get(dataStore);
            if (list == null)
            {
                list = new ArrayList<DeletedDataPE>();
                map.put(dataStore, list);
            }
            list.add(dataSet);
        }
        return map;
    }

}