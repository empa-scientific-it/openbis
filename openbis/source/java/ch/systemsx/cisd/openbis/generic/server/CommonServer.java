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

package ch.systemsx.cisd.openbis.generic.server;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAttachmentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IAuthorizationGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICorePluginTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataStoreBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletedDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletionTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypePropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGridCustomFilterOrColumnBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IScriptBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISpaceBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ITrashBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IVocabularyTermBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityHistoryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataManagementSystemDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.HibernateSearchDataProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.DynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityValidationCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationScriptRunner;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IdentifierExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IPropertyTypeUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LastModificationState;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedUiActionDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewColumnOrFilter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PersonAdapter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.ICorePluginResourceLoader;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationHolderDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.AuthorizationGroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreServiceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityHistoryTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataManagementSystemTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomFilterTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.PropertyTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.RoleAssignmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ScriptTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.SpaceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.TypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

public final class CommonServer extends AbstractCommonServer<ICommonServerForInternalUse> implements
        ICommonServerForInternalUse
{
    private final LastModificationState lastModificationState;

    private final IDataStoreServiceRegistrator dataStoreServiceRegistrator;

    public CommonServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final ICommonBusinessObjectFactory businessObjectFactory,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator,
            final LastModificationState lastModificationState)
    {
        this(authenticationService, sessionManager, daoFactory, null, businessObjectFactory,
                dataStoreServiceRegistrator, lastModificationState);
    }

    CommonServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final ICommonBusinessObjectFactory businessObjectFactory,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator,
            final LastModificationState lastModificationState)
    {
        super(authenticationService, sessionManager, daoFactory, propertiesBatchManager,
                businessObjectFactory);
        this.dataStoreServiceRegistrator = dataStoreServiceRegistrator;
        this.lastModificationState = lastModificationState;
    }

    ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    @Override
    public final ICommonServerForInternalUse createLogger(IInvocationLoggerContext context)
    {
        return new CommonServerLogger(getSessionManager(), context);
    }

    //
    // ISystemAuthenticator
    //

    @Override
    public SessionContextDTO tryToAuthenticateAsSystem()
    {
        final PersonPE systemUser = getSystemUser();
        HibernateUtils.initialize(systemUser.getAllPersonRoles());
        RoleAssignmentPE role = new RoleAssignmentPE();
        role.setDatabaseInstance(getDAOFactory().getHomeDatabaseInstance());
        role.setRole(RoleCode.ADMIN);
        systemUser.addRoleAssignment(role);
        String sessionToken =
                sessionManager.tryToOpenSession(systemUser.getUserId(),
                        new AuthenticatedPersonBasedPrincipalProvider(systemUser));
        Session session = sessionManager.getSession(sessionToken);
        session.setPerson(systemUser);
        return tryGetSession(sessionToken);
    }

    //
    // IGenericServer
    //

    @Override
    public final List<Space> listSpaces(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final Session session = getSession(sessionToken);
        final DatabaseInstancePE databaseInstance =
                SpaceIdentifierHelper.getDatabaseInstance(identifier, getDAOFactory());
        final List<SpacePE> spaces = getDAOFactory().getSpaceDAO().listSpaces(databaseInstance);
        final SpacePE homeSpaceOrNull = session.tryGetHomeGroup();
        for (final SpacePE space : spaces)
        {
            space.setHome(space.equals(homeSpaceOrNull));
        }
        Collections.sort(spaces);
        return SpaceTranslator.translate(spaces);
    }

    @Override
    public final void registerSpace(final String sessionToken, final String spaceCode,
            final String descriptionOrNull)
    {
        final Session session = getSession(sessionToken);
        final ISpaceBO groupBO = businessObjectFactory.createSpaceBO(session);
        groupBO.define(spaceCode, descriptionOrNull);
        groupBO.save();
    }

    @Override
    public final void updateScript(final String sessionToken, final IScriptUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IScriptBO bo = businessObjectFactory.createScriptBO(session);
        bo.update(updates);
    }

    @Override
    public final void updateSpace(final String sessionToken, final ISpaceUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final ISpaceBO groupBO = businessObjectFactory.createSpaceBO(session);
        groupBO.update(updates);
    }

    @Override
    public final void registerPerson(final String sessionToken, final String userID)
    {
        registerPersons(sessionToken, Arrays.asList(userID));
    }

    @Override
    public final List<RoleAssignment> listRoleAssignments(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<RoleAssignmentPE> roles =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        return RoleAssignmentTranslator.translate(roles);
    }

    @Override
    public final void registerSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setGrantee(grantee);
        newRoleAssignment.setSpaceIdentifier(spaceIdentifier);
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    @Override
    public final void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setGrantee(grantee);
        newRoleAssignment.setDatabaseInstanceIdentifier(new DatabaseInstanceIdentifier(
                DatabaseInstanceIdentifier.HOME));
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    @Override
    public final void deleteSpaceRole(final String sessionToken, final RoleCode roleCode,
            final SpaceIdentifier spaceIdentifier, final Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindSpaceRoleAssignment(roleCode,
                        spaceIdentifier.getSpaceCode(), grantee);
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
                if (roleAssigment.getDatabaseInstance() != null
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
    }

    @Override
    public final void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final Grantee grantee)
    {
        final Session session = getSession(sessionToken);
        final IRoleAssignmentDAO roleAssignmentDAO = getDAOFactory().getRoleAssignmentDAO();
        final RoleAssignmentPE roleAssignment =
                roleAssignmentDAO.tryFindInstanceRoleAssignment(roleCode, grantee);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given database instance role does not exist.");
        }
        if (roleAssignment.getPerson() != null
                && roleAssignment.getPerson().equals(session.tryGetPerson())
                && roleAssignment.getRole().equals(RoleCode.ADMIN)
                && roleAssignment.getDatabaseInstance() != null)
        {
            throw new UserFailureException(
                    "For safety reason you cannot give away your own omnipotence. "
                            + "Ask another instance admin to do that for you.");
        }
        roleAssignmentDAO.deleteRoleAssignment(roleAssignment);
    }

    @Override
    public final List<Person> listPersons(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();
        Collections.sort(persons);
        return PersonTranslator.translate(persons);
    }

    @Override
    public final List<Person> listActivePersons(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listActivePersons();
        Collections.sort(persons);
        return PersonTranslator.translate(persons);
    }

    @Override
    public final List<Project> listProjects(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return ProjectTranslator.translate(projects);
    }

    @Override
    public final List<SampleType> listSampleTypes(final String sessionToken)
    {
        checkSession(sessionToken);
        final List<SampleTypePE> sampleTypes = getDAOFactory().getSampleTypeDAO().listSampleTypes();
        Collections.sort(sampleTypes);
        return SampleTypeTranslator.translate(sampleTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    public final List<Sample> listSamples(final String sessionToken,
            final ListSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(new ListOrSearchSampleCriteria(criteria));
    }

    @Override
    public List<Sample> searchForSamples(String sessionToken, DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        SearchHelper searchHelper =
                new SearchHelper(session, businessObjectFactory, getDAOFactory());
        return searchHelper.searchForSamples(criteria);
    }

    @Override
    public final List<ExternalData> listSampleExternalData(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<ExternalData> datasets =
                datasetLister.listBySampleTechId(sampleId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    @Override
    public final List<ExternalData> listExperimentExternalData(final String sessionToken,
            final TechId experimentId, boolean showOnlyDirectlyConnected)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<ExternalData> datasets =
                datasetLister.listByExperimentTechId(experimentId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    // 'fast' implementation
    @Override
    public List<ExternalData> listDataSetRelationships(String sessionToken, TechId datasetId,
            DataSetRelationshipRole role)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        List<ExternalData> datasets = null;
        switch (role)
        {
            case CONTAINER:
                datasets = datasetLister.listByContainerTechId(datasetId);
                Collections.sort(datasets, ExternalData.DATA_SET_COMPONENTS_COMPARATOR);
                break;
            case CHILD:
                datasets = datasetLister.listByChildTechId(datasetId);
                Collections.sort(datasets);
                break;
            case PARENT:
                datasets = datasetLister.listByParentTechIds(Arrays.asList(datasetId.getId()));
                Collections.sort(datasets);
                break;
        }
        return datasets;
    }

    @Override
    public final List<PropertyType> listPropertyTypes(final String sessionToken,
            boolean withRelations)
    {
        final Session session = getSession(sessionToken);
        final IPropertyTypeTable propertyTypeTable =
                businessObjectFactory.createPropertyTypeTable(session);
        if (withRelations)
        {
            propertyTypeTable.loadWithRelations();
        } else
        {
            propertyTypeTable.load();
        }
        final List<PropertyTypePE> propertyTypes = propertyTypeTable.getPropertyTypes();
        Collections.sort(propertyTypes);
        return PropertyTypeTranslator.translate(propertyTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    public List<EntityTypePropertyType<?>> listEntityTypePropertyTypes(String sessionToken)
    {
        List<PropertyType> propertyTypes = listPropertyTypes(sessionToken, true);
        return extractAssignments(propertyTypes);
    }

    @Override
    public List<EntityHistory> listEntityHistory(String sessionToken, EntityKind entityKind,
            TechId entityID)
    {
        Session session = getSession(sessionToken);
        IEntityHistoryDAO entityPropertyHistoryDAO = getDAOFactory().getEntityPropertyHistoryDAO();
        List<AbstractEntityPropertyHistoryPE> result =
                entityPropertyHistoryDAO.getPropertyHistory(
                        DtoConverters.convertEntityKind(entityKind), entityID);
        return EntityHistoryTranslator.translate(result, session.getBaseIndexURL());
    }

    private static List<EntityTypePropertyType<?>> extractAssignments(
            List<PropertyType> listPropertyTypes)
    {
        List<EntityTypePropertyType<?>> result = new ArrayList<EntityTypePropertyType<?>>();
        for (PropertyType propertyType : listPropertyTypes)
        {
            extractAssignments(result, propertyType);
        }
        Collections.sort(result);
        return result;
    }

    private static void extractAssignments(List<EntityTypePropertyType<?>> result,
            final PropertyType propertyType)
    {
        for (ExperimentTypePropertyType etpt : propertyType.getExperimentTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (SampleTypePropertyType etpt : propertyType.getSampleTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (MaterialTypePropertyType etpt : propertyType.getMaterialTypePropertyTypes())
        {
            result.add(etpt);
        }
        for (DataSetTypePropertyType etpt : propertyType.getDataSetTypePropertyTypes())
        {
            result.add(etpt);
        }
    }

    @Override
    public final List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText,
            final boolean useWildcardSearchMode, int maxSize)
    {
        checkSession(sessionToken);
        final List<MatchingEntity> list = new ArrayList<MatchingEntity>();
        for (final SearchableEntity searchableEntity : searchableEntities)
        {
            HibernateSearchDataProvider dataProvider =
                    new HibernateSearchDataProvider(getDAOFactory());
            List<MatchingEntity> entities =
                    getDAOFactory().getHibernateSearchDAO().searchEntitiesByTerm(searchableEntity,
                            queryText, dataProvider, useWildcardSearchMode, list.size(), maxSize);
            list.addAll(entities);
        }
        return list;
    }

    @Override
    public final List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final ProjectIdentifier projectIdentifier)
    {
        return listExperiments(sessionToken, experimentType, null, projectIdentifier, false, false);
    }

    @Override
    public final List<Experiment> listExperimentsHavingSamples(final String sessionToken,
            final ExperimentType experimentType, final ProjectIdentifier projectIdentifier)
    {
        return listExperiments(sessionToken, experimentType, null, projectIdentifier, true, false);
    }

    @Override
    public final List<Experiment> listExperimentsHavingDataSets(final String sessionToken,
            final ExperimentType experimentType, final ProjectIdentifier projectIdentifier)
    {
        return listExperiments(sessionToken, experimentType, null, projectIdentifier, false, true);
    }

    @Override
    public final List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final SpaceIdentifier spaceIdentifier)
    {
        return listExperiments(sessionToken, experimentType, spaceIdentifier, null, false, false);
    }

    @Override
    public List<Experiment> listExperiments(String sessionToken,
            List<ExperimentIdentifier> experimentIdentifiers)
    {
        Session session = getSession(sessionToken);
        IExperimentTable experimentTable = businessObjectFactory.createExperimentTable(session);

        experimentTable.load(experimentIdentifiers);

        List<ExperimentPE> experiments = experimentTable.getExperiments();
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL());
    }

    private final List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final SpaceIdentifier spaceIdentifierOrNull,
            final ProjectIdentifier projectIdentifierOrNull, boolean onlyHavingSamples,
            boolean onlyHavingDataSets)
    {
        final Session session = getSession(sessionToken);
        final IExperimentTable experimentTable =
                businessObjectFactory.createExperimentTable(session);
        if (projectIdentifierOrNull != null)
        {
            experimentTable.load(experimentType.getCode(), projectIdentifierOrNull,
                    onlyHavingSamples, onlyHavingDataSets);
        } else if (spaceIdentifierOrNull != null)
        {
            experimentTable.load(experimentType.getCode(), spaceIdentifierOrNull);
        }
        final List<ExperimentPE> experiments = experimentTable.getExperiments();
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL());
    }

    @Override
    public final List<ExperimentType> listExperimentTypes(final String sessionToken)
    {
        final List<ExperimentTypePE> experimentTypes =
                listEntityTypes(sessionToken, EntityKind.EXPERIMENT);
        return ExperimentTranslator.translate(experimentTypes);
    }

    @Override
    public List<MaterialType> listMaterialTypes(String sessionToken)
    {
        final List<MaterialTypePE> materialTypes =
                listEntityTypes(sessionToken, EntityKind.MATERIAL);
        return MaterialTypeTranslator.translate(materialTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    public MaterialType getMaterialType(String sessionToken, String code)
    {
        final EntityTypePE materialType = findEntityType(EntityKind.MATERIAL, code);
        return MaterialTypeTranslator.translateSimple(materialType);
    }

    private <T extends EntityTypePE> List<T> listEntityTypes(String sessionToken,
            EntityKind entityKind)
    {
        checkSession(sessionToken);
        final List<T> types =
                getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind))
                        .listEntityTypes();
        Collections.sort(types);
        return types;
    }

    @Override
    public final List<DataType> listDataTypes(final String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<DataTypePE> dataTypePEs = getDAOFactory().getPropertyTypeDAO().listDataTypes();
        final List<DataType> dataTypes = DataTypeTranslator.translate(dataTypePEs);
        Collections.sort(dataTypes, new Comparator<DataType>()
            {
                @Override
                public int compare(DataType o1, DataType o2)
                {
                    return o1.getCode().name().compareTo(o2.getCode().name());
                }
            });
        return dataTypes;
    }

    @Override
    public List<FileFormatType> listFileFormatTypes(String sessionToken)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<FileFormatTypePE> fileFormatTypePEs =
                getDAOFactory().getFileFormatTypeDAO().listFileFormatTypes();
        final List<FileFormatType> fileFormatTypes = TypeTranslator.translate(fileFormatTypePEs);
        Collections.sort(fileFormatTypes, new Comparator<FileFormatType>()
            {
                @Override
                public int compare(FileFormatType o1, FileFormatType o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
        return fileFormatTypes;
    }

    @Override
    public final List<Vocabulary> listVocabularies(final String sessionToken,
            final boolean withTerms, boolean excludeInternal)
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        final List<VocabularyPE> vocabularies =
                getDAOFactory().getVocabularyDAO().listVocabularies(excludeInternal);
        if (withTerms)
        {
            for (final VocabularyPE vocabularyPE : vocabularies)
            {
                enrichWithTerms(vocabularyPE);
            }
        }
        Collections.sort(vocabularies);
        return VocabularyTranslator.translate(vocabularies, withTerms);
    }

    private void enrichWithTerms(final VocabularyPE vocabularyPE)
    {
        HibernateUtils.initialize(vocabularyPE.getTerms());
    }

    @Override
    public String assignPropertyType(final String sessionToken, NewETPTAssignment assignment)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        final ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind kind =
                DtoConverters.convertEntityKind(assignment.getEntityKind());
        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session, kind);
        etptBO.createAssignment(assignment);

        return String.format("%s property type '%s' successfully assigned to %s type '%s'",
                getAssignmentType(assignment), assignment.getPropertyTypeCode(), kind.getLabel(),
                assignment.getEntityTypeCode());
    }

    private String getAssignmentType(NewETPTAssignment assignment)
    {
        if (assignment.isDynamic())
        {
            return "Dynamic";
        } else if (assignment.isManaged())
        {
            return "Managed";
        } else if (assignment.isMandatory())
        {
            return "Mandatory";
        } else
        {
            return "Optional";
        }
    }

    @Override
    public void updatePropertyTypeAssignment(final String sessionToken,
            NewETPTAssignment assignmentUpdates)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session,
                        DtoConverters.convertEntityKind(assignmentUpdates.getEntityKind()));
        etptBO.loadAssignment(assignmentUpdates.getPropertyTypeCode(),
                assignmentUpdates.getEntityTypeCode());
        etptBO.updateLoadedAssignment(assignmentUpdates);
    }

    @Override
    public void unassignPropertyType(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session,
                        DtoConverters.convertEntityKind(entityKind));
        etptBO.loadAssignment(propertyTypeCode, entityTypeCode);
        etptBO.deleteLoadedAssignment();
    }

    @Override
    public int countPropertyTypedEntities(String sessionToken, EntityKind entityKind,
            String propertyTypeCode, String entityTypeCode)
    {
        assert sessionToken != null : "Unspecified session token";
        Session session = getSession(sessionToken);

        IEntityTypePropertyTypeBO etptBO =
                businessObjectFactory.createEntityTypePropertyTypeBO(session,
                        DtoConverters.convertEntityKind(entityKind));
        return etptBO.countAssignmentValues(propertyTypeCode, entityTypeCode);
    }

    @Override
    public final void registerPropertyType(final String sessionToken,
            final PropertyType propertyType)
    {
        assert sessionToken != null : "Unspecified session token";
        assert propertyType != null : "Unspecified property type";

        final Session session = getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.define(propertyType);
        propertyTypeBO.save();
    }

    @Override
    public final void updatePropertyType(final String sessionToken,
            final IPropertyTypeUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.update(updates);
    }

    @Override
    public final void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabulary != null : "Unspecified vocabulary";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
    }

    @Override
    public final void updateVocabulary(final String sessionToken, final IVocabularyUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.update(updates);
    }

    @Override
    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<String> vocabularyTerms, Long previousTermOrdinal)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";
        assert previousTermOrdinal != null : "Unspecified previous term ordinal";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.addNewTerms(vocabularyTerms, previousTermOrdinal);
        vocabularyBO.save();
    }

    @Override
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";
        assert code != null : "Unspecified code";
        assert previousTermOrdinal != null : "Unspecified previous term ordinal";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.addNewUnofficialTerm(code, label, description, previousTermOrdinal);
        vocabularyBO.save();
    }

    @Override
    public final void updateVocabularyTerm(final String sessionToken,
            final IVocabularyTermUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IVocabularyTermBO vocabularyTermBO =
                businessObjectFactory.createVocabularyTermBO(session);
        vocabularyTermBO.update(updates);
    }

    @Override
    public void deleteVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeDeleted, List<VocabularyTermReplacement> termsToBeReplaced)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.delete(termsToBeDeleted, termsToBeReplaced);
        vocabularyBO.save();
    }

    @Override
    public void makeVocabularyTermsOfficial(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> termsToBeOfficial)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";

        final Session session = getSession(sessionToken);
        final IVocabularyTermBO vocabularyTermBO =
                businessObjectFactory.createVocabularyTermBO(session);
        vocabularyTermBO.makeOfficial(termsToBeOfficial);
    }

    @Override
    public void registerProject(String sessionToken, ProjectIdentifier projectIdentifier,
            String description, String leaderId, Collection<NewAttachment> attachments)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        projectBO.define(projectIdentifier, description, leaderId);
        projectBO.save();
        for (NewAttachment attachment : attachments)
        {
            final AttachmentPE attachmentPE = AttachmentTranslator.translate(attachment);
            projectBO.addAttachment(attachmentPE);
        }
        projectBO.save();

    }

    @Override
    public List<ExternalData> searchForDataSets(String sessionToken, DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        SearchHelper searchHelper =
                new SearchHelper(session, businessObjectFactory, getDAOFactory());
        return searchHelper.searchForDataSets(criteria);
    }

    @Override
    public ExternalData getDataSetInfo(final String sessionToken, final TechId datasetId)
    {
        final Session session = getSession(sessionToken);
        final IDataBO datasetBO = businessObjectFactory.createDataBO(session);
        datasetBO.loadDataByTechId(datasetId);
        datasetBO.enrichWithParentsAndExperiment();
        datasetBO.enrichWithChildren();
        datasetBO.enrichWithContainedDataSets();
        datasetBO.enrichWithProperties();
        final DataPE dataset = datasetBO.getData();
        return DataSetTranslator.translate(dataset, session.getBaseIndexURL(), false);
    }

    @Override
    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IDataBO dataSetBO = businessObjectFactory.createDataBO(session);
        dataSetBO.update(updates);
        DataSetUpdateResult result = new DataSetUpdateResult();
        DataPE data = dataSetBO.getData();
        result.setModificationDate(data.getModificationDate());
        List<String> parents = IdentifierExtractor.extract(data.getParents());
        Collections.sort(parents);
        result.setParentCodes(parents);
        result.setContainedDataSetCodes(Code.extractCodes(data.getContainedDataSets()));
        return result;
    }

    @Override
    public List<ExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities relatedEntities, boolean withDetails)
    {
        final Session session = getSession(sessionToken);
        final Set<DataPE> resultSet = new LinkedHashSet<DataPE>();
        // TODO 2009-08-17, Piotr Buczek: [LMS-1149] optimize performance
        addRelatedDataSets(resultSet, relatedEntities.getEntities());
        final List<ExternalData> list = new ArrayList<ExternalData>(resultSet.size());
        for (final DataPE hit : resultSet)
        {
            HibernateUtils.initialize(hit.getChildRelationships());
            list.add(DataSetTranslator.translate(hit, session.getBaseIndexURL(), withDetails));
        }
        return list;
    }

    private void addRelatedDataSets(final Set<DataPE> resultSet,
            final List<? extends IEntityInformationHolder> relatedEntities)
    {
        final IDataDAO dataDAO = getDAOFactory().getDataDAO();
        EnumMap<EntityKind, List<IEntityInformationHolder>> entities =
                new EnumMap<EntityKind, List<IEntityInformationHolder>>(EntityKind.class);

        for (IEntityInformationHolder entity : relatedEntities)
        {
            if (isEntityKindRelatedWithDataSets(entity.getEntityKind()))
            {
                List<IEntityInformationHolder> entitiesOfGivenKind =
                        entities.get(entity.getEntityKind());
                if (entitiesOfGivenKind == null)
                {
                    entitiesOfGivenKind = new ArrayList<IEntityInformationHolder>();
                    entities.put(entity.getEntityKind(), entitiesOfGivenKind);
                }
                entitiesOfGivenKind.add(entity);
            }
        }

        for (Entry<EntityKind, List<IEntityInformationHolder>> entry : entities.entrySet())
        {
            if (entry.getValue() != null && entry.getValue().size() > 0)
            {
                List<DataPE> relatedDataSets =
                        dataDAO.listRelatedDataSets(entry.getValue(), entry.getKey());
                resultSet.addAll(relatedDataSets);
            }
        }
    }

    private boolean isEntityKindRelatedWithDataSets(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind entityKind)
    {
        switch (entityKind)
        {
            case EXPERIMENT:
            case SAMPLE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties)
    {
        final Session session = getSession(sessionToken);
        final IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        return materialLister.list(criteria, withProperties);
    }

    @Override
    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
    }

    @Override
    public void updateSampleType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.SAMPLE, entityType);
    }

    @Override
    public void registerMaterialType(String sessionToken, MaterialType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
    }

    @Override
    public void updateMaterialType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.MATERIAL, entityType);
    }

    @Override
    public void registerExperimentType(String sessionToken, ExperimentType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
    }

    @Override
    public void updateExperimentType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.EXPERIMENT, entityType);
    }

    @Override
    public void registerFileFormatType(String sessionToken, FileFormatType type)
    {
        checkSession(sessionToken);
        FileFormatTypePE fileFormatType = new FileFormatTypePE();
        try
        {
            fileFormatType.setCode(type.getCode());
            fileFormatType.setDescription(type.getDescription());
            getDAOFactory().getFileFormatTypeDAO().createOrUpdate(fileFormatType);
        } catch (final DataAccessException ex)
        {
            DataAccessExceptionTranslator.throwException(ex,
                    String.format("File format type '%s' ", fileFormatType.getCode()), null);
        }
    }

    @Override
    public void registerDataSetType(String sessionToken, DataSetType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
        dataStoreServiceRegistrator.register(entityType);
    }

    @Override
    public void updateDataSetType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.DATA_SET, entityType);
    }

    private void updateEntityType(String sessionToken, EntityKind entityKind, EntityType entityType)
    {
        checkSession(sessionToken);
        IEntityTypeDAO entityTypeDAO =
                getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind));
        EntityTypePE entityTypePE = entityTypeDAO.tryToFindEntityTypeByCode(entityType.getCode());
        entityTypePE.setDescription(entityType.getDescription());
        updateSpecificEntityTypeProperties(entityKind, entityTypePE, entityType);
        entityTypeDAO.createOrUpdateEntityType(entityTypePE);
    }

    private void updateSpecificEntityTypeProperties(EntityKind entityKind,
            EntityTypePE entityTypePE, EntityType entityType)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            SampleTypePE sampleTypePE = (SampleTypePE) entityTypePE;
            SampleType sampleType = (SampleType) entityType;
            sampleTypePE.setListable(sampleType.isListable());
            sampleTypePE.setAutoGeneratedCode(sampleType.isAutoGeneratedCode());
            sampleTypePE.setShowParentMetadata(sampleType.isShowParentMetadata());
            sampleTypePE.setGeneratedCodePrefix(sampleType.getGeneratedCodePrefix());
            sampleTypePE.setSubcodeUnique(sampleType.isSubcodeUnique());
            sampleTypePE.setContainerHierarchyDepth(sampleType.getContainerHierarchyDepth());
            sampleTypePE
                    .setGeneratedFromHierarchyDepth(sampleType.getGeneratedFromHierarchyDepth());

            if (sampleType.getValidationScript() == null
                    || sampleType.getValidationScript().getName() == null
                    || sampleType.getValidationScript().getName().equals(""))
            {
                sampleTypePE.setValidationScript(null);
            } else
            {
                ScriptPE script = getDAOFactory().getScriptDAO()
                        .tryFindByName(sampleType.getValidationScript().getName());
                if (script != null)
                {
                    sampleTypePE.setValidationScript(script);
                }
            }

        } else if (entityKind == EntityKind.DATA_SET)
        {
            DataSetTypePE dataSetTypePE = (DataSetTypePE) entityTypePE;
            DataSetType dataSetType = (DataSetType) entityType;
            dataSetTypePE.setDeletionDisallow(dataSetType.isDeletionDisallow());
            dataSetTypePE.setMainDataSetPath(dataSetType.getMainDataSetPath());
            String mainDataSetPattern = dataSetType.getMainDataSetPattern();
            EntityTypeBO.assertValidDataSetTypeMainPattern(mainDataSetPattern);
            dataSetTypePE.setMainDataSetPattern(mainDataSetPattern);

            if (dataSetType.getValidationScript() == null
                    || dataSetType.getValidationScript().getName() == null
                    || dataSetType.getValidationScript().getName().equals(""))
            {
                dataSetTypePE.setValidationScript(null);
            } else
            {
                ScriptPE script = getDAOFactory().getScriptDAO()
                        .tryFindByName(dataSetType.getValidationScript().getName());
                if (script != null)
                {
                    dataSetTypePE.setValidationScript(script);
                }
            }

        }
    }

    @Override
    public void deleteDataSets(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType type, boolean forceNotExistingLocations, boolean isTrashEnabled)
    {
        deleteDataSetsCommon(sessionToken, dataSetCodes, reason, type, forceNotExistingLocations,
                false, isTrashEnabled);
    }

    @Override
    public void deleteDataSetsForced(String sessionToken, List<String> dataSetCodes, String reason,
            DeletionType type, boolean forceNotExistingLocations, boolean isTrashEnabled)
    {
        deleteDataSetsCommon(sessionToken, dataSetCodes, reason, type, forceNotExistingLocations,
                true, isTrashEnabled);
    }

    @SuppressWarnings("deprecation")
    private void deleteDataSetsCommon(String sessionToken, List<String> dataSetCodes,
            String reason, DeletionType type, boolean forceNotExistingLocations,
            boolean forceDisallowedTypes, boolean isTrashEnabled)
    {
        // TODO 2011-08-09, Piotr Buczek: simplify it when we remove the switch turning off trash
        // provide data set ids directly (no need to use codes)
        Session session = getSession(sessionToken);
        // NOTE: logical deletion and new implementation of permanent deletion doesn't use
        // IDataSetTypeSlaveServerPlugin (we have just 1 implementation!)
        switch (type)
        {
            case PERMANENT:
                if (isTrashEnabled)
                {
                    IDeletedDataSetTable deletedDataSetTable =
                            businessObjectFactory.createDeletedDataSetTable(session);
                    deletedDataSetTable.loadByDataSetCodes(dataSetCodes);
                    deletedDataSetTable.permanentlyDeleteLoadedDataSets(reason,
                            forceNotExistingLocations, forceDisallowedTypes);
                } else
                {
                    final IDataSetTable dataSetTable =
                            businessObjectFactory.createDataSetTable(session);
                    permanentlyDeleteDataSets(session, dataSetTable, dataSetCodes, reason,
                            forceNotExistingLocations, forceDisallowedTypes);
                }
                break;
            case TRASH:
                final IDataSetTable dataSetTable =
                        businessObjectFactory.createDataSetTable(session);
                dataSetTable.loadByDataSetCodes(dataSetCodes, false, false);
                List<DataPE> dataSets = dataSetTable.getDataSets();
                ITrashBO trashBO = businessObjectFactory.createTrashBO(session);
                trashBO.createDeletion(reason);
                trashBO.trashDataSets(TechId.createList(dataSets));
                break;
        }
    }

    @Override
    public void deleteSamples(String sessionToken, List<TechId> sampleIds, String reason,
            DeletionType deletionType)
    {
        Session session = getSession(sessionToken);
        switch (deletionType)
        {
            case PERMANENT:
                ISampleTable sampleTableBO = businessObjectFactory.createSampleTable(session);
                sampleTableBO.deleteByTechIds(sampleIds, reason);
                break;
            case TRASH:
                ITrashBO trashBO = businessObjectFactory.createTrashBO(session);
                trashBO.createDeletion(reason);
                trashBO.trashSamples(sampleIds);
                break;
        }
    }

    @Override
    public void deleteExperiments(String sessionToken, List<TechId> experimentIds, String reason,
            DeletionType deletionType)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        switch (deletionType)
        {
            case PERMANENT:
                experimentBO.deleteByTechIds(experimentIds, reason);
                break;
            case TRASH:
                ITrashBO trashBO = businessObjectFactory.createTrashBO(session);
                trashBO.createDeletion(reason);
                trashBO.trashExperiments(experimentIds);
                break;
        }
    }

    @Override
    public void deleteVocabularies(String sessionToken, List<TechId> vocabularyIds, String reason)
    {
        Session session = getSession(sessionToken);
        IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        for (TechId id : vocabularyIds)
        {
            vocabularyBO.deleteByTechId(id, reason);
        }
    }

    @Override
    public void deletePropertyTypes(String sessionToken, List<TechId> propertyTypeIds, String reason)
    {
        Session session = getSession(sessionToken);
        IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        for (TechId id : propertyTypeIds)
        {
            propertyTypeBO.deleteByTechId(id, reason);
        }
    }

    // TODO 2009-06-24 IA: add unit tests to project deletion (all layers)
    @Override
    public void deleteProjects(String sessionToken, List<TechId> projectIds, String reason)
    {
        Session session = getSession(sessionToken);
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        for (TechId id : projectIds)
        {
            projectBO.deleteByTechId(id, reason);
        }
    }

    @Override
    public void deleteSpaces(String sessionToken, List<TechId> groupIds, String reason)
    {
        Session session = getSession(sessionToken);
        ISpaceBO groupBO = businessObjectFactory.createSpaceBO(session);
        for (TechId id : groupIds)
        {
            groupBO.deleteByTechId(id, reason);
        }
    }

    @Override
    public void deleteScripts(String sessionToken, List<TechId> scriptIds)
    {
        Session session = getSession(sessionToken);
        IScriptBO scriptBO = businessObjectFactory.createScriptBO(session);
        for (TechId id : scriptIds)
        {
            scriptBO.deleteByTechId(id);
        }
    }

    @Override
    public void deleteExperimentAttachments(String sessionToken, TechId experimentId,
            List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        deleteHolderAttachments(session, experimentBO.getExperiment(), fileNames, reason);
    }

    @Override
    public void updateExperimentAttachments(String sessionToken, TechId experimentId,
            Attachment attachment)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
        attachmentBO.updateAttachment(experimentBO.getExperiment(), attachment);
        attachmentBO.save();
    }

    @Override
    public void addExperimentAttachment(String sessionToken, TechId experimentId,
            NewAttachment attachment)
    {
        Session session = getSession(sessionToken);
        IExperimentBO bo = businessObjectFactory.createExperimentBO(session);
        bo.loadDataByTechId(experimentId);
        bo.addAttachment(AttachmentTranslator.translate(attachment));
        bo.save();
    }

    @Override
    public void deleteSampleAttachments(String sessionToken, TechId sampleId,
            List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        deleteHolderAttachments(session, sampleBO.getSample(), fileNames, reason);
    }

    @Override
    public void deleteProjectAttachments(String sessionToken, TechId projectId,
            List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        projectBO.loadDataByTechId(projectId);
        deleteHolderAttachments(session, projectBO.getProject(), fileNames, reason);
    }

    private void deleteHolderAttachments(Session session, AttachmentHolderPE holder,
            List<String> fileNames, String reason) throws DataAccessException
    {
        IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
        attachmentBO.deleteHolderAttachments(holder, fileNames, reason);
    }

    @Override
    public List<Attachment> listExperimentAttachments(String sessionToken, TechId experimentId)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        return AttachmentTranslator.translate(
                listHolderAttachments(session, experimentBO.getExperiment()),
                session.getBaseIndexURL());
    }

    @Override
    public List<Attachment> listSampleAttachments(String sessionToken, TechId sampleId)
    {
        Session session = getSession(sessionToken);
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        return AttachmentTranslator.translate(listHolderAttachments(session, sampleBO.getSample()),
                session.getBaseIndexURL());
    }

    @Override
    public List<Attachment> listProjectAttachments(String sessionToken, TechId projectId)
    {
        Session session = getSession(sessionToken);
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        projectBO.loadDataByTechId(projectId);
        return AttachmentTranslator.translate(
                listHolderAttachments(session, projectBO.getProject()), session.getBaseIndexURL());
    }

    private List<AttachmentPE> listHolderAttachments(Session session, AttachmentHolderPE holder)
    {
        return getDAOFactory().getAttachmentDAO().listAttachments(holder);
    }

    @Override
    public String uploadDataSets(String sessionToken, List<String> dataSetCodes,
            DataSetUploadContext uploadContext)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(dataSetCodes, true, false);
        return dataSetTable.uploadLoadedDataSetsToCIFEX(uploadContext);
    }

    @Override
    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary)
    {
        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return vocabularyBO.countTermsUsageStatistics();
    }

    @Override
    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return VocabularyTermTranslator.translateTerms(vocabularyBO.enrichWithTerms());
    }

    @Override
    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        final List<DataSetTypePE> dataSetTypes = listEntityTypes(sessionToken, EntityKind.DATA_SET);
        return DataSetTypeTranslator.translate(dataSetTypes,
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    public LastModificationState getLastModificationState(String sessionToken)
    {
        checkSession(sessionToken);
        return lastModificationState;
    }

    @Override
    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleId != null : "Unspecified sample techId.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        sampleBO.enrichWithAttachments();
        sampleBO.enrichWithPropertyTypes();
        final SamplePE sample = sampleBO.getSample();
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL());
    }

    @Override
    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.update(updates);
        sampleBO.save();
        SampleUpdateResult result = new SampleUpdateResult();
        SamplePE sample = sampleBO.getSample();
        result.setModificationDate(sample.getModificationDate());
        List<String> parents = IdentifierExtractor.extract(sample.getParents());
        Collections.sort(parents);
        result.setParents(parents);
        return result;
    }

    @Override
    public Experiment getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadByExperimentIdentifier(identifier);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        if (experiment == null)
        {
            throw UserFailureException.fromTemplate(
                    "No experiment could be found with given identifier '%s'.", identifier);
        }
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    @Override
    public Experiment getExperimentInfo(final String sessionToken, final TechId experimentId)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();
        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    @Override
    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.update(updates);
        experimentBO.save();
        ExperimentUpdateResult result = new ExperimentUpdateResult();
        ExperimentPE experiment = experimentBO.getExperiment();
        result.setModificationDate(experiment.getModificationDate());
        result.setSamples(Code.extractCodes(experiment.getSamples()));
        return result;
    }

    @Override
    public Project getProjectInfo(String sessionToken, TechId projectId)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        bo.enrichWithAttachments();
        final ProjectPE project = bo.getProject();
        return ProjectTranslator.translate(project);
    }

    @Override
    public Project getProjectInfo(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadByProjectIdentifier(projectIdentifier);
        final ProjectPE project = bo.getProject();
        return ProjectTranslator.translate(project);
    }

    @Override
    public IIdHolder getProjectIdHolder(String sessionToken, String projectPermId)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadByPermId(projectPermId);
        final ProjectPE project = bo.getProject();
        return new TechId(project.getId());
    }

    @Override
    public Material getMaterialInfo(String sessionToken, final MaterialIdentifier identifier)
    {
        Session session = getSession(sessionToken);
        IMaterialBO materialBO = getBusinessObjectFactory().createMaterialBO(session);
        materialBO.loadByMaterialIdentifier(identifier);
        materialBO.enrichWithProperties();
        return MaterialTranslator.translate(materialBO.getMaterial());
    }

    @Override
    public Material getMaterialInfo(final String sessionToken, final TechId materialId)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.loadDataByTechId(materialId);
        materialBO.enrichWithProperties();
        final MaterialPE material = materialBO.getMaterial();
        return MaterialTranslator.translate(material, true);
    }

    @Override
    public IEntityInformationHolderWithPermId getMaterialInformationHolder(String sessionToken,
            MaterialIdentifier identifier)
    {
        return getMaterialInfo(sessionToken, identifier);
    }

    @Override
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, Date version)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.update(new MaterialUpdateDTO(materialId, properties, version));
        materialBO.save();
        return materialBO.getMaterial().getModificationDate();
    }

    @Override
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            final EntityKind entityKind, final String permId)
    {
        checkSession(sessionToken);
        switch (entityKind)
        {
            case DATA_SET:
                return createInformationHolder(entityKind, permId, getDAOFactory().getDataDAO()
                        .tryToFindDataSetByCode(permId));
            case SAMPLE:
            case EXPERIMENT:
                return createInformationHolder(entityKind, permId, getDAOFactory().getPermIdDAO()
                        .tryToFindByPermId(permId, DtoConverters.convertEntityKind(entityKind)));
            case MATERIAL:
                MaterialIdentifier identifier = MaterialIdentifier.tryParseIdentifier(permId);
                return getMaterialInformationHolder(sessionToken, identifier);
        }
        throw UserFailureException.fromTemplate("Operation not available for "
                + entityKind.getDescription() + "s");
    }

    private IEntityInformationHolderWithPermId createInformationHolder(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind kind, final String permId,
            IEntityInformationHolderDTO entityOrNull)
    {
        if (entityOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no %s with permId '%s'.",
                    kind.getDescription(), permId);
        }
        return createInformationHolder(kind, entityOrNull);
    }

    private IEntityInformationHolderWithPermId createInformationHolder(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind kind,
            IEntityInformationHolderDTO entity)
    {
        assert entity != null;
        final EntityType entityType =
                EntityHelper.createEntityType(kind, entity.getEntityType().getCode());
        final String code = entity.getCode();
        final Long id = HibernateUtils.getId(entity);
        final String permId = entity.getPermId();
        return new BasicEntityInformationHolder(kind, entityType, code, id, permId);
    }

    @Override
    public String generateCode(String sessionToken, String prefix, EntityKind entityKind)
    {
        checkSession(sessionToken);
        return prefix + getDAOFactory().getCodeSequenceDAO().getNextCodeSequenceId(entityKind);
    }

    @Override
    public Date updateProject(String sessionToken, ProjectUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.update(updates);
        bo.save();
        return bo.getProject().getModificationDate();
    }

    private void deleteEntityTypes(String sessionToken, EntityKind entityKind, List<String> codes)
            throws UserFailureException
    {
        final Session session = getSession(sessionToken);
        for (String code : codes)
        {
            IEntityTypeBO bo = businessObjectFactory.createEntityTypeBO(session);
            bo.load(DtoConverters.convertEntityKind(entityKind), code);
            bo.delete();
        }
    }

    @Override
    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.DATA_SET, entityTypesCodes);
    }

    @Override
    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.EXPERIMENT, entityTypesCodes);

    }

    @Override
    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.MATERIAL, entityTypesCodes);

    }

    @Override
    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes)
            throws UserFailureException
    {
        deleteEntityTypes(sessionToken, EntityKind.SAMPLE, entityTypesCodes);
    }

    @Override
    public void deleteFileFormatTypes(String sessionToken, List<String> codes)
            throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token";
        checkSession(sessionToken);
        IFileFormatTypeDAO dao = getDAOFactory().getFileFormatTypeDAO();
        for (String code : codes)
        {
            FileFormatTypePE type = dao.tryToFindFileFormatTypeByCode(code);
            if (type == null)
            {
                throw new UserFailureException(String.format("File format type '%s' not found.",
                        code));
            } else
            {
                try
                {
                    dao.delete(type);
                } catch (DataIntegrityViolationException ex)
                {
                    throw new UserFailureException(
                            String.format(
                                    "File format type '%s' is being used. Use 'Data Set Search' to find all connected data sets.",
                                    code));
                }
            }
        }
    }

    @Override
    public String getTemplateColumns(String sessionToken, EntityKind entityKind, String type,
            boolean autoGenerate, boolean withExperiments, boolean withSpace,
            BatchOperationKind operationKind)
    {
        List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        if ((entityKind.equals(EntityKind.SAMPLE) || entityKind.equals(EntityKind.DATA_SET) || entityKind
                .equals(EntityKind.MATERIAL))
                && EntityType.isDefinedInFileEntityTypeCode(type))
        {
            types.addAll(getDAOFactory().getEntityTypeDAO(
                    DtoConverters.convertEntityKind(entityKind)).listEntityTypes());
        } else
        {
            types.add(findEntityType(entityKind, type));
        }
        StringBuilder sb = new StringBuilder();
        boolean firstSection = true;
        for (EntityTypePE entityType : types)
        {
            String section =
                    createTemplateForType(entityKind, autoGenerate, entityType, firstSection,
                            withExperiments, withSpace, operationKind);
            if (types.size() != 1)
            {
                section =
                        String.format(
                                "[%s]\n%s%s\n",
                                entityType.getCode(),
                                firstSection ? "# Comments must be located after the type declaration ('[TYPE]').\n"
                                        : "", section);
            }
            sb.append(section);
            firstSection = false;
        }
        return sb.toString();
    }

    private static final String UPDATE_TEMPLATE_COMMENT =
            "# If one doesn't want to modify values in a column the column can be removed completely from the file.\n"
                    + "# Empty value in a column also means that the value stored in openBIS shouldn't be changed.\n"
                    + "# To delete a value/connection from openBIS one needs to put \"--DELETE--\" or \"__DELETE__\" into the corresponding cell.\n";

    private String createTemplateForType(EntityKind entityKind, boolean autoGenerate,
            EntityTypePE entityType, boolean addComments, boolean withExperiments,
            boolean withSpace, BatchOperationKind operationKind)
    {
        List<String> columns = new ArrayList<String>();
        switch (entityKind)
        {
            case SAMPLE:
                if (autoGenerate == false)
                {
                    columns.add(Identifier.IDENTIFIER_COLUMN);
                }
                columns.add(NewSample.CONTAINER);
                columns.add(NewSample.PARENTS);
                if (withExperiments)
                {
                    columns.add(NewSample.EXPERIMENT);
                }
                if (withSpace)
                {
                    columns.add(NewSample.SPACE);
                }
                addPropertiesToTemplateColumns(columns,
                        ((SampleTypePE) entityType).getSampleTypePropertyTypes());
                break;
            case DATA_SET:
                columns.add(Code.CODE);
                columns.add(NewDataSet.CONTAINER);
                columns.add(NewDataSet.PARENTS);
                columns.add(NewDataSet.EXPERIMENT);
                columns.add(NewDataSet.SAMPLE);
                columns.add(NewDataSet.FILE_FORMAT);
                addPropertiesToTemplateColumns(columns,
                        ((DataSetTypePE) entityType).getDataSetTypePropertyTypes());
                break;
            case MATERIAL:
                columns.add(Code.CODE);
                addPropertiesToTemplateColumns(columns,
                        ((MaterialTypePE) entityType).getMaterialTypePropertyTypes());
                break;
            case EXPERIMENT:
                columns.add(Identifier.IDENTIFIER_COLUMN);
                if (operationKind == BatchOperationKind.UPDATE)
                {
                    columns.add("project");
                }
                addPropertiesToTemplateColumns(columns,
                        ((ExperimentTypePE) entityType).getExperimentTypePropertyTypes());
                break;
        }
        StringBuilder sb = new StringBuilder();
        for (String column : columns)
        {
            if (sb.length() != 0)
            {
                sb.append("\t");
            }
            sb.append(column);
        }
        if (addComments)
        {
            switch (operationKind)
            {
                case REGISTRATION:
                    if (entityKind.equals(EntityKind.SAMPLE))
                    {
                        if (withSpace)
                        {
                            sb.insert(0, NewSample.WITH_SPACE_COMMENT);
                        }
                        if (withExperiments)
                        {
                            sb.insert(0, NewSample.WITH_EXPERIMENTS_COMMENT);
                        }
                        sb.insert(0, NewSample.SAMPLE_REGISTRATION_TEMPLATE_COMMENT);
                    }
                    break;
                case UPDATE:
                    if (entityKind.equals(EntityKind.SAMPLE))
                    {
                        sb.insert(0, UpdatedSample.SAMPLE_UPDATE_TEMPLATE_COMMENT);
                    } else if (entityKind.equals(EntityKind.DATA_SET))
                    {
                        sb.insert(0, UpdatedDataSet.DATASET_UPDATE_TEMPLATE_COMMENT);
                    } else
                    {
                        sb.insert(0, UPDATE_TEMPLATE_COMMENT);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    private <T extends EntityTypePropertyTypePE> void addPropertiesToTemplateColumns(
            List<String> columns, Set<T> propertyTypes)
    {
        List<T> sortedPropertyTypes = asSortedList(propertyTypes);
        for (EntityTypePropertyTypePE etpt : sortedPropertyTypes)
        {
            if (etpt.isDynamic() == false)
            {
                String code = etpt.getPropertyType().getCode();
                if (etpt.isManaged())
                {
                    String script = etpt.getScript().getScript();
                    ManagedPropertyEvaluator evaluator =
                            ManagedPropertyEvaluatorFactory.createManagedPropertyEvaluator(script);
                    List<String> batchColumnNames = evaluator.getBatchColumnNames();
                    if (batchColumnNames.isEmpty())
                    {
                        columns.add(code);
                    } else
                    {
                        for (String name : batchColumnNames)
                        {
                            columns.add(code + ':' + name);
                        }
                    }
                } else
                {
                    columns.add(code);
                }
            }
        }
    }

    private <T extends EntityTypePropertyTypePE> List<T> asSortedList(Set<T> propertyTypes)
    {
        List<T> list = new ArrayList<T>(propertyTypes);
        Collections.sort(list);
        return list;
    }

    private EntityTypePE findEntityType(EntityKind entityKind, String type)
    {
        EntityTypePE typeOrNull =
                getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind))
                        .tryToFindEntityTypeByCode(type);
        if (typeOrNull == null)
        {
            throw new UserFailureException("Unknown " + entityKind.name() + " type '" + type + "'");
        }
        return typeOrNull;
    }

    @Override
    public void updateFileFormatType(String sessionToken, AbstractType type)
    {
        checkSession(sessionToken);
        IFileFormatTypeDAO dao = getDAOFactory().getFileFormatTypeDAO();
        FileFormatTypePE typePE = dao.tryToFindFileFormatTypeByCode(type.getCode());
        typePE.setDescription(type.getDescription());
        dao.createOrUpdate(typePE);

    }

    @Override
    public void updateProjectAttachments(String sessionToken, TechId projectId,
            Attachment attachment)
    {
        Session session = getSession(sessionToken);
        IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
        attachmentBO.updateAttachment(bo.getProject(), attachment);
        attachmentBO.save();
    }

    @Override
    public void addProjectAttachments(String sessionToken, TechId projectId,
            NewAttachment attachment)
    {
        Session session = getSession(sessionToken);
        IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        bo.addAttachment(AttachmentTranslator.translate(attachment));
        bo.save();
    }

    @Override
    public void updateSampleAttachments(String sessionToken, TechId sampleId, Attachment attachment)
    {
        Session session = getSession(sessionToken);
        ISampleBO bo = businessObjectFactory.createSampleBO(session);
        bo.loadDataByTechId(sampleId);
        IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
        attachmentBO.updateAttachment(bo.getSample(), attachment);
        attachmentBO.save();
    }

    @Override
    public void addSampleAttachments(String sessionToken, TechId sampleId, NewAttachment attachment)
    {
        Session session = getSession(sessionToken);
        ISampleBO bo = businessObjectFactory.createSampleBO(session);
        bo.loadDataByTechId(sampleId);
        bo.addAttachment(AttachmentTranslator.translate(attachment));
        bo.save();
    }

    @Override
    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken,
            DataStoreServiceKind dataStoreServiceKind)
    {
        checkSession(sessionToken);

        List<DatastoreServiceDescription> result = new ArrayList<DatastoreServiceDescription>();
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            result.addAll(convertAndFilter(dataStore.getServices(), dataStoreServiceKind));
        }
        return result;
    }

    private static List<DatastoreServiceDescription> convertAndFilter(
            Set<DataStoreServicePE> services, DataStoreServiceKind dataStoreServiceKind)
    {
        List<DatastoreServiceDescription> result = new ArrayList<DatastoreServiceDescription>();
        for (DataStoreServicePE service : services)
        {
            if (service.getKind() == dataStoreServiceKind)
            {
                result.add(DataStoreServiceTranslator.translate(service));
            }
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        return dataSetTable.createReportFromDatasets(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), datasetCodes);
    }

    @Override
    public TableModel createReportFromAggregationService(String sessionToken,
            DatastoreServiceDescription serviceDescription, Map<String, Object> parameters)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        return dataSetTable.createReportFromAggregationService(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), parameters);
    }

    @Override
    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        Map<String, String> parameterBindings = new HashMap<String, String>();
        dataSetTable.processDatasets(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), datasetCodes, parameterBindings);
    }

    @Override
    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup)
    {
        Session session = getSession(sessionToken);
        IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.define(newAuthorizationGroup);
        bo.save();
    }

    @Override
    public void registerScript(String sessionToken, Script script)
    {
        Session session = getSession(sessionToken);
        IScriptBO bo = businessObjectFactory.createScriptBO(session);
        bo.define(script);
        bo.save();
    }

    @Override
    public void deleteAuthorizationGroups(String sessionToken, List<TechId> groupIds, String reason)
    {
        Session session = getSession(sessionToken);
        IAuthorizationGroupBO authGroupBO =
                businessObjectFactory.createAuthorizationGroupBO(session);
        for (TechId id : groupIds)
        {
            authGroupBO.deleteByTechId(id, reason);
        }
    }

    @Override
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken)
    {
        checkSession(sessionToken);
        final List<AuthorizationGroupPE> persons =
                getDAOFactory().getAuthorizationGroupDAO().list();
        Collections.sort(persons);
        return AuthorizationGroupTranslator.translate(persons);
    }

    @Override
    public List<Script> listScripts(String sessionToken, ScriptType scriptTypeOrNull,
            EntityKind entityKindOrNull)
    {
        checkSession(sessionToken);
        final List<ScriptPE> scripts =
                getDAOFactory().getScriptDAO().listEntities(scriptTypeOrNull, entityKindOrNull);
        Collections.sort(scripts);
        return ScriptTranslator.translate(scripts);
    }

    @Override
    public Date updateAuthorizationGroup(String sessionToken, AuthorizationGroupUpdates updates)
    {
        final Session session = getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.update(updates);
        bo.save();
        return bo.getAuthorizationGroup().getModificationDate();
    }

    @Override
    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId)
    {
        final Session session = getSession(sessionToken);
        IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizatonGroupId);
        return PersonTranslator.translate(bo.getAuthorizationGroup().getPersons());
    }

    @Override
    public void addPersonsToAuthorizationGroup(String sessionToken, TechId authorizationGroupId,
            List<String> personsCodes)
    {
        List<String> inexistent =
                addExistingPersonsToAuthorizationGroup(sessionToken, authorizationGroupId,
                        personsCodes);
        if (inexistent.size() > 0)
        {
            registerPersons(sessionToken, inexistent);
            addExistingPersonsToAuthorizationGroup(sessionToken, authorizationGroupId, inexistent);
        }
    }

    private List<String> addExistingPersonsToAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes)
    {
        final Session session = getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizationGroupId);
        List<String> inexistent = bo.addPersons(personsCodes);
        bo.save();
        return inexistent;
    }

    @Override
    public void removePersonsFromAuthorizationGroup(String sessionToken,
            TechId authorizationGroupId, List<String> personsCodes)
    {
        final Session session = getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizationGroupId);
        bo.removePersons(personsCodes);
        bo.save();
    }

    // --- grid custom filters and columns

    private IGridCustomFilterOrColumnBO createGridCustomColumnBO(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        return businessObjectFactory.createGridCustomColumnBO(session);
    }

    private IGridCustomFilterOrColumnBO createGridCustomFilterBO(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        return businessObjectFactory.createGridCustomFilterBO(session);
    }

    private void registerFilterOrColumn(NewColumnOrFilter filter, IGridCustomFilterOrColumnBO bo)
    {
        bo.define(filter);
        bo.save();
    }

    private void deleteFiltersOrColumns(List<TechId> filterIds, IGridCustomFilterOrColumnBO bo)
    {
        for (TechId id : filterIds)
        {
            bo.deleteByTechId(id);
        }
    }

    @Override
    public List<GridCustomFilter> listFilters(String sessionToken, String gridId)
    {
        checkSession(sessionToken);
        List<GridCustomFilterPE> filters =
                getDAOFactory().getGridCustomFilterDAO().listFilters(gridId);
        Collections.sort(filters);
        return GridCustomFilterTranslator.translate(filters);
    }

    @Override
    public void registerFilter(String sessionToken, NewColumnOrFilter filter)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomFilterBO(sessionToken);
        registerFilterOrColumn(filter, bo);
    }

    @Override
    public void deleteFilters(String sessionToken, List<TechId> filterIds)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomFilterBO(sessionToken);
        deleteFiltersOrColumns(filterIds, bo);
    }

    @Override
    public void updateFilter(String sessionToken, IExpressionUpdates updates)
    {
        assert updates != null : "Unspecified updates";
        createGridCustomFilterBO(sessionToken).update(updates);
    }

    // -- columns

    @Override
    public void registerGridCustomColumn(String sessionToken, NewColumnOrFilter column)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomColumnBO(sessionToken);
        registerFilterOrColumn(column, bo);
    }

    @Override
    public void deleteGridCustomColumns(String sessionToken, List<TechId> columnIds)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomColumnBO(sessionToken);
        deleteFiltersOrColumns(columnIds, bo);
    }

    @Override
    public void updateGridCustomColumn(String sessionToken, IExpressionUpdates updates)
    {
        assert updates != null : "Unspecified updates";
        createGridCustomColumnBO(sessionToken).update(updates);
    }

    // --

    @Override
    public void keepSessionAlive(String sessionToken) throws UserFailureException
    {
        checkSession(sessionToken);
    }

    @Override
    public void updateVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> terms)
    {
        Session session = getSession(sessionToken);
        IVocabularyBO bo = getBusinessObjectFactory().createVocabularyBO(session);
        bo.loadDataByTechId(vocabularyId);
        bo.updateTerms(terms);
        bo.save();
    }

    @Override
    public void deleteMaterials(String sessionToken, List<TechId> materialIds, String reason)
    {
        Session session = getSession(sessionToken);
        IMaterialTable materialTable = businessObjectFactory.createMaterialTable(session);
        materialTable.deleteByTechIds(materialIds, reason);
    }

    @Override
    public int lockDatasets(String sessionToken, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.lockDatasets();
    }

    @Override
    public int unlockDatasets(String sessionToken, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.unlockDatasets();
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(String sessionToken,
            DatastoreServiceDescription serviceDescription, String dataSetCode)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        return dataSetTable.retrieveLinkFromDataSet(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), dataSetCode);
    }

    @Override
    public Script getScriptInfo(String sessionToken, TechId scriptId)
    {
        getSession(sessionToken);
        ScriptPE script = getDAOFactory().getScriptDAO().getByTechId(scriptId);
        return ScriptTranslator.translate(script);
    }

    @Override
    public String evaluate(String sessionToken, DynamicPropertyEvaluationInfo info)
    {
        Session session = getSession(sessionToken);
        IEntityInformationWithPropertiesHolder entity = getEntity(info, session);
        try
        {
            DynamicPropertyCalculator calculator =
                    DynamicPropertyCalculator.create(info.getScript());
            IDynamicPropertyEvaluator evaluator =
                    new DynamicPropertyEvaluator(getDAOFactory(), null);
            IEntityAdaptor adaptor = EntityAdaptorFactory.create(entity, evaluator);
            calculator.setEntity(adaptor);
            return calculator.evalAsString();
        } catch (Throwable e)
        {
            // return error message if there is a problem with evaluation
            return e.getMessage();
        }
    }

    @Override
    public String evaluate(String sessionToken, EntityValidationEvaluationInfo info)
    {
        Session session = getSession(sessionToken);
        IEntityInformationWithPropertiesHolder entity = getEntity(info, session);
        try
        {
            EntityValidationCalculator calculator =
                    EntityValidationCalculator.create(info.getScript());
            IDynamicPropertyEvaluator evaluator =
                    new DynamicPropertyEvaluator(getDAOFactory(), null);
            IEntityAdaptor adaptor = EntityAdaptorFactory.create(entity, evaluator);
            calculator.setEntity(adaptor);
            calculator.setIsNewEntity(info.isNew());
            return calculator.evalAsString();
        } catch (Throwable e)
        {
            // return error message if there is a problem with evaluation
            return e.getMessage();
        }
    }

    @Override
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            BasicEntityDescription info)
    {
        Session session = getSession(sessionToken);
        IEntityInformationWithPropertiesHolder entity = getEntity(info, session);
        return createInformationHolder(info.getEntityKind(), entity);
    }

    private IEntityInformationWithPropertiesHolder getEntity(BasicEntityDescription info,
            Session session)
    {
        IEntityInformationWithPropertiesHolder entity = null;
        String entityIdentifier = info.getEntityIdentifier();
        EntityKind entityKind = info.getEntityKind();
        switch (entityKind)
        {
            case DATA_SET:
                IDataBO bo = businessObjectFactory.createDataBO(session);
                bo.loadByCode(entityIdentifier);
                entity = bo.getData();
                break;
            case EXPERIMENT:
                IExperimentBO expBO = businessObjectFactory.createExperimentBO(session);
                ExperimentIdentifier expIdentifier =
                        new ExperimentIdentifierFactory(entityIdentifier).createIdentifier();
                entity = expBO.tryFindByExperimentIdentifier(expIdentifier);
                break;
            case SAMPLE:
                ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
                sampleBO.tryToLoadBySampleIdentifier(SampleIdentifierFactory
                        .parse(entityIdentifier));
                entity = sampleBO.tryToGetSample();
                break;
            case MATERIAL:
                entity =
                        getDAOFactory().getMaterialDAO().tryFindMaterial(
                                MaterialIdentifier.tryParseIdentifier(entityIdentifier));
                break;
        }
        if (entity == null)
        {
            throw new UserFailureException(String.format("%s '%s' not found",
                    entityKind.getDescription(), entityIdentifier));
        }
        return entity;
    }

    @Override
    public void updateManagedPropertyOnExperiment(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);

        // Evaluate the script
        experimentBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = experimentBO.getExperiment().getProperties();
        ManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty, updateAction);

        experimentBO.updateManagedProperty(managedProperty);
        experimentBO.save();
    }

    @Override
    public void updateManagedPropertyOnSample(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(experimentId);

        // Evaluate the script
        sampleBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = sampleBO.getSample().getProperties();
        ManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty, updateAction);

        sampleBO.updateManagedProperty(managedProperty);
        sampleBO.save();
    }

    @Override
    public void updateManagedPropertyOnDataSet(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        IDataBO dataSetBO = businessObjectFactory.createDataBO(session);
        dataSetBO.loadDataByTechId(experimentId);

        // Evaluate the script
        dataSetBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = dataSetBO.getData().getProperties();
        ManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty, updateAction);

        dataSetBO.updateManagedProperty(managedProperty);
        dataSetBO.save();
    }

    @Override
    public void updateManagedPropertyOnMaterial(String sessionToken, TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.loadDataByTechId(experimentId);

        // Evaluate the script
        materialBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = materialBO.getMaterial().getProperties();
        ManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty, updateAction);

        materialBO.updateManagedProperty(managedProperty);
        materialBO.save();
    }

    private static void extendWithPerson(IManagedUiAction updateAction, PersonPE personOrNull)
    {
        if (personOrNull != null && updateAction instanceof ManagedUiActionDescription)
        {
            final String userId = personOrNull.getUserId();
            String userName = userId;
            if (personOrNull.getFirstName() != null && personOrNull.getLastName() != null)
            {
                userName = personOrNull.getFirstName() + " " + personOrNull.getLastName();
            }
            final IPerson person = new PersonAdapter(userId, userName);
            final ManagedUiActionDescription action = (ManagedUiActionDescription) updateAction;
            action.setPerson(person);
        }
    }

    private ManagedPropertyEvaluator tryManagedPropertyEvaluator(IManagedProperty managedProperty,
            Set<? extends EntityPropertyPE> properties)
    {
        String managedPropertyCode = managedProperty.getPropertyTypeCode();

        EntityPropertyPE managedPropertyPE = null;
        for (EntityPropertyPE property : properties)
        {
            if (property.getEntityTypePropertyType().getPropertyType().getCode()
                    .equals(managedPropertyCode))
            {
                managedPropertyPE = property;
            }
        }
        if (null == managedPropertyPE)
        {
            return null;

        }

        return ManagedPropertyEvaluatorFactory.createManagedPropertyEvaluator(managedPropertyPE
                .getEntityTypePropertyType().getScript().getScript());
    }

    @Override
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        checkSession(sessionToken);
        IDataStoreDAO dataStoreDAO = getDAOFactory().getDataStoreDAO();
        List<DataStorePE> dataStores = dataStoreDAO.listDataStores();
        if (dataStores.size() != 1)
        {
            throw EnvironmentFailureException
                    .fromTemplate(
                            "Expected exactly one Data Store Server to be registered in openBIS but found %s.",
                            dataStores.size());
        }
        return dataStores.get(0).getDownloadUrl();
    }

    @Override
    public void updateDataSetProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        checkSession(sessionToken);
        ExternalData dataSet = getDataSetInfo(sessionToken, entityId);
        try
        {
            DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
            updates.setDatasetId(entityId);
            updates.setVersion(dataSet.getModificationDate());
            Map<String, String> properties = createPropertiesMap(modifiedProperties);
            updates.setProperties(EntityHelper.translatePropertiesMapToList(properties));
            Experiment exp = dataSet.getExperiment();
            if (exp != null)
            {
                updates.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(exp
                        .getIdentifier()));
            }
            String sampleIdentifier = dataSet.getSampleIdentifier();
            if (sampleIdentifier != null)
            {
                updates.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sampleIdentifier));
            }
            if (dataSet instanceof DataSet)
            {
                updates.setFileFormatTypeCode(((DataSet) dataSet).getFileFormatType().getCode());
            }
            updateDataSet(sessionToken, updates);
        } catch (UserFailureException e)
        {
            throw wrapExceptionWithEntityIdentifier(e, dataSet);
        }
    }

    @Override
    public void updateExperimentProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        checkSession(sessionToken);
        Experiment experiment = getExperimentInfo(sessionToken, entityId);
        try
        {
            ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
            updates.setVersion(experiment.getModificationDate());
            updates.setExperimentId(entityId);
            updates.setAttachments(Collections.<NewAttachment> emptySet());
            updates.setProjectIdentifier(new ProjectIdentifierFactory(experiment.getProject()
                    .getIdentifier()).createIdentifier());
            Map<String, String> properties = createPropertiesMap(modifiedProperties);
            updates.setProperties(EntityHelper.translatePropertiesMapToList(properties));
            updateExperiment(sessionToken, updates);
        } catch (UserFailureException e)
        {
            throw wrapExceptionWithEntityIdentifier(e, experiment);
        }
    }

    @Override
    public void updateSampleProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        checkSession(sessionToken);
        Map<String, String> properties = createPropertiesMap(modifiedProperties);
        Sample sample = getSampleInfo(sessionToken, entityId).getParent();
        try
        {
            EntityHelper.updateSampleProperties(this, sessionToken, sample, properties);
        } catch (UserFailureException e)
        {
            throw wrapExceptionWithEntityIdentifier(e, sample);
        }
    }

    @Override
    public void updateMaterialProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        checkSession(sessionToken);
        Date modificationDate =
                getDAOFactory().getMaterialDAO().tryGetByTechId(entityId).getModificationDate();
        Map<String, String> properties = createPropertiesMap(modifiedProperties);
        updateMaterial(sessionToken, entityId,
                EntityHelper.translatePropertiesMapToList(properties), modificationDate);
    }

    private Map<String, String> createPropertiesMap(List<PropertyUpdates> updates)
    {
        Map<String, String> properties = new HashMap<String, String>();
        for (PropertyUpdates p : updates)
        {
            properties.put(CodeConverter.getPropertyTypeCode(p.getPropertyCode()), p.getValue());
        }
        return properties;
    }

    @Override
    public final List<Deletion> listDeletions(final String sessionToken, boolean withDeletedEntities)
    {
        Session session = getSession(sessionToken);
        IDeletionTable deletionTable = businessObjectFactory.createDeletionTable(session);
        deletionTable.load(withDeletedEntities);
        return deletionTable.getDeletions();
    }

    @Override
    public final void revertDeletions(final String sessionToken, final List<TechId> deletionIds)
    {
        final Session session = getSession(sessionToken);

        final ITrashBO trashBO = getBusinessObjectFactory().createTrashBO(session);
        for (TechId deletionId : deletionIds)
        {
            trashBO.revertDeletion(deletionId);
        }
    }

    @Override
    public final void deletePermanently(final String sessionToken, final List<TechId> deletionIds,
            boolean forceNotExistingLocations)
    {
        deletePermanentlyCommon(sessionToken, deletionIds, forceNotExistingLocations, false);
    }

    @Override
    public void deletePermanentlyForced(String sessionToken, List<TechId> deletionIds,
            boolean forceNotExistingLocations)
    {
        deletePermanentlyCommon(sessionToken, deletionIds, forceNotExistingLocations, true);
    }

    private void deletePermanentlyCommon(String sessionToken, List<TechId> deletionIds,
            boolean forceNotExistingLocations, boolean forceDisallowedTypes)
    {
        checkSession(sessionToken);

        IDeletionDAO deletionDAO = getDAOFactory().getDeletionDAO();
        // NOTE: we can't do bulk deletions to preserve original reasons
        for (TechId deletionId : deletionIds)
        {
            DeletionPE deletion = deletionDAO.getByTechId(deletionId);
            String deletionReason = deletion.getReason();
            DeletionType deletionType = DeletionType.PERMANENT;

            List<TechId> singletonList = Collections.singletonList(deletionId);
            List<String> trashedDataSets = deletionDAO.findTrashedDataSetCodes(singletonList);
            deleteDataSetsCommon(sessionToken, trashedDataSets, deletionReason, deletionType,
                    forceNotExistingLocations, forceDisallowedTypes, true);

            // we need to first delete components and then containers not to break constraints
            List<TechId> trashedComponentSamples =
                    deletionDAO.findTrashedComponentSampleIds(singletonList);
            deleteSamples(sessionToken, trashedComponentSamples, deletionReason, deletionType);
            List<TechId> trashedNonComponentSamples =
                    deletionDAO.findTrashedNonComponentSampleIds(singletonList);
            deleteSamples(sessionToken, trashedNonComponentSamples, deletionReason, deletionType);

            List<TechId> trashedExperiments = deletionDAO.findTrashedExperimentIds(singletonList);
            deleteExperiments(sessionToken, trashedExperiments, deletionReason, deletionType);

            // WORKAROUND to get the fresh deletion and fix org.hibernate.NonUniqueObjectException
            DeletionPE freshDeletion = deletionDAO.getByTechId(TechId.create(deletion));
            deletionDAO.delete(freshDeletion);
        }
    }

    private static UserFailureException wrapExceptionWithEntityIdentifier(
            UserFailureException exception, IEntityInformationHolderWithIdentifier entity)
    {
        return UserFailureException.fromTemplate(exception, "%s '%s': %s", entity.getEntityKind()
                .getDescription(), entity.getIdentifier(), exception.getMessage());
    }

    @Override
    public void registerPlugin(String sessionToken, CorePlugin plugin,
            ICorePluginResourceLoader resourceLoader)
    {
        Session session = getSession(sessionToken);
        EncapsulatedCommonServer encapsulated = EncapsulatedCommonServer.create(this, sessionToken);
        MasterDataRegistrationScriptRunner scriptRunner =
                new MasterDataRegistrationScriptRunner(encapsulated);

        ICorePluginTable pluginTable =
                businessObjectFactory.createCorePluginTable(session, scriptRunner);
        pluginTable.registerPlugin(plugin, resourceLoader);
    }

    @Override
    public List<DataStore> listDataStores()
    {
        IDataStoreDAO dataStoreDAO = getDAOFactory().getDataStoreDAO();
        List<DataStorePE> dataStorePEs = dataStoreDAO.listDataStores();
        return DataStoreTranslator.translate(dataStorePEs);
    }

    @Override
    public List<Material> searchForMaterials(String sessionToken, DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        SearchHelper searchHelper =
                new SearchHelper(session, businessObjectFactory, getDAOFactory());
        return searchHelper.searchForMaterials(criteria);
    }

    @Override
    public String performCustomImport(String sessionToken, String customImportCode,
            CustomImportFile customImportFile)
    {
        Session session = getSession(sessionToken);

        SectionProperties[] sectionProperties =
                PropertyParametersUtil.extractSectionProperties(configurer.getResolvedProps(),
                        CustomImport.PropertyNames.CUSTOM_IMPORTS.getName(), false);

        for (SectionProperties props : sectionProperties)
        {
            if (props.getKey().equals(customImportCode))
            {
                String dssCode =
                        props.getProperties().getProperty(
                                CustomImport.PropertyNames.DATASTORE_CODE.getName());
                String dropboxName =
                        props.getProperties().getProperty(
                                CustomImport.PropertyNames.DROPBOX_NAME.getName());
                IDataStoreBO dataStore = businessObjectFactory.createDataStoreBO(session);
                dataStore.loadByCode(dssCode);
                dataStore.uploadFile(dropboxName, customImportFile);
                return customImportFile.getFileName();
            }
        }

        throw new UserFailureException(String.format("Cannot upload file '%s' to the dss.",
                customImportFile.getFileName()));
    }

    @Override
    public void sendCountActiveUsersEmail(String sessionToken)
    {
        Session session = getSession(sessionToken);
        String email = session.getUserEmail();
        String hostName = null;
        DatabaseConfigurationContext ctx = DatabaseContextUtils.getDatabaseContext(getDAOFactory());
        try
        {
            hostName = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex)
        {
            // impossible
            operationLog.warn("Couldn't get the hostname.");
        }

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Number of active users: ").append(countActivePersons(sessionToken))
                .append("\n").append("Hostname: ").append(hostName).append("\n")
                .append("Database instance: ").append(ctx.getDatabaseInstance()).append(" [name=")
                .append(ctx.getDatabaseName()).append("]").append("\n");

        final IMailClient mailClient = new MailClient(mailClientParameters);
        sendEmail(mailClient, emailBody.toString(), "Number of active users", CISDHelpdeskEmail,
                email);
    }

    @Override
    public List<ExternalDataManagementSystem> listExternalDataManagementSystems(String sessionToken)
    {
        checkSession(sessionToken);

        ArrayList<ExternalDataManagementSystem> results =
                new ArrayList<ExternalDataManagementSystem>();
        for (ExternalDataManagementSystemPE edms : getDAOFactory()
                .getExternalDataManagementSystemDAO().listExternalDataManagementSystems())
        {
            results.add(ExternalDataManagementSystemTranslator.translate(edms));
        }

        return results;
    }

    @Override
    public ExternalDataManagementSystem getExternalDataManagementSystem(String sessionToken,
            String code)
    {
        checkSession(sessionToken);

        return ExternalDataManagementSystemTranslator.translate(getDAOFactory()
                .getExternalDataManagementSystemDAO().tryToFindExternalDataManagementSystemByCode(
                        code));
    }

    @Override
    public void createOrUpdateExternalDataManagementSystem(String sessionToken,
            ExternalDataManagementSystem edms)
    {
        checkSession(sessionToken);

        IDAOFactory daoFactory = getDAOFactory();
        IExternalDataManagementSystemDAO edmsDAO = daoFactory.getExternalDataManagementSystemDAO();
        ExternalDataManagementSystemPE edmsPE =
                edmsDAO.tryToFindExternalDataManagementSystemByCode(edms.getCode());
        if (edmsPE == null)
        {
            edmsPE = new ExternalDataManagementSystemPE();
            edmsPE.setDatabaseInstance(daoFactory.getHomeDatabaseInstance());
        }

        edmsDAO.createOrUpdateExternalDataManagementSystem(ExternalDataManagementSystemTranslator
                .translate(edms, edmsPE));
    }
}
