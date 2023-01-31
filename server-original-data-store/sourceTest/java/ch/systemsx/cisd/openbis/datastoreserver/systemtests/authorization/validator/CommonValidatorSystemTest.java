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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.CommonAuthorizationSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public abstract class CommonValidatorSystemTest<O> extends CommonAuthorizationSystemTest
{

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE, Object param);

    protected abstract O validateObject(ProjectAuthorizationUser user, O object, Object param);

    protected CommonValidatorSystemTestAssertions<O> getAssertions()
    {
        return new CommonValidatorSystemTestAssertionsDefault<O>();
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithNull(ProjectAuthorizationUser user, Object param)
    {
        ValidationResult result = tryValidateObject(user, null, param);
        getAssertions().assertWithNullObject(user, result.getResult(), result.getError(), param);
    }

    protected void assertWithNull(ProjectAuthorizationUser user, O result, Throwable t, Object param)
    {
        assertNull(result);
        assertNoException(t);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithProject11Object(ProjectAuthorizationUser user, Object param)
    {
        ValidationResult result = tryValidateObject(user, createObject(getSpace1(), getProject11(), param), param);
        getAssertions().assertWithProject11Object(user, result.getResult(), result.getError(), param);
    }

    @Test(dataProvider = PERSON_AND_PARAM_PROVIDER, groups = GROUP_PA_TESTS)
    public void testWithProject21Object(ProjectAuthorizationUser user, Object param)
    {
        ValidationResult result = tryValidateObject(user, createObject(getSpace2(), getProject21(), param), param);
        getAssertions().assertWithProject21Object(user, result.getResult(), result.getError(), param);
    }

    protected ValidationResult tryValidateObject(ProjectAuthorizationUser user, O object, Object param)
    {
        try
        {
            O result = validateObject(user, object, param);
            return new ValidationResult(result);
        } catch (Throwable t)
        {
            return new ValidationResult(t);
        }
    }

    private class ValidationResult
    {

        private O result;

        private Throwable error;

        public ValidationResult(O result)
        {
            this.result = result;
        }

        public ValidationResult(Throwable error)
        {
            this.error = error;
        }

        public O getResult()
        {
            return result;
        }

        public Throwable getError()
        {
            return error;
        }
    }

}
