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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.parser.AbstractParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IParserObjectFactoryFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.ParserException;
import ch.systemsx.cisd.common.reflection.BeanUtils;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.string.ReflectingStringUnescaper;
import ch.systemsx.cisd.common.string.UnicodeUtils;
import ch.systemsx.cisd.openbis.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ArchivingResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUploadParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedCriteriaOrSelectedEntityHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityPropertyUpdatesResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListEntityHistoryCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMetaprojectsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListPersonsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria2;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListScriptsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RelatedDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleChildrenInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchOption;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Type;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.ITableDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.queue.ConsumerQueue;
import ch.systemsx.cisd.openbis.generic.client.web.server.queue.ConsumerTask;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractExternalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractMaterialProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AttachmentVersionsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AuthorizationGroupProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CacheManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.CustomGridColumnProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DataSetTypeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DeletionsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.EntityHistoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.EntityTypePropertyTypeBrowserProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.EntityTypePropertyTypeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.EntityTypeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ExperimentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.FileFormatTypesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.GridCustomFilterProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.MatchingEntitiesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.MetaprojectProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.PersonsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ProjectsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.PropertyTypeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.RoleAssignmentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.SampleProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.SampleTypeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ScriptProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.SpacesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.TableDataProviderFactory;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.TableForUpdateExporter;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.VocabulariesProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.VocabularyTermsProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ResultSetTranslator.Escape;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SearchableEntityTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.SearchableSearchDomainTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.TSVRenderer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.ICustomImportService;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomainSearchOption;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AsyncBatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentVersions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DynamicPropertyEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityValidationEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IMetaprojectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsIds;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ProjectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ValidationException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.DataSetTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.MaterialTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.SampleTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.util.WebClientConfigUtils;

/**
 * The {@link ICommonClientService} implementation.
 *
 * @author Franz-Josef Elmer
 */
