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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GridCustomFilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = RoleWithIdentifier.class)
public class AuthorizationTestCase extends AssertJUnit
{

    protected static final PersonPE PERSON_PE = new PersonPE();

    protected static final String SPACE_CODE = "G1";

    protected static final String SPACE_PROJECT_CODE = "P1";

    protected static final String SPACE_ANOTHER_PROJECT_CODE = "P11";

    protected static final String SPACE_PROJECT_EXPERIMENT_CODE = "E1";

    protected static final String SPACE_ANOTHER_PROJECT_EXPERIMENT_CODE = "E11";

    protected static final String ANOTHER_SPACE_CODE = "G2";

    protected static final String ANOTHER_SPACE_PROJECT_CODE = "P2";

    protected static final String ANOTHER_SPACE_ANOTHER_PROJECT_CODE = "P22";

    protected static final String ANOTHER_SPACE_PROJECT_EXPERIMENT_CODE = "E2";

    protected static final String ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_CODE = "E22";

    protected static final String NON_EXISTENT_SPACE_CODE = "G3";

    protected static final String NON_EXISTENT_SPACE_PROJECT_CODE = "P3";

    protected static final String NON_EXISTENT_SPACE_PROJECT_EXPERIMENT_CODE = "E3";

    protected static final SpacePE SPACE_PE = new SpacePE();

    protected static final ProjectPE SPACE_PROJECT_PE = new ProjectPE();

    protected static final ProjectPE SPACE_ANOTHER_PROJECT_PE = new ProjectPE();

    protected static final ExperimentPE SPACE_PROJECT_EXPERIMENT_PE = new ExperimentPE();

    protected static final ExperimentPE SPACE_ANOTHER_PROJECT_EXPERIMENT_PE = new ExperimentPE();

    protected static final SpacePE ANOTHER_SPACE_PE = new SpacePE();

    protected static final ProjectPE ANOTHER_SPACE_PROJECT_PE = new ProjectPE();

    protected static final ProjectPE ANOTHER_SPACE_ANOTHER_PROJECT_PE = new ProjectPE();

    protected static final ExperimentPE ANOTHER_SPACE_PROJECT_EXPERIMENT_PE = new ExperimentPE();

    protected static final ExperimentPE ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_PE = new ExperimentPE();

    protected static final SpacePE NON_EXISTENT_SPACE_PE = new SpacePE();

    protected static final ProjectPE NON_EXISTENT_SPACE_PROJECT_PE = new ProjectPE();

    protected static final ExperimentPE NON_EXISTENT_SPACE_PROJECT_EXPERIMENT_PE = new ExperimentPE();

    protected static final List<SpacePE> ALL_SPACES_PE = Arrays.asList(SPACE_PE, ANOTHER_SPACE_PE);

