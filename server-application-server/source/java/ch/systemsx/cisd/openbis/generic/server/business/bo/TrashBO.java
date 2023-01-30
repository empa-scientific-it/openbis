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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.DefaultAccessController;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.PersistentEntityPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePEPredicate;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author Piotr Buczek
 * @author Franz-Josef Elmer
 */
public class TrashBO extends AbstractBusinessObject implements ITrashBO
{
    static final EnumSet<DataSetFetchOption> DATA_SET_FETCH_OPTIONS = EnumSet.of(DataSetFetchOption.EXPERIMENT, DataSetFetchOption.SAMPLE);

    private enum CascadeSampleDependentComponents
    {
        TRUE, FALSE
    }

    private final ICommonBusinessObjectFactory boFactory;

    private DeletionPE deletion;

    public TrashBO(IDAOFactory daoFactory, ICommonBusinessObjectFactory boFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        this.boFactory = boFactory;
    }

    @Override
    public DeletionPE getDeletion()
    {
        return deletion;
    }

    @Override
    public void createDeletion(String reason)
    {
        try
        {
            deletion = new DeletionPE();
            deletion.setReason(reason);
            deletion.setRegistrator(session.tryGetPerson());
            getDeletionDAO().create(deletion);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Deletion");
        }
    }

    @Override
    public void trashDataSets(List<TechId> dataSetIds)
    {
        assert deletion != null;

        IDatasetLister datasetLister = boFactory.createDatasetLister(session);
        final Set<TechId> experimentIds = new HashSet<TechId>();
        final Set<TechId> sampleIds = new HashSet<TechId>();
        for (AbstractExternalData dataSet : listDataSets(datasetLister, dataSetIds))
        {
            DataSetType dataSetType = dataSet.getDataSetType();
            if (dataSetType.isDeletionDisallow())
            {
                throw new UserFailureException("Data set " + dataSet.getCode()
                        + " can not be deleted because it isn't allowed by its data set type "
                        + dataSetType.getCode() + ".");
            }
            Sample sample = dataSet.getSample();
            if (sample != null)
            {
                sampleIds.add(new TechId(sample));
            }
            if (dataSet.getExperiment() != null)
            {
                experimentIds.add(new TechId(dataSet.getExperiment()));
            }
        }
        assertDataSetDeletionBusinessRules(experimentIds, sampleIds, dataSetIds);
        TrashOperationsManager trashManager = new TrashOperationsManager(session, deletion, this, getTransactionTimeStamp());
        trashDataSets(trashManager, dataSetIds, true, new IDataSetFilter()
            {
                @Override
                public List<TechId> filter(List<AbstractExternalData> dataSets)
                {
                    List<TechId> filtered = new ArrayList<TechId>();
                    for (AbstractExternalData dataSet : dataSets)
                    {
                        Sample sample = dataSet.getSample();
                        Experiment experiment = dataSet.getExperiment();
                        if (contains(sampleIds, sample) || contains(experimentIds, experiment))
                        {
                            filtered.add(new TechId(dataSet));
                        }
                    }
                    return filtered;
                }

                private boolean contains(Set<TechId> ids, IIdAndCodeHolder entity)
                {
                    return entity != null && ids.contains(new TechId(HibernateUtils.getId(entity)));
                }
            });
        trashManager.trash();
    }

    @Override
    public void trashExperiments(List<TechId> experimentIds)
    {
        assert deletion != null;
        TrashOperationsManager trashManager = new TrashOperationsManager(session, deletion, this, getTransactionTimeStamp());
        trashManager.addTrashOperation(EntityKind.EXPERIMENT, experimentIds, true);
        Set<TechId> eIds = new LinkedHashSet<TechId>(experimentIds);
        Set<TechId> dependentSampleIds = trashExperimentDependentSamples(trashManager, eIds);
        trashExperimentDependentDataSets(trashManager, eIds, dependentSampleIds);
        trashManager.trash();
    }

