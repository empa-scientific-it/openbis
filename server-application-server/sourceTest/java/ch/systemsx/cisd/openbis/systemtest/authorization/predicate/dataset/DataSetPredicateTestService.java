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

package ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataPEPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewDataSetsWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.NewExternalDataPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * @author pkupczyk
 */
@Component
public class DataSetPredicateTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDataSetTechIdPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId dataSetTechId)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDataSetTechIdCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DataSetTechIdCollectionPredicate.class) List<TechId> dataSetTechIds)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDataSetCodeCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDataSetCodePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String dataSetCode)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDataSetUpdatesPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class) DataSetUpdatesDTO dataSetUpdates)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDataSetUpdatesCollectionPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DataSetUpdatesCollectionPredicate.class) List<DataSetUpdatesDTO> dataSetUpdates)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testDataPEPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = DataPEPredicate.class) DataPE dataPE)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testNewDataSetsWithTypePredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = NewDataSetsWithTypePredicate.class) NewDataSetsWithTypes newDataSetsWithTypes)
    {
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    public void testNewExternalDataPredicate(IAuthSessionProvider sessionProvider,
            @AuthorizationGuard(guardClass = NewExternalDataPredicate.class) NewExternalData newExternalData)
    {
    }

}