    static
    {
        PERSON_PE.setUserId("test");

        SPACE_PE.setId(1000L);
        SPACE_PE.setCode(SPACE_CODE);

        SPACE_PROJECT_PE.setCode(SPACE_PROJECT_CODE);
        SPACE_PROJECT_PE.setSpace(SPACE_PE);

        SPACE_ANOTHER_PROJECT_PE.setId(1200L);
        SPACE_ANOTHER_PROJECT_PE.setCode(SPACE_ANOTHER_PROJECT_CODE);
        SPACE_ANOTHER_PROJECT_PE.setSpace(SPACE_PE);

        SPACE_PROJECT_EXPERIMENT_PE.setId(1110L);
        SPACE_PROJECT_EXPERIMENT_PE.setCode(SPACE_PROJECT_EXPERIMENT_CODE);
        SPACE_PROJECT_EXPERIMENT_PE.setProject(SPACE_PROJECT_PE);

        SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.setId(1210L);
        SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.setCode(SPACE_ANOTHER_PROJECT_EXPERIMENT_CODE);
        SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.setProject(SPACE_ANOTHER_PROJECT_PE);

        ANOTHER_SPACE_PE.setId(2000L);
        ANOTHER_SPACE_PE.setCode(ANOTHER_SPACE_CODE);

        ANOTHER_SPACE_PROJECT_PE.setId(2100L);
        ANOTHER_SPACE_PROJECT_PE.setCode(ANOTHER_SPACE_PROJECT_CODE);
        ANOTHER_SPACE_PROJECT_PE.setSpace(ANOTHER_SPACE_PE);

        ANOTHER_SPACE_ANOTHER_PROJECT_PE.setId(2200L);
        ANOTHER_SPACE_ANOTHER_PROJECT_PE.setCode(ANOTHER_SPACE_ANOTHER_PROJECT_CODE);
        ANOTHER_SPACE_ANOTHER_PROJECT_PE.setSpace(ANOTHER_SPACE_PE);

        ANOTHER_SPACE_PROJECT_EXPERIMENT_PE.setId(2110L);
        ANOTHER_SPACE_PROJECT_EXPERIMENT_PE.setCode(ANOTHER_SPACE_PROJECT_EXPERIMENT_CODE);
        ANOTHER_SPACE_PROJECT_EXPERIMENT_PE.setProject(ANOTHER_SPACE_PROJECT_PE);

        ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.setId(2210L);
        ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.setCode(ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_CODE);
        ANOTHER_SPACE_ANOTHER_PROJECT_EXPERIMENT_PE.setProject(ANOTHER_SPACE_ANOTHER_PROJECT_PE);

        NON_EXISTENT_SPACE_PE.setId(3000L);
        NON_EXISTENT_SPACE_PE.setCode(NON_EXISTENT_SPACE_CODE);

        NON_EXISTENT_SPACE_PROJECT_PE.setId(3100L);
        NON_EXISTENT_SPACE_PROJECT_PE.setCode(NON_EXISTENT_SPACE_PROJECT_CODE);
        NON_EXISTENT_SPACE_PROJECT_PE.setSpace(NON_EXISTENT_SPACE_PE);

        NON_EXISTENT_SPACE_PROJECT_EXPERIMENT_PE.setId(3110L);
        NON_EXISTENT_SPACE_PROJECT_EXPERIMENT_PE.setCode(NON_EXISTENT_SPACE_PROJECT_EXPERIMENT_CODE);
        NON_EXISTENT_SPACE_PROJECT_EXPERIMENT_PE.setProject(NON_EXISTENT_SPACE_PROJECT_PE);
    }

    protected Mockery context;

    protected IAuthorizationDataProvider provider;

    protected RoleWithIdentifier createProjectRole(RoleCode roleCode, String spaceCode, String projectCode)
    {
        SpacePE spacePE = new SpacePE();
        spacePE.setCode(spaceCode);

        ProjectPE projectPE = new ProjectPE();
        projectPE.setCode(projectCode);
        projectPE.setSpace(spacePE);

        return createProjectRole(roleCode, projectPE);
    }

    protected RoleWithIdentifier createProjectRole(RoleCode roleCode, ProjectPE project)
    {
        return new RoleWithIdentifier(RoleLevel.PROJECT, roleCode, null, project);
    }

    /**
     * Creates a role with level {@link RoleLevel#SPACE} with specified role code for specified space.
     */
    protected RoleWithIdentifier createSpaceRole(RoleCode roleCode, SpaceIdentifier spaceIdentifier)
    {
        return createSpaceRole(roleCode, spaceIdentifier.getSpaceCode());
    }

    protected RoleWithIdentifier createSpaceRole(RoleCode roleCode, String spaceCode)
    {
        SpacePE groupPE = new SpacePE();
        groupPE.setCode(spaceCode);
        return createSpaceRole(roleCode, groupPE);
    }

    protected RoleWithIdentifier createSpaceRole(RoleCode roleCode, SpacePE space)
    {
        return new RoleWithIdentifier(RoleLevel.SPACE, roleCode, space, null);
    }

    /**
     * Creates a role with level {@link RoleLevel#INSTANCE} with specified role code for specified database instance.
     */
    protected RoleWithIdentifier createInstanceRole(RoleCode roleCode)
    {
        return new RoleWithIdentifier(RoleLevel.INSTANCE, roleCode, null, null);
    }

