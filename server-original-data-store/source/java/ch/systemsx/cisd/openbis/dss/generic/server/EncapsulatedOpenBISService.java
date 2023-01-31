/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.IPersonalAccessTokenId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.update.PersonalAccessTokenUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.api.retry.RetryCaller;
import ch.systemsx.cisd.common.api.retry.config.RetryConfiguration;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataStoreServerV3Factory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ManagedAuthentication;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.AbstractOpenBisServiceFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceFactory;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceV3Factory;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

/**
 * A class that encapsulates the {@link IServiceForDataStoreServer}.
 * 
 * @author Bernd Rinn
 */
public final class EncapsulatedOpenBISService implements IEncapsulatedOpenBISService, FactoryBean
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EncapsulatedOpenBISService.class);

    private final IServiceForDataStoreServer service;

    private Integer version;

    private DatabaseInstance homeDatabaseInstance;

    // this session object is automatically kept up-to-date by an aspect
    private OpenBISSessionHolder session;

    private IShareIdManager shareIdManager;

    private IServiceConversationClientManagerLocal conversationClient;

    private static class RetryingOpenBisCreator<T> extends RetryCaller<T, RuntimeException>
    {
        protected String timeout;

        private final AbstractOpenBisServiceFactory<T> openBisServiceFactory;

        RetryingOpenBisCreator(String openBISURL, String timeout, AbstractOpenBisServiceFactory<T> openBisServiceFactory)
        {
            super(new RetryConfiguration()
                {
                    @Override
                    public float getWaitingTimeBetweenRetriesIncreasingFactor()
                    {
                        return 2;
                    }

                    @Override
                    public int getWaitingTimeBetweenRetries()
                    {
                        return 5000;
                    }

                    @Override
                    public int getMaximumNumberOfRetries()
                    {
                        return 5;
                    }
                }, new Log4jSimpleLogger(operationLog));
            this.timeout = timeout;
            this.openBisServiceFactory = openBisServiceFactory;
        }

        @Override
        protected boolean isRetryableException(RuntimeException e)
        {
            return true;
        }

        @Override
        protected T call() throws RuntimeException
        {
            T service = null;

            if (timeout.startsWith("$"))
            {
                service = openBisServiceFactory.createService();
            } else
            {
                service = openBisServiceFactory.createService(normalizeTimeout(timeout));
            }

            return service;
        }
    }

    private static RetryingOpenBisCreator<IServiceForDataStoreServer> getGenericRetryingOpenBisCreator(String openBISURL, String timeout)
    {
        return new RetryingOpenBisCreator<IServiceForDataStoreServer>(openBISURL, timeout, new OpenBisServiceFactory(openBISURL));
    }

    private static RetryingOpenBisCreator<IApplicationServerApi> getGenericRetryingOpenBisV3Creator(String openBISURL, String timeout)
    {
        return new RetryingOpenBisCreator<IApplicationServerApi>(openBISURL, timeout, new OpenBisServiceV3Factory(openBISURL));
    }

    public static IServiceForDataStoreServer createOpenBisService(String openBISURL, String timeout)
    {
        return getGenericRetryingOpenBisCreator(openBISURL, timeout).callWithRetry();
    }

    public static IApplicationServerApi createOpenBisV3Service(String openBISURL, String timeout)
    {
        return getGenericRetryingOpenBisV3Creator(openBISURL, timeout).callWithRetry();
    }
    
    public static IDataStoreServerApi createDataStoreV3Service(String dataStoreServerUrl, String timeout)
    {
        return new RetryingOpenBisCreator<>(dataStoreServerUrl, timeout, new DataStoreServerV3Factory(dataStoreServerUrl)).callWithRetry();
    }

    /**
     * Creates a remote version of {@link IGeneralInformationService} for specified URL and time out (in minutes).
     */
    public static IGeneralInformationService createGeneralInformationService(String openBISURL,
            String timeout)
    {
        ServiceFinder finder = new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        if (timeout.startsWith("$"))
        {
            return finder.createService(IGeneralInformationService.class, openBISURL);
        }
        return finder.createService(IGeneralInformationService.class, openBISURL,
                normalizeTimeout(timeout));
    }

    /**
     * Creates a remote version of {@link IQueryApiServer} for specified URL and time out (in minutes)
     */
    public static IQueryApiServer createQueryApiServer(String openBISURL, String timeout)
    {
        ServiceFinder finder =
                new ServiceFinder("openbis", IQueryApiServer.QUERY_PLUGIN_SERVER_URL);
        if (timeout.startsWith("$"))
        {
            return finder.createService(IQueryApiServer.class, openBISURL);
        }
        return finder.createService(IQueryApiServer.class, openBISURL, normalizeTimeout(timeout));
    }

    private static long normalizeTimeout(String timeout)
    {
        return Integer.parseInt(timeout) * DateUtils.MILLIS_PER_MINUTE;
    }

    public EncapsulatedOpenBISService(IServiceForDataStoreServer service,
            OpenBISSessionHolder sessionHolder, String downloadUrl)
    {
        this(service, sessionHolder, downloadUrl, null);
    }

    public EncapsulatedOpenBISService(IServiceForDataStoreServer service,
            OpenBISSessionHolder sessionHolder, String downloadUrl, IShareIdManager shareIdManager)
    {
        this.shareIdManager = shareIdManager;
        assert service != null : "Given IETLLIMSService implementation can not be null.";
        assert sessionHolder != null : "Given OpenBISSessionHolder can not be null.";
        this.service = service;
        this.session = sessionHolder;
    }

    @Override
    public IEncapsulatedBasicOpenBISService getBasicFilteredOpenBISService(String user)
    {
        return new EncapsulatedFilteredBasicOpenBISService(user, service, this,
                session.getSessionToken());
    }

    private IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    @Override
    public Object getObject() throws Exception
    {
        return this;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class getObjectType()
    {
        return IEncapsulatedOpenBISService.class;
    }

    @Override
    public boolean isSingleton()
    {
        return true;
    }

    //
    // IEncapsulatedOpenBISService
    //
    @Override
    public String getSessionToken()
    {
        return session.getSessionToken();
    }

    @Override
    public Experiment tryGetExperiment(ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null : " Unspecified experiment identifier.";
        return service.tryGetExperiment(session.getSessionToken(), experimentIdentifier);
    }

    @Override
    public Space tryGetSpace(SpaceIdentifier spaceIdentifier) throws UserFailureException
    {
        assert spaceIdentifier != null : "Unspecified space identifier";
        return service.tryGetSpace(session.getSessionToken(), spaceIdentifier);
    }

    @Override
    public Project tryGetProject(ProjectIdentifier projectIdentifier) throws UserFailureException
    {
        assert projectIdentifier != null : "Unspecified project identifier";
        return service.tryGetProject(session.getSessionToken(), projectIdentifier);
    }

    @Override
    public List<Sample> listSamples(ListSampleCriteria criteria)
    {
        assert criteria != null : "Unspecifed criteria.";
        return service.listSamples(session.getSessionToken(), criteria);
    }

    @Override
    public final Sample tryGetSampleWithExperiment(final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";
        return service.tryGetSampleWithExperiment(session.getSessionToken(), sampleIdentifier);
    }

    @Override
    public SampleIdentifier tryGetSampleIdentifier(String samplePermID) throws UserFailureException
    {
        return service.tryGetSampleIdentifier(session.getSessionToken(), samplePermID);
    }

    @Override
    public Map<String, SampleIdentifier> listSampleIdentifiers(List<String> samplePermID)
            throws UserFailureException
    {
        return service.listSamplesByPermId(session.getSessionToken(), samplePermID);
    }

    @Override
    public ExperimentType getExperimentType(String experimentTypeCode) throws UserFailureException
    {
        return service.getExperimentType(session.getSessionToken(), experimentTypeCode);
    }

    @Override
    public Collection<VocabularyTerm> listVocabularyTerms(String vocabularyCode)
            throws UserFailureException
    {
        return service.listVocabularyTerms(session.getSessionToken(), vocabularyCode);
    }

    @Override
    public Vocabulary tryGetVocabulary(String code)
    {
        return service.tryGetVocabulary(session.getSessionToken(), code);
    }

    @Override
    public SampleType getSampleType(String sampleTypeCode) throws UserFailureException
    {
        return service.getSampleType(session.getSessionToken(), sampleTypeCode);
    }

    @Override
    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode)
    {
        return service.getDataSetType(session.getSessionToken(), dataSetTypeCode);
    }

    @Override
    public List<AbstractExternalData> listDataSetsByExperimentID(long experimentID)
            throws UserFailureException
    {
        TechId id = new TechId(experimentID);
        return service.listDataSetsByExperimentID(session.getSessionToken(), id);
    }

    @Override
    public List<AbstractExternalData> listDataSetsBySampleID(long sampleID,
            boolean showOnlyDirectlyConnected)
    {
        TechId id = new TechId(sampleID);
        return service.listDataSetsBySampleID(session.getSessionToken(), id,
                showOnlyDirectlyConnected);
    }

    @Override
    public List<AbstractExternalData> listDataSetsByCode(List<String> dataSetCodes)
            throws UserFailureException
    {
        return service.listDataSetsByCode(session.getSessionToken(), dataSetCodes);
    }

    @Override
    public long registerExperiment(NewExperiment experiment) throws UserFailureException
    {
        assert experiment != null : "Unspecified experiment.";
        return service.registerExperiment(session.getSessionToken(), experiment);
    }

    @Override
    public void registerSamples(List<NewSamplesWithTypes> newSamples, String userIDOrNull)
            throws UserFailureException
    {
        assert newSamples != null : "Unspecified samples.";

        service.registerSamples(session.getSessionToken(), newSamples, userIDOrNull);
    }

    @Override
    public long registerSample(NewSample newSample, String userIDOrNull)
            throws UserFailureException
    {
        assert newSample != null : "Unspecified sample.";

        return service.registerSample(session.getSessionToken(), newSample, userIDOrNull);
    }

    @Override
    public void updateSample(SampleUpdatesDTO sampleUpdate) throws UserFailureException
    {
        assert sampleUpdate != null : "Unspecified sample.";

        service.updateSample(session.getSessionToken(), sampleUpdate);
    }

    @Override
    public final void registerDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data)
    {
        assert dataSetInformation != null : "missing sample identifier";
        assert data != null : "missing data";

        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        if (sampleIdentifier == null)
        {
            ExperimentIdentifier experimentIdentifier =
                    dataSetInformation.getExperimentIdentifier();
            service.registerDataSet(session.getSessionToken(), experimentIdentifier, data);
        } else
        {
            service.registerDataSet(session.getSessionToken(), sampleIdentifier, data);
        }
        setShareId(data);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Registered in openBIS: data set " + dataSetInformation.describe()
                    + ".");
        }
    }

    @Override
    public final void updateDataSet(String code, List<NewProperty> properties, SpaceIdentifier space)
            throws UserFailureException

    {
        assert code != null : "missing data set code";
        assert properties != null : "missing data";
        assert space != null : "space missing";

        service.addPropertiesToDataSet(session.getSessionToken(), properties, code, space);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data set " + code + ".");
        }
    }

    @Override
    public boolean isDataSetOnTrashCanOrDeleted(String dataSetCode)
    {
        return service.isDataSetOnTrashCanOrDeleted(session.getSessionToken(), dataSetCode);
    }

    @Override
    public void updateShareIdAndSize(String dataSetCode, String shareId, long size)
            throws UserFailureException
    {
        service.updateShareIdAndSize(session.getSessionToken(), dataSetCode, shareId, size);

        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data set " + dataSetCode + ", share Id: "
                    + shareId + ", size: " + size);
        }
    }

    @Override
    public final void updateDataSetStatuses(List<String> codes, DataSetArchivingStatus newStatus,
            boolean presentInArchive) throws UserFailureException

    {
        assert codes != null : "missing data set codes";
        assert newStatus != null : "missing status";

        service.updateDataSetStatuses(session.getSessionToken(), codes, newStatus, presentInArchive);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Updated in openBIS: data sets " + codes + ", status=" + newStatus);
        }
    }

    @Override
    public boolean compareAndSetDataSetStatus(String dataSetCode, DataSetArchivingStatus oldStatus,
            DataSetArchivingStatus newStatus, boolean newPresentInArchive)
            throws UserFailureException
    {
        assert dataSetCode != null : "missing data set codes";
        assert oldStatus != null : "missing old status";
        assert newStatus != null : "missing new status";
        return service.compareAndSetDataSetStatus(session.getSessionToken(), dataSetCode,
                oldStatus, newStatus, newPresentInArchive);
    }

    @Override
    public IEntityProperty[] tryGetPropertiesOfSample(SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";
        return service.tryGetPropertiesOfSample(session.getSessionToken(), sampleIdentifier);
    }

    @Override
    public final IEntityProperty[] tryGetPropertiesOfTopSample(
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";
        return service.tryGetPropertiesOfSample(session.getSessionToken(), sampleIdentifier);
    }

    @Override
    public final List<Sample> listSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException
    {
        return service.listSamplesByCriteria(session.getSessionToken(), criteria);
    }

    @Override
    public final int getVersion()
    {
        if (version == null)
        {
            version = service.getVersion();
        }
        return version;
    }

    @Override
    public final DatabaseInstance getHomeDatabaseInstance()
    {
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = service.getHomeDatabaseInstance(session.getSessionToken());
        }
        return homeDatabaseInstance;
    }

    @Override
    public final String createPermId()
    {
        return service.createPermId(session.getSessionToken());
    }

    @Override
    public final List<String> createPermIds(int n)
    {
        return service.createPermIds(session.getSessionToken(), n);
    }

    @Override
    public long drawANewUniqueID()
    {
        return service.drawANewUniqueID(session.getSessionToken());
    }

    @Override
    public IDatasetLocationNode tryGetDataSetLocation(String dataSetCode)
            throws UserFailureException
    {
        return service.tryGetDataSetLocation(session.getSessionToken(), dataSetCode);
    }

    @Override
    public AbstractExternalData tryGetDataSet(String dataSetCode) throws UserFailureException
    {
        return service.tryGetDataSet(session.getSessionToken(), dataSetCode);
    }

    @Override
    public AbstractExternalData tryGetThinDataSet(String dataSetCode) throws UserFailureException
    {
        return service.tryGetThinDataSet(session.getSessionToken(), dataSetCode);
    }

    @Override
    public AbstractExternalData tryGetLocalDataSet(String dataSetCode) throws UserFailureException
    {
        return service.tryGetLocalDataSet(session.getSessionToken(), dataSetCode,
                session.getDataStoreCode());
    }

    @Override
    public AbstractExternalData tryGetDataSet(String sToken, String dataSetCode)
            throws UserFailureException
    {
        return service.tryGetDataSet(sToken, dataSetCode);
    }

    @Override
    public void checkInstanceAdminAuthorization(String sToken) throws UserFailureException
    {
        service.checkInstanceAdminAuthorization(sToken);
    }

    @Override
    public void checkProjectPowerUserAuthorization(String sessionToken) throws UserFailureException
    {
        service.checkProjectPowerUserAuthorization(sessionToken);
    }

    @Override
    public void checkDataSetAccess(String sToken, String dataSetCode) throws UserFailureException
    {
        service.checkDataSetAccess(sToken, dataSetCode);
    }

    @Override
    public void checkDataSetCollectionAccess(String sToken, List<String> dataSetCodes)
            throws UserFailureException
    {
        service.checkDataSetCollectionAccess(sToken, dataSetCodes);
    }

    @Override
    public void checkSpaceAccess(String sToken, SpaceIdentifier spaceId)
    {
        service.checkSpaceAccess(sToken, spaceId);
    }

    @Override
    public void checkExperimentAccess(String sToken, String experimentIdentifier)
    {
        service.checkExperimentAccess(sToken, experimentIdentifier);
    }

    @Override
    public void checkSampleAccess(String sToken, String sampleIdentifier)
    {
        service.checkSampleAccess(sToken, sampleIdentifier);
    }

    @Override
    public List<DataSetShareId> listDataSetShareIds() throws UserFailureException
    {
        List<DataSetShareId> shareIds =
                service.listShareIds(session.getSessionToken(), session.getDataStoreCode());
        for (DataSetShareId dataSetShareId : shareIds)
        {
            if (dataSetShareId.getShareId() == null)
            {
                dataSetShareId
                        .setShareId(ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID);
            }
        }
        return shareIds;
    }

    @Override
    public List<SimpleDataSetInformationDTO> listPhysicalDataSets() throws UserFailureException
    {
        List<SimpleDataSetInformationDTO> dataSets =
                service.listPhysicalDataSets(session.getSessionToken(), session.getDataStoreCode());
        return injectDefaultShareIdIfMissing(dataSets);
    }

    @Override
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(int chunkSize)
            throws UserFailureException
    {
        List<SimpleDataSetInformationDTO> dataSets =
                service.listOldestPhysicalDataSets(session.getSessionToken(),
                        session.getDataStoreCode(), chunkSize);
        return injectDefaultShareIdIfMissing(dataSets);
    }

    @Override
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(Date youngerThan,
            int chunkSize) throws UserFailureException
    {
        List<SimpleDataSetInformationDTO> dataSets =
                service.listOldestPhysicalDataSets(session.getSessionToken(),
                        session.getDataStoreCode(), youngerThan, chunkSize);
        return injectDefaultShareIdIfMissing(dataSets);
    }

    @Override
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsByArchivingStatus(DataSetArchivingStatus archivingStatus, Boolean presentInArchive)
            throws UserFailureException
    {
        List<SimpleDataSetInformationDTO> dataSets = service.listPhysicalDataSetsByArchivingStatus(session.getSessionToken(),
                session.getDataStoreCode(), archivingStatus, presentInArchive);
        return injectDefaultShareIdIfMissing(dataSets);
    }

    @Override
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsWithUnknownSize(int chunkSize, String dataSetCodeLowerLimit)
            throws UserFailureException
    {
        List<SimpleDataSetInformationDTO> dataSets =
                service.listPhysicalDataSetsWithUnknownSize(session.getSessionToken(),
                        session.getDataStoreCode(), chunkSize, dataSetCodeLowerLimit);
        return injectDefaultShareIdIfMissing(dataSets);
    }

    @Override
    public void updatePhysicalDataSetsSize(Map<String, Long> sizeMap) throws UserFailureException
    {
        service.updatePhysicalDataSetsSize(session.getSessionToken(), sizeMap);
    }

    private List<SimpleDataSetInformationDTO> injectDefaultShareIdIfMissing(
            List<SimpleDataSetInformationDTO> dataSets)
    {
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            if (dataSet.getDataSetShareId() == null)
            {
                dataSet.setDataSetShareId(ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID);
            }
        }
        return dataSets;
    }

    @Override
    public List<AbstractExternalData> listNewerDataSets(TrackingDataSetCriteria criteria)
            throws UserFailureException
    {
        return service
                .listDataSets(session.getSessionToken(), session.getDataStoreCode(), criteria);
    }

    @Override
    public List<AbstractExternalData> listAvailableDataSets(ArchiverDataSetCriteria criteria)
            throws UserFailureException
    {
        return service.listAvailableDataSets(session.getSessionToken(), session.getDataStoreCode(),
                criteria);
    }

    @Override
    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull,
            Date maxDeletionDataOrNull)
    {
        return service.listDeletedDataSets(session.getSessionToken(),
                lastSeenDeletionEventIdOrNull, maxDeletionDataOrNull);
    }

    @Override
    public void archiveDataSets(List<String> dataSetCodes, boolean removeFromDataStore, Map<String, String> options)
            throws UserFailureException
    {
        service.archiveDatasets(session.getSessionToken(), dataSetCodes, removeFromDataStore, options);
    }

    @Override
    public void unarchiveDataSets(List<String> dataSetCodes) throws UserFailureException
    {
        service.unarchiveDatasets(session.getSessionToken(), dataSetCodes);
    }

    @Override
    public SessionContextDTO tryAuthenticate(String user, String password)
    {
        return service.tryAuthenticate(user, password);
    }

    @Override
    public SessionContextDTO tryGetSession(String sToken)
    {
        return service.tryGetSession(sToken);
    }

    @Override
    public void checkSession(String sessionToken)
    {
        service.checkSession(sessionToken);
    }

    @Override
    public Map<String, String> getServerInformation()
    {
        return service.getServerInformation(session.getSessionToken());
    }

    @Override
    public List<String> generateCodes(String prefix, EntityKind entityKind, int size)
    {
        return service.generateCodes(session.getSessionToken(), prefix, entityKind, size);
    }

    @Override
    public List<Person> listAdministrators()
    {
        return service.listAdministrators(session.getSessionToken());
    }

    @Override
    public Person tryPersonWithUserIdOrEmail(String useridOrEmail)
    {
        return service.tryPersonWithUserIdOrEmail(session.getSessionToken(), useridOrEmail);
    }

    @Override
    public Sample registerSampleAndDataSet(NewSample newSample, NewExternalData externalData,
            String userIdOrNull) throws UserFailureException
    {
        Sample sample =
                service.registerSampleAndDataSet(session.getSessionToken(), newSample,
                        externalData, userIdOrNull);
        setShareId(externalData);
        return sample;
    }

    @Override
    public Sample updateSampleAndRegisterDataSet(SampleUpdatesDTO newSample,
            NewExternalData externalData)
    {
        Sample sample =
                service.updateSampleAndRegisterDataSet(session.getSessionToken(), newSample,
                        externalData);
        setShareId(externalData);
        return sample;
    }

    @Override
    public AtomicEntityOperationResult performEntityOperations(
            AtomicEntityOperationDetails operationDetails)
    {
        IServiceForDataStoreServer conversationalService =
                conversationClient.getETLService(session.getSessionToken());

        AtomicEntityOperationResult operations =
                conversationalService.performEntityOperations(session.getSessionToken(),
                        operationDetails);

        List<? extends NewExternalData> dataSets = operationDetails.getDataSetRegistrations();
        for (NewExternalData dataSet : dataSets)
        {
            setShareId(dataSet);
        }
        return operations;
    }

    private void setShareId(NewExternalData data)
    {
        getShareIdManager().setShareId(data.getCode(), data.getShareId());
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
    {
        return service.searchForSamples(session.getSessionToken(), searchCriteria);
    }

    @Override
    public List<AbstractExternalData> searchForDataSets(SearchCriteria searchCriteria)
    {
        return service.searchForDataSets(session.getSessionToken(), searchCriteria);
    }

    @Override
    public List<Experiment> searchForExperiments(SearchCriteria searchCriteria)
    {
        return service.searchForExperiments(session.getSessionToken(), searchCriteria);
    }

    @Override
    public List<Project> listProjects()
    {
        return service.listProjects(session.getSessionToken());
    }

    @Override
    public List<Experiment> listExperiments(ProjectIdentifier projectIdentifier)
    {
        return service.listExperiments(session.getSessionToken(), projectIdentifier);
    }

    @Override
    public Material tryGetMaterial(MaterialIdentifier materialIdentifier)
    {
        return service.tryGetMaterial(session.getSessionToken(), materialIdentifier);
    }

    @Override
    public List<Material> listMaterials(ListMaterialCriteria criteria, boolean withProperties)
    {
        return service.listMaterials(session.getSessionToken(), criteria, withProperties);
    }

    @Override
    public Metaproject tryGetMetaproject(String name, String ownerId)
    {
        return service.tryGetMetaproject(session.getSessionToken(), name, ownerId);
    }

    @Override
    public void removeDataSetsPermanently(List<String> dataSetCodes, String reason)
    {
        service.removeDataSetsPermanently(session.getSessionToken(), dataSetCodes, reason);
    }

    @Override
    public void updateDataSet(DataSetUpdatesDTO dataSetUpdates)
    {
        service.updateDataSet(session.getSessionToken(), dataSetUpdates);
    }

    @Override
    public List<String> getTrustedCrossOriginDomains()
    {
        return service.getTrustedCrossOriginDomains(session.getSessionToken());
    }

    @Override
    public void setStorageConfirmed(List<String> dataSetCodes)
    {
        service.setStorageConfirmed(session.getSessionToken(), dataSetCodes);
    }

    @Override
    public void markSuccessfulPostRegistration(String dataSetCode)
    {
        service.markSuccessfulPostRegistration(session.getSessionToken(), dataSetCode);
    }

    @Override
    public void notifyDatasetAccess(String dataSetCode)
    {
        service.notifyDatasetAccess(session.getSessionToken(), dataSetCode);
    }

    @Override
    public List<AbstractExternalData> listDataSetsForPostRegistration()
    {
        return service.listDataSetsForPostRegistration(session.getSessionToken(),
                session.getDataStoreCode());
    }

    @Override
    public EntityOperationsState didEntityOperationsSucceed(TechId registrationId)
    {
        return service.didEntityOperationsSucceed(session.getSessionToken(), registrationId);
    }

    @Override
    public void heartbeat()
    {
        service.heartbeat(session.getSessionToken());
    }

    @Override
    public boolean doesUserHaveRole(String user, String roleCode, String spaceOrNull)
    {
        return service.doesUserHaveRole(session.getSessionToken(), user, roleCode, spaceOrNull);
    }

    @Override
    public List<String> filterToVisibleDataSets(String user, List<String> dataSetCodes)
    {
        return service.filterToVisibleDataSets(session.getSessionToken(), user, dataSetCodes);
    }

    @Override
    public List<String> filterToVisibleExperiments(String user, List<String> experimentIds)
    {
        return service.filterToVisibleExperiments(session.getSessionToken(), user, experimentIds);
    }

    @Override
    public List<String> filterToVisibleSamples(String user, List<String> sampleIdentifiers)
    {
        return service.filterToVisibleSamples(session.getSessionToken(), user, sampleIdentifiers);
    }

    @Override
    public ExternalDataManagementSystem tryGetExternalDataManagementSystem(
            String externalDataManagementSystemCode)
    {
        return service.tryGetExternalDataManagementSystem(session.getSessionToken(),
                externalDataManagementSystemCode);
    }

    public void setConversationClient(IServiceConversationClientManagerLocal conversationClient)
    {
        this.conversationClient = conversationClient;
    }

    @Override
    public List<? extends EntityTypePropertyType<?>> listPropertyDefinitionsForEntityType(
            String code,
            ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind)
    {
        return service.listPropertyDefinitionsForType(session.getSessionToken(), code, entityKind);
    }

    @Override
    public List<Metaproject> listMetaprojects()
    {
        throw new UnsupportedOperationException(
                "Listing metaprojects is available only for the user-filtered version of service");
    }

    @Override
    public Metaproject tryGetMetaproject(String name)
    {
        throw new UnsupportedOperationException(
                "Getting metaproject is available only for the user-filtered version of service");
    }

    @Override
    @ManagedAuthentication
    public MetaprojectAssignments getMetaprojectAssignments(String name)
    {
        throw new UnsupportedOperationException(
                "getting metaproject assignments is available only for the user-filtered version of service");
    }

    @Override
    @ManagedAuthentication
    public List<Metaproject> listMetaprojectsForEntity(IObjectId entityId)
    {
        throw new UnsupportedOperationException(
                "Listing metaprojects is available only for the user-filtered version of service");
    }

    @Override
    public Map<IObjectId, List<Metaproject>> listMetaprojectsForEntities(Collection<? extends IObjectId> entityIds)
    {
        throw new UnsupportedOperationException(
                "Listing metaprojects is available only for the user-filtered version of service");
    }

    @Override
    public List<AuthorizationGroup> listAuthorizationGroups()
    {
        return service.listAuthorizationGroups(session.getSessionToken());
    }

    @Override
    public List<AuthorizationGroup> listAuthorizationGroupsForUser(String userId)
    {
        return service.listAuthorizationGroupsForUser(session.getSessionToken(), userId);
    }

    @Override
    public List<Person> listUsersForAuthorizationGroup(TechId authorizationGroupId)
    {
        return service.listUsersForAuthorizationGroup(session.getSessionToken(), authorizationGroupId);
    }

    @Override
    public List<RoleAssignment> listRoleAssignments()
    {
        return service.listRoleAssignments(session.getSessionToken());
    }

    @Override
    public List<Attachment> listAttachments(AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId)
    {
        return service.listAttachments(session.getSessionToken(), attachmentHolderKind, attachmentHolderId);
    }

    @Override
    public InputStream getAttachmentContent(AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId, String fileName,
            Integer versionOrNull)
    {
        AttachmentWithContent attachment =
                service.getAttachment(session.getSessionToken(), attachmentHolderKind, attachmentHolderId, fileName, versionOrNull);
        return attachment != null ? new ByteArrayInputStream(attachment.getContent()) : null;
    }

    @Override
    public List<AbstractExternalData> listNotArchivedDatasetsWithMetaproject(final IMetaprojectId metaprojectId)
    {
        return service.listNotArchivedDatasetsWithMetaproject(session.getSessionToken(), metaprojectId);
    }

    @Override public Map<IPersonalAccessTokenId, PersonalAccessToken> getPersonalAccessTokens(
            final List<? extends IPersonalAccessTokenId> personalAccessTokenIds, final PersonalAccessTokenFetchOptions fetchOptions)
    {
        return ServiceProvider.getV3ApplicationService().getPersonalAccessTokens(session.getSessionToken(), personalAccessTokenIds, fetchOptions);
    }

    @Override public void updatePersonalAccessTokens(final List<PersonalAccessTokenUpdate> personalAccessTokenUpdates)
    {
        ServiceProvider.getV3ApplicationService().updatePersonalAccessTokens(session.getSessionToken(), personalAccessTokenUpdates);
    }

    @Override public SearchResult<SessionInformation> searchSessionInformation(final SessionInformationSearchCriteria searchCriteria,
            final SessionInformationFetchOptions fetchOptions)
    {
        return ServiceProvider.getV3ApplicationService().searchSessionInformation(session.getSessionToken(), searchCriteria, fetchOptions);
    }

    @Override
    public Experiment tryGetExperimentByPermId(String permId) throws UserFailureException
    {
        assert permId != null : " Unspecified experiment perm id.";
        return service.tryGetExperimentByPermId(session.getSessionToken(), new PermId(permId));
    }

    @Override
    public Project tryGetProjectByPermId(String permId) throws UserFailureException
    {
        assert permId != null : " Unspecified project perm id.";
        return service.tryGetProjectByPermId(session.getSessionToken(), new PermId(permId));
    }

    @Override
    public Sample tryGetSampleByPermId(String permId) throws UserFailureException
    {
        assert permId != null : " Unspecified sample perm id.";
        return service.tryGetSampleByPermId(session.getSessionToken(), new PermId(permId));
    }
}
