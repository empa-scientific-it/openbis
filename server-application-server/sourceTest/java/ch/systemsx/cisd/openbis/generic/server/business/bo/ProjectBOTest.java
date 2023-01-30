/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PERSON;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PROJECT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link ProjectBO} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = DeletedExperimentPE.class)
public final class ProjectBOTest extends AbstractBOTest
{
    private static final TechId EXAMPLE_PROJECT_ID = new TechId(EXAMPLE_PROJECT.getId());

    private final ProjectBO createProjectBO()
    {
        return new ProjectBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, relationshipService,
                managedPropertyEvaluatorFactory, null, new EntityHistoryCreator());
    }

    @Test
    public final void testLoadByPermId()
    {
        final ProjectBO projectBO = createProjectBO();

        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryGetByPermID("20120814110011738-105");
                    will(returnValue(EXAMPLE_PROJECT));
                }
            });

        projectBO.loadByPermId("20120814110011738-105");
        ProjectPE project = projectBO.getProject();

        assertEquals(EXAMPLE_PROJECT, project);
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testLoadByPermIdNonexistent()
    {
        final ProjectBO projectBO = createProjectBO();

        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryGetByPermID("UNKNOWN-PERM-ID");
                    will(returnValue(null));
                }
            });

        projectBO.loadByPermId("UNKNOWN-PERM-ID");
    }

    @Test
    public void testDeleteProjectWithNoExperiments()
    {
        final RecordingMatcher<EventPE> eventRecorder = new RecordingMatcher<EventPE>();
        prepareGetProject();
        prepareGetUndeletedExperiments();
        prepareGetTrashedExperiments();
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).delete(EXAMPLE_PROJECT);

                    one(eventDAO).persist(with(eventRecorder));
                }
            });

        ProjectBO projectBO = createProjectBO();
        projectBO.deleteByTechId(EXAMPLE_PROJECT_ID, "my reason");

        assertEquals(EventType.DELETION, eventRecorder.recordedObject().getEventType());
        assertEquals(EntityType.PROJECT, eventRecorder.recordedObject().getEntityType());
        assertEquals("my reason", eventRecorder.recordedObject().getReason());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteProjectWithOnlyUndeletedExperiments()
    {
        prepareGetProject();
        ExperimentPE e1 = new ExperimentPE();
        e1.setCode("E1");
        ExperimentPE e2 = new ExperimentPE();
        e2.setCode("E2");
        prepareGetUndeletedExperiments(e1, e2);
        prepareGetTrashedExperiments();

        try
        {
            createProjectBO().deleteByTechId(EXAMPLE_PROJECT_ID, "my reason");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Project 'MY_GREAT_PROJECT' can not be deleted because "
                    + "the following experiments still exist: [E1, E2]", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteProjectWithOnlyTrashedExperiments()
    {
        prepareGetProject();
        prepareGetUndeletedExperiments();
        DeletedExperimentPE e3 = new DeletedExperimentPE();
        e3.setCode("E3");
        e3.setProjectInternal(EXAMPLE_PROJECT);
        DeletedExperimentPE e4 = new DeletedExperimentPE();
        e4.setCode("E4");
        e4.setProjectInternal(new ProjectPE());
        prepareGetTrashedExperiments(e3, e4);

        try
        {
            createProjectBO().deleteByTechId(EXAMPLE_PROJECT_ID, "my reason");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Project 'MY_GREAT_PROJECT' can not be deleted because "
                    + "the following experiments are in the trash can: [E3]", ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteProjectWithUndeletedAndTrashedExperiments()
    {
        prepareGetProject();
        ExperimentPE e1 = new ExperimentPE();
        e1.setCode("E1");
        ExperimentPE e2 = new ExperimentPE();
        e2.setCode("E2");
        prepareGetUndeletedExperiments(e1, e2);
        DeletedExperimentPE e3 = new DeletedExperimentPE();
        e3.setCode("E3");
        e3.setProjectInternal(EXAMPLE_PROJECT);
        DeletedExperimentPE e4 = new DeletedExperimentPE();
        e4.setCode("E4");
        e4.setProjectInternal(new ProjectPE());
        prepareGetTrashedExperiments(e3, e4);

        try
        {
            createProjectBO().deleteByTechId(EXAMPLE_PROJECT_ID, "my reason");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Project 'MY_GREAT_PROJECT' can not be deleted because "
                    + "the following experiments still exist: [E1, E2]\n"
                    + "In addition the following experiments are in the trash can: [E3]",
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private void prepareGetProject()
    {
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).getByTechId(EXAMPLE_PROJECT_ID);
                    will(returnValue(EXAMPLE_PROJECT));

                }
            });
    }

    private void prepareGetUndeletedExperiments(final ExperimentPE... experiments)
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).listExperimentsWithProperties(Collections.singletonList(EXAMPLE_PROJECT), false, false);
                    will(returnValue(Arrays.asList(experiments)));
                }
            });
    }

    private void prepareGetTrashedExperiments(final DeletedExperimentPE... experiments)
    {
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).listAllEntities();
                    DeletionPE deletionPE = new DeletionPE();
                    deletionPE.setId(4711L);
                    will(returnValue(Arrays.asList(deletionPE)));

                    one(deletionDAO).findTrashedExperimentIds(
                            Arrays.<TechId> asList(new TechId(deletionPE.getId())));
                    List<TechId> ids = Arrays.asList(new TechId(42));
                    will(returnValue(ids));

                    one(deletionDAO).listDeletedEntities(EntityKind.EXPERIMENT, ids);
                    will(returnValue(Arrays.asList(experiments)));
                }
            });
    }

    @Test
    public final void testSaveWithNullProject()
    {
        boolean exceptionThrown = false;
        try
        {
            createProjectBO().save();
        } catch (final AssertionError ex)
        {
            exceptionThrown = true;
        }
        AssertJUnit.assertTrue("Can not save a null project", exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineWithNullCode()
    {
        final ProjectBO projectBO = createProjectBO();
        boolean fail = true;
        try
        {
            projectBO.define(new NewProject(null, null), null);
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testSave()
    {
        final ProjectBO projectBO = createProjectBO();
        prepareDefineProject();

        context.checking(new Expectations()
            {
                {
                    one(permIdDAO).createPermId();
                }
            });

        projectBO.define(new NewProject(createProjectIdent().toString(), null), null);

        context.checking(new Expectations()
            {
                {
                    one(projectDAO).createProject(projectBO.getProject(), EXAMPLE_PERSON);
                }
            });
        projectBO.save();
        context.assertIsSatisfied();
    }

    private void prepareDefineProject()
    {
        context.checking(new Expectations()
            {
                {
                    ManagerTestTool.prepareFindGroup(this,
                            ManagerTestTool.EXAMPLE_SESSION.tryGetHomeGroupCode(), daoFactory,
                            spaceDAO);
                }
            });
    }

    @Test
    public final void testSaveWithAlreadyExistingProject()
    {
        final ProjectBO projectBO = createProjectBO();
        final ProjectIdentifier projIdent = createProjectIdent();

        prepareDefineProject();

        context.checking(new Expectations()
            {
                {
                    one(permIdDAO).createPermId();
                }
            });

        projectBO.define(new NewProject(projIdent.toString(), null), null);

        context.checking(new Expectations()
            {
                {
                    one(projectDAO).createProject(projectBO.getProject(), EXAMPLE_PERSON);
                    will(throwException(ManagerTestTool.createUniqueViolationException()));
                }
            });

        try
        {
            // System.err.print(projectBO.getProject());
            projectBO.save();
            fail("An UserFailureException must be thrown here.");
        } catch (final UserFailureException ex)
        {
            assertEquals(
                    String.format(DataAccessExceptionTranslator.UNIQUE_VIOLATION_FORMAT,
                            String.format("Project '%s'", projIdent.getProjectCode())),
                    ex.getMessage());
        }
        context.assertIsSatisfied();
    }

    private ProjectIdentifier createProjectIdent()
    {
        final ProjectPE projectDTO = EXAMPLE_PROJECT;
        return new ProjectIdentifier(projectDTO.getSpace().getCode(), projectDTO.getCode());
    }
}
