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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.entity;

import java.util.List;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.entity.EntityValidatorTestService;

/**
 * @author pkupczyk
 */
public abstract class MatchingEntityValidatorSystemTest extends CommonValidatorSystemTest<MatchingEntity>
{

    @Override
    protected MatchingEntity validateObject(ProjectAuthorizationUser user, MatchingEntity object, Object param)
    {
        List<MatchingEntity> list = getBean(EntityValidatorTestService.class).testMatchingEntityValidator(user.getSessionProvider(), object);
        return list != null && list.size() == 1 ? list.get(0) : null;
    }

}
