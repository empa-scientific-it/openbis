package ch.ethz.sis.openbis.generic.server.asapi.v3.pat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.delete.AuthorizationGroupDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.archive.DataSetArchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.lock.DataSetLockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unarchive.DataSetUnarchiveOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.unlock.DataSetUnlockOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.ExternalDmsDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.search.ExternalDmsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.MaterialType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.ObjectKindModification;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.objectkindmodification.search.ObjectKindModificationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.IOperationExecutionResults;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.OperationExecution;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.delete.OperationExecutionDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.fetchoptions.OperationExecutionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.id.IOperationExecutionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.search.OperationExecutionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.update.OperationExecutionUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.delete.PersonDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create.PluginCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.delete.PluginDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.evaluate.PluginEvaluationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.PluginPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update.PluginUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.delete.PropertyTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.IPropertyTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update.PropertyTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.create.QueryCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.delete.QueryDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.QueryExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.execute.SqlExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QueryDatabaseSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search.QuerySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.update.QueryUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.delete.SemanticAnnotationDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.ISemanticAnnotationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.update.SemanticAnnotationUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.AggregationService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ProcessingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ReportingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.SearchDomainServiceExecutionResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AggregationServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ProcessingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ReportingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.SearchDomainServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.AggregationServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ProcessingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ReportingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.SearchDomainServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.AggregationServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ProcessingServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ReportingServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchDomainServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.delete.TagDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.delete.VocabularyTermDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.pat.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.authentication.pat.PersonalAccessTokenSession;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

public class ApplicationServerApiPersonalAccessTokenDecorator implements IApplicationServerInternalApi
{

    private final IApplicationServerInternalApi applicationServerApi;

    public ApplicationServerApiPersonalAccessTokenDecorator(IApplicationServerInternalApi applicationServerApi)
    {
        this.applicationServerApi = applicationServerApi;
    }

    @Override public int getMajorVersion()
    {
        return applicationServerApi.getMajorVersion();
    }

    @Override public int getMinorVersion()
    {
        return applicationServerApi.getMinorVersion();
    }

    @Override public String login(final String userId, final String password)
    {
        return applicationServerApi.login(userId, password);
    }

    @Override public String loginAs(final String userId, final String password, final String asUserId)
    {
        return applicationServerApi.loginAs(userId, password, asUserId);
    }

    @Override public String loginAsSystem()
    {
        return applicationServerApi.loginAsSystem();
    }

    @Override public void registerUser(final String sessionTokenOrPAT)
    {
        // do not convert pat token
        applicationServerApi.registerUser(sessionTokenOrPAT);
    }

    @Override public String loginAsAnonymousUser()
    {
        return applicationServerApi.loginAsAnonymousUser();
    }

    @Override public void logout(final String sessionTokenOrPAT)
    {
        // do not convert pat token
        applicationServerApi.logout(sessionTokenOrPAT);
    }

    @Override public SessionInformation getSessionInformation(final String sessionTokenOrPAT)
    {
        return applicationServerApi.getSessionInformation(toSessionToken(sessionTokenOrPAT));
    }

    @Override public boolean isSessionActive(final String sessionTokenOrPAT)
    {
        return applicationServerApi.isSessionActive(toSessionToken(sessionTokenOrPAT));
    }

    @Override public List<SpacePermId> createSpaces(final String sessionTokenOrPAT, final List<SpaceCreation> newSpaces)
    {
        return createSpaces(toSessionToken(sessionTokenOrPAT), newSpaces);
    }

    @Override public List<ProjectPermId> createProjects(final String sessionTokenOrPAT, final List<ProjectCreation> newProjects)
    {
        return createProjects(toSessionToken(sessionTokenOrPAT), newProjects);
    }

    @Override public List<ExperimentPermId> createExperiments(final String sessionTokenOrPAT, final List<ExperimentCreation> newExperiments)
    {
        return createExperiments(toSessionToken(sessionTokenOrPAT), newExperiments);
    }

    @Override public List<EntityTypePermId> createExperimentTypes(final String sessionTokenOrPAT,
            final List<ExperimentTypeCreation> newExperimentTypes)
    {
        return createExperimentTypes(toSessionToken(sessionTokenOrPAT), newExperimentTypes);
    }

    @Override public List<SamplePermId> createSamples(final String sessionTokenOrPAT,
            final List<SampleCreation> newSamples)
    {
        return applicationServerApi.createSamples(toSessionToken(sessionTokenOrPAT), newSamples);
    }

    @Override public List<EntityTypePermId> createSampleTypes(final String sessionTokenOrPAT,
            final List<SampleTypeCreation> newSampleTypes)
    {
        return applicationServerApi.createSampleTypes(toSessionToken(sessionTokenOrPAT), newSampleTypes);
    }

    @Override public List<DataSetPermId> createDataSets(final String sessionTokenOrPAT,
            final List<DataSetCreation> newDataSets)
    {
        return applicationServerApi.createDataSets(toSessionToken(sessionTokenOrPAT), newDataSets);
    }

    @Override public List<EntityTypePermId> createDataSetTypes(final String sessionTokenOrPAT,
            final List<DataSetTypeCreation> newDataSetTypes)
    {
        return applicationServerApi.createDataSetTypes(toSessionToken(sessionTokenOrPAT), newDataSetTypes);
    }

