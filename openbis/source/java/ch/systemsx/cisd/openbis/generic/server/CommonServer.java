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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractExpressionPredicate.DeleteGridCustomColumnPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractExpressionPredicate.DeleteGridCustomFilterPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractExpressionPredicate.UpdateGridCustomColumnPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractExpressionPredicate.UpdateGridCustomFilterPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ExperimentTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ProjectTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.SpaceTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.BasicEntityDescriptionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DeletionTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectPermIdStringPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ProjectUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdCollectionReadWritePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdReadWritePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SpaceUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DeletionValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.EntityHistoryValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExpressionValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExternalDataValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.MatchingEntityValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SamplePropertyAccessValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SearchDomainSearchResultValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceOrProjectValidator;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.EntityCodeGenerator;
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
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMetaprojectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IProjectBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IScriptBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISearchDomainSearcher;
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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMetaprojectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelatedEntityFinder;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.HibernateSearchDataProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.SampleDataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.EntityAdaptorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.INonAbstractEntityAdapter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.JythonEntityValidationCalculator.IValidationRequestDelegate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.IEntityValidatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.entity_validation.api.IEntityValidator;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationScriptRunner;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchDomain;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IdentifierExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IPerson;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IManagedPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.AuthorizationGroupTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreServiceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataStoreTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityHistoryTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExperimentTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataManagementSystemTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomFilterTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MaterialTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.MetaprojectTranslator;
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
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

