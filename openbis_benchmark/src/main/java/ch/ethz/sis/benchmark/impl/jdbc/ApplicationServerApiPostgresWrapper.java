package ch.ethz.sis.benchmark.impl.jdbc;

import ch.ethz.sis.benchmark.impl.IApplicationServerApiWrapper;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.AggregationServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ProcessingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ReportingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.SearchDomainServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.ICustomASServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.*;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationServerApiPostgresWrapper implements IApplicationServerApiWrapper {

    private IApplicationServerApi instance;
    private String databaseURL;
    private String databaseUser;
    private String databasePass;
    private Connection connection;

    public ApplicationServerApiPostgresWrapper(String databaseURL,
                                               String databaseUser,
                                               String databasePass) throws SQLException {
        //
        this.databaseURL = databaseURL;
        this.databaseUser = databaseUser;
        this.databasePass = databasePass;
        DriverManager.registerDriver(new org.postgresql.Driver());
        //
    }


    public void setInstance(IApplicationServerApi instance) {
        this.instance = instance;
    }

    @Override
    public String login(String s, String s1) {
        //
        try {
            connection = DriverManager.getConnection(databaseURL, databaseUser, databasePass);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        //
        return instance.login(s, s1);
    }

    @Override
    public String loginAs(String s, String s1, String s2) {
        return instance.loginAs(s, s1, s2);
    }

    @Override
    public String loginAsAnonymousUser() {
        return instance.loginAsAnonymousUser();
    }

    @Override
    public void logout(String s) {
        //
        try {
            connection.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        //
        instance.logout(s);
    }

    @Override
    public SessionInformation getSessionInformation(String s) {
        return instance.getSessionInformation(s);
    }

    @Override
    public boolean isSessionActive(String s) {
        return instance.isSessionActive(s);
    }

    @Override
    public List<SpacePermId> createSpaces(String s, List<SpaceCreation> list) {
        return instance.createSpaces(s, list);
    }

    @Override
    public List<ProjectPermId> createProjects(String s, List<ProjectCreation> list) {
        return instance.createProjects(s, list);
    }

    @Override
    public List<ExperimentPermId> createExperiments(String s, List<ExperimentCreation> list) {
        return instance.createExperiments(s, list);
    }

    @Override
    public List<EntityTypePermId> createExperimentTypes(String s, List<ExperimentTypeCreation> list) {
        return instance.createExperimentTypes(s, list);
    }

    @Override
    public List<SamplePermId> createSamples(String s, List<SampleCreation> list) {
        long start = System.currentTimeMillis();
        Set<String> spaceCodes = list.stream().map(sc -> ((SpacePermId) sc.getSpaceId()).getPermId()).collect(Collectors.toSet());
        Map<String, Long> spaceIdsByCode = SQLQueries.getSpaceIds(connection, spaceCodes);

        Set<String> projectIdentifiers = list.stream().map(sc -> ((ProjectIdentifier) sc.getProjectId() != null)?((ProjectIdentifier) sc.getProjectId()).getIdentifier():null).collect(Collectors.toSet());

        Map<String, Long> projectIdsByIdentifier = Map.of();
        if(!projectIdentifiers.contains(null)) {
            projectIdsByIdentifier = SQLQueries.getProjectIds(connection, projectIdentifiers, spaceIdsByCode);
        }

        Set<String> experimentIdentifiers = list.stream().map(sc -> ((ExperimentIdentifier) sc.getExperimentId() != null)?((ExperimentIdentifier) sc.getExperimentId()).getIdentifier():null).collect(Collectors.toSet());
        Map<String, Long> experimentIdsByIdentifier = Map.of();
        if(!experimentIdentifiers.contains(null)) {
            experimentIdsByIdentifier = SQLQueries.getExperimentIds(connection, experimentIdentifiers, projectIdsByIdentifier);
        }

        Set<String> sampleTypeCodes = list.stream().map(sc -> ((EntityTypePermId) sc.getTypeId()).getPermId()).collect(Collectors.toSet());
        Map<String, Long> sampleTypeIdsByCode = SQLQueries.getTypeIds(connection, sampleTypeCodes);

        Set<String> propertyTypeCodes = list.stream().flatMap(sc -> sc.getProperties().keySet().stream()).collect(Collectors.toSet());
        Map<String, Long> propertyTypeIdsByCode = SQLQueries.getPropertyTypeIds(connection, propertyTypeCodes);

        SessionInformation sessionInformation = instance.getSessionInformation(s);
        String user_id = sessionInformation.getPerson().getUserId();
        Long personId = SQLQueries.getPersonId(connection, user_id);

        Set<String> stIdsptIds = new HashSet<>();
        for (SampleCreation sampleCreation:list) {
            Long sampleTypeId = sampleTypeIdsByCode.get(((EntityTypePermId) sampleCreation.getTypeId()).getPermId());
            for (String propertyCode:sampleCreation.getProperties().keySet()) {
                Long propertyTypeId = propertyTypeIdsByCode.get(propertyCode);
                stIdsptIds.add(sampleTypeId + ":" + propertyTypeId);
            }
        }
        Map<String, Long> stIdsptIdsByReference = SQLQueries.getSampleTypePropertyTypeIds(connection, stIdsptIds);

        List<SamplePermId> permIds = new ArrayList<>(list.size());

        List<Long> nextIds = SQLQueries.nextSampleIds(connection, list.size());

        List<List<Object>> sampleInsertArgs = new ArrayList<>();
        List<List<Object>> samplePropertiesInsertArgs = new ArrayList<>();
        for (int idx = 0; idx < list.size(); idx++) {
            SampleCreation sampleCreation = list.get(idx);

            Long id = nextIds.get(idx);
            String perm_id = UUID.randomUUID().toString();
            permIds.add(new SamplePermId(perm_id));
            String code = sampleCreation.getCode();
            Long proj_id = null;
            if (sampleCreation.getProjectId() != null) {
                proj_id = projectIdsByIdentifier.get(((ProjectIdentifier) sampleCreation.getProjectId()).getIdentifier());
            }
            Long expe_id = null;
            if (sampleCreation.getExperimentId() != null) {
                expe_id = experimentIdsByIdentifier.get(((ExperimentIdentifier) sampleCreation.getExperimentId()).getIdentifier());
            }
            Long saty_id = sampleTypeIdsByCode.get(((EntityTypePermId)sampleCreation.getTypeId()).getPermId());
            Long pers_id_registerer = personId;
            Long modification_timestamp = personId;
            Long space_id = spaceIdsByCode.get(((SpacePermId) sampleCreation.getSpaceId()).getPermId());
            sampleInsertArgs.add(Arrays.asList(id, perm_id, code, proj_id, expe_id, saty_id, pers_id_registerer, modification_timestamp, space_id));

            Long sampleTypeId = sampleTypeIdsByCode.get(((EntityTypePermId) sampleCreation.getTypeId()).getPermId());
            for (String propertyCode:sampleCreation.getProperties().keySet()) {
                Long propertyTypeId = propertyTypeIdsByCode.get(propertyCode);
                String reference = sampleTypeId + ":" + propertyTypeId;
                samplePropertiesInsertArgs.add(Arrays.asList(id, stIdsptIdsByReference.get(reference), sampleCreation.getProperties().get(propertyCode), pers_id_registerer, modification_timestamp));
            }
        }

        try {
            connection.setAutoCommit(false);

            long end = System.currentTimeMillis();
            System.out.println("ApplicationServerApiPostgresWrapper.createSamples Preparation Took: " + (end-start)/1000 + " sec");
            int inserts = SQLQueries.insertSamples(connection, sampleInsertArgs);
            long end2 = System.currentTimeMillis();
            System.out.println("ApplicationServerApiPostgresWrapper.createSamples inserts Took: " + (end2-end)/1000 + " sec");
            int propertyInserts = SQLQueries.insertSamplesProperties(connection, samplePropertiesInsertArgs);
            long end3 = System.currentTimeMillis();
            System.out.println("ApplicationServerApiPostgresWrapper.createSamples property inserts Took: " + (end3-end2)/1000 + " sec");
            System.out.println("ApplicationServerApiPostgresWrapper.createSamples Total: " + (end3-start)/1000 + " sec");
            System.out.println("ApplicationServerApiPostgresWrapper.createSamples Total: " + list.size()/((end3-start)/1000) + " samples/sec");

            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (Exception ex2) {}
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex3) {}
        }

        return permIds;
    }

    @Override
    public List<EntityTypePermId> createSampleTypes(String s, List<SampleTypeCreation> list) {
        return instance.createSampleTypes(s, list);
    }

    @Override
    public List<DataSetPermId> createDataSets(String s, List<DataSetCreation> list) {
        return instance.createDataSets(s, list);
    }

    @Override
    public List<EntityTypePermId> createDataSetTypes(String s, List<DataSetTypeCreation> list) {
        return instance.createDataSetTypes(s, list);
    }

    @Override
    public List<MaterialPermId> createMaterials(String s, List<MaterialCreation> list) {
        return instance.createMaterials(s, list);
    }

    @Override
    public List<EntityTypePermId> createMaterialTypes(String s, List<MaterialTypeCreation> list) {
        return instance.createMaterialTypes(s, list);
    }

    @Override
    public List<PropertyTypePermId> createPropertyTypes(String s, List<PropertyTypeCreation> list) {
        return instance.createPropertyTypes(s, list);
    }

    @Override
    public List<PluginPermId> createPlugins(String s, List<PluginCreation> list) {
        return instance.createPlugins(s, list);
    }

    @Override
    public List<VocabularyPermId> createVocabularies(String s, List<VocabularyCreation> list) {
        return instance.createVocabularies(s, list);
    }

    @Override
    public List<VocabularyTermPermId> createVocabularyTerms(String s, List<VocabularyTermCreation> list) {
        return instance.createVocabularyTerms(s, list);
    }

    @Override
    public List<TagPermId> createTags(String s, List<TagCreation> list) {
        return instance.createTags(s, list);
    }

    @Override
    public List<AuthorizationGroupPermId> createAuthorizationGroups(String s, List<AuthorizationGroupCreation> list) {
        return instance.createAuthorizationGroups(s, list);
    }

    @Override
    public List<RoleAssignmentTechId> createRoleAssignments(String s, List<RoleAssignmentCreation> list) {
        return instance.createRoleAssignments(s, list);
    }

    @Override
    public List<PersonPermId> createPersons(String s, List<PersonCreation> list) {
        return instance.createPersons(s, list);
    }

    @Override
    public List<ExternalDmsPermId> createExternalDataManagementSystems(String s, List<ExternalDmsCreation> list) {
        return instance.createExternalDataManagementSystems(s, list);
    }

    @Override
    public List<QueryTechId> createQueries(String s, List<QueryCreation> list) {
        return instance.createQueries(s, list);
    }

    @Override
    public List<SemanticAnnotationPermId> createSemanticAnnotations(String s, List<SemanticAnnotationCreation> list) {
        return instance.createSemanticAnnotations(s, list);
    }

    @Override
    public void updateSpaces(String s, List<SpaceUpdate> list) {
        instance.updateSpaces(s, list);
    }

    @Override
    public void updateProjects(String s, List<ProjectUpdate> list) {
        instance.updateProjects(s, list);
    }

    @Override
    public void updateExperiments(String s, List<ExperimentUpdate> list) {
        instance.updateExperiments(s, list);
    }

    @Override
    public void updateExperimentTypes(String s, List<ExperimentTypeUpdate> list) {
        instance.updateExperimentTypes(s, list);
    }

    @Override
    public void updateSamples(String s, List<SampleUpdate> list) {
        instance.updateSamples(s, list);
    }

    @Override
    public void updateSampleTypes(String s, List<SampleTypeUpdate> list) {
        instance.updateSampleTypes(s, list);
    }

    @Override
    public void updateDataSets(String s, List<DataSetUpdate> list) {
        instance.updateDataSets(s, list);
    }

    @Override
    public void updateDataSetTypes(String s, List<DataSetTypeUpdate> list) {
        instance.updateDataSetTypes(s, list);
    }

    @Override
    public void updateMaterials(String s, List<MaterialUpdate> list) {
        instance.updateMaterials(s, list);
    }

    @Override
    public void updateMaterialTypes(String s, List<MaterialTypeUpdate> list) {
        instance.updateMaterialTypes(s, list);
    }

    @Override
    public void updateExternalDataManagementSystems(String s, List<ExternalDmsUpdate> list) {
        instance.updateExternalDataManagementSystems(s, list);
    }

    @Override
    public void updatePropertyTypes(String s, List<PropertyTypeUpdate> list) {
        instance.updatePropertyTypes(s, list);
    }

    @Override
    public void updatePlugins(String s, List<PluginUpdate> list) {
        instance.updatePlugins(s, list);
    }

    @Override
    public void updateVocabularies(String s, List<VocabularyUpdate> list) {
        instance.updateVocabularies(s, list);
    }

    @Override
    public void updateVocabularyTerms(String s, List<VocabularyTermUpdate> list) {
        instance.updateVocabularyTerms(s, list);
    }

    @Override
    public void updateTags(String s, List<TagUpdate> list) {
        instance.updateTags(s, list);
    }

    @Override
    public void updateAuthorizationGroups(String s, List<AuthorizationGroupUpdate> list) {
        instance.updateAuthorizationGroups(s, list);
    }

    @Override
    public void updatePersons(String s, List<PersonUpdate> list) {
        instance.updatePersons(s, list);
    }

    @Override
    public void updateOperationExecutions(String s, List<OperationExecutionUpdate> list) {
        instance.updateOperationExecutions(s, list);
    }

    @Override
    public void updateSemanticAnnotations(String s, List<SemanticAnnotationUpdate> list) {
        instance.updateSemanticAnnotations(s, list);
    }

    @Override
    public void updateQueries(String s, List<QueryUpdate> list) {
        instance.updateQueries(s, list);

    }

    @Override
    public Map<IObjectId, Rights> getRights(String s, List<? extends IObjectId> list, RightsFetchOptions rightsFetchOptions) {
        return instance.getRights(s, list, rightsFetchOptions);
    }

    @Override
    public Map<ISpaceId, Space> getSpaces(String s, List<? extends ISpaceId> list, SpaceFetchOptions spaceFetchOptions) {
        return instance.getSpaces(s, list, spaceFetchOptions);
    }

    @Override
    public Map<IProjectId, Project> getProjects(String s, List<? extends IProjectId> list, ProjectFetchOptions projectFetchOptions) {
        return instance.getProjects(s, list, projectFetchOptions);
    }

    @Override
    public Map<IExperimentId, Experiment> getExperiments(String s, List<? extends IExperimentId> list, ExperimentFetchOptions experimentFetchOptions) {
        return instance.getExperiments(s, list, experimentFetchOptions);
    }

    @Override
    public Map<IEntityTypeId, ExperimentType> getExperimentTypes(String s, List<? extends IEntityTypeId> list, ExperimentTypeFetchOptions experimentTypeFetchOptions) {
        return instance.getExperimentTypes(s, list, experimentTypeFetchOptions);
    }

    @Override
    public Map<ISampleId, Sample> getSamples(String s, List<? extends ISampleId> list, SampleFetchOptions sampleFetchOptions) {
        return instance.getSamples(s, list, sampleFetchOptions);
    }

    @Override
    public Map<IEntityTypeId, SampleType> getSampleTypes(String s, List<? extends IEntityTypeId> list, SampleTypeFetchOptions sampleTypeFetchOptions) {
        return instance.getSampleTypes(s, list, sampleTypeFetchOptions);
    }

    @Override
    public Map<IDataSetId, DataSet> getDataSets(String s, List<? extends IDataSetId> list, DataSetFetchOptions dataSetFetchOptions) {
        return instance.getDataSets(s, list, dataSetFetchOptions);
    }

    @Override
    public Map<IEntityTypeId, DataSetType> getDataSetTypes(String s, List<? extends IEntityTypeId> list, DataSetTypeFetchOptions dataSetTypeFetchOptions) {
        return instance.getDataSetTypes(s, list, dataSetTypeFetchOptions);
    }

    @Override
    public Map<IMaterialId, Material> getMaterials(String s, List<? extends IMaterialId> list, MaterialFetchOptions materialFetchOptions) {
        return instance.getMaterials(s, list, materialFetchOptions);
    }

    @Override
    public Map<IEntityTypeId, MaterialType> getMaterialTypes(String s, List<? extends IEntityTypeId> list, MaterialTypeFetchOptions materialTypeFetchOptions) {
        return instance.getMaterialTypes(s, list, materialTypeFetchOptions);
    }

    @Override
    public Map<IPropertyTypeId, PropertyType> getPropertyTypes(String s, List<? extends IPropertyTypeId> list, PropertyTypeFetchOptions propertyTypeFetchOptions) {
        return instance.getPropertyTypes(s, list, propertyTypeFetchOptions);
    }

    @Override
    public Map<IPluginId, Plugin> getPlugins(String s, List<? extends IPluginId> list, PluginFetchOptions pluginFetchOptions) {
        return instance.getPlugins(s, list, pluginFetchOptions);
    }

    @Override
    public Map<IVocabularyId, Vocabulary> getVocabularies(String s, List<? extends IVocabularyId> list, VocabularyFetchOptions vocabularyFetchOptions) {
        return instance.getVocabularies(s, list, vocabularyFetchOptions);
    }

    @Override
    public Map<IVocabularyTermId, VocabularyTerm> getVocabularyTerms(String s, List<? extends IVocabularyTermId> list, VocabularyTermFetchOptions vocabularyTermFetchOptions) {
        return instance.getVocabularyTerms(s, list, vocabularyTermFetchOptions);
    }

    @Override
    public Map<ITagId, Tag> getTags(String s, List<? extends ITagId> list, TagFetchOptions tagFetchOptions) {
        return instance.getTags(s, list, tagFetchOptions);
    }

    @Override
    public Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(String s, List<? extends IAuthorizationGroupId> list, AuthorizationGroupFetchOptions authorizationGroupFetchOptions) {
        return instance.getAuthorizationGroups(s, list, authorizationGroupFetchOptions);
    }

    @Override
    public Map<IRoleAssignmentId, RoleAssignment> getRoleAssignments(String s, List<? extends IRoleAssignmentId> list, RoleAssignmentFetchOptions roleAssignmentFetchOptions) {
        return instance.getRoleAssignments(s, list, roleAssignmentFetchOptions);
    }

    @Override
    public Map<IPersonId, Person> getPersons(String s, List<? extends IPersonId> list, PersonFetchOptions personFetchOptions) {
        return instance.getPersons(s, list, personFetchOptions);
    }

    @Override
    public Map<IExternalDmsId, ExternalDms> getExternalDataManagementSystems(String s, List<? extends IExternalDmsId> list, ExternalDmsFetchOptions externalDmsFetchOptions) {
        return instance.getExternalDataManagementSystems(s, list, externalDmsFetchOptions);
    }

    @Override
    public Map<ISemanticAnnotationId, SemanticAnnotation> getSemanticAnnotations(String s, List<? extends ISemanticAnnotationId> list, SemanticAnnotationFetchOptions semanticAnnotationFetchOptions) {
        return instance.getSemanticAnnotations(s, list, semanticAnnotationFetchOptions);
    }

    @Override
    public Map<IOperationExecutionId, OperationExecution> getOperationExecutions(String s, List<? extends IOperationExecutionId> list, OperationExecutionFetchOptions operationExecutionFetchOptions) {
        return instance.getOperationExecutions(s, list, operationExecutionFetchOptions);
    }

    @Override
    public Map<IQueryId, Query> getQueries(String s, List<? extends IQueryId> list, QueryFetchOptions queryFetchOptions) {
        return instance.getQueries(s, list, queryFetchOptions);
    }

    @Override
    public Map<IQueryDatabaseId, QueryDatabase> getQueryDatabases(String s, List<? extends IQueryDatabaseId> list, QueryDatabaseFetchOptions queryDatabaseFetchOptions) {
        return instance.getQueryDatabases(s, list, queryDatabaseFetchOptions);
    }

    @Override
    public SearchResult<Space> searchSpaces(String s, SpaceSearchCriteria spaceSearchCriteria, SpaceFetchOptions spaceFetchOptions) {
        return instance.searchSpaces(s, spaceSearchCriteria, spaceFetchOptions);
    }

    @Override
    public SearchResult<Project> searchProjects(String s, ProjectSearchCriteria projectSearchCriteria, ProjectFetchOptions projectFetchOptions) {
        return instance.searchProjects(s, projectSearchCriteria, projectFetchOptions);
    }

    @Override
    public SearchResult<Experiment> searchExperiments(String s, ExperimentSearchCriteria experimentSearchCriteria, ExperimentFetchOptions experimentFetchOptions) {
        return instance.searchExperiments(s, experimentSearchCriteria, experimentFetchOptions);
    }

    @Override
    public SearchResult<ExperimentType> searchExperimentTypes(String s, ExperimentTypeSearchCriteria experimentTypeSearchCriteria, ExperimentTypeFetchOptions experimentTypeFetchOptions) {
        return instance.searchExperimentTypes(s, experimentTypeSearchCriteria, experimentTypeFetchOptions);
    }

    @Override
    public SearchResult<Sample> searchSamples(String s, SampleSearchCriteria sampleSearchCriteria, SampleFetchOptions sampleFetchOptions) {
        return instance.searchSamples(s, sampleSearchCriteria, sampleFetchOptions);
    }

    @Override
    public SearchResult<SampleType> searchSampleTypes(String s, SampleTypeSearchCriteria sampleTypeSearchCriteria, SampleTypeFetchOptions sampleTypeFetchOptions) {
        return instance.searchSampleTypes(s, sampleTypeSearchCriteria, sampleTypeFetchOptions);
    }

    @Override
    public SearchResult<DataSet> searchDataSets(String s, DataSetSearchCriteria dataSetSearchCriteria, DataSetFetchOptions dataSetFetchOptions) {
        return instance.searchDataSets(s, dataSetSearchCriteria, dataSetFetchOptions);
    }

    @Override
    public SearchResult<DataSetType> searchDataSetTypes(String s, DataSetTypeSearchCriteria dataSetTypeSearchCriteria, DataSetTypeFetchOptions dataSetTypeFetchOptions) {
        return instance.searchDataSetTypes(s, dataSetTypeSearchCriteria, dataSetTypeFetchOptions);
    }

    @Override
    public SearchResult<Material> searchMaterials(String s, MaterialSearchCriteria materialSearchCriteria, MaterialFetchOptions materialFetchOptions) {
        return instance.searchMaterials(s, materialSearchCriteria, materialFetchOptions);
    }

    @Override
    public SearchResult<ExternalDms> searchExternalDataManagementSystems(String s, ExternalDmsSearchCriteria externalDmsSearchCriteria, ExternalDmsFetchOptions externalDmsFetchOptions) {
        return instance.searchExternalDataManagementSystems(s, externalDmsSearchCriteria, externalDmsFetchOptions);
    }

    @Override
    public SearchResult<MaterialType> searchMaterialTypes(String s, MaterialTypeSearchCriteria materialTypeSearchCriteria, MaterialTypeFetchOptions materialTypeFetchOptions) {
        return instance.searchMaterialTypes(s, materialTypeSearchCriteria, materialTypeFetchOptions);
    }

    @Override
    public SearchResult<Plugin> searchPlugins(String s, PluginSearchCriteria pluginSearchCriteria, PluginFetchOptions pluginFetchOptions) {
        return instance.searchPlugins(s, pluginSearchCriteria, pluginFetchOptions);
    }

    @Override
    public SearchResult<Vocabulary> searchVocabularies(String s, VocabularySearchCriteria vocabularySearchCriteria, VocabularyFetchOptions vocabularyFetchOptions) {
        return instance.searchVocabularies(s, vocabularySearchCriteria, vocabularyFetchOptions);
    }

    @Override
    public SearchResult<VocabularyTerm> searchVocabularyTerms(String s, VocabularyTermSearchCriteria vocabularyTermSearchCriteria, VocabularyTermFetchOptions vocabularyTermFetchOptions) {
        return instance.searchVocabularyTerms(s, vocabularyTermSearchCriteria, vocabularyTermFetchOptions);
    }

    @Override
    public SearchResult<Tag> searchTags(String s, TagSearchCriteria tagSearchCriteria, TagFetchOptions tagFetchOptions) {
        return instance.searchTags(s, tagSearchCriteria, tagFetchOptions);
    }

    @Override
    public SearchResult<AuthorizationGroup> searchAuthorizationGroups(String s, AuthorizationGroupSearchCriteria authorizationGroupSearchCriteria, AuthorizationGroupFetchOptions authorizationGroupFetchOptions) {
        return instance.searchAuthorizationGroups(s, authorizationGroupSearchCriteria, authorizationGroupFetchOptions);
    }

    @Override
    public SearchResult<RoleAssignment> searchRoleAssignments(String s, RoleAssignmentSearchCriteria roleAssignmentSearchCriteria, RoleAssignmentFetchOptions roleAssignmentFetchOptions) {
        return instance.searchRoleAssignments(s, roleAssignmentSearchCriteria, roleAssignmentFetchOptions);
    }

    @Override
    public SearchResult<Person> searchPersons(String s, PersonSearchCriteria personSearchCriteria, PersonFetchOptions personFetchOptions) {
        return instance.searchPersons(s, personSearchCriteria, personFetchOptions);
    }

    @Override
    public SearchResult<CustomASService> searchCustomASServices(String s, CustomASServiceSearchCriteria customASServiceSearchCriteria, CustomASServiceFetchOptions customASServiceFetchOptions) {
        return instance.searchCustomASServices(s, customASServiceSearchCriteria, customASServiceFetchOptions);
    }

    @Override
    public SearchResult<SearchDomainService> searchSearchDomainServices(String s, SearchDomainServiceSearchCriteria searchDomainServiceSearchCriteria, SearchDomainServiceFetchOptions searchDomainServiceFetchOptions) {
        return instance.searchSearchDomainServices(s, searchDomainServiceSearchCriteria, searchDomainServiceFetchOptions);
    }

    @Override
    public SearchResult<AggregationService> searchAggregationServices(String s, AggregationServiceSearchCriteria aggregationServiceSearchCriteria, AggregationServiceFetchOptions aggregationServiceFetchOptions) {
        return instance.searchAggregationServices(s, aggregationServiceSearchCriteria, aggregationServiceFetchOptions);
    }

    @Override
    public SearchResult<ReportingService> searchReportingServices(String s, ReportingServiceSearchCriteria reportingServiceSearchCriteria, ReportingServiceFetchOptions reportingServiceFetchOptions) {
        return instance.searchReportingServices(s, reportingServiceSearchCriteria, reportingServiceFetchOptions);
    }

    @Override
    public SearchResult<ProcessingService> searchProcessingServices(String s, ProcessingServiceSearchCriteria processingServiceSearchCriteria, ProcessingServiceFetchOptions processingServiceFetchOptions) {
        return instance.searchProcessingServices(s, processingServiceSearchCriteria, processingServiceFetchOptions);
    }

    @Override
    public SearchResult<ObjectKindModification> searchObjectKindModifications(String s, ObjectKindModificationSearchCriteria objectKindModificationSearchCriteria, ObjectKindModificationFetchOptions objectKindModificationFetchOptions) {
        return instance.searchObjectKindModifications(s, objectKindModificationSearchCriteria, objectKindModificationFetchOptions);
    }

    @Override
    public SearchResult<GlobalSearchObject> searchGlobally(String s, GlobalSearchCriteria globalSearchCriteria, GlobalSearchObjectFetchOptions globalSearchObjectFetchOptions) {
        return instance.searchGlobally(s, globalSearchCriteria, globalSearchObjectFetchOptions);
    }

    @Override
    public SearchResult<OperationExecution> searchOperationExecutions(String s, OperationExecutionSearchCriteria operationExecutionSearchCriteria, OperationExecutionFetchOptions operationExecutionFetchOptions) {
        return instance.searchOperationExecutions(s, operationExecutionSearchCriteria, operationExecutionFetchOptions);
    }

    @Override
    public SearchResult<DataStore> searchDataStores(String s, DataStoreSearchCriteria dataStoreSearchCriteria, DataStoreFetchOptions dataStoreFetchOptions) {
        return instance.searchDataStores(s, dataStoreSearchCriteria, dataStoreFetchOptions);
    }

    @Override
    public SearchResult<SemanticAnnotation> searchSemanticAnnotations(String s, SemanticAnnotationSearchCriteria semanticAnnotationSearchCriteria, SemanticAnnotationFetchOptions semanticAnnotationFetchOptions) {
        return instance.searchSemanticAnnotations(s, semanticAnnotationSearchCriteria, semanticAnnotationFetchOptions);
    }

    @Override
    public SearchResult<PropertyType> searchPropertyTypes(String s, PropertyTypeSearchCriteria propertyTypeSearchCriteria, PropertyTypeFetchOptions propertyTypeFetchOptions) {
        return instance.searchPropertyTypes(s, propertyTypeSearchCriteria, propertyTypeFetchOptions);
    }

    @Override
    public SearchResult<PropertyAssignment> searchPropertyAssignments(String s, PropertyAssignmentSearchCriteria propertyAssignmentSearchCriteria, PropertyAssignmentFetchOptions propertyAssignmentFetchOptions) {
        return instance.searchPropertyAssignments(s, propertyAssignmentSearchCriteria, propertyAssignmentFetchOptions);
    }

    @Override
    public SearchResult<Query> searchQueries(String s, QuerySearchCriteria querySearchCriteria, QueryFetchOptions queryFetchOptions) {
        return instance.searchQueries(s, querySearchCriteria, queryFetchOptions);
    }

    @Override
    public SearchResult<QueryDatabase> searchQueryDatabases(String s, QueryDatabaseSearchCriteria queryDatabaseSearchCriteria, QueryDatabaseFetchOptions queryDatabaseFetchOptions) {
        return instance.searchQueryDatabases(s, queryDatabaseSearchCriteria, queryDatabaseFetchOptions);
    }

    @Override
    public void deleteSpaces(String s, List<? extends ISpaceId> list, SpaceDeletionOptions spaceDeletionOptions) {
        instance.deleteSpaces(s, list, spaceDeletionOptions);
    }

    @Override
    public void deleteProjects(String s, List<? extends IProjectId> list, ProjectDeletionOptions projectDeletionOptions) {
        instance.deleteProjects(s, list, projectDeletionOptions);
    }

    @Override
    public IDeletionId deleteExperiments(String s, List<? extends IExperimentId> list, ExperimentDeletionOptions experimentDeletionOptions) {
        return instance.deleteExperiments(s, list, experimentDeletionOptions);
    }

    @Override
    public IDeletionId deleteSamples(String s, List<? extends ISampleId> list, SampleDeletionOptions sampleDeletionOptions) {
        return instance.deleteSamples(s, list, sampleDeletionOptions);
    }

    @Override
    public IDeletionId deleteDataSets(String s, List<? extends IDataSetId> list, DataSetDeletionOptions dataSetDeletionOptions) {
        return instance.deleteDataSets(s, list, dataSetDeletionOptions);
    }

    @Override
    public void deleteMaterials(String s, List<? extends IMaterialId> list, MaterialDeletionOptions materialDeletionOptions) {
        instance.deleteMaterials(s, list, materialDeletionOptions);
    }

    @Override
    public void deletePlugins(String s, List<? extends IPluginId> list, PluginDeletionOptions pluginDeletionOptions) {
        instance.deletePlugins(s, list, pluginDeletionOptions);
    }

    @Override
    public void deletePropertyTypes(String s, List<? extends IPropertyTypeId> list, PropertyTypeDeletionOptions propertyTypeDeletionOptions) {
        instance.deletePropertyTypes(s, list, propertyTypeDeletionOptions);
    }

    @Override
    public void deleteVocabularies(String s, List<? extends IVocabularyId> list, VocabularyDeletionOptions vocabularyDeletionOptions) {
        instance.deleteVocabularies(s, list, vocabularyDeletionOptions);
    }

    @Override
    public void deleteVocabularyTerms(String s, List<? extends IVocabularyTermId> list, VocabularyTermDeletionOptions vocabularyTermDeletionOptions) {
        instance.deleteVocabularyTerms(s, list, vocabularyTermDeletionOptions);
    }

    @Override
    public void deleteExperimentTypes(String s, List<? extends IEntityTypeId> list, ExperimentTypeDeletionOptions experimentTypeDeletionOptions) {
        instance.deleteExperimentTypes(s, list, experimentTypeDeletionOptions);
    }

    @Override
    public void deleteSampleTypes(String s, List<? extends IEntityTypeId> list, SampleTypeDeletionOptions sampleTypeDeletionOptions) {
        instance.deleteSampleTypes(s, list, sampleTypeDeletionOptions);
    }

    @Override
    public void deleteDataSetTypes(String s, List<? extends IEntityTypeId> list, DataSetTypeDeletionOptions dataSetTypeDeletionOptions) {
        instance.deleteDataSetTypes(s, list, dataSetTypeDeletionOptions);
    }

    @Override
    public void deleteMaterialTypes(String s, List<? extends IEntityTypeId> list, MaterialTypeDeletionOptions materialTypeDeletionOptions) {
        instance.deleteMaterialTypes(s, list, materialTypeDeletionOptions);
    }

    @Override
    public void deleteExternalDataManagementSystems(String s, List<? extends IExternalDmsId> list, ExternalDmsDeletionOptions externalDmsDeletionOptions) {
        instance.deleteExternalDataManagementSystems(s, list, externalDmsDeletionOptions);
    }

    @Override
    public void deleteTags(String s, List<? extends ITagId> list, TagDeletionOptions tagDeletionOptions) {
        instance.deleteTags(s, list, tagDeletionOptions);
    }

    @Override
    public void deleteAuthorizationGroups(String s, List<? extends IAuthorizationGroupId> list, AuthorizationGroupDeletionOptions authorizationGroupDeletionOptions) {
        instance.deleteAuthorizationGroups(s, list, authorizationGroupDeletionOptions);
    }

    @Override
    public void deleteRoleAssignments(String s, List<? extends IRoleAssignmentId> list, RoleAssignmentDeletionOptions roleAssignmentDeletionOptions) {
        instance.deleteRoleAssignments(s, list, roleAssignmentDeletionOptions);
    }

    @Override
    public void deleteOperationExecutions(String s, List<? extends IOperationExecutionId> list, OperationExecutionDeletionOptions operationExecutionDeletionOptions) {
        instance.deleteOperationExecutions(s, list, operationExecutionDeletionOptions);
    }

    @Override
    public void deleteSemanticAnnotations(String s, List<? extends ISemanticAnnotationId> list, SemanticAnnotationDeletionOptions semanticAnnotationDeletionOptions) {
        instance.deleteSemanticAnnotations(s, list, semanticAnnotationDeletionOptions);
    }

    @Override
    public void deleteQueries(String s, List<? extends IQueryId> list, QueryDeletionOptions queryDeletionOptions) {
        instance.deleteQueries(s, list, queryDeletionOptions);
    }

    @Override
    public void deletePersons(String s, List<? extends IPersonId> list, PersonDeletionOptions personDeletionOptions) {
        instance.deletePersons(s, list, personDeletionOptions);
    }

    @Override
    public SearchResult<Deletion> searchDeletions(String s, DeletionSearchCriteria deletionSearchCriteria, DeletionFetchOptions deletionFetchOptions) {
        return instance.searchDeletions(s, deletionSearchCriteria, deletionFetchOptions);
    }

    @Override
    public void revertDeletions(String s, List<? extends IDeletionId> list) {
        instance.revertDeletions(s, list);
    }

    @Override
    public void confirmDeletions(String s, List<? extends IDeletionId> list) {
        instance.confirmDeletions(s, list);
    }

    @Override
    public Object executeCustomASService(String s, ICustomASServiceId iCustomASServiceId, CustomASServiceExecutionOptions customASServiceExecutionOptions) {
        return instance.executeCustomASService(s, iCustomASServiceId, customASServiceExecutionOptions);
    }

    @Override
    public SearchResult<SearchDomainServiceExecutionResult> executeSearchDomainService(String s, SearchDomainServiceExecutionOptions searchDomainServiceExecutionOptions) {
        return instance.executeSearchDomainService(s, searchDomainServiceExecutionOptions);
    }

    @Override
    public TableModel executeAggregationService(String s, IDssServiceId iDssServiceId, AggregationServiceExecutionOptions aggregationServiceExecutionOptions) {
        return instance.executeAggregationService(s, iDssServiceId, aggregationServiceExecutionOptions);
    }

    @Override
    public TableModel executeReportingService(String s, IDssServiceId iDssServiceId, ReportingServiceExecutionOptions reportingServiceExecutionOptions) {
        return instance.executeReportingService(s, iDssServiceId, reportingServiceExecutionOptions);
    }

    @Override
    public void executeProcessingService(String s, IDssServiceId iDssServiceId, ProcessingServiceExecutionOptions processingServiceExecutionOptions) {
        instance.executeProcessingService(s, iDssServiceId, processingServiceExecutionOptions);
    }

    @Override
    public TableModel executeQuery(String s, IQueryId iQueryId, QueryExecutionOptions queryExecutionOptions) {
        return instance.executeQuery(s, iQueryId, queryExecutionOptions);
    }

    @Override
    public TableModel executeSql(String s, String s1, SqlExecutionOptions sqlExecutionOptions) {
        return instance.executeSql(s, s1, sqlExecutionOptions);
    }

    @Override
    public PluginEvaluationResult evaluatePlugin(String s, PluginEvaluationOptions pluginEvaluationOptions) {
        return instance.evaluatePlugin(s, pluginEvaluationOptions);
    }

    @Override
    public void archiveDataSets(String s, List<? extends IDataSetId> list, DataSetArchiveOptions dataSetArchiveOptions) {
        instance.archiveDataSets(s, list, dataSetArchiveOptions);
    }

    @Override
    public void unarchiveDataSets(String s, List<? extends IDataSetId> list, DataSetUnarchiveOptions dataSetUnarchiveOptions) {
        instance.unarchiveDataSets(s, list, dataSetUnarchiveOptions);
    }

    @Override
    public void lockDataSets(String s, List<? extends IDataSetId> list, DataSetLockOptions dataSetLockOptions) {
        instance.lockDataSets(s, list, dataSetLockOptions);
    }

    @Override
    public void unlockDataSets(String s, List<? extends IDataSetId> list, DataSetUnlockOptions dataSetUnlockOptions) {
        instance.unlockDataSets(s, list, dataSetUnlockOptions);
    }

    @Override
    public IOperationExecutionResults executeOperations(String s, List<? extends IOperation> list, IOperationExecutionOptions iOperationExecutionOptions) {
        return instance.executeOperations(s, list, iOperationExecutionOptions);
    }

    @Override
    public Map<String, String> getServerInformation(String s) {
        return instance.getServerInformation(s);
    }

    @Override
    public List<String> createPermIdStrings(String s, int i) {
        return instance.createPermIdStrings(s, i);
    }

    @Override
    public List<String> createCodes(String s, String s1, EntityKind entityKind, int i) {
        return instance.createCodes(s, s1, entityKind, i);
    }

    @Override
    public int getMajorVersion() {
        return instance.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return instance.getMinorVersion();
    }
}
