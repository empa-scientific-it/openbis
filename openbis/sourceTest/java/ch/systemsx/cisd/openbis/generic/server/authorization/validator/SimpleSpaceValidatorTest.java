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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SimpleSpaceValidatorTest extends CommonValidatorTest<Space>
{

    @Override
    protected Space createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        Space space = new Space();
        space.setCode(spacePE.getCode());
        return space;
    }

    @Override
    protected boolean validateObject(PersonPE personPE, Space object)
    {
        SimpleSpaceValidator validator = new SimpleSpaceValidator();
        validator.init(provider);
        return validator.isValid(personPE, object);
    }

    @Override
    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, boolean result)
    {
        assertFalse(result);
    }

}