public final class CommonClientService extends AbstractClientService implements
        ICommonClientService, ICustomImportService
{
    @Resource(name = "registration-queue")
    private ConsumerQueue asyncRegistrationQueue;

    private final ICommonServer commonServer;

    @Autowired
    private IApplicationServerApi applicationServerApi;

    @Autowired
    private IAuthorizationConfig authorizationConfig;

    public CommonClientService(final ICommonServer commonServer,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.commonServer = commonServer;
    }

    @Override
    protected final IServer getServer()
    {
        return commonServer;
    }

    // ----------- export and listing with cache generic functionality

    /**
     * Returns and removes cached export criteria.
     */
    private final <T> TableExportCriteria<T> getAndRemoveExportCriteria(final String exportDataKey)
    {
        final CacheManager<String, TableExportCriteria<T>> exportManager = getExportManager();
        final TableExportCriteria<T> exportCriteria = exportManager.tryGetData(exportDataKey);
        assert exportCriteria != null : "No export criteria found at key " + exportDataKey;
        exportManager.removeData(exportDataKey);
        return exportCriteria;
    }

    protected final <T> GridRowModels<T> fetchCachedEntities(
            final TableExportCriteria<T> exportCriteria)
    {
        return fetchCachedEntities(exportCriteria, Escape.NO);
    }

    protected final <T> GridRowModels<T> fetchCachedEntities(
            final TableExportCriteria<T> exportCriteria, Escape escape)
    {
        IResultSetConfig<String, T> resultSetConfig = createExportListCriteria(exportCriteria);
        IOriginalDataProvider<T> dummyDataProvider = createDummyDataProvider();
        final IResultSet<String, T> result = getResultSet(resultSetConfig, dummyDataProvider);
        final ResultSet<T> entities = ResultSetTranslator.translate(result, escape);
        return entities.getList();
    }

    private static <T> IResultSetConfig<String, T> createExportListCriteria(
            final TableExportCriteria<T> exportCriteria)
    {
        final DefaultResultSetConfig<String, T> criteria = DefaultResultSetConfig.createFetchAll();
        criteria.setSortInfo(exportCriteria.getSortInfo());
        criteria.setFilters(exportCriteria.getFilters());
        criteria.setCacheConfig(ResultSetFetchConfig.createFetchFromCache(exportCriteria
                .getResultSetKey()));
        criteria.setAvailableColumns(exportCriteria.getAvailableColumns());
        criteria.setPresentedColumns(exportCriteria.getColumnDefs());
        criteria.setGridDisplayId(exportCriteria.getGridDisplayId());
        return criteria;
    }

    /**
     * Assumes that preparation of the export ( {@link #prepareExportSamples(TableExportCriteria)} has been invoked before and returned with an
     * exportDataKey passed here as a parameter.
     */
    @Override
    public final String getExportTable(final String exportDataKey, final String lineSeparator)
    {
        // NOTE: no generics in GWT
        return getGenericExportTable(exportDataKey, lineSeparator);
    }

    private final <T> String getGenericExportTable(final String exportDataKey,
            final String lineSeparator)
    {
        // Not directly needed but this refreshes the session.
        String session = getSessionToken();
        final TableExportCriteria<T> exportCriteria = getAndRemoveExportCriteria(exportDataKey);

        final GridRowModels<T> entities = fetchCachedEntities(exportCriteria);
        EntityKind entityKindForUpdate = exportCriteria.getEntityKindForUpdateOrNull();
        if (entityKindForUpdate != null)
        {
            return TableForUpdateExporter.getExportTableForUpdate(entities, entityKindForUpdate,
                    lineSeparator, commonServer, session);
        }
        ITableDataProvider dataProvider =
                TableDataProviderFactory.createDataProvider(entities,
                        exportCriteria.getColumnDefs());
        return TSVRenderer.createTable(dataProvider, lineSeparator);
    }

    @Override
    public final void removeResultSet(final String resultSetKey)
    {
        try
        {
            // Not directly needed but this refreshes the session.
            getSessionToken();
            getResultSetManager().removeResultSet(resultSetKey);
        } catch (ch.systemsx.cisd.common.exceptions.InvalidSessionException e)
        {
            return; // there is no session, so nothing has to be removed from it
        } catch (InvalidSessionException e)
        {
            return; // there is no session, so nothing has to be removed from it
        }
    }

    // --------------- end export & listing

    //
    // IGenericClientService
    //

    @Override
    public final void registerSpace(final String groupCode, final String descriptionOrNull)
    {
        final String sessionToken = getSessionToken();
        commonServer.registerSpace(sessionToken, groupCode, descriptionOrNull);
    }

    @Override
    public final void updateSpace(final ISpaceUpdates updates)
    {
        assert updates != null : "Unspecified updates.";

        final String sessionToken = getSessionToken();
        commonServer.updateSpace(sessionToken, updates);
    }

    @Override
    public final ScriptUpdateResult updateScript(final IScriptUpdates updates)
    {
        assert updates != null : "Unspecified updates.";

        final String sessionToken = getSessionToken();
        return commonServer.updateScript(sessionToken, updates);
    }

    @Override
    public final void registerPerson(final String code)
    {
        final String sessionToken = getSessionToken();
        commonServer.registerPerson(sessionToken, code);
    }

    @Override
    public final void registerProjectRole(final RoleWithHierarchy role, final String projectIdentifier,
            final Grantee grantee)
    {
        final String sessionToken = getSessionToken();
        final ProjectIdentifier projectIdentifierObject = ProjectIdentifierFactory.parse(projectIdentifier);
        commonServer.registerProjectRole(sessionToken, role.getRoleCode(), projectIdentifierObject, grantee);
    }

    @Override
    public final void registerSpaceRole(final RoleWithHierarchy role, final String group,
            final Grantee grantee)
    {
        final String sessionToken = getSessionToken();
        final SpaceIdentifier groupIdentifier =
                new SpaceIdentifier(group);
        commonServer.registerSpaceRole(sessionToken, role.getRoleCode(), groupIdentifier, grantee);
    }

    @Override
    public final void registerInstanceRole(final RoleWithHierarchy role, final Grantee grantee)
    {
        final String sessionToken = getSessionToken();
        commonServer.registerInstanceRole(sessionToken, role.getRoleCode(), grantee);
    }

    @Override
    public final void deleteProjectRole(final RoleWithHierarchy role, final String projectIdentifier,
            final Grantee grantee)
    {
        final String sessionToken = getSessionToken();
        final ProjectIdentifier projectIdentifierObject = ProjectIdentifierFactory.parse(projectIdentifier);
        commonServer.deleteProjectRole(sessionToken, role.getRoleCode(), projectIdentifierObject, grantee);
    }

    @Override
    public final void deleteSpaceRole(final RoleWithHierarchy role, final String group,
            final Grantee grantee)
    {
        final String sessionToken = getSessionToken();
        final SpaceIdentifier groupIdentifier =
                new SpaceIdentifier(group);
        commonServer.deleteSpaceRole(sessionToken, role.getRoleCode(), groupIdentifier, grantee);
    }

    @Override
    public final void deleteInstanceRole(final RoleWithHierarchy role, final Grantee grantee)
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteInstanceRole(sessionToken, role.getRoleCode(), grantee);
    }

    @Override
    public final List<SampleType> listSampleTypes()
    {
        final String sessionToken = getSessionToken();
        final List<SampleType> sampleTypes = commonServer.listSampleTypes(sessionToken);
        return sampleTypes;
    }

    // --------- methods preparing exported content. Note: GWT does not support
    // generic methods :(

    @Override
    public Map<String, List<IManagedInputWidgetDescription>> listManagedInputWidgetDescriptions(
            EntityKind entityKind, String entityTypeCode)
    {
        final String sessionToken = getSessionToken();
        return commonServer.listManagedInputWidgetDescriptions(sessionToken, entityKind,
                entityTypeCode);
    }

    @Override
    public String prepareExportSamples(TableExportCriteria<TableModelRowWithObject<Sample>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public final String prepareExportExperiments(
            final TableExportCriteria<TableModelRowWithObject<Experiment>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public final String prepareExportMatchingEntities(
            final TableExportCriteria<TableModelRowWithObject<MatchingEntity>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportPropertyTypes(
            TableExportCriteria<TableModelRowWithObject<PropertyType>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportEntityHistory(
            TableExportCriteria<TableModelRowWithObject<EntityHistory>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportPropertyTypeAssignments(
            TableExportCriteria<TableModelRowWithObject<EntityTypePropertyType<?>>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportProjects(
            TableExportCriteria<TableModelRowWithObject<Project>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportDeletions(
            TableExportCriteria<TableModelRowWithObject<Deletion>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportVocabularies(
            final TableExportCriteria<TableModelRowWithObject<Vocabulary>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportVocabularyTerms(
            TableExportCriteria<TableModelRowWithObject<VocabularyTermWithStats>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportMaterialTypes(
            final TableExportCriteria<TableModelRowWithObject<MaterialType>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportExperimentTypes(
            final TableExportCriteria<TableModelRowWithObject<ExperimentType>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportSampleTypes(
            final TableExportCriteria<TableModelRowWithObject<SampleType>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportDataSetTypes(
            final TableExportCriteria<TableModelRowWithObject<DataSetType>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportFileTypes(TableExportCriteria<FileFormatType> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportAttachmentVersions(
            TableExportCriteria<TableModelRowWithObject<AttachmentVersions>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportScripts(TableExportCriteria<TableModelRowWithObject<Script>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportMetaprojects(
            TableExportCriteria<TableModelRowWithObject<Metaproject>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportSpaces(TableExportCriteria<TableModelRowWithObject<Space>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportPersons(TableExportCriteria<TableModelRowWithObject<Person>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public String prepareExportRoleAssignments(
            TableExportCriteria<TableModelRowWithObject<RoleAssignment>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    // ---------------- methods which list entities using cache

    @Override
    public final ResultSetWithEntityTypes<Sample> listSamples(
            final ListSampleDisplayCriteria listCriteria)
    {
        final String sessionToken = getSessionToken();
        return listEntitiesWithTypes(listCriteria, new ListSamplesOriginalDataProvider(
                commonServer, sessionToken, listCriteria));
    }

    @Override
    public TypedTableResultSet<Sample> listSamples2(ListSampleDisplayCriteria2 criteria)
    {
        SampleProvider provider = new SampleProvider(commonServer, getSessionToken(), criteria);
        return listEntities(provider, criteria);
    }

    @Override
    public List<Sample> listMetaprojectSamples(Long metaprojectId)
    {
        return commonServer.listMetaprojectSamples(getSessionToken(), new MetaprojectTechIdId(
                metaprojectId));
    }

    @Override
    public TypedTableResultSet<AbstractExternalData> searchForDataSets(
            final DetailedSearchCriteria criteria,
            final IResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig)
    {
        final String sessionToken = getSessionToken();
        return listEntities(new AbstractExternalDataProvider(commonServer, sessionToken)
            {
                @Override
                protected List<AbstractExternalData> getDataSets()
                {
                    return commonServer.searchForDataSets(sessionToken, criteria);
                }
            }, resultSetConfig);
    }

    @Override
    public TypedTableResultSet<AbstractExternalData> searchForDataSets(
            RelatedDataSetCriteria<? extends IEntityInformationHolder> criteria,
            final IResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> resultSetConfig)
    {
        final String sessionToken = getSessionToken();
        final DataSetRelatedEntities entities = extractRelatedEntities(criteria);
        return listEntities(new AbstractExternalDataProvider(commonServer, sessionToken)
            {
                @Override
                protected List<AbstractExternalData> getDataSets()
                {
                    return commonServer.listRelatedDataSets(sessionToken, entities, false);
                }
            }, resultSetConfig);
    }

    private <E extends IEntityInformationHolder> DataSetRelatedEntities extractRelatedEntities(
            RelatedDataSetCriteria<E> criteria)
    {
        List<TableModelRowWithObject<E>> rows = criteria.tryGetSelectedEntities();
        if (rows == null)
        {
            TableExportCriteria<TableModelRowWithObject<E>> displayedEntitiesCriteria =
                    criteria.tryGetDisplayedEntities();
            assert displayedEntitiesCriteria != null : "displayedEntitiesCriteria is null";
            rows = fetchCachedEntities(displayedEntitiesCriteria).extractOriginalObjects();
        }
        List<E> entities = new ArrayList<E>();
        for (TableModelRowWithObject<E> row : rows)
        {
            entities.add(row.getObjectOrNull());
        }
        return new DataSetRelatedEntities(entities);
    }

    @Override
    public final TypedTableResultSet<Experiment> listExperiments(
            final ListExperimentsCriteria listCriteria)
    {
        final String sessionToken = getSessionToken();
        return listEntities(new ExperimentProvider(commonServer, sessionToken, listCriteria),
                listCriteria);
    }

    @Override
    public final List<Experiment> listMetaprojectExperiments(final Long metaprojectId)
    {
        return commonServer.listMetaprojectExperiments(getSessionToken(), new MetaprojectTechIdId(
                metaprojectId));
    }

    @Override
    public TypedTableResultSet<PropertyType> listPropertyTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<PropertyType>> criteria)
    {
        return listEntities(new PropertyTypeProvider(commonServer, applicationServerApi, getSessionToken()), criteria);
    }

    @Override
    public TypedTableResultSet<EntityHistory> listEntityHistory(ListEntityHistoryCriteria criteria)
    {
        String sessionToken = getSessionToken();
        return listEntities(new EntityHistoryProvider(commonServer, sessionToken, criteria),
                criteria);
    }

    @Override
    public final TypedTableResultSet<MatchingEntity> listMatchingEntities(
            final SearchableEntity searchableEntityOrNull, final String queryText,
            final boolean useWildcardSearchMode,
            final IResultSetConfig<String, TableModelRowWithObject<MatchingEntity>> resultSetConfig)
    {

        ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity[] matchingEntities =
                SearchableEntityTranslator.translate(searchableEntityOrNull);

        // to make a search for "All" include both all entities AND all search domains, remove the null check here.
        SearchDomain[] matchingSearchDomains = {};
        if (searchableEntityOrNull != null)
        {
            String sessionToken = getSessionToken();
            List<SearchDomain> availableSearchDomains = commonServer.listAvailableSearchDomains(sessionToken);
            matchingSearchDomains =
                    SearchableSearchDomainTranslator.translate(searchableEntityOrNull, availableSearchDomains);
        }

        MatchingEntitiesProvider provider =
                new MatchingEntitiesProvider(commonServer, getSessionToken(), matchingEntities, matchingSearchDomains,
                        queryText, useWildcardSearchMode, webClientConfigurationProvider);
        return listEntities(provider, resultSetConfig);
    }

    @Override
    public TypedTableResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignmentsFromBrowser(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityTypePropertyType<?>>> criteria,
            EntityType entity, List<NewPTNewAssigment> propertyTypesAsgs)
    {
        return listEntities(new EntityTypePropertyTypeBrowserProvider(applicationServerApi, getSessionToken(), entity, propertyTypesAsgs),
                criteria);
    }

    @Override
    public TypedTableResultSet<EntityTypePropertyType<?>> listPropertyTypeAssignments(
            DefaultResultSetConfig<String, TableModelRowWithObject<EntityTypePropertyType<?>>> criteria,
            EntityType entity)
    {
        return listEntities(new EntityTypePropertyTypeProvider(commonServer, applicationServerApi, getSessionToken(), entity),
                criteria);
    }

    @Override
    public List<EntityTypePropertyType<?>> listPropertyTypeAssignments(EntityType entityType)
    {
        return commonServer.listEntityTypePropertyTypes(getSessionToken(), entityType);
    }

    @Override
    public TypedTableResultSet<Space> listSpaces(
            DefaultResultSetConfig<String, TableModelRowWithObject<Space>> criteria)
    {
        SpacesProvider spacesProvider = new SpacesProvider(commonServer, getSessionToken());
        return listEntities(spacesProvider, criteria);
    }

    @Override
    public TypedTableResultSet<Script> listScripts(final ListScriptsCriteria criteria)
    {
        ScriptProvider scriptProvider =
                new ScriptProvider(commonServer, getSessionToken(), criteria.tryGetScriptType(),
                        criteria.tryGetEntityKind(), webClientConfigurationProvider);
        return listEntities(scriptProvider, criteria);
    }

    @Override
    public List<Metaproject> listMetaprojects()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.listMetaprojects(getSessionToken());
    }

    @Override
    public TypedTableResultSet<Metaproject> listMetaprojects(ListMetaprojectsCriteria criteria)
            throws UserFailureException
    {
        MetaprojectProvider metaprojectProvider =
                new MetaprojectProvider(commonServer, getSessionToken(), criteria);
        return listEntities(metaprojectProvider, criteria);
    }

    @Override
    public List<MetaprojectAssignmentsCount> listMetaprojectAssignmentsCounts()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.listMetaprojectAssignmentsCounts(getSessionToken());
    }

    @Override
    public MetaprojectAssignmentsCount getMetaprojectAssignmentsCount(Long metaprojectId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.getMetaprojectAssignmentsCount(getSessionToken(),
                new MetaprojectTechIdId(metaprojectId));
    }

    @Override
    public Metaproject getMetaproject(Long metaprojectId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.getMetaproject(getSessionToken(),
                new MetaprojectTechIdId(metaprojectId));
    }

    @Override
    public Metaproject getMetaproject(String metaprojectIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.getMetaproject(getSessionToken(), new MetaprojectIdentifierId(
                metaprojectIdentifier));
    }

    @Override
    public Metaproject updateMetaproject(Long metaprojectId, IMetaprojectUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.updateMetaproject(getSessionToken(), new MetaprojectTechIdId(
                metaprojectId), updates);
    }

    @Override
    public List<AuthorizationGroup> listAuthorizationGroups()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<AuthorizationGroup> authGroups =
                commonServer.listAuthorizationGroups(sessionToken);
        return authGroups;
    }

    @Override
    public TypedTableResultSet<Person> listPersons(final ListPersonsCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        String sessionToken = getSessionToken();
        TechId authorizationGroupId = criteria.getAuthorizationGroupId();
        return listEntities(new PersonsProvider(commonServer, sessionToken, authorizationGroupId),
                criteria);
    }

    @Override
    public final List<Person> listPersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<Person> persons = commonServer.listPersons(sessionToken);
        return persons;
    }

    @Override
    public final List<Person> listActivePersons()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<Person> persons = commonServer.listActivePersons(sessionToken);
        return persons;
    }

    @Override
    public TypedTableResultSet<RoleAssignment> listRoleAssignments(
            DefaultResultSetConfig<String, TableModelRowWithObject<RoleAssignment>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new RoleAssignmentProvider(commonServer, authorizationConfig, getSessionToken()), criteria);
    }

    public final List<RoleAssignment> listRoleAssignments()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<RoleAssignment> roles = commonServer.listRoleAssignments(sessionToken);
        return roles;
    }

    @Override
    public TypedTableResultSet<Project> listProjects(
            DefaultResultSetConfig<String, TableModelRowWithObject<Project>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        ProjectsProvider projectsProvider = new ProjectsProvider(commonServer, getSessionToken());
        return listEntities(projectsProvider, criteria);
    }

    @Override
    public List<Project> listProjectsForTree()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.listProjects(getSessionToken());
    }

    @Override
    public TypedTableResultSet<Vocabulary> listVocabularies(boolean withTerms,
            boolean excludeInternal,
            DefaultResultSetConfig<String, TableModelRowWithObject<Vocabulary>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        VocabulariesProvider provider =
                new VocabulariesProvider(commonServer, getSessionToken(), withTerms,
                        excludeInternal);
        return listEntities(provider, criteria);
    }

    @Override
    public TypedTableResultSet<VocabularyTermWithStats> listVocabularyTerms(
            final Vocabulary vocabulary,
            DefaultResultSetConfig<String, TableModelRowWithObject<VocabularyTermWithStats>> criteria)
    {
        VocabularyTermsProvider vocabularyTermsProvider =
                new VocabularyTermsProvider(commonServer, getSessionToken(), vocabulary);
        return listEntities(vocabularyTermsProvider, criteria);
    }

    @Override
    public TypedTableResultSet<MaterialType> listMaterialTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new EntityTypeProvider<MaterialType>(commonServer, getSessionToken())
            {
                @Override
                protected List<MaterialType> listTypes()
                {
                    return commonServer.listMaterialTypes(sessionToken);
                }
            }, criteria);
    }

    @Override
    public TypedTableResultSet<SampleType> listSampleTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<SampleType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new SampleTypeProvider(commonServer, applicationServerApi, getSessionToken()), criteria);
    }

    @Override
    public TypedTableResultSet<ExperimentType> listExperimentTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<ExperimentType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new EntityTypeProvider<ExperimentType>(commonServer, getSessionToken())
            {
                @Override
                protected List<ExperimentType> listTypes()
                {
                    return commonServer.listExperimentTypes(sessionToken);
                }
            }, criteria);
    }

    @Override
    public TypedTableResultSet<DataSetType> listDataSetTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<DataSetType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new DataSetTypeProvider(commonServer, getSessionToken()), criteria);
    }

    @Override
    public TypedTableResultSet<FileFormatType> listFileTypes(
            DefaultResultSetConfig<String, TableModelRowWithObject<FileFormatType>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        FileFormatTypesProvider provider =
                new FileFormatTypesProvider(commonServer, getSessionToken());
        return listEntities(provider, criteria);
    }

    @Override
    public TypedTableResultSet<AbstractExternalData> listSampleDataSets(final TechId sampleId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria,
            final boolean showOnlyDirectlyConnected)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new AbstractExternalDataProvider(commonServer, getSessionToken())
            {
                @Override
                protected List<AbstractExternalData> getDataSets()
                {
                    return commonServer.listSampleExternalData(sessionToken, sampleId,
                            showOnlyDirectlyConnected);
                }
            }, criteria);
    }

    @Override
    public TypedTableResultSet<AbstractExternalData> listExperimentDataSets(
            final TechId experimentId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria,
            final boolean onlyDirectlyConnected)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new AbstractExternalDataProvider(commonServer, getSessionToken())
            {
                @Override
                protected List<AbstractExternalData> getDataSets()
                {
                    return commonServer.listExperimentExternalData(sessionToken, experimentId,
                            onlyDirectlyConnected);
                }
            }, criteria);
    }

    @Override
    public TypedTableResultSet<AbstractExternalData> listMetaprojectDataSets(
            final TechId metaprojectId,
            DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new AbstractExternalDataProvider(commonServer, getSessionToken())
            {
                @Override
                protected List<AbstractExternalData> getDataSets()
                {
                    return commonServer.listMetaprojectExternalData(sessionToken,
                            new MetaprojectTechIdId(metaprojectId));
                }
            }, criteria);
    }

    @Override
    public List<AbstractExternalData> listMetaprojectDataSets(final Long metaprojectId)
    {
        return commonServer.listMetaprojectExternalData(getSessionToken(), new MetaprojectTechIdId(
                metaprojectId));
    }

    @Override
    public TypedTableResultSet<AbstractExternalData> listDataSetRelationships(
            final TechId datasetId,
            final DataSetRelationshipRole role,
            final DefaultResultSetConfig<String, TableModelRowWithObject<AbstractExternalData>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new AbstractExternalDataProvider(commonServer, getSessionToken())
            {
                @Override
                protected List<AbstractExternalData> getDataSets()
                {
                    return commonServer.listDataSetRelationships(sessionToken, datasetId, role);
                }
            }, criteria);
    }

    // ---------------- end list using cache ----------

    @Override
    public final List<SearchableEntity> listSearchableEntities()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {

        try
        {
            // This also refreshes the session.
            String sessionToken = getSessionToken();
            final List<SearchableEntity> searchableEntities = new ArrayList<>();
            BeanUtils.createBeanList(SearchableEntity.class, Arrays
                    .asList(ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity
                            .values()));
            for (ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity entity : ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity
                    .values())
            {
                SearchableEntity searchableEntity = new SearchableEntity();
                searchableEntity.setName(entity.name());
                searchableEntity.setDescription(
                        WebClientConfigUtils.getTranslatedDescription(webClientConfigurationProvider, entity));
                searchableEntity.setType(Type.ENTITY);
                searchableEntities.add(searchableEntity);
            }
            List<SearchDomain> searchDomains = commonServer.listAvailableSearchDomains(sessionToken);
            for (SearchDomain searchDomain : searchDomains)
            {
                SearchableEntity searchableEntity = new SearchableEntity();
                searchableEntity.setName(searchDomain.getName());
                searchableEntity.setType(Type.SEARCH_DOMAIN);
                searchableEntity.setDescription(searchDomain.getLabel());
                searchableEntity.setPossibleSearchOptionsKey(searchDomain.getPossibleSearchOptionsKey());
                searchableEntity.setPossibleSearchOptions(getSearchOptions(searchDomain));
                searchableEntities.add(searchableEntity);
            }
            return searchableEntities;
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    private List<SearchOption> getSearchOptions(SearchDomain searchDomain)
    {
        List<SearchOption> searchOptions = new ArrayList<>();
        List<SearchDomainSearchOption> options = searchDomain.getPossibleSearchOptions();
        for (SearchDomainSearchOption option : options)
        {
            SearchOption searchOption = new SearchOption();
            searchOption.setCode(option.getCode());
            searchOption.setLabel(option.getLabel());
            searchOption.setDescription(option.getDescription());
            searchOptions.add(searchOption);
        }
        return searchOptions;
    }

    @Override
    public List<ExperimentType> listExperimentTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<ExperimentType> experimentTypes = commonServer.listExperimentTypes(sessionToken);
        return experimentTypes;
    }

    @Override
    public List<PropertyType> listPropertyTypes(boolean withRelations)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<PropertyType> propertyTypes =
                commonServer.listPropertyTypes(sessionToken, withRelations);
        return propertyTypes;
    }

    @Override
    public final List<DataType> listDataTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<DataType> dataTypes = commonServer.listDataTypes(sessionToken);
        return dataTypes;
    }

    @Override
    public String registerEntitytypeAndAssignPropertyTypes(NewETNewPTAssigments newETNewPTAssigments)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.registerEntitytypeAndAssignPropertyTypes(sessionToken, newETNewPTAssigments);
    }

    @Override
    public String updateEntitytypeAndPropertyTypes(NewETNewPTAssigments newETNewPTAssigments)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.updateEntitytypeAndPropertyTypes(sessionToken, newETNewPTAssigments);
    }

    @Override
    public String registerAndAssignPropertyType(PropertyType propertyType, NewETPTAssignment assignment)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.registerAndAssignPropertyType(sessionToken, propertyType, assignment);
    }

    @Override
    public String assignPropertyType(NewETPTAssignment assignment)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.assignPropertyType(sessionToken, assignment);
    }

    @Override
    public void updatePropertyTypeAssignment(NewETPTAssignment assignmentUpdates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.updatePropertyTypeAssignment(sessionToken, assignmentUpdates);
    }

    @Override
    public void unassignPropertyType(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.unassignPropertyType(sessionToken, entityKind, propertyTypeCode,
                entityTypeCode);
    }

    @Override
    public int countPropertyTypedEntities(EntityKind entityKind, String propertyTypeCode,
            String entityTypeCode)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.countPropertyTypedEntities(sessionToken, entityKind, propertyTypeCode,
                entityTypeCode);
    }

    @Override
    public final void registerPropertyType(final PropertyType propertyType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert propertyType != null : "Unspecified property type.";
        final String sessionToken = getSessionToken();
        commonServer.registerPropertyType(sessionToken, propertyType);
    }

    @Override
    public final void updatePropertyType(final IPropertyTypeUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";

        final String sessionToken = getSessionToken();
        commonServer.updatePropertyType(sessionToken, updates);
    }

    @Override
    public final void updateVocabularyTerm(final IVocabularyTermUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";

        final String sessionToken = getSessionToken();
        commonServer.updateVocabularyTerm(sessionToken, updates);
    }

    @Override
    public final void registerVocabulary(final String termsSessionKey,
            final NewVocabulary vocabulary)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabulary != null : "Unspecified vocabulary.";

        final String sessionToken = getSessionToken();
        if (vocabulary.isUploadedFromFile())
        {
            List<VocabularyTerm> extractedTerms =
                    extractVocabularyTermsFromUploadedData(termsSessionKey,
                            BatchOperationKind.REGISTRATION);
            vocabulary.setTerms(extractedTerms);
        }
        commonServer.registerVocabulary(sessionToken, vocabulary);
    }

    @Override
    public final void updateVocabulary(final IVocabularyUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";

        final String sessionToken = getSessionToken();
        commonServer.updateVocabulary(sessionToken, updates);
    }

    private final List<VocabularyTerm> extractVocabularyTermsFromUploadedData(String sessionKey,
            BatchOperationKind operationKind)
    {
        VocabularyTermsExtractor extractor =
                new VocabularyTermsExtractor(operationKind).prepareTerms(sessionKey);
        return extractor.getTerms();
    }

    private class VocabularyTermsExtractor
    {
        private List<VocabularyTerm> terms;

        private final BatchOperationKind operationKind;

        private VocabularyTermsExtractor(BatchOperationKind operationKind)
        {
            this.operationKind = operationKind;
        }

        public List<VocabularyTerm> getTerms()
        {
            return terms;
        }

        public VocabularyTermsExtractor prepareTerms(final String sessionKey)
        {
            HttpSession session = null;
            UploadedFilesBean uploadedFiles = null;
            try
            {
                session = getHttpSession();
                assert session.getAttribute(sessionKey) != null
                        && session.getAttribute(sessionKey) instanceof UploadedFilesBean : String
                                .format("No UploadedFilesBean object as session attribute '%s' found.",
                                        sessionKey);
                uploadedFiles = (UploadedFilesBean) session.getAttribute(sessionKey);
                final BisTabFileLoader<VocabularyTerm> tabFileLoader = createTermsLoader();
                terms = loadTermsFromFiles(uploadedFiles, tabFileLoader);
                return this;
            } finally
            {
                if (uploadedFiles != null)
                {
                    uploadedFiles.deleteTransferredFiles();
                }
                if (session != null)
                {
                    session.removeAttribute(sessionKey);
                }
            }
        }

        private BisTabFileLoader<VocabularyTerm> createTermsLoader()
        {
            final BisTabFileLoader<VocabularyTerm> tabFileLoader =
                    new BisTabFileLoader<VocabularyTerm>(
                            new IParserObjectFactoryFactory<VocabularyTerm>()
                                {
                                    @Override
                                    public final IParserObjectFactory<VocabularyTerm> createFactory(
                                            final IPropertyMapper propertyMapper)
                                            throws ParserException
                                    {
                                        switch (operationKind)
                                        {
                                            case REGISTRATION:
                                                return new VocabularyTermObjectFactory(
                                                        propertyMapper);
                                            case UPDATE:
                                                return new UpdatedVocabularyTermObjectFactory(
                                                        propertyMapper);
                                        }
                                        throw new UnsupportedOperationException(operationKind
                                                + " is not supported");
                                    }
                                },
                            false);
            return tabFileLoader;
        }

        private class VocabularyTermObjectFactory extends
                AbstractParserObjectFactory<VocabularyTerm>
        {
            protected VocabularyTermObjectFactory(IPropertyMapper propertyMapper)
            {
                super(VocabularyTerm.class, propertyMapper);
            }

        }

        private class UpdatedVocabularyTermObjectFactory extends
                AbstractParserObjectFactory<VocabularyTerm>
        {

            private final VocabularyTermBatchUpdateDetails basicBatchUpdateDetails;

            protected UpdatedVocabularyTermObjectFactory(IPropertyMapper propertyMapper)
            {
                super(VocabularyTerm.class, propertyMapper);
                this.basicBatchUpdateDetails = createBasicBatchUpdateDetails();
            }

            /**
             * Prepares details about which values should be updated in general taking into account only the information about availability of columns
             * in the file.
             */
            private VocabularyTermBatchUpdateDetails createBasicBatchUpdateDetails()
            {
                boolean updateLabel = isColumnAvailable(VocabularyTerm.LABEL);
                boolean updateDescription = isColumnAvailable(VocabularyTerm.DESCRIPTION);
                return new VocabularyTermBatchUpdateDetails(updateLabel, updateDescription);
            }

            @Override
            public VocabularyTerm createObject(String[] lineTokens) throws ParserException
            {
                final VocabularyTerm term = super.createObject(lineTokens);
                final VocabularyTermBatchUpdateDetails updateDetails =
                        createBatchUpdateDetails(term);
                cleanUp(term);
                return new UpdatedVocabularyTerm(term, updateDetails);
            }

            //
            // handle empty values and deletion
            //

            /**
             * Returns details about which values should be updated for the specified term. If a cell was left empty in the file the corresponding
             * value will not be modified.
             */
            private VocabularyTermBatchUpdateDetails createBatchUpdateDetails(VocabularyTerm term)
            {
                final boolean updateLabel =
                        basicBatchUpdateDetails.isLabelUpdateRequested()
                                && isNotEmpty(term.getLabel());
                final boolean updateDescription =
                        basicBatchUpdateDetails.isDescriptionUpdateRequested()
                                && isNotEmpty(term.getDescription());
                return new VocabularyTermBatchUpdateDetails(updateLabel, updateDescription);
            }

            /** Cleans properties of the specified term that are marked for deletion. */
            private void cleanUp(VocabularyTerm term)
            {
                if (isDeletionMark(term.getLabel()))
                {
                    term.setLabel(null);
                }
                if (isDeletionMark(term.getDescription()))
                {
                    term.setDescription(null);
                }
            }
        }

        private List<VocabularyTerm> loadTermsFromFiles(UploadedFilesBean uploadedFiles,
                final BisTabFileLoader<VocabularyTerm> tabFileLoader)
        {
            final List<VocabularyTerm> results = new ArrayList<VocabularyTerm>();
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                Reader reader = UnicodeUtils.createReader(multipartFile.getInputStream());
                Map<String, String> defaults = Collections.emptyMap();
                final List<VocabularyTerm> loadedTerms =
                        tabFileLoader.load(
                                new DelegatedReader(reader, multipartFile.getOriginalFilename()),
                                defaults);
                results.addAll(loadedTerms);
            }
            // set initial order equivalent with order from the file
            Long order = 1L;
            for (VocabularyTerm term : results)
            {
                term.setOrdinal(order++);
            }
            return results;
        }
    }

    @Override
    public void addVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> vocabularyTerms,
            Long previousTermOrdinal)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary id.";

        if (vocabularyTerms != null && vocabularyTerms.isEmpty() == false)
        {
            final String sessionToken = getSessionToken();
            commonServer.addVocabularyTerms(sessionToken, vocabularyId, vocabularyTerms,
                    previousTermOrdinal);
        }
    }

    @Override
    public void addUnofficialVocabularyTerm(TechId vocabularyId, String code, String label,
            String description, Long previousTermOrdinal)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary id.";

        final String sessionToken = getSessionToken();
        commonServer.addUnofficialVocabularyTerm(sessionToken, vocabularyId, code, label,
                description, previousTermOrdinal);
    }

    @Override
    public void deleteVocabularyTerms(TechId vocabularyId, List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary id.";
        assert termsToBeDeleted != null : "Unspecified term to be deleted.";
        assert termsToBeReplaced != null : "Unspecified term to be replaced.";

        final String sessionToken = getSessionToken();
        commonServer.deleteVocabularyTerms(sessionToken, vocabularyId, termsToBeDeleted,
                termsToBeReplaced);
    }

    @Override
    public void makeVocabularyTermsOfficial(TechId vocabularyId,
            List<VocabularyTerm> termsToBeOfficial)
    {
        assert vocabularyId != null : "Unspecified vocabulary id.";
        assert termsToBeOfficial != null : "Unspecified term to be official.";

        final String sessionToken = getSessionToken();
        commonServer.makeVocabularyTermsOfficial(sessionToken, vocabularyId, termsToBeOfficial);
    }

    @Override
    public List<VocabularyTerm> listVocabularyTerms(Vocabulary vocabulary)
    {
        final String sessionToken = getSessionToken();
        final Set<VocabularyTerm> terms =
                commonServer.listVocabularyTerms(sessionToken, vocabulary);
        return new ArrayList<VocabularyTerm>(terms);
    }

    @Override
    public void registerProject(String sessionKey, final Project project)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert project != null : "Unspecified project.";
        final String sessionToken = getSessionToken();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    final ProjectIdentifier projectIdentifier =
                            new ProjectIdentifierFactory(project.getIdentifier())
                                    .createIdentifier();
                    Person leader = project.getProjectLeader();
                    final String leaderId =
                            leader == null ? null : project.getProjectLeader().getUserId();
                    commonServer.registerProject(sessionToken, projectIdentifier,
                            project.getDescription(), leaderId, attachments);
                }
            }.process(sessionKey, getHttpSession(), project.getNewAttachments());

    }

    @Override
    public String prepareExportDataSetSearchHits(
            TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
    public List<MaterialType> listMaterialTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<MaterialType> materialTypes = commonServer.listMaterialTypes(sessionToken);
        return materialTypes;
    }

    @Override
    public List<DataSetType> listDataSetTypes()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final List<DataSetType> types = commonServer.listDataSetTypes(sessionToken);
        return types;
    }

    @Override
    public TypedTableResultSet<Material> listMaterials(final ListMaterialDisplayCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new AbstractMaterialProvider()
            {

                @Override
                protected List<Material> getMaterials()
                {
                    if (criteria.getListCriteria() != null)
                    {
                        return commonServer.listMaterials(getSessionToken(),
                                criteria.getListCriteria(), true);
                    } else if (criteria.getMetaprojectCriteria() != null)
                    {
                        return commonServer.listMetaprojectMaterials(getSessionToken(),
                                new MetaprojectTechIdId(criteria.getMetaprojectCriteria()
                                        .getMetaprojectId()));
                    } else
                    {
                        throw new IllegalArgumentException("Unsupported criteria: " + criteria);
                    }
                }
            }, criteria);
    }

    @Override
    public List<Material> listMetaprojectMaterials(Long metaprojectId)
    {
        return commonServer.listMetaprojectMaterials(getSessionToken(), new MetaprojectTechIdId(
                metaprojectId));
    }

    @Override
    public String prepareExportMaterials(
            TableExportCriteria<TableModelRowWithObject<Material>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public void registerMaterialType(MaterialType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.registerMaterialType(sessionToken, entityType);
    }

    @Override
    public void registerExperimentType(ExperimentType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.registerExperimentType(sessionToken, entityType);
    }

    @Override
    public void registerSampleType(SampleType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.registerSampleType(sessionToken, entityType);
    }

    @Override
    public void registerDataSetType(DataSetType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.registerDataSetType(sessionToken, entityType);
    }

    @Override
    public void registerFileType(FileFormatType type)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.registerFileFormatType(sessionToken, type);
    }

    @Override
    public void updateEntityType(EntityKind entityKind, EntityType entityType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        switch (entityKind)
        {
            case MATERIAL:
                commonServer.updateMaterialType(sessionToken, entityType);
                break;
            case SAMPLE:
                commonServer.updateSampleType(sessionToken, entityType);
                break;
            case EXPERIMENT:
                commonServer.updateExperimentType(sessionToken, entityType);
                break;
            case DATA_SET:
                commonServer.updateDataSetType(sessionToken, entityType);
                break;
        }
    }

    @Override
    public String uploadDataSets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria,
            DataSetUploadParameters uploadParameters)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        List<String> datasetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);
        return uploadDataSets(datasetCodes, uploadParameters);
    }

    private String uploadDataSets(List<String> dataSetCodes,
            DataSetUploadParameters uploadParameters)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        DataSetUploadContext uploadContext = new DataSetUploadContext();
        uploadContext.setCifexURL(uploadParameters.getCifexURL());
        uploadContext.setComment(uploadParameters.getComment());
        uploadContext.setFileName(uploadParameters.getFileName());
        uploadContext.setUserID(uploadParameters.getUserID());
        uploadContext.setPassword(uploadParameters.getPassword());
        return commonServer.uploadDataSets(sessionToken, dataSetCodes, uploadContext);
    }

    @Override
    public void deleteDataSet(String singleData, String reason, DeletionType deletionType,
            boolean forceDisallowedTypes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<String> dataSetCodes = Collections.singletonList(singleData);

        if (forceDisallowedTypes)
        {
            commonServer.deleteDataSetsForced(sessionToken, dataSetCodes, reason, deletionType,
                    isTrashEnabled());
        } else
        {
            commonServer.deleteDataSets(sessionToken, dataSetCodes, reason, deletionType,
                    isTrashEnabled());
        }
    }

    @Override
    public void deleteDataSets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria, String reason,
            DeletionType deletionType, boolean forceDisallowedTypes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<String> dataSetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);

        if (forceDisallowedTypes)
        {
            commonServer.deleteDataSetsForced(sessionToken, dataSetCodes, reason, deletionType,
                    isTrashEnabled());
        } else
        {
            commonServer.deleteDataSets(sessionToken, dataSetCodes, reason, deletionType,
                    isTrashEnabled());
        }
    }

    @Override
    public void deleteSamples(List<TechId> sampleIds, String reason, DeletionType deletionType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteSamples(sessionToken, sampleIds, reason, deletionType);
    }

    @Override
    public void deleteSample(TechId sampleId, String reason, DeletionType deletionType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteSamples(sessionToken, Collections.singletonList(sampleId), reason,
                deletionType);
    }

    @Override
    public void deleteSamples(DisplayedOrSelectedIdHolderCriteria<? extends IIdHolder> criteria,
            String reason, DeletionType deletionType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<TechId> sampleIds = extractTechIds(criteria);
        commonServer.deleteSamples(sessionToken, sampleIds, reason, deletionType);
    }

    @Override
    public void deleteExperiment(TechId experimentId, String reason, DeletionType deletionType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteExperiments(sessionToken, Collections.singletonList(experimentId),
                reason, deletionType);
    }

    @Override
    public void deleteExperiments(
            DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Experiment>> criteria,
            String reason, DeletionType deletionType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<TechId> experimentIds = extractTechIds(criteria);
        commonServer.deleteExperiments(sessionToken, experimentIds, reason, deletionType);
    }

    @Override
    public void deleteVocabularies(List<TechId> vocabularyIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteVocabularies(sessionToken, vocabularyIds, reason);
    }

    @Override
    public void deletePropertyTypes(List<TechId> propertyTypeIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deletePropertyTypes(sessionToken, propertyTypeIds, reason);
    }

    @Override
    public void deleteProjects(List<TechId> projectIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteProjects(sessionToken, projectIds, reason);
    }

    @Override
    public void deleteMetaprojects(List<TechId> metaprojectIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();

        List<IMetaprojectId> ids = new ArrayList<IMetaprojectId>();
        for (TechId metaprojectId : metaprojectIds)
        {
            ids.add(new MetaprojectTechIdId(metaprojectId));
        }

        commonServer.deleteMetaprojects(sessionToken, ids, reason);
    }

    @Override
    public void deleteSpaces(List<TechId> groupIds, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteSpaces(sessionToken, groupIds, reason);
    }

    @Override
    public void deleteScripts(List<TechId> scriptIds)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteScripts(sessionToken, scriptIds);
    }

    @Override
    public void deleteAttachments(TechId holderId, AttachmentHolderKind holderKind,
            List<String> fileNames, String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        switch (holderKind)
        {
            case EXPERIMENT:
                commonServer.deleteExperimentAttachments(sessionToken, holderId, fileNames, reason);
                break;
            case SAMPLE:
                commonServer.deleteSampleAttachments(sessionToken, holderId, fileNames, reason);
                break;
            case PROJECT:
                commonServer.deleteProjectAttachments(sessionToken, holderId, fileNames, reason);
                break;
        }
    }

    @Override
    public TypedTableResultSet<AttachmentVersions> listAttachmentVersions(
            final TechId holderId,
            final AttachmentHolderKind holderKind,
            final DefaultResultSetConfig<String, TableModelRowWithObject<AttachmentVersions>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new AttachmentVersionsProvider(commonServer, getSessionToken(),
                holderId, holderKind), criteria);
    }

    @Override
    public LastModificationState getLastModificationState()
    {
        return commonServer.getLastModificationState(getSessionToken());
    }

    @Override
    public final Experiment getExperimentInfo(final String experimentIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final ExperimentIdentifier identifier =
                new ExperimentIdentifierFactory(experimentIdentifier).createIdentifier();
        final Experiment experiment = commonServer.getExperimentInfo(sessionToken, identifier);
        transformXML(experiment);
        return experiment;
    }

    @Override
    public Experiment getExperimentInfoByPermId(String experimentPermId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        IEntityInformationHolderWithPermId expInfo =
                commonServer.getEntityInformationHolder(sessionToken, EntityKind.EXPERIMENT,
                        experimentPermId);
        return getExperimentInfo(new TechId(expInfo.getId()));
    }

    @Override
    public final Experiment getExperimentInfo(final TechId experimentId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Experiment experiment = commonServer.getExperimentInfo(sessionToken, experimentId);
        transformXML(experiment);
        return experiment;
    }

    @Override
    public Project getProjectInfo(TechId projectId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Project project = commonServer.getProjectInfo(sessionToken, projectId);
        return project;
    }

    @Override
    public Project getProjectInfo(BasicProjectIdentifier projectIdentifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Project project =
                commonServer.getProjectInfo(sessionToken, new ProjectIdentifier(projectIdentifier));
        return project;
    }

    @Override
    public Project getProjectInfoByPermId(String projectPermId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final IIdHolder holder = commonServer.getProjectIdHolder(sessionToken, projectPermId);
        return getProjectInfo(new TechId(holder.getId()));
    }

    @Override
    public String generateCode(String prefix, EntityKind entityKind)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.generateCode(sessionToken, prefix, entityKind);
    }

    @Override
    public int updateProject(final ProjectUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final AtomicInteger version = new AtomicInteger();
        new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    ProjectUpdatesDTO updatesDTO = translate(updates);
                    updatesDTO.setAttachments(attachments);
                    int versionNumber = commonServer.updateProject(sessionToken, updatesDTO);
                    version.set(versionNumber);
                }
            }.process(updates.getAttachmentSessionKey(), getHttpSession(), updates.getAttachments());
        return version.get();
    }

    private static ProjectUpdatesDTO translate(ProjectUpdates updates)
    {
        ProjectUpdatesDTO updatesDTO = new ProjectUpdatesDTO();
        updatesDTO.setDescription(updates.getDescription());
        updatesDTO.setVersion(updates.getVersion());
        updatesDTO.setTechId(updates.getTechId());
        updatesDTO.setSpaceCode(updates.getSpaceCode());
        return updatesDTO;
    }

    @Override
    public void deleteEntityTypes(EntityKind entityKind, List<String> entityTypesCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        switch (entityKind)
        {
            case DATA_SET:
                commonServer.deleteDataSetTypes(sessionToken, entityTypesCodes);
                break;
            case SAMPLE:
                commonServer.deleteSampleTypes(sessionToken, entityTypesCodes);
                break;
            case EXPERIMENT:
                commonServer.deleteExperimentTypes(sessionToken, entityTypesCodes);
                break;
            case MATERIAL:
                commonServer.deleteMaterialTypes(sessionToken, entityTypesCodes);
                break;
        }
    }

    @Override
    public IEntityInformationHolderWithPermId getEntityInformationHolder(EntityKind entityKind,
            String permId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.getEntityInformationHolder(sessionToken, entityKind, permId);
    }

    @Override
    public IEntityInformationHolderWithPermId getMaterialInformationHolder(
            MaterialIdentifier identifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.getMaterialInformationHolder(sessionToken, identifier);
    }

    @Override
    public Material getMaterialInfo(MaterialIdentifier identifier)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.getMaterialInfo(sessionToken, identifier);
    }

    @Override
    public Material getMaterialInfo(TechId techId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Material material = commonServer.getMaterialInfo(sessionToken, techId);
        transformXML(material);
        return material;
    }

    @Override
    public String getTemplate(EntityKind entityKind, String type, String format, boolean autoGenerate,
            boolean withExperiments, boolean withSpace, BatchOperationKind operationKind)
    {
        String sessionToken = getSessionToken();
        return commonServer.getTemplateColumns(sessionToken, entityKind, type, format, autoGenerate,
                withExperiments, withSpace, operationKind);
    }

    @Override
    public List<FileFormatType> listFileTypes()
    {
        final String sessionToken = getSessionToken();
        final List<FileFormatType> types = commonServer.listFileFormatTypes(sessionToken);
        return types;
    }

    @Override
    public void deleteFileFormatTypes(List<String> fileFormatTypeCodes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteFileFormatTypes(sessionToken, fileFormatTypeCodes);
    }

    @Override
    public void updateFileFormatType(AbstractType type)
    {
        final String sessionToken = getSessionToken();
        commonServer.updateFileFormatType(sessionToken, type);
    }

    @Override
    public void updateAttachment(TechId holderId, AttachmentHolderKind holderKind,
            Attachment attachment)
    {
        final String sessionToken = getSessionToken();
        switch (holderKind)
        {
            case EXPERIMENT:
                commonServer.updateExperimentAttachments(sessionToken, holderId, attachment);
                break;
            case SAMPLE:
                commonServer.updateSampleAttachments(sessionToken, holderId, attachment);
                break;
            case PROJECT:
                commonServer.updateProjectAttachments(sessionToken, holderId, attachment);
                break;
        }
    }

    @Override
    public void addAttachment(final TechId holderId, String sessionKey,
            final AttachmentHolderKind holderKind, final NewAttachment attachment)
    {
        final String sessionToken = getSessionToken();

        AttachmentRegistrationHelper helper = new AttachmentRegistrationHelper()
            {
                @Override
                public void register(Collection<NewAttachment> attachments)
                {
                    switch (holderKind)
                    {
                        case EXPERIMENT:
                            commonServer
                                    .addExperimentAttachment(sessionToken, holderId, attachment);
                            break;
                        case SAMPLE:
                            commonServer.addSampleAttachments(sessionToken, holderId, attachment);
                            break;
                        case PROJECT:
                            commonServer.addProjectAttachments(sessionToken, holderId, attachment);
                            break;
                    }
                }
            };
        helper.process(sessionKey, getHttpSession(), Collections.singletonList(attachment));
    }

    private static final String MANAGED_PROPERTY_UPDATE_ERROR_MSG =
            "Problem occured when updating managed property. "
                    + "Contact instance admin about a possible bug in script definition.";

    @Override
    public void updateManagedProperty(TechId entityId, EntityKind entityKind,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            switch (entityKind)
            {
                case EXPERIMENT:
                    commonServer.updateManagedPropertyOnExperiment(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
                case SAMPLE:
                    commonServer.updateManagedPropertyOnSample(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
                case DATA_SET:
                    commonServer.updateManagedPropertyOnDataSet(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
                case MATERIAL:
                    commonServer.updateManagedPropertyOnMaterial(sessionToken, entityId,
                            managedProperty, updateAction);
                    break;
            }
        } catch (final UserFailureException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof ValidationException)
            {
                throw new ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException(
                        cause.getMessage());
            }
            if (operationLog.isInfoEnabled())
            {
                // we do not log this as an error, because it can be only user's fault
                operationLog.info(
                        MANAGED_PROPERTY_UPDATE_ERROR_MSG + " DETAILS: " + e.getMessage(), e);
            }
            throw UserFailureExceptionTranslator.translate(e, MANAGED_PROPERTY_UPDATE_ERROR_MSG);
        }
    }

    @Override
    public List<DatastoreServiceDescription> listDataStoreServices(
            DataStoreServiceKind dataStoreServiceKind)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.listDataStoreServices(sessionToken, dataStoreServiceKind);
    }

    @Override
    public TableModelReference createReportFromDatasets(
            DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        final String sessionToken = getSessionToken();

        DatastoreServiceDescription desc =
                DatastoreServiceDescription.reporting(serviceDescription.getKey(), serviceDescription.getLabel(),
                        serviceDescription.getDatasetTypeCodes(), null, serviceDescription.tryReportingPluginType());
        List<String> datasetCodes =
                extractDatasetCodes(displayedOrSelectedDatasetCriteria, desc);
        final TableModel tableModel =
                commonServer.createReportFromDatasets(sessionToken, desc.getKey(), datasetCodes);
        String resultSetKey = saveReportInCache(tableModel);
        return new TableModelReference(resultSetKey, tableModel.getHeader());
    }

    @Override
    public TableModelReference createReportFromAggregationService(
            DatastoreServiceDescription serviceDescription, Map<String, Object> parameters)
    {
        final String sessionToken = getSessionToken();
        final TableModel tableModel =
                commonServer.createReportFromAggregationService(sessionToken, serviceDescription,
                        parameters);
        String resultSetKey = saveReportInCache(tableModel);
        return new TableModelReference(resultSetKey, tableModel.getHeader());
    }

    @Override
    public TableModelReference createReportFromTableModel(TableModel tableModel)
    {
        // WORKAROUND Need to unescape table model that was provided by the client.
        // The table model will be sent back to client and escaped. Without unescaping here
        // it would be escaped twice.
        ReflectingStringUnescaper.unescapeDeep(tableModel);
        String resultSetKey = saveReportInCache(tableModel);
        return new TableModelReference(resultSetKey, tableModel.getHeader());
    }

    @Override
    public TypedTableResultSet<ReportRowModel> listReport(
            IResultSetConfig<String, TableModelRowWithObject<ReportRowModel>> resultSetConfig)
    {
        IOriginalDataProvider<TableModelRowWithObject<ReportRowModel>> dataProvider =
                new IOriginalDataProvider<TableModelRowWithObject<ReportRowModel>>()
                    {
                        @Override
                        public List<TableModelColumnHeader> getHeaders()
                        {
                            return null;
                        }

                        @Override
                        public List<TableModelRowWithObject<ReportRowModel>> getOriginalData(
                                int maxSize) throws UserFailureException
                        {
                            throw new IllegalStateException("Data not found in the cache");
                        }

                    };
        return new TypedTableResultSet<ReportRowModel>(listEntities(resultSetConfig, dataProvider));
    }

    @Override
    public String prepareExportReport(
            TableExportCriteria<TableModelRowWithObject<ReportRowModel>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    private List<String> extractDatasetCodes(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> experimentCriteria)
    {
        // Get the referenced experiments
        List<Experiment> experiments = getReferencedExperiments(experimentCriteria);
        if (null == experiments)
        {
            return new ArrayList<String>();
        }

        DataSetRelatedEntities dataSetRelatedExperiments = new DataSetRelatedEntities(experiments);
        List<AbstractExternalData> relatedDataSets =
                commonServer.listRelatedDataSets(getSessionToken(), dataSetRelatedExperiments,
                        false);
        return Code.extractCodes(relatedDataSets);

    }

    private List<Experiment> getReferencedExperiments(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> experimentCriteria)
    {
        List<TableModelRowWithObject<Experiment>> rows;
        if (experimentCriteria.hasSelectedItems())
        {
            rows = experimentCriteria.tryGetSelectedItems();
        } else
        {
            TableExportCriteria<TableModelRowWithObject<Experiment>> displayedItemsCriteria =
                    experimentCriteria.tryGetDisplayedItems();
            assert displayedItemsCriteria != null : "displayedItemsCriteria is null";
            rows = fetchCachedEntities(displayedItemsCriteria).extractOriginalObjects();
        }
        List<Experiment> experiments = new ArrayList<Experiment>();
        for (TableModelRowWithObject<Experiment> row : rows)
        {
            experiments.add(row.getObjectOrNull());
        }
        return experiments;
    }

    private List<String> extractDatasetCodes(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        return extractDatasetCodes(displayedOrSelectedDatasetCriteria, null);
    }

    private List<String> extractDatasetCodes(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria,
            DatastoreServiceDescription serviceDescriptionOrNull)
    {
        if (displayedOrSelectedDatasetCriteria.tryGetSelectedItems() != null)
        {
            return displayedOrSelectedDatasetCriteria.tryGetSelectedItems();
        } else
        {
            TableExportCriteria<TableModelRowWithObject<AbstractExternalData>> displayedItemsCriteria =
                    displayedOrSelectedDatasetCriteria.tryGetDisplayedItems();
            assert displayedItemsCriteria != null : "displayedItemsCriteria is null";
            List<TableModelRowWithObject<AbstractExternalData>> datasets =
                    fetchCachedEntities(displayedItemsCriteria).extractOriginalObjects();
            if (serviceDescriptionOrNull != null)
            {
                datasets = filterDatasets(datasets, serviceDescriptionOrNull);
            }
            List<String> codes = new ArrayList<String>();
            for (TableModelRowWithObject<AbstractExternalData> row : datasets)
            {
                codes.add(row.getObjectOrNull().getCode());
            }
            return codes;
        }
    }

    // returns datasets which have type code belonging to the specified set and belong to the same
    // dataset store as the plugin
    private static List<TableModelRowWithObject<AbstractExternalData>> filterDatasets(
            List<TableModelRowWithObject<AbstractExternalData>> datasets,
            DatastoreServiceDescription serviceDescription)
    {
        String[] datasetTypeCodes = serviceDescription.getDatasetTypeCodes();
        Set<String> datasetTypeCodesMap = new HashSet<String>(Arrays.asList(datasetTypeCodes));
        List<TableModelRowWithObject<AbstractExternalData>> result =
                new ArrayList<TableModelRowWithObject<AbstractExternalData>>();
        String serviceDatastoreCode = serviceDescription.getDatastoreCode();
        for (TableModelRowWithObject<AbstractExternalData> row : datasets)
        {
            AbstractExternalData dataset = row.getObjectOrNull();
            String datasetTypeCode = dataset.getDataSetType().getCode();
            if (datasetTypeCodesMap.contains(datasetTypeCode))
            {
                String datasetDatastoreCode = dataset.getDataStore().getCode();

                if (serviceDatastoreCode == null || datasetDatastoreCode.equals(serviceDatastoreCode))
                {
                    result.add(row);
                }
            }
        }
        return result;
    }

    @Override
    public void processDatasets(DatastoreServiceDescription serviceDescription,
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        final String sessionToken = getSessionToken();

        DatastoreServiceDescription desc =
                DatastoreServiceDescription.processing(serviceDescription.getKey(), serviceDescription.getLabel(),
                        serviceDescription.getDatasetTypeCodes(), null);

        List<String> datasetCodes =
                extractDatasetCodes(displayedOrSelectedDatasetCriteria, desc);
        commonServer.processDatasets(sessionToken, desc, datasetCodes);
    }

    @Override
    public ArchivingResult archiveDatasets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria, boolean removeFromDataStore)
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);
        int result = commonServer.archiveDatasets(sessionToken, datasetCodes, removeFromDataStore, new HashMap<>());
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public ArchivingResult unarchiveDatasets(
            DisplayedOrSelectedDatasetCriteria displayedOrSelectedDatasetCriteria)
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(displayedOrSelectedDatasetCriteria);
        int result = commonServer.unarchiveDatasets(sessionToken, datasetCodes);
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public void deleteAuthorizationGroups(List<TechId> groupIds, String reason)
    {
        final String sessionToken = getSessionToken();
        commonServer.deleteAuthorizationGroups(sessionToken, groupIds, reason);
    }

    @Override
    public TypedTableResultSet<AuthorizationGroup> listAuthorizationGroups(
            DefaultResultSetConfig<String, TableModelRowWithObject<AuthorizationGroup>> resultSetConfig)
    {
        return listEntities(new AuthorizationGroupProvider(commonServer, getSessionToken()),
                resultSetConfig);
    }

    @Override
    public String prepareExportAuthorizationGroups(
            TableExportCriteria<TableModelRowWithObject<AuthorizationGroup>> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
    public void registerAuthorizationGroup(NewAuthorizationGroup newAuthorizationGroup)
    {
        final String sessionToken = getSessionToken();
        commonServer.registerAuthorizationGroup(sessionToken, newAuthorizationGroup);
    }

    @Override
    public void registerScript(Script script)
    {
        final String sessionToken = getSessionToken();
        commonServer.registerScript(sessionToken, script);
    }

    @Override
    public List<Person> listPersonsInAuthorizationGroup(TechId group)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.listPersonInAuthorizationGroup(sessionToken, group);
    }

    @Override
    public void updateAuthorizationGroup(AuthorizationGroupUpdates updates)
    {
        assert updates != null : "Unspecified updates.";

        final String sessionToken = getSessionToken();
        commonServer.updateAuthorizationGroup(sessionToken, updates);
    }

    @Override
    public void addPersonsToAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes)
    {
        final String sessionToken = getSessionToken();
        commonServer.addPersonsToAuthorizationGroup(sessionToken, authorizationGroupId,
                personsCodes);
    }

    @Override
    public void removePersonsFromAuthorizationGroup(TechId authorizationGroupId,
            List<String> personsCodes)
    {
        final String sessionToken = getSessionToken();
        commonServer.removePersonsFromAuthorizationGroup(sessionToken, authorizationGroupId,
                personsCodes);
    }

    private <T extends IIdHolder> List<TechId> extractTechIds(
            DisplayedOrSelectedIdHolderCriteria<T> displayedOrSelectedEntitiesCriteria)
    {
        if (displayedOrSelectedEntitiesCriteria.tryGetSelectedItems() != null)
        {
            return displayedOrSelectedEntitiesCriteria.tryGetSelectedItems();
        } else
        {
            TableExportCriteria<T> displayedItemsCriteria =
                    displayedOrSelectedEntitiesCriteria.tryGetDisplayedItems();
            assert displayedItemsCriteria != null : "displayedItemsCriteria is null";
            List<T> entities = fetchCachedEntities(displayedItemsCriteria).extractOriginalObjects();

            List<TechId> ids = new ArrayList<TechId>();
            for (T entity : entities)
            {
                if (entity instanceof TableModelRowWithObject<?>)
                {
                    TableModelRowWithObject<?> row = (TableModelRowWithObject<?>) entity;
                    Object object = row.getObjectOrNull();
                    if (object instanceof IIdHolder)
                    {
                        ids.add(TechId.create((IIdHolder) object));
                    }
                }
            }
            ids = TechId.createList(entities);
            return ids;
        }
    }

    // -- custom grid filters

    @Override
    public List<GridCustomFilter> listFilters(String gridId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.listFilters(getSessionToken(), gridId);
    }

    @Override
    public TypedTableResultSet<GridCustomFilter> listFilters(
            final String gridId,
            DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomFilter>> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new GridCustomFilterProvider(commonServer, getSessionToken(), gridId),
                resultSetConfig);
    }

    @Override
    public String prepareExportFilters(
            TableExportCriteria<TableModelRowWithObject<GridCustomFilter>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public void registerFilter(NewColumnOrFilter filter)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert filter != null : "Unspecified filter.";
        commonServer.registerFilter(getSessionToken(), filter);
    }

    @Override
    public void deleteFilters(List<TechId> filterIds)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        commonServer.deleteFilters(getSessionToken(), filterIds);
    }

    @Override
    public final void updateFilter(final IExpressionUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified updates.";
        commonServer.updateFilter(getSessionToken(), updates);
    }

    // -- grid custom columns

    @Override
    public List<GridCustomColumn> listGridCustomColumns(String gridId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return commonServer.listGridCustomColumns(getSessionToken(), gridId);
    }

    @Override
    public TypedTableResultSet<GridCustomColumn> listGridCustomColumns(
            final String gridId,
            DefaultResultSetConfig<String, TableModelRowWithObject<GridCustomColumn>> resultSetConfig)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return listEntities(new CustomGridColumnProvider(commonServer, getSessionToken(), gridId),
                resultSetConfig);
    }

    @Override
    public String prepareExportColumns(
            TableExportCriteria<TableModelRowWithObject<GridCustomColumn>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    @Override
    public void registerColumn(NewColumnOrFilter newColumn)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert newColumn != null : "Unspecified grid custom column.";
        commonServer.registerGridCustomColumn(getSessionToken(), newColumn);
    }

    @Override
    public void deleteColumns(List<TechId> columnIds)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        commonServer.deleteGridCustomColumns(getSessionToken(), columnIds);
    }

    @Override
    public void updateColumn(IExpressionUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert updates != null : "Unspecified grid custom updates.";

        commonServer.updateGridCustomColumn(getSessionToken(), updates);
    }

    // --

    @Override
    public String keepSessionAlive()
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            commonServer.keepSessionAlive(getSessionToken());
            return null;
        } catch (final Exception e)
        {
            String reason = getDisabledText();
            return reason == null ? "Session expired. Please, login again." : reason;
        }
    }

    @Override
    public void updateVocabularyTerms(String termsSessionKey, TechId vocabularyId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        assert vocabularyId != null : "Unspecified vocabulary.";
        final String sessionToken = getSessionToken();
        List<VocabularyTerm> extractedTerms =
                extractVocabularyTermsFromUploadedData(termsSessionKey, BatchOperationKind.UPDATE);
        commonServer.updateVocabularyTerms(sessionToken, vocabularyId, extractedTerms);
    }

    @Override
    public void deleteMaterials(
            DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Material>> criteria,
            String reason)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<TechId> materialIds = extractTechIds(criteria);
        commonServer.deleteMaterials(sessionToken, materialIds, reason);
    }

    @Override
    public ArchivingResult lockDatasets(DisplayedOrSelectedDatasetCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(criteria);
        int result = commonServer.lockDatasets(sessionToken, datasetCodes);
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public ArchivingResult unlockDatasets(DisplayedOrSelectedDatasetCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(criteria);
        int result = commonServer.unlockDatasets(sessionToken, datasetCodes);
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(DatastoreServiceDescription serviceDescription,
            String dataSetCode)
    {
        final String sessionToken = getSessionToken();
        final LinkModel url =
                commonServer.retrieveLinkFromDataSet(sessionToken, serviceDescription, dataSetCode);
        return url;
    }

    @Override
    public Script getScriptInfo(TechId scriptId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        final Script script = commonServer.getScriptInfo(sessionToken, scriptId);
        return script;
    }

    @Override
    public String evaluate(DynamicPropertyEvaluationInfo info)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.evaluate(sessionToken, info);
    }

    @Override
    public String evaluate(EntityValidationEvaluationInfo info)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.evaluate(sessionToken, info);
    }

    @Override
    public IEntityInformationHolderWithPermId getEntityInformationHolder(BasicEntityDescription info)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return commonServer.getEntityInformationHolder(sessionToken, info);
    }

    @Override
    public ArchivingResult archiveDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria, boolean removeFromDataStore)
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(criteria);
        int result = commonServer.archiveDatasets(sessionToken, datasetCodes, removeFromDataStore, new HashMap<>());
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public ArchivingResult unarchiveDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria)
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(criteria);
        int result = commonServer.unarchiveDatasets(sessionToken, datasetCodes);
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public ArchivingResult lockDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(criteria);
        int result = commonServer.lockDatasets(sessionToken, datasetCodes);
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public ArchivingResult unlockDatasets(
            DisplayedCriteriaOrSelectedEntityHolder<TableModelRowWithObject<Experiment>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<String> datasetCodes = extractDatasetCodes(criteria);
        int result = commonServer.unlockDatasets(sessionToken, datasetCodes);
        return new ArchivingResult(datasetCodes.size(), result);
    }

    @Override
    public EntityPropertyUpdatesResult updateProperties(List<EntityPropertyUpdates> updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        for (EntityPropertyUpdates entityPropertyUpdates : updates)
        {
            EntityPropertyUpdatesResult result = updateProperties(entityPropertyUpdates);
            if (result.tryGetErrorMessage() != null)
            {
                return result;
            }
        }
        return new EntityPropertyUpdatesResult();
    }

    @Override
    public EntityPropertyUpdatesResult updateProperties(EntityPropertyUpdates updates)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();

        IResultSetManager<String> resultSetManager = getResultSetManager();
        resultSetManager.lockResultSet(updates.getResultSetKey());
        final EntityKind entityKind = updates.getEntityKind();
        final TechId entityId = updates.getEntityId();
        final List<PropertyUpdates> modifiedProperties = updates.getModifiedProperties();

        final EntityPropertyUpdatesResult result = new EntityPropertyUpdatesResult();

        try
        {
            switch (entityKind)
            {
                case DATA_SET:
                    commonServer
                            .updateDataSetProperties(sessionToken, entityId, modifiedProperties);
                    break;
                case EXPERIMENT:
                    commonServer.updateExperimentProperties(sessionToken, entityId,
                            modifiedProperties);
                    break;
                case MATERIAL:
                    commonServer.updateMaterialProperties(sessionToken, entityId,
                            modifiedProperties);
                    break;
                case SAMPLE:
                    commonServer.updateSampleProperties(sessionToken, entityId, modifiedProperties);
                    break;
            }
        } catch (final UserFailureException e)
        {
            result.setErrorMessage(UserFailureExceptionTranslator.translate(e).getMessage());
        }
        return result;
    }

    @Override
    public TypedTableResultSet<Deletion> listDeletions(
            DefaultResultSetConfig<String, TableModelRowWithObject<Deletion>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        DeletionsProvider deletionsProvider =
                new DeletionsProvider(commonServer, getSessionToken());
        return listEntities(deletionsProvider, criteria);
    }

    @Override
    public void revertDeletions(List<TechId> deletionIds)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        try
        {
            commonServer.revertDeletions(getSessionToken(), deletionIds);
        } catch (UserFailureException ex)
        {
            throw new ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException(
                    ex.getMessage() + "\n\nTry reverting the dependent deletion first.");
        }
    }

    @Override
    public void revertDeletions(DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        revertDeletions(extractTechIds(criteria));
    }

    @Override
    public void deletePermanently(DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Deletion>> criteria, boolean forceDisallowedTypes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        deletePermanently(extractTechIds(criteria), forceDisallowedTypes);
    }

    private void deletePermanently(List<TechId> deletionIds, boolean forceDisallowedTypes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        if (forceDisallowedTypes)
        {
            commonServer.deletePermanentlyForced(getSessionToken(), deletionIds);
        } else
        {
            commonServer.deletePermanently(getSessionToken(), deletionIds);
        }
    }

    @Override
    public void emptyTrash(boolean forceDisallowedTypes)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        List<Deletion> deletions = commonServer.listDeletions(getSessionToken(), false);
        deletePermanently(TechId.createList(deletions), forceDisallowedTypes);
    }

    @Override
    public List<BatchRegistrationResult> performCustomImport(final String sessionKey, final String customImportCode, final boolean async,
            final String userEmail)
    {
        HttpSession httpSession = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        ConsumerTask asyncCustomImportTask = null;
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        final String sessionId = getSessionToken();
        try
        {
            uploadedFiles = (UploadedFilesBean) httpSession.getAttribute(sessionKey);
            abortIfMaxSizeExceeded(uploadedFiles);

            CustomImportFile customImportFile = getCustomImportFile(uploadedFiles);

            if (async)
            {
                asyncCustomImportTask = new ConsumerTask(uploadedFiles)
                    {
                        @Override
                        public String getName()
                        {
                            return "Custom import";
                        }

                        @Override
                        public String getUserEmail()
                        {
                            return userEmail;
                        }

                        @Override
                        public void doActionOrThrowException(Writer writer)
                        {
                            RequestContextHolder.setRequestAttributes(requestAttributes);
                            // Some stuff is repeated on the async executor, this is expected
                            CustomImportFile customImportFileAsync = getCustomImportFile(this.getFilesForTask());
                            // Execute task
                            commonServer.performCustomImport(sessionId, customImportCode, customImportFileAsync);
                        }
                    };

                return AsyncBatchRegistrationResult.singletonList(customImportFile.getFileName());
            } else
            {
                commonServer.performCustomImport(getSessionToken(), customImportCode, customImportFile);
                return Collections.singletonList(new BatchRegistrationResult(customImportFile.getFileName(), String
                        .format("Import successfully completed.")));
            }
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } catch (Exception e)
        {
            String message = ExceptionUtils.getEndOfChain(e).getMessage();

            throw new ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException(
                    extractStackTraces(message)
                            + " (More details can be found in the server logs.)");
        } finally
        {
            if (async && (asyncCustomImportTask != null))
            {
                asyncRegistrationQueue.addTaskAsLast(asyncCustomImportTask);
            } else
            {
                cleanUploadedFiles(sessionKey, httpSession, uploadedFiles);
            }
        }
    }

    @Override
    public void sendCountActiveUsersEmail()
    {
        commonServer.sendCountActiveUsersEmail(getSessionToken());
    }

    private String extractStackTraces(String message)
    {
        String[] lines = message.split("\n");
        StringBuilder builder = new StringBuilder();
        for (String line : lines)
        {
            if (line.startsWith("\tat ") == false)
            {
                if (builder.length() > 0)
                {
                    builder.append('\n');
                }
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static void abortIfMaxSizeExceeded(UploadedFilesBean uploadedFiles)
    {
        if (uploadedFiles != null)
        {
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                long fileSize = multipartFile.getSize();
                if (fileSize > FileUtils.ONE_GB)
                {
                    String maxSizeString = FileUtilities.byteCountToDisplaySize(FileUtils.ONE_GB);
                    String fileSizeString = FileUtilities.byteCountToDisplaySize(fileSize);
                    String errorMessage =
                            String.format("The file %s(%s) is larger than the maximum (%s).",
                                    multipartFile.getOriginalFilename(), fileSizeString,
                                    maxSizeString);
                    throw new UserFailureException(errorMessage);
                }
            }
        }
    }

    private static CustomImportFile getCustomImportFile(UploadedFilesBean uploadedFiles)
    {
        if (uploadedFiles != null)
        {
            if (uploadedFiles.size() != 1)
            {
                throw new UserFailureException(String.format(
                        "Expecting exactly one file, but %s found.", uploadedFiles.size()));
            }
            for (final IUncheckedMultipartFile multipartFile : uploadedFiles.iterable())
            {
                final String fileName = multipartFile.getOriginalFilename();
                // NOTE: this will load the entire attachments in memory
                final byte[] content = multipartFile.getBytes();
                return new CustomImportFile(fileName, content);
            }
        }

        return null;
    }

    @Override
    public void assignEntitiesToMetaProjects(EntityKind entityKind, List<Long> metaProjectIds,
            List<Long> entityIds)
    {
        MetaprojectAssignmentsIds ids = new MetaprojectAssignmentsIds();
        for (Long id : entityIds)
        {
            addId(entityKind, id, ids);
        }
        for (Long metaProjectId : metaProjectIds)
        {
            commonServer.addToMetaproject(getSessionToken(),
                    new MetaprojectTechIdId(metaProjectId), ids);
        }
    }

    @Override
    public void removeEntitiesFromMetaProjects(EntityKind entityKind, List<Long> metaProjectIds,
            List<Long> entityIds)
    {
        MetaprojectAssignmentsIds ids = new MetaprojectAssignmentsIds();
        for (Long id : entityIds)
        {
            addId(entityKind, id, ids);
        }
        for (Long metaProjectId : metaProjectIds)
        {
            commonServer.removeFromMetaproject(getSessionToken(), new MetaprojectTechIdId(
                    metaProjectId), ids);
        }
    }

    private void addId(EntityKind entityKind, Long id, MetaprojectAssignmentsIds assignments)
    {
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            assignments.addMaterial(new MaterialTechIdId(id));
        } else if (EntityKind.DATA_SET.equals(entityKind))
        {
            assignments.addDataSet(new DataSetTechIdId(id));
        } else if (EntityKind.SAMPLE.equals(entityKind))
        {
            assignments.addSample(new SampleTechIdId(id));
        } else if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            assignments.addExperiment(new ExperimentTechIdId(id));
        } else
        {
            throw new IllegalArgumentException("Unknown entity kind " + entityKind);
        }
    }

    @Override
    public void registerMetaProject(String name)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        Metaproject metaproject = new Metaproject();
        metaproject.setName(name);
        commonServer.registerMetaproject(getSessionToken(), metaproject);
    }

    @Override
    public List<String> listPredeployedPlugins(ScriptType scriptType)
    {
        return commonServer.listPredeployedPlugins(getSessionToken(), scriptType);
    }

    @Override
    public String getDisabledText()
    {
        return commonServer.getDisabledText();
    }

    @Override
    public List<String> listSampleDataSets(TechId sampleId, boolean showOnlyDirectlyConnected)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        List<AbstractExternalData> list = commonServer.listSampleExternalData(sessionToken, sampleId, showOnlyDirectlyConnected);
        return Code.extractCodes(list);
    }

    private SampleChildrenInfo getSampleChildInfo(TechId sampleId, boolean showOnlyDirectlyConnected, boolean fillChildList)
    {
        final int MAX_INFO_SIZE = 10;

        final String sessionToken = getSessionToken();
        final ListSampleCriteria sampleCriteria = ListSampleCriteria.createForParent(sampleId);
        List<Sample> results = commonServer.listSamples(sessionToken, sampleCriteria);

        SampleChildrenInfo childrenInfo = new SampleChildrenInfo(sampleId.toString());
        int count = 0;
        // get the derived samples
        if (fillChildList)
        {
            for (Sample child : results)
            {
                // after showing MAX_INFO_SIZE children/datasets we say
                // how many more is not shown
                if (count++ == MAX_INFO_SIZE)
                {
                    break;
                }
                childrenInfo.addDerivedSample(child.getIdentifier());
            }
        }
        childrenInfo.setChildCount(results.size());

        // get the data sets
        List<AbstractExternalData> dataSetList = commonServer.listSampleExternalData(sessionToken, sampleId, showOnlyDirectlyConnected);
        List<String> dataSetCodes = Code.extractCodes(dataSetList);
        count = 0;
        if (fillChildList)
        {
            for (String str : dataSetCodes)
            {
                if (count++ == MAX_INFO_SIZE)
                {
                    break;
                }
                childrenInfo.addDataSet(str);
            }
        }
        childrenInfo.setDataSetCount(dataSetCodes.size());
        return childrenInfo;
    }

    @Override
    public List<SampleChildrenInfo> getSampleChildrenInfo(List<TechId> sampleIds, boolean showOnlyDirectlyConnected)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        List<SampleChildrenInfo> list = new ArrayList<SampleChildrenInfo>();
        // if we need info for just one sample send back the
        // MAX_INFO_SIZE number of children and data sets
        if (sampleIds.size() == 1)
        {
            TechId sampleId = sampleIds.get(0);
            list.add(getSampleChildInfo(sampleId, showOnlyDirectlyConnected, true));
        } else
        {
            // if we need info for multiple samples, send back the
            // children and data set count for the first MAX_INFO_SIZE
            for (TechId sampleId : sampleIds)
            {
                list.add(getSampleChildInfo(sampleId, showOnlyDirectlyConnected, false));
            }
        }
        return list;
    }

}
