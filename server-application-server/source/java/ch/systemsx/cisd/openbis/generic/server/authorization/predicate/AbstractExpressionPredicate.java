/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractExpressionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractGridExpressionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * An <code>IPredicate</code> implementation based on {@link AbstractGridExpressionPE} of a grid custom filter or column. Public internal class
 * provide predicates for deletions based on {@link TechId} and updates base on {@link IExpressionUpdates}.
 * 
 * @author Piotr Buczek
 * @author Tomasz Pylak
 */

abstract public class AbstractExpressionPredicate<T> extends AbstractPredicate<T>
{

    public static class DeleteGridCustomFilterPredicate extends
            AbstractExpressionPredicate<TechId>
    {
        public DeleteGridCustomFilterPredicate()
        {
            super("delete grid custom filter");
        }

        @Override
        public AbstractExpressionPE<?> convert(TechId techId)
        {
            return authorizationDataProvider.getGridCustomFilter(techId);
        }
    }

    public static class DeleteGridCustomColumnPredicate extends
            AbstractExpressionPredicate<TechId>
    {
        public DeleteGridCustomColumnPredicate()
        {
            super("delete grid custom column");
        }

        @Override
        public AbstractExpressionPE<?> convert(TechId techId)
        {
            return authorizationDataProvider.getGridCustomColumn(techId);
        }
    }

    public static class UpdateGridCustomFilterPredicate extends
            AbstractExpressionPredicate<IExpressionUpdates>
    {
        public UpdateGridCustomFilterPredicate()
        {
            super("update grid custom filter");
        }

        @Override
        public AbstractExpressionPE<?> convert(IExpressionUpdates criteria)
        {
            TechId techId = TechId.create(criteria);
            return authorizationDataProvider.getGridCustomFilter(techId);
        }
    }

    public static class UpdateGridCustomColumnPredicate extends
            AbstractExpressionPredicate<IExpressionUpdates>
    {
        public UpdateGridCustomColumnPredicate()
        {
            super("update grid custom column");
        }

        @Override
        public AbstractExpressionPE<?> convert(IExpressionUpdates criteria)
        {
            TechId techId = TechId.create(criteria);
            return authorizationDataProvider.getGridCustomColumn(techId);
        }
    }

    abstract protected AbstractExpressionPE<?> convert(T value);

    private final String description;

    protected IAuthorizationDataProvider authorizationDataProvider;

    public AbstractExpressionPredicate(String description)
    {
        this.description = description;
    }

    //
    // AbstractPredicate
    //

    @Override
    public final String getCandidateDescription()
    {
        return description;
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        this.authorizationDataProvider = provider;
    }

    @Override
    protected final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final T value)
    {
        AbstractExpressionPE<?> gridExpression = convert(value);
        final boolean matching = isMatching(person, gridExpression);
        if (matching)
        {
            return Status.OK;
        }
        String userId = person.getUserId();
        return Status.createError(createErrorMsg(gridExpression, userId));
    }

    private static boolean isMatching(PersonPE person, AbstractExpressionPE<?> gridExpression)
    {
        // needs to be an instance admin in filter database instance or registrator of a filter
        return isInstanceAdmin(person) || isRegistrator(person, gridExpression);
    }

    private String createErrorMsg(AbstractExpressionPE<?> gridExpression, String userId)
    {
        return String.format("User '%s' does not have enough privileges to"
                + " perform " + description + " '%s'. "
                + "One needs to be either registrator or database instance admin.", userId,
                gridExpression);
    }

    private static boolean isRegistrator(final PersonPE person,
            final AbstractExpressionPE<?> gridExpression)
    {
        return person.equals(gridExpression.getRegistrator());
    }

    private static boolean isInstanceAdmin(final PersonPE person)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            if (roleAssignment.getRoleWithHierarchy().isInstanceLevel())
            {
                return true;
            }
        }
        return false;
    }
}
