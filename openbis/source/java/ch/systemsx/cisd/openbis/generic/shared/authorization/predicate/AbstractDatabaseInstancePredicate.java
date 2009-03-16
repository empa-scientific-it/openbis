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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.DatabaseInstanceIdentifierHelper;

/**
 * An {@link AbstractPredicate} extension which works with database instances.
 * 
 * @author Christian Ribeaud
 */
abstract class AbstractDatabaseInstancePredicate<T> extends AbstractPredicate<T>
{
    static final String STATUS_MESSAGE_PREFIX_FORMAT =
            "User '%s' does not have enough privileges to ";

    protected IAuthorizationDataProvider authorizationDataProvider;

    boolean inited;

    protected DatabaseInstancePE getDatabaseInstance(final DatabaseInstanceIdentifier identifier)
    {
        return DatabaseInstanceIdentifierHelper.getDatabaseInstance(identifier,
                authorizationDataProvider);
    }

    //
    // AbstractPredicate
    //

    public void init(IAuthorizationDataProvider provider)
    {
        assert inited == false : "Already initialized";
        this.authorizationDataProvider = provider;
        inited = true;
    }

}
