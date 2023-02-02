package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.api.MinimalMinorVersion;
import ch.systemsx.cisd.common.api.retry.RetryCaller;
import ch.systemsx.cisd.common.api.retry.RetryProxyFactory;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.common.multiplexer.ThreadPoolMultiplexer;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.OpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.DssServiceRpcScreeningHolder;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.DssServiceRpcScreeningMultiplexer;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningBatchHandler;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningFactory;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.IDataSetFilter;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.TypeBasedDataSetFilter;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.WellImageCache.CachedImage;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.WellImageCache.WellImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentImageMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageRepresentationFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;

/**
 * A client side facade of openBIS and Datastore Server API.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningOpenbisServiceFacade implements IScreeningOpenbisServiceFacade
{
    static final int MAJOR_VERSION_AS = 1;

    static final int MAJOR_VERSION_DSS = 1;

    private static final String OPENBIS_SCREENING_API = "/rmi-screening-api-v" + MAJOR_VERSION_AS;

    static final long SERVER_TIMEOUT_MILLIS = ServiceFinder.SERVER_TIMEOUT_IN_MINUTES * DateUtils.MILLIS_PER_MINUTE;

    private static final IDssServiceRpcScreeningFactory DSS_SERVICE_FACTORY =
            new IDssServiceRpcScreeningFactory()
                {
                    @Override
                    public DssServiceRpcScreeningHolder createDssService(String serverUrl)
                    {
                        return new DssServiceRpcScreeningHolder(serverUrl, MAJOR_VERSION_DSS,
                                SERVER_TIMEOUT_MILLIS);
                    }
                };

    private final IScreeningApiServer openbisScreeningServer;

    private final IGeneralInformationService generalInformationService;

    private final IGeneralInformationChangingService generalInformationChangingService;

    private final IDssComponent dssComponent;

    private final DssServiceRpcScreeningMultiplexer dssMultiplexer;

    private final String sessionToken;

    private final int minorVersionApplicationServer;

    private final Map<IImageDatasetIdentifier, ImageDatasetMetadata> imageMetadataCache =
            new ConcurrentHashMap<IImageDatasetIdentifier, ImageDatasetMetadata>();

    private final WellImageCache imageCache = new WellImageCache();

    private IDssServiceRpcScreeningFactory dssServiceCache;

    private final IOpenbisServiceFacade openbisServiceFacade;

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL. Authenticates the user.
     * 
     * @return null if the user could not be authenticated.
     */
    public static IScreeningOpenbisServiceFacade tryCreate(final String userId,
            final String userPassword, final String serverUrl)
    {
        RetryCaller<IScreeningOpenbisServiceFacade, RuntimeException> caller =
                new RetryCaller<IScreeningOpenbisServiceFacade, RuntimeException>()
                    {
                        @Override
                        protected IScreeningOpenbisServiceFacade call()
                        {
                            final IScreeningApiServer openbisServer =
                                    createScreeningOpenbisServer(serverUrl, SERVER_TIMEOUT_MILLIS);
                            final String sessionToken =
                                    openbisServer.tryLoginScreening(userId, userPassword);
                            if (sessionToken == null)
                            {
                                return null;
                            }
                            return tryCreate(sessionToken, serverUrl, openbisServer, null);
                        }
                    };
        return caller.callWithRetry();
    }

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL for an authenticated user.
     * 
     * @param sessionToken The session token for the authenticated user
     * @param serverUrl The URL for the openBIS application server
     */
    public static IScreeningOpenbisServiceFacade tryCreate(final String sessionToken,
            final String serverUrl)
    {
        return tryCreate(sessionToken, serverUrl, (IDssServiceRpcScreening) null, SERVER_TIMEOUT_MILLIS);
    }

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL for an authenticated user. This version should be used
     * when facade is running from the dss process (like a dropbox) and all dss calls should be send to this instance.
     */
    public static IScreeningOpenbisServiceFacade tryCreateWithLocalDss(final String sessionToken, final String serverUrl,
            IDssServiceRpcScreening localDss)
    {
        return tryCreate(sessionToken, serverUrl, localDss, SERVER_TIMEOUT_MILLIS);
    }

    public static IScreeningOpenbisServiceFacade tryCreateWithLocalDss(final String sessionToken, final String serverUrl,
            IDssServiceRpcScreening localDss, long timeout)
    {
        return tryCreate(sessionToken, serverUrl, localDss, timeout);
    }

    private static IScreeningOpenbisServiceFacade tryCreate(final String sessionToken, final String serverUrl,
            final IDssServiceRpcScreening localDss, final long timeout)
    {
        RetryCaller<IScreeningOpenbisServiceFacade, RuntimeException> caller =
                new RetryCaller<IScreeningOpenbisServiceFacade, RuntimeException>()
                    {
                        @Override
                        protected IScreeningOpenbisServiceFacade call()
                        {
                            return tryCreate(sessionToken, serverUrl,
                                    createScreeningOpenbisServer(serverUrl, timeout), localDss);
                        }
                    };
        return caller.callWithRetry();
    }

    /**
     * This constructor is only for use in tests. Do not use it otherwise.
     */
    public static IScreeningOpenbisServiceFacade tryCreateForTest(String sessionToken,
            String serverUrl, final IScreeningApiServer openbisServer)
    {
        return tryCreate(sessionToken, serverUrl, openbisServer, null);
    }

    private static IScreeningOpenbisServiceFacade tryCreate(String sessionToken, String serverUrl,
            final IScreeningApiServer openbisServer, IDssServiceRpcScreening localDss)
    {
        final IGeneralInformationService generalInformationService =
                createGeneralInformationService(serverUrl);
        IGeneralInformationChangingService generalInformationChangingService =
                createGeneralInformationChangingService(serverUrl);
        final int minorVersion = openbisServer.getMinorVersion();
        final IDssComponent dssComponent =
                DssComponentFactory.tryCreate(sessionToken, serverUrl, SERVER_TIMEOUT_MILLIS);

        IScreeningOpenbisServiceFacade facade =
                new ScreeningOpenbisServiceFacade(sessionToken, openbisServer, minorVersion,
                        DSS_SERVICE_FACTORY, dssComponent, generalInformationService,
                        generalInformationChangingService, localDss);

        return RetryProxyFactory.createProxy(facade);
    }

    /**
     * Searchs for Material given a search criteria. This functions purpose is mainly to expose this functionality to ScreeningML.
     */
    @Override
    public List<Material> searchForMaterials(SearchCriteria searchCriteria)
    {
        return this.generalInformationService.searchForMaterials(this.sessionToken, searchCriteria);
    }

    /**
     * Searchs for Samples given a search criteria. This functions purpose is mainly to expose this functionality to ScreeningML.
     */
    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
    {
        return this.generalInformationService.searchForSamples(this.sessionToken, searchCriteria);
    }

    private static IScreeningApiServer createScreeningOpenbisServer(String serverUrl, long timeout)
    {
        ServiceFinder serviceFinder = new ServiceFinder("openbis", OPENBIS_SCREENING_API);
        return serviceFinder.createService(IScreeningApiServer.class, serverUrl, timeout);
    }

    private static IGeneralInformationService createGeneralInformationService(String serverUrl)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        serverUrl);
        return service;
    }

    private static IGeneralInformationChangingService createGeneralInformationChangingService(
            String serverUrl)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationChangingService.SERVICE_URL);
        IGeneralInformationChangingService service =
                generalInformationServiceFinder.createService(
                        IGeneralInformationChangingService.class, serverUrl);
        return service;
    }

    ScreeningOpenbisServiceFacade(String sessionToken, IScreeningApiServer screeningServer,
            int minorVersion, final IDssServiceRpcScreeningFactory dssServiceFactory,
            IDssComponent dssComponent, IGeneralInformationService generalInformationService,
            IGeneralInformationChangingService generalInformationChangingService, final IDssServiceRpcScreening localDss)
    {
        this.openbisScreeningServer = screeningServer;
        this.generalInformationService = generalInformationService;
        this.generalInformationChangingService = generalInformationChangingService;
        this.dssComponent = dssComponent;
        this.sessionToken = sessionToken;
        openbisServiceFacade =
                new OpenbisServiceFacade(sessionToken, generalInformationService,
                        generalInformationChangingService, dssComponent);

        this.minorVersionApplicationServer = minorVersion;

        if (localDss != null)
        {
            dssServiceCache = new IDssServiceRpcScreeningFactory()
                {
                    @Override
                    public DssServiceRpcScreeningHolder createDssService(String serverUrl)
                    {
                        return new DssServiceRpcScreeningHolder(serverUrl, localDss);
                    }
                };
        } else
        {
            dssServiceCache = new IDssServiceRpcScreeningFactory()
                {
                    private final Map<String/* url */, DssServiceRpcScreeningHolder> cache =
                            new HashMap<String, DssServiceRpcScreeningHolder>();

                    @Override
                    public DssServiceRpcScreeningHolder createDssService(String serverUrl)
                    {
                        DssServiceRpcScreeningHolder dssServiceHolder = cache.get(serverUrl);
                        if (dssServiceHolder == null)
                        {
                            dssServiceHolder = dssServiceFactory.createDssService(serverUrl);
                            cache.put(serverUrl, dssServiceHolder);
                        }
                        return dssServiceHolder;
                    }
                };
        }
        IMultiplexer multiplexer = new ThreadPoolMultiplexer("screening-facade-multiplexer");
        dssMultiplexer = new DssServiceRpcScreeningMultiplexer(multiplexer, dssServiceCache);
    }

    /**
     * Return the session token for this authenticated user.
     */
    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    /** Closes connection with the server. After calling this method this facade cannot be used. */
    @Override
    public void logout()
    {
        checkASMinimalMinorVersion("logoutScreening");
        openbisScreeningServer.logoutScreening(sessionToken);
    }

    @Override
    public void clearWellImageCache()
    {
        imageCache.clear();
    }

    /**
     * Return the list of all visible plates assigned to any experiment, along with their hierarchical context (space, project, experiment).
     */
    @Override
    public List<Plate> listPlates()
    {
        checkASMinimalMinorVersion("listPlates");
        return openbisScreeningServer.listPlates(sessionToken);
    }

    @Override
    public List<PlateMetadata> getPlateMetadataList(List<? extends PlateIdentifier> plateIdentifiers)
    {
        checkASMinimalMinorVersion("getPlateMetadataList", List.class);
        return openbisScreeningServer.getPlateMetadataList(sessionToken, plateIdentifiers);
    }

    /**
     * Return the list of all plates for the given <var>experiment</var>.
     */
    @Override
    public List<Plate> listPlates(ExperimentIdentifier experiment)
    {
        if (hasASMethod("listPlates", ExperimentIdentifier.class))
        {
            return openbisScreeningServer.listPlates(sessionToken, experiment);
        } else
        {
            final List<Plate> allPlates = listPlates();
            final List<Plate> result = new ArrayList<Plate>(allPlates.size());
            for (Plate plate : allPlates)
            {
                if (plate.getExperimentIdentifier().getPermId().equals(experiment.getPermId())
                        || plate.getExperimentIdentifier().getAugmentedCode()
                                .equals(experiment.getAugmentedCode()))
                {
                    result.add(plate);
                }
            }
            return result;
        }
    }

    @Override
    public List<Plate> listPlates(ExperimentIdentifier experiment, String analysisProcedure)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                ScreeningConstants.DEFAULT_PLATE_SAMPLE_TYPE_CODE));
        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, experiment.getExperimentCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PROJECT, experiment.getProjectCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.SPACE, experiment.getSpaceCode()));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createExperimentCriteria(experimentCriteria));
        List<Sample> samples = openbisServiceFacade.searchForSamples(searchCriteria);
        List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> dataSets =
                openbisServiceFacade.listDataSets(samples, null);
        Set<String> sampleIdentifiers = new HashSet<String>();
        for (ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataSet : dataSets)
        {
            if (analysisProcedure.equals(dataSet.getProperties().get(
                    ScreeningConstants.ANALYSIS_PROCEDURE)))
            {
                sampleIdentifiers.add(dataSet.getSampleIdentifierOrNull());
            }
        }

        List<Plate> plates = new ArrayList<Plate>();
        for (Sample sample : samples)
        {
            String sampleIdentifier = sample.getIdentifier();
            if (sampleIdentifiers.contains(sampleIdentifier))
            {
                PlateIdentifier augmentedCode = PlateIdentifier.createFromAugmentedCode(sampleIdentifier);
                String spaceCode = augmentedCode.tryGetSpaceCode();
                String projectCode = augmentedCode.tryGetProjectCode();
                String permID = sample.getPermId();
                String experimentIdentifierOrNull = sample.getExperimentIdentifierOrNull();
                ExperimentIdentifier expermientIdentifier =
                        experimentIdentifierOrNull == null ? null : ExperimentIdentifier
                                .createFromAugmentedCode(experimentIdentifierOrNull);
                plates.add(new Plate(sample.getCode(), spaceCode, permID, expermientIdentifier));
            }
        }
        return plates;
    }

    @Override
    public List<ExperimentIdentifier> listExperiments()
    {
        checkASMinimalMinorVersion("listExperiments");
        return openbisScreeningServer.listExperiments(sessionToken);
    }

    @Override
    public List<ExperimentIdentifier> listExperiments(String userId)
    {
        checkASMinimalMinorVersion("listExperiments", String.class);
        return openbisScreeningServer.listExperiments(sessionToken, userId);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing feature vectors.
     */
    @Override
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates)
    {
        checkASMinimalMinorVersion("listFeatureVectorDatasets", List.class);
        return openbisScreeningServer.listFeatureVectorDatasets(sessionToken, plates);
    }

    @Override
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates, String analysisProcedureOrNull)
    {
        List<FeatureVectorDatasetReference> dataSets = listFeatureVectorDatasets(plates);
        return filterByAnalysisProcedure(dataSets, analysisProcedureOrNull);
    }

    private <T extends DatasetReference> List<T> filterByAnalysisProcedure(List<T> dataSets,
            String analysisProcedureOrNull)
    {
        if (analysisProcedureOrNull == null)
        {
            return dataSets;
        }
        List<T> filteredDataSets = new ArrayList<T>();
        for (T dataSet : dataSets)
        {
            if (analysisProcedureOrNull.equals(dataSet.getProperties().get(
                    ScreeningConstants.ANALYSIS_PROCEDURE)))
            {
                filteredDataSets.add(dataSet);
            }
        }
        return filteredDataSets;
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing raw images.
     * 
     * @deprecated Use {@link #listRawImageDatasets(List)} instead.
     */
    @Override
    @Deprecated
    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates)
    {
        checkASMinimalMinorVersion("listImageDatasets", List.class);
        return openbisScreeningServer.listImageDatasets(sessionToken, plates);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing raw images.
     */
    @Override
    public List<ImageDatasetReference> listRawImageDatasets(List<? extends PlateIdentifier> plates)
    {
        if (hasASMethod("listRawImageDatasets", List.class))
        {
            return openbisScreeningServer.listRawImageDatasets(sessionToken, plates);
        } else
        {
            checkASMinimalMinorVersion("listImageDatasets", List.class);
            return openbisScreeningServer.listImageDatasets(sessionToken, plates);
        }
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing images.
     */
    @Override
    public List<ImageDatasetReference> listSegmentationImageDatasets(
            List<? extends PlateIdentifier> plates)
    {
        if (hasASMethod("listSegmentationImageDatasets", List.class))
        {
            return openbisScreeningServer.listSegmentationImageDatasets(sessionToken, plates);
        }
        return Collections.emptyList();
    }

    @Override
    public List<ImageDatasetReference> listSegmentationImageDatasets(
            List<? extends PlateIdentifier> plates, String analysisProcedureOrNull)
    {
        List<ImageDatasetReference> dataSets = listSegmentationImageDatasets(plates);
        return filterByAnalysisProcedure(dataSets, analysisProcedureOrNull);
    }

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to the specified <var>materialIdentifier</var>. If
     * <code>findDatasets == true</code>, find also the connected image and image analysis data sets for the relevant plates.
     */
    @Override
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets)
    {
        checkASMinimalMinorVersion("listPlateWells", ExperimentIdentifier.class,
                MaterialIdentifier.class, boolean.class);
        return openbisScreeningServer.listPlateWells(sessionToken, experimentIdentifer,
                materialIdentifier, findDatasets);
    }

    /**
     * For the given <var>materialIdentifier</var> find all plate locations that are connected to it. If <code>findDatasets == true</code>, find also
     * the connected image and image analysis data sets for the relevant plates.
     */
    @Override
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        checkASMinimalMinorVersion("listPlateWells", MaterialIdentifier.class, boolean.class);
        return openbisScreeningServer
                .listPlateWells(sessionToken, materialIdentifier, findDatasets);
    }

    /**
     * For the given <var>plateIdentifier</var> find all wells that are connected to it.
     */
    @Override
    public List<WellIdentifier> listPlateWells(PlateIdentifier plateIdentifier)
    {
        checkASMinimalMinorVersion("listPlateWells", PlateIdentifier.class);
        return openbisScreeningServer.listPlateWells(sessionToken, plateIdentifier);
    }

    @Override
    public Map<String, String> getPlateProperties(PlateIdentifier plateIdentifier)
    {
        Sample plateSample = openbisScreeningServer.getPlateSample(sessionToken, plateIdentifier);
        Map<String, String> properties = plateSample.getProperties();
        return properties;
    }

    @Override
    public void updatePlateProperties(PlateIdentifier plateIdentifier, Map<String, String> properties)
    {
        Sample plateSample = openbisScreeningServer.getPlateSample(sessionToken, plateIdentifier);
        generalInformationChangingService.updateSampleProperties(sessionToken, plateSample.getId(),
                properties);
    }

    @Override
    public Map<String, String> getWellProperties(WellIdentifier wellIdentifier)
    {
        Sample wellSample = openbisScreeningServer.getWellSample(sessionToken, wellIdentifier);
        Map<String, String> properties = wellSample.getProperties();
        return properties;
    }

    @Override
    public void updateWellProperties(WellIdentifier wellIdentifier, Map<String, String> properties)
    {
        Sample wellSample = openbisScreeningServer.getWellSample(sessionToken, wellIdentifier);
        generalInformationChangingService.updateSampleProperties(sessionToken, wellSample.getId(),
                properties);
    }

    /**
     * Get proxies to the data sets owned by specified well.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Override
    public List<IDataSetDss> getDataSets(WellIdentifier wellIdentifier,
            String datasetTypeCodePattern) throws IllegalStateException,
            EnvironmentFailureException
    {
        return getDataSets(wellIdentifier, new TypeBasedDataSetFilter(datasetTypeCodePattern));
    }

    @Override
    public List<IDataSetDss> getDataSets(WellIdentifier wellIdentifier, IDataSetFilter dataSetFilter)
            throws IllegalStateException, EnvironmentFailureException
    {
        final Sample wellSample = getWellSample(wellIdentifier);
        return getDataSets(wellSample, dataSetFilter);
    }

    @Override
    public IDataSetDss getDataSet(String dataSetCode) throws IllegalStateException,
            EnvironmentFailureException
    {
        return RetryProxyFactory.createProxy(dssComponent.getDataSet(dataSetCode));
    }

    /**
     * Get proxies to the data sets owned by specified plate.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     */
    @Override
    public List<IDataSetDss> getDataSets(PlateIdentifier plateIdentifier,
            final String datasetTypeCodePattern) throws IllegalStateException,
            EnvironmentFailureException
    {
        return getDataSets(plateIdentifier, new TypeBasedDataSetFilter(datasetTypeCodePattern));
    }

    @Override
    public List<IDataSetDss> getDataSets(PlateIdentifier plateIdentifier,
            IDataSetFilter dataSetFilter) throws IllegalStateException, EnvironmentFailureException
    {
        checkASMinimalMinorVersion("getPlateSample", PlateIdentifier.class);
        Sample sample = openbisScreeningServer.getPlateSample(sessionToken, plateIdentifier);
        return getDataSets(sample, dataSetFilter);
    }

    private List<IDataSetDss> getDataSets(final Sample sample, IDataSetFilter filter)
    {
        final List<DataSet> dataSets =
                generalInformationService.listDataSetsForSample(sessionToken, sample, true);
        final List<IDataSetDss> result = new ArrayList<IDataSetDss>();
        for (DataSet dataSet : dataSets)
        {
            if (filter.pass(dataSet))
            {
                IDataSetDss dataSetDss = dssComponent.getDataSet(dataSet.getCode());
                result.add(RetryProxyFactory.createProxy(dataSetDss));
            }
        }
        return result;
    }

    @Override
    public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getFullDataSets(
            PlateIdentifier plateIdentifier, IDataSetFilter dataSetFilter)
            throws IllegalStateException, EnvironmentFailureException
    {
        checkASMinimalMinorVersion("getPlateSample", PlateIdentifier.class);
        Sample sample = openbisScreeningServer.getPlateSample(sessionToken, plateIdentifier);
        return getFullDataSets(sample, dataSetFilter);
    }

    private List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getFullDataSets(
            final Sample sample, IDataSetFilter filter)
    {
        final List<DataSet> dataSets =
                generalInformationService.listDataSets(sessionToken, Arrays.asList(sample),
                        EnumSet.of(Connections.PARENTS));
        final List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> result =
                new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
        for (DataSet dataSet : dataSets)
        {
            if (filter.pass(dataSet))
            {
                ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet fullDataset =
                        new ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet(
                                openbisServiceFacade, dssComponent, dataSet, null);

                result.add(fullDataset);
            }
        }
        return result;
    }

    @Override
    public List<IDataSetDss> getDataSets(final ExperimentIdentifier experimentIdentifier,
            IDataSetFilter filter)
    {
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken,
                        Collections.singletonList(experimentIdentifier.getAugmentedCode()));

        final List<DataSet> dataSets =
                generalInformationService.listDataSetsForExperiments(sessionToken, experiments,
                        EnumSet.of(Connections.PARENTS));

        final List<IDataSetDss> result = new ArrayList<IDataSetDss>();
        for (DataSet dataSet : dataSets)
        {
            if (filter.pass(dataSet))
            {
                IDataSetDss dataSetDss = dssComponent.getDataSet(dataSet.getCode());
                result.add(RetryProxyFactory.createProxy(dataSetDss));
            }
        }
        return result;
    }

    @Override
    public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getFullDataSets(
            ExperimentIdentifier experimentIdentifier, IDataSetFilter dataSetFilter)
            throws IllegalStateException, EnvironmentFailureException
    {
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken,
                        Collections.singletonList(experimentIdentifier.getAugmentedCode()));
        if (experiments.isEmpty())
        {
            throw UserFailureException.fromTemplate("Experiment '%s' does not exist.",
                    experimentIdentifier);
        }
        return getFullDataSets(experiments, dataSetFilter);
    }

    private List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getFullDataSets(
            final List<Experiment> experiments, IDataSetFilter filter)
    {
        final List<DataSet> dataSets =
                generalInformationService.listDataSetsForExperiments(sessionToken, experiments,
                        EnumSet.of(Connections.PARENTS));
        final List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> result =
                new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
        for (DataSet dataSet : dataSets)
        {
            if (filter.pass(dataSet))
            {
                ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet fullDataset =
                        new ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet(
                                openbisServiceFacade, dssComponent, dataSet, null);

                result.add(fullDataset);
            }
        }
        return result;
    }

    @Override
    public List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> getDataSetMetaData(
            List<String> dataSetCodes)
    {
        List<DataSet> dataSets =
                generalInformationService.getDataSetMetaData(sessionToken, dataSetCodes);
        final List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> result =
                new ArrayList<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
        for (DataSet dataSet : dataSets)
        {
            ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet fullDataset =
                    new ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet(openbisServiceFacade,
                            dssComponent, dataSet, null);
            result.add(fullDataset);
        }
        return result;
    }

    /**
     * Upload a new data set to the DSS for a well.
     * 
     * @param wellIdentifier Identifier of a well that should become owner of the new data set
     * @param dataSetFile A file or folder containing the data
     * @param dataSetMetadataOrNull The optional metadata overriding server defaults for the new data set
     * @return A proxy to the newly added data set
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to the server.
     * @throws IOException when accessing the data set file or folder fails
     */
    @Override
    public IDataSetDss putDataSet(WellIdentifier wellIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException
    {
        final Sample wellSample = getWellSample(wellIdentifier);
        return createDataSetDss(wellSample, dataSetMetadataOrNull, dataSetFile);
    }

    private Sample getWellSample(WellIdentifier wellIdentifier)
    {
        checkASMinimalMinorVersion("getWellSample", WellIdentifier.class);
        return openbisScreeningServer.getWellSample(sessionToken, wellIdentifier);
    }

    @Override
    public IDataSetDss putDataSet(PlateIdentifier plateIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException
    {
        Sample sample = openbisScreeningServer.getPlateSample(sessionToken, plateIdentifier);
        return createDataSetDss(sample, dataSetMetadataOrNull, dataSetFile);
    }

    @Override
    public IDataSetDss putDataSet(ExperimentIdentifier experimentIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException
    {
        final DataSetOwner dataSetOwner =
                new DataSetOwner(DataSetOwnerType.EXPERIMENT,
                        experimentIdentifier.getAugmentedCode());
        final NewDataSetMetadataDTO dataSetMetadata =
                (dataSetMetadataOrNull == null) ? new NewDataSetMetadataDTO()
                        : dataSetMetadataOrNull;
        return createDatasetDss(dataSetMetadata, dataSetFile, dataSetOwner);
    }

    private IDataSetDss createDataSetDss(Sample sample,
            NewDataSetMetadataDTO dataSetMetadataOrNull, File dataSetFile) throws IOException
    {
        final DataSetOwner dataSetOwner =
                new DataSetOwner(DataSetOwnerType.SAMPLE, sample.getIdentifier());
        final NewDataSetMetadataDTO dataSetMetadata =
                (dataSetMetadataOrNull == null) ? new NewDataSetMetadataDTO()
                        : dataSetMetadataOrNull;

        return createDatasetDss(dataSetMetadata, dataSetFile, dataSetOwner);
    }

    private IDataSetDss createDatasetDss(NewDataSetMetadataDTO dataSetMetadata, File dataSetFile,
            final DataSetOwner dataSetOwner) throws IOException
    {
        final String dataSetFolderNameOrNull =
                dataSetFile.isDirectory() ? dataSetFile.getName() : null;
        final List<FileInfoDssDTO> fileInfos = getFileInfosForPath(dataSetFile);
        final NewDataSetDTO newDataSet =
                new NewDataSetDTO(dataSetMetadata, dataSetOwner, dataSetFolderNameOrNull, fileInfos);
        IDataSetDss dataSetDss = dssComponent.putDataSet(newDataSet, dataSetFile);
        return RetryProxyFactory.createProxy(dataSetDss);
    }

    private List<FileInfoDssDTO> getFileInfosForPath(File file) throws IOException
    {
        ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
        if (false == file.exists())
        {
            return fileInfos;
        }

        String path = file.getCanonicalPath();
        if (false == file.isDirectory())
        {
            path = file.getParentFile().getCanonicalPath();
        }

        FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
        builder.appendFileInfosForFile(file, fileInfos, true);
        return fileInfos;
    }

    /**
     * Converts a given list of dataset codes to dataset identifiers which can be used in other API calls.
     */
    @Override
    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes)
    {
        checkASMinimalMinorVersion("getDatasetIdentifiers", List.class);
        return openbisScreeningServer.getDatasetIdentifiers(sessionToken, datasetCodes);
    }

    @Override
    public List<String> listAvailableFeatureNames(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return listAvailableFeatureCodes(featureDatasets);
    }

    /**
     * For a given set of feature vector data sets provides the list of all available features. This is just the code of the feature. If for different
     * data sets different sets of features are available, provides the union of the feature names of all data sets.
     */
    @Override
    public List<String> listAvailableFeatureCodes(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String> handler =
                new IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String>()
                    {
                        @Override
                        @SuppressWarnings("deprecation")
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "listAvailableFeatureNames",
                                    List.class);
                            // Use old method in order to allow accessing older servers.
                            return dssService.getService().listAvailableFeatureNames(sessionToken,
                                    references);
                        }
                    };

        return dssMultiplexer.process(featureDatasets, handler).getMergedBatchResultsWithoutDuplicates();
    }

    @Override
    public List<String> listAvailableFeatureLists(IFeatureVectorDatasetIdentifier featureDataset)
    {
        IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String> handler =
                new IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "getFeatureList",
                                    IFeatureVectorDatasetIdentifier.class, String.class);

                            return dssService.getService().listAvailableFeatureLists(sessionToken,
                                    references.get(0));
                        }
                    };

        return dssMultiplexer.process(Collections.singletonList(featureDataset), handler)
                .getMergedBatchResultsWithoutDuplicates();
    }

    @Override
    public List<String> getFeatureList(IFeatureVectorDatasetIdentifier featureDataset,
            final String featureListCode)
    {
        IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String> handler =
                new IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "getFeatureList",
                                    IFeatureVectorDatasetIdentifier.class, String.class);

                            return dssService.getService().getFeatureList(sessionToken,
                                    references.get(0), featureListCode);
                        }
                    };

        return dssMultiplexer.process(Collections.singletonList(featureDataset), handler)
                .getMergedBatchResultsWithoutDuplicates();
    }

    /**
     * For a given set of feature vector data sets provide the list of all available features. This contains the code, label and description of the
     * feature. If for different data sets different sets of features are available, provide the union of the features of all data sets. Only
     * available when all data store services have minor version 9 or newer.
     */
    @Override
    public List<FeatureInformation> listAvailableFeatures(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, FeatureInformation> handler =
                new IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, FeatureInformation>()
                    {
                        @Override
                        public List<FeatureInformation> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            if (hasDSSMethod(dssService, "listAvailableFeatures", List.class))
                            {
                                return dssService.getService().listAvailableFeatures(sessionToken,
                                        references);
                            } else
                            {
                                checkDSSMinimalMinorVersion(dssService,
                                        "listAvailableFeatureNames", List.class);

                                List<FeatureInformation> result =
                                        new ArrayList<FeatureInformation>();

                                // Use old method in order to allow accessing older servers.
                                @SuppressWarnings("deprecation")
                                final List<String> codes =
                                        dssService.getService().listAvailableFeatureNames(
                                                sessionToken, references);
                                for (String code : codes)
                                {
                                    result.add(new FeatureInformation(code, code, ""));
                                }

                                return result;
                            }
                        }
                    };

        return dssMultiplexer.process(featureDatasets, handler).getMergedBatchResultsWithoutDuplicates();
    }

    /**
     * For a given set of plates and a set of features (given by their code), provide all the feature vectors.
     * 
     * @param plates The plates to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the <var>featureDatasets</var>.
     */
    @Override
    public List<FeatureVectorDataset> loadFeaturesForPlates(List<? extends PlateIdentifier> plates,
            final List<String> featureCodesOrNull)
    {
        final List<FeatureVectorDatasetReference> datasets = listFeatureVectorDatasets(plates);
        return loadFeatures(datasets, featureCodesOrNull);
    }

    @Override
    public List<FeatureVectorDataset> loadFeaturesForPlates(List<? extends PlateIdentifier> plates,
            List<String> featureCodesOrNull, String analysisProcedureOrNull)
    {
        List<FeatureVectorDatasetReference> datasets =
                listFeatureVectorDatasets(plates, analysisProcedureOrNull);
        return loadFeatures(datasets, featureCodesOrNull);
    }

    /**
     * For a given set of data sets and a set of features (given by their code), provide all the feature vectors.
     * 
     * @param featureDatasets The data sets to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the <var>featureDatasets</var>.
     */
    @Override
    public List<FeatureVectorDataset> loadFeatures(
            List<FeatureVectorDatasetReference> featureDatasets,
            final List<String> featureCodesOrNull)
    {
        final List<String> featureNames =
                (isEmpty(featureCodesOrNull)) ? listAvailableFeatureNames(featureDatasets)
                        : featureCodesOrNull;

        IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetReference, FeatureVectorDataset> handler =
                new IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetReference, FeatureVectorDataset>()
                    {
                        @Override
                        public List<FeatureVectorDataset> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<FeatureVectorDatasetReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadFeatures", List.class,
                                    List.class);
                            return dssService.getService().loadFeatures(sessionToken, references,
                                    featureNames);
                        }
                    };

        return dssMultiplexer.process(featureDatasets, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    public List<FeatureVectorDatasetWellReference> convertToFeatureVectorDatasetWellIdentifier(
            List<PlateWellReferenceWithDatasets> plateWellReferenceWithDataSets)
    {
        final List<FeatureVectorDatasetWellReference> result =
                new ArrayList<FeatureVectorDatasetWellReference>(
                        plateWellReferenceWithDataSets.size());
        for (PlateWellReferenceWithDatasets plateWellRef : plateWellReferenceWithDataSets)
        {
            for (FeatureVectorDatasetReference fvdr : plateWellRef
                    .getFeatureVectorDatasetReferences())
            {
                result.add(createFVDatasetReference(fvdr, plateWellRef.getWellPosition()));
            }
        }
        return result;
    }

    private FeatureVectorDatasetWellReference createFVDatasetReference(
            FeatureVectorDatasetReference fvdr, WellPosition wellPosition)
    {
        return new FeatureVectorDatasetWellReference(fvdr.getDatasetCode(), fvdr.getDataSetType(),
                fvdr.getDatastoreServerUrl(), fvdr.getPlate(), fvdr.getExperimentIdentifier(),
                fvdr.getPlateGeometry(), fvdr.getRegistrationDate(), fvdr.getParentImageDataset(),
                fvdr.getProperties(), wellPosition);
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            final List<FeatureVectorDatasetWellReference> datasetWellReferences,
            final List<String> featureCodesOrNull)
    {
        final List<String> featureNames =
                (isEmpty(featureCodesOrNull)) ? listAvailableFeatureNames(datasetWellReferences)
                        : featureCodesOrNull;

        IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetWellReference, FeatureVectorWithDescription> handler =
                new IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetWellReference, FeatureVectorWithDescription>()
                    {
                        @Override
                        public List<FeatureVectorWithDescription> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<FeatureVectorDatasetWellReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService,
                                    "loadFeaturesForDatasetWellReferences", List.class, List.class);
                            return dssService.getService().loadFeaturesForDatasetWellReferences(
                                    sessionToken, references, featureNames);
                        }
                    };

        return dssMultiplexer.process(datasetWellReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    private boolean isEmpty(final List<String> featureCodeOrNull)
    {
        return featureCodeOrNull == null || featureCodeOrNull.isEmpty();
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            List<String> featureCodesOrNull)
    {
        return loadFeaturesForPlateWells(experimentIdentifer, materialIdentifier, null,
                featureCodesOrNull);
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            String analysisProcedureOrNull, List<String> featureCodesOrNull)
    {
        final List<PlateWellReferenceWithDatasets> plateWellRefs =
                listPlateWells(experimentIdentifer, materialIdentifier, true);
        return loadFeatureVectors(featureCodesOrNull, analysisProcedureOrNull, plateWellRefs);
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            MaterialIdentifier materialIdentifier, List<String> featureCodesOrNull)
    {
        return loadFeaturesForPlateWells(materialIdentifier, null, featureCodesOrNull);
    }

    @Override
    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            MaterialIdentifier materialIdentifier, String analysisProcedureOrNull,
            List<String> featureCodesOrNull)
    {
        final List<PlateWellReferenceWithDatasets> plateWellRefs =
                listPlateWells(materialIdentifier, true);
        return loadFeatureVectors(featureCodesOrNull, analysisProcedureOrNull, plateWellRefs);
    }

    private List<FeatureVectorWithDescription> loadFeatureVectors(List<String> featureCodesOrNull,
            String analysisProcedureOrNull, final List<PlateWellReferenceWithDatasets> plateWellRefs)
    {
        final List<String> featureCodes =
                isEmpty(featureCodesOrNull) ? listAvailableFeatureCodesForPlateWells(plateWellRefs)
                        : featureCodesOrNull;
        final List<FeatureVectorDatasetWellReference> datasetWellReferences =
                filterByAnalysisProcedure(
                        convertToFeatureVectorDatasetWellIdentifier(plateWellRefs),
                        analysisProcedureOrNull);
        final List<FeatureVectorWithDescription> featureVectors =
                loadFeaturesForDatasetWellReferences(datasetWellReferences, featureCodes);
        return featureVectors;
    }

    private List<String> listAvailableFeatureCodesForPlateWells(
            final List<PlateWellReferenceWithDatasets> plateWellRefs)
    {
        final List<FeatureVectorDatasetReference> featureVectorDatasetReferences =
                new ArrayList<FeatureVectorDatasetReference>(plateWellRefs.size());
        for (PlateWellReferenceWithDatasets plateWellRef : plateWellRefs)
        {
            featureVectorDatasetReferences.addAll(plateWellRef.getFeatureVectorDatasetReferences());
        }
        final List<String> availableFeatureCodes =
                listAvailableFeatureCodes(featureVectorDatasetReferences);
        return availableFeatureCodes;
    }

    /**
     * An interface to provide mapping between image references and output streams where the images should be saved.
     */
    public static interface IImageOutputStreamProvider
    {
        /**
         * @return output stream where the image for the specified reference should be saved.
         * @throws IOException when creating the output stream fails
         */
        OutputStream getOutputStream(PlateImageReference imageReference) throws IOException;
    }

    @Override
    public List<WellPosition> convertToWellPositions(List<WellIdentifier> wellIds)
    {
        final List<WellPosition> result = new ArrayList<WellPosition>(wellIds.size());
        for (WellIdentifier id : wellIds)
        {
            result.add(id.getWellPosition());
        }
        return result;
    }

    @Override
    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef)
    {
        return createPlateImageReferences(imageDatasetRef, null, null, null);
    }

    @Override
    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef, List<String> channelCodesOrNull,
            List<WellPosition> wellsOrNull)
    {
        return createPlateImageReferences(imageDatasetRef, null, channelCodesOrNull, wellsOrNull);
    }

    @Override
    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef, ImageDatasetMetadata metadataOrNull,
            List<String> channelCodesOrNull, List<WellPosition> wellsOrNull)
    {
        final List<WellPosition> wellsToUse =
                (wellsOrNull == null || wellsOrNull.isEmpty()) ? createWellPositions(imageDatasetRef
                        .getPlateGeometry()) : wellsOrNull;
        return createPlateImageReferences((IImageDatasetIdentifier) imageDatasetRef,
                metadataOrNull, channelCodesOrNull, wellsToUse);
    }

    @Override
    public List<PlateImageReference> createPlateImageReferences(
            IImageDatasetIdentifier imageDatasetId, List<String> channeldCodesOrNull,
            List<WellPosition> wellsToUse)
    {
        return createPlateImageReferences(imageDatasetId, null, channeldCodesOrNull, wellsToUse);
    }

    @Override
    public List<PlateImageReference> createPlateImageReferences(
            IImageDatasetIdentifier imageDatasetId, ImageDatasetMetadata metadataOrNull,
            List<String> channelCodesOrNull, List<WellPosition> wellsToUse)
    {
        final ImageDatasetMetadata metadata = getImageMetadata(imageDatasetId, metadataOrNull);

        final List<String> channelsToUse =
                (channelCodesOrNull == null || channelCodesOrNull.isEmpty()) ? metadata
                        .getChannelCodes() : channelCodesOrNull;
        final List<PlateImageReference> result =
                new ArrayList<PlateImageReference>(wellsToUse.size()
                        * metadata.getNumberOfChannels() * metadata.getNumberOfTiles());
        for (WellPosition well : wellsToUse)
        {
            for (String channel : channelsToUse)
            {
                for (int tile = 0; tile < metadata.getNumberOfTiles(); ++tile)
                {
                    result.add(new PlateImageReference(tile, channel, well, metadata
                            .getImageDataset()));
                }
            }
        }
        return result;
    }

    private ImageDatasetMetadata getImageMetadata(IImageDatasetIdentifier imageDatasetRef,
            ImageDatasetMetadata metadataOrNull)
    {
        if (metadataOrNull != null)
        {
            return metadataOrNull;
        }
        return listImageMetadata(imageDatasetRef);
    }

    private List<WellPosition> createWellPositions(Geometry plateGeometry)
    {
        final List<WellPosition> result =
                new ArrayList<WellPosition>(plateGeometry.getNumberOfRows()
                        * plateGeometry.getNumberOfColumns());
        for (int row = 1; row <= plateGeometry.getNumberOfRows(); ++row)
        {
            for (int col = 1; col <= plateGeometry.getNumberOfColumns(); ++col)
            {
                result.add(new WellPosition(row, col));
            }
        }
        return result;
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position, channel and tile) in the provided output streams.
     * Output streams will not be closed automatically.<br/>
     * <p>
     * If there is an image reference specified which is not referring to the existing image on the server, nothing will be written to the output
     * stream returned by the output streams provider. No exception will be thrown.
     * </p>
     * The images will be converted to PNG format before being shipped.<br/>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams fails
     */
    @Override
    public void loadImages(List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider) throws IOException
    {
        loadImages(imageReferences, outputStreamProvider, true);
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position, channel and tile) in the provided output streams.
     * Output streams will not be closed automatically.<br/>
     * <p>
     * If there is an image reference specified which is not referring to the existing image on the server, nothing will be written to the output
     * stream returned by the output streams provider. No exception will be thrown.
     * </p>
     * If <code>convertToPng==true</code>, the images will be converted to PNG format before being shipped, otherwise they will be shipped in the
     * format that they are stored on the server.<br/>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams fails
     */
    @Override
    public void loadImages(final List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider, final boolean convertToPNG)
            throws IOException
    {
        try
        {
            IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                    new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                        {
                            @Override
                            public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                    List<PlateImageReference> references)
                            {
                                final InputStream stream;
                                if (hasDSSMethod(dssService, "loadImages", List.class,
                                        boolean.class))
                                {
                                    // Only available since v1.3
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references, convertToPNG);
                                } else
                                {
                                    checkDSSMinimalMinorVersion(dssService, "loadImages",
                                            List.class);
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references);
                                }
                                try
                                {
                                    final ConcatenatedFileOutputStreamWriter imagesWriter =
                                            new ConcatenatedFileOutputStreamWriter(stream);
                                    for (PlateImageReference imageRef : references)
                                    {
                                        OutputStream output =
                                                outputStreamProvider.getOutputStream(imageRef);
                                        imagesWriter.writeNextBlock(output);
                                    }
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                } finally
                                {
                                    try
                                    {
                                        stream.close();
                                    } catch (IOException ex)
                                    {
                                        throw new WrappedIOException(ex);
                                    }
                                }

                                return null;
                            }
                        };

            dssMultiplexer.process(imageReferences, handler);

        } catch (WrappedIOException ex)
        {
            throw ex.getIoException();
        }
    }

    @Override
    public void loadImages(final List<PlateImageReference> imageReferences,
            final boolean convertToPNG, final IPlateImageHandler plateImageHandler)
            throws IOException
    {
        try
        {
            IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                    new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                        {
                            @Override
                            public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                    List<PlateImageReference> references)
                            {
                                final InputStream stream;
                                if (hasDSSMethod(dssService, "loadImages", List.class,
                                        boolean.class))
                                {
                                    // Only available since v1.3
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references, convertToPNG);
                                } else
                                {
                                    checkDSSMinimalMinorVersion(dssService, "loadImages",
                                            List.class);
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references);
                                }

                                processImagesStreamUnchecked(plateImageHandler, imageReferences,
                                        stream);

                                return null;
                            }
                        };

            dssMultiplexer.process(imageReferences, handler);

        } catch (WrappedIOException ex)
        {
            throw ex.getIoException();
        }
    }

    @Override
    public List<byte[]> loadImages(IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
            throws IOException
    {
        DssServiceRpcScreeningHolder dssServiceHolder =
                dssServiceCache.createDssService(dataSetIdentifier.getDatastoreServerUrl());
        InputStream stream =
                dssServiceHolder.getService().loadImages(sessionToken, dataSetIdentifier,
                        wellPositions, channel, thumbnailSizeOrNull);
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(stream);
        List<byte[]> result = new ArrayList<byte[]>();
        long size;
        do
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            size = imagesWriter.writeNextBlock(outputStream);
            if (size > 0)
            {
                result.add(outputStream.toByteArray());
            }
        } while (size >= 0);
        return result;
    }

    @Override
    public void loadImages(IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            String channel, ImageSize thumbnailSizeOrNull, IPlateImageHandler plateImageHandler)
            throws IOException
    {
        final DssServiceRpcScreeningHolder dssServiceHolder =
                dssServiceCache.createDssService(dataSetIdentifier.getDatastoreServerUrl());
        final IDssServiceRpcScreening service = dssServiceHolder.getService();
        checkDSSMinimalMinorVersion(dssServiceHolder, "listPlateImageReferences",
                IDatasetIdentifier.class, List.class, String.class);
        final List<PlateImageReference> plateImageReferences =
                service.listPlateImageReferences(sessionToken, dataSetIdentifier, wellPositions,
                        channel);
        checkDSSMinimalMinorVersion(dssServiceHolder, "loadImages", List.class, ImageSize.class);
        final InputStream stream =
                service.loadImages(sessionToken, plateImageReferences, thumbnailSizeOrNull);
        processImagesStream(plateImageHandler, plateImageReferences, stream);
    }

    @Override
    public byte[] loadImageWellCaching(final PlateImageReference imageReference,
            final ImageSize imageSizeOrNull) throws IOException
    {
        // PlateImageReference should really implement IImageDatasetIdentifier, however it doesn't,
        // so we need to convert to ImageDatasetReference here.
        final IImageDatasetIdentifier imageDatasetId =
                new ImageDatasetReference(imageReference.getDatasetCode(), null,
                        imageReference.getDatastoreServerUrl(), null, null, null, null, null, null);
        final ImageDatasetMetadata imageMetadata = listImageMetadata(imageDatasetId);
        final ImageSize size =
                (imageSizeOrNull == null) ? new ImageSize(imageMetadata.getWidth(),
                        imageMetadata.getHeight()) : imageSizeOrNull;
        final WellImages images = imageCache.getWellImages(imageReference, size, imageMetadata);
        if (images.isLoaderCall())
        {
            try
            {
                final List<PlateImageReference> imageReferences =
                        createPlateImageReferences(imageDatasetId, imageMetadata, null,
                                Collections.singletonList(imageReference.getWellPosition()));
                loadImages(imageReferences, imageSizeOrNull, new IPlateImageHandler()
                    {
                        @Override
                        public void handlePlateImage(PlateImageReference plateImageReference,
                                byte[] imageFileBytes)
                        {
                            images.putImage(plateImageReference, imageFileBytes);
                        }
                    });
            } catch (IOException ex)
            {
                images.cancel(ex);
                throw ex;
            } catch (RuntimeException ex)
            {
                images.cancel(ex);
                throw ex;
            }
        }
        final CachedImage imageOrNull = images.getImage(imageReference);
        if (imageOrNull == null)
        {
            throw new IOException(imageReference + " doesn't exist.");
        }
        return imageOrNull.getImageData();
    }

    @Override
    public void loadImages(List<PlateImageReference> imageReferences, final ImageSize sizeOrNull,
            final IPlateImageHandler plateImageHandler) throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadImages", List.class,
                                    ImageSize.class);
                            final InputStream stream =
                                    dssService.getService().loadImages(sessionToken, references,
                                            sizeOrNull);

                            processImagesStreamUnchecked(plateImageHandler, references, stream);
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    @Override
    public byte[] loadThumbnailImageWellCaching(final PlateImageReference imageReference)
            throws IOException
    {
        // PlateImageReference should really implement IImageDatasetIdentifier, however it doesn't,
        // so we need to convert to ImageDatasetReference here.
        final IImageDatasetIdentifier imageDatasetId =
                new ImageDatasetReference(imageReference.getDatasetCode(), null,
                        imageReference.getDatastoreServerUrl(), null, null, null, null, null, null);
        final ImageDatasetMetadata imageMetadata = listImageMetadata(imageDatasetId);
        if (false == imageMetadata.hasThumbnails())
        {
            String error =
                    String.format("No thumbnail images for data set '%s' have been "
                            + "found on the server", imageMetadata.getImageDataset()
                                    .getDatasetCode());
            throw new RuntimeException(error);
        }

        final WellImages images =
                imageCache.getWellImages(
                        imageReference,
                        new ImageSize(imageMetadata.getThumbnailWidth(), imageMetadata
                                .getThumbnailHeight()),
                        imageMetadata);
        if (images.isLoaderCall())
        {
            try
            {
                final List<PlateImageReference> imageReferences =
                        createPlateImageReferences(imageDatasetId, imageMetadata, null,
                                Collections.singletonList(imageReference.getWellPosition()));
                loadThumbnailImages(imageReferences, new IPlateImageHandler()
                    {
                        @Override
                        public void handlePlateImage(PlateImageReference plateImageReference,
                                byte[] imageFileBytes)
                        {
                            images.putImage(plateImageReference, imageFileBytes);
                        }
                    });

            } catch (IOException ex)
            {
                images.cancel(ex);
                throw ex;
            } catch (RuntimeException ex)
            {
                images.cancel(ex);
                throw ex;
            }
        }
        final CachedImage imageOrNull = images.getImage(imageReference);
        if (imageOrNull == null)
        {
            throw new IOException(imageReference + " doesn't exist.");
        }
        return imageOrNull.getImageData();
    }

    @Override
    public void loadThumbnailImages(List<PlateImageReference> imageReferences,
            final IPlateImageHandler plateImageHandler) throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadThumbnailImages",
                                    List.class);
                            final InputStream stream =
                                    dssService.getService().loadThumbnailImages(sessionToken,
                                            references);
                            processImagesStreamUnchecked(plateImageHandler, references, stream);
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    @Override
    public void loadThumbnailImages(List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider) throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadThumbnailImages",
                                    List.class);
                            final InputStream stream =
                                    dssService.getService().loadThumbnailImages(sessionToken,
                                            references);
                            try
                            {
                                final ConcatenatedFileOutputStreamWriter imagesWriter =
                                        new ConcatenatedFileOutputStreamWriter(stream);
                                for (PlateImageReference imageRef : references)
                                {
                                    OutputStream output =
                                            outputStreamProvider.getOutputStream(imageRef);
                                    imagesWriter.writeNextBlock(output);
                                }
                            } catch (IOException ex)
                            {
                                throw new WrappedIOException(ex);
                            } finally
                            {
                                try
                                {
                                    stream.close();
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                }
                            }
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    @Override
    public void loadPhysicalThumbnails(List<PlateImageReference> imageReferences,
            final ImageRepresentationFormat format, final IPlateImageHandler plateImageHandler)
            throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadPhysicalThumbnails",
                                    List.class, ImageRepresentationFormat.class);
                            final InputStream stream =
                                    dssService.getService().loadPhysicalThumbnails(sessionToken,
                                            references, format);
                            processImagesStreamUnchecked(plateImageHandler, references, stream);
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    @Override
    public void loadPhysicalThumbnails(List<PlateImageReference> imageReferences,
            final ImageRepresentationFormat format,
            final IImageOutputStreamProvider outputStreamProvider) throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadPhysicalThumbnails",
                                    List.class, ImageRepresentationFormat.class);
                            final InputStream stream =
                                    dssService.getService().loadPhysicalThumbnails(sessionToken,
                                            references, format);
                            try
                            {
                                final ConcatenatedFileOutputStreamWriter imagesWriter =
                                        new ConcatenatedFileOutputStreamWriter(stream);
                                for (PlateImageReference imageRef : references)
                                {
                                    OutputStream output =
                                            outputStreamProvider.getOutputStream(imageRef);
                                    imagesWriter.writeNextBlock(output);
                                }
                            } catch (IOException ex)
                            {
                                throw new WrappedIOException(ex);
                            } finally
                            {
                                try
                                {
                                    stream.close();
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                }
                            }
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    @Override
    public void saveImageTransformerFactory(List<IDatasetIdentifier> dataSetIdentifiers,
            String channel, IImageTransformerFactory transformerFactoryOrNull)
    {
        Map<String, List<IDatasetIdentifier>> map =
                DssServiceRpcScreeningMultiplexer.getReferencesPerDataStore(dataSetIdentifiers);
        Set<Entry<String, List<IDatasetIdentifier>>> entrySet = map.entrySet();
        for (Entry<String, List<IDatasetIdentifier>> entry : entrySet)
        {
            String serverUrl = entry.getKey();
            IDssServiceRpcScreening service =
                    dssServiceCache.createDssService(serverUrl).getService();
            service.saveImageTransformerFactory(sessionToken, entry.getValue(), channel,
                    transformerFactoryOrNull);
        }
    }

    @Override
    public IImageTransformerFactory getImageTransformerFactoryOrNull(
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        Map<String, List<IDatasetIdentifier>> map =
                DssServiceRpcScreeningMultiplexer.getReferencesPerDataStore(dataSetIdentifiers);
        Set<Entry<String, List<IDatasetIdentifier>>> entrySet = map.entrySet();
        if (entrySet.size() != 1)
        {
            throw new IllegalArgumentException("Only one data store expected instead of "
                    + map.keySet());
        }
        Entry<String, List<IDatasetIdentifier>> entry = entrySet.iterator().next();
        IDssServiceRpcScreening service =
                dssServiceCache.createDssService(entry.getKey()).getService();
        return service.getImageTransformerFactoryOrNull(sessionToken, dataSetIdentifiers, channel);
    }

    @Override
    public ImageDatasetMetadata listImageMetadata(IImageDatasetIdentifier imageDataset)
    {
        final List<ImageDatasetMetadata> metadataList =
                listImageMetadata(Collections.singletonList(imageDataset));
        if (metadataList.isEmpty())
        {
            throw new IllegalArgumentException("Cannot find metadata for image data set '"
                    + imageDataset + "'.");
        }
        return metadataList.get(0);
    }

    @Override
    public List<ImageDatasetMetadata> listImageMetadata(
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        IDssServiceRpcScreeningBatchHandler<IImageDatasetIdentifier, ImageDatasetMetadata> handler =
                new IDssServiceRpcScreeningBatchHandler<IImageDatasetIdentifier, ImageDatasetMetadata>()
                    {
                        @Override
                        public List<ImageDatasetMetadata> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<IImageDatasetIdentifier> references)
                        {
                            List<ImageDatasetMetadata> result =
                                    new ArrayList<ImageDatasetMetadata>();

                            checkDSSMinimalMinorVersion(dssService, "listImageMetadata", List.class);
                            final Iterator<IImageDatasetIdentifier> it = references.iterator();
                            while (it.hasNext())
                            {
                                final IImageDatasetIdentifier ref = it.next();
                                final ImageDatasetMetadata cached = imageMetadataCache.get(ref);
                                if (cached != null)
                                {
                                    result.add(cached);
                                    it.remove();
                                }
                            }
                            if (references.isEmpty())
                            {
                                return result;
                            }
                            final List<ImageDatasetMetadata> metadata =
                                    dssService.getService().listImageMetadata(sessionToken,
                                            references);
                            for (ImageDatasetMetadata md : metadata)
                            {
                                imageMetadataCache.put(md.getImageDataset(), md);
                            }
                            result.addAll(metadata);
                            return result;
                        }
                    };

        return dssMultiplexer.process(imageDatasets, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    public List<PlateWellMaterialMapping> listPlateMaterialMapping(
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        return openbisScreeningServer.listPlateMaterialMapping(sessionToken, plates,
                materialTypeIdentifierOrNull);
    }

    @Override
    public List<String> listAnalysisProcedures(ExperimentIdentifier experimentIdentifier)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, experimentIdentifier.getExperimentCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PROJECT, experimentIdentifier.getProjectCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.SPACE, experimentIdentifier.getSpaceCode()));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createExperimentCriteria(experimentCriteria));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        Set<String> procedures = new HashSet<String>();
        for (DataSet dataSet : dataSets)
        {
            HashMap<String, String> properties = dataSet.getProperties();
            String analysisProcedure = properties.get(ScreeningConstants.ANALYSIS_PROCEDURE);
            if (analysisProcedure != null)
            {
                procedures.add(analysisProcedure);
            }
        }
        ArrayList<String> result = new ArrayList<String>(procedures);
        Collections.sort(result);
        return result;
    }

    // --------- helpers -----------

    private static final class WrappedIOException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        private final IOException ioException;

        WrappedIOException(IOException cause)
        {
            super(cause);
            ioException = cause;
        }

        public final IOException getIoException()
        {
            return ioException;
        }

    }

    private void checkDSSMinimalMinorVersion(final DssServiceRpcScreeningHolder serviceHolder,
            final String methodName, final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IDssServiceRpcScreening.class, methodName, parameterTypes);
        if (hasDSSMethod(serviceHolder, methodName, parameterTypes) == false)
        {
            final String paramString = Arrays.asList(parameterTypes).toString();
            throw new UnsupportedOperationException(String.format(
                    "Method '%s(%s)' requires minor version %d, "
                            + "but server '%s' has only minor version %d.",
                    methodName,
                    paramString.substring(1, paramString.length() - 1), minimalMinorVersion,
                    serviceHolder.getServerUrl(), serviceHolder.getMinorVersion()));
        }
    }

    private void checkASMinimalMinorVersion(final String methodName,
            final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IScreeningApiServer.class, methodName, parameterTypes);
        if (minorVersionApplicationServer < minimalMinorVersion)
        {
            final String paramString = Arrays.asList(parameterTypes).toString();
            throw new UnsupportedOperationException(String.format(
                    "Method '%s(%s)' requires minor version %d, "
                            + "but server has only minor version %d.",
                    methodName,
                    paramString.substring(1, paramString.length() - 1), minimalMinorVersion,
                    minorVersionApplicationServer));
        }
    }

    private boolean hasDSSMethod(final DssServiceRpcScreeningHolder serviceHolder,
            final String methodName, final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IDssServiceRpcScreening.class, methodName, parameterTypes);
        return serviceHolder.getMinorVersion() >= minimalMinorVersion;
    }

    private boolean hasASMethod(final String methodName, final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IScreeningApiServer.class, methodName, parameterTypes);
        return minorVersionApplicationServer >= minimalMinorVersion;
    }

    private static int getMinimalMinorVersion(final Class<?> clazz, final String methodName,
            final Class<?>... parameterTypes)
    {
        assert clazz != null : "Unspecified class.";
        assert methodName != null : "Unspecified method name.";

        final Class<?>[] actualParameterTypes = new Class<?>[parameterTypes.length + 1];
        actualParameterTypes[0] = String.class; // The token field
        System.arraycopy(parameterTypes, 0, actualParameterTypes, 1, parameterTypes.length);
        final Method method;
        try
        {
            method = clazz.getMethod(methodName, actualParameterTypes);
        } catch (Exception ex)
        {
            throw new Error("Method not found.", ex);
        }
        final MinimalMinorVersion minimalMinorVersion =
                method.getAnnotation(MinimalMinorVersion.class);
        if (minimalMinorVersion == null)
        {
            return 0;
        } else
        {
            return minimalMinorVersion.value();
        }
    }

    @Override
    public void loadImages(List<PlateImageReference> imageReferences,
            final LoadImageConfiguration configuration, final IPlateImageHandler plateImageHandler)
            throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadImages", List.class,
                                    LoadImageConfiguration.class);
                            final InputStream stream =
                                    dssService.getService().loadImages(sessionToken, references,
                                            configuration);
                            processImagesStreamUnchecked(plateImageHandler, references, stream);
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    @Override
    public void loadImages(final List<PlateImageReference> imageReferences,
            final IPlateImageHandler plateImageHandler, final ImageRepresentationFormat format)
            throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadImages", List.class,
                                    ImageRepresentationFormat.class);
                            final InputStream stream =
                                    dssService.getService().loadImages(sessionToken, references,
                                            format);
                            processImagesStreamUnchecked(plateImageHandler, references, stream);
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    @Override
    public void loadImages(List<PlateImageReference> imageReferences,
            final IPlateImageHandler plateImageHandler,
            final IImageRepresentationFormatSelectionCriterion... criteria) throws IOException
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, Void>()
                    {
                        @Override
                        public List<Void> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadImages", List.class,
                                    IImageRepresentationFormatSelectionCriterion[].class);
                            final InputStream stream =
                                    dssService.getService().loadImages(sessionToken, references,
                                            criteria);
                            processImagesStreamUnchecked(plateImageHandler, references, stream);
                            return null;
                        }
                    };

        dssMultiplexer.process(imageReferences, handler);
    }

    private void processImagesStreamUnchecked(final IPlateImageHandler plateImageHandler,
            List<PlateImageReference> references, final InputStream stream)
    {
        try
        {
            processImagesStream(plateImageHandler, references, stream);
        } catch (IOException ex)
        {
            throw new WrappedIOException(ex);
        } finally
        {
            try
            {
                stream.close();
            } catch (IOException ex)
            {
                throw new WrappedIOException(ex);
            }
        }
    }

    private void processImagesStream(final IPlateImageHandler plateImageHandler,
            List<PlateImageReference> references, final InputStream stream) throws IOException
    {
        final ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(stream);
        int index = 0;
        long size;
        do
        {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            size = imagesWriter.writeNextBlock(outputStream);
            if (size >= 0)
            {
                plateImageHandler.handlePlateImage(references.get(index),
                        outputStream.toByteArray());
            }
            index++;
        } while (size >= 0);
    }

    @Override
    public ExperimentImageMetadata getExperimentImageMetadata(
            ExperimentIdentifier experimentIdentifier)
    {
        checkASMinimalMinorVersion("getExperimentImageMetadata", ExperimentIdentifier.class);
        return openbisScreeningServer
                .getExperimentImageMetadata(sessionToken, experimentIdentifier);
    }

    @Override
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            List<? extends IDatasetIdentifier> dataSetIdentifiers)
    {
        List<IDatasetIdentifier> simplerList =
                new ArrayList<IDatasetIdentifier>(dataSetIdentifiers.size());
        simplerList.addAll(dataSetIdentifiers);
        Map<String, List<IDatasetIdentifier>> map =
                DssServiceRpcScreeningMultiplexer.getReferencesPerDataStore(simplerList);
        Set<Entry<String, List<IDatasetIdentifier>>> entrySet = map.entrySet();
        if (entrySet.size() != 1)
        {
            throw new IllegalArgumentException("Only one data store expected instead of "
                    + map.keySet());
        }
        Entry<String, List<IDatasetIdentifier>> entry = entrySet.iterator().next();
        IDssServiceRpcScreening service =
                dssServiceCache.createDssService(entry.getKey()).getService();
        return service.listAvailableImageRepresentationFormats(sessionToken, dataSetIdentifiers);
    }

}
