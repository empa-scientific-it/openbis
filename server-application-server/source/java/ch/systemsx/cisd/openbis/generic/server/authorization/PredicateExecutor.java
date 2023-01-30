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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ShouldFlattenCollections;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.ArrayPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.CollectionPredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.IPredicate;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A static class to execute {@link IPredicate}.
 * 
 * @author Christian Ribeaud
 */
public final class PredicateExecutor
{
    private IPredicateFactory predicateFactory;

    private IAuthorizationDataProvider authorizationDataProvider;

    /**
     * Sets the <code>IPredicateProvider</code> implementation.
     */
    final void setPredicateFactory(final IPredicateFactory predicateProvider)
    {
        predicateFactory = predicateProvider;
    }

    /**
     * Sets the {@link IAuthorizationDAOFactory} implementation.
     */
    final void setDAOFactory(final IAuthorizationDAOFactory daoFactory)
    {
        authorizationDataProvider = new AuthorizationDataProvider(daoFactory);
    }

    final void setAuthorizationDataProvider(IAuthorizationDataProvider authorizationDataProvider)
    {
        this.authorizationDataProvider = authorizationDataProvider;
    }

    /**
     * Creates, casts and ensures that the returned {@link IPredicate} is not <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    private final <T> IPredicate<T> createPredicate(
            final Class<? extends IPredicate<?>> predicateClass)
    {
        assert predicateFactory != null : "Unspecified predicate factory";
        Class<? extends IPredicate<T>> predicateClassT = (Class<? extends IPredicate<T>>) predicateClass;
        final IPredicate<?> predicate = predicateFactory.createPredicateForClass(predicateClassT);
        assert predicate != null : "Predicate factory should not return a null predicate";
        return (IPredicate<T>) predicate;
    }

    @SuppressWarnings("unchecked")
    private final static <T> T[] castToArray(final T argumentValue)
    {
        return (T[]) argumentValue;
    }

    @SuppressWarnings("unchecked")
    private final static <T> List<T> castToList(final T argumentValue)
    {
        return (List<T>) argumentValue;
    }

    /**
     * Finds out and executes the appropriate {@link IPredicate} for given <var>argument</var>.
     */
    public final <T> Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final Argument<T> argument)
    {
        assert person != null : "Person unspecified";
        assert allowedRoles != null : "Allowed roles not specified";
        assert argument != null : "Argument not specified";
        final T argumentValue = argument.tryGetArgument();
        final Class<? extends IPredicate<?>> predicateClass =
                argument.getPredicateCandidate().guardClass();
        final Class<T> argumentType = argument.getType();
        ShouldFlattenCollections flattenAnnotation =
                predicateClass.getAnnotation(ShouldFlattenCollections.class);
        boolean shouldFlattenCollections =
                (null == flattenAnnotation) ? true : flattenAnnotation.value();
        return evaluate(person, allowedRoles, argumentValue, predicateClass, argumentType,
                shouldFlattenCollections);
    }

    @Private
    final <T> Status evaluate(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final T argumentValue,
            final Class<? extends IPredicate<?>> predicateClass, final Class<T> argumentType,
            final boolean shouldFlattenCollections)
    {
        assert authorizationDataProvider != null : "Authorization data provider not set";
        final IPredicate<T> predicate = createPredicate(predicateClass);
        if (List.class.isAssignableFrom(argumentType) && shouldFlattenCollections)
        {
            final CollectionPredicate<T> collectionPredicate =
                    new CollectionPredicate<T>(predicate);
            collectionPredicate.init(authorizationDataProvider);
            final List<T> list = castToList(argumentValue);
            try
            {
                return collectionPredicate.evaluate(person, allowedRoles, list);
            } catch (final ClassCastException e)
            {
                throw new IllegalArgumentException(String.format("Given predicate class '%s' "
                        + "and list type '%s' are not compatible.", predicateClass.getName(), list
                        .get(0).getClass().getName()));
            }
        }
        if (argumentType.isArray() && shouldFlattenCollections)
        {
            final ArrayPredicate<T> arrayPredicate = new ArrayPredicate<T>(predicate);
            arrayPredicate.init(authorizationDataProvider);
            try
            {
                return arrayPredicate.evaluate(person, allowedRoles, castToArray(argumentValue));
            } catch (final ClassCastException e)
            {
                throw new IllegalArgumentException(String.format("Given predicate class '%s' "
                        + "and array type '%s' are not compatible.", predicateClass.getName(),
                        argumentType.getComponentType().getName()));
            }
        }
        predicate.init(authorizationDataProvider);
        try
        {
            return predicate.evaluate(person, allowedRoles, argumentValue);
        } catch (final ClassCastException e)
        {
            throw new IllegalArgumentException(String.format("Given predicate class '%s' "
                    + "and argument type '%s' are not compatible.", predicateClass.getName(),
                    argumentType.getName()));
        }
    }
}
