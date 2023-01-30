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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
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
public class UpdateDataSetParentsTest extends BaseTest
{

    Sample sample;

    @Test
    public void dataSetCanBeUpdatedToHaveAnotherDataSetAsItsParent() throws Exception
    {
        AbstractExternalData parentToBe = create(aDataSet().inSample(sample));
        AbstractExternalData childToBe = create(aDataSet().inSample(sample));

        perform(anUpdateOf(childToBe).withParent(parentToBe));

        assertThat(childToBe, hasParents(parentToBe));
        assertThat(parentToBe, hasChildren(childToBe));
    }

    @Test
    public void dataSetCanBeUpdatedToHaveDifferentParent() throws Exception
    {
        AbstractExternalData parent = create(aDataSet().inSample(sample));
        AbstractExternalData child = create(aDataSet().inSample(sample).withParent(parent));
        AbstractExternalData newParent = create(aDataSet().inSample(sample));

        perform(anUpdateOf(child).withParent(newParent));

        assertThat(child, hasParents(newParent));
        assertThat(parent, hasNoChildren());
        assertThat(newParent, hasChildren(child));
    }

    @Test
    public void parentOfDataSetCanBeRemoved() throws Exception
    {
        AbstractExternalData parent1 = create(aDataSet().inSample(sample));
        AbstractExternalData parent2 = create(aDataSet().inSample(sample));
        AbstractExternalData child = create(aDataSet().inSample(sample).withParents(parent1, parent2));

        perform(anUpdateOf(child).withParent(parent1));

        assertThat(child, hasParents(parent1));
        assertThat(parent1, hasChildren(child));
        assertThat(parent2, hasNoChildren());
    }

    @Test
    public void allParentsOfDataSetCanBeRemoved() throws Exception
    {
        AbstractExternalData parent1 = create(aDataSet().inSample(sample));
        AbstractExternalData parent2 = create(aDataSet().inSample(sample));
        AbstractExternalData child = create(aDataSet().inSample(sample).withParents(parent1, parent2));

        perform(anUpdateOf(child).removingParents());

        assertThat(child, hasNoParents());
        assertThat(parent1, hasNoChildren());
        assertThat(parent2, hasNoChildren());
    }

