package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

import ch.systemsx.cisd.common.action.IMapper;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomColumnPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

// TODO 2010-07-14, Piotr Buczek: write tests for nontrivial methods
final public class AuthorizationDataProvider implements IAuthorizationDataProvider
{
    private static final class PermId2StringMapper implements IMapper<List<PermId>, List<?>>
    {
        @Override
        public List<?> map(List<PermId> permIds)
        {
            return PermId.asStrings(permIds);
        }
    }

    private static final class TechId2LongMapper implements IMapper<List<TechId>, List<?>>
    {
        @Override
        public List<?> map(List<TechId> techIds)
        {
            return TechId.asLongs(techIds);
        }
    }

    private static final class GroupExperimentMapper implements IMapper<List<ExperimentAccessPE>, List<ExperimentAccessPE>>
    {
        @Override
        public List<ExperimentAccessPE> map(List<ExperimentAccessPE> list)
        {
            for (ExperimentAccessPE item : list)
            {
                item.setGroup(true);
            }
            return list;
        }
    }

    private static final class GroupSampleMapper implements IMapper<List<SampleAccessPE>, List<SampleAccessPE>>
    {
        @Override
        public List<SampleAccessPE> map(List<SampleAccessPE> list)
        {
            for (SampleAccessPE item : list)
            {
                item.setGroup(true);
            }
            return list;
        }
    }

    private static final class GroupDataSetMapper implements IMapper<List<DataSetAccessPE>, List<DataSetAccessPE>>
    {
        @Override
        public List<DataSetAccessPE> map(List<DataSetAccessPE> list)
        {
            for (DataSetAccessPE item : list)
            {
                item.setGroup(true);
            }
            return list;
        }
    }

    private final IAuthorizationDAOFactory daoFactory;

    public AuthorizationDataProvider(IAuthorizationDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public IAuthorizationDAOFactory getDaoFactory()
    {
        return daoFactory;
    }

    @Override
    public IAuthorizationConfig getAuthorizationConfig()
    {
        return daoFactory.getAuthorizationConfig();
    }

    @Override
    public List<SpacePE> listSpaces()
    {
        return daoFactory.getSpaceDAO().listSpaces();
    }

    @Override
    public SpacePE tryGetSpace(String spaceCode)
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCode(spaceCode);
    }

    @Override
    public ExperimentPE tryGetExperimentByTechId(TechId techId)
    {
        return daoFactory.getExperimentDAO().tryGetByTechId(techId);
    }

    @Override
    public Map<TechId, ExperimentPE> tryGetExperimentsByTechIds(Collection<TechId> techIds)
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByIDs(TechId.asLongs(techIds));

        Map<TechId, ExperimentPE> map = new HashMap<TechId, ExperimentPE>();

        for (ExperimentPE experiment : experiments)
        {
            map.put(new TechId(experiment.getId()), experiment);
        }

