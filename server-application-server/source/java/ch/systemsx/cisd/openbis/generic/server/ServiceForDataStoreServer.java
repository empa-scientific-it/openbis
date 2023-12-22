/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.Complete;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.LocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.StorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgressListener;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgressStack;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.IOperationsExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.ListSampleTechIdByIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.utils.ExceptionUtils;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.DummyAuthenticationService;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.api.retry.RetryCaller;
import ch.systemsx.cisd.common.api.retry.config.DefaultRetryConfiguration;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.api.v1.SearchCriteriaToDetailedSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AtomicOperationsPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExistingSampleIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExistingSpaceIdentifierOrProjectPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentPermIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ListSamplesByPropertyPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplesWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdentifierExistingSpacePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPermIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleAugmentedCodeReadWritePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePermIdNullAllowedPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePermIdStringPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleValidator;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.DataSetBatchUpdate;
import ch.systemsx.cisd.openbis.generic.server.batch.DataSetCheckBeforeBatchUpdate;
import ch.systemsx.cisd.openbis.generic.server.batch.SampleBatchRegistration;
import ch.systemsx.cisd.openbis.generic.server.batch.SampleCheckBeforeUpdate;
import ch.systemsx.cisd.openbis.generic.server.batch.SampleUpdate;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationServerManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityObjectIdHelper;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAuthorizationGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMetaprojectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISpaceBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleCodeGeneratorByType;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.experimentlister.ExperimentLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetRegistrationCache;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDataSourceManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMetaprojectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.LogMessagePrefixGenerator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.EntityOperationsState;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMetaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyTypeWithVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityCollectionForCreationOrUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityOperationsLogEntryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewLinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpaceRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.AuthorizationGroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypePropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTypePropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataManagementSystemTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypePropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypePropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;
import ch.systemsx.cisd.openbis.generic.shared.translator.SpaceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
public class ServiceForDataStoreServer extends AbstractCommonServer<IServiceForDataStoreServer>
        implements IServiceForDataStoreServer
{

    @Private
    static final EnumSet<DataSetFetchOption> DATASET_FETCH_OPTIONS_FILE_DATASETS = EnumSet.of(
            DataSetFetchOption.BASIC, DataSetFetchOption.EXPERIMENT, DataSetFetchOption.SAMPLE);

    private final IDAOFactory daoFactory;

    private final IDataStoreServiceFactory dssFactory;

    private final TrustedCrossOriginDomainsProvider trustedOriginDomainProvider;

    private final IETLEntityOperationChecker entityOperationChecker;

    private final ISessionManager<Session> sessionManagerForEntityOperation;

    private final IDataStoreServiceRegistrator dataStoreServiceRegistrator;

    private final IDataStoreDataSourceManager dataSourceManager;

    private IServiceConversationClientManagerLocal conversationClient;

    private IServiceConversationServerManagerLocal conversationServer;

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    @Autowired
    private IOperationsExecutor operationsExecutor;

    @Autowired
    private IConcurrentOperationLimiter operationLimiter;

    private long timeout = 5; // minutes

    public ServiceForDataStoreServer(IAuthenticationService authenticationService,
            IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory boFactory, IDataStoreServiceFactory dssFactory,
            TrustedCrossOriginDomainsProvider trustedOriginDomainProvider,
            IETLEntityOperationChecker entityOperationChecker,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator,
            IDataStoreDataSourceManager dataSourceManager,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this(authenticationService, sessionManager, daoFactory, null, boFactory, dssFactory,
                trustedOriginDomainProvider, entityOperationChecker, dataStoreServiceRegistrator,
                dataSourceManager, new DefaultSessionManager<Session>(new SessionFactory(),
                        new LogMessagePrefixGenerator(), new DummyAuthenticationService(),
                        new RequestContextProviderAdapter(new IRequestContextProvider()
                        {
                            @Override
                            public HttpServletRequest getHttpServletRequest()
                            {
                                return null;
                            }
                        }),
                        30),
                managedPropertyEvaluatorFactory, null, null);
    }

    ServiceForDataStoreServer(IAuthenticationService authenticationService,
            IOpenBisSessionManager sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager, ICommonBusinessObjectFactory boFactory,
            IDataStoreServiceFactory dssFactory,
            TrustedCrossOriginDomainsProvider trustedOriginDomainProvider,
            IETLEntityOperationChecker entityOperationChecker,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator,
            IDataStoreDataSourceManager dataSourceManager,
            ISessionManager<Session> sessionManagerForEntityOperation,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, IOperationsExecutor operationsExecutor,
            IConcurrentOperationLimiter operationLimiter)
    {
        super(authenticationService, sessionManager, daoFactory, propertiesBatchManager, boFactory);
        this.daoFactory = daoFactory;
        this.dssFactory = dssFactory;
        this.trustedOriginDomainProvider = trustedOriginDomainProvider;
        this.entityOperationChecker = entityOperationChecker;
        this.dataStoreServiceRegistrator = dataStoreServiceRegistrator;
        this.dataSourceManager = dataSourceManager;
        this.sessionManagerForEntityOperation = sessionManagerForEntityOperation;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
        this.operationsExecutor = operationsExecutor;
        this.operationLimiter = operationLimiter;
    }

    @Override
    public IServiceForDataStoreServer createLogger(IInvocationLoggerContext context)
    {
        return new ServiceForDataStoreServerLogger(getSessionManager(), context);
    }

    @Override
    public int getVersion()
    {
        return IServer.VERSION;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public DatabaseInstance getHomeDatabaseInstance(final String sessionToken)
    {
        final DatabaseInstance result = new DatabaseInstance();
        result.setCode("CISD");
        result.setHomeDatabase(true);
        result.setId(1L);
        String uuid = getDatabaseInstanceUUID();
        result.setIdentifier(uuid);
        result.setUuid(uuid);
        return result;
    }

    private String getDatabaseInstanceUUID()
    {
        List<DataStorePE> stores = daoFactory.getDataStoreDAO().listDataStores();
        if (stores.size() == 0)
        {
            return UUID.randomUUID().toString().toUpperCase();
        } else
        {
            return stores.get(0).getDatabaseInstanceUUID();
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo info)
    {
        Session session = getSession(sessionToken);

        String dssSessionToken = info.getSessionToken();
        String dssURL = checkVersion(info, session, dssSessionToken);
        IDataStoreDAO dataStoreDAO = daoFactory.getDataStoreDAO();
        DataStorePE dataStore = dataStoreDAO.tryToFindDataStoreByCode(info.getDataStoreCode());
        if (dataStore == null)
        {
            dataStore = new DataStorePE();
            dataStore.setDatabaseInstanceUUID(getDatabaseInstanceUUID());
        }
        dataStore.setCode(info.getDataStoreCode());
        dataStore.setDownloadUrl(info.getDownloadUrl());
        dataStore.setRemoteUrl(dssURL);
        dataStore.setSessionToken(dssSessionToken);
        dataStore.setArchiverConfigured(info.isArchiverConfigured());
        dataStore.setServices(new HashSet<DataStoreServicePE>()); // services will be set by the
                                                                  // dataStoreServiceRegistrator
        // setServices(dataStore, info.getServicesDescriptions(), dataStoreDAO);
        dataStoreDAO.createOrUpdateDataStore(dataStore);
        dataStoreServiceRegistrator.setServiceDescriptions(dataStore,
                info.getServicesDescriptions());
        dataSourceManager.handle(info.getDataStoreCode(), info.getDataSourceDefinitions());

        conversationClient.setDataStoreInformation(dssURL, info.getTimeoutInMinutes());
        conversationServer.setDataStoreInformation(info.getDataStoreCode(), dssURL,
                info.getTimeoutInMinutes());
        CommonServiceProvider.setDataStoreServerApi(dssURL, info.getTimeoutInMinutes());
    }

    private String checkVersion(DataStoreServerInfo info, Session session, String dssSessionToken)
    {
        int port = info.getPort();
        String remoteHost = session.getRemoteHost() + ":" + port;
        String dssURL = (info.isUseSSL() ? "https://" : "http://") + remoteHost;
        checkVersion(dssSessionToken, dssURL);
        return dssURL;
    }

    private void checkVersion(String dssSessionToken, final String dssURL)
    {
        final IDataStoreService service = dssFactory.create(dssURL, timeout * 60 * 1000);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Obtain version of Data Store Server at " + dssURL);
        }

        int dssVersion =
                new RetryCaller<Integer, InvalidAuthenticationException>(DefaultRetryConfiguration.getInstance(), new Log4jSimpleLogger(operationLog))
                {
                    @Override protected Integer call() throws InvalidAuthenticationException
                    {
                        return service.getVersion(dssSessionToken);
                    }
                }.callWithRetry();

        if (IDataStoreService.VERSION != dssVersion)
        {
            String msg =
                    "Data Store Server version is " + dssVersion + " instead of "
                            + IDataStoreService.VERSION;
            notificationLog.error(msg);
            throw new ConfigurationFailureException(msg);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Data Store Server (version " + dssVersion + ") registered for "
                    + dssURL);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public String createPermId(final String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken); // throws exception if invalid sessionToken
        return daoFactory.getPermIdDAO().createPermId();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> createPermIds(String sessionToken, int n) throws UserFailureException
    {
        checkSession(sessionToken);
        return daoFactory.getPermIdDAO().createPermIds(n);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public long drawANewUniqueID(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        return daoFactory.getCodeSequenceDAO().getNextCodeSequenceId();
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) List<ExperimentIdentifier> experimentIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("SessionToken was null");
        }
        if (experimentIdentifiers == null)
        {
            throw new IllegalArgumentException("ExperimentIdentifiers were null");
        }
        if (experimentFetchOptions == null)
        {
            throw new IllegalArgumentException("ExperimentFetchOptions were null");
        }

        checkSession(sessionToken);

        if (experimentFetchOptions.isSubsetOf(ExperimentFetchOption.BASIC,
                ExperimentFetchOption.METAPROJECTS))
        {
            ExperimentLister lister =
                    new ExperimentLister(getDAOFactory(), getSession(sessionToken)
                            .getBaseIndexURL());
            return lister.listExperiments(experimentIdentifiers, experimentFetchOptions);
        } else
        {
            List<Experiment> experiments = new ArrayList<Experiment>();
            for (ExperimentIdentifier experimentIdentifier : experimentIdentifiers)
            {
                Experiment experiment = tryGetExperiment(sessionToken, experimentIdentifier);
                if (experiment != null)
                {
                    experiment.setFetchOptions(new ExperimentFetchOptions(ExperimentFetchOption
                            .values()));
                    experiments.add(experiment);
                }
            }
            return experiments;
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperimentsForProjects(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) List<ProjectIdentifier> projectIdentifiers,
            ExperimentFetchOptions experimentFetchOptions)
    {
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("SessionToken was null");
        }
        if (projectIdentifiers == null)
        {
            throw new IllegalArgumentException("ProjectIdentifiers were null");
        }
        if (experimentFetchOptions == null)
        {
            throw new IllegalArgumentException("ExperimentFetchOptions were null");
        }

        checkSession(sessionToken);

        if (experimentFetchOptions.isSubsetOf(ExperimentFetchOption.BASIC,
                ExperimentFetchOption.METAPROJECTS))
        {
            ExperimentLister lister =
                    new ExperimentLister(daoFactory, getSession(sessionToken).getBaseIndexURL());
            return lister.listExperimentsForProjects(projectIdentifiers, experimentFetchOptions);
        } else
        {
            List<Experiment> experiments = new ArrayList<Experiment>();
            for (ProjectIdentifier projectIdentifier : projectIdentifiers)
            {
                List<Experiment> projectExperiments =
                        listExperiments(sessionToken, projectIdentifier);
                if (projectExperiments != null)
                {
                    for (Experiment projectExperiment : projectExperiments)
                    {
                        if (projectExperiment != null)
                        {
                            projectExperiment.setFetchOptions(new ExperimentFetchOptions(
                                    ExperimentFetchOption.values()));
                            experiments.add(projectExperiment);
                        }
                    }

                }
            }
            return experiments;
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Experiment tryGetExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierExistingSpacePredicate.class) ExperimentIdentifier experimentIdentifier)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experimentIdentifier != null : "Unspecified experiment identifier.";

        final Session session = getSession(sessionToken);
        ExperimentPE experiment = tryLoadExperimentByIdentifier(session, experimentIdentifier);
        return translateExperimentWithMetaprojects(session, experiment);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Experiment tryGetExperimentByPermId(String sessionToken, @AuthorizationGuard(guardClass = ExperimentPermIdPredicate.class) PermId permId)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert permId != null : "Unspecified experiment perm id.";

        final Session session = getSession(sessionToken);
        ExperimentPE experiment = tryLoadExperimentByPermId(session, permId.getId());
        return translateExperimentWithMetaprojects(session, experiment);
    }

    private Experiment translateExperimentWithMetaprojects(final Session session, ExperimentPE experiment)
    {
        if (experiment == null)
        {
            return null;
        }
        enrichWithProperties(experiment);

        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), experiment);

        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojectPEs), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()),
                LoadableFields.PROPERTIES);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> listSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class)
            final ListSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(new ListOrSearchSampleCriteria(criteria));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample tryGetSampleWithExperiment(final String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSampleIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        SamplePE sample = tryLoadSample(session, sampleIdentifier);
        return translateSampleWithMetaProjects(session, sample);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample tryGetSampleByPermId(String sessionToken, @AuthorizationGuard(guardClass = SamplePermIdNullAllowedPredicate.class) PermId permId)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";

        final Session session = getSession(sessionToken);
        SamplePE sample = tryLoadSample(session, permId.getId());
        return translateSampleWithMetaProjects(session, sample);
    }

    private Sample translateSampleWithMetaProjects(final Session session, SamplePE sample)
    {
        Collection<MetaprojectPE> metaprojects = Collections.emptySet();
        if (sample != null)
        {
            HibernateUtils.initialize(sample.getProperties());
            enrichWithProperties(sample.getExperiment());
            metaprojects =
                    getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                            session.tryGetPerson(), sample);
        }
        return SampleTranslator.translate(sample, session.getBaseIndexURL(), true, true,
                MetaprojectTranslator.translate(metaprojects), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public SampleIdentifier tryGetSampleIdentifier(String sessionToken,
            @AuthorizationGuard(guardClass = SamplePermIdStringPredicate.class) String samplePermID)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert samplePermID != null : "Unspecified sample perm ID.";

        final SamplePE sample = daoFactory.getSampleDAO().tryToFindByPermID(samplePermID);
        return (sample == null) ? null : sample.getSampleIdentifier();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Map<String, SampleIdentifier> listSamplesByPermId(final String sessionToken,
            @AuthorizationGuard(guardClass = SamplePermIdStringPredicate.class) List<String> samplePermIds)
    {
        List<SamplePE> samples =
                daoFactory.getSampleDAO().listByPermID(new HashSet<String>(samplePermIds));

        Map<String, SampleIdentifier> map = new HashMap<String, SampleIdentifier>();

        for (SamplePE sample : samples)
        {
            map.put(sample.getPermId(), sample.getSampleIdentifier());
        }

        return map;
    }

    private ExperimentPE tryLoadExperimentByIdentifier(final Session session,
            ExperimentIdentifier experimentIdentifier)
    {
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        return experimentBO.tryFindByExperimentIdentifier(experimentIdentifier);
    }

    private ExperimentPE tryLoadExperimentByPermId(final Session session,
            String permId)
    {
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        return experimentBO.tryFindByExperimentId(new ExperimentPermIdId(permId));
    }

    private SamplePE tryLoadSample(final Session session, SampleIdentifier sampleIdentifier)
    {
        SamplePE result = null;
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        try
        {
            sampleBO.tryToLoadBySampleIdentifier(sampleIdentifier);
            result = sampleBO.tryToGetSample();
        } catch (UserFailureException ufe)
        {
            // sample does not exist
        }
        return result;
    }

    private SamplePE tryLoadSample(final Session session, String permId)
    {
        SamplePE result = null;
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        try
        {
            sampleBO.tryToLoadBySamplePermId(permId);
            result = sampleBO.tryToGetSample();
        } catch (UserFailureException ufe)
        {
            // sample does not exist
        }
        return result;
    }

    private void enrichWithProperties(ExperimentPE experiment)
    {
        if (experiment == null)
        {
            return;
        }
        HibernateUtils.initialize(experiment.getProperties());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public ExperimentType getExperimentType(String sessionToken, String experimentTypeCode)
            throws UserFailureException
    {
        checkSession(sessionToken);

        IEntityTypeDAO entityTypeDAO = getDAOFactory().getEntityTypeDAO(EntityKind.EXPERIMENT);
        EntityTypePE entityType = entityTypeDAO.tryToFindEntityTypeByCode(experimentTypeCode);
        if (entityType == null)
        {
            throw new UserFailureException("No Experiment type found with code '"
                    + experimentTypeCode + "'.");
        }
        assert entityType instanceof ExperimentTypePE : "Not an ExperimentTypePE: " + entityType;
        ExperimentTypePE experimentType = (ExperimentTypePE) entityType;
        HibernateUtils.initialize(experimentType.getExperimentTypePropertyTypes());
        return ExperimentTypeTranslator.translate(experimentType, null, null);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException
    {
        checkSession(sessionToken);

        ISampleTypeDAO sampleTypeDAO = getDAOFactory().getSampleTypeDAO();
        SampleTypePE sampleType = sampleTypeDAO.tryFindSampleTypeByCode(sampleTypeCode);
        if (sampleType == null)
        {
            throw new UserFailureException("No sample type found with code '" + sampleTypeCode
                    + "'.");
        }
        HibernateUtils.initialize(sampleType.getSampleTypePropertyTypes());
        return SampleTypeTranslator.translate(sampleType, null, null);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public DataSetTypeWithVocabularyTerms getDataSetType(String sessionToken, String dataSetTypeCode)
            throws UserFailureException
    {
        checkSession(sessionToken);

        IDataSetTypeDAO dataSetTypeDAO = getDAOFactory().getDataSetTypeDAO();
        DataSetTypePE dataSetType = dataSetTypeDAO.tryToFindDataSetTypeByCode(dataSetTypeCode);
        if (dataSetType == null)
        {
            throw new UserFailureException("No data set type found with code '" + dataSetTypeCode
                    + "'.");
        }
        Set<DataSetTypePropertyTypePE> dataSetTypePropertyTypes =
                dataSetType.getDataSetTypePropertyTypes();
        HibernateUtils.initialize(dataSetTypePropertyTypes);
        DataSetTypeWithVocabularyTerms result = new DataSetTypeWithVocabularyTerms();
        result.setDataSetType(DataSetTypeTranslator.translate(dataSetType, null, null));
        for (DataSetTypePropertyTypePE dataSetTypePropertyTypePE : dataSetTypePropertyTypes)
        {
            PropertyTypePE propertyTypePE = dataSetTypePropertyTypePE.getPropertyType();
            PropertyTypeWithVocabulary propertyType = new PropertyTypeWithVocabulary();
            propertyType.setCode(propertyTypePE.getCode());
            VocabularyPE vocabulary = propertyTypePE.getVocabulary();
            if (vocabulary != null)
            {
                Set<VocabularyTermPE> terms = vocabulary.getTerms();
                HibernateUtils.initialize(terms);
                propertyType.setTerms(VocabularyTermTranslator.translateTerms(terms));
            }
            result.addPropertyType(propertyType);
        }
        return result;
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<AbstractExternalData> listDataSetsByExperimentID(final String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            final TechId experimentID) throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IDatasetLister datasetLister = createDatasetLister(session);
        List<AbstractExternalData> datasets =
                datasetLister.listByExperimentTechId(experimentID, true);
        Collections.sort(datasets);
        return datasets;
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<AbstractExternalData> listDataSetsBySampleID(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class)
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<AbstractExternalData> datasets =
                datasetLister.listBySampleTechId(sampleId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<AbstractExternalData> listDataSetsByCode(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        return datasetLister.listByDatasetCode(dataSetCodes);
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = ProjectValidator.class)
    public List<Project> listProjects(String sessionToken)
    {
        checkSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return ProjectTranslator.translate(projects);
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Experiment> listExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IExperimentTable experimentTable =
                businessObjectFactory.createExperimentTable(session);
        experimentTable.load(EntityType.ALL_TYPES_CODE, projectIdentifier);
        final List<ExperimentPE> experiments = experimentTable.getExperiments();
        return translateExperimentsWithMetaprojectAssignments(session, experiments);
    }

    private List<Experiment> translateExperimentsWithMetaprojectAssignments(final Session session, final List<ExperimentPE> experiments)
    {
        final Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectAssignmentsForEntities(
                        session.tryGetPerson(), experiments, EntityKind.EXPERIMENT);
        Map<Long, Set<Metaproject>> assignments =
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs);
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL(), assignments,
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public IEntityProperty[] tryGetPropertiesOfTopSample(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        SamplePE sample = sampleBO.getSample();
        if (sample == null)
        {
            return null;
        }
        SamplePE top = sample.getTop();
        Set<SamplePropertyPE> properties = top.getProperties();
        HibernateUtils.initialize(properties);
        return EntityPropertyTranslator.translate(properties.toArray(new SamplePropertyPE[0]),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>(),
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public IEntityProperty[] tryGetPropertiesOfSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleIdentifierPredicate.class) SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadBySampleIdentifier(sampleIdentifier);
        SamplePE sample = sampleBO.getSample();
        if (sample == null)
        {
            return null;
        }
        Set<SamplePropertyPE> properties = sample.getProperties();
        HibernateUtils.initialize(properties);
        return EntityPropertyTranslator.translate(properties.toArray(new SamplePropertyPE[0]),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>(),
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerEntities(String sessionToken, EntityCollectionForCreationOrUpdate collection)
            throws UserFailureException
    {
        Session session = getSession(sessionToken);

        for (NewExperiment experiment : collection.getNewExperiments())
        {
            registerExperiment(session, experiment);
        }

        DataSetRegistrationCache cache = new DataSetRegistrationCache();
        for (NewExternalData dataSet : collection.getNewDataSets())
        {
            registerDataSetInternal(session, dataSet, cache);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public long registerExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class) NewExperiment experiment) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experiment != null : "Unspecified new example.";

        final Session session = getSession(sessionToken);
        return registerExperiment(session, experiment);
    }

    private long registerExperiment(final Session session, NewExperiment experiment)
    {
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.define(experiment);
        experimentBO.save();
        return experimentBO.getExperiment().getId();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class)
            final List<NewSamplesWithTypes> newSamplesWithType, String userIDOrNull)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        PersonPE registratorOrNull =
                userIDOrNull != null ? getOrCreatePerson(sessionToken, userIDOrNull) : null;
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            registerSamples(session, samples, registratorOrNull);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public long registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, String userIDOrNull) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        final Session session = getSession(sessionToken);
        SamplePE samplePE = registerSampleInternal(session, newSample, userIDOrNull);
        return samplePE.getId();
    }

    private PersonPE getOrCreatePerson(String sessionToken, String userID)
    {
        PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userID);
        if (person != null)
        {
            return person;
        }
        List<PersonPE> persons = registerPersons(sessionToken, Collections.singletonList(userID));
        return persons.get(0);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class) SampleUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        updateSampleInternal(updates, session);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleIdentifierPredicate.class)
            final SampleIdentifier sampleIdentifier, final NewExternalData externalData)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleIdentifier != null : "Unspecified sample identifier.";

        final Session session = getSession(sessionToken);
        registerDataSetInternal(session, sampleIdentifier, externalData, new DataSetRegistrationCache());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void registerDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            final ExperimentIdentifier experimentIdentifier, final NewExternalData externalData)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert experimentIdentifier != null : "Unspecified experiment identifier.";

        final Session session = getSession(sessionToken);
        registerDataSetInternal(session, experimentIdentifier, externalData, new DataSetRegistrationCache());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            final SpaceIdentifier identifier) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.addPropertiesToDataSet(dataSetCode, properties);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public boolean isDataSetOnTrashCanOrDeleted(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode)
    {
        // Check if dataset is available for retrieval
        DataPE dataSet = getDAOFactory().getDataDAO().tryToFindFullDataSetByCode(dataSetCode, false, false);
        boolean isDataSetAvailable = dataSet != null;
        // Check if the dataset is on the table, since can't be retrieved is on the trashcan
        boolean isDataSetOnTrashCan = getDAOFactory().getDataDAO().exists(dataSetCode);
        // Check if the dataset is finally deleted
        boolean isDataSetDeleted = getDAOFactory().getEventDAO().tryFind(
                dataSetCode,
                ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType.DATASET,
                ch.systemsx.cisd.openbis.generic.shared.dto.EventType.DELETION) != null;

        return !isDataSetAvailable && (isDataSetOnTrashCan || isDataSetDeleted);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateShareIdAndSize(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode, String shareId, long size) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        IDataDAO dataSetDAO = getDAOFactory().getDataDAO();

        DataPE dataSet = dataSetDAO.tryToFindFullDataSetByCode(dataSetCode, false, false);
        if (dataSet == null) // Dataset is not available for retrieval
        {
            // Check if the dataset is on the table, since can't be retrieved is on the trashcan
            boolean isDataSetOnTrashCan = getDAOFactory().getDataDAO().exists(dataSetCode);
            // Check if the dataset is finally deleted
            boolean isDataSetDeleted = getDAOFactory().getEventDAO().tryFind(
                    dataSetCode,
                    ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType.DATASET,
                    ch.systemsx.cisd.openbis.generic.shared.dto.EventType.DELETION) != null;

            if (isDataSetOnTrashCan)
            {
                operationLog.info("The data set has been moved to the trashcan and the share will not be updated: " + dataSetCode);
                return;
            } else if (isDataSetDeleted)
            {
                operationLog.info("The data set has been deleted and the share will not be updated: " + dataSetCode);
                return;
            } else
            {
                throw new UserFailureException("Unknown data set, that has not been deleted, check for storage errors: " + dataSetCode);
            }
        }
        ExternalDataPE externalData = dataSet.tryAsExternalData();
        if (externalData == null)
        {
            throw new UserFailureException("Can't update share id and size of a virtual data set: "
                    + dataSetCode);
        }
        // data sets consisting out of empty folders have a size of 0,
        // but we want the size of a data set to be strictly positive
        long positiveSize = Math.max(1, size);
        externalData.setShareId(shareId);
        externalData.setSize(positiveSize);
        dataSetDAO.updateDataSet(dataSet, session.tryGetPerson());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateDataSetStatuses(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes, final DataSetArchivingStatus newStatus,
            boolean presentInArchive) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.updateStatuses(dataSetCodes, newStatus, presentInArchive);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public boolean compareAndSetDataSetStatus(String sessionToken, String dataSetCode,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(dataSetCode);
        return dataBO.compareAndSetDataSetStatus(oldStatus, newStatus, newPresentInArchive);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public int archiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes,
            boolean removeFromDataStore, Map<String, String> options)
    {
        return super.archiveDatasets(sessionToken, datasetCodes, removeFromDataStore, options);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public int unarchiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        return super.unarchiveDatasets(sessionToken, datasetCodes);
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public IDatasetLocationNode tryGetDataSetLocation(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = getSession(sessionToken);
        IDatasetLister lister = businessObjectFactory.createDatasetLister(session);
        return lister.listLocationsByDatasetCode(dataSetCode);
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public AbstractExternalData tryGetLocalDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode,
            String dataStore) throws UserFailureException
    {
        AbstractExternalData dataSet = tryGetDataSet(sessionToken, dataSetCode);
        if (dataSet != null && dataSet.getDataStore().getCode().equals(dataStore))
        {
            return dataSet;
        }
        return null;
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public AbstractExternalData tryGetDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = getSession(sessionToken); // assert authenticated

        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(dataSetCode);
        dataBO.enrichWithParentsAndExperiment();
        dataBO.enrichWithChildren();
        dataBO.enrichWithProperties();
        dataBO.enrichWithContainedDataSets();
        DataPE dataPE = dataBO.tryGetData();
        if (null == dataPE)
        {
            return null;
        }
        Collection<MetaprojectPE> metaprojects =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), dataPE);
        return DataSetTranslator.translate(dataPE, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojects), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public AbstractExternalData tryGetThinDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert dataSetCode != null : "Unspecified data set code.";

        Session session = getSession(sessionToken); // assert authenticated

        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(dataSetCode);
        DataPE dataPE = dataBO.tryGetData();
        if (null == dataPE)
        {
            return null;
        }
        return DataSetTranslator.translate(dataPE, session.getBaseIndexURL(),
                Collections.<Metaproject> emptyList(), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    public void checkProjectPowerUserAuthorization(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public void checkDataSetAccess(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode) throws UserFailureException
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public void checkDataSetCollectionAccess(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes)
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void checkSpaceAccess(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) SpaceIdentifier spaceId)
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void checkExperimentAccess(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class) String experimentIdentifier)
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void checkSampleAccess(String sessionToken,
            @AuthorizationGuard(guardClass = SampleAugmentedCodeReadWritePredicate.class) String sampleIdentifier)
    {
        checkSession(sessionToken);
        // do nothing, the access rights specified in method annotations are checked by a proxy
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> listSamplesByCriteria(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSamplesByPropertyPredicate.class)
            final ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert criteria != null : "Unspecified criteria.";

        Session session = getSession(sessionToken);
        ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        List<SamplePE> samples = sampleTable.getSamples();

        final Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectAssignmentsForEntities(
                        session.tryGetPerson(), samples, EntityKind.SAMPLE);
        Map<Long, Set<Metaproject>> assignments =
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs);

        return SampleTranslator
                .translate(samples, "", assignments, managedPropertyEvaluatorFactory,
                        new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<DataSetShareId> listShareIds(final String sessionToken, String dataStoreCode)
            throws UserFailureException
    {
        Session session = getSession(sessionToken);
        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        return datasetLister.listAllDataSetShareIdsByDataStore(dataStore.getId());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listPhysicalDataSets(final String sessionToken,
            String dataStoreCode) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        final IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        final List<AbstractExternalData> dataSets =
                datasetLister.listByDataStore(dataStore.getId(),
                        DATASET_FETCH_OPTIONS_FILE_DATASETS);
        return SimpleDataSetHelper.filterAndTranslate(dataSets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(String sessionToken,
            String dataStoreCode, int limit) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        final IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        final List<AbstractExternalData> dataSets =
                datasetLister.listByDataStore(dataStore.getId(), limit,
                        DATASET_FETCH_OPTIONS_FILE_DATASETS);
        return SimpleDataSetHelper.filterAndTranslate(dataSets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listOldestPhysicalDataSets(String sessionToken,
            String dataStoreCode, Date youngerThan, int limit) throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        final IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        final List<AbstractExternalData> dataSets =
                datasetLister.listByDataStore(dataStore.getId(), youngerThan, limit,
                        DATASET_FETCH_OPTIONS_FILE_DATASETS);
        return SimpleDataSetHelper.filterAndTranslate(dataSets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsByArchivingStatus(String sessionToken, String dataStoreCode,
            DataSetArchivingStatus archivingStatus, Boolean presentInArchive)
    {
        final Session session = getSession(sessionToken);
        final DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        final IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        final List<AbstractExternalData> dataSets =
                datasetLister.listByArchivingStatus(dataStore.getId(), archivingStatus, presentInArchive,
                        DATASET_FETCH_OPTIONS_FILE_DATASETS);
        return SimpleDataSetHelper.filterAndTranslate(dataSets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<SimpleDataSetInformationDTO> listPhysicalDataSetsWithUnknownSize(String sessionToken, String dataStoreCode, int chunkSize,
            String dataSetCodeLowerLimit)
    {
        final Session session = getSession(sessionToken);
        final DataStorePE dataStore = loadDataStore(session, dataStoreCode);
        final IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        final List<AbstractExternalData> dataSets =
                datasetLister.listByDataStoreWithUnknownSize(dataStore.getId(), chunkSize, dataSetCodeLowerLimit,
                        DATASET_FETCH_OPTIONS_FILE_DATASETS);
        return SimpleDataSetHelper.filterAndTranslate(dataSets);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updatePhysicalDataSetsSize(String sessionToken, Map<String, Long> sizeMap)
    {
        assert sessionToken != null : "Unspecified session token.";
        final Session session = getSession(sessionToken);
        final IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.updateSizes(sizeMap);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<AbstractExternalData> listAvailableDataSets(String sessionToken,
            String dataStoreCode, ArchiverDataSetCriteria criteria)
    {
        Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        return datasetLister.listByArchiverCriteria(dataStoreCode, criteria);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<AbstractExternalData> listDataSets(String sessionToken, String dataStoreCode,
            TrackingDataSetCriteria criteria)
    {
        Session session = getSession(sessionToken);
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException("Unknown data store: " + dataStoreCode);
        }
        final IDatasetLister datasetLister = createDatasetLister(session);
        List<AbstractExternalData> allDataSets = datasetLister.listByTrackingCriteria(criteria);
        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData externalData : allDataSets)
        {
            if (dataStoreCode.equals(externalData.getDataStore().getCode()))
            {
                result.add(externalData);
            }
        }
        return result;
    }

    private DataStorePE loadDataStore(Session session, String dataStoreCode)
    {
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException(String.format("Unknown data store '%s'", dataStoreCode));
        }
        return dataStore;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull, Date maxDeletionDataOrNull)
    {
        checkSession(sessionToken);
        return getDAOFactory().getEventDAO().listDeletedDataSets(lastSeenDeletionEventIdOrNull,
                maxDeletionDataOrNull);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Collection<VocabularyTerm> listVocabularyTerms(String sessionToken, String vocabularyCode)
            throws UserFailureException
    {
        checkSession(sessionToken);
        VocabularyPE vocabularyOrNull =
                getDAOFactory().getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);
        if (vocabularyOrNull == null)
        {
            throw new UserFailureException(String.format("Vocabulary '%s' not found",
                    vocabularyCode));
        }
        return VocabularyTermTranslator.translateTerms(vocabularyOrNull.getTerms());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Vocabulary tryGetVocabulary(String sessionToken, String vocabularyCode)
    {
        checkSession(sessionToken);
        VocabularyPE vocabularyOrNull =
                getDAOFactory().getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);

        if (vocabularyOrNull == null)
        {
            return null;
        } else
        {
            return VocabularyTranslator.translate(vocabularyOrNull, true);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> generateCodes(String sessionToken, String prefix,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind, int number)
    {
        checkSession(sessionToken);
        EntityCodeGenerator generator = new EntityCodeGenerator(daoFactory);
        if (ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.SAMPLE.equals(entityKind))
        {
            Properties serviceProperties = configurer.getResolvedProps();
            if (PropertyUtils.getBoolean(serviceProperties, Constants.CREATE_CONTINUOUS_SAMPLES_CODES_KEY, false))
            {
                generator = new SampleCodeGeneratorByType(daoFactory);
            }
        }
        return generator.generateCodes(prefix, entityKind, number);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Person> listAdministrators(String sessionToken)
    {
        checkSession(sessionToken);
        // Get all Persons in the DB
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();

        // Filter down to the admins
        ArrayList<PersonPE> admins = new ArrayList<PersonPE>();
        for (PersonPE person : persons)
        {
            for (final RoleAssignmentPE roleAssigment : person.getRoleAssignments())
            {
                if (roleAssigment.getRoleWithHierarchy().isInstanceLevel()
                        && roleAssigment.getRole().equals(RoleCode.ADMIN))
                {
                    admins.add(person);
                }
            }
        }
        Collections.sort(admins);
        return PersonTranslator.translate(admins);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Person tryPersonWithUserIdOrEmail(String sessionToken, String useridOrEmail)
    {
        checkSession(sessionToken);

        PersonPE personPE = tryFindPersonForUserIdOrEmail(useridOrEmail);
        return (null != personPE) ? PersonTranslator.translate(personPE) : null;
    }

    private PersonPE tryFindPersonForUserIdOrEmail(String userIdOrEmail)
    {
        if (userIdOrEmail == null)
        {
            return null;
        }

        // First search for a userId match
        IPersonDAO personDao = getDAOFactory().getPersonDAO();
        PersonPE person = personDao.tryFindPersonByUserId(userIdOrEmail);
        if (null != person)
        {
            return person;
        }
        // Didn't find one -- try email
        return personDao.tryFindPersonByEmail(userIdOrEmail);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample registerSampleAndDataSet(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            final NewSample newSample, final NewExternalData externalData, String userIdOrNull)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert newSample != null : "Unspecified new sample.";

        // Register the Sample
        final Session session = getSession(sessionToken);
        SamplePE samplePE = registerSampleInternal(session, newSample, userIdOrNull);

        // Register the data set
        registerDataSetInternal(getSession(sessionToken), samplePE, externalData, new DataSetRegistrationCache());
        Sample result =
                SampleTranslator.translate(samplePE, session.getBaseIndexURL(), null,
                        managedPropertyEvaluatorFactory,
                        new SamplePropertyAccessValidator(session, getDAOFactory()));
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Sample updateSampleAndRegisterDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class) SampleUpdatesDTO updates, NewExternalData externalData)
    {
        final Session session = getSession(sessionToken);

        // Update the sample
        final ISampleBO sampleBO = updateSampleInternal(updates, session);

        // Register the data set
        final SamplePE samplePE = sampleBO.getSample();
        registerDataSetInternal(getSession(sessionToken), samplePE, externalData, new DataSetRegistrationCache());

        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), samplePE);

        Sample result =
                SampleTranslator.translate(samplePE, session.getBaseIndexURL(),
                        MetaprojectTranslator.translate(metaprojectPEs),
                        managedPropertyEvaluatorFactory,
                        new SamplePropertyAccessValidator(session, getDAOFactory()));
        return result;
    }

    private ISampleBO updateSampleInternal(SampleUpdatesDTO updates, final Session session)
    {
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.update(updates);
        sampleBO.save();

        return sampleBO;
    }

    private SamplePE registerSampleInternal(Session session, NewSample newSample,
            String userIdOrNull)
    {
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.define(newSample);
        if (userIdOrNull != null)
        {
            sampleBO.getSample().setRegistrator(
                    getOrCreatePerson(session.getSessionToken(), userIdOrNull));
        }
        sampleBO.save();
        SamplePE samplePE = sampleBO.getSample();
        return samplePE;
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Space tryGetSpace(String sessionToken,
            @AuthorizationGuard(guardClass = ExistingSpaceIdentifierOrProjectPredicate.class) SpaceIdentifier spaceIdentifier)
    {

        Session session = getSession(sessionToken);
        ISpaceBO spaceBO = businessObjectFactory.createSpaceBO(session);
        SpaceIdentifier identifier =
                new SpaceIdentifier(spaceIdentifier.getSpaceCode());
        try
        {
            spaceBO.load(identifier);
            return SpaceTranslator.translate(spaceBO.getSpace());
        } catch (UserFailureException ufe)
        {
            // space does not exist
            return null;
        }
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Project tryGetProject(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierExistingSpacePredicate.class) ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        try
        {
            bo.loadByProjectIdentifier(projectIdentifier);
            final ProjectPE project = bo.getProject();
            return ProjectTranslator.translate(project);
        } catch (UserFailureException ufe)
        {
            // project does not exist
            return null;
        }
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.SPACE_ETL_SERVER })
    public Project tryGetProjectByPermId(String sessionToken, @AuthorizationGuard(guardClass = ProjectPermIdPredicate.class) PermId permId)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        try
        {
            bo.loadByPermId(permId.getId());
            final ProjectPE project = bo.getProject();
            return ProjectTranslator.translate(project);
        } catch (UserFailureException ufe)
        {
            // project does not exist
            return null;
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Material tryGetMaterial(String sessionToken, MaterialIdentifier materialIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO bo = businessObjectFactory.createMaterialBO(session);
        try
        {
            bo.loadByMaterialIdentifier(materialIdentifier);
            bo.enrichWithProperties();
            MaterialPE materialPE = bo.getMaterial();
            Collection<MetaprojectPE> metaprojectPEs = Collections.emptySet();
            if (materialPE != null)
            {
                metaprojectPEs =
                        getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                                session.tryGetPerson(), materialPE);
            }
            return MaterialTranslator.translate(materialPE,
                    MetaprojectTranslator.translate(metaprojectPEs),
                    managedPropertyEvaluatorFactory,
                    new SamplePropertyAccessValidator(session, getDAOFactory()));
        } catch (UserFailureException ufe)
        {
            // material does not exist
            return null;
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Metaproject tryGetMetaproject(String sessionToken, String name, String ownerId)
    {
        final Session session = getSession(sessionToken);
        final IMetaprojectBO bo = businessObjectFactory.createMetaprojectBO(session);

        MetaprojectPE pe = bo.tryFindByMetaprojectId(new MetaprojectIdentifierId(ownerId, name));

        if (pe == null)
        {
            return null;
        } else
        {
            return MetaprojectTranslator.translate(pe);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            @AuthorizationGuard(guardClass = AtomicOperationsPredicate.class) AtomicEntityOperationDetails operationDetails)
    {
        IServiceConversationProgressListener progressListener =
                ServiceConversationsThreadContext.getProgressListener();

        TechId registrationId = operationDetails.getRegistrationIdOrNull();

        EntityOperationsInProgress.getInstance().addRegistrationPending(registrationId);

        String sessionTokenForEntityOperation = null;
        try
        {
            final Session session = getSession(sessionToken);
            final String userId = operationDetails.tryUserIdOrNull();
            boolean authorize = (userId != null);
            Session sessionForEntityOperation = session;
            if (authorize)
            {
                sessionTokenForEntityOperation =
                        sessionManagerForEntityOperation.tryToOpenSession(userId, "dummy password");
                sessionForEntityOperation =
                        sessionManagerForEntityOperation.getSession(sessionTokenForEntityOperation);
                injectPerson(sessionForEntityOperation, userId);
                sessionForEntityOperation.setCreatorPerson(session.tryGetPerson());
            }

            long spacesCreated =
                    createSpaces(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long materialsCreated =
                    createMaterials(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long projectsCreated =
                    createProjects(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long projectsUpdated =
                    updateProjects(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long vocabulariesUpdated =
                    updateVocabularies(session, sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long experimentsCreated =
                    createExperiments(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long experimentsUpdates =
                    updateExperiments(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long samplesCreated =
                    createSamples(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long samplesUpdated =
                    updateSamples(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long dataSetsCreated = createDataSetsV3(sessionForEntityOperation, operationDetails, progressListener, authorize);

            long dataSetsUpdated =
                    updateDataSets(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long materialsUpdates =
                    updateMaterials(sessionForEntityOperation, operationDetails, progressListener,
                            authorize);

            long metaprojectsCreated =
                    createMetaprojects(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long metaprojectsUpdates =
                    updateMetaprojects(sessionForEntityOperation, operationDetails,
                            progressListener, authorize);

            long spaceRolesAssigned = assignSpaceRoles(sessionForEntityOperation, operationDetails, progressListener, authorize);

            long spaceRolesRevoked = revokeSpaceRoles(sessionForEntityOperation, operationDetails, progressListener, authorize);

            // If the id is not null, the caller wants to persist the fact that the operation was
            // invoked and completed;
            // if the id is null, the caller does not care.
            if (null != registrationId)
            {
                daoFactory.getEntityOperationsLogDAO().addLogEntry(registrationId.getId());
            }

            return new AtomicEntityOperationResult(spacesCreated, projectsCreated, projectsUpdated,
                    materialsCreated, materialsUpdates, experimentsCreated, experimentsUpdates,
                    samplesCreated, samplesUpdated, dataSetsCreated, dataSetsUpdated,
                    metaprojectsCreated, metaprojectsUpdates, vocabulariesUpdated, spaceRolesAssigned, spaceRolesRevoked);
        } catch (org.hibernate.StaleObjectStateException e)
        {
            throw new UserFailureException("The operation has failed due to conflict with simultanous operations.");
        } finally
        {
            EntityOperationsInProgress.getInstance().removeRegistrationPending(registrationId);
            if (sessionTokenForEntityOperation != null)
            {
                sessionManagerForEntityOperation.closeSession(sessionTokenForEntityOperation);
            }
            try
            {
                daoFactory.getSessionFactory().getCurrentSession().flush();
            } catch (Exception e)
            {
            }
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public EntityOperationsState didEntityOperationsSucceed(String token, TechId registrationId)
    {
        if (registrationId == null)
        {
            return EntityOperationsState.NO_OPERATION;
        }

        if (EntityOperationsInProgress.getInstance().isRegistrationPending(registrationId))
        {
            return EntityOperationsState.IN_PROGRESS;
        }

        EntityOperationsLogEntryPE logEntry =
                daoFactory.getEntityOperationsLogDAO().tryFindLogEntry(registrationId.getId());

        if (logEntry != null)
        {
            return EntityOperationsState.OPERATION_SUCCEEDED;
        } else
        {
            return EntityOperationsState.NO_OPERATION;
        }
    }

    private long createSpaces(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        ArrayList<SpacePE> spacePEsCreated = new ArrayList<SpacePE>();
        List<NewSpace> newSpaces = operationDetails.getSpaceRegistrations();
        if (authorize)
        {
            checkSpaceCreationAllowed(session, newSpaces);
        }

        int index = 0;
        for (NewSpace newSpace : newSpaces)
        {
            SpacePE spacePE =
                    registerSpaceInternal(session, newSpace, operationDetails.tryUserIdOrNull());
            spacePEsCreated.add(spacePE);
            progress.update("createSpaces", newSpaces.size(), ++index);
        }
        return index;
    }

    protected void checkSpaceCreationAllowed(Session session, List<NewSpace> newSpaces)
    {
        if (newSpaces != null && newSpaces.isEmpty() == false)
        {
            entityOperationChecker.assertSpaceCreationAllowed(session, newSpaces);
        }
    }

    protected void checkSpaceRoleAssignmentAllowed(Session session, SpaceIdentifier space)
    {
        entityOperationChecker.assertSpaceRoleAssignmentAllowed(session, space);
    }

    private long updateVocabularies(Session etlSession, Session userSession, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<VocabularyUpdatesDTO> updates = operationDetails.getVocabularyUpdates();

        if (updates != null && updates.isEmpty() == false)
        {
            entityOperationChecker.assertVocabularyUpdateAllowed(etlSession);
            entityOperationChecker.assertVocabularyUpdateAllowed(userSession);

            for (VocabularyUpdatesDTO update : updates)
            {
                updateVocabulary(userSession, update);
            }
        }

        return updates.size();
    }

    private void updateVocabulary(Session session, VocabularyUpdatesDTO updates)
    {
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.update(updates);
    }

    private long createMaterials(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        MaterialHelper materialHelper =
                new MaterialHelper(session, businessObjectFactory, getDAOFactory(),
                        getPropertiesBatchManager(), managedPropertyEvaluatorFactory);
        Map<String, List<NewMaterial>> materialRegs = operationDetails.getMaterialRegistrations();
        if (authorize)
        {
            checkMaterialCreationAllowed(session, materialRegs);
        }

        List<NewMaterialWithType> materials = materialHelper.convertMaterialRegistrationIntoMaterialsWithType(materialRegs);

        Map<String, Set<String>> materialTypesWithMateiralProperties = materialHelper.getPropertyTypesOfMaterialType(materialRegs.keySet());

        List<List<NewMaterialWithType>> materialGroups = MaterialGroupingDAG.groupByDepencies(materials, materialTypesWithMateiralProperties);
        int index = 0;

        for (List<NewMaterialWithType> materialsGroup : materialGroups)
        {
            materialHelper.registerMaterials(materialsGroup);
            progress.update("createMaterials", materialRegs.size(), ++index);
        }
        return index;
    }

    private long updateMaterials(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        MaterialHelper materialHelper =
                new MaterialHelper(session, businessObjectFactory, getDAOFactory(),
                        getPropertiesBatchManager(), managedPropertyEvaluatorFactory);

        List<MaterialUpdateDTO> allMaterialUpdates = operationDetails.getMaterialUpdates();

        if (authorize)
        {
            checkMaterialUpdateAllowed(session, allMaterialUpdates);
        }

        materialHelper.updateMaterials(allMaterialUpdates);

        // in material helper call the update of materials - but this has to wait fo change of the
        // material updates to a map
        return allMaterialUpdates.size();
    }

    protected void checkMaterialCreationAllowed(Session session,
            Map<String, List<NewMaterial>> materials)
    {
        if (materials != null && materials.isEmpty() == false)
        {
            entityOperationChecker.assertMaterialCreationAllowed(session, materials);
        }
    }

    protected void checkMaterialUpdateAllowed(Session session,
            List<MaterialUpdateDTO> materialUpdates)
    {
        if (materialUpdates != null && materialUpdates.isEmpty() == false)
        {
            entityOperationChecker.assertMaterialUpdateAllowed(session, materialUpdates);
        }
    }

    private long updateMetaprojects(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<MetaprojectUpdatesDTO> updates = operationDetails.getMetaprojectUpdates();

        for (MetaprojectUpdatesDTO update : updates)
        {
            updateMetaprojects(session, update);
        }

        return updates.size();
    }

    private void updateMetaprojects(Session session, MetaprojectUpdatesDTO update)
    {
        IMetaprojectBO metaprojectBO = businessObjectFactory.createMetaprojectBO(session);
        metaprojectBO.loadDataByTechId(update.getMetaprojectId());

        Metaproject updates = new Metaproject();
        updates.setName(metaprojectBO.getMetaproject().getName());
        updates.setDescription(update.getDescription());
        metaprojectBO.update(updates);

        metaprojectBO.addSamples(update.getAddedSamples());
        metaprojectBO.removeSamples(update.getRemovedSamples());
        metaprojectBO.addDataSets(update.getAddedDataSets());
        metaprojectBO.removeDataSets(update.getRemovedDataSets());
        metaprojectBO.addExperiments(update.getAddedExperiments());
        metaprojectBO.removeExperiments(update.getRemovedExperiments());
        metaprojectBO.addMaterials(update.getAddedMaterials());
        metaprojectBO.removeMaterials(update.getRemovedMaterials());

        metaprojectBO.save();
    }

    private long assignSpaceRoles(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progressListener, boolean authorize)
    {
        // see also ch.systemsx.cisd.openbis.generic.server.CommonServer.registerSpaceRole(String, RoleCode, SpaceIdentifier, Grantee)
        List<SpaceRoleAssignment> spaceRoleAssignments = operationDetails.getSpaceRoleAssignments();
        int index = 0;
        int assignmentCount = 0;
        for (SpaceRoleAssignment assignment : spaceRoleAssignments)
        {
            assignmentCount += assignment.getGrantees().size();
        }

        if (assignmentCount < 1)
        {
            return assignmentCount;
        }

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);

        for (SpaceRoleAssignment assignment : spaceRoleAssignments)
        {
            RoleCode roleCode = assignment.getRoleCode();
            SpaceIdentifier space = assignment.getSpaceIdentifier();
            if (authorize)
            {
                checkSpaceRoleAssignmentAllowed(session, space);
            }
            for (Grantee grantee : assignment.getGrantees())
            {
                final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
                newRoleAssignment.setGrantee(grantee);
                newRoleAssignment.setSpaceIdentifier(space);
                newRoleAssignment.setRole(roleCode);

                table.add(newRoleAssignment);
                progressListener.update("assignSpaceRole", assignmentCount, ++index);
            }
        }

        table.save();
        return index;
    }

    private long revokeSpaceRoles(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progressListener, boolean authorize)
    {
        // see also ch.systemsx.cisd.openbis.generic.server.CommonServer.deleteSpaceRole(String, RoleCode, SpaceIdentifier, Grantee)
        // Did not refactor the above method to share code for fear of making merges to the release branch difficult
        List<SpaceRoleAssignment> spaceRoleRevocations = operationDetails.getSpaceRoleRevocations();
        int index = 0;
        int assignmentCount = 0;
        for (SpaceRoleAssignment assignment : spaceRoleRevocations)
        {
            assignmentCount += assignment.getGrantees().size();
        }

        if (assignmentCount < 1)
        {
            return assignmentCount;
        }

        for (SpaceRoleAssignment assignment : spaceRoleRevocations)
        {
            RoleCode roleCode = assignment.getRoleCode();
            SpaceIdentifier space = assignment.getSpaceIdentifier();
            for (Grantee grantee : assignment.getGrantees())
            {
                final RoleAssignmentPE roleAssignment =
                        getDAOFactory().getRoleAssignmentDAO().tryFindSpaceRoleAssignment(roleCode,
                                space.getSpaceCode(), grantee);
                if (roleAssignment == null)
                {
                    throw new UserFailureException("Given space role does not exist.");
                }
                final PersonPE personPE = session.tryGetPerson();
                if (roleAssignment.getPerson() != null && roleAssignment.getPerson().equals(personPE)
                        && roleAssignment.getRole().equals(RoleCode.ADMIN))
                {
                    boolean isInstanceAdmin = false;
                    for (final RoleAssignmentPE roleAssigment : personPE.getRoleAssignments())
                    {
                        if (roleAssigment.getRoleWithHierarchy().isInstanceLevel()
                                && roleAssigment.getRole().equals(RoleCode.ADMIN))
                        {
                            isInstanceAdmin = true;
                        }
                    }
                    if (isInstanceAdmin == false)
                    {
                        throw new UserFailureException(
                                "For safety reason you cannot give away your own space admin power. "
                                        + "Ask instance admin to do that for you.");
                    }
                }
                getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
                progressListener.update("revokeSpaceRole", assignmentCount, ++index);
            }
        }

        return index;
    }

    private long createMetaprojects(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        final List<NewMetaproject> metaprojectRegistrations =
                operationDetails.getMetaprojectRegistrations();
        int index = 0;
        for (NewMetaproject metaproject : metaprojectRegistrations)
        {
            registerMetaproject(session, metaproject);
            progress.update("createMetaProjects", metaprojectRegistrations.size(), ++index);
        }
        return index;
    }

    private MetaprojectPE registerMetaproject(final Session session, NewMetaproject metaproject)
    {
        IMetaprojectBO metaprojectBO = businessObjectFactory.createMetaprojectBO(session);

        Metaproject registration = new Metaproject();
        registration.setName(metaproject.getName());
        registration.setDescription(metaproject.getDescription());
        metaprojectBO.define(metaproject.getOwnerId(), registration);

        metaprojectBO.addSamples(metaproject.getSamples());
        metaprojectBO.addExperiments(metaproject.getExperiments());
        metaprojectBO.addMaterials(metaproject.getMaterials());
        metaprojectBO.addDataSets(metaproject.getDatasets());
        metaprojectBO.save();
        return metaprojectBO.getMetaproject();
    }

    private SpacePE registerSpaceInternal(Session session, NewSpace newSpace,
            String registratorUserIdOrNull)
    {
        // create space
        ISpaceBO groupBO = businessObjectFactory.createSpaceBO(session);
        groupBO.define(newSpace.getCode(), newSpace.getDescription());
        SpacePE space = groupBO.getSpace();

        if (registratorUserIdOrNull != null)
        {
            space.setRegistrator(
                    getOrCreatePerson(session.getSessionToken(), registratorUserIdOrNull));
        }
        groupBO.save();

        // create ADMIN role assignemnt
        if (newSpace.getSpaceAdminUserId() != null)
        {
            IRoleAssignmentTable roleTable =
                    businessObjectFactory.createRoleAssignmentTable(session);
            NewRoleAssignment assignment = new NewRoleAssignment();
            SpaceIdentifier spaceIdentifier = new SpaceIdentifier(space.getCode());
            assignment.setSpaceIdentifier(spaceIdentifier);
            assignment.setRole(RoleCode.ADMIN);
            Grantee grantee = Grantee.createPerson(newSpace.getSpaceAdminUserId());
            assignment.setGrantee(grantee);
            roleTable.add(assignment);
            roleTable.save();
        }
        return space;

    }

    private long createProjects(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        ArrayList<ProjectPE> projectPEsCreated = new ArrayList<ProjectPE>();
        List<NewProject> newProjects = operationDetails.getProjectRegistrations();
        if (authorize)
        {
            checkProjectCreationAllowed(session, newProjects);
        }
        int index = 0;
        for (NewProject newProject : newProjects)
        {
            ProjectPE projectPE =
                    registerProjectInternal(session, newProject, operationDetails.tryUserIdOrNull());
            projectPEsCreated.add(projectPE);
            progress.update("createProjects", newProjects.size(), ++index);
        }
        return index;
    }

    protected void checkProjectCreationAllowed(Session session, List<NewProject> newProjects)
    {
        if (newProjects != null && newProjects.isEmpty() == false)
        {
            entityOperationChecker.assertProjectCreationAllowed(session, newProjects);
        }
    }

    private ProjectPE registerProjectInternal(Session session, NewProject newProject,
            String registratorUserIdOrNull)
    {
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        projectBO.define(newProject, null);
        if (registratorUserIdOrNull != null)
        {
            projectBO.getProject().setRegistrator(
                    getOrCreatePerson(session.getSessionToken(), registratorUserIdOrNull));
        }
        projectBO.save();

        return projectBO.getProject();
    }

    private long updateProjects(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        ArrayList<ProjectPE> projectPEsUpdated = new ArrayList<ProjectPE>();
        List<ProjectUpdatesDTO> projectsToUpdate = operationDetails.getProjectUpdates();
        if (authorize)
        {
            checkProjectUpdateAllowed(session, projectsToUpdate);
        }
        int index = 0;
        for (ProjectUpdatesDTO project : projectsToUpdate)
        {
            ProjectPE projectPE =
                    updateProjectInternal(session, project, operationDetails.tryUserIdOrNull());
            projectPEsUpdated.add(projectPE);
            progress.update("updateProjects", projectsToUpdate.size(), ++index);
        }
        return index;
    }

    protected void checkProjectUpdateAllowed(Session session,
            List<ProjectUpdatesDTO> projectsToUpdate)
    {
        if (projectsToUpdate != null && projectsToUpdate.isEmpty() == false)
        {
            entityOperationChecker.assertProjectUpdateAllowed(session, projectsToUpdate);
        }
    }

    private ProjectPE updateProjectInternal(Session session, ProjectUpdatesDTO projectToUpdate,
            String registratorUserIdOrNull)
    {
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        if (projectToUpdate.getTechId() != null)
        {
            projectBO.loadDataByTechId(projectToUpdate.getTechId());
        } else if (projectToUpdate.getPermId() != null)
        {
            projectBO.loadByPermId(projectToUpdate.getPermId());
        } else
        {
            ProjectIdentifier identifier =
                    new ProjectIdentifierFactory(projectToUpdate.getIdentifier()).createIdentifier();
            projectBO.loadByProjectIdentifier(identifier);
        }
        projectBO.update(projectToUpdate);
        if (registratorUserIdOrNull != null)
        {
            projectBO.getProject().setModifier(
                    getOrCreatePerson(session.getSessionToken(), registratorUserIdOrNull));
        }
        projectBO.save();

        return projectBO.getProject();
    }

    private long createSamples(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<NewSample> newSamples = operationDetails.getSampleRegistrations();

        if (authorize)
        {
            authorizeSampleCreation(session, newSamples);
        }
        String userIdOrNull = operationDetails.tryUserIdOrNull();
        PersonPE registratorOrNull = tryFindPersonForUserIdOrEmail(userIdOrNull);
        final ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);

        List<List<NewSample>> sampleGroups = splitIntoDependencyGroups(newSamples);

        for (List<NewSample> groupOfSamples : sampleGroups)
        {
            BatchOperationExecutor.executeInBatches(new SampleBatchRegistration(sampleTable,
                    groupOfSamples, registratorOrNull), getBatchSize(operationDetails), progress,
                    "createContainerSamples");
        }
        return newSamples.size();
    }

    /**
     * Splits the samples using the grouping dag into groups, that can be executed in batches one after another, that samples in later batches depend
     * only on the samples from earlier batches
     */
    private List<List<NewSample>> splitIntoDependencyGroups(List<NewSample> newSamples)
    {
        return SampleGroupingDAG.groupByDepencies(newSamples);
    }

    private void authorizeSampleCreation(Session session, List<NewSample> newSamples)
    {
        List<NewSample> instanceSamples = new ArrayList<NewSample>();
        List<NewSample> spaceSamples = new ArrayList<NewSample>();

        for (NewSample newSample : newSamples)
        {
            SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(newSample);
            if (sampleIdentifier.isDatabaseInstanceLevel())
            {
                instanceSamples.add(newSample);
            } else
            {
                spaceSamples.add(newSample);
            }
        }

        checkInstanceSampleCreationAllowed(session, instanceSamples);
        checkSpaceSampleCreationAllowed(session, spaceSamples);
    }

    private void checkInstanceSampleCreationAllowed(Session session, List<NewSample> instanceSamples)
    {
        if (instanceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertInstanceSampleCreationAllowed(session, instanceSamples);
        }
    }

    private void checkSpaceSampleCreationAllowed(Session session, List<NewSample> spaceSamples)
    {
        if (spaceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertSpaceSampleCreationAllowed(session, spaceSamples);
        }
    }

    private long updateSamples(final Session session,
            AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<SampleUpdatesDTO> sampleUpdates = operationDetails.getSampleUpdates();
        int sampleUpdateCount = sampleUpdates.size();
        if (sampleUpdateCount < 1)
        {
            return 0;
        }
        progress.update("authorizingSampleUpdates", sampleUpdateCount, 0);
        if (authorize)
        {
            checkSampleUpdatesAllowed(session, sampleUpdates);
        }
        progress.update("authorizingSampleUpdates", sampleUpdateCount, sampleUpdateCount);
        final ISampleTable sampleTable = businessObjectFactory.createSampleTable(session);

        BatchOperationExecutor.executeInBatches(new SampleCheckBeforeUpdate(sampleTable,
                sampleUpdates), getBatchSize(operationDetails), progress,
                "checkSamplesBeforeUpdate");

        BatchOperationExecutor.executeInBatches(new SampleUpdate(sampleTable, sampleUpdates),
                getBatchSize(operationDetails), progress, "updateSamples");

        return sampleUpdateCount;
    }

    private void checkSampleUpdatesAllowed(final Session session,
            List<SampleUpdatesDTO> sampleUpdates)
    {
        List<SampleUpdatesDTO> instanceSamples = new ArrayList<SampleUpdatesDTO>();
        List<SampleUpdatesDTO> spaceSamples = new ArrayList<SampleUpdatesDTO>();
        for (SampleUpdatesDTO sampleUpdate : sampleUpdates)
        {
            SampleIdentifier sampleIdentifier = sampleUpdate.getSampleIdentifier();
            if (sampleIdentifier.isDatabaseInstanceLevel())
            {
                instanceSamples.add(sampleUpdate);
            } else
            {
                spaceSamples.add(sampleUpdate);
            }
        }
        checkInstanceSampleUpdateAllowed(session, instanceSamples);
        checkSpaceSampleUpdateAllowed(session, spaceSamples);
    }

    private void checkInstanceSampleUpdateAllowed(Session session,
            List<SampleUpdatesDTO> instanceSamples)
    {
        if (instanceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertInstanceSampleUpdateAllowed(session, instanceSamples);
        }
    }

    private void checkSpaceSampleUpdateAllowed(Session session, List<SampleUpdatesDTO> spaceSamples)
    {
        if (spaceSamples.isEmpty() == false)
        {
            entityOperationChecker.assertSpaceSampleUpdateAllowed(session, spaceSamples);
        }
    }

    private long createDataSetsV3(Session session, AtomicEntityOperationDetails operationDetails,
            final IServiceConversationProgressListener conversationProgress, boolean authorize)
    {
        if (operationDetails.getDataSetRegistrations() == null || operationDetails.getDataSetRegistrations().isEmpty())
        {
            return 0;
        }

        if (authorize)
        {
            checkDataSetCreationAllowed(session, operationDetails.getDataSetRegistrations());
        }

        IOperationContext context = new OperationContext(session);
        context.setAttribute(ListSampleTechIdByIdentifier.CONTAINER_SHORTCUT_ALLOWED_ATTRIBUTE, true);
        context.addProgressListener(new IProgressListener()
            {
                @Override
                public void onProgress(IProgressStack progressStack)
                {
                    if (progressStack.size() > 0)
                    {
                        IProgress progress = progressStack.iterator().next();
                        int totalItemsToProcess = progress.getTotalItemsToProcess() != null ? progress.getTotalItemsToProcess() : 0;
                        int numItemsProcessed = progress.getNumItemsProcessed() != null ? progress.getNumItemsProcessed() : 0;
                        conversationProgress.update(progress.getLabel(), totalItemsToProcess, numItemsProcessed);
                    }
                }
            });

        List<DataSetCreation> creations = new LinkedList<DataSetCreation>();

        for (NewExternalData newData : operationDetails.getDataSetRegistrations())
        {
            // Fields that were in V2 but are intentionally ignored in V3:
            // - newData.getUserId() and newData.getUserEMail() fields were used in V2 to find a registrator (see in DataBO.tryToGetRegistrator()),
            // but found registrator was anyway overwritten by RelationshipUtils.setExperimentForDataSet() call at the end of DataBO.define() method
            // (a user from the session was used as registrator and modifier)
            // - newData.getAssociatedSampleCode() field was never used in V2
            // - newData.getRegistrationDate() field was never used in V2

            DataSetCreation creation = new DataSetCreation();
            creation.setCode(newData.getCode());
            creation.setDataSetKind(map(newData.getDataSetKind()));
            creation.setMeasured(newData.isMeasured());
            creation.setDataProducer(newData.getDataProducerCode());
            creation.setDataProductionDate(newData.getProductionDate());

            // type
            if (newData.getDataSetType() != null)
            {
                creation.setTypeId(new EntityTypePermId(newData.getDataSetType().getCode(),
                        ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind.DATA_SET));
            }

            // experiment
            if (newData.getExperimentIdentifierOrNull() != null)
            {
                creation.setExperimentId(new ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier(newData
                        .getExperimentIdentifierOrNull().toString()));
            }

            // sample
            injectSampleId(creation, newData);

            // data store
            if (newData.getDataStoreCode() != null)
            {
                creation.setDataStoreId(new DataStorePermId(newData.getDataStoreCode()));
            }

            // properties
            if (newData.getDataSetProperties() != null)
            {
                IPropertyTypeDAO propertyTypeDAO = daoFactory.getPropertyTypeDAO();
                for (NewProperty property : newData.getDataSetProperties())
                {
                    PropertyTypePE propertyTypePE = getPropertyType(propertyTypeDAO, property.getPropertyCode());
                    if(propertyTypePE.isMultiValue()) {
                        String value = property.getValue();
                        if(value != null && value.startsWith("[")) {
                            value = value.substring(1, value.length()-1);
                            String[] propertyValues = Stream.of(value.split(","))
                                    .map(String::trim)
                                    .toArray(String[]::new);
                            creation.setProperty(property.getPropertyCode(), propertyValues);
                        } else {
                            creation.setProperty(property.getPropertyCode(), property.getValue());
                        }
                    } else
                    {
                        String value = property.getValue();
                        if(propertyTypePE.getType().getCode() == DataTypeCode.CONTROLLEDVOCABULARY ||
                            propertyTypePE.getType().getCode() == DataTypeCode.SAMPLE) {
                            if(value != null && value.startsWith("[")) {
                                value = value.substring(1, value.length()-1);
                            }
                        }
                        creation.setProperty(property.getPropertyCode(), value);
                    }
                }
            }

            // parents
            if (newData.getParentDataSetCodes() != null)
            {
                List<IDataSetId> parentIds = new LinkedList<IDataSetId>();
                for (String parentCode : newData.getParentDataSetCodes())
                {
                    parentIds.add(new DataSetPermId(parentCode));
                }
                creation.setParentIds(parentIds);
            }

            if (newData instanceof NewContainerDataSet)
            {
                NewContainerDataSet newContainerData = (NewContainerDataSet) newData;

                if (newContainerData.getContainedDataSetCodes() != null)
                {
                    List<IDataSetId> componentIds = new LinkedList<IDataSetId>();
                    for (String componentCode : newContainerData.getContainedDataSetCodes())
                    {
                        componentIds.add(new DataSetPermId(componentCode));
                    }
                    creation.setComponentIds(componentIds);
                }
            } else if (newData instanceof NewLinkDataSet)
            {
                NewLinkDataSet newLinkData = (NewLinkDataSet) newData;

                LinkedDataCreation linkCreation = new LinkedDataCreation();
                linkCreation.setExternalCode(newLinkData.getExternalCode());
                linkCreation.setExternalDmsId(new ExternalDmsPermId(newLinkData.getExternalDataManagementSystemCode()));

                creation.setLinkedData(linkCreation);
            } else
            {
                // newData is instance of NewExternalData or NewDataSet

                PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
                physicalCreation.setLocation(newData.getLocation());
                physicalCreation.setShareId(newData.getShareId());
                physicalCreation.setSize(newData.getSize());
                physicalCreation.setSpeedHint(newData.getSpeedHint());
                physicalCreation.setH5Folders(newData.isH5Folders());
                physicalCreation.setH5arFolders(newData.isH5arFolders());

                // complete
                if (newData.getComplete() != null)
                {
                    Complete complete = null;
                    switch (newData.getComplete())
                    {
                        case T:
                            complete = Complete.YES;
                            break;
                        case F:
                            complete = Complete.NO;
                            break;
                        case U:
                            complete = Complete.UNKNOWN;
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported complete value: " + newData.getComplete());
                    }
                    physicalCreation.setComplete(complete);
                }

                // file format type
                if (newData.getFileFormatType() != null)
                {
                    physicalCreation.setFileFormatTypeId(new FileFormatTypePermId(newData.getFileFormatType().getCode()));
                }

                // locator type
                if (newData.getLocatorType() != null)
                {
                    physicalCreation.setLocatorTypeId(new LocatorTypePermId(newData.getLocatorType().getCode()));
                }

                // storage format
                if (newData.getStorageFormat() != null)
                {
                    physicalCreation.setStorageFormatId(new StorageFormatPermId(newData.getStorageFormat().getCode()));
                }

                creation.setPhysicalData(physicalCreation);
            }

            creations.add(creation);
        }

        try
        {
            CreateDataSetsOperation operation = new CreateDataSetsOperation(creations);
            List<IOperationResult> results = operationsExecutor.execute(context, Arrays.asList(operation), null);
            return ((CreateDataSetsOperationResult) results.get(0)).getObjectIds().size();
        } catch (Throwable t)
        {
            throw ExceptionUtils.create(context, t);
        }
    }

    private PropertyTypePE getPropertyType(IPropertyTypeDAO propertyTypeDAO, String propertyTypeCode) {
        PropertyTypePE propertyTypePE = propertyTypeDAO.tryFindPropertyTypeByCode(propertyTypeCode);
        if (propertyTypePE == null)
        {
            throw new UserFailureException(String.format("Property type '%s' does not exist.",
                    propertyTypeCode));
        }
        return propertyTypePE;
    }

    private DataSetKind map(ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind dataSetKind)
    {
        if (dataSetKind != null)
        {
            return DataSetKind.valueOf(dataSetKind.name());
        }
        return null;
    }

    private void injectSampleId(DataSetCreation creation, NewExternalData newData)
    {
        SampleIdentifier sampleIdentifier = newData.getSampleIdentifierOrNull();
        String permId = newData.getSamplePermIdOrNull();
        if (sampleIdentifier != null)
        {
            ISampleId sampleId;
            if (permId != null)
            {
                sampleId = new SamplePermId(permId);
            } else
            {
                sampleId = new ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier(
                        sampleIdentifier.toString());
            }
            creation.setSampleId(sampleId);
        }
    }

    private void checkDataSetCreationAllowed(Session session,
            List<? extends NewExternalData> dataSets)
    {
        if (dataSets != null && dataSets.isEmpty() == false)
        {
            entityOperationChecker.assertDataSetCreationAllowed(session, dataSets);
        }
    }

    private long updateDataSets(final Session session,
            AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        final List<DataSetBatchUpdatesDTO> dataSetUpdates = operationDetails.getDataSetUpdates();
        final int dataSetUpdatesCount = dataSetUpdates.size();
        if (dataSetUpdatesCount < 1)
        {
            return 0;
        }

        progress.update("authorizingDataSetUpdates", dataSetUpdatesCount, 0);
        if (authorize)
        {
            checkDataSetUpdateAllowed(session, dataSetUpdates);
        }
        progress.update("authorizingDataSetUpdates", dataSetUpdatesCount, dataSetUpdatesCount);
        final IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);

        BatchOperationExecutor.executeInBatches(new DataSetCheckBeforeBatchUpdate(dataSetTable,
                dataSetUpdates), getBatchSize(operationDetails), progress,
                "checkDataSetsBeforeUpdate");

        BatchOperationExecutor.executeInBatches(
                new DataSetBatchUpdate(dataSetTable, dataSetUpdates),
                getBatchSize(operationDetails), progress, "updateDataSets");

        return dataSetUpdatesCount;
    }

    private void checkDataSetUpdateAllowed(Session session, List<DataSetBatchUpdatesDTO> dataSets)
    {
        if (dataSets != null && dataSets.isEmpty() == false)
        {
            entityOperationChecker.assertDataSetUpdateAllowed(session, dataSets);
        }
    }

    private IDataBO registerDataSetInternal(final Session session, NewExternalData dataSet, DataSetRegistrationCache cache)
    {
        SampleIdentifier sampleIdentifier = dataSet.getSampleIdentifierOrNull();
        if (sampleIdentifier != null)
        {
            return registerDataSetInternal(session, sampleIdentifier, dataSet, cache);
        } else
        {
            ExperimentIdentifier experimentIdentifier = dataSet.getExperimentIdentifierOrNull();
            return registerDataSetInternal(session, experimentIdentifier, dataSet, cache);
        }
    }

    private long createExperiments(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        final List<NewExperiment> experimentRegistrations =
                operationDetails.getExperimentRegistrations();
        if (authorize)
        {
            checkExperimentCreationAllowed(session, experimentRegistrations);
        }
        int index = 0;
        for (NewExperiment experiment : experimentRegistrations)
        {
            registerExperiment(session, experiment);
            progress.update("createExperiments", experimentRegistrations.size(), ++index);
        }
        return index;
    }

    protected void checkExperimentCreationAllowed(Session session,
            List<NewExperiment> newExperiments)
    {
        if (newExperiments != null && newExperiments.isEmpty() == false)
        {
            entityOperationChecker.assertExperimentCreationAllowed(session, newExperiments);
        }
    }

    private void updateExperiment(Session session, ExperimentUpdatesDTO updates)
    {
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.update(updates);
        experimentBO.save();
    }

    private long updateExperiments(Session session, AtomicEntityOperationDetails operationDetails,
            IServiceConversationProgressListener progress, boolean authorize)
    {
        List<ExperimentUpdatesDTO> updates = operationDetails.getExperimentUpdates();

        if (authorize)
        {
            checkExperimentUpdateAllowed(session, updates);
        }

        for (ExperimentUpdatesDTO update : updates)
        {
            updateExperiment(session, update);
        }

        return updates.size();
    }

    protected void checkExperimentUpdateAllowed(Session session,
            List<ExperimentUpdatesDTO> experimentUpdates)
    {
        if (experimentUpdates != null && experimentUpdates.isEmpty() == false)
        {
            entityOperationChecker.assertExperimentUpdateAllowed(session, experimentUpdates);
        }
    }

    private IDataBO registerDataSetInternal(final Session session,
            SampleIdentifier sampleIdentifier, NewExternalData externalData, DataSetRegistrationCache cache)
    {
        SamplePE sample = cache.getSamples().get(sampleIdentifier);

        if (sample == null)
        {
            final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
            sampleBO.loadBySampleIdentifier(sampleIdentifier);
            sample = sampleBO.getSample();
            cache.getSamples().put(sampleIdentifier, sample);
        }

        return registerDataSetInternal(session, sample, externalData, cache);
    }

    private IDataBO registerDataSetInternal(final Session session, SamplePE sample,
            NewExternalData externalData, DataSetRegistrationCache cache)
    {
        final IDataBO dataBO = cache.getDataBO() != null ? cache.getDataBO() : businessObjectFactory.createDataBO(session);
        cache.setDataBO(dataBO);

        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        dataBO.setCache(cache);
        dataBO.define(externalData, sample, sourceType);
        dataBO.save();

        boolean isContainer = externalData instanceof NewContainerDataSet;
        if (isContainer)
        {
            dataBO.setContainedDataSets(sample.getExperiment(), sample, (NewContainerDataSet) externalData);
        }

        final String dataSetCode = dataBO.getData().getCode();
        assert dataSetCode != null : "Data set code not specified.";

        return dataBO;
    }

    private IDataBO registerDataSetInternal(final Session session,
            ExperimentIdentifier experimentIdentifier, NewExternalData externalData, DataSetRegistrationCache cache)
    {
        if (false == cache.getExperiments().containsKey(experimentIdentifier))
        {
            cache.getExperiments().put(experimentIdentifier, tryLoadExperimentByIdentifier(session, experimentIdentifier));
        }
        ExperimentPE experiment = cache.getExperiments().get(experimentIdentifier);

        if (experiment == null)
        {
            throw new UserFailureException("Unknown experiment '" + experimentIdentifier + "'.");
        }
        if (experiment.getDeletion() != null)
        {
            throw new UserFailureException("Data set can not be registered because experiment '"
                    + experiment.getIdentifier() + "' is in trash.");
        }
        final IDataBO externalDataBO = cache.getDataBO() != null ? cache.getDataBO() : businessObjectFactory.createDataBO(session);
        cache.setDataBO(externalDataBO);

        SourceType sourceType =
                externalData.isMeasured() ? SourceType.MEASUREMENT : SourceType.DERIVED;
        externalDataBO.setCache(cache);
        externalDataBO.define(externalData, experiment, sourceType);
        externalDataBO.save();

        boolean isContainer = externalData instanceof NewContainerDataSet;
        if (isContainer)
        {
            externalDataBO.setContainedDataSets(experiment, null, (NewContainerDataSet) externalData);
        }

        final String dataSetCode = externalDataBO.getData().getCode();
        assert dataSetCode != null : "Data set code not specified.";

        return externalDataBO;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria)
    {
        Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_SAMPLES, new ConcurrentOperation<List<Sample>>()
            {
                @Override
                public List<Sample> execute()
                {
                    DetailedSearchCriteria detailedSearchCriteria =
                            SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                                    SearchableEntityKind.SAMPLE, searchCriteria);
                    SearchHelper searchHelper =
                            new SearchHelper(session, businessObjectFactory, getDAOFactory());
                    return searchHelper.searchForSamples(session.getUserName(), detailedSearchCriteria);
                }
            });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<AbstractExternalData> searchForDataSets(String sessionToken,
            SearchCriteria searchCriteria)
    {
        Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_DATA_SETS, new ConcurrentOperation<List<AbstractExternalData>>()
            {
                @Override
                public List<AbstractExternalData> execute()
                {
                    DetailedSearchCriteria detailedSearchCriteria =
                            SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(),
                                    SearchableEntityKind.DATA_SET, searchCriteria);
                    SearchHelper searchHelper =
                            new SearchHelper(session, businessObjectFactory, getDAOFactory());
                    return searchHelper.searchForDataSets(session.getUserName(), detailedSearchCriteria);
                }
            });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    @ReturnValueFilter(validatorClass = ExperimentValidator.class)
    public List<Experiment> searchForExperiments(String sessionToken, SearchCriteria searchCriteria)
    {
        Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_EXPERIMENTS, new ConcurrentOperation<List<Experiment>>()
            {
                @Override
                public List<Experiment> execute()
                {
                    DetailedSearchCriteria detailedSearchCriteria =
                            SearchCriteriaToDetailedSearchCriteriaTranslator.convert(getDAOFactory(), SearchableEntityKind.EXPERIMENT,
                                    searchCriteria);
                    SearchHelper searchHelper = new SearchHelper(session, businessObjectFactory, getDAOFactory());
                    List<ExperimentPE> experiments = searchHelper.searchForExperiments(session.getUserName(), detailedSearchCriteria);
                    return translateExperimentsWithMetaprojectAssignments(session, experiments);
                }
            });
    }

    @Override
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties)
    {
        Session session = getSession(sessionToken);
        IMaterialLister lister = businessObjectFactory.createMaterialLister(session);
        ListMaterialCriteria criteriaWithIds = populateMissingTypeId(criteria);
        return lister.list(criteriaWithIds, withProperties);
    }

    private ListMaterialCriteria populateMissingTypeId(ListMaterialCriteria criteria)
    {
        MaterialType materialTypeOrNull = criteria.tryGetMaterialType();
        if (materialTypeOrNull != null && materialTypeOrNull.getId() == null)
        {
            String materialTypeCode = materialTypeOrNull.getCode();
            EntityTypePE typeWithId =
                    daoFactory.getEntityTypeDAO(EntityKind.MATERIAL).tryToFindEntityTypeByCode(
                            materialTypeCode);
            if (typeWithId == null)
            {
                throw UserFailureException.fromTemplate("Invalid material type '%s'",
                        materialTypeCode);
            } else
            {
                MaterialType materialTypeWithId = new MaterialType();
                materialTypeWithId.setId(typeWithId.getId());
                materialTypeWithId.setCode(materialTypeCode);
                return ListMaterialCriteria.createFromMaterialType(materialTypeWithId);
            }
        }

        return criteria;

    }

    @Override
    @SuppressWarnings("deprecation")
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void removeDataSetsPermanently(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes, String reason)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        permanentlyDeleteDataSets(session, dataSetTable, dataSetCodes, reason, false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void updateDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class) DataSetUpdatesDTO dataSetUpdates)
    {
        final Session session = getSession(sessionToken);
        final IDataBO dataSetBO = businessObjectFactory.createDataBO(session);
        dataSetBO.update(dataSetUpdates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> getTrustedCrossOriginDomains(String sessionToken)
    {
        return trustedOriginDomainProvider.getTrustedDomains();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void setStorageConfirmed(String sessionToken, List<String> dataSetCodes)
    {
        checkSession(sessionToken);

        List<Long> dataSetIds = new LinkedList<Long>();
        for (String dataSetCode : dataSetCodes)
        {

            if (daoFactory.getDataDAO().confirmStorage(dataSetCode))
            {
                daoFactory.getPostRegistrationDAO().addDataSet(dataSetCode);
            } else if (daoFactory.getDataDAO().exists(dataSetCode) == false)
            {
                throw new UserFailureException("Storage confirmation for a dataset: " + dataSetCode
                        + " failed because the data set has been already deleted.");
            }

            Long id = daoFactory.getDataDAO().tryToFindDataSetIdByCode(dataSetCode).getId();
            dataSetIds.add(id);
        }
        IDynamicPropertyEvaluationScheduler indexUpdater =
                daoFactory.getPersistencyResources().getDynamicPropertyEvaluationScheduler();
        indexUpdater.scheduleUpdate(DynamicPropertyEvaluationOperation.evaluate(DataPE.class,
                dataSetIds));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void markSuccessfulPostRegistration(String sessionToken, String dataSetCode)
    {
        checkSession(sessionToken);

        daoFactory.getPostRegistrationDAO().removeDataSet(dataSetCode);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void notifyDatasetAccess(String sessionToken, String dataSetCode)
    {
        checkSession(sessionToken);

        daoFactory.getDataDAO().updateAccessTimestamp(dataSetCode);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<AbstractExternalData> listDataSetsForPostRegistration(String sessionToken,
            String dataStoreCode)
    {
        Session session = getSession(sessionToken);

        // find all datasets for registration
        final IDatasetLister datasetLister = createDatasetLister(session);
        Collection<Long> allDataSetIds =
                daoFactory.getPostRegistrationDAO().listDataSetsForPostRegistration();
        List<AbstractExternalData> allDataSets = datasetLister.listByDatasetIds(allDataSetIds);

        // find datastore
        DataStorePE dataStore =
                getDAOFactory().getDataStoreDAO().tryToFindDataStoreByCode(dataStoreCode);
        if (dataStore == null)
        {
            throw new UserFailureException("Unknown data store: " + dataStoreCode);
        }

        // filter datasets by datastore
        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData externalData : allDataSets)
        {
            if (dataStoreCode.equals(externalData.getDataStore().getCode()))
            {
                result.add(externalData);
            }
        }
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public void heartbeat(String token)
    {
        // do nothing
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public boolean doesUserHaveRole(String token, String user, String roleCode, String spaceOrNull)
    {
        return new AuthorizationServiceUtils(daoFactory, user).doesUserHaveRole(roleCode,
                spaceOrNull);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleDataSets(String token, String user, List<String> dataSetCodes)
    {
        return new AuthorizationServiceUtils(daoFactory, user).filterDataSetCodes(dataSetCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleExperiments(String token, String user,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class) List<String> experimentIds)
    {
        return new AuthorizationServiceUtils(daoFactory, user).filterExperimentIds(experimentIds);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<String> filterToVisibleSamples(String token, String user,
            @AuthorizationGuard(guardClass = SampleAugmentedCodePredicate.class) List<String> sampleIds)
    {
        return new AuthorizationServiceUtils(daoFactory, user).filterSampleIds(sampleIds);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<? extends EntityTypePropertyType<?>> listPropertyDefinitionsForType(
            String sessionToken, String code, EntityKind entityKind)
    {
        IEntityTypeDAO edao = daoFactory.getEntityTypeDAO(entityKind);
        IEntityPropertyTypeDAO dao = daoFactory.getEntityPropertyTypeDAO(entityKind);

        EntityTypePE type = edao.tryToFindEntityTypeByCode(code);
        List<EntityTypePropertyTypePE> propertiesPE = dao.listEntityPropertyTypes(type);

        if (entityKind == EntityKind.DATA_SET)
        {
            Collection<DataSetTypePropertyType> collection =
                    CollectionUtils.collect(propertiesPE,
                            DataSetTypePropertyTypeTranslator.TRANSFORMER);
            return new LinkedList<DataSetTypePropertyType>(collection);
        } else if (entityKind == EntityKind.SAMPLE)
        {
            Collection<SampleTypePropertyType> collection =
                    CollectionUtils.collect(propertiesPE,
                            SampleTypePropertyTypeTranslator.TRANSFORMER);
            return new LinkedList<SampleTypePropertyType>(collection);
        } else if (entityKind == EntityKind.EXPERIMENT)
        {
            Collection<ExperimentTypePropertyType> collection =
                    CollectionUtils.collect(propertiesPE,
                            ExperimentTypePropertyTypeTranslator.TRANSFORMER);
            return new LinkedList<ExperimentTypePropertyType>(collection);
        } else if (entityKind == EntityKind.MATERIAL)
        {
            Collection<MaterialTypePropertyType> collection =
                    CollectionUtils.collect(propertiesPE,
                            MaterialTypePropertyTypeTranslator.TRANSFORMER);
            return new LinkedList<MaterialTypePropertyType>(collection);
        } else
        {
            throw new IllegalArgumentException("Unsupported entity kind " + entityKind);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public ExternalDataManagementSystem tryGetExternalDataManagementSystem(String token,
            String externalDataManagementSystemCode)
    {
        checkSession(token);

        ExternalDataManagementSystemPE externalSystem =
                getDAOFactory().getExternalDataManagementSystemDAO()
                        .tryToFindExternalDataManagementSystemByCode(
                                externalDataManagementSystemCode);

        if (externalSystem != null)
        {
            return ExternalDataManagementSystemTranslator.translate(externalSystem);
        } else
        {
            return null;
        }
    }

    private int getBatchSize(AtomicEntityOperationDetails details)
    {
        return details == null || details.getBatchSizeOrNull() == null ? BatchOperationExecutor
                .getDefaultBatchSize() : details.getBatchSizeOrNull();
    }

    public void setConversationClient(IServiceConversationClientManagerLocal conversationClient)
    {
        this.conversationClient = conversationClient;
    }

    public void setConversationServer(IServiceConversationServerManagerLocal conversationServer)
    {
        this.conversationServer = conversationServer;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Metaproject> listMetaprojects(String sessionToken, String userId)
    {
        IMetaprojectDAO metaprojectDAO = daoFactory.getMetaprojectDAO();
        PersonPE owner = daoFactory.getPersonDAO().tryFindPersonByUserId(userId);

        if (owner == null)
        {
            throw new IllegalArgumentException("User with id " + userId + " doesn't exist.");
        }

        List<MetaprojectPE> metaprojectPEs = metaprojectDAO.listMetaprojects(owner);

        return MetaprojectTranslator.translate(metaprojectPEs);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public MetaprojectAssignments getMetaprojectAssignments(String systemSessionToken, String name,
            String userName, EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions)
    {
        Metaproject metaproject = tryGetMetaproject(systemSessionToken, name, userName);

        if (metaproject == null)
        {
            throw UserFailureException.fromTemplate("Can't find metaproject '%s/%s'", userName,
                    name);
        }

        MetaprojectAssignmentsHelper helper =
                new MetaprojectAssignmentsHelper(daoFactory, managedPropertyEvaluatorFactory);
        return helper.getMetaprojectAssignments(getSession(systemSessionToken), metaproject,
                userName, fetchOptions);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Metaproject> listMetaprojectsForEntity(String systemSessionToken, String userId,
            IObjectId entityId)
    {
        Map<IObjectId, List<Metaproject>> map = listMetaprojectsForEntities(systemSessionToken, userId, Collections.singletonList(entityId));
        return map.get(entityId);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Map<IObjectId, List<Metaproject>> listMetaprojectsForEntities(String systemSessionToken, String userId,
            Collection<? extends IObjectId> entityIds)
    {
        IMetaprojectDAO metaprojectDAO = daoFactory.getMetaprojectDAO();
        PersonPE owner = daoFactory.getPersonDAO().tryFindPersonByUserId(userId);

        EntityObjectIdHelper helper = new EntityObjectIdHelper(businessObjectFactory);
        Map<IObjectId, List<Metaproject>> map = new HashMap<IObjectId, List<Metaproject>>();

        if (entityIds != null)
        {
            for (IObjectId entityId : entityIds)
            {
                if (entityId != null)
                {
                    IEntityInformationHolderDTO entity =
                            helper.getEntityById(getSession(systemSessionToken), entityId);

                    Collection<MetaprojectPE> metaprojectPEs =
                            metaprojectDAO.listMetaprojectsForEntity(owner, entity);

                    map.put(entityId, MetaprojectTranslator.translate(metaprojectPEs));
                }
            }
        }

        return map;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken)
    {
        // see ch.systemsx.cisd.openbis.generic.server.CommonServer.listAuthorizationGroups(String)
        checkSession(sessionToken);
        final List<AuthorizationGroupPE> authorizationGroups =
                getDAOFactory().getAuthorizationGroupDAO().list();
        Collections.sort(authorizationGroups);
        return AuthorizationGroupTranslator.translate(authorizationGroups);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<AuthorizationGroup> listAuthorizationGroupsForUser(String sessionToken, String userId)
    {
        checkSession(sessionToken);
        final List<AuthorizationGroupPE> allAuthorizationGroups =
                getDAOFactory().getAuthorizationGroupDAO().list();
        ArrayList<AuthorizationGroupPE> authorizationGroups = new ArrayList<AuthorizationGroupPE>();
        for (AuthorizationGroupPE authorizationGroup : allAuthorizationGroups)
        {
            Set<PersonPE> persons = authorizationGroup.getPersons();
            for (PersonPE person : persons)
            {
                if (userId.equals(person.getUserId()))
                {
                    authorizationGroups.add(authorizationGroup);
                }
            }
        }

        Collections.sort(authorizationGroups);
        return AuthorizationGroupTranslator.translate(authorizationGroups);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Person> listUsersForAuthorizationGroup(String sessionToken, TechId authorizationGroupId)
    {
        final Session session = getSession(sessionToken);
        IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizationGroupId);
        return PersonTranslator.translate(bo.getAuthorizationGroup().getPersons());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<RoleAssignment> listRoleAssignments(String sessionToken)
    {
        checkSession(sessionToken);
        final List<RoleAssignmentPE> roles =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        return RoleAssignmentTranslator.translate(roles);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<Attachment> listAttachments(String sessionToken, AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId)
    {
        Session session = getSession(sessionToken);

        AttachmentHolderPE attachmentHolder = null;

        switch (attachmentHolderKind)
        {
            case PROJECT:
            {
                IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
                projectBO.loadDataByTechId(new TechId(attachmentHolderId));
                attachmentHolder = projectBO.getProject();
                break;
            }
            case EXPERIMENT:
            {
                IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
                experimentBO.loadDataByTechId(new TechId(attachmentHolderId));
                attachmentHolder = experimentBO.getExperiment();
                break;
            }
            case SAMPLE:
            {
                ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
                sampleBO.loadDataByTechId(new TechId(attachmentHolderId));
                attachmentHolder = sampleBO.getSample();
                break;
            }
        }

        if (attachmentHolder != null)
        {
            List<AttachmentPE> attachments = getDAOFactory().getAttachmentDAO().listAttachments(attachmentHolder);
            return AttachmentTranslator.translate(attachments, session.getBaseIndexURL());
        } else
        {
            return null;
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public AttachmentWithContent getAttachment(String sessionToken, AttachmentHolderKind attachmentHolderKind, Long attachmentHolderId,
            String fileName, Integer versionOrNull)
    {
        Session session = getSession(sessionToken);

        AttachmentPE attachment = null;

        switch (attachmentHolderKind)
        {
            case PROJECT:
            {
                IProjectBO bo = businessObjectFactory.createProjectBO(session);
                bo.loadDataByTechId(new TechId(attachmentHolderId));
                attachment = bo.tryGetProjectFileAttachment(fileName, versionOrNull);
                break;
            }
            case EXPERIMENT:
            {
                IExperimentBO bo = businessObjectFactory.createExperimentBO(session);
                bo.loadDataByTechId(new TechId(attachmentHolderId));
                attachment = bo.tryGetExperimentFileAttachment(fileName, versionOrNull);
                break;
            }
            case SAMPLE:
            {
                ISampleBO bo = businessObjectFactory.createSampleBO(session);
                bo.loadDataByTechId(new TechId(attachmentHolderId));
                attachment = bo.tryGetSampleFileAttachment(fileName, versionOrNull);
                break;
            }
        }

        if (attachment != null)
        {
            return AttachmentTranslator.translateWithContent(attachment);
        } else
        {
            return null;
        }
    }

    public void setTimeout(String timeout)
    {
        try
        {
            this.timeout = Long.parseLong(timeout);
        } catch (Exception e)
        {
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public List<AbstractExternalData> listNotArchivedDatasetsWithMetaproject(String sessionToken, final IMetaprojectId metaprojectId)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final Metaproject metaproject = CommonServiceProvider.getCommonServer().getMetaprojectWithoutOwnershipChecks(sessionToken, metaprojectId);
        return datasetLister.listByMetaprojectIdAndArchivalState(metaproject.getId(), false);
    }
}