public final class CommonServer extends AbstractCommonServer<ICommonServerForInternalUse> implements
        ICommonServerForInternalUse
{
    private static final Comparator<TechId> TECH_ID_COMPARATOR = new Comparator<TechId>()
    {
        @Override
        public int compare(TechId o1, TechId o2)
        {
            long id1 = o1.getId();
            long id2 = o2.getId();
            return id1 < id2 ? -1 : (id1 > id2 ? 1 : 0);
        }
    };

    private final LastModificationState lastModificationState;

    private final IDataStoreServiceRegistrator dataStoreServiceRegistrator;

    private final IEntityValidatorFactory entityValidationFactory;

    private final IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory;

    private final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    private final IConcurrentOperationLimiter operationLimiter;

    private String defaultPutDataStoreServerCodeOrNull;

    protected ICommonServer commonServer;

    public CommonServer(final IAuthenticationService authenticationService,
            final IOpenBisSessionManager sessionManager, final IDAOFactory daoFactory,
            final ICommonBusinessObjectFactory businessObjectFactory,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator,
            final LastModificationState lastModificationState,
            IEntityValidatorFactory entityValidationFactory,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, IConcurrentOperationLimiter operationLimiter)
    {
        this(authenticationService, sessionManager, daoFactory, null, businessObjectFactory,
                dataStoreServiceRegistrator, lastModificationState, entityValidationFactory,
                dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory, operationLimiter);
    }

    CommonServer(final IAuthenticationService authenticationService,
            final IOpenBisSessionManager sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final ICommonBusinessObjectFactory businessObjectFactory,
            IDataStoreServiceRegistrator dataStoreServiceRegistrator,
            final LastModificationState lastModificationState,
            IEntityValidatorFactory entityValidationFactory,
            IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory, IConcurrentOperationLimiter operationLimiter)
    {
        super(authenticationService, sessionManager, daoFactory, propertiesBatchManager,
                businessObjectFactory);
        this.dataStoreServiceRegistrator = dataStoreServiceRegistrator;
        this.lastModificationState = lastModificationState;
        this.entityValidationFactory = entityValidationFactory;
        this.dynamicPropertyCalculatorFactory = dynamicPropertyCalculatorFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
        this.operationLimiter = operationLimiter;
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
    // IGenericServer
    //

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SimpleSpaceOrProjectValidator.class)
    public List<Space> listSpaces(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        final List<SpacePE> spaces = getDAOFactory().getSpaceDAO().listSpaces();
        final SpacePE homeSpaceOrNull = session.tryGetHomeGroup();
        for (final SpacePE space : spaces)
        {
            space.setHome(space.equals(homeSpaceOrNull));
        }
        Collections.sort(spaces);
        return SpaceTranslator.translate(spaces);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("REGISTER_SPACE")
    public void registerSpace(String sessionToken, String spaceCode, String descriptionOrNull)
    {
        final Session session = getSession(sessionToken);
        final ISpaceBO spaceBO = businessObjectFactory.createSpaceBO(session);
        spaceBO.define(spaceCode, descriptionOrNull);
        spaceBO.save();
        // If the user who registers this space is _not_ instance admin, make him space admin for
        // the freshly created space.
        PersonPE person = session.tryGetPerson();
        if (person != null
                && person.isSystemUser() == false
                && new AuthorizationServiceUtils(getDAOFactory(), session.getUserName())
                .doesUserHaveRole(RoleCode.ADMIN.toString(), null) == false)
        {
            registerSpaceRole(sessionToken, RoleCode.ADMIN, new SpaceIdentifier(spaceCode),
                    Grantee.createPerson(session.getUserName()));
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public ScriptUpdateResult updateScript(final String sessionToken, final IScriptUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IScriptBO bo = businessObjectFactory.createScriptBO(session);
        bo.update(updates);

        ScriptUpdateResult result = new ScriptUpdateResult();
        result.setModificationDate(bo.getScript().getModificationDate());
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("UPDATE_SPACE")
    public void updateSpace(final String sessionToken,
            @AuthorizationGuard(guardClass = SpaceUpdatesPredicate.class) final ISpaceUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final ISpaceBO spaceBO = businessObjectFactory.createSpaceBO(session);
        spaceBO.update(updates);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerPerson(String sessionToken, String userID)
    {
        registerPersons(sessionToken, Arrays.asList(userID));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    public List<RoleAssignment> listRoleAssignments(String sessionToken)
    {
        checkSession(sessionToken);
        final List<RoleAssignmentPE> roles =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        return RoleAssignmentTranslator.translate(roles);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    public void registerProjectRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) ProjectIdentifier projectIdentifier, Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setProjectIdentifier(projectIdentifier);

        registerRole(roleCode, grantee, session, newRoleAssignment);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    public void registerSpaceRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) SpaceIdentifier spaceIdentifier, Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setSpaceIdentifier(spaceIdentifier);

        registerRole(roleCode, grantee, session, newRoleAssignment);

    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerInstanceRole(String sessionToken, RoleCode roleCode, Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        registerRole(roleCode, grantee, session, newRoleAssignment);
    }

    protected void registerRole(RoleCode roleCode, Grantee grantee, final Session session, final NewRoleAssignment newRoleAssignment)
    {
        newRoleAssignment.setGrantee(grantee);
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    public void deleteProjectRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) ProjectIdentifier projectIdentifier, Grantee grantee)
    {
        final Session session = getSession(sessionToken);

        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindProjectRoleAssignment(roleCode,
                        projectIdentifier, grantee);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given project role does not exist.");
        }

        final PersonPE personPE = session.tryGetPerson();

        if (roleAssignment.getPerson() != null && roleAssignment.getPerson().equals(personPE)
                && roleAssignment.getRole().equals(RoleCode.ADMIN))
        {
            boolean isInstanceAdmin = false;
            boolean isSpaceAdmin = false;

            for (final RoleAssignmentPE userAssignment : personPE.getRoleAssignments())
            {
                if (userAssignment.getRoleWithHierarchy().isInstanceLevel() && userAssignment.getRole().equals(RoleCode.ADMIN))
                {
                    isInstanceAdmin = true;
                } else if (userAssignment.getRoleWithHierarchy().isSpaceLevel()
                        && userAssignment.getSpace().getCode().equals(roleAssignment.getProject().getSpace().getCode())
                        && userAssignment.getRole().equals(RoleCode.ADMIN))
                {
                    isSpaceAdmin = true;
                }
            }

            if (isInstanceAdmin == false && isSpaceAdmin == false)
            {
                throw new UserFailureException(
                        "For safety reason you cannot give away your own project admin power. "
                                + "Ask space or instance admin to do that for you.");
            }
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    public void deleteSpaceRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) SpaceIdentifier spaceIdentifier, Grantee grantee)
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

            for (final RoleAssignmentPE userAssignment : personPE.getRoleAssignments())
            {
                if (userAssignment.getRoleWithHierarchy().isInstanceLevel() && userAssignment.getRole().equals(RoleCode.ADMIN))
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteInstanceRole(String sessionToken, RoleCode roleCode, Grantee grantee)
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
                && roleAssignment.getRoleWithHierarchy().isInstanceLevel())
        {
            throw new UserFailureException(
                    "For safety reason you cannot give away your own omnipotence. "
                            + "Ask another instance admin to do that for you.");
        }
        roleAssignmentDAO.deleteRoleAssignment(roleAssignment);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    public List<Person> listPersons(String sessionToken)
    {
        checkSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();
        Collections.sort(persons);
        return PersonTranslator.translate(persons);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Person> listActivePersons(String sessionToken)
    {
        checkSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listActivePersons();
        Collections.sort(persons);
        return PersonTranslator.translate(persons);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ProjectValidator.class)
    public List<Project> listProjects(String sessionToken)
    {
        checkSession(sessionToken);
        final List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects();
        Collections.sort(projects);
        return ProjectTranslator.translate(projects);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<SampleType> listSampleTypes(String sessionToken)
    {
        checkSession(sessionToken);
        final List<SampleTypePE> sampleTypes = getDAOFactory().getSampleTypeDAO().listSampleTypes();
        Collections.sort(sampleTypes);
        List<SampleType> translateSampleTypes =
                SampleTypeTranslator.translate(sampleTypes, new HashMap<MaterialTypePE, MaterialType>(),
                        new HashMap<PropertyTypePE, PropertyType>());

        return translateSampleTypes;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Map<String, List<IManagedInputWidgetDescription>> listManagedInputWidgetDescriptions(
            String sessionToken, EntityKind entityKind, String entityTypeCode)
    {
        checkSession(sessionToken);

        EntityTypePE entityTypePE =
                getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind))
                        .tryToFindEntityTypeByCode(entityTypeCode);

        EntityType entityType = EntityTypeTranslator.translate(entityTypePE);

        List<? extends EntityTypePropertyType<?>> assignedPropertyTypes =
                entityType.getAssignedPropertyTypes();

        return listManagedInputWidgetDescriptions(assignedPropertyTypes);
    }

    private Map<String, List<IManagedInputWidgetDescription>> listManagedInputWidgetDescriptions(
            List<? extends EntityTypePropertyType<?>> propertyTypes)
    {
        Map<String, List<IManagedInputWidgetDescription>> result =
                new HashMap<String, List<IManagedInputWidgetDescription>>();
        for (EntityTypePropertyType<?> entityTypePropertyType : propertyTypes)
        {
            String propertyTypeCode = entityTypePropertyType.getPropertyType().getCode();
            if (entityTypePropertyType.isManaged())
            {
                IManagedPropertyEvaluator evaluator =
                        managedPropertyEvaluatorFactory
                                .createManagedPropertyEvaluator(entityTypePropertyType);
                List<IManagedInputWidgetDescription> inputWidgetDescriptions =
                        evaluator.getInputWidgetDescriptions();
                if (inputWidgetDescriptions.isEmpty() == false)
                {
                    result.put(propertyTypeCode, inputWidgetDescriptions);
                }
            }
        }
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> listSamples(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class) final ListSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        return sampleLister.list(new ListOrSearchSampleCriteria(criteria));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Sample> listMetaprojectSamples(final String sessionToken,
            IMetaprojectId metaprojectId)
    {
        final Session session = getSession(sessionToken);
        final AuthorizationServiceUtils authorization = getAuthorizationService(session);
        final Metaproject metaproject = getMetaproject(sessionToken, metaprojectId);
        final ISampleLister lister = businessObjectFactory.createSampleLister(session);

        List<Sample> samples =
                lister.list(new ListOrSearchSampleCriteria(new MetaprojectCriteria(metaproject
                        .getId())));
        List<Sample> translatedSamples = new ArrayList<Sample>(samples.size());

        for (Sample sample : samples)
        {
            if (authorization.canAccessSample(sample))
            {
                translatedSamples.add(sample);
            } else
            {
                translatedSamples.add(SampleTranslator.translateWithoutRevealingData(sample));
            }
        }

        return translatedSamples;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<Sample> listSamplesOnBehalfOfUser(final String sessionToken,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class) final ListSampleCriteria criteria, String userId)
    {
        final Session session = getSession(sessionToken);

        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        if (person == null)
        {
            throw new UserFailureException("Unknown user: " + userId);
        }

        final ISampleLister sampleLister =
                businessObjectFactory.createSampleLister(session, person.getId());
        List<Sample> samples = sampleLister.list(new ListOrSearchSampleCriteria(criteria));
        List<Sample> validSamples = new ArrayList<Sample>();

        SampleValidator validator = new SampleValidator();
        validator.init(new AuthorizationDataProvider(getDAOFactory()));

        for (Sample sample : samples)
        {
            if (validator.isValid(person, sample))
            {
                validSamples.add(sample);
            }
        }

        return validSamples;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> listSamplesByMaterialProperties(String sessionToken, Collection<TechId> materialIds)
    {
        final Session session = getSession(sessionToken);

        ISampleLister lister = businessObjectFactory.createSampleLister(session);
        Collection<TechId> ids = lister.listSamplesByMaterialProperties(materialIds);
        return lister.list(new ListOrSearchSampleCriteria(TechId.asLongs(ids)));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SampleValidator.class)
    public List<Sample> searchForSamples(String sessionToken, DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_SAMPLES,
                new ConcurrentOperation<List<Sample>>()
                {
                    @Override
                    public List<Sample> execute()
                    {
                        SearchHelper searchHelper =
                                new SearchHelper(session, businessObjectFactory, getDAOFactory());
                        return searchHelper.searchForSamples(session.getUserName(), criteria);
                    }
                });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<AbstractExternalData> listSampleExternalData(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<AbstractExternalData> datasets =
                datasetLister.listBySampleTechId(sampleId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<AbstractExternalData> listExperimentExternalData(final String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) final TechId experimentId, boolean showOnlyDirectlyConnected)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        final List<AbstractExternalData> datasets =
                datasetLister.listByExperimentTechId(experimentId, showOnlyDirectlyConnected);
        Collections.sort(datasets);
        return datasets;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<AbstractExternalData> listMetaprojectExternalData(final String sessionToken,
            final IMetaprojectId metaprojectId)
    {
        final Session session = getSession(sessionToken);
        final AuthorizationServiceUtils authorization = getAuthorizationService(session);
        final Metaproject metaproject = getMetaproject(sessionToken, metaprojectId);
        final IDatasetLister lister = createDatasetLister(session);

        final List<AbstractExternalData> datasets = lister.listByMetaprojectId(metaproject.getId());
        Collections.sort(datasets);
        List<AbstractExternalData> translatedDatasets = new ArrayList<AbstractExternalData>();

        for (AbstractExternalData dataset : datasets)
        {
            if (authorization.canAccessDataSet(dataset))
            {
                translatedDatasets.add(dataset);
            } else
            {
                translatedDatasets.add(DataSetTranslator.translateWithoutRevealingData(dataset));
            }
        }

        return translatedDatasets;
    }

    // 'fast' implementation
    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<AbstractExternalData> listDataSetRelationships(final String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) final TechId datasetId, final DataSetRelationshipRole role)
    {
        final Session session = getSession(sessionToken);
        final IDatasetLister datasetLister = createDatasetLister(session);
        List<AbstractExternalData> datasets = null;
        switch (role)
        {
            case CONTAINER:
                datasets = datasetLister.listByContainerTechId(datasetId);
                Collections.sort(datasets, new AbstractExternalData.DataSetComponentsComparator(datasetId));
                break;
            case COMPONENT:
                datasets = datasetLister.listByComponentTechId(datasetId);
                Collections.sort(datasets);
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<PropertyType> listPropertyTypes(final String sessionToken, boolean withRelations)
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
        return PropertyTypeTranslator.translate(propertyTypes, new HashMap<MaterialTypePE, MaterialType>(),
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<EntityTypePropertyType<?>> listEntityTypePropertyTypes(final String sessionToken)
    {
        List<PropertyType> propertyTypes = listPropertyTypes(sessionToken, true);
        return extractAssignments(null, propertyTypes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<EntityTypePropertyType<?>> listEntityTypePropertyTypes(final String sessionToken,
            final EntityType entityType)
    {
        List<PropertyType> propertyTypes = listPropertyTypes(sessionToken, true);
        return extractAssignments(entityType, propertyTypes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = EntityHistoryValidator.class)
    public List<EntityHistory> listEntityHistory(String sessionToken, EntityKind entityKind,
            TechId entityID)
    {
        Session session = getSession(sessionToken);

        AuthorizationServiceUtils authorizationUtils =
                new AuthorizationServiceUtils(getDAOFactory(), session.tryGetPerson().getUserId());
        authorizationUtils.checkAccessEntity(entityKind, entityID);

        IEntityHistoryDAO entityPropertyHistoryDAO = getDAOFactory().getEntityPropertyHistoryDAO();
        List<AbstractEntityPropertyHistoryPE> result =
                entityPropertyHistoryDAO.getPropertyHistory(
                        DtoConverters.convertEntityKind(entityKind), entityID);
        return EntityHistoryTranslator.translate(result, session.getBaseIndexURL(),
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()), new RelatedEntityFinder(getDAOFactory()));
    }

    private static List<EntityTypePropertyType<?>> extractAssignments(EntityType entityTypeOrNull,
            List<PropertyType> listPropertyTypes)
    {
        List<EntityTypePropertyType<?>> result = new ArrayList<EntityTypePropertyType<?>>();
        for (PropertyType propertyType : listPropertyTypes)
        {
            extractAssignments(result, entityTypeOrNull, propertyType);
        }
        result.sort(null);
        return result;
    }

    private static void extractAssignments(List<EntityTypePropertyType<?>> result,
            EntityType entityTypeOrNull, final PropertyType propertyType)
    {
        List<EntityTypePropertyType<?>> allTypes = new ArrayList<EntityTypePropertyType<?>>();

        for (ExperimentTypePropertyType etpt : propertyType.getExperimentTypePropertyTypes())
        {
            allTypes.add(etpt);
        }
        for (SampleTypePropertyType etpt : propertyType.getSampleTypePropertyTypes())
        {
            allTypes.add(etpt);
        }
        for (MaterialTypePropertyType etpt : propertyType.getMaterialTypePropertyTypes())
        {
            allTypes.add(etpt);
        }
        for (DataSetTypePropertyType etpt : propertyType.getDataSetTypePropertyTypes())
        {
            allTypes.add(etpt);
        }

        if (entityTypeOrNull == null)
        {
            result.addAll(allTypes);
        } else
        {
            for (EntityTypePropertyType<?> type : allTypes)
            {
                if (entityTypeOrNull.isEntityKind(type.getEntityKind())
                        && entityTypeOrNull.equals(type.getEntityType()))
                {
                    result.add(type);
                }
            }
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = MatchingEntityValidator.class)
    public List<MatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText,
            final boolean useWildcardSearchMode, int maxSize)
    {
        Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_GLOBALLY,
                new ConcurrentOperation<List<MatchingEntity>>()
                {
                    @Override
                    public List<MatchingEntity> execute()
                    {
                        final List<MatchingEntity> list = new ArrayList<MatchingEntity>();
                        for (final SearchableEntity searchableEntity : searchableEntities)
                        {
                            HibernateSearchDataProvider dataProvider =
                                    new HibernateSearchDataProvider(getDAOFactory());
                            List<MatchingEntity> entities =
                                    getDAOFactory().getHibernateSearchDAO().searchEntitiesByTerm(
                                            session.getUserName(), searchableEntity, queryText, dataProvider,
                                            useWildcardSearchMode, list.size(), maxSize);
                            list.addAll(entities);
                        }
                        Collections.sort(list, new Comparator<MatchingEntity>()
                        {
                            @Override
                            public int compare(MatchingEntity o1, MatchingEntity o2)
                            {
                                return new Double(o2.getScore()).compareTo(o1.getScore());
                            }
                        });
                        return list;
                    }
                });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Experiment> listMetaprojectExperiments(final String sessionToken,
            IMetaprojectId metaprojectId)
    {
        final Session session = getSession(sessionToken);
        final AuthorizationServiceUtils authorization = getAuthorizationService(session);

        Metaproject metaproject = getMetaproject(sessionToken, metaprojectId);
        Collection<MetaprojectAssignmentPE> assignments =
                new MetaprojectAssignmentsHelper(getDAOFactory(), managedPropertyEvaluatorFactory)
                        .getMetaprojectAssignments(metaproject.getId(), EntityKind.EXPERIMENT);
        List<ExperimentPE> experimentsPE = new ArrayList<ExperimentPE>();

        for (MetaprojectAssignmentPE assignment : assignments)
        {
            experimentsPE.add(assignment.getExperiment());
        }

        List<Experiment> experiments = translateExperiments(sessionToken, experimentsPE);
        List<Experiment> translatedExperiments = new ArrayList<Experiment>(experiments.size());

        for (Experiment experiment : experiments)
        {
            if (authorization.canAccessExperiment(experiment))
            {
                translatedExperiments.add(experiment);
            } else
            {
                translatedExperiments.add(ExperimentTranslator
                        .translateWithoutRevealingData(experiment));
            }
        }

        return translatedExperiments;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Experiment> listExperiments(final String sessionToken,
            ExperimentType experimentType,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) ProjectIdentifier projectIdentifier)
    {
        List<ProjectIdentifier> projectIdentifiers = projectIdentifier != null ? Collections.singletonList(projectIdentifier) : null;
        return listExperiments(sessionToken, experimentType, null, projectIdentifiers, false, false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Experiment> listExperiments(final String sessionToken,
            ExperimentType experimentType,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) List<ProjectIdentifier> projectIdentifiers)
    {
        return listExperiments(sessionToken, experimentType, null, projectIdentifiers, false, false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Experiment> listExperimentsHavingSamples(final String sessionToken,
            ExperimentType experimentType,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) List<ProjectIdentifier> projectIdentifiers)
    {
        return listExperiments(sessionToken, experimentType, null, projectIdentifiers, true, false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Experiment> listExperimentsHavingDataSets(final String sessionToken,
            ExperimentType experimentType,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) List<ProjectIdentifier> projectIdentifiers)
    {
        return listExperiments(sessionToken, experimentType, null, projectIdentifiers, false, true);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> listExperiments(final String sessionToken, ExperimentType experimentType, SpaceIdentifier spaceIdentifier)
    {
        return listExperiments(sessionToken, experimentType, spaceIdentifier, null, false, false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Experiment> listExperiments(final String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) List<ExperimentIdentifier> experimentIdentifiers)
    {
        Session session = getSession(sessionToken);
        IExperimentTable experimentTable = businessObjectFactory.createExperimentTable(session);

        experimentTable.load(experimentIdentifiers);

        List<ExperimentPE> experiments = experimentTable.getExperiments();
        final Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory()
                        .getMetaprojectDAO()
                        .listMetaprojectAssignmentsForEntities(
                                session.tryGetPerson(),
                                experiments,
                                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT);
        Map<Long, Set<Metaproject>> assignments =
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs);
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL(), assignments,
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    private final List<Experiment> listExperiments(final String sessionToken,
            final ExperimentType experimentType, final SpaceIdentifier spaceIdentifierOrNull,
            final List<ProjectIdentifier> projectIdentifiersOrNull, boolean onlyHavingSamples,
            boolean onlyHavingDataSets)
    {
        final Session session = getSession(sessionToken);
        final IExperimentTable experimentTable =
                businessObjectFactory.createExperimentTable(session);
        if (projectIdentifiersOrNull != null)
        {
            experimentTable.load(experimentType.getCode(), projectIdentifiersOrNull,
                    onlyHavingSamples, onlyHavingDataSets);
        } else if (spaceIdentifierOrNull != null)
        {
            experimentTable.load(experimentType.getCode(), spaceIdentifierOrNull);
        }
        return translateExperiments(sessionToken, experimentTable.getExperiments());
    }

    private List<Experiment> translateExperiments(String sessionToken,
            List<ExperimentPE> experiments)
    {
        final Session session = getSession(sessionToken);
        final Collection<MetaprojectAssignmentPE> assignmentPEs =
                getDAOFactory()
                        .getMetaprojectDAO()
                        .listMetaprojectAssignmentsForEntities(
                                session.tryGetPerson(),
                                experiments,
                                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT);
        Map<Long, Set<Metaproject>> assignments =
                MetaprojectTranslator.translateMetaprojectAssignments(assignmentPEs);
        Collections.sort(experiments);
        return ExperimentTranslator.translate(experiments, session.getBaseIndexURL(), assignments,
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<ExperimentType> listExperimentTypes(String sessionToken)
    {
        final List<ExperimentTypePE> experimentTypes =
                listEntityTypes(sessionToken, EntityKind.EXPERIMENT);
        return ExperimentTranslator.translate(experimentTypes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<MaterialType> listMaterialTypes(String sessionToken)
    {
        final List<MaterialTypePE> materialTypes =
                listEntityTypes(sessionToken, EntityKind.MATERIAL);
        return MaterialTypeTranslator.translate(materialTypes, new HashMap<MaterialTypePE, MaterialType>(),
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<DataType> listDataTypes(final String sessionToken)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<Vocabulary> listVocabularies(final String sessionToken, final boolean withTerms,
            boolean excludeInternal)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public String registerEntitytypeAndAssignPropertyTypes(final String sessionToken, NewETNewPTAssigments newETNewPTAssigments)
    {
        newETNewPTAssigments.updateOrdinalToDBOrder();

        List<String> results = new ArrayList<String>();
        // Entity Type Registration
        switch (newETNewPTAssigments.getEntity().getEntityKind())
        {
            case SAMPLE:
                registerSampleType(sessionToken, (SampleType) newETNewPTAssigments.getEntity());
                break;
            case DATA_SET:
                registerDataSetType(sessionToken, (DataSetType) newETNewPTAssigments.getEntity());
                break;
            case EXPERIMENT:
                registerExperimentType(sessionToken, (ExperimentType) newETNewPTAssigments.getEntity());
                break;
            case MATERIAL:
                registerMaterialType(sessionToken, (MaterialType) newETNewPTAssigments.getEntity());
                break;
        }
        // Property Types Registration/Assigments
        for (NewPTNewAssigment assigment : newETNewPTAssigments.getAssigments())
        {
            if (false == assigment.isExistingPropertyType())
            {
                registerPropertyType(sessionToken, assigment.getPropertyType());
            }
            String result = assignPropertyType(sessionToken, assigment.getAssignment());
            results.add(result);
        }
        return results.toString();
    }

    public static PropertyType returnIfContained(PropertyType propertyType, List<EntityTypePropertyType<?>> inList)
    {
        for (@SuppressWarnings("rawtypes")
                EntityTypePropertyType etpt : inList)
        {
            if (etpt.getPropertyType().getCode().equals(propertyType.getCode()))
            {
                return etpt.getPropertyType();
            }
        }
        return null;
    }

    public void runIntegrityTest(List<EntityTypePropertyType<?>> ini, List<EntityTypePropertyType<?>> fin)
    {
        for (@SuppressWarnings("rawtypes")
                EntityTypePropertyType etpt : fin)
        {
            PropertyType contained = returnIfContained(etpt.getPropertyType(), ini);
            if (contained != null &&
                    false == contained.getModificationDate().equals(etpt.getPropertyType().getModificationDate()))
            {
                throw new UserFailureException("Unfortunately " + contained.getCode()
                        + " has been modified in the meantime.\n\n"
                        + "Please, refresh the data and try it again.");
            }
        }
    }

    @SuppressWarnings("cast")
    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public String updateEntitytypeAndPropertyTypes(final String sessionToken, NewETNewPTAssigments newETNewPTAssigments)
    {
        newETNewPTAssigments.updateOrdinalToDBOrder();

        // Entity Type Update
        switch (newETNewPTAssigments.getEntity().getEntityKind())
        {
            case SAMPLE:
                updateSampleType(sessionToken, (SampleType) newETNewPTAssigments.getEntity());
                break;
            case DATA_SET:
                updateDataSetType(sessionToken, (DataSetType) newETNewPTAssigments.getEntity());
                break;
            case EXPERIMENT:
                updateExperimentType(sessionToken, (ExperimentType) newETNewPTAssigments.getEntity());
                break;
            case MATERIAL:
                updateMaterialType(sessionToken, (MaterialType) newETNewPTAssigments.getEntity());
                break;
        }

        List<String> results = new ArrayList<String>();

        // Update Algorithm for property types
        // It calculates the changes necessary without destroying assignments when possible to prevent data loss
        List<EntityTypePropertyType<?>> ini = listEntityTypePropertyTypes(sessionToken, newETNewPTAssigments.getEntity());
        @SuppressWarnings("unchecked")
        List<EntityTypePropertyType<?>> fin = (List<EntityTypePropertyType<?>>) newETNewPTAssigments.getEntity().getAssignedPropertyTypes();

        runIntegrityTest(ini, fin); // Since some changes trigger other changes this ones are validated only once at the beginning.

        for (int i = 0; i < fin.size(); i++)
        {
            if (i < ini.size()) // Is Check Possible
            {
                newETNewPTAssigments.getAssigments().get(i).getAssignment().setModificationDate(null); // Avoid further validation

                if (ini.get(i).getPropertyType().getCode().equals(fin.get(i).getPropertyType().getCode())) // Do nothing.
                {
                    // Positions are equal but maybe something have changed.
                    newETNewPTAssigments.getAssigments().get(i).getAssignment().setModificationDate(null);
                    updatePropertyTypeAssignment(sessionToken, newETNewPTAssigments.getAssigments().get(i).getAssignment());
                } else
                // Something needs to be done.
                {
                    if (false == fin.contains(ini.get(i))) // Delete
                    {
                        unassignPropertyType(sessionToken,
                                newETNewPTAssigments.getEntity().getEntityKind(),
                                ini.get(i).getPropertyType().getCode(),
                                newETNewPTAssigments.getEntity().getCode());
                        ini.remove(i);
                        i--;
                    } else
                    {
                        if (ini.contains(fin.get(i))) // Is present into another position, but is not this one, move it.
                        { // Edit
                            newETNewPTAssigments.getAssigments().get(i).getAssignment().setModificationDate(null);
                            updatePropertyTypeAssignment(sessionToken, newETNewPTAssigments.getAssigments().get(i).getAssignment());
                            ini.remove(fin.get(i));
                            ini.add(i, fin.get(i));
                        } else
                        // Not present, insert
                        { // Insert
                            if (false == newETNewPTAssigments.getAssigments().get(i).isExistingPropertyType())
                            {
                                registerPropertyType(sessionToken, newETNewPTAssigments.getAssigments().get(i).getPropertyType());
                            }
                            String result = assignPropertyType(sessionToken, newETNewPTAssigments.getAssigments().get(i).getAssignment());
                            results.add(result);
                            ini.add(i, fin.get(i));
                        }
                    }

                }
            } else
            {
                if (false == newETNewPTAssigments.getAssigments().get(i).isExistingPropertyType())
                {
                    registerPropertyType(sessionToken, newETNewPTAssigments.getAssigments().get(i).getPropertyType());
                }
                String result = assignPropertyType(sessionToken, newETNewPTAssigments.getAssigments().get(i).getAssignment());
                results.add(result);
                ini.add(i, fin.get(i));
            }

        }

        for (int i = 0; i < ini.size(); i++)
        {
            if (i < fin.size() && ini.get(i).getEntityType().getCode().equals(fin.get(i).getEntityType().getCode())) // Do nothing.
            {
                // Positions are equal
            } else
            {
                unassignPropertyType(sessionToken,
                        newETNewPTAssigments.getEntity().getEntityKind(),
                        ini.get(i).getPropertyType().getCode(),
                        newETNewPTAssigments.getEntity().getCode());
                // This is a hack, for some reason on the tests the changes don't go in until another change is done.
                unassignPropertyType(sessionToken,
                        newETNewPTAssigments.getEntity().getEntityKind(),
                        ini.get(i).getPropertyType().getCode(),
                        newETNewPTAssigments.getEntity().getCode());
                ini.remove(i);
                i--;
            }
        }

        return results.toString();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public String registerAndAssignPropertyType(final String sessionToken, final PropertyType propertyType, NewETPTAssignment assignment)
    {
        registerPropertyType(sessionToken, propertyType);
        return assignPropertyType(sessionToken, assignment);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerPropertyType(final String sessionToken, final PropertyType propertyType)
    {
        assert sessionToken != null : "Unspecified session token";
        assert propertyType != null : "Unspecified property type";

        final Session session = getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.define(propertyType);
        propertyTypeBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void updatePropertyType(final String sessionToken, final IPropertyTypeUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IPropertyTypeBO propertyTypeBO = businessObjectFactory.createPropertyTypeBO(session);
        propertyTypeBO.update(updates);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("REGISTER_VOCABULARY")
    public void registerVocabulary(final String sessionToken, final NewVocabulary vocabulary)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabulary != null : "Unspecified vocabulary";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.define(vocabulary);
        vocabularyBO.save();
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("WRITE_VOCABULARY")
    public void updateVocabulary(String sessionToken, IVocabularyUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.update(updates);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("WRITE_VOCABULARY_TERM")
    public void addVocabularyTerms(String sessionToken, TechId vocabularyId,
            List<VocabularyTerm> vocabularyTerms, Long previousTermOrdinal)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.addNewTerms(vocabularyTerms, previousTermOrdinal);
        vocabularyBO.save();
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("WRITE_UNOFFICIAL_VOCABULARY_TERM")
    public void addUnofficialVocabularyTerm(String sessionToken, TechId vocabularyId, String code,
            String label, String description, Long previousTermOrdinal)
    {
        assert sessionToken != null : "Unspecified session token";
        assert vocabularyId != null : "Unspecified vocabulary id";
        assert code != null : "Unspecified code";

        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(vocabularyId);
        vocabularyBO.addNewUnofficialTerm(code, label, description, previousTermOrdinal);
        vocabularyBO.save();
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("WRITE_VOCABULARY_TERM")
    public void updateVocabularyTerm(final String sessionToken, final IVocabularyTermUpdates updates)
    {
        assert sessionToken != null : "Unspecified session token";
        assert updates != null : "Unspecified updates";

        final Session session = getSession(sessionToken);
        final IVocabularyTermBO vocabularyTermBO =
                businessObjectFactory.createVocabularyTermBO(session);
        vocabularyTermBO.update(updates);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("WRITE_VOCABULARY_TERM")
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
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("WRITE_VOCABULARY")
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
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @Capability("REGISTER_PROJECT")
    public void registerProject(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) ProjectIdentifier projectIdentifier, String description,
            String leaderId,
            Collection<NewAttachment> attachments)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO projectBO = businessObjectFactory.createProjectBO(session);

        NewProject project = new NewProject(projectIdentifier.toString(), description);
        projectBO.define(project, leaderId);
        projectBO.save();
        for (NewAttachment attachment : attachments)
        {
            final AttachmentPE attachmentPE = AttachmentTranslator.translate(attachment);
            projectBO.addAttachment(attachmentPE);
        }
        projectBO.save();

    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public List<Experiment> searchForExperiments(String sessionToken,
            DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_EXPERIMENTS, new ConcurrentOperation<List<Experiment>>()
        {
            @Override
            public List<Experiment> execute()
            {
                SearchHelper searchHelper =
                        new SearchHelper(session, businessObjectFactory, getDAOFactory());
                String userId = session.getUserName();
                List<ExperimentPE> experiments = searchHelper.searchForExperiments(userId, criteria);
                return translateExperiments(sessionToken, experiments);
            }
        });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = SearchDomainSearchResultValidator.class)
    public List<SearchDomainSearchResultWithFullEntity> searchOnSearchDomain(String sessionToken,
            String preferredSearchDomainOrNull, String searchString, Map<String, String> optionalParametersOrNull)
    {
        Session session = getSession(sessionToken);

        ISearchDomainSearcher searcher = businessObjectFactory.createSearchDomainSearcher(session);
        return searcher.searchForEntitiesWithSequences(preferredSearchDomainOrNull, searchString, optionalParametersOrNull);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<SearchDomain> listAvailableSearchDomains(String sessionToken)
    {
        Session session = getSession(sessionToken);

        ISearchDomainSearcher searcher = businessObjectFactory.createSearchDomainSearcher(session);
        return searcher.listAvailableSearchDomains();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<AbstractExternalData> searchForDataSets(String sessionToken,
            DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_DATA_SETS, new ConcurrentOperation<List<AbstractExternalData>>()
        {
            @Override
            public List<AbstractExternalData> execute()
            {
                SearchHelper searchHelper =
                        new SearchHelper(session, businessObjectFactory, getDAOFactory());
                return searchHelper.searchForDataSets(session.getUserName(), criteria);
            }
        });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<AbstractExternalData> searchForDataSetsOnBehalfOfUser(String sessionToken,
            DetailedSearchCriteria criteria, String userId)
    {
        final Session session = getSession(sessionToken);
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        if (person == null)
        {
            throw new UserFailureException("Unknown user: " + userId);
        }

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_DATA_SETS, new ConcurrentOperation<List<AbstractExternalData>>()
        {
            @Override
            public List<AbstractExternalData> execute()
            {
                SearchHelper searchHelper =
                        new SearchHelper(session, businessObjectFactory, getDAOFactory());
                List<AbstractExternalData> unfilteredDatasets =
                        searchHelper.searchForDataSets(userId, person.getId(), criteria);

                final ExternalDataValidator validator = new ExternalDataValidator();
                validator.init(new AuthorizationDataProvider(getDAOFactory()));

                final ArrayList<AbstractExternalData> datasets =
                        new ArrayList<AbstractExternalData>(unfilteredDatasets.size());

                for (AbstractExternalData dataset : unfilteredDatasets)
                {
                    if (validator.doValidation(person, dataset))
                    {
                        datasets.add(dataset);
                    }
                }
                return datasets;
            }
        });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public AbstractExternalData getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId)
    {
        final Session session = getSession(sessionToken);
        final IDataBO datasetBO = businessObjectFactory.createDataBO(session);
        datasetBO.loadDataByTechId(datasetId);
        datasetBO.enrichWithParentsAndExperiment();
        // not fetching children, as no caller is using them and it's causing potential performance problem with many children
        // datasetBO.enrichWithChildren();
        datasetBO.enrichWithContainedDataSets();
        datasetBO.enrichWithProperties();
        final DataPE dataset = datasetBO.getData();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), dataset);
        return DataSetTranslator.translate(dataset, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojectPEs), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("WRITE_DATASET")
    public DataSetUpdateResult updateDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class) DataSetUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IDataBO dataSetBO = businessObjectFactory.createDataBO(session);
        dataSetBO.update(updates);
        DataSetUpdateResult result = new DataSetUpdateResult();
        DataPE data = dataSetBO.getData();
        result.setVersion(data.getVersion());
        List<String> parents = IdentifierExtractor.extract(data.getParents());
        Collections.sort(parents);
        result.setParentCodes(parents);
        result.setContainedDataSetCodes(Code.extractCodes(data.getContainedDataSets()));
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public List<AbstractExternalData> listRelatedDataSets(String sessionToken,
            DataSetRelatedEntities relatedEntities, boolean withDetails)
    {
        final Session session = getSession(sessionToken);
        final Set<DataPE> resultSet = new LinkedHashSet<DataPE>();
        // TODO 2009-08-17, Piotr Buczek: [LMS-1149] optimize performance
        addRelatedDataSets(resultSet, relatedEntities.getEntities());

        IMetaprojectDAO mpd = this.getDAOFactory().getMetaprojectDAO();

        Collection<MetaprojectAssignmentPE> assignments =
                mpd.listMetaprojectAssignmentsForEntities(session.tryGetPerson(), resultSet,
                        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.DATA_SET);

        Map<Long, Set<Metaproject>> translation =
                MetaprojectTranslator.translateMetaprojectAssignments(assignments);

        final List<AbstractExternalData> list =
                new ArrayList<AbstractExternalData>(resultSet.size());
        for (final DataPE hit : resultSet)
        {
            HibernateUtils.initialize(hit.getChildRelationships());
            list.add(DataSetTranslator.translate(hit, session.getBaseIndexURL(), withDetails,
                    translation.get(hit.getId()), managedPropertyEvaluatorFactory,
                    new SamplePropertyAccessValidator(session, getDAOFactory())));
        }
        return list;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    @Capability("SEARCH_ON_BEHALF_OF_USER")
    public List<AbstractExternalData> listRelatedDataSetsOnBehalfOfUser(String sessionToken,
            DataSetRelatedEntities relatedEntities, boolean withDetails, String userId)
    {
        final Session session = getSession(sessionToken);
        final Set<DataPE> resultSet = new LinkedHashSet<DataPE>();
        // TODO 2009-08-17, Piotr Buczek: [LMS-1149] optimize performance
        addRelatedDataSets(resultSet, relatedEntities.getEntities());

        IMetaprojectDAO mpd = this.getDAOFactory().getMetaprojectDAO();

        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        if (person == null)
        {
            throw new UserFailureException("Unknown user: " + userId);
        }

        Collection<MetaprojectAssignmentPE> assignments =
                mpd.listMetaprojectAssignmentsForEntities(person, resultSet,
                        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.DATA_SET);

        Map<Long, Set<Metaproject>> translation =
                MetaprojectTranslator.translateMetaprojectAssignments(assignments);

        ExternalDataValidator validator = new ExternalDataValidator();
        validator.init(new AuthorizationDataProvider(getDAOFactory()));

        final List<AbstractExternalData> list =
                new ArrayList<AbstractExternalData>(resultSet.size());
        for (final DataPE hit : resultSet)
        {
            HibernateUtils.initialize(hit.getChildRelationships());

            AbstractExternalData dataSet = DataSetTranslator.translate(hit, session.getBaseIndexURL(), withDetails,
                    translation.get(hit.getId()), managedPropertyEvaluatorFactory,
                    new SamplePropertyAccessValidator(session, getDAOFactory()));

            if (validator.isValid(person, dataSet))
            {
                list.add(dataSet);
            }
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Material> listMaterials(String sessionToken, ListMaterialCriteria criteria,
            boolean withProperties)
    {
        final Session session = getSession(sessionToken);
        final IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        return materialLister.list(criteria, withProperties);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Collection<TechId> listMaterialIdsByMaterialProperties(String sessionToken, Collection<TechId> materialIds)
    {
        final Session session = getSession(sessionToken);
        final IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        return materialLister.listMaterialsByMaterialProperties(materialIds);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Material> listMetaprojectMaterials(String sessionToken, IMetaprojectId metaprojectId)
    {
        final Session session = getSession(sessionToken);
        final Metaproject metaproject = getMetaproject(sessionToken, metaprojectId);
        final IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        return materialLister.list(new MetaprojectCriteria(metaproject.getId()), true);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerSampleType(String sessionToken, SampleType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void updateSampleType(String sessionToken, EntityType entityType)
    {
        try
        {
            updateEntityType(sessionToken, EntityKind.SAMPLE, entityType);
        } catch (DataAccessException e)
        {
            if (SampleDataAccessExceptionTranslator.isUniqueSubcodeViolationException(e))
            {
                throw new UserFailureException(
                        "Cannot enable 'Unique Subcodes' option as some of the samples of this type already have duplicated subcodes.");
            }
            throw e;
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerMaterialType(String sessionToken, MaterialType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void updateMaterialType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.MATERIAL, entityType);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerExperimentType(String sessionToken, ExperimentType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void updateExperimentType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.EXPERIMENT, entityType);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerDataSetType(String sessionToken, DataSetType entityType)
    {
        final Session session = getSession(sessionToken);
        IEntityTypeBO entityTypeBO = businessObjectFactory.createEntityTypeBO(session);
        entityTypeBO.define(entityType);
        entityTypeBO.save();
        dataStoreServiceRegistrator.register(entityType);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void updateDataSetType(String sessionToken, EntityType entityType)
    {
        updateEntityType(sessionToken, EntityKind.DATA_SET, entityType);
    }

    private void setValidationScript(EntityTypePE entityTypePE, EntityType entityType)
    {
        if (entityType.getValidationScript() == null
                || entityType.getValidationScript().getName() == null
                || entityType.getValidationScript().getName().equals(""))
        {
            entityTypePE.setValidationScript(null);
        } else
        {
            ScriptPE script =
                    getDAOFactory().getScriptDAO().tryFindByName(
                            entityType.getValidationScript().getName());

            if (script != null && entityType.isEntityKind(script.getEntityKind()))
            {
                entityTypePE.setValidationScript(script);
            } else
            {
                entityTypePE.setValidationScript(null);
            }
        }
    }

    private void updateEntityType(String sessionToken, EntityKind entityKind, EntityType entityType)
    {
        checkSession(sessionToken);
        IEntityTypeDAO entityTypeDAO =
                getDAOFactory().getEntityTypeDAO(DtoConverters.convertEntityKind(entityKind));
        EntityTypePE entityTypePE = entityTypeDAO.tryToFindEntityTypeByCode(entityType.getCode());
        if (entityType.getModificationDate() != null && // Avoid validation, needed to make multiple modifications with one call
                entityTypePE.getModificationDate().equals(entityType.getModificationDate()) == false)
        {
            throw new UserFailureException("Unfortunately " + entityType.getCode()
                    + " has been modified in the meantime.\n\n"
                    + "Please, refresh the data and try it again.");
        }

        entityTypePE.setDescription(entityType.getDescription());

        setValidationScript(entityTypePE, entityType);

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

            setValidationScript(entityTypePE, entityType);

        } else if (entityKind == EntityKind.DATA_SET)
        {
            DataSetTypePE dataSetTypePE = (DataSetTypePE) entityTypePE;
            DataSetType dataSetType = (DataSetType) entityType;
            dataSetTypePE.setDeletionDisallow(dataSetType.isDeletionDisallow());
            dataSetTypePE.setMainDataSetPath(dataSetType.getMainDataSetPath());
            String mainDataSetPattern = dataSetType.getMainDataSetPattern();
            EntityTypeBO.assertValidDataSetTypeMainPattern(mainDataSetPattern);
            dataSetTypePE.setMainDataSetPattern(mainDataSetPattern);

            setValidationScript(entityTypePE, entityType);

        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("DELETE_DATASET")
    public void deleteDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes, String reason, DeletionType type,
            boolean isTrashEnabled)
    {
        deleteDataSetsCommon(sessionToken, dataSetCodes, reason, type, false, isTrashEnabled);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_DISABLED)
    @Capability("FORCE_DELETE_DATASET")
    public void deleteDataSetsForced(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes, String reason, DeletionType type,
            boolean isTrashEnabled)
    {
        deleteDataSetsCommon(sessionToken, dataSetCodes, reason, type, true, isTrashEnabled);
    }

    @SuppressWarnings("deprecation")
    private void deleteDataSetsCommon(String sessionToken, List<String> dataSetCodes,
            String reason, DeletionType type, boolean forceDisallowedTypes, boolean isTrashEnabled)
    {
        // TODO 2011-08-09, Piotr Buczek: simplify it when we remove the switch turning off trash
        // provide data set ids directly (no need to use codes)
        Session session = getSession(sessionToken);
        // NOTE: logical deletion and new implementation of permanent deletion doesn't use
        // IDataSetTypeSlaveServerPlugin (we have just 1 implementation!)
        switch (type)
        {
            case PERMANENT:
                List<DataPE> dataSets1 = getDAOFactory().getDataDAO().listByCode(new HashSet<String>(dataSetCodes));
                Date timeStamp = getDAOFactory().getTransactionTimestamp();
                RelationshipUtils.updateModificationDateAndModifierOfRelatedEntitiesOfDataSets(dataSets1, session, timeStamp);
                if (isTrashEnabled)
                {
                    IDeletedDataSetTable deletedDataSetTable =
                            businessObjectFactory.createDeletedDataSetTable(session);
                    deletedDataSetTable.loadByDataSetCodes(dataSetCodes);
                    deletedDataSetTable.permanentlyDeleteLoadedDataSets(reason,
                            forceDisallowedTypes);
                } else
                {
                    final IDataSetTable dataSetTable =
                            businessObjectFactory.createDataSetTable(session);
                    permanentlyDeleteDataSets(session, dataSetTable, dataSetCodes, reason,
                            forceDisallowedTypes);
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("DELETE_SAMPLE")
    public void deleteSamples(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdCollectionReadWritePredicate.class) List<TechId> sampleIds, String reason,
            DeletionType deletionType)
    {
        Session session = getSession(sessionToken);
        switch (deletionType)
        {
            case PERMANENT:
                List<SamplePE> samples = getDAOFactory().getSampleDAO().listByIDs(TechId.asLongs(sampleIds));
                Date timeStamp = getDAOFactory().getTransactionTimestamp();
                RelationshipUtils.updateModificationDateAndModifierOfRelatedEntitiesOfSamples(samples, session, timeStamp);
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("DELETE_EXPERIMENT")
    public void deleteExperiments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdCollectionPredicate.class) List<TechId> experimentIds, String reason,
            DeletionType deletionType)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        switch (deletionType)
        {
            case PERMANENT:
                List<ExperimentPE> experiments = getDAOFactory().getExperimentDAO().listByIDs(TechId.asLongs(experimentIds));
                Date timeStamp = getDAOFactory().getTransactionTimestamp();
                RelationshipUtils.updateModificationDateAndModifierOfRelatedProjectsOfExperiments(experiments, session, timeStamp);
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
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("DELETE_VOCABULARY")
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN })
    @Capability("DELETE_PROJECT")
    public void deleteProjects(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdCollectionPredicate.class) List<TechId> projectIds, String reason)
    {
        Session session = getSession(sessionToken);
        IProjectBO projectBO = businessObjectFactory.createProjectBO(session);
        for (TechId id : projectIds)
        {
            projectBO.deleteByTechId(id, reason);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    @Capability("DELETE_SPACE")
    public void deleteSpaces(String sessionToken,
            @AuthorizationGuard(guardClass = SpaceTechIdCollectionPredicate.class) List<TechId> spaceIds, String reason)
    {
        Session session = getSession(sessionToken);
        ISpaceBO spaceBO = businessObjectFactory.createSpaceBO(session);
        for (TechId id : spaceIds)
        {
            spaceBO.deleteByTechId(id, reason);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteScripts(String sessionToken, List<TechId> scriptIds)
    {
        Session session = getSession(sessionToken);
        IScriptBO scriptBO = businessObjectFactory.createScriptBO(session);
        List<String> namesOfPredeployedPlugins = new ArrayList<String>();
        for (TechId id : scriptIds)
        {
            ScriptPE script = scriptBO.deleteByTechId(id);
            if (script.getPluginType() == PluginType.PREDEPLOYED)
            {
                namesOfPredeployedPlugins.add(script.getName());
            }
        }
        if (namesOfPredeployedPlugins.isEmpty())
        {
            return;
        }
        IHotDeploymentController hotDeploymentController =
                entityValidationFactory.getHotDeploymentController();
        if (hotDeploymentController == null)
        {
            operationLog
                    .warn("Can not disable hot-deployed plugins because of missing controller.");
            return;
        }
        for (String name : namesOfPredeployedPlugins)
        {
            hotDeploymentController.disablePlugin(name);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("DELETE_EXPERIMENT_ATTACHMENT")
    public void deleteExperimentAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId, List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        deleteHolderAttachments(session, experimentBO.getExperiment(), fileNames, reason);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_EXPERIMENT_ATTACHMENT")
    public void updateExperimentAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_EXPERIMENT_ATTACHMENT")
    public void addExperimentAttachment(String sessionToken, @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            NewAttachment attachment)
    {
        Session session = getSession(sessionToken);
        IExperimentBO bo = businessObjectFactory.createExperimentBO(session);
        bo.loadDataByTechId(experimentId);
        bo.addAttachment(AttachmentTranslator.translate(attachment));
        bo.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("DELETE_SAMPLE_ATTACHMENT")
    public void deleteSampleAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdReadWritePredicate.class) TechId sampleId, List<String> fileNames, String reason)
    {
        Session session = getSession(sessionToken);
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        deleteHolderAttachments(session, sampleBO.getSample(), fileNames, reason);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN })
    @Capability("DELETE_PROJECT_ATTACHMENT")
    public void deleteProjectAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId, List<String> fileNames, String reason)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Attachment> listExperimentAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        return AttachmentTranslator.translate(
                listHolderAttachments(session, experimentBO.getExperiment()),
                session.getBaseIndexURL());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Attachment> listSampleAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleId)
    {
        Session session = getSession(sessionToken);
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        return AttachmentTranslator.translate(listHolderAttachments(session, sampleBO.getSample()),
                session.getBaseIndexURL());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Attachment> listProjectAttachments(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public String uploadDataSets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes,
            DataSetUploadContext uploadContext)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(dataSetCodes, true, false);
        return dataSetTable.uploadLoadedDataSetsToCIFEX(uploadContext);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public List<VocabularyTermWithStats> listVocabularyTermsWithStatistics(String sessionToken,
            Vocabulary vocabulary)
    {
        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return vocabularyBO.countTermsUsageStatistics();
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.PROJECT_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public Set<VocabularyTerm> listVocabularyTerms(String sessionToken, Vocabulary vocabulary)
    {
        final Session session = getSession(sessionToken);
        final IVocabularyBO vocabularyBO = businessObjectFactory.createVocabularyBO(session);
        vocabularyBO.loadDataByTechId(TechId.create(vocabulary));
        return VocabularyTermTranslator.translateTerms(vocabularyBO.enrichWithTerms());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<DataSetType> listDataSetTypes(String sessionToken)
    {
        final List<DataSetTypePE> dataSetTypes = listEntityTypes(sessionToken, EntityKind.DATA_SET);
        return DataSetTypeTranslator.translate(dataSetTypes, new HashMap<MaterialTypePE, MaterialType>(),
                new HashMap<PropertyTypePE, PropertyType>());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public LastModificationState getLastModificationState(String sessionToken)
    {
        checkSession(sessionToken);
        return lastModificationState;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public SampleParentWithDerived getSampleInfo(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) final TechId sampleId) throws UserFailureException
    {
        assert sessionToken != null : "Unspecified session token.";
        assert sampleId != null : "Unspecified sample techId.";

        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        sampleBO.enrichWithAttachments();
        sampleBO.enrichWithPropertyTypes();
        final SamplePE sample = sampleBO.getSample();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), sample);
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                        .getSampleInfo(session, sample), session.getBaseIndexURL(),
                MetaprojectTranslator
                        .translate(metaprojectPEs),
                managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_SAMPLE")
    public SampleUpdateResult updateSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class) SampleUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.update(updates);
        sampleBO.save();
        SampleUpdateResult result = new SampleUpdateResult();
        SamplePE sample = sampleBO.getSample();
        result.setVersion(sample.getVersion());
        List<String> parents = IdentifierExtractor.extract(sample.getParents());
        Collections.sort(parents);
        result.setParents(parents);
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Experiment getExperimentInfo(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) ExperimentIdentifier identifier)
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

        Collection<MetaprojectPE> metaprojects =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), experiment);

        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojects), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Experiment getExperimentInfo(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);
        experimentBO.enrichWithProperties();
        experimentBO.enrichWithAttachments();
        final ExperimentPE experiment = experimentBO.getExperiment();

        Collection<MetaprojectPE> metaprojects =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), experiment);

        return ExperimentTranslator.translate(experiment, session.getBaseIndexURL(),
                MetaprojectTranslator.translate(metaprojects), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()),
                ExperimentTranslator.LoadableFields.PROPERTIES,
                ExperimentTranslator.LoadableFields.ATTACHMENTS);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_EXPERIMENT_SAMPLE")
    public ExperimentUpdateResult updateExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentUpdatesPredicate.class) ExperimentUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.update(updates);
        experimentBO.save();
        ExperimentUpdateResult result = new ExperimentUpdateResult();
        ExperimentPE experiment = experimentBO.getExperiment();
        result.setVersion(experiment.getVersion());
        result.setSamples(Code.extractCodes(experiment.getSamples()));
        return result;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Project getProjectInfo(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        bo.enrichWithAttachments();
        final ProjectPE project = bo.getProject();
        return ProjectTranslator.translate(project);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Project getProjectInfo(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectIdentifierPredicate.class) ProjectIdentifier projectIdentifier)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadByProjectIdentifier(projectIdentifier);
        final ProjectPE project = bo.getProject();
        return ProjectTranslator.translate(project);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public IIdHolder getProjectIdHolder(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectPermIdStringPredicate.class) String projectPermId)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadByPermId(projectPermId);
        final ProjectPE project = bo.getProject();
        return new TechId(project.getId());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Material getMaterialInfo(String sessionToken, MaterialIdentifier identifier)
    {
        Session session = getSession(sessionToken);
        IMaterialBO materialBO = getBusinessObjectFactory().createMaterialBO(session);
        materialBO.loadByMaterialIdentifier(identifier);
        materialBO.enrichWithProperties();
        MaterialPE materialPE = materialBO.getMaterial();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), materialPE);

        return MaterialTranslator.translate(materialPE,
                MetaprojectTranslator.translate(metaprojectPEs), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Material getMaterialInfo(String sessionToken, TechId materialId)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.loadDataByTechId(materialId);
        materialBO.enrichWithProperties();
        final MaterialPE material = materialBO.getMaterial();
        Collection<MetaprojectPE> metaprojectPEs =
                getDAOFactory().getMetaprojectDAO().listMetaprojectsForEntity(
                        session.tryGetPerson(), material);
        return MaterialTranslator.translate(material, true,
                MetaprojectTranslator.translate(metaprojectPEs), managedPropertyEvaluatorFactory,
                new SamplePropertyAccessValidator(session, getDAOFactory()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public IEntityInformationHolderWithPermId getMaterialInformationHolder(String sessionToken,
            MaterialIdentifier identifier)
    {
        return getMaterialInfo(sessionToken, identifier);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_MATERIAL")
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, String[] metaprojects, Date version)
    {
        final Session session = getSession(sessionToken);
        final IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        MaterialUpdateDTO updates = new MaterialUpdateDTO(materialId, properties, version);
        updates.setMetaprojectsOrNull(metaprojects);
        materialBO.update(updates);
        materialBO.save();
        return materialBO.getMaterial().getModificationDate();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            EntityKind entityKind, String permId)
    {
        Session session = getSession(sessionToken);

        AuthorizationServiceUtils authorizationUtils =
                new AuthorizationServiceUtils(getDAOFactory(), session.tryGetPerson().getUserId());
        authorizationUtils.checkAccessEntity(entityKind, permId);

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
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public String generateCode(String sessionToken, String prefix, EntityKind entityKind)
    {
        checkSession(sessionToken);
        return new EntityCodeGenerator(getDAOFactory()).generateCode(prefix, entityKind);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN })
    @Capability("WRITE_PROJECT")
    public int updateProject(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectUpdatesPredicate.class) ProjectUpdatesDTO updates)
    {
        final Session session = getSession(sessionToken);
        final IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.update(updates);
        bo.save();
        return bo.getProject().getVersion();
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteDataSetTypes(String sessionToken, List<String> entityTypesCodes)
    {
        deleteEntityTypes(sessionToken, EntityKind.DATA_SET, entityTypesCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteExperimentTypes(String sessionToken, List<String> entityTypesCodes)
    {
        deleteEntityTypes(sessionToken, EntityKind.EXPERIMENT, entityTypesCodes);

    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteMaterialTypes(String sessionToken, List<String> entityTypesCodes)
    {
        deleteEntityTypes(sessionToken, EntityKind.MATERIAL, entityTypesCodes);

    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteSampleTypes(String sessionToken, List<String> entityTypesCodes)
    {
        deleteEntityTypes(sessionToken, EntityKind.SAMPLE, entityTypesCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteFileFormatTypes(String sessionToken, List<String> codes)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public String getTemplateColumns(String sessionToken, EntityKind entityKind, String type,
            String format, boolean autoGenerate, boolean withExperiments, boolean withSpace,
            BatchOperationKind operationKind)
    {
        List<EntityTypePE> types = new ArrayList<EntityTypePE>();
        if ((entityKind.equals(EntityKind.SAMPLE) || entityKind.equals(EntityKind.DATA_SET) || entityKind
                .equals(EntityKind.MATERIAL)) && EntityType.isDefinedInFileEntityTypeCode(type))
        {
            types.addAll(getDAOFactory().getEntityTypeDAO(
                    DtoConverters.convertEntityKind(entityKind)).listEntityTypes());
        } else
        {
            types.add(findEntityType(entityKind, type));
        }

        if (format.equalsIgnoreCase("json"))
        {
            return getJsonTemplateColumns(entityKind, autoGenerate, withExperiments, withSpace, operationKind, types);
        } else
        {
            return getTsvTemplateColumns(entityKind, autoGenerate, withExperiments, withSpace, operationKind, types);
        }
    }

    private String getJsonTemplateColumns(final EntityKind entityKind, final boolean autoGenerate, final boolean withExperiments,
            final boolean withSpace, final BatchOperationKind operationKind, final List<EntityTypePE> types)
    {
        final StringBuilder sb = new StringBuilder();
        for (EntityTypePE entityType : types)
        {
            final List<String> columns = new ArrayList<>();
            final Set<? extends EntityTypePropertyTypePE> propertyTypes;
            switch (entityKind)
            {
                case SAMPLE:
                    propertyTypes = ((SampleTypePE) entityType).getSampleTypePropertyTypes();
                    break;
                case DATA_SET:
                    propertyTypes = ((DataSetTypePE) entityType).getDataSetTypePropertyTypes();
                    break;
                case MATERIAL:
                    propertyTypes = ((MaterialTypePE) entityType).getMaterialTypePropertyTypes();
                    break;
                case EXPERIMENT:
                    propertyTypes = ((ExperimentTypePE) entityType).getExperimentTypePropertyTypes();
                    break;
                default:
                    throw new UserFailureException("Unknown entity kind: " + entityKind.name());
            }

            addPropertiesToTemplateColumns(columns, propertyTypes);

            for (final String column : columns)
            {
                if (sb.length() > 0)
                {
                    sb.append(",");
                }
                sb.append("\n    \"").append(column).append("\" : \"\"");
            }
        }
        return String.format("{\n  \"properties\" : {%s\n  }\n}", sb);
    }

    private String getTsvTemplateColumns(final EntityKind entityKind, final boolean autoGenerate, final boolean withExperiments,
            final boolean withSpace,
            final BatchOperationKind operationKind, final List<EntityTypePE> types)
    {
        final StringBuilder sb = new StringBuilder();
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
                                        : "",
                                section);
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
                    } else if (entityKind.equals(EntityKind.EXPERIMENT))
                    {
                        sb.insert(0, NewBasicExperiment.EXPERIMENT_REGISTRATION_TEMPLATE_COMMENT);
                    }
                    break;
                case UPDATE:
                    if (entityKind.equals(EntityKind.SAMPLE))
                    {
                        sb.insert(0, UpdatedSample.SAMPLE_UPDATE_TEMPLATE_COMMENT);
                    } else if (entityKind.equals(EntityKind.DATA_SET))
                    {
                        sb.insert(0, UpdatedDataSet.DATASET_UPDATE_TEMPLATE_COMMENT);
                    } else if (entityKind.equals(EntityKind.EXPERIMENT))
                    {
                        sb.insert(0, UpdatedBasicExperiment.EXPERIMENT_UPDATE_TEMPLATE_COMMENT);
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
                    IManagedPropertyEvaluator evaluator =
                            managedPropertyEvaluatorFactory.createManagedPropertyEvaluator(etpt);
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void updateFileFormatType(String sessionToken, AbstractType type)
    {
        checkSession(sessionToken);
        IFileFormatTypeDAO dao = getDAOFactory().getFileFormatTypeDAO();
        FileFormatTypePE typePE = dao.tryToFindFileFormatTypeByCode(type.getCode());
        typePE.setDescription(type.getDescription());
        dao.createOrUpdate(typePE);

    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN })
    @Capability("WRITE_PROJECT_ATTACHMENT")
    public void updateProjectAttachments(String sessionToken, @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId,
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
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.PROJECT_ADMIN })
    @Capability("WRITE_PROJECT_ATTACHMENT")
    public void addProjectAttachments(String sessionToken, @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId,
            NewAttachment attachment)
    {
        Session session = getSession(sessionToken);
        IProjectBO bo = businessObjectFactory.createProjectBO(session);
        bo.loadDataByTechId(projectId);
        bo.addAttachment(AttachmentTranslator.translate(attachment));
        bo.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_SAMPLE_ATTACHMENT")
    public void updateSampleAttachments(String sessionToken, @AuthorizationGuard(guardClass = SampleTechIdReadWritePredicate.class) TechId sampleId,
            Attachment attachment)
    {
        Session session = getSession(sessionToken);
        ISampleBO bo = businessObjectFactory.createSampleBO(session);
        bo.loadDataByTechId(sampleId);
        IAttachmentBO attachmentBO = businessObjectFactory.createAttachmentBO(session);
        attachmentBO.updateAttachment(bo.getSample(), attachment);
        attachmentBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_SAMPLE_ATTACHMENT")
    public void addSampleAttachments(String sessionToken, @AuthorizationGuard(guardClass = SampleTechIdReadWritePredicate.class) TechId sampleId,
            NewAttachment attachment)
    {
        Session session = getSession(sessionToken);
        ISampleBO bo = businessObjectFactory.createSampleBO(session);
        bo.loadDataByTechId(sampleId);
        bo.addAttachment(AttachmentTranslator.translate(attachment));
        bo.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<DataStore> listDataStores(String sessionToken)
    {
        checkSession(sessionToken);
        return listDataStores();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public TableModel createReportFromDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        return dataSetTable.createReportFromDatasets(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), datasetCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public TableModel createReportFromDatasets(String sessionToken, String serviceKey,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        return dataSetTable.createReportFromDatasets(serviceKey, datasetCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public TableModel createReportFromAggregationService(String sessionToken,
            DatastoreServiceDescription serviceDescription, Map<String, Object> parameters)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        return dataSetTable.createReportFromAggregationService(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), parameters);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("PROCESS_DATASET")
    public void processDatasets(String sessionToken,
            DatastoreServiceDescription serviceDescription,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        Map<String, String> parameterBindings = new HashMap<String, String>();
        dataSetTable.processDatasets(serviceDescription.getKey(), datasetCodes, parameterBindings);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("ARCHIVE_DATASET")
    public int archiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes,
            boolean removeFromDataStore, Map<String, String> options)
    {
        return super.archiveDatasets(sessionToken, datasetCodes, removeFromDataStore, options);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("UNARCHIVE_DATASET")
    public int unarchiveDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        return super.unarchiveDatasets(sessionToken, datasetCodes);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerAuthorizationGroup(String sessionToken,
            NewAuthorizationGroup newAuthorizationGroup)
    {
        Session session = getSession(sessionToken);
        IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.define(newAuthorizationGroup);
        bo.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerScript(String sessionToken, Script script)
    {
        Session session = getSession(sessionToken);
        IScriptBO bo = businessObjectFactory.createScriptBO(session);
        bo.define(script);
        bo.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void registerOrUpdatePredeployedPlugin(String sessionToken, Script script)
    {
        Session session = getSession(sessionToken);
        try
        {
            IScriptBO bo = businessObjectFactory.createScriptBO(session);
            bo.tryDefineOrUpdateIfPossible(script);
        } catch (IllegalArgumentException e)
        {
            operationLog.warn(e.getMessage());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void invalidatePredeployedPlugin(String sessionToken, String name, ScriptType scriptType)
    {
        Session session = getSession(sessionToken);

        try
        {
            IScriptBO bo = businessObjectFactory.createScriptBO(session);
            bo.tryDeleteOrInvalidatePredeployedPlugin(name, scriptType);
        } catch (IllegalArgumentException e)
        {
            operationLog.warn(e.getMessage());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public void deleteAuthorizationGroups(String sessionToken, List<TechId> authGroupIds,
            String reason)
    {
        Session session = getSession(sessionToken);
        IAuthorizationGroupBO authGroupBO =
                businessObjectFactory.createAuthorizationGroupBO(session);
        for (TechId id : authGroupIds)
        {
            authGroupBO.deleteByTechId(id, reason);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    public List<AuthorizationGroup> listAuthorizationGroups(String sessionToken)
    {
        checkSession(sessionToken);
        final List<AuthorizationGroupPE> persons =
                getDAOFactory().getAuthorizationGroupDAO().list();
        Collections.sort(persons);
        return AuthorizationGroupTranslator.translate(persons);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Script> listScripts(String sessionToken, ScriptType scriptTypeOrNull,
            EntityKind entityKindOrNull)
    {
        checkSession(sessionToken);
        final List<ScriptPE> scripts =
                getDAOFactory().getScriptDAO().listEntities(scriptTypeOrNull, entityKindOrNull);
        Collections.sort(scripts);
        List<Script> result =
                ScriptTranslator.enhancePredeployedPlugins(ScriptTranslator.translate(scripts),
                        entityValidationFactory, dynamicPropertyCalculatorFactory,
                        managedPropertyEvaluatorFactory);
        if (entityKindOrNull != null)
        {
            for (Iterator<Script> iterator = result.iterator(); iterator.hasNext(); )
            {
                Script script = iterator.next();
                if (isApplicableScript(script, entityKindOrNull) == false)
                {
                    iterator.remove();
                }
            }
        }
        return result;
    }

    private boolean isApplicableScript(Script script, EntityKind entityKind)
    {
        EntityKind[] entityKinds = script.getEntityKind();
        if (entityKinds == null)
        {
            return true;
        }
        for (EntityKind ek : entityKinds)
        {
            if (ek == entityKind)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public Date updateAuthorizationGroup(String sessionToken, AuthorizationGroupUpdates updates)
    {
        final Session session = getSession(sessionToken);
        final IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.update(updates);
        bo.save();
        return bo.getAuthorizationGroup().getModificationDate();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    public List<Person> listPersonInAuthorizationGroup(String sessionToken,
            TechId authorizatonGroupId)
    {
        final Session session = getSession(sessionToken);
        IAuthorizationGroupBO bo = businessObjectFactory.createAuthorizationGroupBO(session);
        bo.loadByTechId(authorizatonGroupId);
        return PersonTranslator.translate(bo.getAuthorizationGroup().getPersons());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    @ReturnValueFilter(validatorClass = ExpressionValidator.class)
    public List<GridCustomFilter> listFilters(String sessionToken, String gridId)
    {
        checkSession(sessionToken);
        List<GridCustomFilterPE> filters =
                getDAOFactory().getGridCustomFilterDAO().listFilters(gridId);
        Collections.sort(filters);
        return GridCustomFilterTranslator.translate(filters);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("WRITE_FILTER")
    public void registerFilter(String sessionToken, NewColumnOrFilter filter)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomFilterBO(sessionToken);
        registerFilterOrColumn(filter, bo);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("DELETE_FILTER")
    public void deleteFilters(String sessionToken,
            @AuthorizationGuard(guardClass = DeleteGridCustomFilterPredicate.class) List<TechId> filterIds)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomFilterBO(sessionToken);
        deleteFiltersOrColumns(filterIds, bo);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("WRITE_FILTER")
    public void updateFilter(String sessionToken,
            @AuthorizationGuard(guardClass = UpdateGridCustomFilterPredicate.class) IExpressionUpdates updates)
    {
        assert updates != null : "Unspecified updates";
        createGridCustomFilterBO(sessionToken).update(updates);
    }

    // -- columns

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("WRITE_CUSTOM_COLUMN")
    public void registerGridCustomColumn(String sessionToken, NewColumnOrFilter column)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomColumnBO(sessionToken);
        registerFilterOrColumn(column, bo);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("DELETE_CUSTOM_COLUMN")
    public void deleteGridCustomColumns(String sessionToken,
            @AuthorizationGuard(guardClass = DeleteGridCustomColumnPredicate.class) List<TechId> columnIds)
    {
        IGridCustomFilterOrColumnBO bo = createGridCustomColumnBO(sessionToken);
        deleteFiltersOrColumns(columnIds, bo);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_POWER_USER)
    @Capability("WRITE_CUSTOM_COLUMN")
    public void updateGridCustomColumn(String sessionToken,
            @AuthorizationGuard(guardClass = UpdateGridCustomColumnPredicate.class) IExpressionUpdates updates)
    {
        assert updates != null : "Unspecified updates";
        createGridCustomColumnBO(sessionToken).update(updates);
    }

    // --

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void keepSessionAlive(String sessionToken)
    {
        checkSession(sessionToken);
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER })
    @Capability("WRITE_VOCABULARY")
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("DELETE_MATERIAL")
    public void deleteMaterials(String sessionToken, List<TechId> materialIds, String reason)
    {
        Session session = getSession(sessionToken);
        IMaterialTable materialTable = businessObjectFactory.createMaterialTable(session);
        materialTable.deleteByTechIds(materialIds, reason);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("LOCK_DATA_SETS")
    public int lockDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.lockDatasets();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("UNLOCK_DATA_SETS")
    public int unlockDatasets(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.unlockDatasets();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public LinkModel retrieveLinkFromDataSet(String sessionToken,
            DatastoreServiceDescription serviceDescription,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        return dataSetTable.retrieveLinkFromDataSet(serviceDescription.getKey(),
                serviceDescription.getDatastoreCode(), dataSetCode);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Script getScriptInfo(String sessionToken, TechId scriptId)
    {
        getSession(sessionToken);
        ScriptPE script = getDAOFactory().getScriptDAO().getByTechId(scriptId);
        return ScriptTranslator.enhancePredeployedPlugin(ScriptTranslator.translate(script),
                entityValidationFactory, dynamicPropertyCalculatorFactory,
                managedPropertyEvaluatorFactory);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public String evaluate(String sessionToken, DynamicPropertyEvaluationInfo info)
    {
        Session session = getSession(sessionToken);
        IEntityInformationWithPropertiesHolder entity = getEntity(info, session);
        try
        {
            IDynamicPropertyCalculator calculator =
                    dynamicPropertyCalculatorFactory.getCalculator(info.getPluginType(),
                            info.getSciptName(), info.getScript());
            IDynamicPropertyEvaluator evaluator =
                    new DynamicPropertyEvaluator(getDAOFactory(), null,
                            dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory);
            IEntityAdaptor adaptor =
                    EntityAdaptorFactory.create(entity, evaluator, getDAOFactory()
                            .getSessionFactory().getCurrentSession());
            return calculator.eval(adaptor);
        } catch (Throwable e)
        {
            operationLog.warn("Dynamic property evaluation failed", e);
            // return error message if there is a problem with evaluation
            return e.getMessage();
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public String evaluate(String sessionToken, EntityValidationEvaluationInfo info)
    {
        Session session = getSession(sessionToken);
        IEntityInformationWithPropertiesHolder entity = getEntity(info, session);
        try
        {
            final List<String> objectsWhichValidationWouldBeForced = new LinkedList<String>();

            IEntityValidator entityValidator =
                    entityValidationFactory.createEntityValidator(info.getPluginType(),
                            info.getScriptName(), info.getScript());
            entityValidator.init(new IValidationRequestDelegate<INonAbstractEntityAdapter>()
            {
                @Override
                public void requestValidation(INonAbstractEntityAdapter entityAdaptor)
                {
                    IEntityInformationWithPropertiesHolder localEntity =
                            entityAdaptor.entityPE();
                    objectsWhichValidationWouldBeForced.add(localEntity.getEntityKind() + " "
                            + localEntity.getIdentifier());
                }
            });

            IDynamicPropertyEvaluator evaluator =
                    new DynamicPropertyEvaluator(getDAOFactory(), null,
                            dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory);
            IEntityAdaptor adaptor =
                    EntityAdaptorFactory.create(entity, evaluator, getDAOFactory()
                            .getSessionFactory().getCurrentSession());
            String result = entityValidator.validate(adaptor, info.isNew());

            if (result != null)
            {
                return "Validation fail: " + result;
            } else
            {
                result = "Validation OK";
                if (objectsWhichValidationWouldBeForced.size() > 0)
                {
                    result += "\n\nOther entities would be forced to validate:\n";
                    for (Object o : objectsWhichValidationWouldBeForced)
                    {
                        result += "  " + o + "\n";
                    }
                }
                return result;
            }
        } catch (Throwable e)
        {
            operationLog.warn("Validation script evaluation failed", e);
            // return error message if there is a problem with evaluation
            return e.getMessage();
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public IEntityInformationHolderWithPermId getEntityInformationHolder(String sessionToken,
            @AuthorizationGuard(guardClass = BasicEntityDescriptionPredicate.class) BasicEntityDescription info)
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void updateManagedPropertyOnExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadDataByTechId(experimentId);

        // Evaluate the script
        experimentBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = experimentBO.getExperiment().getProperties();
        IManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty,
                PersonTranslator.translateToIPerson(session.tryGetPerson()), updateAction);

        experimentBO.updateManagedProperty(managedProperty);
        experimentBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void updateManagedPropertyOnSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdReadWritePredicate.class) TechId sampleId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);

        // Evaluate the script
        sampleBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = sampleBO.getSample().getProperties();
        IManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty,
                PersonTranslator.translateToIPerson(session.tryGetPerson()), updateAction);

        sampleBO.updateManagedProperty(managedProperty);
        sampleBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void updateManagedPropertyOnDataSet(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId dataSetId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        IDataBO dataSetBO = businessObjectFactory.createDataBO(session);
        dataSetBO.loadDataByTechId(dataSetId);

        // Evaluate the script
        dataSetBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = dataSetBO.getData().getProperties();
        IManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty,
                PersonTranslator.translateToIPerson(session.tryGetPerson()), updateAction);

        dataSetBO.updateManagedProperty(managedProperty);
        dataSetBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void updateManagedPropertyOnMaterial(String sessionToken, TechId materialId,
            IManagedProperty managedProperty, IManagedUiAction updateAction)
    {
        Session session = getSession(sessionToken);
        IMaterialBO materialBO = businessObjectFactory.createMaterialBO(session);
        materialBO.loadDataByTechId(materialId);

        // Evaluate the script
        materialBO.enrichWithProperties();
        Set<? extends EntityPropertyPE> properties = materialBO.getMaterial().getProperties();
        IManagedPropertyEvaluator evaluator =
                tryManagedPropertyEvaluator(managedProperty, properties);
        extendWithPerson(updateAction, session.tryGetPerson());
        evaluator.updateFromUI(managedProperty,
                PersonTranslator.translateToIPerson(session.tryGetPerson()), updateAction);

        materialBO.updateManagedProperty(managedProperty);
        materialBO.save();
    }

    private static void extendWithPerson(IManagedUiAction updateAction, PersonPE personOrNull)
    {
        if (personOrNull != null && updateAction instanceof ManagedUiActionDescription)
        {
            final IPerson person = PersonTranslator.translateToIPerson(personOrNull);
            final ManagedUiActionDescription action = (ManagedUiActionDescription) updateAction;
            action.setPerson(person);
        }
    }

    private IManagedPropertyEvaluator tryManagedPropertyEvaluator(IManagedProperty managedProperty,
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

        return managedPropertyEvaluatorFactory.createManagedPropertyEvaluator(managedPropertyPE
                .getEntityTypePropertyType());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        checkSession(sessionToken);
        IDataStoreDAO dataStoreDAO = getDAOFactory().getDataStoreDAO();
        List<DataStorePE> dataStores = dataStoreDAO.listDataStores();
        // Easy case: exactly one DSS
        if (dataStores.size() == 1)
        {
            return dataStores.get(0).getDownloadUrl();
        }
        // Error: No DSS at all
        if (dataStores.isEmpty())
        {
            throw new ConfigurationFailureException("No Data Store Server registered to openBIS.");
        }
        // More than one DSS: see whether dss-rpc.put.dss-code is configured to tell us which one to
        // use.
        if (defaultPutDataStoreServerCodeOrNull == null)
        {
            throw ConfigurationFailureException
                    .fromTemplate(
                            "There are %d Data Store Servers registered in openBIS, but property dss-rpc.put.dss-code is not set.",
                            dataStores.size());
        }
        for (DataStorePE store : dataStores)
        {
            if (defaultPutDataStoreServerCodeOrNull.equalsIgnoreCase(store.getCode()))
            {
                return store.getDownloadUrl();
            }
        }
        throw ConfigurationFailureException
                .fromTemplate(
                        "Property dss-rpc.put.dss-code is set to '%s', but no Data Store Server with that code is known.",
                        defaultPutDataStoreServerCodeOrNull);
    }

    public void setDefaultPutDataStoreServerCode(String defaultPutDataStoreServerCode)
    {
        this.defaultPutDataStoreServerCodeOrNull =
                isResolved(defaultPutDataStoreServerCode) ? defaultPutDataStoreServerCode : null;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_DATASET_PROPERTIES")
    public void updateDataSetProperties(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId entityId, List<PropertyUpdates> modifiedProperties)
    {
        checkSession(sessionToken);
        AbstractExternalData dataSet = getDataSetInfo(sessionToken, entityId);
        try
        {
            DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
            updates.setDatasetId(entityId);
            updates.setVersion(dataSet.getVersion());
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
            if (dataSet instanceof PhysicalDataSet)
            {
                updates.setFileFormatTypeCode(((PhysicalDataSet) dataSet).getFileFormatType()
                        .getCode());
            }
            updateDataSet(sessionToken, updates);
        } catch (UserFailureException e)
        {
            throw wrapExceptionWithEntityIdentifier(e, dataSet);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_EXPERIMENT_PROPERTIES")
    public void updateExperimentProperties(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId entityId, List<PropertyUpdates> modifiedProperties)
    {
        checkSession(sessionToken);
        Experiment experiment = getExperimentInfo(sessionToken, entityId);
        try
        {
            ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
            updates.setVersion(experiment.getVersion());
            updates.setExperimentId(entityId);
            updates.setAttachments(Collections.<NewAttachment>emptySet());
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @Capability("WRITE_SAMPLE_PROPERTIES")
    public void updateSampleProperties(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdReadWritePredicate.class) TechId entityId, List<PropertyUpdates> modifiedProperties)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @Capability("WRITE_MATERIAL_PROPERTIES")
    public void updateMaterialProperties(String sessionToken, TechId entityId,
            List<PropertyUpdates> modifiedProperties)
    {
        checkSession(sessionToken);
        Date modificationDate =
                getDAOFactory().getMaterialDAO().tryGetByTechId(entityId).getModificationDate();
        Map<String, String> properties = createPropertiesMap(modifiedProperties);
        updateMaterial(sessionToken, entityId,
                EntityHelper.translatePropertiesMapToList(properties), null, modificationDate);
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
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @ReturnValueFilter(validatorClass = DeletionValidator.class)
    public List<Deletion> listDeletions(String sessionToken, boolean withDeletedEntities)
    {
        Session session = getSession(sessionToken);
        IDeletionTable deletionTable = businessObjectFactory.createDeletionTable(session);
        deletionTable.load(withDeletedEntities);
        return deletionTable.getDeletions();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    @ReturnValueFilter(validatorClass = DeletionValidator.class)
    public List<Deletion> listOriginalDeletions(String sessionToken)
    {
        Session session = getSession(sessionToken);
        IDeletionTable deletionTable = businessObjectFactory.createDeletionTable(session);
        deletionTable.loadOriginal();
        return deletionTable.getDeletions();
    }

    @Override
    public void revertDeletions(final String sessionToken, final List<TechId> deletionIds)
    {
        if (deletionIds != null && !deletionIds.isEmpty())
        {
            List<IDeletionId> v3DeletionIds = deletionIds.stream().map(id -> new DeletionTechId(id.getId())).collect(Collectors.toList());
            CommonServiceProvider.getApplicationServerApi().revertDeletions(sessionToken, v3DeletionIds);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_ADMIN)
    @Capability("PURGE")
    public void deletePermanently(final String sessionToken,
            @AuthorizationGuard(guardClass = DeletionTechIdCollectionPredicate.class) final List<TechId> deletionIds)
    {
        deletePermanentlyCommon(sessionToken, deletionIds, false);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_DISABLED)
    @Capability("FORCE_PURGE")
    public void deletePermanentlyForced(final String sessionToken,
            @AuthorizationGuard(guardClass = DeletionTechIdCollectionPredicate.class) final List<TechId> deletionIds)
    {
        deletePermanentlyCommon(sessionToken, deletionIds, true);
    }

    private void deletePermanentlyCommon(String sessionToken, List<TechId> deletionIds,
            boolean forceDisallowedTypes)
    {
        Session session = getSession(sessionToken);
        PersonPE registrator = session.tryGetPerson();

        IDeletionDAO deletionDAO = getDAOFactory().getDeletionDAO();
        ISampleDAO sampleDAO = getDAOFactory().getSampleDAO();
        Collections.sort(deletionIds, TECH_ID_COMPARATOR);
        // NOTE: we can't do bulk deletions to preserve original reasons
        for (TechId deletionId : deletionIds)
        {
            DeletionPE deletion = deletionDAO.getByTechId(deletionId);
            String deletionReason = deletion.getReason();
            DeletionType deletionType = DeletionType.PERMANENT;

            List<TechId> singletonList = Collections.singletonList(deletionId);
            List<String> trashedDataSets = deletionDAO.findTrashedDataSetCodes(singletonList);
            deleteDataSetsCommon(sessionToken, trashedDataSets, deletionReason, deletionType,
                    forceDisallowedTypes, true);

            sampleDAO.deletePermanently(deletion, registrator);

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
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Material> searchForMaterials(String sessionToken, DetailedSearchCriteria criteria)
    {
        final Session session = getSession(sessionToken);

        return operationLimiter.executeLimitedWithTimeout(ConcurrentOperation.SEARCH_MATERIALS, new ConcurrentOperation<List<Material>>()
        {
            @Override
            public List<Material> execute()
            {
                SearchHelper searchHelper =
                        new SearchHelper(session, businessObjectFactory, getDAOFactory());
                return searchHelper.searchForMaterials(session.getUserName(), criteria);
            }
        });
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_USER)
    public void performCustomImport(String sessionToken, String customImportCode,
            CustomImportFile customImportFile) throws UserFailureException
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
                return;
            }
        }

        throw new UserFailureException(String.format("Cannot upload file '%s' to the dss.",
                customImportFile.getFileName()));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
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
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public ExternalDataManagementSystem getExternalDataManagementSystem(String sessionToken,
            String code)
    {
        checkSession(sessionToken);

        return ExternalDataManagementSystemTranslator.translate(getDAOFactory()
                .getExternalDataManagementSystemDAO().tryToFindExternalDataManagementSystemByCode(
                        code));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
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
        }

        edmsDAO.createOrUpdateExternalDataManagementSystem(ExternalDataManagementSystemTranslator
                .translate(edms, edmsPE));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<Metaproject> listMetaprojects(String sessionToken)
    {
        Session session = getSession(sessionToken);
        PersonPE owner = session.tryGetPerson();
        return listMetaProjects(owner);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<Metaproject> listMetaprojectsOnBehalfOfUser(String sessionToken, String userId)
    {
        checkSession(sessionToken);
        PersonPE user = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        if (user == null)
        {
            throw new UserFailureException("Unknown user: " + userId);
        }
        return listMetaProjects(user);
    }

    private List<Metaproject> listMetaProjects(PersonPE owner)
    {
        IDAOFactory daoFactory = getDAOFactory();
        IMetaprojectDAO metaprojectDAO = daoFactory.getMetaprojectDAO();

        List<MetaprojectPE> metaprojectPEs = metaprojectDAO.listMetaprojects(owner);

        return MetaprojectTranslator.translate(metaprojectPEs);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<MetaprojectAssignmentsCount> listMetaprojectAssignmentsCounts(String sessionToken)
    {
        List<Metaproject> metaprojects = listMetaprojects(sessionToken);
        List<MetaprojectAssignmentsCount> counts =
                new ArrayList<MetaprojectAssignmentsCount>(metaprojects.size());

        for (Metaproject metaproject : metaprojects)
        {
            counts.add(getMetaprojectAssignmentsCount(metaproject));
        }

        return counts;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public MetaprojectAssignmentsCount getMetaprojectAssignmentsCount(String sessionToken,
            IMetaprojectId metaprojectId)
    {
        Metaproject metaproject = getMetaproject(sessionToken, metaprojectId);
        return getMetaprojectAssignmentsCount(metaproject);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public MetaprojectAssignments getMetaprojectAssignments(String sessionToken,
            IMetaprojectId metaprojectId)
    {
        return getMetaprojectAssignments(sessionToken, metaprojectId,
                EnumSet.allOf(MetaprojectAssignmentsFetchOption.class));
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public MetaprojectAssignments getMetaprojectAssignmentsOnBehalfOfUser(String sessionToken,
            IMetaprojectId metaprojectId, String userId)
    {
        Session session = getSession(sessionToken);
        PersonPE user = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userId);
        if (user == null)
        {
            throw new UserFailureException("Unknown user: " + userId);
        }
        return getMetaprojectAssignments(session, metaprojectId,
                EnumSet.allOf(MetaprojectAssignmentsFetchOption.class), user);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public MetaprojectAssignments getMetaprojectAssignments(String sessionToken,
            IMetaprojectId metaprojectId, EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions)
    {
        Session session = getSession(sessionToken);
        return getMetaprojectAssignments(session, metaprojectId, fetchOptions,
                session.tryGetPerson());
    }

    private MetaprojectAssignments getMetaprojectAssignments(Session session,
            IMetaprojectId metaprojectId, EnumSet<MetaprojectAssignmentsFetchOption> fetchOptions,
            PersonPE user)
    {
        if (metaprojectId == null)
        {
            throw new UserFailureException("Metaproject id cannot be null");
        }
        if (fetchOptions == null)
        {
            throw new UserFailureException("Fetch options cannot be null");
        }

        Metaproject metaproject = getMetaproject(session, metaprojectId, user, true);

        MetaprojectAssignmentsHelper helper =
                new MetaprojectAssignmentsHelper(getDAOFactory(), managedPropertyEvaluatorFactory);

        return helper.getMetaprojectAssignments(session, metaproject, session.getUserName(),
                fetchOptions);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void addToMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToAdd)
    {
        if (metaprojectId == null)
        {
            throw new UserFailureException("Metaproject id cannot be null");
        }
        if (assignmentsToAdd == null)
        {
            throw new UserFailureException("Assignments to add cannot be null");
        }

        Session session = getSession(sessionToken);

        IMetaprojectBO metaprojectBO = getBusinessObjectFactory().createMetaprojectBO(session);
        metaprojectBO.loadByMetaprojectId(metaprojectId);

        getAuthorizationService(session).checkAccessMetaproject(metaprojectBO.getMetaproject());

        metaprojectBO.addExperiments(assignmentsToAdd.getExperiments());
        metaprojectBO.addSamples(assignmentsToAdd.getSamples());
        metaprojectBO.addDataSets(assignmentsToAdd.getDataSets());
        metaprojectBO.addMaterials(assignmentsToAdd.getMaterials());

        metaprojectBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void removeFromMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            MetaprojectAssignmentsIds assignmentsToRemove)
    {
        if (metaprojectId == null)
        {
            throw new UserFailureException("Metaproject id cannot be null");
        }
        if (assignmentsToRemove == null)
        {
            throw new UserFailureException("Assignments to remove cannot be null");
        }

        Session session = getSession(sessionToken);

        IMetaprojectBO metaprojectBO = getBusinessObjectFactory().createMetaprojectBO(session);
        metaprojectBO.loadByMetaprojectId(metaprojectId);

        getAuthorizationService(session).checkAccessMetaproject(metaprojectBO.getMetaproject());

        metaprojectBO.removeExperiments(assignmentsToRemove.getExperiments());
        metaprojectBO.removeSamples(assignmentsToRemove.getSamples());
        metaprojectBO.removeDataSets(assignmentsToRemove.getDataSets());
        metaprojectBO.removeMaterials(assignmentsToRemove.getMaterials());

        metaprojectBO.save();
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void deleteMetaproject(String sessionToken, IMetaprojectId metaprojectId, String reason)
    {
        if (metaprojectId == null)
        {
            throw new UserFailureException("Metaproject id cannot be null");
        }

        Session session = getSession(sessionToken);

        IMetaprojectBO metaprojectBO = getBusinessObjectFactory().createMetaprojectBO(session);
        metaprojectBO.loadByMetaprojectId(metaprojectId);

        getAuthorizationService(session).checkAccessMetaproject(metaprojectBO.getMetaproject());

        metaprojectBO.deleteByMetaprojectId(metaprojectId, reason);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public void deleteMetaprojects(String sessionToken, List<IMetaprojectId> metaprojectIds,
            String reason)
    {
        if (metaprojectIds == null)
        {
            throw new UserFailureException("Metaproject ids cannot be null");
        }

        for (IMetaprojectId metaprojectId : metaprojectIds)
        {
            deleteMetaproject(sessionToken, metaprojectId, reason);
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Metaproject registerMetaproject(String sessionToken,
            IMetaprojectRegistration registration)
    {
        Session session = getSession(sessionToken);

        IMetaprojectBO metaprojectBO = getBusinessObjectFactory().createMetaprojectBO(session);
        metaprojectBO.define(session.tryGetPerson().getUserId(), registration);
        metaprojectBO.save();

        return MetaprojectTranslator.translate(metaprojectBO.getMetaproject());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Metaproject updateMetaproject(String sessionToken, IMetaprojectId metaprojectId,
            IMetaprojectUpdates updates)
    {
        Session session = getSession(sessionToken);

        IMetaprojectBO metaprojectBO = getBusinessObjectFactory().createMetaprojectBO(session);
        metaprojectBO.loadByMetaprojectId(metaprojectId);

        getAuthorizationService(session).checkAccessMetaproject(metaprojectBO.getMetaproject());

        metaprojectBO.update(updates);
        metaprojectBO.save();

        return MetaprojectTranslator.translate(metaprojectBO.getMetaproject());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public Metaproject getMetaproject(String sessionToken, IMetaprojectId metaprojectId)
    {
        Session session = getSession(sessionToken);
        return getMetaproject(session, metaprojectId, session.tryGetPerson());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.SPACE_ETL_SERVER)
    public Metaproject getMetaprojectWithoutOwnershipChecks(String sessionToken, IMetaprojectId metaprojectId)
    {
        Session session = getSession(sessionToken);
        return getMetaproject(session, metaprojectId, session.tryGetPerson(), false);
    }

    private Metaproject getMetaproject(Session session, IMetaprojectId metaprojectId, PersonPE user)
    {
        return getMetaproject(session, metaprojectId, user, true);
    }

    private Metaproject getMetaproject(Session session, IMetaprojectId metaprojectId, PersonPE user, boolean checkAccess)
    {
        if (metaprojectId == null)
        {
            throw new UserFailureException("Metaproject id cannot be null");
        }

        IMetaprojectBO metaprojectBO = getBusinessObjectFactory().createMetaprojectBO(session);
        MetaprojectPE metaprojectPE = metaprojectBO.tryFindByMetaprojectId(metaprojectId);

        if (metaprojectPE == null)
        {
            throw new UserFailureException("Metaproject with id: " + metaprojectId
                    + " doesn't exist");
        }

        if (checkAccess)
        {
            AuthorizationServiceUtils authorizationUtils =
                    new AuthorizationServiceUtils(getDAOFactory(), user);
            authorizationUtils.checkAccessMetaproject(metaprojectPE);
        }
        return MetaprojectTranslator.translate(metaprojectPE);
    }

    private MetaprojectAssignmentsCount getMetaprojectAssignmentsCount(Metaproject metaproject)
    {
        IMetaprojectDAO metaprojectDAO = getDAOFactory().getMetaprojectDAO();

        MetaprojectAssignmentsCount count = new MetaprojectAssignmentsCount();
        count.setMetaproject(metaproject);
        count.setExperimentCount(metaprojectDAO.getMetaprojectAssignmentsCount(metaproject.getId(),
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.EXPERIMENT));
        count.setSampleCount(metaprojectDAO.getMetaprojectAssignmentsCount(metaproject.getId(),
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.SAMPLE));
        count.setDataSetCount(metaprojectDAO.getMetaprojectAssignmentsCount(metaproject.getId(),
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.DATA_SET));
        count.setMaterialCount(metaprojectDAO.getMetaprojectAssignmentsCount(metaproject.getId(),
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL));
        return count;
    }

    private AuthorizationServiceUtils getAuthorizationService(Session session)
    {
        return new AuthorizationServiceUtils(getDAOFactory(), session.tryGetPerson());
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.PROJECT_OBSERVER)
    public List<String> listPredeployedPlugins(String sessionToken, ScriptType scriptType)
    {
        checkSession(sessionToken);

        switch (scriptType)
        {
            case ENTITY_VALIDATION:
                return entityValidationFactory.listPredeployedPlugins();
            case DYNAMIC_PROPERTY:
                return dynamicPropertyCalculatorFactory.listPredeployedPlugins();
            case MANAGED_PROPERTY:
                return managedPropertyEvaluatorFactory.listPredeployedPlugins();
        }

        return null;
    }

    @Override
    public String getDisabledText()
    {
        return tryGetDisabledText();
    }
}
