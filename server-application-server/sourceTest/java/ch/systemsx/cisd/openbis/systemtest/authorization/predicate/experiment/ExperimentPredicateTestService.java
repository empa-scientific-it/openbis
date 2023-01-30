/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.experiment;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ExperimentTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentListPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentPEOrNullPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentPermIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExperimentUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExperimentsWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.UpdatedExperimentsWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentPredicateTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentTechIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentTechId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentTechIdCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentTechIdCollectionPredicate.class) List<TechId> experimentTechIds)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentPermIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentPermIdPredicate.class) PermId experimentPermId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentPEPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentPEPredicate.class) ExperimentPE experimentPE)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentPEOrNullPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentPEOrNullPredicate.class) ExperimentPE experimentPE)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentListPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentListPredicate.class) List<Experiment> experimentList)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentAugmentedCodePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentAugmentedCodePredicate.class) String experimentAugmentedCode)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testNewExperimentPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class) NewExperiment newExperiment)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentIdPredicate.class) IExperimentId experimentId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExperimentUpdatesPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExperimentUpdatesPredicate.class) ExperimentUpdatesDTO experimentUpdates)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testNewExperimentsWithTypePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = NewExperimentsWithTypePredicate.class) NewExperimentsWithType experimentsWithType)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testUpdatedExperimentsWithTypePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = UpdatedExperimentsWithTypePredicate.class) UpdatedExperimentsWithType experimentsWithType)
    {
    }

}
