/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IDeletablePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.DeletionTranslator;

/**
 * Business object implementing {@link IDeletionTable}.
 * 
 * @author Franz-Josef Elmer
 */
public class DeletionTable extends AbstractBusinessObject implements IDeletionTable
{
    private List<Deletion> deletions;

    public DeletionTable(IDAOFactory daoFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    @Override
    public List<Deletion> getDeletions()
    {
        return deletions;
    }

    @Override
    public void load(boolean withEntities)
    {
        List<DeletionPE> deletionPEs = getDeletionDAO().listAllEntities();
        load(deletionPEs, withEntities, false);
    }

    @Override
    public void load(List<Long> deletionIds, boolean withEntities)
    {
        List<DeletionPE> deletionPEs = getDeletionDAO().findAllById(deletionIds);
        load(deletionPEs, withEntities, false);
    }

    @Override
    public void loadOriginal()
    {
        List<DeletionPE> deletionPEs = getDeletionDAO().listAllEntities();
        load(deletionPEs, true, true);
    }

    @Override
    public void loadOriginal(List<Long> deletionIds)
    {
        List<DeletionPE> deletionPEs = getDeletionDAO().findAllById(deletionIds);
        load(deletionPEs, true, true);
    }

    private void load(List<DeletionPE> deletionPEs, boolean withEntities, boolean onlyOriginal)
    {
        Collections.sort(deletionPEs);
        deletions = DeletionTranslator.translate(deletionPEs);
        if (false == withEntities)
        {
            return;
        }
        Map<Long, RootEntitiesFinder> findersMap = new HashMap<Long, RootEntitiesFinder>();
        for (DeletionPE deletionPE : deletionPEs)
        {
            findersMap.put(deletionPE.getId(), new RootEntitiesFinder());
        }

        if (false == deletions.isEmpty())
        {
            findEntities(TrashEntity.EXPERIMENT, findersMap, onlyOriginal);
            findEntities(TrashEntity.SAMPLE, findersMap, onlyOriginal);
            findEntities(TrashEntity.DATA_SET, findersMap, onlyOriginal);
        }
        for (Deletion deletion : deletions)
        {
            findersMap.get(deletion.getId()).addRootEntitiesTo(deletion);
        }
    }

    private void findEntities(TrashEntity kind, Map<Long, RootEntitiesFinder> findersMap,
            boolean onlyOriginal)
    {
        IDeletionDAO deletionDAO = getDeletionDAO();

        for (Deletion deletion : deletions)
        {
            List<TechId> deletedEntitiesIds;

            if (onlyOriginal)
            {
                deletedEntitiesIds =
                        kind.originalDeletedEntityIds(deletionDAO,
                                Collections.singletonList(new TechId(deletion.getId())));
            } else
            {
                deletedEntitiesIds =
                        kind.deletedEntityIds(deletionDAO,
                                Collections.singletonList(new TechId(deletion.getId())));
            }
            EntityKind entityKind = kind.entityKind;

            int count = deletedEntitiesIds.size();

            switch (entityKind)
            {
                case DATA_SET:
                    deletion.setTotalDatasetsCount(count);
                    break;
                case SAMPLE:
                    deletion.setTotalSamplesCount(count);
                    break;
                case EXPERIMENT:
                    deletion.setTotalExperimentsCount(count);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported entity type " + entityKind);
            }

            List<? extends IDeletablePE> entities =
                    deletionDAO.listDeletedEntities(kind.entityKind, deletedEntitiesIds);
            addEntities(entities, findersMap);

        }

    }

    private enum TrashEntity
    {
        SAMPLE(EntityKind.SAMPLE)
        {
            @Override
            List<TechId> deletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs)
            {
                return dao.findTrashedSampleIds(deletionIDs);
            }

            @Override
            List<TechId> originalDeletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs)
            {
                return dao.findOriginalTrashedSampleIds(deletionIDs);
            }

        },
        EXPERIMENT(EntityKind.EXPERIMENT)
        {
            @Override
            List<TechId> deletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs)
            {
                return dao.findTrashedExperimentIds(deletionIDs);
            }

            @Override
            List<TechId> originalDeletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs)
            {
                return dao.findOriginalTrashedExperimentIds(deletionIDs);
            }
        },
        DATA_SET(EntityKind.DATA_SET)
        {
            @Override
            List<TechId> deletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs)
            {
                return dao.findTrashedDataSetIds(deletionIDs);
            }

            @Override
            List<TechId> originalDeletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs)
            {
                return dao.findOriginalTrashedDataSetIds(deletionIDs);
            }
        };

        private TrashEntity(EntityKind entityKind)
        {
            this.entityKind = entityKind;
        }

        final EntityKind entityKind;

        abstract List<TechId> deletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs);

        abstract List<TechId> originalDeletedEntityIds(IDeletionDAO dao, List<TechId> deletionIDs);

    }

    private void addEntities(List<? extends IDeletablePE> entities,
            Map<Long, RootEntitiesFinder> findersMap)
    {
        for (IDeletablePE entity : entities)
        {
            DeletionPE deletion = entity.getDeletion();
            if (deletion != null)
            {
                RootEntitiesFinder finder = findersMap.get(deletion.getId());
                if (finder != null)
                {
                    finder.addEntity(entity);
                }
            }
        }
    }

}
