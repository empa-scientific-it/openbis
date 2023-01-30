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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.object.IObjectsProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <i>abstract</i> <code>IValidator</code> implementation which mainly checks method parameters before doing the real work.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractValidator<T> implements IValidator<T>
{

    protected IAuthorizationDataProvider authorizationDataProvider;

    /**
     * Does the validation after parameters have been checked.
     * <p>
     * Must be implemented by subclasses.
     * </p>
     */
    public abstract boolean doValidation(final PersonPE person, final T value);

    //
    // IValidator
    //

    @Override
    public final boolean isValid(final PersonPE person, final T value)
    {
        assert person != null : "Unspecified person";
        assert value != null : "Unspecified value";
        return doValidation(person, value);
    }

    protected <O> boolean isValidPA(PersonPE person, IObjectsProvider<O> provider)
    {
        IProjectAuthorization<O> pa = new ProjectAuthorizationBuilder<O>()
                .withData(authorizationDataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromPersonPE(person))
                .withObjects(provider)
                .build();

        return pa.getObjectsWithoutAccess().isEmpty();
    }

    @Override
    public void init(
            @SuppressWarnings("hiding") IAuthorizationDataProvider authorizationDataProvider)
    {
        this.authorizationDataProvider = authorizationDataProvider;
    }

}
