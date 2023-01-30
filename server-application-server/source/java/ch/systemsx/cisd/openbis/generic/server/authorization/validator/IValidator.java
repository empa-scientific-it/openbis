/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.ValidatorStore;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Performs some validation which returns <code>true</code> or <code>false</code> based on the input objects.
 * <p>
 * Each implementation is expected to have an empty <code>public</code> constructor and is expected to be stateless. Use {@link ValidatorStore} to get
 * an implementation.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IValidator<T>
{

    /**
     * Validates given <var>value</var> for given <var>person</var>.
     */
    public boolean isValid(final PersonPE person, final T value);

    /**
     * initializes validator with authorization data provider.
     */
    public void init(IAuthorizationDataProvider authorizationDataProvider);

}