    @Override public List<MaterialPermId> createMaterials(final String sessionTokenOrPAT,
            final List<MaterialCreation> newMaterials)
    {
        return applicationServerApi.createMaterials(toSessionToken(sessionTokenOrPAT), newMaterials);
    }

    @Override public List<EntityTypePermId> createMaterialTypes(final String sessionTokenOrPAT,
            final List<MaterialTypeCreation> newMaterialTypes)
    {
        return applicationServerApi.createMaterialTypes(toSessionToken(sessionTokenOrPAT), newMaterialTypes);
    }

    @Override public List<PropertyTypePermId> createPropertyTypes(final String sessionTokenOrPAT,
            final List<PropertyTypeCreation> newPropertyTypes)
    {
        return applicationServerApi.createPropertyTypes(toSessionToken(sessionTokenOrPAT), newPropertyTypes);
    }

    @Override public List<PluginPermId> createPlugins(final String sessionTokenOrPAT,
            final List<PluginCreation> newPlugins)
    {
        return applicationServerApi.createPlugins(toSessionToken(sessionTokenOrPAT), newPlugins);
    }

    @Override public List<VocabularyPermId> createVocabularies(final String sessionTokenOrPAT,
            final List<VocabularyCreation> newVocabularies)
    {
        return applicationServerApi.createVocabularies(toSessionToken(sessionTokenOrPAT), newVocabularies);
    }

    @Override public List<VocabularyTermPermId> createVocabularyTerms(final String sessionTokenOrPAT,
            final List<VocabularyTermCreation> newVocabularyTerms)
    {
        return applicationServerApi.createVocabularyTerms(toSessionToken(sessionTokenOrPAT), newVocabularyTerms);
    }

    @Override public List<TagPermId> createTags(final String sessionTokenOrPAT, final List<TagCreation> newTags)
    {
        return applicationServerApi.createTags(toSessionToken(sessionTokenOrPAT), newTags);
    }

    @Override public List<AuthorizationGroupPermId> createAuthorizationGroups(final String sessionTokenOrPAT,
            final List<AuthorizationGroupCreation> newAuthorizationGroups)
    {
        return applicationServerApi.createAuthorizationGroups(toSessionToken(sessionTokenOrPAT), newAuthorizationGroups);
    }

    @Override public List<RoleAssignmentTechId> createRoleAssignments(final String sessionTokenOrPAT,
            final List<RoleAssignmentCreation> newRoleAssignments)
    {
        return applicationServerApi.createRoleAssignments(toSessionToken(sessionTokenOrPAT), newRoleAssignments);
    }

    @Override public List<PersonPermId> createPersons(final String sessionTokenOrPAT,
            final List<PersonCreation> newPersons)
    {
        return applicationServerApi.createPersons(toSessionToken(sessionTokenOrPAT), newPersons);
    }

    @Override public List<ExternalDmsPermId> createExternalDataManagementSystems(final String sessionTokenOrPAT,
            final List<ExternalDmsCreation> newExternalDataManagementSystems)
    {
        return applicationServerApi.createExternalDataManagementSystems(toSessionToken(sessionTokenOrPAT), newExternalDataManagementSystems);
    }

    @Override public List<QueryTechId> createQueries(final String sessionTokenOrPAT,
            final List<QueryCreation> newQueries)
    {
        return applicationServerApi.createQueries(toSessionToken(sessionTokenOrPAT), newQueries);
    }

    @Override public List<SemanticAnnotationPermId> createSemanticAnnotations(final String sessionTokenOrPAT,
            final List<SemanticAnnotationCreation> newAnnotations)
    {
        return applicationServerApi.createSemanticAnnotations(toSessionToken(sessionTokenOrPAT), newAnnotations);
    }

    @Override public void updateSpaces(final String sessionTokenOrPAT, final List<SpaceUpdate> spaceUpdates)
    {
        applicationServerApi.updateSpaces(toSessionToken(sessionTokenOrPAT), spaceUpdates);
    }

    @Override public void updateProjects(final String sessionTokenOrPAT, final List<ProjectUpdate> projectUpdates)
    {
        applicationServerApi.updateProjects(toSessionToken(sessionTokenOrPAT), projectUpdates);
    }

    @Override public void updateExperiments(final String sessionTokenOrPAT,
            final List<ExperimentUpdate> experimentUpdates)
    {
        applicationServerApi.updateExperiments(toSessionToken(sessionTokenOrPAT), experimentUpdates);
    }

    @Override public void updateExperimentTypes(final String sessionTokenOrPAT,
            final List<ExperimentTypeUpdate> experimentTypeUpdates)
    {
        applicationServerApi.updateExperimentTypes(toSessionToken(sessionTokenOrPAT), experimentTypeUpdates);
    }

    @Override public void updateSamples(final String sessionTokenOrPAT, final List<SampleUpdate> sampleUpdates)
    {
        applicationServerApi.updateSamples(toSessionToken(sessionTokenOrPAT), sampleUpdates);
    }

    @Override public void updateSampleTypes(final String sessionTokenOrPAT,
            final List<SampleTypeUpdate> sampleTypeUpdates)
    {
        applicationServerApi.updateSampleTypes(toSessionToken(sessionTokenOrPAT), sampleTypeUpdates);
    }

