/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IMetaprojectRegistration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IMetaprojectUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.material.IMaterialId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.IMetaprojectId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.metaproject.MetaprojectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author Pawel Glyzewski
 */
public class MetaprojectBO extends AbstractBusinessObject implements IMetaprojectBO
{

    private IExperimentBO experimentBO;

    private ISampleBO sampleBO;

    private IDataBO dataBO;

    private IMaterialBO materialBO;

    private MetaprojectPE metaproject;

    private Map<Class<? extends IEntityWithMetaprojects>, List<Long>> changedEntitiesIds;

    private List<IEntityWithMetaprojects> entitiesWithMetaproject =
            new ArrayList<IEntityWithMetaprojects>();

    private boolean dataChanged;

    public MetaprojectBO(final IDAOFactory daoFactory, IExperimentBO experimentBO,
            ISampleBO sampleBO, IDataBO dataBO, IMaterialBO materialBO, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);

        this.experimentBO = experimentBO;
        this.sampleBO = sampleBO;
        this.dataBO = dataBO;
        this.materialBO = materialBO;
    }

    @Override
    public MetaprojectPE tryFindByMetaprojectId(IMetaprojectId metaprojectId)
    {
        if (metaprojectId == null)
        {
            throw new UserFailureException("Metaproject id cannot be null");
        }
        if (metaprojectId instanceof MetaprojectIdentifierId)
        {
            MetaprojectIdentifierId identifierId = (MetaprojectIdentifierId) metaprojectId;
            MetaprojectIdentifier identifier =
                    MetaprojectIdentifier.parse(identifierId.getIdentifier());
            return getMetaprojectDAO().tryFindByOwnerAndName(identifier.getMetaprojectOwnerId(),
                    identifier.getMetaprojectName());
        } else if (metaprojectId instanceof MetaprojectTechIdId)
        {
            MetaprojectTechIdId techIdId = (MetaprojectTechIdId) metaprojectId;
            return getMetaprojectDAO().tryGetByTechId(new TechId(techIdId.getTechId()));
        } else
        {
            throw new IllegalArgumentException("Unsupported metaproject id: " + metaprojectId);
        }
    }

    @Override
    public void loadDataByTechId(TechId metaprojectId)
    {
        try
        {
            metaproject = getMetaprojectDAO().getByTechId(metaprojectId);
        } catch (ObjectRetrievalFailureException exception)
        {
            throw new UserFailureException(String.format(
                    "Metaproject with ID '%s' does not exist.", metaprojectId));
        }

        initChangedEntities();
        dataChanged = false;
    }

    @Override
    public void loadByMetaprojectId(IMetaprojectId metaprojectId)
    {
        metaproject = tryFindByMetaprojectId(metaprojectId);

        if (metaproject == null)
        {
            throw new UserFailureException(String.format(
                    "Metaproject with ID '%s' does not exist.", metaprojectId));
        }

        initChangedEntities();
        dataChanged = false;
    }

    @Override
    public MetaprojectPE getMetaproject()
    {
        return metaproject;
    }

    @Override
    public void save() throws UserFailureException
    {
        assert metaproject != null : "Can not save an undefined metaproject.";

        if (dataChanged)
        {
            for (IEntityWithMetaprojects entity : entitiesWithMetaproject)
            {
                entity.addMetaproject(metaproject);
            }
            try
            {
                getMetaprojectDAO().createOrUpdateMetaproject(metaproject, findPerson());

                IDynamicPropertyEvaluationScheduler indexUpdater =
                        getPersistencyResources().getDynamicPropertyEvaluationScheduler();

                for (Map.Entry<Class<? extends IEntityWithMetaprojects>, List<Long>> changedEntry : changedEntitiesIds.entrySet())
                {

                    indexUpdater.scheduleUpdate(DynamicPropertyEvaluationOperation.evaluate(changedEntry.getKey(), changedEntry.getValue()));
                }

            } catch (final DataAccessException ex)
            {
                throwException(ex, "Metaproject '" + metaproject.getName() + "'");
            }

            dataChanged = false;
        }
    }

    @Override
    public void deleteByMetaprojectId(IMetaprojectId metaprojectId, String reason)
            throws UserFailureException
    {
        loadByMetaprojectId(metaprojectId);

        getMetaprojectDAO().delete(metaproject);
        getEventDAO().persist(createDeletionEvent(metaproject, session.tryGetPerson(), reason));
    }

    private static EventPE createDeletionEvent(MetaprojectPE metaproject, PersonPE registrator,
            String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.METAPROJECT);
        event.setIdentifiers(Collections.singletonList(metaproject.getName()));
        event.setDescription(metaproject.getName());
        event.setRegistrator(registrator);
        event.setReason(reason);

        return event;
    }

    @Override
    public void define(String ownerId, IMetaprojectRegistration registration)
    {
        if (registration == null)
        {
            throw new UserFailureException("Metaproject data cannot be null");
        }
        if (registration.getName() == null)
        {
            throw new UserFailureException("Metaproject name cannot be null");
        }

        metaproject = new MetaprojectPE();
        metaproject.setName(registration.getName());
        metaproject.setDescription(registration.getDescription());
        metaproject.setOwner(getPersonDAO().tryFindPersonByUserId(ownerId));
        metaproject.setPrivate(true);

        initChangedEntities();
        dataChanged = true;
    }

    @Override
    public void update(IMetaprojectUpdates updates)
    {
        if (updates == null)
        {
            throw new UserFailureException("Metaproject data cannot be null");
        }
        if (updates.getName() == null)
        {
            throw new UserFailureException("Metaproject name cannot be null");
        }

        try
        {
            if (metaproject.getName().equals(updates.getName()) == false)
            {
                metaproject.setName(updates.getName());
                addToChangedEntities(ExperimentPE.class, listEntityIds(EntityKind.EXPERIMENT));
                addToChangedEntities(SamplePE.class, listEntityIds(EntityKind.SAMPLE));
                addToChangedEntities(DataPE.class, listEntityIds(EntityKind.DATA_SET));
                addToChangedEntities(MaterialPE.class, listEntityIds(EntityKind.MATERIAL));
            }
            metaproject.setDescription(updates.getDescription());
            dataChanged = true;
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Metaproject '" + metaproject.getName() + "'");
        }
    }

    @Override
    public void addExperiments(List<? extends IExperimentId> experimentIds)
    {
        addEntities(experimentIds);
    }

    @Override
    public void addSamples(List<? extends ISampleId> sampleIds)
    {
        addEntities(sampleIds);
    }

    @Override
    public void addDataSets(List<? extends IDataSetId> dataSetIds)
    {
        addEntities(dataSetIds);
    }

    @Override
    public void addMaterials(List<? extends IMaterialId> materialIds)
    {
        addEntities(materialIds);
    }

    @Override
    public void removeExperiments(List<? extends IExperimentId> experimentIds)
    {
        removeEntities(experimentIds);
    }

    @Override
    public void removeSamples(List<? extends ISampleId> sampleIds)
    {
        removeEntities(sampleIds);
    }

    @Override
    public void removeDataSets(List<? extends IDataSetId> dataSetIds)
    {
        removeEntities(dataSetIds);
    }

    @Override
    public void removeMaterials(List<? extends IMaterialId> materialIds)
    {
        removeEntities(materialIds);
    }

    private <T extends IObjectId> void addEntities(Collection<T> entityIds)
    {
        for (T entityId : entityIds)
        {
            IEntityWithMetaprojects entityPE = findById(entityId);
            if (entityPE == null)
            {
                throw new IllegalArgumentException("Entity for id: " + entityId + " doesn't exist.");
            }

            boolean canAccess = canAccessEntity(entityPE);
            if (false == canAccess)
            {
                throw new AuthorizationFailureException("Cannot access entity with id " + entityId);
            }

            entitiesWithMetaproject.add(entityPE);
            addToChangedEntities(entityPE.getClass(), entityPE.getId());
        }
        dataChanged = true;
    }

    private <T extends IObjectId> void removeEntities(Collection<T> entityIds)
    {
        for (T entityId : entityIds)
        {
            IEntityWithMetaprojects entityPE = findById(entityId);
            if (entityPE != null)
            {
                entityPE.removeMetaproject(metaproject);
                addToChangedEntities(entityPE.getClass(), entityPE.getId());
            }
        }
        dataChanged = true;
    }

    private IEntityWithMetaprojects findById(IObjectId entityId)
    {
        if (entityId instanceof IMaterialId)
        {
            return materialBO.tryFindByMaterialId((IMaterialId) entityId);
        } else if (entityId instanceof ISampleId)
        {
            return sampleBO.tryFindBySampleId((ISampleId) entityId);
        } else if (entityId instanceof IDataSetId)
        {
            return dataBO.tryFindByDataSetId((IDataSetId) entityId);
        } else if (entityId instanceof IExperimentId)
        {
            return experimentBO.tryFindByExperimentId((IExperimentId) entityId);
        } else
        {
            throw new IllegalArgumentException("Unsupported entity type " + entityId.getClass());
        }
    }

    private boolean canAccessEntity(IEntityWithMetaprojects entity)
    {
        AuthorizationServiceUtils authorizationUtils = new AuthorizationServiceUtils(getDaoFactory(), findPerson());

        if (entity instanceof MaterialPE)
        {
            return true;
        } else if (entity instanceof ExperimentPE)
        {
            return authorizationUtils.canAccessExperiment((ExperimentPE) entity);
        } else if (entity instanceof SamplePE)
        {
            return authorizationUtils.canAccessSample((SamplePE) entity);
        } else if (entity instanceof DataPE)
        {
            return authorizationUtils.canAccessDataSet((DataPE) entity);
        } else
        {
            throw new IllegalArgumentException("Unsupported entity kind " + entity.getClass());
        }
    }

    private Collection<Long> listEntityIds(EntityKind entityKind)
    {
        return getMetaprojectDAO().listMetaprojectEntityIds(metaproject.getId(), entityKind);
    }

    private void initChangedEntities()
    {
        changedEntitiesIds = new HashMap<Class<? extends IEntityWithMetaprojects>, List<Long>>();
    }

    private void addToChangedEntities(Class<? extends IEntityWithMetaprojects> entityClass, Long entityId)
    {
        List<Long> ids = changedEntitiesIds.get(entityClass);
        if (ids == null)
        {
            ids = new ArrayList<Long>();
            changedEntitiesIds.put(entityClass, ids);
        }
        ids.add(entityId);
    }

    private void addToChangedEntities(Class<? extends IEntityWithMetaprojects> entityClass, Collection<Long> entityIds)
    {
        for (Long entityId : entityIds)
        {
            addToChangedEntities(entityClass, entityId);
        }
    }

}
