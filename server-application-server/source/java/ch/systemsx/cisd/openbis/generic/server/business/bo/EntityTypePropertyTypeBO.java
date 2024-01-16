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
package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.EntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;

/**
 * The unique {@link IEntityTypePropertyTypeBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public class EntityTypePropertyTypeBO extends AbstractBusinessObject implements
        IEntityTypePropertyTypeBO
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EntityTypePropertyTypeBO.class);

    private EntityKind entityKind;

    private IEntityPropertiesConverter propertiesConverter;

    private EntityTypePropertyTypePE assignment;

    @Private
    EntityTypePropertyTypeBO(IDAOFactory daoFactory, Session session, EntityKind entityKind,
            IEntityPropertiesConverter converter,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        propertiesConverter = converter;
        this.entityKind = entityKind;
    }

    public EntityTypePropertyTypeBO(IDAOFactory daoFactory, Session session, EntityKind entityKind,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        propertiesConverter =
                new EntityPropertiesConverter(entityKind, daoFactory, entityInformationProvider,
                        managedPropertyEvaluatorFactory);
        this.entityKind = entityKind;
    }

    @Override
    public EntityTypePropertyTypePE getLoadedAssignment()
    {
        if (assignment == null)
        {
            throw new IllegalStateException("No assignment loaded.");
        }
        return assignment;
    }

    @Override
    public void deleteLoadedAssignment()
    {
        if (assignment == null)
        {
            return;
        }

        new InternalPropertyTypeAuthorization().canDeletePropertyAssignment(session, assignment.getPropertyType(), assignment);

        getEntityPropertyTypeDAO(entityKind).delete(assignment);
        assignment = null;
    }

    @Override
    public void loadAssignment(String propertyTypeCode, String entityTypeCode)
    {
        EntityTypePE entityType = findEntityType(entityTypeCode);
        PropertyTypePE propertyType = findPropertyType(propertyTypeCode);
        IEntityPropertyTypeDAO entityPropertyTypeDAO = getEntityPropertyTypeDAO(entityKind);
        assignment = entityPropertyTypeDAO.tryFindAssignment(entityType, propertyType);
    }

    @Override
    public int countAssignmentValues(String propertyTypeCode, String entityTypeCode)
    {
        IEntityPropertyTypeDAO entityPropertyTypeDAO = getEntityPropertyTypeDAO(entityKind);
        return entityPropertyTypeDAO.countAssignmentValues(entityTypeCode, propertyTypeCode);
    }

    @Override
    public void createAssignment(NewETPTAssignment newAssignment)
    {
        EntityTypePE entityType = findEntityType(newAssignment.getEntityTypeCode());
        PropertyTypePE propertyType = findPropertyType(newAssignment.getPropertyTypeCode());
        ScriptPE scriptOrNull = tryFindScript(newAssignment);
        assignment =
                createAssignment(newAssignment.isMandatory(), newAssignment.getSection(),
                        newAssignment.getOrdinal(), entityType, propertyType, scriptOrNull,
                        newAssignment.isShownInEditView(), newAssignment.getShowRawValue(),
                        newAssignment.isUnique());
        String defaultValue = newAssignment.getDefaultValue();
        if (newAssignment.isDynamic())
        {
            List<Long> entityIds = getAllEntityIds(entityType);
            addPropertyWithDefaultValue(entityType, propertyType,
                    BasicConstant.DYNAMIC_PROPERTY_PLACEHOLDER_VALUE, entityIds, null);
        } else if (newAssignment.isManaged())
        {
            List<Long> entityIds = getAllEntityIds(entityType);
            addPropertyWithDefaultValue(entityType, propertyType,
                    BasicConstant.MANAGED_PROPERTY_PLACEHOLDER_VALUE, entityIds, null);
        } else if (newAssignment.isMandatory())
        // fill default property values
        {
            String errorMsgTemplate =
                    "Cannot create mandatory assignment. "
                            + "Please specify 'Initial Value', which will be used for %s %s%s "
                            + "of type '%s' already existing in the database.";
            List<Long> entityIds = getAllEntityIds(entityType);
            addPropertyWithDefaultValue(entityType, propertyType, defaultValue, entityIds,
                    errorMsgTemplate);
        } else if (StringUtils.isEmpty(defaultValue) == false)
        {
            List<Long> entityIds = getAllEntityIds(entityType);
            addPropertyWithDefaultValue(entityType, propertyType, defaultValue, entityIds, null);
        }
    }

    private ScriptPE tryFindScript(NewETPTAssignment newAssignment)
    {
        if (newAssignment.getScriptName() == null)
        {
            return null;
        } else
        {
            return getScriptDAO().tryFindByName(newAssignment.getScriptName());
        }
    }

    private List<Long> getAllEntityIds(EntityTypePE entityType)
    {
        return getEntityPropertyTypeDAO(entityKind).listEntityIds(entityType);
    }

    private void addPropertyWithDefaultValue(EntityTypePE entityType, PropertyTypePE propertyType,
            String defaultValue, List<Long> entityIds, String errorMsgTemplate)
    {
        IEntityPropertyTypeDAO entityPropertyTypeDAO = getEntityPropertyTypeDAO(entityKind);
        final int size = entityIds.size();
        if (size > 0)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(getMemoryUsageMessage());
            }
            if (StringUtils.isEmpty(defaultValue))
            {
                throw new UserFailureException(String.format(errorMsgTemplate, size,
                        entityKind.getLabel(), createPlural(size), entityType.getCode()));
            }
            PersonPE registrator = findPerson();
            String validatedValue =
                    propertiesConverter.tryCreateValidatedPropertyValue(propertyType, assignment,
                            defaultValue);

            if (validatedValue != null)
            {
                final List<EntityPropertyPE> properties =
                        propertiesConverter.createValidatedProperty(propertyType, assignment,
                                registrator, validatedValue);
                for (EntityPropertyPE property : properties) {
                    entityPropertyTypeDAO.createProperties(property, entityIds);
                }
                entityPropertyTypeDAO.updateEntityModificationTimestamps(entityIds);
            }

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(getMemoryUsageMessage());
            }
        }
    }

    private String getMemoryUsageMessage()
    {
        Runtime runtime = Runtime.getRuntime();
        long mb = 1024l * 1024l;
        long totalMemory = runtime.totalMemory() / mb;
        long freeMemory = runtime.freeMemory() / mb;
        long maxMemory = runtime.maxMemory() / mb;
        return "MEMORY (in MB): free:" + freeMemory + " total:" + totalMemory + " max:" + maxMemory;
    }

    @Override
    public void updateLoadedAssignment(NewETPTAssignment assignmentUpdates)
    {
        if (assignmentUpdates.getModificationDate() != null && // Avoid validation, needed to make multiple modifications with one call
                assignment.getModificationDate().equals(assignmentUpdates.getModificationDate()) == false)
        {
            throwModifiedEntityException("Property type assignment");
        }

        UpdateAuthorizationSnapshot snapshotBefore = new UpdateAuthorizationSnapshot(assignment);

        // if ordinal was changed some etpts need to be shifted by 1
        Long ordinal = assignmentUpdates.getOrdinal();
        final Long currentOrdinal = (ordinal == null ? assignment.getOrdinal() : ordinal) + 1;
        if (assignment.getOrdinal().equals(currentOrdinal) == false)
        {
            increaseOrdinals(assignment.getEntityType(), currentOrdinal, 1);
        }
        assignment.setOrdinal(currentOrdinal);
        assignment.setSection(assignmentUpdates.getSection());
        // fill missing property values if we change from optional to mandatory
        if (assignmentUpdates.isMandatory() && (assignment.isMandatory() == false))
        {
            final EntityTypePE entityType = assignment.getEntityType();
            final PropertyTypePE propertyType = assignment.getPropertyType();
            String errorMsgTemplate =
                    "Cannot change assignment to mandatory. "
                            + "Please specify 'Update Value', which will be used for %s %s%s "
                            + "of type '%s' already existing in the database "
                            + "without any value for this property.";
            List<Long> entityIds =
                    getEntityPropertyTypeDAO(entityKind).listIdsOfEntitiesWithoutPropertyValue(
                            assignment);
            addPropertyWithDefaultValue(entityType, propertyType,
                    assignmentUpdates.getDefaultValue(), entityIds, errorMsgTemplate);
        }
        assignment.setMandatory(assignmentUpdates.isMandatory());
        if (assignmentUpdates.isDynamic() != assignment.isDynamic())
        {
            throw new UserFailureException(String.format(
                    "Changing assignment from '%s' to '%s' for %s.%s is not allowed. "
                            + "Please create a new assignment.",
                    describeDynamic(assignment.isDynamic()),
                    describeDynamic(assignmentUpdates.isDynamic()),
                    assignmentUpdates.getEntityTypeCode(),
                    assignmentUpdates.getPropertyTypeCode()));
        }
        boolean scriptChanged = false;
        if ((assignment.isDynamic() || assignment.isManaged())
                && assignment.getScript().getName().equals(assignmentUpdates.getScriptName()) == false)
        {
            scriptChanged = true;
            ScriptPE script = getScriptDAO().tryFindByName(assignmentUpdates.getScriptName());
            assignment.setScript(script);
        }
        assignment.setShownInEditView(assignmentUpdates.isShownInEditView());
        assignment.setShowRawValue(assignmentUpdates.getShowRawValue());

        UpdateAuthorizationSnapshot snapshotAfter = new UpdateAuthorizationSnapshot(assignment);

        if (snapshotBefore.equals(snapshotAfter) == false)
        {
            new InternalPropertyTypeAuthorization().canUpdatePropertyAssignment(session, assignment.getPropertyType(), assignment);
        }

        validateAndSave();

        if (scriptChanged)
        {
            getEntityPropertyTypeDAO(entityKind).scheduleDynamicPropertiesEvaluation(assignment);
        }
    }

    private static String describeDynamic(boolean dynamic)
    {
        return (dynamic ? "" : "not ") + "dynamic";
    }

    private void validateAndSave()
    {
        getEntityPropertyTypeDAO(entityKind).validateAndSaveUpdatedEntity(assignment);
    }

    private String createPlural(int size)
    {
        return size == 1 ? "" : "s";
    }

    private EntityTypePropertyTypePE createAssignment(final boolean mandatory,
            final String section, final Long previousETPTOrdinal, final EntityTypePE entityType,
            final PropertyTypePE propertyType, ScriptPE scriptOrNull, boolean shownInEditView,
            boolean showRawValue, boolean unique)
    {
        checkAssignmentDoesNotExist(entityType, propertyType);
        Long previousOrdinal =
                (previousETPTOrdinal == null) ? getEntityPropertyTypeDAO(entityKind).getMaxOrdinal(
                        entityType) : previousETPTOrdinal;
        // need to shift existing etpts to create space for new one
        final Long currentOrdinal = previousOrdinal + 1;
        increaseOrdinals(entityType, currentOrdinal, 1);

        final EntityTypePropertyTypePE etpt =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        etpt.setPropertyType(propertyType);
        etpt.setRegistrator(findPerson());
        etpt.setEntityType(entityType);
        etpt.setMandatory(mandatory);
        etpt.setSection(section);
        etpt.setOrdinal(currentOrdinal);
        etpt.setScript(scriptOrNull);
        etpt.setShownInEditView(shownInEditView);
        etpt.setShowRawValue(showRawValue);
        etpt.setUnique(unique);

        new InternalPropertyTypeAuthorization().canCreatePropertyAssignment(session, etpt.getPropertyType(), etpt);

        try
        {
            getEntityPropertyTypeDAO(entityKind).createEntityPropertyTypeAssignment(etpt);
        } catch (DataAccessException e)
        {
            throwException(e, createExceptionMessage(entityType, propertyType));
        }
        return etpt;
    }

    /**
     * shift specified entity type etpts by specified increment starting from etpt with specified ordinal
     * 
     * @param entityType
     */
    private void increaseOrdinals(EntityTypePE entityType, Long startOrdinal, int increment)
    {
        getEntityPropertyTypeDAO(entityKind).increaseOrdinals(entityType, startOrdinal, increment);
    }

    private PropertyTypePE findPropertyType(String propertyTypeCode)
    {
        PropertyTypePE propertyType =
                getPropertyTypeDAO().tryFindPropertyTypeByCode(propertyTypeCode);
        if (propertyType == null)
        {
            throw new UserFailureException(String.format("Property type '%s' does not exist.",
                    propertyTypeCode));
        }
        // if (propertyType.isManagedInternally())
        // {
        // throw new UserFailureException(String.format(
        // "Property type '%s' is managed internally.", propertyTypeCode));
        // }
        return propertyType;
    }

    private EntityTypePE findEntityType(String entityTypeCode)
    {
        EntityTypePE entityType =
                getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(entityTypeCode);
        if (entityType == null)
        {
            throw new UserFailureException(String.format("%s type '%s' does not exist.",
                    StringUtils.capitalize(entityKind.getLabel()), entityTypeCode));
        }
        return entityType;
    }

    private void checkAssignmentDoesNotExist(EntityTypePE entityType, PropertyTypePE propertyType)
    {
        if (getEntityPropertyTypeDAO(entityKind).tryFindAssignment(entityType, propertyType) != null)
        {
            throw new UserFailureException(createExceptionMessage(entityType, propertyType));
        }
    }

    private String createExceptionMessage(EntityTypePE entityType, PropertyTypePE propertyType)
    {
        return String.format("Property type '%s' is already assigned to %s type '%s'.",
                propertyType.getCode(), entityKind.getLabel(), entityType.getCode());
    }

    private static class UpdateAuthorizationSnapshot
    {
        @SuppressWarnings("unused")
        private boolean mandatory;

        @SuppressWarnings("unused")
        private ScriptPE script;

        @SuppressWarnings("unused")
        private boolean showInEditView;

        @SuppressWarnings("unused")
        private boolean showRawValue;

        public UpdateAuthorizationSnapshot(EntityTypePropertyTypePE assignment)
        {
            this.mandatory = assignment.isMandatory();
            this.script = assignment.getScript();
            this.showInEditView = assignment.isShownInEditView();
            this.showRawValue = assignment.isShownInEditView();
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
    }
}