    @Override public void updateDataSets(final String sessionTokenOrPAT, final List<DataSetUpdate> dataSetUpdates)
    {
        applicationServerApi.updateDataSets(toSessionToken(sessionTokenOrPAT), dataSetUpdates);
    }

    @Override public void updateDataSetTypes(final String sessionTokenOrPAT,
            final List<DataSetTypeUpdate> dataSetTypeUpdates)
    {
        applicationServerApi.updateDataSetTypes(toSessionToken(sessionTokenOrPAT), dataSetTypeUpdates);
    }

    @Override public void updateMaterials(final String sessionTokenOrPAT,
            final List<MaterialUpdate> materialUpdates)
    {
        applicationServerApi.updateMaterials(toSessionToken(sessionTokenOrPAT), materialUpdates);
    }

    @Override public void updateMaterialTypes(final String sessionTokenOrPAT,
            final List<MaterialTypeUpdate> materialTypeUpdates)
    {
        applicationServerApi.updateMaterialTypes(toSessionToken(sessionTokenOrPAT), materialTypeUpdates);
    }

    @Override public void updateExternalDataManagementSystems(final String sessionTokenOrPAT,
            final List<ExternalDmsUpdate> externalDmsUpdates)
    {
        applicationServerApi.updateExternalDataManagementSystems(toSessionToken(sessionTokenOrPAT), externalDmsUpdates);
    }

    @Override public void updatePropertyTypes(final String sessionTokenOrPAT,
            final List<PropertyTypeUpdate> propertyTypeUpdates)
    {
        applicationServerApi.updatePropertyTypes(toSessionToken(sessionTokenOrPAT), propertyTypeUpdates);
    }

    @Override public void updatePlugins(final String sessionTokenOrPAT, final List<PluginUpdate> pluginUpdates)
    {
        applicationServerApi.updatePlugins(toSessionToken(sessionTokenOrPAT), pluginUpdates);
    }

    @Override public void updateVocabularies(final String sessionTokenOrPAT,
            final List<VocabularyUpdate> vocabularyUpdates)
    {
        applicationServerApi.updateVocabularies(toSessionToken(sessionTokenOrPAT), vocabularyUpdates);
    }

    @Override public void updateVocabularyTerms(final String sessionTokenOrPAT,
            final List<VocabularyTermUpdate> vocabularyTermUpdates)
    {
        applicationServerApi.updateVocabularyTerms(toSessionToken(sessionTokenOrPAT), vocabularyTermUpdates);
    }

    @Override public void updateTags(final String sessionTokenOrPAT, final List<TagUpdate> tagUpdates)
    {
        applicationServerApi.updateTags(toSessionToken(sessionTokenOrPAT), tagUpdates);
    }

    @Override public void updateAuthorizationGroups(final String sessionTokenOrPAT,
            final List<AuthorizationGroupUpdate> authorizationGroupUpdates)
    {
        applicationServerApi.updateAuthorizationGroups(toSessionToken(sessionTokenOrPAT), authorizationGroupUpdates);
    }

    @Override public void updatePersons(final String sessionTokenOrPAT, final List<PersonUpdate> personUpdates)
    {
        applicationServerApi.updatePersons(toSessionToken(sessionTokenOrPAT), personUpdates);
    }

    @Override public void updateOperationExecutions(final String sessionTokenOrPAT,
            final List<OperationExecutionUpdate> executionUpdates)
    {
        applicationServerApi.updateOperationExecutions(toSessionToken(sessionTokenOrPAT), executionUpdates);
    }

    @Override public void updateSemanticAnnotations(final String sessionTokenOrPAT,
            final List<SemanticAnnotationUpdate> annotationUpdates)
    {
        applicationServerApi.updateSemanticAnnotations(toSessionToken(sessionTokenOrPAT), annotationUpdates);
    }

    @Override public void updateQueries(final String sessionTokenOrPAT, final List<QueryUpdate> queryUpdates)
    {
        applicationServerApi.updateQueries(toSessionToken(sessionTokenOrPAT), queryUpdates);
    }

    @Override public Map<IObjectId, Rights> getRights(final String sessionTokenOrPAT,
            final List<? extends IObjectId> ids, final RightsFetchOptions fetchOptions)
    {
        return applicationServerApi.getRights(toSessionToken(sessionTokenOrPAT), ids, fetchOptions);
    }

    @Override public Map<ISpaceId, Space> getSpaces(final String sessionTokenOrPAT,
            final List<? extends ISpaceId> spaceIds, final SpaceFetchOptions fetchOptions)
    {
        return applicationServerApi.getSpaces(toSessionToken(sessionTokenOrPAT), spaceIds, fetchOptions);
    }

    @Override public Map<IProjectId, Project> getProjects(final String sessionTokenOrPAT,
            final List<? extends IProjectId> projectIds, final ProjectFetchOptions fetchOptions)
    {
        return applicationServerApi.getProjects(toSessionToken(sessionTokenOrPAT), projectIds, fetchOptions);
    }

    @Override public Map<IExperimentId, Experiment> getExperiments(final String sessionTokenOrPAT,
            final List<? extends IExperimentId> experimentIds,
            final ExperimentFetchOptions fetchOptions)
    {
        return applicationServerApi.getExperiments(toSessionToken(sessionTokenOrPAT), experimentIds, fetchOptions);
    }

