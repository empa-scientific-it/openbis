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
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.IPredicateFactory;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Performs some predicate which returns a {@link Status} based on the input objects.
 * <p>
 * Each implementation is expected to have an empty <code>public</code> constructor and might be statefull. So do not try to reuse it. Use a
 * {@link IPredicateFactory} to get an implementation.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IPredicate<T>
{

    /**
     * Evaluates given <var>valueOrNull</var>.
     * <p>
     * This method could be called more than once.
     * </p>
     * 
     * @param allowedRoles the person roles that matches the ones required by the method (or task).
     */
    public Status evaluate(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final T valueOrNull) throws UserFailureException;

    /**
     * Initialises this predicate with data provided by the specified provider.
     * <p>
     * This method prepares the {@link IPredicate} and is ensured to be called only once.
     * </p>
     */
    public void init(IAuthorizationDataProvider provider);
}
