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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.multiplexer.IMultiplexer;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.DssServiceRpcScreeningHolder;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.DssServiceRpcScreeningMultiplexer;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningBatchHandler;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningFactory;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.internal.IDssServiceRpcScreeningMultiplexer;
import ch.systemsx.cisd.openbis.generic.server.AbstractASyncAction;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.DatasetReferencePredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.ExperimentIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.ExperimentSearchCriteriaPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.MaterialExperimentFeatureVectorSummaryValidator;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.MaterialFeaturesOneExpPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.PlateIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.PlateWellReferenceWithDatasetsValidator;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.ScreeningExperimentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.ScreeningPlateListReadOnlyPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.ScreeningPlateValidator;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.WellContentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.WellIdentifierPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.WellSearchCriteriaPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.AnalysisProcedureResult;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.AnalysisSettings;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ExperimentFeatureVectorSummaryLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.FeatureVectorValuesLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.LogicalImageLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.MaterialFeatureVectorSummaryLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.MaterialFeaturesFromAllExperimentsLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.PlateContentLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ScreeningApiImpl;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ScreeningUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.WellContentLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.ImageResolutionTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.LogicalImageInfoTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetImageRepresentationFormats;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentImageMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.NewLibrary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesManyExpCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageResolutionLoader;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