    @Override
    public void trashSamples(final List<TechId> sampleIds)
    {
        assert deletion != null;

        TrashOperationsManager trashManager = new TrashOperationsManager(session, deletion, this, getTransactionTimeStamp());
        Set<TechId> allSampleIds = trashSamples(trashManager, sampleIds, CascadeSampleDependentComponents.TRUE, true);
        Set<TechId> experimentsOfSamples = getExperimentsOfSamples(sampleIds);
        trashSampleDependentDataSets(trashManager, experimentsOfSamples, allSampleIds);
        trashManager.trash();
    }

    private Set<TechId> getExperimentsOfSamples(List<TechId> sampleIds)
    {
        Set<TechId> experimentIds = new HashSet<TechId>();
        ISampleLister sampleLister = boFactory.createSampleLister(session);
        List<Sample> samples = sampleLister.list(new ListOrSearchSampleCriteria(TechId.asLongs(sampleIds)));
        for (Sample sample : samples)
        {
            Experiment experiment = sample.getExperiment();
            if (experiment != null)
            {
                experimentIds.add(new TechId(experiment));
            }
        }
        return experimentIds;
    }

    private Set<TechId> trashSamples(TrashOperationsManager trashManager,
            final List<TechId> sampleIds, final CascadeSampleDependentComponents cascadeType, boolean isOriginalDeletion)
    {
        assert deletion != null;
        Set<TechId> allSampleIds = new LinkedHashSet<TechId>(sampleIds);

        trashManager.addTrashOperation(EntityKind.SAMPLE, sampleIds, isOriginalDeletion);

        if (cascadeType == CascadeSampleDependentComponents.TRUE)
        {
            allSampleIds.addAll(trashSampleDependentComponents(trashManager, sampleIds));
        }
        return allSampleIds;
    }

    private static interface IDataSetFilter
    {
        public List<TechId> filter(List<AbstractExternalData> dataSets);
    }

    private static interface IIdHolderProvider
    {
        public IIdHolder getIdHolder(AbstractExternalData dataSet);
    }

    private static final class DataSetFilter implements IDataSetFilter
    {
        private Set<Long> ids;

        private IIdHolderProvider idHolderProvider;

        DataSetFilter(Collection<TechId> ids, IIdHolderProvider idHolderProvider)
        {
            this.ids = new LinkedHashSet<Long>(TechId.asLongs(ids));
            this.idHolderProvider = idHolderProvider;

        }

        @Override
        public List<TechId> filter(List<AbstractExternalData> dataSets)
        {
            List<TechId> deletableDataSets = new ArrayList<TechId>();
            for (AbstractExternalData dataSet : dataSets)
            {
                IIdHolder entity = idHolderProvider.getIdHolder(dataSet);
                if (entity != null && ids.contains(entity.getId()))
                {
                    deletableDataSets.add(new TechId(dataSet.getId()));
                }
            }
            return deletableDataSets;
        }
    }

    private void trashDataSets(TrashOperationsManager trashManager, final List<TechId> dataSetIds,
            boolean isOriginalDeletion, IDataSetFilter filterOrNull)
    {
        assert deletion != null;

        if (dataSetIds.isEmpty())
        {
            return;
        }
        IDatasetLister datasetLister = boFactory.createDatasetLister(session);
        List<TechId> allDeletables = DataSetUtils.getAllDeletableComponentsRecursively(dataSetIds,
                isOriginalDeletion, datasetLister, this);
        List<AbstractExternalData> dataSets = listDataSets(datasetLister, allDeletables);
        checkForNonDeletableDataSets(dataSets);
        if (filterOrNull != null)
        {
            allDeletables = filterOrNull.filter(dataSets);
        }
        List<TechId> deletableOriginals = new ArrayList<TechId>(dataSetIds);
        deletableOriginals.retainAll(allDeletables);
        if (isOriginalDeletion)
        {
            allDeletables.removeAll(deletableOriginals);
            trashManager.addTrashOperation(EntityKind.DATA_SET, deletableOriginals, true);
        }
        trashManager.addTrashOperation(EntityKind.DATA_SET, allDeletables, false);
    }

