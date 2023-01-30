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
public class UnassignSampleFromSpaceTest extends BaseTest
{
    Space space;

    Experiment experiment;

    @Test
    public void spaceLevelSampleCanBeUnassignedFromSpace() throws Exception
    {
        Sample sample = create(aSample().inSpace(space));

        perform(anUpdateOf(sample).removingSpace());

        assertThat(sample, hasNoSpace());
    }

    @Test
    public void sampleWithAnExperimentCanBeUnassignedFromSpace() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingSpace());

        assertThat(sample, hasNoSpace());
    }

    @Test
    public void experimentAssignmentOfSampleIsRemovedWhenSampleIsUnassignedFromSpace()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingSpace());

        assertThat(sample, hasNoSpace());
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void sampleWithDataSetCannotBeUnassignedFromSpace() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));
        create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).removingSpace());
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void childSampleCantBeUnassignedFromSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(child).removingSpace());
    }

    @Test
    public void parentSampleCanBeUnassignedFromSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingSpace());

        assertThat(parent, hasNoSpace());
    }

    @Test
    public void spaceAssignmentOfChildSampleIsNotChangedWhenParentSampleIsUnassignedFromSpace()
            throws Exception
    {

        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingSpace());

        assertThat(child, is(inSpace(space)));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void componentOfSpaceLevelSampleCantBeUnassignedFromSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).removingSpace());
    }

    @Test
    public void containerOfSpaceLevelSampleCanBeUnassignedFromSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).removingSpace());

        assertThat(container, hasNoSpace());
    }

    @Test
    public void spaceAssociationOfComponentSampleIsNotChangedWhenContainerSampleIsUnassignedFromSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).removingSpace());

        assertThat(component, is(inSpace(space)));
    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromSpace", groups = "authorization")
    public void unassigningIsAllowedFor(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).removingSpace().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromSpace", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void unassigningIsNotAllowedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).removingSpace().as(user));
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

    AuthorizationRule unassignSampleFromSpaceRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        spaceDomain = new SpaceDomain(instance);

        unassignSampleFromSpaceRule =
                and(rule(spaceDomain, RoleWithHierarchy.SPACE_USER),
                        rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }

    @DataProvider
    Object[][] rolesAllowedToUnassignSampleFromSpace()
    {
        return RolePermutator.getAcceptedPermutations(unassignSampleFromSpaceRule, spaceDomain,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToUnassignSampleFromSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(unassignSampleFromSpaceRule),
                spaceDomain, instance);
    }
}
