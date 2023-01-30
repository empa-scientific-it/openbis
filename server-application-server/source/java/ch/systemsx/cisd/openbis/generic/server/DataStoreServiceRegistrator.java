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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;

/**
 * Implementation of {@link IDataStoreServiceRegistrator}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServiceRegistrator implements IDataStoreServiceRegistrator
{
    private static Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DataStoreServiceRegistrator.class);

    private final IDAOFactory daoFactory;

    private final Map<String, Map<DataStoreServiceKind, Map<String, DatastoreServiceDescription>>> dataStoreToServicesMap =
            new HashMap<String, Map<DataStoreServiceKind, Map<String, DatastoreServiceDescription>>>();

    public DataStoreServiceRegistrator(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public void setServiceDescriptions(DataStorePE dataStore,
            DatastoreServiceDescriptions serviceDescriptions)
    {
        Set<DataStoreServicePE> dataStoreServices = createDataStoreServices(serviceDescriptions);
        dataStore.setServices(dataStoreServices);
        daoFactory.getDataStoreDAO().createOrUpdateDataStore(dataStore);
        Map<DataStoreServiceKind, Map<String, DatastoreServiceDescription>> serviceKindToServicesMap =
                new HashMap<DataStoreServiceKind, Map<String, DatastoreServiceDescription>>();
        serviceKindToServicesMap.put(DataStoreServiceKind.QUERIES,
                extractMap(serviceDescriptions.getReportingServiceDescriptions()));
        serviceKindToServicesMap.put(DataStoreServiceKind.PROCESSING,
                extractMap(serviceDescriptions.getProcessingServiceDescriptions()));
        dataStoreToServicesMap.put(dataStore.getCode(), serviceKindToServicesMap);
    }

    private Map<String, DatastoreServiceDescription> extractMap(
            List<DatastoreServiceDescription> descriptions)
    {
        Map<String, DatastoreServiceDescription> map =
                new HashMap<String, DatastoreServiceDescription>();
        for (DatastoreServiceDescription description : descriptions)
        {
            map.put(description.getKey(), description);
        }
        return map;
    }

    private Set<DataStoreServicePE> createDataStoreServices(
            DatastoreServiceDescriptions serviceDescriptions)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();
        IDataSetTypeDAO dataSetTypeDAO = daoFactory.getDataSetTypeDAO();
        List<DataSetTypePE> allDataSetTypes = dataSetTypeDAO.listAllEntities();

        Set<DataStoreServicePE> processing =
                createDataStoreServices(serviceDescriptions.getProcessingServiceDescriptions(),
                        DataStoreServiceKind.PROCESSING, allDataSetTypes);
        services.addAll(processing);

        Set<DataStoreServicePE> queries =
                createDataStoreServices(serviceDescriptions.getReportingServiceDescriptions(),
                        DataStoreServiceKind.QUERIES, allDataSetTypes);
        services.addAll(queries);

        return services;
    }

    private Set<DataStoreServicePE> createDataStoreServices(
            List<DatastoreServiceDescription> serviceDescriptions,
            DataStoreServiceKind serviceKind, List<DataSetTypePE> allDataSetTypes)
    {
        Set<DataStoreServicePE> services = new HashSet<DataStoreServicePE>();
        for (DatastoreServiceDescription desc : serviceDescriptions)
        {
            DataStoreServicePE service = new DataStoreServicePE();
            service.setKey(desc.getKey());
            service.setLabel(desc.getLabel());
            service.setKind(serviceKind);
            Set<DataSetTypePE> datasetTypes =
                    extractDataSetTypes(desc.getDatasetTypeCodes(), desc, allDataSetTypes);
            service.setDatasetTypes(datasetTypes);
            service.setReportingPluginTypeOrNull(desc.tryReportingPluginType());
            services.add(service);
        }
        return services;
    }

    private Set<DataSetTypePE> extractDataSetTypes(String[] dataSetTypeCodePatterns,
            DatastoreServiceDescription serviceDescription, List<DataSetTypePE> allDataSetTypes)
    {
        Set<DataSetTypePE> dataSetTypes = new HashSet<DataSetTypePE>();
        Set<String> missingCodes = new HashSet<String>();

        for (String pattern : dataSetTypeCodePatterns)
        {
            boolean found = false;
            // Try to find the specified data set type
            for (DataSetTypePE dataSetType : allDataSetTypes)
            {
                if (dataSetType.getCode().matches(pattern))
                {
                    dataSetTypes.add(dataSetType);
                    found = true;
                }
            }
            if (false == found)
            {
                missingCodes.add(pattern);
            }
        }
        if (missingCodes.size() > 0)
        {
            notifyDataStoreServerMisconfiguration(missingCodes, serviceDescription);
        }
        return dataSetTypes;
    }

    private void notifyDataStoreServerMisconfiguration(Set<String> missingCodes,
            DatastoreServiceDescription serviceDescription)
    {
        String missingCodesText = CollectionUtils.abbreviate(missingCodes, -1);
        notificationLog.warn(String.format("The Datastore Server Plugin '%s' is misconfigured. "
                + "It refers to the dataset types which do not exist in openBIS: %s",
                serviceDescription.toString(), missingCodesText));
    }

    @Override
    public void register(DataSetType dataSetType)
    {
        IDataStoreDAO dataStoreDAO = daoFactory.getDataStoreDAO();

        Set<Entry<String, Map<DataStoreServiceKind, Map<String, DatastoreServiceDescription>>>> entrySet =
                dataStoreToServicesMap.entrySet();
        for (Entry<String, Map<DataStoreServiceKind, Map<String, DatastoreServiceDescription>>> entry : entrySet)
        {
            String dataStoreCode = entry.getKey();
            Map<DataStoreServiceKind, Map<String, DatastoreServiceDescription>> serviceKindToServicesMap =
                    entry.getValue();
            DataStorePE dataStore = dataStoreDAO.tryToFindDataStoreByCode(dataStoreCode);
            Set<DataStoreServicePE> services = dataStore.getServices();
            for (DataStoreServicePE service : services)
            {
                Set<DataSetTypePE> dataSetTypes = service.getDatasetTypes();
                if (containsDataType(dataSetTypes, dataSetType))
                {
                    continue;
                }
                DatastoreServiceDescription description =
                        serviceKindToServicesMap.get(service.getKind()).get(service.getKey());
                if (description == null)
                {
                    continue;
                }
                if (matchesPattern(description, dataSetType))
                {
                    DataSetTypePE newDataSetType =
                            daoFactory.getDataSetTypeDAO().tryToFindDataSetTypeByCode(
                                    dataSetType.getCode());
                    if (newDataSetType != null)
                    {
                        Set<DataSetTypePE> extendedDataSetTypes =
                                new HashSet<DataSetTypePE>(dataSetTypes);
                        extendedDataSetTypes.add(newDataSetType);
                        service.setDatasetTypes(extendedDataSetTypes);
                    }
                }
            }
            dataStoreDAO.createOrUpdateDataStore(dataStore);
        }
    }

    private boolean matchesPattern(DatastoreServiceDescription description, DataSetType dataSetType)
    {
        String[] datasetTypeCodePatterns = description.getDatasetTypeCodes();
        for (String pattern : datasetTypeCodePatterns)
        {
            if (dataSetType.getCode().matches(pattern))
            {
                return true;
            }
        }
        return false;
    }

    private boolean containsDataType(Collection<DataSetTypePE> dataSetTypes, DataSetType dataSetType)
    {
        for (DataSetTypePE dataSetTypePE : dataSetTypes)
        {
            if (dataSetTypePE.getCode().equals(dataSetType.getCode()))
            {
                return true;
            }
        }
        return false;
    }

}
