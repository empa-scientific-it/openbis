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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>AbstractPredicate</code> extension which delegates its method calls to the encapsulated {@link AbstractPredicate} allowing the value to be
 * null.
 * 
 * @author Piotr Buczek
 */
abstract class DelegatedNullableAbstractPredicate<T> extends AbstractPredicate<T>
{
    private final AbstractPredicate<T> delegate;

    public DelegatedNullableAbstractPredicate(AbstractPredicate<T> delegate)
    {
        this.delegate = delegate;
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        delegate.init(provider);
    }

    @Override
    protected boolean isNullValueAllowed()
    {
        return true;
    }

    @Override
    public final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T valueOrNull)
    {
        if (valueOrNull == null)
        {
            return Status.OK;
        } else
        {
            return delegate.doEvaluation(person, allowedRoles, valueOrNull);
        }
    }

}