    @Test
    public void duplicateParentDefinitionsAreSilentlyDismissed() throws Exception
    {
        AbstractExternalData parent1 = create(aDataSet().inSample(sample));
        AbstractExternalData parent2 = create(aDataSet().inSample(sample));
        AbstractExternalData child = create(aDataSet().inSample(sample));

        perform(anUpdateOf(child).withParents(parent1, parent2, parent1, parent2, parent2));

        assertThat(child, hasParents(parent1, parent2));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void dataSetCannotBeItsOwnParent() throws Exception
    {
        AbstractExternalData data = create(aDataSet().inSample(sample));

        perform(anUpdateOf(data).withParent(data));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void dataSetCannotBeItsOwnGrandParent() throws Exception
    {
        AbstractExternalData parent = create(aDataSet().inSample(sample));
        AbstractExternalData child = create(aDataSet().inSample(sample).withParent(parent));

        perform(anUpdateOf(parent).withParent(child));
    }

    @Test
    public void parentCanBeInDifferentSpaceThanChild() throws Exception
    {
        AbstractExternalData parent = create(aDataSet().inSample(parentSample));
        AbstractExternalData child = create(aDataSet().inSample(childSample));

        perform(anUpdateOf(child).withParent(parent));

        assertThat(child, hasParents(parent));
    }

    Sample childSample;

    Sample parentSample;

    Space childSpace;

    Space parentSpace;

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAddParentToDataSet", groups = "authorization")
    public void addingParentToDataSetIsAllowedFor(RoleWithHierarchy childSpaceRole,
            RoleWithHierarchy parentSpaceRole, RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData parentToBe = create(aDataSet().inSample(parentSample));
        AbstractExternalData childToBe = create(aDataSet().inSample(childSample));
        String user =
                create(aSession().withSpaceRole(childSpaceRole, childSpace)
                        .withSpaceRole(parentSpaceRole, parentSpace).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(childToBe).withParent(parentToBe).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAddParentToDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void addingParentToDataSetNotIsAllowedFor(RoleWithHierarchy childSpaceRole,
            RoleWithHierarchy parentSpaceRole, RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData parentToBe = create(aDataSet().inSample(parentSample));
        AbstractExternalData childToBe = create(aDataSet().inSample(childSample));
        String user =
                create(aSession().withSpaceRole(childSpaceRole, childSpace)
                        .withSpaceRole(parentSpaceRole, parentSpace).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(childToBe).withParent(parentToBe).as(user));
    }

    @Test(dataProvider = "rolesAllowedToRemoveParentFromDataSet", groups = "authorization")
    public void removingParentFromDataSetIsAllowedFor(RoleWithHierarchy childSpaceRole,
            RoleWithHierarchy parentSpaceRole, RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData parent1 = create(aDataSet().inSample(parentSample));
        AbstractExternalData parent2 = create(aDataSet().inSample(parentSample));
        AbstractExternalData child = create(aDataSet().inSample(childSample).withParents(parent1, parent2));

        String user =
                create(aSession().withSpaceRole(childSpaceRole, childSpace)
                        .withSpaceRole(parentSpaceRole, parentSpace).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(child).withParent(parent1).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveParentFromDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void removingParentFromDataSetNotIsAllowedFor(RoleWithHierarchy childSpaceRole,
            RoleWithHierarchy parentSpaceRole, RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData parent1 = create(aDataSet().inSample(parentSample));
        AbstractExternalData parent2 = create(aDataSet().inSample(parentSample));
        AbstractExternalData child = create(aDataSet().inSample(childSample).withParents(parent1, parent2));

        String user =
                create(aSession().withSpaceRole(childSpaceRole, childSpace)
                        .withSpaceRole(parentSpaceRole, parentSpace).withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(child).withParent(parent1).as(user));
    }

    @BeforeMethod
    void createFixture() throws Exception
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        sample = create(aSample().inExperiment(experiment));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());

        childSpace = create(aSpace());
        parentSpace = create(aSpace());
        Project childProject = create(aProject().inSpace(childSpace));
        Project parentProject = create(aProject().inSpace(parentSpace));
        Experiment childExperiment = create(anExperiment().inProject(childProject));
        Experiment parentExperiment = create(anExperiment().inProject(parentProject));
        childSample = create(aSample().inExperiment(childExperiment));
        parentSample = create(aSample().inExperiment(parentExperiment));

    }

    GuardedDomain childSpaceDomain;

    GuardedDomain parentSpaceDomain;

    GuardedDomain instance;

    AuthorizationRule updateParentsOfDataSetRule;

    AuthorizationRule removeParentFromDataSetRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        childSpaceDomain = new SpaceDomain(instance);
        parentSpaceDomain = new SpaceDomain(instance);

        updateParentsOfDataSetRule =
                and(rule(childSpaceDomain, RoleWithHierarchy.SPACE_POWER_USER),
                        or(rule(parentSpaceDomain, RoleWithHierarchy.SPACE_POWER_USER),
                                rule(parentSpaceDomain, RoleWithHierarchy.SPACE_ETL_SERVER)));
    }

    @DataProvider
    Object[][] rolesAllowedToAddParentToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(updateParentsOfDataSetRule, childSpaceDomain,
                parentSpaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAddParentToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(updateParentsOfDataSetRule),
                childSpaceDomain, parentSpaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToRemoveParentFromDataSet()
    {
        return RolePermutator.getAcceptedPermutations(updateParentsOfDataSetRule, childSpaceDomain,
                parentSpaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToRemoveParentFromDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(updateParentsOfDataSetRule),
                childSpaceDomain, parentSpaceDomain, instance);
    }
}