    @Override public Map<IEntityTypeId, ExperimentType> getExperimentTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> experimentTypeIds,
            final ExperimentTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.getExperimentTypes(toSessionToken(sessionTokenOrPAT), experimentTypeIds, fetchOptions);
    }

    @Override public Map<ISampleId, Sample> getSamples(final String sessionTokenOrPAT,
            final List<? extends ISampleId> sampleIds, final SampleFetchOptions fetchOptions)
    {
        return applicationServerApi.getSamples(toSessionToken(sessionTokenOrPAT), sampleIds, fetchOptions);
    }

    @Override public Map<IEntityTypeId, SampleType> getSampleTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> sampleTypeIds, final SampleTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.getSampleTypes(toSessionToken(sessionTokenOrPAT), sampleTypeIds, fetchOptions);
    }

    @Override public Map<IDataSetId, DataSet> getDataSets(final String sessionTokenOrPAT,
            final List<? extends IDataSetId> dataSetIds, final DataSetFetchOptions fetchOptions)
    {
        return applicationServerApi.getDataSets(toSessionToken(sessionTokenOrPAT), dataSetIds, fetchOptions);
    }

    @Override public Map<IEntityTypeId, DataSetType> getDataSetTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> dataSetTypeIds,
            final DataSetTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.getDataSetTypes(toSessionToken(sessionTokenOrPAT), dataSetTypeIds, fetchOptions);
    }

    @Override public Map<IMaterialId, Material> getMaterials(final String sessionTokenOrPAT,
            final List<? extends IMaterialId> materialIds, final MaterialFetchOptions fetchOptions)
    {
        return applicationServerApi.getMaterials(toSessionToken(sessionTokenOrPAT), materialIds, fetchOptions);
    }

    @Override public Map<IEntityTypeId, MaterialType> getMaterialTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> materialTypeIds,
            final MaterialTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.getMaterialTypes(toSessionToken(sessionTokenOrPAT), materialTypeIds, fetchOptions);
    }

    @Override public Map<IPropertyTypeId, PropertyType> getPropertyTypes(final String sessionTokenOrPAT,
            final List<? extends IPropertyTypeId> typeIds, final PropertyTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.getPropertyTypes(toSessionToken(sessionTokenOrPAT), typeIds, fetchOptions);
    }

    @Override public Map<IPluginId, Plugin> getPlugins(final String sessionTokenOrPAT,
            final List<? extends IPluginId> pluginIds, final PluginFetchOptions fetchOptions)
    {
        return applicationServerApi.getPlugins(toSessionToken(sessionTokenOrPAT), pluginIds, fetchOptions);
    }

    @Override public Map<IVocabularyId, Vocabulary> getVocabularies(final String sessionTokenOrPAT,
            final List<? extends IVocabularyId> vocabularyIds,
            final VocabularyFetchOptions fetchOptions)
    {
        return applicationServerApi.getVocabularies(toSessionToken(sessionTokenOrPAT), vocabularyIds, fetchOptions);
    }

    @Override public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(final String sessionTokenOrPAT,
            final List<? extends IVocabularyTermId> vocabularyTermIds,
            final VocabularyTermFetchOptions fetchOptions)
    {
        return applicationServerApi.getVocabularyTerms(toSessionToken(sessionTokenOrPAT), vocabularyTermIds, fetchOptions);
    }

    @Override public Map<ITagId, Tag> getTags(final String sessionTokenOrPAT, final List<? extends ITagId> tagIds,
            final TagFetchOptions fetchOptions)
    {
        return applicationServerApi.getTags(toSessionToken(sessionTokenOrPAT), tagIds, fetchOptions);
    }

    @Override public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(final String sessionTokenOrPAT,
            final List<? extends IAuthorizationGroupId> groupIds,
            final AuthorizationGroupFetchOptions fetchOptions)
    {
        return applicationServerApi.getAuthorizationGroups(toSessionToken(sessionTokenOrPAT), groupIds, fetchOptions);
    }

    @Override public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(final String sessionTokenOrPAT,
            final List<? extends IRoleAssignmentId> ids,
            final RoleAssignmentFetchOptions fetchOptions)
    {
        return applicationServerApi.getRoleAssignments(toSessionToken(sessionTokenOrPAT), ids, fetchOptions);
    }

    @Override public Map<IPersonId, Person> getPersons(final String sessionTokenOrPAT,
            final List<? extends IPersonId> ids, final PersonFetchOptions fetchOptions)
    {
        return applicationServerApi.getPersons(toSessionToken(sessionTokenOrPAT), ids, fetchOptions);
    }

    @Override public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(final String sessionTokenOrPAT,
            final List<? extends IExternalDmsId> externalDmsIds,
            final ExternalDmsFetchOptions fetchOptions)
    {
        return applicationServerApi.getExternalDataManagementSystems(toSessionToken(sessionTokenOrPAT), externalDmsIds, fetchOptions);
    }

    @Override public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(final String sessionTokenOrPAT,
            final List<? extends ISemanticAnnotationId> annotationIds,
            final SemanticAnnotationFetchOptions fetchOptions)
    {
        return applicationServerApi.getSemanticAnnotations(toSessionToken(sessionTokenOrPAT), annotationIds, fetchOptions);
    }

    @Override public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(final String sessionTokenOrPAT,
            final List<? extends IOperationExecutionId> executionIds,
            final OperationExecutionFetchOptions fetchOptions)
    {
        return applicationServerApi.getOperationExecutions(toSessionToken(sessionTokenOrPAT), executionIds, fetchOptions);
    }

    @Override public Map<IQueryId, Query> getQueries(final String sessionTokenOrPAT,
            final List<? extends IQueryId> queryIds, final QueryFetchOptions fetchOptions)
    {
        return applicationServerApi.getQueries(toSessionToken(sessionTokenOrPAT), queryIds, fetchOptions);
    }

    @Override public Map<IQueryDatabaseId, QueryDatabase> getQueryDatabases(final String sessionTokenOrPAT,
            final List<? extends IQueryDatabaseId> queryDatabaseIds,
            final QueryDatabaseFetchOptions fetchOptions)
    {
        return applicationServerApi.getQueryDatabases(toSessionToken(sessionTokenOrPAT), queryDatabaseIds, fetchOptions);
    }

    @Override public SearchResult<Space> searchSpaces(final String sessionTokenOrPAT, final SpaceSearchCriteria searchCriteria,
            final SpaceFetchOptions fetchOptions)
    {
        return applicationServerApi.searchSpaces(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Project> searchProjects(final String sessionTokenOrPAT,
            final ProjectSearchCriteria searchCriteria, final ProjectFetchOptions fetchOptions)
    {
        return applicationServerApi.searchProjects(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Experiment> searchExperiments(final String sessionTokenOrPAT,
            final ExperimentSearchCriteria searchCriteria, final ExperimentFetchOptions fetchOptions)
    {
        return applicationServerApi.searchExperiments(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<ExperimentType> searchExperimentTypes(final String sessionTokenOrPAT,
            final ExperimentTypeSearchCriteria searchCriteria,
            final ExperimentTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.searchExperimentTypes(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Sample> searchSamples(final String sessionTokenOrPAT,
            final SampleSearchCriteria searchCriteria, final SampleFetchOptions fetchOptions)
    {
        return applicationServerApi.searchSamples(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<SampleType> searchSampleTypes(final String sessionTokenOrPAT,
            final SampleTypeSearchCriteria searchCriteria, final SampleTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.searchSampleTypes(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<DataSet> searchDataSets(final String sessionTokenOrPAT,
            final DataSetSearchCriteria searchCriteria, final DataSetFetchOptions fetchOptions)
    {
        return applicationServerApi.searchDataSets(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<DataSetType> searchDataSetTypes(final String sessionTokenOrPAT,
            final DataSetTypeSearchCriteria searchCriteria, final DataSetTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.searchDataSetTypes(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Material> searchMaterials(final String sessionTokenOrPAT,
            final MaterialSearchCriteria searchCriteria, final MaterialFetchOptions fetchOptions)
    {
        return applicationServerApi.searchMaterials(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<ExternalDms> searchExternalDataManagementSystems(final String sessionTokenOrPAT,
            final ExternalDmsSearchCriteria searchCriteria,
            final ExternalDmsFetchOptions fetchOptions)
    {
        return applicationServerApi.searchExternalDataManagementSystems(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<MaterialType> searchMaterialTypes(final String sessionTokenOrPAT,
            final MaterialTypeSearchCriteria searchCriteria,
            final MaterialTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.searchMaterialTypes(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Plugin> searchPlugins(final String sessionTokenOrPAT,
            final PluginSearchCriteria searchCriteria, final PluginFetchOptions fetchOptions)
    {
        return applicationServerApi.searchPlugins(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Vocabulary> searchVocabularies(final String sessionTokenOrPAT,
            final VocabularySearchCriteria searchCriteria, final VocabularyFetchOptions fetchOptions)
    {
        return applicationServerApi.searchVocabularies(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<VocabularyTerm> searchVocabularyTerms(final String sessionTokenOrPAT,
            final VocabularyTermSearchCriteria searchCriteria,
            final VocabularyTermFetchOptions fetchOptions)
    {
        return applicationServerApi.searchVocabularyTerms(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Tag> searchTags(final String sessionTokenOrPAT, final TagSearchCriteria searchCriteria,
            final TagFetchOptions fetchOptions)
    {
        return applicationServerApi.searchTags(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<AuthorizationGroup> searchAuthorizationGroups(final String sessionTokenOrPAT,
            final AuthorizationGroupSearchCriteria searchCriteria,
            final AuthorizationGroupFetchOptions fetchOptions)
    {
        return applicationServerApi.searchAuthorizationGroups(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<RoleAssignment> searchRoleAssignments(final String sessionTokenOrPAT,
            final RoleAssignmentSearchCriteria searchCriteria,
            final RoleAssignmentFetchOptions fetchOptions)
    {
        return applicationServerApi.searchRoleAssignments(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Person> searchPersons(final String sessionTokenOrPAT,
            final PersonSearchCriteria searchCriteria, final PersonFetchOptions fetchOptions)
    {
        return applicationServerApi.searchPersons(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<CustomASService> searchCustomASServices(final String sessionTokenOrPAT,
            final CustomASServiceSearchCriteria searchCriteria,
            final CustomASServiceFetchOptions fetchOptions)
    {
        return applicationServerApi.searchCustomASServices(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<SearchDomainService> searchSearchDomainServices(final String sessionTokenOrPAT,
            final SearchDomainServiceSearchCriteria searchCriteria,
            final SearchDomainServiceFetchOptions fetchOptions)
    {
        return applicationServerApi.searchSearchDomainServices(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<AggregationService> searchAggregationServices(final String sessionTokenOrPAT,
            final AggregationServiceSearchCriteria searchCriteria,
            final AggregationServiceFetchOptions fetchOptions)
    {
        return applicationServerApi.searchAggregationServices(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<ReportingService> searchReportingServices(final String sessionTokenOrPAT,
            final ReportingServiceSearchCriteria searchCriteria,
            final ReportingServiceFetchOptions fetchOptions)
    {
        return applicationServerApi.searchReportingServices(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<ProcessingService> searchProcessingServices(final String sessionTokenOrPAT,
            final ProcessingServiceSearchCriteria searchCriteria,
            final ProcessingServiceFetchOptions fetchOptions)
    {
        return applicationServerApi.searchProcessingServices(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<ObjectKindModification> searchObjectKindModifications(final String sessionTokenOrPAT,
            final ObjectKindModificationSearchCriteria searchCriteria,
            final ObjectKindModificationFetchOptions fetchOptions)
    {
        return applicationServerApi.searchObjectKindModifications(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<GlobalSearchObject> searchGlobally(final String sessionTokenOrPAT,
            final GlobalSearchCriteria searchCriteria, final GlobalSearchObjectFetchOptions fetchOptions)
    {
        return applicationServerApi.searchGlobally(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<OperationExecution> searchOperationExecutions(final String sessionTokenOrPAT,
            final OperationExecutionSearchCriteria searchCriteria,
            final OperationExecutionFetchOptions fetchOptions)
    {
        return applicationServerApi.searchOperationExecutions(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<DataStore> searchDataStores(final String sessionTokenOrPAT,
            final DataStoreSearchCriteria searchCriteria, final DataStoreFetchOptions fetchOptions)
    {
        return applicationServerApi.searchDataStores(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<SemanticAnnotation> searchSemanticAnnotations(final String sessionTokenOrPAT,
            final SemanticAnnotationSearchCriteria searchCriteria,
            final SemanticAnnotationFetchOptions fetchOptions)
    {
        return applicationServerApi.searchSemanticAnnotations(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<PropertyType> searchPropertyTypes(final String sessionTokenOrPAT,
            final PropertyTypeSearchCriteria searchCriteria,
            final PropertyTypeFetchOptions fetchOptions)
    {
        return applicationServerApi.searchPropertyTypes(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<PropertyAssignment> searchPropertyAssignments(final String sessionTokenOrPAT,
            final PropertyAssignmentSearchCriteria searchCriteria,
            final PropertyAssignmentFetchOptions fetchOptions)
    {
        return applicationServerApi.searchPropertyAssignments(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Query> searchQueries(final String sessionTokenOrPAT,
            final QuerySearchCriteria searchCriteria, final QueryFetchOptions fetchOptions)
    {
        return applicationServerApi.searchQueries(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<QueryDatabase> searchQueryDatabases(final String sessionTokenOrPAT,
            final QueryDatabaseSearchCriteria searchCriteria,
            final QueryDatabaseFetchOptions fetchOptions)
    {
        return applicationServerApi.searchQueryDatabases(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public void deleteSpaces(final String sessionTokenOrPAT, final List<? extends ISpaceId> spaceIds,
            final SpaceDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteSpaces(toSessionToken(sessionTokenOrPAT), spaceIds, deletionOptions);
    }

    @Override public void deleteProjects(final String sessionTokenOrPAT, final List<? extends IProjectId> projectIds,
            final ProjectDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteProjects(toSessionToken(sessionTokenOrPAT), projectIds, deletionOptions);
    }

    @Override public IDeletionId deleteExperiments(final String sessionTokenOrPAT,
            final List<? extends IExperimentId> experimentIds,
            final ExperimentDeletionOptions deletionOptions)
    {
        return applicationServerApi.deleteExperiments(toSessionToken(sessionTokenOrPAT), experimentIds, deletionOptions);
    }

    @Override public IDeletionId deleteSamples(final String sessionTokenOrPAT,
            final List<? extends ISampleId> sampleIds, final SampleDeletionOptions deletionOptions)
    {
        return applicationServerApi.deleteSamples(toSessionToken(sessionTokenOrPAT), sampleIds, deletionOptions);
    }

    @Override public IDeletionId deleteDataSets(final String sessionTokenOrPAT,
            final List<? extends IDataSetId> dataSetIds, final DataSetDeletionOptions deletionOptions)
    {
        return applicationServerApi.deleteDataSets(toSessionToken(sessionTokenOrPAT), dataSetIds, deletionOptions);
    }

    @Override public void deleteMaterials(final String sessionTokenOrPAT,
            final List<? extends IMaterialId> materialIds, final MaterialDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteMaterials(toSessionToken(sessionTokenOrPAT), materialIds, deletionOptions);
    }

    @Override public void deletePlugins(final String sessionTokenOrPAT, final List<? extends IPluginId> pluginIds,
            final PluginDeletionOptions deletionOptions)
    {
        applicationServerApi.deletePlugins(toSessionToken(sessionTokenOrPAT), pluginIds, deletionOptions);
    }

    @Override public void deletePropertyTypes(final String sessionTokenOrPAT,
            final List<? extends IPropertyTypeId> propertyTypeIds,
            final PropertyTypeDeletionOptions deletionOptions)
    {
        applicationServerApi.deletePropertyTypes(toSessionToken(sessionTokenOrPAT), propertyTypeIds, deletionOptions);
    }

    @Override public void deleteVocabularies(final String sessionTokenOrPAT,
            final List<? extends IVocabularyId> ids, final VocabularyDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteVocabularies(toSessionToken(sessionTokenOrPAT), ids, deletionOptions);
    }

    @Override public void deleteVocabularyTerms(final String sessionTokenOrPAT,
            final List<? extends IVocabularyTermId> termIds,
            final VocabularyTermDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteVocabularyTerms(toSessionToken(sessionTokenOrPAT), termIds, deletionOptions);
    }

    @Override public void deleteExperimentTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> experimentTypeIds,
            final ExperimentTypeDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteExperimentTypes(toSessionToken(sessionTokenOrPAT), experimentTypeIds, deletionOptions);
    }

    @Override public void deleteSampleTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> sampleTypeIds, final SampleTypeDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteSampleTypes(toSessionToken(sessionTokenOrPAT), sampleTypeIds, deletionOptions);
    }

    @Override public void deleteDataSetTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> dataSetTypeIds,
            final DataSetTypeDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteDataSetTypes(toSessionToken(sessionTokenOrPAT), dataSetTypeIds, deletionOptions);
    }

    @Override public void deleteMaterialTypes(final String sessionTokenOrPAT,
            final List<? extends IEntityTypeId> materialTypeIds,
            final MaterialTypeDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteMaterialTypes(toSessionToken(sessionTokenOrPAT), materialTypeIds, deletionOptions);
    }

    @Override public void deleteExternalDataManagementSystems(final String sessionTokenOrPAT,
            final List<? extends IExternalDmsId> externalDmsIds,
            final ExternalDmsDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteExternalDataManagementSystems(toSessionToken(sessionTokenOrPAT), externalDmsIds, deletionOptions);
    }

    @Override public void deleteTags(final String sessionTokenOrPAT, final List<? extends ITagId> tagIds,
            final TagDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteTags(toSessionToken(sessionTokenOrPAT), tagIds, deletionOptions);
    }

    @Override public void deleteAuthorizationGroups(final String sessionTokenOrPAT,
            final List<? extends IAuthorizationGroupId> groupIds,
            final AuthorizationGroupDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteAuthorizationGroups(toSessionToken(sessionTokenOrPAT), groupIds, deletionOptions);
    }

    @Override public void deleteRoleAssignments(final String sessionTokenOrPAT,
            final List<? extends IRoleAssignmentId> assignmentIds,
            final RoleAssignmentDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteRoleAssignments(toSessionToken(sessionTokenOrPAT), assignmentIds, deletionOptions);
    }

    @Override public void deleteOperationExecutions(final String sessionTokenOrPAT,
            final List<? extends IOperationExecutionId> executionIds,
            final OperationExecutionDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteOperationExecutions(toSessionToken(sessionTokenOrPAT), executionIds, deletionOptions);
    }

    @Override public void deleteSemanticAnnotations(final String sessionTokenOrPAT,
            final List<? extends ISemanticAnnotationId> annotationIds,
            final SemanticAnnotationDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteSemanticAnnotations(toSessionToken(sessionTokenOrPAT), annotationIds, deletionOptions);
    }

    @Override public void deleteQueries(final String sessionTokenOrPAT, final List<? extends IQueryId> queryIds,
            final QueryDeletionOptions deletionOptions)
    {
        applicationServerApi.deleteQueries(toSessionToken(sessionTokenOrPAT), queryIds, deletionOptions);
    }

    @Override public void deletePersons(final String sessionTokenOrPAT, final List<? extends IPersonId> personIds,
            final PersonDeletionOptions deletionOptions)
    {
        applicationServerApi.deletePersons(toSessionToken(sessionTokenOrPAT), personIds, deletionOptions);
    }

    @Override public SearchResult<Deletion> searchDeletions(final String sessionTokenOrPAT,
            final DeletionSearchCriteria searchCriteria, final DeletionFetchOptions fetchOptions)
    {
        return applicationServerApi.searchDeletions(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public SearchResult<Event> searchEvents(final String sessionTokenOrPAT, final EventSearchCriteria searchCriteria,
            final EventFetchOptions fetchOptions)
    {
        return applicationServerApi.searchEvents(toSessionToken(sessionTokenOrPAT), searchCriteria, fetchOptions);
    }

    @Override public void revertDeletions(final String sessionTokenOrPAT,
            final List<? extends IDeletionId> deletionIds)
    {
        applicationServerApi.revertDeletions(toSessionToken(sessionTokenOrPAT), deletionIds);
    }

    @Override public void confirmDeletions(final String sessionTokenOrPAT,
            final List<? extends IDeletionId> deletionIds)
    {
        applicationServerApi.confirmDeletions(toSessionToken(sessionTokenOrPAT), deletionIds);
    }

    @Override public Object executeCustomASService(final String sessionTokenOrPAT, final ICustomASServiceId serviceId,
            final CustomASServiceExecutionOptions options)
    {
        return applicationServerApi.executeCustomASService(toSessionToken(sessionTokenOrPAT), serviceId, options);
    }

    @Override public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(final String sessionTokenOrPAT,
            final SearchDomainServiceExecutionOptions options)
    {
        return applicationServerApi.executeSearchDomainService(toSessionToken(sessionTokenOrPAT), options);
    }

    @Override public TableModel executeAggregationService(final String sessionTokenOrPAT, final IDssServiceId serviceId,
            final AggregationServiceExecutionOptions options)
    {
        return applicationServerApi.executeAggregationService(toSessionToken(sessionTokenOrPAT), serviceId, options);
    }

    @Override public TableModel executeReportingService(final String sessionTokenOrPAT, final IDssServiceId serviceId,
            final ReportingServiceExecutionOptions options)
    {
        return applicationServerApi.executeReportingService(toSessionToken(sessionTokenOrPAT), serviceId, options);
    }

    @Override public void executeProcessingService(final String sessionTokenOrPAT, final IDssServiceId serviceId,
            final ProcessingServiceExecutionOptions options)
    {
        applicationServerApi.executeProcessingService(toSessionToken(sessionTokenOrPAT), serviceId, options);
    }

    @Override public TableModel executeQuery(final String sessionTokenOrPAT, final IQueryId queryId,
            final QueryExecutionOptions options)
    {
        return applicationServerApi.executeQuery(toSessionToken(sessionTokenOrPAT), queryId, options);
    }

    @Override public TableModel executeSql(final String sessionTokenOrPAT, final String sql, final SqlExecutionOptions options)
    {
        return applicationServerApi.executeSql(toSessionToken(sessionTokenOrPAT), sql, options);
    }

    @Override public PluginEvaluationResult evaluatePlugin(final String sessionTokenOrPAT,
            final PluginEvaluationOptions options)
    {
        return applicationServerApi.evaluatePlugin(toSessionToken(sessionTokenOrPAT), options);
    }

    @Override public void archiveDataSets(final String sessionTokenOrPAT, final List<? extends IDataSetId> dataSetIds,
            final DataSetArchiveOptions options)
    {
        applicationServerApi.archiveDataSets(toSessionToken(sessionTokenOrPAT), dataSetIds, options);
    }

    @Override public void unarchiveDataSets(final String sessionTokenOrPAT,
            final List<? extends IDataSetId> dataSetIds, final DataSetUnarchiveOptions options)
    {
        applicationServerApi.unarchiveDataSets(toSessionToken(sessionTokenOrPAT), dataSetIds, options);
    }

    @Override public void lockDataSets(final String sessionTokenOrPAT, final List<? extends IDataSetId> dataSetIds,
            final DataSetLockOptions options)
    {
        applicationServerApi.lockDataSets(toSessionToken(sessionTokenOrPAT), dataSetIds, options);
    }

    @Override public void unlockDataSets(final String sessionTokenOrPAT, final List<? extends IDataSetId> dataSetIds,
            final DataSetUnlockOptions options)
    {
        applicationServerApi.unlockDataSets(toSessionToken(sessionTokenOrPAT), dataSetIds, options);
    }

    @Override public IOperationExecutionResults executeOperations(final String sessionTokenOrPAT,
            final List<? extends IOperation> operations, final IOperationExecutionOptions options)
    {
        return applicationServerApi.executeOperations(toSessionToken(sessionTokenOrPAT), operations, options);
    }

    @Override public Map<String, String> getServerInformation(final String sessionTokenOrPAT)
    {
        return applicationServerApi.getServerInformation(toSessionToken(sessionTokenOrPAT));
    }

    @Override public Map<String, String> getServerPublicInformation()
    {
        return applicationServerApi.getServerPublicInformation();
    }

    @Override public List<String> createPermIdStrings(final String sessionTokenOrPAT, final int count)
    {
        return applicationServerApi.createPermIdStrings(toSessionToken(sessionTokenOrPAT), count);
    }

    @Override public List<String> createCodes(final String sessionTokenOrPAT, final String prefix, final EntityKind entityKind, final int count)
    {
        return applicationServerApi.createCodes(toSessionToken(sessionTokenOrPAT), prefix, entityKind, count);
    }

    private String toSessionToken(String sessionTokenOrPAT)
    {
        IPersonalAccessTokenDAO patDAO = CommonServiceProvider.getDAOFactory().getPersonalAccessTokenDAO();
        PersonalAccessToken patToken = patDAO.getTokenByHash(sessionTokenOrPAT);

        if (patToken == null)
        {
            return sessionTokenOrPAT;
        } else
        {
            Date now = new Date();

            if (now.before(patToken.getValidFrom()))
            {
                throw new InvalidSessionException("Personal access token is not yet valid.");
            }

            if (now.after(patToken.getValidUntil()))
            {
                throw new InvalidSessionException("Personal access token is no longer valid.");
            }

            patToken.setLastAccessedAt(now);
            patDAO.updateToken(patToken);

            final PersonalAccessTokenSession patSession =
                    patDAO.getSessionByUserIdAndSessionName(patToken.getUserId(), patToken.getSessionName());

            if (patSession == null)
            {
                throw new InvalidSessionException("Personal access token session does not exist.");
            } else
            {
                return patSession.getHash();
            }
        }
    }

}
