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

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation based on a {@link Collection}.
 * 
 * @author Christian Ribeaud
 */
public class CollectionPredicate<T> extends AbstractPredicate<Collection<T>>
{
    private final IPredicate<T> predicate;

    public CollectionPredicate(final IPredicate<T> predicate)
    {
        this.predicate = predicate;
    }

    protected Status itemEvaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T item)
    {
        return predicate.evaluate(person, allowedRoles, item);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        predicate.init(provider);
    }

    @Override
    protected final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final Collection<T> value)
    {
        IServiceConversationProgressListener listener =
                ServiceConversationsThreadContext.getProgressListener();

        int index = 0;

        for (final T item : value)
        {
            final Status status = itemEvaluate(person, allowedRoles, item);
            if (status.getFlag().equals(StatusFlag.OK) == false)
            {
                return status;
            }
            listener.update("authorize", value.size(), ++index);
        }
        return Status.OK;
    }

    @Override
    public final String getCandidateDescription()
    {
        if (predicate instanceof AbstractPredicate<?>)
        {
            return ((AbstractPredicate<?>) predicate).getCandidateDescription();
        }
        return "collection";
    }
}
