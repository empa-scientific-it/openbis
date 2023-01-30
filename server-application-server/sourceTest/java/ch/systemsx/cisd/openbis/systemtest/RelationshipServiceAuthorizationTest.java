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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 */
public class RelationshipServiceAuthorizationTest extends BaseTest
{
    Space sourceSpace;

    Space destinationSpace;

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    Project sourceProject;

    Project destinationProject;

    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Sample sourceSample;

    Sample destinationSample;

    Sample sharedSample;

    AbstractExternalData sourceDataSet;

    AbstractExternalData destinationDataSet;

    @BeforeMethod
    public void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));
        sourceSample = create(aSample().inExperiment(sourceExperiment));

        destinationSpace = create(aSpace());
        destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
        destinationSample = create(aSample().inExperiment(destinationExperiment));

        sharedSample = create(aSample());

        sourceDataSet = create(aDataSet().inSample(sourceSample));
        destinationDataSet = create(aDataSet().inSample(destinationSample));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    @Test(dataProvider = "rolesAllowedToAssignExperimentToProject", groups = "authorization")
    public void assigningExperimentToProjectIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignExperimentToProject", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningExperimentToProjectIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignProjectToSpace", groups = "authorization")
    public void assigningProjectToSpaceIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignProjectToSpace", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assignProjectToSpaceIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment", groups = "authorization")
    public void assigningSampleToExperimentIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assignSampleToExperimentIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromExperiment", groups = "authorization")
    public void unassigningSampleFromExperimentIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unassignSampleFromExperiment(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromExperiment", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void unassigningSampleFromExperimentIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unassignSampleFromExperiment(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToUnshareSample", groups = "authorization")
    public void unsharingSampleIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unshareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToUnshareSample", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void unsharingSampleIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unshareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToSpace", groups = "authorization")
    public void assigningSampleToSpaceIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignSampleToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToSpace", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToSpaceIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignSampleToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToShareSample", groups = "authorization")
    public void sharingSampleIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        shareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToShareSample", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void sharingSampleIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        shareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToExperiment", groups = "authorization")
    public void assigningDataSetToExperimentIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignDataSetToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToExperimentIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignDataSetToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToSample", groups = "authorization")
    public void assigningDataSetToSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignDataSetToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        assignDataSetToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAddParentToSample", groups = "authorization")
    public void addingParentToSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addParentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAddParentToSample", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void addingParentToSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addParentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToRemoveParentFromSample", groups = "authorization")
    public void removingParentFromSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        removeParentFromSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveParentFromSample", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void removingParentFromSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        removeParentFromSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAddContainerToSample", groups = "authorization")
    public void addingContainerToSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addContainerToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAddContainerToSample", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void addingContainerToSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addContainerToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToRemoveContainerFromSample", groups = "authorization")
    public void removingContainerFromSampleIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        removeContainerFromSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveContainerFromSample", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void removingContainerFromSampleIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        removeContainerFromSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAddParentToDataSet", groups = "authorization")
    public void addingParentToDataSetIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addParentToDataSet(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAddParentToDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void addingParentToDataSetIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addParentToDataSet(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToRemoveParentFromDataSet", groups = "authorization")
    public void removingParentFromDataSetIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        removeParentFromDataSet(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveParentFromDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void removingParentFromDataSetIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        removeParentFromDataSet(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAddContainerToDataSet", groups = "authorization")
    public void addingContainerToDataSetIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addContainerToDataSet(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAddContainerToDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void addingContainerToDataSetIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        addContainerToDataSet(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToRemoveContainerFromDataSet", groups = "authorization")
    public void removingContainerFromDataSetIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole) throws Exception
    {
        removeContainerFromDataSet(spaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveContainerFromDataSet", expectedExceptions =
    { AuthorizationFailureException.class }, groups = "authorization")
    public void removingContainerFromDataSetIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole) throws Exception
    {
        removeContainerFromDataSet(spaceRole, destinationSpaceRole, instanceRole);
    }

    void assignExperimentToProject(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignExperimentToProject(sessionManager.getSession(session),
                pe(sourceExperiment), pe(destinationProject));
    }

    void assignProjectToSpace(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignProjectToSpace(sessionManager.getSession(session),
                pe(sourceProject), pe(destinationSpace));
    }

    void assignSampleToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignSampleToExperiment(sessionManager.getSession(session),
                pe(sourceSample), pe(destinationExperiment));
    }

    void unassignSampleFromExperiment(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(spaceRole, sourceSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.unassignSampleFromExperiment(sessionManager.getSession(session),
                pe(sourceSample));
    }

    void unshareSample(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(spaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.unshareSample(sessionManager.getSession(session), pe(sharedSample),
                pe(destinationSpace));
    }

    void assignSampleToSpace(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {

        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignSampleToSpace(sessionManager.getSession(session),
                pe(sourceSample), pe(destinationSpace));
    }

    void shareSample(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession().withSpaceRole(spaceRole, sourceSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.shareSample(sessionManager.getSession(session), pe(sourceSample));
    }

    void assignDataSetToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignDataSetToExperiment(sessionManager.getSession(session),
                pe(sourceDataSet), pe(destinationExperiment));
    }

    void assignDataSetToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {

        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignDataSetToSample(sessionManager.getSession(session),
                pe(sourceDataSet), pe(destinationSample));
    }

    void addParentToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.addParentToSample(sessionManager.getSession(session), pe(sourceSample),
                pe(destinationSample));

    }

    void removeParentFromSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.removeParentFromSample(sessionManager.getSession(session),
                pe(sourceSample), pe(destinationSample));
    }

    void addContainerToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignSampleToContainer(sessionManager.getSession(session),
                pe(sourceSample), pe(destinationSample));

    }

    void removeContainerFromSample(RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.removeSampleFromContainer(sessionManager.getSession(session),
                pe(this.sourceSample));
    }

    void addParentToDataSet(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.addParentToDataSet(sessionManager.getSession(session),
                pe(sourceDataSet), pe(destinationDataSet));

    }

    void removeParentFromDataSet(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.removeParentFromDataSet(sessionManager.getSession(session),
                pe(sourceDataSet), pe(destinationDataSet));
    }

    void addContainerToDataSet(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignDataSetToContainer(sessionManager.getSession(session),
                pe(sourceDataSet), pe(destinationDataSet));

    }

    void removeContainerFromDataSet(RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.removeDataSetFromContainer(sessionManager.getSession(session),
                pe(this.sourceDataSet), pe(destinationDataSet));
    }

    GuardedDomain instance = new InstanceDomain();

    GuardedDomain source = new SpaceDomain(instance);

    GuardedDomain destination = new SpaceDomain(instance);

    private static AuthorizationRule spaceAdminOrSpaceEtlServer(GuardedDomain domain)
    {
        return or(rule(domain, RoleWithHierarchy.SPACE_POWER_USER),
                rule(domain, RoleWithHierarchy.SPACE_ETL_SERVER));
    }

    AuthorizationRule spaceAdminOrSpaceEtlServer = and(spaceAdminOrSpaceEtlServer(source),
            spaceAdminOrSpaceEtlServer(destination));

    AuthorizationRule spaceAdminOrSpaceEtlServerSingle = spaceAdminOrSpaceEtlServer(source);

    private static AuthorizationRule spaceUserOrBetter(GuardedDomain domain)
    {
        return or(rule(domain, RoleWithHierarchy.SPACE_USER),
                rule(domain, RoleWithHierarchy.SPACE_POWER_USER),
                rule(domain, RoleWithHierarchy.SPACE_ETL_SERVER));
    }

    AuthorizationRule spaceUserOrBetter = and(spaceUserOrBetter(source),
            spaceUserOrBetter(destination));

    AuthorizationRule spaceUserOrBetterSingle = spaceUserOrBetter(source);

    AuthorizationRule instanceEtlServer = rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER);

    @DataProvider(name = "rolesAllowedToAssignExperimentToProject")
    RoleWithHierarchy[][] rolesAllowedToAssignExperimentToProject()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignExperimentToProject")
    RoleWithHierarchy[][] rolesNotAllowedToAssignExperimentToProject()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignProjectToSpace")
    RoleWithHierarchy[][] rolesAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignProjectToSpace")
    RoleWithHierarchy[][] rolesNotAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignSampleToExperiment")
    RoleWithHierarchy[][] rolesAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(spaceUserOrBetter, source, destination,
                instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignSampleToExperiment")
    RoleWithHierarchy[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceUserOrBetter), source, destination,
                instance);
    }

    @DataProvider(name = "rolesAllowedToUnassignSampleFromExperiment")
    RoleWithHierarchy[][] rolesAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServerSingle, source,
                instance);
    }

    @DataProvider(name = "rolesNotAllowedToUnassignSampleFromExperiment")
    RoleWithHierarchy[][] rolesNotAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServerSingle),
                source, instance);
    }

    @DataProvider(name = "rolesAllowedToUnshareSample")
    RoleWithHierarchy[][] rolesAllowedToUnshareSample()
    {
        return RolePermutator.getAcceptedPermutations(instanceEtlServer, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToUnshareSample")
    RoleWithHierarchy[][] rolesNotAllowedToUnshareSample()
    {
        return RolePermutator
                .getAcceptedPermutations(not(instanceEtlServer), destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignSampleToSpace")
    RoleWithHierarchy[][] rolesAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignSampleToSpace")
    RoleWithHierarchy[][] rolesNotAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToShareSample")
    RoleWithHierarchy[][] rolesAllowedToShareSample()
    {
        return RolePermutator.getAcceptedPermutations(instanceEtlServer, source, instance);
    }

    @DataProvider(name = "rolesNotAllowedToShareSample")
    RoleWithHierarchy[][] rolesNotAllowedToShareSample()
    {
        return RolePermutator.getAcceptedPermutations(not(instanceEtlServer), source, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignDataSetToExperiment")
    RoleWithHierarchy[][] rolesAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignDataSetToExperiment")
    RoleWithHierarchy[][] rolesNotAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignDataSetToSample")
    RoleWithHierarchy[][] rolesAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignDataSetToSample")
    RoleWithHierarchy[][] rolesNotAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAddParentToSample")
    RoleWithHierarchy[][] rolesAllowedToAddParentToSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceUserOrBetter, source, destination,
                instance);
    }

    @DataProvider(name = "rolesNotAllowedToAddParentToSample")
    RoleWithHierarchy[][] rolesNotAllowedToAddParentToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceUserOrBetter), source, destination,
                instance);
    }

    @DataProvider(name = "rolesAllowedRemoveParentFromSample")
    RoleWithHierarchy[][] rolesAllowedToRemoveParentFromSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToRemoveParentFromSample")
    RoleWithHierarchy[][] rolesNotAllowedToRemoveParentFromSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAddContainerToSample")
    RoleWithHierarchy[][] rolesAllowedToAddContainerToSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAddContainerToSample")
    RoleWithHierarchy[][] rolesNotAllowedToAddContainerToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedRemoveContainerFromSample")
    RoleWithHierarchy[][] rolesAllowedToRemoveContainerFromSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServerSingle, source,
                instance);
    }

    @DataProvider(name = "rolesNotAllowedToRemoveContainerFromSample")
    RoleWithHierarchy[][] rolesNotAllowedToRemoveContainerFromSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServerSingle),
                source, instance);
    }

    @DataProvider(name = "rolesAllowedToAddParentToDataSet")
    RoleWithHierarchy[][] rolesAllowedToAddParentToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAddParentToDataSet")
    RoleWithHierarchy[][] rolesNotAllowedToAddParentToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedRemoveParentFromDataSet")
    RoleWithHierarchy[][] rolesAllowedToRemoveParentFromDataSet()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToRemoveParentFromDataSet")
    RoleWithHierarchy[][] rolesNotAllowedToRemoveParentFromDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAddContainerToDataSet")
    RoleWithHierarchy[][] rolesAllowedToAddContainerToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAddContainerToDataSet")
    RoleWithHierarchy[][] rolesNotAllowedToAddContainerToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer), source,
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedRemoveContainerFromDataSet")
    RoleWithHierarchy[][] rolesAllowedToRemoveContainerFromDataSet()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServerSingle, source,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToRemoveContainerFromDataSet")
    RoleWithHierarchy[][] rolesNotAllowedToRemoveContainerFromDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServerSingle),
                source, instance);
    }

    ExperimentPE pe(Experiment experiment)
    {
        return daoFactory.getExperimentDAO().tryGetByPermID(experiment.getPermId());
    }

    ProjectPE pe(Project project)
    {
        return daoFactory.getProjectDAO().tryFindProject(project.getSpace().getCode(),
                project.getCode());
    }

    SpacePE pe(Space space)
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCode(space.getCode());
    }

    SamplePE pe(Sample sample)
    {
        return daoFactory.getSampleDAO().tryToFindByPermID(sample.getPermId());
    }

    DataPE pe(AbstractExternalData data)
    {
        return daoFactory.getDataDAO().tryToFindDataSetByCode(data.getCode());
    }
}
