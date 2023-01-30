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
 * An <code>AbstractPredicate</code> extension which delegates its method calls to the encapsulated {@link IPredicate}.
 * <p>
 * Each implementation should know how to convert <code>T</code> to <code>P</code> by implementing {@link #tryConvert(Object)} method. Note that
 * {@link #doEvaluation(PersonPE, List, Object)} delegates its call to {@link IPredicate#evaluate(PersonPE, List, Object)} of the specified
 * <code>delegate</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class DelegatedPredicate<P, T> extends AbstractPredicate<T>
{
    private final IPredicate<P> delegate;

    boolean initialized;

    protected IAuthorizationDataProvider authorizationDataProvider;

    public DelegatedPredicate(final IPredicate<P> delegate)
    {
        this.delegate = delegate;
    }

    /**
     * Converts given <var>value</var> to type needed. Returns null if no authorization is needed.
     */
    public abstract P tryConvert(final T value);

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        assert initialized == false;
        this.authorizationDataProvider = provider;
        delegate.init(provider);
        initialized = true;
    }

    @Override
    public Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T value)
    {
        P convertedValue = tryConvert(value);
        if (convertedValue != null)
        {
            return delegate.evaluate(person, allowedRoles, convertedValue);
        } else
        {
            return Status.OK;
        }
    }
}