        return map;
    }

    @Override
    public ExperimentPE tryGetExperimentByPermId(String permId)
    {
        return daoFactory.getExperimentDAO().tryGetByPermID(permId);
    }

    @Override
    public SamplePE tryGetSampleByPermId(String permId)
    {
        return daoFactory.getSampleDAO().tryToFindByPermID(permId);
    }

    @Override
    public SamplePE tryGetSampleBySpaceAndCode(SpacePE space, String sampleCode)
    {
        return daoFactory.getSampleDAO().tryFindByCodeAndSpace(sampleCode, space);
    }

    @Override
    public SamplePE tryGetSampleByProjectAndCode(ProjectPE project, String sampleCode)
    {
        return daoFactory.getSampleDAO().tryfindByCodeAndProject(sampleCode, project);
    }

    @Override
    public Map<TechId, SamplePE> tryGetSamplesByTechIds(Collection<TechId> techIds)
    {
        List<SamplePE> samples = daoFactory.getSampleDAO().listByIDs(TechId.asLongs(techIds));

        Map<TechId, SamplePE> map = new HashMap<TechId, SamplePE>();

        for (SamplePE sample : samples)
        {
            map.put(new TechId(sample.getId()), sample);
        }

        return map;
    }

    @Override
    public Map<TechId, DataPE> tryGetDataSetsByTechIds(Collection<TechId> techIds)
    {
        List<DataPE> dataSets = daoFactory.getDataDAO().listByIDs(TechId.asLongs(techIds));

        Map<TechId, DataPE> map = new HashMap<TechId, DataPE>();

        for (DataPE dataSet : dataSets)
        {
            map.put(new TechId(dataSet.getId()), dataSet);
        }

        return map;
    }

    @Override
    public ProjectPE tryGetProjectByPermId(PermId permId)
    {
        return daoFactory.getProjectDAO().tryGetByPermID(permId.getId());
    }

    @Override
    public ProjectPE tryGetProjectByTechId(TechId techId)
    {
        return daoFactory.getProjectDAO().tryGetByTechId(techId);
    }

    @Override
    public Map<TechId, ProjectPE> tryGetProjectsByTechIds(Collection<TechId> techIds)
    {
        List<ProjectPE> projects = daoFactory.getProjectDAO().listByIDs(TechId.asLongs(techIds));

        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();

        for (ProjectPE project : projects)
        {
            map.put(new TechId(project.getId()), project);
        }

        return map;
    }

    @Override
    public ProjectPE tryGetProjectByIdentifier(ProjectIdentifier identifier)
    {
        return daoFactory.getProjectDAO().tryFindProject(
                identifier.getSpaceCode(), identifier.getProjectCode());
    }

    @Override
    public ProjectPE tryGetProjectForDataSet(String dataSetCode)
    {
        DataPE dataSet = daoFactory.getDataDAO().tryToFindDataSetByCode(dataSetCode);
        if (dataSet != null && dataSet.getExperiment() != null)
        {
            return dataSet.getExperiment().getProject();
        } else
        {
            return null;
        }
    }

    @Override
    public Set<DataSetAccessPE> getDatasetCollectionAccessDataByTechIds(final List<TechId> dataSetTechIds, boolean grouped)
    {
        return getEntityCollectionAccessData("dataset", DataSetAccessPE.DATASET_ACCESS_BY_TECH_IDS_QUERY_NAME,
                DataSetAccessPE.DATA_SET_IDS_PARAMETER_NAME, dataSetTechIds, new TechId2LongMapper(), grouped ? new GroupDataSetMapper() : null);
    }

    @Override
    public Set<DataSetAccessPE> getDatasetCollectionAccessDataByCodes(final List<String> dataSetCodes)
    {
        return getEntityCollectionAccessData("dataset", DataSetAccessPE.DATASET_ACCESS_BY_CODES_QUERY_NAME,
                DataSetAccessPE.DATA_SET_CODES_PARAMETER_NAME, dataSetCodes, null, new GroupDataSetMapper());
    }

    @Override
    public Set<DataSetAccessPE> getDeletedDatasetCollectionAccessData(final List<TechId> deletionIds)
    {
        return getEntityCollectionAccessData("deletion", DataSetAccessPE.DELETED_DATASET_ACCESS_QUERY_NAME,
                DataSetAccessPE.DELETION_IDS_PARAMETER_NAME, deletionIds, new TechId2LongMapper(), new GroupDataSetMapper());
    }

    @Override
    public Set<SampleAccessPE> getSampleCollectionAccessDataByTechIds(List<TechId> sampleTechIds, boolean grouped)
    {
        return getEntityCollectionAccessData("sample", SampleAccessPE.SAMPLE_ACCESS_BY_TECH_IDS_QUERY_NAME,
                SampleAccessPE.SAMPLE_IDS_PARAMETER_NAME, sampleTechIds, new TechId2LongMapper(), grouped ? new GroupSampleMapper() : null);
    }

    @Override
    public Set<SampleAccessPE> getSampleCollectionAccessDataByPermIds(List<PermId> samplePermIds)
    {
        return getEntityCollectionAccessData("sample", SampleAccessPE.SAMPLE_ACCESS_BY_PERM_IDS_QUERY_NAME,
                SampleAccessPE.SAMPLE_IDS_PARAMETER_NAME, samplePermIds, new PermId2StringMapper(), new GroupSampleMapper());
    }

    @Override
    public Set<SampleAccessPE> getDeletedSampleCollectionAccessData(List<TechId> deletionIds)
    {
        return getEntityCollectionAccessData("deletion", SampleAccessPE.DELETED_SAMPLE_ACCESS_QUERY_NAME,
                SampleAccessPE.DELETION_IDS_PARAMETER_NAME, deletionIds, new TechId2LongMapper(), new GroupSampleMapper());
    }

    @Override
    public Set<ExperimentAccessPE> getExperimentCollectionAccessData(final List<TechId> experimentIds, boolean grouped)
    {
        return getEntityCollectionAccessData("experiment", ExperimentAccessPE.EXPERIMENT_ACCESS_QUERY_NAME,
                ExperimentAccessPE.EXPERIMENT_IDS_PARAMETER_NAME, experimentIds, new TechId2LongMapper(),
                grouped ? new GroupExperimentMapper() : null);
    }

    @Override
    public Set<ExperimentAccessPE> getDeletedExperimentCollectionAccessData(final List<TechId> deletionIds)
    {
        return getEntityCollectionAccessData("deletion", ExperimentAccessPE.DELETED_EXPERIMENT_ACCESS_QUERY_NAME,
                ExperimentAccessPE.DELETION_IDS_PARAMETER_NAME, deletionIds, new TechId2LongMapper(), new GroupExperimentMapper());
    }

    private <V, R> Set<R> getEntityCollectionAccessData(String entityName, String queryName, String parameterName, List<V> values,
            IMapper<List<V>, List<?>> valuesMapperOrNull, IMapper<List<R>, List<R>> resultMapperOrNull)
    {
        Session session = daoFactory.getSessionFactory().getCurrentSession();

        final Set<R> fullResults = new HashSet<R>();

        // WORKAROUND Problem in Hibernate when the number of data set codes > 1000
        // Though this query runs quickly within the pgadmin tool, even for large numbers of
        // data set codes, Hibernate becomes *very* slow when the size of the data set codes
        // exceeds 1000. For that reason, break down the query into smaller sections and
        // reassemble the results.

        BatchOperationExecutor.executeInBatches(new IBatchOperation<V>()
            {
                @Override
                public void execute(List<V> entities)
                {
                    List<?> mappedValues = valuesMapperOrNull != null ? valuesMapperOrNull.map(entities) : entities;
                    assertNoNullElements(mappedValues);
                    Query query = session.getNamedQuery(queryName);
                    query.setReadOnly(true);
                    query.setParameterList(parameterName, mappedValues);

                    List<R> singleResults = cast(query.list());

                    if (resultMapperOrNull != null)
                    {
                        singleResults = resultMapperOrNull.map(singleResults);
                    }

                    fullResults.addAll(singleResults);
                }

                @Override
                public List<V> getAllEntities()
                {
                    return values;
                }

                @Override
                public String getEntityName()
                {
                    return entityName;
                }

                @Override
                public String getOperationName()
                {
                    return "authorization";
                }
            });

        return fullResults;
    }
    
    private void assertNoNullElements(List<?> list)
    {
        for (Object object : list)
        {
            if (object == null)
            {
                throw new NullPointerException();
            }
        }
    }

    @Override
    public SpacePE tryGetSpace(SpaceOwnerKind kind, TechId techId)
    {
        switch (kind)
        {
            case EXPERIMENT:
                ExperimentPE experiment = daoFactory.getExperimentDAO().getByTechId(techId);
                return experiment.getProject().getSpace();
            case SPACE:
                SpacePE space = daoFactory.getSpaceDAO().getByTechId(techId);
                return space;
            case PROJECT:
                ProjectPE project = daoFactory.getProjectDAO().getByTechId(techId);
                return project.getSpace();
        }
        return null;
    }

    @Override
    public Set<SpacePE> getDistinctSpacesByEntityIds(SpaceOwnerKind kind, List<TechId> techIds)
    {
        Set<SpacePE> spaces = new HashSet<SpacePE>();
        List<Long> ids = TechId.asLongs(techIds);
        switch (kind)
        {
            case EXPERIMENT:
                spaces.addAll(daoFactory.getExperimentDAO().listSpacesByExperimentIds(ids));
                break;
            case SPACE:
                List<SpacePE> allSpaces = daoFactory.getSpaceDAO().listSpaces();
                Set<Long> idSet = new HashSet<Long>(ids);
                for (SpacePE space : allSpaces)
                {
                    if (idSet.contains(space.getId()))
                    {
                        spaces.add(space);
                    }
                }
                break;
            case PROJECT:
                List<ProjectPE> projects = daoFactory.getProjectDAO().listProjects();
                Set<Long> idSet2 = new HashSet<Long>(ids);
                for (ProjectPE project : projects)
                {
                    if (idSet2.contains(project.getId()))
                    {
                        spaces.add(project.getSpace());
                    }
                }
                break;
        }
        return spaces;
    }

    @Override
    public SamplePE getSample(TechId techId)
    {
        return daoFactory.getSampleDAO().getByTechId(techId);
    }

    @Override
    public GridCustomFilterPE getGridCustomFilter(TechId techId)
    {
        return daoFactory.getGridCustomFilterDAO().getByTechId(techId);
    }

    @Override
    public GridCustomColumnPE getGridCustomColumn(TechId techId)
    {
        return daoFactory.getGridCustomColumnDAO().getByTechId(techId);
    }

    @Override
    public QueryPE getQuery(TechId techId)
    {
        return daoFactory.getQueryDAO().getByTechId(techId);
    }

    @Override
    public List<DeletionPE> getDeletions(List<TechId> deletionIds)
    {
        return daoFactory.getDeletionDAO().findAllById(TechId.asLongs(deletionIds));
    }

    @Override
    public MetaprojectPE getMetaproject(TechId id)
    {
        return daoFactory.getMetaprojectDAO().getByTechId(id);
    }

    /**
     * Casts given <var>list</var> to specified type.
     * <p>
     * The purpose of this method is to avoid <code>SuppressWarnings("unchecked")</code> in calling methods.
     * </p>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final <T> List<T> cast(final List list)
    {
        return list;
    }
}