    private void checkForNonDeletableDataSets(List<AbstractExternalData> dataSets)
    {
        Map<DataSetArchivingStatus, List<String>> statusToCodesMap =
                new TreeMap<DataSetArchivingStatus, List<String>>();
        for (AbstractExternalData dataSet : dataSets)
        {
            if (dataSet instanceof PhysicalDataSet)
            {
                PhysicalDataSet physicalDataSet = (PhysicalDataSet) dataSet;
                DataSetArchivingStatus archivingStatus = physicalDataSet.getStatus();
                if (archivingStatus.isDeletable() == false)
                {
                    List<String> codes = statusToCodesMap.get(archivingStatus);
                    if (codes == null)
                    {
                        codes = new ArrayList<String>();
                        statusToCodesMap.put(archivingStatus, codes);
                    }
                    codes.add(dataSet.getCode());

                }
            }
        }

        StringBuilder builder = new StringBuilder();
        Set<Entry<DataSetArchivingStatus, List<String>>> entrySet = statusToCodesMap.entrySet();
        for (Entry<DataSetArchivingStatus, List<String>> entry : entrySet)
        {
            builder.append("\n Status: ").append(entry.getKey()).append(", data sets: ");
            builder.append(entry.getValue());
        }
        if (builder.length() > 0)
        {
            throw new UserFailureException(
                    "Deletion not possible because the following data sets are not deletable:"
                            + builder);
        }
    }

