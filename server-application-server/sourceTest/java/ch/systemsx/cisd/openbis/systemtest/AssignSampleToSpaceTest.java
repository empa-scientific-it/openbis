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
import org.testng.annotations.BeforeMethod;
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
public class AssignSampleToSpaceTest extends BaseTest
{

    Experiment experiment;

    Space sourceSpace;

    Space destinationSpace;

    @Test
    public void sampleWithExperimentCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfSampleIsRemovedWhenSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(sample, hasNoExperiment());
    }

    @Test
    public void spaceSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void sharedSampleCanBeAssignedToSpace() throws Exception
    {
        Sample sample = create(aSample());

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void childSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(child).toSpace(destinationSpace));

        assertThat(child, is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfParentSampleIsNotChangedWhenChildSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(child).toSpace(destinationSpace));

        assertThat(parent, is(inSpace(sourceSpace)));
    }

    @Test
    public void parentSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(parent).toSpace(destinationSpace));

        assertThat(parent, is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfChildSampleIsNotChangedWhenParentSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(parent).toSpace(destinationSpace));

        assertThat(child, is(inSpace(sourceSpace)));
    }

    @Test
    public void componentSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).toSpace(destinationSpace));

        assertThat(component, is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfContainerSampleIsNotChangedWhenComponentSampleIsAssignedToAnotherSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).toSpace(destinationSpace));

        assertThat(container, is(inSpace(sourceSpace)));
    }

    @Test
    public void containerSampleCanBeAssignedToAnotherSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).toSpace(destinationSpace));

        assertThat(container, is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfComponentSampleIsNotChangedWhenContainerSampleIsAssingnedToAnotherSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).toSpace(destinationSpace));

        assertThat(component, is(inSpace(sourceSpace)));
    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAssignSampleToSpace", groups = "authorization")
    public void assigningSampleToAnotherSpaceIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToSpace", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToAnotherSpaceIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSharedSampleToSpace", groups = "authorization")
    public void assigningSharedSampleToSpaceIsAllowedFor(RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample sharedSample = create(aSample());
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sharedSample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSharedSampleToSpace", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSharedSampleToSpaceIsNotAllowedFor(RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample sharedSample = create(aSample());
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sharedSample).toSpace(destinationSpace).as(user));
    }

    @Test
    public void assigningSampleToSameSpaceIsAllowedToAllSpaceUsers() throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));
        String user = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_USER, sourceSpace));

        perform(anUpdateOf(sample).toSpace(sourceSpace).as(user));

        assertThat(sample, is(inSpace(sourceSpace)));
    }

    @BeforeMethod
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));
        experiment = create(anExperiment().inProject(project));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignSampleToSpaceRule;

    AuthorizationRule assignSharedSampleToSpaceRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignSampleToSpaceRule =
                or(and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER)),

                        and(rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(destination, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));

        assignSharedSampleToSpaceRule =
                and(rule(destination, RoleWithHierarchy.SPACE_USER),
                        rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignSampleToSpaceRule, source, destination,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSampleToSpaceRule), source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSharedSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignSharedSampleToSpaceRule, destination,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSharedSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSharedSampleToSpaceRule),
                destination, instance);
    }
}
