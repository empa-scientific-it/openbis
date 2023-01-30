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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.sample;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ExistingSampleIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ListSampleCriteriaPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ListSamplesByPropertyPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewSamplesWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleAugmentedCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdentifierCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleListPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePEOrNullPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePermIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePermIdStringPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SamplePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdCollectionReadWritePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleTechIdReadWritePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author pkupczyk
 */
@Component
public class SamplePredicateTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleIdentifierPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleIdentifierPredicate.class) SampleIdentifier sampleIdentifier)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testExistingSampleIdentifierPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ExistingSampleIdentifierPredicate.class) SampleIdentifier sampleIdentifier)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleTechIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleTechId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleTechIdReadWritePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleTechIdReadWritePredicate.class) TechId sampleTechId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleAugmentedCodePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleAugmentedCodePredicate.class) String sampleAugmentedCode)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSamplePEPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SamplePEPredicate.class) SamplePE samplePE)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleListPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleListPredicate.class) List<Sample> sampleList)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSamplePredicate(IAuthSessionProvider sessionProvider, @AuthorizationGuard(guardClass = SamplePredicate.class) Sample sample)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSamplePermIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SamplePermIdPredicate.class) PermId samplePermId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleTechIdCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleTechIdCollectionPredicate.class) List<TechId> sampleTechIds)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleTechIdCollectionReadWritePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleTechIdCollectionReadWritePredicate.class) List<TechId> sampleTechIds)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleIdentifierCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleIdentifierCollectionPredicate.class) List<SampleIdentifier> sampleIdentifiers)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testListSampleCriteriaPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ListSampleCriteriaPredicate.class) ListSampleCriteria listSampleCriteria)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleIdPredicate.class) ISampleId sampleId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleUpdatesPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class) SampleUpdatesDTO sampleUpdatesDTO)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testNewSamplePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class) NewSample newSample)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testNewSamplesWithTypePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class) NewSamplesWithTypes newSamplesWithTypes)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSamplePEOrNullPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SamplePEOrNullPredicate.class) SamplePE samplePE)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSampleUpdatesCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SampleUpdatesCollectionPredicate.class) List<SampleUpdatesDTO> sampleUpdatesDTOs)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testListSamplesByPropertyPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = ListSamplesByPropertyPredicate.class) ListSamplesByPropertyCriteria listSamplesByPropertyCriteria)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testSamplePermIdStringPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = SamplePermIdStringPredicate.class) String samplePermIdString)
    {
    }

}