    protected RoleAssignmentPE createProjectRoleAssignment(RoleCode roleCode, String spaceCode, String projectCode)
    {
        SpacePE space = new SpacePE();
        space.setCode(spaceCode);

        ProjectPE project = new ProjectPE();
        project.setCode(projectCode);
        project.setSpace(space);

        RoleAssignmentPE assignment = new RoleAssignmentPE();
        assignment.setRole(roleCode);
        assignment.setProject(project);

        return assignment;
    }

    protected RoleAssignmentPE createSpaceRoleAssignment(RoleCode roleCode, String spaceCode)
    {
        SpacePE space = new SpacePE();
        space.setCode(spaceCode);

        RoleAssignmentPE assignment = new RoleAssignmentPE();
        assignment.setRole(roleCode);
        assignment.setSpace(space);

        return assignment;
    }

    protected RoleAssignmentPE createInstanceRoleAssignment(RoleCode roleCode)
    {
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        assignment.setRole(roleCode);

        return assignment;
    }

    /**
     * Creates a person. Only userId and databaseInstance are definied.
     */
    protected PersonPE createPerson()
    {
        return createPerson("megapixel");
    }

    /** Creates a person with specified userId and database instance. */
    protected PersonPE createPerson(String userId)
    {
        final PersonPE personPE = new PersonPE();
        personPE.setUserId(userId);
        return personPE;
    }

    /**
     * Creates a list of spaces which contains {@link #createSpace()} and {@link #createAnotherSpace()}.
     */
    protected List<SpacePE> createSpaces()
    {
        final List<SpacePE> spaces = new ArrayList<SpacePE>();
        spaces.add(createSpace());
        spaces.add(createAnotherSpace());
        return spaces;
    }

    /**
     * Creates a group with code {@link #SPACE_CODE}.
     */
    protected SpacePE createSpace()
    {
        return createSpace(SPACE_CODE);
    }

    /**
     * Creates a space with code {@link #ANOTHER_SPACE_CODE}.
     */
    protected SpacePE createAnotherSpace()
    {
        return createSpace(ANOTHER_SPACE_CODE);
    }

    /**
     * Creates a space based on the specified identifier.
     */
    protected SpacePE createSpace(SpaceIdentifier identifier)
    {
        return createSpace(identifier.getSpaceCode());
    }

    /**
     * Creates a space with specified group code and database instance.
     */
    protected SpacePE createSpace(final String spaceCode)
    {
        final SpacePE space = new SpacePE();
        space.setCode(spaceCode);
        return space;
    }

    /**
     * Creates a person with two {@link RoleAssignmentPE} instances. One instance ADMIN role and a USER role for the group
     * {@link #createAnotherSpace()}.
     */
    protected PersonPE createPersonWithRoleAssignments()
    {
        final PersonPE person = createPerson();
        assignRoles(person);
        return person;
    }

