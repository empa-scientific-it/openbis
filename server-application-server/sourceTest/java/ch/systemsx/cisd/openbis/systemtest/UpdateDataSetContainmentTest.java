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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
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
import ch.systemsx.cisd.openbis.systemtest.base.builder.ExternalDataBuilder;

/**
 * @author anttil
 */
public class UpdateDataSetContainmentTest extends BaseTest
{

    Sample sample;

    @Test
    public void dataSetCanBeSetToBeContainedByAnotherDataSetByUpdatingTheContainerDataSet()
            throws Exception
    {
        AbstractExternalData componentCandidate = create(aDataSet().inSample(sample));
        AbstractExternalData container = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(container).withComponent(componentCandidate));

        assertThat(componentCandidate, hasContainer(container));
    }

    @Test
    public void dataSetCanBeSetToBeContainedByAnotherDataSetByUpdatingTheComponentDataSet()
            throws Exception
    {
        AbstractExternalData componentCandidate = create(aDataSet().inSample(sample));
        AbstractExternalData container = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(componentCandidate).withContainer(container));

        assertThat(componentCandidate, hasContainer(container));
    }

    @Test
    public void dataSetWithContainerTypeCanBeContainerOfAnotherDataSet() throws Exception
    {
        AbstractExternalData container = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component = create(aDataSet().inSample(sample));

        perform(anUpdateOf(component).withContainer(container));

        assertThat(component, hasContainer(container));
    }

    @Test
    public void dataSetWithContainerTypeCanHaveComponents() throws Exception
    {
        AbstractExternalData container = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component = create(aDataSet().inSample(sample));

        perform(anUpdateOf(container).withComponent(component));

        assertThat(component, hasContainer(container));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void dataSetWithComponentTypeCannotBeContainerOfAnotherDataSet() throws Exception
    {
        AbstractExternalData component1 = create(aDataSet().inSample(sample));
        AbstractExternalData component2 = create(aDataSet().inSample(sample));

        perform(anUpdateOf(component1).withContainer(component2));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void dataSetWithComponentTypeCannotHaveComponents() throws Exception
    {
        AbstractExternalData component1 = create(aDataSet().inSample(sample));
        AbstractExternalData component2 = create(aDataSet().inSample(sample));

        perform(anUpdateOf(component1).withComponent(component2));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void dataSetCannotBeSelfContainedViaContainerRelation() throws Exception
    {
        AbstractExternalData component1 = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component2 = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component3 = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(component1).withContainer(component2));
        perform(anUpdateOf(component2).withContainer(component3));
        perform(anUpdateOf(component3).withContainer(component1));
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void dataSetCannotBeSelfContainedViaComponentsRelation() throws Exception
    {
        AbstractExternalData component1 = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component2 = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component3 = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(component1).withComponent(component2));
        perform(anUpdateOf(component2).withComponent(component3));
        perform(anUpdateOf(component3).withComponent(component1));
    }

    @Test
    public void containmentCanBeRemoved() throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sample));
        AbstractExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));
        daoFactory.getSessionFactory().getCurrentSession().flush();

        perform(anUpdateOf(container).withComponents());

        assertThat(component, hasNoContainer());
    }

    @Test
    public void dataSetCanBeAddedToAnotherContainer() throws Exception
    {
        ExternalDataBuilder containerBuilder = aDataSet().inSample(sample).asContainer();
        AbstractExternalData component1 = create(aDataSet().inSample(sample));
        AbstractExternalData component2 = create(aDataSet().inSample(sample));
        AbstractExternalData container = create(containerBuilder.withComponent(component1).withComponent(component2));
        AbstractExternalData newContainer = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(newContainer).withComponent(component2));

        assertThat(component1, hasContainer(container).orderInContainer(0));
        assertThat(component2, hasContainer(container).orderInContainer(1));
        assertThat(component2, hasContainer(newContainer).orderInContainer(0));
        assertThat(container, hasComponents(component1, component2));
        assertThat(newContainer, hasComponents(component2));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void dataSetCannotContainItself() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().inSample(sample));

        perform(anUpdateOf(dataset).withComponent(dataset));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void subcomponentsAreNotAllowed() throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sample));
        AbstractExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));
        AbstractExternalData subcomponent = create(aDataSet().inSample(sample));

        perform(anUpdateOf(component).withComponent(subcomponent));

        assertThat(component, hasContainer(container));
        assertThat(subcomponent, hasContainer(component));
    }

    @Test(expectedExceptions =
    { UserFailureException.class })
    public void containerCannotBeInDifferentSpaceThanComponent() throws Exception
    {
        Space containerSpace = create(aSpace());
        Project containerProject = create(aProject().inSpace(containerSpace));
        Experiment containerExperiment = create(anExperiment().inProject(containerProject));
        Sample containerSample = create(aSample().inExperiment(containerExperiment));
        AbstractExternalData container = create(aDataSet().inSample(containerSample));
        AbstractExternalData component = create(aDataSet().inSample(sample));

        perform(anUpdateOf(container).withComponent(component));

    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAddContainerToDataSet", groups = "authorization")
    public void addingContainerToDataSetIsAllowedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData container = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component = create(aDataSet().inSample(sample));
        String user =
                create(aSession().withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponent(component).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAddContainerToDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void addingContainerToDataSetNotIsAllowedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData container = create(aDataSet().inSample(sample).asContainer());
        AbstractExternalData component = create(aDataSet().inSample(sample));
        String user =
                create(aSession().withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponent(component).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAddContainerToDataSet", groups = "authorization")
    public void removingContainerFromDataSetIsAllowedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sample));
        AbstractExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));

        String user =
                create(aSession().withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponents().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAddContainerToDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void removingContainerFromDataSetNotIsAllowedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sample));
        AbstractExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));
        String user =
                create(aSession().withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponents().as(user));
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
    }

    GuardedDomain spaceDomain;

    GuardedDomain instance;

    AuthorizationRule addParentToSampleRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        spaceDomain = new SpaceDomain(instance);

        addParentToSampleRule = rule(spaceDomain, RoleWithHierarchy.SPACE_POWER_USER);
    }

    @DataProvider
    Object[][] rolesAllowedToAddContainerToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(addParentToSampleRule, spaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAddContainerToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(addParentToSampleRule), spaceDomain,
                instance);
    }
}
