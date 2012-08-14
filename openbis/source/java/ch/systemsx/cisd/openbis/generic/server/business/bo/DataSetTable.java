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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator.translateToDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.exception.DataSetDeletionDisallowedTypesException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.exception.DataSetDeletionUnknownLocationsException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.event.DeleteDataSetEventBuilder;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link DataPE}.
 * <p>
 * We are using an interface here to keep the system testable.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class DataSetTable extends AbstractDataSetBusinessObject implements IDataSetTable
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetTable.class);

    @Private
    static final String UPLOAD_COMMENT_TEXT = "Uploaded zip file contains the following data sets:";

    @Private
    static final String NEW_LINE = "\n";

    @Private
    static final String AND_MORE_TEMPLATE = "and %d more.";

    private boolean dataChanged;

    @Private
    static String createUploadComment(List<? extends DataPE> dataSets)
    {
        StringBuilder builder = new StringBuilder(UPLOAD_COMMENT_TEXT);
        for (int i = 0, n = dataSets.size(); i < n; i++)
        {
            builder.append(NEW_LINE);
            String code = dataSets.get(i).getCode();
            int length = builder.length() + code.length();
            if (i < n - 1)
            {
                length += NEW_LINE.length() + String.format(AND_MORE_TEMPLATE, n - i - 1).length();
            }
            if (length < BasicConstant.MAX_LENGTH_OF_CIFEX_COMMENT)
            {
                builder.append(code);
            } else
            {
                builder.append(String.format(AND_MORE_TEMPLATE, n - i));
                break;
            }
        }
        return builder.toString();
    }

    private static void assertDatasetsAreAvailable(List<DataPE> datasets)
    {
        List<String> notAvailableDatasets = new ArrayList<String>();
        for (DataPE dataSet : datasets)
        {
            if (dataSet.isExternalData() && dataSet.isAvailable() == false)
            {
                notAvailableDatasets.add(dataSet.getCode());
            }
        }
        throwUnavailableOperationExceptionIfNecessary(notAvailableDatasets);
    }

    private static void throwUnavailableOperationExceptionIfNecessary(
            List<String> unavailableDatasets)
    {
        if (unavailableDatasets.isEmpty() == false)
        {
            throw UserFailureException.fromTemplate(
                    "Operation failed because following data sets are not available "
                            + "(they are archived or their status is pending): %s. "
                            + "Unarchive these data sets or filter them out using data set status "
                            + "before performing the operation once again.",
                    CollectionUtils.abbreviate(unavailableDatasets, 10));
        }
    }

    public static void assertDatasetsAreDeletable(List<DataPE> datasets)
    {
        List<String> notDeletableDatasets = new ArrayList<String>();
        for (DataPE dataSet : datasets)
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

    public static void assertDatasetsWithDisallowedTypes(List<DataPE> datasets,
            boolean forceDisallowedTypes)
    {
        if (forceDisallowedTypes)
        {
            return;
        }

        List<String> datasetsWithDisallowedTypes = new ArrayList<String>();
        for (DataPE dataSet : datasets)
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

    private final IDataStoreServiceFactory dssFactory;

    private List<DataPE> dataSets;

    public DataSetTable(IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory,
            Session session, IRelationshipService relationshipService)
    {
        super(daoFactory, session, relationshipService);
        this.dssFactory = dssFactory;
    }

    //
    // IExternalDataTable
    //

    @Override
    public final List<DataPE> getDataSets()
    {
        assert dataSets != null : "Data Sets not loaded.";
        return dataSets;
    }

    @Override
    public List<ExternalDataPE> getNonDeletableExternalDataSets()
    {
        List<ExternalDataPE> result = new ArrayList<ExternalDataPE>();
        for (DataPE dataSet : dataSets)
        {
            final ExternalDataPE externalDataSet = dataSet.tryAsExternalData();
            if (externalDataSet != null && externalDataSet.isDeletable() == false)
            {
                result.add(externalDataSet);
            }
        }
        return result;
    }

    @Override
    public final List<ExternalDataPE> getExternalData()
    {
        assert dataSets != null : "Data Sets not loaded.";
        final List<ExternalDataPE> result = new ArrayList<ExternalDataPE>();
        for (DataPE dataSet : dataSets)
        {
            if (dataSet.isExternalData())
                result.add(dataSet.tryAsExternalData());
        }
        return result;
    }

    @Override
    public void setDataSets(List<DataPE> dataSets)
    {
        this.dataSets = dataSets;
    }

    @Override
    public void loadByDataSetCodes(List<String> dataSetCodes, boolean withProperties,
            boolean lockForUpdate)
    {
        IDataDAO dataDAO = getDataDAO();

        dataSets = new ArrayList<DataPE>();
        dataSets.addAll(dataDAO.tryToFindFullDataSetsByCodes(dataSetCodes, withProperties,
                lockForUpdate));
    }

    @Override
    public void loadByIds(List<TechId> ids)
    {
        IDataDAO dataDAO = getDataDAO();

        dataSets = new ArrayList<DataPE>();
        dataSets.addAll(dataDAO.tryToFindFullDataSetsByIds(TechId.asLongs(ids), false, false));
    }

    @Override
    public final void loadBySampleTechId(final TechId sampleId)
    {
        assert sampleId != null : "Unspecified sample id";
        final SamplePE sample = getSampleDAO().getByTechId(sampleId);
        dataSets = new ArrayList<DataPE>();
        dataSets.addAll(getDataDAO().listDataSets(sample));
    }

    @Override
    public void loadByExperimentTechId(final TechId experimentId)
    {
        assert experimentId != null : "Unspecified experiment id";

        ExperimentPE experiment = getExperimentDAO().getByTechId(experimentId);
        dataSets = new ArrayList<DataPE>();
        dataSets.addAll(getDataDAO().listDataSets(experiment));
    }

    @Override
    public void deleteLoadedDataSets(String reason, boolean forceNotExistingLocations,
            boolean forceDisallowedTypes)
    {
        assertDatasetsAreDeletable(dataSets);
        assertDatasetsWithDisallowedTypes(dataSets, forceDisallowedTypes);

        Map<DataStorePE, List<DataPE>> allToBeDeleted = groupDataSetsByDataStores();
        Map<DataStorePE, List<ExternalDataPE>> availableDatasets =
                filterAvailableDatasets(allToBeDeleted);

        assertDataSetsAreKnown(availableDatasets, forceNotExistingLocations);
        for (Map.Entry<DataStorePE, List<DataPE>> entry : allToBeDeleted.entrySet())
        {
            List<DataPE> allDataSets = entry.getValue();

            // delete locally from DB
            for (DataPE dataSet : allDataSets)
            {
                deleteDataSetLocally(dataSet, reason);
            }
        }
    }

    private Map<DataStorePE, List<ExternalDataPE>> filterAvailableDatasets(
            Map<DataStorePE, List<DataPE>> map)
    {
        Map<DataStorePE, List<ExternalDataPE>> result =
                new HashMap<DataStorePE, List<ExternalDataPE>>();
        for (Map.Entry<DataStorePE, List<DataPE>> entry : map.entrySet())
        {
            ArrayList<ExternalDataPE> available = new ArrayList<ExternalDataPE>();
            for (DataPE data : entry.getValue())
            {
                ExternalDataPE externalData = data.tryAsExternalData();
                if (externalData != null && externalData.isAvailable())
                {

                    available.add(externalData);
                }
            }
            result.put(entry.getKey(), available);
        }
        return result;
    }

    private void deleteDataSetLocally(DataPE dataSet, String reason) throws UserFailureException
    {
        try
        {
            getDataDAO().delete(dataSet);

            DeleteDataSetEventBuilder builder =
                    new DeleteDataSetEventBuilder(dataSet, session.tryGetPerson());
            builder.setReason(reason);

            getEventDAO().persist(builder.getEvent());

        } catch (final DataIntegrityViolationException ex)
        {
            // needed because we throw an exception in DAO instead of relying on DB FK integrity
            throwEntityInUseException(String.format("Data Set '%s'", dataSet.getCode()),
                    EntityKind.DATA_SET);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Data Set '%s'", dataSet.getCode()),
                    EntityKind.DATA_SET);
        }
    }

    @Override
    public String uploadLoadedDataSetsToCIFEX(DataSetUploadContext uploadContext)
    {
        assertDatasetsAreAvailable(dataSets);
        Map<DataStorePE, List<DataPE>> map = groupDataByDataStores();
        assertDataSetsAreKnown(map, false);
        uploadContext.setUserEMail(session.getPrincipal().getEmail());
        uploadContext.setSessionUserID(session.getUserName());
        if (StringUtils.isBlank(uploadContext.getComment()))
        {
            uploadContext.setComment(createUploadComment(dataSets));
        }
        List<DataPE> dataSetsWithUnknownDSS = new ArrayList<DataPE>();
        for (Map.Entry<DataStorePE, List<DataPE>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<DataPE> dataSetsOfStore = entry.getValue();
            for (DataPE dataSet : dataSetsOfStore)
            {
                HibernateUtils.initialize(dataSet.getParents());
                HibernateUtils.initialize(dataSet.getProperties());
                SamplePE sampleOrNull = dataSet.tryGetSample();
                ExperimentPE experiment;
                if (sampleOrNull != null) // needed? dataSet should always have experiment
                {
                    experiment = sampleOrNull.getExperiment();
                } else
                {
                    experiment = dataSet.getExperiment();
                }
                HibernateUtils.initialize(experiment.getProject().getSpace());
                HibernateUtils.initialize(experiment.getProperties());
            }
            if (StringUtils.isBlank(dataStore.getRemoteUrl()))
            {
                dataSetsWithUnknownDSS.addAll(dataSetsOfStore);
            } else
            {
                uploadDataSetsToCIFEX(dataStore, dataSetsOfStore, uploadContext);
            }
        }
        StringBuilder builder = new StringBuilder();
        if (dataSetsWithUnknownDSS.isEmpty() == false)
        {
            builder.append("The following data sets couldn't been uploaded because of unkown data store:");
            for (DataPE dataSet : dataSetsWithUnknownDSS)
            {
                builder.append(' ').append(dataSet.getCode());
            }
        }
        return builder.toString();
    }

    private <D extends DataPE> void assertDataSetsAreKnown(Map<DataStorePE, List<D>> map,
            boolean ignoreNonExistingLocation)
    {
        // Set<String> knownLocations = new LinkedHashSet<String>();
        List<String> unknownDataSets = new ArrayList<String>();
        for (Map.Entry<DataStorePE, List<D>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<ExternalDataPE> externalDatas = filterRealDataSets(entry.getValue());
            Set<String> knownLocations =
                    getKnownDataSets(dataStore,
                            DataSetTranslator.translateToDescriptions(externalDatas),
                            ignoreNonExistingLocation);
            for (ExternalDataPE dataSet : externalDatas)
            {
                if (dataSet.getStatus() == DataSetArchivingStatus.ARCHIVED)
                {
                    // archived datasets are currently not available in the data store
                    // but can be deleted
                } else if (knownLocations.contains(dataSet.getLocation()) == false)
                {
                    unknownDataSets.add(dataSet.getCode());
                }
            }
        }
        if (unknownDataSets.isEmpty() == false)
        {
            throw new DataSetDeletionUnknownLocationsException(unknownDataSets);
        }
    }

    private List<ExternalDataPE> filterRealDataSets(List<? extends DataPE> mixedDataSets)
    {
        List<ExternalDataPE> realDataSets = new ArrayList<ExternalDataPE>();
        for (DataPE dataSet : mixedDataSets)
        {
            if (dataSet instanceof ExternalDataPE)
            {
                realDataSets.add((ExternalDataPE) dataSet);
            }
        }
        return realDataSets;
    }

    /** groups data sets by data stores */
    private Map<DataStorePE, List<DataPE>> groupDataByDataStores()
    {
        Map<DataStorePE, List<DataPE>> map = new LinkedHashMap<DataStorePE, List<DataPE>>();
        for (DataPE dataSet : dataSets)
        {
            DataStorePE dataStore = dataSet.getDataStore();
            List<DataPE> list = map.get(dataStore);
            if (list == null)
            {
                list = new ArrayList<DataPE>();
                map.put(dataStore, list);
            }
            list.add(dataSet);
        }
        return map;
    }

    /** groups data sets by data stores filtering out container data sets */
    private Map<DataStorePE, List<ExternalDataPE>> groupExternalDataByDataStores()
    {
        Map<DataStorePE, List<ExternalDataPE>> map =
                new LinkedHashMap<DataStorePE, List<ExternalDataPE>>();
        for (DataPE dataSet : dataSets)
        {
            if (dataSet.isExternalData())
            {
                DataStorePE dataStore = dataSet.getDataStore();
                List<ExternalDataPE> list = map.get(dataStore);
                if (list == null)
                {
                    list = new ArrayList<ExternalDataPE>();
                    map.put(dataStore, list);
                }
                list.add(dataSet.tryAsExternalData());
            }
        }
        return map;
    }

    /** groups all data sets (both virtual and non-virtual) by data stores */
    private Map<DataStorePE, List<DataPE>> groupDataSetsByDataStores()
    {
        Map<DataStorePE, List<DataPE>> map = new LinkedHashMap<DataStorePE, List<DataPE>>();
        for (DataPE dataSet : dataSets)
        {
            DataStorePE dataStore = dataSet.getDataStore();
            List<DataPE> list = map.get(dataStore);
            if (list == null)
            {
                list = new ArrayList<DataPE>();
                map.put(dataStore, list);
            }
            list.add(dataSet);
        }
        return map;
    }

    private void uploadDataSetsToCIFEX(DataStorePE dataStore, List<DataPE> list,
            DataSetUploadContext context)
    {
        IDataStoreService service = dssFactory.create(dataStore.getRemoteUrl());
        String sessionToken = dataStore.getSessionToken();
        List<ExternalData> cleanDataSets = DataSetTranslator.translate(list, "?", "?");
        service.uploadDataSetsToCIFEX(sessionToken, cleanDataSets, context);
    }

    // null if DSS URL has not been specified
    private IDataStoreService tryGetDataStoreService(DataStorePE dataStore)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            return null;
        }
        return dssFactory.create(remoteURL);
    }

    private Set<String> getKnownDataSets(DataStorePE dataStore,
            List<DatasetDescription> dataSetDescriptions, boolean ignoreNonExistingLocation)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            // Assuming dummy data store "knows" all locations
            Set<String> locations = new HashSet<String>();
            for (DatasetDescription dataSet : dataSetDescriptions)
            {
                locations.add(dataSet.getDataSetLocation());
            }
            return locations;
        }
        IDataStoreService service = dssFactory.create(remoteURL);
        String sessionToken = dataStore.getSessionToken();
        return new HashSet<String>(service.getKnownDataSets(sessionToken, dataSetDescriptions,
                ignoreNonExistingLocation));
    }

    @Override
    public void processDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes, Map<String, String> parameterBindings)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);
        String sessionToken = dataStore.getSessionToken();
        String userSessionToken = session.getSessionToken();
        parameterBindings.put(Constants.USER_PARAMETER, session.tryGetPerson().getUserId());
        service.processDatasets(sessionToken, userSessionToken, datastoreServiceKey, locations,
                parameterBindings, tryGetLoggedUserEmail());
    }

    private String tryGetLoggedUserEmail()
    {
        return session.tryGetPerson() == null ? null : session.tryGetPerson().getEmail();
    }

    private ConfigurationFailureException createUnknownDataStoreServerException()
    {
        return new ConfigurationFailureException(
                "Connection to Data Store Server has not been configured. "
                        + "Conntact your administrator.");
    }

    @Override
    public TableModel createReportFromDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);
        String sessionToken = dataStore.getSessionToken();
        String userSessionToken = session.getSessionToken();
        return service.createReportFromDatasets(sessionToken, userSessionToken,
                datastoreServiceKey, locations, tryGetLoggedUserEmail());
    }

    private List<DatasetDescription> loadAvailableDatasetDescriptions(List<String> dataSetCodes)
    {
        IDataDAO dataDAO = getDataDAO();
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        List<String> notAvailableDatasets = new ArrayList<String>();
        List<DataPE> data = dataDAO.tryToFindFullDataSetsByCodes(dataSetCodes, false, false);
        for (DataPE dataSet : data)
        {
            if (dataSet.isExternalData())
            {
                ExternalDataPE externalData = dataSet.tryAsExternalData();
                if (externalData.getStatus().isAvailable())
                {
                    result.add(translateToDescription(externalData));
                } else
                {
                    notAvailableDatasets.add(dataSet.getCode());
                }
            } else
            {
                result.add(translateToDescription(dataSet));
            }
        }
        throwUnavailableOperationExceptionIfNecessary(notAvailableDatasets);
        return result;
    }

    private DataStorePE findDataStore(String datastoreCode)
    {
        DataStorePE dataStore = getDataStoreDAO().tryToFindDataStoreByCode(datastoreCode);
        if (dataStore == null)
        {
            throw new IllegalStateException("Cannot find the data store " + datastoreCode);
        }
        return dataStore;
    }

    @Override
    public void loadByDataStore(DataStorePE dataStore)
    {
        assert dataStore != null : "Unspecified data store";
        assert dataSets == null : "Data already loaded";
        dataSets = new ArrayList<DataPE>();
        dataSets.addAll(getDataDAO().listExternalData(dataStore));
    }

    //
    // Archiving
    //

    @Override
    public int archiveDatasets(boolean removeFromDataStore)
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupExternalDataByDataStores();
        Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService =
                enrichWithService(datasetsByStore);

        DataSetArchivingStatus pendingStatus =
                (removeFromDataStore) ? DataSetArchivingStatus.ARCHIVE_PENDING
                        : DataSetArchivingStatus.BACKUP_PENDING;
        int result =
                filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.AVAILABLE,
                        pendingStatus);
        performArchiving(datasetsWithService, removeFromDataStore);
        return result;
    }

    @Override
    public int unarchiveDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupExternalDataByDataStores();
        Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService =
                enrichWithService(datasetsByStore);
        int result =
                filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.ARCHIVED,
                        DataSetArchivingStatus.UNARCHIVE_PENDING);
        performUnarchiving(datasetsWithService);
        return result;
    }

    @Override
    public int lockDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupExternalDataByDataStores();
        return filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.AVAILABLE,
                DataSetArchivingStatus.LOCKED);
    }

    @Override
    public int unlockDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupExternalDataByDataStores();
        return filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.LOCKED,
                DataSetArchivingStatus.AVAILABLE);
    }

    private int filterByStatusAndUpdate(Map<DataStorePE, List<ExternalDataPE>> datasetsByStore,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus)
    {
        List<String> codesToUpdate = new ArrayList<String>();
        IDataDAO dataDAO = getDataDAO();
        for (List<ExternalDataPE> data : datasetsByStore.values())
        {
            Iterator<ExternalDataPE> iterator = data.iterator();
            while (iterator.hasNext())
            {
                ExternalDataPE dataSet = iterator.next();
                if (dataSet.getStatus() != oldStatus)
                {
                    iterator.remove();
                } else
                {
                    codesToUpdate.add(dataSet.getCode());
                }
            }
        }
        // WORKAROUND In order not to load data set properties at the end of transaction
        // for Hibernate Search indexing, we don't make change to PE's loaded by Hibernate,
        // but perform a 'bulk update' operation. Such an operation is quicker and Hibernate
        // Search doesn't spot the change. The drawback is that the loaded objects are
        // not updated with a new status.
        dataDAO.updateDataSetStatuses(codesToUpdate, newStatus);
        return codesToUpdate.size();
    }

    private interface IArchivingAction
    {
        public void execute(String sessionToken, IDataStoreService service,
                List<DatasetDescription> descriptions, String userEmailOrNull);

        public DataSetArchivingStatus getStatusToRestoreOnFailure();
    }

    private void performUnarchiving(Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore)
    {
        performArchivingAction(datasetsByStore, new IArchivingAction()
            {
                @Override
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userEmailOrNull)
                {
                    service.unarchiveDatasets(sessionToken, descriptions, userEmailOrNull);
                }

                @Override
                public DataSetArchivingStatus getStatusToRestoreOnFailure()
                {
                    return DataSetArchivingStatus.ARCHIVED;
                }

            });

    }

    private void performArchiving(Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore,
            final boolean removeFromDataStore)
    {
        performArchivingAction(datasetsByStore, new IArchivingAction()
            {
                @Override
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userEmailOrNull)
                {
                    service.archiveDatasets(sessionToken, descriptions, userEmailOrNull,
                            removeFromDataStore);
                }

                @Override
                public DataSetArchivingStatus getStatusToRestoreOnFailure()
                {
                    return DataSetArchivingStatus.AVAILABLE;
                }
            });
    }

    private static class DataStoreWithService
    {
        DataStorePE dataStore;

        IDataStoreService service;
    }

    private Map<DataStoreWithService, List<ExternalDataPE>> enrichWithService(
            Map<DataStorePE, List<ExternalDataPE>> datasetsByStore)
    {

        Map<DataStoreWithService, List<ExternalDataPE>> result =
                new HashMap<DataStoreWithService, List<ExternalDataPE>>();
        for (Entry<DataStorePE, List<ExternalDataPE>> entry : datasetsByStore.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            IDataStoreService service = tryGetDataStoreService(dataStore);
            if (service == null)
            {
                throw createUnknownDataStoreServerException();
            }
            List<ExternalDataPE> datasets = entry.getValue();
            DataStoreWithService dataStoreWithService = new DataStoreWithService();
            dataStoreWithService.dataStore = dataStore;
            dataStoreWithService.service = service;

            result.put(dataStoreWithService, datasets);
        }
        return result;
    }

    private void performArchivingAction(
            Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore,
            IArchivingAction archivingAction)
    {
        Iterator<Entry<DataStoreWithService, List<ExternalDataPE>>> iterator =
                datasetsByStore.entrySet().iterator();

        while (iterator.hasNext())
        {
            Entry<DataStoreWithService, List<ExternalDataPE>> entry = iterator.next();
            DataStorePE dataStore = entry.getKey().dataStore;
            IDataStoreService service = entry.getKey().service;
            List<ExternalDataPE> datasets = entry.getValue();
            if (datasets.isEmpty())
            {
                continue;
            }
            List<DatasetDescription> descriptions =
                    DataSetTranslator.translateToDescriptions(datasets);
            String sessionToken = dataStore.getSessionToken();
            String userEmailOrNull = tryGetLoggedUserEmail();
            try
            {
                archivingAction.execute(sessionToken, service, descriptions, userEmailOrNull);
            } catch (Exception e)
            {
                operationLog.error("Operation failed for the following data sets: "
                        + CollectionUtils.abbreviate(Code.extractCodes(datasets), 10), e);
                clearPendingStatuses(datasets, iterator,
                        archivingAction.getStatusToRestoreOnFailure());
                throw UserFailureException
                        .fromTemplate(
                                e,
                                "Operation couldn't be performed for following datasets: %s. "
                                        + "Archiver may not be configured properly. Please contact your administrator.",
                                CollectionUtils.abbreviate(Code.extractCodes(datasets), 10));
            }
        }
    }

    /**
     * clear the pending status of data sets when the remote invocation to DSS has failed.
     */
    private void clearPendingStatuses(List<ExternalDataPE> datasets,
            Iterator<Entry<DataStoreWithService, List<ExternalDataPE>>> iterator,
            DataSetArchivingStatus statusToRestoreOnFailure)
    {
        ArrayList<ExternalDataPE> datasetsWithPendingStatus =
                new ArrayList<ExternalDataPE>(datasets);
        while (iterator.hasNext())
        {
            datasetsWithPendingStatus.addAll(iterator.next().getValue());
        }
        List<String> codes = Code.extractCodes(datasetsWithPendingStatus);
        getDataDAO().updateDataSetStatuses(codes, statusToRestoreOnFailure);
    }

    @Override
    public void save()
    {
        assert dataChanged == true : "Data not changed";
        assert dataSets != null : "Undefined data sets.";
        try
        {
            checkMandatoryProperties();
            IDataDAO dataDAO = getDataDAO();
            dataDAO.updateDataSets(dataSets, findPerson());
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of data sets"));
        }
        dataChanged = false;
        operationLog.info("State of data sets saved.");
    }

    private void checkMandatoryProperties()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (DataPE s : dataSets)
        {
            entityPropertiesConverter.checkMandatoryProperties(s.getProperties(),
                    s.getDataSetType(), cache);
        }
    }

    @Override
    public void checkBeforeUpdate(List<DataSetBatchUpdatesDTO> updates)
    {
        if (updates == null)
        {
            throw new IllegalArgumentException("Data set updates list cannot be null.");
        }

        loadByDataSetCodes(Code.extractCodes(updates), true, true);

        Map<String, Date> versionsMap = new HashMap<String, Date>();
        for (DataPE dataSet : dataSets)
        {
            versionsMap.put(dataSet.getCode(), dataSet.getModificationDate());
        }

        for (DataSetBatchUpdatesDTO update : updates)
        {
            if (update.getCode() == null)
            {
                throw new UserFailureException(
                        "Data set doesn't have a specified code and therefore cannot be updated.");
            }

            Date version = versionsMap.get(update.getCode());

            if (version == null)
            {
                throw new UserFailureException("Data set with code " + update.getCode()
                        + " is not in the database and therefore cannot be updated.");
            } else if (version.equals(update.getVersion()) == false)
            {
                StringBuffer sb = new StringBuffer();
                sb.append("Data set ");
                sb.append(update.getCode());
                sb.append(" has been updated since it was retrieved.\n");
                sb.append("[Current: " + version);
                sb.append(", Retrieved: " + update.getVersion());
                sb.append("]");
                throw new EnvironmentFailureException(sb.toString());
            }
        }
    }

    @Override
    public void update(List<DataSetBatchUpdatesDTO> updates)
    {
        assert updates != null : "Unspecified updates.";

        setBatchUpdateMode(true);
        loadByDataSetCodes(Code.extractCodes(updates), true, true);

        final Map<String, DataPE> dataSetsByCode = new HashMap<String, DataPE>();
        for (DataPE dataSet : dataSets)
        {
            dataSetsByCode.put(dataSet.getIdentifier(), dataSet);
        }
        for (DataSetBatchUpdatesDTO dataSetUpdates : updates)
        {
            final DataPE dataSet = dataSetsByCode.get(dataSetUpdates.getCode());
            prepareBatchUpdate(dataSet, dataSetUpdates);
        }
        setBatchUpdateMode(false);
        dataChanged = true;
        operationLog.info("External data updated");

    }

    private DataPE prepareBatchUpdate(DataPE dataSet, DataSetBatchUpdatesDTO dataSetUpdates)
    {
        if (dataSet == null)
        {
            throw new UserFailureException(String.format("Data set with code '%s' does not exist.",
                    dataSetUpdates.getCode()));
        }

        DataSetBatchUpdateDetails details = dataSetUpdates.getDetails();

        updateBatchProperties(dataSet, dataSetUpdates.getProperties(),
                details.getPropertiesToUpdate());
        checkPropertiesBusinessRules(dataSet);

        boolean isExperimentFromSample = false;

        if (details.isSampleUpdateRequested())
        {
            if (dataSetUpdates.getSampleIdentifierOrNull() != null)
            {
                // update sample and indirectly experiment
                updateSample(dataSet, dataSetUpdates.getSampleIdentifierOrNull());
                isExperimentFromSample = true;
            } else
            {
                // remove connection with sample
                dataSet.setSample(null);
            }
        }

        if (details.isExperimentUpdateRequested() && !isExperimentFromSample)
        {
            updateExperiment(dataSet, dataSetUpdates.getExperimentIdentifierOrNull());
            dataSet.setSample(null);
        }
        if (details.isContainerUpdateRequested())
        {
            updateContainer(dataSet, dataSetUpdates.getModifiedContainerDatasetCodeOrNull());
        }
        if (details.isParentsUpdateRequested())
        {
            setParents(dataSet, Arrays.asList(dataSetUpdates.getModifiedParentDatasetCodesOrNull()));
        }
        if (details.isFileFormatUpdateRequested())
        {
            updateFileFormatType(dataSet, dataSetUpdates.getFileFormatTypeCode());
        }
        if (dataSet.getContainer() != null)
        {
            // space could be changed by change of experiment
            checkSameSpace(dataSet.getContainer(), dataSet);
        }

        checkSameSpace(dataSet, dataSet.getContainedDataSets()); // even if components were not
                                                                 // changed

        return dataSet;
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(String key, String datastoreCode, String dataSetCode)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations =
                loadAvailableDatasetDescriptions(Collections.singletonList(dataSetCode));
        if (locations.size() < 1)
        {
            throw new UserFailureException(String.format("Data set with code '%s' does not exist.",
                    dataSetCode));
        }

        DatasetDescription dataSet = locations.get(0);
        String sessionToken = dataStore.getSessionToken();
        return service.retrieveLinkFromDataSet(sessionToken, key, dataSet);
    }

    @Override
    public TableModel createReportFromAggregationService(String datastoreServiceKey,
            String datastoreCode, Map<String, Object> parameters)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        String sessionToken = dataStore.getSessionToken();
        String userSessionToken = session.getSessionToken();
        return service.createReportFromAggregationService(sessionToken, userSessionToken,
                datastoreServiceKey, parameters, tryGetLoggedUserEmail());
    }
}