    protected PersonPE createPersonWithRoleAssignments(RoleAssignmentPE... assignments)
    {
        PersonPE person = createPerson();
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(assignments)));
        return person;
    }

    /**
     * Assigns two {@link RoleAssignmentPE} instances to specified person. One instance ADMIN role and a USER role for the group
     * {@link #createAnotherSpace()}.
     */
    protected void assignRoles(PersonPE person)
    {
        final Set<RoleAssignmentPE> list = new HashSet<RoleAssignmentPE>();
        // Database assignment
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        assignment.setRole(RoleCode.ADMIN);
        person.addRoleAssignment(assignment);
        list.add(assignment);
        // Group assignment
        assignment = new RoleAssignmentPE();
        assignment.setRole(RoleCode.USER);
        assignment.setSpace(createAnotherSpace());
        person.addRoleAssignment(assignment);
        list.add(assignment);

        person.setRoleAssignments(list);
    }

    /**
     * Creates a project in the specified group.
     */
    protected ProjectPE createProject(SpacePE group)
    {
        final ProjectPE projectPE = new ProjectPE();
        projectPE.setSpace(group);
        return projectPE;
    }

    /**
     * Creates an experiment in the specified group.
     */
    protected ExperimentPE createExperiment(SpacePE group)
    {
        final ExperimentPE experiment = new ExperimentPE();
        final ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode("XXX");
        experiment.setExperimentType(experimentType);
        experiment.setProject(createProject(group));
        return experiment;
    }

    /**
     * Creates a sample in the specified group.
     */
    protected SamplePE createSample(SpacePE group)
    {
        final SamplePE sample = new SamplePE();
        sample.setSpace(group);
        sample.setSampleType(createSampleType());
        return sample;
    }

    /**
     * Creates sample type.
     */
    protected SampleTypePE createSampleType()
    {
        SampleTypePE type = new SampleTypePE();
        type.setContainerHierarchyDepth(0);
        type.setGeneratedFromHierarchyDepth(0);
        type.setListable(true);
        type.setAutoGeneratedCode(false);
        type.setShowParentMetadata(false);
        type.setSubcodeUnique(false);
        return type;
    }

    /**
     * Creates a sample in the specified database instance.
     */
    protected SamplePE createSample()
    {
        final SamplePE sample = new SamplePE();
        sample.setSampleType(createSampleType());
        return sample;
    }

    /**
     * Creates a filter in the specified database instance and registrator and ownership flag.
     */
    protected GridCustomFilterPE createFilter(PersonPE registrator, boolean isPublic)
    {
        final GridCustomFilterPE filter = new GridCustomFilterPE();
        filter.setRegistrator(registrator);
        filter.setPublic(isPublic);
        filter.setExpression(""); // needed for translation
        return filter;
    }

    /**
     * Creates a list of roles which contains a space admin role for space {@link AuthorizationTestCase#ANOTHER_SPACE_CODE}.
     */
    protected List<RoleWithIdentifier> createAnotherSpaceAdminRole()
    {
        final List<RoleWithIdentifier> list = new ArrayList<RoleWithIdentifier>();
        final RoleWithIdentifier spaceRole =
                createSpaceRole(RoleCode.USER, new SpaceIdentifier(ANOTHER_SPACE_CODE));
        list.add(spaceRole);
        return list;
    }

    /**
     * Creates a list of roles which contains a space role for a USER and group defined by code {@link #SPACE_CODE}. If
     * <code>withInstanceRole == true</code> the list contains in addition an instance ADMIN role.
     */
    protected List<RoleWithIdentifier> createRoles(final boolean withInstanceRole)
    {
        final List<RoleWithIdentifier> list = new ArrayList<RoleWithIdentifier>();
        final RoleWithIdentifier spaceRole =
                createSpaceRole(RoleCode.USER, new SpaceIdentifier(SPACE_CODE));
        list.add(spaceRole);
        if (withInstanceRole)
        {
            final RoleWithIdentifier databaseInstanceRole = createInstanceRole(RoleCode.ADMIN);
            list.add(databaseInstanceRole);
        }
        return list;
    }

    /**
     * Prepares {@link #provider} to expect a query for the home database instance and spaces.
     */
    protected final void prepareProvider(
            final List<SpacePE> spaces)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).listSpaces();
                    will(returnValue(spaces));
                }
            });
    }

    /**
     * Prepares {@link #provider} to expect a query to list spaces which will return the specified list of spaces and a query for the specified entity
     * kind and technical id which will return the specifier space.
     */
    protected final void prepareProvider(final List<SpacePE> spaces, final SpacePE spacePE,
            final SpaceOwnerKind entityKind, final TechId techId)
    {
        context.checking(new Expectations()
            {
                {
                    one(provider).listSpaces();
                    will(returnValue(spaces));

                    one(provider).tryGetSpace(entityKind, techId);
                    will(returnValue(spacePE));
                }
            });
    }

    protected void expectAuthorizationConfig(final IAuthorizationConfig config)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).getAuthorizationConfig();
                    will(returnValue(config));
                }
            });
    }

    protected static void assertOK(Status status)
    {
        assertTrue(status.isOK());
    }

    protected static void assertError(Status status)
    {
        assertTrue(status.isError());
    }

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        provider = context.mock(IAuthorizationDataProvider.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

}
