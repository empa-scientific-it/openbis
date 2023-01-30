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
import ch.systemsx.cisd.common.exceptions.UserFailureException;
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
public class UnassignSampleFromExperimentTest extends BaseTest
{
    Experiment experiment;

    Space space;

    @Test
    public void experimentAssignmentOfSampleIsRemoved() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingExperiment());

        assertThat(sample, hasNoExperiment());
    }

    @Test
    public void spaceAssociationOfSampleIsLeftIntact() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingExperiment());

        assertThat(sample, is(inSpace(space)));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void unassigningFailsIfTheSampleHasDataSets() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).removingExperiment());
    }

    @Test
    public void childSampleCanBeUnassigned() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(child).removingExperiment());

        assertThat(child, hasNoExperiment());
    }

    @Test
    public void experimentAssignmentOfParentSampleIsNotChangedWhenChildSampleIsUnassigned()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(child).removingExperiment());

        assertThat(parent, is(inExperiment(experiment)));
    }

    @Test
    public void parentSampleCanBeUnassigned() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingExperiment());

        assertThat(parent, hasNoExperiment());
    }

    @Test
    public void experimentAssignmentOfChildSampleIsNotChangedWhenParentSampleIsUnassigned()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingExperiment());

        assertThat(child, is(inExperiment(experiment)));
    }

    @Test
    public void componentSampleCanBeUnassigned() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(component).removingExperiment());

        assertThat(component, hasNoExperiment());
    }

    @Test
    public void experimentAssignmentOfContainerSampleIsNotChangedWhenComponentSampleIsUnassigned()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(component).removingExperiment());

        assertThat(container, is(inExperiment(experiment)));
    }

    @Test
    public void containerSampleCanBeUnassigned() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(container).removingExperiment());

        assertThat(container, hasNoExperiment());
    }

    @Test
    public void experimentAssignmentOfComponentSampleIsNotChangedWhenContainerSampleIsUnassigned()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(container).removingExperiment());

        assertThat(component, is(inExperiment(experiment)));
    }

    @Test
    public void unassigningCanBeDoneThroughExperimentUpdate() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(experiment).removingSamples());

        assertThat(sample, hasNoExperiment());
    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromExperiment", groups = "authorization")
    public void unassigningIsAllowedFor(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).removingExperiment().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromExperiment", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void unassigningIsNotAllowedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).removingExperiment().as(user));
    }

    @BeforeMethod
    void createFixture() throws Exception
    {
        space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        experiment = create(anExperiment().inProject(project));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain spaceDomain;

    GuardedDomain instance;

    AuthorizationRule unassignSampleFromExperimentRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        spaceDomain = new SpaceDomain(instance);

        unassignSampleFromExperimentRule =
                or(rule(spaceDomain, RoleWithHierarchy.SPACE_POWER_USER),

                        and(rule(spaceDomain, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));
    }

    @DataProvider
    Object[][] rolesAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(unassignSampleFromExperimentRule,
                spaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(unassignSampleFromExperimentRule),
                spaceDomain, instance);
    }
}