/**
 * The concrete {@link IScreeningServer} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(ResourceNames.SCREENING_PLUGIN_SERVER)
public final class ScreeningServer extends AbstractServer<IScreeningServer> implements
        IScreeningServer, IScreeningApiServer, InitializingBean, IAnalysisSettingSetter
{

    /**
     * The minor version of this service.
     */
    public static final int MINOR_VERSION = 11;

    @Resource(name = ResourceNames.SCREENING_BUSINESS_OBJECT_FACTORY)
    private IScreeningBusinessObjectFactory businessObjectFactory;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    // necessary to make it possible to run asynchronous actions
    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    @Resource(name = ComponentNames.MANAGED_PROPERTY_EVALUATOR_FACTORY)
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    private AnalysisSettings analysisSettings;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.MULTIPLEXER)
    private IMultiplexer multiplexer;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private DAOFactory daoFactory;

    private IDssServiceRpcScreeningMultiplexer dssMultiplexer;

    public ScreeningServer()
    {
    }

    @Private
    ScreeningServer(final IOpenBisSessionManager sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final IScreeningBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin,
            final IDssServiceRpcScreeningMultiplexer dssMultiplexer)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
        this.dssMultiplexer = dssMultiplexer;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        setAnalysisSettings(new AnalysisSettings(configurer.getResolvedProps()));
    }

    @Override
    public void setAnalysisSettings(AnalysisSettings analysisSettings)
    {
        this.analysisSettings = analysisSettings;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    @Override
    public final IScreeningServer createLogger(IInvocationLoggerContext context)
    {
        return new ScreeningServerLogger(getSessionManager(), context);
    }

    //
    // IScreeningServer
    //

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public SampleParentWithDerived getSampleInfo(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class)
            final TechId sampleId) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        final SamplePE sample = sampleBO.getSample();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), sample);
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL(), MetaprojectTranslator
                        .translate(metaprojectPEs),
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, businessObjectFactory.getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public PlateContent getPlateContent(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId plateId)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadImagesAndMetadata(session, daoFactory.getSessionFactory().getCurrentSession(), businessObjectFactory,
                managedPropertyEvaluatorFactory, plateId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public FeatureVectorDataset getFeatureVectorDataset(String sessionToken,
            @AuthorizationGuard(guardClass = DatasetReferencePredicate.class) DatasetReference dataset, CodeAndLabel featureName)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadFeatureVectorDataset(session, daoFactory.getSessionFactory().getCurrentSession(), businessObjectFactory,
                managedPropertyEvaluatorFactory, dataset, featureName);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public PlateImages getPlateContentForDataset(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadImagesAndMetadataForDataset(session, daoFactory.getSessionFactory().getCurrentSession(), businessObjectFactory,
                managedPropertyEvaluatorFactory, datasetId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = WellContentValidator.class)
    public List<WellContent> listPlateWells(String sessionToken,
            @AuthorizationGuard(guardClass = WellSearchCriteriaPredicate.class) WellSearchCriteria materialCriteria)
    {
        Session session = getSession(sessionToken);
        return WellContentLoader.load(session, businessObjectFactory, getDAOFactory(),
                materialCriteria);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<WellReplicaImage> listWellImages(String sessionToken, TechId materialId,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId)
    {
        Session session = getSession(sessionToken);
        return WellContentLoader.loadWithImages(session, businessObjectFactory, getDAOFactory(),
                materialId, experimentId, createDefaultSettings());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Material> listMaterials(String sessionToken,
            @AuthorizationGuard(guardClass = WellSearchCriteriaPredicate.class) WellSearchCriteria materialCriteria)
    {
        Session session = getSession(sessionToken);
        return WellContentLoader.loadMaterials(session, businessObjectFactory, getDAOFactory(),
                materialCriteria);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public FeatureVectorValues getWellFeatureVectorValues(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode, String datastoreCode, WellLocation wellLocation)
    {
        Session session = getSession(sessionToken);
        return FeatureVectorValuesLoader.loadFeatureVectorValues(session, businessObjectFactory,
                datasetCode, datastoreCode, wellLocation);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public LogicalImageInfo getImageDatasetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode, String datastoreCode, WellLocation wellLocationOrNull)
    {
        Session session = getSession(sessionToken);
        return LogicalImageLoader.loadLogicalImageInfo(session, daoFactory.getSessionFactory().getCurrentSession(), businessObjectFactory,
                managedPropertyEvaluatorFactory, datasetCode, datastoreCode, wellLocationOrNull);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public ImageDatasetEnrichedReference getImageDatasetReference(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode, String datastoreCode)
    {
        Session session = getSession(sessionToken);
        return LogicalImageLoader.getImageDatasetReference(session, daoFactory.getSessionFactory().getCurrentSession(), businessObjectFactory,
                managedPropertyEvaluatorFactory, datasetCode, datastoreCode);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ImageResolution> getImageDatasetResolutions(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode, String datastoreCode)
    {
        checkSession(sessionToken);
        IImageResolutionLoader loader =
                businessObjectFactory.tryCreateImageResolutionLoader(datasetCode, datastoreCode);
        return loader.getImageResolutions();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public ImageSampleContent getImageDatasetInfosForSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleId, WellLocation wellLocationOrNull)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.getImageDatasetInfosForSample(session, daoFactory.getSessionFactory().getCurrentSession(), businessObjectFactory,
                managedPropertyEvaluatorFactory, sampleId, wellLocationOrNull);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public AbstractExternalData getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId)
    {
        return commonServer.getDataSetInfo(sessionToken, datasetId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Material getMaterialInfo(String sessionToken, TechId materialId)
    {
        return commonServer.getMaterialInfo(sessionToken, materialId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException
    {
        checkSession(sessionToken);
        IVocabularyDAO vocabularyDAO = getDAOFactory().getVocabularyDAO();
        VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode(code);
        return VocabularyTranslator.translate(vocabulary);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("WRITE_EXPERIMENT_SAMPLE_MATERIAL")
    public void registerLibraries(String sessionToken, List<NewLibrary> newLibraries)
    {
        for (NewLibrary newLibrary : newLibraries)
        {
            new LibraryRegistrationTask(sessionToken, newLibrary.getNewGenesOrNull(), newLibrary.getNewOligosOrNull(),
                    newLibrary.getNewSamplesWithType(), commonServer, genericServer, getDAOFactory()).doAction(new StringWriter());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("WRITE_EXPERIMENT_SAMPLE_MATERIAL")
    public void registerLibrariesAsync(final String sessionToken, final List<NewLibrary> newLibraries, final String userEmail)
    {
        executeASync(userEmail, new AbstractASyncAction()
            {
                @Override
                public String getName()
                {
                    return "Library Batch Registration";
                }

                @Override
                protected void doActionOrThrowException(Writer messageWriter)
                {
                    for (NewLibrary newLibrary : newLibraries)
                    {
                        new LibraryRegistrationTask(sessionToken, newLibrary.getNewGenesOrNull(), newLibrary.getNewOligosOrNull(),
                                newLibrary.getNewSamplesWithType(), commonServer, genericServer, getDAOFactory()).doAction(messageWriter);
                    }
                }
            });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Material> listExperimentMaterials(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId, MaterialType materialType)
    {
        // TODO 2010-09-01, Piotr Buczek: move it to some BO when we have more queries like that
        IScreeningQuery dao = createDAO(getDAOFactory());
        DataIterator<Long> materialIdsIterator =
                dao.getMaterialsForExperimentWells(experimentId.getId(), materialType.getId());
        Collection<Long> materialIds = new ArrayList<Long>();
        for (Long id : materialIdsIterator)
        {
            materialIds.add(id);
        }
        return commonServer.listMaterials(sessionToken,
                ListMaterialCriteria.createFromMaterialIds(materialIds), true);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public ExperimentFeatureVectorSummary getExperimentFeatureVectorSummary(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        Session session = getSession(sessionToken);
        // NOTE: we want the settings to be passed form the client in future
        MaterialSummarySettings settings = createDefaultSettings();
        ExperimentFeatureVectorSummaryLoader experimentFeatureVectorSummaryLoader =
                new ExperimentFeatureVectorSummaryLoader(session, businessObjectFactory,
                        getDAOFactory(), null, settings);
        return experimentFeatureVectorSummaryLoader.loadExperimentFeatureVectors(experimentId,
                analysisProcedureCriteria, analysisSettings);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = MaterialExperimentFeatureVectorSummaryValidator.class)
    public List<MaterialSimpleFeatureVectorSummary> getMaterialFeatureVectorsFromAllExperiments(
            String sessionToken, MaterialFeaturesManyExpCriteria criteria)
    {
        Session session = getSession(sessionToken);
        // NOTE: we want the settings to be passed form the client in future
        MaterialSummarySettings settings = createDefaultSettings();

        TechId projectTechIdOrNull =
                tryFetchProjectId(sessionToken, criteria.getExperimentSearchCriteria());
        return MaterialFeaturesFromAllExperimentsLoader.loadMaterialFeatureVectorsFromAllAssays(
                session, businessObjectFactory, getDAOFactory(), criteria.getMaterialId(),
                criteria.getAnalysisProcedureCriteria(), criteria.isComputeRanks(),
                projectTechIdOrNull, settings);
    }

    private TechId tryFetchProjectId(String sessionToken,
            WellSearchCriteria.ExperimentSearchByProjectCriteria experimentCriteria)
    {
        if (experimentCriteria == null || experimentCriteria.isAllExperiments())
        {
            return null;
        } else
        {
            return fetchProjectId(sessionToken, experimentCriteria.tryGetProjectIdentifier());
        }
    }

    private TechId fetchProjectId(String sessionToken, BasicProjectIdentifier basicProjectIdentifier)
    {
        ProjectIdentifier projectIdentifier = new ProjectIdentifier(basicProjectIdentifier);
        Project project = commonServer.getProjectInfo(sessionToken, projectIdentifier);
        return new TechId(project);
    }

    public static MaterialSummarySettings createDefaultSettings()
    {
        MaterialSummarySettings settings = new MaterialSummarySettings();
        settings.setAggregationType(MaterialReplicaSummaryAggregationType.MEDIAN);
        settings.setFeatureCodes(new ArrayList<String>());
        settings.setReplicaMatrialTypePatterns(new String[] { "GENE", "CONTROL", "COMPOUND" });
        settings.setMaterialDetailsPropertyType(ScreeningConstants.GENE_SYMBOLS);
        settings.setBiologicalReplicatePropertyTypeCodes("CONCENTRATION", "SIRNA");
        return settings;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public MaterialReplicaFeatureSummaryResult getMaterialFeatureVectorSummary(String sessionToken,
            @AuthorizationGuard(guardClass = MaterialFeaturesOneExpPredicate.class) MaterialFeaturesOneExpCriteria criteria)
    {
        Session session = getSession(sessionToken);
        // NOTE: we want the settings to be passed form the client in future
        MaterialSummarySettings settings = createDefaultSettings();
        return MaterialFeatureVectorSummaryLoader.loadMaterialFeatureVectors(session,
                businessObjectFactory, getDAOFactory(), criteria, settings);
    }

    // --------- IScreeningOpenbisServer - method signature should be changed with care

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException
    {
        return createScreeningApiImpl(sessionToken).listFeatureVectorDatasets(plates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ImageDatasetReference> listImageDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException
    {
        return createScreeningApiImpl(sessionToken).listImageDatasets(plates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Map<String, ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LogicalImageInfo> getImageInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        checkSession(sessionToken);

        if (datasetCodes == null)
        {
            throw new IllegalArgumentException("Data set codes were null");
        }

        Map<String, ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LogicalImageInfo> map =
                new HashMap<String, ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.LogicalImageInfo>();

        for (String datasetCode : datasetCodes)
        {
            DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(datasetCode);

            if (dataSet != null)
            {
                LogicalImageInfo internalInfo = getImageDatasetInfo(sessionToken, datasetCode, dataSet.getDataStore().getCode(), null);
                map.put(datasetCode, new LogicalImageInfoTranslator().translate(internalInfo));
            }
        }

        return map;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Map<String, List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution>> getImageResolutions(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        checkSession(sessionToken);

        if (datasetCodes == null)
        {
            throw new IllegalArgumentException("Data set codes were null");
        }

        Map<String, List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution>> map =
                new HashMap<String, List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution>>();

        for (String datasetCode : datasetCodes)
        {
            DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(datasetCode);

            if (dataSet != null)
            {
                List<ImageResolution> internalResolutions = getImageDatasetResolutions(sessionToken, datasetCode, dataSet.getDataStore().getCode());

                if (internalResolutions != null)
                {
                    List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution> apiResolutions =
                            new LinkedList<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageResolution>();

                    for (ImageResolution internalResolution : internalResolutions)
                    {
                        apiResolutions.add(new ImageResolutionTranslator().translate(internalResolution));
                    }

                    map.put(datasetCode, apiResolutions);
                }
            }
        }

        return map;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ImageDatasetReference> listRawImageDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException
    {
        return createScreeningApiImpl(sessionToken).listRawImageDatasets(plates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ImageDatasetReference> listSegmentationImageDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates)
            throws IllegalArgumentException
    {
        return createScreeningApiImpl(sessionToken).listSegmentationImageDatasets(plates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class) ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier experimentIdentifer,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        return createScreeningApiImpl(sessionToken).listPlateWells(experimentIdentifer,
                materialIdentifier, findDatasets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = PlateWellReferenceWithDatasetsValidator.class)
    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        return createScreeningApiImpl(sessionToken)
                .listPlateWells(materialIdentifier, findDatasets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<WellIdentifier> listPlateWells(String sessionToken,
            @AuthorizationGuard(guardClass = PlateIdentifierPredicate.class) PlateIdentifier plateIdentifier)
    {
        return createScreeningApiImpl(sessionToken).listPlateWells(plateIdentifier);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Sample getWellSample(String sessionToken,
            @AuthorizationGuard(guardClass = WellIdentifierPredicate.class) WellIdentifier wellIdentifier)
    {
        return createScreeningApiImpl(sessionToken).getWellSample(wellIdentifier, true);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Sample getPlateSample(String sessionToken,
            @AuthorizationGuard(guardClass = PlateIdentifierPredicate.class) PlateIdentifier plateIdentifier)
    {
        return createScreeningApiImpl(sessionToken).getPlateSample(plateIdentifier);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ScreeningPlateValidator.class)
    public List<Plate> listPlates(String sessionToken) throws IllegalArgumentException
    {
        return createScreeningApiImpl(sessionToken).listPlates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Plate> listPlates(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class) ExperimentIdentifier experiment) throws IllegalArgumentException
    {
        return createScreeningApiImpl(sessionToken).listPlates(experiment);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ScreeningExperimentValidator.class)
    public List<ExperimentIdentifier> listExperiments(String sessionToken)
    {
        return createScreeningApiImpl(sessionToken).listExperiments();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<ExperimentIdentifier> listExperiments(String sessionToken, String userId)
    {
        return createScreeningApiImpl(sessionToken).listExperiments(userId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        return createScreeningApiImpl(sessionToken).getDatasetIdentifiers(datasetCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public AnalysisProcedures listNumericalDatasetsAnalysisProcedures(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentSearchCriteriaPredicate.class) ExperimentSearchCriteria experimentSearchCriteria)
    {
        checkSession(sessionToken);
        IScreeningQuery dao = createDAO(getDAOFactory());

        SingleExperimentSearchCriteria singleExpCriteria =
                (experimentSearchCriteria != null) ? experimentSearchCriteria.tryGetExperiment()
                        : null;
        List<AnalysisProcedureResult> analysisProcedures;
        if (singleExpCriteria == null)
        {
            analysisProcedures = dao.listAllAnalysisProcedures();
        } else
        {
            analysisProcedures =
                    dao.listAnalysisProceduresForExperiment(singleExpCriteria.getExperimentId()
                            .getId());
        }
        return ScreeningUtils.filterNumericalDatasetsAnalysisProcedures(analysisProcedures);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<PlateWellMaterialMapping> listPlateMaterialMapping(String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        return createScreeningApiImpl(sessionToken).listPlateMaterialMapping(plates,
                materialTypeIdentifierOrNull);
    }

    private static IScreeningQuery createDAO(IDAOFactory daoFactory)
    {
        return QueryTool.getManagedQuery(IScreeningQuery.class);
    }

    private ScreeningApiImpl createScreeningApiImpl(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        return new ScreeningApiImpl(session, businessObjectFactory, getDAOFactory(),
                managedPropertyEvaluatorFactory);
    }

    @Override
    public void logoutScreening(String sessionToken)
    {
        logout(sessionToken);
    }

    @Override
    public String tryLoginScreening(String userId, String userPassword)
    {
        SessionContextDTO sessionContext = tryAuthenticate(userId, userPassword);
        if (sessionContext != null)
        {
            return sessionContext.getSessionToken();
        } else
        {
            return null;
        }
    }

    @Override
    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<PlateMetadata> getPlateMetadataList(String sessionToken,
            @AuthorizationGuard(guardClass = ScreeningPlateListReadOnlyPredicate.class) List<? extends PlateIdentifier> plateIdentifiers)
            throws IllegalArgumentException
    {
        return createScreeningApiImpl(sessionToken).getPlateMetadata(plateIdentifiers);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public ExperimentImageMetadata getExperimentImageMetadata(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentIdentifierPredicate.class) ExperimentIdentifier experimentIdentifer)
    {
        checkSession(sessionToken);
        return createScreeningApiImpl(sessionToken).getExperimentImageMetadata(experimentIdentifer);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> listAvailableFeatureCodes(final String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String> handler =
                new IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            return dssService.getService().listAvailableFeatureCodes(sessionToken,
                                    references);
                        }
                    };

        return getDssMultiplexer().process(featureDatasets, handler).getMergedBatchResultsWithoutDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<FeatureInformation> listAvailableFeatures(final String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, FeatureInformation> handler =
                new IDssServiceRpcScreeningBatchHandler<IFeatureVectorDatasetIdentifier, FeatureInformation>()
                    {
                        @Override
                        public List<FeatureInformation> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            return dssService.getService().listAvailableFeatures(sessionToken,
                                    references);
                        }
                    };

        return getDssMultiplexer().process(featureDatasets, handler).getMergedBatchResultsWithoutDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset> loadFeatures(
            final String sessionToken, List<FeatureVectorDatasetReference> featureDatasets,
            final List<String> featureCodes)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetReference, ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset> handler =
                new IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetReference, ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset>()
                    {
                        @Override
                        public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<FeatureVectorDatasetReference> references)
                        {
                            return dssService.getService().loadFeatures(sessionToken, references,
                                    featureCodes);
                        }
                    };

        return getDssMultiplexer().process(featureDatasets, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            final String sessionToken,
            List<FeatureVectorDatasetWellReference> datasetWellReferences,
            final List<String> featureCodes)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetWellReference, FeatureVectorWithDescription> handler =
                new IDssServiceRpcScreeningBatchHandler<FeatureVectorDatasetWellReference, FeatureVectorWithDescription>()
                    {
                        @Override
                        public List<FeatureVectorWithDescription> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<FeatureVectorDatasetWellReference> references)
                        {
                            return dssService.getService().loadFeaturesForDatasetWellReferences(
                                    sessionToken, references, featureCodes);
                        }
                    };

        return getDssMultiplexer().process(datasetWellReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadImagesBase64(final String sessionToken,
            List<PlateImageReference> imageReferences, final boolean convertToPng)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadImagesBase64(sessionToken,
                                    references, convertToPng);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadThumbnailImagesBase64(final String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadThumbnailImagesBase64(sessionToken,
                                    references);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadImagesBase64(final String sessionToken,
            List<PlateImageReference> imageReferences, final ImageSize size)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadImagesBase64(sessionToken,
                                    references, size);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadImagesBase64(final String sessionToken,
            List<PlateImageReference> imageReferences)
    {
        checkSession(sessionToken);

        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadImagesBase64(sessionToken,
                                    references);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadImagesBase64(final String sessionToken,
            List<PlateImageReference> imageReferences, final LoadImageConfiguration configuration)
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadImagesBase64(sessionToken,
                                    references, configuration);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadImagesBase64(final String sessionToken,
            List<PlateImageReference> imageReferences, final ImageRepresentationFormat format)
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadImagesBase64(sessionToken,
                                    references, format);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadImagesBase64(final String sessionToken,
            List<PlateImageReference> imageReferences,
            final IImageRepresentationFormatSelectionCriterion... criteria)
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadImagesBase64(sessionToken,
                                    references, criteria);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ImageDatasetMetadata> listImageMetadata(final String sessionToken,
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
                            return dssService.getService().listImageMetadata(sessionToken,
                                    references);
                        }
                    };

        return getDssMultiplexer().process(imageDatasets, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<DatasetImageRepresentationFormats> listAvailableImageRepresentationFormats(
            final String sessionToken, List<? extends IDatasetIdentifier> imageDatasets)
    {
        IDssServiceRpcScreeningBatchHandler<IDatasetIdentifier, DatasetImageRepresentationFormats> handler =
                new IDssServiceRpcScreeningBatchHandler<IDatasetIdentifier, DatasetImageRepresentationFormats>()
                    {
                        @Override
                        public List<DatasetImageRepresentationFormats> handle(
                                DssServiceRpcScreeningHolder dssService,
                                List<IDatasetIdentifier> references)
                        {
                            return dssService.getService().listAvailableImageRepresentationFormats(
                                    sessionToken, references);
                        }
                    };

        return getDssMultiplexer().process(imageDatasets, handler).getMergedBatchResultsWithDuplicates();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> loadPhysicalThumbnailsBase64(final String sessionToken,
            List<PlateImageReference> imageReferences, final ImageRepresentationFormat format)
    {
        IDssServiceRpcScreeningBatchHandler<PlateImageReference, String> handler =
                new IDssServiceRpcScreeningBatchHandler<PlateImageReference, String>()
                    {
                        @Override
                        public List<String> handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            return dssService.getService().loadPhysicalThumbnailsBase64(
                                    sessionToken, references, format);
                        }
                    };

        return getDssMultiplexer().process(imageReferences, handler).getMergedBatchResultsWithDuplicates();
    }

    private IDssServiceRpcScreeningMultiplexer getDssMultiplexer()
    {
        if (dssMultiplexer == null)
        {
            dssMultiplexer =
                    new DssServiceRpcScreeningMultiplexer(multiplexer,
                            new IDssServiceRpcScreeningFactory()
                                {
                                    @Override
                                    public DssServiceRpcScreeningHolder createDssService(
                                            String serverUrl)
                                    {
                                        return new DssServiceRpcScreeningHolder(serverUrl,
                                                getMajorVersion(), 5 * DateUtils.MILLIS_PER_MINUTE);
                                    }
                                });
        }
        return dssMultiplexer;
    }

}
