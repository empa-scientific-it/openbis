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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.internal.NamedSequence;
import org.jmock.lib.action.ReturnValueAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.Utils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for {@link TrashBO}.
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses = { ScriptBO.class, ScriptBO.IScriptFactory.class, ScriptPE.class })
public final class TrashBOTest extends AbstractBOTest
{

    private static String EXAMPLE_REASON = "example reason";

    private static Long COMPONENT_CONTAINER_RELATIONSHIP_ID = 1001L;

    private static Long CHILDREN_PARENT_RELATIONSHIP_ID = 1002L;

    private static TechId EXAMPLE_ID = new TechId(1L);

    private static List<TechId> EXAMPLE_ID_LIST = TechId.createList(1, 2, 3);

    private ITrashBO trashBO;

    private ICommonBusinessObjectFactory boFactory;

    private IDataSetTable dataSetTable;

    private int dataSetTableSequenceId;

    private Session session;

    @Override
    @BeforeMethod
    public void beforeMethod()
    {
        super.beforeMethod();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        dataSetTable = context.mock(IDataSetTable.class);
        session = ManagerTestTool.createSession();
        RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setRole(RoleCode.ADMIN);
        session.tryGetPerson().addRoleAssignment(roleAssignment);
        trashBO = new TrashBO(daoFactory, boFactory, session, managedPropertyEvaluatorFactory, null, null);
        context.checking(new Expectations()
            {
                {
                    allowing(boFactory).createDataSetTable(session);
                    will(returnValue(dataSetTable));

                    allowing(relationshipTypeDAO).tryFindRelationshipTypeByCode(BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
                    RelationshipTypePE type = new RelationshipTypePE();
                    type.setId(COMPONENT_CONTAINER_RELATIONSHIP_ID);
                    will(returnValue(type));

                    allowing(relationshipTypeDAO).tryFindRelationshipTypeByCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
                    type = new RelationshipTypePE();
                    type.setId(CHILDREN_PARENT_RELATIONSHIP_ID);
                    will(returnValue(type));

                    allowing(boFactory).createSampleLister(session);
                    will(returnValue(sampleLister));

                    allowing(boFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));
                }
            });
    }

    private DeletionPE createDeletion()
    {
        return createDeletion(EXAMPLE_REASON);
    }