    private Set<TechId> trashSampleDependentComponents(TrashOperationsManager trashManager,
            List<TechId> sampleIds)
    {
        final ISampleDAO sampleDAO = getSampleDAO();

        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.SAMPLE, sampleIds,
                        "listSampleIdsByContainerIds")
                    {
                        @Override
                        public Collection<TechId> listAction(List<TechId> entities)
                        {
                            return sampleDAO.listSampleIdsByContainerIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        // We have a business rule that there is just 1 level of components and using this here
        // improves performance.
        return trashSamples(trashManager, batchOperation.getResults(), CascadeSampleDependentComponents.FALSE, false);
    }

    private void trashSampleDependentDataSets(TrashOperationsManager trashManager,
            Set<TechId> experimentsOfSamples, Set<TechId> sampleIds)
    {
        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.DATA_SET, new ArrayList<TechId>(sampleIds),
                        "listDataSetIdsBySampleIds")
                    {
                        @Override
                        public List<TechId> listAction(List<TechId> entities)
                        {
                            List<TechId> ids = getDataDAO().listDataSetIdsBySampleIds(entities);
                            return ids;
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        List<TechId> dataSetIds = batchOperation.getResults();
        assertDataSetDeletionBusinessRules(experimentsOfSamples, sampleIds, dataSetIds);
        trashDataSets(trashManager, dataSetIds, false, new DataSetFilter(sampleIds,
                new IIdHolderProvider()
                    {
                        @Override
                        public IIdHolder getIdHolder(AbstractExternalData dataSet)
                        {
                            return dataSet.getSample();
                        }
                    }));
    }

    private Set<TechId> trashExperimentDependentSamples(TrashOperationsManager trashManager, Set<TechId> experimentIds)
    {
        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.SAMPLE, new ArrayList<TechId>(experimentIds),
                        "listSampleIdsByExperimentIds")
                    {
                        @Override
                        public List<TechId> listAction(List<TechId> entities)
                        {
                            return getSampleDAO().listSampleIdsByExperimentIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        List<TechId> sampleIds = batchOperation.getResults();
        return trashSamples(trashManager, sampleIds, CascadeSampleDependentComponents.TRUE, false);
    }

    private void trashExperimentDependentDataSets(TrashOperationsManager trashManager, Set<TechId> experimentIds,
            Set<TechId> dependentSampleIds)
    {
        AbstractQueryBatchOperation batchOperation =
                new AbstractQueryBatchOperation(EntityKind.DATA_SET, new ArrayList<TechId>(experimentIds),
                        "listDataSetIdsByExperimentIds")
                    {
                        @Override
                        public List<TechId> listAction(List<TechId> entities)
                        {
                            return getDataDAO().listDataSetIdsByExperimentIds(entities);
                        }
                    };
        BatchOperationExecutor.executeInBatches(batchOperation);
        List<TechId> dataSetIds = batchOperation.getResults();
        assertDataSetDeletionBusinessRules(experimentIds, dependentSampleIds, dataSetIds);
        trashDataSets(trashManager, dataSetIds, false, new DataSetFilter(experimentIds,
                new IIdHolderProvider()
                    {
                        @Override
                        public IIdHolder getIdHolder(AbstractExternalData dataSet)
                        {
                            return dataSet.getExperiment();
                        }
                    }));
    }

    private void assertDataSetDeletionBusinessRules(Set<TechId> experimentIds, Set<TechId> sampleIdes,
            List<TechId> dataSetIds)
    {
        IDatasetLister datasetLister = boFactory.createDatasetLister(session);
        Map<Long, Set<Long>> containerIds = datasetLister.listContainerIds(TechId.asLongs(dataSetIds));
        if (containerIds.isEmpty())
        {
            return;
        }
        Set<Long> allRelatedDataSets = new LinkedHashSet<Long>();
        addIds(allRelatedDataSets, containerIds);
        Set<Long> eIds = new LinkedHashSet<Long>(TechId.asLongs(experimentIds));
        Set<Long> sIds = new LinkedHashSet<Long>(TechId.asLongs(sampleIdes));
        List<Long> ids = new ArrayList<Long>(allRelatedDataSets);
        List<AbstractExternalData> relatedDataSets = listDataSets(datasetLister, TechId.createList(ids));
        StringBuilder builder = new StringBuilder();
        int numberOfForeignDataSets = 0;
        for (AbstractExternalData relatedDataSet : relatedDataSets)
        {
            if (numberOfForeignDataSets >= 10)
            {
                break;
            }
            Sample sample = relatedDataSet.getSample();
            Experiment experiment = relatedDataSet.getExperiment();
            if (sample != null)
            {
                if (sIds.contains(sample.getId()) == false)
                {
                    HibernateUtils.initialize(sample);
                    addTo(builder, "sample " + sample.getIdentifier(), relatedDataSet, containerIds);
                    numberOfForeignDataSets++;
                }
            } else if (experiment != null)
            {
                if (eIds.contains(experiment.getId()) == false)
                {
                    HibernateUtils.initialize(experiment);
                    addTo(builder, "experiment " + experiment.getIdentifier(), relatedDataSet, containerIds);
                    numberOfForeignDataSets++;
                }
            }
        }
        if (numberOfForeignDataSets > 0)
        {
            throw new UserFailureException(builder.toString().trim());
        }
    }

    private void addTo(StringBuilder builder, String entityDescription, AbstractExternalData dataSet,
            Map<Long, Set<Long>> containerIds)
    {
        String findOriginalDataSet = findOriginalDataSet(containerIds, dataSet);
        builder.append("The data set " + findOriginalDataSet + " is a component of the data set "
                + dataSet.getCode() + " which belongs to the " + entityDescription + " which is outside the deletion set.\n");
    }

    private String findOriginalDataSet(Map<Long, Set<Long>> relations, AbstractExternalData dataSet)
    {
        Set<Entry<Long, Set<Long>>> entrySet = relations.entrySet();
        for (Entry<Long, Set<Long>> entry : entrySet)
        {
            if (entry.getValue().contains(dataSet.getId()))
            {
                return getDataDAO().getByTechId(new TechId(entry.getKey())).getCode();
            }
        }
        return null;
    }

    private void addIds(Set<Long> ids, Map<Long, Set<Long>> mappedIds)
    {
        for (Set<Long> set : mappedIds.values())
        {
            ids.addAll(set);
        }
    }

    private List<AbstractExternalData> listDataSets(IDatasetLister datasetLister, List<TechId> dataSetIds)
    {
        return datasetLister.listByDatasetIds(TechId.asLongs(dataSetIds), DATA_SET_FETCH_OPTIONS);
    }

    @Override
    public void revertDeletion(TechId deletionId)
    {
        try
        {
            deletion = getDeletionDAO().getByTechId(deletionId);
            getDeletionDAO().revert(deletion, session.tryGetPerson());
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Deletion");
        }
    }

    private static final class TrashOperationsManager
    {
        private final Session session;

        private final DeletionPE deletion;

        private final IDAOFactory daoFactory;

        private final Map<EntityKind, Set<TechId>> entityIdsByKind = new HashMap<EntityKind, Set<TechId>>();

        private final List<TrashBatchOperation> operations = new ArrayList<TrashBatchOperation>();

        private final PersonPE person;

        private final List<RoleWithIdentifier> allowedRoles;

        private final ExperimentPEPredicate experimentPredicate;

        private final SamplePEPredicate samplesPredicate;

        private final DataPEPredicate dataSetPredicate;

        private final Date modificationTimestamp;

        TrashOperationsManager(Session session, DeletionPE deletion, IDAOFactory daoFactory, Date modificationTimestamp)
        {
            this.session = session;
            this.deletion = deletion;
            this.daoFactory = daoFactory;
            this.modificationTimestamp = modificationTimestamp;
            EntityKind[] values = EntityKind.values();
            for (EntityKind entityKind : values)
            {
                entityIdsByKind.put(entityKind, new HashSet<TechId>());
            }
            person = session.tryGetPerson();
            allowedRoles = DefaultAccessController.getUserRoles(person);
            experimentPredicate = new ExperimentPEPredicate();
            experimentPredicate.init(new AuthorizationDataProvider(daoFactory));
            samplesPredicate = new SamplePEPredicate();
            samplesPredicate.init(new AuthorizationDataProvider(daoFactory));
            dataSetPredicate = new DataPEPredicate();
            dataSetPredicate.init(new AuthorizationDataProvider(daoFactory));
        }

        void addTrashOperation(EntityKind entityKind, List<TechId> entityIds, boolean isOriginalDeletion)
        {
            if (entityIds.isEmpty() == false)
            {
                List<Long> ids = TechId.asLongs(entityIds);
                if (entityKind == EntityKind.EXPERIMENT)
                {
                    List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByIDs(ids);
                    assertAuthorization(experimentPredicate, experiments, "experiments");
                } else if (entityKind == EntityKind.SAMPLE)
                {
                    List<SamplePE> samples = daoFactory.getSampleDAO().listByIDs(ids);
                    assertAuthorization(samplesPredicate, samples, "samples");
                } else if (entityKind == EntityKind.DATA_SET)
                {
                    List<DataPE> dataSets = daoFactory.getDataDAO().listByIDs(ids);
                    assertAuthorization(dataSetPredicate, dataSets, "data sets");
                }
                entityIdsByKind.get(entityKind).addAll(entityIds);
                IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
                operations.add(0, new TrashBatchOperation(entityKind, entityIds, deletion, deletionDAO, isOriginalDeletion));
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void assertAuthorization(PersistentEntityPredicate entityPredicate,
                List<? extends IIdentifierHolder> entities, String entityKindName)
        {
            List<String> identifiers = new ArrayList<String>();
            String errorMessage = "";
            for (IIdentifierHolder identifierHolder : entities)
            {
                Status status = entityPredicate.evaluate(person, allowedRoles, identifierHolder);
                if (status.isOK() == false)
                {
                    identifiers.add(identifierHolder.getIdentifier());
                    errorMessage = status.tryGetErrorMessage();
                }
            }
            if (identifiers.isEmpty() == false)
            {
                throw new AuthorizationFailureException("Can not delete " + entityKindName + " "
                        + CollectionUtils.abbreviate(identifiers, 10) + " because user "
                        + errorMessage);
            }
        }

        void trash()
        {
            updateModificationDateAndModifierOfRelatedProjectsOfExperiments();
            updateModificationDateAndModifierOfRelatedEntitiesOfSamples();
            updateModificationDateAndModifierOfRelatedEntitiesOfDataSets();
            for (TrashBatchOperation operation : operations)
            {
                BatchOperationExecutor.executeInBatches(operation);
            }
        }

        private void updateModificationDateAndModifierOfRelatedProjectsOfExperiments()
        {
            List<Long> ids = TechId.asLongs(entityIdsByKind.get(EntityKind.EXPERIMENT));
            if (ids.isEmpty() == false)
            {
                List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByIDs(ids);
                RelationshipUtils.updateModificationDateAndModifierOfRelatedProjectsOfExperiments(
                        experiments, session, modificationTimestamp);
            }
        }

        private void updateModificationDateAndModifierOfRelatedEntitiesOfSamples()
        {
            List<Long> ids = TechId.asLongs(entityIdsByKind.get(EntityKind.SAMPLE));
            if (ids.isEmpty() == false)
            {
                List<SamplePE> samples = daoFactory.getSampleDAO().listByIDs(ids);
                RelationshipUtils.updateModificationDateAndModifierOfRelatedEntitiesOfSamples(
                        samples, session, modificationTimestamp);
            }
        }

        private void updateModificationDateAndModifierOfRelatedEntitiesOfDataSets()
        {
            List<Long> ids = TechId.asLongs(entityIdsByKind.get(EntityKind.DATA_SET));
            if (ids.isEmpty() == false)
            {
                List<DataPE> dataSets = daoFactory.getDataDAO().listByIDs(ids);
                RelationshipUtils.updateModificationDateAndModifierOfRelatedEntitiesOfDataSets(
                        dataSets, session, modificationTimestamp);
            }
        }

    }

    private static class TrashBatchOperation implements IBatchOperation<TechId>
    {
        private final EntityKind entityKind;

        private final List<TechId> entityIds;

        private final DeletionPE deletion;

        private final IDeletionDAO deletionDAO;

        private final boolean isOriginalDeletion;

        public TrashBatchOperation(EntityKind entityKind, List<TechId> entityIds,
                DeletionPE deletion, IDeletionDAO deletionDAO, boolean isOriginalDeletion)
        {
            this.entityKind = entityKind;
            this.entityIds = entityIds;
            this.deletion = deletion;
            this.deletionDAO = deletionDAO;
            this.isOriginalDeletion = isOriginalDeletion;
        }

        @Override
        public void execute(List<TechId> entities)
        {
            deletionDAO.trash(entityKind, entities, deletion, isOriginalDeletion);
        }

        @Override
        public List<TechId> getAllEntities()
        {
            return entityIds;
        }

        @Override
        public String getEntityName()
        {
            return entityKind.getLabel();
        }

        @Override
        public String getOperationName()
        {
            return "trash";
        }

        @Override
        public String toString()
        {
            return getOperationName() + (isOriginalDeletion ? " original " : " dependent ") + entityKind
                    + " " + getEntityName() + " " + entityIds;
        }

    }

    private abstract static class AbstractQueryBatchOperation implements IBatchOperation<TechId>
    {
        private final EntityKind entityKind;

        private final List<TechId> entityIds;

        private final String operationName;

        private final Set<TechId> results = new LinkedHashSet<TechId>();

        public AbstractQueryBatchOperation(EntityKind entityKind, List<TechId> entityIds,
                String operationName)
        {
            this.entityKind = entityKind;
            this.entityIds = entityIds;
            this.operationName = operationName;
        }

        public abstract Collection<TechId> listAction(List<TechId> entities);

        @Override
        public void execute(List<TechId> entities)
        {
            results.addAll(listAction(entities));
        }

        @Override
        public List<TechId> getAllEntities()
        {
            return entityIds;
        }

        @Override
        public String getEntityName()
        {
            return entityKind.getLabel();
        }

        @Override
        public String getOperationName()
        {
            return operationName;
        }

        public List<TechId> getResults()
        {
            return new ArrayList<TechId>(results);
        }
    }

}
