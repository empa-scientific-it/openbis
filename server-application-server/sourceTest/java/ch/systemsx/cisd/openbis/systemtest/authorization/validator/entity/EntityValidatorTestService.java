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

package ch.systemsx.cisd.openbis.systemtest.authorization.validator.entity;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.EntityHistoryValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.MatchingEntityValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;

/**
 * @author pkupczyk
 */
@Component
public class EntityValidatorTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = EntityHistoryValidator.class)
    public EntityHistory testEntityHistoryValidator(IAuthSessionProvider sessionProvider, EntityHistory history)
    {
        return history;
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = MatchingEntityValidator.class)
    public List<MatchingEntity> testMatchingEntityValidator(IAuthSessionProvider sessionProvider, MatchingEntity matchingEntity)
    {
        return Collections.singletonList(matchingEntity);
    }

}
