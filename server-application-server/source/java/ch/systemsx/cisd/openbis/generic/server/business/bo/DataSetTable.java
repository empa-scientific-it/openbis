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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.multiplexer.BatchHandlerAbstract;
import ch.systemsx.cisd.common.multiplexer.IBatch;
import ch.systemsx.cisd.common.multiplexer.IBatchHandler;
import ch.systemsx.cisd.common.multiplexer.IBatchIdProvider;
import ch.systemsx.cisd.common.multiplexer.IBatchResults;
import ch.systemsx.cisd.common.multiplexer.IBatchesResults;
import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.exception.DataSetDeletionDisallowedTypesException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.event.DeleteDataSetEventBuilder;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableModelAppender;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableModelAppender.TableModelWithDifferentColumnCountException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableModelAppender.TableModelWithDifferentColumnIdsException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TableModelAppender.TableModelWithIncompatibleColumnTypesException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportingPluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
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

    private final IMultiplexer multiplexer;

    private List<DataPE> dataSets;

    public DataSetTable(IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory,
            Session session, IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IMultiplexer multiplexer, DataSetTypeWithoutExperimentChecker dataSetTypeChecker)
    {
        super(daoFactory, session, relationshipService, conversationClient, entityInformationProvider,
                managedPropertyEvaluatorFactory, dataSetTypeChecker);
        this.dssFactory = dssFactory;
        this.multiplexer = multiplexer;
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
        dataSets = new ArrayList<DataPE>();
        if (ids.isEmpty())
        {
            return;
        }

        IDataDAO dataDAO = getDataDAO();
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
    public final void loadBySampleTechIdWithoutRelationships(final TechId sampleId)
    {
        assert sampleId != null : "Unspecified sample id";
        final SamplePE sample = getSampleDAO().getByTechId(sampleId);
        dataSets = new ArrayList<DataPE>();
        dataSets.addAll(getDataDAO().listDataSetsWithoutRelationships(sample));
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
    public void deleteLoadedDataSets(String reason, boolean forceDisallowedTypes)
    {
        assertDatasetsAreDeletable(dataSets);
        assertDatasetsWithDisallowedTypes(dataSets, forceDisallowedTypes);

        Map<DataStorePE, List<DataPE>> allToBeDeleted = groupDataSetsByDataStores();
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
                ExperimentPE experiment = getExperimentOrNull(dataSet);
                if (experiment != null)
                {
                    HibernateUtils.initialize(experiment.getProject().getSpace());
                    HibernateUtils.initialize(experiment.getProperties());
                }
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

    private ExperimentPE getExperimentOrNull(DataPE dataSet)
    {
        SamplePE sampleOrNull = dataSet.tryGetSample();
        return sampleOrNull != null ? sampleOrNull.getExperiment() : dataSet.getExperiment();
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
        IDataStoreService service =
                getConversationClient().getDataStoreService(dataStore.getRemoteUrl(),
                        session.getSessionToken());
        String sessionToken = dataStore.getSessionToken();
        List<AbstractExternalData> cleanDataSets =
                DataSetTranslator.translate(list, "?", "?", new HashMap<Long, Set<Metaproject>>(),
                        managedPropertyEvaluatorFactory, new IValidator<IIdentifierHolder>()
                            {
                                @Override
                                public boolean isValid(IIdentifierHolder object)
                                {
                                    return false;
                                }
                            });
        service.uploadDataSetsToCIFEX(sessionToken, cleanDataSets, context);
    }

    @Override
    public void processDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes, Map<String, String> parameterBindings)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore, dssFactory);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);
        String sessionToken = dataStore.getSessionToken();
        String userSessionToken = session.getSessionToken();
        validateParameterBindings(datastoreServiceKey, parameterBindings);
        validateProcessingServiceKey(dataStore, datastoreServiceKey, datastoreCode);
        parameterBindings.put(Constants.USER_PARAMETER, session.tryGetPerson().getUserId());
        service.processDatasets(sessionToken, userSessionToken, datastoreServiceKey, locations,
                parameterBindings, tryGetLoggedUserId(), tryGetLoggedUserEmail());
    }

    @Override
    public void processDatasets(final String datastoreServiceKey, final List<String> datasetCodes, final Map<String, String> parameterBindings)
    {

        final List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);

        IBatchIdProvider<DatasetDescription, String> batchIdProvider =
                new IBatchIdProvider<DatasetDescription, String>()
                    {
                        @Override
                        public String getBatchId(DatasetDescription object)
                        {
                            return object.getDataStoreCode();
                        }
                    };

        IBatchHandler<DatasetDescription, String, Void> batchHandler =
                new BatchHandlerAbstract<DatasetDescription, String, Void>()
                    {
                        @Override
                        public void validateBatch(IBatch<DatasetDescription, String> batch)
                        {
                            validateParameterBindings(datastoreServiceKey, parameterBindings);
                            
                            DataStorePE dataStore = findDataStore(batch.getId());
                            validateProcessingServiceKey(dataStore, datastoreServiceKey, batch.getId());
                        }

                        @Override
                        public List<Void> processBatch(IBatch<DatasetDescription, String> batch)
                        {
                            DataStorePE dataStore = findDataStore(batch.getId());
                            String sessionToken = dataStore.getSessionToken();
                            String userSessionToken = session.getSessionToken();
                            IDataStoreService service = tryGetDataStoreService(dataStore, dssFactory);
                            parameterBindings.put(Constants.USER_PARAMETER, session.tryGetPerson().getUserId());
                            service.processDatasets(sessionToken, userSessionToken, datastoreServiceKey, batch.getObjects(),
                                    parameterBindings, tryGetLoggedUserId(), tryGetLoggedUserEmail());
                            return Collections.emptyList();
                        }
                    };

        multiplexer.process(locations, batchIdProvider, batchHandler);

    }

    private void validateParameterBindings(final String datastoreServiceKey, final Map<String, String> parameterBindings)
    {
        String value = parameterBindings.get(Constants.USER_PARAMETER);
        if (value != null)
        {
            throw new UserFailureException("Couldn't execute processing plugin '" + datastoreServiceKey
                    + "' because parameter '" + Constants.USER_PARAMETER
                    + "' has already be bound to the value '" + value
                    + "'. Note, that parameter '" + Constants.USER_PARAMETER
                    + "' is a reserved parameter used internally.");
        }
    }

    private void validateProcessingServiceKey(DataStorePE dataStore, final String datastoreServiceKey, String dataStoreCode)
    {
        for (DataStoreServicePE service : dataStore.getServices())
        {
            if (service.getKey().equals(datastoreServiceKey) && service.getKind().equals(DataStoreServiceKind.PROCESSING))
            {
                return;
            }
        }

        throw new UserFailureException("Data store '" + dataStoreCode + "' does not have '" + datastoreServiceKey
                + "' processing plugin configured.");
    }
    
    private void validateReportingServiceKey(DataStorePE dataStore, final String datastoreServiceKey, String dataStoreCode)
    {
        for (DataStoreServicePE service : dataStore.getServices())
        {
            if (service.getKey().equals(datastoreServiceKey) && service.isTableReport())
            {
                return;
            }
        }

        throw new UserFailureException("Data store '" + dataStoreCode + "' does not have '" + datastoreServiceKey
                + "' reporting plugin configured.");
    }
    
    private void validateAggregationServiceKey(DataStorePE dataStore, final String datastoreServiceKey, String dataStoreCode)
    {
        for (DataStoreServicePE service : dataStore.getServices())
        {
            if (service.getKey().equals(datastoreServiceKey) && ReportingPluginType.AGGREGATION_TABLE_MODEL.equals(service.getReportingPluginTypeOrNull()))
            {
                return;
            }
        }
        
        throw new UserFailureException("Data store '" + dataStoreCode + "' does not have '" + datastoreServiceKey
                + "' aggregation plugin configured.");
    }

    private String tryGetLoggedUserEmail()
    {
        return session.tryGetPerson() == null ? null : session.tryGetPerson().getEmail();
    }

    private String tryGetLoggedUserId()
    {
        return session.tryGetPerson() == null ? null : session.tryGetPerson().getUserId();
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

        if (StringUtils.isBlank(dataStore.getRemoteUrl()))
        {
            throw createUnknownDataStoreServerException();
        }
        validateReportingServiceKey(dataStore, datastoreServiceKey, datastoreCode);

        IDataStoreService service =
                getConversationClient().getDataStoreService(dataStore.getRemoteUrl(),
                        session.getSessionToken());
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);
        String sessionToken = dataStore.getSessionToken();
        String userSessionToken = session.getSessionToken();
        return service.createReportFromDatasets(sessionToken, userSessionToken,
                datastoreServiceKey, locations, tryGetLoggedUserId(), tryGetLoggedUserEmail());
    }

    @Override
    public TableModel createReportFromDatasets(final String datastoreServiceKey,
            final List<String> datasetCodes)
    {
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);

        IBatchIdProvider<DatasetDescription, String> batchIdProvider =
                new IBatchIdProvider<DatasetDescription, String>()
                    {
                        @Override
                        public String getBatchId(DatasetDescription object)
                        {
                            return object.getDataStoreCode();
                        }
                    };

        IBatchHandler<DatasetDescription, String, TableModel> batchHandler =
                new BatchHandlerAbstract<DatasetDescription, String, TableModel>()
                    {
                        @Override
                        public void validateBatch(IBatch<DatasetDescription, String> batch)
                        {
                            DataStorePE dataStore = findDataStore(batch.getId());
                            validateReportingServiceKey(dataStore, datastoreServiceKey, batch.getId());
                        }

                        @Override
                        public List<TableModel> processBatch(IBatch<DatasetDescription, String> batch)
                        {
                            DataStorePE dataStore = findDataStore(batch.getId());
                            String sessionToken = session.getSessionToken();
                            String storeSessionToken = dataStore.getSessionToken();

                            IDataStoreService service =
                                    getConversationClient().getDataStoreService(
                                            dataStore.getRemoteUrl(), sessionToken);

                            TableModel tableModel =
                                    service.createReportFromDatasets(storeSessionToken,
                                            sessionToken, datastoreServiceKey, batch.getObjects(),
                                            tryGetLoggedUserId(), tryGetLoggedUserEmail());

                            return Collections.singletonList(tableModel);
                        }
                    };

        IBatchesResults<String, TableModel> batchesResults =
                multiplexer.process(locations, batchIdProvider, batchHandler);

        TableModelAppender tableModelAppender = new TableModelAppender();
        for (IBatchResults<String, TableModel> batchResults : batchesResults.getBatchResults())
        {
            try
            {
                tableModelAppender.append(batchResults.getResults().get(0));
            } catch (TableModelWithDifferentColumnCountException e)
            {
                throw new UserFailureException("Could not merge reports from multiple data stores because '" + batchResults.getBatchId()
                        + "' data store returned a table with an incorrect number of columns (expected: " + e.getExpectedColumnCount()
                        + ", got: " + e.getAppendedColumnCount() + ")");
            } catch (TableModelWithDifferentColumnIdsException e)
            {
                throw new UserFailureException("Could not merge reports from multiple data stores because '" + batchResults.getBatchId()
                        + "' data store returned a table with different column ids (expected: " + e.getExpectedColumnIds()
                        + ", got: " + e.getAppendedColumnIds() + ")");
            } catch (TableModelWithIncompatibleColumnTypesException e)
            {
                throw new UserFailureException("Could not merge reports from multiple data stores because '" + batchResults.getBatchId()
                        + "' data store returned a table with incompatible types of columns (expected: " + e.getExpectedColumnTypes()
                        + ", got: " + e.getAppendedColumnTypes() + ")");
            }
        }
        return tableModelAppender.toTableModel();
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
            throw new UserFailureException("Cannot find the data store " + datastoreCode);
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
    public int archiveDatasets(boolean removeFromDataStore, Map<String, String> options)
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupExternalDataByDataStores();
        Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService =
                removeDataStoresWhereArchivingIsCurrentlyNotPossible(enrichWithService(datasetsByStore));

        DataSetArchivingStatus pendingStatus =
                (removeFromDataStore) ? DataSetArchivingStatus.ARCHIVE_PENDING
                        : DataSetArchivingStatus.BACKUP_PENDING;
        int result =
                filterByStatusAndUpdate(remap(datasetsWithService), DataSetArchivingStatus.AVAILABLE,
                        pendingStatus);
        
        performArchiving(datasetsWithService, removeFromDataStore, options);
        return result;
    }
    
    private Map<DataStorePE, List<ExternalDataPE>> remap(Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService)
    {
        Map<DataStorePE, List<ExternalDataPE>> result = new HashMap<>();
        for (Entry<DataStoreWithService, List<ExternalDataPE>> entry : datasetsWithService.entrySet())
        {
            result.put(entry.getKey().dataStore, entry.getValue());
        }
        return result;
    }

    private Map<DataStoreWithService, List<ExternalDataPE>> removeDataStoresWhereArchivingIsCurrentlyNotPossible(Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService)
    {
        Map<DataStoreWithService, List<ExternalDataPE>> result = new HashMap<>();
        for (Entry<DataStoreWithService, List<ExternalDataPE>> entry : datasetsWithService.entrySet())
        {
            DataStoreWithService storeWithService = entry.getKey();
            if (storeWithService.service.isArchivingPossible(storeWithService.dataStore.getSessionToken()))
            {
                result.put(storeWithService, entry.getValue());
            } else
            {
                operationLog.warn("Archiving is currently not possible on Data Store " + storeWithService.dataStore.getCode());
            }
        }
        return result;
    }

    @Override
    public int unarchiveDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupExternalDataByDataStores();
        Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService =
                enrichWithService(datasetsByStore);

        boolean needRefresh = prepareForUnarchiving(datasetsWithService);

        if (needRefresh)
        {
            datasetsByStore = groupExternalDataByDataStores();
            datasetsWithService =
                    enrichWithService(datasetsByStore);
        }

        int result =
                filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.ARCHIVED,
                        DataSetArchivingStatus.UNARCHIVE_PENDING);
        performUnarchiving(datasetsWithService);
        return result;
    }

    /**
     * Asks datastore servers for the extended list of datasets to unarchive, and reloads itself.
     * 
     * @return true if the reaload of data has happened
     */
    private boolean prepareForUnarchiving(Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService)
    {
        List<String> result = new LinkedList<String>();

        boolean enhancementsFound = false;

        for (Entry<DataStoreWithService, List<ExternalDataPE>> entry : datasetsWithService.entrySet())
        {
            DataStoreWithService dssws = entry.getKey();
            DataStorePE dataStore = dssws.dataStore;
            IDataStoreService service = dssws.service;
            List<ExternalDataPE> datasets = entry.getValue();
            if (datasets.isEmpty())
            {
                continue;
            }

            List<String> dataSetCodes = new LinkedList<String>();
            for (ExternalDataPE data : datasets)
            {
                dataSetCodes.add(data.getCode());
            }

            List<String> enhancedCodes = service.getDataSetCodesForUnarchiving(dataStore.getSessionToken(), session.getSessionToken(),
                    dataSetCodes, tryGetLoggedUserId());
            result.addAll(enhancedCodes);

            if (false == new HashSet<String>(dataSetCodes).containsAll(enhancedCodes))
            {
                enhancementsFound = true;
            }
        }

        if (enhancementsFound)
        {
            loadByDataSetCodes(result, false, true);
            return true;
        }
        return false;
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
                List<DatasetDescription> descriptions, String userId, String userEmailOrNull);

        public DataSetArchivingStatus getStatusToRestoreOnFailure();
    }

    private void performUnarchiving(Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore)
    {
        performArchivingAction(datasetsByStore, new IArchivingAction()
            {
                @Override
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userId, String userEmailOrNull)
                {
                    service.unarchiveDatasets(sessionToken, session.getSessionToken(),
                            descriptions, userId, userEmailOrNull);
                }

                @Override
                public DataSetArchivingStatus getStatusToRestoreOnFailure()
                {
                    return DataSetArchivingStatus.ARCHIVED;
                }

            });

    }

    private void performArchiving(Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore,
            final boolean removeFromDataStore, Map<String, String> options)
    {
        performArchivingAction(datasetsByStore, new IArchivingAction()
            {
                @Override
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userId, String userEmailOrNull)
                {
                    service.archiveDatasets(sessionToken, session.getSessionToken(), descriptions,
                            userId, userEmailOrNull, removeFromDataStore, options);
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
            IDataStoreService service = tryGetDataStoreService(dataStore, dssFactory);
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
            String userId = tryGetLoggedUserId();
            String userEmailOrNull = tryGetLoggedUserEmail();
            try
            {
                archivingAction.execute(sessionToken, service, descriptions, userId,
                        userEmailOrNull);
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
        clearSampleCache();
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

        Map<String, Integer> versionsMap = new HashMap<String, Integer>();
        for (DataPE dataSet : dataSets)
        {
            versionsMap.put(dataSet.getCode(), dataSet.getVersion());
        }

        for (DataSetBatchUpdatesDTO update : updates)
        {
            if (update.getCode() == null)
            {
                throw new UserFailureException(
                        "Data set doesn't have a specified code and therefore cannot be updated.");
            }

            Integer version = versionsMap.get(update.getCode());

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
                throw new UserFailureException(sb.toString());
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

        updateProperties(dataSet.getEntityType(), dataSetUpdates.getProperties(), details.getPropertiesToUpdate(), dataSet, dataSet);
        checkPropertiesBusinessRules(dataSet);

        if (details.isSampleUpdateRequested())
        {
            SampleIdentifier sampleIdentifierOrNull = dataSetUpdates.getSampleIdentifierOrNull();
            if (sampleIdentifierOrNull != null)
            {
                updateSample(dataSet, sampleIdentifierOrNull);
            } else
            {
                updateExperiment(dataSet, dataSetUpdates.getExperimentIdentifierOrNull());
            }
        } else if (details.isExperimentUpdateRequested() && dataSetUpdates.getExperimentIdentifierOrNull() != null)
        {
            updateExperiment(dataSet, dataSetUpdates.getExperimentIdentifierOrNull());
        }
        if (details.isContainerUpdateRequested())
        {
            if (dataSetUpdates.getModifiedContainedDatasetCodesOrNull() != null)
            {
                setContainedDataSets(dataSet,
                        Arrays.asList(dataSetUpdates.getModifiedContainedDatasetCodesOrNull()));
            }

            if (dataSetUpdates.getModifiedContainerDatasetCodeOrNull() != null)
            {
                updateContainers(dataSet, dataSetUpdates.getModifiedContainerDatasetCodeOrNull());
            }
        }
        if (details.isParentsUpdateRequested())
        {
            setParents(dataSet, Arrays.asList(dataSetUpdates.getModifiedParentDatasetCodesOrNull()));
        }
        if (details.isFileFormatUpdateRequested())
        {
            updateFileFormatType(dataSet, dataSetUpdates.getFileFormatTypeCode());
        }

        return dataSet;
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(String key, String datastoreCode, String dataSetCode)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore, dssFactory);
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

        if (StringUtils.isBlank(dataStore.getRemoteUrl()))
        {
            throw createUnknownDataStoreServerException();
        }
        validateAggregationServiceKey(dataStore, datastoreServiceKey, datastoreCode);

        IDataStoreService service =
                getConversationClient().getDataStoreService(dataStore.getRemoteUrl(),
                        session.getSessionToken());
        String sessionToken = dataStore.getSessionToken();
        String userSessionToken = session.getSessionToken();
        return service.createReportFromAggregationService(sessionToken, userSessionToken,
                datastoreServiceKey, parameters, tryGetLoggedUserId(), tryGetLoggedUserEmail());
    }

}
