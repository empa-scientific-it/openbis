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

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class CommonValidatorSystemTestAssertionsDelegate<O> extends CommonValidatorSystemTestAssertions<O>
{

    protected CommonValidatorSystemTestAssertions<O> delegate;

    public CommonValidatorSystemTestAssertionsDelegate(CommonValidatorSystemTestAssertions<O> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public void assertWithNullObject(ProjectAuthorizationUser user, O result, Throwable error, Object param)
    {
        delegate.assertWithNullObject(user, result, error, param);
    }

    @Override
    public void assertWithProject11Object(ProjectAuthorizationUser user, O result, Throwable t, Object param)
    {
        delegate.assertWithProject11Object(user, result, t, param);
    }

    @Override
    public void assertWithProject21Object(ProjectAuthorizationUser user, O result, Throwable t, Object param)
    {
        delegate.assertWithProject21Object(user, result, t, param);
    }

}
