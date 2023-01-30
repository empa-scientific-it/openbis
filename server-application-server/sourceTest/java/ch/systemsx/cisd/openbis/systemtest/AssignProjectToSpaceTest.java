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

package ch.systemsx.cisd.openbis.systemtest;

import static org.hamcrest.CoreMatchers.is;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 */
public class AssignProjectToSpaceTest extends BaseTest
{
    Space sourceSpace;

    Space destinationSpace;

    @Test
    public void projectCanBeAssignedToAnotherSpace() throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));

        perform(anUpdateOf(project).toSpace(destinationSpace));

        assertThat(project, is(inSpace(destinationSpace)));
    }

    @Test
    public void assigningProjectToAnotherSpaceChangesSpaceAssignmentOfSamplesInExperimentsInThatProject()
            throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(project).toSpace(destinationSpace));

        assertThat(sample, is(inSpace(destinationSpace)));
    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAssignProjectToSpace", groups = "authorization")
    public void assigningProjectToSpaceIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(project).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignProjectToSpace", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningProjectToSpaceIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(project).toSpace(destinationSpace).as(user));
    }

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignProjectToSpaceRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignProjectToSpaceRule =
                and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider
    Object[][] rolesAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignProjectToSpaceRule, source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignProjectToSpaceRule), source,
                destination, instance);
    }

}