    private DeletionPE createDeletion(final String reason)
    {
        final RecordingMatcher<DeletionPE> deletionMatcher = new RecordingMatcher<DeletionPE>();
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).create(with(deletionMatcher));
                }
            });
        trashBO.createDeletion(reason);
        return deletionMatcher.recordedObject();
    }

    @Test
    public final void testCreateDeletion()
    {
        final String reason = EXAMPLE_REASON;
        DeletionPE deletionPE = createDeletion(reason);
        assertEquals(reason, deletionPE.getReason());
        assertEquals(session.tryGetPerson(), deletionPE.getRegistrator());

        context.assertIsSatisfied();
    }

    @Test
    public final void testRevertDeletion()
    {
        final TechId deletionId = EXAMPLE_ID;
        context.checking(new Expectations()
            {
                {
                    DeletionPE dummyDeletion = new DeletionPE();

                    one(deletionDAO).getByTechId(deletionId);
                    will(returnValue(dummyDeletion));

                    one(deletionDAO).revert(dummyDeletion, session.tryGetPerson());
                }
            });
        trashBO.revertDeletion(deletionId);

        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testTrashSamplesFailsWithNoDeletion()
    {
        trashBO.trashSamples(EXAMPLE_ID_LIST);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testTrashExperimentsFailsWithNoDeletion()
    {
        trashBO.trashExperiments(EXAMPLE_ID_LIST);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testTrashDataSetsFailsWithNoDeletion()
    {
        trashBO.trashDataSets(EXAMPLE_ID_LIST);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashExperimentsAlreadyTrashed()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1\nE2\nE3\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        final List<TechId> experimentIds = EXAMPLE_ID_LIST;
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    one(deletionDAO).trash(EntityKind.EXPERIMENT, experimentIds, deletion, true);
                    will(returnValue(0));
                }
            });
        trashBO.trashExperiments(experimentIds);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashUnavailableDataSets()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, data sets: DS1\n");
        prepareEntityGraph(g);
        g.ds(1).nonDeletable();
        createDeletion();

        try
        {
            trashBO.trashDataSets(asIds(g.ds(1)));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "Deletion not possible because the following data sets are not deletable:\n"
                            + " Status: ARCHIVE_PENDING, data sets: [DS1]",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    private void prepareEntityGraph(EntityGraphGenerator g)
    {
        g.assertConsistency();
        prepareListExperimentsByIds(g);
        prepareListSampleIdsByExperimentIds(g);
        prepareListDataSetIdsByExperimentIds(g);
        prepareListDataSetIdsBySampleIds(g);
        prepareGetByTechId(g);
        prepareFindChildrenOrComponentIds(g);
        prepareListSamples(g);
        prepareListByDataSetIds(g);
        prepareListDataSetContainerIds(g);
        prepareListDataSetComponentIds(g);
        prepareGetDataSetsAndNonDeletableDataSets(g);
        prepareListSampleIdsByContainerIds(g);
    }

    private void prepareListSampleIdsByExperimentIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<List<TechId>> handler = new AbstractMockHandler<List<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getSampleIdsByExperimentIds(argument);
                    print("listSampleIdsByExperimentIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listSampleIdsByExperimentIds(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareListSampleIdsByContainerIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<TechId>> handler = new AbstractMockHandler<Collection<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getSampleIdsByContainerIds(argument);
                    print("listSampleIdsByContainerIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listSampleIdsByContainerIds(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareListDataSetIdsByExperimentIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<List<TechId>> handler = new AbstractMockHandler<List<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getDataSetIdsByExperimentIds(argument);
                    print("listDataSetIdsByExperimentIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).listDataSetIdsByExperimentIds(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareListDataSetIdsBySampleIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<TechId>> handler = new AbstractMockHandler<Collection<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getDataSetIdsBySampleIds(argument);
                    print("listDataSetIdsBySampleIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).listDataSetIdsBySampleIds(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareGetByTechId(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<TechId> handler = new AbstractMockHandler<TechId>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    DataSetNode dataSetNode = g.getDataSets().get(argument.getId());
                    ExternalDataPE dataSet = Utils.createData(dataSetNode);
                    print("getByTechId(" + argument + ") = " + dataSet);
                    return dataSet;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).getByTechId(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareFindChildrenOrComponentIds(final EntityGraphGenerator g)
    {
        class FindChildrenIdsMockHandler
        {
            private Collection<TechId> ids;

            private long relationshipTypeId;

            Matcher<Collection<TechId>> getIdsMatcher()
            {
                return new BaseMatcher<Collection<TechId>>()
                    {
                        @SuppressWarnings("unchecked")
                        @Override
                        public boolean matches(Object arg0)
                        {
                            ids = (Collection<TechId>) arg0;
                            return true;
                        }

                        @Override
                        public void describeTo(Description description)
                        {
                            description.appendValue(ids);
                        }
                    };
            }

            Matcher<Long> getTypeMatcher()
            {
                return new BaseMatcher<Long>()
                    {
                        @Override
                        public boolean matches(Object arg0)
                        {
                            relationshipTypeId = (Long) arg0;
                            return true;
                        }

                        @Override
                        public void describeTo(Description description)
                        {
                            description.appendValue(relationshipTypeId);
                        }
                    };
            }

            Action getReturnAction()
            {
                return new Action()
                    {

                        @Override
                        public Object invoke(Invocation invocation) throws Throwable
                        {
                            String methodName;
                            List<TechId> dataSetIds;
                            if (relationshipTypeId == CHILDREN_PARENT_RELATIONSHIP_ID)
                            {
                                methodName = "getChildrenDataSetIdsByDataSetIds";
                                dataSetIds = g.getChildrenDataSetIdsByDataSetIds(ids);
                            } else if (relationshipTypeId == COMPONENT_CONTAINER_RELATIONSHIP_ID)
                            {
                                methodName = "getComponentDataSetIdsByDataSetIds";
                                dataSetIds = g.getComponentDataSetIdsByDataSetIds(ids);
                            } else
                            {
                                throw new AssertionError("Unknown relationship id: " + relationshipTypeId);
                            }
                            print(methodName + "(" + ids + ") = " + dataSetIds);
                            return new LinkedHashSet<TechId>(dataSetIds);
                        }

                        @Override
                        public void describeTo(Description arg0)
                        {
                        }
                    };
            }
        }
        final FindChildrenIdsMockHandler handler = new FindChildrenIdsMockHandler();
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).findChildrenIds(with(handler.getIdsMatcher()), with(handler.getTypeMatcher()));
                    will(handler.getReturnAction());
                }
            });
    }

    private void prepareListSamples(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<ListOrSearchSampleCriteria> handler = new AbstractMockHandler<ListOrSearchSampleCriteria>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<Sample> samples = new ArrayList<Sample>();
                    Map<Long, SampleNode> sampleNodes = g.getSamples();
                    for (Long id : argument.getSampleIds())
                    {
                        SampleNode sampleNode = sampleNodes.get(id);
                        samples.add(Utils.createSample(sampleNode));
                    }
                    print("list(" + argument + ") = " + samples);
                    return samples;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(sampleLister).list(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareListExperimentsByIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<Long>> handler = new AbstractMockHandler<Collection<Long>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<ExperimentPE> experiments = new ArrayList<ExperimentPE>();
                    Map<Long, ExperimentNode> experimentNodes = g.getExperiments();
                    for (Long id : argument)
                    {
                        ExperimentNode experimentNode = experimentNodes.get(id);
                        experiments.add(Utils.createExperimentPE(experimentNode));
                    }
                    print("listByExperimentIds(" + argument + ") = " + experiments);
                    return experiments;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(experimentDAO).listByIDs(with(handler));
                    will(handler);
                }
            });

    }

    private void prepareListByDataSetIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<Long>> handler = new AbstractMockHandler<Collection<Long>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
                    Map<Long, DataSetNode> dataSetNodes = g.getDataSets();
                    for (Long id : argument)
                    {
                        DataSetNode dataSetNode = dataSetNodes.get(id);
                        AbstractExternalData dataSet = Utils.createExternalData(dataSetNode);
                        dataSet.setDataSetType(new DataSetType());
                        dataSets.add(dataSet);
                    }
                    print("listByDataSetIds(" + argument + ") = " + dataSets);
                    return dataSets;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(datasetLister).listByDatasetIds(with(handler), with(TrashBO.DATA_SET_FETCH_OPTIONS));
                    will(handler);
                }
            });
    }

    private void prepareListDataSetContainerIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<Long>> handler = new AbstractMockHandler<Collection<Long>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    Map<Long, Set<Long>> idsMap = g.getContainerDataSetIdsMap(
                            TechId.createList(new ArrayList<Long>(argument)));
                    print("listContainerIds(" + argument + ") = " + idsMap);
                    return idsMap;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(datasetLister).listContainerIds(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareListDataSetComponentIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<Long>> handler = new AbstractMockHandler<Collection<Long>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    Map<Long, Set<Long>> idsMap = g.getComponentDataSetIdsMap(
                            TechId.createList(new ArrayList<Long>(argument)));
                    print("listComponentIds(" + argument + ") = " + idsMap);
                    return idsMap;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(datasetLister).listComponetIds(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareGetDataSetsAndNonDeletableDataSets(final EntityGraphGenerator g)
    {
        class DataSetTableMockHandler extends BaseMatcher<List<TechId>>
        {
            private List<DataPE> dataSets = new ArrayList<DataPE>();

            private List<ExternalDataPE> nonDeletableDataSets = new ArrayList<ExternalDataPE>();

            @Override
            public void describeTo(Description description)
            {
                description.appendText("<" + dataSets + ">");
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean matches(Object obj)
            {
                print("DataSetTable.loadByIds(" + obj + ")");
                dataSets.clear();
                nonDeletableDataSets.clear();
                Map<Long, DataSetNode> dataSetNodes = g.getDataSets();
                for (TechId id : (List<TechId>) obj)
                {
                    DataSetNode dataSetNode = dataSetNodes.get(id.getId());
                    if (dataSetNode != null)
                    {
                        ExternalDataPE data = Utils.createData(dataSetNode);
                        dataSets.add(data);
                        if (dataSetNode.isDeletable() == false)
                        {
                            nonDeletableDataSets.add(data);
                        }
                    }
                }
                return true;
            }

        }

        final DataSetTableMockHandler handler = new DataSetTableMockHandler();
        context.checking(new Expectations()
            {
                {
                    NamedSequence dataSetTableSequence = new NamedSequence("DATA SET TABLE " + dataSetTableSequenceId++);
                    allowing(dataSetTable).loadByIds(with(handler));
                    inSequence(dataSetTableSequence);

                    allowing(dataSetTable).getNonDeletableExternalDataSets();
                    will(new ReturnValueAction(handler.nonDeletableDataSets));
                    inSequence(dataSetTableSequence);

                    allowing(dataSetTable).getDataSets();
                    will(new ReturnValueAction(handler.dataSets));
                    inSequence(dataSetTableSequence);
                }
            });
    }

    private static abstract class AbstractMockHandler<T> extends BaseMatcher<T> implements Action
    {
        protected T argument;

        @Override
        public void describeTo(Description description)
        {
            description.appendText("<" + argument + ">");
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object obj)
        {
            argument = (T) obj;
            return true;
        }
    }

    private List<TechId> asIds(IIdHolder... entities)
    {
        return TechId.createList(Arrays.asList(entities));
    }

    private static final void print(Object message)
    {
        String methodName = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace)
        {
            if (stackTraceElement.getClassName().equals(TrashBOTest.class.getName()))
            {
                methodName = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
            }
        }
        System.out.println(methodName + ": " + message);
    }

}